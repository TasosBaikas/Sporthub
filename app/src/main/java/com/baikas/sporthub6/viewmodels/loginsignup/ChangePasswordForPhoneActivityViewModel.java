package com.baikas.sporthub6.viewmodels.loginsignup;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.result.Result;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChangePasswordForPhoneActivityViewModel extends ViewModel {

    private final MutableLiveData<Result<Void>> messageToUserLiveData = new MutableLiveData<>();


    @Inject
    public ChangePasswordForPhoneActivityViewModel() {
    }

    public MutableLiveData<Result<Void>> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }
}
