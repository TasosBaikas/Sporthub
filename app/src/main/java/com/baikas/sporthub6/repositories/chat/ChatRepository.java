package com.baikas.sporthub6.repositories.chat;

import com.baikas.sporthub6.exceptions.ValidationException;
import com.baikas.sporthub6.firebase.dao.UserFirebaseDao;
import com.baikas.sporthub6.firebase.dao.chat.ChatFirebaseDao;
import com.baikas.sporthub6.interfaces.FirestoreCallbackDocumentChanges;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.chat.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.HttpsCallableResult;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

public class ChatRepository {

    private final ChatFirebaseDao chatFirebaseDao;


    @Inject
    public ChatRepository(ChatFirebaseDao chatFirebaseDao) {
        this.chatFirebaseDao = chatFirebaseDao;
    }

    public CompletableFuture<Void> saveChatIfPrivateConversation(Chat chat) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        chatFirebaseDao.saveChatIfPrivateConversation(chat)
                .addOnSuccessListener((HttpsCallableResult unused) -> completableFuture.complete(null))
                .addOnFailureListener((e) -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }


    public CompletableFuture<Chat> getChatById(String id) {
        CompletableFuture<Chat> completableFuture = new CompletableFuture<>();

        chatFirebaseDao.getChatById(id)
                .addOnSuccessListener((DocumentSnapshot chatSnapshot) -> {
                    if (chatSnapshot == null || !chatSnapshot.exists()){
                        completableFuture.complete(null);
                        return;
                    }

                    Chat chat = new Chat(chatSnapshot.getData());
                    completableFuture.complete(chat);
                })
                .addOnFailureListener((e) -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }


    public CompletableFuture<Void> addOrRemoveYourPhoneNumberToThatChat(@NotNull String id, @NotNull String chatId, boolean addOrRemove){
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        chatFirebaseDao.addOrRemoveYourPhoneNumberToThatChat(id, chatId, addOrRemove)
                .addOnSuccessListener((HttpsCallableResult unused)-> completableFuture.complete(null))
                .addOnFailureListener(e -> completableFuture.completeExceptionally(e));


        return completableFuture;
    }


    public CompletableFuture<List<Chat>> getChatsPaginated(String userId, String chatPreviewTypes,@NotNull AtomicReference<DocumentSnapshot> lastVisibleDocumentChat,int limit) {
        CompletableFuture<List<Chat>> completableFuture = new CompletableFuture<>();

        chatFirebaseDao.getChatsPaginated(userId,chatPreviewTypes, lastVisibleDocumentChat.get(),limit)
                .addOnSuccessListener((QuerySnapshot queryChats) -> {
                    if (queryChats == null || queryChats.isEmpty()){
                        completableFuture.complete(Collections.emptyList());
                        return;
                    }

                    List<Chat> chatList = new ArrayList<>();
                    for (int i = 0; i < queryChats.getDocuments().size(); i++) {
                        DocumentSnapshot docChat = queryChats.getDocuments().get(i);

                        if (i == queryChats.getDocuments().size() - 1)
                            lastVisibleDocumentChat.set(docChat);

                        chatList.add(new Chat(docChat.getData()));
                    }

                    completableFuture.complete(chatList);
                }).addOnFailureListener(e -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }

    public CompletableFuture<Void> leaveChat(String yourId, String chatId) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        chatFirebaseDao.leaveChatIfMatchConversation(yourId,chatId)
                .addOnSuccessListener((HttpsCallableResult unused)-> completableFuture.complete(null))
                .addOnFailureListener(e -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }


    public CompletableFuture<Void> kickUser(String userToKick, String chatId) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        chatFirebaseDao.kickUserIfMatchConversation(userToKick, chatId)
                .addOnSuccessListener((HttpsCallableResult unused)-> completableFuture.complete(null))
                .addOnFailureListener(e -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }


    public CompletableFuture<Chat> getPrivateChatOrCreate(String fromId, String toId) {
        CompletableFuture<Chat> completableFuture = new CompletableFuture<>();


        chatFirebaseDao.getPrivateChatOrCreate(fromId, toId)
                .addOnSuccessListener((HttpsCallableResult taskResult) -> {
                    if (taskResult.getData() == null) {
                        completableFuture.completeExceptionally(new RuntimeException(""));
                        return;
                    }

                    Chat chat = new Chat((Map<String, Object>) taskResult.getData());

                    completableFuture.complete(chat);
                })
                .addOnFailureListener((e) -> {
                    completableFuture.completeExceptionally(e);
                });

        return completableFuture;
    }

    public CompletableFuture<Void> deleteChatIfMatchConversation(String chatId,String userId) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();


        this.chatFirebaseDao.destroyChatIfMatchConversation(chatId,userId)
                .addOnSuccessListener((HttpsCallableResult result) -> completableFuture.complete(null))
                .addOnFailureListener((e) -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }



    public CompletableFuture<Void> deleteChatIfPrivateConversation(String chatId, String userId) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();


        this.chatFirebaseDao.destroyChatIfPrivateConversation(chatId,userId)
                .addOnSuccessListener((HttpsCallableResult result) -> completableFuture.complete(null))
                .addOnFailureListener((e) -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }

    public CompletableFuture<Void> changeAdmin(String userToGiveAdmin, String chatId) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        String yourId = FirebaseAuth.getInstance().getUid();

        this.chatFirebaseDao.changeAdminIfMatchConversation(userToGiveAdmin,yourId,chatId)
                .addOnSuccessListener((HttpsCallableResult unused) -> completableFuture.complete(null))
                .addOnFailureListener(e -> {

                    try{
                        completableFuture.completeExceptionally(new ValidationException(e.getMessage()));

                    }catch (Exception ignored){}

                    completableFuture.completeExceptionally(new ValidationException("Δοκιμάστε ξανά"));
                });

        return completableFuture;
    }

    public CompletableFuture<Void> updatePinnedMessage(ChatMessage chatMessage) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        String yourId = FirebaseAuth.getInstance().getUid();

        this.chatFirebaseDao.updatePinnedMessage(chatMessage,yourId)
                .addOnSuccessListener((HttpsCallableResult unused) -> completableFuture.complete(null))
                .addOnFailureListener(e -> {

                    try{
                        completableFuture.completeExceptionally(new ValidationException(e.getMessage()));

                    }catch (Exception ignored){}

                    completableFuture.completeExceptionally(new ValidationException("Δοκιμάστε ξανά"));
                });

        return completableFuture;
    }

    public CompletableFuture<Void> deletePinnedMessage(ChatMessage pinnedMessage) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        String yourId = FirebaseAuth.getInstance().getUid();

        this.chatFirebaseDao.deletePinnedMessage(pinnedMessage,yourId)
                .addOnSuccessListener((HttpsCallableResult unused) -> completableFuture.complete(null))
                .addOnFailureListener(e -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }

    public CompletableFuture<Void> blockPlayer(String userIdToBlock) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();


        this.chatFirebaseDao.blockPlayer(userIdToBlock)
                .addOnSuccessListener((HttpsCallableResult unused) -> completableFuture.complete(null))
                .addOnFailureListener(e -> completableFuture.completeExceptionally(e));

        return completableFuture;
    }

    public void initCurrentUsersInMatchListener(String chatId, FirestoreCallbackDocumentChanges<String> firestoreCallbackDocumentChanges) {
        chatFirebaseDao.initCurrentUsersInMatchListener(chatId, firestoreCallbackDocumentChanges);
    }

    public void initMessagesNotSeenByUser(String userId, int limit, FirestoreCallbackDocumentChanges<Chat> chatFirestoreCallbackDocumentChanges) {
        chatFirebaseDao.initMessagesNotSeenByUser(userId, limit, chatFirestoreCallbackDocumentChanges);
    }

    public void removeListeners() {
        chatFirebaseDao.removeListeners();
    }


    public void initListenerForChatUpdates(String userId, String chatType ,FirestoreCallbackDocumentChanges<Chat> firestoreCallbackDocumentChanges) {
        chatFirebaseDao.initListenerForChatUpdates(userId, chatType,firestoreCallbackDocumentChanges);

    }



}
