package com.baikas.sporthub6.adapters.edit.userprofile;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.interfaces.onclick.OnImageClickOpenAlertDialog;
import com.baikas.sporthub6.interfaces.onclick.OnClickRotateImagesLessPopular;
import com.baikas.sporthub6.interfaces.onclick.OnClickRotateImagesMorePopular;
import com.baikas.sporthub6.models.PhotoDetails;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UserProfileImagesForEditAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<PhotoDetails> photoDetailsList;
    private final int VIEW_TYPE_LOADING = -1;
    private final int VIEW_TYPE_IMAGE = 1;
    private final String yourId;
    private final OnClickRotateImagesMorePopular onClickRotateImagesMorePopular;
    private final OnClickRotateImagesLessPopular onClickRotateImagesLessPopular;
    private final OnImageClickOpenAlertDialog onImageClickOpenAlertDialog;


    public UserProfileImagesForEditAdapter(List<PhotoDetails> photoDetailsList, String yourId, OnClickRotateImagesMorePopular onClickRotateImagesMorePopular, OnClickRotateImagesLessPopular onClickRotateImagesLessPopular, OnImageClickOpenAlertDialog onImageClickOpenAlertDialog) {
        this.photoDetailsList = photoDetailsList;
        this.yourId = yourId;
        this.onClickRotateImagesMorePopular = onClickRotateImagesMorePopular;
        this.onClickRotateImagesLessPopular = onClickRotateImagesLessPopular;

        this.onImageClickOpenAlertDialog = onImageClickOpenAlertDialog;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return photoDetailsList.size();
    }


    public List<PhotoDetails> getCurrentList() {
        return photoDetailsList;
    }


    public void submitList(List<PhotoDetails> newPhotoDetailsList) {
        if (this.photoDetailsList == null) {
            this.photoDetailsList = new ArrayList<>();
            this.photoDetailsList.addAll(newPhotoDetailsList);
            return;
        }


        this.photoDetailsList.clear();
        this.photoDetailsList.addAll(newPhotoDetailsList);
    }


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        View rotateImageMorePopular;
        View rotateImageLessPopular;
        ImageView imageView;

        public ViewHolder(@NotNull View view) {
            super(view);

            imageView = view.findViewById(R.id.user_image);
            rotateImageMorePopular = view.findViewById(R.id.rotate_image_more_popular);
            rotateImageLessPopular = view.findViewById(R.id.rotate_image_less_popular);

        }

        public void blockArrows(){

            rotateImageLessPopular.setEnabled(false);
            rotateImageMorePopular.setEnabled(false);
        }

        public void enableArrows(){

            rotateImageLessPopular.setEnabled(true);
            rotateImageMorePopular.setEnabled(true);
        }


    }


    public static class LoadingHolder extends RecyclerView.ViewHolder {

        public LoadingHolder(@NotNull View view) {
            super(view);
        }
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        if (viewType == VIEW_TYPE_LOADING){
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_loading, viewGroup, false);
            return new LoadingHolder(view);
        }


        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_for_edit_user_profile_image, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof LoadingHolder)
            return;


        final String finalFilePath = photoDetailsList.get(position).getFilePath();

        ViewHolder realViewHolder = (ViewHolder)viewHolder;

        if (position == 0){
            realViewHolder.rotateImageMorePopular.setVisibility(View.INVISIBLE);
        }else{
            realViewHolder.rotateImageMorePopular.setVisibility(View.VISIBLE);
        }

        if (position == this.getItemCount() - 1){
            realViewHolder.rotateImageLessPopular.setVisibility(View.INVISIBLE);
        }else{
            realViewHolder.rotateImageLessPopular.setVisibility(View.VISIBLE);
        }

        String imageUrl = photoDetailsList.get(position).getPhotoDownloadUrl();

        SetImageWithGlide.setImageWithGlideOrDefaultImage(imageUrl, realViewHolder.imageView, realViewHolder.itemView.getContext());


        realViewHolder.imageView.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                onImageClickOpenAlertDialog.openAlertDialog(finalFilePath);
            }
        });

        realViewHolder.rotateImageMorePopular.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {
                realViewHolder.blockArrows();
                new Handler().postDelayed(()-> realViewHolder.enableArrows(), 500);

                onClickRotateImagesMorePopular.onClickRotateImagesMorePopular(finalFilePath);
            }
        });


        realViewHolder.rotateImageLessPopular.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {
                realViewHolder.blockArrows();
                new Handler().postDelayed(()-> realViewHolder.enableArrows(), 500);

                onClickRotateImagesLessPopular.onClickRotateImagesLessPopular(finalFilePath);
            }
        });


    }


    @Override
    public int getItemViewType(int position) {
        PhotoDetails photoDetails = photoDetailsList.get(position);

        if (photoDetails == null)
            return VIEW_TYPE_LOADING;

        return VIEW_TYPE_IMAGE;
    }

}
