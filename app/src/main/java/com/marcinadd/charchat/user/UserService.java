package com.marcinadd.charchat.user;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.marcinadd.charchat.R;

public class UserService {
    private static final UserService ourInstance = new UserService();

    private UserService() {
    }

    public static UserService getInstance() {
        return ourInstance;
    }

    public void setSidebarData(View headerView, @Nullable FirebaseUser user) {
        if (user != null) {
            TextView username = headerView.findViewById(R.id.navigation_header_username);
            TextView email = headerView.findViewById(R.id.navigation_header_email);
            username.setText(user.getDisplayName());
            email.setText(user.getEmail());
        }
    }

}
