package org.studip.unofficial_app.model.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import org.studip.unofficial_app.model.ForumResource;

public class ForumViewModel extends AndroidViewModel
{
    public final ForumResource f;
    public ForumViewModel(@NonNull Application application, String courseID) {
        super(application);
        f = new ForumResource(application, courseID);
    }
}
