package com.baikas.sporthub6.adapters.singupprofile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.listeners.OnClickListenerPass1;
import com.baikas.sporthub6.listeners.OnClickListenerPass2;
import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChooseSportForPriorityAdapter extends RecyclerView.Adapter<ChooseSportForPriorityAdapter.ViewHolder>{
    private final List<UserLevelBasedOnSport> sportsWithLevelAndPriorityList;
    private final OnClickListenerPass1<String> onClick;
    private final OnClickListenerPass2<String,Integer> onSpinnerClick;


    public ChooseSportForPriorityAdapter(Map<String,UserLevelBasedOnSport> sportsWithLevelAndPriorityMap, OnClickListenerPass1<String> onClick, OnClickListenerPass2<String,Integer> onSpinnerClick) {
        this.sportsWithLevelAndPriorityList = new ArrayList<>(sportsWithLevelAndPriorityMap.values());
        this.onClick = onClick;
        this.onSpinnerClick = onSpinnerClick;
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
        TextView priorityBubble;
        CardView sportCardView;
        ImageView sportImage;
        TextView sportName;
        Spinner dropdownMenuSportLevel;

        public ViewHolder(@NotNull View view) {
            super(view);

            // Initialize your views here
            priorityBubble = view.findViewById(R.id.priority_number);
            sportCardView = view.findViewById(R.id.sport_card_view);
            sportImage = view.findViewById(R.id.sport_image);
            sportName = view.findViewById(R.id.sport_name);
            dropdownMenuSportLevel = view.findViewById(R.id.dropdown_menu_sport_level);
        }

    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_sport_list_choose_order_of_preference_and_level, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

        UserLevelBasedOnSport levelAndPrioritySport = sportsWithLevelAndPriorityList.get(position);

        String sport = levelAndPrioritySport.getSportName();
        viewHolder.sportImage.setImageResource(SportConstants.SPORTS_MAP.get(sport).getSportImageId());

        viewHolder.sportName.setText(SportConstants.SPORTS_MAP.get(sport).getGreekName());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(viewHolder.itemView.getContext(),
                R.array.levels_with_short_text, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        viewHolder.dropdownMenuSportLevel.setAdapter(adapter);

        viewHolder.dropdownMenuSportLevel.setSelection(((int) levelAndPrioritySport.getLevel()) - 1);
        viewHolder.dropdownMenuSportLevel.setOnItemSelectedListener(levelSelectedListener(sport));

        if (levelAndPrioritySport.isEnabled()){
            viewHolder.priorityBubble.setVisibility(View.VISIBLE);
            viewHolder.priorityBubble.setText(String.valueOf(levelAndPrioritySport.getPriority()));

            viewHolder.sportCardView.setAlpha(1f);
        }else{
            viewHolder.priorityBubble.setVisibility(View.INVISIBLE);
            viewHolder.sportCardView.setAlpha(0.3f);
        }

        viewHolder.sportCardView.setOnClickListener((v)->onClick.onClick(levelAndPrioritySport.getSportName()));
    }


    private AdapterView.OnItemSelectedListener levelSelectedListener(String sportName) {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                onSpinnerClick.onClick(sportName,position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
    }

}
