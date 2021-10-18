package org.studip.unofficial_app.model.viewmodels;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipUser;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.NewsResource;

import io.reactivex.schedulers.Schedulers;
public class HomeViewModel extends AndroidViewModel
{
    private MutableLiveData<StudipUser> user;
    public final NewsResource news;
    public HomeViewModel(Application c)
    {
        super(c);
        news = new NewsResource(c,null);
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public LiveData<StudipUser> getUser(Context c)
    {
        if (user == null) {
            user = new MutableLiveData<>();
            StudipUser dummy = new StudipUser();
            dummy.name = new StudipUser.Name();
            dummy.name.given = "";
            user.setValue(dummy);
            //System.out.println("userID: "+APIProvider.getAPI(c).getUserID());
            API api = APIProvider.getAPI(c);
            if (api != null) {
                DBProvider.getDB(c).userDao().getSingle(api.getUserID()).subscribeOn(Schedulers.io()).subscribe((studipUser, throwable) ->
                {
                    if (throwable != null) {
                        throwable.printStackTrace();
                    }
                    if (studipUser != null) {
                        user.postValue(studipUser);
                    }
                });
            }
        }
        return user;
    }
}
