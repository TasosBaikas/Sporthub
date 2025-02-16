package com.baikas.sporthub6.activities.matches;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.chat.ChatActivity;
import com.baikas.sporthub6.activities.edits.userprofile.EditUserProfileGeneralActivity;
import com.baikas.sporthub6.alertdialogs.UserProfileDialogFragment;
import com.baikas.sporthub6.interfaces.gonext.GoToChatActivity;
import com.baikas.sporthub6.interfaces.gonext.GoToEditUserProfileGeneral;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.matches.seewhorequested.SeeWhoRequestedActivityViewModel;
import com.baikas.sporthub6.viewpageradapters.ViewPager2SeeWhoRequestedAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SeeWhoRequestedActivity extends AppCompatActivity implements OpenUserProfile, GoToEditUserProfileGeneral, GoToChatActivity {

    ProgressBar loadingBar;
    SeeWhoRequestedActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_who_requested);

        viewModel = new ViewModelProvider(this).get(SeeWhoRequestedActivityViewModel.class);

        String matchId = (String) getIntent().getSerializableExtra("matchId");
        String sport = (String) getIntent().getSerializableExtra("sport");

        viewModel.setSport(sport);

        loadingBar = findViewById(R.id.loadingBar);

        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((view) -> this.onBackPressed());



        ViewPager2 viewPager = findViewById(R.id.view_pager2_having_fragments_of_see_who_requested);

        TabLayout tabLayout = findViewById(R.id.tab_layout_see_who_requested);


        Bundle bundle = new Bundle();
        bundle.putString("matchId",matchId);
        bundle.putString("sport",sport);
        viewPager.setAdapter(new ViewPager2SeeWhoRequestedAdapter(this,bundle));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0){
                tab.setText("Εκκρεμεις αιτησεις");
                tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.green));
            }else if (position == 1){
                tab.setText("Αγνοηθηκαν");
            }
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                try {
                    int position = tab.getPosition();

                    switch (position) {
                        case 0:
                            tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(SeeWhoRequestedActivity.this, R.color.green));
                            break;
                        case 1:
                            tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(SeeWhoRequestedActivity.this, R.color.red));
                            break;
                    }
                }catch (Exception e){}
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optional
            }
        });


    }

    @Override
    public void onBackPressed() {

        if (isTaskRoot()) {

            Intent intent = new Intent(this, MatchShowActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            intent.putExtra("sport",viewModel.getSport());

            startActivity(intent);
            finish();
            return;
        }

        super.onBackPressed();
    }


    @Override
    public CompletableFuture<Void> openUserProfile(String requesterId, @Nullable String sport) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();


        if (sport == null || sport.isEmpty()) {
            loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(this, R.color.green));
        }else{
            loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(this, SportConstants.SPORTS_MAP.get(sport).getSportColor()));
        }

        loadingBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            loadingBar.setVisibility(View.GONE);
            completableFuture.complete(null);
        }, 6000);


        viewModel.getUserById(requesterId).observe(this, (User user) -> {
            loadingBar.setVisibility(View.GONE);

            UserProfileDialogFragment dialogFragment = new UserProfileDialogFragment();

            Bundle bundle = new Bundle();
            bundle.putSerializable("user", (Serializable) user);
            dialogFragment.setArguments(bundle);

            dialogFragment.show(getSupportFragmentManager(), "UserProfileDialogFragment");

            completableFuture.complete(null);
        });

        return completableFuture;
    }


    @Override
    public void goToEditUserProfileGeneral() {
        Intent intent = new Intent(this, EditUserProfileGeneralActivity.class);

        startActivity(intent);
    }

    @Override
    public void goToChatActivity(Chat chat) {

        Intent intent = new Intent(this, ChatActivity.class);

        intent.putExtra("chat", (Serializable) chat);

        startActivity(intent);
    }
}