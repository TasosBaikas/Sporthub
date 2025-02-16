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
import com.baikas.sporthub6.adapters.matchesshow.seewhorequested.SeeWhoRequestedAdapter;
import com.baikas.sporthub6.helpers.managers.ToastManager;
import com.baikas.sporthub6.interfaces.NotifyOtherFragment;
import com.baikas.sporthub6.interfaces.gonext.OpenUserProfile;
import com.baikas.sporthub6.listeners.OnClickListenerPass2CompletableFuture;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.constants.JoinOrIgnore;
import com.baikas.sporthub6.models.result.Result;
import com.baikas.sporthub6.models.user.User;
import com.baikas.sporthub6.viewmodels.matches.seewhorequested.SeeWhoRequestedFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SeeWhoRequestedFragment extends Fragment implements OnClickListenerPass2CompletableFuture<String, JoinOrIgnore>, NotifyOtherFragment {


    private SeeWhoRequestedFragmentViewModel viewModel;
    private OpenUserProfile openUserProfile;
    private ProgressBar loadingBar;
    private SeeWhoRequestedAdapter adapter;
    private RecyclerView recyclerView;

    public SeeWhoRequestedFragment() {}


    public static SeeWhoRequestedFragment newInstance(Bundle bundle) {
        SeeWhoRequestedFragment fragment = new SeeWhoRequestedFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try{
            this.openUserProfile = (OpenUserProfile) context;
        }catch (ClassCastException e){
            throw new RuntimeException("should impl openUserProfile");
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
        return inflater.inflate(R.layout.fragment_see_who_requested, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        viewModel = new ViewModelProvider(this).get(SeeWhoRequestedFragmentViewModel.class);

        Bundle bundle = getArguments();
        if (bundle == null){
            ToastManager.showToast(getContext(),"reopen the app", Toast.LENGTH_SHORT);
            getActivity().finish();
            return;
        }

        viewModel.setMatchId(bundle.getString("matchId"));
        viewModel.setSport(bundle.getString("sport"));

        recyclerView = requireView().findViewById(R.id.recycler_view_show_users_who_requested);


        loadingBar = requireView().findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);


        viewModel.getMatchById(viewModel.getMatchId(), viewModel.getSport());

        viewModel.getMessageToUserLiveData().observe(getViewLifecycleOwner(), (String message) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.GONE);

            ToastManager.showToast(getContext(), message, Toast.LENGTH_SHORT);
        });


        viewModel.getCurrentMatchLiveData().observe(getViewLifecycleOwner(),(Match currentMatch) -> {

            List<String> usersRequestedAndNotIgnored = new ArrayList<>(currentMatch.getUserRequestsToJoinMatch());

            usersRequestedAndNotIgnored.removeAll(currentMatch.getUsersInChat());

            usersRequestedAndNotIgnored.removeAll(currentMatch.getAdminIgnoredRequesters());


            if (adapter == null){
                adapter = new SeeWhoRequestedAdapter(viewModel.getUsersRequestedLiveData().getValue(),viewModel.getCurrentMatch(),this,openUserProfile);
                recyclerView.setAdapter(adapter);
                WeakReference<RecyclerView> weakRef = new WeakReference<>(recyclerView);
                new Handler().postDelayed(() -> {
                    if (weakRef.get() == null)
                        return;

                    weakRef.get().smoothScrollBy(1,1);
                },2000);


                recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
            }

            viewModel.findUsersThatHaveRequestedButNotIgnored(usersRequestedAndNotIgnored);

            adapter.submitUpdatedMatch(currentMatch);
        });


        viewModel.getUsersRequestedLiveData().observe(getViewLifecycleOwner(),(List<User> users) -> {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED)
                return;

            loadingBar.setVisibility(View.GONE);

            for (String userId:viewModel.getCurrentMatch().getUsersInChat()) {
                users.removeIf((User user) -> user.getId().equals(userId));
            }


            TextView noRequestsTextView = requireView().findViewById(R.id.no_requests_text_view);
            if (users.isEmpty()){
                noRequestsTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);

                return;
            }
            noRequestsTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            adapter.submitNewRequestersList(users);
            adapter.notifyDataSetChanged();
        });



    }


    @Override
    public CompletableFuture<Void> onClick(String requesterId, JoinOrIgnore joinOrIgnore) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        loadingBar.setVisibility(View.VISIBLE);

        String yourId = FirebaseAuth.getInstance().getUid();
        viewModel.joinOrIgnore(requesterId,yourId,joinOrIgnore,viewModel.getCurrentMatch()).observe(this,(Result<Void> result) ->{
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

    }
}