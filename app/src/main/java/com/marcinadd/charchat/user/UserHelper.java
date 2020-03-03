package com.marcinadd.charchat.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.marcinadd.charchat.LoginActivity;
import com.marcinadd.charchat.NavigationDrawerActivity;
import com.marcinadd.charchat.R;

public class UserHelper {
    private static final UserHelper ourInstance = new UserHelper();

    private UserHelper() {
    }

    public static UserHelper getInstance() {
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

    public void startNavigationDrawer(Activity activity, boolean registerFCMToken) {
        if (registerFCMToken) {
            UserService.getInstance().registerFCMToken();
        }
        activity.startActivity(new Intent(activity, NavigationDrawerActivity.class));
        activity.finish();
    }

    public void saveCurrentUsernameInSharedPreferences(String username, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(LoginActivity.CURRENT_USER_USERNAME, username).apply();
    }
}
