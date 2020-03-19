package com.marcinadd.charchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.marcinadd.charchat.image.ImageService;
import com.marcinadd.charchat.image.listener.OnImageUploadedListener;
import com.marcinadd.charchat.user.UserHelper;
import com.marcinadd.charchat.user.avatar.AvatarClickDialogFragment;
import com.marcinadd.charchat.user.avatar.AvatarHelper;
import com.marcinadd.charchat.user.avatar.AvatarService;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;

import java.util.ArrayList;

public class NavigationDrawerActivity extends AppCompatActivity implements AvatarService.OnAvatarUpdatedListener {

    private final String AVATARS = "avatars";
    private AppBarConfiguration mAppBarConfiguration;
    private ImageView mImageViewAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_chat)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        View headerView = navigationView.getHeaderView(0);
        UserHelper.getInstance().setSidebarData(headerView, firebaseUser, this);

        mImageViewAvatar = headerView.findViewById(R.id.navigation_header_avatar);
        mImageViewAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AvatarClickDialogFragment().show(getSupportFragmentManager(), "TAG");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            UserHelper.getInstance().logout(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void onChangeAvatar() {
        ImagePicker.with(this)
                .setMultipleMode(false)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Config.RC_PICK_IMAGES && data != null) {
            final ImageService imageService = ImageService.getInstance();
            final AvatarService avatarService = AvatarService.getInstance();
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            imageService.uploadImageByPath(images.get(0).getPath(), AVATARS, new OnImageUploadedListener() {
                @Override
                public void onImageUploaded(final String serverPath) {
                    AvatarService.getInstance().deleteCurrentAvatarImageFromServer(new AvatarService.OnAvatarDeletedListener() {
                        @Override
                        public void onAvatarDeleted() {
                            avatarService.updateAvatarInUserCredentials(serverPath, NavigationDrawerActivity.this);
                        }
                    });
                }
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAvatarUpdated(String newPath) {
        AvatarHelper.getInstance().loadAvatarIntoImageView(newPath, this, mImageViewAvatar);
    }
}
