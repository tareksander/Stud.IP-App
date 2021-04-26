package org.studip.unofficial_app.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipFolder;
import org.studip.unofficial_app.databinding.FragmentFileBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.viewmodels.FileViewModel;
import org.studip.unofficial_app.model.viewmodels.HomeActivityViewModel;
import org.studip.unofficial_app.ui.HomeActivity;
import org.studip.unofficial_app.ui.fragments.dialog.MkdirDialogFragment;

import java.io.IOException;
import java.io.InputStream;

import kotlin.io.ByteStreamsKt;
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
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        binding = FragmentFileBinding.inflate(inflater);
        h = new ViewModelProvider(requireActivity()).get(HomeActivityViewModel.class);
        m = new ViewModelProvider(requireActivity()).get(FileViewModel.class);
        
        final FileAdapter ad = new FileAdapter(requireContext(),ArrayAdapter.IGNORE_ITEM_VIEW_TYPE);
        binding.fileList.setAdapter(ad);
        
        m.getStatus().observe(getViewLifecycleOwner(), status -> HomeActivity.onStatusReturn(requireActivity(),status));
        m.isRefreshing().observe(getViewLifecycleOwner(), ref -> binding.fileRefresh.setRefreshing(ref));
        
        binding.fileRefresh.setOnRefreshListener( () -> m.refresh(requireActivity()));
        
        if (h.filesCourse.getValue() == null) {
            binding.filesCourseName.setText(R.string.my_documents);
        }
        
        h.filesCourse.observe(getViewLifecycleOwner(), (course) -> {
            if (course != null) {
                binding.filesCourseName.setText(course.title);
                m.setFolder(requireActivity(),course.course_id,true);
            } else {
                binding.filesCourseName.setText(R.string.my_documents);
            }
        });
        
        
        m.get().observe(getViewLifecycleOwner(), folder -> {
            if (folder != null)
            {
                ad.clear();
                ad.addAll(folder.subfolders);
                ad.addAll(folder.file_refs);
                binding.fileList.setAdapter(ad); // when the fragment is first shown, the data will not be visible without this
            }
        });
        
        if (m.getStatus().getValue() == -1) {
            System.out.println("refresh");
            m.refresh(requireActivity());
        }
        
        
        binding.buttonMkdir.setOnClickListener(this::onMkdir);
        binding.buttonUpload.setOnClickListener(this::onUpload);
        
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intentData)
    {
        super.onActivityResult(requestCode, resultCode, intentData);
        if (intentData != null)
        {
            Uri file = intentData.getData();
            StudipFolder f = m.get().getValue();
            if (f != null)
            {
                binding.fileRefresh.setRefreshing(true);
                byte[] data = null;
                try (InputStream in = requireActivity().getContentResolver().openInputStream(file)) {
                    data = ByteStreamsKt.readBytes(in);
                }
                catch (IOException ignored) {}
                if (data != null)
                {
                    API api = APIProvider.getAPI(requireActivity());
                    if (api != null)
                    {
                        //System.out.println(api);
                        /*
                        MultipartBody.Part.Builder b = new MultipartBody.Builder()
                                .addFormDataPart("name", "_FILES")
                                .addFormDataPart("filename", getFileName(file, requireActivity()))
                                .addPart(RequestBody.create(data));
                         */
                        MultipartBody.Part p = MultipartBody.Part.createFormData("filename",getFileName(file,requireActivity()), RequestBody.create(data));
                        api.file.upload(f.id, p).enqueue(new Callback<StudipFolder.FileRef>()
                        {
                            @Override
                            public void onResponse(@NotNull Call<StudipFolder.FileRef> call, @NotNull Response<StudipFolder.FileRef> response)
                            {
                                if (response.code() == 201)
                                {
                                    m.refresh(requireActivity());
                                } else {
                                    binding.fileRefresh.setRefreshing(false);
                                }
                            }

                            @Override
                            public void onFailure(@NotNull Call<StudipFolder.FileRef> call, @NotNull Throwable t)
                            {
                                binding.fileRefresh.setRefreshing(false);
                            }
                        });
                    } else {
                        binding.fileRefresh.setRefreshing(false);
                    }
                } else {
                    binding.fileRefresh.setRefreshing(false);
                }
            }
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
        Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_OPEN_DOCUMENT);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null)
        {
            startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_file)), 123);
        }
    }
    
    public void onMkdir(View v) {
        API api = APIProvider.getAPI(requireActivity());
        StudipFolder folder = m.get().getValue();
        if (api != null && folder != null)
        {
            MkdirDialogFragment f = new MkdirDialogFragment();
            f.setListener((name) -> api.folder.createFolder(folder.id, name, null).enqueue(new Callback<StudipFolder>()
            {
                @Override
                public void onResponse(Call<StudipFolder> call, Response<StudipFolder> response)
                {
                    if (response.code() == 200)
                    {
                        m.refresh(requireActivity());
                    }
                }

                @Override
                public void onFailure(Call<StudipFolder> call, Throwable t) {}
            }));
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
                v = new TextView(requireActivity());
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
                    h.filesCourse.setValue(null);
                    m.setFolder(requireActivity(),null,false);
                    return true;
                });
            } else {
                position--;
                Object o = getItem(position);
                if (o instanceof StudipFolder.SubFolder) {
                    StudipFolder.SubFolder f = (StudipFolder.SubFolder) o;
                    v.setText(f.name);
                    v.setOnClickListener(v1 -> m.setFolder(requireActivity(),f.id,false));
                    v.setOnLongClickListener(v1 -> {
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
                    v.setOnClickListener(v1 -> APIProvider.getAPI(requireActivity()).downloadFile(requireActivity(),f.id,f.name));
                    v.setOnLongClickListener(v1 -> {
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
