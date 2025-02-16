package com.baikas.sporthub6.activities.edits.matchdetails;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.models.constants.MatchDurationConstants;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.viewmodels.edits.matchdetails.EditMatchDurationActivityViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditMatchDurationActivity extends AppCompatActivity {

    EditMatchDurationActivityViewModel viewModel;
    ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_match_duration);

        viewModel = new ViewModelProvider(this).get(EditMatchDurationActivityViewModel.class);

        String matchId = (String)getIntent().getExtras().get("matchId");
        String sport = (String)getIntent().getExtras().get("sport");


        loadingBar = findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);

        viewModel.getMatchById(matchId,sport).observe(this, (Match match) -> {
            loadingBar.setVisibility(View.INVISIBLE);

            viewModel.setMatch(match);

            ProgressBar loadingBar = findViewById(R.id.loadingBar);

            View backButton = findViewById(R.id.back_button);
            backButton.setOnClickListener(v -> this.onBackPressed());

            viewModel.getMessageToUserLiveData().observe(this, (String message) -> {
                if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                    return;

                loadingBar.setVisibility(View.GONE);
                ToastManager.showToast(this,message, Toast.LENGTH_SHORT);
            });

            setLinearLayoutThatHasTheDurations();


            View buttonChange = findViewById(R.id.button_change_match_duration);
            buttonChange.setOnClickListener(v -> {
                loadingBar.setVisibility(View.VISIBLE);

                viewModel.updateMatch(viewModel.getCurrentMatch());
            });

        });


    }

    View previousBackgroundSnoozeChatMessages = null;
    private void setLinearLayoutThatHasTheDurations() {

        LinearLayout linearLayout = findViewById(R.id.layout_duration_options);

        // Get the number of child views in the LinearLayout
        int childCount = linearLayout.getChildCount();

        // Loop through the child views
        for (int i = 0; i < childCount; i++) {
            // Get each child view
            FrameLayout frameLayout = ((FrameLayout) linearLayout.getChildAt(i));

            TextView textView = (TextView) frameLayout.getChildAt(0);

            String matchDurationInitValue = MatchDurationConstants.convertMillisecondsToDuration(viewModel.getCurrentMatch().getMatchDuration());
            if (matchDurationInitValue.equals(textView.getText().toString())){
                previousBackgroundSnoozeChatMessages = textView;
                animateBackgroundTransition(textView, ContextCompat.getColor(this, R.color.transparent), ContextCompat.getColor(this, R.color.green_with_opacity));
            }

            frameLayout.setOnClickListener((View layoutClick) -> {
                String matchDurationInString = MatchDurationConstants.convertMillisecondsToDuration(viewModel.getCurrentMatch().getMatchDuration());
                if (matchDurationInString.equals(textView.getText().toString()))
                    return;


                viewModel.getCurrentMatch().setMatchDuration(MatchDurationConstants.returnInMilliSecondsTheTimeInterval(textView.getText().toString()));

                animateBackgroundTransition(previousBackgroundSnoozeChatMessages, ContextCompat.getColor(this, R.color.green_with_opacity), ContextCompat.getColor(this, R.color.transparent));

                animateBackgroundTransition(textView, ContextCompat.getColor(this, R.color.transparent), ContextCompat.getColor(this, R.color.green_with_opacity));
                previousBackgroundSnoozeChatMessages = textView;
            });
        }
    }

    private void animateBackgroundTransition(View viewToAnimate,int colorFrom,int colorTo){

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(200); // set the duration of the animation
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                viewToAnimate.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

}