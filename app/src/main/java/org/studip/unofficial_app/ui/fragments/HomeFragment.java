package org.studip.unofficial_app.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.Features;
import org.studip.unofficial_app.api.rest.StudipNews;
import org.studip.unofficial_app.databinding.FragmentHomeBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.viewmodels.HomeViewModel;
import org.studip.unofficial_app.ui.HomeActivity;
import org.studip.unofficial_app.ui.NewsAdapter;


public class HomeFragment extends SwipeRefreshFragment
{
    private HomeViewModel m;
    private FragmentHomeBinding binding;
    
    private static final String LIST_KEY = "list";

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        m = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        
        
        binding = FragmentHomeBinding.inflate(inflater);
        binding.setLifecycleOwner(this);
        binding.setC(requireContext());
        binding.setAdapter(new NewsAdapter(requireContext(), ArrayAdapter.NO_SELECTION));
        binding.setM(m);
        
        setSwipeRefreshLayout(binding.homeRefresh);
    
        API api = APIProvider.getAPI(requireActivity());
        if (api != null && api.isFeatureEnabled(Features.FEATURE_GLOBAL_NEWS)) {
            m.news.isRefreshing().observe(getViewLifecycleOwner(), binding.homeRefresh::setRefreshing);
            binding.homeRefresh.setOnRefreshListener(() -> m.news.refresh(requireActivity()));
            m.news.get().observe(getViewLifecycleOwner(), studipNews ->
            {
                if (studipNews.size() == 0) {
                    m.news.refresh(requireContext());
                }
                binding.getAdapter().setNews(studipNews.toArray(new StudipNews[0]));
                if (savedInstanceState != null && savedInstanceState.containsKey(LIST_KEY)) {
                    binding.homeList.onRestoreInstanceState(savedInstanceState.getParcelable(LIST_KEY));
                    savedInstanceState.remove(LIST_KEY);
                }
            });
            m.news.getStatus().observe(getViewLifecycleOwner(), status -> HomeActivity.onStatusReturn(requireActivity(), status));
        } else {
            binding.homeRefresh.setOnRefreshListener(() -> binding.homeRefresh.setRefreshing(false));
        }
        
        binding.duplicateButton.setOnClickListener(v -> {
            Intent i = new Intent(requireActivity(), HomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
            i.setAction(requireActivity().getPackageName()+".duplicate");
            startActivity(i);
        });
        
        return binding.getRoot();
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LIST_KEY,binding.homeList.onSaveInstanceState());
    }
}