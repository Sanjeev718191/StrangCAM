package com.sk.strangcam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;

import com.bumptech.glide.Glide;
import com.sk.strangcam.databinding.ActivityConnectingBinding;

public class ConnectingActivity extends AppCompatActivity {

    ActivityConnectingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AnimationDrawable animationDrawable = (AnimationDrawable) binding.connectingRootLayout.getBackground();
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

        String imageUrl = getIntent().getStringExtra("profile");
        String decision = getIntent().getStringExtra("request");
        Glide.with(this).load(imageUrl).into(binding.connectingUserImageView);

//        if(decision.equals("call")){
//            startActivity(new Intent(ConnectingActivity.this, CallActivity.class));
//            finish();
//        } else {
//            startActivity(new Intent(ConnectingActivity.this, ChatActivity.class));
//            finish();
//        }

    }
}