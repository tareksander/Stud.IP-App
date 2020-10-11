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

import android.app.Activity;

import javax.net.ssl.HttpsURLConnection;

public class API2
{
    private final String server; // hostname
    private ExecutorService exec = Executors.newFixedThreadPool(8);
    private ServerAuthenticator auth;
    private final String user_id_monitor = "";
    private volatile String user_id_cached;

    private static final String HTTPS = "https://";
    private static final String API = "/api.php/";
    
    private static final String CREDENTIALS_SERVER = "credentials_server";
    private static final String CREDENTIALS_USERNAME = "credentials_username";
    private static final String CREDENTIALS_PASSWORD = "credentials_password";
    
    
    private static final String route_discovery = "discovery";

    private static final int METHOD_GET = 1;
    private static final int METHOD_POST = 2;
    private static final int METHOD_PUT = 3;
    private static final int METHOD_DELETE = 4;

    public API2(String server)
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

    public Future submit(Route r)
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
            exec.submit(new BasicRoute(route_discovery)).get();
        } catch (Exception e)
        {
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
    
    public class CallbackRoute implements Runnable
    {
        private Route r;
        private RouteCallback callback;
        private Activity a;
        public CallbackRoute(Route r, RouteCallback callback, Activity a)
        {
            this.r = r;
            this.callback = callback;
            this.a = a;
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
                a.runOnUiThread(callback);
                return;
            }
            callback.setResult(s);
            a.runOnUiThread(callback);
        }
    }
    
    public abstract class Route implements Callable<String>
    {
        private String route;
        private int method;
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
            HttpsURLConnection con = (HttpsURLConnection) new URL(getFullURL()).openConnection();
            con.setInstanceFollowRedirects(false);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setRequestMethod("POST");
            con.connect();
            handleResponseCode(con.getResponseCode());
            return readConnection(con);
        }
        @Override
        public String put() throws IOException, InvalidMethodException, AuthorisationException, RouteInactiveException
        {
            HttpsURLConnection con = (HttpsURLConnection) new URL(getFullURL()).openConnection();
            con.setInstanceFollowRedirects(false);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setRequestMethod("PUT");
            con.connect();
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
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
