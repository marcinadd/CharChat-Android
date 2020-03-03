package com.marcinadd.charchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.marcinadd.charchat.user.UserService;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements UserService.OnUserUsernameCheckDoneListener {

    private final int RC_SIGN_IN = 9659;
    public static final String CURRENT_USER_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean hasUsernameSet = !prefs.getString(CURRENT_USER_USERNAME, "").isEmpty();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            if (hasUsernameSet) {
                startActivity(new Intent(this, NavigationDrawerActivity.class));
                finish();
            } else {
                // User returned to app without inserting username
                startActivity(new Intent(this, UsernameSetActivity.class));
                finish();
            }
        } else {
            login();
        }
    }

    public void login() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build());
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                UserService.getInstance().checkIfCurrentUserHasUsername(this);
            }
        }
    }

    @Override
    public void onUsernameAlreadySet() {
        startActivity(new Intent(this, NavigationDrawerActivity.class));
        finish();
    }

    @Override
    public void onUsernameNotExisting() {
        startActivity(new Intent(this, UsernameSetActivity.class));
    }
}
