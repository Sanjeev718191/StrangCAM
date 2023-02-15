package com.sk.strangcam.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sk.strangcam.databinding.ActivityConnectingBinding;

import java.util.HashMap;

public class ConnectingActivity extends AppCompatActivity {

    ActivityConnectingBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    boolean isOkay = false;
    String decision;

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
        decision = getIntent().getStringExtra("request");
        Glide.with(this).load(imageUrl).into(binding.connectingUserImageView);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        String userName = auth.getUid();

        if(decision.equals("call")){
            //call activity implementation
            database.getReference().child("users")
                    .orderByChild("status")
                    .equalTo(0).limitToFirst(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.getChildrenCount() > 0){
                                //if a online user is waiting
                                if(isOkay) return;
                                isOkay = true;
                                for(DataSnapshot childSnap : snapshot.getChildren()){
                                    database.getReference()
                                            .child("users")
                                            .child(childSnap.getKey())
                                            .child("incoming")
                                            .setValue(userName);
                                    database.getReference()
                                            .child("users")
                                            .child(childSnap.getKey())
                                            .child("status")
                                            .setValue(1);
                                    Intent intent = new Intent(ConnectingActivity.this, CallActivity.class);
                                    String incoming = childSnap.child("incoming").getValue(String.class);
                                    String createdBy = childSnap.child("createdBy").getValue(String.class);
                                    boolean isAvailable = childSnap.child("isAvailable").getValue(Boolean.class);
                                    intent.putExtra("username", userName);
                                    intent.putExtra("incoming", incoming);
                                    intent.putExtra("createdBy", createdBy);
                                    intent.putExtra("isAvailable", isAvailable);
                                    startActivity(intent);
                                    finish();
                                }

                            } else {
                                //No user is online

                                HashMap<String, Object> room = new HashMap<>();
                                room.put("incoming", userName);
                                room.put("createdBy", userName);
                                room.put("isAvailable", true);
                                room.put("status", 0);

                                database.getReference().child("users")
                                        .child(userName)
                                        .setValue(room).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                database.getReference().child("users")
                                                        .child(userName).addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if(snapshot.child("status").exists()){
                                                                    if(snapshot.child("status").getValue(Integer.class) == 1){
                                                                        if(isOkay){
                                                                            return;
                                                                        }
                                                                        isOkay = true;
                                                                        Intent intent = new Intent(ConnectingActivity.this, CallActivity.class);
                                                                        String incoming = snapshot.child("incoming").getValue(String.class);
                                                                        String createdBy = snapshot.child("createdBy").getValue(String.class);
                                                                        boolean isAvailable = snapshot.child("isAvailable").getValue(Boolean.class);
                                                                        intent.putExtra("username", userName);
                                                                        intent.putExtra("incoming", incoming);
                                                                        intent.putExtra("createdBy", createdBy);
                                                                        intent.putExtra("isAvailable", isAvailable);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                            }
                                        });

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        } else {
            database.getReference().child("chatRooms")
                    .orderByChild("status")
                    .equalTo(0).limitToFirst(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.getChildrenCount() > 0){
                                if(isOkay) return;
                                isOkay = true;
                                for(DataSnapshot childSnap : snapshot.getChildren()){
                                    database.getReference()
                                            .child("chatRooms")
                                            .child(childSnap.getKey())
                                            .child("incoming")
                                            .setValue(userName);
                                    database.getReference()
                                            .child("chatRooms")
                                            .child(childSnap.getKey())
                                            .child("status")
                                            .setValue(1);
                                    Intent intent = new Intent(ConnectingActivity.this, ChatActivity.class);
                                    String incoming = childSnap.child("incoming").getValue(String.class);
                                    String createdBy = childSnap.child("createdBy").getValue(String.class);
                                    boolean isAvailable = childSnap.child("isAvailable").getValue(Boolean.class);
                                    intent.putExtra("username", userName);
                                    intent.putExtra("incoming", incoming);
                                    intent.putExtra("createdBy", createdBy);
                                    intent.putExtra("isAvailable", isAvailable);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                //create new room
                                HashMap<String, Object> room = new HashMap<>();
                                room.put("incoming", userName);
                                room.put("createdBy", userName);
                                room.put("isAvailable", true);
                                room.put("status", 0);

                                database.getReference().child("chatRooms")
                                        .child(userName)
                                        .setValue(room).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                database.getReference().child("chatRooms")
                                                        .child(userName).addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if(snapshot.child("status").exists()){
                                                                    if(snapshot.child("status").getValue(Integer.class) == 1){
                                                                        if(isOkay) return;
                                                                        isOkay = true;
                                                                        Intent intent = new Intent(ConnectingActivity.this, ChatActivity.class);
                                                                        String incoming = snapshot.child("incoming").getValue(String.class);
                                                                        String createdBy = snapshot.child("createdBy").getValue(String.class);
                                                                        boolean isAvailable = snapshot.child("isAvailable").getValue(Boolean.class);
                                                                        intent.putExtra("username", userName);
                                                                        intent.putExtra("incoming", incoming);
                                                                        intent.putExtra("createdBy", createdBy);
                                                                        intent.putExtra("isAvailable", isAvailable);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(decision.equals("call")){
            if(database.getReference().child("users").child(auth.getUid()) != null)
                database.getReference().child("users").child(auth.getUid()).setValue(null);
        } else {
            if(database.getReference().child("chatRooms").child(auth.getUid()) != null)
                database.getReference().child("chatRooms").child(auth.getUid()).setValue(null);
        }
        finish();
    }
}