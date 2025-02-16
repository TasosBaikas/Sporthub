package com.baikas.sporthub6.firebase.dao;

import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.FirestoreCallbackDocumentChanges;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.RequestMatchesFromServer;
import com.baikas.sporthub6.models.user.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class MatchFirebaseDao {

    private final FirebaseFirestore firestore;
    private final Map<String,ListenerRegistration> yourMatchesAdminListenerList = new HashMap<>();
    private final Map<String,ListenerRegistration> yourMatchesListenerList = new HashMap<>();
    private final FirebaseFunctions functions;



    @Inject
    public MatchFirebaseDao(FirebaseFirestore firestore, FirebaseFunctions functions) {
        this.firestore = firestore;
        this.functions = functions;
    }

    public Task<HttpsCallableResult> saveMatch(Match match) {

        Map<String,Object> map = new HashMap<>();

        Gson gson = new Gson();
        String jsonStringMach = gson.toJson(match);
        map.put("match",jsonStringMach);

        return functions.getHttpsCallable("matchRepositorySaveMatch")
                .call(map);
    }


    public Task<HttpsCallableResult> updateMatchChangeSomeValues(Match match, String yourId) {

        Map<String,String> map = new HashMap<>();

        Gson gson = new Gson();
        String jsonStringMach = gson.toJson(match);
        map.put("match",jsonStringMach);

        map.put("yourId",yourId);

        return functions.getHttpsCallable("matchRepositoryUpdateMatchIfAdminChangesSomeValues")
                .call(map);
    }

    public Task<HttpsCallableResult> userOnlyJoinMatch(String yourId, String matchId, String sport) {
        Map<String,String> map = new HashMap<>();

        map.put("yourId",yourId);
        map.put("matchId",matchId);
        map.put("sport",sport);

        return functions.getHttpsCallable("matchRepositoryUserOnlyJoinMatch")
                .call(map);
    }

    public Task<HttpsCallableResult> userJoinOrCancelRequestForMatch(String yourId, String matchId, String sport) {
        Map<String,String> map = new HashMap<>();

        map.put("yourId",yourId);
        map.put("matchId",matchId);
        map.put("sport",sport);

        return functions.getHttpsCallable("matchRepositoryUserJoinOrCancelRequestForMatch")
                .call(map);
    }


    public Task<HttpsCallableResult> adminAcceptsUser(String userToAccept, String adminId, String matchId, String sport) {

        Map<String,String> map = new HashMap<>();

        map.put("userToAccept",userToAccept);
        map.put("adminId",adminId);
        map.put("matchId",matchId);
        map.put("sport",sport);

        return functions.getHttpsCallable("matchRepositoryAdminAcceptsUser")
                .call(map);
    }

    public Task<DocumentSnapshot> getMatchById(String matchId, String sport) {
        CollectionReference matchesCollection = firestore.collection("matches");
        DocumentReference matchDoc = matchesCollection.document(sport).collection("matchesById").document(matchId);

        return matchDoc.get();
    }


    public void initYourMatchesAllSportsListener(String sport, String userId, FirestoreCallbackDocumentChanges<Match> firestoreCallbackDocumentChanges) {
        if (yourMatchesListenerList.get(sport) != null)
            return;

        CollectionReference chatCollection = firestore.collection("matches").document(sport)
                .collection("matchesById");

        ListenerRegistration yourMatchList = chatCollection
                .whereArrayContains("usersInChat",userId)
                .whereGreaterThan("matchDateInUTC", TimeFromInternet.getInternetTimeEpochUTC())
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        firestoreCallbackDocumentChanges.onError(error);
                        return;
                    }

                    List<Match> changedDocuments = value.getDocumentChanges().stream()
                            .map((DocumentChange docChange) -> new Match(docChange.getDocument().getData()))
                            .collect(Collectors.toList());

                    firestoreCallbackDocumentChanges.onSuccess(changedDocuments);
                });


        yourMatchesListenerList.put(sport,yourMatchList);
    }

    public void initYourMatchesAdminAllSportsListener(String sport, String admin, FirestoreCallbackDocumentChanges<Match> firestoreCallbackDocumentChanges) {
        if (yourMatchesAdminListenerList.get(sport) != null)
            return;

        CollectionReference chatCollection = firestore.collection("matches").document(sport)
                .collection("matchesById");

        ListenerRegistration yourMatchAdminList = chatCollection
                .whereEqualTo("admin.userId",admin)
                .whereGreaterThan("matchDateInUTC", TimeFromInternet.getInternetTimeEpochUTC())
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        firestoreCallbackDocumentChanges.onError(error);
                        return;
                    }


                    List<Match> changedDocuments = value.getDocuments().stream()
                            .map((DocumentSnapshot doc) -> new Match(doc.getData()))
                            .collect(Collectors.toList());

                    firestoreCallbackDocumentChanges.onSuccess(changedDocuments);
                });


        yourMatchesAdminListenerList.put(sport,yourMatchAdminList);
    }


    public Task<QuerySnapshot> getMatchesThatYouRequested(String userId, String sport) {
        CollectionReference chatCollection = firestore.collection("matches").document(sport)
                .collection("matchesById");

        return chatCollection
                .whereArrayContains("userRequestsToJoinMatch",userId)
                .whereGreaterThan("matchDateInUTC", TimeFromInternet.getInternetTimeEpochUTC())
                .get();
    }



    public Task<HttpsCallableResult> getPaginatedMatchesByDateAndRadius(User user, RequestMatchesFromServer requestMatchesFromServer) {
        Map<String,Object> map = new HashMap<>();

        Gson gson = new Gson();
        String userString = gson.toJson(user);
        map.put("user",userString);

        String requestMatchesFromServerString = gson.toJson(requestMatchesFromServer);
        map.put("requestMatchesFromServer",requestMatchesFromServerString);


        return functions.getHttpsCallable("fetchMatchesPaginatedByRadiusAndDate")
                .call(map);
    }

    public void removeListeners() {

        if (yourMatchesListenerList.size() != 0) {

            for (String sport:SportConstants.SPORTS_MAP.keySet()) {
                ListenerRegistration listener =  yourMatchesListenerList.get(sport);
                if (listener == null)
                    continue;

                listener.remove();
                yourMatchesListenerList.remove(sport);
            }
        }

        if (yourMatchesAdminListenerList.size() != 0) {

            for (String sport:SportConstants.SPORTS_MAP.keySet()) {
                ListenerRegistration listener =  yourMatchesAdminListenerList.get(sport);
                if (listener == null)
                    continue;

                listener.remove();
                yourMatchesAdminListenerList.remove(sport);
            }

        }

    }


    public Task<HttpsCallableResult> ignoreRequesterIfAdmin(String userToIgnore, String adminId, String matchId, String sport) {
        Map<String,String> map = new HashMap<>();

        map.put("userToIgnore",userToIgnore);
        map.put("adminId",adminId);
        map.put("matchId",matchId);
        map.put("sport",sport);


        return functions.getHttpsCallable("matchRepositoryIgnoreRequesterIfAdmin")
                .call(map);
    }

}
