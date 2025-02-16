package com.baikas.sporthub6.adapters.alertdialogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserProfileSportsAdapter extends RecyclerView.Adapter<UserProfileSportsAdapter.ViewHolder>{

    private List<UserLevelBasedOnSport> userLevelBasedOnSports;


    public UserProfileSportsAdapter(List<UserLevelBasedOnSport> userLevelBasedOnSports) {
        this.userLevelBasedOnSports = userLevelBasedOnSports;
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return userLevelBasedOnSports.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView sportImage;
        TextView levelTextView;


        public ViewHolder(@NotNull View view) {
            super(view);


            sportImage = view.findViewById(R.id.sport_image);
            levelTextView = view.findViewById(R.id.sport_level);

        }
    }


    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_sport_for_user_profile, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull  ViewHolder viewHolder, final int position) {

        UserLevelBasedOnSport userLevelBasedOnSport = this.userLevelBasedOnSports.get(position);

        String sportName = userLevelBasedOnSport.getSportName();
        viewHolder.sportImage.setImageResource(SportConstants.SPORTS_MAP.get(sportName).getSportImageId());

        long level = userLevelBasedOnSport.getLevel();
        String userLevel = "Επίπεδο: " + level;
        viewHolder.levelTextView.setText(userLevel);//todo change that to image

    }

}
