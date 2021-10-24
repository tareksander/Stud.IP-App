package org.studip.unofficial_app.model;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipMessage;
import org.studip.unofficial_app.model.room.DB;

import java.io.IOException;
import java.util.Objects;

public class NewMessagesWork extends Worker
{
    public static final String WORK_NAME = "new_messages_work_";
    
    
    public NewMessagesWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    
    
    @NonNull
    @Override
    public Result doWork() {
        final String box = getInputData().getString("box");
        Context c = getApplicationContext();
        API api = APIProvider.getAPI(c);
        DB db = DBProvider.getDB(c);
        if (api != null) {
            StudipMessage newm;
            int offset = 0;
            while (true) {
                try {
                    newm = Objects.requireNonNull(api.user.userBox(api.getUserID(), box, offset, 1).execute().body())
                            .collection.values().toArray(new StudipMessage[0])[0];
                    MessagesResource.preprocessMessage(newm);
                } catch (Exception e) {
                    break;
                }
                if (db.messagesDao().get(newm.message_id) != null) {
                    break;
                }
                db.messagesDao().updateInsert(newm);
                offset++;
            }
            
        }
        return Result.failure();
    }
}
