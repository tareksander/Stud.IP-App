package org.studip.unofficial_app.ui.plugins.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.plugins.meetings.MeetingsRoom;
import org.studip.unofficial_app.databinding.DialogMeetingsRoomsBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.viewmodels.MeetingsRoomsViewModel;
import org.studip.unofficial_app.model.viewmodels.StringViewModelFactory;
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
        MeetingsRoomsViewModel m = new ViewModelProvider(this,new StringViewModelFactory(requireActivity().getApplication(),args.getString(COURSE_ID_KEY))).get(MeetingsRoomsViewModel.class);
        
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
