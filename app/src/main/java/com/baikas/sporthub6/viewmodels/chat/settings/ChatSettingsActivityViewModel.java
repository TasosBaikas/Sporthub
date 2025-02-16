package com.baikas.sporthub6.viewmodels.chat.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.models.constants.ChatTypesConstants;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.repositories.UserRepository;
import com.baikas.sporthub6.repositories.chat.ChatMessageRepository;
import com.baikas.sporthub6.repositories.chat.ChatRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatSettingsActivityViewModel extends ViewModel {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CheckInternetConnection checkInternetConnection;
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private Map<String, User> chatMembers = new HashMap<>();
    private final List<String> usersClicked = new ArrayList<>();
    private String userClickedGiveAdmin;
    private Chat chat;


    @Inject
    public ChatSettingsActivityViewModel( UserRepository userRepository, ChatRepository chatRepository, CheckInternetConnection checkInternetConnection,ChatMessageRepository chatMessageRepository) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.checkInternetConnection = checkInternetConnection;
    }



    public LiveData<Void> deleteChat(Chat chatTemp) {
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        if (!chatTemp.isAdmin(FirebaseAuth.getInstance().getUid())){
            errorMessageLiveData.postValue("Δεν είστε διαχειριστής");
            return liveData;
        }

        new Thread(()-> {

            try{

                Chat chat = chatRepository.getChatById(chatTemp.getId()).join();
                String userId = FirebaseAuth.getInstance().getUid();

                if (chat.getChatType().equals(ChatTypesConstants.MATCH_CONVERSATION)){

                    chatRepository.deleteChatIfMatchConversation(chat.getId(),userId).join();
                    liveData.postValue(null);
                    return;
                }

                if (chat.isPrivateConversation()){

                    chatRepository.deleteChatIfPrivateConversation(chat.getId(),userId).join();
                    liveData.postValue(null);
                    return;
                }

            }catch (Exception eDontUse){
                Throwable cause = eDontUse.getCause();
                if (cause == null)
                    return;

                errorMessageLiveData.postValue(cause.getMessage());
                return;
            }

        }).start();


        return liveData;
    }


    public LiveData<Void> kickUser(String userToKick, String chatId) {
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        chatRepository.kickUser(userToKick,chatId)
                .thenRunAsync(()-> liveData.postValue(null))
                .exceptionally((Throwable eDontUse) -> {

                    Throwable cause = eDontUse.getCause();

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                });

        return liveData;
    }

    public LiveData<Void> changeAdmin(String userToGiveAdmin, String chatId) {
        MutableLiveData<Void> mutableLiveData = new MutableLiveData<>();

        chatRepository.changeAdmin(userToGiveAdmin,chatId)
                .thenRun(()-> {
                    chat.setAdminId(userToGiveAdmin);
                    mutableLiveData.postValue(null);
                })
                .exceptionally((eDontUse)-> {

                    Throwable cause = eDontUse.getCause();

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                });

        return mutableLiveData;
    }

    public LiveData<Void> leaveChat(Chat chat) {
        MutableLiveData<Void> liveData = new MutableLiveData<>();

        if (chat.isAdmin(FirebaseAuth.getInstance().getUid())){
            errorMessageLiveData.postValue("Πρέπει να δώσετε την διαχείριση πρώτα");
            return liveData;
        }

        if (chat.getMembersIds().size() <= 1){
            errorMessageLiveData.postValue("Είστε μόνος σας στο chat. Μπορείτε να διαγράψετε την ομάδα");
            return liveData;
        }

        chatRepository.leaveChat(FirebaseAuth.getInstance().getUid(), chat.getId())
                .thenRun(()-> liveData.postValue(null))
                .exceptionally((Throwable eDontUse) -> {
                    Throwable cause = eDontUse.getCause();

                    errorMessageLiveData.postValue(cause.getMessage());
                    return null;
                });

        return liveData;
    }


    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public void setChatMembers(Map<String, User> chatMembers) {
        this.chatMembers = chatMembers;
    }

    public Map<String, User> getChatMembers() {
        return chatMembers;
    }


    public List<String> getUsersClicked() {
        return usersClicked;
    }

    public String getUserClickedGiveAdmin() {
        return userClickedGiveAdmin;
    }

    public void setUserClickedGiveAdmin(String userClickedGiveAdmin) {
        this.userClickedGiveAdmin = userClickedGiveAdmin;
    }


}
