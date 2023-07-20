package com.example.whatsapp.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.whatsapp.Adapter.StatusAdapter;
import com.example.whatsapp.Models.StatusModel;
import com.example.whatsapp.Models.UserStatusModel;
import com.example.whatsapp.Models.UsersModel;
import com.example.whatsapp.databinding.FragmentStatusBinding;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class StatusFragment extends Fragment {

    FragmentStatusBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;
    StatusAdapter statusAdapter;
    ArrayList<UserStatusModel> userStatusesList;
    ArrayList<UserStatusModel> MyStatusList;    // To store MyStatuses (i.e Account Owner)
    UserStatusModel userStatusModel;
    UsersModel usersModel;  // Used to get userInfo from UsersModel and set them to views of UsersStatusModel
    SimpleDateFormat simpleDateFormat;
    Calendar calendar;
    String CurrentTime;

    public StatusFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStatusBinding.inflate(inflater, container, false);

        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("hh:mm a");

        userStatusesList = new ArrayList<>();
        MyStatusList = new ArrayList<>();
        statusAdapter = new StatusAdapter(userStatusesList, getContext());
        binding.statusRCV.setAdapter(statusAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.statusRCV.setLayoutManager(linearLayoutManager);

//        binding.statusRCV.showShimmerAdapter();

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usersModel = snapshot.getValue(UsersModel.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // Reading data from Realtime database.
        database.getReference().child("Stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userStatusesList.clear();
                    for (DataSnapshot storySnapshot : snapshot.getChildren()) {

                        UserStatusModel userStatusModel = new UserStatusModel();
                        userStatusModel.setUsername(storySnapshot.child("username").getValue(String.class));
                        userStatusModel.setProfilePic(storySnapshot.child("profilePic").getValue(String.class));
                        userStatusModel.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));

                        // Storing the status Images url inside ArrayList and then set it to ArrayList created in UserStatusModel.
                        ArrayList<StatusModel> statusesList = new ArrayList<>();
                        for (DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()) {
                            StatusModel statusModel = statusSnapshot.getValue(StatusModel.class);
                            statusesList.add(statusModel);
                        }
                        userStatusModel.setStatuses(statusesList);

                        // Separating Statuses of Receiver and Sender
                        if (!auth.getUid().equals(storySnapshot.getKey())) {
                            userStatusesList.add(userStatusModel);
                        } else
                            MyStatusList.add(userStatusModel);

                    }
//                    binding.statusRCV.hideShimmerAdapter();
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Uploading image as status
        binding.addStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.Companion.with(StatusFragment.this)
                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });

        // Setting views On MyStatus
      /* userStatusModel = new UserStatusModel();
        userStatusModel = MyStatusList.get();
       /* if (!MyStatusList.isEmpty())
        {
            // To show the last status updated on Circular Status View In statusFragment.
            StatusModel lastStatus = userStatusModel.getStatuses().get(userStatusModel.getStatuses().size() - 1);
            Picasso.get().load(lastStatus.getImageUrl()).into(binding.circleImg);

            binding.MyStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<MyStory> myStories = new ArrayList<>();

                    for (StatusModel status : userStatusModel.getStatuses()) {
                        myStories.add(new MyStory(status.getImageUrl()));
                    }

                    new StoryView.Builder(((MainActivity) getContext()).getSupportFragmentManager())
                            .setStoriesList(myStories) // Required
                            .setStoryDuration(10000) // Default is 2000 Millis (2 Seconds)
                            .setTitleText(userStatusModel.getUsername()) // Default is Hidden
                            .setSubtitleText(userStatusModel.getLastUpdated().toString()) // Default is Hidden
                            .setTitleLogoUrl(userStatusModel.getProfilePic()) // Default is Hidden
                            .setStoryClickListeners(new StoryClickListeners() {
                                @Override
                                public void onDescriptionClickListener(int position) {
                                    //your action
                                }

                                @Override
                                public void onTitleIconClickListener(int position) {
                                    //your action
                                }
                            }) // Optional Listeners
                            .build() // Must be called before calling show method
                            .show();
                }
            });
        }*/

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (data.getData() != null) {

                dialog.show();
                Date date = new Date();
                Uri file = data.getData();

                // Getting reference Of Firebase and creating child nodes.
                final StorageReference reference = storage.getReference().child("Status").child(date.getTime() + "");

                reference.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                            @Override
                            // After generating URL in firebase storage, we will update the userinfo seen in inside status and the image uploaded as status in RealTime database.
                            public void onSuccess(Uri uri) {
                                UserStatusModel userStatusModel = new UserStatusModel();
                                CurrentTime = simpleDateFormat.format(calendar.getTime());

                                // Setting UserInfo
                                userStatusModel.setUsername(usersModel.getUsername());
                                userStatusModel.setProfilePic(usersModel.getProfilePic());
                                userStatusModel.setLastUpdated(date.getTime());

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("username", userStatusModel.getUsername());
                                hashMap.put("profilePic", userStatusModel.getProfilePic());
                                hashMap.put("lastUpdated", userStatusModel.getLastUpdated());

                                // Updating UserInfo in Realtime database which is seen on opening a status.
                                database.getReference().child("Stories").child(auth.getUid())
                                        .updateChildren(hashMap);

                                // Setting Status and Its Time
                                String ImageUrl = uri.toString();
                                StatusModel statusModel = new StatusModel(ImageUrl, CurrentTime, userStatusModel.getLastUpdated());

                                    /* Updating Status in Realtime database ,by creating "status node and inserting
                                       the picture uploaded as status by getting url of image. */
                                database.getReference().child("Stories").child(auth.getUid())
                                        .child("statuses")
                                        .push().setValue(statusModel);

                                dialog.dismiss();

                            }
                        });
                    }
                });

            }
        }

    }
}