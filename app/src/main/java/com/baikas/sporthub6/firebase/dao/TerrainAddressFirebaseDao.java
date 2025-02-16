package com.baikas.sporthub6.firebase.dao;

import com.baikas.sporthub6.models.TerrainAddress;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class TerrainAddressFirebaseDao {
    private final FirebaseFirestore firestore;
    private final String mainCollection = "userSavedTerrainAddresses";

    private final FirebaseFunctions functions;


    @Inject
    public TerrainAddressFirebaseDao(FirebaseFirestore firestore,FirebaseFunctions functions) {
        this.firestore = firestore;
        this.functions = functions;

    }

    public Task<DocumentSnapshot> getTerrainAddresses(String userId){
        DocumentReference docRef = firestore.collection(mainCollection).document(userId);

        return docRef.get();
    }

    public Task<HttpsCallableResult> updateTerrainAddresses(Map<String, List<TerrainAddress>> terrainAddressesMap, String yourId){

        Map<String,Object> map = new HashMap<>();

        map.put("yourId",yourId);

        Gson gson = new Gson();
        String jsonStringTerrainAddressesMap = gson.toJson(terrainAddressesMap);
        map.put("terrainAddressesMap",jsonStringTerrainAddressesMap);


        return functions.getHttpsCallable("terrainRepositoryUpdateTerrain")
                .call(map);
    }

}
