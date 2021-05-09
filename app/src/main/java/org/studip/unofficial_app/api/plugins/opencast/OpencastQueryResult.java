package org.studip.unofficial_app.api.plugins.opencast;
import com.google.gson.annotations.SerializedName;
public class OpencastQueryResult
{
    @SerializedName("search-results")
    public SearchResults search_results;
    public static class SearchResults {
        public Result result;
        public static class Result {
            public String id;
            public String org;
            public MediaPackage mediapackage;
            public static class MediaPackage {
                public String title;
                public String series;
                public Media media;
                public static class Media {
                    public Track[] track;
                    public static class Track {
                        public String id;
                        public String type;
                        public String mimetype;
                        public String url;
                        public Video video;
                        public static class Video {
                            public String id;
                            public String resolution;
                        }
                    }
                }
            }
        }
    }
}
