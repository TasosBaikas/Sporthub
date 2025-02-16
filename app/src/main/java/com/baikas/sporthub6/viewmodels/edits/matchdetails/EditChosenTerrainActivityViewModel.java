package com.baikas.sporthub6.viewmodels.edits.matchdetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.TerrainAddress;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.MatchRepository;
import com.baikas.sporthub6.repositories.TerrainAddressRepository;
import com.baikas.sporthub6.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EditChosenTerrainActivityViewModel extends ViewModel {

    MatchRepository matchRepository;
    UserRepository userRepository;
    TerrainAddressRepository terrainAddressRepository;
    MutableLiveData<List<TerrainAddress>> terrainAddressListLiveData = new MutableLiveData<>();
    TerrainAddress selectedTerrain;
    Integer radioButtonSelection = null;
    String sport;
    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();


    @Inject
    public EditChosenTerrainActivityViewModel(MatchRepository matchRepository, UserRepository userRepository, TerrainAddressRepository terrainAddressRepository) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.terrainAddressRepository = terrainAddressRepository;
        this.terrainAddressListLiveData.setValue(new ArrayList<>());
    }

    public LiveData<List<String>> getTerrainTitles(String userId, String sport){
        MutableLiveData<List<String>> liveData = new MutableLiveData<>();

        terrainAddressRepository.getSportTerrainAddresses(userId,sport)
                .thenAccept((List<TerrainAddress> terrainList) -> {

                    this.getTerrainAddressList().clear();
                    this.getTerrainAddressList().add(null);
                    this.getTerrainAddressList().addAll(terrainList);

                    List<String> terrainTitlesList = terrainList.stream()
                            .map((TerrainAddress terrainAddress) -> terrainAddress.getAddressTitle())
                            .collect(Collectors.toList());

                    terrainTitlesList.add(0,"Δεν έχει επιλεχθεί γήπεδο");

                    liveData.postValue(terrainTitlesList);

                })
                .exceptionally((e)->{
                    messageToUserLiveData.postValue("Κλείστε και ξανά ανοίξτε την καρτέλα");
                    return null;
                });

        return liveData;
    }

    public LiveData<Match> getMatchById(String matchId, String sport) {
        MutableLiveData<Match> liveData = new MutableLiveData<>();

        matchRepository.getMatchById(matchId,sport).thenAccept((match -> liveData.postValue(match)))
                .exceptionally((throwable)->{
                    messageToUserLiveData.postValue("Δοκιμάστε ξανά");
                    return null;
                });

        return liveData;
    }

    public MutableLiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }

    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept((user -> liveData.postValue(user)))
                .exceptionally((throwable)->{
                    messageToUserLiveData.postValue("Δοκιμάστε ξανά...");
                    return null;
                });

        return liveData;
    }

    public LiveData<Void> saveTerrainAddress(TerrainAddress terrainAddress) {
        MutableLiveData<Void> mutableLiveData = new MutableLiveData<>();

        terrainAddressRepository.saveTerrainAddress(terrainAddress)
                .thenAccept((unused -> mutableLiveData.postValue(unused)))
                .exceptionally((e)->{
                    Throwable cause = e.getCause();

                    messageToUserLiveData.postValue(cause.getMessage());
                    return null;
                });

        return mutableLiveData;
    }

    public LiveData<Void> updateMatch(Match match) {
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        matchRepository.updateMatchIfAdminChangeSomeValues(match).thenAccept((unused -> {
                    messageToUserLiveData.postValue("Επιτυχής αλλαγή");

                    liveData.postValue(null);
                }))
                .exceptionally((eDontUse)->{
                    Throwable cause = eDontUse.getCause();

                    messageToUserLiveData.postValue(cause.getMessage());
                    return null;
                });

        return liveData;
    }

    public TerrainAddress getSelectedTerrain() {
        return selectedTerrain;
    }

    public void setSelectedTerrain(TerrainAddress selectedTerrain) {
        this.selectedTerrain = selectedTerrain;
    }

    public Integer getRadioButtonSelection() {
        return radioButtonSelection;
    }

    public void setRadioButtonSelection(Integer radioButtonSelection) {
        this.radioButtonSelection = radioButtonSelection;
    }

    public MutableLiveData<List<TerrainAddress>> getTerrainAddressListLiveData() {
        return terrainAddressListLiveData;
    }

    public List<TerrainAddress> getTerrainAddressList() {
        return terrainAddressListLiveData.getValue();
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public String getSport() {
        return sport;
    }

}
