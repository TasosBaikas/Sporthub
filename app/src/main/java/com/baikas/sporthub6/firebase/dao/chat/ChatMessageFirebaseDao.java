package com.baikas.sporthub6.firebase.dao.chat;

import com.baikas.sporthub6.helpers.comparators.ChatMessagesComparator;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.FirestoreCallbackDocumentChanges;
import com.baikas.sporthub6.models.chat.ChatMessage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class ChatMessageFirebaseDao {

    private final FirebaseFirestore firestore;
    private ListenerRegistration onMessagesListener;
    private final FirebaseFunctions functions;


    @Inject
    public ChatMessageFirebaseDao(FirebaseFirestore firestore, FirebaseFunctions functions) {
        this.firestore = firestore;
        this.functions = functions;
    }

    public Task<HttpsCallableResult> saveChatMessage(ChatMessage chatMessage) {

        Map<String,Object> map = new HashMap<>();


        Gson gson = new Gson();
        String jsonStringChatMessage = gson.toJson(chatMessage);
        map.put("chatMessage",jsonStringChatMessage);


        return functions.getHttpsCallable("chatMessageRepositorySaveChatMessage")
                .call(map);
    }


    public Task<HttpsCallableResult> changeSeenByUser(ChatMessage chatMessage, String yourId) {

        Map<String,Object> map = new HashMap<>();

        Gson gson = new Gson();
        String jsonStringChatMessage = gson.toJson(chatMessage);
        map.put("chatMessage",jsonStringChatMessage);
        map.put("yourId",yourId);


        return functions.getHttpsCallable("chatMessageRepositoryChangeSeenBy")
                .call(map);
    }

    public Task<HttpsCallableResult> deleteChatMessage(ChatMessage chatMessage, String yourId) {

        Map<String,Object> map = new HashMap<>();

        Gson gson = new Gson();
        String jsonStringChatMessage = gson.toJson(chatMessage);
        map.put("chatMessage",jsonStringChatMessage);
        map.put("yourId",yourId);


        return functions.getHttpsCallable("chatMessageRepositoryVirtualDeleteMessage")
                .call(map);
    }

    public Task<HttpsCallableResult> updateEmojiCount(ChatMessage chatMessage, String emojiClicked, String yourId) {

        Map<String,Object> map = new HashMap<>();


        Gson gson = new Gson();
        String jsonStringChatMessage = gson.toJson(chatMessage);
        map.put("chatMessage",jsonStringChatMessage);
        map.put("emojiClicked",emojiClicked);
        map.put("yourId",yourId);


        return functions.getHttpsCallable("chatMessageRepositoryUpdateEmojiCount")
                .call(map);
    }

    public Task<QuerySnapshot> getChatMessageBatchPaginated(String chatId, @NotNull Long lastVisibleDocumentCreatedAtUTC){
        CollectionReference chatMessageBatchRef = firestore.collection("chatMessageBatch").document(chatId).collection("chatMessageBatchesSpecificChat");

        return chatMessageBatchRef
                .orderBy("createdAtUTC", Query.Direction.DESCENDING)
                .whereLessThan("createdAtUTC", lastVisibleDocumentCreatedAtUTC)
                .limit(1)
                .get();

    }

    public Task<HttpsCallableResult> getPinnedMessage(String chatId, String yourId) {

        Map<String,Object> map = new HashMap<>();

        map.put("chatId",chatId);
        map.put("yourId",yourId);


        return functions.getHttpsCallable("chatMessageRepositoryGetPinnedMessage")
                .call(map);

    }


    public void initOnMessagesListener(String chatId, FirestoreCallbackDocumentChanges<ChatMessage> firestoreCallbackChatMessages){
        if (onMessagesListener != null)
            return;

        CollectionReference chatMessageBatchRef = firestore.collection("chatMessageBatch").document(chatId).collection("chatMessageBatchesSpecificChat");

        onMessagesListener = chatMessageBatchRef
                .whereGreaterThan("lastModifiedTimeInUTC", TimeFromInternet.getInternetTimeEpochUTC())
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        firestoreCallbackChatMessages.onError(error);
                        return;
                    }


                    List<ChatMessage> changedDocuments = new ArrayList<>();
                    for (DocumentChange docChange : value.getDocumentChanges()) {

                        List<Map<String,Object>> chatMessagesList = (List<Map<String,Object>>) docChange.getDocument().getData().get("chatMessages");

                        List<ChatMessage> chatMessages = chatMessagesList.stream()
                                                                .map((message) -> new ChatMessage(message))
                                                                .collect(Collectors.toList());

                        changedDocuments.addAll(chatMessages);
                    }

                    changedDocuments.sort(new ChatMessagesComparator());

                    firestoreCallbackChatMessages.onSuccess(changedDocuments);
                });
    }

    public void removeListeners() {
        if (onMessagesListener == null)
            return;

        onMessagesListener.remove();
        onMessagesListener = null;
    }



}
