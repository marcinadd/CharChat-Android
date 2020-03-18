package com.marcinadd.charchat.user.avatar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.marcinadd.charchat.NavigationDrawerActivity;
import com.marcinadd.charchat.R;


public class AvatarClickDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.avatar_dialog_title)
                .setItems(R.array.avatar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                break;
                            case 1:
                                if (getActivity() != null)
                                    ((NavigationDrawerActivity) getActivity()).onChangeAvatar();
                                break;
                        }
                    }
                });
        return builder.create();
    }
}
