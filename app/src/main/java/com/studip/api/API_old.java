package com.studip.api;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.content.Context;
import android.content.SharedPreferences;

import javax.net.ssl.HttpsURLConnection;

import androidx.appcompat.app.AlertDialog;
import androidx.security.crypto.EncryptedSharedPreferences;

import com.google.gson.JsonObject;
import com.studip.R;

public class API_old
{
    
    private final String server; // hostname
    private ExecutorService exec = Executors.newFixedThreadPool(8);
    private ServerAuthenticator auth;
    private final String user_id_monitor = "";
    private volatile String user_id_cached;
    
    private static final String HTTPS = "https://";


    private static final String CREDENTIALS_SERVER = "credentials_server";
    private static final String CREDENTIALS_USERNAME = "credentials_username";
    private static final String CREDENTIALS_PASSWORD = "credentials_password";

    static final String api_user = "/api.php/user";
    static final String api_discovery = "/api.php/discovery";
    static final String api_global_colors = "/api.php/studip/colors";
    static final String api_global_settings = "/api.php/studip/settings";
    static final String api_courses = "/courses";
    static final String api_course = "/course";
    static final String api_file = "/api.php/file";
    static final String api_news_postfix = "/news";
    static final String api_courses_postfix = "/courses";
    static final String api_events_postfix = "/events";
    static final String api_limit_1000 = "?limit=1000";
    
    
    private class ServerAuthenticator extends Authenticator
    {
        PasswordAuthentication p;
        ServerAuthenticator(String username,char[] password)
        {
            p = new PasswordAuthentication(username, password);
        }
        @Override
        protected PasswordAuthentication getPasswordAuthentication()
        {
            //System.out.println("Protocol: "+getRequestingProtocol()+"\n Host: "+getRequestingHost());
            if (getRequestingProtocol().equals("https"))
            {
                if (getRequestingHost() != null)
                {
                    if (getRequestingHost().equals(server))
                    {
                        //System.out.println("Allow authentication");
                        return p;
                    }
                }
            }
            //System.out.println("Deny authentication");
            return null;
        }
    }
    
    
    
    
    
    
    
    public API_old(String server)
    {
        this.server = server;
    }
    
    public void save(SharedPreferences prefs)
    {
        if (prefs instanceof EncryptedSharedPreferences)
        {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(CREDENTIALS_SERVER,server);
            if (auth != null)
            {
                edit.putString(CREDENTIALS_USERNAME,auth.p.getUserName());
                edit.putString(CREDENTIALS_PASSWORD,String.valueOf(auth.p.getPassword()));
            }
            else
            {
                edit.remove(CREDENTIALS_USERNAME);
                edit.remove(CREDENTIALS_PASSWORD);
            }
            edit.apply();
        }
        else
        {
            System.err.println("not saving, as the preferences are not encrypted");
        }
    }
    public static API_old restore(SharedPreferences prefs, Context c) throws ExecutionException, InterruptedException
    {
        if (prefs instanceof EncryptedSharedPreferences)
        {
            String server = prefs.getString(CREDENTIALS_SERVER,null);
            if (server == null)
            {
                throw new NullPointerException("URL in shared preferences not found or not a String");
            }
            String username = prefs.getString(CREDENTIALS_USERNAME,null);
            String password = prefs.getString(CREDENTIALS_PASSWORD,null);
            if (username != null)
            {
                if (password == null)
                {
                    throw new NullPointerException("username found in shared preferences, but no password");
                }
                API_old api = new API_old(server);
                if (! api.login(username,password.toCharArray()).get())
                {
                    new AlertDialog.Builder(c).setTitle(R.string.login_error_title).setMessage(R.string.restore_error_message).show();
                }
                return api;
            }
            else
            {
                return new API_old(server);
            }
        }
        else
        {
            System.err.println("not loading, as the preferences are not encrypted");
            return null;
        }
    }
    
    
    public boolean logged_in()
    {
        if (auth != null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    public boolean logout(SharedPreferences prefs)
    {
        exec.shutdown();
        while (! exec.isTerminated())
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e) {return false;}
        }
        exec = Executors.newFixedThreadPool(4);
        auth = null;
        Authenticator.setDefault(new DenyAuthenticator());
        if (prefs instanceof EncryptedSharedPreferences)
        {
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove(CREDENTIALS_SERVER);
            edit.remove(CREDENTIALS_USERNAME);
            edit.remove(CREDENTIALS_PASSWORD);
            edit.apply();
        }
        else
        {
            System.err.println("not loading, as the preferences are not encrypted");
        }
        return true;
    }
    public Future<Boolean> login(String username,char[] password)
    {
        return exec.submit(new LoginRequest(username,password));
    }
    
    
    
    
    public String getUserID()
    {
        if (user_id_cached != null)
        {
            return user_id_cached;
        }
        else
        {
            synchronized (user_id_monitor)
            {
                try
                {
                    String json = (String) exec.submit(new UserDataRequest()).get();
                    if (json == null)
                    {
                        return null;
                    }
                    JsonObject tree = ResponseParser.parseQuery(json);
                    if (tree == null)
                    {
                        return null;
                    }
                    String userID = ResponseParser.getValue(tree,"user_id");
                    if (userID == null)
                    {
                        return null;
                    }
                    user_id_cached = userID;
                    return user_id_cached;
                }
                catch (Exception e)
                {
                    return null;
                }
            }
        }
    }


    public Future<String> getUserdata()
    {
        return exec.submit(new UserDataRequest());
    }
    public Future<String> getUserNews()
    {
        return exec.submit(new UserNewsRequest());
    }
    
    public Future<String> getUserCourses()
    {
        return exec.submit(new UserCoursesRequest());
    }

    public Future<String> getCourseEvents(String courseID)
    {
        return exec.submit(new CourseEventsRequest(courseID));
    }


    private class CourseEventsRequest implements Callable
    {
        String courseID;
        public CourseEventsRequest(String courseID)
        {
            this.courseID = courseID;
        }
        @Override
        public String call() throws Exception
        {
            try
            {
                HttpsURLConnection con = (HttpsURLConnection) new URL(HTTPS + server + api_course+"/"+courseID+api_events_postfix+api_limit_1000).openConnection();
                con.setInstanceFollowRedirects(false);
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                if (con.getResponseCode() == 200)
                {
                    BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder build = new StringBuilder();
                    String res;
                    while ((res = r.readLine()) != null)
                    {
                        build.append(res);
                    }
                    return build.toString();
                }
                else
                {
                    return "";
                }
            } catch (Exception e)
            {
                return "";
            }
        }
    }
    
    private class UserCoursesRequest implements Callable
    {
        @Override
        public String call() throws Exception
        {
            try
            {
                HttpsURLConnection con = (HttpsURLConnection) new URL(HTTPS + server + api_user+"/"+getUserID()+api_courses_postfix+api_limit_1000).openConnection();
                con.setInstanceFollowRedirects(false);
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                if (con.getResponseCode() == 200)
                {
                    BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder build = new StringBuilder();
                    String res;
                    while ((res = r.readLine()) != null)
                    {
                        build.append(res);
                    }
                    return build.toString();
                }
                else
                {
                    return "";
                }
            } catch (Exception e)
            {
                return "";
            }
        }
    }
    
    private class UserDataRequest implements Callable
    {
        @Override
        public String call() throws Exception
        {
            try
            {
                HttpsURLConnection con = (HttpsURLConnection) new URL(HTTPS + server + api_user).openConnection();
                con.setInstanceFollowRedirects(false);
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                if (con.getResponseCode() == 200)
                {
                    BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder build = new StringBuilder();
                    String res;
                    while ((res = r.readLine()) != null)
                    {
                        build.append(res);
                    }
                    return build.toString();
                }
                else
                {
                    return "";
                }
            } catch (Exception e)
            {
                return "";
            }
        }
    }
    
    
    private class UserNewsRequest implements Callable
    {
        @Override
        public String call() throws Exception
        {
            try
            {
                HttpsURLConnection con = (HttpsURLConnection) new URL(HTTPS + server + api_user + "/" + getUserID() + api_news_postfix).openConnection();
                con.setInstanceFollowRedirects(false);
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                if (con.getResponseCode() == 200)
                {
                    BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder build = new StringBuilder();
                    String res;
                    while ((res = r.readLine()) != null)
                    {
                        build.append(res);
                    }
                    return build.toString();
                }
                else
                {
                    System.out.print("error code: "+con.getResponseCode());
                    return "";
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                return "";
            }
        }
    }
    
    
    private class LoginRequest implements Callable
    {
        String username;
        char[] password;
        public LoginRequest(String username,char[] password)
        {
            this.username = username;
            this.password = password.clone();
        }
        @Override
        public Boolean call() throws Exception
        {
            try
            {
                auth = new ServerAuthenticator(username, password);
                Authenticator.setDefault(auth);
                for (int i = 0; i < password.length; i++)
                {
                    password[i] = '*';
                }
                HttpsURLConnection con = (HttpsURLConnection) new URL(HTTPS + server + api_discovery).openConnection();
                con.setInstanceFollowRedirects(false);
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.connect();
                //System.out.println("response code: " + con.getResponseCode());
                if (con.getResponseCode() == 200)
                {
                    con.disconnect();
                    return new Boolean(true);
                }
                else
                {
                    con.disconnect();
                    Authenticator.setDefault(new DenyAuthenticator());
                    auth = null;
                    return new Boolean(false);
                }
            } catch (Exception e)
            {
                //e.printStackTrace();
                Authenticator.setDefault(new DenyAuthenticator());
                auth = null;
                return new Boolean(false);
            }
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
}
