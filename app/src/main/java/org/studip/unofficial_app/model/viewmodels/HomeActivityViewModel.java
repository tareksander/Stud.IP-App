package org.studip.unofficial_app.model.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.ui.HomeActivity;

public class HomeActivityViewModel extends ViewModel
{
    private static final String FILES_COURSE_KEY = "files_course";
    private final SavedStateHandle h;
    public HomeActivityViewModel(SavedStateHandle h) {
        this.h = h;
        filesCourse = h.getLiveData(FILES_COURSE_KEY);
    }
    
    public void setFilesCourse(StudipCourse c) {
        h.set(FILES_COURSE_KEY,c);
    }
    
    public final LiveData<StudipCourse> filesCourse;
    public final MutableLiveData<Boolean> connectionLostDialogShown = new MutableLiveData<>(false);
    
}
