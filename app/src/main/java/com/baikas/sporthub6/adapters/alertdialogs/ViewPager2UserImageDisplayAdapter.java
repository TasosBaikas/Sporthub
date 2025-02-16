package com.baikas.sporthub6.adapters.alertdialogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.interfaces.onclick.OnClickLeftSide;
import com.baikas.sporthub6.interfaces.onclick.OnClickRightSide;
import com.baikas.sporthub6.models.PhotoDetails;
import com.baikas.sporthub6.models.user.UserImages;

import org.jetbrains.annotations.NotNull;

public class ViewPager2UserImageDisplayAdapter extends RecyclerView.Adapter<ViewPager2UserImageDisplayAdapter.ViewHolder> {

    private final UserImages userImages;
    private final OnClickLeftSide onClickLeftSide;
    private final OnClickRightSide onClickRightSide;

    public ViewPager2UserImageDisplayAdapter(UserImages userImages, OnClickLeftSide onClickLeftSide, OnClickRightSide onClickRightSide) {
        this.userImages = userImages;
        this.onClickLeftSide = onClickLeftSide;
        this.onClickRightSide = onClickRightSide;
    }

    public void newUserImagesUpdatePhotos(UserImages userImages) {

        this.userImages.getPhotoDetails().clear();
        this.userImages.getPhotoDetails().addAll(userImages.getPhotoDetails());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View leftSide;
        ImageView image;
        View rightSide;

        public ViewHolder(@NotNull View view) {
            super(view);

            leftSide = view.findViewById(R.id.left_side);
            image = view.findViewById(R.id.user_image);
            rightSide = view.findViewById(R.id.right_side);
        }
    }


    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public ViewPager2UserImageDisplayAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_show_user_image, viewGroup, false);

        return new ViewPager2UserImageDisplayAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPager2UserImageDisplayAdapter.ViewHolder holder, int position) {

        PhotoDetails photoDetails = userImages.getPhotoDetails().get(position);

        holder.leftSide.setOnClickListener((v) -> {
            this.onClickLeftSide.onClickLeftSide();
        });

        holder.rightSide.setOnClickListener((v) -> {
            this.onClickRightSide.onClickRightSide();
        });

        SetImageWithGlide.setImageWithGlideOrDefaultImage(photoDetails.getPhotoDownloadUrl(), holder.image, holder.itemView.getContext());
    }

    @Override
    public int getItemCount() {
        return userImages.getPhotoDetails().size();
    }
}
