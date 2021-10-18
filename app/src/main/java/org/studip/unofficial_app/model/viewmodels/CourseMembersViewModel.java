package org.studip.unofficial_app.model.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import org.studip.unofficial_app.model.MembersResource;

public class CourseMembersViewModel extends AndroidViewModel
{
    public final MembersResource members;
    public CourseMembersViewModel(@NonNull Application application, String cid) {
        super(application);
        members = new MembersResource(application, cid);
    }
}
