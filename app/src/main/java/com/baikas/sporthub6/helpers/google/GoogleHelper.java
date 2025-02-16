package com.baikas.sporthub6.helpers.google;

import android.content.Context;

import com.baikas.sporthub6.R;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class GoogleHelper {

    public static GoogleSignInOptions getGoogleSignInOptions(Context context){

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.serverClientIdForGoogle))
                .requestEmail()
                .build();

        return googleSignInOptions;
    }


}
