package com.marcinadd.charchat.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.marcinadd.charchat.LoginActivity;
import com.marcinadd.charchat.NavigationDrawerActivity;
import com.marcinadd.charchat.R;
import com.marcinadd.charchat.chat.db.model.FieldNames;
import com.marcinadd.charchat.chat.model.User;
import com.marcinadd.charchat.user.avatar.AvatarHelper;

public class UserHelper {
    private static final UserHelper ourInstance = new UserHelper();

    private UserHelper() {
    }

    public static UserHelper getInstance() {
        return ourInstance;
    }

    public void setSidebarData(final View headerView, FirebaseUser user, final Activity activity) {
        final TextView mTextViewUsername = headerView.findViewById(R.id.navigation_header_username);
        final ImageView mImageViewAvatar = headerView.findViewById(R.id.navigation_header_avatar);
        if (user != null) {
//            TextView email = headerView.findViewById(R.id.navigation_header_email);
            UserService.getInstance().getUserCredentials(user.getUid(), new UserService.OnUserCredentialsLoadedListener() {
                @Override
                public void onUserCredentialsLoaded(User user) {
                    mTextViewUsername.setText(user.getName());
                    if (user.getAvatar() != null) {
                        AvatarHelper.getInstance().loadAvatarIntoImageView(user.getAvatar(), activity, mImageViewAvatar);
                    }
                }
            });
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
        preferences.edit().putString(FieldNames.USERNAME.toString(), username).apply();
    }

    public void logout(Activity activity) {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        sharedPreferences.edit().remove(FieldNames.USERNAME.toString()).apply();
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }
}
