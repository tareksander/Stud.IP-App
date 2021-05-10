package org.studip.unofficial_app.model.viewmodels;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.studip.unofficial_app.api.plugins.opencast.OpencastVideo;
import org.studip.unofficial_app.model.APIProvider;

public class OpencastViewModel extends AndroidViewModel
{
    private final MutableLiveData<Boolean> status = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> refreshing = new MutableLiveData<>(false);
    private final MutableLiveData<OpencastVideo[]> data = new MutableLiveData<>();
    private final String course;
    
    public OpencastViewModel(@NonNull Application application, String course) {
        super(application);
        this.course = course;
        refresh(application);
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void refresh(Context c) {
        if (! refreshing.getValue()) {
            refreshing.setValue(true);
            APIProvider.getAPI(c).opencast.getOpencast(course).subscribe((opencastVideos, throwable) -> {
                //System.out.println("fetched");
                refreshing.postValue(false);
                if (opencastVideos == null) {
                    status.postValue(true);
                } else {
                    data.postValue(opencastVideos);
                }
            });
        }
    }
    
    public LiveData<OpencastVideo[]> getVideos() {
        return data;
    }
    
    public LiveData<Boolean> isRefreshing() {
        return refreshing;
    }
    
    public LiveData<Boolean> isError() {
        return status;
    }
    
}
