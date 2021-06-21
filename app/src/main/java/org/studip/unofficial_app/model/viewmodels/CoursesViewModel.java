package org.studip.unofficial_app.model.viewmodels;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import org.studip.unofficial_app.model.CoursesResource;
import org.studip.unofficial_app.model.SemesterResource;

public class CoursesViewModel  extends AndroidViewModel {
    
    public final MutableLiveData<Intent> forumIntent = new MutableLiveData<>();
    public final SemesterResource semester;
    public final CoursesResource courses;
    public CoursesViewModel(@NonNull Application application) {
        super(application);
        courses = new CoursesResource(application);
        semester = new SemesterResource(application);
    }
}
