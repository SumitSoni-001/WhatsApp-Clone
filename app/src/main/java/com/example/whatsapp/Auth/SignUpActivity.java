package com.example.whatsapp.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.whatsapp.OtherActivities.MainActivity;
import com.example.whatsapp.R;
import com.example.whatsapp.Models.UsersModel;
import com.example.whatsapp.databinding.ActivitySignUpBinding;
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

public class SignUpActivity extends AppCompatActivity {

   ActivitySignUpBinding binding; /* "viewBinding" generates a binding class for each xml layout file present in that module.
      An Instance of a Binding class contains direct references to all views that have an Id in the corresponding Layout.*/

   private FirebaseAuth auth;   // "FirebaseAuth" is used for Authentication in Firebase

   FirebaseDatabase database;   // "FirebaseDatabase" is used to store Id and Password or Other Info of the user in the Firebase Database.

    ProgressDialog progressDialog;  /* It is used to show Loading/Progress.[Note:- // Progress Dialog should be started in onClick Method , because
    it starts after clicking the button. & should be stopped in onComplete method.*/

    GoogleSignInClient mGoogleSignInClient;     // Used for Google SignIn

    private CallbackManager callbackManager;    // Used for facebook SignIn
    private static final String EMAIL = "email";
    private LoginButton loginButton;        // For Facebook


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());      // "getRoot()" method takes us to the SignUp Activity.

        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(SignUpActivity.this);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("We are Creating Your Account");

        // Setting OnClickListener on the SignUp button.
        binding.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (binding.etUserName.getText().toString().isEmpty()){
                // If the Username box is empty , this condition will through a Error.
                    binding.etUserName.setError("Enter any name");
                    return;
                }

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

                // Starting Authentication Process :- Creating User Id with Email ID and Password.
                auth.createUserWithEmailAndPassword(binding.etEmail.getText().toString() , binding.etPassword.getText().toString()).
                        addOnCompleteListener(new OnCompleteListener<AuthResult>() {    // "addOnCompleteListener()" is a builtIn method in firebase, which tells about the completion of the process(here 'Authentication').
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()){

                                    progressDialog.dismiss();

                                    // Getting Users Info(Email , UserName , Password) stored in the Firebase.
                                    UsersModel user = new UsersModel(binding.etUserName.getText().toString() , binding.etEmail.getText().toString() , binding.etPassword.getText().toString());

                                    // Getting UserID (Uid) created in the Firebase
                                    String id = task.getResult().getUser().getUid();

                                    //Setting the Uid and other Info of the User in Realtime Database.
                                    database.getReference().child("Users").child(id).setValue(user);    // The data in the firebase is stored in JSON format.

                                    Toast.makeText(SignUpActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(SignUpActivity.this , MainActivity.class);
                                    startActivity(intent);
                                    finishAffinity();
                                }

                                else{
                                    progressDialog.dismiss();
                                    Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Setting OnClickListener on the "Already Login" TextView.
        binding.tvAlreadyLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this , SignInActivity.class);
                startActivity(intent);
            }
        });

        binding.signUpPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this , GetPhoneNum.class));
            }
        });

                                            /* SIGNUP WITH GOOGLE */

        // Setting OnClickListener on the Google button.
        binding.googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //  By adding this Statement, If the User is Signed In ; he will see the Main Activity on the 1st screen after Opening the app.
        if (auth.getCurrentUser() != null){
            Intent intent = new Intent(SignUpActivity.this , MainActivity.class);
            startActivity(intent);
            finish();
        }

        /* Facebook Login */

//        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton = (LoginButton) binding.LoginButton;
        loginButton.setPermissions(Arrays.asList(EMAIL));
        // If you are using in a fragment, call loginButton.setFragment(this);

        // Callback registration
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(SignUpActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

                                        // SignUp through Google

    int RC_SIGN_UP = 60;

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_UP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_UP) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
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
                            users.setUsername(user.getDisplayName().toString());
                            users.setEmail(user.getEmail().toString());
                            users.setProfilePic(user.getPhotoUrl().toString());

                            database.getReference().child("Users").child(user.getUid()).setValue(users);

                            Intent intent = new Intent(SignUpActivity.this , MainActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        }

                        else {
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

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
                            users.setPhone(user.getPhoneNumber());
                            users.setProfilePic(user.getPhotoUrl().toString());

                            database.getReference().child("Users").child(user.getUid()).setValue(users);

                            Intent intent = new Intent(SignUpActivity.this , MainActivity.class);
                            startActivity(intent);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                finishAffinity();
                            }

                        }

                        else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed: "+task.getException().getMessage() , Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }
                    }
                });
    }

    public void facebookButton(View view) {
        if (view == binding.facebookButton) {
            binding.LoginButton.performClick();
        }
    }

}