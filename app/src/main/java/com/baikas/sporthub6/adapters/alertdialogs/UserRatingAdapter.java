package com.baikas.sporthub6.adapters.alertdialogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.interfaces.onclick.OnClickRate;

import org.jetbrains.annotations.NotNull;

public class UserRatingAdapter extends RecyclerView.Adapter<UserRatingAdapter.ViewHolder>{

    private long rating;
    public final int STARS = 5;
    public final OnClickRate onClickRate;

    public UserRatingAdapter(long rating, OnClickRate onClickRate) {
        this.rating = rating;
        this.onClickRate = onClickRate;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return STARS;
    }


    public void submitNewRating(long rating) {
        this.rating = rating;
    }


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView star;

        public ViewHolder(@NotNull View view) {
            super(view);

            star = view.findViewById(R.id.star);
        }
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public UserRatingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_star_for_rating, viewGroup, false);

        return new UserRatingAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull UserRatingAdapter.ViewHolder viewHolder, final int position) {

        if (position + 1 <= rating)
            viewHolder.star.setImageResource(R.drawable.star_filled);
        else
            viewHolder.star.setImageResource(R.drawable.star_not_filled);


        viewHolder.star.setOnClickListener((v) -> {

            int adapterPosition = viewHolder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {

                onClickRate.onClickRate(adapterPosition + 1);
            }
        });



    }

}
