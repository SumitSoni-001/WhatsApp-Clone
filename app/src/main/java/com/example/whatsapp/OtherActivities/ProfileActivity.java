package com.example.whatsapp.OtherActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.whatsapp.Models.UsersModel;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.ActivityProfileBinding;
import com.github.drjacky.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;    // It is used to store image , video data on Firebase.
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Profile Pic");
        progressDialog.setCancelable(false);

        binding.addProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent , 40);*/

                ImagePicker.Companion.with(ProfileActivity.this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .cropOval()	    		//Allow dimmed layer to have a circle inside
//                        .galleryOnly()          //We have to define what image provider we want to use
                        .compress(1024)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();

            }
        });

        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Username = binding.etName.getText().toString();
                String about = binding.etAbout.getText().toString();

                /* Hashmap is used to store data in the form of key-value pairs.
                    "key" is the child node in Firebase Realtime Database.  ForEx :- username , about.
                    and "value" is the data we pass in the key. forEX :- Username , about.
                 */
                HashMap<String , Object> hashMap = new HashMap<>();
                hashMap.put("username" , Username);
                hashMap.put("about" , about);

                database.getReference().child("Users").child(auth.getUid()).updateChildren(hashMap);
                Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
            }
        });

        // Getting the data from Realtime database and setting it on the views of Profile Activity.
        database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UsersModel users = snapshot.getValue(UsersModel.class);
                Picasso.get().load(users.getProfilePic()).placeholder(R.drawable.user2).into(binding.profile);
                binding.etName.setText(users.getUsername());
                binding.etAbout.setText(users.getAbout());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
//        startActivity(new Intent(ProfileActivity.this , SettingActivity.class));
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data.getData() != null) {

            progressDialog.show();
            Uri file = data.getData();
            binding.profile.setImageURI(file);

            // Getting reference Of Firebase and creating child nodes.
            /* The child node in which the image is stored is named as "Id of user", because only profile pic is changed and for a change at
                single location we will not increase the storage space. But in Social media sites, when we add post, a new image is uploaded
                everytime without removing old post.*/
            final StorageReference reference = storage.getReference().child("Profile Pictures").child(auth.getUid());

            reference.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Now the URL of the Image is stored in FirebaseStorage.
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
    /* Inserting the Profile Pic in Realtime database inside Users node, and the Image is Inserted in the current User's node by making a
    child named "profile pic" in the form of string , because we know that a URL is a String. */
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