package com.baikas.sporthub6.viewmodels.mainpage.mainactivity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baikas.sporthub6.helpers.comparators.ChatComparator;
import com.baikas.sporthub6.helpers.comparators.MatchComparator;
import com.baikas.sporthub6.models.MatchFilter;
import com.baikas.sporthub6.models.constants.ConfigurationsConstants;
import com.baikas.sporthub6.helpers.operations.ListOperations;
import com.baikas.sporthub6.interfaces.FirestoreCallbackDocumentChanges;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.repositories.MatchRepository;
import com.baikas.sporthub6.repositories.UserRepository;
import com.baikas.sporthub6.repositories.chat.ChatRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainActivityViewModel extends ViewModel {
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> usersRequestedToJoinLiveData = new MutableLiveData<>();
    private final MutableLiveData<MatchFilter> matchFilterLiveData = new MutableLiveData<>();
    private final List<Match> usersRequestedToJoin = new ArrayList<>();
    private final MutableLiveData<Integer> messagesNotSeenRelevantChatsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> messagesNotSeenIrrelevantChatsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> messagesNotSeenPrivateChatsLiveData = new MutableLiveData<>();
    private final List<Chat> allMatchesNotSeen = new ArrayList<>();
    private final MutableLiveData<List<Chat>> notifyFragmentLiveData = new MutableLiveData<>();

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final ChatRepository chatRepository;



    @Inject
    public MainActivityViewModel(UserRepository userRepository, MatchRepository matchRepository,ChatRepository chatRepository) {
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.chatRepository = chatRepository;

        this.usersRequestedToJoinLiveData.setValue(0);
        this.messagesNotSeenRelevantChatsLiveData.setValue(0);
        this.messagesNotSeenIrrelevantChatsLiveData.setValue(0);
        this.messagesNotSeenPrivateChatsLiveData.setValue(0);
        this.notifyFragmentLiveData.setValue(new ArrayList<>());

        this.matchFilterLiveData.setValue(MatchFilter.resetFilterDisabled());
    }


    public MutableLiveData<Integer> getUsersRequestedToJoinLiveData() {
        return usersRequestedToJoinLiveData;
    }



    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();

        userRepository.getUserById(userId).thenAccept((user -> liveData.postValue(user)))
                .exceptionally((throwable)->{
                    errorMessageLiveData.postValue("Δοκιμάστε ξανά");
                    return null;
                });

        return liveData;
    }


    public void initUsersRequestedInYourMatchesAdminListener(String userId){
        matchRepository.initYourMatchesAdminAllSportsListener(userId, new FirestoreCallbackDocumentChanges<Match>() {

            @Override
            public void onSuccess(List<Match> documents) {
                if (documents.isEmpty())
                    return;

                ListOperations.combineListsAndAfterSort(documents,usersRequestedToJoin, new MatchComparator());

                List<String> usersRequestedButNotIgnoredByAdminAll = new ArrayList<>();
                for (Match match:usersRequestedToJoin) {
                    if (match.getUserRequestsToJoinMatch().isEmpty())
                        continue;

                    List<String> usersRequestedButNotIgnoredByAdmin = new ArrayList<>(match.getUserRequestsToJoinMatch());

                    usersRequestedButNotIgnoredByAdmin.removeAll(match.getUsersInChat());

                    usersRequestedButNotIgnoredByAdmin.removeAll(match.getAdminIgnoredRequesters());


                    //add to All
                    usersRequestedButNotIgnoredByAdminAll.addAll(usersRequestedButNotIgnoredByAdmin);
                }

                int totalRequests = usersRequestedButNotIgnoredByAdminAll.size();

                usersRequestedToJoinLiveData.setValue(totalRequests);
            }

            @Override
            public void onError(Exception e) {
                
            }
        });
    }

    public void initMessagesNotSeenByUser(String userId) {

        int limit = ConfigurationsConstants.MAX_CHAT_FETCH_FROM_LISTENERS;

        chatRepository.initMessagesNotSeenByUser(userId, limit,new FirestoreCallbackDocumentChanges<Chat>() {

            @Override
            public void onSuccess(List<Chat> documents) {
                if (documents.isEmpty())
                    return;

                ListOperations.combineListsAndAfterSort(documents,allMatchesNotSeen,new ChatComparator());

                notifyFragmentLiveData.setValue(allMatchesNotSeen);

                int newMessagesRelevantChats = 0;
                int newMessagesIrrelevantChats = 0;
                int newMessagesPrivateChats = 0;

                String userId = FirebaseAuth.getInstance().getUid();
                for (Chat chat : allMatchesNotSeen) {

                    if (!chat.getNotSeenByUsersId().contains(userId))
                        continue;

                    if (chat.isPrivateConversation()) {
                        newMessagesPrivateChats++;
                        continue;
                    }

                    if (chat.isChatMatchIsRelevant()) {
                        newMessagesRelevantChats++;
                        continue;
                    }else{
                        newMessagesIrrelevantChats++;
                        continue;
                    }

                }

                messagesNotSeenRelevantChatsLiveData.setValue(newMessagesRelevantChats);
                messagesNotSeenIrrelevantChatsLiveData.setValue(newMessagesIrrelevantChats);
                messagesNotSeenPrivateChatsLiveData.setValue(newMessagesPrivateChats);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }




    public LiveData<Match> getMatchById(String matchId, String sport) {
        MutableLiveData<Match> liveData = new MutableLiveData<>();

        matchRepository.getMatchById(matchId,sport).thenAccept((match -> liveData.postValue(match)))
                .exceptionally((throwable)->{
                    errorMessageLiveData.postValue("Δοκιμάστε ξανά");
                    return null;
                });

        return liveData;
    }

    public MutableLiveData<MatchFilter> getMatchFilterLiveData() {
        return matchFilterLiveData;
    }

    public MutableLiveData<Integer> getMessagesNotSeenRelevantChatsLiveData() {
        return messagesNotSeenRelevantChatsLiveData;
    }

    public MutableLiveData<Integer> getMessagesNotSeenIrrelevantChatsLiveData() {
        return messagesNotSeenIrrelevantChatsLiveData;
    }

    public MutableLiveData<Integer> getMessagesNotSeenPrivateChatsLiveData() {
        return messagesNotSeenPrivateChatsLiveData;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public LiveData<List<Chat>> getNotifyFragmentLiveData() {
        return notifyFragmentLiveData;
    }

    public List<Chat> getAllMatchesNotSeen() {
        return allMatchesNotSeen;
    }


    public void removeListeners() {
        matchRepository.removeListeners();
        chatRepository.removeListeners();
    }

}
