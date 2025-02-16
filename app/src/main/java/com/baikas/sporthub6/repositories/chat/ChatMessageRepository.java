package com.baikas.sporthub6.repositories.chat;

import com.baikas.sporthub6.exceptions.EmptyStringException;
import com.baikas.sporthub6.exceptions.NoInternetConnectionException;
import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.firebase.dao.chat.ChatFirebaseDao;
import com.baikas.sporthub6.firebase.dao.chat.ChatMessageFirebaseDao;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.FirestoreCallbackDocumentChanges;
import com.baikas.sporthub6.models.chat.ChatMessage;


import com.baikas.sporthub6.models.chat.ChatMessageBatch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.HttpsCallableResult;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

public class ChatMessageRepository {

    private final ChatMessageFirebaseDao chatMessageFirebaseDao;
    private final CheckInternetConnection checkInternetConnection;


    @Inject
    public ChatMessageRepository(ChatMessageFirebaseDao chatMessageFirebaseDao,CheckInternetConnection checkInternetConnection) {
        this.chatMessageFirebaseDao = chatMessageFirebaseDao;
        this.checkInternetConnection = checkInternetConnection;
    }

    public CompletableFuture<ChatMessageBatch> getChatMessagesPaginated(String chatId, @NotNull AtomicReference<Long> lastVisibleDocumentCreatedAtUTC){
        CompletableFuture<ChatMessageBatch> completableFuture = new CompletableFuture<>();

        if (!checkInternetConnection.isNetworkConnected()){
            completableFuture.completeExceptionally(new NoInternetConnectionException("No internet connection"));
            return completableFuture;
        }



        long paginationTime;
        if (lastVisibleDocumentCreatedAtUTC.get() == null) {
            paginationTime = TimeFromInternet.getInternetTimeEpochUTC();
        }else
            paginationTime = lastVisibleDocumentCreatedAtUTC.get();


        chatMessageFirebaseDao.getChatMessageBatchPaginated(chatId, paginationTime)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        completableFuture.complete(null);
                        return;
                    }

                    ChatMessageBatch chatMessageBatch = new ChatMessageBatch(queryDocumentSnapshots.getDocuments().get(0).getData());
                    List<ChatMessage> chatMessages = chatMessageBatch.getChatMessages();
                    lastVisibleDocumentCreatedAtUTC.set(chatMessages.get(chatMessages.size()-1).getCreatedAtUTC());

                    completableFuture.complete(chatMessageBatch);
                })
                .addOnFailureListener(e -> completableFuture.completeExceptionally(e));


        return completableFuture;

    }

    public CompletableFuture<ChatMessage> getChatPinnedMessage(String chatId) {
        CompletableFuture<ChatMessage> completableFuture = new CompletableFuture<>();

        String yourId = FirebaseAuth.getInstance().getUid();

        this.chatMessageFirebaseDao.getPinnedMessage(chatId, yourId)
                .addOnSuccessListener((HttpsCallableResult result) -> {

                    if (result == null || result.getData() == null) {
                        completableFuture.complete(null);
                        return;
                    }

                    ChatMessage chatMessageFromDb = new ChatMessage((Map<String, Object>) result.getData());

                    completableFuture.complete(chatMessageFromDb);
                })
                .addOnFailureListener(e -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }


    public CompletableFuture<Void> saveChatMessage(ChatMessage chatMessage){
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        if (chatMessage.getMessage() == null || chatMessage.getMessage().trim().equals("")) {
            completableFuture.completeExceptionally(new EmptyStringException());
            return completableFuture;
        }

        if (!checkInternetConnection.isNetworkConnected()){
            completableFuture.completeExceptionally(new NoInternetConnectionException("No internet connection!"));
            return completableFuture;
        }

        this.chatMessageFirebaseDao.saveChatMessage(chatMessage)
                .addOnSuccessListener((HttpsCallableResult unused)-> completableFuture.complete(null))
                .addOnFailureListener(e -> completableFuture.completeExceptionally(e));


        return completableFuture;
    }

    Set<String> hasSeenTheMessage = new HashSet<>();
    public CompletableFuture<Void> changeSeenByUser(ChatMessage chatMessage,String playerId){
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        if (chatMessage.getSeenByUsersId().contains(playerId) || hasSeenTheMessage.contains(chatMessage.getId())) {
            completableFuture.complete(null);
            return completableFuture;
        }

        hasSeenTheMessage.add(chatMessage.getId());

        chatMessageFirebaseDao.changeSeenByUser(chatMessage, playerId)
                .addOnSuccessListener((HttpsCallableResult unused)-> completableFuture.complete(null))
                .addOnFailureListener(e -> {
                    hasSeenTheMessage.remove(chatMessage.getId());

                    completableFuture.completeExceptionally(e);
                });

        return completableFuture;
    }


    public CompletableFuture<Void> updateEmojiCount(ChatMessage chatMessage, String emojiClicked, String userId) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        this.chatMessageFirebaseDao.updateEmojiCount(chatMessage,emojiClicked,userId)
                .addOnSuccessListener((HttpsCallableResult unused)-> completableFuture.complete(null))
                .addOnFailureListener(e -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }

    public CompletableFuture<ChatMessage> deleteChatMessage(ChatMessage chatMessage) {
        CompletableFuture<ChatMessage> completableFuture = new CompletableFuture<>();

        String yourId = FirebaseAuth.getInstance().getUid();

        this.chatMessageFirebaseDao.deleteChatMessage(chatMessage,yourId)
                .addOnSuccessListener((HttpsCallableResult result)-> {

                    if (result == null || result.getData() == null) {
                        completableFuture.complete(null);
                        return;
                    }

                    ChatMessage virtualDeletedChatMessage = new ChatMessage((Map<String, Object>) result.getData());

                    completableFuture.complete(virtualDeletedChatMessage);
                })
                .addOnFailureListener(e -> completableFuture.completeExceptionally(e));


        return completableFuture;
    }

    public void initOnMessagesListener(String chatId, FirestoreCallbackDocumentChanges<ChatMessage> firestoreCallbackChatMessages){
        chatMessageFirebaseDao.initOnMessagesListener(chatId, firestoreCallbackChatMessages);
    }


    public void removeListeners() {
        chatMessageFirebaseDao.removeListeners();
    }


}
