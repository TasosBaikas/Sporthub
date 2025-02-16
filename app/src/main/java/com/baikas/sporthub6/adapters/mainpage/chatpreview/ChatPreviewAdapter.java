package com.baikas.sporthub6.adapters.mainpage.chatpreview;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.GetColor;
import com.baikas.sporthub6.models.constants.ChatMessageType;
import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.models.user.UserShortForm;
import com.baikas.sporthub6.R;
import com.baikas.sporthub6.models.constants.ChatTypesConstants;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.time.GreekDateFormatter;
import com.baikas.sporthub6.interfaces.gonext.GoToChatActivity;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.chat.ChatMessage;
import com.baikas.sporthub6.models.chat.fakemessages.FakeChatForMessage;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class ChatPreviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Chat> chatList;
    private final String chatPreviewType;
    private final String userId;
    private final GoToChatActivity goToChatActivity;
    private final OpenUserProfile openUserProfile;

    private static final int VIEW_TYPE_LOADING = -1;
    private static final int VIEW_TYPE_MESSAGE_FOR_USER = 0;
    private static final int VIEW_TYPE_MATCH_CHAT = 1;
    private static final int VIEW_TYPE_PRIVATE_CHAT = 2;

    public ChatPreviewAdapter(List<Chat> chatList, String chatPreviewType, String userId, GoToChatActivity goToChatActivity, OpenUserProfile openUserProfile) {
        this.chatList = chatList;
        this.chatPreviewType = chatPreviewType;

        this.userId = userId;
        this.goToChatActivity = goToChatActivity;
        this.openUserProfile = openUserProfile;
    }


    public List<Chat> getCurrentList() {
        return this.chatList;
    }

    public void submitList(List<Chat> newChatList) {
        if (this.chatList == null) {
            this.chatList = new ArrayList<>();
            this.chatList.addAll(newChatList);
            return;
        }


        this.chatList.clear();
        this.chatList.addAll(newChatList);
    }



    public static class ViewHolder extends ViewHolderBase {
        ImageView fire1;
        ImageView fire2;
        ImageView fire3;

        ImageView sportImage;
        TextView matchDayTime;
        TextView ifMoreThan1Week;
        TextView teamMembersTextView;
        View crownSvgImage;
        View membersImage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            fire1 = itemView.findViewById(R.id.fire1);
            fire2 = itemView.findViewById(R.id.fire2);
            fire3 = itemView.findViewById(R.id.fire3);

            matchDayTime = itemView.findViewById(R.id.match_daytime);
            ifMoreThan1Week = itemView.findViewById(R.id.if_more_than_week);
            sportImage = itemView.findViewById(R.id.sport_image);

            membersImage = itemView.findViewById(R.id.members_image);
            teamMembersTextView = itemView.findViewById(R.id.members_count);

            crownSvgImage = itemView.findViewById(R.id.crown_icon);


        }



        public void setProfileImageSport(String sport) {
            int drawable = SportConstants.SPORTS_MAP.get(sport).getSportImageId();

            this.sportImage.setImageResource(drawable);
        }



        public void handleIfMatchConversation(Chat chat, String yourId, OpenUserProfile openUserProfile) {

            sportImage.setVisibility(View.VISIBLE);
            setProfileImageSport(chat.getSport());

            membersImage.setVisibility(View.VISIBLE);
            teamMembersTextView.setVisibility(View.VISIBLE);

            if (chat.isAdmin(yourId))
                crownSvgImage.setVisibility(View.VISIBLE);
            else
                crownSvgImage.setVisibility(View.GONE);


            String matchDateInString = GreekDateFormatter.epochToDayAndTime(chat.getMatchDateInUTC());
            matchDayTime.setText(matchDateInString);

            if ((GreekDateFormatter.diffLessThan1Week(TimeFromInternet.getInternetTimeEpochUTC(), chat.getMatchDateInUTC()) && chat.isChatMatchIsRelevant())){
                ifMoreThan1Week.setVisibility(View.GONE);
            }else{
                ifMoreThan1Week.setVisibility(View.VISIBLE);
                ifMoreThan1Week.setText("(" + GreekDateFormatter.epochToFormattedDayAndMonth(chat.getMatchDateInUTC()) + ")");
            }

            if (chat.getLastChatMessage() == null || chat.getLastChatMessage().getUserShortForm() == null){
                lastMessengerPhotoImage.setImageResource(R.drawable.no_profile_image_svg);

                lastMessengerPhotoImage.setOnClickListener(null);
            }else {
                SetImageWithGlide.setImageWithGlideOrDefaultImage(chat.getLastChatMessage().getUserShortForm().getProfileImageUrl(), lastMessengerPhotoImage, itemView.getContext());

                lastMessengerPhotoImage.setOnClickListener((view) -> {

                    String userIdOfLastMessage = chat.getLastChatMessage().getUserShortForm().getUserId();
                    openUserProfile.openUserProfile(userIdOfLastMessage, null);
                });
            }

            if (chat.isChatMatchIsRelevant())
                littleDelimiter.setBackgroundColor(ContextCompat.getColor(itemView.getContext(),R.color.green));
            else
                littleDelimiter.setBackgroundColor(ContextCompat.getColor(itemView.getContext(),R.color.yellow));


            int teamMembers = (new HashSet<>(chat.getMembersIds())).size();
            teamMembersTextView.setVisibility(View.VISIBLE);
            teamMembersTextView.setText(String.valueOf(teamMembers));

        }

        public void setColorIfAdmin(Chat chat, String userId) {

            String adminId = chat.getAdminId();

            if (userId == null || userId.equals("") || !userId.equals(adminId))
                return;

            String text = lastMessagePreview.getText().toString();

            SpannableString spannableString = new SpannableString(text);

            int orangeColor = ContextCompat.getColor(itemView.getContext(), R.color.orange_little_darker); // Make sure you have a color defined in your colors.xml

            int indexOf = text.indexOf(":");
            if (indexOf == -1)
                return;

            spannableString.setSpan(new ForegroundColorSpan(orangeColor), 0, indexOf + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            lastMessagePreview.setText(spannableString);
        }
    }

    public static class ViewHolderBase extends RecyclerView.ViewHolder {

        View backgroundLayout;
        View layout;
        TextView otherUsername;
        ImageView lastMessengerPhotoImage;
        TextView textViewTimeAndDay;
        TextView notSeen;

        TextView lastMessagePreview;
        View littleDelimiter;

        public ViewHolderBase(@NonNull View itemView) {
            super(itemView);


            backgroundLayout = itemView.findViewById(R.id.background_chat_preview);
            layout = itemView.findViewById(R.id.layout_team_hub_item);

            otherUsername = itemView.findViewById(R.id.other_username);

            lastMessengerPhotoImage = itemView.findViewById(R.id.last_messenger_profile_image);

            notSeen = itemView.findViewById(R.id.not_seen);


            textViewTimeAndDay = itemView.findViewById(R.id.time_and_day_send_last_message);
            lastMessagePreview = itemView.findViewById(R.id.lastMessagePreview);

            littleDelimiter = itemView.findViewById(R.id.view_small_line);
        }

        public void setTimeAndUserThatSendLastMessage(Chat chat) {

            long lastMessageCreatedTime = chat.getLastChatMessage().getCreatedAtUTC();
            String dayAndTime = GreekDateFormatter.getInFormatDayAndTime(lastMessageCreatedTime);

            this.textViewTimeAndDay.setText(dayAndTime);
        }

        public void setLastMessageText(Chat chat, String userId) {
            ChatMessage lastChatMessage = chat.getLastChatMessage();

//            lastMessagePreview.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white_or_black_with_opacity_mode));


            String firstName = lastChatMessage.getUserShortForm().getFirstName();
            String lastName = lastChatMessage.getUserShortForm().getLastName();

            String text = "";
            if (lastChatMessage.getMessageType().equals(ChatMessageType.ONLY_FOR_FIRST_MESSAGE) && chat.isPrivateConversation()) {

                text = "Το Ιδιωτικό chat μόλις δημιουργήθηκε";
            }else if (lastChatMessage.getMessageType().equals(ChatMessageType.ONLY_FOR_FIRST_MESSAGE) && !chat.isPrivateConversation()) {

                text = "Η ομάδα μόλις δημιουργήθηκε";
            }else if (lastChatMessage.getMessageType().equals(ChatMessageType.USER_JOINED)){

                text = "Ο χρήστης " + firstName + " " + lastName.charAt(0) + "."  + " μόλις εισήλθε";
            }else if (lastChatMessage.getMessageType().equals(ChatMessageType.USER_LEFT)) {

                text = "Ο χρήστης " + firstName + " " + lastName.charAt(0) + "." + " έφυγε";
            }else if (lastChatMessage.getMessageType().equals(ChatMessageType.PHONE_ENABLED)) {

                String username = firstName + " " + lastName;

                text = "Ο " + username + " δήλωσε το κινητό του για άμεση επικοινωνία";
            }else if (lastChatMessage.getMessageType().equals(ChatMessageType.PHONE_DISABLED)){

                String username = firstName + " " + lastName;

                text = "Ο " + username + " απέσυρε το κινητό του";
            }else if (lastChatMessage.getUserShortForm().getId().equals(userId)){
                text = "Εσείς: " + lastChatMessage.getMessage();

            }else{
                text = firstName + " " + lastName.charAt(0) +": " + lastChatMessage.getMessage();
            }
            lastMessagePreview.setText(text);



            if (chat.getNotSeenByUsersId().contains(userId)) {
                lastMessagePreview.setTypeface(null, Typeface.BOLD);
                textViewTimeAndDay.setTypeface(null, Typeface.BOLD);
                notSeen.setVisibility(View.VISIBLE);
            }else {
                lastMessagePreview.setTypeface(null, Typeface.NORMAL);
                textViewTimeAndDay.setTypeface(null, Typeface.NORMAL);
                notSeen.setVisibility(View.GONE);
            }
        }

    }

    public static class ViewHolderPrivateConversation extends ViewHolderBase {


        public ViewHolderPrivateConversation(@NonNull View itemView) {
            super(itemView);

        }


        public void handleIfPrivateConversation(Chat chat, String yourId, OpenUserProfile openUserProfile) {

            littleDelimiter.setBackgroundColor(ContextCompat.getColor(itemView.getContext(),R.color.blue));

            Optional<UserShortForm> otherUserOptional = chat.getPrivateConversation2Users().stream()
                    .filter((UserShortForm user) -> !user.getUserId().equals(yourId))
                    .findFirst();


            if (otherUserOptional.isPresent()) {
                UserShortForm otherUser = otherUserOptional.get();

                String otherUserUsername = otherUser.getFirstName() + " " + otherUser.getLastName();
                otherUsername.setText(otherUserUsername);

                lastMessengerPhotoImage.setOnClickListener((view) -> {

                    openUserProfile.openUserProfile(otherUser.getUserId(), null);
                });

                SetImageWithGlide.setImageWithGlideOrDefaultImage(otherUser.getProfileImageUrl(),lastMessengerPhotoImage,itemView.getContext());
            }else{
                otherUsername.setText("");
                SetImageWithGlide.setImageWithGlideOrDefaultImage("",lastMessengerPhotoImage,itemView.getContext());

                lastMessengerPhotoImage.setOnClickListener(null);
            }


        }

    }

    public static class LoadingHolder extends RecyclerView.ViewHolder {
        public LoadingHolder(@NotNull View view) {
            super(view);
        }
    }

    public static class MessageForUserHolder extends RecyclerView.ViewHolder {
        TextView textViewMessageToUser;
        public MessageForUserHolder(@NotNull View view) {
            super(view);

            textViewMessageToUser = view.findViewById(R.id.text_view_message_to_user);
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_TYPE_LOADING){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_loading,viewGroup,false);
            return new LoadingHolder(view);
        }

        if (viewType == VIEW_TYPE_MESSAGE_FOR_USER){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message_to_user,viewGroup,false);
            return new MessageForUserHolder(view);
        }

        if (viewType == VIEW_TYPE_PRIVATE_CHAT){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_preview_private_chat,viewGroup,false);
            return new ViewHolderPrivateConversation(view);
        }


        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_preview_match_chat,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadingHolder)
            return;

        if (holder instanceof MessageForUserHolder){
            MessageForUserHolder messageForUserHolder = (MessageForUserHolder) holder;

            FakeChatForMessage chatForMessageToUser = (FakeChatForMessage) chatList.get(position);
            messageForUserHolder.textViewMessageToUser.setText(chatForMessageToUser.getMessageToUser());
            return;
        }

        Chat chat = chatList.get(position);

        ViewHolderBase baseViewHolder = (ViewHolderBase) holder;
        baseViewHolder.layout.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {
                goToChatActivity.goToChatActivity(chat);
            }
        });

        if (position % 2 == 0){
            baseViewHolder.backgroundLayout.setBackgroundColor(ContextCompat.getColor(baseViewHolder.itemView.getContext(), R.color.grey_mode));
        }else{
            baseViewHolder.backgroundLayout.setBackgroundColor(ContextCompat.getColor(baseViewHolder.itemView.getContext(), R.color.white_or_black_primary_total));
        }

        baseViewHolder.setTimeAndUserThatSendLastMessage(chat);

        String yourId = FirebaseAuth.getInstance().getUid();

        baseViewHolder.setLastMessageText(chat,yourId);

        if (holder instanceof ViewHolderPrivateConversation){
            ViewHolderPrivateConversation privateViewHolder = ((ViewHolderPrivateConversation) holder);

            privateViewHolder.handleIfPrivateConversation(chat,yourId, openUserProfile);

            return;
        }

        ViewHolder realViewHolder = ((ViewHolder) holder);


        realViewHolder.handleIfMatchConversation(chat,yourId, openUserProfile);

        realViewHolder.fire1.setVisibility(View.GONE);
        realViewHolder.fire2.setVisibility(View.GONE);
        realViewHolder.fire3.setVisibility(View.GONE);

        long matchTime = chat.getMatchDateInUTC();
        if (matchTime >= TimeFromInternet.getInternetTimeEpochUTC()){

            int hours3 = 3 * 60 * 60 * 1000;
            int hours6 = 2 * hours3;
            int hours9 = 3 * hours3;

            if (matchTime < TimeFromInternet.getInternetTimeEpochUTC() + hours3)
                realViewHolder.fire1.setVisibility(View.VISIBLE);

            if (matchTime < TimeFromInternet.getInternetTimeEpochUTC() + hours6)
                realViewHolder.fire2.setVisibility(View.VISIBLE);

            if (matchTime < TimeFromInternet.getInternetTimeEpochUTC() + hours9)
                realViewHolder.fire3.setVisibility(View.VISIBLE);

        }


        realViewHolder.setColorIfAdmin(chat, userId);

    }




    @Override
    public int getItemCount() {
        return this.chatList.size();
    }



    @Override
    public int getItemViewType(int position) {
        Chat chat = chatList.get(position);
        
        if (chat == null)
            return VIEW_TYPE_LOADING;

        if (chat instanceof FakeChatForMessage){
            return VIEW_TYPE_MESSAGE_FOR_USER;
        }
        
        if (chat.isPrivateConversation())
            return VIEW_TYPE_PRIVATE_CHAT;

        return VIEW_TYPE_MATCH_CHAT;
    }
}
