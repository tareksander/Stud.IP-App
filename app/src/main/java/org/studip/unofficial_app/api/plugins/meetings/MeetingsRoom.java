package org.studip.unofficial_app.api.plugins.meetings;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MeetingsRoom  implements Serializable
{
    public String meeting_id; // the id used in the join link
    public String course_id;
    public String group_id;
    public String active;
    public String identifier;
    public String remote_id;
    public String name;
    public String recording_url;
    public String driver; // has to be "BigBlueButton" for this app to work
    public String server_index;
    public String attendee_password;
    public String moderator_password;
    public String user_id;
    public String join_as_moderator;
    public Features features;
    public String folder_id;
    public String mkdate;
    public String chdate;
    public boolean has_recordings;
    public Details details;
    public boolean enabled;
    
    public static class Features  implements Serializable {
        public boolean muteOnStart;
        public boolean room_anyone_can_start;
        public boolean webcamsOnlyForModerator;
        public boolean lockSettingsDisableCam;
        public String lockSettingsDisableMic;
        public boolean lockSettingsDisableNote;
        public boolean lockSettingsDisablePrivateChat;
        public int maxParticipants;
        public int duration;
        public String welcome;
        public String record;
        public boolean giveAccessToRecordings;
        public String logoutUrl;
        @SerializedName("guestPolicy-ALWAYS_ACCEPT")
        public String guestPolicy_ALWAYS_ACCEPT;
        @SerializedName("guestPolicy-ASK_MODERATOR")
        public String guestPolicy_ASK_MODERATOR;
    }
    public static class Details  implements Serializable {
        public String creator; // full name
        public String date; // full date
    }
}
