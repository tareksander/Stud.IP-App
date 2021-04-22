package org.studip.unofficial_app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.api.rest.StudipSemester;
import org.studip.unofficial_app.databinding.CoursesEntryBinding;
import org.studip.unofficial_app.databinding.FragmentCoursesBinding;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.room.DB;
import org.studip.unofficial_app.model.viewmodels.CoursesViewModel;
import org.studip.unofficial_app.ui.HomeActivity;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class CoursesFragment extends Fragment
{
    
    private CoursesViewModel m;
    private FragmentCoursesBinding binding;
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        binding = FragmentCoursesBinding.inflate(inflater);
        m = new ViewModelProvider(requireActivity()).get(CoursesViewModel.class);
        
        
        SemesterAdapter semad = new SemesterAdapter(requireActivity(),R.layout.list_textview);
        binding.semesterSelect.setAdapter(semad);
        
        CoursesAdapter coursead = new CoursesAdapter(requireActivity(),ArrayAdapter.NO_SELECTION);
        binding.coursesList.setAdapter(coursead);
        
        String semid;

        binding.coursesRefresh.setOnRefreshListener(() -> {
            m.semester.refresh(requireActivity());
            m.courses.refresh(requireActivity());
        });
        m.courses.isRefreshing().observe(getViewLifecycleOwner(), (ref) -> binding.coursesRefresh.setRefreshing(ref));


        m.courses.getStatus().observe(getViewLifecycleOwner(), status -> HomeActivity.onStatusReturn(requireActivity(),status));
        m.semester.getStatus().observe(getViewLifecycleOwner(), status -> HomeActivity.onStatusReturn(requireActivity(),status));
        
        m.semester.get().observe(getViewLifecycleOwner(), (sems) -> {
            if (sems.length == 0 && m.semester.getStatus().getValue() == -1) {
                m.semester.refresh(requireActivity());
            }
            Arrays.sort(sems, (o1, o2) -> -Long.compare(o1.begin, o2.begin));
            semad.clear();
            semad.addAll(sems);
            long unixtime = System.currentTimeMillis()/1000;
            for (int i = 0;i<sems.length;i++) {
                StudipSemester s = sems[i];
                if (unixtime >= s.begin && unixtime <= s.end) {
                    binding.semesterSelect.setSelection(i);
                    break;
                }
            }
        });
        
        DB db = DBProvider.getDB(requireActivity());
        
        LiveData<StudipCourse[]> dball = db.courseDao().observeAll();
        final LiveData<StudipCourse[]>[] dbsem = new LiveData[]{null};

        dball.observe(getViewLifecycleOwner(),(c) -> {
            if (c.length == 0 && m.courses.getStatus().getValue() == -1) {
                //System.out.println("refresh");
                m.courses.refresh(requireActivity());
            }
            coursead.clear();
            coursead.addAll(c);
        });
        
        binding.semesterSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (dbsem[0] != null) {
                    dbsem[0].removeObservers(getViewLifecycleOwner());
                }
                // /api.php/semester/
                //System.out.println("selected semester"+semad.getItem(position).title+"  "+semad.getItem(position).id);
                dball.removeObservers(getViewLifecycleOwner());
                dbsem[0] = db.courseDao().observeSemester(semad.getItem(position).id);
                dbsem[0].observe(getViewLifecycleOwner(),(c) -> {
                    //System.out.println("updated semester courses");
                    coursead.clear();
                    coursead.addAll(c);
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                //System.out.println("nothing selected");
                dball.observe(getViewLifecycleOwner(),(c) -> {
                    if (c.length == 0 && m.courses.getStatus().getValue() == -1) {
                        m.courses.refresh(requireActivity());
                    }
                    coursead.clear();
                    coursead.addAll(c);
                });
            }
        });
        
        
        return binding.getRoot();
    }
    
    private class SemesterAdapter extends ArrayAdapter<StudipSemester> {

        public SemesterAdapter(@NonNull Context context, int resource)
        {
            super(context, resource);
        }
    }
    
    
    private class CoursesAdapter extends ArrayAdapter<StudipCourse> {

        public CoursesAdapter(@NonNull Context context, int resource)
        {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            StudipCourse c = getItem(position);
            View v = (convertView != null) ? convertView : getLayoutInflater().inflate(R.layout.courses_entry,parent,false);
            CoursesEntryBinding b = CoursesEntryBinding.bind(v);
            b.courseName.setText(c.title);
            if (c.modules_object != null)
            {
                if (c.modules_object.forum != null)
                {
                    b.courseForum.setVisibility(View.VISIBLE);
                }
                if (c.modules_object.documents != null)
                {
                    b.courseFiles.setVisibility(View.VISIBLE);
                }
            }
            
            return v;
        }
    }
    
}
