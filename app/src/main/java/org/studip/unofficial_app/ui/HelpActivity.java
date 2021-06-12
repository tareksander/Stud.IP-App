package org.studip.unofficial_app.ui;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.studip.unofficial_app.R;

public class HelpActivity extends AppCompatActivity
{
    public static Spanned fromHTML(@NonNull String html) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // it's OK, since the version check is there
            //noinspection deprecation
            return Html.fromHtml(html);
        } else {
            return Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        TextView v = findViewById(R.id.help_view);
        v.setText(fromHTML(getString(R.string.help_content)));
        v.setMovementMethod(new LinkMovementMethod());
    }
    
}