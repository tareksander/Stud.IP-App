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
    public MessagesResource(Context c)
    {
        super(c);
    }

    @Override
    protected LiveData<StudipMessage[]> getDBData(Context c)
    {
        return DBProvider.getDB(c).messagesDao().observeAll();
    }

    @Override
    protected Call<StudipCollection<StudipMessage>> getCall(Context c)
    {
        // TODO only get the necessary messages, so get one at a time and stop if the first known message is encountered
        return Objects.requireNonNull(APIProvider.getAPI(c)).user.userBox(
                Objects.requireNonNull(APIProvider.getAPI(c)).getUserID(),"inbox",0,10000);
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
            //System.out.println(m.subject);
        }
        db.messagesDao().updateInsertMultiple(res.collection.values().toArray(new StudipMessage[0]));
    }
}
