package org.studip.unofficial_app.model.viewmodels;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.studip.unofficial_app.model.MessagesResource;

public class MessagesViewModel extends AndroidViewModel
{
    public final MessagesResource mes;
    public final MutableLiveData<Intent> source = new MutableLiveData<>();
    public final MutableLiveData<String> open = new MutableLiveData<>();
    public MessagesViewModel(@NonNull Application application)
    {
        super(application);
        mes = new MessagesResource(application);
    }
}
