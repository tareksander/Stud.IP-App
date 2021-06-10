package org.studip.unofficial_app.ui;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
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

import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.databinding.ActivityWebViewBinding;
import org.studip.unofficial_app.model.APIProvider;

import java.util.ArrayList;

public class WebViewActivity extends AppCompatActivity
{
    
    private ActivityWebViewBinding binding;
    private WebView browserView = null;
    private final WebViewActivity a = this;
    private boolean login = false;
    private ActivityResultLauncher<String[]> launch;
    private ValueCallback<Uri[]> filePathCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebViewBinding.inflate(getLayoutInflater());
    
    
        launch = registerForActivityResult(new ActivityResultContracts.OpenDocument(), result -> filePathCallback.onReceiveValue(new Uri[]{result}));
        
        
        
        API api = APIProvider.getAPI(a);
        if (api == null || api.getHostname() == null) {
            finish();
            return;
        }
        login = savedInstanceState == null;
        recreateWebView();
        
        if (savedInstanceState != null && savedInstanceState.containsKey("current_url")) {
            browserView.loadUrl(savedInstanceState.getString("current_url"));
        } else {
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
        
        setContentView(binding.getRoot());
    }
    
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            outState.putString("current_url", browserView.getUrl());
        } catch (Exception ignored) {}
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void recreateWebView() {
        binding.browserViewParent.removeAllViews();
        if (browserView != null) {
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
        
        
        
        binding.browserViewParent.addView(browserView);
        
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
        
        
    
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            binding.browserViewParent.removeAllViews();
            binding.browserViewParent.addView(view);
        }
    
        @Override
        public void onHideCustomView() {
            binding.browserViewParent.removeAllViews();
            binding.browserViewParent.addView(browserView);
        }
    
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback, FileChooserParams fileChooserParams) {
            filePathCallback = callback;
            launch.launch(new String[]{"*/*"});
            return true;
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
            if (!api.getHostname().equals(uri.getHost())) {
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
        binding.browserViewParent.removeAllViews();
        if (browserView != null) {
            browserView.destroy();
        }
    }
}