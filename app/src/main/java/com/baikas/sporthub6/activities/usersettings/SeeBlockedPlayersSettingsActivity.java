package com.baikas.sporthub6.activities.usersettings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.chat.ChatActivity;
import com.baikas.sporthub6.activities.edits.userprofile.EditUserProfileGeneralActivity;
import com.baikas.sporthub6.adapters.usersettings.SeeBlockedPlayersAdapter;
import com.baikas.sporthub6.alertdialogs.UserProfileDialogFragment;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.interfaces.gonext.GoToChatActivity;
import com.baikas.sporthub6.interfaces.gonext.GoToEditUserProfileGeneral;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.interfaces.onclick.OnClickUnblockPlayer;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.settings.SeeBlockedPlayersSettingsActivityViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SeeBlockedPlayersSettingsActivity extends AppCompatActivity implements OnClickUnblockPlayer, OpenUserProfile, GoToChatActivity, GoToEditUserProfileGeneral {

    SeeBlockedPlayersSettingsActivityViewModel viewModel;
    ProgressBar loadingBar;
    SeeBlockedPlayersAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_see_blocked_players);

        viewModel = new ViewModelProvider(this).get(SeeBlockedPlayersSettingsActivityViewModel.class);


        viewModel.getMessageToUserLiveData().observe(this, (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.GONE);
            ToastManager.showToast(this, message, Toast.LENGTH_SHORT);
        });

        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener((v) -> this.onBackPressed());

        loadingBar = findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);


        viewModel.getBlockedUsers(FirebaseAuth.getInstance().getUid());

        viewModel.getBlockedPlayersMutableLiveData().observe(this, (Map<String, User> blockedUsers) -> {
            loadingBar.setVisibility(View.INVISIBLE);

            RecyclerView recyclerView = findViewById(R.id.recycler_view_show_blocked);
            TextView noBlockedTextView = findViewById(R.id.no_blocked_text_view);

            if (blockedUsers == null || blockedUsers.isEmpty()){
                recyclerView.setVisibility(View.INVISIBLE);
                noBlockedTextView.setVisibility(View.VISIBLE);
                return;
            }else{
                recyclerView.setVisibility(View.VISIBLE);
                noBlockedTextView.setVisibility(View.INVISIBLE);
            }

            if (adapter != null){

                adapter.submitList(new ArrayList<>(blockedUsers.values()));
                adapter.notifyDataSetChanged();
                return;
            }

            adapter = new SeeBlockedPlayersAdapter(new ArrayList<>(blockedUsers.values()), this,this);
            recyclerView.setAdapter(adapter);

            recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        });

    }

    @Override
    public void unBlockPlayer(String userId) {

        loadingBar.setVisibility(View.VISIBLE);

        viewModel.unblockUser(userId).observe(this, (unused -> {

            loadingBar.setVisibility(View.INVISIBLE);
        }));

    }

    @Override
    public CompletableFuture<Void> openUserProfile(String userId, @Nullable String sport) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();


        loadingBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            loadingBar.setVisibility(View.GONE);
            completableFuture.complete(null);
        }, 6000);


        viewModel.getUserById(userId).observe(this, (User user) -> {
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
    public void goToChatActivity(Chat chat) {

        Intent intent = new Intent(this, ChatActivity.class);

        intent.putExtra("chat", (Serializable) chat);

        startActivity(intent);
    }

    @Override
    public void goToEditUserProfileGeneral() {
        Intent intent = new Intent(this, EditUserProfileGeneralActivity.class);

        startActivity(intent);
    }
}