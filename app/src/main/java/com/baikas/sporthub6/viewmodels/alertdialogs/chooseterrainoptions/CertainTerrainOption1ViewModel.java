package com.baikas.sporthub6.viewmodels.alertdialogs.chooseterrainoptions;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.TerrainAddress;
import com.baikas.sporthub6.repositories.TerrainAddressRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class CertainTerrainOption1ViewModel extends ViewModel {

    private final TerrainAddressRepository terrainAddressRepository;
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private List<String> userSavedTerrainAddresses = new ArrayList<>();

    @Inject
    public CertainTerrainOption1ViewModel(TerrainAddressRepository terrainAddressRepository) {
        this.terrainAddressRepository = terrainAddressRepository;
    }




    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }
}
