package com.baikas.sporthub6.viewmodels.settings.terrainaddresses;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.NoMoreDataToLoadException;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.models.fakemessages.FakeTerrainAddressForMessage;
import com.baikas.sporthub6.models.TerrainAddress;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.TerrainAddressRepository;
import com.baikas.sporthub6.repositories.UserRepository;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TerrainAddresses2ActivityViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final TerrainAddressRepository terrainAddressRepository;
    private LatLng terrainLatLng;
    private String terrainAddress;
    private String terrainTitle;
    private String sportName;
    private CheckInternetConnection checkInternetConnection;
    private final List<TerrainAddress> terrainAddressList = new ArrayList<>();
    private final MutableLiveData<Void> terrainAddressLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    @Inject
    public TerrainAddresses2ActivityViewModel(UserRepository userRepository, TerrainAddressRepository terrainAddressRepository, CheckInternetConnection checkInternetConnection) {
        this.userRepository = userRepository;
        this.terrainAddressRepository = terrainAddressRepository;
        this.checkInternetConnection = checkInternetConnection;
    }

    public void loadAddressesFromServer(String sportNameEnglish){

        String yourId = FirebaseAuth.getInstance().getUid();
        terrainAddressRepository.getSportTerrainAddresses(yourId,sportNameEnglish)
                .thenAccept((List<TerrainAddress> newAddresses) -> {

                    terrainAddressList.clear();
                    if (newAddresses == null || newAddresses.size() == 0){
                        throw new NoMoreDataToLoadException();
                    }

                    terrainAddressList.addAll(newAddresses);
                })
                .exceptionally(throwable -> {

                    terrainAddressList.removeIf((terrainAddress) -> terrainAddress instanceof FakeTerrainAddressForMessage);

                    Throwable cause = throwable.getCause();

                    if (cause instanceof NoMoreDataToLoadException) {//todo also this must show a message at the bottom or something

                        terrainAddressList.add(new FakeTerrainAddressForMessage("Δεν έχετε ορίσει κάποια διεύθυνση"));

                        return null;
                    }

                    if (!checkInternetConnection.isNetworkConnected()) {

                        terrainAddressList.add(new FakeTerrainAddressForMessage("Δεν έχετε internet"));

                        Log.i(TAG, "Δεν έχετε internet", cause);
                        errorMessageLiveData.postValue("Δεν έχετε internet");
                        return null;
                    }

                    terrainAddressList.add(new FakeTerrainAddressForMessage("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά"));

                    Log.e(TAG, "Error fetching data: " + throwable.getMessage());
                    errorMessageLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                    return null;
                }).whenComplete((unused,unusedThrowable)->{

                    if (terrainAddressList.size() - 1 >= 0){
                        TerrainAddress lastElement = terrainAddressList.get(terrainAddressList.size() - 1);
                        if (!(lastElement instanceof FakeTerrainAddressForMessage))
                            terrainAddressList.add(new FakeTerrainAddressForMessage("Δεν έχετε ορίσει άλλη διεύθυνση"));

                    }

                    terrainAddressLiveData.postValue(null);

                });

    }

    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept((user -> liveData.postValue(user)))
                .exceptionally((throwable)->{
                    errorMessageLiveData.postValue("Δοκίμασε ξανά!");
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

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                });

        return mutableLiveData;
    }

    public LiveData<Void> updateTerrainTitle(TerrainAddress terrainAddress) {
        MutableLiveData<Void> mutableLiveData = new MutableLiveData<>();

        terrainAddressRepository.updateTerrainTitle(terrainAddress)
                .thenAccept((unused -> mutableLiveData.postValue(unused)))
                .exceptionally((e)->{
                    Throwable cause = e.getCause();

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                });

        return mutableLiveData;
    }

    public MutableLiveData<Void> deleteTerrainAddress(String terrainId, String sport, String userId) {
        MutableLiveData<Void> mutableLiveData = new MutableLiveData<>();

        terrainAddressRepository.deleteTerrainAddressById(terrainId,sport,userId)
                .thenAccept((unused -> mutableLiveData.postValue(unused)))
                .exceptionally((e)->{
                    Throwable cause = e.getCause();

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                });

        return mutableLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public MutableLiveData<Void> getTerrainAddressesLiveData() {
        return terrainAddressLiveData;
    }

    public List<TerrainAddress> getTerrainAddressesList() {
        return terrainAddressList;
    }

    public LatLng getTerrainLatLng() {
        return terrainLatLng;
    }

    public String getTerrainTitle() {
        return terrainTitle;
    }

    public void setTerrainTitle(String terrainTitle) {
        this.terrainTitle = terrainTitle;
    }

    public void setTerrainLatLng(LatLng terrainLatLng) {
        this.terrainLatLng = terrainLatLng;
    }

    public String getTerrainAddress() {
        return terrainAddress;
    }

    public void setTerrainAddress(String terrainAddress) {
        this.terrainAddress = terrainAddress;
    }

    public String getSportName() {
        return sportName;
    }

    public void setSportName(String sportName) {
        this.sportName = sportName;
    }



}
