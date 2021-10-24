package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;

import com.squareup.picasso.Picasso;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.rest.StudipUser;
import org.studip.unofficial_app.databinding.DialogUserBinding;
import org.studip.unofficial_app.model.DBProvider;

public class UserDialogFragment extends DialogFragment
{
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());
        
        Bundle args = getArguments();
        if (args == null) {
            dismiss();
            return b.create();
        }
        String uid = args.getString("uid");
        String username = args.getString("username");
        if (uid == null && username == null) {
            dismiss();
            return b.create();
        }
    
        DialogUserBinding binding = DialogUserBinding.inflate(getLayoutInflater());
        b.setView(binding.getRoot());
        b.setTitle(R.string.user_dialog);
        AlertDialog d = b.create();
        
        Observer<StudipUser> obs = (user) -> {
            if (user == null) {
                dismiss();
            } else {
                Picasso.get().load(user.avatar_original).into(binding.avatar);
                binding.userName.setText(getString(R.string.username_display, user.name.username));
                binding.userFullname.setText(getString(R.string.user_fullname_display, user.name.formatted));
                binding.mail.setOnClickListener((c) -> {
                    dismiss();
                    Bundle args2 = new Bundle();
                    args2.putString(MessageWriteDialogFragment.ADDRESSEE_KEY, user.user_id);
                    MessageWriteDialogFragment msg = new MessageWriteDialogFragment();
                    msg.setArguments(args2);
                    msg.show(getParentFragmentManager(), "message_write");
                });
            }
        };
        
        if (uid != null) {
            DBProvider.getDB(requireActivity()).userDao().observe(uid).observe(this, obs);
        } else {
            DBProvider.getDB(requireActivity()).userDao().observeUsername(username).observe(this, obs);
        }
        
        return d;
    }
}
