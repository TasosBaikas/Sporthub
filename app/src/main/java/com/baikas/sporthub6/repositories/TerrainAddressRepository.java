package com.baikas.sporthub6.repositories;

import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.firebase.dao.TerrainAddressFirebaseDao;
import com.baikas.sporthub6.helpers.comparators.TerrainAddressPriorityComparator;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.operations.ListOperations;
import com.baikas.sporthub6.models.Sport;
import com.baikas.sporthub6.models.TerrainAddress;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.functions.HttpsCallableResult;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class TerrainAddressRepository {

    TerrainAddressFirebaseDao terrainAddressFirebaseDao;

    @Inject
    public TerrainAddressRepository(TerrainAddressFirebaseDao terrainAddressFirebaseDao) {
        this.terrainAddressFirebaseDao = terrainAddressFirebaseDao;
    }

    public CompletableFuture<Map<String,List<TerrainAddress>>> getAllSportTerrainAddresses(String userId){
        CompletableFuture<Map<String,List<TerrainAddress>>> completableFuture = new CompletableFuture<>();

        terrainAddressFirebaseDao.getTerrainAddresses(userId)
                .addOnSuccessListener((DocumentSnapshot docSnap) -> {

                    if (docSnap == null || !docSnap.exists()){
                        completableFuture.complete(null);
                        return;
                    }

                    Map<String, Object> rawData = docSnap.getData();

                    Map<String, List<TerrainAddress>> finalMap = unwrapMap(rawData);

                    SportConstants.SPORTS_MAP.values()
                                    .forEach((sport)->finalMap.computeIfAbsent(sport.getEnglishName(), k -> new ArrayList<>()));


                    completableFuture.complete(finalMap);
                }).addOnFailureListener(e -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }

    public CompletableFuture<List<TerrainAddress>> getSportTerrainAddresses(String userId,@NotNull String sport){
        CompletableFuture<List<TerrainAddress>> completableFuture = new CompletableFuture<>();

        terrainAddressFirebaseDao.getTerrainAddresses(userId)
                .addOnSuccessListener((DocumentSnapshot docSnap) -> {

                    if (docSnap == null || !docSnap.exists()){
                        completableFuture.complete(new ArrayList<>());
                        return;
                    }

                    Map<String, Object> rawData = docSnap.getData();

                    Map<String, List<TerrainAddress>> finalMap = unwrapMap(rawData);

                    finalMap.computeIfAbsent(sport, k -> new ArrayList<>());

                    List<TerrainAddress> terrainAddressList = finalMap.get(sport);

                    terrainAddressList.sort(new TerrainAddressPriorityComparator());

                    completableFuture.complete(terrainAddressList);
                }).addOnFailureListener((e) -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }

    public CompletableFuture<Void> saveTerrainAddress(@NotNull TerrainAddress terrainAddress){
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        String userId = terrainAddress.getCreatorId();

        terrainAddressFirebaseDao.getTerrainAddresses(userId)
                .addOnSuccessListener((DocumentSnapshot docSnap) -> {

                    if (docSnap == null || !docSnap.exists()){

                        Map<String,List<TerrainAddress>> addressesMap = new HashMap<>();

                        for (Sport sport: SportConstants.SPORTS_MAP.values()) {
                            addressesMap.put(sport.getEnglishName(),new ArrayList<>());
                        }

                        terrainAddress.setPriority(0);
                        addressesMap.get(terrainAddress.getSport()).add(terrainAddress);

                        terrainAddressFirebaseDao.updateTerrainAddresses(addressesMap,userId)
                                .addOnSuccessListener((HttpsCallableResult unused) -> completableFuture.complete(null))
                                .addOnFailureListener((e) -> completableFuture.completeExceptionally(e));
                        return;
                    }

                    Map<String, Object> rawData = docSnap.getData();

                    Map<String, List<TerrainAddress>> finalMap = unwrapMap(rawData);

                    finalMap.computeIfAbsent(terrainAddress.getSport(), k -> new ArrayList<>());

                    boolean titleAlreadyExists = finalMap.get(terrainAddress.getSport())
                            .stream()
                            .anyMatch(terrain -> terrain.getAddressTitle().equals(terrainAddress.getAddressTitle()));

                    if (titleAlreadyExists) {
                        completableFuture.completeExceptionally(new ValidationException("Αυτό το όνομα υπάρχει ήδη"));
                        return;
                    }

                    List<TerrainAddress> addressesForThatSport = finalMap.get(terrainAddress.getSport());
                    terrainAddress.setPriority(addressesForThatSport.size());

                    addressesForThatSport.add(terrainAddress);

                    terrainAddressFirebaseDao.updateTerrainAddresses(finalMap,userId)
                            .addOnSuccessListener((HttpsCallableResult unused) -> completableFuture.complete(null))
                            .addOnFailureListener((e) -> completableFuture.completeExceptionally(e));

                }).addOnFailureListener((e) -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }

    public CompletableFuture<Void> updateTerrainTitle(@NotNull TerrainAddress terrainAddress){
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        String userId = terrainAddress.getCreatorId();

        terrainAddressFirebaseDao.getTerrainAddresses(userId)
                .addOnSuccessListener((DocumentSnapshot docSnap) -> {

                    if (docSnap == null || !docSnap.exists()){
                        completableFuture.completeExceptionally(new RuntimeException("Κάτι πήγε στραβά"));
                        return;
                    }

                    Map<String, Object> rawData = docSnap.getData();

                    Map<String, List<TerrainAddress>> finalMap = unwrapMap(rawData);

                    if (finalMap.get(terrainAddress.getSport()) == null){
                        completableFuture.completeExceptionally(new NoSuchElementException("Δεν υπάρχει η διεύθυνση"));
                        return;
                    }

                    boolean titleAlreadyExists = finalMap.get(terrainAddress.getSport())
                            .stream()
                            .anyMatch(terrain -> terrain.getAddressTitle().equals(terrainAddress.getAddressTitle()));

                    if (titleAlreadyExists) {
                        completableFuture.completeExceptionally(new ValidationException("Αυτό το όνομα υπάρχει ήδη"));
                        return;
                    }

                    List<TerrainAddress> listOfAddresses = finalMap.get(terrainAddress.getSport());
                    ListOperations.setElementToList(terrainAddress,listOfAddresses);

                    terrainAddressFirebaseDao.updateTerrainAddresses(finalMap,userId)
                            .addOnSuccessListener((HttpsCallableResult unused) -> completableFuture.complete(null))
                            .addOnFailureListener((e) -> completableFuture.completeExceptionally(e));

                }).addOnFailureListener(completableFuture::completeExceptionally);

        return completableFuture;
    }

    public CompletableFuture<Void> deleteTerrainAddressById(@NotNull String terrainAddressId,String sport, String userId){
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        terrainAddressFirebaseDao.getTerrainAddresses(userId)
                .addOnSuccessListener((DocumentSnapshot docSnap) -> {

                    if (docSnap == null || !docSnap.exists()){
                        completableFuture.completeExceptionally(new RuntimeException("Κάτι πήγε στραβά"));
                        return;
                    }

                    Map<String, Object> rawData = docSnap.getData();

                    Map<String, List<TerrainAddress>> finalMap = unwrapMap(rawData);

                    List<TerrainAddress> listOfAddresses = finalMap.get(sport);
                    if (listOfAddresses == null){
                        completableFuture.completeExceptionally(new NoSuchElementException("Δεν υπάρχει η διεύθυνση"));
                        return;
                    }

                    Optional<TerrainAddress> terrainAddressOptional = listOfAddresses.stream()
                            .filter((TerrainAddress terrainAddress) -> terrainAddress.getId().equals(terrainAddressId))
                            .findFirst();

                    if (!terrainAddressOptional.isPresent()){
                        completableFuture.completeExceptionally(new NoSuchElementException("Δεν υπάρχει η διεύθυνση"));
                        return;
                    }

                    long basePriority = terrainAddressOptional.get().getPriority();
                    for (TerrainAddress terrain:listOfAddresses) {
                        if (terrain.getPriority() <= basePriority)
                            continue;

                        terrain.setPriority(terrain.getPriority() - 1);
                    }

                    ListOperations.removeElementById(terrainAddressId,listOfAddresses);

                    terrainAddressFirebaseDao.updateTerrainAddresses(finalMap,userId)
                            .addOnSuccessListener((HttpsCallableResult unused) -> completableFuture.complete(null))
                            .addOnFailureListener((e) -> completableFuture.completeExceptionally(e));

                }).addOnFailureListener(completableFuture::completeExceptionally);

        return completableFuture;
    }

    private Map<String, List<TerrainAddress>> unwrapMap(Map<String, Object> rawData) {
        Map<String, List<TerrainAddress>> finalMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : rawData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                List<TerrainAddress> addressList = new ArrayList<>();

                for (Object item : list) {
                    TerrainAddress address = new TerrainAddress((Map<String, Object>) item);
                    addressList.add(address);
                }

                finalMap.put(key, addressList);
            }
        }
        return finalMap;
    }

}
