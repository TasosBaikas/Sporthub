package com.baikas.sporthub6.firebase.dao;

import com.baikas.sporthub6.models.user.UserFcm;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class FcmUserFirebaseDao {

    private final FirebaseFunctions functions;

    @Inject
    public FcmUserFirebaseDao(FirebaseFunctions functions) {
        this.functions = functions;
    }


    public Task<HttpsCallableResult> saveUserFcmToken(UserFcm userFcm) {

        Map<String,String> map = new HashMap<>();

        Gson gson = new Gson();
        String jsonStringUserFcm = gson.toJson(userFcm);
        map.put("userFcm",jsonStringUserFcm);


        return functions.getHttpsCallable("fcmRepositorySaveUserFcmToken")
                .call(map);
    }

    public Task<HttpsCallableResult> deleteFcmTokenOfThisDevice(String yourId, String deviceUUID) {

        Map<String,String> map = new HashMap<>();

        map.put("yourId",yourId);
        map.put("deviceUUID",deviceUUID);


        return functions.getHttpsCallable("fcmRepositoryDeleteUserFcmToken")
                .call(map);
    }
}
