package com.sk.strangcam.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
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
import com.sk.strangcam.Models.EmailUser;
import com.sk.strangcam.Models.User;
import com.google.firebase.database.annotations.NotNull;
import com.sk.strangcam.R;
import com.sk.strangcam.databinding.ActivityLoginBinding;


public class LoginActivity extends AppCompatActivity {

    LinearLayoutCompat linearLayoutCompat;
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 11;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    ProgressDialog progressDialog;

    ActivityLoginBinding binding;

    String loginType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
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

        binding.TermsConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://doc-hosting.flycricket.io/strangcam-privacy-policy/88cffe33-48da-4d9a-8296-35f854a10115/privacy"));
                startActivity(intent);
            }
        });

        binding.signInMailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSignInLayout();
            }
        });

        binding.signupMailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSignUpLayout();
            }
        });

        binding.forgetPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setForgetPassLayout();
            }
        });

        binding.loginSignupForgetPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(loginType != null){
                    if(loginType.equals("in")){
                        if(binding.inputEmailText.getText().toString().isEmpty()){
                            binding.inputEmail.setError("Please enter Email");
                        } else if(binding.inputPasswordText.getText().toString().isEmpty()) {
                            binding.inputPasswordText.setError("Please enter Password");
                        } else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmailText.getText().toString()).matches()){
                            binding.inputEmail.setError("Please enter correct email");
                        } else if(binding.inputPasswordText.getText().toString().length() < 6){
                            binding.inputPasswordText.setError("Password must contain at least 6 digits");
                        } else {
                            SignIn();
                        }
                    }
                    else if(loginType.equals("up")){
                        if(binding.inputEmailText.getText().toString().isEmpty()){
                            binding.inputEmail.setError("Please enter Email");
                        } else if(binding.inputPasswordText.getText().toString().isEmpty()) {
                            binding.inputPasswordText.setError("Please enter Password");
                        } else if(binding.inputPasswordConfirmText.getText().toString().isEmpty()) {
                            binding.inputPasswordConfirmText.setError("Please enter Confirm Password");
                        } else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmailText.getText().toString()).matches()){
                            binding.inputEmail.setError("Please enter correct email");
                        } else if(binding.inputPasswordText.getText().toString().length() < 6){
                            binding.inputPasswordText.setError("Password must contain at least 6 digits");
                        } else if(!binding.inputPasswordText.getText().toString().equals(binding.inputPasswordConfirmText.getText().toString())) {
                            binding.inputPasswordConfirm.setError("Password Miss match");
                        } else {
                            SignUp();
                        }
                    }
                    else if(loginType.equals("forget")){
                        if(binding.inputEmailText.getText().toString().isEmpty()){
                            binding.inputEmail.setError("Enter your registered email id");
                        } else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmailText.getText().toString()).matches()){
                            binding.inputEmail.setError("Please enter correct email");
                        } else {
                            resetPassword();
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Please retry!", Toast.LENGTH_SHORT).show();
                }
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
            try {
                GoogleSignInAccount account = task.getResult();
                authWithGoogle(account.getIdToken());
                progressDialog.show();
            } catch (RuntimeException e){
                e.printStackTrace();
            }
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
                                            if(snapshot.getValue(String.class) == null || snapshot.getValue(String.class).equals("")){
                                                User firebaseUser = new User(user.getUid(), user.getDisplayName(), user.getPhotoUrl().toString(), "Unknown", 500);
                                                database.getReference().child("profiles").child(user.getUid()).setValue(firebaseUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            progressDialog.dismiss();
                                                            Intent intent = new Intent(LoginActivity.this, EditProfileActivity.class);
                                                            intent.putExtra("id", user.getUid());
                                                            startActivity(intent);
                                                            finishAffinity();
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

    private void setSignInLayout(){
        loginType = "in";
        binding.inputEmail.setVisibility(View.VISIBLE);
        binding.inputPassword.setVisibility(View.VISIBLE);
        binding.inputPassword.setHint("Password");
        binding.inputPasswordConfirm.setVisibility(View.GONE);
        binding.loginSignupForgetPassButton.setText("Sign In");
        binding.signInUpInput.setVisibility(View.VISIBLE);
        binding.signInMailBtn.setVisibility(View.GONE);
        binding.signupMailBtn.setVisibility(View.VISIBLE);
        binding.forgetPassButton.setVisibility(View.VISIBLE);
    }

    private void setSignUpLayout(){
        loginType = "up";
        binding.inputEmail.setVisibility(View.VISIBLE);
        binding.inputPassword.setVisibility(View.VISIBLE);
        binding.inputPassword.setHint("Password");
        binding.inputPasswordConfirm.setVisibility(View.VISIBLE);
        binding.loginSignupForgetPassButton.setText("Sign Up");
        binding.signInUpInput.setVisibility(View.VISIBLE);
        binding.signInMailBtn.setVisibility(View.VISIBLE);
        binding.signupMailBtn.setVisibility(View.GONE);
        binding.forgetPassButton.setVisibility(View.VISIBLE);
    }

    private void setForgetPassLayout(){
        loginType = "forget";
        binding.inputEmail.setVisibility(View.VISIBLE);
        binding.inputPassword.setVisibility(View.GONE);
        binding.inputPasswordConfirm.setVisibility(View.GONE);
        binding.loginSignupForgetPassButton.setText("Reset Password");
        binding.signInUpInput.setVisibility(View.VISIBLE);
        binding.signInMailBtn.setVisibility(View.VISIBLE);
        binding.signupMailBtn.setVisibility(View.VISIBLE);
        binding.forgetPassButton.setVisibility(View.GONE);
    }

    private void SignUp(){

        progressDialog.show();
        String email = binding.inputEmailText.getText().toString().trim();
        String password = binding.inputPasswordText.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    User firebaseUser = new User(user.getUid(), "", "", "Unknown", 500);
                    database.getReference().child("profiles").child(user.getUid()).setValue(firebaseUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            EmailUser emailUser = new EmailUser(email, password, user.getUid());
                            database.getReference("emailUsers").child(user.getUid()).setValue(emailUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        if(task.isSuccessful()){
                                            progressDialog.dismiss();
                                            Intent intent = new Intent(LoginActivity.this, EditProfileActivity.class);
                                            intent.putExtra("id", user.getUid());
                                            startActivity(intent);
                                            finishAffinity();
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void SignIn(){

        progressDialog.show();
        String email = binding.inputEmailText.getText().toString().trim();
        String password = binding.inputPasswordText.getText().toString().trim();

        database.getReference("emailUsers").orderByChild("email").startAt(email).endAt(email+"\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() > 0){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        EmailUser curr = ds.getValue(EmailUser.class);
                        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    goToNextActivity();
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Email not registered, Please Sign Up to Continue", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void resetPassword(){

        progressDialog.show();
        String email = binding.inputEmailText.getText().toString().trim();

        database.getReference("emailUsers").orderByChild("email").startAt(email).endAt(email+"\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() > 0){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        EmailUser curr = ds.getValue(EmailUser.class);
                        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                                progressDialog.dismiss();
                                setSignInLayout();
                            }
                        });
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Email not registered, Please Sign Up to Continue", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }

}