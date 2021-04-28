package org.studip.unofficial_app.model.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import org.studip.unofficial_app.model.MessagesResource;

public class MessagesViewModel extends AndroidViewModel
{
    public final MessagesResource mes;
    public MessagesViewModel(@NonNull Application application)
    {
        super(application);
        mes = new MessagesResource(application);
    }
}
