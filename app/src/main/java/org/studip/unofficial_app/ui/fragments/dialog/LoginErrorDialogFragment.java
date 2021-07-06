package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.ui.HelpActivity;

public class LoginErrorDialogFragment extends DialogFragment
{
    public static final String ERROR_CODE = "err";
    
    private AlertDialog d;
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());
        
        Bundle args = getArguments();
        if (args == null || args.getInt(ERROR_CODE, -100) == -100) {
            dismiss();
            return b.create();
        }
        int code = args.getInt(ERROR_CODE);
        
        int msg;
        switch (code) {
            case 401:
                msg = R.string.login_error_auth;
                break;
            case 403:
            case 404:
            case 405:
                msg = R.string.login_error_no_api;
                break;
            default:
                msg = R.string.login_error_message;
        }
        //System.out.println(code);
        b.setTitle(R.string.login_error_title).setMessage(HelpActivity.fromHTML(getString(msg), false, null));
        d = b.create();
        
        
        return d;
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
