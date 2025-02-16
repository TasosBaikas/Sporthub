package com.baikas.sporthub6.viewmodels;

import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.MatchFilter;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MatchFilterActivityViewModel extends ViewModel {

    private MatchFilter matchFilter;

    @Inject
    public MatchFilterActivityViewModel() {
    }

    public MatchFilter getFilter() {
        return matchFilter;
    }

    public void setFilter(MatchFilter matchFilter) {
        this.matchFilter = matchFilter;
    }
}
