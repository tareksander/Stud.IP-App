package com.studip.api;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;
import androidx.security.crypto.EncryptedSharedPreferences;

import com.studip.Data;
import com.studip.R;

import javax.net.ssl.HttpsURLConnection;

public class API
{
    private final String server; // hostname
    private final int EXECUTOR_THREADS_NUM = 8;
    private ExecutorService exec = Executors.newFixedThreadPool(EXECUTOR_THREADS_NUM);
    private ServerAuthenticator auth;
    private final String user_id_monitor = "";
    private volatile String user_id_cached;

    private static final String HTTPS = "https://";
    private static final String API = "/api.php/";
    
    public static final String CREDENTIALS_SERVER = "credentials_server";
    public static final String CREDENTIALS_USERNAME = "credentials_username";
    public static final String CREDENTIALS_PASSWORD = "credentials_password";
    
    
    private static final String route_discovery = "discovery";

    private static final int METHOD_GET = 1;
    private static final int METHOD_POST = 2;
    private static final int METHOD_PUT = 3;
    private static final int METHOD_DELETE = 4;
    private static final int METHOD_HEAD = 5;
    
    public API(String server)
    {
        this.server = server;
    }
    
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
            if (getRequestingProtocol().equals("https"))
            {
                if (getRequestingHost() != null)
                {
                    if (getRequestingHost().equals(server))
                    {
                        return p;
                    }
                }
            }
            return null;
        }
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
    public static API restore(SharedPreferences prefs, Context c) throws ExecutionException, InterruptedException
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
                API api = new API(server);
                try
                {
                    if (! api.login(username,password.toCharArray()))
                    {
                        new AlertDialog.Builder(c).setTitle(R.string.login_error_title).setMessage(R.string.restore_error_message).show();
                    }
                }
                catch (Exception e)
                {
                    new AlertDialog.Builder(c).setTitle(R.string.login_error_title).setMessage(R.string.restore_error_message).show();
                }
                return api;
            }
            else
            {
                return new API(server);
            }
        }
        else
        {
            System.err.println("not loading, as the preferences are not encrypted");
            return null;
        }
    }
    
    public boolean serverReachable()
    {
        Future<String> f = exec.submit(new BasicRoute(route_discovery));
        try
        {
            f.get();
        } catch (Exception ex)
        {
            if (ex instanceof ExecutionException)
            {
                Throwable e = ex.getCause();
                if (!(e instanceof InvalidMethodException) && !(e instanceof AuthorisationException) && !(e instanceof RouteInactiveException))
                {
                    return false;
                }
            }
            return false;
        }
        return true;
    }
    
    public boolean api_enabled() throws Exception
    {
        Future<String> f = exec.submit(new BasicRoute(route_discovery));
        try
        {
            f.get();
        } catch (Exception e)
        {
            if (e instanceof ExecutionException)
            {
                if (e.getCause() instanceof RouteInactiveException)
                    return false;
                else
                    throw e;
            }
            else
            {
                throw e;
            }
        }
        return true;
    }

    public Future<String> submit(Route r)
    {
        return exec.submit(r);
    }
    
    public Future submitWithCallback(CallbackRoute r)
    {
        return exec.submit(r);
    }

    public boolean login(String username,char[] password) throws Exception
    {
        try
        {
            auth = new ServerAuthenticator(username,password);
            Authenticator.setDefault(auth);
            exec.submit(new BasicRoute(route_discovery)).get();
        } catch (Exception e)
        {
            auth = null;
            Authenticator.setDefault(new DenyAuthenticator());
            if (e instanceof ExecutionException)
            {
                if (e.getCause() instanceof IOException)
                {
                    throw (IOException) e.getCause();
                }
                if (e.getCause() instanceof AuthorisationException)
                {
                    return false;
                }
                throw e;
            }
            throw e;
        }
        return true;
    }
    
    public void logout(SharedPreferences prefs)
    {
        exec.shutdown();
        try
        {
            exec.awaitTermination(1000, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {}
        exec.shutdownNow();
        Authenticator.setDefault(new DenyAuthenticator());
        auth = null;
        save(prefs);
        Data.user = null;
        exec = Executors.newFixedThreadPool(EXECUTOR_THREADS_NUM);
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
    
    public class CallbackRoute implements Runnable
    {
        private final Route r;
        private final RouteCallback callback;
        public CallbackRoute(Route r, RouteCallback callback)
        {
            this.r = r;
            this.callback = callback;
        }
        @Override
        public void run()
        {
            String s;
            try
            {
                s = r.call();
            } catch (Exception e)
            {
                callback.setError(e);
                callback.run();
                return;
            }
            callback.setResult(s);
            callback.run();
        }
    }
    
    public abstract class Route implements Callable<String>
    {
        final String route;
        private final int method;
        public Route(String route,int method)
        {
            this.route = route;
            this.method = method;
        }
        public Route(String route)
        {
            this.route = route;
            this.method = METHOD_GET;
        }
        public String getFullURL()
        {
            return HTTPS + server + API + route;
        }
        @Override
        public String call() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            switch (method)
            {
                case METHOD_GET:
                    return get();
                case METHOD_POST:
                    return post();
                case METHOD_PUT:
                    return put();
                case METHOD_DELETE:
                    return delete();
                case METHOD_HEAD:
                    return head();
            }
            throw new InvalidMethodException();
        }
        public String readConnection(HttpsURLConnection con) throws IOException
        {
            BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String s;
            while ((s = r.readLine()) != null)
            {
                builder.append(s);
            }
            con.disconnect();
            return builder.toString();
        }
        public void handleResponseCode(int code) throws AuthorisationException, RouteInactiveException, InvalidMethodException, IOException
        {
            switch (code)
            {
                case 200:
                    return;
                case 401:
                    throw new AuthorisationException();
                case 403:
                    throw new RouteInactiveException();
                case 404:
                    throw new InvalidMethodException();
                default:
                    throw new IOException("unexpected status code: "+code);
            }
        }
        public abstract String get() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException;
        public abstract String post() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException;
        public abstract String put() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException;
        public abstract String delete() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException;
        public abstract String head() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException;
    }
    public class BasicRoute extends Route
    {
        public BasicRoute(String route, int method)
        {
            super(route, method);
        }
        public BasicRoute(String route)
        {
            super(route);
        }
        @Override
        public String get() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            HttpsURLConnection con = (HttpsURLConnection) new URL(getFullURL()).openConnection();
            con.setInstanceFollowRedirects(false);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.connect();
            handleResponseCode(con.getResponseCode());
            return readConnection(con);
        }
        @Override
        public String post() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            throw new InvalidMethodException();
        }
        @Override
        public String put() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            throw new InvalidMethodException();
        }
        @Override
        public String delete() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            HttpsURLConnection con = (HttpsURLConnection) new URL(getFullURL()).openConnection();
            con.setInstanceFollowRedirects(false);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setRequestMethod("DELETE");
            con.connect();
            handleResponseCode(con.getResponseCode());
            return readConnection(con);
        }

        @Override
        public String head() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            throw new InvalidMethodException();
        }
    }
    
    public class UserRoute extends BasicRoute
    {
        final String userID;
        public UserRoute(String route, int method,String userID)
        {
            super(route, method);
            this.userID = userID;
        }
        public UserRoute(String route,String userID)
        {
            super(route);
            this.userID = userID;
        }
        @Override
        public String getFullURL()
        {
            if (route.equals(""))
            {
                return HTTPS + server + API + "user/" + userID;
            }
            else
            {
                return HTTPS + server + API + "user/" + userID + "/" + route;
            }
        }
    }

    public class CourseRoute extends BasicRoute
    {
        final String courseID;
        public CourseRoute(String route, int method,String courseID)
        {
            super(route, method);
            this.courseID = courseID;
        }
        public CourseRoute(String route,String courseID)
        {
            super(route);
            this.courseID = courseID;
        }
        @Override
        public String getFullURL()
        {
            if (route.equals(""))
            {
                return HTTPS + server + API + "course/" + courseID;
            }
            else
            {
                return HTTPS + server + API + "course/" + courseID + "/" + route;
            }
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
