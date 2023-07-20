package com.example.whatsapp.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.example.whatsapp.Models.UsersModel;
import com.example.whatsapp.databinding.ActivityOtpBinding;
import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {
    ActivityOtpBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    ProgressDialog progressDialog;
    String CountryCode , PhoneNum , completeNumber;
    private String OtpId;
    Pinview OTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        OTP = findViewById(R.id.OTP);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Getting phone number and country code from Phone Activity.
        CountryCode = getIntent().getStringExtra("code");
        PhoneNum = getIntent().getStringExtra("phone");

        completeNumber = CountryCode + " " +PhoneNum;

        binding.countryCode.setText(CountryCode);
        binding.tvPhone.setText(PhoneNum);
        binding.tvCountryCode.setText(CountryCode);
        binding.TvPhone.setText(PhoneNum);

        progressDialog = new ProgressDialog(OtpActivity.this);
        progressDialog.setMessage("Sending OTP");
        progressDialog.setCancelable(false);
        progressDialog.show();

        binding.wrongNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OtpActivity.this , GetPhoneNum.class));
            }
        });

        InitiateOTP();

        // We need to click the next btn only when the Authenticated Number's SIM is not in our device.
        binding.GetOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (OTP.getValue()/*.toString()*/.isEmpty())
                    Toast.makeText(OtpActivity.this, "Blank Field can not be processed", Toast.LENGTH_SHORT).show();

                else if (OTP.getValue()/*.toString()*/.length() != 6)
                    Toast.makeText(OtpActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();

                else{
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OtpId , OTP.getValue());
                    signInWithPhoneAuthCredential(credential);
                    binding.GetOtp.setText("Next");
                }

            }
        });

        binding.Resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResendVerificationCode(completeNumber , forceResendingToken);
            }
        });

    }

    private void ResendVerificationCode(String phoneNum , PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNum)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(OtpActivity.this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void InitiateOTP(){

        // Turn off phone auth app verification.
        FirebaseAuth.getInstance().getFirebaseAuthSettings()
                .setAppVerificationDisabledForTesting(true);

        // The "InitiateOTP" Will generate OTP for the Number entered and the OTP is valid for 60 seconds
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(completeNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                            @Override
                            public void onCodeSent(@NonNull String VerificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {

                                Toast.makeText(OtpActivity.this, "Enter the OTP we sent \nto "+completeNumber , Toast.LENGTH_SHORT).show();

                                // This method will work when the SIM is not in our device.
                                progressDialog.dismiss();
                                super.onCodeSent(VerificationId, forceResendingToken);
                                OtpId = VerificationId;  // String 's' stores the otp id generated by the Authentication Provider and is Stored to 'OtpId' in order to match with the otp entered in EditText.
                                forceResendingToken = token;
                            }

                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                // This method will work when the SIM is in our device.
                                progressDialog.setMessage("Verifying Code");
                                progressDialog.show();
                                signInWithPhoneAuthCredential(phoneAuthCredential);

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(OtpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })

                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        binding.OTP.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser) {
                Toast.makeText(OtpActivity.this, pinview.getValue(), Toast.LENGTH_SHORT).show();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OtpId , pinview.getValue());
                signInWithPhoneAuthCredential(credential);
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        progressDialog.setMessage("Logging In");
        progressDialog.setCancelable(false);
        progressDialog.show();

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            progressDialog.dismiss();

                            // Getting Users Info(Phone Number) stored in the Firebase.
                            UsersModel user = new UsersModel(completeNumber , PhoneNum);

                            // Getting UserID (Uid) created in the Firebase
                            String id = task.getResult().getUser().getUid();

                            //Setting the Uid and other Info of the User in Realtime Database.
                            database.getReference().child("Users").child(id).setValue(user);    // The data in the firebase is stored in JSON format.

                            Toast.makeText(OtpActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(OtpActivity.this , UserInfoActivity.class));
                            finish();

                        }
                        else {
                            progressDialog.dismiss();
                            Toast.makeText(OtpActivity.this, task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }

}