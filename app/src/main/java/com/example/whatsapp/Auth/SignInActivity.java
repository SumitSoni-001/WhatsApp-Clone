package com.example.whatsapp.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.whatsapp.OtherActivities.MainActivity;
import com.example.whatsapp.R;
import com.example.whatsapp.Models.UsersModel;
import com.example.whatsapp.databinding.ActivitySignInBinding;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity {

    ActivitySignInBinding binding;
    ProgressDialog progressDialog;
    private FirebaseAuth auth;   // "FirebaseAuth" is used for Authentication in Firebase
    FirebaseDatabase database;
    GoogleSignInClient mGoogleSignInClient;     // Used for Google SignIn
    private CallbackManager callbackManager;    // Used for facebook SignIn
    private static final String EMAIL = "email";
    private LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());      // "getRoot()" method takes us to the SignIn Activity.

        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        callbackManager = CallbackManager.Factory.create();

        progressDialog = new ProgressDialog(SignInActivity.this);
        progressDialog.setTitle("Logging In");
        progressDialog.setMessage("We are Creating Your Account");

        // Setting OnClickListener on the SignUp button.
        binding.signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (binding.etEmail.getText().toString().isEmpty()){
                    // If the Email box is empty , this condition will through a Error.
                    binding.etEmail.setError("Enter Email Id");
                    return;
                }

                if (binding.etPassword.getText().toString().isEmpty()){
                    // If the Password Box is empty , this condition will through a Error.
                    binding.etPassword.setError("Enter a strong Password");
                    return;
                }

                progressDialog.show();

                // Starting Authentication Process :- Signing In the User with Email ID and Password.
                auth.signInWithEmailAndPassword(binding.etEmail.getText().toString() , binding.etPassword.getText().toString()).
                        addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                progressDialog.dismiss();

                                if (task.isSuccessful()){

                                    Intent intent = new Intent(SignInActivity.this , MainActivity.class);
                                    startActivity(intent);
                                }

                                else{
                                    Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        });

        // Setting OnClickListener on the "Not Login" TextView.
        binding.tvNotLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        binding.signInPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this , GetPhoneNum.class));
            }
        });

                                /* Remember The User */
//        binding.googleBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AccountManager am = AccountManager.get(SignInActivity.this); // "this" references the current Context
//
//                Account[] accounts = am.getAccountsByType("com.google");
//            }
//        });

        //  By adding this Statement, If the User is Signed In ; he will see the Main Activity on the 1st screen after Opening the app.

        if (auth.getCurrentUser() != null){
            Intent intent = new Intent(SignInActivity.this , MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

                                    /* Facebook Login */

//        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton = (LoginButton) binding.loginButton;
        loginButton.setPermissions(Arrays.asList(EMAIL));
        // If you are using in a fragment, call loginButton.setFragment(this);

        // Callback registration
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(SignInActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
                                /* Google signIn */
    int RC_SIGN_IN = 77;

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        // FB Auth
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = auth.getCurrentUser();

                            UsersModel users = new UsersModel();
                            users.setUsername(user.getDisplayName());
                            users.setEmail(user.getEmail());
                            users.setProfilePic(user.getPhotoUrl().toString());

                            database.getReference().child("Users").child(user.getUid()).setValue(users);

                            Intent intent = new Intent(SignInActivity.this , MainActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        }

                        else {
                            Toast.makeText(SignInActivity.this, task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

                                   /* Facebook signIn */

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("TAG", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();

                            UsersModel users = new UsersModel();
                            users.setUsername(user.getDisplayName());
                            users.setEmail(user.getEmail());
                            users.setProfilePic(user.getPhotoUrl().toString());

                            database.getReference().child("Users").child(user.getUid()).setValue(users);

                            Intent intent = new Intent(SignInActivity.this , MainActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        }
                        else {
                            Toast.makeText(SignInActivity.this, "Authentication failed: "+task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void facebookBtn(View view) {    // When we click on facebook btn then the call is transfered to login button and it shows result according to login button.
        if (view == binding.facebookBtn) {
            binding.loginButton.performClick();
        }
    }

}