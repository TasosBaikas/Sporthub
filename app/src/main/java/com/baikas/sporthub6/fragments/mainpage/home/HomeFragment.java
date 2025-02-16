package com.baikas.sporthub6.fragments.mainpage.home;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.baikas.sporthub6.customclasses.Prevent2ClicksListener;
import com.baikas.sporthub6.helpers.comparators.UserLevelBasedOnSportComparator;
import com.baikas.sporthub6.helpers.images.SetImageWithGlide;
import com.baikas.sporthub6.interfaces.MatchFilterUpdated;
import com.baikas.sporthub6.interfaces.NotifyToUpdateImage;
import com.baikas.sporthub6.interfaces.RequestMatchFilter;
import com.baikas.sporthub6.interfaces.gonext.GoToEditSportPriorities;
import com.baikas.sporthub6.interfaces.gonext.GoToMatchFilterActivity;
import com.baikas.sporthub6.listeners.OnClickListenerPassNothing;
import com.baikas.sporthub6.models.MatchFilter;
import com.baikas.sporthub6.models.Sport;
import com.baikas.sporthub6.models.user.UserLevelBasedOnSport;
import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.mainpage.home.SportAdapter;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.listeners.OnClickListenerPass1;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.mainpage.home.HomeFragmentViewModel;
import com.baikas.sporthub6.viewpageradapters.ViewPager2HomeFragmentsAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;

@AndroidEntryPoint
public class HomeFragment extends Fragment implements OnClickListenerPass1<String>, OnClickListenerPassNothing, NotifyToUpdateImage,
        MatchFilterUpdated, RequestMatchFilter {

    private RecyclerView sportRecyclerView;
    private SportAdapter sportAdapter;
    private HomeFragmentViewModel viewModel;
    private ViewPager2 viewPager;
    private CircleImageView userImageButton;
    private GoToEditSportPriorities goToEditSportPriorities;
    private GoToMatchFilterActivity goToMatchFilterActivity;
    private ProgressBar loadingBar;

    public HomeFragment() {
        // Required empty public constructor
    }
    public static HomeFragment newInstance(Bundle bundle) {
        HomeFragment fragment = new HomeFragment();

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            goToEditSportPriorities = (GoToEditSportPriorities) context;
        }catch (ClassCastException e){
            throw new RuntimeException("activity must impl GoToEditSportPriorities");
        }

        try {
            goToMatchFilterActivity = (GoToMatchFilterActivity) context;
        }catch (ClassCastException e){
            throw new RuntimeException("activity must impl goToMatchFilterActivity");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeFragmentViewModel.class);

        Bundle bundle = getArguments();
        if (bundle != null){
            viewModel.getMatchFilterLiveData().setValue((MatchFilter) bundle.get("matchFilter"));
        }

        loadingBar = view.findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);

        viewModel.getMatchFilterLiveData().observe(getViewLifecycleOwner(), (MatchFilter matchFilter) -> {

            View tickIcon = requireView().findViewById(R.id.filtered_set_show_tick_icon);
            if (matchFilter.isEnabled()){
                tickIcon.setVisibility(View.VISIBLE);
            }else
                tickIcon.setVisibility(View.INVISIBLE);

        });

        viewModel.getUserById(FirebaseAuth.getInstance().getUid()).observe(getViewLifecycleOwner(), (User user) -> {

            if (viewModel.firstInit()){

                Optional<UserLevelBasedOnSport> userLevelBasedOnSportOptional = user.getUserLevels().values().stream()
                                .filter((UserLevelBasedOnSport userLevelBasedOnSport) -> userLevelBasedOnSport.getPriority() == 1)
                                .findAny();

                AtomicReference<String> atomicReference = new AtomicReference<>();
                if (userLevelBasedOnSportOptional.isPresent())
                    atomicReference.set(userLevelBasedOnSportOptional.get().getSportName());
                else
                    atomicReference.set(SportConstants.TENNIS);

                viewModel.getSelectedSportLiveData().setValue(atomicReference);
            }


            if (getActivity() == null)
                return;


            View goToMatchFilter = requireView().findViewById(R.id.go_to_match_filter_activity);
            goToMatchFilter.setOnClickListener(new Prevent2ClicksListener() {
                @Override
                public void onClickExecuteCode(View v) {

                    goToMatchFilterActivity.goToMatchFilterActivity();
                }
            });

            viewPager = requireView().findViewById(R.id.viewPager);
            TabLayout tabLayout = requireView().findViewById(R.id.tabLayout);


//            AtomicReference<String> selectedSport = viewModel.getSelectedSportLiveData().getValue();

            viewModel.getSelectedSportLiveData().observe(getViewLifecycleOwner(), (AtomicReference<String> selectedSport) -> {
                loadingBar.setVisibility(View.INVISIBLE);

                if (this.sportAdapter != null)
                    this.sportAdapter.notifyDataSetChanged();


                viewPager.setSaveEnabled(false);
                viewPager.setAdapter(new ViewPager2HomeFragmentsAdapter(getActivity(), viewModel.getFragmentsBundles(selectedSport.get())));

                List<String> daysAndMonthsFormatted = viewModel.getDayAndMonthFormatted();
                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    if (getContext() == null)
                        return;

                    tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(getContext(), SportConstants.SPORTS_MAP.get(selectedSport.get()).getSportColor()));

                    tab.setText(daysAndMonthsFormatted.get(position)); // Set tab names here
                }).attach();

                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        viewPager.setCurrentItem(tab.getPosition(), false);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });

            });



            DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
            userImageButton = requireView().findViewById(R.id.user_image_button);

            userImageButton.setOnClickListener((imageButton -> drawerLayout.openDrawer(GravityCompat.END)));

            SetImageWithGlide.setImageWithGlideOrDefaultImage(user.getProfileImageUrl(),userImageButton,getContext());

            sportRecyclerView = requireView().findViewById(R.id.sport_recycler_view);


            List<Sport> sportList = user.getUserLevels().values().stream()
                    .filter((UserLevelBasedOnSport::isEnabled))
                    .sorted(new UserLevelBasedOnSportComparator())
                    .map((UserLevelBasedOnSport userLevelBasedOnSport) -> SportConstants.SPORTS_MAP.get(userLevelBasedOnSport.getSportName()))
                    .collect(Collectors.toList());

            sportList.add(null);
            sportAdapter = new SportAdapter(sportList, viewModel.getSelectedSportLiveData().getValue(), this,this);

            sportRecyclerView.setAdapter(sportAdapter);

            LinearLayoutManager horizontalLayoutManager
                    = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

            sportRecyclerView.setLayoutManager(horizontalLayoutManager);



        });


//        String selectedSportPosition = viewModel.getSelectedSportLiveData().getValue().get();
//        this.smoothScrollToSelectedSport(SportConstants.SPORTSLIST.indexOf(selectedSportPosition));


    }



    private void smoothScrollToSelectedSport(Integer selectedSportPosition){
        sportRecyclerView.smoothScrollBy((selectedSportPosition * ((int) getResources().getDimension(R.dimen.sport_image_width) + (int) getResources().getDimension(R.dimen.margin_start_sport_images)) - 700), 0);// approximate calculation
    }


    private void changeViewPagerAdapter(String sport) {
        if (getActivity() == null)
            return;

        List<Bundle> fragmentBundles = viewModel.getFragmentsBundles(sport);

        viewPager.setAdapter(new ViewPager2HomeFragmentsAdapter(getActivity(), fragmentBundles));

        this.sportAdapter.notifyDataSetChanged();

//        this.smoothScrollToSelectedSport(SportConstants.SPORTSLIST.indexOf(sport));
    }


    @Override
    public void onClick(String selectedSport) {

        AtomicReference<String> selectedSportAtomic = viewModel.getSelectedSportLiveData().getValue();

        if (selectedSportAtomic.get().equals(selectedSport))
            return;

        selectedSportAtomic.set(selectedSport);

        viewModel.getSelectedSportLiveData().setValue(viewModel.getSelectedSportLiveData().getValue());
    }

    @Override
    public void onClick() {
        goToEditSportPriorities.goToEditSportPriorities();
    }

    @Override
    public void notifyToUpdateImage(Uri photoUri) {
        if (userImageButton == null || photoUri == null)
            return;

        userImageButton.setImageURI(photoUri);
    }

    @Override
    public void matchFilterUpdated(MatchFilter updatedMatchFilter) {
        if (updatedMatchFilter == null || viewPager == null)
            return;

        viewModel.getMatchFilterLiveData().setValue(updatedMatchFilter);

        if (getActivity() == null)
            return;

        String selectedSport = viewModel.getSelectedSportLiveData().getValue().get();
        List<Bundle> fragmentBundles = viewModel.getFragmentsBundles(selectedSport);

        viewPager.setAdapter(new ViewPager2HomeFragmentsAdapter(getActivity(), fragmentBundles));
    }

    @Override
    public MatchFilter requestMatchFilter() {
        return viewModel.getMatchFilterLiveData().getValue();
    }
}
