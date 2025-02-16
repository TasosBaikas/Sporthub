package com.baikas.sporthub6.adapters.edit.editsport;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.listeners.OnClickListenerPass1;
import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EditSportPrioritiesAdapter extends RecyclerView.Adapter<EditSportPrioritiesAdapter.ViewHolder>{
    private final List<UserLevelBasedOnSport> sportsWithLevelAndPriorityList;
    private final OnClickListenerPass1<Integer> onClick;


    public EditSportPrioritiesAdapter(List<UserLevelBasedOnSport> sportsWithLevelAndPriorityList, OnClickListenerPass1<Integer> onClick) {
        this.sportsWithLevelAndPriorityList = sportsWithLevelAndPriorityList;
        this.onClick = onClick;
    }



    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sportsWithLevelAndPriorityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView priorityBubble;
        CardView sportCardView;
        ImageView sportImage;
        TextView sportName;

        public ViewHolder(@NotNull View view) {
            super(view);

            // Initialize your views here
            priorityBubble = view.findViewById(R.id.priority_number);
            sportCardView = view.findViewById(R.id.sport_card_view);
            sportImage = view.findViewById(R.id.sport_image);
            sportName = view.findViewById(R.id.sport_name);
        }

    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public EditSportPrioritiesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_sport_list_choose_order_of_preference, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull EditSportPrioritiesAdapter.ViewHolder viewHolder, final int position) {

        UserLevelBasedOnSport levelAndPrioritySport = sportsWithLevelAndPriorityList.get(position);

        String sport = levelAndPrioritySport.getSportName();
        viewHolder.sportImage.setImageResource(SportConstants.SPORTS_MAP.get(sport).getSportImageId());

        viewHolder.sportName.setText(SportConstants.SPORTS_MAP.get(sport).getGreekName());

        if (levelAndPrioritySport.isEnabled()){
            viewHolder.priorityBubble.setVisibility(View.VISIBLE);
            viewHolder.priorityBubble.setText(String.valueOf(levelAndPrioritySport.getPriority()));

            viewHolder.sportCardView.setAlpha(1f);
        }else{
            viewHolder.priorityBubble.setVisibility(View.INVISIBLE);
            viewHolder.sportCardView.setAlpha(0.3f);
        }

        viewHolder.sportCardView.setOnClickListener((v)->onClick.onClick(position));
    }


}
