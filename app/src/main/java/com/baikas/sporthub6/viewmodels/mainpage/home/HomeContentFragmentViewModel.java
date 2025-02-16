package com.baikas.sporthub6.viewmodels.mainpage.home;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.NoInternetConnectionException;
import com.baikas.sporthub6.exceptions.NoMoreDataToLoadException;
import com.baikas.sporthub6.exceptions.PaginationException;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.helpers.comparators.MatchComparator;
import com.baikas.sporthub6.helpers.operations.ListOperations;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.time.GreekDateFormatter;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.models.MatchFilter;
import com.baikas.sporthub6.models.RequestMatchesFromServer;
import com.baikas.sporthub6.models.fakemessages.FakeMatchDayDelimiter;
import com.baikas.sporthub6.models.fakemessages.FakeMatchForMessage;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.repositories.MatchRepository;
import com.baikas.sporthub6.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeContentFragmentViewModel extends ViewModel {

    private boolean isLoading = false;
    private boolean noMoreItemsToLoad = false;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final CheckInternetConnection checkInternetConnection;
    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Match>> matchesLiveData = new MutableLiveData<>();
    private RequestMatchesFromServer requestMatchesFromServer;

    public final int MAX_ITEMS_PER_SCROLL = 9;


    @Inject
    public HomeContentFragmentViewModel(MatchRepository matchRepository, UserRepository userRepository, CheckInternetConnection checkInternetConnection) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.checkInternetConnection = checkInternetConnection;
        matchesLiveData.setValue(new ArrayList<>());

        this.requestMatchesFromServer = new RequestMatchesFromServer("",0,0,"",MatchFilter.resetFilterDisabled(),MAX_ITEMS_PER_SCROLL);
    }

    public static long start;
    public void loadDataFromServer(RequestMatchesFromServer requestMatchesFromServer){

        getMatches().removeIf((match) -> match instanceof FakeMatchForMessage && !match.getId().equals("NoMoreTeams"));
        getMatches().removeIf((Match match) -> match instanceof FakeMatchDayDelimiter);

        if (!getMatches().isEmpty())
            return;

//        start = TimeFromInternet.getInternetTimeEpochUTC();


        userRepository.getUserById(FirebaseAuth.getInstance().getUid())//this is a very fast operation
                .thenCompose(user -> matchRepository.getMatchesCloseToUser(user,requestMatchesFromServer))
                .thenAccept(moreMatches -> {
//                    long end = TimeFromInternet.getInternetTimeEpochUTC();
//                    messageToUserLiveData.postValue(String.valueOf(end-start));
                    if (moreMatches == null || moreMatches.isEmpty()) {
                        this.noMoreItemsToLoad = true;
                        throw new NoMoreDataToLoadException();
                    }

                    new Handler(Looper.getMainLooper()).post(()-> {

                        ListOperations.combineListsAndAfterSort(moreMatches,getMatches(),new MatchComparator());

                        if (!getMatches().isEmpty() && getMatches().get(getMatches().size() - 1) instanceof FakeMatchForMessage)
                            getMatches().remove(getMatches().size() - 1);

                        if (getMatches().size() < MAX_ITEMS_PER_SCROLL){

                            this.noMoreItemsToLoad = true;
                            getMatches().add(FakeMatchForMessage.fakeMatchNoMoreTeams("Δεν υπάρχουν άλλες ομάδες"));
                        }
                    });
                })
                .exceptionally(throwable -> {

                    Throwable cause = throwable.getCause();

                    FakeMatchForMessage fakeMatchForMessage;
                    if (cause instanceof NoMoreDataToLoadException) {//todo also this must show a message at the bottom or something

                        if (!getMatches().isEmpty()) {
                            Match lastMatch = getMatches().get(getMatches().size() - 1);
                            if (lastMatch instanceof FakeMatchForMessage && lastMatch.getId().equals("NoMoreTeams"))
                                return null;
                        }

                        fakeMatchForMessage = FakeMatchForMessage.fakeMatchNoMoreTeams("Δεν υπάρχουν άλλες ομάδες");
                        getMatches().add(fakeMatchForMessage);
                        return null;
                    }

                    if (cause instanceof TimeoutException) {//
                        if (getMatches().isEmpty()) {
                            fakeMatchForMessage = new FakeMatchForMessage("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                            getMatches().add(fakeMatchForMessage);
                        }

                        messageToUserLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                        Log.i(TAG, "Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά", cause);
                        return null;
                    }

                    if (cause instanceof PaginationException) {//todo i have to handle the situation where the pagination fails because the lastVisible has no match
                        messageToUserLiveData.postValue("Υπάρχει πρόβλημα... Κλείστε και ανοίξτε την εφαρμογή!");
                        Log.i(TAG, "PaginationException: ", cause);
                        return null;
                    }

                    if (cause instanceof NoInternetConnectionException || !checkInternetConnection.isNetworkConnected()) {
                        if (getMatches().isEmpty()) {
                            fakeMatchForMessage = new FakeMatchForMessage("Δεν έχετε internet");
                            getMatches().add(fakeMatchForMessage);
                        }

                        Log.i(TAG, "Δεν έχετε internet", cause);;
                        messageToUserLiveData.postValue("Δεν έχετε internet");
                        return null;
                    }


                    Log.e(TAG, "Error fetching data: " + throwable.getMessage());
                    messageToUserLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                    return null;
                }).whenComplete((unused,unusedThrowable)->{//todo update getmore
                    new Handler(Looper.getMainLooper()).post(()->{
                        this.setLoading(false);


                        matchesLiveData.setValue(getMatches());
                    });
                });

    }

    public void addDayDelimiters(List<Match> matches) {

        if (requestMatchesFromServer.getDateBeginForPaginationUTC() != 0 && requestMatchesFromServer.getDateLastForPaginationUTC() != 0)
            return;

        if (matches == null || matches.isEmpty() || matches.get(0) instanceof FakeMatchForMessage)
            return;


        matches.removeIf((Match match) -> match instanceof FakeMatchDayDelimiter);

        for (int i = 0; i < matches.size() - 1; i++) {

            Match match = matches.get(i);
            if (match instanceof FakeMatchDayDelimiter || match instanceof FakeMatchForMessage)
                continue;

            Match matchAfter = matches.get(i + 1);
            if (matchAfter instanceof FakeMatchDayDelimiter || matchAfter instanceof FakeMatchForMessage)
                continue;


            if (GreekDateFormatter.isDifferentCalendarDay(match.getMatchDateInUTC(), matchAfter.getMatchDateInUTC())){

                matches.add(i + 1,new FakeMatchDayDelimiter());
                continue;
            }

        }

    }


    public void getMoreData(RequestMatchesFromServer requestMatchesFromServer){


        getMatches().removeIf((Match match) -> match instanceof FakeMatchDayDelimiter);

        userRepository.getUserById(FirebaseAuth.getInstance().getUid())
                .thenCompose(user -> matchRepository.getMatchesCloseToUser(user,requestMatchesFromServer))
                .thenAccept(moreMatches -> {
                    if (moreMatches == null || moreMatches.isEmpty()) {
                        noMoreItemsToLoad = true;
                        throw new NoMoreDataToLoadException();
                    }

                    new Handler(Looper.getMainLooper()).post(()-> {

                        getMatches().removeAll(Collections.singleton(null));

                        ListOperations.combineListsAndAfterSort(moreMatches,getMatches(),new MatchComparator());
                    });
                })
                .exceptionally(throwable -> {
                    getMatches().removeAll(Collections.singleton(null));


                    if (!getMatches().isEmpty()) {
                        Match lastMatch = getMatches().get(getMatches().size() - 1);
                        if (lastMatch instanceof FakeMatchForMessage && !lastMatch.getId().equals("NoMoreTeams"))
                            getMatches().remove(getMatches().size() - 1);
                    }

                    Throwable cause = throwable.getCause();

                    FakeMatchForMessage fakeMatchForMessage;
                    if (cause instanceof NoMoreDataToLoadException) {//todo also this must show a message at the bottom or something


                        if (!getMatches().isEmpty()) {
                            Match lastMatch = getMatches().get(getMatches().size() - 1);
                            if (lastMatch instanceof FakeMatchForMessage && lastMatch.getId().equals("NoMoreTeams"))
                                return null;
                        }

                        fakeMatchForMessage = FakeMatchForMessage.fakeMatchNoMoreTeams("Δεν υπάρχουν άλλες ομάδες");
                        getMatches().add(fakeMatchForMessage);
                        return null;
                    }

                    if (cause instanceof TimeoutException) {//
                        messageToUserLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                        Log.i(TAG, "loadDataFromServer: timeout", cause);
                        return null;
                    }

                    if (cause instanceof PaginationException) {//todo i have to handle the situation where the pagination fails because the lastVisible has no match
                        messageToUserLiveData.postValue("Υπάρχει πρόβλημα... Κλείστε και ανοίξτε την εφαρμογή!");
                        Log.i(TAG, "PaginationException: ", cause);
                        return null;
                    }

                    if (cause instanceof NoInternetConnectionException || !checkInternetConnection.isNetworkConnected()) {
                        messageToUserLiveData.postValue("Δεν έχετε internet");
                        return null;
                    }


                    Log.e(TAG, "Error fetching data: " + throwable.getMessage());
                    messageToUserLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                    return null;
                }).whenComplete((unused,unusedThrowable)->{
                    new Handler(Looper.getMainLooper()).post(()->{
                        setLoading(false);

                        matchesLiveData.setValue(getMatches());
                    });
                });
    }

    public LiveData<Match> userToJoinMatch(String requesterId, String matchId, String sport) {
        MutableLiveData<Match> liveData = new MutableLiveData<>();

        matchRepository.userJoinOrCancelRequestForMatch(requesterId, matchId, sport)
                .thenAccept((Match fakeRefMatch) -> {

                    Match updatedRef = new Match(fakeRefMatch);

                    if (updatedRef.isUserRequestedToJoin(requesterId))
                        messageToUserLiveData.postValue("Περιμένετε τον διαχειριστή να απαντήσει!");

//                    List<Match> matchesFakeRef = getMatches();
//                    ListOperations.setElementToList(fakeRefMatch,matchesFakeRef);
//                    getMatchesLiveData().setValue(matchesFakeRef);

                    liveData.postValue(updatedRef);
                })
                .exceptionally((eDontUse -> {

                    Throwable cause = eDontUse.getCause();

                    messageToUserLiveData.postValue(cause.getMessage());
                    return null;
                }));


        return liveData;
    }



    public void clearData() {
        this.isLoading = false;
        this.noMoreItemsToLoad = false;


        requestMatchesFromServer.setLastVisibleDocumentMatchId("");

        getMatches().clear();
    }


    public boolean isNoMoreItemsToLoad() {
        return noMoreItemsToLoad;
    }

    public void setNoMoreItemsToLoad(boolean noMoreItemsToLoad) {
        this.noMoreItemsToLoad = noMoreItemsToLoad;
    }

    public MutableLiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }

    public List<Match> getMatches(){
        return matchesLiveData.getValue();
    }


    public MutableLiveData<List<Match>> getMatchesLiveData() {
        return matchesLiveData;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public RequestMatchesFromServer getRequestMatchesFromServer() {
        return requestMatchesFromServer;
    }

    public void setRequestMatchesFromServer(RequestMatchesFromServer requestMatchesFromServer) {
        this.requestMatchesFromServer = requestMatchesFromServer;
    }
}
