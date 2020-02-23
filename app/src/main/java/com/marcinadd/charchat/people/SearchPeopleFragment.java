package com.marcinadd.charchat.people;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.marcinadd.charchat.R;
import com.marcinadd.charchat.chat.model.User;
import com.marcinadd.charchat.people.service.OnPeopleSearchLoadedListener;
import com.marcinadd.charchat.people.service.PeopleService;

import java.util.ArrayList;
import java.util.List;

public class SearchPeopleFragment extends Fragment implements OnPeopleSearchLoadedListener {
    private ProgressBar progressLoader;
    private MyUserRecyclerViewAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        View mView = inflater.inflate(R.layout.fragment_search_people, container, false);
        progressLoader = mView.findViewById(R.id.progress_loader);
        RecyclerView recyclerView = mView.findViewById(R.id.recyclerView);

        initRecycler(recyclerView);

        return mView;
    }

    private void initRecycler(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        List<User> users = new ArrayList<>();
        adapter = new MyUserRecyclerViewAdapter(users, null);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_search_people_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                progressLoader.setVisibility(View.VISIBLE);
                PeopleService.getInstance().queryUsersByUsernamePart(query, SearchPeopleFragment.this);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        progressLoader = null;
        adapter = null;
        super.onDestroy();
    }

    @Override
    public void onPeopleLoadedListener(List<User> users) {
        adapter.setData(users);
        progressLoader.setVisibility(View.GONE);
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(User item);
    }
}
