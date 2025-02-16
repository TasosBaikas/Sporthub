package com.baikas.sporthub6.fragments.mainpage.chatpreview;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.mainpage.chatpreview.ChatPreviewAdapter;
import com.baikas.sporthub6.adapters.skeleton.SkeletonAdapter;

import com.baikas.sporthub6.models.constants.ChatPreviewTypesConstants;
import com.baikas.sporthub6.models.constants.ConfigurationsConstants;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.interfaces.AttachListeners;
import com.baikas.sporthub6.interfaces.gonext.GoToChatActivity;
import com.baikas.sporthub6.interfaces.NotifyFragmentsToScrollToTop;
import com.baikas.sporthub6.interfaces.RemoveListeners;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.chat.fakemessages.FakeChatForMessage;
import com.baikas.sporthub6.models.updates.DiffCallBackGeneralized;
import com.baikas.sporthub6.viewmodels.mainpage.teamhub.ChatPreviewContentFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatPreviewContentFragment extends Fragment implements RemoveListeners, AttachListeners, NotifyFragmentsToScrollToTop,
GoToChatActivity, OpenUserProfile {


    private ProgressBar loadingBar;
    private ChatPreviewAdapter chatPreviewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChatPreviewContentFragmentViewModel viewModel;
    private GoToChatActivity goToChatActivity;
    private OpenUserProfile showUserProfileDialog;

    private RecyclerView chatPreviewRecyclerView;

    public ChatPreviewContentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            showUserProfileDialog = (OpenUserProfile) context;
        }catch (ClassCastException e){
            throw new RuntimeException("the activity must implement OpenUserProfile");
        }

        try {
            goToChatActivity = (GoToChatActivity) context;
        }catch (ClassCastException e){
            throw new RuntimeException("the activity must implement GoToChatActivity");
        }
    }

    public static ChatPreviewContentFragment newInstance(Bundle bundle) {
        ChatPreviewContentFragment fragment = new ChatPreviewContentFragment();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_preview_content, container, false);
    }


    private final int MAX_LOADING_TIME = ConfigurationsConstants.TIMEOUT_TIME;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(ChatPreviewContentFragmentViewModel.class);


        Bundle bundle = getArguments();
        if (bundle != null){
            viewModel.setChatPreviewType(bundle.getString("chatPreviewTypes"));
        }

        viewModel.loadDataFromServer(viewModel.getChatPreviewType())
                .observe(getViewLifecycleOwner(),unused ->{
            this.attachListeners();
        });


        loadingBar = requireView().findViewById(R.id.loadingBar);

        swipeRefreshLayout = requireView().findViewById(R.id.swipe_refresh_layout);

        if (viewModel.getChatPreviewType().equals(ChatPreviewTypesConstants.RELEVANT_MATCHES)) {

            loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(getContext(), R.color.green));
            swipeRefreshLayout.setColorSchemeResources(R.color.green);
        }else if (viewModel.getChatPreviewType().equals(ChatPreviewTypesConstants.NON_RELEVANT_MATCHES)) {

            loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(getContext(), R.color.yellow));
            swipeRefreshLayout.setColorSchemeResources(R.color.yellow);
        }else if (viewModel.getChatPreviewType().equals(ChatPreviewTypesConstants.PRIVATE_CONVERSATIONS)) {

            loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(getContext(), R.color.blue_dark));
            swipeRefreshLayout.setColorSchemeResources(R.color.blue);
        }

        swipeRefreshLayout.setOnRefreshListener(()->{
            if (!isAdded())
                return;

            viewModel.clearData();
            viewModel.loadDataFromServer( viewModel.getChatPreviewType());
        });


        loadingBar.setVisibility(View.VISIBLE);


        chatPreviewRecyclerView = this.requireView().findViewById(R.id.recycler_view_team_hub_chat_preview);


        chatPreviewRecyclerView.setAdapter(new SkeletonAdapter(ChatPreviewContentFragmentViewModel.MAX_ITEMS_PER_SCROLL));
        WeakReference<RecyclerView> weakRef = new WeakReference<>(chatPreviewRecyclerView);
        new Handler().postDelayed(() -> {
            if (weakRef.get() == null)
                return;

            weakRef.get().smoothScrollBy(1,1);
        },2000);

        chatPreviewRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));


        viewModel.getMainChatsLiveData().observe(getViewLifecycleOwner(), newChatsList -> {

            fromSkeletonToRealAdapter();

            boolean shouldScrollToTop = ifNewChatScrollToTop(chatPreviewAdapter.getCurrentList(), newChatsList);


            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBackGeneralized<>(chatPreviewAdapter.getCurrentList(), newChatsList));
            chatPreviewAdapter.submitList(newChatsList);
            diffResult.dispatchUpdatesTo(chatPreviewAdapter);

            if (shouldScrollToTop)
                chatPreviewRecyclerView.scrollToPosition(0);
        });


        viewModel.getMessageToUserLiveData().observe(getViewLifecycleOwner(),(message -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.GONE);
            ToastManager.showToast(getActivity(), message, Toast.LENGTH_SHORT);
        }));



        chatPreviewRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (viewModel.isLoading())
                    return;

                if (recyclerView.getLayoutManager() == null)
                    return;

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();


                if (dx==0 && dy==0)
                    return;

                int mainMatchesSize = viewModel.getChats().size();
                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() >= mainMatchesSize - 2) {
                    //top of chat!
                    getMoreData();
                }
            }
        });


    }

    private boolean ifNewChatScrollToTop(List<Chat> currentList, List<Chat> newChatsList) {
        if (currentList.isEmpty() || newChatsList.isEmpty())
            return true;

        if (currentList.get(0) == null || newChatsList.get(0) == null || newChatsList.get(0) instanceof FakeChatForMessage)
            return true;

        String oldChat = currentList.get(0).getId();
        String newChat = newChatsList.get(0).getId();
        if (!oldChat.equals(newChat))
            return true;

        return false;
    }


    public void getMoreData() {
        if (chatPreviewRecyclerView.getAdapter() instanceof SkeletonAdapter)
            return;

        viewModel.setLoading(true);

        viewModel.getChats().add(null);
        viewModel.getMainChatsLiveData().setValue(viewModel.getChats());


        viewModel.getMoreData(viewModel.getChatPreviewType());
    }

    public void fromSkeletonToRealAdapter(){
        if (chatPreviewRecyclerView == null)
            return;


        if (chatPreviewAdapter == null)
            chatPreviewAdapter = new ChatPreviewAdapter(new ArrayList<>(),viewModel.getChatPreviewType(),FirebaseAuth.getInstance().getUid(),this, this);

        setAdapterWithDelay(chatPreviewAdapter,0,false);
        setAdapterWithDelay(chatPreviewAdapter,MAX_LOADING_TIME,true);
    }

    private void setAdapterWithDelay(ChatPreviewAdapter chatPreviewAdapter, long loadMoreTime, boolean mandatoryUpdate) {
        WeakReference<ChatPreviewContentFragment> fragmentWeakReference = new WeakReference<>(this);

        new Handler().postDelayed(() -> {

            ChatPreviewContentFragment teamHubFragment = fragmentWeakReference.get();
            if (teamHubFragment == null)
                return;

            if (!viewModel.getChats().isEmpty() || mandatoryUpdate) {

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                loadingBar.setVisibility(View.GONE);

                if (chatPreviewRecyclerView.getAdapter() instanceof SkeletonAdapter) {
                    if (!(chatPreviewRecyclerView.getAdapter() instanceof ChatPreviewAdapter))
                        chatPreviewRecyclerView.setAdapter(chatPreviewAdapter);
                }
            }
        }, loadMoreTime);
    }


    @Override
    public void attachListeners() {
        viewModel.initListenerForChatUpdates(FirebaseAuth.getInstance().getUid(),viewModel.getChatPreviewType());
    }

    @Override
    public void removeListeners() {
        viewModel.removeListeners();
    }

    @Override
    public void scrollToTop() {
        if (chatPreviewRecyclerView == null)
            return;

        chatPreviewRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void goToChatActivity(Chat chat) {

        goToChatActivity.goToChatActivity(chat);

    }

    @Override
    public CompletableFuture<Void> openUserProfile(String userId, @Nullable String sport) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        if (!isAdded() || getContext() == null)
            return completableFuture;

        loadingBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            loadingBar.setVisibility(View.GONE);
            completableFuture.complete(null);
        }, 6000);

        showUserProfileDialog.openUserProfile(userId, sport)
                .whenComplete((o,e) ->{
                    loadingBar.setVisibility(View.GONE);
                    completableFuture.complete(null);
                });

        return completableFuture;
    }
}