package org.studip.unofficial_app.ui.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import org.studip.unofficial_app.api.rest.StudipNews;
import org.studip.unofficial_app.databinding.CourseNewsDialogBinding;
import org.studip.unofficial_app.model.viewmodels.NewsDialogViewModel;
import org.studip.unofficial_app.model.viewmodels.StringViewModelFactory;
import org.studip.unofficial_app.ui.HomeActivity;
import org.studip.unofficial_app.ui.NewsAdapter;

import java.util.List;

public class CourseNewsDialogFragment extends DialogFragment
{
    
    private static final String LIST_KEY = "list";
    private CourseNewsDialogBinding binding;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());


        binding = CourseNewsDialogBinding.inflate(getLayoutInflater());
        Bundle args = getArguments();
        if (args == null) {
            dismiss();
            return  b.create();
        }
        String cid = args.getString("cid");
        if (cid != null) {
            NewsDialogViewModel m = new ViewModelProvider(this,new StringViewModelFactory(requireActivity().getApplication(),cid)).get(NewsDialogViewModel.class);

            final NewsAdapter ad = new NewsAdapter(requireContext(), ArrayAdapter.NO_SELECTION);
            binding.courseNewsList.setAdapter(ad);
            m.news.get().observe(this, (news) -> {
                if (news.size() != 0) {
                    ad.setNews(news.toArray(new StudipNews[0]));
                }
                if (savedInstanceState != null && savedInstanceState.containsKey(LIST_KEY)) {
                    binding.courseNewsList.onRestoreInstanceState(savedInstanceState.getParcelable(LIST_KEY));
                    savedInstanceState.remove(LIST_KEY);
                }
            });
            
            m.news.isRefreshing().observe(this, ref -> {
                if (! ref) {
                    List<StudipNews> l = m.news.get().getValue();
                    if (l != null && l.size() == 0) {
                        dismiss();
                    }
                }
            });
            
            m.news.getStatus().observe(this, (status) -> {
                //System.out.println(status);
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
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LIST_KEY, binding.courseNewsList.onSaveInstanceState());
    }
}
