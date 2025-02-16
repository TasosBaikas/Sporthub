package com.baikas.sporthub6.viewmodels.mainpage.teamhub;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.exceptions.NoMoreDataToLoadException;
import com.baikas.sporthub6.exceptions.PaginationException;
import com.baikas.sporthub6.helpers.comparators.ChatComparator;
import com.baikas.sporthub6.models.constants.ChatPreviewTypesConstants;
import com.baikas.sporthub6.helpers.operations.ListOperations;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.time.GreekDateFormatter;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.FirestoreCallbackDocumentChanges;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.chat.fakemessages.FakeChatForMessage;
import com.baikas.sporthub6.repositories.chat.ChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatPreviewContentFragmentViewModel extends ViewModel {

    private boolean isLoading = false;
    private final ChatRepository chatRepository;
    private final CheckInternetConnection checkInternetConnection;

    private final MutableLiveData<String> messageToUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Chat>> mainChatsLiveData = new MutableLiveData<>();
    @NotNull
    private final AtomicReference<DocumentSnapshot> lastVisibleDocumentChat = new AtomicReference<>(null);
    private String chatPreviewType;

    public static final int MAX_ITEMS_PER_SCROLL = 10;


    @Inject
    public ChatPreviewContentFragmentViewModel(ChatRepository chatRepository, CheckInternetConnection checkInternetConnection) {
        this.chatRepository = chatRepository;
        this.checkInternetConnection = checkInternetConnection;
        mainChatsLiveData.setValue(new ArrayList<>());
    }

    public LiveData<Void> loadDataFromServer(String chatPreviewType){
        MutableLiveData<Void> completedLiveData = new MutableLiveData<>();

        if (!getChats().isEmpty())
            return completedLiveData;

        String userId = FirebaseAuth.getInstance().getUid();


        chatRepository.getChatsPaginated(userId,chatPreviewType,lastVisibleDocumentChat,MAX_ITEMS_PER_SCROLL)
                .thenAccept((List<Chat> moreChats) -> {
                    if (moreChats == null || moreChats.isEmpty())
                        throw new NoMoreDataToLoadException();

                    new Handler(Looper.getMainLooper()).post(()-> {

                        ListOperations.combineListsAndAfterSort(moreChats,getChats(),new ChatComparator());

                        if (!getChats().isEmpty() && getChats().get(getChats().size() - 1) instanceof FakeChatForMessage)
                            getChats().remove(getChats().size() - 1);

                        if (getChats().size() < MAX_ITEMS_PER_SCROLL){

                            getChats().add(FakeChatForMessage.fakeChatNoMoreMessages("Δεν υπάρχουν άλλα chat"));
                        }
                    });
                })
                .exceptionally(throwable -> {

                    getChats().removeIf((chat) -> chat instanceof FakeChatForMessage && !chat.getId().equals("NoMoreMessages"));

                    Throwable cause = throwable.getCause();

                    FakeChatForMessage fakeChatForMessage;
                    if (cause instanceof NoMoreDataToLoadException) {//todo also this must show a message at the bottom or something

                        if (getChats().isEmpty()){
                            fakeChatForMessage = new FakeChatForMessage("Δεν υπάρχουν chat");
                            getChats().add(fakeChatForMessage);
                            return null;
                        }

                        Chat lastChat = getChats().get(getChats().size() - 1);
                        if (lastChat instanceof FakeChatForMessage && lastChat.getId().equals("NoMoreMessages"))
                            return null;

                        fakeChatForMessage = FakeChatForMessage.fakeChatNoMoreMessages("Δεν υπάρχουν άλλα chat");
                        getChats().add(fakeChatForMessage);
                        return null;
                    }

                    if (cause instanceof TimeoutException) {//
                        if (getChats().isEmpty()) {
                            fakeChatForMessage = new FakeChatForMessage("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                            getChats().add(fakeChatForMessage);
                        }

                        messageToUserLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                        Log.i(TAG, "loadDataFromServer: timeout", cause);;
                        return null;
                    }

                    if (cause instanceof PaginationException) {//todo i have to handle the situation where the pagination fails because the lastVisible has no match
                        messageToUserLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                        return null;
                    }

                    if (!checkInternetConnection.isNetworkConnected()) {
                        if (getChats().isEmpty()) {
                            fakeChatForMessage = new FakeChatForMessage("Δεν έχετε internet");
                            getChats().add(fakeChatForMessage);
                        }

                        messageToUserLiveData.postValue("Δεν έχετε internet");
                        return null;
                    }


                    Log.e(TAG, "Error fetching data: " + throwable.getMessage());
                    messageToUserLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                    return null;
                }).whenComplete((unused,unusedThrowable)->{//todo update getmore
                    new Handler(Looper.getMainLooper()).post(()->{
                        getChats().removeAll(Collections.singleton(null));

                        this.removeMatchesBasedOnChatTypeFilters(getChats());

                        getChats().sort(new ChatComparator());

                        mainChatsLiveData.setValue(getChats());

                        completedLiveData.setValue(null);
                    });
                });


        return completedLiveData;
    }

    private void removeMatchesBasedOnChatTypeFilters(List<Chat> chatsFakeRef) {

        if (chatPreviewType.equals(ChatPreviewTypesConstants.RELEVANT_MATCHES)){

            chatsFakeRef.removeIf(chat -> chat != null && !(chat instanceof FakeChatForMessage) && GreekDateFormatter.roundUpToMinute(chat.getMatchDateInUTC()) < TimeFromInternet.getInternetTimeEpochUTC());
        }

    }


    public void getMoreData(String chatPreviewType){
        String userId = FirebaseAuth.getInstance().getUid();

        chatRepository.getChatsPaginated(userId,chatPreviewType,lastVisibleDocumentChat,MAX_ITEMS_PER_SCROLL)
                .thenAccept(moreChats -> {
                    if (moreChats == null || moreChats.isEmpty())
                        throw new NoMoreDataToLoadException();

                    new Handler(Looper.getMainLooper()).post(()-> {

                        getChats().removeAll(Collections.singleton(null));

                        ListOperations.combineListsAndAfterSort(moreChats,getChats(),new ChatComparator());
                    });
                })
                .exceptionally(throwable -> {

                    getChats().removeAll(Collections.singleton(null));

                    getChats().removeIf((chat) -> chat instanceof FakeChatForMessage && !chat.getId().equals("NoMoreMessages"));

                    Throwable cause = throwable.getCause();

                    FakeChatForMessage fakeChatForMessage;
                    if (cause instanceof NoMoreDataToLoadException) {//todo also this must show a message at the bottom or something

                        if (getChats().isEmpty()){
                            fakeChatForMessage = new FakeChatForMessage("Δεν υπάρχουν chat");
                            getChats().add(fakeChatForMessage);
                            return null;
                        }

                        Chat lastChat = getChats().get(getChats().size() - 1);
                        if (lastChat instanceof FakeChatForMessage && lastChat.getId().equals("NoMoreMessages"))
                            return null;

                        fakeChatForMessage = FakeChatForMessage.fakeChatNoMoreMessages("Δεν υπάρχουν άλλα chat");
                        getChats().add(fakeChatForMessage);
                        return null;
                    }

                    if (cause instanceof TimeoutException) {//
                        messageToUserLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                        Log.i(TAG, "loadDataFromServer: timeout", cause);
                        return null;
                    }

                    if (cause instanceof PaginationException) {//todo i have to handle the situation where the pagination fails because the lastVisible has no match
                        messageToUserLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                        return null;
                    }

                    if (!checkInternetConnection.isNetworkConnected()) {
                        messageToUserLiveData.postValue("Δεν έχετε internet");
                        return null;
                    }


                    Log.e(TAG, "Error fetching data: " + throwable.getMessage());
                    messageToUserLiveData.postValue("Υπάρχει πρόβλημα με την σύνδεση προσπαθήστε ξανά");
                    return null;
                }).whenComplete((unused,unusedThrowable)->{
                    new Handler(Looper.getMainLooper()).post(()->{
                        setLoading(false);

                        getChats().removeAll(Collections.singleton(null));

                        this.removeMatchesBasedOnChatTypeFilters(getChats());

                        getChats().sort(new ChatComparator());

                        mainChatsLiveData.setValue(getChats());
                    });
                });


    }

    public void initListenerForChatUpdates(String userId, String chatPreviewType) {
        chatRepository.initListenerForChatUpdates(userId, chatPreviewType,new FirestoreCallbackDocumentChanges<Chat>() {
            @Override
            public void onSuccess(List<Chat> documents) {
                if (documents.isEmpty())
                    return;

                boolean chatMessagesIsCurrentlyLoadingMoreData = false;
                if (getChats().size() != 0 && getChats().get(getChats().size() - 1) == null){
                    getChats().remove(null);
                    chatMessagesIsCurrentlyLoadingMoreData = true;
                }

                ListOperations.combineListsAndAfterSort(documents,getChats(), new ChatComparator());

                if (chatMessagesIsCurrentlyLoadingMoreData)
                    getChats().add(null);


                mainChatsLiveData.setValue(getChats());
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }


    public void clearData() {
        isLoading = false;

        lastVisibleDocumentChat.set(null);

        getChats().clear();
    }

    public void setChatPreviewType(String chatPreviewType) {
        this.chatPreviewType = chatPreviewType;
    }
    
    

    public String getChatPreviewType() {
        return this.chatPreviewType;
    }
    public MutableLiveData<List<Chat>> getMainChatsLiveData() {
        return mainChatsLiveData;
    }

    public List<Chat> getChats(){
        return mainChatsLiveData.getValue();
    }

    public MutableLiveData<String> getMessageToUserLiveData() {
        return messageToUserLiveData;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }




    public void removeListeners(){
        chatRepository.removeListeners();
    }
}
