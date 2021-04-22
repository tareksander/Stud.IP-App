package org.studip.unofficial_app.api.rest;
import java.io.Serializable;

// from route dispatch.php/globalsearch/find?search=
public class StudipSearchResult implements Serializable
{
    public CourseResultList GlobalSearchMyCourses;
    public CourseResultList GlobalSearchCourses;
    public static class CourseResultList
    {
        public String name;
        public String fullsearch;
        public CourseResult[] content;
        public boolean more;
        public boolean plus;
    }
    public static class CourseResult
    {
        public String id;
        public String name;
        public String url;
        public String date;
        public String expand;
        public String img;
    }
    public ModuleResultList GlobalSearchModules;
    public static class ModuleResultList
    {
        public String name;
        public String fullsearch;
        public ModuleResult[] content;
        public boolean more;
        public boolean plus;
    }
    public static class ModuleResult
    {
        public String name;
        public String url;
        public String img;
        public String date;
        public String expand;
        public String additional;
    }
    public UserResultList GlobalSearchUsers;
    public static class UserResultList
    {
        public String name;
        public String fullsearch;
        public UserResult[] content;
        public boolean more;
        public boolean plus;
    }
    public static class UserResult
    {
        public String id;
        public String name;
        public String url;
        public String additional;
        public String expand;
        public String img;
    }
    public InstituteResultList GlobalSearchInstitutes;
    public static class InstituteResultList
    {
        public String name;
        public String fullsearch;
        public InstituteResult[] content;
        public boolean more;
        public boolean plus;
    }
    public static class InstituteResult
    {
        public String id;
        public String name;
        public String url;
        public String expand;
        public String img;
    }
}
