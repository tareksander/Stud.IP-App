package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.studip.unofficial_app.R;

public class MkdirDialogFragment extends DialogFragment
{
    public void setListener(MkdirDialogListener listener)
    {
        this.list = listener;
    }

    public interface MkdirDialogListener {
        void onSubmit(String name);
    }
    
    private MkdirDialogListener list = null;
    private EditText ed = null;
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        if (ed != null) {
            outState.putString("editor_text",ed.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }
    
    
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());

        ed = new EditText(getActivity());
        ed.setSingleLine(true);
        if (savedInstanceState != null) {
            ed.setText(savedInstanceState.getString("editor_text",""));
        }
        b.setView(ed);
        
        b.setTitle(R.string.mkdir);
        b.setCancelable(true);
        b.setPositiveButton(R.string.ok, (dialog, which) ->
        {
            if (ed.getText().toString().equals("")) {
                dismiss();
            } else {
                if (list != null) {
                    list.onSubmit(ed.getText().toString());
                }
            }
        });
        
        
        return b.create();
    }
}
