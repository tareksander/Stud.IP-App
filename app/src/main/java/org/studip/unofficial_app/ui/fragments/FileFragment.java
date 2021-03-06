package org.studip.unofficial_app.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.Features;
import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.api.rest.StudipFolder;
import org.studip.unofficial_app.api.routes.Studip;
import org.studip.unofficial_app.databinding.DialogFileEntryBinding;
import org.studip.unofficial_app.databinding.DialogForumEntryBinding;
import org.studip.unofficial_app.databinding.FragmentFileBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.viewmodels.FileViewModel;
import org.studip.unofficial_app.model.viewmodels.HomeActivityViewModel;
import org.studip.unofficial_app.model.viewmodels.MkdirDialogViewModel;
import org.studip.unofficial_app.ui.HomeActivity;
import org.studip.unofficial_app.ui.fragments.dialog.MkdirDialogFragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileFragment extends SwipeRefreshFragment
{
    private HomeActivityViewModel h;
    private FragmentFileBinding binding;
    private FileViewModel m;
    
    
    private ActivityResultLauncher<String[]> launch;
    private static final String LIST_KEY = "list";
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        binding = FragmentFileBinding.inflate(inflater);
        h = new ViewModelProvider(requireActivity()).get(HomeActivityViewModel.class);
        m = new ViewModelProvider(requireActivity()).get(FileViewModel.class);
        
        setSwipeRefreshLayout(binding.fileRefresh);
    
        API api2 = APIProvider.getAPI(requireActivity());
        if (api2 != null && api2.isFeatureEnabled(Features.FEATURE_FILES)
                &&  ( api2.isFeatureEnabled(Features.FEATURE_USER_FILES) || api2.isFeatureEnabled(Features.FEATURE_COURSE_FILES))) {
            final FileAdapter ad = new FileAdapter(requireContext(), ArrayAdapter.IGNORE_ITEM_VIEW_TYPE);
            binding.fileList.setAdapter(ad);
    
            m.getStatus().observe(getViewLifecycleOwner(), status -> HomeActivity.onStatusReturn(requireActivity(), status));
            m.isRefreshing().observe(getViewLifecycleOwner(), ref -> binding.fileRefresh.setRefreshing(ref));
    
            binding.fileRefresh.setOnRefreshListener(() -> m.refresh(requireActivity()));
    
            if (h.filesCourse.getValue() == null) {
                binding.filesCourseName.setText(R.string.my_documents);
            }
    
            h.filesCourse.observe(getViewLifecycleOwner(), (course) -> {
                if (course != null) {
                    binding.filesCourseName.setText(course.title);
                }
                else {
                    binding.filesCourseName.setText(R.string.my_documents);
                }
            });
    
            m.get().observe(getViewLifecycleOwner(), folder -> {
                if (folder != null) {
                    //System.out.println(folder.subfolders);
                    //System.out.println(folder.file_refs);
                    ad.clear();
                    ad.addAll((Object[]) folder.subfolders);
                    ad.addAll((Object[]) folder.file_refs);
                    binding.fileList.setAdapter(ad); // when the fragment is first shown, the data will not be visible without this
                    if (savedInstanceState != null && savedInstanceState.containsKey(LIST_KEY)) {
                        binding.fileList.onRestoreInstanceState(savedInstanceState.getParcelable(LIST_KEY));
                        savedInstanceState.remove(LIST_KEY);
                    }
                }
                else {
                    API api = APIProvider.getAPI(requireActivity());
                    if (api != null && ! api.isFeatureEnabled(Features.FEATURE_USER_FILES)) {
                        ad.clear();
                    }
                }
            });
    
            if (m.getStatus().getValue() == -1) {
                //System.out.println("refresh");
                m.refresh(requireActivity());
            }
            
            
            binding.filesCourseName.setOnLongClickListener(v -> {
                StudipFolder f = m.get().getValue();
                StudipCourse course = h.filesCourse.getValue();
                final Activity a = requireActivity();
                if (f != null && ShortcutManagerCompat.isRequestPinShortcutSupported(a)) {
                    if ((f.name == null || f.name.equals("")) && course == null) {
                        return true;
                    }
                    ShortcutInfoCompat.Builder b = new ShortcutInfoCompat.Builder(a, "folder:"+f.id);
                    b.setIcon(IconCompat.createWithResource(a, R.drawable.file_blue));
                    if ((f.name == null || f.name.equals(""))) {
                        b.setShortLabel(course.title);
                    } else {
                        b.setShortLabel(f.name);
                    }
                    Intent i = new Intent(a, HomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    i.setAction(a.getPackageName()+".dynamic_shortcut");
                    Uri data = Uri.parse(a.getPackageName()+".folder://"+f.id);
                    if (course != null) {
                        data = data.buildUpon().query(course.course_id).build();
                    }
                    i.setData(data);
                    b.setIntent(i);
                    ShortcutManagerCompat.requestPinShortcut(a, b.build(), null);
                }
                return true;
            });
            
            binding.buttonMkdir.setOnClickListener(this::onMkdir);
            binding.buttonUpload.setOnClickListener(this::onUpload);
    
    
            MkdirDialogViewModel mkdirm = new ViewModelProvider(requireActivity()).get(MkdirDialogViewModel.class);
            mkdirm.dirName.observe(getViewLifecycleOwner(), (dirname) -> {
                if (dirname != null) {
                    mkdirm.dirName.setValue(null);
                    API api = APIProvider.getAPI(requireActivity());
                    StudipFolder folder = m.get().getValue();
                    if (api != null && folder != null && folder.id != null) {
                        api.folder.createFolder(folder.id, dirname, null).enqueue(new Callback<StudipFolder>()
                        {
                            @Override
                            public void onResponse(Call<StudipFolder> call, Response<StudipFolder> response) {
                                if (response.code() == 200) {
                                    m.refresh(requireActivity());
                                }
                            }
                    
                            @Override
                            public void onFailure(Call<StudipFolder> call, Throwable t) {
                            }
                        });
                    }
                }
            });
        } else {
            binding.fileRefresh.setOnRefreshListener(() -> binding.fileRefresh.setRefreshing(false));
        }
        
        launch = registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), this::uploadFiles);
        
        return binding.getRoot();
    }
    
    private void uploadFiles(List<Uri> files) {
        final boolean[] finished = new boolean[files.size()];
        MutableLiveData<Boolean> obs = new MutableLiveData<>();
        obs.observe(this, o -> {
            for (boolean b  : finished) {
                if (! b) {
                    return;
                }
            }
            m.refresh(requireActivity());
        });
        for (int i = 0;i<files.size();i++) {
            Uri file = files.get(i);
            if (file != null)
            {
                StudipFolder f = m.get().getValue();
                if (f != null)
                {
                    binding.fileRefresh.setRefreshing(true);
                    byte[] data = null;
                    try (InputStream in = requireActivity().getContentResolver().openInputStream(file)) {
                        data = readFully(in);
                    }
                    catch (IOException ignored) {}
                    if (data != null)
                    {
                        API api = APIProvider.getAPI(requireActivity());
                        if (api != null)
                        {
                            MultipartBody.Part p = MultipartBody.Part.createFormData("filename",getFileName(file,requireActivity()), RequestBody.create(data));
                            int finalI = i;
                            api.file.upload(f.id, p).enqueue(new Callback<StudipFolder.FileRef>()
                            {
                                @Override
                                public void onResponse(@NotNull Call<StudipFolder.FileRef> call, @NotNull Response<StudipFolder.FileRef> response)
                                {
                                    finished[finalI] = true;
                                    obs.setValue(true);
                                    if (response.code() != 201) {
                                        if (response.code() == 500) {
                                            Toast.makeText(requireActivity(),R.string.upload_failed,Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                        
                                @Override
                                public void onFailure(@NotNull Call<StudipFolder.FileRef> call, @NotNull Throwable t)
                                {
                                    finished[finalI] = true;
                                    obs.setValue(true);
                                    //binding.fileRefresh.setRefreshing(false);
                                }
                            });
                        } else {
                            finished[i] = true;
                            obs.setValue(true);
                            //binding.fileRefresh.setRefreshing(false);
                        }
                    } else {
                        finished[i] = true;
                        obs.setValue(true);
                        //binding.fileRefresh.setRefreshing(false);
                    }
                } else {
                    finished[i] = true;
                    obs.setValue(true);
                }
            } else {
                finished[i] = true;
                obs.setValue(true);
            }
        }
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LIST_KEY, binding.fileList.onSaveInstanceState());
    }
    
    public static byte[] readFully(InputStream in) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(1024*1024)) {
            byte[] buffer = new byte[1024*1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }
    

    private static String getFileName(Uri uri, Activity a)
    {
        String result = null;
        if (uri.getScheme().equals("content"))
        {
            try (Cursor cursor = a.getContentResolver().query(uri, null, null, null, null);)
            {
                if (cursor != null && cursor.moveToFirst())
                {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null)
        {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1)
            {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public void onUpload(View v) {
        API api = APIProvider.getAPI(requireActivity());
        if (api != null && ! api.isFeatureEnabled(Features.FEATURE_USER_FILES) && h.filesCourse.getValue() == null) {
            return;
        }
        Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_OPEN_DOCUMENT);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null)
        {
            if (launch != null) {
                launch.launch(new String[]{"*/*"});
            }
            //startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_file)), 123);
        }
    }
    
    public void onMkdir(View v) {
        API api = APIProvider.getAPI(requireActivity());
        if (api != null && ! api.isFeatureEnabled(Features.FEATURE_USER_FILES) && h.filesCourse.getValue() == null) {
            return;
        }
        StudipFolder folder = m.get().getValue();
        if (api != null && folder != null)
        {
            MkdirDialogFragment f = new MkdirDialogFragment();
            f.show(getParentFragmentManager(), "mkdir_dialog");
        }
    }
    
    private class FileAdapter extends ArrayAdapter<Object> {


        public FileAdapter(@NonNull Context context, int resource)
        {
            super(context, resource);
        }

        @Override
        public int getCount()
        {
            API api = APIProvider.getAPI(requireActivity());
            if (api != null && ! api.isFeatureEnabled(Features.FEATURE_USER_FILES) && h.filesCourse.getValue() == null) {
                return 0;
            }
            return super.getCount()+1;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            TextView v;
            if (convertView instanceof TextView) {
                v = (TextView) convertView;
            } else {
                DialogFileEntryBinding b = DialogFileEntryBinding.inflate(getLayoutInflater());
                v = b.t;
            }
            if (position ==  0) {
                v.setText("..");
                v.setOnClickListener(v1 -> {
                    StudipFolder f = m.get().getValue();
                    if (f != null && ! f.parent_id.equals("")) {
                        m.setFolder(requireActivity(),f.parent_id,false);
                    }
                });
                v.setOnLongClickListener(v1 -> {
                    h.setFilesCourse(null);
                    m.setFolder(requireActivity(),null,false);
                    return true;
                });
            } else {
                position--;
                Object o = getItem(position);
                if (o instanceof StudipFolder.SubFolder) {
                    StudipFolder.SubFolder f = (StudipFolder.SubFolder) o;
                    v.setText(f.name);
                    v.setOnClickListener(v1 -> {
                        if (binding.fileRefresh.isRefreshing()) { return; }
                        m.setFolder(requireActivity(),f.id,false);
                    });
                    v.setOnLongClickListener(v1 -> {
                        if (binding.fileRefresh.isRefreshing()) { return true; }
                        binding.fileRefresh.setRefreshing(true);
                        APIProvider.getAPI(requireActivity()).folder.delete(f.id).enqueue(new Callback<Void>()
                        {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response)
                            {
                                if (response.code() == 200) {
                                    m.refresh(requireActivity());
                                } else {
                                    binding.fileRefresh.setRefreshing(false);
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                binding.fileRefresh.setRefreshing(false);
                            }
                        });
                        return true;
                    });
                } else if (o instanceof StudipFolder.FileRef) {
                    StudipFolder.FileRef f = (StudipFolder.FileRef) o;
                    v.setText(f.name);
                    v.setOnClickListener(v1 -> {
                        if (binding.fileRefresh.isRefreshing()) { return; }
                        if (Build.VERSION.SDK_INT <= 28 && ContextCompat.checkSelfPermission(requireActivity(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                            return;
                        }
                        APIProvider.getAPI(requireActivity()).downloadFile(requireActivity(),f.id,f.name, false);
                    });
                    v.setOnLongClickListener(v1 -> {
                        if (binding.fileRefresh.isRefreshing()) { return true; }
                        binding.fileRefresh.setRefreshing(true);
                        APIProvider.getAPI(requireActivity()).file.delete(f.id).enqueue(new Callback<Void>()
                        {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response)
                            {
                                if (response.code() == 200) {
                                    m.refresh(requireActivity());
                                } else {
                                    binding.fileRefresh.setRefreshing(false);
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                binding.fileRefresh.setRefreshing(false);
                            }
                        });
                        return true;
                    });
                } else {
                    v.setText("");
                }
            }
            
            return v;
        }
    }
    
    
    
    
    
}
