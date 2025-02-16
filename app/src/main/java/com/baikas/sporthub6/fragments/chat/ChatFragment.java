package com.baikas.sporthub6.fragments.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.interfaces.chat.AddOrRemoveLoadingBar;
import com.baikas.sporthub6.interfaces.chat.ChatMessageRequestsUsersAsync;
import com.baikas.sporthub6.interfaces.chat.NotifyToUpdatePinnedMessage;
import com.baikas.sporthub6.listeners.chat.OnLongClickOtherMessage;
import com.baikas.sporthub6.listeners.chat.OnLongClickUserMessage;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.chat.fakemessages.FakeChatMessageForUser;
import com.baikas.sporthub6.models.updates.DiffCallBackForChatConversation;
import com.baikas.sporthub6.models.updates.DiffCallBackGeneralized;
import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.chat.ChatAdapter;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.interfaces.AttachListeners;
import com.baikas.sporthub6.interfaces.RemoveListeners;
import com.baikas.sporthub6.interfaces.chat.AnimateRecyclerView;
import com.baikas.sporthub6.listeners.chat.MessageAddedOrUpdated;
import com.baikas.sporthub6.listeners.chat.OnEmojiClickListener;
import com.baikas.sporthub6.interfaces.chat.ShowAlertDialogEmojisInMessage;
import com.baikas.sporthub6.models.chat.ChatMessage;
import com.baikas.sporthub6.models.constants.MessageStatus;
import com.baikas.sporthub6.viewmodels.chat.ChatFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatFragment extends Fragment implements AnimateRecyclerView, OnEmojiClickListener,
        ChatMessageRequestsUsersAsync,OnLongClickUserMessage, OnLongClickOtherMessage,MessageAddedOrUpdated,
        RemoveListeners, AttachListeners {

    private ChatFragmentViewModel viewModel;
    private RecyclerView chatRecyclerView;
    private NotifyToUpdatePinnedMessage notifyToUpdatePinnedMessage;
    private ShowAlertDialogEmojisInMessage showAlertDialogEmojisInMessage;
    private static boolean restartCompleted = false;
    private AddOrRemoveLoadingBar activityAddOrRemoveLoadingBar;


    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            showAlertDialogEmojisInMessage = (ShowAlertDialogEmojisInMessage) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ShowAlertDialogEmojisInMessage interface");
        }

        try {
            notifyToUpdatePinnedMessage = (NotifyToUpdatePinnedMessage) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement NotifyToUpdatePinnedMessage interface");
        }

        try {
            activityAddOrRemoveLoadingBar = (AddOrRemoveLoadingBar) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement AddOrRemoveLoadingBar interface");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connectivityManager = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {

                if (viewModel == null)
                    return;

                // Network is available
                viewModel.loadDataFromServer();
                Log.d("NetworkCallback", "Network is active");
            }
            @Override
            public void onLost(Network network) {
                noInternetConnection();
            }

        };

    }


    private void noInternetConnection(){

        // Network is unavailable
        if (viewModel == null || !viewModel.getChatMessageList().isEmpty())
            return;


        new Handler(Looper.getMainLooper()).post(() -> {

            viewModel.getChatMessageList().add(new FakeChatMessageForUser("Δεν έχετε internet"));

            activityAddOrRemoveLoadingBar.remove();

            viewModel.getChatMessageListLiveData().postValue(viewModel.getChatMessageList());

            Log.d("NetworkCallback", "Network is lost");
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void attachListeners() {
        viewModel.attachListeners();
    }

    @Override
    public void removeListeners() {
        viewModel.removeListeners();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!CheckInternetConnection.isNetworkConnected(getContext())){
            noInternetConnection();
        }

        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setRestartCompleted(true);

        this.viewModel = new ViewModelProvider(this).get(ChatFragmentViewModel.class);


        // Retrieve the data passed from the Activity
        Bundle bundle = getArguments();
        if (bundle == null)
            return;

        viewModel.setChat((Chat) bundle.getSerializable("chat"));


//        viewModel.getUsersWithChatAction().putAll((Map<String, User>) bundle.getSerializable("chatMembers"));


        viewModel.attachListeners();

        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), (String errorMessage) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            activityAddOrRemoveLoadingBar.remove();
            ToastManager.showToast(getContext(), errorMessage, Toast.LENGTH_SHORT);
        });


        chatRecyclerView = requireView().findViewById(R.id.recycler_view_chat);
        ChatAdapter chatAdapter = new ChatAdapter(new ArrayList<>(),
                FirebaseAuth.getInstance().getUid(), viewModel.getChat(),viewModel.getUsersMapFromAsync(),this,this,this,showAlertDialogEmojisInMessage,this,this);

        chatRecyclerView.setAdapter(chatAdapter);
        WeakReference<RecyclerView> weakRef = new WeakReference<>(chatRecyclerView);
        new Handler().postDelayed(() -> {
            if (weakRef.get() == null)
                return;

            if (weakRef.get().getScrollState() == RecyclerView.SCROLL_STATE_IDLE)
                weakRef.get().smoothScrollBy(-1,-1);

        },2000);


        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true));

        if (viewModel.getWidthReference() == null) {

            View widthReference = requireView().findViewById(R.id.widthReference);
            widthReference.post(() -> {
                viewModel.setWidthReference(widthReference.getWidth());
                chatAdapter.setTextMessageMaxWidth(widthReference.getWidth());
            });

        }else{
            chatAdapter.setTextMessageMaxWidth(viewModel.getWidthReference());
        }

        viewModel.getPinMessageUpdatedLiveData().observe(getViewLifecycleOwner(), (ChatMessage updatedPinnedMessage) -> {

            notifyToUpdatePinnedMessage.notifyToUpdatePinnedMessage(updatedPinnedMessage);
        });

        activityAddOrRemoveLoadingBar.add();

        viewModel.getChatMessageListLiveData().observe(getViewLifecycleOwner(), newMessagesList -> {
            if (newMessagesList.isEmpty())
                return;


            activityAddOrRemoveLoadingBar.remove();

            boolean shouldScrollToBottom = ifNewMessageThenShouldScrollToBottom(chatAdapter.getCurrentList(), newMessagesList);


            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBackGeneralized<>(chatAdapter.getCurrentList(), newMessagesList));
            chatAdapter.submitList(newMessagesList);
            diffResult.dispatchUpdatesTo(chatAdapter);


            if (shouldScrollToBottom)
                chatRecyclerView.smoothScrollToPosition(0);
        });


        chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (viewModel.isLoading())
                    return;

//                if (dx==0 && dy==0)
//                    return;

                if (recyclerView.getLayoutManager() == null)
                    return;

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                int chatMessagesSize = viewModel.getChatMessageList().size();
                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() >= chatMessagesSize - 7) {

                    getMoreData();
                    return;
                }

            }
        });
    }


    private boolean ifNewMessageThenShouldScrollToBottom(List<ChatMessage> currentList, List<ChatMessage> newMessagesList) {
        if ((currentList.isEmpty() && !newMessagesList.isEmpty()) || newMessagesList.isEmpty()){
            return true;
        }

        if (!currentList.isEmpty()){
            if (newMessagesList.get(0) instanceof FakeChatMessageForUser)
                return false;

            String oldMessageId = currentList.get(0).getId();
            String newMessageId = newMessagesList.get(0).getId();
            if (!oldMessageId.equals(newMessageId))
                return true;
        }

        return false;
    }


    public void getMoreData() {
        if (viewModel.isLoading())
            return;

        if (isFirstMessage())
            return;

        viewModel.setLoading(true);

        viewModel.getChatMessageList().add(null);
        viewModel.getChatMessageListLiveData().setValue(viewModel.getChatMessageList());

        viewModel.getMoreData();
    }

    private boolean isFirstMessage() {

        int size = viewModel.getChatMessageList().size() - 1;

        return size >= 0
                && viewModel.getChatMessageList().get(size) != null
                && viewModel.getChatMessageList().get(size).isFirstMessage();
    }

    @Override
    public void messageAdded(ChatMessage chatMessage) {
        if (!isAdded())
            return;

        ChatMessage chatMessageCopy = new ChatMessage(chatMessage);

        if (chatMessageCopy.getMessage().trim().equals(""))
            return;

        chatMessageCopy.setMessageStatus(MessageStatus.WAITING_CONFIRMATION);

        viewModel.getChatMessageList().add(0,chatMessageCopy);
        viewModel.getChatMessageListLiveData().setValue(viewModel.getChatMessageList());

    }


    @Override
    public void onTextMessageClick(View viewForAnimation) {
        if (!isAdded())
            return;

        if (viewForAnimation.getVisibility() == View.VISIBLE){
            animateGone(chatRecyclerView, viewForAnimation);
        }else{
            animateVisible(chatRecyclerView, viewForAnimation);
        }
    }

    private void animateVisible(RecyclerView recyclerView, View myView) {
        TransitionManager.beginDelayedTransition(recyclerView, new ChangeBounds());

        myView.setVisibility(View.VISIBLE);
    }

    private void animateGone(RecyclerView recyclerView, View myView) {
        TransitionManager.beginDelayedTransition(recyclerView, new ChangeBounds());
        myView.setVisibility(View.GONE);
    }

    @Override
    public void animateGoneLayout(View myView) {
        if (!isAdded())
            return;

        TransitionManager.beginDelayedTransition(chatRecyclerView, new ChangeBounds());
        myView.setVisibility(View.GONE);
    }


    @Override
    public void onClickEnabledAnimation(View emojiButton,float LOW_OPACITY) {
        if (!isAdded() || getContext() == null)
            return;

        if (!CheckInternetConnection.isNetworkConnected(getContext())){
            return;
        }

        if (emojiButton.getAlpha() == 1f) {
            emojiButton.setAlpha(LOW_OPACITY);
            return;
        }

        Animation scaleUp = AnimationUtils.loadAnimation(getContext(), R.anim.chat_scale_up_button_emoji);
        Animation scaleDown = AnimationUtils.loadAnimation(getContext(), R.anim.chat_scale_down_button_emoji);

        scaleUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Start scale down animation once the scale up ends
                emojiButton.startAnimation(scaleDown);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        emojiButton.setAlpha(1f);
        emojiButton.startAnimation(scaleUp);
    }

    @Override
    public void onClickUpdateEmojiCount(ChatMessage chatMessage, String emojiClicked) {
        if (!isAdded() || getContext() == null)
            return;

        if (!CheckInternetConnection.isNetworkConnected(getContext())){
            ToastManager.showToast(getContext(), "No internet connection!", Toast.LENGTH_SHORT);
            return;
        }

        viewModel.updateEmojiCount(chatMessage, emojiClicked, FirebaseAuth.getInstance().getUid());
    }

    @Override
    public boolean checkInternetConnection(Context context) {
        if (!CheckInternetConnection.isNetworkConnected(context)){
            ToastManager.showToast(context, "No internet connection!", Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }



    @Override
    public void onLongClickUserMessage(ChatMessage chatMessage) {
        if (!isAdded() || getActivity() == null)
            return;


        View customAlertDialogView = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_chat_message_long_click_options, null);

        // Create the AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(customAlertDialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();



        View copyMessage = customAlertDialogView.findViewById(R.id.copy_message);

        copyMessage.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                copyMessageToClipBoard(chatMessage.getMessage());
                new Handler(Looper.getMainLooper()).postDelayed(alertDialog::dismiss,200);
            }
        });

        View pinMessageLayout = customAlertDialogView.findViewById(R.id.pin_message_layout);
        if (viewModel.getChat().isAdmin(FirebaseAuth.getInstance().getUid()))
            pinMessageLayout.setVisibility(View.VISIBLE);
        else
            pinMessageLayout.setVisibility(View.GONE);

        View pinMessage = customAlertDialogView.findViewById(R.id.pin_message);
        pinMessage.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                viewModel.pinMessage(chatMessage);
                activityAddOrRemoveLoadingBar.add();
                new Handler(Looper.getMainLooper()).postDelayed(alertDialog::dismiss,200);
            }
        });


        View deleteChatMessage = customAlertDialogView.findViewById(R.id.delete_chat_message);

        deleteChatMessage.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                viewModel.deleteChatMessage(chatMessage);

                activityAddOrRemoveLoadingBar.add();
                new Handler(Looper.getMainLooper()).postDelayed(alertDialog::dismiss,200);
            }
        });


        // Show the AlertDialog
        alertDialog.show();

        Window window = alertDialog.getWindow();
        if (window != null) {

            window.setBackgroundDrawableResource(R.drawable.background_with_borders);
        }

    }

    @Override
    public void onLongClickOtherMessage(ChatMessage chatMessage) {
        if (!isAdded() || getActivity() == null)
            return;

        this.copyMessageToClipBoard(chatMessage.getMessage());
    }



    public void copyMessageToClipBoard(String textToCopy){

        if (getActivity() == null)
            return;
        // Get the system clipboard service
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        // Create a ClipData item from the text
        ClipData clip = ClipData.newPlainText("Copied Text", textToCopy);

        // Copy the ClipData item to the clipboard
        clipboard.setPrimaryClip(clip);

        // Show a toast to indicate the text has been copied
        ToastManager.showToast(getContext(), "Το κείμενο αντιγράφηκε", Toast.LENGTH_SHORT);

    }


    public static boolean isRestartCompleted() {
        return restartCompleted;
    }

    public static void setRestartCompleted(boolean restartCompleted) {
        ChatFragment.restartCompleted = restartCompleted;
    }


    @Override
    public void requestFromNotSeenBy(ChatMessage chatMessage, List<String> notSeenByIds) {

        viewModel.requestFromNotSeenBy(chatMessage, notSeenByIds);

    }

}
