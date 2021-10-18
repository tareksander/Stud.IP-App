package org.studip.unofficial_app.ui.fragments.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.rest.StudipCourseMember;
import org.studip.unofficial_app.api.rest.StudipCourseMemberWithUser;
import org.studip.unofficial_app.databinding.DialogCourseMembersBinding;
import org.studip.unofficial_app.databinding.MembersEntryBinding;
import org.studip.unofficial_app.model.viewmodels.CourseMembersViewModel;
import org.studip.unofficial_app.model.viewmodels.StringViewModelFactory;

public class CourseMembersDialogFragment extends DialogFragment
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
        String cid = args.getString("cid");
        if (cid == null) {
            dismiss();
            return b.create();
        }
        
        b.setTitle(getString(R.string.members_dialog_title));
        
        DialogCourseMembersBinding binding = DialogCourseMembersBinding.inflate(getLayoutInflater());
        CourseMembersViewModel m = new ViewModelProvider(this,new StringViewModelFactory(requireActivity().getApplication(), cid))
                .get(CourseMembersViewModel.class);
        
        binding.membersRefresh.setOnRefreshListener(() -> m.members.refresh(requireActivity()));
        m.members.isRefreshing().observe(this, binding.membersRefresh::setRefreshing);
        
        CourseMembersAdapter ad = new CourseMembersAdapter(requireActivity(), ArrayAdapter.IGNORE_ITEM_VIEW_TYPE);
        binding.memberList.setAdapter(ad);
        
        m.members.get().observe(this, (StudipCourseMemberWithUser[] ms) -> {
            if (ms.length == 0) {
                m.members.refresh(requireActivity());
            }
            ad.clear();
            ad.addAll(ms);
        });
        
        
        b.setView(binding.getRoot());
        
        return b.create();
    }
    
    
    public class CourseMembersAdapter extends ArrayAdapter<StudipCourseMemberWithUser> {
    
        public CourseMembersAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }
    
    
        @SuppressLint("SetTextI18n")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            MembersEntryBinding b;
            if (convertView != null) {
                b = MembersEntryBinding.bind(convertView);
            } else {
                b = MembersEntryBinding.inflate(getLayoutInflater());
            }
            StudipCourseMemberWithUser m = getItem(position);
            b.number.setText(Integer.toString(position));
            b.status.setText(m.member.status);
            if (m.user != null) {
                if (m.user.name.suffix != null && ! m.user.name.suffix.equals("")) {
                    b.name.setText(String.format("%s, %s, %s", m.user.name.family, m.user.name.given, m.user.name.suffix));
                }
                else {
                    b.name.setText(String.format("%s, %s", m.user.name.family, m.user.name.given));
                }
            }
            return b.getRoot();
        }
    }
    
    
}
