package com.baikas.sporthub6.repositories;

import android.os.Handler;
import android.os.Looper;

import com.baikas.sporthub6.exceptions.PaginationException;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.firebase.dao.MatchFirebaseDao;
import com.baikas.sporthub6.helpers.comparators.MatchComparator;
import com.baikas.sporthub6.models.constants.ConfigurationsConstants;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.interfaces.FirestoreCallbackDocumentChanges;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.RequestMatchesFromServer;
import com.baikas.sporthub6.models.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

public class MatchRepository {

    private final MatchFirebaseDao matchFirebaseDao;


    @Inject
    public MatchRepository(MatchFirebaseDao matchFirebaseDao) {
        this.matchFirebaseDao = matchFirebaseDao;
    }


    public CompletableFuture<List<Match>> getMatchesCloseToUser(User user, RequestMatchesFromServer requestMatchesFromServer){

        CompletableFuture<List<Match>> completableFuture = new CompletableFuture<>();

        int timeout = ConfigurationsConstants.TIMEOUT_TIME;
        timeoutOnTheAsyncCall(completableFuture,timeout);

        matchFirebaseDao.getPaginatedMatchesByDateAndRadius(user,requestMatchesFromServer)
                .addOnSuccessListener((HttpsCallableResult taskResult) -> {
                    if (completableFuture.isDone())
                        return;

                    if (taskResult.getData() == null ) {
                        completableFuture.complete(Collections.emptyList());
                        return;
                    }

                    List<Map<String,Object>> dataList = (List<Map<String,Object>>)taskResult.getData();
                    if (dataList.isEmpty()){
                        completableFuture.complete(Collections.emptyList());
                        return;
                    }
                    List<Match> matchesList = new ArrayList<>();
                    for (Map<String,Object> map: dataList) {
                        matchesList.add(new Match(map));
                    }

                    Match lastMatch = matchesList.get(matchesList.size() - 1);
                    requestMatchesFromServer.setLastVisibleDocumentMatchId(lastMatch.getId());

                    completableFuture.complete(matchesList);
                }).addOnFailureListener((e) -> completableFuture.completeExceptionally(new ValidationException(e.getMessage())));

        return completableFuture;

    }

    public CompletableFuture<Void> saveMatch(Match match) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        Match copyMatch = new Match(match);

        String adminId = copyMatch.getAdmin().getUserId();
        if (!copyMatch.getUsersInChat().contains(adminId))
            copyMatch.getUsersInChat().add(adminId);


        matchFirebaseDao.saveMatch(copyMatch)
                .addOnSuccessListener((HttpsCallableResult result) -> completableFuture.complete(null))
                .addOnFailureListener((e) -> completableFuture.completeExceptionally(e));


        return completableFuture;
    }

    public CompletableFuture<Void> updateMatchIfAdminChangeSomeValues(Match updatedMatch) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        String yourId = FirebaseAuth.getInstance().getUid();

        matchFirebaseDao.updateMatchChangeSomeValues(updatedMatch,yourId)
                .addOnSuccessListener((HttpsCallableResult result) -> completableFuture.complete(null))
                .addOnFailureListener((e) -> completableFuture.completeExceptionally(e));


        return completableFuture;
    }


    private void timeoutOnTheAsyncCall(CompletableFuture<List<Match>> completableFuture, int timeout) {
        WeakReference<CompletableFuture<List<Match>>> futureWeakReference = new WeakReference<>(completableFuture);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            CompletableFuture<List<Match>> future = futureWeakReference.get();

            if (future != null && !future.isDone())
                future.completeExceptionally(new TimeoutException());

        }, timeout);

    }

    public CompletableFuture<Void> userOnlyJoinMatch(String requesterId, String matchId, String sport) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();


        matchFirebaseDao.userOnlyJoinMatch(requesterId,matchId,sport)
                .addOnSuccessListener((HttpsCallableResult result) -> completableFuture.complete(null))
                .addOnFailureListener((e) -> completableFuture.completeExceptionally(e));


        return completableFuture;
    }

    public CompletableFuture<Match> userJoinOrCancelRequestForMatch(String requesterId, String matchId, String sport) {
        CompletableFuture<Match> completableFuture = new CompletableFuture<>();


        matchFirebaseDao.userJoinOrCancelRequestForMatch(requesterId,matchId,sport)
                .addOnSuccessListener((HttpsCallableResult result) -> {

                    if (result.getData() == null ) {
                        completableFuture.completeExceptionally(new RuntimeException("Something went wrong"));
                        return;
                    }

                    completableFuture.complete(new Match((Map<String, Object>) result.getData()));
                }).addOnFailureListener(e -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }


    public CompletableFuture<Match> adminAcceptsUser(String requesterId, String adminId, String matchId, String sport) {
        CompletableFuture<Match> completableFuture = new CompletableFuture<>();


        matchFirebaseDao.adminAcceptsUser(requesterId,adminId,matchId,sport)
                .addOnSuccessListener((HttpsCallableResult result) -> {

                    if (result.getData() == null ) {
                        completableFuture.completeExceptionally(new RuntimeException("Something went wrong"));
                        return;
                    }

                    completableFuture.complete(new Match((Map<String, Object>) result.getData()));
                }).addOnFailureListener(e -> completableFuture.completeExceptionally(e));

       return completableFuture;
    }

    public void initYourMatchesAllSportsListener(String adminId, FirestoreCallbackDocumentChanges<Match> firestoreCallbackDocumentChanges) {
        for (String sportName : SportConstants.SPORTS_MAP.keySet()) {
            matchFirebaseDao.initYourMatchesAllSportsListener(sportName,adminId, firestoreCallbackDocumentChanges);
        }
    }

    public void initYourMatchesAdminAllSportsListener(String adminId, FirestoreCallbackDocumentChanges<Match> firestoreCallbackDocumentChanges) {
        for (String sportName : SportConstants.SPORTS_MAP.keySet()) {
            matchFirebaseDao.initYourMatchesAdminAllSportsListener(sportName,adminId, firestoreCallbackDocumentChanges);
        }
    }

    public void removeListeners() {
        matchFirebaseDao.removeListeners();
    }


    public CompletableFuture<Match> ignoreRequesterIfAdmin(String requesterId,String adminId,String matchId, String sport) {
        CompletableFuture<Match> completableFuture = new CompletableFuture<>();


        matchFirebaseDao.ignoreRequesterIfAdmin(requesterId, adminId, matchId, sport)
                .addOnSuccessListener((HttpsCallableResult result) -> {

                    if (result.getData() == null ) {
                        completableFuture.completeExceptionally(new RuntimeException("Something went wrong"));
                        return;
                    }

                    completableFuture.complete(new Match((Map<String, Object>) result.getData()));
                }).addOnFailureListener(e -> completableFuture.completeExceptionally(e));


        return completableFuture;
    }

    public CompletableFuture<List<Match>> getMatchesThatYouRequested(String userId) {
        CompletableFuture<List<Match>> completableFuture = new CompletableFuture<>();

        List<Task> tasks = new ArrayList<>();
        for (String sportName: SportConstants.SPORTS_MAP.keySet()) {
            tasks.add(matchFirebaseDao.getMatchesThatYouRequested(userId,sportName));
        }

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener((List<Object> results)->{

                    List<Match> matches = new ArrayList<>();
                    for (Object result: results) {

                        if (!(result instanceof QuerySnapshot))
                            continue;

                        QuerySnapshot snapshot = ((QuerySnapshot) result);
                        for (DocumentSnapshot docSnap : snapshot.getDocuments()) {
                            Match match = new Match(docSnap.getData());

                            if (match.isMember(userId))
                                continue;


                            matches.add(match);
                        }
                    }

                    matches.sort(new MatchComparator());
                    completableFuture.complete(matches);
                }).addOnFailureListener((e) -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }

    public CompletableFuture<Match> getMatchById(String matchId,String sport) {
        CompletableFuture<Match> completableFuture = new CompletableFuture<>();

        matchFirebaseDao.getMatchById(matchId,sport)
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (doc == null || !doc.exists()){
                        completableFuture.completeExceptionally(new IllegalStateException("Η Ομάδα δεν υπάρχει!"));
                        return;
                    }

                    completableFuture.complete(new Match(doc.getData()));
                }).addOnFailureListener(completableFuture::completeExceptionally);

        return completableFuture;
    }

}

