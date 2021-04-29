package org.studip.unofficial_app.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import org.studip.unofficial_app.R;

public class HelpActivity extends AppCompatActivity
{
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
    
        TextView v = findViewById(R.id.help_view);
    
        Spanned s;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            s = Html.fromHtml(getString(R.string.help_content));
        } else {
            s = Html.fromHtml(getString(R.string.help_content),Html.FROM_HTML_MODE_LEGACY);
        }
        v.setText(s);
    }
}