package org.studip.unofficial_app.api.rest;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class StudipNotifications implements Serializable
{
    public long server_timestamp;
    @SerializedName("PersonalNotifications.newNotifications")
    public Notification[] notifications;
    public PersonalNotifications personalnotifications;
    public static class PersonalNotifications implements Serializable {
        public Notification[] notifications;
    }
    public static class Notification implements Serializable {
        public long personal_notification_id;
        public String url;
        public String text;
        public String avatar;
        public String dialog;
        public String html_id;
        public String mkdate;
        public String id;
        public int more_unseen;
        public String html;
    }
}
