package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.studip.unofficial_app.api.rest.StudipMessage;
import org.studip.unofficial_app.databinding.DialogViewMessageBinding;
import org.studip.unofficial_app.ui.HelpActivity;

public class MessageDialogFragment extends DialogFragment
{
    public static final String ARG_MESSAGE_ID = "message id";
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());

        DialogViewMessageBinding binding = DialogViewMessageBinding.inflate(getLayoutInflater());
        
        
        
        Bundle args = getArguments();
        if (args == null || ! (args.getSerializable(ARG_MESSAGE_ID) instanceof StudipMessage)) {
            dismiss();
            return b.create();
        }
        StudipMessage m = (StudipMessage) args.getSerializable(ARG_MESSAGE_ID);
        
        TextView title = new TextView(requireActivity());
        title.setPadding((int) (8*getResources().getDisplayMetrics().density),(int) (8*getResources().getDisplayMetrics().density),(int) (8*getResources().getDisplayMetrics().density), 0);
        b.setCustomTitle(title);
        title.setText(m.subject);
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP,26);
        title.setTextIsSelectable(true);
        
        
        binding.messageContent.setText(HelpActivity.fromHTML(m.message_html));
        binding.messageContent.setTextIsSelectable(true);
        binding.messageReply.setOnClickListener(v -> {
            Bundle args2 = new Bundle();
            args2.putString(MessageWriteDialogFragment.SUBJECT_KEY, "RE: "+m.subject);
            args2.putString(MessageWriteDialogFragment.ADDRESSEE_KEY, m.sender);
            MessageWriteDialogFragment d = new MessageWriteDialogFragment();
            d.setArguments(args2);
            d.show(getParentFragmentManager(), "message_write");
            dismiss();
        });
    
        b.setView(binding.getRoot());
        return b.create();
    }
}
