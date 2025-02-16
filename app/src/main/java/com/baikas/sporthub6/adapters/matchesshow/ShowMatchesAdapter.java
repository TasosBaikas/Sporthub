package com.baikas.sporthub6.adapters.matchesshow;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.models.constants.MatchDurationConstants;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.converters.DpToPxConverter;
import com.baikas.sporthub6.helpers.time.GreekDateFormatter;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.listeners.OnClickListenerPass1;
import com.baikas.sporthub6.models.fakemessages.FakeMatchForMessage;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.Sport;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShowMatchesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private final int VIEW_TYPE_FAKE_MATCH_MESSAGE = 0;
    private final int VIEW_TYPE_MATCH = 1;
    private final int VIEW_TYPE_MESSAGE_TO_USER_2_LINES = 2;
    private List<Match> userMatches;
    private final OnClickListenerPass1<Match> matchClick;



    public ShowMatchesAdapter(List<Match> userMatches, OnClickListenerPass1<Match> matchClick) {
        this.userMatches = userMatches;
        this.matchClick = matchClick;
    }

    public List<Match> getCurrentList() {
        return this.userMatches;
    }


    public void submitList(List<Match> newMessages) {
        if (this.userMatches == null) {
            this.userMatches = new ArrayList<>();
            this.userMatches.addAll(newMessages);
            return;
        }


        this.userMatches.clear();
        this.userMatches.addAll(newMessages);
    }



    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return userMatches.size();
    }

    public static class FakeMatchViewHolder extends RecyclerView.ViewHolder {

        TextView textViewMessageToUser;
        public FakeMatchViewHolder(@NotNull View view) {
            super(view);

            textViewMessageToUser = view.findViewById(R.id.text_view_message_to_user);

        }
    }

    public static class MessageToUser2LinesHolder extends RecyclerView.ViewHolder {

        TextView firstLine;
        ImageView imageView;

        public MessageToUser2LinesHolder(@NotNull View view) {
            super(view);

            firstLine = view.findViewById(R.id.text_view_first_line);
            imageView = view.findViewById(R.id.imageView2);

        }
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        View frameLayout;
        View backgroundLayout;
        ImageView sportImage;
        View fire1;
        View fire2;
        View fire3;
        TextView timeAndDayOfMatch;
        TextView ifMoreThan1Week;
        TextView matchDurationTextView;
        TextView teamMembersTextView;

        LinearLayout linearLayout3;
        ImageView crownIcon;
        ImageView notificationIcon;
        TextView notificationTextView;
        View viewSmallLine;

        public ViewHolder(@NotNull View view) {
            super(view);

            frameLayout = view.findViewById(R.id.frame_layout_background);
            backgroundLayout = view.findViewById(R.id.background_layout);

            sportImage = view.findViewById(R.id.sport_image);

            fire1 = view.findViewById(R.id.fire1);
            fire2 = view.findViewById(R.id.fire2);
            fire3 = view.findViewById(R.id.fire3);

            timeAndDayOfMatch = view.findViewById(R.id.time_and_day_of_match);
            ifMoreThan1Week = view.findViewById(R.id.if_more_than_week);
            matchDurationTextView = view.findViewById(R.id.match_duration);

            teamMembersTextView = view.findViewById(R.id.members_count);

            linearLayout3 = view.findViewById(R.id.linearLayout3);


            crownIcon = view.findViewById(R.id.crown_icon);
            notificationIcon = view.findViewById(R.id.notification_icon);
            notificationTextView = view.findViewById(R.id.notification_text_view);

            viewSmallLine = view.findViewById(R.id.view_small_line);
        }

        public void calculateNotificationLogic(Match match,Set<String> usersRequestedButNotSeenByAdmin,String userId){

            if (!match.isAdmin(userId)){
                crownIcon.setVisibility(View.GONE);
                notificationIcon.setVisibility(View.GONE);
                notificationTextView.setVisibility(View.GONE);

                return;
            }

            crownIcon.setVisibility(View.VISIBLE);

            if (!usersRequestedButNotSeenByAdmin.isEmpty()) {

                notificationIcon.setVisibility(View.VISIBLE);
                notificationTextView.setVisibility(View.VISIBLE);


                if (usersRequestedButNotSeenByAdmin.size() == 1)
                    notificationTextView.setText("1 αίτηση σε εκκρεμότητα");
                else
                    notificationTextView.setText( usersRequestedButNotSeenByAdmin.size() + " αιτήσεις σε εκκρεμότητα");

            }else {
                notificationIcon.setVisibility(View.GONE);
                notificationTextView.setVisibility(View.GONE);
            }

        }

    }



    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        if (viewType == VIEW_TYPE_FAKE_MATCH_MESSAGE){
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_message_to_user, viewGroup, false);

            return new FakeMatchViewHolder(view);
        }

        if (viewType == VIEW_TYPE_MESSAGE_TO_USER_2_LINES){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message_to_user_2_lines_create_new_match,viewGroup,false);
            return new MessageToUser2LinesHolder(view);
        }


        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_show_your_matches, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {

        if (viewHolder instanceof MessageToUser2LinesHolder){
            MessageToUser2LinesHolder messageHolder = ((MessageToUser2LinesHolder) viewHolder);

            messageHolder.firstLine.setText("Δεν είστε σε ενεργές ομάδες");

            messageHolder.imageView.setImageResource(R.drawable.plus_sign_green_50px);
            return;
        }


        if (viewHolder instanceof FakeMatchViewHolder){
            FakeMatchViewHolder messageHolder = ((FakeMatchViewHolder) viewHolder);

            FakeMatchForMessage fakeMatchForMessage = ((FakeMatchForMessage) userMatches.get(position));

            messageHolder.textViewMessageToUser.setText(fakeMatchForMessage.getMessageToUser());
            return;
        }

        ViewHolder realViewHolder = (ViewHolder) viewHolder;

        Match match = userMatches.get(position);


        realViewHolder.frameLayout.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {
                matchClick.onClick(match);
            }
        });


        if (position % 2 == 0){
            realViewHolder.backgroundLayout.setBackgroundColor(ContextCompat.getColor(realViewHolder.itemView.getContext(), R.color.grey_mode));
        }else{
            realViewHolder.backgroundLayout.setBackgroundColor(ContextCompat.getColor(realViewHolder.itemView.getContext(), R.color.white_or_black_primary_total));
        }


        long matchTime = match.getMatchDateInUTC();

        Sport sport = SportConstants.SPORTS_MAP.get(match.getSport());
        realViewHolder.sportImage.setImageResource(sport.getSportImageId());//add -1 check


        int teamMembers = (new HashSet<>(match.getUsersInChat())).size();
        realViewHolder.teamMembersTextView.setText(String.valueOf(teamMembers));


        int hours3 = 3 * 60 * 60 * 1000;
        int hours6 = 2 * hours3;
        int hours9 = 3 * hours3;


        if (matchTime < TimeFromInternet.getInternetTimeEpochUTC() + hours3)
            realViewHolder.fire1.setVisibility(View.VISIBLE);
        else
            realViewHolder.fire1.setVisibility(View.GONE);

        if (matchTime < TimeFromInternet.getInternetTimeEpochUTC() + hours6)
            realViewHolder.fire2.setVisibility(View.VISIBLE);
        else
            realViewHolder.fire2.setVisibility(View.GONE);

        if (matchTime < TimeFromInternet.getInternetTimeEpochUTC() + hours9)
            realViewHolder.fire3.setVisibility(View.VISIBLE);
        else
            realViewHolder.fire3.setVisibility(View.GONE);



        String matchDateString = GreekDateFormatter.epochToDayAndTime(match.getMatchDateInUTC());

        if (GreekDateFormatter.diffLessThan1Week(TimeFromInternet.getInternetTimeEpochUTC(), matchTime)){
            realViewHolder.ifMoreThan1Week.setVisibility(View.GONE);
        }else {
            realViewHolder.ifMoreThan1Week.setVisibility(View.VISIBLE);
            realViewHolder.ifMoreThan1Week.setText("(" + GreekDateFormatter.epochToFormattedDayAndMonth(matchTime) + ")");
        }

        realViewHolder.timeAndDayOfMatch.setText(matchDateString);

        Pair<Long,Long> hoursAndMinutes = MatchDurationConstants.formatMatchDuration(match.getMatchDuration());

        long hours = hoursAndMinutes.first;
        long minutes = hoursAndMinutes.second;

        String matchDurationText = "";
        if (3 * 60 * 60 * 1000 < match.getMatchDuration())
            matchDurationText = "Διάρκεια αγώνα πάνω από 3 ώρες";
        else if (hours == 1 && minutes > 0)
            matchDurationText = "Διάρκεια αγώνα 1 ώρα και " + minutes + " λεπτά";
        else if (hours == 1 && minutes == 0)
            matchDurationText = "Διάρκεια αγώνα 1 ώρα";
        else if (hours > 0 && minutes > 0)
            matchDurationText =  "Διάρκεια αγώνα " + hours + " ώρες και " + minutes + " λεπτά";
        else if (hours > 0 && minutes == 0)
            matchDurationText =  "Διάρκεια αγώνα " + hours + " ώρες";


        realViewHolder.matchDurationTextView.setText(matchDurationText);


        //icons

        String userId = FirebaseAuth.getInstance().getUid();


        Set<String> usersRequestedButNotSeenByAdmin = new HashSet<>(match.getUserRequestsToJoinMatch());

        match.getAdminIgnoredRequesters().forEach(usersRequestedButNotSeenByAdmin::remove);
        match.getUsersInChat().forEach(usersRequestedButNotSeenByAdmin::remove);


        realViewHolder.calculateNotificationLogic(match,usersRequestedButNotSeenByAdmin,userId);


        realViewHolder.viewSmallLine.setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.getContext(), SportConstants.SPORTS_MAP.get(match.getSport()).getSportColor()));


    }


    @Override
    public int getItemViewType(int position) {

        if (userMatches.size() == 1 && userMatches.get(0) instanceof FakeMatchForMessage && userMatches.get(0).getId().equals("NoMoreTeams"))
            return VIEW_TYPE_MESSAGE_TO_USER_2_LINES;

        Match match = userMatches.get(position);
        if (match instanceof FakeMatchForMessage)
            return VIEW_TYPE_FAKE_MATCH_MESSAGE;

        return VIEW_TYPE_MATCH;
    }
}
