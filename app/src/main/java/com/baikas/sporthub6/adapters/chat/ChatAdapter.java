package com.baikas.sporthub6.adapters.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.models.constants.ChatMessageType;
import com.baikas.sporthub6.models.constants.EmojisTypes;
import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.R;
import com.baikas.sporthub6.helpers.time.GreekDateFormatter;
import com.baikas.sporthub6.interfaces.chat.AnimateRecyclerView;
import com.baikas.sporthub6.interfaces.chat.ChatMessageRequestsUsersAsync;
import com.baikas.sporthub6.listeners.chat.OnEmojiClickListener;
import com.baikas.sporthub6.interfaces.chat.ShowAlertDialogEmojisInMessage;
import com.baikas.sporthub6.listeners.chat.OnLongClickOtherMessage;
import com.baikas.sporthub6.listeners.chat.OnLongClickUserMessage;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.chat.ChatMessage;
import com.baikas.sporthub6.models.chat.fakemessages.FakeChatMessageForUser;
import com.baikas.sporthub6.models.user.UserShortForm;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int CLOSE_MESSAGES_TIME = 10 * 60 * 1000;
    private List<ChatMessage> chatMessagesList;
    private final Set<String> messageStatusIsBeenClicked = new HashSet<>();
//    private final Map<String,User> usersWithChatAction;
    private final String userId;
    private final Chat chat;
    private final Map<String, UserShortForm> usersFromAsync;
    private int textMessageMaxWidth;
    private final AnimateRecyclerView animateRecyclerView;
    private final ChatMessageRequestsUsersAsync chatMessageRequestsUsersAsync;
    private final OnEmojiClickListener onEmojiClickListener;
    private final ShowAlertDialogEmojisInMessage showAlertDialogEmojisInMessage;
    private final OnLongClickUserMessage onLongClickUserMessage;
    private final OnLongClickOtherMessage onLongClickOtherMessage;
    private static final float LOW_OPACITY_FOR_EMOJI = 0.5f;
    private static final int VIEW_TYPE_DELETED_OTHER = -3;
    private static final int VIEW_TYPE_DELETED_USER = -2;
    private static final int VIEW_TYPE_LOADING = -1;
    private static final int VIEW_TYPE_CHAT_MESSAGE_FOR_USER = 0;
    private static final int VIEW_TYPE_FIRST_MESSAGE_PRIVATE_CONVERSATION = 1;
    private static final int VIEW_TYPE_FIRST_MESSAGE_MATCH_CONVERSATION = 2;
    private static final int VIEW_TYPE_USER_TEXT_MESSAGE = 3;
    private static final int VIEW_TYPE_OTHER_TEXT_MESSAGE = 4;
    private static final int VIEW_TYPE_PHONE_ENABLED = 5;
    private static final int VIEW_TYPE_PHONE_DISABLED = 6;
    private static final int VIEW_TYPE_USER_JOINED = 100;
    private static final int VIEW_TYPE_USER_LEFT = 101;


    public ChatAdapter(List<ChatMessage> chatMessagesList, String userId, Chat chat, Map<String, UserShortForm> usersFromAsync,
                       AnimateRecyclerView animateRecyclerView, OnEmojiClickListener onEmojiClickListener, ChatMessageRequestsUsersAsync chatMessageRequestsUsersAsync,
                       ShowAlertDialogEmojisInMessage showAlertDialogEmojisInMessage, OnLongClickUserMessage onLongClickUserMessage, OnLongClickOtherMessage onLongClickOtherMessage) {
        this.chatMessagesList = chatMessagesList;
        this.userId = userId;
        this.chat = chat;
        this.usersFromAsync = usersFromAsync;

        this.animateRecyclerView = animateRecyclerView;
        this.onEmojiClickListener = onEmojiClickListener;
        this.chatMessageRequestsUsersAsync = chatMessageRequestsUsersAsync;
        this.showAlertDialogEmojisInMessage = showAlertDialogEmojisInMessage;
        this.onLongClickUserMessage = onLongClickUserMessage;
        this.onLongClickOtherMessage = onLongClickOtherMessage;
    }


    @Override
    public int getItemCount() {
        return this.chatMessagesList.size();
    }

    public List<ChatMessage> getCurrentList() {
        return this.chatMessagesList;
    }

    public void submitList(List<ChatMessage> newMessages) {
        if (this.chatMessagesList == null) {
            this.chatMessagesList = new ArrayList<>();
            this.chatMessagesList.addAll(newMessages);
            return;
        }


        this.chatMessagesList.clear();
        this.chatMessagesList.addAll(newMessages);
    }


    public void setTextMessageMaxWidth(int width) {
        this.textMessageMaxWidth = width;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_TYPE_LOADING){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_loading,viewGroup,false);
            return new LoadingHolder(view);
        }

        if (viewType == VIEW_TYPE_DELETED_USER){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_user_message_text,viewGroup,false);
            return new DeletedUserMessageHolder(view,onEmojiClickListener,showAlertDialogEmojisInMessage);
        }

        if (viewType == VIEW_TYPE_DELETED_OTHER){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_other_message,viewGroup,false);
            return new DeletedOtherMessageHolder(view,onEmojiClickListener,showAlertDialogEmojisInMessage);
        }

        if (viewType == VIEW_TYPE_CHAT_MESSAGE_FOR_USER){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message_to_user,viewGroup,false);
            return new MessageForUserHolder(view);
        }

        if (viewType == VIEW_TYPE_USER_JOINED){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_user_joined_message,viewGroup,false);
            return new UserJoinedHolder(view);
        }

        if (viewType == VIEW_TYPE_USER_LEFT){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_user_left_message,viewGroup,false);
            return new UserLeftHolder(view);
        }

        if (viewType == VIEW_TYPE_PHONE_ENABLED){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_user_enabled_phone,viewGroup,false);
            return new UserEnabledPhoneHolder(view);
        }

        if (viewType == VIEW_TYPE_PHONE_DISABLED){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_user_disabled_phone,viewGroup,false);
            return new UserDisabledPhoneHolder(view);
        }

        if (viewType == VIEW_TYPE_FIRST_MESSAGE_MATCH_CONVERSATION){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_first_message_if_match_conversation,viewGroup,false);
            return new FirstMessageIfMatchConversationHolder(view);
        }

        if (viewType == VIEW_TYPE_FIRST_MESSAGE_PRIVATE_CONVERSATION){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_first_message_if_private_conversation,viewGroup,false);
            return new FirstMessageIfPrivateConversationHolder(view);
        }

        if (viewType == VIEW_TYPE_USER_TEXT_MESSAGE){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_user_message_text, viewGroup, false);
            return new UserMessageViewHolder(view,onEmojiClickListener,showAlertDialogEmojisInMessage);
        }

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_other_message, viewGroup, false);
        return new OtherMessageViewHolder(view,onEmojiClickListener, showAlertDialogEmojisInMessage);
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadingHolder) {
            LoadingHolder loadingHolder = (LoadingHolder) holder;


            if (chat.isPrivateConversation()){


                loadingHolder.loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(loadingHolder.itemView.getContext(), R.color.blue));
                return;
            }

            if (chat.isChatMatchIsRelevant()){
                loadingHolder.loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(loadingHolder.itemView.getContext(), R.color.green));
            }else{
                loadingHolder.loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(loadingHolder.itemView.getContext(), R.color.yellow));
            }

            return;
        }

        ChatMessage chatMessage = chatMessagesList.get(position);

        if (holder instanceof MessageForUserHolder){
            MessageForUserHolder messageForUserHolder = (MessageForUserHolder) holder;

            FakeChatMessageForUser chatForMessageToUser = (FakeChatMessageForUser) chatMessage;
            messageForUserHolder.textViewMessageToUser.setText(chatForMessageToUser.getMessageToUser());
            return;
        }


        if (holder instanceof FirstMessageIfPrivateConversationHolder){
            FirstMessageIfPrivateConversationHolder firstMessageHolder = (FirstMessageIfPrivateConversationHolder) holder;

            Optional<UserShortForm> userShortFormOptional = this.chat.getPrivateConversation2Users().stream()
                    .filter((UserShortForm userTemp) -> !userTemp.getId().equals(FirebaseAuth.getInstance().getUid()))
                    .findFirst();

            if (!userShortFormOptional.isPresent()){

                firstMessageHolder.otherUserProfileImage.setImageResource(R.drawable.no_profile_image_svg);
                firstMessageHolder.otherUsername.setText("Anonymous");

                firstMessageHolder.otherRegion.setText("Unknown");
                return;
            }

            UserShortForm userShortForm = userShortFormOptional.get();

            SetImageWithGlide.setImageWithGlideOrDefaultImage(userShortForm.getProfileImageUrl(),firstMessageHolder.otherUserProfileImage,firstMessageHolder.itemView.getContext());

            String username = userShortForm.getFirstName() + " " + userShortForm.getLastName();
            firstMessageHolder.otherUsername.setText(username);

            firstMessageHolder.otherRegion.setText(userShortForm.getRegion());
            return;
        }


        if (holder instanceof FirstMessageIfMatchConversationHolder){
            FirstMessageIfMatchConversationHolder firstMessageHolder = (FirstMessageIfMatchConversationHolder) holder;

            String matchDateText = GreekDateFormatter.epochToDayAndTime(chat.getMatchDateInUTC());
            firstMessageHolder.matchDateString.setText(matchDateText + " " + GreekDateFormatter.epochToFormattedDayAndMonth(chat.getMatchDateInUTC()));

            return;
        }


        if (holder instanceof UserJoinedHolder){
            UserJoinedHolder userJoinedHolder = (UserJoinedHolder) holder;

            String username = chatMessage.getUserShortForm().getFirstName() + " " + chatMessage.getUserShortForm().getLastName();//todo must get from map users (because of deletion)
            String text = "Ο χρήστης " + username + " εισήλθε";

            userJoinedHolder.userJoinedTextView.setText(text);

            userJoinedHolder.setSeenByUsers(holder.itemView,chatMessage,usersFromAsync, chatMessageRequestsUsersAsync);

            return;
        }

        if (holder instanceof UserLeftHolder){
            UserLeftHolder userLeftHolder = (UserLeftHolder) holder;

            String username = chatMessage.getUserShortForm().getFirstName() + " " + chatMessage.getUserShortForm().getLastName();//todo must get from map users (because of deletion)
            String text = "Ο χρήστης " + username + " αποχώρησε";

            userLeftHolder.userLeftTextView.setText(text);

            userLeftHolder.setSeenByUsers(holder.itemView,chatMessage,usersFromAsync, chatMessageRequestsUsersAsync);

            return;
        }

        if (holder instanceof UserEnabledPhoneHolder){
            UserEnabledPhoneHolder userEnabledPhoneHolder = (UserEnabledPhoneHolder) holder;

            String username = chatMessage.getUserShortForm().getFirstName() + " " + chatMessage.getUserShortForm().getLastName();//todo must get from map users (because of deletion)
            String text = "Ο " + username + " δήλωσε το κινητό του για άμεση επικοινωνία";

            userEnabledPhoneHolder.userEnabledPhoneTextView.setText(text);

            userEnabledPhoneHolder.setSeenByUsers(holder.itemView,chatMessage,usersFromAsync, chatMessageRequestsUsersAsync);

            return;
        }


        if (holder instanceof UserDisabledPhoneHolder){
            UserDisabledPhoneHolder userDisabledPhoneHolder = (UserDisabledPhoneHolder) holder;

            String username = chatMessage.getUserShortForm().getFirstName() + " " + chatMessage.getUserShortForm().getLastName();//todo must get from map users (because of deletion)
            String text = "Ο " + username + " απέσυρε το κινητό του";

            userDisabledPhoneHolder.userDisabledPhoneTextView.setText(text);

            userDisabledPhoneHolder.setSeenByUsers(holder.itemView,chatMessage,usersFromAsync, chatMessageRequestsUsersAsync);

            return;
        }

        if (this.textMessageMaxWidth == 0)
            this.textMessageMaxWidth = 800;



        if (holder instanceof UserMessageViewHolder || holder instanceof DeletedUserMessageHolder) {

            UserMessageViewHolder userMessageViewHolder = (UserMessageViewHolder) holder;

            userMessageViewHolder.setTextDateTime(chatMessagesList,position);


            userMessageViewHolder.textMessageTextView.setMaxWidth(this.textMessageMaxWidth);
            userMessageViewHolder.textMessageTextView.setText(chatMessage.getMessage());

            if (chatMessage.isMessageConfirmed())
                userMessageViewHolder.tickStatus.setImageResource(R.drawable.tick_icon);
            else
                userMessageViewHolder.tickStatus.setImageResource(R.drawable.sand_clock);



            userMessageViewHolder.setEmojiImageButtons(EmojisTypes.HEART,userMessageViewHolder.heartEmoji,chatMessage,userId);


            userMessageViewHolder.setEmojiLayout(chatMessage);



            if (this.messageStatusIsBeenClicked.contains(chatMessage.getId())) {
                userMessageViewHolder.messageStatusTextView.setVisibility(TextView.VISIBLE);
                userMessageViewHolder.messageStatusTextView.setText(chatMessage.getMessageStatus().toString());
            }
            else {
                userMessageViewHolder.messageStatusTextView.setVisibility(TextView.GONE);
            }

            userMessageViewHolder.textMessageTextView.setOnClickListener((View view)->{
                userMessageViewHolder.messageStatusTextView.setText(chatMessage.getMessageStatus().toString());

                animateRecyclerView.onTextMessageClick(userMessageViewHolder.messageStatusTextView);

                if (userMessageViewHolder.messageStatusTextView.getVisibility() == View.VISIBLE) {
                    this.messageStatusIsBeenClicked.add(chatMessage.getId());
                }else {
                    this.messageStatusIsBeenClicked.remove(chatMessage.getId());
                }
            });

            userMessageViewHolder.textMessageTextView.setOnLongClickListener((View view)->{
                onLongClickUserMessage.onLongClickUserMessage(chatMessage);
                return true;
            });

            userMessageViewHolder.setSeenByUsers(holder.itemView,chatMessage,usersFromAsync, chatMessageRequestsUsersAsync);

            //we dont use return
        }


        if (holder instanceof DeletedUserMessageHolder){

            DeletedUserMessageHolder deletedMessageHolder = (DeletedUserMessageHolder) holder;

            deletedMessageHolder.heartEmoji.setVisibility(View.GONE);

            deletedMessageHolder.emojisLayout.setVisibility(View.GONE);

            deletedMessageHolder.textMessageTextView.setOnLongClickListener((View view)-> true);

            //we dont use return
        }


        if (holder instanceof OtherMessageViewHolder || holder instanceof DeletedOtherMessageHolder){

            OtherMessageViewHolder otherMessageViewHolder = (OtherMessageViewHolder) holder;

            otherMessageViewHolder.setTextDateTime(chatMessagesList,position);

            UserShortForm userOther = chatMessage.getUserShortForm();

            if (chat.isAdmin(chatMessage.getUserShortForm().getUserId())){
                otherMessageViewHolder.otherName.setTextColor(ContextCompat.getColor(otherMessageViewHolder.itemView.getContext(), R.color.orange_little_darker));
            }else{
                otherMessageViewHolder.otherName.setTextColor(ContextCompat.getColor(otherMessageViewHolder.itemView.getContext(), R.color.colorOnPrimary));
            }

            UserShortForm userFromAsync = usersFromAsync.get(chatMessage.getUserShortForm().getId());
            if (userFromAsync == null){
                otherMessageViewHolder.setOtherName(chatMessagesList,position,userOther.getFirstName() + " " +  userOther.getLastName());
                otherMessageViewHolder.setOtherProfileImage(chatMessagesList,position,userOther);
            }else{
                chat.getLastChatMessage().setItHasProfileImageInDisplay(false);
                otherMessageViewHolder.setOtherName(chatMessagesList,position,userFromAsync.getFirstName() + " " +  userFromAsync.getLastName());
                otherMessageViewHolder.setOtherProfileImage(chatMessagesList,position, userFromAsync);
            }


            otherMessageViewHolder.textMessageTextView.setMaxWidth(this.textMessageMaxWidth);
            otherMessageViewHolder.textMessageTextView.setText(chatMessage.getMessage());

            otherMessageViewHolder.textMessageTextView.setOnLongClickListener((View view)->{
                onLongClickOtherMessage.onLongClickOtherMessage(chatMessage);
                return true;
            });


            otherMessageViewHolder.setEmojiImageButtons(EmojisTypes.HEART,otherMessageViewHolder.heartEmoji,chatMessage,userId);


            otherMessageViewHolder.setEmojiLayout(chatMessage);


            otherMessageViewHolder.setSeenByUsers(holder.itemView,chatMessage,usersFromAsync, chatMessageRequestsUsersAsync);


            //we dont use return
        }



        if (holder instanceof DeletedOtherMessageHolder){

            DeletedOtherMessageHolder deletedMessageHolder = (DeletedOtherMessageHolder) holder;

            deletedMessageHolder.heartEmoji.setVisibility(View.INVISIBLE);

            deletedMessageHolder.emojisLayout.setVisibility(View.GONE);

            deletedMessageHolder.textMessageTextView.setOnLongClickListener((View view)-> true);

            //we dont use return
        }

    }

    public interface SeenByFunctionality{


        default void setSeenByUsers(View itemView, ChatMessage chatMessage, Map<String, UserShortForm> usersFromAsync, ChatMessageRequestsUsersAsync chatMessageRequestsUsersAsync){

            ViewGroup seenByOthersLayout = itemView.findViewById(R.id.layout_seen_by_others);
            seenByOthersLayout.removeAllViews();
            seenByOthersLayout.setVisibility(View.GONE);

            List<String> seenByChangeRef = new ArrayList<>(chatMessage.getSeenByUsersId());
            seenByChangeRef.removeIf((String userId) -> userId.equals(FirebaseAuth.getInstance().getUid()));

            if (seenByChangeRef.isEmpty()){
                return;
            }

            for (String userId : seenByChangeRef) {

                if (usersFromAsync.get(userId) == null){
                    seenByOthersLayout.setVisibility(View.VISIBLE);
                    chatMessageRequestsUsersAsync.requestFromNotSeenBy(chatMessage, seenByChangeRef);
                    return;
                }
            }


            Context context = seenByOthersLayout.getContext();


            for (String playerId :seenByChangeRef) {

                UserShortForm user = usersFromAsync.get(playerId);
                if (user == null){
                    ImageView playerImage = (ImageView) LayoutInflater.from(context).inflate(R.layout.item_chat_circle_image_view_seen_by_others, seenByOthersLayout, false);

                    SetImageWithGlide.setImageWithGlideOrDefaultImage("",playerImage,context);

                    seenByOthersLayout.setVisibility(View.VISIBLE);
                    seenByOthersLayout.addView(playerImage);
                    continue;
                }

                if (user.getUserId().equals(FirebaseAuth.getInstance().getUid()))
                    continue;

                // Dynamically add buttons to the first LinearLayout
                ImageView playerImage = (ImageView) LayoutInflater.from(context).inflate(R.layout.item_chat_circle_image_view_seen_by_others, seenByOthersLayout, false);


                String profileImageUrl = user.getProfileImageUrl();

                SetImageWithGlide.setImageWithGlideOrDefaultImage(profileImageUrl,playerImage,context);

                seenByOthersLayout.setVisibility(View.VISIBLE);
                seenByOthersLayout.addView(playerImage);
            }



        }

    }


    public static abstract class GeneralChatMessageHolder extends RecyclerView.ViewHolder implements SeenByFunctionality {

        public final ConstraintLayout constraintLayout;

        public final TextView textDateTime;
        public final ViewGroup emojisLayout;
        public final ViewGroup seenByOthersLayout;

        public final OnEmojiClickListener onEmojiClickListener;
        public final ShowAlertDialogEmojisInMessage showAlertDialogEmojisInMessage;

        public final ImageView heartEmoji;
        public final ImageView tickStatus;

        public final TextView textMessageTextView;

        public final TextView messageStatusTextView;


        public GeneralChatMessageHolder(@NotNull View view, OnEmojiClickListener onEmojiClickListener, ShowAlertDialogEmojisInMessage showAlertDialogEmojisInMessage) {
            super(view);

            this.onEmojiClickListener = onEmojiClickListener;
            this.showAlertDialogEmojisInMessage = showAlertDialogEmojisInMessage;



            constraintLayout = itemView.findViewById(R.id.user_message_text_constraint_layout);

            textDateTime = itemView.findViewById(R.id.textDateTime);

            seenByOthersLayout = itemView.findViewById(R.id.layout_seen_by_others);

            emojisLayout = itemView.findViewById(R.id.layout_emojis);

            heartEmoji = itemView.findViewById(R.id.heart_emoji);
            tickStatus = itemView.findViewById(R.id.tick_status_image);

            textMessageTextView = itemView.findViewById(R.id.textMessage);

            messageStatusTextView = itemView.findViewById(R.id.message_status);
        }

        void setTextDateTime(List<ChatMessage> chatMessagesList, int position) {

            String timeInFormat = GreekDateFormatter.getGreekFormattedDate(chatMessagesList.get(position).getCreatedAtUTC());
            this.textDateTime.setText(timeInFormat);

            if (position + 1 == chatMessagesList.size() || chatMessagesList.get(position + 1) == null){

                ChatMessage chatMessage = chatMessagesList.get(position);

                boolean veryCloseMessages;
                if (chatMessage.getPreviousMessageCreatedAtUTC() == 0)
                    veryCloseMessages = false;
                else
                    veryCloseMessages = chatMessage.getCreatedAtUTC() - chatMessage.getPreviousMessageCreatedAtUTC() < CLOSE_MESSAGES_TIME;

                if (veryCloseMessages) {
                    this.textDateTime.setVisibility(TextView.GONE);
                    return;
                }

                this.textDateTime.setVisibility(TextView.VISIBLE);
                return;
            }

            boolean veryCloseMessages = chatMessagesList.get(position).getCreatedAtUTC() - chatMessagesList.get(position + 1).getCreatedAtUTC() < CLOSE_MESSAGES_TIME;

            if (veryCloseMessages) {
                this.textDateTime.setVisibility(TextView.GONE);
                return;
            }

            this.textDateTime.setVisibility(TextView.VISIBLE);
        }


        public void setEmojiLayout(ChatMessage chatMessage) {
            Context context = this.emojisLayout.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            this.emojisLayout.removeAllViews();
            this.emojisLayout.setVisibility(View.GONE);
            for (Map.Entry<String,List<String>> emojisEntry :chatMessage.getEmojisMap().entrySet()) {

                String key = emojisEntry.getKey();

                int emojiCount = emojisEntry.getValue().size();

                if (emojiCount == 0)
                    continue;


                ConstraintLayout constraintLayout = (ConstraintLayout) inflater.inflate(R.layout.item_chat_emoji_count_display, this.emojisLayout, false);

                TextView countEmojisText = constraintLayout.findViewById(R.id.count_emoji);

                String emojiCountWithX = emojiCount + "x";
                countEmojisText.setText(emojiCountWithX);

                ImageView emojiImage = constraintLayout.findViewById(R.id.emoji_image);
                if (key.equals(EmojisTypes.HEART)){

                    emojiImage.setImageResource(R.drawable.heart_png);
                }


                emojiImage.setOnClickListener(new Prevent2ClicksListener() {
                    @Override
                    public void onClickExecuteCode(View v) {

                        showAlertDialogEmojisInMessage.showAlertDialog(chatMessage);
                    }
                });

                this.emojisLayout.setVisibility(View.VISIBLE);
                this.emojisLayout.addView(constraintLayout);
            }
        }


        public void setEmojiImageButtons(String emoji,ImageView emojiImage,ChatMessage chatMessage,String userId){

            emojiImage.setClickable(chatMessage.isMessageConfirmed());//TODO

            if (chatMessage.getEmojisMap().get(emoji).contains(userId)){

                emojiImage.setAlpha(1f);
                emojiImage.setImageResource(R.drawable.heart_png);
            }else if (!chatMessage.getEmojisMap().get(emoji).contains(userId)){

                emojiImage.setAlpha(LOW_OPACITY_FOR_EMOJI);
                emojiImage.setImageResource(R.drawable.heart_png_not_selected);
            }


            emojiImage.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    if (!onEmojiClickListener.checkInternetConnection(itemView.getContext()))
                        return;

                    onEmojiClickListener.onClickEnabledAnimation(emojiImage,LOW_OPACITY_FOR_EMOJI);
                    onEmojiClickListener.onClickUpdateEmojiCount(chatMessage,emoji);
                }
            });

        }

//        private void blockAllEmojis() {
//            this.heartEmoji.setClickable(false);
//        }
//
//        private void enableAllEmojis() {
//            this.heartEmoji.setClickable(true);
//        }

    }


    public static class MessageForUserHolder extends RecyclerView.ViewHolder {
        TextView textViewMessageToUser;
        public MessageForUserHolder(@NotNull View view) {
            super(view);

            textViewMessageToUser = view.findViewById(R.id.text_view_message_to_user);
        }
    }


    public static class UserMessageViewHolder extends GeneralChatMessageHolder {

        public UserMessageViewHolder(@NonNull View itemView, OnEmojiClickListener onEmojiClickListener, ShowAlertDialogEmojisInMessage showAlertDialogEmojisInMessage) {
            super(itemView, onEmojiClickListener,showAlertDialogEmojisInMessage);
        }

    }


    static class OtherMessageViewHolder extends GeneralChatMessageHolder {

        final TextView otherName;
        final TextView textMessageTextView;
        final ImageView otherProfileImage;


        public OtherMessageViewHolder(@NonNull View itemView, OnEmojiClickListener onEmojiClickListener, ShowAlertDialogEmojisInMessage showAlertDialogEmojisInMessage) {
            super(itemView, onEmojiClickListener,showAlertDialogEmojisInMessage);


            otherName = itemView.findViewById(R.id.other_name);

            textMessageTextView = itemView.findViewById(R.id.textMessage);
            otherProfileImage = itemView.findViewById(R.id.other_profile_image);

        }



        public void setOtherName(List<ChatMessage> chatMessagesList, int position, String username) {
            if (position + 1 == chatMessagesList.size() || chatMessagesList.get(position + 1) == null){
                ChatMessage chatMessage = chatMessagesList.get(position);

                if (chatMessage.getPreviousMessageUserId().isEmpty()) {

                    this.otherName.setVisibility(View.VISIBLE);
                    this.otherName.setText(username);
                    return;
                }

                boolean veryCloseMessages = chatMessage.getCreatedAtUTC() - chatMessage.getPreviousMessageCreatedAtUTC() < CLOSE_MESSAGES_TIME;

                if (!veryCloseMessages){

                    this.otherName.setVisibility(View.VISIBLE);
                    this.otherName.setText(username);
                    return;
                }

                if (chatMessage.getUserShortForm().getId().equals(chatMessage.getPreviousMessageUserId())) {
                    this.otherName.setVisibility(View.GONE);
                    return;
                }


                this.otherName.setVisibility(View.VISIBLE);
                this.otherName.setText(username);
                return;
            }



            ChatMessage chatMessage = chatMessagesList.get(position);
            ChatMessage chatMessagePrevious = chatMessagesList.get(position + 1);

            if (chatMessagePrevious.isFirstMessage()){
                this.otherName.setVisibility(View.VISIBLE);
                this.otherName.setText(username);
                return;
            }

            boolean veryCloseMessages = chatMessage.getCreatedAtUTC() - chatMessagePrevious.getCreatedAtUTC() < CLOSE_MESSAGES_TIME;

            if (!veryCloseMessages){

                this.otherName.setVisibility(View.VISIBLE);
                this.otherName.setText(username);
                return;
            }

            if (chatMessage.getUserShortForm().getId().equals(chatMessagePrevious.getUserShortForm().getId())) {
                this.otherName.setVisibility(View.GONE);
                return;
            }


            this.otherName.setVisibility(View.VISIBLE);
            this.otherName.setText(username);
        }

        public void setOtherProfileImage(List<ChatMessage> chatMessagesList, int position,UserShortForm otherUser) {
            ChatMessage chatMessage = chatMessagesList.get(position);

            if (position == 0){

                chatMessage.setItHasProfileImageInDisplay(true);

                SetImageWithGlide.setImageWithGlideOrDefaultImage(otherUser.getProfileImageUrl(),otherProfileImage, itemView.getContext());
                this.otherProfileImage.setVisibility(View.VISIBLE);
                return;
            }

            ChatMessage chatMessageBelow = chatMessagesList.get(position - 1);

            boolean veryCloseMessages = chatMessageBelow.getCreatedAtUTC() - chatMessage.getCreatedAtUTC()  < CLOSE_MESSAGES_TIME;

            if (!veryCloseMessages){
                chatMessage.setItHasProfileImageInDisplay(true);

                SetImageWithGlide.setImageWithGlideOrDefaultImage(otherUser.getProfileImageUrl(),otherProfileImage, itemView.getContext());
                this.otherProfileImage.setVisibility(View.VISIBLE);
                return;
            }

            if (chatMessage.getUserShortForm().getId().equals(chatMessageBelow.getUserShortForm().getId())) {
                this.otherProfileImage.setVisibility(View.INVISIBLE);
                return;
            }

            chatMessage.setItHasProfileImageInDisplay(true);

            SetImageWithGlide.setImageWithGlideOrDefaultImage(otherUser.getProfileImageUrl(),otherProfileImage, itemView.getContext());
            this.otherProfileImage.setVisibility(View.VISIBLE);
        }
    }

    public static class UserJoinedHolder extends RecyclerView.ViewHolder implements SeenByFunctionality {

        TextView userJoinedTextView;

        public UserJoinedHolder(@NotNull View view) {
            super(view);

            userJoinedTextView = view.findViewById(R.id.user_join_text);
        }
    }

    public static class UserLeftHolder extends RecyclerView.ViewHolder implements SeenByFunctionality{

        TextView userLeftTextView;

        public UserLeftHolder(@NotNull View view) {
            super(view);

            userLeftTextView = view.findViewById(R.id.user_left_text);
        }

    }

    public static class UserEnabledPhoneHolder extends RecyclerView.ViewHolder implements SeenByFunctionality{

        TextView userEnabledPhoneTextView;

        public UserEnabledPhoneHolder(@NotNull View view) {
            super(view);

            userEnabledPhoneTextView = view.findViewById(R.id.user_enabled_phone_text_view);
        }

    }


    public static class UserDisabledPhoneHolder extends RecyclerView.ViewHolder implements SeenByFunctionality{

        TextView userDisabledPhoneTextView;

        public UserDisabledPhoneHolder(@NotNull View view) {
            super(view);

            userDisabledPhoneTextView = view.findViewById(R.id.user_disabled_phone_text_view);
        }

    }


    public static class LoadingHolder extends RecyclerView.ViewHolder {

        ProgressBar loadingBar;

        public LoadingHolder(@NotNull View view) {
            super(view);

            this.loadingBar = view.findViewById(R.id.progress_circular);
        }
    }
    public static class DeletedUserMessageHolder extends UserMessageViewHolder {

        public DeletedUserMessageHolder(@NonNull View itemView, OnEmojiClickListener onEmojiClickListener, ShowAlertDialogEmojisInMessage showAlertDialogEmojisInMessage) {
            super(itemView, onEmojiClickListener,showAlertDialogEmojisInMessage);
        }

    }


    public static class DeletedOtherMessageHolder extends OtherMessageViewHolder {

        public DeletedOtherMessageHolder(@NonNull View itemView, OnEmojiClickListener onEmojiClickListener, ShowAlertDialogEmojisInMessage showAlertDialogEmojisInMessage) {
            super(itemView, onEmojiClickListener,showAlertDialogEmojisInMessage);
        }

    }

    public static class FirstMessageIfMatchConversationHolder extends RecyclerView.ViewHolder {

        TextView matchDateString;

        public FirstMessageIfMatchConversationHolder(@NonNull View itemView) {
            super(itemView);

            matchDateString = itemView.findViewById(R.id.match_date_string);
        }

    }


    public static class FirstMessageIfPrivateConversationHolder extends RecyclerView.ViewHolder {

        ImageView otherUserProfileImage;
        TextView otherUsername;
        TextView otherRegion;


        public FirstMessageIfPrivateConversationHolder(@NonNull View itemView) {
            super(itemView);

            otherUserProfileImage = itemView.findViewById(R.id.other_user_profile_image);
            otherUsername = itemView.findViewById(R.id.other_username);
            otherRegion = itemView.findViewById(R.id.other_region);

        }

    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = chatMessagesList.get(position);

        if (chatMessage == null)
            return VIEW_TYPE_LOADING;

        if (chatMessage instanceof FakeChatMessageForUser)
            return VIEW_TYPE_CHAT_MESSAGE_FOR_USER;

        if (chatMessage.isDeleted() && chatMessage.getUserShortForm().getId().equals(userId))
            return VIEW_TYPE_DELETED_USER;

        if (chatMessage.isDeleted() && !chatMessage.getUserShortForm().getId().equals(userId))
            return VIEW_TYPE_DELETED_OTHER;

        if (chatMessage.getMessageType().equals(ChatMessageType.USER_JOINED))
            return VIEW_TYPE_USER_JOINED;

        if (chatMessage.getMessageType().equals(ChatMessageType.USER_LEFT))
            return VIEW_TYPE_USER_LEFT;

        if (chatMessage.getMessageType().equals(ChatMessageType.PHONE_ENABLED))
            return VIEW_TYPE_PHONE_ENABLED;

        if (chatMessage.getMessageType().equals(ChatMessageType.PHONE_DISABLED))
            return VIEW_TYPE_PHONE_DISABLED;

        if (chatMessage.isFirstMessage()) {

            if (chat.isPrivateConversation())
                return VIEW_TYPE_FIRST_MESSAGE_PRIVATE_CONVERSATION;
            else
                return VIEW_TYPE_FIRST_MESSAGE_MATCH_CONVERSATION;

        }

        if (chatMessage.getUserShortForm().getId().equals(userId))
            return VIEW_TYPE_USER_TEXT_MESSAGE;


        return VIEW_TYPE_OTHER_TEXT_MESSAGE;
    }
}
