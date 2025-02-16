package com.baikas.sporthub6.repositories;

import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.util.Log;

import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.firebase.dao.UserFirebaseDao;
import com.baikas.sporthub6.helpers.images.ImageTransformations;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;
import com.baikas.sporthub6.models.user.usernotifications.UserNotifications;
import com.baikas.sporthub6.sqlite.dao.UserDao;
import com.baikas.sporthub6.sqlite.dao.UserLevelDao;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.functions.HttpsCallableResult;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

public class UserRepository {

    private final UserFirebaseDao userFirebaseDao;
    private final UserDao userDao;
    private final UserLevelDao userLevelDao;
    private final CheckInternetConnection checkInternetConnection;


    @Inject
    public UserRepository(UserDao userDao, UserLevelDao userLevelDao, UserFirebaseDao userFirebaseDao, CheckInternetConnection checkInternetConnection) {
        this.userDao = userDao;
        this.userLevelDao = userLevelDao;
        this.userFirebaseDao = userFirebaseDao;
        this.checkInternetConnection = checkInternetConnection;
    }

    public CompletableFuture<User> getUserById(String userId) {

        Random random = new Random();
        int randomInt = random.nextInt(10);


        return CompletableFuture.supplyAsync(() -> userDao.getUserDetails(userId))
                .thenCompose(userDetails -> {

                    if (randomInt == 3 && checkInternetConnection.isNetworkConnected())
                        return this.getUserFromFirebase(userId);


                    if (userDetails != null) {

                        User user = new User(userDetails);
                        return CompletableFuture.completedFuture(user);
                    }


                    return this.getUserFromFirebase(userId);
                });
    }

    public CompletableFuture<User> getUserFromFirebase(String userId){

        CompletableFuture<User> completableFuture = new CompletableFuture<>();
        userFirebaseDao.getUserById(userId)
                .addOnSuccessListener(httpsResult -> {

                    Map<String,Object> userData = (Map<String, Object>) httpsResult.getData();

                    if (userData == null){
                        completableFuture.complete(null);//this line doesn't have to wait this.updateUser()
                        return;
                    }

                    User firebaseUser = new User(userData);

                    if (userId.equals(FirebaseAuth.getInstance().getUid())) {
                        try {
                            this.saveUserInSqlite(firebaseUser);
                        }catch (SQLiteException e){
                            this.updateUser(firebaseUser);
                        }
                    }
                    completableFuture.complete(firebaseUser);//this line doesn't have to wait this.updateUser()
                })
                .addOnFailureListener(e -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }



    private CompletableFuture<Void> saveUserInSqlite(User firebaseUser) {

        if (!firebaseUser.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            completableFuture.completeExceptionally(new IllegalStateException());
            return completableFuture;
        }


        return CompletableFuture.supplyAsync(() ->{

            userDao.insert(firebaseUser);

            for (UserLevelBasedOnSport userLevelBasedOnSport:firebaseUser.getUserLevels().values())
                userLevelDao.insert(userLevelBasedOnSport);//todo

            return null;
        });
    }

    public CompletableFuture<Void> saveUser(@NotNull User user, @Nullable UserNotifications userNotifications) {

        if (!user.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            completableFuture.completeExceptionally(new IllegalStateException());
            return completableFuture;
        }


        return CompletableFuture.supplyAsync(() -> {
                CompletableFuture<Void> completableFuture = new CompletableFuture<>();

                if (userNotifications == null){
                    completableFuture.complete(null);
                    return completableFuture;
                }

                userFirebaseDao.saveUser(user,userNotifications)
                        .addOnSuccessListener((unused2) -> completableFuture.complete(null))
                        .addOnFailureListener((e)-> completableFuture.completeExceptionally(e));

                try {
                    completableFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                return completableFuture;
            })
            .thenAcceptAsync(aVoid -> {
                userDao.insert(user);

                for (UserLevelBasedOnSport userLevelBasedOnSport:user.getUserLevels().values())
                    userLevelDao.insert(userLevelBasedOnSport);//todo

            });
    }

    public CompletableFuture<Void> saveUserAndProfileImage(@NotNull User user, @NotNull UserNotifications userNotifications, @NotNull Bitmap profileImageBitmap) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        if (!user.getId().equals(FirebaseAuth.getInstance().getUid())) {
            completableFuture.completeExceptionally(new IllegalStateException());
            return completableFuture;
        }

        User userTemp = new User(user);

        String reference = userTemp.getUserId() + "_" + UUID.randomUUID();

        byte[] imageInBytes = ImageTransformations.getByteArrayFromBitmap(profileImageBitmap,80);

        userFirebaseDao.saveUserProfileImage(imageInBytes,reference)
                .addOnSuccessListener((newUri)->{

                    userTemp.setProfileImageUrl(newUri.toString());
                    userTemp.setProfileImagePath(reference);

                    this.saveUser(userTemp, userNotifications)
                            .thenAccept((throwable -> {

                                completableFuture.complete(null);
                            })).exceptionally((throwable -> {
                                completableFuture.completeExceptionally(new RuntimeException("Error:Η φωτογραφία προφίλ δεν αποθηκεύτηκε!"));
                                return null;
                            }));
                }).addOnFailureListener((e) -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }

    public CompletableFuture<Void> saveUserProfileImage(@NotNull User user, @NotNull Bitmap profileImageBitmap) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        if (!user.getId().equals(FirebaseAuth.getInstance().getUid())) {
            completableFuture.completeExceptionally(new IllegalStateException());
            return completableFuture;
        }

        String reference = user.getUserId() + "_" + UUID.randomUUID();

        byte[] imageInBytes = ImageTransformations.getByteArrayFromBitmap(profileImageBitmap,80);

        userFirebaseDao.saveUserProfileImage(imageInBytes,reference)
                .addOnSuccessListener((newUri)->{

                    String previousUrl = user.getProfileImageUrl();
                    String previousImagePath = user.getProfileImagePath();

                    user.setProfileImageUrl(newUri.toString());
                    user.setProfileImagePath(reference);

                    this.updateUser(user)
                            .thenAcceptAsync((throwable -> {

                                if (!previousUrl.isEmpty())//delete the previousImage
                                    userFirebaseDao.deleteProfileImageByImagePath(previousImagePath);

                                completableFuture.complete(null);
                            })).exceptionally((throwable -> {
                                user.setProfileImageUrl(previousUrl);
                                user.setProfileImagePath(previousImagePath);
                                this.updateUser(user);

                                completableFuture.completeExceptionally(new RuntimeException("Error:Η φωτογραφία προφίλ δεν αποθηκεύτηκε!"));
                                return null;
                            }));
                }).addOnFailureListener((e) -> completableFuture.completeExceptionally(new RuntimeException("Error:Η φωτογραφία προφίλ δεν αποθηκεύτηκε!")));

        return completableFuture;
    }

    public CompletableFuture<Void> deleteUserProfileImage(String profileImagePath, User user){
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        if (!user.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
            completableFuture.completeExceptionally(new IllegalStateException());
            return completableFuture;
        }

        userFirebaseDao.deleteProfileImageByImagePath(profileImagePath)
                .addOnSuccessListener((unused)->{

                    user.setProfileImageUrl("");
                    user.setProfileImagePath("");
                    this.updateUser(user);

                    completableFuture.complete(null);
                })
                .addOnFailureListener(e -> completableFuture.completeExceptionally(e));


        return completableFuture;
    }

    public CompletableFuture<Void> updateUser(User user) {

        if (!user.getId().equals(FirebaseAuth.getInstance().getUid())) {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            completableFuture.completeExceptionally(new IllegalStateException());
            return completableFuture;
        }

        return CompletableFuture.supplyAsync(() -> {
                    CompletableFuture<Void> completableFuture = new CompletableFuture<>();

                    userFirebaseDao.updateUser(user)
                            .addOnSuccessListener((HttpsCallableResult unused) -> completableFuture.complete(null))
                            .addOnFailureListener(e -> completableFuture.completeExceptionally(new Exception("Δεν έγινε αλλαγή")));

                    // Wait for the CompletableFuture to be completed
                    try {
                        completableFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                })
                .thenRun(() -> {
                    userDao.update(user);
                    for (UserLevelBasedOnSport userLevel : user.getUserLevels().values()) {
                        userLevelDao.update(userLevel);
                    }
                });

    }

    public CompletableFuture<User> getUserByIdWithPhoneIfEnabled(@NotNull String id, @NotNull String chatId){
        CompletableFuture<User> completableFuture = new CompletableFuture<>();

        userFirebaseDao.getUserByIdWithPhoneIfEnabled(id, chatId)
                .addOnSuccessListener((HttpsCallableResult result) -> {

                    Map<String,Object> userData = (Map<String, Object>) result.getData();

                    if (userData == null){
                        completableFuture.complete(null);//this line doesn't have to wait this.updateUser()
                        return;
                    }

                    User userWithPhone = new User(userData);

                    completableFuture.complete(userWithPhone);//this line doesn't have to wait this.updateUser()
                }).addOnFailureListener((e) -> completableFuture.completeExceptionally(e));



        return completableFuture;
    }


    public CompletableFuture<Map<String, User>> getUsersByIdWithPhoneIfEnabled(@NotNull List<String> idList, String chatId){

        CompletableFuture<Map<String, User>> completableFuture = new CompletableFuture<>();
        if (idList.isEmpty())
            return CompletableFuture.completedFuture(new HashMap<>());


        List<Task<HttpsCallableResult>> tasks = new ArrayList<>();
        for (String userId: idList)
            tasks.add(userFirebaseDao.getUserByIdWithPhoneIfEnabled(userId, chatId));

        Task<List<Object>> allTasks = Tasks.whenAllSuccess(tasks);

        allTasks.addOnSuccessListener(results -> {

            Map<String, User> userMap = new HashMap<>();
            for (Object result : results) {
                // Each result is an HttpsCallableResult
                HttpsCallableResult httpsResult = (HttpsCallableResult) result;

                if (httpsResult == null || httpsResult.getData() == null)
                    continue;

                User userWithPhone = new User((Map<String, Object>) httpsResult.getData());

                userMap.put(userWithPhone.getUserId(), userWithPhone);
            }

            completableFuture.complete(userMap);
        }).addOnFailureListener(e -> completableFuture.completeExceptionally(e));


        return completableFuture;
    }



    public CompletableFuture<Map<String,User>> getUsersByIds(@NotNull List<String> idList){

        CompletableFuture<Map<String,User>> completableFuture = new CompletableFuture<>();
        if (idList.isEmpty())
            return CompletableFuture.completedFuture(new HashMap<>());


        List<Task<HttpsCallableResult>> tasks = new ArrayList<>();
        for (String userId: idList)
            tasks.add(userFirebaseDao.getUserById(userId));

        Task<List<Object>> allTasks = Tasks.whenAllSuccess(tasks);


        allTasks.addOnSuccessListener(results -> {

            Map<String, User> userMap = new HashMap<>();
            for (Object result : results) {
                // Each result is an HttpsCallableResult
                HttpsCallableResult httpsResult = (HttpsCallableResult) result;

                if (httpsResult == null || httpsResult.getData() == null)
                    continue;


                User user = new User((Map<String, Object>) httpsResult.getData());

                userMap.put(user.getUserId(),user);
            }

            completableFuture.complete(userMap);
        }).addOnFailureListener(e -> completableFuture.completeExceptionally(e));


        return completableFuture;
    }

    public CompletableFuture<Void> deleteAccount(String uid) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        userFirebaseDao.deleteAccount(uid)
                .addOnSuccessListener((unused2) -> completableFuture.complete(null))
                .addOnFailureListener((e)->completableFuture.completeExceptionally(new RuntimeException("Δεν έγινε πλήρης διαγραφή λογαριασμού!!")));


        return completableFuture;
    }


//    public CompletableFuture<Void> updateLevel(User user, String sport,int levelSelected,int previousLevel) {
//
//        UserLevelBasedOnSport userLevel = user.getUserLevels().get(sport);//todo
//        userLevel.setLevel(levelSelected);
//
//        if (!user.getId().equals(FirebaseAuth.getInstance().getUid())) {
//            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
//            completableFuture.completeExceptionally(new IllegalStateException());
//            return completableFuture;
//        }
//
//        return CompletableFuture.runAsync(()->userLevelDao.update(userLevel))
//                .thenCompose((unused -> {
//
//                CompletableFuture<Void> completableFuture = new CompletableFuture<>();
//
//                userFirebaseDao.updateLevel(userLevel)
//                        .addOnSuccessListener((unused1)->completableFuture.complete(unused1))
//                        .addOnFailureListener(e -> {
//
//                            new Thread(()->{
//
//                                userLevel.setLevel(previousLevel);
//                                rollBackUserLevelSQLite(userLevel);
//
//                            }).start();
//
//                            Log.e("UserRepository updateLevel","Firebase error" + e ,e);
//                            completableFuture.completeExceptionally(new RuntimeException("Firebase error"));
//                        });
//
//                return completableFuture;
//            }));
//
//    }

    public void deleteUserFromSqlite(String userId) {

        CompletableFuture.runAsync(()->{
            userDao.deleteById(userId);
            userLevelDao.deleteById(userId);
        });
    }

    public void rollBackUserLevelSQLite(UserLevelBasedOnSport userLevel){
        try {
            userLevelDao.update(userLevel);
        }catch (Exception e){Log.e("UserRepository RollBack","RollbackUserLevelSQLite failed!!" + e,e);}
    }



}
