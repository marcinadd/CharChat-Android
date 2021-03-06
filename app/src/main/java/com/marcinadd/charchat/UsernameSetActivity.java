package com.marcinadd.charchat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.marcinadd.charchat.user.UserHelper;
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
        UserHelper.getInstance().startNavigationDrawer(this, true);
        UserHelper.getInstance().saveCurrentUsernameInSharedPreferences(username, getApplicationContext());
    }

    @Override
    public void onFailure() {

    }
}
