package com.baikas.sporthub6.adapters.chat;

import android.graphics.Paint;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.interfaces.chat.AlertDialogRateUser;
import com.baikas.sporthub6.interfaces.chat.OnClickDialUpNumber;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.uimodel.UIPlayer;
import com.baikas.sporthub6.R;
import com.baikas.sporthub6.models.user.User;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChatMembersShowAdapter extends RecyclerView.Adapter<ChatMembersShowAdapter.ViewHolder>{

    private List<User> chatMembers;
    private Chat chat;
    private OnClickDialUpNumber onClickDialUpNumber;
    private OpenUserProfile openUserProfile;
    private AlertDialogRateUser alertDialogRateUser;
//    private final OnClickListenerPass2<String, Button> joinClickListener;


    public ChatMembersShowAdapter(List<User> chatMembers, Chat chat, OnClickDialUpNumber onClickDialUpNumber, OpenUserProfile openUserProfile, AlertDialogRateUser alertDialogRateUser) {
        this.chatMembers = chatMembers;
        this.chat = chat;
        this.onClickDialUpNumber = onClickDialUpNumber;
        this.openUserProfile = openUserProfile;
        this.alertDialogRateUser = alertDialogRateUser;
    }

    @Override
    public int getItemCount() {
        return chatMembers.size();
    }






    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        View backgroundLayout;
        UIPlayer player1;
        TextView phoneNumberTextView;
        View rateUserStar;
//        View viewSmallLine;

        public ViewHolder(@NotNull View view) {
            super(view);

            backgroundLayout = view.findViewById(R.id.background_layout);

            player1 = new UIPlayer(view.getContext());
            player1.profileImage = view.findViewById(R.id.user_profile_image);
            player1.name = view.findViewById(R.id.name);
            player1.level = view.findViewById(R.id.level);
            player1.age = view.findViewById(R.id.age);
            player1.region = view.findViewById(R.id.region);

            phoneNumberTextView = view.findViewById(R.id.phone_declaration);

            rateUserStar = view.findViewById(R.id.rate_user_star);
//            viewSmallLine = view.findViewById(R.id.view_small_line);

        }

        public void setViewColor(Chat chat) {

//            viewSmallLine.setVisibility(View.VISIBLE);
//
//            if (chat.isPrivateConversation()) {
//                viewSmallLine.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.blue));
//                return;
//            }
//
//            if (chat.isChatMatchIsRelevant())
//                viewSmallLine.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.green));
//            else
//                viewSmallLine.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.yellow));

        }

        public void setPhoneNumberThings(User chatMember, OnClickDialUpNumber onClickDialUpNumber) {

            if (chatMember.getPhoneNumber() == null || chatMember.getPhoneNumber().isEmpty()) {
                phoneNumberTextView.setText("Δεν έχει δηλωθεί");
                phoneNumberTextView.setPaintFlags(phoneNumberTextView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

                return;
            }


            phoneNumberTextView.setText(chatMember.getPhoneNumber());
            phoneNumberTextView.setPaintFlags(phoneNumberTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            phoneNumberTextView.setOnClickListener((view) -> {
                onClickDialUpNumber.onClickDialUpNumber(chatMember.getPhoneNumber());
            });

        }
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_chat_member_show_match_conv, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

        User chatMember = chatMembers.get(position);

        if (position % 2 == 0){
            viewHolder.backgroundLayout.setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.grey_mode));
        }else{
            viewHolder.backgroundLayout.setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.white_or_black_primary_total));
        }


//        viewHolder.setViewColor(chat);


        viewHolder.player1.mapWithUserWithSport(chatMember,this.chat.getSport());

        viewHolder.player1.profileImage.setOnClickListener((v)->openUserProfile.openUserProfile(chatMember.getUserId(), chat.getSport()));

        viewHolder.setPhoneNumberThings(chatMember, onClickDialUpNumber);

        if (chatMember.getUserId().equals(FirebaseAuth.getInstance().getUid()))
            viewHolder.rateUserStar.setVisibility(View.GONE);
        else
            viewHolder.rateUserStar.setVisibility(View.VISIBLE);

        viewHolder.rateUserStar.setOnClickListener((view) -> {

            alertDialogRateUser.alertDialogRateUser(chatMember);
        });

    }

}
