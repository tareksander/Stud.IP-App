package org.studip.unofficial_app.model.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import org.studip.unofficial_app.model.CoursesResource;
import org.studip.unofficial_app.model.SemesterResource;

public class CoursesViewModel  extends AndroidViewModel {
    
    
    public final SemesterResource semester;
    public final CoursesResource courses;
    public CoursesViewModel(@NonNull Application application) {
        super(application);
        courses = new CoursesResource(application);
        semester = new SemesterResource(application);
    }
}
