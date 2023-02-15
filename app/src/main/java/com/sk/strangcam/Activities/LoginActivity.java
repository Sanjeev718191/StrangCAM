package com.sk.strangcam.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sk.strangcam.Models.User;
import com.google.firebase.database.annotations.NotNull;
import com.sk.strangcam.R;


public class LoginActivity extends AppCompatActivity {

    LinearLayoutCompat linearLayoutCompat;
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 11;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.show();

        linearLayoutCompat = findViewById(R.id.Google_signing_btn);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        if(mAuth.getCurrentUser() != null) {
            goToNextActivity();
        } else {
            progressDialog.dismiss();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        linearLayoutCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
            }
        });

    }

    private void goToNextActivity() {
        progressDialog.dismiss();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finishAffinity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) { //fsdfafdd
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult();
            authWithGoogle(account.getIdToken());
            progressDialog.show();
        }
    }

    void authWithGoogle(String idToken) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                //to add information in database
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            database.getReference()
                                    .child("profiles")
                                    .child(user.getUid()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.getValue(String.class).equals("")){
                                                User firebaseUser = new User(user.getUid(), user.getDisplayName(), user.getPhotoUrl().toString(), "Unknown", 500);
                                                database.getReference()
                                                        .child("profiles")
                                                        .child(user.getUid())
                                                        .setValue(firebaseUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    progressDialog.dismiss();
                                                                    goToNextActivity();
                                                                } else {
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            } else {
                                                progressDialog.dismiss();
                                                goToNextActivity();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            progressDialog.dismiss();
                                            Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });


                        } else {
                            progressDialog.dismiss();
                            Log.e("Error", task.getException().getLocalizedMessage());
                        }
                    }
                });
    }

}