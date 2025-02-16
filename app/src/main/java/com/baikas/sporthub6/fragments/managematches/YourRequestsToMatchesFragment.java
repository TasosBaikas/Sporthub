package com.baikas.sporthub6.fragments.managematches;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.matchesshow.YourRequestsAdapter;
import com.baikas.sporthub6.helpers.internet.CheckInternetConnection;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.helpers.operations.ListOperations;
import com.baikas.sporthub6.interfaces.UpdateSpecificMatch;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.listeners.OnClickJoinMatch;
import com.baikas.sporthub6.listeners.OnClickShowMatchDetails;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.updates.DiffCallBackGeneralized;
import com.baikas.sporthub6.viewmodels.matches.YourRequestsToMatchesFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class YourRequestsToMatchesFragment extends Fragment implements OnClickJoinMatch,OpenUserProfile, OnClickShowMatchDetails, UpdateSpecificMatch {


    private ProgressBar loadingBar;

    private YourRequestsToMatchesFragmentViewModel viewModel;

    private OpenUserProfile showUserProfileDialog;
    private OnClickShowMatchDetails onClickShowMatchDetailsAlertDialog;


    public YourRequestsToMatchesFragment() {
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_your_requests_to_matches, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(YourRequestsToMatchesFragmentViewModel.class);

        viewModel.loadFromServerMatchesThatYouRequested(FirebaseAuth.getInstance().getUid());

        RecyclerView recyclerView = requireView().findViewById(R.id.recycler_view_your_requests_to_matches);

        YourRequestsAdapter adapter = new YourRequestsAdapter(viewModel.getMatches(),this,this,this);

        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));

        ProgressBar loadingBar = requireView().findViewById(R.id.loadingBar);

        loadingBar.setVisibility(View.VISIBLE);


        viewModel.getMatchesLiveData().observe(getViewLifecycleOwner(),(List<Match> newMatchesList) -> {
            if (!newMatchesList.isEmpty())
                loadingBar.setVisibility(View.GONE);


            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBackGeneralized<>(adapter.getCurrentList(), newMatchesList));
            adapter.submitList(newMatchesList);
            diffResult.dispatchUpdatesTo(adapter);
        });

        viewModel.getMessageToUserLiveData().observe(getViewLifecycleOwner(),(String message)->{
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            ToastManager.showToast(getContext(),message,Toast.LENGTH_SHORT);
        });

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

        viewModel.userToJoinMatch(requesterId, matchId, sport).observe(getViewLifecycleOwner(),(unused) -> {
            completableFuture.complete(null);
        });

        return completableFuture;
    }

    @Override
    public void updateSpecificMatch(Match match) {

        ListOperations.setElementToList(match,viewModel.getMatches());
        viewModel.getMatchesLiveData().setValue(viewModel.getMatches());

    }

    @Override
    public CompletableFuture<Void> onClickShowMatchDetails(Match match) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        onClickShowMatchDetailsAlertDialog.onClickShowMatchDetails(match)
                .whenComplete((o1,e) -> completableFuture.complete(null));

        return completableFuture;
    }

    @Override
    public CompletableFuture<Void> openUserProfile(String userId, @Nullable String sport) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        if (!isAdded() || getContext() == null)
            return completableFuture;


        showUserProfileDialog.openUserProfile(userId, null)
                .whenComplete((o,e) -> completableFuture.complete(null));

        return completableFuture;
    }
}