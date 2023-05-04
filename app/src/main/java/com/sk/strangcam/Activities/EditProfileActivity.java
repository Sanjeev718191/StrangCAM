package com.sk.strangcam.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.sk.strangcam.Models.User;
import com.sk.strangcam.R;
import com.sk.strangcam.databinding.ActivityEditProfileBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    ActivityEditProfileBinding binding;
    String id;
    FirebaseDatabase database;
    StorageReference storageReference;
    ProgressDialog progressDialog;
    int SELECT_PICTURE = 10;
    Uri imageUri;
    User user;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        id = getIntent().getStringExtra("id");
        database = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("User Profile Picture");

        database.getReference("profiles").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if(!user.getProfile().equals("")){
                    Glide.with(EditProfileActivity.this).load(user.getProfile()).into(binding.userSetImageView);
                }
                if(!user.getName().equals("")){
                    binding.inputNameText.setText(user.getName());
                }
                binding.inputAddressText.setText(user.getCity());
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        callOnClickListener();

    }

    private void callOnClickListener() {
        binding.userSetImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
            }
        });
        binding.selectImageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
            }
        });
        binding.setUserDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.inputNameText.getText().toString().isEmpty()){
                    binding.inputNameText.setError("Please enter name");
                } else {
                    try {
                        setUserData();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == SELECT_PICTURE){
                imageUri = data.getData();
                if (null != imageUri) {
                    binding.userSetImageView.setImageURI(imageUri);
                }
            }
        }
    }

    @SuppressLint("ResourceType")
    private void setUserData() throws IOException {
        progressDialog.show();
        if(imageUri != null){
            setUserWithImage();
        } else {
            user.setName(binding.inputNameText.getText().toString());
            if(!binding.inputAddressText.getText().toString().isEmpty()){
                user.setCity(binding.inputAddressText.getText().toString());
            }
            database.getReference("profiles").child(user.getuID()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    progressDialog.dismiss();
                    startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                    finishAffinity();
                }
            });
        }
    }

    private void setUserWithImage() throws IOException {
        if(imageUri != null){
            final StorageReference fileRef = storageReference.child(user.getuID()+".jpg");

//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver() , imageUri);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//            byte[] data = baos.toByteArray();
//            uploadTask = fileRef.putBytes(data);

            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = (Uri) task.getResult();
                        user.setProfile(downloadUri.toString());
                        user.setName(binding.inputNameText.getText().toString());
                        if(!binding.inputAddressText.getText().toString().isEmpty()){
                            user.setCity(binding.inputAddressText.getText().toString());
                        }
                        database.getReference("profiles").child(user.getuID()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                                finishAffinity();
                            }
                        });

                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Error, Please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Image is not selected", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

}