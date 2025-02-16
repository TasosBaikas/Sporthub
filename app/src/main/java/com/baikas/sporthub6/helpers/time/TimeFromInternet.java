package com.baikas.sporthub6.helpers.time;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.baikas.sporthub6.exceptions.NoInternetConnectionException;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.hitl.container.FirebaseCloudFunctionsHilt;
import com.baikas.sporthub6.interfaces.chat.TimeFetchCallback;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.net.InetAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class TimeFromInternet {

    private static long startTime = 0;
    private static long startTimeOfDeviceBootLoad = 0;
    private static boolean timeFromInternet = false;
    private static final Object LOCK = new Object();


    public static void initInternetEpoch(@Nullable Context context, @Nullable TimeFetchCallback timeFetchCallback){
        if (timeFromInternet)
            return;

        startTimeOfDeviceBootLoad = SystemClock.elapsedRealtime();
        startTime = Instant.now().toEpochMilli();
        if (context != null) {
            if (!CheckInternetConnection.isNetworkConnected(context)) {
                return;
            }
        }

        CompletableFuture.runAsync(()->{
            synchronized (LOCK) {
                if (timeFromInternet)
                    return;

                long epochTime;
                try {
                    epochTime = getNTPTime().get();
                    timeFromInternet = true;
                } catch (Exception e) {
                    try{
                        epochTime = fetchWorldTimeAPI().get();
                        timeFromInternet = true;
                    }catch (Exception e2){
                        try {
                            epochTime = getTimeFromServer().get();
                            timeFromInternet = true;
                        } catch (Exception e3) {
                            Log.e("TimeFromInternet", "all methods failed ... using system time");
                            timeFromInternet = false;
                            epochTime = Instant.now().toEpochMilli();
                        }
                    }
                }

                startTimeOfDeviceBootLoad = SystemClock.elapsedRealtime();
                startTime = epochTime;
                if (timeFetchCallback != null)
                    timeFetchCallback.onTimeFetched(startTime);
            }
        });
    }

    private static CompletableFuture<Long> getTimeFromServer() {
        CompletableFuture<Long> completableFuture = new CompletableFuture<>();

        String serverRegion = FirebaseCloudFunctionsHilt.SERVER_REGION;

        FirebaseFunctions.getInstance(serverRegion).getHttpsCallable("getServerTime").call()
                .addOnSuccessListener((HttpsCallableResult taskResult) -> {
                    if (taskResult.getData() == null) {
                        completableFuture.completeExceptionally(new RuntimeException());
                        return;
                    }

                    Map<String,Long> map =((HashMap<String,Long>) taskResult.getData());

                    completableFuture.complete((Long) map.get("serverTime"));

                }).addOnFailureListener(completableFuture::completeExceptionally);

        return completableFuture;
    }

    public static void initInternetEpoch(@Nullable Context context) {
        initInternetEpoch(context,null);
    }

    public static boolean isTimeFromInternet(){
        return timeFromInternet;
    }


    public static long getInternetTimeEpochUTC(){
        if (!timeFromInternet)
            initInternetEpoch(null);

        return startTime + (SystemClock.elapsedRealtime() - startTimeOfDeviceBootLoad);
    }


    private static CompletableFuture<Long> fetchWorldTimeAPI() {
        CompletableFuture<Long> completableFuture = new CompletableFuture<>();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://worldtimeapi.org/api/ip").build();

        try (Response response = client.newCall(request).execute()) {
            String jsonResponse = response.body().string();
            JSONObject jsonObject = new JSONObject(jsonResponse);

            long result = jsonObject.getLong("unixtime") * 1000;
            completableFuture.complete(result);
        } catch (Exception e) {
            Log.e("TimeFetching", "Error fetching time from WorldTimeAPI", e);
            throw new RuntimeException("FetchWorldTimeApi failed",e);
        }

        return completableFuture;
    }


    private static CompletableFuture<Long> getNTPTime() {
        CompletableFuture<Long> completableFuture = new CompletableFuture<>();
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(4000);

        try {
            client.open();
            InetAddress inetAddress = InetAddress.getByName("pool.ntp.org");
            TimeInfo timeInfo = client.getTime(inetAddress);
            timeInfo.computeDetails();
            long serverTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
            completableFuture.complete(serverTime);
        }catch (Exception e) {
            Log.e("TimeFromInternet", " getNTPTime failed");
            throw new RuntimeException("getNTPTime failed", e);
        }finally {
            client.close();
        }

        return completableFuture;
    }
}
