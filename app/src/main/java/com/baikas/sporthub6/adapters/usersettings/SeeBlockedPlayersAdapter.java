package com.baikas.sporthub6.adapters.usersettings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.interfaces.onclick.OnClickUnblockPlayer;
import com.baikas.sporthub6.models.uimodel.UIPlayer;
import com.baikas.sporthub6.models.user.User;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SeeBlockedPlayersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<User> blockedUsers;
    private final OpenUserProfile openUserProfile;
    private final OnClickUnblockPlayer onClickUnblockPlayer;


    public SeeBlockedPlayersAdapter(List<User> blockedUsers, OpenUserProfile openUserProfile, OnClickUnblockPlayer onClickUnblockPlayer) {
        this.blockedUsers = blockedUsers;
        this.openUserProfile = openUserProfile;
        this.onClickUnblockPlayer = onClickUnblockPlayer;
    }

    public void submitList(List<User> blockedUsers) {

        this.blockedUsers.clear();
        this.blockedUsers.addAll(blockedUsers);
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return blockedUsers.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View backgroundLayout;
        public UIPlayer uiPlayer;
        public View unblockPlayer;

        public ViewHolder(View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.background_layout);


            uiPlayer = new UIPlayer(itemView.getContext());
            uiPlayer.profileImage = itemView.findViewById(R.id.profile_image);
            uiPlayer.name = itemView.findViewById(R.id.name);
            uiPlayer.age = itemView.findViewById(R.id.age);
            uiPlayer.region = itemView.findViewById(R.id.region);

            unblockPlayer = itemView.findViewById(R.id.unblock_player);
        }
    }


    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_settings_blocked_users, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {


        User user = blockedUsers.get(position);

        ViewHolder realViewHolder = (ViewHolder) viewHolder;


        if (position % 2 == 0){
            realViewHolder.backgroundLayout.setBackgroundColor(ContextCompat.getColor(realViewHolder.itemView.getContext(), R.color.grey_mode));
        }else{
            realViewHolder.backgroundLayout.setBackgroundColor(ContextCompat.getColor(realViewHolder.itemView.getContext(), R.color.white_or_black_primary_total));
        }


        realViewHolder.uiPlayer.mapWithUser(user);

        realViewHolder.uiPlayer.profileImage.setOnClickListener((v) -> {
            openUserProfile.openUserProfile(user.getUserId(), "");
        });

        realViewHolder.unblockPlayer.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                onClickUnblockPlayer.unBlockPlayer(user.getUserId());
            }
        });

    }

}
