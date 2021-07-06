package org.studip.unofficial_app.api;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;

import org.jetbrains.annotations.NotNull;
import org.studip.unofficial_app.api.plugins.courseware.Courseware;
import org.studip.unofficial_app.api.plugins.meetings.Meetings;
import org.studip.unofficial_app.api.plugins.opencast.Opencast;
import org.studip.unofficial_app.api.rest.StudipUser;
import org.studip.unofficial_app.api.routes.Course;
import org.studip.unofficial_app.api.routes.Discovery;
import org.studip.unofficial_app.api.routes.Dispatch;
import org.studip.unofficial_app.api.routes.File;
import org.studip.unofficial_app.api.routes.Folder;
import org.studip.unofficial_app.api.routes.Forum;
import org.studip.unofficial_app.api.routes.Message;
import org.studip.unofficial_app.api.routes.OAuth;
import org.studip.unofficial_app.api.routes.Semester;
import org.studip.unofficial_app.api.routes.Studip;
import org.studip.unofficial_app.api.routes.User;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.GsonProvider;
import org.studip.unofficial_app.model.Settings;
import org.studip.unofficial_app.model.SettingsProvider;
import org.studip.unofficial_app.model.room.UserDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.schedulers.Schedulers;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class API
{
    
    public final Retrofit retrofit;
    private final ConcurrentHashMap<String, List<Cookie>> cookies = new ConcurrentHashMap<>();
    
    private static final String AUTH_COOKIE_KEY = "cookie";
    private static final String HOSTNAME_KEY = "hostname";
    private static final String AUTH_COOKIE_NAME = "Seminar_Session";
    private static final String USERID_KEY = "user_id";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String DISABLED_FEATURES_KEY = "disabled features";
    private static final String OAUTH_TOKEN_KEY = "oauth_token";
    private static final String OAUTH_TOKEN_SECRET_KEY = "oauth_token_secret";
    
    
    public static final String HTTPS = "https://";
    
    
    
    
    public final Discovery discovery;
    public final Course course;
    public final File file;
    public final Folder folder;
    public final Forum forum;
    public final Message message;
    public final Studip studip;
    public final User user;
    public final Semester semester;
    public final Dispatch dispatch;
    public final OAuth oauth;
    
    
    public final Opencast opencast;
    public final Courseware courseware;
    public final Meetings meetings;
    
    //private final TestRoutes tests;
    
    private final String hostname;
    
    
    private int auth_method = 0;
    
    //private String folder_id = null;
    private String userID = null;
    private String username = null;
    private String password = null;
    
    private OAuthUtils.OAuthToken oauth_token;
    
    private Set<String> disabled_features = new HashSet<>();
    
    
    public API(String hostname) {
        this.hostname = hostname;
        retrofit = new Retrofit.Builder()
                .baseUrl(HTTPS+hostname+"/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(GsonProvider.getGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .client(new OkHttpClient.Builder().cookieJar(new CookieJar()
                {
                    // simple cookie jar implementation
                    @Override
                    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list)
                    {
                        if (httpUrl.isHttps())
                        {
                            // sync the cookies with the WebViews, used for the Meetings plugin with cookie auth
                            CookieManager m = CookieManager.getInstance();
                            for (Cookie c : list)
                            {
                                m.setCookie(httpUrl.host(), c.toString());
                            }
                            List<Cookie> clist = cookies.get(httpUrl.host());
                            if (clist != null)
                            {
                                for (Cookie c : list)
                                {
                                    if (!clist.contains(c))
                                    {
                                        clist.add(c);
                                    }
                                }
                            }
                            else
                            {
                                cookies.put(httpUrl.host(), new LinkedList<>(list));
                            }
                        }
                    }

                    @NotNull
                    @Override
                    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl)
                    {
                        if (httpUrl.isHttps()) {
                            List<Cookie> clist = cookies.get(httpUrl.host());
                            if (clist == null) {
                                return Collections.emptyList();
                            } else {
                                //System.out.println("Cookies "+clist.toString()+" send to URL "+httpUrl.toString());
                                return clist;
                            }
                        } else {
                            //System.out.println("cookies denied for: "+httpUrl.toString());
                            return Collections.emptyList();
                        }
                    }
                }).authenticator((route, response) ->
                {
                    if (response.request().header("Authorization") != null) {
                        return null;
                    }
                    if (route != null && route.address().url().isHttps() && route.address().url().host().equals(hostname.split("/")[0])) {
                        if (password != null && auth_method == Settings.AUTHENTICATION_BASIC) {
                            //System.out.println("Basic Authentication used for "+route.address().url().toString());
                            return response.request().newBuilder().header("Authorization", Credentials.basic(username, password)).build();
                        }
                        if (oauth_token != null && ! oauth_token.isTemp && auth_method == Settings.AUTHENTICATION_OAUTH) {
                            //System.out.println("OAuth Authentication used for "+route.address().url().toString());
                            OAuthUtils.OAuthData o = OAuthUtils.hosts.get(hostname);
                            if (o != null) {
                                // OAuthUtils.getAuthHeader(this, o, oauth_token, uri)
                                //System.out.println(response.request().url().toString());
                                TreeMap<String, String> query = new TreeMap<>();
                                for (String name : response.request().url().queryParameterNames()) {
                                    query.put(name, response.request().url().queryParameter(name));
                                }
                                return response.request().newBuilder().header("Authorization",
                                        OAuthUtils.getAuthHeader(this, o, oauth_token, response.request().url().encodedPath(),
                                                response.request().method(), query)).build();
                            }
                        }
                    //} else {
                        //System.out.println("auth denied for: "+route.address().url().toString());
                    }
                    return null;
                }).build())
                .build();
        discovery = retrofit.create(Discovery.class);
        course = retrofit.create(Course.class);
        file = retrofit.create(File.class);
        folder = retrofit.create(Folder.class);
        forum = retrofit.create(Forum.class);
        message = retrofit.create(Message.class);
        studip = retrofit.create(Studip.class);
        user = retrofit.create(User.class);
        semester = retrofit.create(Semester.class);
        dispatch = retrofit.create(Dispatch.class);
        oauth = retrofit.create(OAuth.class);
        
        //tests = retrofit.create(TestRoutes.class);
        
        opencast = new Opencast(retrofit);
        courseware = new Courseware(retrofit);
        meetings = new Meetings(retrofit);
    }
    
    public boolean isFeatureEnabled(String feature) {
        return ! disabled_features.contains(feature);
    }
    
    public void downloadFile(@NonNull Context con, @NonNull String fid, String filename, boolean url) {
        DownloadManager m = (DownloadManager) con.getSystemService(Context.DOWNLOAD_SERVICE);
        
        String uri = HTTPS+hostname;
        if (url) {
            uri += fid;
        } else {
            uri += "/api.php/file/"+fid+"/download";
        }
        //System.out.println(uri);
        if (URLUtil.isHttpsUrl(uri))
        {
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(uri));
            if (filename == null) {
                r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                
            } else {
                String[] parts = filename.split("\\.");
                String extension = null;
                if (parts.length != 0) {
                    extension = parts[parts.length - 1];
                }
                //System.out.println(extension);
                if (extension != null) {
                    //System.out.println(MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension));
                    r.setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension));
                }
                r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                r.setVisibleInDownloadsUi(true);
                r.allowScanningByMediaScanner();
    
                r.setTitle(filename);
                r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            }
            
            
            
            boolean authed = false;
            
            
            if (auth_method != Settings.AUTHENTICATION_COOKIE && ! url)
            {
                if (auth_method == Settings.AUTHENTICATION_BASIC) {
                    r.addRequestHeader("Authorization", Credentials.basic(username, password));
                    authed = true;
                } else {
                    OAuthUtils.OAuthData o = OAuthUtils.hosts.get(hostname);
                    if (o != null && oauth_token != null) {
                        TreeMap<String, String> query = new TreeMap<>();
                        Uri u = Uri.parse(uri);
                        for (String name : u.getQueryParameterNames()) {
                            query.put(name, u.getQueryParameter(name));
                        }
                        r.addRequestHeader("Authorization", OAuthUtils.getAuthHeader(this, o, oauth_token, u.getPath(), "GET", query));
                        authed = true;
                    }
                }
                //System.out.println("authed basic");
            }
            else
            {
                String auth = null;
                List<Cookie> l = cookies.get(hostname);
                if (l != null) {
                    for (Cookie c : l) {
                        if (c.name().equals(AUTH_COOKIE_NAME)) {
                            auth = c.name()+"="+c.value();
                            break;
                        }
                    }
                }
                if (auth != null)
                {
                    r.addRequestHeader("Cookie",auth);
                    authed = true;
                    //System.out.println("authed cookie");
                }
            }


            if (authed)
            {
                m.enqueue(r);
            }
        } else {
            throw new RuntimeException("download would not be secure");
        }
    }
    
    private Cookie getSessionCookie() {
        List<Cookie> l = cookies.get(hostname);
        if (l != null) {
            for (Cookie c : l) {
                if (c.name().equals(AUTH_COOKIE_NAME)) {
                    return c;
                }
            }
        }
        return null;
    }
    
    
    
    
    
    public boolean authWebView(HttpAuthHandler handler, String host) {
        if (! handler.useHttpAuthUsernamePassword()) {
            handler.cancel();
            return false;
        }
        
        // only authenticate for the Stud.IP server
        if (hostname.equals(host)) {
            if (auth_method == Settings.AUTHENTICATION_BASIC) {
                handler.proceed(username, password);
                return true;
            }
            if (auth_method == Settings.AUTHENTICATION_COOKIE) {
                // TODO redirect to login screen instead
                
                return false;
            }
        }
        handler.cancel();
        return false;
    }
    
    public void autofillLoginForm(WebView v) {
        if (username != null && password != null) {
            v.evaluateJavascript("" +
                    "var e = document.getElementById(\"loginname\");\n" +
                    "if (e != null) e.value = \"" + username + "\";" +
                    "e = document.getElementById(\"password\");\n" +
                    "if (e != null) e.value = \"" + password + "\";", null);
        }
    }
    
    
    public void authToken(Context c) {
        if (oauth_token != null && oauth_token.isTemp) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(API.HTTPS+hostname+ OAuthUtils.authorize_url+"?oauth_token="+oauth_token.oauth_token));
            c.startActivity(i);
        }
    }
    
    
    public String getHostname() {
        return hostname;
    }
    
    public String getUserID() {
        return userID;
    }
    
    
    // 0 = IOException or timeout
    // 200 = success
    // 401 = unauthorized
    // 403 = route inactive
    // 404 = invalidMethod / route not found
    // 405 = Method not Allowed
    public LiveData<Integer> logged_in(Context con) {
        if (userID != null) {
            Call<StudipUser> c = user.user();
            return login_call(con, c, DBProvider.getDB(con).userDao());
        } else  {
            return new MutableLiveData<>(0);
        }
    }
    
    public void setToken(OAuthUtils.OAuthToken tok) {
        oauth_token = tok;
    }
    
    // -2 = error parsing the response
    // 0 = IOException or timeout
    // 200 = success
    // 401 = unauthorized, OAUth not enabled for the installation
    public LiveData<Integer> login(Context con, String username, String password, int auth_method) {
        this.auth_method = auth_method;
        this.username = username;
        if (auth_method == Settings.AUTHENTICATION_BASIC) {
            this.password = password;
        } else {
            this.password = null;
        }
        if (auth_method == Settings.AUTHENTICATION_BASIC || auth_method == Settings.AUTHENTICATION_COOKIE) {
            Call<StudipUser> ca = user.login(Credentials.basic(username,password));
            return login_call(con, ca, DBProvider.getDB(con).userDao());
        } else {
            MutableLiveData<Integer> d = new MutableLiveData<>(-1);
            Call<String> c = OAuthUtils.accessToken(this, oauth_token);
            if (c == null) {
                d.setValue(0);
                return d;
            }
            UserDao user = DBProvider.getDB(con).userDao();
            final API api = this;
            Context appcon = con.getApplicationContext();
            c.enqueue(new Callback<String>()
            {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.code() != 200) {
                        d.setValue(response.code());
                        return;
                    }
                    String body = response.body();
                    if (body != null) {
                        oauth_token = OAuthUtils.getTokenFromResponse(body, false);
                        if (oauth_token == null) {
                            d.setValue(-2);
                        } else {
                            api.user.user().enqueue(new Callback<StudipUser>()
                            {
                                @SuppressLint("CheckResult")
                                @Override
                                public void onResponse(@NonNull Call<StudipUser> call, @NonNull Response<StudipUser> response) {
                                    StudipUser u = response.body();
                                    if (u != null) {
                                        boolean same = userID == null || userID.equals(u.user_id);
                                        userID = u.user_id;
                                        //noinspection ResultOfMethodCallIgnored
                                        user.updateInsertAsync(response.body()).timeout(10,TimeUnit.SECONDS)
                                                .subscribeOn(Schedulers.io()).subscribe(() -> {
                                            d.postValue(200);
                                            if (! same) { // if using another account, delete all present data
                                                //System.out.println("another account used");
                                                Settings settings = SettingsProvider.getSettings(appcon);
                                                settings.logout = true;
                                                settings.safe(SettingsProvider.getSettingsPreferences(appcon));
                                                System.exit(0);
                                            }
                                        },throwable -> d.postValue(0));
                                    } else {
                                        d.setValue(response.code());
                                    }
                                }
    
                                @Override
                                public void onFailure(@NonNull Call<StudipUser> call, @NonNull Throwable t) {
                                    d.setValue(0);
                                }
                            });
                        }
                    } else {
                        d.setValue(-2);
                    }
                }
                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    d.setValue(0);
                }
            });
            return d;
        }
    }
    
    public LiveData<Integer> discover() {
        MutableLiveData<Integer> d = new MutableLiveData<>(-1);
        discovery.discovery().enqueue(new Callback<HashMap<String, HashMap<String, String>>>()
        {
            @Override
            public void onResponse(@NotNull Call<HashMap<String, HashMap<String, String>>> call,
                                   @NotNull Response<HashMap<String, HashMap<String, String>>> response) {
                HashMap<String, HashMap<String, String>> res = response.body();
                if (res != null) {
                    disabled_features.clear();
                    Features.featureGlobalNews(disabled_features, res);
                    Features.featureFiles(disabled_features, res);
                    Features.featureUserFiles(disabled_features, res);
                    Features.featureCourseFiles(disabled_features, res);
                    Features.featureForum(disabled_features, res);
                    Features.featureMessages(disabled_features, res);
                    Features.featureCourses(disabled_features, res);
                    Features.featurePlanner(disabled_features, res);
                    Features.featureBlubber(disabled_features, res);
                }
                d.setValue(response.code());
            }
    
            @Override
            public void onFailure(@NotNull Call<HashMap<String, HashMap<String, String>>> call, @NotNull Throwable t) {
                d.setValue(0);
            }
        });
        
        return d;
    }
    
    
    private LiveData<Integer> login_call(Context con, Call<StudipUser> c, UserDao user) {
        Context appcon = con.getApplicationContext();
        MutableLiveData<Integer> d = new MutableLiveData<>(-1);
        c.enqueue(new Callback<StudipUser>()
        {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            @SuppressLint("CheckResult")
            @Override
            public void onResponse(@NotNull Call<StudipUser> call, @NotNull Response<StudipUser> response)
            {
                int code = response.code();
                //System.out.println("code: "+code);
                if (code == 200 && response.body() != null) {
                    //System.out.println("id: "+userID);
                    boolean same = userID == null || userID.equals(response.body().user_id);
                    userID = response.body().user_id;
                    user.updateInsertAsync(response.body()).timeout(10,TimeUnit.SECONDS).subscribeOn(Schedulers.io()).subscribe(() -> {
                        d.postValue(code);
                        if (! same) { // if using another account, delete all present data
                            //System.out.println("another account used");
                            Settings settings = SettingsProvider.getSettings(appcon);
                            settings.logout = true;
                            settings.safe(SettingsProvider.getSettingsPreferences(appcon));
                            System.exit(0);
                        }
                    },throwable -> d.postValue(0));
                } else {
                    d.postValue(code);
                }
            }
            @Override
            public void onFailure(@NotNull Call<StudipUser> call, @NotNull Throwable t)
            {
                d.postValue(0);
            }
        });
        return d;
    }
    
    @SuppressLint("ApplySharedPref")
    public void logout(EncryptedSharedPreferences prefs) {
        String hostname = prefs.getString(HOSTNAME_KEY,null);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.putString(HOSTNAME_KEY,hostname);
        edit.commit();
    }
    
    public void save(EncryptedSharedPreferences prefs) {
        SharedPreferences.Editor edit = prefs.edit();
        
        edit.putString(HOSTNAME_KEY,hostname);
        String auth = "";
        Cookie c = getSessionCookie();
        if (c != null) {
            auth = c.value();
        }
        edit.putString(AUTH_COOKIE_KEY,auth);
        edit.putString(USERID_KEY,userID);
        
        if (auth_method == Settings.AUTHENTICATION_BASIC) {
            edit.putString(USERNAME_KEY,username);
            edit.putString(PASSWORD_KEY,password);
        }
        
        edit.putStringSet(DISABLED_FEATURES_KEY, disabled_features);
        
        if (oauth_token != null && ! oauth_token.isTemp) {
            edit.putString(OAUTH_TOKEN_KEY, oauth_token.oauth_token);
            edit.putString(OAUTH_TOKEN_SECRET_KEY, oauth_token.oauth_token_secret);
        }
        
        edit.apply();
    }
    
    public static API load(EncryptedSharedPreferences prefs) {
        String hostname = prefs.getString(HOSTNAME_KEY,null);
        if (hostname == null) {
            return null;
        }
        
        
        API api = new API(hostname);
        String auth = prefs.getString(AUTH_COOKIE_KEY,null);
        
        
        if (auth != null) {
            ArrayList<Cookie> l = new ArrayList<>();
            l.add(new Cookie.Builder().name(AUTH_COOKIE_NAME).hostOnlyDomain(hostname.split("/")[0]).value(auth).secure().build());
            api.cookies.put(hostname, l);
            api.userID = prefs.getString(USERID_KEY,null);
            api.auth_method = Settings.AUTHENTICATION_COOKIE;
        }
        
        String username = prefs.getString(USERNAME_KEY,null);
        String password = prefs.getString(PASSWORD_KEY,null);
        
        
        if (username != null && password != null) {
            api.username = username;
            api.password = password;
            api.auth_method = Settings.AUTHENTICATION_BASIC;
            api.cookies.clear();
        }
        
        api.disabled_features = prefs.getStringSet(DISABLED_FEATURES_KEY, new HashSet<>());
        
        String oauth_token = prefs.getString(OAUTH_TOKEN_KEY, null);
        String oauth_secret = prefs.getString(OAUTH_TOKEN_SECRET_KEY, null);
        if (oauth_secret != null && oauth_token != null) {
            api.oauth_token = new OAuthUtils.OAuthToken(oauth_token, oauth_secret, false);
            api.auth_method = Settings.AUTHENTICATION_OAUTH;
            api.cookies.clear();
        }
        
        return api;
    }
    
    public String getDisabledFeatures(Context c) {
        return Features.listUnavailableFeatures(disabled_features, c);
    }
    
    public void ignoreDisabledFeatures() {
        disabled_features.clear();
    }
    
}
