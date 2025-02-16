package com.baikas.sporthub6.adapters.matchesshow.seewhorequested;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.uimodel.UIPlayer;
import com.baikas.sporthub6.R;
import com.baikas.sporthub6.listeners.OnClickListenerPass2CompletableFuture;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.constants.JoinOrIgnore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SeeWhoRequestedAdapter extends RecyclerView.Adapter<SeeWhoRequestedAdapter.ViewHolder>{


    private final List<User> usersThatRequested;
    private Match currentMatch;
    private final OnClickListenerPass2CompletableFuture<String, JoinOrIgnore> onClick;
    private final OpenUserProfile onClickSeeProfileOfUserListener;


    public SeeWhoRequestedAdapter(List<User> usersThatRequested, Match currentMatch, OnClickListenerPass2CompletableFuture<String, JoinOrIgnore> onClick, OpenUserProfile onClickSeeProfileOfUserListener) {

        if (usersThatRequested == null)
            this.usersThatRequested = new ArrayList<>();
        else
            this.usersThatRequested = usersThatRequested;

        this.currentMatch = currentMatch;
        this.onClick = onClick;
        this.onClickSeeProfileOfUserListener = onClickSeeProfileOfUserListener;
    }



    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return usersThatRequested.size();
    }

    public void submitUpdatedMatch(Match currentMatch) {
        this.currentMatch = new Match(currentMatch);
    }

    public void submitNewRequestersList(List<User> requesters) {
        this.usersThatRequested.clear();

        this.usersThatRequested.addAll(requesters);
    }




    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        View backgroundLayout;
        UIPlayer requester;

        View requestJoinButton;
        View ignoreRequesterButton;

        public ViewHolder(@NotNull View view) {
            super(view);


            backgroundLayout = view.findViewById(R.id.background_layout);


            requester = new UIPlayer(view.getContext());
            requester.profileImage = view.findViewById(R.id.requester_profile_image);
            requester.name = view.findViewById(R.id.requester_name);
            requester.level = view.findViewById(R.id.requester_level);
            requester.age = view.findViewById(R.id.requester_age);
            requester.region = view.findViewById(R.id.requester_region);
            
            // Button
            requestJoinButton = view.findViewById(R.id.request_join);
            ignoreRequesterButton = view.findViewById(R.id.ignore_player);
        }

        public void blockButtons(){
            requestJoinButton.setEnabled(false);
            ignoreRequesterButton.setEnabled(false);
        }

        public void enableButtons(){
            requestJoinButton.setEnabled(true);
            ignoreRequesterButton.setEnabled(true);
        }

    }


    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_see_who_requested, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

        User requester = usersThatRequested.get(position);


        if (position % 2 == 0){
            viewHolder.backgroundLayout.setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.grey_mode));
        }else{
            viewHolder.backgroundLayout.setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.white_or_black_primary_total));
        }

        viewHolder.requester.mapWithUserWithSport(requester,currentMatch.getSport());

        viewHolder.requester.profileImage.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                onClickSeeProfileOfUserListener.openUserProfile(requester.getUserId(), null);
            }
        });


        if (currentMatch.getAdminIgnoredRequesters().contains(requester.getUserId())){
            viewHolder.ignoreRequesterButton.setAlpha(0.4f);
            viewHolder.requestJoinButton.setAlpha(0.4f);
        }else{
            viewHolder.ignoreRequesterButton.setAlpha(1f);
            viewHolder.requestJoinButton.setAlpha(1f);
        }

        if (currentMatch.getUsersInChat().contains(requester.getUserId())){
            viewHolder.ignoreRequesterButton.setAlpha(0.4f);
            viewHolder.requestJoinButton.setAlpha(0.4f);
        }

        // Button
        viewHolder.requestJoinButton.setOnClickListener((View buttonClicked)->{
            viewHolder.blockButtons();

            onClick.onClick(requester.getUserId(),JoinOrIgnore.JOIN)
                    .whenComplete((o1,o2)->viewHolder.enableButtons());
        });

        viewHolder.ignoreRequesterButton.setOnClickListener((View buttonClicked)->{
            viewHolder.blockButtons();

            onClick.onClick(requester.getUserId(),JoinOrIgnore.IGNORE)
                    .whenComplete((o1,o2)->viewHolder.enableButtons());
        });
    }



    @Override
    public long getItemId(int position) {
        return position;
    }

}
