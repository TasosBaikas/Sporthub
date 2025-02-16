package com.baikas.sporthub6.viewmodels.settings.terrainaddresses;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.TerrainAddress;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.TerrainAddressRepository;
import com.baikas.sporthub6.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TerrainAddresses1ActivityViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final TerrainAddressRepository terrainAddressRepository;
    private final Map<String,List<TerrainAddress>> allSportTerrainAddressList = new HashMap<>();

    @Inject
    public TerrainAddresses1ActivityViewModel(UserRepository userRepository, TerrainAddressRepository terrainAddressRepository) {
        this.userRepository = userRepository;
        this.terrainAddressRepository = terrainAddressRepository;
    }


    public LiveData<Result<User>> getUserById(String userId) {
        MutableLiveData<Result<User>> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept((user -> liveData.postValue(new Result.Success<>(user))))
                .exceptionally((throwable)->{
                    liveData.postValue(new Result.Failure<>(new RuntimeException("Δοκιμάστε ξανά")));
                    return null;
                });

        return liveData;
    }

    public LiveData<Void> loadTerrainDataLiveData() {
        MutableLiveData<Void> mutableLiveData = new MutableLiveData<>();

        String userId = FirebaseAuth.getInstance().getUid();

        terrainAddressRepository.getAllSportTerrainAddresses(userId)
                .thenAccept((Map<String, List<TerrainAddress>> allSportTerrainAddresses) -> {

                    if (allSportTerrainAddresses == null){
                        return;
                    }

                    allSportTerrainAddressList.putAll(allSportTerrainAddresses);
                    mutableLiveData.postValue(null);
        });

        return mutableLiveData;
    }

    public Map<String, List<TerrainAddress>> allSportTerrainAddresses() {
        return this.allSportTerrainAddressList;
    }
}
