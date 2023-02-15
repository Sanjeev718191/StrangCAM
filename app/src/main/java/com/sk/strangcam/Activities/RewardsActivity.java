package com.sk.strangcam.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sk.strangcam.R;
import com.sk.strangcam.databinding.ActivityRewardsBinding;

public class RewardsActivity extends AppCompatActivity {

    ActivityRewardsBinding binding;
    String currentUid;
    FirebaseDatabase database;
    int coins = 0;
    private RewardedAd mRewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRewardsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        currentUid = FirebaseAuth.getInstance().getUid();
        loadAd();

        database.getReference().child("profiles")
                .child(currentUid)
                .child("coins")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        coins = snapshot.getValue(Integer.class);
                        binding.coins.setText("You have : "+coins);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.ad1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRewardedAd != null) {
                    Activity activityContext = RewardsActivity.this;
                    mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            loadAd();
                            coins = coins+200;
                            database.getReference().child("profiles")
                                    .child(currentUid)
                                    .child("coins")
                                    .setValue(coins);
                            binding.ad1play.setImageResource(R.drawable.accept);
                            binding.coins.setText("You have : "+coins);
                            binding.ad1.setEnabled(false);
                        }
                    });
                } else {
                }
            }
        });

        binding.ad2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRewardedAd != null) {
                    Activity activityContext = RewardsActivity.this;
                    mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            loadAd();
                            coins = coins+200;
                            database.getReference().child("profiles")
                                    .child(currentUid)
                                    .child("coins")
                                    .setValue(coins);
                            binding.ad2play.setImageResource(R.drawable.accept);
                            binding.coins.setText("You have : "+coins);
                            binding.ad2.setEnabled(false);
                        }
                    });
                } else {
                }
            }
        });

        binding.ad3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRewardedAd != null) {
                    Activity activityContext = RewardsActivity.this;
                    mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            loadAd();
                            coins = coins+200;
                            database.getReference().child("profiles")
                                    .child(currentUid)
                                    .child("coins")
                                    .setValue(coins);
                            binding.ad3play.setImageResource(R.drawable.accept);
                            binding.coins.setText("You have : "+coins);
                            binding.ad3.setEnabled(false);
                        }
                    });
                } else {
                }
            }
        });

    }

    private void loadAd(){
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mRewardedAd = null;
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                mRewardedAd = rewardedAd;
            }
        });
    }

}