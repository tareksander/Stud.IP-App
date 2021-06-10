package org.studip.unofficial_app.model.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.Features;
import org.studip.unofficial_app.api.rest.StudipFolder;
import org.studip.unofficial_app.model.APIProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileViewModel extends ViewModel
{
    private final MutableLiveData<StudipFolder> folder = new MutableLiveData<>();
    private final MutableLiveData<Boolean> refreshing = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> status = new MutableLiveData<>(-1);
    
    
    
    private static final String COURSE_KEY = "isCourse";
    private static final String FOLDER_KEY = "currentFolder";
    
    private LiveData<Boolean> courseID;
    private LiveData<String> folderID;
    
    private final SavedStateHandle h;
    
    public LiveData<Integer> getStatus() {
        return status;
    }
    public LiveData<Boolean> isRefreshing() {
        return refreshing;
    }
    
    
    public FileViewModel(SavedStateHandle h) {
        this.h = h;
        courseID = h.getLiveData(COURSE_KEY, false);
        folderID = h.getLiveData(FOLDER_KEY, null);
    }
    
    public void setFolder(Context c, String folderID, boolean courseID) {
        h.set(COURSE_KEY, courseID);
        h.set(FOLDER_KEY, folderID);
        refresh(c);
    }
    
    
    
    public LiveData<StudipFolder> get() {
        return folder;
    }
    
    public void refresh(Context con) {
        Context c = con.getApplicationContext();
        if (! refreshing.getValue()) {
            refreshing.setValue(true);
            Call<StudipFolder> call = null;
            if (folderID.getValue() != null) {
                if (courseID.getValue())
                {
                    call = APIProvider.getAPI(c).course.folder(folderID.getValue());
                } else {
                    call = APIProvider.getAPI(c).folder.get(folderID.getValue());
                }
            } else {
                API api = APIProvider.getAPI(c);
                if (api.isFeatureEnabled(Features.FEATURE_USER_FILES)) {
                    call = APIProvider.getAPI(c).user.userFolder(APIProvider.getAPI(c).getUserID());
                }
            }
            if (call != null) {
                call.enqueue(new Callback<StudipFolder>()
                {
                    @Override
                    public void onResponse(@NotNull Call<StudipFolder> call, @NotNull Response<StudipFolder> response) {
                        StudipFolder res = response.body();
                        status.setValue(response.code());
                        if (res != null) {
                            folder.setValue(res);
                        }
                        refreshing.setValue(false);
                    }
        
                    @Override
                    public void onFailure(@NotNull Call<StudipFolder> call, @NotNull Throwable t) {
                        t.printStackTrace();
                        refreshing.setValue(false);
                    }
                });
            } else {
                status.setValue(200);
                folder.setValue(null);
                refreshing.setValue(false);
            }
        }
    }
    
}
