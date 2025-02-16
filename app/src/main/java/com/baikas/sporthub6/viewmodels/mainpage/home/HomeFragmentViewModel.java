package com.baikas.sporthub6.viewmodels.mainpage.home;

import android.os.Bundle;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.helpers.time.GreekDateFormatter;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.models.MatchFilter;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;


@HiltViewModel
public class HomeFragmentViewModel extends ViewModel {

    private final UserRepository userRepository;
    private MutableLiveData<MatchFilter> matchFilterLiveData = new MutableLiveData<>();
    private boolean firstInit = true;
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<AtomicReference<String>> selectedSportLiveData = new MutableLiveData<>();
    private final int NUMBER_OF_DAYS_PLUS_ALL = 16;


    @Inject
    public HomeFragmentViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;

        matchFilterLiveData.setValue(MatchFilter.resetFilterDisabled());
    }


    public List<Bundle> getFragmentsBundles(String sport) {
        List<Bundle> fragmentBundles = new ArrayList<>();

        Bundle bundle = new Bundle();

        bundle.putLong("dateBeginForPaginationUTC", 0);
        bundle.putLong("dateLastForPaginationUTC", 0);
        bundle.putString("sport",sport);
        bundle.putSerializable("matchFilter",matchFilterLiveData.getValue());
        fragmentBundles.add(bundle);

        Instant timeAddAlways1Day = Instant.ofEpochMilli(TimeFromInternet.getInternetTimeEpochUTC());
        for (int i = 1; i < NUMBER_OF_DAYS_PLUS_ALL; i++) {

            long timeInEpochOla = timeAddAlways1Day.toEpochMilli();
            Pair<Long,Long> startAndEndOfDay = GreekDateFormatter.getStartAndEndOfDay(timeInEpochOla);

            bundle = new Bundle();
            bundle.putLong("dateBeginForPaginationUTC", startAndEndOfDay.first);
            bundle.putLong("dateLastForPaginationUTC", startAndEndOfDay.second);
            bundle.putString("sport",sport);
            bundle.putSerializable("matchFilter",matchFilterLiveData.getValue());
            fragmentBundles.add(i,bundle);

            timeAddAlways1Day = timeAddAlways1Day.plus(1, ChronoUnit.DAYS);

        }

        return fragmentBundles;
    }

    public List<String> getDayAndMonthFormatted(){
        List<String> daysAndMonthsFormatted = new ArrayList<>();

        Instant timeAddAlways1Day = Instant.ofEpochMilli(TimeFromInternet.getInternetTimeEpochUTC());

        String textOla = "Ολες";
        daysAndMonthsFormatted.add(textOla);

        for (int i = 1; i < NUMBER_OF_DAYS_PLUS_ALL; i++) {
            // Set data to your itemView here
            String dayInGreek = GreekDateFormatter.returnDayNameInGreekNoTones(timeAddAlways1Day.toEpochMilli()).substring(0,3);
            String text =  dayInGreek + "\n" + GreekDateFormatter.epochToFormattedDayAndMonth(timeAddAlways1Day.toEpochMilli());
            daysAndMonthsFormatted.add(text);


            timeAddAlways1Day = timeAddAlways1Day.plus(1, ChronoUnit.DAYS);
        }

        return daysAndMonthsFormatted;
    };

    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept((user -> liveData.postValue(user)))
                .exceptionally((throwable)->{
                    errorMessageLiveData.postValue("Δοκιμάστε ξανά");
                    return null;
                });

        return liveData;
    }



    public boolean firstInit() {
        boolean firstInitTemp = firstInit;
        firstInit = false;

        return firstInitTemp;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }


    public MutableLiveData<AtomicReference<String>> getSelectedSportLiveData() {
        return selectedSportLiveData;
    }

    public MutableLiveData<MatchFilter> getMatchFilterLiveData() {
        return matchFilterLiveData;
    }
}
