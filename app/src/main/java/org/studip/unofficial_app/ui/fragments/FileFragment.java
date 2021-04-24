package org.studip.unofficial_app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.rest.StudipFolder;
import org.studip.unofficial_app.databinding.FragmentFileBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.viewmodels.FileViewModel;
import org.studip.unofficial_app.model.viewmodels.HomeActivityViewModel;
import org.studip.unofficial_app.ui.HomeActivity;

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
        
        int status = m.getStatus().getValue();
        
        m.get().observe(getViewLifecycleOwner(), folder -> {
            if (folder != null)
            {
                ad.clear();
                ad.addAll(folder.subfolders);
                ad.addAll(folder.file_refs);
                // recreate the fragment once when the data has been loaded, otherwise it somehow doesn't show until the user navigates to the fragment and tries to navigate away
                // it is only once, because when the fragment is recreated,  status is captured again, with the code of 200
                if (status == -1)
                {
                    //System.out.println("recreated");
                    getParentFragmentManager().beginTransaction().detach(this).attach(this).commit();
                }
            }
        });
        
        if (m.getStatus().getValue() == -1) {
            m.refresh(requireActivity());
        }
        
        
        return binding.getRoot();
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
                } else if (o instanceof StudipFolder.FileRef) {
                    StudipFolder.FileRef f = (StudipFolder.FileRef) o;
                    v.setText(f.name);
                    v.setOnClickListener(v1 -> APIProvider.getAPI(requireActivity()).downloadFile(requireActivity(),f.id,f.name));
                } else {
                    v.setText("");
                }
            }
            
            return v;
        }
    }
    
    
    
    
    
}
