package com.baikas.sporthub6.activities.edits.matchdetails;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.viewmodels.edits.matchdetails.EditPermitableLevelsViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditPermitableLevels extends AppCompatActivity {

    EditPermitableLevelsViewModel viewModel;
    ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_permitable_levels);

        viewModel = new ViewModelProvider(this).get(EditPermitableLevelsViewModel.class);

        String matchId = (String)getIntent().getExtras().get("matchId");
        String sport = (String)getIntent().getExtras().get("sport");

        loadingBar = findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);

        viewModel.getMatchById(matchId,sport).observe(this,(Match match) -> {

            loadingBar.setVisibility(View.INVISIBLE);

            viewModel.setMatch(match);

            viewModel.setFromLevel(Math.toIntExact(match.getLevels().get(0)));
            viewModel.setToLevel(Math.toIntExact(match.getLevels().get(match.getLevels().size() - 1)));


            ProgressBar loadingBar = findViewById(R.id.loadingBar);

            viewModel.getMessageToUserLiveData().observe(this, (String message) -> {
                if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                    return;

                loadingBar.setVisibility(View.GONE);
                ToastManager.showToast(this,message, Toast.LENGTH_SHORT);
            });


            View backButton = findViewById(R.id.back_button);
            backButton.setOnClickListener(v -> this.onBackPressed());

            setPermitableLevelsUi();


            View buttonChange = findViewById(R.id.button_change_permitable_levels);
            buttonChange.setOnClickListener(v -> {
                loadingBar.setVisibility(View.VISIBLE);

                Match matchForUpdate = viewModel.getMatch();

                int levelFrom = viewModel.getFromLevel();
                int levelTo = viewModel.getToLevel();
                List<Long> levels = new ArrayList<>();
                for (int i = 0; i <= levelTo - levelFrom; i++) {
                    levels.add((long) (levelFrom + i));
                }
                matchForUpdate.setLevels(levels);

                viewModel.updateMatch(matchForUpdate);
            });

        });

    }

    private void setPermitableLevelsUi() {


        View downLevelFrom = findViewById(R.id.down_level_from);
        View upLevelFrom = findViewById(R.id.up_level_from);

        View downLevelTo = findViewById(R.id.down_level_to);
        View upLevelTo = findViewById(R.id.up_level_to);

        TextView levelFrom = findViewById(R.id.level_from);
        levelFrom.setText("Επίπεδο " + viewModel.getFromLevel());

        downLevelFrom.setOnClickListener((View viewClicked) -> {
            int newLevel = downVote(viewModel.getFromLevel());
            viewModel.setFromLevel(newLevel);

            String text = levelFrom.getText().toString();
            String newText = text.substring(0,Math.max(text.length() - 1,0)) + newLevel;
            levelFrom.setText(newText);
        });

        upLevelFrom.setOnClickListener((View viewClicked) -> {
            if (viewModel.getFromLevel() >= viewModel.getToLevel())
                return;

            int newLevel = upVote(viewModel.getFromLevel());
            viewModel.setFromLevel(newLevel);

            String text = levelFrom.getText().toString();
            String newText = text.substring(0,Math.max(text.length() - 1,0)) + newLevel;
            levelFrom.setText(newText);
        });


        TextView levelTo = findViewById(R.id.level_to);
        levelTo.setText("Επίπεδο " + viewModel.getToLevel());

        downLevelTo.setOnClickListener((View viewClicked) -> {
            if (viewModel.getToLevel() <= viewModel.getFromLevel())
                return;

            int newLevel = downVote(viewModel.getToLevel());
            viewModel.setToLevel(newLevel);

            String text = levelTo.getText().toString();
            String newText = text.substring(0,Math.max(text.length() - 1,0)) + newLevel;
            levelTo.setText(newText);
        });

        upLevelTo.setOnClickListener((View viewClicked) -> {

            int newLevel = upVote(viewModel.getToLevel());
            viewModel.setToLevel(newLevel);

            String text = levelTo.getText().toString();
            String newText = text.substring(0,Math.max(text.length() - 1,0)) + newLevel;
            levelTo.setText(newText);

        });

    }


    private int downVote(int level) {
        if (level <= 1)
            return level;

        return level - 1;
    }

    private int upVote(int level) {
        if (level >= Match.MAX_LEVEL)
            return level;

        return level + 1;
    }

}