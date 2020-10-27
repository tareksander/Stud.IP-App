package com.studip.api;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.appcompat.app.AlertDialog;
import androidx.security.crypto.EncryptedSharedPreferences;

import com.studip.Data;
import com.studip.R;

import javax.net.ssl.HttpsURLConnection;

public class API
{
    private final String server; // hostname
    private final int EXECUTOR_THREADS_NUM = 10;
    private ExecutorService exec = Executors.newFixedThreadPool(EXECUTOR_THREADS_NUM);
    private ServerAuthenticator auth;
    private final String user_id_monitor = "";
    private volatile String user_id_cached;

    private static final String HTTPS = "https://";
    public static final String API = "/api.php/";
    
    public static final String CREDENTIALS_SERVER = "credentials_server";
    public static final String CREDENTIALS_USERNAME = "credentials_username";
    public static final String CREDENTIALS_PASSWORD = "credentials_password";
    
    private CookieStore cookies;
    
    
    
    private static final String route_discovery = "discovery";

    public static final int METHOD_GET = 1;
    public static final int METHOD_POST = 2;
    public static final int METHOD_PUT = 3;
    public static final int METHOD_DELETE = 4;
    public static final int METHOD_HEAD = 5;
    
    private final static String multipart_delimiter1 = "aqb2ig8al79fdj0dsj39fm39smb7y35d52xv24bx24dg26bds712ns843bdvaa1a87hnd923n9d2hfzg3r6t2f7623gf632gf67327f67w3tgr";
    
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

    public Future<byte[]> submitByteRoute(ByteRoute r)
    {
        return exec.submit(r);
    }
    
    public Future submitWithCallback(CallbackRoute r)
    {
        return exec.submit(r);
    }

    public Future submitWithByteCallback(CallbackByteRoute r)
    {
        return exec.submit(r);
    }

    public boolean login(String username,char[] password) throws Exception
    {
        CookieHandler.setDefault(new CookieManager(null,CookiePolicy.ACCEPT_ALL));
        cookies = ((CookieManager) CookieHandler.getDefault()).getCookieStore();
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
            if (cookies.getCookies().size() == 0)
            {
                return false;
            }
            else
            {
                return true;
            }
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
    public class CallbackByteRoute implements Runnable
    {
        private final ByteRoute r;
        private final ByteRouteCallback callback;
        public CallbackByteRoute(ByteRoute r, ByteRouteCallback callback)
        {
            this.r = r;
            this.callback = callback;
        }
        @Override
        public void run()
        {
            byte[] b;
            try
            {
                b = r.call();
            } catch (Exception e)
            {
                callback.setError(e);
                callback.run();
                return;
            }
            callback.setResult(b);
            callback.run();
        }
    }
    public abstract class Route implements Callable<String>
    {
        final String route;
        final String post_data;
        private final int method;
        private int recursive_call = 0;
        private final int recursive_call_limit = 20;
        public Route(String route,int method,String post_data)
        {
            this.route = route;
            this.method = method;
            this.post_data = post_data;
        }
        public Route(String route,int method)
        {
            this.route = route;
            this.method = method;
            post_data = null;
        }
        public Route(String route)
        {
            this.route = route;
            this.method = METHOD_GET;
            post_data = null;
        }
        public String getFullURL()
        {
            return HTTPS + server + API + route;
        }
        @Override
        public String call() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            //System.out.println("route called!");
            try
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
            } catch (ConnectException e) // it seems the server refuses connections if they happen too fast, so try again later
            {
                if (recursive_call >= recursive_call_limit)
                {
                    throw e;
                }
                recursive_call++;
                try
                {
                    Thread.sleep((int)Math.ceil(Math.random()*20));
                }
                catch (InterruptedException ignored) {}
                return call();
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
            r.close();
            con.disconnect();
            return builder.toString();
        }
        public void handleResponseCode(int code) throws AuthorisationException, RouteInactiveException, InvalidMethodException, IOException
        {
            switch (code)
            {
                case 201:
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
    public abstract class ByteRoute implements Callable<byte[]>
    {
        final String route;
        final byte[] post_data;
        private final int method;
        private int recursive_call = 0;
        private final int recursive_call_limit = 20;
        public ByteRoute(String route,int method,byte[] post_data)
        {
            this.route = route;
            this.method = method;
            this.post_data = post_data;
        }
        public ByteRoute(String route,int method)
        {
            this.route = route;
            this.method = method;
            this.post_data = null;
        }
        public ByteRoute(String route)
        {
            this.route = route;
            this.method = METHOD_GET;
            post_data = null;
        }
        public String getFullURL()
        {
            return HTTPS + server + API + route;
        }
        @Override
        public byte[] call() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            try
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
                    default:
                        throw new InvalidMethodException();
                }
            }
            catch (ConnectException e) // it seems the server refuses connections if they happen too fast, so try again later
            {
                if (recursive_call >= recursive_call_limit)
                {
                    throw e;
                }
                recursive_call++;
                try
                {
                    Thread.sleep((int) Math.ceil(Math.random() * 20));
                }
                catch (InterruptedException ignored) {}
                return call();
            }
        }
        public byte[] readConnection(HttpsURLConnection con) throws IOException
        {
            ByteBuffer b = ByteBuffer.allocate(con.getContentLength());
            InputStream s = con.getInputStream();
            byte[] tmp = new byte[100];
            int read;
            while ((read = s.read(tmp)) != -1)
            {
                b.put(tmp,0,read);
            }
            s.close();
            con.disconnect();
            return b.array();
        }
        public void handleResponseCode(int code) throws AuthorisationException, RouteInactiveException, InvalidMethodException, IOException
        {
            switch (code)
            {
                case 201:
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
        public abstract byte[] get() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException;
        public abstract byte[] post() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException;
        public abstract byte[] put() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException;
        public abstract byte[] delete() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException;
        public abstract byte[] head() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException;
    }
    public class BasicRoute extends Route
    {
        public BasicRoute(String route, int method,String post_data)
        {
            super(route, method, post_data);
        }
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
            //System.out.println(getFullURL());
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
            HttpsURLConnection con = (HttpsURLConnection) new URL(getFullURL()).openConnection();
            con.setRequestMethod("POST");
            con.setInstanceFollowRedirects(false);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            con.connect();
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
            w.write(post_data);
            w.close();
            handleResponseCode(con.getResponseCode());
            return readConnection(con);
        }
        @Override
        public String put() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            HttpsURLConnection con = (HttpsURLConnection) new URL(getFullURL()).openConnection();
            con.setRequestMethod("PUT");
            con.setInstanceFollowRedirects(false);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            con.connect();
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
            w.write(post_data);
            w.close();
            handleResponseCode(con.getResponseCode());
            return readConnection(con);
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
            HttpsURLConnection con = (HttpsURLConnection) new URL(getFullURL()).openConnection();
            con.setInstanceFollowRedirects(false);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setRequestMethod("HEAD");
            con.connect();
            handleResponseCode(con.getResponseCode());
            return readConnection(con);
        }
    }
    public class BlankRoute extends BasicRoute
    {
        String full_route;
        public BlankRoute(String full_route)
        {
            super("");
            this.full_route = full_route;
        }
        @Override
        public String getFullURL()
        {
            //System.out.println("full URL: "+HTTPS + server + full_route);
            return HTTPS + server + full_route;
        }
    }
    public class DispatchRoute extends BasicRoute
    {
        // Routes that aren't in the api, but are used by the site's javascript
        public DispatchRoute(String route, int method, String post_data)
        {
            super(route, method, post_data);
        }
        public DispatchRoute(String route, int method)
        {
            super(route, method);
        }
        public DispatchRoute(String route)
        {
            super(route);
        }
        @Override
        public String getFullURL()
        {
            return HTTPS + server + "/dispatch.php/" + route;
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
    public class MessageRoute extends BasicRoute
    {
        String messageID;
        public MessageRoute(String route, String messageID)
        {
            super(route);
            this.messageID = messageID;
        }
        public MessageRoute(String route, int method, String messageID)
        {
            super(route, method);
            this.messageID = messageID;
        }
        @Override
        public String getFullURL()
        {
            if (route.equals(""))
            {
                return HTTPS + server + API + "message/" + messageID;
            }
            else
            {
                return HTTPS + server + API + "message/" + messageID + "/" + route;
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

    public class FolderRoute extends BasicRoute
    {
        final String folderID;
        public FolderRoute(String route, int method,String folderID,String post_data)
        {
            super(route, method,post_data);
            this.folderID = folderID;
        }
        public FolderRoute(String route, int method,String folderID)
        {
            super(route, method);
            this.folderID = folderID;
        }
        public FolderRoute(String route,String folderID)
        {
            super(route);
            this.folderID = folderID;
        }
        @Override
        public String getFullURL()
        {
            if (route.equals(""))
            {
                return HTTPS + server + API + "folder/" + folderID;
            }
            else
            {
                return HTTPS + server + API + "folder/" + folderID + "/" + route;
            }
        }
    }

    public class FileRoute extends BasicRoute
    {
        final String fileID;
        public FileRoute(String route, int method,String fileID)
        {
            super(route, method);
            this.fileID = fileID;
        }
        public FileRoute(String route,String fileID)
        {
            super(route);
            this.fileID = fileID;
        }
        @Override
        public String getFullURL()
        {
            if (route.equals(""))
            {
                return HTTPS + server + API + "file/" + fileID;
            }
            else
            {
                return HTTPS + server + API + "file/" + fileID + "/" + route;
            }
        }
    }
    public class DownloadFileRoute extends ByteRoute
    {
        String fileID;
        public DownloadFileRoute(String fileID)
        {
            super("download");
            this.fileID = fileID;
        }

        @Override
        public String getFullURL()
        {
            return HTTPS + server + API + "file/" + fileID + "/download";
        }
        @Override
        public byte[] get() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
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
        public byte[] post() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            throw new InvalidMethodException();
        }

        @Override
        public byte[] put() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            throw new InvalidMethodException();
        }

        @Override
        public byte[] delete() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            throw new InvalidMethodException();
        }

        @Override
        public byte[] head() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            throw new InvalidMethodException();
        }
    }
    public static byte[] formatUploadData(String filename,byte[] data) throws IOException
    {
        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        o.write('\r');
        o.write('\n');
        o.write('-');
        o.write('-');
        o.write(multipart_delimiter1.getBytes());
        o.write('\r');
        o.write('\n');
        o.write(("Content-Disposition: form-data; name=\"_FILES\"; filename=\""+filename+"\"").getBytes());
        o.write('\r');
        o.write('\n');
        o.write("Content-Transfer-Encoding: binary".getBytes());
        o.write('\r');
        o.write('\n');
        o.write('\r');
        o.write('\n');
        o.write(data);
        o.write('\r');
        o.write('\n');
        o.write('-');
        o.write('-');
        o.write(multipart_delimiter1.getBytes());
        o.write('-');
        o.write('-');
        //System.out.println("data formatted");
        return o.toByteArray();
    }
    public class UploadFileRoute extends ByteRoute
    {
        String folderID;
        public UploadFileRoute(String folderID,byte[] post_data)
        {
            super("",METHOD_POST,post_data);
            this.folderID = folderID;
        }

        @Override
        public String getFullURL()
        {
            return HTTPS + server + API + "file/" + folderID;
        }
        @Override
        public byte[] get() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            throw new InvalidMethodException();
        }

        @Override
        public byte[] post() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            
            HttpsURLConnection con = (HttpsURLConnection) new URL(getFullURL()).openConnection();
            con.setRequestMethod("POST");
            con.setInstanceFollowRedirects(false);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type","multipart/form-data; boundary="+multipart_delimiter1);
            con.setRequestProperty("Content-Length",String.valueOf(post_data.length));
            con.connect();
            OutputStream w = con.getOutputStream();
            w.write(post_data);
            
            w.close();
            handleResponseCode(con.getResponseCode());
            return readConnection(con);
        }

        @Override
        public byte[] put() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            throw new InvalidMethodException();
        }

        @Override
        public byte[] delete() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            throw new InvalidMethodException();
        }

        @Override
        public byte[] head() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            throw new InvalidMethodException();
        }
    }
    public class SendMessageRoute extends BasicRoute
    {
        public SendMessageRoute(String post_data)
        {
            super(null,METHOD_POST, post_data);
        }
        @Override
        public String getFullURL()
        {
            return HTTPS + server + API + "messages";
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
