package org.studip.unofficial_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.databinding.ActivityServerSelectBinding;
import org.studip.unofficial_app.model.APIProvider;

public class ServerSelectActivity extends AppCompatActivity
{
    private ActivityServerSelectBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        
        
        
        API api = APIProvider.getAPI(this);
        if (api != null) {
            //System.out.println("api found");
            toLoginActivity();
        }
        
        

        //System.out.println("ServerSelect");
        
        binding = ActivityServerSelectBinding.inflate(getLayoutInflater());
        
        
        binding.serverUrlsList.setOnItemClickListener((parent, view, position, id) -> onSubmitURL(view));
        
        
        setContentView(binding.getRoot());
    }
    

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    public void onSettingsButton(View v)
    {
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
    }
    
    public void onHelpButton(View v) {
        Intent intent = new Intent(this,HelpActivity.class);
        startActivity(intent);
    }
    
    
    private void toLoginActivity()
    {
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void onSubmitURL(View v)
    {
        if (v.equals(binding.submitUrl))
        {
            TextView t = findViewById(R.id.server_url);
            APIProvider.newAPI(t.getText().toString());
            toLoginActivity();
        }
        else
        {
            if (v instanceof TextView)
            {
                TextView t = (TextView) v;
                APIProvider.newAPI(t.getText().toString());
                toLoginActivity();
            }
        }
    }
    
}
