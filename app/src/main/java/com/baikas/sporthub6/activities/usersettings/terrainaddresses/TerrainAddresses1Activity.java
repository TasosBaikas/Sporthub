package com.baikas.sporthub6.activities.usersettings.terrainaddresses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.usersettings.SportChooseWithDescriptionAdapter;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.listeners.OnClickListenerPass1;
import com.baikas.sporthub6.models.Sport;
import com.baikas.sporthub6.viewmodels.settings.terrainaddresses.TerrainAddresses1ActivityViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TerrainAddresses1Activity extends AppCompatActivity implements OnClickListenerPass1<String> {

    TerrainAddresses1ActivityViewModel viewModel;
    SportChooseWithDescriptionAdapter adapter;

    @Override
    protected void onRestart() {
        super.onRestart();

        viewModel.loadTerrainDataLiveData().observe(this,(unused) -> {
            if (adapter == null)
                return;

            adapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terrain_addresses1);

        viewModel = new ViewModelProvider(this).get(TerrainAddresses1ActivityViewModel.class);



        View backButton = findViewById(R.id.arrow_back);
        backButton.setOnClickListener((View view) -> onBackPressed());

        RecyclerView recyclerView = findViewById(R.id.recycler_view_choose_sport);

        List<Sport> sportList = new ArrayList<>(SportConstants.SPORTS_MAP.values());

        adapter = new SportChooseWithDescriptionAdapter(viewModel.allSportTerrainAddresses(),sportList,this);

        recyclerView.setAdapter(adapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);


        viewModel.loadTerrainDataLiveData().observe(this,(unused) -> {

            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onClick(String sportNameEnglish) {
        Intent intent = new Intent(this, TerrainAddresses2Activity.class);

        intent.putExtra("sportNameEnglish",sportNameEnglish);

        startActivity(intent);
    }
}