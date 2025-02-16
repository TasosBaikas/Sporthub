package com.baikas.sporthub6.firebase.dao.chat;

import com.baikas.sporthub6.helpers.comparators.ChatComparator;
import com.baikas.sporthub6.models.constants.ChatPreviewTypesConstants;
import com.baikas.sporthub6.models.constants.ChatTypesConstants;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.FirestoreCallbackDocumentChanges;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.chat.ChatMessage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class ChatFirebaseDao {

    private final FirebaseFirestore firestore;
    private ListenerRegistration listenerForChatUpdates;

    private ListenerRegistration listenerCurrentUsersInMatchListener;
    private ListenerRegistration listenerMessageNotSeen;
    private final FirebaseFunctions functions;


    @Inject
    public ChatFirebaseDao(FirebaseFirestore firestore, FirebaseFunctions functions) {
        this.firestore = firestore;
        this.functions = functions;
    }

    public Task<DocumentSnapshot> getChatById(String id) {
        DocumentReference chatDoc = firestore.collection("chats").document(id);

        return chatDoc.get();
    }

    public Task<QuerySnapshot> getChatsPaginated(String userId, String chatPreviewType ,DocumentSnapshot lastVisibleDocumentChat, int limit) {
        CollectionReference collectionChat = firestore.collection("chats");

        if (lastVisibleDocumentChat == null){
            Query query = collectionChat
                    .whereArrayContains("membersIds", userId);

            query = getQueryPreparedByChatPreviewType(query,chatPreviewType);

            query = query.orderBy("modifiedTimeByLastChatMessageUTC", Query.Direction.DESCENDING)
                    .limit(limit);

            return query.get();
        }

        Query query = collectionChat
                .whereArrayContains("membersIds", userId);

        query = getQueryPreparedByChatPreviewType(query,chatPreviewType);

        query = query.orderBy("modifiedTimeByLastChatMessageUTC", Query.Direction.DESCENDING)
                .startAfter(lastVisibleDocumentChat)
                .limit(limit);

        return query.get();
    }



    public Query getQueryPreparedByChatPreviewType(Query query,String chatPreviewType) {

        if (chatPreviewType.equals(ChatPreviewTypesConstants.RELEVANT_MATCHES)){
            return query.whereEqualTo("chatMatchIsRelevant", true)
                    .whereEqualTo("chatType", ChatTypesConstants.MATCH_CONVERSATION);
        }else if (chatPreviewType.equals(ChatPreviewTypesConstants.NON_RELEVANT_MATCHES)){
            return query.whereEqualTo("chatMatchIsRelevant", false)
                    .whereEqualTo("chatType", ChatTypesConstants.MATCH_CONVERSATION);
        }else if (chatPreviewType.equals(ChatPreviewTypesConstants.PRIVATE_CONVERSATIONS)){
            return query.whereEqualTo("chatType", ChatTypesConstants.PRIVATE_CONVERSATION);
        }

        return query;
    }


    public Task<HttpsCallableResult> leaveChatIfMatchConversation(String yourId, String chatId) {
        Map<String,Object> map = new HashMap<>();
        map.put("chatId",chatId);
        map.put("yourId",yourId);

        return functions.getHttpsCallable("chatRepositoryLeaveChatIfMatchConversation")
                .call(map);
    }

    public Task<HttpsCallableResult> kickUserIfMatchConversation(String userToKick, String chatId) {
        Map<String,Object> map = new HashMap<>();

        map.put("chatId",chatId);
        map.put("userToKick",userToKick);

        return functions.getHttpsCallable("chatRepositoryKickUserIfMatchConversation")//todo
                .call(map);
    }

    public Task<HttpsCallableResult> saveChatIfPrivateConversation(Chat chat) {
        Map<String,Object> map = new HashMap<>();

        Gson gson = new Gson();
        String jsonStringChat = gson.toJson(chat);
        map.put("chat",jsonStringChat);


        return functions.getHttpsCallable("chatRepositorySaveChatIfPrivateConversation")
                .call(map);
    }

    public Task<HttpsCallableResult> destroyChatIfMatchConversation(String chatId, String yourId) {

        Map<String,String> map = new HashMap<>();

        map.put("chatId",chatId);
        map.put("yourId",yourId);


        return functions.getHttpsCallable("chatRepositoryDestroyChatIfMatchConversation")
                .call(map);
    }

    public Task<HttpsCallableResult> destroyChatIfPrivateConversation(String chatId, String yourId) {

        Map<String,String> map = new HashMap<>();

        map.put("chatId",chatId);
        map.put("yourId",yourId);


        return functions.getHttpsCallable("chatRepositoryDestroyChatIfPrivateConversation")
                .call(map);
    }

    public Task<HttpsCallableResult> changeAdminIfMatchConversation(String userToGiveAdmin, String yourId, String chatId) {

        Map<String,String> map = new HashMap<>();

        map.put("userToGiveAdmin",userToGiveAdmin);
        map.put("yourId",yourId);
        map.put("chatId",chatId);


        return functions.getHttpsCallable("chatRepositoryChangeAdminIfMatchConversation")
                .call(map);
    }


    public Task<HttpsCallableResult> addOrRemoveYourPhoneNumberToThatChat(String yourId, String chatId, boolean addOrRemove) {

        Map<String,Object> map = new HashMap<>();

        map.put("yourId",yourId);
        map.put("chatId",chatId);
        map.put("addOrRemove",addOrRemove);


        return functions.getHttpsCallable("chatRepositoryAddOrRemoveYourPhoneNumberToThatChat")
                .call(map);
    }

    public Task<HttpsCallableResult> getPrivateChatOrCreate(String user1Id, String user2Id) {

        Map<String,Object> map = new HashMap<>();

        map.put("user1Id",user1Id);
        map.put("user2Id",user2Id);


        return functions.getHttpsCallable("chatRepositoryGetPrivateChatOrCreate")
                .call(map);
    }

    public void initListenerForChatUpdates(String userId, String chatPreviewType,FirestoreCallbackDocumentChanges<Chat> firestoreCallbackDocumentChanges) {
        if (listenerForChatUpdates != null)
            return;

        CollectionReference chatCollection = firestore.collection("chats");

        Query query = chatCollection
                .whereArrayContains("membersIds", userId);

        query = getQueryPreparedByChatPreviewType(query,chatPreviewType);

        long timeAfterIWantToFetch = TimeFromInternet.getInternetTimeEpochUTC() - 10 * 60 * 1000;

        listenerForChatUpdates = query
                .whereGreaterThan("lastModifiedTimeInUTC", timeAfterIWantToFetch)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        firestoreCallbackDocumentChanges.onError(error);
                        return;
                    }


                    List<Chat> changedDocuments = new ArrayList<>();
                    for (DocumentChange docChange : value.getDocumentChanges()) {

                        Chat chat = new Chat(docChange.getDocument().getData());

                        changedDocuments.add(chat);
                    }

                    firestoreCallbackDocumentChanges.onSuccess(changedDocuments);
                });
    }


    public void initMessagesNotSeenByUser(String userId, int limit ,FirestoreCallbackDocumentChanges<Chat> chatFirestoreCallbackDocumentChanges) {
        if (listenerMessageNotSeen != null)
            return;

        CollectionReference chatCollection = firestore.collection("chats");


        listenerMessageNotSeen = chatCollection
                .whereArrayContains("membersIds", userId)
                .orderBy("modifiedTimeByLastChatMessageUTC", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        chatFirestoreCallbackDocumentChanges.onError(error);
                        return;
                    }


                    List<Chat> changedDocuments = new ArrayList<>();
                    for (DocumentChange docChange : value.getDocumentChanges()) {

                        Chat chat = new Chat(docChange.getDocument().getData());
                        if (!chat.isMember(userId))
                            continue;

                        changedDocuments.add(chat);
                    }

                    changedDocuments.sort(new ChatComparator());

                    chatFirestoreCallbackDocumentChanges.onSuccess(changedDocuments);
                });
    }

    public void initCurrentUsersInMatchListener(String chatId, FirestoreCallbackDocumentChanges<String> firestoreCallbackDocumentChanges) {
        if (listenerCurrentUsersInMatchListener != null)
            return;


        CollectionReference chatCollection = firestore.collection("chats");

        listenerCurrentUsersInMatchListener = chatCollection
                .whereEqualTo("id",chatId)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        firestoreCallbackDocumentChanges.onError(error);
                        return;
                    }

                    List<String> changedDocuments = new ArrayList<>();
                    for (DocumentChange docChange : value.getDocumentChanges()) {

                        Chat chat = new Chat(docChange.getDocument().getData());

                        changedDocuments.addAll(chat.getMembersIds());
                    }

                    firestoreCallbackDocumentChanges.onSuccess(changedDocuments);
                });

    }

    public Task<HttpsCallableResult> updatePinnedMessage(ChatMessage pinnedMessage, String yourId) {

        Map<String,String> map = new HashMap<>();

        Gson gson = new Gson();
        String jsonStringChatMessage = gson.toJson(pinnedMessage);
        map.put("pinnedMessage", jsonStringChatMessage);

        map.put("yourId",yourId);


        return functions.getHttpsCallable("chatRepositoryUpdatePinnedMessage")
                .call(map);
    }

    public Task<HttpsCallableResult> deletePinnedMessage(ChatMessage pinnedMessage, String yourId) {

        Map<String,String> map = new HashMap<>();

        Gson gson = new Gson();
        String jsonStringChatMessage = gson.toJson(pinnedMessage);
        map.put("pinnedMessage", jsonStringChatMessage);

        map.put("yourId",yourId);


        return functions.getHttpsCallable("chatRepositoryDeletePinnedMessage")
                .call(map);
    }

    public Task<HttpsCallableResult> blockPlayer(String userIdToBlock) {
        Map<String,Object> map = new HashMap<>();

        map.put("userIdToBlock", userIdToBlock);


        return functions.getHttpsCallable("userPrivacyBlockPlayer")
                .call(map);
    }

    public void removeListeners() {
        if (listenerCurrentUsersInMatchListener != null) {

            listenerCurrentUsersInMatchListener.remove();
            listenerCurrentUsersInMatchListener = null;
        }

        if (listenerMessageNotSeen != null) {

            listenerMessageNotSeen.remove();
            listenerMessageNotSeen = null;
        }

        if (listenerForChatUpdates != null) {

            listenerForChatUpdates.remove();
            listenerForChatUpdates = null;
        }
    }



}
