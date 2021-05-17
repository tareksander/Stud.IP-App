package org.studip.unofficial_app.api;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
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
import org.studip.unofficial_app.api.routes.Semester;
import org.studip.unofficial_app.api.routes.Studip;
import org.studip.unofficial_app.api.routes.TestRoutes;
import org.studip.unofficial_app.api.routes.User;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.GsonProvider;
import org.studip.unofficial_app.model.Settings;
import org.studip.unofficial_app.model.SettingsProvider;
import org.studip.unofficial_app.model.room.UserDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
    private static final String AUTH_METHOD_KEY = "method";
    //private static final String USER_FOLDER_KEY = "user_top_folder";
    
    
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
                    if (route != null && route.address().url().isHttps() && route.address().url().host().equals(hostname) && password != null && auth_method == Settings.AUTHENTICATION_BASIC) {
                        //System.out.println("Basic Authentication used for "+route.address().url().toString());
                        return response.request().newBuilder().header("Authorization",Credentials.basic(username,password)).build();
                    } else {
                        //System.out.println("basic auth denied for: "+route.address().url().toString());
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
    
        //tests = retrofit.create(TestRoutes.class);
        
        opencast = new Opencast(retrofit);
        courseware = new Courseware(retrofit);
        meetings = new Meetings(retrofit);
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

            if (auth_method == Settings.AUTHENTICATION_BASIC && ! url)
            {
                r.addRequestHeader("Authorization", Credentials.basic(username, password));
                authed = true;
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
    
    
    
    public boolean authWebView(WebView v, HttpAuthHandler handler, String host) {
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
    
    public LiveData<Integer> login(Context con, String username, String password, int auth_method) {
        Call<StudipUser> c = user.login(Credentials.basic(username,password));
        this.auth_method = auth_method;
        this.username = username;
        if (auth_method == Settings.AUTHENTICATION_BASIC) {
            this.password = password;
        } else {
            this.password = null;
        }
        return login_call(con, c, DBProvider.getDB(con).userDao());
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
                    d.postValue(0);
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
        List<Cookie> l = cookies.get(hostname);
        if (l != null) {
            for (Cookie c : l) {
                if (c.name().equals(AUTH_COOKIE_NAME)) {
                    auth = c.value();
                    //System.out.println("auth cookie saved");
                    break;
                }
            }
        }
        edit.putString(AUTH_COOKIE_KEY,auth);
        edit.putString(USERID_KEY,userID);
        
        if (auth_method == Settings.AUTHENTICATION_BASIC) {
            edit.putString(USERNAME_KEY,username);
            edit.putString(PASSWORD_KEY,password);
        }
        
        //edit.putString(USER_FOLDER_KEY,folder_id);
        
        edit.apply();
    }
    
    public static API load(EncryptedSharedPreferences prefs) {
        String hostname = prefs.getString(HOSTNAME_KEY,null);
        if (hostname == null) {
            return null;
        }
        //System.out.println("Hostname: "+hostname);
        
        
        API api = new API(hostname);
        String auth = prefs.getString(AUTH_COOKIE_KEY,null);
        
        
        if (auth != null) {
            ArrayList<Cookie> l = new ArrayList<>();
            l.add(new Cookie.Builder().name(AUTH_COOKIE_NAME).hostOnlyDomain(hostname).value(auth).secure().build());
            api.cookies.put(hostname, l);
            api.userID = prefs.getString(USERID_KEY,null);
            //System.out.println("cookie found: "+l.toString());
        }
        
        String username = prefs.getString(USERNAME_KEY,null);
        String password = prefs.getString(PASSWORD_KEY,null);
        
        
        
        if (username != null && password != null) {
            api.username = username;
            api.password = password;
            api.auth_method = Settings.AUTHENTICATION_BASIC;
        }
        
        /*
        api.folder_id = prefs.getString(USER_FOLDER_KEY,null);
        if (api.folder_id == null) {
            api.folder_id = ""; // to indicate loading is in progress
            api.save(prefs);
            api.user.userFolder(api.userID).enqueue(new Callback<StudipFolder>()
            {
                @Override
                public void onResponse(@NotNull Call<StudipFolder> call, @NotNull Response<StudipFolder> response) {
                    StudipFolder f = response.body();
                    if (f != null && f.id != null && ! f.id.equals("")) {
                        api.folder_id = f.id;
                    }
                }
                @Override
                public void onFailure(@NotNull Call<StudipFolder> call, @NotNull Throwable t) {}
            });
        }
        */
        
        // To test if authentication is used on foreign websites, but there should not be calls able to be made to foreign websites anyways
        /*
        System.out.println("trying google");
        api.tests.tryGoogle().enqueue(new Callback<ResponseBody>()
        {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
            {
                
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t)
            {

            }
        });
        */
        
        
        return api;
    }
    
    
    
    
}
