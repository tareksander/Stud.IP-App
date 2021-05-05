package org.studip.unofficial_app;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import static android.provider.DocumentsContract.Root;
import static android.provider.DocumentsContract.Document;
import androidx.annotation.Nullable;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.api.rest.StudipFolder;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.room.DB;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;
import retrofit2.Call;

public class DocumentsProvider extends android.provider.DocumentsProvider
{
    public static final String AUTHORITIES = "org.studip.unofficial_app.documents";
    
    private static final String[] DEFAULT_ROOT_PROJECTION =
            new String[]{Root.COLUMN_ROOT_ID, Root.COLUMN_MIME_TYPES,
                    Root.COLUMN_FLAGS, Root.COLUMN_ICON, Root.COLUMN_TITLE,
                    Root.COLUMN_SUMMARY, Root.COLUMN_DOCUMENT_ID,
                    Root.COLUMN_AVAILABLE_BYTES,};
    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new
            String[]{Document.COLUMN_DOCUMENT_ID, Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME, Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS, Document.COLUMN_SIZE,};
    
    
    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        // TODO update roots when updating courses and logging in/out
        // TODO dynamic roots: https://medium.com/androiddevelopers/building-a-documentsprovider-f7f2fb38e86a
        // return immediately, update the roots in a callback
        // result.newRow(); is not thread-safe, synchronize
        
        API api = APIProvider.getAPI(getContext());
        DB db = DBProvider.getDB(getContext());
        MatrixCursor result = new MatrixCursor((projection != null) ? projection : DEFAULT_ROOT_PROJECTION);
        if (api != null && db != null) {
            // add the user root
            try {
                Call<StudipFolder> call = api.user.userFolder(api.getUserID());
                call.timeout().deadline(5, TimeUnit.SECONDS);
                StudipFolder f = call.execute().body();
                
                if (f != null && f.id != null && ! f.id.equals("")) {
                    MatrixCursor.RowBuilder row = result.newRow();
                    row.add(Root.COLUMN_ROOT_ID, api.getUserID());
                    row.add(Root.COLUMN_ICON, R.drawable.seminar_blue);
                    row.add(Root.COLUMN_TITLE, getContext().getString(R.string.app_name));
                    row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE);
                    row.add(Root.COLUMN_DOCUMENT_ID, f.id);
                    row.add(Root.COLUMN_SUMMARY,getContext().getString(R.string.own_files));
                }
            } catch (Exception ignored) {}
            StudipCourse[] courses = db.courseDao().getDocumentsCourses();
            if (courses != null) {
                for (StudipCourse c : courses) {
                    try {
                        Call<StudipFolder> call = api.course.folder(c.course_id);
                        call.timeout().deadline(5, TimeUnit.SECONDS);
                        StudipFolder f = call.execute().body();
                        if (f != null && f.id != null && ! f.id.equals("")) {
                            MatrixCursor.RowBuilder row = result.newRow();
                            row.add(Root.COLUMN_ROOT_ID, c.course_id);
                            row.add(Root.COLUMN_ICON, R.drawable.seminar_blue);
                            row.add(Root.COLUMN_TITLE, getContext().getString(R.string.app_name));
                            row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE);
                            row.add(Root.COLUMN_DOCUMENT_ID, f.id);
                            row.add(Root.COLUMN_SUMMARY, c.title);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        return result;
    }
    
    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        return null;
    }
    
    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        return null;
    }
    
    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, @Nullable CancellationSignal signal) throws FileNotFoundException {
        return null;
    }
    
    @Override
    public boolean onCreate() {
        return true;
    }
}
