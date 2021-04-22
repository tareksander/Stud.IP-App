package org.studip.unofficial_app.model;
import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public abstract class NetworkResource<T,R>
{
    protected abstract LiveData<T> getDBData(Context c);
    protected abstract Call<R> getCall(Context c);
    protected abstract void updateDB(Context c,R res);
    
    protected final MutableLiveData<Integer> status = new MutableLiveData<>(-1);
    protected final MutableLiveData<Boolean> refreshing = new MutableLiveData<>(false);
    private final LiveData<T> res;
    public NetworkResource(Context c) {
        res = Transformations.distinctUntilChanged(getDBData(c));
    }
    
    public LiveData<T> get() {
        return res;
    }
    
    public LiveData<Integer> getStatus() {
        return status;
    }
    public LiveData<Boolean> isRefreshing() {
        return refreshing;
    }
    
    public void refresh(Context con) {
        Context c = con.getApplicationContext();
        if (! refreshing.getValue()) {
            refreshing.setValue(true);
            getCall(con).enqueue(new Callback<R>()
            {
                @Override
                public void onResponse(@NotNull Call<R> call, @NotNull Response<R> response)
                {
                    R res = response.body();
                    status.setValue(response.code());
                    if (res != null) {
                        new Thread(() -> {
                            try {
                                updateDB(c,res);
                            } catch (Exception ignored) {}
                            refreshing.postValue(false);
                        }).start();
                    } else {
                        refreshing.setValue(false);
                    }
                }
                @Override
                public void onFailure(@NotNull Call<R> call, @NotNull Throwable t)
                {
                    refreshing.setValue(false);
                }
            });
        }
    }
}
