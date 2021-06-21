package org.studip.unofficial_app.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.databinding.ActivityWebViewBinding;
import org.studip.unofficial_app.model.APIProvider;

import java.net.URLEncoder;

public class WebViewActivity extends AppCompatActivity
{
    
    private ActivityWebViewBinding binding;
    private WebView browserView = null;
    private final WebViewActivity a = this;
    private boolean login = false;
    private ActivityResultLauncher<String[]> launch;
    private ValueCallback<Uri[]> filePathCallback;
    private Handler h = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebViewBinding.inflate(getLayoutInflater());
        
    
        launch = registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(),
                result -> filePathCallback.onReceiveValue(result.toArray(new Uri[0])));
        
        
        
        API api = APIProvider.getAPI(a);
        if (api == null || api.getHostname() == null) {
            finish();
            return;
        }
        login = savedInstanceState == null;
        recreateWebView();
        
        
        boolean loaded = false;
        if (savedInstanceState != null) {
            try {
                browserView.restoreState(savedInstanceState);
                loaded = true;
            }
            catch (Exception ignored) {}
        }
        if (! loaded) {
            Intent i = getIntent();
            if (Intent.ACTION_VIEW.equals(i.getAction())) {
                browserView.loadUrl(i.getDataString());
            }
            else {
                browserView.loadUrl(API.HTTPS + api.getHostname() + "/dispatch.php/start");
            }
        }
        
        binding.browserRefresh.setOnRefreshListener(() -> {
            if (browserView!= null) {
                browserView.reload();
            }
        });
        final Activity a = this;
        binding.favimage.setOnClickListener(v -> {
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(a)) {
                ShortcutInfoCompat.Builder b1 = new ShortcutInfoCompat.Builder(a, "webview:"+browserView.getUrl());
                b1.setIcon(IconCompat.createWithResource(a, R.drawable.globe_blue));
                b1.setShortLabel(browserView.getTitle());
                Intent i = new Intent(a, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.setAction(a.getPackageName()+".dynamic_shortcut");
                Uri data = Uri.parse(a.getPackageName()+".webview://"+ Uri.encode(browserView.getUrl()));
                i.setData(data);
                b1.setIntent(i);
                ShortcutManagerCompat.requestPinShortcut(a, b1.build(), null);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.browserRefresh.getViewTreeObserver().addOnScrollChangedListener(() -> {
                if (browserView != null && ! browserView.canScrollVertically(1)) {
                    binding.favimage.setVisibility(View.VISIBLE);
                } else {
                    binding.favimage.setVisibility(View.GONE);
                }
            });
        }
        setContentView(binding.getRoot());
    }
    
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            browserView.saveState(outState);
        } catch (Exception ignored) {}
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void recreateWebView() {
        if (browserView != null) {
            binding.browserRefresh.removeView(browserView);
            browserView.destroy();
        }
        browserView = new WebView(getApplicationContext());
        
        browserView.setWebViewClient(new WebActivityWebViewClient());
        browserView.setWebChromeClient(new WebActivityWebChromeClient());
    
        WebSettings settings = browserView.getSettings();
        // Stud.IP uses javascript, but the WebView is only able to load pages from the server, so it is safe
        settings.setJavaScriptEnabled(true);
        settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36");
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        CookieManager.getInstance().setAcceptThirdPartyCookies(browserView, true);
        browserView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            API api = APIProvider.getAPI(a);
            if (api == null || api.getHostname() == null) {
                return;
            }
            Uri uri = Uri.parse(url);
            String s = uri.getPath();
            if (uri.getQuery() != null) {
                s += "?" + uri.getQuery();
            }
            api.downloadFile(a, s, uri.getLastPathSegment()+uri.getQuery(), true);
        });
        binding.browserRefresh.addView(browserView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && browserView.canGoBack()) {
            browserView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    public class WebActivityWebChromeClient extends WebChromeClient {
        private View customView = null;
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            binding.browserRefresh.removeView(browserView);
            binding.browserRefresh.addView(view);
            customView = view;
        }
    
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                binding.browserProgress.setVisibility(View.GONE);
            } else {
                binding.browserProgress.setProgress(newProgress);
                binding.browserProgress.setVisibility(View.VISIBLE);
            }
        }
    
        @Override
        public void onHideCustomView() {
            if (customView != null) {
                binding.browserRefresh.removeView(customView);
            }
            binding.browserRefresh.addView(browserView);
            customView = null;
        }
    
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback, FileChooserParams fileChooserParams) {
            filePathCallback = callback;
            launch.launch(new String[]{"*/*"});
            return true;
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (hasFocus) {
                binding.favimage.setVisibility(View.VISIBLE);
                h.postDelayed(() -> binding.favimage.setVisibility(View.GONE), 5000);
            }
            else {
                binding.favimage.setVisibility(View.GONE);
            }
        }
    }
    
    public class WebActivityWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //System.out.println(url);
            API api = APIProvider.getAPI(a);
            if (api == null || api.getHostname() == null) {
                return true;
            }
            Uri uri = Uri.parse(url);
            if (!api.getHostname().split("/")[0].equals(uri.getHost())) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addCategory(Intent.CATEGORY_BROWSABLE);
                i.setData(uri);
                try {
                    startActivity(i);
                } catch (ActivityNotFoundException ignored) {}
                return true;
            } else {
                if ("/logout.php".equals(uri.getPath())) {
                    finish();
                    return true;
                }
            }
            return false;
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl().toString());
        }
    
        
    
        @Override
        public void onPageFinished(WebView view, String url) {
            Uri uri = Uri.parse(url);
            if (login) {
                login = false;
                API api = APIProvider.getAPI(a);
                if (api == null || api.getHostname() == null) {
                    return;
                }
                if (api.getHostname().equals(uri.getHost())) {
                    //System.out.println("login form filled");
                    api.autofillLoginForm(view);
                }
            }
            if ("/dispatch.php/start".equals(uri.getPath())) {
                view.evaluateJavascript("var e = document.querySelector(\"div[class*=\\\"helpbar-container\\\"]\");\n" +
                        "if (e != null) e.remove();", null);
            }
            binding.browserRefresh.setRefreshing(false);
        }
        
        
        
        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            if (errorResponse.getStatusCode() == 401) {
                finish();
            }
        }
    
        @Override
        public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
            recreateWebView();
            return true;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.browserRefresh.removeView(browserView);
        if (browserView != null) {
            browserView.destroy();
        }
    }
}