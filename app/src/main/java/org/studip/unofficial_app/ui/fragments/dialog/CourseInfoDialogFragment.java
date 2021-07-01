package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.api.rest.StudipSemester;
import org.studip.unofficial_app.databinding.DialogCourseInfoBinding;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.room.DB;

import java.io.Serializable;

public class CourseInfoDialogFragment extends DialogFragment
{
    public static final String COURSE = "course";
    
    
    
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());
        
        Bundle args = getArguments();
        if (args == null) {
            dismiss();
            return b.create();
        }
        Serializable s = args.getSerializable(COURSE);
        if (!(s instanceof StudipCourse)) {
            dismiss();
            return b.create();
        }
        StudipCourse c = (StudipCourse) s;
        
        b.setTitle(c.title);
    
        DialogCourseInfoBinding binding = DialogCourseInfoBinding.inflate(getLayoutInflater());
    
        
        binding.description.setText(c.description);
        
        
        
        DB db = DBProvider.getDB(requireActivity());
        LiveData<StudipSemester[]> sems = db.semesterDao().observeAll();
        sems.observe(this, sms -> {
            sems.removeObservers(this);
            for (StudipSemester sem : sms) {
                if (sem.id.equals(c.start_semester)) {
                    binding.startSemester.setText(getString(R.string.start_semester, sem.title));
                }
                if (sem.id.equals(c.end_semester)) {
                    binding.endSemester.setText(getString(R.string.end_semester, sem.title));
                }
            }
        });
        
        b.setView(binding.getRoot());
        
        return b.create();
    }
}
