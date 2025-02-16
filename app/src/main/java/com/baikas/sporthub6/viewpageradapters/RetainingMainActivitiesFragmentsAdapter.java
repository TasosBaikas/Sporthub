package com.baikas.sporthub6.viewpageradapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.baikas.sporthub6.fragments.mainpage.home.HomeFragment;
import com.baikas.sporthub6.fragments.mainpage.chatpreview.ChatPreviewFragment;
import com.baikas.sporthub6.fragments.mainpage.terrainnearby.TerrainNearbyFragment;
import com.baikas.sporthub6.models.MatchFilter;

public class RetainingMainActivitiesFragmentsAdapter extends FragmentStateAdapter {

    Bundle bundle;

    public RetainingMainActivitiesFragmentsAdapter(FragmentActivity fragmentActivity, MatchFilter matchFilter) {
        super(fragmentActivity);

        bundle = new Bundle();

        bundle.putSerializable("matchFilter", matchFilter);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return HomeFragment.newInstance(bundle);
            case 1:
                return new ChatPreviewFragment();
            case 2:
                return new TerrainNearbyFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;  // since you have 3 fragments
    }
}
