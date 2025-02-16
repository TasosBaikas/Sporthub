package com.baikas.sporthub6.helpers.operations;

import com.baikas.sporthub6.interfaces.ObjectWithId;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ListOperations{

    public static <T extends ObjectWithId> void combineListsAndAfterSort(List<T> fromList, List<T> toList, Comparator<T> comparator) {
        for (T fromItem : fromList) {
            boolean found = false;
            for (int i = 0; i < toList.size(); i++) {
                if (toList.get(i) == null) continue;

                // This assumes T has a method called getId().
                // You might need to adjust this part based on the actual implementation of T.
                if (fromItem.getId().equals(toList.get(i).getId())) {
                    toList.set(i, fromItem);
                    found = true;
                    break;
                }
            }

            if (!found) {
                toList.add(0, fromItem);
            }
        }

        toList.sort(comparator);
    }

    public static <T extends ObjectWithId> void updateOrAddAndSort(T newMatch, List<T> currentList, Comparator<T> comparator) {
        boolean isMatchFound = false;

        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).getId().equals(newMatch.getId())) {  // Assuming Match has a getId() method
                currentList.set(i, newMatch);  // Replace with the newMatch
                isMatchFound = true;
                break;
            }
        }

        if (!isMatchFound) {
            currentList.add(newMatch);
        }

        currentList.sort(comparator);
    }

    public static <T extends ObjectWithId> void updateOrAdd(T newMatch, List<T> currentList) {
        boolean isMatchFound = false;

        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).getId().equals(newMatch.getId())) {  // Assuming Match has a getId() method
                currentList.set(i, newMatch);  // Replace with the newMatch
                isMatchFound = true;
                break;
            }
        }

        if (!isMatchFound) {
            currentList.add(newMatch);
        }
    }

    public static <T extends ObjectWithId> void setElementToList(T fakeRef,List<T> fakeRefList) {

        // Find an existing match with the same ID as fakeRefMatch
        Optional<T> existingMatch = fakeRefList.stream()
                .filter(m -> m.getId().equals(fakeRef.getId()))
                .findFirst();

        if (existingMatch.isPresent()) {
            // Update existing match
            int index = fakeRefList.indexOf(existingMatch.get());
            fakeRefList.set(index, fakeRef);
        }
    }

    public static <T> List<T> mapToList(Map<String, T> userMap) {
        List<T> list = new ArrayList<>();

        for (Map.Entry<String, T> entry : userMap.entrySet()) {
            T user = entry.getValue();
            list.add(user);
        }

        return list;
    }

    public static <T extends ObjectWithId> void addOnlyToListAndSort(List<T> fromList, List<T> toList, Comparator<T> comparator) {
        // Create a set of the IDs already present in the toList.
        Set<String> existingIds = toList.stream()
                .filter(Objects::nonNull)
                .map(T::getId)
                .collect(Collectors.toSet());

        // Filter out the messages from the fromList that have an ID not already present in toList.
        List<T> newMessages = fromList.stream()
                .filter(msg -> !existingIds.contains(msg.getId()))
                .collect(Collectors.toList());

        // Add all new messages to the end of toList.
        toList.addAll(newMessages);

        // Sort the list
        toList.sort(comparator);
    }

    public static <T extends ObjectWithId> void removeElementById(String terrainAddressId, List<T> listOfAddresses) {
        if (terrainAddressId == null || listOfAddresses == null) {
            return;
        }

        Iterator<T> iterator = listOfAddresses.iterator();
        while (iterator.hasNext()) {
            T item = iterator.next();
            if (terrainAddressId.equals(item.getId())) {
                iterator.remove();
                break; // Remove this line if you want to remove all instances with the given ID
            }
        }
    }

}
