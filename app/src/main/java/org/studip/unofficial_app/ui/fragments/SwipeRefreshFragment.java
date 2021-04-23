package org.studip.unofficial_app.ui.fragments;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public abstract class SwipeRefreshFragment extends Fragment
{
    private SwipeRefreshLayout ref;
    protected void setSwipeRefreshLayout(SwipeRefreshLayout ref) {
        this.ref = ref;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (ref != null && ref.isRefreshing()) { // this is somehow needed, because the spinner disappears when changing fragments and returning
            ref.setRefreshing(false);
            ref.setRefreshing(true);
        }
    }
}
