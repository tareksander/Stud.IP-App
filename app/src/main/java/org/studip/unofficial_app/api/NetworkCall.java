package org.studip.unofficial_app.api;
import android.util.Pair;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class NetworkCall<T>
{
    // 0 = IO-Error
    // 200 = OK
    // 401 = Unauthorized -> logged out
    // 403 = route inactive
    // 404 = invalidMethod / route not found
    // 405 = Method not Allowed
    private final MutableLiveData<Pair<T,Integer>> data = new MutableLiveData<>(new Pair<>(null,-1));
    public NetworkCall(Call<T> c) {
        c.enqueue(new Callback<T>()
        {
            @Override
            public void onResponse(@NotNull Call<T> call, @NotNull Response<T> response)
            {
                T res = response.body();
                data.setValue(new Pair<>(res,response.code()));
            }
            @Override
            public void onFailure(@NotNull Call<T> call, @NotNull Throwable t)
            {
                data.setValue(new Pair<>(null,0));
            }
        });
    }
    public void remove(LifecycleOwner l) {
        data.removeObservers(l);
    }
    public LiveData<Pair<T,Integer>> getData() {
        return data;
    }
}
