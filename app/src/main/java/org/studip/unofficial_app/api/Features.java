package org.studip.unofficial_app.api;

import android.content.Context;

import org.studip.unofficial_app.R;

import java.util.HashMap;
import java.util.Set;

public class Features
{
    public static final String FEATURE_GLOBAL_NEWS = "news";
    public static final String FEATURE_FILES = "files";
    public static final String FEATURE_USER_FILES = "user_files";
    public static final String FEATURE_COURSE_FILES = "course_files";
    public static final String FEATURE_FORUM = "forum";
    public static final String FEATURE_MESSAGES = "messages";
    public static final String FEATURE_COURSES = "courses";
    public static final String FEATURE_PLANNER = "planner";
    public static final String FEATURE_BLUBBER = "blubber";
    
    
    
    public static void featureGlobalNews(Set<String> disabled, HashMap<String, HashMap<String, String>> f) {
        disabled.add(FEATURE_GLOBAL_NEWS);
        if (unavailable(f, "/studip/news", true, false, false, false)) {
            disabled.add(FEATURE_GLOBAL_NEWS);
            return;
        }
        if (unavailable(f, "/news/:news_id", true, false, false, false)) {
            disabled.add(FEATURE_GLOBAL_NEWS);
        }
    }
    
    public static void featureFiles(Set<String> disabled, HashMap<String, HashMap<String, String>> f) {
        disabled.add(FEATURE_FILES);
        if (unavailable(f, "/file/:file_ref_id", true, true, false, true)) {
            disabled.add(FEATURE_FILES);
            return;
        }
        if (unavailable(f, "/file/:file_ref_id/copy/:destination_folder_id", false, false, true, false)) {
            disabled.add(FEATURE_FILES);
            return;
        }
        if (unavailable(f, "/file/:file_ref_id/download", true, false, false, false)) {
            disabled.add(FEATURE_FILES);
            return;
        }
        if (unavailable(f, "/file/:file_ref_id/update", false, false, true, false)) {
            disabled.add(FEATURE_FILES);
            return;
        }
        if (unavailable(f, "/file/:folder_id", false, false, true, false)) {
            disabled.add(FEATURE_FILES);
            return;
        }
        if (unavailable(f, "/folder/:folder_id", true, true, false, true)) {
            disabled.add(FEATURE_FILES);
            return;
        }
        if (unavailable(f, "/folder/:folder_id/files", true, false, false, false)) {
            disabled.add(FEATURE_FILES);
            return;
        }
        if (unavailable(f, "/folder/:folder_id/subfolders", true, false, false, false)) {
            disabled.add(FEATURE_FILES);
            return;
        }
        if (unavailable(f, "/studip/content_terms_of_use_list", true, false, false, false)) {
            disabled.add(FEATURE_FILES);
            return;
        }
        if (unavailable(f, "/studip/file_system/folder_types", true, false, false, false)) {
            disabled.add(FEATURE_FILES);
            return;
        }
        if (unavailable(f, "/folder/:parent_folder_id/new_folder", false, false, true, false)) {
            disabled.add(FEATURE_FILES);
        }
    }
    
    public static void featureUserFiles(Set<String> disabled, HashMap<String, HashMap<String, String>> f) {
        disabled.add(FEATURE_USER_FILES);
        if (unavailable(f, "/user/:user_id/top_folder", true, false, false, false)) {
            disabled.add(FEATURE_USER_FILES);
        }
    }
    
    public static void featureCourseFiles(Set<String> disabled, HashMap<String, HashMap<String, String>> f) {
        if (unavailable(f, "/course/:course_id/top_folder", true, false, false, false)) {
            disabled.add(FEATURE_COURSE_FILES);
        }
    }
    
    public static void featureForum(Set<String> disabled, HashMap<String, HashMap<String, String>> f) {
        if (unavailable(f, "/course/:course_id/forum_categories", true, false, false, false)) {
            disabled.add(FEATURE_FORUM);
            return;
        }
        if (unavailable(f, "/forum_category/:category_id", true, false, false, false)) {
            disabled.add(FEATURE_FORUM);
            return;
        }
        if (unavailable(f, "/forum_category/:category_id/areas", true, false, true, false)) {
            disabled.add(FEATURE_FORUM);
            return;
        }
        if (unavailable(f, "/forum_entry/:entry_id", true, true, true, true)) {
            disabled.add(FEATURE_FORUM);
        }
    }
    
    public static void featureMessages(Set<String> disabled, HashMap<String, HashMap<String, String>> f) {
        if (unavailable(f, "/message/:message_id", true,true, false, true)) {
            disabled.add(FEATURE_MESSAGES);
            return;
        }
        if (unavailable(f, "/message/:message_id/file_folder", true, false, false, false)) {
            disabled.add(FEATURE_MESSAGES);
            return;
        }
        if (unavailable(f, "/messages", false, false, true, false)) {
            disabled.add(FEATURE_MESSAGES);
            return;
        }
        if (unavailable(f, "/user/:user_id/contacts", true, false, false, false)) {
            disabled.add(FEATURE_MESSAGES);
            return;
        }
        if (unavailable(f, "/user/:user_id/contacts/:friend_id", false, true, false,true )) {
            disabled.add(FEATURE_MESSAGES);
            return;
        }
        if (unavailable(f, "/user/:user_id/:box", true, false, false, false)) {
            disabled.add(FEATURE_MESSAGES);
        }
    }
    
    public static void featureCourses(Set<String> disabled, HashMap<String, HashMap<String, String>> f) {
        if (unavailable(f, "/course/:course_id", true, false, false, false)) {
            disabled.add(FEATURE_COURSES);
            return;
        }
        if (unavailable(f, "/course/:course_id/members", true, false, false, false)) {
            disabled.add(FEATURE_COURSES);
            return;
        }
        if (unavailable(f, "/course/:course_id/news", true, false, false, false)) {
            disabled.add(FEATURE_COURSES);
            return;
        }
        if (unavailable(f, "/user/:user_id/courses", true, false, false, false)) {
            disabled.add(FEATURE_COURSES);
            return;
        }
        if (unavailable(f, "/semesters", true, false, false, false)) {
            disabled.add(FEATURE_COURSES);
            return;
        }
        if (unavailable(f, "/semester/:semester_id", true, false, false, false)) {
            disabled.add(FEATURE_COURSES);
            return;
        }
        if (unavailable(f, "/news/:news_id/comments", true, false, true, false)) {
            disabled.add(FEATURE_COURSES);
        }
    }
    
    public static void featurePlanner(Set<String> disabled, HashMap<String, HashMap<String, String>> f) {
        if (unavailable(f, "/course/:course_id/events", true, false, false, false)) {
            disabled.add(FEATURE_PLANNER);
            return;
        }
        if (unavailable(f, "/user/:user_id/events", true, false, false, false)) {
            disabled.add(FEATURE_PLANNER);
            return;
        }
        if (unavailable(f, "/user/:user_id/schedule", true, false, false, false)) {
            disabled.add(FEATURE_PLANNER);
            return;
        }
        if (unavailable(f, "/user/:user_id/schedule/:semester_id", true, false, false, false)) {
            disabled.add(FEATURE_PLANNER);
        }
    }
    
    public static void featureBlubber(Set<String> disabled, HashMap<String, HashMap<String, String>> f) {
        if (unavailable(f, "/course/:course_id/blubber", true, false, true, false)) {
            disabled.add(FEATURE_BLUBBER);
            return;
        }
        if (unavailable(f, "/blubber/comment/:blubber_id", true, true, false, true)) {
            disabled.add(FEATURE_BLUBBER);
            return;
        }
        if (unavailable(f, "/blubber/posting/:blubber_id", true, true, false, true)) {
            disabled.add(FEATURE_BLUBBER);
            return;
        }
        if (unavailable(f, "/blubber/posting/:blubber_id/comments", true, false, true, false)) {
            disabled.add(FEATURE_BLUBBER);
            return;
        }
        if (unavailable(f, "/blubber/stream/:stream_id", true, false, false, false)) {
            disabled.add(FEATURE_BLUBBER);
            return;
        }
        if (unavailable(f, "/blubber/postings", false, false, true, false)) {
            disabled.add(FEATURE_BLUBBER);
        }
    }
    
    
    public static String listUnavailableFeatures(Set<String> disabled, Context c) {
        StringBuilder b = new StringBuilder();
        if (disabled.contains(FEATURE_GLOBAL_NEWS)) {
            b.append(c.getString(R.string.feature_global_news));
            b.append("<br>");
        }
        if (disabled.contains(FEATURE_FILES)) {
            b.append(c.getString(R.string.feature_files));
            b.append("<br>");
        }
        if (disabled.contains(FEATURE_USER_FILES)) {
            b.append(c.getString(R.string.feature_user_files));
            b.append("<br>");
        }
        if (disabled.contains(FEATURE_COURSE_FILES)) {
            b.append(c.getString(R.string.feature_course_files));
            b.append("<br>");
        }
        if (disabled.contains(FEATURE_FORUM)) {
            b.append(c.getString(R.string.feature_forum));
            b.append("<br>");
        }
        if (disabled.contains(FEATURE_MESSAGES)) {
            b.append(c.getString(R.string.feature_messages));
            b.append("<br>");
        }
        if (disabled.contains(FEATURE_COURSES)) {
            b.append(c.getString(R.string.feature_courses));
            b.append("<br>");
        }
        if (disabled.contains(FEATURE_PLANNER)) {
            b.append(c.getString(R.string.feature_planner));
            b.append("<br>");
        }
        if (disabled.contains(FEATURE_BLUBBER)) {
            b.append(c.getString(R.string.feature_blubber));
            b.append("<br>");
        }
        return b.toString();
    }
    
    
    private static boolean unavailable(HashMap<String, HashMap<String, String>> disc, String route, boolean get, boolean put, boolean post, boolean delete) {
        HashMap<String, String> f = disc.get(route);
        if (f == null) {
            return true;
        }
        if (get && ! f.containsKey("get")) {
            return true;
        }
        if (put && ! f.containsKey("put")) {
            return true;
        }
        if (post && ! f.containsKey("post")) {
            return true;
        }
        if (delete && ! f.containsKey("delete")) {
            return true;
        }
        return false;
    }
}
