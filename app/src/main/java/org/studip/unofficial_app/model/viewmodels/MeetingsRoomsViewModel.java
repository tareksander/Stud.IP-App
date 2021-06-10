package org.studip.unofficial_app.model.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.plugins.meetings.MeetingsRoom;
import org.studip.unofficial_app.model.APIProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeetingsRoomsViewModel extends AndroidViewModel
{
    private final MutableLiveData<MeetingsRoom[]> rooms;
    private final MutableLiveData<Boolean> status = new MutableLiveData<>(false);
    
    public MeetingsRoomsViewModel(@NonNull Application application, String cid, SavedStateHandle h) {
        super(application);
        rooms = h.getLiveData("rooms");
        if (rooms.getValue() == null) {
            API api = APIProvider.getAPI(application);
            if (api != null && api.getUserID() != null) {
                api.meetings.routes.getRooms(cid).enqueue(new Callback<MeetingsRoom[]>()
                {
                    @Override
                    public void onResponse(@NonNull Call<MeetingsRoom[]> call, @NonNull Response<MeetingsRoom[]> response) {
                        MeetingsRoom[] r = response.body();
                        if (r == null) {
                            System.out.println(response.code());
                            status.setValue(true);
                        }
                        else {
                            rooms.setValue(r);
                        }
                    }
            
                    @Override
                    public void onFailure(@NonNull Call<MeetingsRoom[]> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        status.setValue(true);
                    }
                });
            }
            else {
                status.setValue(true);
            }
        }
    }
    
    public LiveData<MeetingsRoom[]> getRooms() {
        return rooms;
    }
    
    public LiveData<Boolean> isError() {
        return status;
    }
}
