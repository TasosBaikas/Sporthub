package com.baikas.sporthub6.viewmodels.matches.createnewmatch;


import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.PaginationException;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.models.constants.HasTerrainTypes;
import com.baikas.sporthub6.models.constants.MatchDurationConstants;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.TerrainAddress;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.MatchRepository;
import com.baikas.sporthub6.repositories.TerrainAddressRepository;
import com.baikas.sporthub6.repositories.UserRepository;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CreateNewMatchActivityAndFragmentsViewModel extends ViewModel {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<Pair<Integer,String>> fragmentChangeLiveData = new MutableLiveData<>();
    private final TerrainAddressRepository terrainAddressRepository;
    private List<TerrainAddress> terrainAddressList = new ArrayList<>();
    private final MutableLiveData<Match> matchLiveData = new MutableLiveData<>();
    private final CheckInternetConnection checkInternetConnection;
    private String sport = "";
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Calendar> fromCalendarLiveData = new MutableLiveData<>();
    public static final int ALL_DAYS = 15;

    @Inject
    public CreateNewMatchActivityAndFragmentsViewModel(MatchRepository matchRepository, UserRepository userRepository, TerrainAddressRepository terrainAddressRepository, CheckInternetConnection checkInternetConnection){
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.terrainAddressRepository = terrainAddressRepository;
        this.checkInternetConnection = checkInternetConnection;
        this.fragmentChangeLiveData.setValue(new Pair<>(0,""));


        fromCalendarLiveData.setValue(Calendar.getInstance(TimeZone.getDefault()));

        fromCalendarLiveData.getValue().setTimeInMillis(TimeFromInternet.getInternetTimeEpochUTC());

        List<Long> allowedLevels = new ArrayList<>();
        for (long i = Match.MIN_LEVEL; i <= Match.MAX_LEVEL; i++) {
            allowedLevels.add(i);
        }

        long matchDuration = MatchDurationConstants.returnInMilliSecondsTheTimeInterval(MatchDurationConstants.HOURS_2_GREEK_TEXT);
        String defaultChosenTerrain = HasTerrainTypes.convertPositionToString(HasTerrainTypes.NO_TERRAIN_CHOICE_INT);


        Match match = new Match("","",null,TimeFromInternet.getInternetTimeEpochUTC(),"",false,0, matchDuration, defaultChosenTerrain,null,allowedLevels,
                new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),0.0,0.0);

        matchLiveData.setValue(match);
    }

    public LiveData<User> getUserById(String userId){
        MutableLiveData<User> userLiveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept((User user) -> {
            if (user == null) {
                errorMessageLiveData.postValue("Something went wrong...");
                return;
            }

            userLiveData.postValue(user);
        })
        .exceptionally((throwable)->{
            errorMessageLiveData.postValue("Something went wrong...");
            return null;
        });

        return userLiveData;
    }

    public LiveData<String> saveMatch(Match match) {
        MutableLiveData<String> liveData = new MutableLiveData<>();

        matchRepository.saveMatch(match).thenAccept((Void unsed) -> {
            liveData.postValue("Επιτυχής δημιουργία!");
        }).exceptionally((eDontUse)-> {

            Throwable cause = eDontUse.getCause();

            errorMessageLiveData.postValue(cause.getMessage());
            return null;
        });

        return liveData;
    }

    public LiveData<List<String>> getTerrainTitles(String userId, String sport){
        MutableLiveData<List<String>> liveData = new MutableLiveData<>();

        if (!checkInternetConnection.isNetworkConnected()){
            getErrorMessageLiveData().postValue("No internet connection");
            return liveData;
        }

        terrainAddressRepository.getSportTerrainAddresses(userId,sport)
                .thenAccept((List<TerrainAddress> terrainList) -> {

                    this.terrainAddressList.clear();
                    this.terrainAddressList.addAll(terrainList);

                    List<String> terrainTitlesWithNumbersOfPriority = new ArrayList<>();
                    for (TerrainAddress address:terrainList) {
                        terrainTitlesWithNumbersOfPriority.add(address.getPriority() + 1 + ") " + address.getAddressTitle());
                    }

                    liveData.postValue(terrainTitlesWithNumbersOfPriority);
                })
                .exceptionally((e)->{
                    errorMessageLiveData.postValue("Κλείστε και ξανά ανοίξτε την καρτέλα");
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

    public void checkTimeConstraints() throws ValidationException{

        // Get the current time
        Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Athens"));
        currentTime.setTimeInMillis(TimeFromInternet.getInternetTimeEpochUTC());//todo time from internet prepei na proeidopoihsw

        // Create Calendar instances for the selected times
        Calendar selectedTimeFrom = getFromCalendar();


//        // Check constraints
//        if (selectedTimeFrom.before(currentTime) ) {
//            // Constraint 2: to time can't be earlier than the current time
//            throw new ValidationException("Η αρχική ώρα είναι πριν το τώρα!");
//        } else if (timeDifferenceHours > 10) {
//            // Constraint 3: The time difference between from and to can't be > 10 hours
//            throw new ValidationException("Η διαφορά είναι πάνω από 10 ώρες!");
//        } else if (selectedTimeTo.before(selectedTimeFrom)) {
//            // Constraint 1: to time can't be earlier than from time
//            throw new ValidationException("Η δευτερεύσουσα ώρα είναι πριν την αρχική!");
//        }
    }

    public MutableLiveData<Pair<Integer, String>> getFragmentChangeLiveData() {
        return fragmentChangeLiveData;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }


    public List<TerrainAddress> getTerrainAddressList() {
        return terrainAddressList;
    }


    public void setTerrainAddressList(List<TerrainAddress> terrainAddressList) {
        this.terrainAddressList = terrainAddressList;
    }

    public Calendar getFromCalendar() {
        return fromCalendarLiveData.getValue();
    }

    public MutableLiveData<Calendar> getFromCalendarLiveData(){
        return fromCalendarLiveData;
    }


    public MutableLiveData<Match> getMatchLiveData() {
        return matchLiveData;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public void clearSomeData() {
        terrainAddressList.clear();

        Match match = matchLiveData.getValue();

        match.setTerrainAddress(null);

        getFromCalendar().setTimeInMillis(TimeFromInternet.getInternetTimeEpochUTC());

        List<Long> allowedLevels = new ArrayList<>();
        for (long i = Match.MIN_LEVEL; i <= Match.MAX_LEVEL; i++) {
            allowedLevels.add(i);
        }

        match.setLevels(allowedLevels);

        match.setMatchDetailsFromAdmin("");
        match.setMatchDuration(MatchDurationConstants.returnInMilliSecondsTheTimeInterval(MatchDurationConstants.HOURS_2_GREEK_TEXT));

        String defaultChosenTerrainType = HasTerrainTypes.convertPositionToString(HasTerrainTypes.NO_TERRAIN_CHOICE_INT);
        match.setHasTerrainType(defaultChosenTerrainType);

    }
}
