package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import org.studip.unofficial_app.api.rest.StudipNews;
import org.studip.unofficial_app.databinding.CourseNewsDialogBinding;
import org.studip.unofficial_app.model.viewmodels.NewsDialogViewModel;
import org.studip.unofficial_app.model.viewmodels.StringViewModelFactory;
import org.studip.unofficial_app.ui.HomeActivity;
import org.studip.unofficial_app.ui.NewsAdapter;

public class CourseNewsDialogFragment extends DialogFragment
{
    

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());


        CourseNewsDialogBinding binding = CourseNewsDialogBinding.inflate(getLayoutInflater());
        
        String cid = getArguments().getString("cid");
        if (cid != null) {
            NewsDialogViewModel m = new ViewModelProvider(this,new StringViewModelFactory(requireActivity().getApplication(),cid)).get(NewsDialogViewModel.class);

            final NewsAdapter ad = new NewsAdapter(requireContext(), ArrayAdapter.NO_SELECTION);
            binding.courseNewsList.setAdapter(ad);
            m.news.get().observe(this, (news) -> {
                //System.out.println(news);
                if (news.size() == 0 && m.news.getStatus().getValue() == 200) {
                    dismiss();
                } else {
                    ad.setNews(news.toArray(new StudipNews[0]));
                }
            });
            
            m.news.getStatus().observe(this, (status) -> {
                if (status != 200 && status != -1)
                {
                    dismiss();
                    HomeActivity.onStatusReturn(requireActivity(), status);
                }
            });
            m.news.refresh(requireActivity());
        } else {
            dismiss();
        }
        
        b.setView(binding.getRoot());
        return b.create();
    }
}
