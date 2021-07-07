package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipMessage;
import org.studip.unofficial_app.databinding.DialogViewMessageBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.room.DB;
import org.studip.unofficial_app.ui.HelpActivity;

import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        
        API api = APIProvider.getAPI(requireActivity());
        if (api != null) {
            // mark the message as read
            api.message.update(m.message_id, "").enqueue(new Callback<Void>()
            {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {}
                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
            });
            DB db = DBProvider.getDB(requireActivity());
            if (db != null) {
                m.unread = false;
                db.messagesDao().updateAsync(m).subscribeOn(Schedulers.io()).subscribe();
            }
        }
        
        TextView title = new TextView(requireActivity());
        title.setPadding((int) (8*getResources().getDisplayMetrics().density),(int) (8*getResources().getDisplayMetrics().density),(int) (8*getResources().getDisplayMetrics().density), 0);
        b.setCustomTitle(title);
        title.setText(m.subject);
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP,26);
        title.setTextIsSelectable(true);
        
        
        binding.messageContent.setText(HelpActivity.fromHTML(m.message_html, true, requireActivity()));
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
