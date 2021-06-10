package org.studip.unofficial_app.api.rest;
import java.io.Serializable;

// from route dispatch.php/globalsearch/find?search=
public class StudipSearchResult implements Serializable
{
    public CourseResultList GlobalSearchMyCourses;
    public CourseResultList GlobalSearchCourses;
    public static class CourseResultList implements Serializable
    {
        public String name;
        public String fullsearch;
        public CourseResult[] content;
        public boolean more;
        public boolean plus;
    }
    public static class CourseResult implements Serializable
    {
        public String id;
        public String name;
        public String url;
        public String date;
        public String expand;
        public String img;
    }
    public ModuleResultList GlobalSearchModules;
    public static class ModuleResultList implements Serializable
    {
        public String name;
        public String fullsearch;
        public ModuleResult[] content;
        public boolean more;
        public boolean plus;
    }
    public static class ModuleResult implements Serializable
    {
        public String name;
        public String url;
        public String img;
        public String date;
        public String expand;
        public String additional;
    }
    public UserResultList GlobalSearchUsers;
    public static class UserResultList implements Serializable
    {
        public String name;
        public String fullsearch;
        public UserResult[] content;
        public boolean more;
        public boolean plus;
    }
    public static class UserResult implements Serializable
    {
        public String id;
        public String name;
        public String url;
        public String additional;
        public String expand;
        public String img;
    }
    public InstituteResultList GlobalSearchInstitutes;
    public static class InstituteResultList implements Serializable
    {
        public String name;
        public String fullsearch;
        public InstituteResult[] content;
        public boolean more;
        public boolean plus;
    }
    public static class InstituteResult implements Serializable
    {
        public String id;
        public String name;
        public String url;
        public String expand;
        public String img;
    }
}
