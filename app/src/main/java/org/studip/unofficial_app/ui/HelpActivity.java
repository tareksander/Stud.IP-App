package org.studip.unofficial_app.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.model.APIProvider;

import java.io.IOException;
import java.nio.charset.Charset;

public class HelpActivity extends AppCompatActivity
{
    public static Spanned fromHTML(@NonNull String html, boolean images, Context c) {
        Html.ImageGetter img = source -> {
            //System.out.println(source);
            if (images) {
                API api = APIProvider.getAPI(c);
                if (api != null && api.getHostname() != null) {
                    Uri path = Uri.parse(source);
                    if ("http".equals(path.getScheme()) || "https".equals(path.getScheme())) {
                        if (path.getHost().equals(api.getHostname().split("/")[0])) {
                            
                        }
                        else {
                            // ensure https
                            if ("http".equals(path.getScheme())) {
                                path = path.buildUpon().scheme("https").build();
                            }
                            try {
                                Picasso.get().load(path).
                                return new BitmapDrawable(c.getResources(), new Bitmap.).;
                                
                                
                                
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            return null;
        };
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // it's OK, since the version check is there
            //noinspection deprecation
            return Html.fromHtml(html, img, null);
        } else {
            return Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY, img, null);
        }
    }
    
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        WebView v = findViewById(R.id.help_view);
        Bundle b = new Bundle();
        v.saveState(b);
        outState.putBundle("webview", b);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        
        WebView v = findViewById(R.id.help_view);
        v.loadDataWithBaseURL("http://help", getString(R.string.help_content), "text/html", "UTF-8", null);
        v.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                System.out.println(url);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(i);
                } catch (ActivityNotFoundException ignored) {}
                return true;
            }
    
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl().toString());
            }
    
            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                finish();
                return true;
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey("webview")) {
            v.restoreState(savedInstanceState.getBundle("webview"));
        }
    }
    
}