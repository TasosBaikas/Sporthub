package com.baikas.sporthub6.adapters.mainpage.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.models.constants.HasTerrainTypes;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.listeners.OnClickJoinMatch;
import com.baikas.sporthub6.listeners.OnClickShowMatchDetails;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.R;
import com.baikas.sporthub6.helpers.time.GreekDateFormatter;
import com.baikas.sporthub6.models.fakemessages.FakeMatchDayDelimiter;
import com.baikas.sporthub6.models.fakemessages.FakeMatchForMessage;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.uimodel.UIPlayer;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int VIEW_TYPE_LOADING = -1;
    private static final int VIEW_TYPE_MESSAGE_TO_USER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_MESSAGE_TO_USER_2_LINES = 2;
    private static final int VIEW_TYPE_DAY_DELIMITER = 3;

    private List<Match> mainMatches;
    private String sport;
    private final OnClickJoinMatch onClickJoinMatch;
    private final OpenUserProfile onClickSeeProfileOfUserListener;
    private final OnClickShowMatchDetails onClickShowMatchDetails;


    public MatchesAdapter(List<Match> mainMatches, OnClickJoinMatch onClickJoinMatch, String sport,
                          OpenUserProfile onClickSeeProfileOfUserListener, OnClickShowMatchDetails onClickShowMatchDetails) {
        this.mainMatches = mainMatches;
        this.onClickJoinMatch = onClickJoinMatch;

        this.sport = sport;
        this.onClickSeeProfileOfUserListener = onClickSeeProfileOfUserListener;
        this.onClickShowMatchDetails = onClickShowMatchDetails;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mainMatches.size();
    }

    public List<Match> getCurrentList() {
        return this.mainMatches;
    }


    public void submitList(List<Match> newMatches) {
        if (this.mainMatches == null) {
            this.mainMatches = new ArrayList<>();
            this.mainMatches.addAll(newMatches);
            return;
        }


        this.mainMatches.clear();
        this.mainMatches.addAll(newMatches);
    }




    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        View frameLayout;
        View backgroundLayout;
        View fire1;
        View fire2;
        View fire3;

        TextView teamTitle;
        TextView ifMoreThanAWeek;

        UIPlayer player1;

        TextView teamMembersTextView;
        TextView haveTerrainTextView;
        TextView alreadyJoinedTextView;
        MaterialButton joinMatchButton;

        View viewSmallLine;


        public ViewHolder(@NotNull View view) {
            super(view);

            frameLayout = view.findViewById(R.id.item_match_frame_layout);

            backgroundLayout = view.findViewById(R.id.background_layout);


            teamTitle = view.findViewById(R.id.team_title);
            ifMoreThanAWeek = view.findViewById(R.id.if_more_than_week);

            player1 = new UIPlayer(view.getContext());
            player1.profileImage = view.findViewById(R.id.player1_profile_image);
            player1.name = view.findViewById(R.id.player1_name);
            player1.level = view.findViewById(R.id.player1_level);
            player1.age = view.findViewById(R.id.player1_age);
            player1.region = view.findViewById(R.id.player1_region);

            fire1 = view.findViewById(R.id.fire1);
            fire2 = view.findViewById(R.id.fire2);
            fire3 = view.findViewById(R.id.fire3);



            teamMembersTextView = view.findViewById(R.id.members_count);
            haveTerrainTextView = view.findViewById(R.id.have_terrain_text_view);

            alreadyJoinedTextView = view.findViewById(R.id.already_joined);
            joinMatchButton = view.findViewById(R.id.join_match);

            viewSmallLine = view.findViewById(R.id.view_small_line);
        }


        public void addJoinButtonLogic(Match match, String userId, OnClickJoinMatch onClickJoinMatch) {

            if (match.isMember(userId)) {
                alreadyJoinedTextView.setVisibility(View.VISIBLE);
                joinMatchButton.setVisibility(View.INVISIBLE);
                return;
            }

            alreadyJoinedTextView.setVisibility(View.INVISIBLE);
            joinMatchButton.setVisibility(View.VISIBLE);

            String text;
            if (match.isUserRequestedToJoin(userId)) {
                text = "Ακύρωση";

                joinMatchButton.setStrokeColorResource(R.color.red);
                joinMatchButton.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.red));
            } else {
                text = "Κάνε\nΑίτηση";

                joinMatchButton.setStrokeColorResource(R.color.green);
                joinMatchButton.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.green));
            }

            joinMatchButton.setText(text);
            joinMatchButton.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    String requesterId = FirebaseAuth.getInstance().getUid();
                    onClickJoinMatch.onClickJoinMatch(requesterId, match.getId(), match.getSport());
                }
            });

        }
    }

    public static class LoadingHolder extends RecyclerView.ViewHolder {

        ProgressBar loadingBar;

        public LoadingHolder(@NotNull View view) {
            super(view);

            loadingBar = view.findViewById(R.id.progress_circular);
        }
    }

    public static class DayDelimiterHolder extends RecyclerView.ViewHolder {


        public DayDelimiterHolder(@NotNull View view) {
            super(view);

        }
    }

    public static class MessageToUserHolder extends RecyclerView.ViewHolder {

        TextView textViewMessageToUser;
        public MessageToUserHolder(@NotNull View view) {
            super(view);

            textViewMessageToUser = view.findViewById(R.id.text_view_message_to_user);

        }
    }

    public static class MessageToUser2LinesHolder extends RecyclerView.ViewHolder {

        public MessageToUser2LinesHolder(@NotNull View view) {
            super(view);
        }
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_TYPE_LOADING){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_loading,viewGroup,false);
            return new LoadingHolder(view);
        }

        if (viewType == VIEW_TYPE_MESSAGE_TO_USER){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message_to_user,viewGroup,false);
            return new MessageToUserHolder(view);
        }

        if (viewType == VIEW_TYPE_MESSAGE_TO_USER_2_LINES){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message_to_user_2_lines_create_new_match,viewGroup,false);
            return new MessageToUser2LinesHolder(view);
        }

        if (viewType == VIEW_TYPE_DAY_DELIMITER){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_team_1v1_day_dilimiter,viewGroup,false);
            return new DayDelimiterHolder(view);
        }

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_team_1v1, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof LoadingHolder) {
            LoadingHolder loadingHolder = (LoadingHolder) viewHolder;

            loadingHolder.loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(loadingHolder.itemView.getContext(), SportConstants.SPORTS_MAP.get(sport).getSportColor()));
            return;
        }


        if (viewHolder instanceof DayDelimiterHolder) {
            return;
        }

        if (viewHolder instanceof MessageToUserHolder){
            MessageToUserHolder messageHolder = ((MessageToUserHolder) viewHolder);

            FakeMatchForMessage fakeMatchForMessage = ((FakeMatchForMessage) mainMatches.get(position));

            messageHolder.textViewMessageToUser.setText(fakeMatchForMessage.getMessageToUser());
            return;
        }

        if (viewHolder instanceof MessageToUser2LinesHolder){
            return;
        }


        Match match = mainMatches.get(position);

        ViewHolder realViewHolder = (ViewHolder) viewHolder;


        realViewHolder.frameLayout.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                onClickShowMatchDetails.onClickShowMatchDetails(match);
            }
        });

        long countDelimiters = mainMatches.subList(0,position).stream()
                .filter((Match matchTemp) -> matchTemp instanceof FakeMatchDayDelimiter)
                .count();

        if ((position + countDelimiters) % 2 == 0){
            realViewHolder.backgroundLayout.setBackgroundColor(ContextCompat.getColor(realViewHolder.itemView.getContext(), R.color.grey_mode));
        }else{
            realViewHolder.backgroundLayout.setBackgroundColor(ContextCompat.getColor(realViewHolder.itemView.getContext(), R.color.white_or_black_primary_total));
        }


        realViewHolder.player1.profileImage.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

                onClickSeeProfileOfUserListener.openUserProfile(match.getAdmin().getUserId(), sport);
            }
        });

        realViewHolder.player1.mapWithUserShortForm(match.getAdmin());

        long matchTime = match.getMatchDateInUTC();
        String timeAndDayAthensTime = GreekDateFormatter.epochToDayAndTime(matchTime);


        if (GreekDateFormatter.diffLessThan1Week(TimeFromInternet.getInternetTimeEpochUTC(), matchTime)){
            realViewHolder.ifMoreThanAWeek.setVisibility(View.INVISIBLE);
        }else {
            realViewHolder.ifMoreThanAWeek.setVisibility(View.VISIBLE);
            realViewHolder.ifMoreThanAWeek.setText("(" + GreekDateFormatter.epochToFormattedDayAndMonth(matchTime) + ")");
        }

        realViewHolder.teamTitle.setText(timeAndDayAthensTime);


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


        int teamMembers = (new HashSet<>(match.getUsersInChat())).size();
        realViewHolder.teamMembersTextView.setVisibility(View.VISIBLE);
        realViewHolder.teamMembersTextView.setText(String.valueOf(teamMembers));

        String hasTerrainInFormForShowMatch = HasTerrainTypes.TERRAIN_OPTIONS_TO_GREEK_SHORT_FORM_MAP.get(match.getHasTerrainType());
        realViewHolder.haveTerrainTextView.setText(hasTerrainInFormForShowMatch);

        if (match.getHasTerrainType().equals(HasTerrainTypes.I_HAVE_CERTAIN_TERRAIN)){
            realViewHolder.haveTerrainTextView.setTextColor(ContextCompat.getColor(realViewHolder.itemView.getContext(), R.color.green));
        }else if (match.getHasTerrainType().equals(HasTerrainTypes.I_HAVE_NOT_CERTAIN_TERRAIN)){
            realViewHolder.haveTerrainTextView.setTextColor(ContextCompat.getColor(realViewHolder.itemView.getContext(), R.color.orange));
        }else
            realViewHolder.haveTerrainTextView.setTextColor(ContextCompat.getColor(realViewHolder.itemView.getContext(), R.color.red));


        String userId = FirebaseAuth.getInstance().getUid();

        realViewHolder.addJoinButtonLogic(match, userId, onClickJoinMatch);


        realViewHolder.viewSmallLine.setBackgroundColor(ContextCompat.getColor(realViewHolder.itemView.getContext(), SportConstants.SPORTS_MAP.get(sport).getSportColor()));


    }


    @Override
    public int getItemViewType(int position) {
        if (mainMatches.get(position) == null)
            return VIEW_TYPE_LOADING;

        if (mainMatches.size() == 1 && mainMatches.get(0) instanceof FakeMatchForMessage && mainMatches.get(0).getId().equals("NoMoreTeams"))
            return VIEW_TYPE_MESSAGE_TO_USER_2_LINES;

        if (mainMatches.get(position) instanceof FakeMatchForMessage)
            return VIEW_TYPE_MESSAGE_TO_USER;

        if (mainMatches.get(position) instanceof FakeMatchDayDelimiter)
            return VIEW_TYPE_DAY_DELIMITER;

        return VIEW_TYPE_ITEM;
    }




}
