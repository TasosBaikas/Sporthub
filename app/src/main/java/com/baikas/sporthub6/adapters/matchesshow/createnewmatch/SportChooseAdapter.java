package com.baikas.sporthub6.adapters.matchesshow.createnewmatch;

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

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SportChooseAdapter extends RecyclerView.Adapter<SportChooseAdapter.ViewHolder>{

    private List<Sport> sports;
    private final OnClickListenerPass1<String> chooseSportClick;


    public SportChooseAdapter(List<Sport> sports, OnClickListenerPass1<String> chooseSportClick) {
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
        TextView countBubble;

        ImageView sportImage;
        TextView sportName;


        public ViewHolder(@NotNull View view) {
            super(view);

            cardView = view.findViewById(R.id.sport_card_view);
            countBubble = view.findViewById(R.id.priority_number);

            sportImage = view.findViewById(R.id.sport_image);
            sportName = view.findViewById(R.id.sport_name);

        }
    }


    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public SportChooseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_sport_list_choose_order_of_preference, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull  SportChooseAdapter.ViewHolder viewHolder, final int position) {

        Sport sport = this.sports.get(position);

        viewHolder.countBubble.setVisibility(View.INVISIBLE);

        viewHolder.sportImage.setBackgroundResource(sport.getSportImageId());

        viewHolder.sportName.setText(sport.getGreekName());

        viewHolder.cardView.setOnClickListener((View view) -> chooseSportClick.onClick(sport.getEnglishName()));
    }

}
