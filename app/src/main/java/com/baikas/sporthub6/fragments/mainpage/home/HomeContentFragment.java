package com.baikas.sporthub6.fragments.mainpage.home;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.mainpage.home.MatchesAdapter;
import com.baikas.sporthub6.adapters.skeleton.SkeletonAdapter;
import com.baikas.sporthub6.helpers.comparators.MatchComparator;
import com.baikas.sporthub6.helpers.time.TimeFromInternet;
import com.baikas.sporthub6.interfaces.chat.AddOrRemoveLoadingBar;
import com.baikas.sporthub6.interfaces.chat.NotifyToUpdatePinnedMessage;
import com.baikas.sporthub6.interfaces.chat.ShowAlertDialogEmojisInMessage;
import com.baikas.sporthub6.models.chat.fakemessages.FakeChatMessageForUser;
import com.baikas.sporthub6.models.constants.ConfigurationsConstants;
import com.baikas.sporthub6.models.constants.SportConstants;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.operations.ListOperations;
import com.baikas.sporthub6.interfaces.NotifyFragmentsToScrollToTop;
import com.baikas.sporthub6.interfaces.UpdateSpecificMatch;
import com.baikas.sporthub6.listeners.OnClickJoinMatch;
import com.baikas.sporthub6.listeners.OnClickShowMatchDetails;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.MatchFilter;
import com.baikas.sporthub6.models.RequestMatchesFromServer;
import com.baikas.sporthub6.models.fakemessages.FakeMatchForMessage;
import com.baikas.sporthub6.models.updates.DiffCallBackGeneralized;
import com.baikas.sporthub6.viewmodels.mainpage.home.HomeContentFragmentViewModel;
import com.google.android.material.appbar.AppBarLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeContentFragment extends Fragment implements OnClickJoinMatch, OpenUserProfile,
        OnClickShowMatchDetails, NotifyFragmentsToScrollToTop, UpdateSpecificMatch {

    private ProgressBar loadingBar;
    private MatchesAdapter matchesAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private HomeContentFragmentViewModel viewModel;

    private RecyclerView matchesRecyclerView;
    private OpenUserProfile showUserProfileDialog;
    private OnClickShowMatchDetails onClickShowMatchDetailsAlertDialog;

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;


    public HomeContentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try{
            showUserProfileDialog = (OpenUserProfile) context;
        }catch (ClassCastException e){
            throw new RuntimeException("CLass must impl OpenUserProfile");
        }

        try{
            onClickShowMatchDetailsAlertDialog = (OnClickShowMatchDetails) context;
        }catch (ClassCastException e){
            throw new RuntimeException("CLass must impl OnClickShowMatchDetails");
        }

    }

    public static HomeContentFragment newInstance(Bundle bundle) {
        HomeContentFragment fragment = new HomeContentFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connectivityManager = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {

                if (viewModel == null)
                    return;

                RequestMatchesFromServer requestMatchesFromServer = viewModel.getRequestMatchesFromServer();
                // Network is available
                viewModel.loadDataFromServer(requestMatchesFromServer);
                Log.d("NetworkCallback", "Network is active");
            }
            @Override
            public void onLost(Network network) {
                noInternetConnection();
            }

        };

    }


    private void noInternetConnection(){

        // Network is unavailable
        if (viewModel == null || !viewModel.getMatches().isEmpty())
            return;


        new Handler(Looper.getMainLooper()).post(() -> {

            viewModel.getMatches().add(new FakeMatchForMessage("Δεν έχετε internet"));

            loadingBar.setVisibility(View.INVISIBLE);

            viewModel.getMatchesLiveData().postValue(viewModel.getMatches());
            Log.d("NetworkCallback", "Network is lost");

        });

    }


    @Override
    public void onStart() {
        super.onStart();

        if (!CheckInternetConnection.isNetworkConnected(getContext())){
            noInternetConnection();
        }

        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_content, container, false);
    }

    private final int MAX_LOADING_TIME = ConfigurationsConstants.TIMEOUT_TIME;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(HomeContentFragmentViewModel.class);

        Bundle bundle = getArguments();

        RequestMatchesFromServer requestMatchesFromServer = viewModel.getRequestMatchesFromServer();
        if (bundle != null){
            requestMatchesFromServer.setDateBeginForPaginationUTC(bundle.getLong("dateBeginForPaginationUTC"));
            requestMatchesFromServer.setDateLastForPaginationUTC(bundle.getLong("dateLastForPaginationUTC"));
            requestMatchesFromServer.setSport(bundle.getString("sport"));
            requestMatchesFromServer.setMatchFilter((MatchFilter) bundle.getSerializable("matchFilter"));
        }


        loadingBar = requireView().findViewById(R.id.loadingBar);

        loadingBar.setIndeterminateTintList(ContextCompat.getColorStateList(getContext(), SportConstants.SPORTS_MAP.get(requestMatchesFromServer.getSport()).getSportColor()));
        loadingBar.setVisibility(View.VISIBLE);

        matchesRecyclerView = this.requireView().findViewById(R.id.home_matches_recycler_view);


        matchesRecyclerView.setAdapter(new SkeletonAdapter(viewModel.MAX_ITEMS_PER_SCROLL));

        matchesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));


        viewModel.getMatchesLiveData().observe(getViewLifecycleOwner(), newMatchesList -> {
            fromSkeletonToRealAdapter();

            doBeforeTheDiffUtil();


            boolean shouldScrollToTop = ifNewMatchScrollToTop(matchesAdapter.getCurrentList(), newMatchesList);

            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBackGeneralized<>(matchesAdapter.getCurrentList(), newMatchesList));
            matchesAdapter.submitList(newMatchesList);
            diffResult.dispatchUpdatesTo(matchesAdapter);

            if (shouldScrollToTop)
                matchesRecyclerView.scrollToPosition(0);

        });

        viewModel.getMessageToUserLiveData().observe(getViewLifecycleOwner(),(message -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.GONE);

            ToastManager.showToast(getActivity(), message, Toast.LENGTH_SHORT);
        }));


        swipeRefreshLayout = requireView().findViewById(R.id.swipe_refresh_layout);

        String sport = viewModel.getRequestMatchesFromServer().getSport();
        swipeRefreshLayout.setColorSchemeResources(SportConstants.SPORTS_MAP.get(sport).getSportColor());

        swipeRefreshLayout.setOnRefreshListener(()->{
                if (!isAdded())
                    return;

                viewModel.clearData();
                viewModel.loadDataFromServer(requestMatchesFromServer);
        });



        matchesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (viewModel.isLoading())
                    return;

                if (viewModel.isNoMoreItemsToLoad())
                    return;

                if (recyclerView.getLayoutManager() == null)
                    return;

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();


//                if (dx==0 && dy==0)
//                    return;

                int mainMatchesSize = viewModel.getMatches().size();
                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() >= mainMatchesSize - 2) {
                    //top of chat!
                    viewModel.setLoading(true);
                    getMoreData();
                }
            }
        });

    }

    private void doBeforeTheDiffUtil() {

        boolean containsNull = viewModel.getMatches().contains(null);
        viewModel.getMatches().removeAll(Collections.singleton(null));

        viewModel.getMatches().removeIf((Match match) -> match != null && match.getMatchDateInUTC() < TimeFromInternet.getInternetTimeEpochUTC());

        viewModel.getMatches().sort(new MatchComparator());

        viewModel.addDayDelimiters(viewModel.getMatches());

        if (containsNull)
            viewModel.getMatches().add(null);
    }

    private boolean ifNewMatchScrollToTop(List<Match> currentList, List<Match> newMatchList) {
        if (currentList.isEmpty() || newMatchList.isEmpty())
            return true;

        if (currentList.get(0) == null || newMatchList.get(0) == null || newMatchList.get(0) instanceof FakeMatchForMessage)
            return true;

        String oldMatch = currentList.get(0).getId();
        String newMatch = newMatchList.get(0).getId();
        if (!oldMatch.equals(newMatch))
            return true;

        return false;
    }



    public void getMoreData() {
        if (matchesRecyclerView.getAdapter() instanceof SkeletonAdapter)
            return;


        viewModel.setLoading(true);

        List<Match> matchesFakeRef = viewModel.getMatches();
        matchesFakeRef.add(null);
        viewModel.getMatchesLiveData().setValue(matchesFakeRef);


        if (!CheckInternetConnection.isNetworkConnected(getContext())){

            new Handler().postDelayed(()->{

                viewModel.setLoading(false);

                viewModel.getMatches().removeAll(Collections.singleton(null));
                viewModel.getMatchesLiveData().setValue(matchesFakeRef);
            },800);

            return;
        }

        new Handler().postDelayed(()->{
            viewModel.getMoreData(viewModel.getRequestMatchesFromServer());
        },100);
    }

    public void fromSkeletonToRealAdapter(){
        if (matchesRecyclerView == null)
            return;


        if (matchesAdapter == null)
            matchesAdapter = new MatchesAdapter(new ArrayList<>(),this, viewModel.getRequestMatchesFromServer().getSport(),this,this);

        setAdapterWithDelay(matchesAdapter,0,false);
        setAdapterWithDelay(matchesAdapter,MAX_LOADING_TIME,true);
    }

    private void setAdapterWithDelay(MatchesAdapter matchesAdapter, long loadMoreTime, boolean mandatoryUpdate) {
        WeakReference<HomeContentFragment> fragmentWeakReference = new WeakReference<>(this);

        new Handler().postDelayed(() -> {

            HomeContentFragment homeContentFragment = fragmentWeakReference.get();
            if (homeContentFragment == null)
                return;

            if (!viewModel.getMatches().isEmpty() || mandatoryUpdate) {

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                loadingBar.setVisibility(View.GONE);

                if (matchesRecyclerView.getAdapter() instanceof SkeletonAdapter) {
                    if (!(matchesRecyclerView.getAdapter() instanceof MatchesAdapter)) {
                        matchesRecyclerView.setAdapter(matchesAdapter);

                        WeakReference<RecyclerView> weakRef = new WeakReference<>(matchesRecyclerView);
                        new Handler().postDelayed(() -> {
                            if (weakRef.get() == null)
                                return;

                            weakRef.get().smoothScrollBy(1,1);
                        },2000);
                    }
                }
            }
        }, loadMoreTime);
    }




    @Override
    public CompletableFuture<Void> onClickJoinMatch(String requesterId, String matchId, String sport) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        if (!isAdded() || getContext() == null) {
            completableFuture.completeExceptionally(new RuntimeException());
            return completableFuture;
        }
        
        if (!CheckInternetConnection.isNetworkConnected(getContext())){
            ToastManager.showToast(getContext(), "No internet connection!", Toast.LENGTH_SHORT);
            completableFuture.completeExceptionally(new RuntimeException());
            return completableFuture;
        }

        loadingBar.setVisibility(View.VISIBLE);
        viewModel.userToJoinMatch(requesterId, matchId, sport).observe(getViewLifecycleOwner(),(Match updatedMatch) -> {
            loadingBar.setVisibility(View.GONE);
            if (getActivity() == null)
                return;

            for (Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
                if (fragment instanceof UpdateSpecificMatch) {
                    ((UpdateSpecificMatch) fragment).updateSpecificMatch(updatedMatch);
                }
            }

            completableFuture.complete(null);
        });

        return completableFuture;
    }


    @Override
    public CompletableFuture<Void> onClickShowMatchDetails(Match match) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();


        loadingBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(()->{
            loadingBar.setVisibility(View.GONE);
            completableFuture.complete(null);
        }, 6000);


        onClickShowMatchDetailsAlertDialog.onClickShowMatchDetails(match)
                .whenComplete((o1,e) -> {
                    loadingBar.setVisibility(View.GONE);
                    completableFuture.complete(null);
                });

        return completableFuture;
    }

    @Override
    public CompletableFuture<Void> openUserProfile(String userId, @Nullable String sport) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        if (!isAdded() || getContext() == null)
            return completableFuture;

        loadingBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(()->{
            loadingBar.setVisibility(View.GONE);
            completableFuture.complete(null);
        }, 6000);

        showUserProfileDialog.openUserProfile(userId, sport)
                .whenComplete((o,e)-> {
                    loadingBar.setVisibility(View.GONE);
                    completableFuture.complete(null);
                });

        return completableFuture;
    }

    @Override
    public void scrollToTop() {
        if (matchesRecyclerView == null)
            return;

        matchesRecyclerView.smoothScrollToPosition(0);
        if (getActivity() == null)
            return;

        AppBarLayout appBarLayout = getActivity().findViewById(R.id.app_bar_layout);
        if (appBarLayout != null)
            appBarLayout.setExpanded(true, true);

    }

    @Override
    public void updateSpecificMatch(Match match) {

        ListOperations.setElementToList(match,viewModel.getMatches());
        viewModel.getMatchesLiveData().setValue(viewModel.getMatches());

    }


}