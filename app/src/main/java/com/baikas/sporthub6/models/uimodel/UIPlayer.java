package com.baikas.sporthub6.models.uimodel;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.models.user.UserShortForm;

import de.hdodenhof.circleimageview.CircleImageView;

public class UIPlayer {

    public Context context;
    public ImageView profileImage;
    public TextView name;
    public TextView level;
    public TextView age;
    public TextView region;

    public UIPlayer(Context context){
        this.context = context;
    }

    public void mapWithUser(User user){

        SetImageWithGlide.setImageWithGlideOrDefaultImage(user.getProfileImageUrl(),profileImage ,context);

        String name = user.getFirstName() + " " + user.getLastName();
        this.name.setText(name);

        if (this.level != null)
            this.level.setVisibility(View.GONE);

        this.age.setText(user.getAge() + " Ετών");
        this.region.setText(user.getRegion());
    }

    public void mapWithUserWithSport(User user, String sport){

        SetImageWithGlide.setImageWithGlideOrDefaultImage(user.getProfileImageUrl(),profileImage,context);

        String name = user.getFirstName() + " " + user.getLastName();
        this.name.setText(name);

        long level = user.getUserLevels().get(sport).getLevel();
        String userLevel = "Επίπεδο: " + level;
        this.level.setText(userLevel);//todo change that to image
        this.age.setText(user.getAge() + " Ετών");
        this.region.setText(user.getRegion());
    }

    public void mapWithUserShortForm(UserShortForm user){

        SetImageWithGlide.setImageWithGlideOrDefaultImage(user.getProfileImageUrl(),profileImage,context);


        String name = user.getFirstName() + " " + user.getLastName();
        this.name.setText(name);

        if (user.getOneSpecifiedSport() != null) {
            long level = user.getOneSpecifiedSport().getLevel();
            String userLevel = "Επίπεδο: " + level;
            this.level.setText(userLevel);
        }else{
            this.level.setVisibility(View.GONE);
        }

        this.age.setText(user.getAge() + " Ετών");
        this.region.setText(user.getRegion());
    }

}
