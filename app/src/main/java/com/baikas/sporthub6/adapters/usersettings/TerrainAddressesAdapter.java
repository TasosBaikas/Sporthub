package com.baikas.sporthub6.adapters.usersettings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.listeners.OnClickListenerPass1;
import com.baikas.sporthub6.models.fakemessages.FakeTerrainAddressForMessage;
import com.baikas.sporthub6.models.TerrainAddress;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TerrainAddressesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<TerrainAddress> terrainAddressList;
    private int VIEW_TYPE_MESSAGE_TO_USER = 0;
    private int VIEW_TYPE_ITEM = 1;
    private final OnClickListenerPass1<TerrainAddress> onAddressClick;


    public TerrainAddressesAdapter(List<TerrainAddress> terrainAddressList, OnClickListenerPass1<TerrainAddress> onAddressClick) {
        this.terrainAddressList = terrainAddressList;
        this.onAddressClick = onAddressClick;
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return terrainAddressList.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        View frameLayout;
        View backgroundLayout;
        TextView terrainTitle;
        TextView terrainAddress;
        View viewSmallLine;

        public ViewHolder(@NotNull View view) {
            super(view);

            frameLayout = view.findViewById(R.id.item_frame_layout);
            backgroundLayout = view.findViewById(R.id.background_layout);

            terrainTitle = view.findViewById(R.id.terrain_title);
            terrainAddress = view.findViewById(R.id.terrain_address_text_view);
            viewSmallLine = view.findViewById(R.id.view_small_line);


        }
    }

    public static class MessageToUserHolder extends RecyclerView.ViewHolder {

        TextView messageToUser;

        public MessageToUserHolder(@NotNull View view) {
            super(view);

            messageToUser = view.findViewById(R.id.text_view_message_to_user);

        }
    }


    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        if (viewType == VIEW_TYPE_MESSAGE_TO_USER){
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_message_to_user, viewGroup, false);

            return new MessageToUserHolder(view);
        }

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_terrain_address, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {

        if (viewHolder instanceof MessageToUserHolder){
            MessageToUserHolder messageToUserHolder = (MessageToUserHolder) viewHolder;

            FakeTerrainAddressForMessage fakeAddressMessage = (FakeTerrainAddressForMessage) terrainAddressList.get(position);

            messageToUserHolder.messageToUser.setText(fakeAddressMessage.getMessageToUser());
            return;
        }


        TerrainAddress terrainAddress = terrainAddressList.get(position);

        ViewHolder realViewHolder = (ViewHolder) viewHolder;


        if (position % 2 == 0){
            realViewHolder.backgroundLayout.setBackgroundColor(ContextCompat.getColor(realViewHolder.itemView.getContext(), R.color.grey_mode));
        }else{
            realViewHolder.backgroundLayout.setBackgroundColor(ContextCompat.getColor(realViewHolder.itemView.getContext(), R.color.white_or_black_primary_total));
        }


        realViewHolder.terrainTitle.setText(terrainAddress.getAddressTitle());
        realViewHolder.terrainAddress.setText(terrainAddress.getAddress());


        realViewHolder.frameLayout.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {

            onAddressClick.onClick(terrainAddress);
            }
        });


        realViewHolder.viewSmallLine.setBackgroundColor(ContextCompat.getColor(realViewHolder.itemView.getContext(), SportConstants.SPORTS_MAP.get(terrainAddress.getSport()).getSportColor()));

    }

    @Override
    public int getItemViewType(int position) {
        TerrainAddress terrainAddress = terrainAddressList.get(position);

        if (terrainAddress instanceof FakeTerrainAddressForMessage)
            return VIEW_TYPE_MESSAGE_TO_USER;

        return VIEW_TYPE_ITEM;
    }
}
