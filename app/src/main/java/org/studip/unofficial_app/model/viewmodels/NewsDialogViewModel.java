package org.studip.unofficial_app.model.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import org.studip.unofficial_app.model.NewsResource;

public class NewsDialogViewModel extends AndroidViewModel
{
    public final NewsResource news;
    public NewsDialogViewModel(@NonNull Application application, String cid) {
        super(application);
        news = new NewsResource(application,cid);
    }
}
