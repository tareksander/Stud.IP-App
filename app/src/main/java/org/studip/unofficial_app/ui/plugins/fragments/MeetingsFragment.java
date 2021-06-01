package org.studip.unofficial_app.ui.plugins.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.PermissionRequest;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.Notifications;
import org.studip.unofficial_app.ui.plugins.MeetingsActivity;
import org.studip.unofficial_app.ui.plugins.MeetingsReceiver;

public class MeetingsFragment extends Fragment
{
    private WebView conference;
    private FrameLayout l;
    private String url;
    
    // https://github.com/bigbluebutton/bigbluebutton/issues/8144
    
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //System.out.println("create fragment");
        setRetainInstance(true);
        
        l = new FrameLayout(requireActivity());
        
        MeetingsActivity a = (MeetingsActivity) requireActivity();
        url = a.url;
        
        conference = createWebView(null);
    
        notification(null);
        
        
        l.addView(conference);
    }
    
    public void notification(String title) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(requireActivity(), Notifications.CHANNEL_MEETINGS);
        Notifications.setType(requireActivity(), b, Notifications.CHANNEL_MEETINGS);
        b.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        b.setSmallIcon(R.drawable.chat_blue);
        b.setOngoing(true);
        b.setContentTitle(title);
    
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_IMMUTABLE;
        }
    
        {
            Intent ret = new Intent(requireActivity(), MeetingsActivity.class);
            ret.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ret.setAction(MeetingsActivity.ACTION_VIEW);
            b.setContentIntent(PendingIntent.getActivity(requireActivity(), 0, ret, flags));
        }
        
        {
            Intent logout = new Intent(requireActivity(), MeetingsReceiver.class);
            logout.setAction(MeetingsActivity.ACTION_LOGOUT);
            b.addAction(R.drawable.home_blue, getString(R.string.logout), 
                    PendingIntent.getBroadcast(requireActivity(), 0, logout, flags));
        }
    
        {
            Intent logout = new Intent(requireActivity(), MeetingsReceiver.class);
            logout.setAction(MeetingsActivity.ACTION_TOGGLE_MIC);
            b.addAction(android.R.drawable.ic_btn_speak_now, getString(R.string.toggle_mic),
                    PendingIntent.getBroadcast(requireActivity(), 0, logout, flags));
        }
        
        
        NotificationManagerCompat m = NotificationManagerCompat.from(requireActivity());
        m.notify(Integer.MIN_VALUE, b.build());
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private WebView createWebView(WebViewClient client) {
        conference = new WebView(requireActivity().getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            conference.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false);
        }
        if (client == null) {
            conference.setWebViewClient(new MeetingsWebViewClient(l));
        } else {
            conference.setWebViewClient(client);
        }
        conference.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                //System.out.println(consoleMessage.message());
                return true;
            }
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                notification(title);
            }
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                for (String r : request.getResources()) {
                    //System.out.println("requesting: "+r);
                    if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(r)) {
                        if (requireActivity().getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
                            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO) == 
                                    PackageManager.PERMISSION_DENIED) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 0);
                                }
                            }
                            request.grant(new String[]{r});
                        } else {
                            request.deny();
                        }
                    }
                    if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r)) {
                        if (requireActivity().getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == 
                                    PackageManager.PERMISSION_DENIED) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
                                }
                            }
                            request.grant(new String[]{r});
                        } else {
                            request.deny();
                        }
                    }
                }
            }
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                l.removeAllViews();
                l.addView(view);
            }
            @Override
            public void onHideCustomView() {
                l.removeAllViews();
                l.addView(conference);
            }
        });
        WebSettings settings = conference.getSettings();
        // BigBlueButton needs Javascript enabled, and to counter malicious scripts, web navigation is disabled after the page finished loading
        settings.setJavaScriptEnabled(true);
        // needed for BigBlueButton to recognize the WebView as a browser
        settings.setUserAgentString(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36");
        settings.setMediaPlaybackRequiresUserGesture(false);
        CookieManager.getInstance().setAcceptThirdPartyCookies(conference, true);
        conference.loadUrl(url);
        return conference;
    }
    
    public void isMicEnabled(ValueCallback<String> resultCallback) {
        conference.evaluateJavascript("var e = document.querySelector(\"i[class*=\\\"icon-bbb-unmute\\\"]\");"+
                "e != null;", resultCallback);
    }
    
    public void toggleMic() {
        //System.out.println("toggle mic");
        conference.evaluateJavascript("var e = document.querySelector(\"i[class*=\\\"icon-bbb-unmute\\\"]\");" +
                        "if (e !== null && e !== undefined) {e.parentNode.parentNode.click(); false} else {" +
                        "e = document.querySelector(\"i[class*=\\\"icon-bbb-mute\\\"]\");" +
                        "if (e !== null && e !== undefined) {e.parentNode.parentNode.click(); true} }"
                , value -> {
                    if ("true".equals(value)) {
                        Toast.makeText(requireActivity(),R.string.mic_on , Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireActivity(),R.string.mic_off , Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    public void goFullscreen() {
        conference.evaluateJavascript("var e = document.querySelector(\"button[class*=\\\"fullScreenButton\\\"]\");" +
                "if (! e.firstChild.className.includes(\"exit\")) e.click();", null);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //System.out.println("destroy view");
        if (requireActivity().isFinishing()) {
            //System.out.println("finishing");
            NotificationManagerCompat m = NotificationManagerCompat.from(requireActivity());
            conference.clearCache(false);
            conference.destroy();
            conference = null;
            m.cancel(Integer.MIN_VALUE);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable  ViewGroup container, @Nullable Bundle savedInstanceState) {
        //System.out.println("create view");
        return l;
    }
    
    @Override
    public void onDetach() {
        //System.out.println("detach");
        super.onDetach();
    }
    
    private class MeetingsWebViewClient extends WebViewClient {
        private FrameLayout f;
        private boolean loading;
        public MeetingsWebViewClient(FrameLayout f) {
            this.f = f;
            loading = true;
        }
    
        @Override
        public void onPageFinished(WebView view, String url) {
            loading = false;
        }
    
        private boolean handleURL(String u) {
            if (loading) {
                return false;
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(u));
            try {
                startActivity(i);
            } catch (ActivityNotFoundException ignored) {}
            return true;
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return handleURL(request.getUrl().toString());
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleURL(url);
        }
        
        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            //System.out.println("auth request");
            API api = APIProvider.getAPI(requireActivity());
            if (api == null) {
                handler.cancel();
                return;
            }
            if (! api.authWebView(view, handler, host)) {
                requireActivity().finish();
            }
        }
    
        @Override
        public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
            f.removeAllViews();
            view.setWebViewClient(null);
            loading = true;
            WebView newweb = createWebView(this);
            f.addView(newweb);
            return true;
        }
    }
    
    
}
