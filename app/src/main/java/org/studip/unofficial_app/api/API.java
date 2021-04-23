package org.studip.unofficial_app.api;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.studip.unofficial_app.api.rest.StudipUser;
import org.studip.unofficial_app.api.routes.Course;
import org.studip.unofficial_app.api.routes.Discovery;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Authenticator;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    
    
    public final Discovery discovery;
    public final Course course;
    public final File file;
    public final Folder folder;
    public final Forum forum;
    public final Message message;
    public final Studip studip;
    public final User user;
    public final Semester semester;
    
    private final TestRoutes tests;
    
    private final String hostname;
    
    // TODO reimplement HTTP Basic Auth
    
    private int auth_method = 0;
    
    private String userID = null;
    private String username = null;
    private String password = null;
    
    public API(String hostname) {
        this.hostname = hostname;
        retrofit = new Retrofit.Builder()
                .baseUrl("https://"+hostname+"/")
                .addConverterFactory(GsonConverterFactory.create(GsonProvider.getGson()))
                .client(new OkHttpClient.Builder().cookieJar(new CookieJar()
                {
                    // simple cookie jar implementation
                    @Override
                    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list)
                    {
                        if (httpUrl.isHttps())
                        {
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
        tests = retrofit.create(TestRoutes.class);
        semester = retrofit.create(Semester.class);
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
                            System.out.println("another account used");
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
