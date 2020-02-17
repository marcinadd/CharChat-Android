package com.marcinadd.charchat.ui.chat;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.marcinadd.charchat.R;
import com.marcinadd.charchat.chat.model.Dialog;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class DialogsListFragment extends Fragment {


    public DialogsListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_dialogs_list, container, false);
        DialogsList dialogsListView = mView.findViewById(R.id.dialogsList);
        DialogsListAdapter dialogsListAdapter = new DialogsListAdapter<Dialog>(null);
        dialogsListView.setAdapter(dialogsListAdapter);
        return mView;
    }

}
