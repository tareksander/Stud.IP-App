package org.studip.unofficial_app.documentsprovider;

import android.annotation.SuppressLint;
import android.app.AuthenticationRequiredException;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;
import androidx.lifecycle.Transformations;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.jetbrains.annotations.NotNull;
import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.api.rest.StudipFolder;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.Settings;
import org.studip.unofficial_app.model.SettingsProvider;
import org.studip.unofficial_app.model.room.DB;
import org.studip.unofficial_app.ui.ServerSelectActivity;
import org.studip.unofficial_app.ui.fragments.FileFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.provider.DocumentsContract.Document;
import static android.provider.DocumentsContract.Root;

public class DocumentsProvider extends android.provider.DocumentsProvider
{
    public static final String AUTHORITIES = "org.studip.unofficial_app.documents";
    
    private static final String[] DEFAULT_ROOT_PROJECTION =
            new String[]{Root.COLUMN_ROOT_ID, Root.COLUMN_MIME_TYPES,
                    Root.COLUMN_FLAGS, Root.COLUMN_ICON, Root.COLUMN_TITLE,
                    Root.COLUMN_SUMMARY, Root.COLUMN_DOCUMENT_ID,
                    Root.COLUMN_AVAILABLE_BYTES};
    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new
            String[]{Document.COLUMN_DOCUMENT_ID, Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME, Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS, Document.COLUMN_SIZE};
    
    
    private final HashMap<String,Integer> openFiles = new HashMap<>();
    
    
    
    private final String DOCS_LOCK = "docs_lock";
    private DocumentsDB docs;
    
    
    private volatile Handler fileHandler = null;
    
    @SuppressLint("CheckResult")
    private void checkDB() {
        //System.out.println("checking doc database");
        synchronized (DOCS_LOCK) {
            if (docs == null) {
                //System.out.println("initializing documents database");
                docs = DocumentsDBProvider.getDB(getContext());
                Transformations.distinctUntilChanged(docs.documents().observeRoots()).observeForever(roots -> {
                    //System.out.println("roots updated");
                    getContext().getContentResolver().notifyChange(DocumentsContract.buildRootsUri(AUTHORITIES), null);
                });
                {
                    // TODO own files only show after restarting the app when the document provider is first enabled
                    API api = APIProvider.getAPI(getContext());
                    if (api != null && api.getUserID() != null) {
                        docs.documents().isInRoots(api.getUserID()).subscribeOn(Schedulers.io()).subscribe((documentRoots, throwable) -> {
                            if (throwable == null && documentRoots.length == 0) {
                                //System.out.println("user not in roots");
                                api.user.userFolder(api.getUserID()).enqueue(new Callback<StudipFolder>()
                                {
                                    @Override
                                    public void onResponse(@NotNull Call<StudipFolder> call, @NotNull Response<StudipFolder> response) {
                                        //System.out.println("got user folder");
                                        StudipFolder f = response.body();
                                        if (f != null && f.id != null && ! f.id.equals("")) {
                                            DocumentRoot r = new DocumentRoot(f.id);
                                            r.user = true;
                                            r.parentID = api.getUserID();
                                            r.enabled = true;
                                            docs.documents().updateInsertAsync(r).subscribeOn(Schedulers.io()).subscribe();
                                            //System.out.println("user inserted into roots");
                                        }
                                    }
                                    @Override
                                    public void onFailure(@NotNull Call<StudipFolder> call, @NotNull Throwable t) {}
                                });
                            }
                        });
                    }
                }
                DB db = DBProvider.getDB(getContext());
                Transformations.distinctUntilChanged(db.courseDao().observeDocumentsCourses()).observeForever(courses -> {
                    for (StudipCourse c : courses) {
                        docs.documents().isInRoots(c.course_id).subscribeOn(Schedulers.io()).subscribe((roots, throwable) -> {
                            if (throwable == null && roots.length == 0) {
                                API api = APIProvider.getAPI(getContext());
                                if (api != null && api.getUserID() != null) {
                                    api.course.folder(c.course_id).enqueue(new Callback<StudipFolder>()
                                    {
                                        @Override
                                        public void onResponse(@NotNull Call<StudipFolder> call, @NotNull Response<StudipFolder> response) {
                                            StudipFolder f = response.body();
                                            if (f != null && f.id != null && ! f.id.equals("")) {
                                                DocumentRoot r = new DocumentRoot(f.id);
                                                r.title = c.title;
                                                r.user = false;
                                                r.parentID = c.course_id;
                                                r.enabled = true;
                                                docs.documents().updateInsertAsync(r).subscribeOn(Schedulers.io()).subscribe();
                                            }
                                        }
                                        @Override
                                        public void onFailure(@NotNull Call<StudipFolder> call, @NotNull Throwable t) {}
                                    });
                                }
                            }
                        });
                    }
                });
            }
        }
    }
    
    private void addFileToCursor(MatrixCursor.RowBuilder b, StudipFolder.FileRef ref, boolean thumbnail, API api) {
        int docflags = 0;
        int writableflags = Document.FLAG_SUPPORTS_DELETE | Document.FLAG_SUPPORTS_RENAME | Document.FLAG_SUPPORTS_WRITE;
        if (Build.VERSION.SDK_INT >= 24) {
            docflags |= Document.FLAG_SUPPORTS_COPY;
            writableflags |= Document.FLAG_SUPPORTS_MOVE | Document.FLAG_SUPPORTS_REMOVE;
        }
        b.add(Document.COLUMN_DOCUMENT_ID, ref.id);
        b.add(Document.COLUMN_DISPLAY_NAME, ref.name);
        int flags = docflags;
        String mime = "*/*";
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String[] parts = ref.name.split("\\.");
        if (parts.length != 0) {
            if (map.hasExtension(parts[parts.length-1])) {
                mime = map.getMimeTypeFromExtension(parts[parts.length-1]);
                if (mime.contains("image/") && thumbnail) {
                    flags |= Document.FLAG_SUPPORTS_THUMBNAIL;
                }
            }
        }
        b.add(Document.COLUMN_MIME_TYPE, mime);
        
        if (ref.is_writable) {
            flags |= writableflags;
        }
        b.add(Document.COLUMN_FLAGS, flags);
        b.add(Document.COLUMN_SIZE, ref.size);
        b.add(Document.COLUMN_LAST_MODIFIED, ref.chdate*1000);
    }
    
    private void addFolderToCursor(MatrixCursor.RowBuilder b, Object f) {
        boolean is_writable;
        String id;
        String name;
        String folder_type;
        long chdate;
        if (f instanceof StudipFolder) {
            StudipFolder folder = (StudipFolder) f;
            is_writable = folder.is_writable;
            id = folder.id;
            name = folder.name;
            folder_type = folder.folder_type;
            chdate = folder.chdate;
        } else {
            if (f instanceof StudipFolder.SubFolder) {
                StudipFolder.SubFolder folder = (StudipFolder.SubFolder) f;
                is_writable = folder.is_writable;
                id = folder.id;
                name = folder.name;
                folder_type = folder.folder_type;
                chdate = folder.chdate;
            } else {
                return;
            }
        }
        int docflags = 0;
        int writableflags = Document.FLAG_SUPPORTS_DELETE | Document.FLAG_SUPPORTS_RENAME | Document.FLAG_SUPPORTS_WRITE;
        if (Build.VERSION.SDK_INT >= 24) {
            docflags |= Document.FLAG_SUPPORTS_COPY;
            writableflags |= Document.FLAG_SUPPORTS_MOVE | Document.FLAG_SUPPORTS_REMOVE;
        }
        b.add(Document.COLUMN_DOCUMENT_ID, id);
        if ("".equals(folder_type)) {
            b.add(Document.COLUMN_DISPLAY_NAME, getContext().getString(R.string.own_files));
        } else {
            b.add(Document.COLUMN_DISPLAY_NAME, name);
        }
        b.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
        int flags = docflags;
        if (is_writable) {
            //System.out.println("writeable");
            //System.out.println(name);
            flags |= Document.FLAG_DIR_SUPPORTS_CREATE | writableflags;
        }
        b.add(Document.COLUMN_FLAGS, flags);
        b.add(Document.COLUMN_SIZE, null);
        b.add(Document.COLUMN_LAST_MODIFIED, chdate*1000);
    }
    
    
    // deletes recursively, revoking document permissions for all children before deleting
    private void deleteRecursive(String documentId, API api, boolean first) throws FileNotFoundException {
        revokeDocumentPermission(documentId);
        boolean file = false;
        try {
            StudipFolder f = api.folder.get(documentId).execute().body();
            if (f == null) {
                file = true;
                throw new FileNotFoundException();
            }
            for (StudipFolder.SubFolder sub : f.subfolders) {
                deleteRecursive(sub.id, api, false);
            }
            if (first) {
                if (api.folder.delete(documentId).execute().code() != 200) {
                    throw new FileNotFoundException();
                }
                getContext().getContentResolver().notifyChange(DocumentsContract.buildChildDocumentsUri(AUTHORITIES, f.parent_id), null);
                getContext().getContentResolver().notifyChange(DocumentsContract.buildDocumentUri(AUTHORITIES, f.id), null);
            }
        } catch (Exception ignored) {
            // maybe it is a file
            try {
                if (file) {
                    StudipFolder.FileRef f = api.file.get(documentId).execute().body();
                    if (f == null || api.file.delete(documentId).execute().code() != 200) {
                        throw new FileNotFoundException();
                    }
                    getContext().getContentResolver().notifyChange(DocumentsContract.buildChildDocumentsUri(AUTHORITIES, f.folder_id), null);
                    getContext().getContentResolver().notifyChange(DocumentsContract.buildDocumentUri(AUTHORITIES, documentId), null);
                    // delete the thumbnails, if it was there
                    new File(getContext().getCacheDir()+"/thumbnail-"+documentId).delete();
                }
            }
            catch (IOException e) {
                throw new FileNotFoundException();
            }
        }
        
    }
    
    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024*1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
    
    
    @NotNull
    public API APICheckLogin() throws FileNotFoundException, AuthenticationRequiredException {
        API api = APIProvider.getAPI(getContext());
        if (api == null || api.getUserID() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent i = new Intent(getContext(), ServerSelectActivity.class);
                //i.putExtra(getContext().getPackageName()+".login",true);
                throw new AuthenticationRequiredException(null,PendingIntent.getActivity(getContext(),0, i,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT));
            } else {
                throw new FileNotFoundException();
            }
        }
        return api;
    }
    
    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        checkDB();
        Settings s = SettingsProvider.getSettings(getContext());
        if (! s.documents_provider) {
            throw new FileNotFoundException();
        }
        //System.out.println("getting roots");
        
        final MatrixCursor result = new MatrixCursor((projection != null) ? projection : DEFAULT_ROOT_PROJECTION);
        
        DocumentRoot[] roots = docs.documents().getRoots();
        for (DocumentRoot r : roots) {
            if (! r.enabled) {
                continue;
            }
            MatrixCursor.RowBuilder row = result.newRow();
            row.add(Root.COLUMN_ROOT_ID, r.parentID);
            if (r.user) {
                row.add(Root.COLUMN_ICON, R.drawable.person_blue);
                row.add(Root.COLUMN_SUMMARY, getContext().getString(R.string.own_files));
            } else {
                row.add(Root.COLUMN_ICON, R.drawable.seminar_blue);
                row.add(Root.COLUMN_SUMMARY, r.title);
            }
            row.add(Root.COLUMN_TITLE, getContext().getString(R.string.app_name));
            int flags = Root.FLAG_SUPPORTS_CREATE | Root.FLAG_SUPPORTS_IS_CHILD;
            if (s.documents_recents) {
                flags |= Root.FLAG_SUPPORTS_RECENTS;
            }
            if (s.documents_search) {
                flags |= Root.FLAG_SUPPORTS_SEARCH;
            }
            row.add(Root.COLUMN_FLAGS, flags);
            row.add(Root.COLUMN_DOCUMENT_ID, r.folderID);
        }
        result.setNotificationUri(getContext().getContentResolver(), DocumentsContract.buildRootsUri(AUTHORITIES));
        return result;
    }
    
    
    
    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        checkDB();
        Settings s = SettingsProvider.getSettings(getContext());
        if (! s.documents_provider) {
            throw new FileNotFoundException();
        }
        //System.out.println("query document: "+documentId);
        final MatrixCursor result = new MatrixCursor((projection != null) ? projection : DEFAULT_DOCUMENT_PROJECTION);
        
        API api = APICheckLogin();
        boolean success = false;
        try {
            StudipFolder.FileRef ref = api.file.get(documentId).execute().body();
            if (ref != null) {
                addFileToCursor(result.newRow(), ref, s.documents_thumbnails, api);
                success = true;
            }
        } catch (Exception ignored) {}
        if (! success) {
            try {
                StudipFolder sub = api.folder.get(documentId).execute().body();
                if (sub != null) {
                    addFolderToCursor(result.newRow(), sub);
                }
            } catch (Exception ignored) {throw new FileNotFoundException();}
        }
        result.setNotificationUri(getContext().getContentResolver(), DocumentsContract.buildDocumentUri(AUTHORITIES,documentId));
        return result;
    }
    
    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        checkDB();
        Settings s = SettingsProvider.getSettings(getContext());
        if (! s.documents_provider) {
            throw new FileNotFoundException();
        }
        //System.out.println("query children");
        final MatrixCursor result = new MatrixCursor((projection != null) ? projection : DEFAULT_DOCUMENT_PROJECTION);
        API api = APICheckLogin();
        
        try {
            StudipFolder f = api.folder.get(parentDocumentId).execute().body();
            if (f != null) {
                for (StudipFolder.SubFolder sub : f.subfolders) {
                    addFolderToCursor(result.newRow(), sub);
                }
                for (StudipFolder.FileRef ref : f.file_refs) {
                    addFileToCursor(result.newRow(), ref, s.documents_thumbnails, api);
                }
            }
        } catch (Exception ignored) {throw new FileNotFoundException();}
        result.setNotificationUri(getContext().getContentResolver(), DocumentsContract.buildChildDocumentsUri(AUTHORITIES,parentDocumentId));
        return result;
    }
    
    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, @Nullable CancellationSignal signal) throws FileNotFoundException {
        Settings s = SettingsProvider.getSettings(getContext());
        if (! s.documents_provider) {
            throw new FileNotFoundException();
        }
        checkDB();
        //System.out.println("open document: mode: "+mode+"   "+documentId);
    
        API api = APICheckLogin();
        
        final boolean isWrite = mode.indexOf('w') != -1;
        final boolean isRead = mode.indexOf('r') != -1;
    
    
        try {
            File tmp = new File(getContext().getCacheDir()+"/"+documentId);
            //System.out.println(tmp.getAbsolutePath());
            synchronized (openFiles) {
                if (!openFiles.containsKey(documentId)) {
                    openFiles.put(documentId, 1);
                }
                else {
                    openFiles.put(documentId, openFiles.get(documentId) + 1);
                }
            }
            // no need to download the file if you only want to write
            if (isRead) {
                if (!tmp.exists() || tmp.lastModified() + 5000 <= System.currentTimeMillis()) {
                    try (ResponseBody b = api.file.download(documentId).execute().body();
                         FileOutputStream out = new FileOutputStream(tmp)) {
                        if (b != null) {
                            copyStream(b.byteStream(), out);
                            //System.out.println("downloaded");
                        }
                        else {
                            openFiles.put(documentId, openFiles.get(documentId) - 1);
                            throw new FileNotFoundException();
                        }
                    }
                }
            }
            return ParcelFileDescriptor.open(tmp, ParcelFileDescriptor.parseMode(mode), fileHandler, e -> {
                if (isWrite) {
                    try (FileInputStream in = new FileInputStream(tmp)) {
                        StudipFolder.FileRef f = api.file.get(documentId).execute().body();
                        if (f != null) {
                            api.file.update(documentId, MultipartBody.Part.createFormData("filename", f.name,
                                    RequestBody.create(FileFragment.readFully(in)))).execute();
                        }
                    }
                    catch (Exception ignored) {}
                    getContext().getContentResolver().notifyChange(DocumentsContract.buildDocumentUri(AUTHORITIES, documentId), null);
                }
                //System.out.println("closed");
                synchronized (openFiles) {
                    if (openFiles.containsKey(documentId)) {
                        openFiles.put(documentId,openFiles.get(documentId)-1);
                        if (openFiles.get(documentId) <= 0 && tmp.lastModified() + 5000 <= System.currentTimeMillis()) {
                            //System.out.println(tmp.delete());
                            openFiles.remove(documentId);
                            //System.out.println("deleted");
                        } else {
                            fileHandler.postDelayed(() -> {
                                synchronized (openFiles) {
                                    // should always delete files, because if the file is still open, it will be closed in the future, and that fill trigger
                                    // this task again
                                    if (! openFiles.containsKey(documentId) || openFiles.get(documentId) <= 0) {
                                        tmp.delete();
                                        //System.out.println("deleted");
                                    }
                                }
                            },6000);
                        }
                    } else {
                        tmp.delete();
                        //System.out.println("deleted");
                    }
                }
            });
        }
        catch (Exception ignored) {
            throw new FileNotFoundException();
        }
    }
    
    @Override
    public String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException {
        Settings s = SettingsProvider.getSettings(getContext());
        if (! s.documents_provider) {
            throw new FileNotFoundException();
        }
        checkDB();
        //System.out.println("create document");
        
        API api = APICheckLogin();
        Uri updateURI = DocumentsContract.buildChildDocumentsUri(AUTHORITIES,parentDocumentId);
        try {
            if (Document.MIME_TYPE_DIR.equals(mimeType)) {
                //System.out.println("creating a directory");
                StudipFolder f = api.folder.createFolder(parentDocumentId,displayName, "").execute().body();
                if (f == null) {
                    throw new FileNotFoundException();
                }
                getContext().getContentResolver().notifyChange(updateURI, null);
                getContext().getContentResolver().notifyChange(DocumentsContract.buildDocumentUri(AUTHORITIES, f.id), null);
                getContext().getContentResolver().notifyChange(DocumentsContract.buildChildDocumentsUri(AUTHORITIES, f.id), null);
                return f.id;
            } else {
                //System.out.println("creating a file");
                StudipFolder.FileRef f = api.file.upload(parentDocumentId,
                        MultipartBody.Part.createFormData("filename", displayName, RequestBody.create(new byte[0]))).execute().body();
                if (f == null) {
                    throw new FileNotFoundException();
                }
                getContext().getContentResolver().notifyChange(updateURI, null);
                getContext().getContentResolver().notifyChange(DocumentsContract.buildDocumentUri(AUTHORITIES, f.id), null);
                return f.id;
            }
        }
        catch (Exception ignored) {
            throw new FileNotFoundException();
        }
    }
    
    @Override
    public boolean isChildDocument(String parentDocumentId, String documentId) {
        Settings s = SettingsProvider.getSettings(getContext());
        if (! s.documents_provider) {
            return false;
        }
        checkDB();
        //System.out.println("isChildDocument");
        try {
            API api = APICheckLogin();
            StudipFolder p = api.folder.get(parentDocumentId).execute().body();
            if (p == null) {
                return false;
            }
            for (StudipFolder.SubFolder f : p.subfolders) {
                if (documentId.equals(f.id)) {
                    return true;
                }
            }
            for (StudipFolder.FileRef f : p.file_refs) {
                if (documentId.equals(f.id)) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }
    
    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        Settings s = SettingsProvider.getSettings(getContext());
        if (! s.documents_provider) {
            throw new FileNotFoundException();
        }
        checkDB();
        //System.out.println("delete document");
    
        API api = APICheckLogin();
        deleteRecursive(documentId, api, true);
    }
    
    @Override
    public void removeDocument(String documentId, String parentDocumentId) throws FileNotFoundException {
        deleteDocument(documentId); // just delete the whole document
    }
    
    
    @Override
    public String renameDocument(String documentId, String displayName) throws FileNotFoundException {
        Settings s = SettingsProvider.getSettings(getContext());
        if (! s.documents_provider) {
            throw new FileNotFoundException();
        }
        checkDB();
        //System.out.println("rename document");
    
        Uri updateURI = DocumentsContract.buildDocumentUri(AUTHORITIES,documentId);
        
        API api = APICheckLogin();
        try {
            StudipFolder.FileRef f = api.file.get(documentId).execute().body();
            if (f == null) {
                throw new FileNotFoundException();
            }
            if (api.file.put(documentId, displayName, null).execute().code() != 200) {
                throw new FileNotFoundException();
            }
            getContext().getContentResolver().notifyChange(updateURI, null);
            getContext().getContentResolver().notifyChange(DocumentsContract.buildChildDocumentsUri(AUTHORITIES, f.folder_id), null);
        }
        catch (IOException e) {
            try {
                StudipFolder f = api.folder.get(documentId).execute().body();
                if (f == null) {
                    throw new FileNotFoundException();
                }
                if (api.folder.put(documentId, displayName, null).execute().code() != 200) {
                    throw new FileNotFoundException();
                }
                getContext().getContentResolver().notifyChange(updateURI, null);
                getContext().getContentResolver().notifyChange(DocumentsContract.buildChildDocumentsUri(AUTHORITIES, f.parent_id), null);
            }
            catch (IOException ignored) {
                throw new FileNotFoundException();
            }
        }
        return null;
    }
    
    // copying within the same document provider
    @Override
    public String copyDocument(String sourceDocumentId, String targetParentDocumentId) throws FileNotFoundException {
        Settings s = SettingsProvider.getSettings(getContext());
        if (! s.documents_provider) {
            throw new FileNotFoundException();
        }
        checkDB();
    
        API api = APICheckLogin();
        //System.out.println("copy document");
    
        try {
            Response<StudipFolder> res = api.folder.copy(sourceDocumentId, targetParentDocumentId).execute();
            StudipFolder f = res.body();
            if (res.code() != 200 || f == null) {
                throw new FileNotFoundException();
            }
            getContext().getContentResolver().notifyChange(DocumentsContract.buildDocumentUri(AUTHORITIES, f.id), null);
            return f.id;
        } catch (Exception ignored) {
            // it's a file
            try {
                Response<StudipFolder.FileRef> res = api.file.copy(sourceDocumentId, targetParentDocumentId).execute();
                StudipFolder.FileRef f = res.body();
                if (res.code() != 200 || f == null) {
                    throw new FileNotFoundException();
                }
                getContext().getContentResolver().notifyChange(DocumentsContract.buildDocumentUri(AUTHORITIES, f.id), null);
                return f.id;
            } catch (Exception ignored2) {
                throw new FileNotFoundException();
            }
        }
    }
    
    @Override
    public String moveDocument(String sourceDocumentId, String sourceParentDocumentId, String targetParentDocumentId) throws FileNotFoundException {
        Settings s = SettingsProvider.getSettings(getContext());
        if (! s.documents_provider) {
            throw new FileNotFoundException();
        }
        checkDB();
    
        API api = APICheckLogin();
        //System.out.println("move document");
    
        try {
            Response<StudipFolder> res = api.folder.move(sourceDocumentId, targetParentDocumentId).execute();
            StudipFolder f = res.body();
            if (res.code() != 200 || f == null) {
                throw new FileNotFoundException();
            }
            getContext().getContentResolver().notifyChange(DocumentsContract.buildDocumentUri(AUTHORITIES, f.id), null);
            getContext().getContentResolver().notifyChange(DocumentsContract.buildChildDocumentsUri(AUTHORITIES, sourceParentDocumentId), null);
            getContext().getContentResolver().notifyChange(DocumentsContract.buildDocumentUri(AUTHORITIES, targetParentDocumentId), null);
            return f.id;
        } catch (Exception ignored) {
            // it's a file
            try {
                Response<StudipFolder.FileRef> res = api.file.move(sourceDocumentId, targetParentDocumentId).execute();
                StudipFolder.FileRef f = res.body();
                if (res.code() != 200 || f == null) {
                    throw new FileNotFoundException();
                }
                getContext().getContentResolver().notifyChange(DocumentsContract.buildDocumentUri(AUTHORITIES, f.id), null);
                getContext().getContentResolver().notifyChange(DocumentsContract.buildChildDocumentsUri(AUTHORITIES, sourceParentDocumentId), null);
                getContext().getContentResolver().notifyChange(DocumentsContract.buildDocumentUri(AUTHORITIES, targetParentDocumentId), null);
                return f.id;
            } catch (Exception ignored2) {
                throw new FileNotFoundException();
            }
        }
    }
    
    @Override
    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException {
        Settings s = SettingsProvider.getSettings(getContext());
        if (! s.documents_provider || ! s.documents_thumbnails) {
            throw new FileNotFoundException();
        }
        checkDB();
    
    
        
        
        ConnectivityManager con = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (! s.load_images_on_mobile && con.isActiveNetworkMetered()) {
            throw new FileNotFoundException();
        }
        
        API api = APICheckLogin();
        
        //System.out.println("thumbnail");
        
        File tmp = new File(getContext().getCacheDir()+"/thumbnail-"+documentId);
        if (! tmp.exists()) {
            try (ResponseBody body = api.file.download(documentId).execute().body();
                 FileOutputStream out = new FileOutputStream(tmp)) {
                if (body != null) {
                    copyStream(body.byteStream(), out);
                    //System.out.println("thumbnail downloaded");
                }
            }
            catch (IOException ignored) {
                throw new FileNotFoundException();
            }
        }
        
        // downscale the image
        RequestCreator c = Picasso.get().load(tmp);
        try {
            Bitmap bmp = c.get();
            if (bmp.getWidth() > 2*sizeHint.x || bmp.getHeight() > 2*sizeHint.y) {
                c.resize(sizeHint.x, sizeHint.y);
                c.centerCrop();
                bmp = c.get();
                try (FileOutputStream out = new FileOutputStream(tmp)) {
                    bmp.compress(Bitmap.CompressFormat.PNG, 0, out);
                    //System.out.println("thumbnail resized");
                } catch (Exception ignored) {}
            }
        }
        catch (IOException ignored) {}
        
        return new AssetFileDescriptor(ParcelFileDescriptor.open(tmp, ParcelFileDescriptor.MODE_READ_ONLY),0, AssetFileDescriptor.UNKNOWN_LENGTH);
    }
    
    private void recentsRecursive(String folder, SortedMap<Long,StudipFolder.FileRef> files, API api) {
        try {
            StudipFolder f = api.folder.get(folder).execute().body();
            if (f != null) {
                for (StudipFolder.FileRef ref : f.file_refs) {
                    files.put(ref.chdate, ref);
                    if (files.size() > 40) { // 40 should be enough recent documents
                        return;
                    }
                }
                for (StudipFolder.SubFolder sub : f.subfolders) {
                    recentsRecursive(sub.id, files, api);
                }
            }
        } catch (Exception ignored) {}
    }
    
    private void searchRecursive(String folder, final MatrixCursor result, String query, API api) {
        try {
            StudipFolder f = api.folder.get(folder).execute().body();
            if (f != null) {
                for (StudipFolder.FileRef ref : f.file_refs) {
                    if (ref.name.toLowerCase().contains(query)) {
                        addFileToCursor(result.newRow(), ref, false, api);
                    }
                }
                for (StudipFolder.SubFolder sub : f.subfolders) {
                    if (sub.name.toLowerCase().contains(query)) {
                        addFolderToCursor(result.newRow(), sub);
                    }
                    searchRecursive(sub.id, result, query, api);
                }
            }
        } catch (Exception ignored) {}
    }
    
    @Override
    public Cursor queryRecentDocuments(String rootId, String[] projection) throws FileNotFoundException {
        Settings s = SettingsProvider.getSettings(getContext());
        if (! s.documents_provider || ! s.documents_recents) {
            throw new FileNotFoundException();
        }
        checkDB();
    
        API api = APICheckLogin();
        
        //System.out.println("recents");
        
        final MatrixCursor result = new MatrixCursor((projection != null) ? projection : DEFAULT_DOCUMENT_PROJECTION);
        
        DocumentRoot[] roots = docs.documents().getRoots();
        for (DocumentRoot r : roots) {
            if (r.parentID.equals(rootId)) {
                SortedMap<Long,StudipFolder.FileRef> list = new TreeMap<>((o1, o2) -> (int) -(o1-o2));
                recentsRecursive(r.folderID, list, api);
                for (Map.Entry<Long,StudipFolder.FileRef> e : list.entrySet()) {
                    addFileToCursor(result.newRow(), e.getValue(), false, api);
                }
                break;
            }
        }
        
        return result;
    }
    
    @Override
    public Cursor querySearchDocuments(String rootId, String query, String[] projection) throws FileNotFoundException {
        Settings s = SettingsProvider.getSettings(getContext());
        if (! s.documents_provider || ! s.documents_search) {
            throw new FileNotFoundException();
        }
        checkDB();
    
        API api = APICheckLogin();
        
        //System.out.println("search");
        
        final MatrixCursor result = new MatrixCursor((projection != null) ? projection : DEFAULT_DOCUMENT_PROJECTION);
        query = query.toLowerCase();
        
        DocumentRoot[] roots = docs.documents().getRoots();
        for (DocumentRoot r : roots) {
            if (r.parentID.equals(rootId)) {
                searchRecursive(r.folderID, result, query, api);
                break;
            }
        }
        return result;
    }
    
    @Override
    public boolean onCreate() {
        new Thread(() -> {
            Looper.prepare();
            fileHandler = new Handler(Looper.myLooper());
            Looper.loop();
        }).start();
        // make sure the handler is initialized before returning
        try {
            while (fileHandler == null) {
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        checkDB(); // somehow doesn't work if initialization is deferred
        // clear the cache, just in case the provider got terminated while files were open
        File cacheDir = getContext().getCacheDir();
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        return true;
    }
}
