package com.baikas.sporthub6.viewpageradapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.baikas.sporthub6.fragments.managematches.ShowYourMatchesFragment;
import com.baikas.sporthub6.fragments.managematches.YourRequestsToMatchesFragment;

public class ViewPager2ShowMatchesAdapter extends FragmentStateAdapter {

    public ViewPager2ShowMatchesAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ShowYourMatchesFragment();
            case 1:
                return new YourRequestsToMatchesFragment();
            default:
                return new ShowYourMatchesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;  // since you have 2 fragments
    }

}
