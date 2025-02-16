package com.baikas.sporthub6.fragments.managematches.seewhorequested;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.matchesshow.seewhorequested.SeeIgnoredAdapter;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.interfaces.NotifyOtherFragment;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.listeners.OnClickListenerPass2CompletableFuture;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.constants.JoinOrIgnore;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.matches.seewhorequested.SeeIgnoredFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class SeeIgnoredFragment extends Fragment implements OnClickListenerPass2CompletableFuture<String, JoinOrIgnore>, NotifyOtherFragment {

    SeeIgnoredFragmentViewModel viewModel;
    ProgressBar loadingBar;
    OpenUserProfile openUserProfile;
    SeeIgnoredAdapter adapter;
    RecyclerView recyclerView;


    public static SeeIgnoredFragment newInstance(Bundle bundle) {
        SeeIgnoredFragment fragment = new SeeIgnoredFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try{
            openUserProfile = (OpenUserProfile) context;
        }catch(ClassCastException e){
            throw new RuntimeException("must impl openuserprofile");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_see_ignored, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SeeIgnoredFragmentViewModel.class);

        Bundle bundle = getArguments();
        if (bundle == null){
            ToastManager.showToast(getContext(),"reopen the app", Toast.LENGTH_SHORT);
            getActivity().finish();
            return;
        }

        viewModel.setMatchId(bundle.getString("matchId"));
        viewModel.setSport(bundle.getString("sport"));

        recyclerView = requireView().findViewById(R.id.recycler_view_show_ignored);


        loadingBar = requireView().findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);


        viewModel.getMatchById(viewModel.getMatchId(), viewModel.getSport());


        viewModel.getMessageToUserLiveData().observe(getViewLifecycleOwner(), (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.GONE);

            ToastManager.showToast(getContext(), message, Toast.LENGTH_SHORT);
        });

        viewModel.getCurrentMatchLiveData().observe(getViewLifecycleOwner(), (Match newMatch) -> {

            if (adapter == null) {
                adapter = new SeeIgnoredAdapter(viewModel.getUsersThatRequestedLiveData().getValue(), newMatch, this, openUserProfile);
                recyclerView.setAdapter(adapter);
                WeakReference<RecyclerView> weakRef = new WeakReference<>(recyclerView);
                new Handler().postDelayed(() -> {
                    if (weakRef.get() == null)
                        return;

                    weakRef.get().smoothScrollBy(1,1);
                },2000);

                recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
            }

            viewModel.findUsersThatAreIgnored(newMatch.getAdminIgnoredRequesters());
            adapter.submitUpdatedMatch(newMatch);
        });


        viewModel.getUsersThatRequestedLiveData().observe(getViewLifecycleOwner(), (List<User> usersUpdated) -> {
            loadingBar.setVisibility(View.GONE);

            TextView noIgnoredTextView = requireView().findViewById(R.id.no_ignored_text_view);
            if (usersUpdated == null || usersUpdated.isEmpty()){
                noIgnoredTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);

                return;
            }
            noIgnoredTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            adapter.submitNewRequestersList(usersUpdated);
            adapter.notifyDataSetChanged();
        });

    }


    @Override
    public CompletableFuture<Void> onClick(String requesterId, JoinOrIgnore joinOrIgnore) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        loadingBar.setVisibility(View.VISIBLE);

        String adminId = FirebaseAuth.getInstance().getUid();

        Match match = viewModel.getCurrentMatchLiveData().getValue();
        if (match == null || !match.isAdmin(adminId)) {
            completableFuture.completeExceptionally(new RuntimeException());
            return completableFuture;
        }

        viewModel.acceptIgnored(requesterId,adminId,match).observe(this,(Result<Void> result) ->{
            loadingBar.setVisibility(View.GONE);

            if (result instanceof Result.Failure){
                Throwable throwable = ((Result.Failure<Void>) result).getThrowable();

                ToastManager.showToast(getContext(),throwable.getMessage(),Toast.LENGTH_SHORT);
                completableFuture.completeExceptionally(throwable);
                return;
            }

            this.notifyFragments();
            completableFuture.complete(null);
        });


        return completableFuture;
    }


    @Override
    public void notifyFragments() {


        for (Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
            if (fragment == this)
                continue;

            if (fragment instanceof NotifyOtherFragment) {
                ((NotifyOtherFragment) fragment).handleNotify();
            }
        }
    }

    @Override
    public void handleNotify() {

        viewModel.getMatchById(viewModel.getMatchId(), viewModel.getSport());
    }
}