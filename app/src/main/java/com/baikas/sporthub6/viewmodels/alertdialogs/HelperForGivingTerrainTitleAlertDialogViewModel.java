package com.baikas.sporthub6.viewmodels.alertdialogs;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.repositories.UserRepository;
import com.baikas.sporthub6.repositories.chat.ChatRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HelperForGivingTerrainTitleAlertDialogViewModel extends ViewModel {


    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();


    @Inject
    public HelperForGivingTerrainTitleAlertDialogViewModel() {
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }
}
