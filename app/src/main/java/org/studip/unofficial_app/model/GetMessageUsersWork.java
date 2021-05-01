package org.studip.unofficial_app.model;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipMessage;
import org.studip.unofficial_app.api.rest.StudipUser;
import org.studip.unofficial_app.model.room.DB;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.LinkedList;

import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetMessageUsersWork extends Worker
{
    public static final String WORK_NAME = "get_message_users";
    
    public GetMessageUsersWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        Context c = getApplicationContext();
        API api = APIProvider.getAPI(c);
        DB db = DBProvider.getDB(c);
        if (api != null) {
            StudipMessage[] messages = db.messagesDao().getAll();
            if (messages != null) {
                final boolean[] finished = new boolean[messages.length];
                int i = 0;
                for (StudipMessage m : messages) {
                    String uid = m.sender;
                    if (uid != null && db.userDao().get(uid) == null) {
                        final int finalI = i;
                        api.user.user(uid).enqueue(new Callback<StudipUser>()
                        {
                            @Override
                            public void onResponse(@NotNull Call<StudipUser> call, @NotNull Response<StudipUser> response) {
                                StudipUser u = response.body();
                                if (u != null) {
                                    db.userDao().updateInsertAsync(u).subscribeOn(Schedulers.io()).subscribe();
                                }
                                finished[finalI] = true;
                            }
                            @Override
                            public void onFailure(@NotNull Call<StudipUser> call, @NotNull Throwable t) {
                                finished[finalI] = true;
                            }
                        });
                        i++;
                    } else {
                        finished[i] = true;
                        i++;
                    }
                }
                //System.out.println("waiting to finish calls");
                boolean allFinished = false;
                while (! allFinished) {
                    allFinished = true;
                    for (boolean b : finished) {
                        if (! b) {
                            allFinished = false;
                            break;
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {}
                }
            }
        }
        //System.out.println("finished");
        return Result.success();
    }
}
