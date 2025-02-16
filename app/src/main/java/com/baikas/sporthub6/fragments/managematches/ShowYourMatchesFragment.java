package com.baikas.sporthub6.fragments.managematches;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.baikas.sporthub6.R;
import com.baikas.sporthub6.adapters.matchesshow.ShowMatchesAdapter;
import com.baikas.sporthub6.interfaces.AttachListeners;
import com.baikas.sporthub6.interfaces.gonext.GoToSeeWhoRequestedActivity;
import com.baikas.sporthub6.listeners.OnClickListenerPass1;
import com.baikas.sporthub6.interfaces.RemoveListeners;
import com.baikas.sporthub6.models.Match;
import com.baikas.sporthub6.models.chat.Chat;
import com.baikas.sporthub6.models.chat.fakemessages.FakeChatForMessage;
import com.baikas.sporthub6.models.fakemessages.FakeMatchForMessage;
import com.baikas.sporthub6.models.updates.DiffCallBackGeneralized;
import com.baikas.sporthub6.viewmodels.matches.ShowYourMatchesFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ShowYourMatchesFragment extends Fragment implements OnClickListenerPass1<Match>,RemoveListeners, AttachListeners {

    private ShowYourMatchesFragmentViewModel viewModel;
    private GoToSeeWhoRequestedActivity goToSeeWhoRequestedActivity;

    public ShowYourMatchesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            goToSeeWhoRequestedActivity = (GoToSeeWhoRequestedActivity) context;
        }catch (ClassCastException e){
            throw new ClassCastException("The activity must implement GoToSeeWhoRequestedActivity");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show_your_matches, container, false);
    }

    @Override
    public void attachListeners(){
        viewModel.initYourMatchesAllSportsListener(FirebaseAuth.getInstance().getUid());
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() == null)
            return;

        this.viewModel = new ViewModelProvider(this).get(ShowYourMatchesFragmentViewModel.class);

        viewModel.initYourMatchesAllSportsListener(FirebaseAuth.getInstance().getUid());

        RecyclerView showMatchesRecyclerView = requireView().findViewById(R.id.recycler_view_show_user_matches);


        ShowMatchesAdapter adapter = new ShowMatchesAdapter(new ArrayList<>(),this);

        showMatchesRecyclerView.setAdapter(adapter);


        showMatchesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));

        ProgressBar loadingBar = requireView().findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.VISIBLE);

        viewModel.getUserMatchesLiveData().observe(getViewLifecycleOwner(),(List<Match> newMatchesList) -> {

            if (!newMatchesList.isEmpty())
                loadingBar.setVisibility(View.GONE);

            boolean shouldScrollToTop = this.ifNewMatchScrollToTop(adapter.getCurrentList(),newMatchesList);

            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBackGeneralized<>(adapter.getCurrentList(), newMatchesList));
            adapter.submitList(newMatchesList);
            diffResult.dispatchUpdatesTo(adapter);


            if (shouldScrollToTop)
                showMatchesRecyclerView.scrollToPosition(0);
        });



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



    @Override
    public void onClick(Match match) {
        goToSeeWhoRequestedActivity.openAlertDialogOptions(match);
    }

    @Override
    public void removeListeners(){
        viewModel.removeListeners();
    }
}