package com.baikas.sporthub6.adapters.mainpage.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.listeners.OnClickListenerPass1;
import com.baikas.sporthub6.listeners.OnClickListenerPassNothing;
import com.baikas.sporthub6.models.Sport;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SportAdapter extends RecyclerView.Adapter< RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_EDIT_BUTTON = 0;
    private final int VIEW_TYPE_SPORT = 1;
    private final List<Sport> sportsList;
    private final AtomicReference<String> selectedSport;
    private final OnClickListenerPass1<String> onClickListener;
    private final OnClickListenerPassNothing onEditButtonClick;


    public SportAdapter(List<Sport> sportsList, AtomicReference<String> selectedSport, OnClickListenerPass1<String> onClickListener, OnClickListenerPassNothing onEditButtonClick) {
        this.sportsList = sportsList;
        this.selectedSport = selectedSport;
        this.onClickListener = onClickListener;
        this.onEditButtonClick = onEditButtonClick;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sportsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView sportImage;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            sportImage = (ImageView) view.findViewById(R.id.sport_image);
        }

    }

    public static class EditButtonViewHolder extends RecyclerView.ViewHolder {

        View cardView;
        public EditButtonViewHolder(View view) {
            super(view);

            cardView = view.findViewById(R.id.edit_card_view);
            // Define click listener for the ViewHolder's View

        }

    }


    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        if (viewType == VIEW_TYPE_EDIT_BUTTON){

            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_edit_button_for_sports, viewGroup, false);

            return new EditButtonViewHolder(view);
        }

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_sport, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {

        if (viewHolder instanceof EditButtonViewHolder){
            EditButtonViewHolder editButtonViewHolder = (EditButtonViewHolder) viewHolder;

            editButtonViewHolder.cardView.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    onEditButtonClick.onClick();
                }
            });
            return;
        }

        ViewHolder realViewHolder = (ViewHolder) viewHolder;

        Sport sport = sportsList.get(position);

        realViewHolder.sportImage.setImageResource(sport.getSportImageId());

        if (sport.getEnglishName().equals(selectedSport.get()))
            realViewHolder.sportImage.setAlpha(1f);
        else
            realViewHolder.sportImage.setAlpha(0.1f);


        realViewHolder.sportImage.setOnClickListener(view -> {
            onClickListener.onClick(sport.getEnglishName());
        });

    }



    @Override
    public int getItemViewType(int position) {
        if (sportsList.get(position) == null)
            return VIEW_TYPE_EDIT_BUTTON;

        return VIEW_TYPE_SPORT;
    }


}