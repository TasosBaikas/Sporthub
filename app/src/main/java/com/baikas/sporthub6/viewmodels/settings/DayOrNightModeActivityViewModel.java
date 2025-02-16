package com.baikas.sporthub6.viewmodels.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.user.User;


public class DayOrNightModeActivityViewModel extends ViewModel {

    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public LiveData<User> saveToSharedPreferencesDayOrNightMode(String dayOrNightMode) {
        MutableLiveData<User> liveData = new MutableLiveData<>();



        return liveData;
    }

}
