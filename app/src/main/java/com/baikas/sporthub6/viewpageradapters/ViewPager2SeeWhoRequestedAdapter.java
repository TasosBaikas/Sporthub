package com.baikas.sporthub6.viewpageradapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.baikas.sporthub6.fragments.mainpage.chatpreview.ChatPreviewContentFragment;
import com.baikas.sporthub6.fragments.managematches.ShowYourMatchesFragment;
import com.baikas.sporthub6.fragments.managematches.YourRequestsToMatchesFragment;
import com.baikas.sporthub6.fragments.managematches.seewhorequested.SeeIgnoredFragment;
import com.baikas.sporthub6.fragments.managematches.seewhorequested.SeeWhoRequestedFragment;

import java.util.List;

public class ViewPager2SeeWhoRequestedAdapter extends FragmentStateAdapter {

    private final Bundle bundle;

    public ViewPager2SeeWhoRequestedAdapter(@NonNull FragmentActivity fragmentActivity, Bundle bundle) {
        super(fragmentActivity);

        this.bundle = bundle;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return SeeWhoRequestedFragment.newInstance(bundle);
            case 1:
                return SeeIgnoredFragment.newInstance(bundle);
            default:
                return SeeWhoRequestedFragment.newInstance(bundle);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
