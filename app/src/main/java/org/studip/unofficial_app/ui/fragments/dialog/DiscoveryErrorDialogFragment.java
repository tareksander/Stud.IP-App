package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.ui.HelpActivity;
import org.studip.unofficial_app.ui.HomeActivity;

public class DiscoveryErrorDialogFragment extends DialogFragment
{
    public static final String CODE = "code";
    public static final String LOGIN = "login";
    
    private AlertDialog d;
    private boolean login = true;
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());
    
        Bundle args = getArguments();
        if (args == null || args.getInt(CODE, -100) == -100) {
            //System.out.println("no code");
            dismiss();
            return b.create();
        }
        login = args.getBoolean(LOGIN, true);
        int code = args.getInt(CODE);
        
        if (code != 200) {
            b.setTitle(R.string.login_error_title).setMessage(R.string.discovery_error).setPositiveButton(R.string.ok, (dialog, which) -> dismiss());
        } else {
            API api = APIProvider.getAPI(requireActivity());
            if (api != null) {
                String disabled = api.getDisabledFeatures(requireActivity());
                String msg = getString(R.string.discovery_msg, disabled);
                if (! disabled.equals("")) {
                    b.setTitle(R.string.discovery_title).setMessage(HelpActivity.fromHTML(msg)).setPositiveButton(R.string.ok, (dialog, which) -> dismiss());
                } else {
                    //System.out.println("nothing disabled");
                    dismiss();
                }
            } else {
                //System.out.println("no api");
                dismiss();
            }
        }
        
        d = b.create();
        
        
        return d;
    }
    
    @Override
    public void dismiss() {
        FragmentActivity a = requireActivity();
        Intent intent = new Intent(a, HomeActivity.class);
        if (! login) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);
        a.finish();
        super.dismiss();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        try {
            TextView t = d.findViewById(android.R.id.message);
            if (t != null) {
                t.setMovementMethod(LinkMovementMethod.getInstance());
            }
        } catch (Exception ignored) {}
    }
}
