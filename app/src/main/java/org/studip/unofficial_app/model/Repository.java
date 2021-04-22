package org.studip.unofficial_app.model;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;

import org.jetbrains.annotations.NotNull;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Repository
{
    private static MutableLiveData<String> userID;
    private static final String USERID_KEY = "uid";
    
    public static LiveData<String> getUserID(Context c) {
        if (userID == null) {
            userID = new MutableLiveData<>();
            userID.setValue(null);
            EncryptedSharedPreferences prefs = APIProvider.getPrefs(c);
            if (prefs != null && prefs.contains(USERID_KEY)) {
                userID.postValue(prefs.getString(USERID_KEY,null));
            } else {
                API api = APIProvider.getAPI(c);
                final Context app = c.getApplicationContext();
                if (api != null) {
                    api.user.user().enqueue(new Callback<StudipUser>()
                    {
                        @Override
                        public void onResponse(@NotNull Call<StudipUser> call, @NotNull Response<StudipUser> response)
                        {
                            if (response.body() != null) {
                                userID.setValue(response.body().user_id);
                                EncryptedSharedPreferences prefs = APIProvider.getPrefs(app);
                                if (prefs != null) {
                                    prefs.edit().putString(USERID_KEY,response.body().user_id).apply();
                                }
                            }
                        }
                        @Override
                        public void onFailure(@NotNull Call<StudipUser> call, @NotNull Throwable t) {}
                    });
                }
            }
        } else {
            
        }
        return userID;
    }
    
    
    
    
    
    
    
    
    
    
    
    
}
