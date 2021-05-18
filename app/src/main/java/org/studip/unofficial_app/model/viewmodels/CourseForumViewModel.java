package org.studip.unofficial_app.model.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import org.studip.unofficial_app.model.ForumResource;

public class CourseForumViewModel extends AndroidViewModel
{
    public final ForumResource f;
    public CourseForumViewModel(@NonNull Application application, String course)
    {
        super(application);
        f = new ForumResource(application,course);
    }
}
