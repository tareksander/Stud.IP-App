package org.studip.unofficial_app.api.plugins.meetings;
import com.google.gson.annotations.SerializedName;
public class MeetingsConfig
{
    public Config config;
    public CourseConfig course_config;
    public static class Config {
        @SerializedName("BigBlueButton")
        public BigBlueButton bbb;
        public static class BigBlueButton {
            public String enable;
            public String display_name;
            // servers
            // features
            public String record;
            public String opencast;
            public String welcome;
            public String preupload;
            public String title;
            // config
            // server_defaults
            // server presets
            // server_course_type
            // server_details
        }
    }
    public static class CourseConfig {
        public String id;
        public String course_id;
        public String title;
        public String introduction;
        public Display display;
        public static class Display {
            public boolean addRoom;
            public boolean editRoom;
            public boolean deleteRoom;
            public boolean deleteRecording;
        }
    }
}
