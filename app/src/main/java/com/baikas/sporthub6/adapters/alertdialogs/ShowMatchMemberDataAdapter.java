package com.baikas.sporthub6.adapters.alertdialogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.listeners.OnClickCreatePrivateConversation;
import com.baikas.sporthub6.models.user.User;
import com.bumptech.glide.Glide;

import java.util.List;

public class ShowMatchMemberDataAdapter extends RecyclerView.Adapter<ShowMatchMemberDataAdapter.ViewHolder>{

    private final List<User> userList;
    private final String sport;
    private final String yourId;
    private final OpenUserProfile openUserProfile;


    public ShowMatchMemberDataAdapter(List<User> userList, String sport, String yourId, OpenUserProfile openUserProfile) {
        this.userList = userList;
        this.sport = sport;
        this.yourId = yourId;
        this.openUserProfile = openUserProfile;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView userProfileImage;
        TextView firstNameLastNameDotted;
        TextView ageOfMatchMember;

        public ViewHolder(View view) {
            super(view);

            userProfileImage = view.findViewById(R.id.user_profile_image);
            firstNameLastNameDotted = view.findViewById(R.id.first_name_last_name_dotted_text_view);
            ageOfMatchMember = view.findViewById(R.id.age_of_match_member);
        }

    }



    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_match_member_show_data, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

        User user = userList.get(position);

        SetImageWithGlide.setImageWithGlideOrDefaultImage(user.getProfileImageUrl(), viewHolder.userProfileImage, viewHolder.itemView.getContext());

        viewHolder.firstNameLastNameDotted.setText(user.getFirstName() + " " + user.getLastName().charAt(0) + ".");

        viewHolder.ageOfMatchMember.setText(user.getAge() + " Ετών");

        viewHolder.userProfileImage.setOnClickListener(new Prevent2ClicksListener() {
            @Override
            public void onClickExecuteCode(View v) {
                openUserProfile.openUserProfile(user.getUserId(), sport);
            }
        });
    }

}
