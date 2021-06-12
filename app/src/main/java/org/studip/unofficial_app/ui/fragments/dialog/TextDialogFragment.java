package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TextDialogFragment extends DialogFragment
{
    public static final String TITLE = "title";
    public static final String TEXT = "text";
    public static final String BUTTON_TEXT = "button";
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        
        Bundle args = getArguments();
        if (args == null || args.getString(TITLE) == null || args.getString(TEXT) == null) {
            dismiss();
            return super.onCreateDialog(savedInstanceState);
        }
        String buttonText = args.getString(BUTTON_TEXT);
    
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());
        
        b.setTitle(args.getString(TITLE));
        b.setMessage(args.getString(TEXT));
        
        if (buttonText != null) {
            b.setNeutralButton(buttonText, (dialog, which) -> dismiss());
        }
        
        return b.create();
    }
}
