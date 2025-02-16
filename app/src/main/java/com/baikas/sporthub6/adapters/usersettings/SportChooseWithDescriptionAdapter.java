package com.baikas.sporthub6.adapters.usersettings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.listeners.OnClickListenerPass1;
import com.baikas.sporthub6.models.Sport;
import com.baikas.sporthub6.models.TerrainAddress;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SportChooseWithDescriptionAdapter extends RecyclerView.Adapter<SportChooseWithDescriptionAdapter.ViewHolder>{

    private final Map<String,List<TerrainAddress>> allSportAddressesTerrain;
    private final List<Sport> sports;
    private final OnClickListenerPass1<String> chooseSportClick;


    public SportChooseWithDescriptionAdapter(Map<String,List<TerrainAddress>> allSportAddressesTerrain,List<Sport> sports, OnClickListenerPass1<String> chooseSportClick) {
        this.allSportAddressesTerrain = allSportAddressesTerrain;
        this.sports = sports;
        this.chooseSportClick = chooseSportClick;
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sports.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        View cardView;
        TextView saveTerrainNumberTextView;
        ImageView sportImage;
        TextView sportName;


        public ViewHolder(@NotNull View view) {
            super(view);

            saveTerrainNumberTextView = view.findViewById(R.id.saved_terrain_number);
            cardView = view.findViewById(R.id.sport_card_view);

            sportImage = view.findViewById(R.id.sport_image);
            sportName = view.findViewById(R.id.sport_name);

        }
    }


    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public SportChooseWithDescriptionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_sport_list_for_addresses, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull SportChooseWithDescriptionAdapter.ViewHolder viewHolder, final int position) {

        Sport sport = this.sports.get(position);
        List<TerrainAddress> terrainAddressList = this.allSportAddressesTerrain.get(sport.getEnglishName());
        if (terrainAddressList == null)
            terrainAddressList = new ArrayList<>();

        int count = terrainAddressList.size();
        if (count == 1){
            viewHolder.saveTerrainNumberTextView.setText("1 Γήπεδο");
        }else{
            viewHolder.saveTerrainNumberTextView.setText(count + " Γήπεδα");
        }


        viewHolder.sportImage.setBackgroundResource(sport.getSportImageId());

        viewHolder.sportName.setText(sport.getGreekName());

        viewHolder.cardView.setOnClickListener((View view) -> chooseSportClick.onClick(sport.getEnglishName()));
    }


}
