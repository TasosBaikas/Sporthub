package com.baikas.sporthub6.adapters.navprofile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.listeners.OnClickListenerPass2;
import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SportDisplayAdapter extends RecyclerView.Adapter<SportDisplayAdapter.ViewHolder>{


    private final List<UserLevelBasedOnSport> sportsWithLevelAndPriorityList;
    private final OnClickListenerPass2<String,Integer> onClickUpdateLevel;


    public SportDisplayAdapter(List<UserLevelBasedOnSport> sportsWithLevelAndPriorityList, OnClickListenerPass2<String,Integer> onClickUpdateLevel) {
        this.sportsWithLevelAndPriorityList = sportsWithLevelAndPriorityList;
        this.onClickUpdateLevel = onClickUpdateLevel;
    }



    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sportsWithLevelAndPriorityList.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView sportImage;
        Spinner dropdownSportLevelSpinner;

        public ViewHolder(@NotNull View view) {
            super(view);

            // Image
            sportImage = view.findViewById(R.id.sport_image);

            dropdownSportLevelSpinner = view.findViewById(R.id.dropdown_sport_level);
        }

    }


    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public SportDisplayAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_sport_display_for_nav_fragment, viewGroup, false);

        return new SportDisplayAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull SportDisplayAdapter.ViewHolder viewHolder, final int position) {

        UserLevelBasedOnSport userLevelBasedOnSport = sportsWithLevelAndPriorityList.get(position);

        int sportImageResource = SportConstants.SPORTS_MAP.get(userLevelBasedOnSport.getSportName()).getSportImageId();
        viewHolder.sportImage.setImageResource(sportImageResource);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(viewHolder.itemView.getContext(),
                R.array.levels, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        viewHolder.dropdownSportLevelSpinner.setAdapter(adapter);

        long sportLevel = userLevelBasedOnSport.getLevel() - 1;
        viewHolder.dropdownSportLevelSpinner.setSelection((int) sportLevel);
        viewHolder.dropdownSportLevelSpinner.setOnItemSelectedListener(levelSelectedListener(userLevelBasedOnSport.getSportName()));

    }



    private AdapterView.OnItemSelectedListener levelSelectedListener(String sportName) {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onClickUpdateLevel.onClick(sportName,position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
    }

}
