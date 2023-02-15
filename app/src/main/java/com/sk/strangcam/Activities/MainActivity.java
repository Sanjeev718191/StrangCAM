package com.sk.strangcam.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sk.strangcam.Models.User;
import com.sk.strangcam.R;
import com.sk.strangcam.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseUser currentUser;
    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    int coins;
    ProgressDialog progressDialog;
    private int requestCode = 1;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        currentUser = auth.getCurrentUser();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.show();

        getOnlineUser();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        database.getReference().child("profiles")
                .child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                        coins = user.getCoins();
                        binding.totalCoins.setText("You have : " + coins);
                        Glide.with(MainActivity.this)
                                .load(user.getProfile())
                                .into(binding.mainUserImageView);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Please check your network connection...", Toast.LENGTH_SHORT).show();
                    }
                });

        binding.mainVideoCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askPermissions();
                if(isPermissionsGranted()){
                    if(coins >= 5){
                        coins = coins-5;
                        database.getReference().child("profiles")
                                .child(currentUser.getUid())
                                .child("coins").setValue(coins);
                        Intent intent = new Intent(MainActivity.this, ConnectingActivity.class);
                        intent.putExtra("profile", user.getProfile());
                        intent.putExtra("request", "call");
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Insufficient Coins", Toast.LENGTH_SHORT).show();
                        animateReward();
                    }
                }
            }
        });


        binding.mainChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(coins >= 5){
                    coins = coins-5;
                    database.getReference().child("profiles")
                            .child(currentUser.getUid())
                            .child("coins").setValue(coins);
                    Intent intent = new Intent(MainActivity.this, ConnectingActivity.class);
                    intent.putExtra("profile", user.getProfile());
                    intent.putExtra("request", "chat");
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Insufficient Coins", Toast.LENGTH_SHORT).show();
                    animateReward();
                }
            }
        });

        binding.getCoinsLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RewardsActivity.class));
            }
        });



    }

    private void getOnlineUser() {
        final int[] vidOnline = {0};
        database.getReference().child("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        vidOnline[0] = (int) snapshot.getChildrenCount();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        final int[] chatOnline = {0};
        database.getReference().child("chatRooms")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatOnline[0] = (int) snapshot.getChildrenCount();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        binding.onlinUserTextView.setText(String.valueOf(chatOnline[0]+vidOnline[0]));
    }


    private void animateReward() {
        binding.getCoinsLinearLayout.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.blue_background));
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(200);
        animation.setStartOffset(20);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(10);
        binding.getCoinsLinearLayout.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.getCoinsLinearLayout.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.gray_background));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void askPermissions() {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private boolean isPermissionsGranted() {
        for(String permission : permissions){
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }



}