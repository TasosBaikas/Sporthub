package com.baikas.sporthub6.viewmodels.chat;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.NoInternetConnectionException;
import com.baikas.sporthub6.exceptions.NoMoreDataToLoadException;
import com.baikas.sporthub6.exceptions.PaginationException;
import com.baikas.sporthub6.helpers.comparators.ChatMessagesComparator;
import com.baikas.sporthub6.helpers.operations.ListOperations;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.interfaces.FirestoreCallbackDocumentChanges;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.chat.ChatMessage;
import com.baikas.sporthub6.models.chat.ChatMessageBatch;
import com.baikas.sporthub6.models.chat.fakemessages.FakeChatMessageForUser;
import com.baikas.sporthub6.models.user.UserShortForm;
import com.baikas.sporthub6.repositories.chat.ChatRepository;
import com.baikas.sporthub6.repositories.UserRepository;
import com.baikas.sporthub6.repositories.chat.ChatMessageRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatFragmentViewModel extends ViewModel{

    private boolean isLoading = false;

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<List<ChatMessage>> chatMessageListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> loadingBarLiveData = new MutableLiveData<>();
    private final Map<String,UserShortForm> usersMapFromAsync = new HashMap<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<ChatMessage> pinMessageUpdatedLiveData = new MutableLiveData<>();
    private Chat chat;
    private Integer widthReference = null;
    private final CheckInternetConnection checkInternetConnection;
    private final AtomicReference<Long> lastVisibleDocumentCreatedAtUTC = new AtomicReference<>(null);
    public int LOW_MESSAGES_CONSTANT = ChatMessageBatch.MAX_MESSAGES/2;


    @Inject
    public ChatFragmentViewModel(ChatMessageRepository chatMessageRepository, UserRepository userRepository, ChatRepository chatRepository, CheckInternetConnection checkInternetConnection) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.checkInternetConnection = checkInternetConnection;
        chatMessageListLiveData.setValue(new ArrayList<>());
    }


    public void loadDataFromServer(){

        getChatMessageList().removeIf((chat) -> chat instanceof FakeChatMessageForUser);

        if (!getChatMessageList().isEmpty())
            return;

        chatMessageRepository.getChatMessagesPaginated(chat.getId(), lastVisibleDocumentCreatedAtUTC)
                .thenAcceptAsync((ChatMessageBatch chatMessageBatch) -> {
                    if (chatMessageBatch == null || chatMessageBatch.getChatMessages().isEmpty())
                        throw new NoMoreDataToLoadException();

                    new Handler(Looper.getMainLooper()).post(() -> {

                        ListOperations.combineListsAndAfterSort(chatMessageBatch.getChatMessages(),getChatMessageList(),new ChatMessagesComparator());

                        chatMessageRepository.changeSeenByUser(getChatMessageList().get(0), FirebaseAuth.getInstance().getUid());
                    });

                })
                .exceptionally((throwable -> {

                    Throwable cause = throwable.getCause();

                    if (cause instanceof NoMoreDataToLoadException) {//todo also this must show a message at the bottom or something
                        return null;
                    }

                    if (cause instanceof TimeoutException) {//
                        if (getChatMessageList().isEmpty()) {
                            getChatMessageList().add(new FakeChatMessageForUser("Υπάρχει πρόβλημα με την σύνδεση"));
                        }

                        errorMessageLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση");
                        Log.i(TAG, "loadDataFromServer: timeout", cause);;
                        return null;
                    }

                    if (throwable.getCause() instanceof NoInternetConnectionException || !checkInternetConnection.isNetworkConnected()) {
                        if (getChatMessageList().isEmpty()) {
                            getChatMessageList().add(new FakeChatMessageForUser("Δεν έχετε internet"));
                        }

                        Log.i(TAG, "Δεν έχετε internet", cause);
                        errorMessageLiveData.postValue("Δεν έχετε internet");
                        return null;
                    }

                    Log.e(TAG, "Error fetching data: " + throwable.getMessage());
                    errorMessageLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                    return null;
                })).whenComplete((unused,unusedThrowable)->{//todo update getmore
                    new Handler(Looper.getMainLooper()).post(()->{

                        getChatMessageList().sort(new ChatMessagesComparator());

                        loadingBarLiveData.setValue(ProgressBar.GONE);

                        chatMessageListLiveData.setValue(getChatMessageList());
                    });
                });
    }

    public void getMoreData(){

        chatMessageRepository.getChatMessagesPaginated(chat.getId(), lastVisibleDocumentCreatedAtUTC)
                .thenAcceptAsync((ChatMessageBatch chatMessageBatch) -> {
                    if (chatMessageBatch == null || chatMessageBatch.getChatMessages().isEmpty())
                        throw new NoMoreDataToLoadException();

                    new Handler(Looper.getMainLooper()).post(() -> {

                        getChatMessageList().removeAll(Collections.singleton(null));

                        ListOperations.addOnlyToListAndSort(chatMessageBatch.getChatMessages(),getChatMessageList(), new ChatMessagesComparator());
                    });
                })
                .exceptionally(throwable -> {
                    getChatMessageList().removeAll(Collections.singleton(null));

                    if (getChatMessageList().size() == 1 && getChatMessageList().get(0) instanceof FakeChatMessageForUser){
                        return null;
                    }

                    if (!getChatMessageList().isEmpty()) {
                        ChatMessage lastChatMessage = getChatMessageList().get(getChatMessageList().size() - 1);
                        if (lastChatMessage instanceof FakeChatMessageForUser)
                            getChatMessageList().remove(getChatMessageList().size() - 1);
                    }


                    Throwable cause = throwable.getCause();

                    if (cause instanceof NoMoreDataToLoadException)
                        return null;


                    if (cause instanceof TimeoutException) {//
                        errorMessageLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                        Log.i(TAG, "loadDataFromServer: timeout", cause);;
                        return null;
                    }

                    if (cause instanceof PaginationException) {//todo i have to handle the situation where the pagination fails because the lastVisible has no match
                        errorMessageLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                        return null;
                    }

                    if (cause instanceof NoInternetConnectionException || !checkInternetConnection.isNetworkConnected()) {
                        errorMessageLiveData.postValue("Δεν έχετε internet");
                        return null;
                    }

                    Log.e(TAG, "Error fetching data: " + throwable.getMessage());
                    errorMessageLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                    return null;
                }).whenComplete((unused,unusedThrowable)->{//todo update getmore
                    new Handler(Looper.getMainLooper()).post(()->{
                        setLoading(false);


                        getChatMessageList().sort(new ChatMessagesComparator());

                        chatMessageListLiveData.setValue(getChatMessageList());
                    });
                });

    }


//    public void initCurrentUsersInMatchListener() {
//        chatRepository.initCurrentUsersInMatchListener(chat.getId(), new FirestoreCallbackDocumentChanges<String>() {
//
//            @Override
//            public void onSuccess(List<String> usersIdsInMatch) {
//
//                if (!usersIdsInMatch.contains(userId))
//                    isUserMemberInMatchLiveData.postValue(false);
//
//                List<String> difference = usersIdsInMatch.stream()
//                        .filter(user -> !usersWithChatAction.containsKey(user))
//                        .collect(Collectors.toList());
//
//                userRepository.getUsersByIds(difference)
//                        .thenAccept((Map<String,User> userIds) -> usersWithChatAction.putAll(userIds));
//            }
//
//            @Override
//            public void onError(Exception e) {
//
//            }
//        });
//    }

    public void initOnMessagesListener(){
        chatMessageRepository.initOnMessagesListener(chat.getId(), new FirestoreCallbackDocumentChanges<ChatMessage>() {
            @Override
            public void onSuccess(List<ChatMessage> documents) {

                if (documents.isEmpty())
                    return;


                boolean chatMessagesIsCurrentlyLoadingMoreData = false;
                if (getChatMessageList().size() != 0 && getChatMessageList().get(getChatMessageList().size() - 1) == null){
                    getChatMessageList().remove(null);
                    chatMessagesIsCurrentlyLoadingMoreData = true;
                }

                List<ChatMessage> chatMessagesThatCanBeUpdated = getChatMessageList();
                if (getChatMessageList().size() - 1 >= 0){

                    ChatMessage lastMessageInChatRightNow = getChatMessageList().get(getChatMessageList().size() - 1);
                    chatMessagesThatCanBeUpdated = messagesAfterLastMessage(lastMessageInChatRightNow, documents);
                }

                if (new HashSet<>(getChatMessageList()).containsAll(chatMessagesThatCanBeUpdated))
                    return;

                ListOperations.combineListsAndAfterSort(chatMessagesThatCanBeUpdated, getChatMessageList(), new ChatMessagesComparator());

                getChatMessageList().removeIf((chatMessage -> chatMessage != null && chatMessage instanceof FakeChatMessageForUser));

                if (!getChatMessageList().isEmpty())
                    chatMessageRepository.changeSeenByUser(getChatMessageList().get(0), FirebaseAuth.getInstance().getUid());

                if (chatMessagesIsCurrentlyLoadingMoreData)
                    getChatMessageList().add(null);


                chatMessageListLiveData.setValue(getChatMessageList());
            }

            @Override
            public void onError(Exception e) {
                errorMessageLiveData.postValue("Reload the page to get the latest messages!");
            }
        });
    }

    public void deleteChatMessage(ChatMessage chatMessage) {

        ChatMessage pinnedMessage = chat.getPinnedMessage();
        if (pinnedMessage != null && !pinnedMessage.isDeleted() && pinnedMessage.getId().equals(chatMessage.getId())){
            errorMessageLiveData.postValue("Πρέπει πρώτα να διαγράψετε το καρφιτσωμένο");
            return;
        }

        chatMessageRepository.deleteChatMessage(chatMessage)
                .thenAccept((ChatMessage virtualDeletedChatMessage) -> {

                    if (virtualDeletedChatMessage == null)
                        return;

                    ListOperations.setElementToList(virtualDeletedChatMessage,getChatMessageList());

                    chatMessageListLiveData.postValue(getChatMessageList());
                })
                .exceptionally(throwable -> {
                    Throwable cause = throwable.getCause();

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                });

    }

    public void requestFromNotSeenBy(ChatMessage chatMessage, List<String> ids) {

        if (this.usersMapFromAsync.keySet().containsAll(ids))
            return;

        List<String> tempIds = new ArrayList<>(ids);
        tempIds.removeAll(this.usersMapFromAsync.keySet());

        this.userRepository.getUsersByIds(tempIds)
                .thenAccept((Map<String, User> users) -> {


            for (User user: users.values())
                usersMapFromAsync.put(user.getUserId(),new UserShortForm(user, chat.getSport()));

            List<ChatMessage> newRef = getChatMessageList().stream()
                            .filter((ChatMessage chatMessageTemp) -> chatMessage.isItHasProfileImageInDisplay())
                            .map((ChatMessage chatMessageTemp) -> new ChatMessage(chatMessageTemp))
                            .collect(Collectors.toList());

            newRef.stream()
                            .forEach((ChatMessage chatMessageTemp) -> chatMessageTemp.setItHasProfileImageInDisplay(false));

            ListOperations.combineListsAndAfterSort(newRef, getChatMessageList(), new ChatMessagesComparator());

            chatMessageListLiveData.postValue(getChatMessageList());
        }).exceptionally((e) -> {
            errorMessageLiveData.postValue("Δεν φορτώθηκαν οι χρήστες");
            return null;
        });

    }

    public void pinMessage(ChatMessage chatMessage) {

        chatRepository.updatePinnedMessage(chatMessage)
                .thenAccept((unused -> pinMessageUpdatedLiveData.postValue(chatMessage)))
                .exceptionally((e) -> {
                    Throwable cause = e.getCause();

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                });

    }

    public void removeListeners() {
        chatMessageRepository.removeListeners();
        chatRepository.removeListeners();
    }

    private List<ChatMessage> messagesAfterLastMessage(ChatMessage lastMessageInChatRightNow, List<ChatMessage> messagesChangeFromListener) {
        return messagesChangeFromListener.stream()
                .filter((chatMessage -> chatMessage.getCreatedAtUTC() >= lastMessageInChatRightNow.getCreatedAtUTC()))
                .collect(Collectors.toList());
    }


    public void updateEmojiCount(ChatMessage chatMessage, String emojiClicked,String userId) {
        chatMessageRepository.updateEmojiCount(chatMessage,emojiClicked,userId)
                .exceptionally((throwable -> {
                    Throwable cause = throwable.getCause();

                    errorMessageLiveData.postValue(cause.getMessage());

                    return null;
                }));
    }

    public void attachListeners() {
//        initCurrentUsersInMatchListener();
        initOnMessagesListener();
    }


    public Integer getWidthReference() {
        return widthReference;
    }

    public void setWidthReference(Integer widthReference) {
        this.widthReference = widthReference;
    }

    public MutableLiveData<Integer> getLoadingBarLiveData() {
        return loadingBarLiveData;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public MutableLiveData<List<ChatMessage>> getChatMessageListLiveData() {
        return chatMessageListLiveData;
    }

    public List<ChatMessage> getChatMessageList(){
        return this.chatMessageListLiveData.getValue();
    }

    public MutableLiveData<ChatMessage> getPinMessageUpdatedLiveData() {
        return pinMessageUpdatedLiveData;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }


    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Map<String, UserShortForm> getUsersMapFromAsync() {
        return usersMapFromAsync;
    }
}
