package com.baikas.sporthub6.activities.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.loginsignup.LoginActivity;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.interfaces.gonext.GoToChatActivity;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.viewmodels.chat.RedirectToChatActivityViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RedirectToChatActivity extends AppCompatActivity implements GoToChatActivity {

    RedirectToChatActivityViewModel viewModel;
    ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redirect_to_chat);

        if (FirebaseAuth.getInstance().getCurrentUser() == null || FirebaseAuth.getInstance().getUid() == null){
            Intent intent = new Intent(this, LoginActivity.class);

            startActivity(intent);
            finish();
            return;
        }


        viewModel = new ViewModelProvider(this).get(RedirectToChatActivityViewModel.class);

        String chatId = (String) getIntent().getExtras().get("chatId");
        viewModel.setChatId(chatId);

        loadingBar = findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);



        viewModel.getChatById(chatId).observe(this, (Chat chat) -> {

           this.goToChatActivity(chat);
        });

        if (!CheckInternetConnection.isNetworkConnected(this))
            viewModel.getErrorLiveData().setValue("No internet connection!");

        View retryButton = findViewById(R.id.button_retry);
        retryButton.setVisibility(View.GONE);

        viewModel.getErrorLiveData().observe(this, (errorMessage -> {
            loadingBar.setVisibility(View.INVISIBLE);
            retryButton.setVisibility(View.VISIBLE);

            ToastManager.showToast(this, errorMessage, Toast.LENGTH_SHORT);
        }));


        retryButton.setOnClickListener((v) -> {
            loadingBar.setVisibility(View.VISIBLE);

            viewModel.getChatById(chatId).observe(this, (Chat chat)->{

                this.goToChatActivity(chat);
            });

        });

    }

    @Override
    public void goToChatActivity(Chat chat) {
        Intent intent = new Intent(this, ChatActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra("chat", (Serializable) chat);

        startActivity(intent);
        finish();
    }
}