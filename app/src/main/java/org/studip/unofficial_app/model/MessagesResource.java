package org.studip.unofficial_app.model;

import android.content.Context;

import androidx.lifecycle.LiveData;

import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipMessage;
import org.studip.unofficial_app.model.room.DB;

import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
// cannot be generic for the type, so has to use raw types
public class MessagesResource extends NetworkResource<StudipMessage[]>
{
    public final String box;
    public final String sender;
    
    public MessagesResource(Context c, String box, String sender)
    {
        super(c);
        this.box = box;
        this.sender = sender;
    }

    @Override
    protected LiveData<StudipMessage[]> getDBData(Context c)
    {
        if ("inbox".equals(box)) {
            return DBProvider.getDB(c).messagesDao().observeAll();
        } else {
            return DBProvider.getDB(c).messagesDao().observeAllSender(sender);
        }
    }

    @Override
    protected Call<StudipCollection<StudipMessage>> getCall(Context c)
    {
        return Objects.requireNonNull(APIProvider.getAPI(c)).user.userBox(
                Objects.requireNonNull(APIProvider.getAPI(c)).getUserID(),box,0,10000);
    }
    
    public static void preprocessMessage(StudipMessage m) {
        m.sender = lastPathSegment(m.sender);
        if (m.recipients != null)
        {
            for (int i = 0; i < m.recipients.length; i++)
            {
                m.recipients[i] = lastPathSegment(m.recipients[i]);
            }
        }
        if (m.attachments != null)
        {
            for (int i = 0; i < m.attachments.length; i++)
            {
                m.attachments[i] = lastPathSegment(m.attachments[i]);
            }
        }
    }
    
    
    // cannot be generic for the type, so has to use raw types
    @SuppressWarnings("unchecked")
    @Override
    protected void updateDB(Context c, Object o)
    {
        StudipCollection<StudipMessage> res = (StudipCollection<StudipMessage>) o;
        DB db = DBProvider.getDB(c);
        for (Map.Entry<String, StudipMessage> entry : res.collection.entrySet()) {
            StudipMessage m = entry.getValue();
            m.sender = lastPathSegment(m.sender);
            preprocessMessage(m);
            //System.out.println(m.subject);
        }
        if ("inbox".equals(box)) {
            db.messagesDao().replaceMessages(res.collection.values().toArray(new StudipMessage[0]));
        } else {
            try {
                db.messagesDao().replaceMessagesSender(res.collection.values().toArray(new StudipMessage[0]), Objects.requireNonNull(APIProvider.getAPI(c)).getUserID());
            } catch (NullPointerException ignored) {}
        }
    }
}
