package com.baikas.sporthub6.adapters.alertdialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.adapters.mainpage.home.MatchesAdapter;
import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.interfaces.ShowPhotoOptions;
import com.baikas.sporthub6.models.PhotoDetails;
import com.baikas.sporthub6.models.fakemessages.FakePhotoDetailsMessageForUser;
import com.bumptech.glide.Glide;
import com.baikas.sporthub6.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UserProfileImagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<PhotoDetails> photoDetailsList;
    private final int VIEW_TYPE_LOADING = -1;
    private final int VIEW_TYPE_MESSAGE_FOR_USER = 0;
    private final int VIEW_TYPE_IMAGE = 1;


    public UserProfileImagesAdapter(List<PhotoDetails> photoDetailsList) {
        this.photoDetailsList = photoDetailsList;
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
        ImageView imageView;

        public ViewHolder(@NotNull View view) {
            super(view);

            imageView = view.findViewById(R.id.user_image);

        }


    }


    public static class MessageToUserHolder extends RecyclerView.ViewHolder {
        TextView textViewMessageToUser;
        public MessageToUserHolder(@NotNull View view) {
            super(view);

            textViewMessageToUser = view.findViewById(R.id.text_view_message_to_user);
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

        if (viewType == VIEW_TYPE_MESSAGE_FOR_USER){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message_to_user,viewGroup,false);
            return new MessageToUserHolder(view);
        }

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_user_profile_images_display, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof LoadingHolder)
            return;

        if (viewHolder instanceof MessageToUserHolder){
            MessageToUserHolder messageToUserHolder = (MessageToUserHolder) viewHolder;

            FakePhotoDetailsMessageForUser fakeMessage = (FakePhotoDetailsMessageForUser) photoDetailsList.get(position);
            messageToUserHolder.textViewMessageToUser.setText(fakeMessage.getMessage());

            return;
        }


        ViewHolder realViewHolder = (ViewHolder)viewHolder;

        String imageUrl = photoDetailsList.get(position).getPhotoDownloadUrl();

        SetImageWithGlide.setImageWithGlideOrClear(imageUrl,realViewHolder.imageView,realViewHolder.itemView.getContext());

    }


    @Override
    public int getItemViewType(int position) {
        PhotoDetails photoDetails = photoDetailsList.get(position);

        if (photoDetails == null)
            return VIEW_TYPE_LOADING;

        if (photoDetails instanceof FakePhotoDetailsMessageForUser)
            return VIEW_TYPE_MESSAGE_FOR_USER;

        return VIEW_TYPE_IMAGE;
    }
}
