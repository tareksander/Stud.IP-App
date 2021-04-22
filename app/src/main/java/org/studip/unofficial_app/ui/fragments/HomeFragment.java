package org.studip.unofficial_app.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.rest.StudipNews;
import org.studip.unofficial_app.api.rest.StudipUser;
import org.studip.unofficial_app.databinding.FragmentHomeBinding;
import org.studip.unofficial_app.model.viewmodels.HomeActivityViewModel;
import org.studip.unofficial_app.model.viewmodels.HomeViewModel;
import org.studip.unofficial_app.ui.HomeActivity;
import org.studip.unofficial_app.ui.NewsAdapter;

import java.util.List;


public class HomeFragment extends Fragment
{
    private HomeViewModel m;
    private HomeActivityViewModel homem;
    private FragmentHomeBinding binding;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        
        
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        m = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        homem = new ViewModelProvider(requireActivity()).get(HomeActivityViewModel.class);;
        binding = FragmentHomeBinding.inflate(inflater);
        binding.setLifecycleOwner(this);
        binding.setC(requireContext());
        binding.setAdapter(new NewsAdapter(requireContext(), ArrayAdapter.NO_SELECTION));
        binding.setM(m);
        
        m.news.isRefreshing().observe(getViewLifecycleOwner(), binding.homeRefresh::setRefreshing);
        m.news.get().observe(getViewLifecycleOwner(), studipNews ->
        {
            if (studipNews.size() == 0) {
                m.news.refresh(requireContext());
            }
            binding.getAdapter().setNews(studipNews.toArray(new StudipNews[0]));
        });
        m.news.getStatus().observe(getViewLifecycleOwner(), status -> {
            if (homem.connectionLostDialogShown.getValue() != null && ! homem.connectionLostDialogShown.getValue()) {
                if (status != -1)
                {
                    //System.out.println(status);
                    if (status == 401)
                    {
                        homem.connectionLostDialogShown.setValue(true);
                        HomeActivity.showConnectionLostDialog(requireActivity(), true);
                    }
                    else
                    {
                        if (status != 200)
                        {
                            homem.connectionLostDialogShown.setValue(true);
                            HomeActivity.showConnectionLostDialog(requireActivity(), false);
                        }
                    }
                }
            }
        });
        
        return binding.getRoot();
    }
    
    
    
    
}