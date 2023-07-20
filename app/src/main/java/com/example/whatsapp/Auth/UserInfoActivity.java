package com.example.whatsapp.Auth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.whatsapp.OtherActivities.MainActivity;
import com.example.whatsapp.databinding.ActivityUserInfoBinding;
import com.github.drjacky.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class UserInfoActivity extends AppCompatActivity {
    ActivityUserInfoBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("User Info");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Profile Pic");
        progressDialog.setCancelable(false);

        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (binding.etName.getText().toString().isEmpty()){
                    binding.etName.setError("Enter your Name");
                }

                String Name = binding.etName.getText().toString();
                HashMap<String , Object> hashMap = new HashMap<>();
                hashMap.put("username" , Name);

                database.getReference().child("Users").child(auth.getUid()).updateChildren(hashMap);
                Toast.makeText(UserInfoActivity.this, "Profile Created", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(UserInfoActivity.this , MainActivity.class));
                finish();
            }
        });

        binding.addImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.Companion.with(UserInfoActivity.this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .cropOval()	    		//Allow dimmed layer to have a circle inside
//                        .galleryOnly()          //We have to define what image provider we want to use
                        .compress(1024)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data.getData() != null){

            progressDialog.show();
            Uri uri = data.getData();
            binding.profile.setImageURI(uri);

            StorageReference reference = storage.getReference().child("Profile Pictures").child(auth.getUid());

            reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            database.getReference().child("Users").child(auth.getUid()).child("profilePic")
                                    .setValue(uri.toString());

                            progressDialog.dismiss();
                        }
                    });
                }
            });

        }
    }
}