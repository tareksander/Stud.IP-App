package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.studip.unofficial_app.api.rest.StudipMessage;
import org.studip.unofficial_app.databinding.MessageViewDialogBinding;

public class MessageDialogFragment extends DialogFragment
{
    public static final String ARG_MESSAGE_ID = "message id";
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());

        MessageViewDialogBinding binding = MessageViewDialogBinding.inflate(getLayoutInflater());
        b.setView(binding.getRoot());
        
        
        Bundle args = getArguments();
        if (args == null || ! (args.getSerializable(ARG_MESSAGE_ID) instanceof StudipMessage)) {
            dismiss();
            return b.create();
        }
        StudipMessage m = (StudipMessage) args.getSerializable(ARG_MESSAGE_ID);
        
        TextView title = new TextView(requireActivity());
        b.setCustomTitle(title);
        title.setText(m.subject);
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP,26);
        
        
        
        Document doc = Jsoup.parse(m.message_html);
        binding.messageContent.setText(doc.wholeText());
        return b.create();
    }
}
