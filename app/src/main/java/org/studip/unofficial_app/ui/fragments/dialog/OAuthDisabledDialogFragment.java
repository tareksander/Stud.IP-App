package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.jsoup.Jsoup;
import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.databinding.DialogOauthDisabledBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.room.DB;
import org.studip.unofficial_app.ui.HelpActivity;

public class OAuthDisabledDialogFragment extends DialogFragment
{
    
    
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());
    
    
        DialogOauthDisabledBinding binding = DialogOauthDisabledBinding.inflate(getLayoutInflater());
        b.setView(binding.getRoot());
        
        b.setTitle(R.string.oauth_disabled_title);
        
        binding.msg.setMovementMethod(LinkMovementMethod.getInstance());
        binding.msg.setText(HelpActivity.fromHTML(getString(R.string.oauth_disabled_msg)));
        
        final String[] name = new String[]{""};
    
        DB db = DBProvider.getDB(requireActivity());
        API api = APIProvider.getAPI(requireActivity());
        if (db != null && api != null && api.getUserID() != null) {
            db.userDao().observe(api.getUserID()).observe(this, u -> name[0] = u.name.formatted);
        }
        
        
        binding.send.setOnClickListener(v1 -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.oauth_mail_template_subject));
            i.putExtra(Intent.EXTRA_TEXT, Jsoup.parse(getString(R.string.oauth_mail_template, name[0])).wholeText());
            i.putExtra(Intent.EXTRA_HTML_TEXT, getString(R.string.oauth_mail_template, name[0]));
            startActivity(Intent.createChooser(i, getString(R.string.message_send)));
        });
        
        return b.create();
    }
    
    
}
