package org.studip.unofficial_app.ui.plugins.fragments.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.plugins.meetings.MeetingsRoom;
import org.studip.unofficial_app.databinding.DialogMeetingsRoomsBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.viewmodels.MeetingsRoomsViewModel;
import org.studip.unofficial_app.model.viewmodels.StringSavedStateViewModelFactory;
import org.studip.unofficial_app.model.viewmodels.StringViewModelFactory;
import org.studip.unofficial_app.ui.HomeActivity;
import org.studip.unofficial_app.ui.plugins.MeetingsActivity;

public class MeetingsRoomsDialog extends DialogFragment
{
    public static final String COURSE_ID_KEY = "cid";
    private DialogMeetingsRoomsBinding binding;
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());
        
        Bundle args = getArguments();
        
        if (args == null || args.getString(COURSE_ID_KEY) == null) {
            dismiss();
            return b.create();
        }
        
        binding = DialogMeetingsRoomsBinding.inflate(getLayoutInflater());
        MeetingsRoomsViewModel m = new ViewModelProvider(this,new StringSavedStateViewModelFactory(this, null,
                requireActivity().getApplication(),args.getString(COURSE_ID_KEY))).get(MeetingsRoomsViewModel.class);
        
        m.getRooms().observe(this, rooms -> {
            if (rooms != null) {
                for (MeetingsRoom r : rooms) {
                    Button button = new Button(requireActivity());
                    button.setText(r.name);
                    button.setOnClickListener(v1 -> {
                        API api = APIProvider.getAPI(requireActivity());
                        if (api == null || api.getUserID() == null) {
                            return;
                        }
                        Intent i = new Intent(requireActivity(), MeetingsActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("url", API.HTTPS+api.getHostname()+"/plugins.php/meetingplugin/api/rooms/join/"+r.course_id+"/"+r.meeting_id);
                        startActivity(i);
                    });
                    button.setOnLongClickListener(v -> {
                        API api = APIProvider.getAPI(requireActivity());
                        if (api == null || api.getUserID() == null) {
                            return true;
                        }
                        final Activity a = requireActivity();
                        if (ShortcutManagerCompat.isRequestPinShortcutSupported(a)) {
                            ShortcutInfoCompat.Builder info = new ShortcutInfoCompat.Builder(a, "meeting:"+r.meeting_id);
                            info.setIcon(IconCompat.createWithResource(a, R.drawable.chat_blue));
                            info.setShortLabel(r.name);
                            Intent i = new Intent(requireActivity(), MeetingsActivity.class);
                            i.setAction(a.getPackageName()+".dynamic_shortcut");
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.putExtra("url", API.HTTPS+api.getHostname()+"/plugins.php/meetingplugin/api/rooms/join/"+r.course_id+"/"+r.meeting_id);
                            info.setIntent(i);
                            ShortcutManagerCompat.requestPinShortcut(a, info.build(), null);
                        }
                        return true;
                    });
                    binding.rooms.addView(button);
                }
            }
        });
        
        m.isError().observe(this, error -> {
            if (error) {
                dismiss();
            }
        });
        
        
        b.setView(binding.getRoot());
        
        return b.create();
    }
}
