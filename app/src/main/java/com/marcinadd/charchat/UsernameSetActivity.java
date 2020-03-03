package com.marcinadd.charchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.marcinadd.charchat.user.UserService;

public class UsernameSetActivity extends AppCompatActivity implements UserService.OnUserUsernameSet {
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username_set);
        editText = findViewById(R.id.editText);
        Button button = findViewById(R.id.username_set_button);
        button.setOnClickListener(onSetButtonClicked());
    }

    View.OnClickListener onSetButtonClicked() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserService.getInstance().setUsernameForCurrentUser(
                        editText.getText().toString().trim(),
                        UsernameSetActivity.this);
            }
        };
    }

    @Override
    public void onSuccess(String username) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.edit().putString(LoginActivity.CURRENT_USER_USERNAME, username).apply();
        startActivity(new Intent(this, NavigationDrawerActivity.class));
        finish();
    }

    @Override
    public void onFailure() {

    }
}
