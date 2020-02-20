package com.marcinadd.charchat.ui.chat;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.marcinadd.charchat.R;
import com.marcinadd.charchat.chat.model.Dialog;
import com.marcinadd.charchat.chat.model.User;
import com.marcinadd.charchat.chat.service.ChatService;
import com.marcinadd.charchat.chat.service.OnDialogsLoadedListener;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.List;

public class DialogsListFragment extends Fragment
        implements OnDialogsLoadedListener, DialogsListAdapter.OnDialogClickListener {

    private DialogsListAdapter dialogsListAdapter;
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_dialogs_list, container, false);
        DialogsList dialogsListView = mView.findViewById(R.id.dialogsList);
        dialogsListAdapter = new DialogsListAdapter<Dialog>(null);
        dialogsListView.setAdapter(dialogsListAdapter);
        dialogsListAdapter.setOnDialogClickListener(this);
        ChatService.getInstance().getChats(this);
        return mView;
    }

    @Override
    public void onDialogsLoaded(List<Dialog> dialogs) {
        dialogsListAdapter.setItems(dialogs);
    }

    @Override
    public void onDialogClick(IDialog dialog) {
        DialogsListFragmentDirections.ActionDialogsListFragmentToMessagesListFragment action =
                DialogsListFragmentDirections.actionDialogsListFragmentToMessagesListFragment();
        User user = (User) dialog.getUsers().get(0);
        action.setUserUid(user.getId());
        action.setChatUid(dialog.getId());
        Navigation.findNavController(mView).navigate(action);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mView = null;
    }
}
