package com.marcinadd.charchat.ui.people;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.marcinadd.charchat.R;
import com.marcinadd.charchat.chat.db.model.Chat;
import com.marcinadd.charchat.chat.model.User;
import com.marcinadd.charchat.chat.service.ChatService;
import com.marcinadd.charchat.chat.service.listener.OnChatCreatedListener;
import com.marcinadd.charchat.chat.service.listener.OnChatsLoadedListener;
import com.marcinadd.charchat.people.OnUserListFragmentInteractionListener;
import com.marcinadd.charchat.people.service.OnPeopleSearchLoadedListener;
import com.marcinadd.charchat.people.service.PeopleService;

import java.util.ArrayList;
import java.util.List;

public class SearchPeopleFragment extends Fragment implements OnPeopleSearchLoadedListener, OnUserListFragmentInteractionListener, OnChatsLoadedListener {
    private ProgressBar progressLoader;
    private MyUserRecyclerViewAdapter adapter;
    private TextView searchCenterText;
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_search_people, container, false);
        progressLoader = mView.findViewById(R.id.progress_loader);
        searchCenterText = mView.findViewById(R.id.searchCenterText);

        RecyclerView recyclerView = mView.findViewById(R.id.recyclerView);
        initRecycler(recyclerView);

        return mView;
    }

    private void initRecycler(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        List<User> users = new ArrayList<>();
        adapter = new MyUserRecyclerViewAdapter(users, this);
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
        if (users.size() != 0) {
            searchCenterText.setVisibility(View.GONE);
        } else {
            searchCenterText.setText(getResources().getString(R.string.not_found));
            searchCenterText.setVisibility(View.VISIBLE);
        }
        progressLoader.setVisibility(View.GONE);

    }

    @Override
    public void onListFragmentInteraction(User item) {
        //TODO Implement chat loading

        ChatService.getInstance().getChatsForOtherAndCurrentUser(item.getId(), item.getName(), this);
    }

    @Override
    public void onChatsLoaded(List<Chat> chats, String otherUserUid, final String otherUserUsername) {
        if (chats.size() == 0) {
            // Create new chat and enter it
            ChatService.getInstance().createNewChat(otherUserUid, otherUserUsername, new OnChatCreatedListener() {
                @Override
                public void onChatCreated(String userUid, String username, String chatId) {
                    navigateToChat(userUid, chatId, otherUserUsername);
                }
            });
        } else {
            //TODO Show list of anonymous chats
            navigateToChat(otherUserUid, chats.get(0).getId(), otherUserUsername);
        }
    }

    private void navigateToChat(final String userUid, final String chatId, final String username) {
        SearchPeopleFragmentDirections.ActionNavSearchToMessagesListFragment action = SearchPeopleFragmentDirections.actionNavSearchToMessagesListFragment();
        action.setUserUid(userUid);
        action.setChatUid(chatId);
        action.setChatName(username);
        Navigation.findNavController(mView).navigate(action);
    }
}
