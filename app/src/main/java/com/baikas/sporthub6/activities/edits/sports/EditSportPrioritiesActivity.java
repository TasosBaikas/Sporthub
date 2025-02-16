package com.baikas.sporthub6.activities.edits.sports;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.activities.MainActivity;
import com.baikas.sporthub6.adapters.edit.editsport.EditSportPrioritiesAdapter;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.listeners.OnClickListenerPass1;
import com.baikas.sporthub6.models.MatchFilter;
import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;
import com.baikas.sporthub6.viewmodels.edits.editsport.EditSportPrioritiesActivityViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditSportPrioritiesActivity extends AppCompatActivity implements OnClickListenerPass1<Integer> {

    EditSportPrioritiesActivityViewModel viewModel;
    EditSportPrioritiesAdapter adapter;
    ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sport_priorities);

        viewModel = new ViewModelProvider(this).get(EditSportPrioritiesActivityViewModel.class);

        View layoutBack = findViewById(R.id.layout_back);
        layoutBack.setOnClickListener((v) -> onBackPressed());
        View backImage = findViewById(R.id.back_image);
        backImage.setOnClickListener((v) -> onBackPressed());

        if (getIntent().getExtras() != null && getIntent().getExtras().get("matchFilter") != null)
            viewModel.setMatchFilter((MatchFilter) getIntent().getExtras().get("matchFilter"));


        loadingBar = findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);

        viewModel.getErrorMessageLiveData().observe(this, (String errorMessage) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.GONE);
            ToastManager.showToast(this,errorMessage,Toast.LENGTH_SHORT);
        });



        viewModel.getSportListWithPrioritiesLiveData().observe(this,(List<UserLevelBasedOnSport> sportPriorities) -> {

            loadingBar.setVisibility(View.GONE);

            long chosenSports = sportPriorities.stream()
                    .filter((sportPriority)-> sportPriority.isEnabled())
                    .count();

            viewModel.setChosenSports((int) chosenSports);

            adapter = new EditSportPrioritiesAdapter(sportPriorities,this);
            RecyclerView recyclerView = this.findViewById(R.id.recycler_view_edit_sports);

            recyclerView.setLayoutManager(new GridLayoutManager(this,3, LinearLayoutManager.VERTICAL,false));
            recyclerView.setAdapter(adapter);


            View changePreferencesButton = findViewById(R.id.change_preferences);
            changePreferencesButton.setOnClickListener(new Prevent2ClicksListener(1800) {
                @Override
                public void onClickExecuteCode(View v) {

                    if (viewModel.getChosenSportsSize() <= 0){
                        ToastManager.showToast(EditSportPrioritiesActivity.this,"Δεν έχετε επιλέξει κάποιο άθλημα", Toast.LENGTH_SHORT);
                        return;
                    }

                    if (!CheckInternetConnection.isNetworkConnected(EditSportPrioritiesActivity.this)){
                        viewModel.getErrorMessageLiveData().postValue("No internet connection!");
                        return;
                    }

                    loadingBar.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(() -> loadingBar.setVisibility(View.GONE),7000);

                    viewModel.saveChanges().observe(EditSportPrioritiesActivity.this,(unused -> {
                        loadingBar.setVisibility(View.GONE);

                        Intent intent = new Intent(EditSportPrioritiesActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        if (viewModel.getMatchFilter() != null)
                            intent.putExtra("matchFilter", viewModel.getMatchFilter());

                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                        finish();

                    }));
                }
            });

        });

    }

    @Override
    public void onClick(Integer sportSelected) {
        List<UserLevelBasedOnSport> userLevelBasedOnSportList = viewModel.getSportListWithPrioritiesLiveData().getValue();

        if (userLevelBasedOnSportList == null || adapter == null)
            return;

        UserLevelBasedOnSport levelAndPriorityBySport = userLevelBasedOnSportList.get(sportSelected);

        int sportPriority;
        if (levelAndPriorityBySport.isEnabled()){
            minus1PreviousPrioritiesOf(levelAndPriorityBySport.getPriority(),userLevelBasedOnSportList);

            sportPriority = viewModel.getChosenSportsSize() - 1;
            levelAndPriorityBySport.setPriority(-1);
        }else{
            sportPriority = viewModel.getChosenSportsSize() + 1;
            levelAndPriorityBySport.setPriority(sportPriority);
        }
        viewModel.setChosenSports(sportPriority);

        adapter.notifyDataSetChanged();
    }

    private void minus1PreviousPrioritiesOf(long basePriority, List<UserLevelBasedOnSport> userLevelBasedOnSportList) {
        if (basePriority == -1)
            return;

        for (UserLevelBasedOnSport priorityBySport:userLevelBasedOnSportList) {
            if (priorityBySport.getPriority() <= basePriority)
                continue;

            long sportPriority = priorityBySport.getPriority();
            priorityBySport.setPriority(sportPriority - 1);
        }
    }
}