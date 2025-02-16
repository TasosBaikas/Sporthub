package com.baikas.sporthub6.helpers.images;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.baikas.sporthub6.R;
import com.bumptech.glide.Glide;

public class SetImageWithGlide {

    public static void setImageWithGlideOrClear(@NonNull String profileImageUrl, ImageView imageView, Context context){

        if (profileImageUrl.isEmpty()) {
            Glide.with(context).clear(imageView);

            return;
        }

        Glide.with(context)
                .load(profileImageUrl)
                .centerCrop()
                .error(R.drawable.no_profile_image_svg)
                .into(imageView);
    }


    public static void setImageWithGlideOrDefaultImage(@NonNull String profileImageUrl, ImageView imageView, Context context){

        if (profileImageUrl.isEmpty()) {
            Glide.with(context)
                    .load(R.drawable.no_profile_image_svg)
                    .centerCrop()
                    .error(R.drawable.no_profile_image_svg)
                    .into(imageView);

            return;
        }

        Glide.with(context)
                .load(profileImageUrl)
                .centerCrop()
                .error(R.drawable.no_profile_image_svg)
                .into(imageView);

    }

}
