package com.baikas.sporthub6.viewpageradapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.baikas.sporthub6.fragments.mainpage.chatpreview.ChatPreviewContentFragment;

import java.util.List;

public class ViewPager2ChatPreviewFragmentsAdapter extends FragmentStateAdapter {

    private final List<Bundle> fragmentBundles;

    public ViewPager2ChatPreviewFragmentsAdapter(@NonNull FragmentActivity fragmentActivity, List<Bundle> fragmentBundles) {
        super(fragmentActivity);
        this.fragmentBundles = fragmentBundles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return ChatPreviewContentFragment.newInstance(fragmentBundles.get(position));
    }

    @Override
    public int getItemCount() {
        return fragmentBundles.size();
    }
}
