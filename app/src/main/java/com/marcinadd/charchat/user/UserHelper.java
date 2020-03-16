package com.marcinadd.charchat.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.marcinadd.charchat.LoginActivity;
import com.marcinadd.charchat.NavigationDrawerActivity;
import com.marcinadd.charchat.R;
import com.marcinadd.charchat.chat.model.User;

public class UserHelper {
    private static final UserHelper ourInstance = new UserHelper();

    private UserHelper() {
    }

    public static UserHelper getInstance() {
        return ourInstance;
    }

    public void setSidebarData(final View headerView, FirebaseUser user, final Activity activity) {
        final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        final TextView mTextViewUsername = headerView.findViewById(R.id.navigation_header_username);
        final ImageView mImageViewAvatar = headerView.findViewById(R.id.navigation_header_avatar);
        if (user != null) {
//            TextView email = headerView.findViewById(R.id.navigation_header_email);
            UserService.getInstance().getUserCredentials(user.getUid(), new UserService.OnUserCredentialsLoadedListener() {
                @Override
                public void onUserCredentialsLoaded(User user) {
                    mTextViewUsername.setText(user.getName());
                    StorageReference storageReference;
                    if (user.getAvatar() != null) {
                        storageReference = firebaseStorage.getReference().child(user.getAvatar());
                        Glide.with(activity)
                                .load(storageReference)
                                .into(mImageViewAvatar);
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
        preferences.edit().putString(LoginActivity.CURRENT_USER_USERNAME, username).apply();
    }
}
