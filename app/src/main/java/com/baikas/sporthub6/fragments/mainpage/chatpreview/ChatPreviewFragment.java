package com.baikas.sporthub6.fragments.mainpage.chatpreview;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.interfaces.BeNotifiedByActivity;
import com.baikas.sporthub6.interfaces.GetFromActivity;
import com.baikas.sporthub6.interfaces.NotifyToUpdateImage;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.R;
import com.baikas.sporthub6.models.constants.ChatPreviewTypesConstants;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.mainpage.teamhub.ChatPreviewFragmentViewModel;
import com.baikas.sporthub6.viewpageradapters.ViewPager2ChatPreviewFragmentsAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;

@AndroidEntryPoint
public class ChatPreviewFragment extends Fragment implements BeNotifiedByActivity<List<Chat>>, NotifyToUpdateImage {

    ChatPreviewFragmentViewModel viewModel;
    GetFromActivity<List<Chat>> getFromActivity;

    ViewPager2 viewPager;
    ImageView userImageButton;
    private final List<WeakReference<View>> weakReferencesCustomViewsOfTabLayout = new ArrayList<>();


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            getFromActivity = (GetFromActivity<List<Chat>>) context;
        }catch (ClassCastException e){
            throw new RuntimeException("Activity must implement getFromActivity");
        }
    }

    public ChatPreviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_preview, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();


        List<Chat> chatMessageNotSeen = getFromActivity.getDataFromActivity();
        viewModel.allChatsNotSeen(chatMessageNotSeen);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(ChatPreviewFragmentViewModel.class);

        if (getActivity() == null || getContext() == null)
            return;


        viewPager = requireView().findViewById(R.id.view_pager2_fragments_team_hub);
        TabLayout tabLayout = requireView().findViewById(R.id.tab_layout_nav_between_chat_sections);


        viewPager.setSaveEnabled(false);
        if (viewPager.getAdapter() == null)
            viewPager.setAdapter(new ViewPager2ChatPreviewFragmentsAdapter(getActivity(), viewModel.getFragmentsBundles()));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {

            View customTab = LayoutInflater.from(getContext()).inflate(R.layout.tab_layout_with_bubbles, null);
            TextView title = customTab.findViewById(R.id.tabTitle);
            ImageView chatCloudImage = customTab.findViewById(R.id.chat_cloud_image);

            TextView chatMembers = customTab.findViewById(R.id.chat_cloud_relevant_chats_members);

            title.setText(ChatPreviewTypesConstants.allTypesInGreekList.get(position));


            if (position == 0) {
                title.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
                tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(getContext(), R.color.green));
                chatMembers.setTextColor(ContextCompat.getColor(getContext(), R.color.white));

                chatCloudImage.setImageResource(R.drawable.chat_cloud_green);
            }else if (position == 1){
                title.setTextColor(ContextCompat.getColor(getContext(), R.color.white_or_black_with_opacity_mode)); // replace 'defaultColor'

                chatMembers.setTextColor(ContextCompat.getColor(getContext(), R.color.pink_dark));
                chatCloudImage.setImageResource(R.drawable.chat_cloud_yellow);
            }else if (position == 2){
                title.setTextColor(ContextCompat.getColor(getContext(), R.color.white_or_black_with_opacity_mode)); // replace 'defaultColor'
                chatMembers.setTextColor(ContextCompat.getColor(getContext(), R.color.white));

                chatCloudImage.setImageResource(R.drawable.chat_cloud_blue);
            }


            try{
                if (weakReferencesCustomViewsOfTabLayout.get(position) == null)
                    throw new NullPointerException();

                weakReferencesCustomViewsOfTabLayout.set(position,new WeakReference<>(customTab));
            }catch (IndexOutOfBoundsException | NullPointerException e){
                weakReferencesCustomViewsOfTabLayout.add(new WeakReference<>(customTab));
            }


            tab.setCustomView(customTab);

        }).attach();


        // Adding a tab selected listener to change the text color dynamically
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (getContext() == null)
                    return;

                try {
                    int position = tab.getPosition();
                    View customTab = tab.getCustomView();
                    TextView title = customTab.findViewById(R.id.tabTitle);

                    switch (position) {
                        case 0:
                            title.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
                            tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(getContext(), R.color.green));
                            break;
                        case 1:
                            title.setTextColor(ContextCompat.getColor(getContext(), R.color.yellow));
                            tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(getContext(), R.color.yellow));
                            break;
                        case 2:
                            title.setTextColor(ContextCompat.getColor(getContext(), R.color.blue));
                            tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(getContext(), R.color.blue));
                            break;
                    }
                }catch (Exception e){}
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                try {
                    View customTab = tab.getCustomView();
                    TextView title = customTab.findViewById(R.id.tabTitle);
                    title.setTextColor(ContextCompat.getColor(getContext(), R.color.white_or_black_with_opacity_mode)); // replace 'defaultColor'
                }catch (Exception e){}
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optional
            }
        });


        viewModel.getMessagesNotSeenRelevantChatsLiveData().observe(getViewLifecycleOwner(), (Integer newMessages) -> {

            View customTab = weakReferencesCustomViewsOfTabLayout.get(0).get();
            if (customTab == null)
                return;

            View chatCloudLayout = customTab.findViewById(R.id.chat_cloud_relevant_chats_layout);
            TextView chatCloudMembersTextView = customTab.findViewById(R.id.chat_cloud_relevant_chats_members);

            this.layoutChatCloud(chatCloudLayout,chatCloudMembersTextView,newMessages);

        });

        viewModel.getMessagesNotSeenIrrelevantChatsLiveData().observe(getViewLifecycleOwner(), (Integer newMessages) -> {

            View customTab = weakReferencesCustomViewsOfTabLayout.get(1).get();
            if (customTab == null)
                return;

            View chatCloudLayout = customTab.findViewById(R.id.chat_cloud_relevant_chats_layout);
            TextView chatCloudMembersTextView = customTab.findViewById(R.id.chat_cloud_relevant_chats_members);

            this.layoutChatCloud(chatCloudLayout,chatCloudMembersTextView,newMessages);
        });


        viewModel.getMessagesNotSeenPrivateChatsLiveData().observe(getViewLifecycleOwner(), (Integer newMessages) -> {

            View customTab = weakReferencesCustomViewsOfTabLayout.get(2).get();
            if (customTab == null)
                return;

            View chatCloudLayout = customTab.findViewById(R.id.chat_cloud_relevant_chats_layout);
            TextView chatCloudMembersTextView = customTab.findViewById(R.id.chat_cloud_relevant_chats_members);

            this.layoutChatCloud(chatCloudLayout,chatCloudMembersTextView,newMessages);
        });


        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        userImageButton = requireView().findViewById(R.id.user_image_button);

        userImageButton.setOnClickListener((imageButton -> drawerLayout.openDrawer(GravityCompat.END)));

        viewModel.getUserById(FirebaseAuth.getInstance().getUid()).observe(getViewLifecycleOwner(),((User user) -> {
            SetImageWithGlide.setImageWithGlideOrDefaultImage(user.getProfileImageUrl(),userImageButton,getContext());
        }));


    }


    private void layoutChatCloud(View layout, TextView chatTextView, int newMessages){
        if (layout == null)
            return;

        if (newMessages == 0){
            layout.setVisibility(View.INVISIBLE);
            return;
        }

        layout.setVisibility(View.VISIBLE);
        if (newMessages <= 3)
            chatTextView.setText(String.valueOf(newMessages));
        else
            chatTextView.setText("+3");

    }


    @Override
    public void notifiedByActivity(List<Chat> listOfData) {
        viewModel.allChatsNotSeen(listOfData);
    }

    @Override
    public void notifyToUpdateImage(Uri photoUri) {
        if (userImageButton == null || photoUri == null)
            return;

        userImageButton.setImageURI(photoUri);
    }


}