package com.sk.strangcam.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.sk.strangcam.Models.InterfaceJava;
import com.sk.strangcam.Models.User;
import com.sk.strangcam.R;
import com.sk.strangcam.databinding.ActivityCallBinding;

public class CallActivity extends AppCompatActivity {

    ActivityCallBinding binding;
    String uinqueId = "";
    String username = "";
    String friendsUsername = "";
    String createdBy = "";
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    boolean isAudio = true;
    boolean isVideo = true;
    boolean pageExit = false;
    boolean isPeerConnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        username = getIntent().getStringExtra("username");
        createdBy = getIntent().getStringExtra("createdBy");
        friendsUsername = getIntent().getStringExtra("incoming");

        setupWebView();

        binding.endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callJavaScriptFunction("javascript:closeConnection()");
                finish();
            }
        });
        binding.muteMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudio = !isAudio;
                callJavaScriptFunction("javascript:toggleAudio(\""+isAudio+"\")");
                if(isAudio) binding.muteMic.setImageResource(R.drawable.btn_unmute_normal);
                else binding.muteMic.setImageResource(R.drawable.btn_mute_normal);
            }
        });

        binding.offCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVideo = !isVideo;
                callJavaScriptFunction("javascript:toggleVideo(\""+isVideo+"\"");
                if(isVideo) binding.offCamera.setImageResource(R.drawable.btn_video_normal);
                else binding.offCamera.setImageResource(R.drawable.btn_video_muted);
            }
        });

    }

    public void setupWebView(){
        binding.videoCallWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        binding.videoCallWebView.getSettings().setJavaScriptEnabled(true);
        binding.videoCallWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.videoCallWebView.addJavascriptInterface(new InterfaceJava(this), "Android");

        loadVideoCall();

    }

    private void loadVideoCall() {

        String filePath = "file:android_asset/call.html";
        binding.videoCallWebView.loadUrl(filePath);

        binding.videoCallWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();
            }
        });

    }

    private void initializePeer() {

        uinqueId = auth.getUid();

        callJavaScriptFunction("javascript:init(\""+uinqueId+"\")");

        if(createdBy.equalsIgnoreCase(username)){
            if(pageExit) return;

            databaseReference.child(username).child("connId").setValue(uinqueId);
            databaseReference.child(username).child("isAvailable").setValue(true);

            binding.connectingAnimationView.setVisibility(View.GONE);
            binding.endCall.setVisibility(View.VISIBLE);
            binding.muteMic.setVisibility(View.VISIBLE);
            binding.offCamera.setVisibility(View.VISIBLE);

            FirebaseDatabase.getInstance().getReference()
                    .child("profiles")
                    .child(friendsUsername)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);

                            Glide.with(CallActivity.this).load(user.getProfile()).into(binding.remoteUserImage);
                            binding.remoteUserName.setText(user.getName());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        } else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendsUsername = createdBy;
                    FirebaseDatabase.getInstance().getReference().child("profiles")
                            .child(friendsUsername)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User user = snapshot.getValue(User.class);

                                    Glide.with(CallActivity.this).load(user.getProfile()).into(binding.remoteUserImage);
                                    binding.remoteUserName.setText(user.getName());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                    FirebaseDatabase.getInstance().getReference()
                            .child("users")
                            .child(friendsUsername)
                            .child("connId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.getValue() != null){
                                        sendCallRequest();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }, 3000);
        }

    }

    public void onPeerConnected(){
        isPeerConnected = true;
    }

    void sendCallRequest(){
        if(!isPeerConnected){
            Toast.makeText(this, "You are not connected. Please check your internet.", Toast.LENGTH_SHORT).show();
            return;
        }
        listenConnId();
    }

    private void listenConnId() {
        databaseReference.child(friendsUsername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null) {
//                    Toast.makeText(CallActivity.this, "Receiver Is Disconnected...", Toast.LENGTH_SHORT).show();
//                    finish();
                    return;
                }
                binding.connectingAnimationView.setVisibility(View.GONE);
                binding.endCall.setVisibility(View.VISIBLE);
                binding.muteMic.setVisibility(View.VISIBLE);
                binding.offCamera.setVisibility(View.VISIBLE);
                String connId = snapshot.getValue(String.class);
                callJavaScriptFunction("javascript:startCall(\""+connId+"\")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void callJavaScriptFunction(String function){
        binding.videoCallWebView.post(new Runnable() {
            @Override
            public void run() {
                binding.videoCallWebView.evaluateJavascript(function, null);
            }
        });
    }


    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            callJavaScriptFunction("javascript:closeConnection()");
            //finish();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to disconnect and exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageExit = true;
        if(databaseReference.child(createdBy) != null)
            databaseReference.child(createdBy).setValue(null);
        finish();
    }
}