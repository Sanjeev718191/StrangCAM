package com.sk.strangcam.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sk.strangcam.Adapters.ChatAdapter;
import com.sk.strangcam.Models.Message;
import com.sk.strangcam.R;
import com.sk.strangcam.databinding.ActivityChatBinding;

import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    String roomId;
    String senderId, receiverId;
    String senderRoom, receiverRoom;
    DatabaseReference senderDatabase, receiverDatabase;
    ChatAdapter adapter;
    ArrayList<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        messageList = new ArrayList<>();
        roomId = getIntent().getStringExtra("createdBy");
        senderId = getIntent().getStringExtra("username");
        receiverId = roomId.equals(senderId) ?
                getIntent().getStringExtra("incoming"):
                roomId;
        senderRoom = senderId+receiverId;
        receiverRoom = receiverId+senderId;
        senderDatabase = FirebaseDatabase.getInstance().getReference("chatRooms").child(roomId).child(senderRoom);
        receiverDatabase = FirebaseDatabase.getInstance().getReference("chatRooms").child(roomId).child(receiverRoom);

        adapter = new ChatAdapter(ChatActivity.this, messageList);
        binding.chatRecycler.setAdapter(adapter);
        binding.chatRecycler.setLayoutManager(new LinearLayoutManager(this));

        senderDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Message message = ds.getValue(Message.class);
                    messageList.add(message);
                }
                adapter.notifyDataSetChanged();
                binding.chatRecycler.scrollToPosition(messageList.size()-1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = binding.inputMessage.getText().toString();
                if(msg == null || msg.equals("")){
                    binding.inputMessage.setError("Please Write message...");
                } else {
                    sendMessage(msg);
                }
            }
        });

        FirebaseDatabase.getInstance().getReference("chatRooms").child(roomId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null){
                    Toast.makeText(ChatActivity.this, "Receiver left the Room...", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        FirebaseDatabase.getInstance().getReference("profiles").child(receiverId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.getValue(String.class).equals("")){
                    getSupportActionBar().setTitle(snapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }

    private void sendMessage(String msg) {
        String messageId = FirebaseDatabase.getInstance().getReference().push().getKey();
        Date date = new Date();
        Message message = new Message(messageId, msg, FirebaseAuth.getInstance().getUid(), date.getTime() );
        messageList.add(message);
        adapter.notifyDataSetChanged();
        binding.chatRecycler.scrollToPosition(messageList.size()-1);

        senderDatabase.child(messageId).setValue(message);
        receiverDatabase.child(messageId).setValue(message);
        binding.inputMessage.setText("");
    }

    boolean doubleBackToExitPressedOnce = false;

    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
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
        if(FirebaseDatabase.getInstance().getReference().child("chatRooms").child(FirebaseAuth.getInstance().getUid()) != null)
            FirebaseDatabase.getInstance().getReference().child("chatRooms").child(FirebaseAuth.getInstance().getUid()).setValue(null);
        finish();
    }
}