package com.baikas.sporthub6.fragments.mainpage.terrainnearby;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.interfaces.NotifyToUpdateImage;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.mainpage.terrainnearby.TerrainNearbyFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;

@AndroidEntryPoint
public class TerrainNearbyFragment extends Fragment implements NotifyToUpdateImage {//todo add viewmodel hilt

    TerrainNearbyFragmentViewModel viewModel;
    ImageView userImageButton;

    public TerrainNearbyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_terrain_nearby, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(TerrainNearbyFragmentViewModel.class);

        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);

        userImageButton = requireView().findViewById(R.id.user_image_button);

        userImageButton.setOnClickListener((imageButton -> drawerLayout.openDrawer(GravityCompat.END)));

        viewModel.getUserById(FirebaseAuth.getInstance().getUid()).observe(getViewLifecycleOwner(),((User user) -> {

            SetImageWithGlide.setImageWithGlideOrDefaultImage(user.getProfileImageUrl(), userImageButton, getContext());
        }));

    }

    @Override
    public void notifyToUpdateImage(Uri photoUri) {
        if (userImageButton == null || photoUri == null)
            return;

        userImageButton.setImageURI(photoUri);
    }
}
