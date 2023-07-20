package com.example.whatsapp.OtherActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.whatsapp.Adapter.MessageAdapter;
import com.example.whatsapp.Models.MessageModels;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.ActivityChatDetailBinding;
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

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ChatDetailActivity extends AppCompatActivity {
    ActivityChatDetailBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;
    String senderId;
    String ReceiverId;
    String SenderRoom;
    String ReceiverRoom;
    SimpleDateFormat simpleDateFormat;
    SimpleDateFormat date;
    Calendar calendar;
    String CurrentTime;
    String CurrentDate;

// By declaring a variable or method "final" , It is Globalised.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        getSupportActionBar().hide();
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Changing color of System Navigation Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.black));
        }

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        calendar = Calendar.getInstance();

        simpleDateFormat = new SimpleDateFormat("hh:mm a");
        date = new SimpleDateFormat("MM/dd/yyyy");
        CurrentTime = simpleDateFormat.format(calendar.getTime());
        CurrentDate = date.format(calendar.getTime());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Image");
        progressDialog.setCancelable(false);

        senderId = auth.getUid();
        // Getting Views from Users Adapter
        ReceiverId = getIntent().getStringExtra("userId");
        String Username = getIntent().getStringExtra("name");
        String ProfilePic = getIntent().getStringExtra("dp");

        SenderRoom = senderId + ReceiverId;    // SenderRoom describes the chats from sender
        ReceiverRoom = ReceiverId + senderId;  // ReceiveRoom describes the chats that the Receiver receives.

        binding.Username.setText(Username);
        Picasso.get().load(ProfilePic).placeholder(R.drawable.user2).into(binding.ProfilePic);

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(ChatDetailActivity.this , MainActivity.class));
                finish();
            }
        });

        ArrayList<MessageModels> list = new ArrayList<>();

        final MessageAdapter messageAdapter = new MessageAdapter(list, this, SenderRoom, ReceiverRoom);
        binding.chatRecyclerView.setAdapter(messageAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        // Reading data from database
        database.getReference().child("Chats").child(SenderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        list.clear(); // It will force/made the snapshot to get newly typed messages only to view on screen.
    /* A for-each loop is used here, because we have to do this work again and again (i.e we have to extract thousands of messages from
       a single sender and for this, we don't make different child's every time), so to remove confusion we use loop here.*/
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) { // It means ki data tb tk lena hai , jb tk child khtm nahi ho jaate.
                            MessageModels model = snapshot1.getValue(MessageModels.class);
                            model.setMessageId(snapshot1.getKey()); // It is the Random Key for each msg in which chat detail is stored.
                            binding.ChatDate.setText(model.getDate());
                            list.add(model); // The data extracted is added in ArrayList, So that it will appear on RecyclerView.
                        }
                        messageAdapter.notifyDataSetChanged(); // It will cause the RCV to update the screen Immediately after the msg is sent.
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // Reading User's (Receiver) Presence Status
        database.getReference().child("Presence").child(ReceiverId).addValueEventListener(new ValueEventListener() {
            // Receiver Id is taken we want to see that the receiver is online/offline.
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String presence = snapshot.getValue(String.class);

                    if (!presence.isEmpty()) {
                        if (presence.equals("Offline")) {
                            binding.Presence.setVisibility(View.GONE);
                        } else {
                            binding.Presence.setVisibility(View.VISIBLE);
                            binding.Presence.setText(presence);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        // Updating Info that the Sender(Apne liye Receiver h jo) is Typing or not
        final Handler handler = new Handler();    // It is a background Thread
        binding.etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("Presence").child(senderId).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(StoppedTyping, 1000);   /* If the sender stopped typing , this method let the receiver know his current Presence
                                                                         status after half second. */
            }

            Runnable StoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("Presence").child(senderId).setValue("Online");
                }
            };
        });

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (binding.etMessage.getText().toString().isEmpty()) {
                    // If the Message box is empty , this condition will through a Error.
                    binding.etMessage.setError("Type a Message");
                    return;
                }
                String message = binding.etMessage.getText().toString();
                Date date = new Date();
                final MessageModels model = new MessageModels(senderId, message);
                model.setTimeStamp(date.getTime());
                model.setTime(CurrentTime);
//                    model.setDate(CurrentDate);
                binding.etMessage.setText(""); // The editText will be blank by default

                String RandomKey = database.getReference().push().getKey(); /* By creating this , we can set the same key in
        both (Sender as well as Receiver Room) to a respective msg. ForEx:- If "Sumit" send 'hlo' to "Sonia" , then
         the random key for msg hlo by "Sumit" will be same in both sender and receiver room. */

                // Inserting chat data in database
                /* AtFirst, the data is stored on SenderSide and after the data is stored on SenderSide , then the data
                    is stored on Receiver side. */
                database.getReference().child("Chats")
                        .child(SenderRoom)
                        .child(RandomKey)
//                        .push()
                        /* It generates a unique key everytime a new child(here 'chat') is added to the specified Firebase Reference
                            (Here References are 'Sender and Receiver Room).*/
                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    /* The setValue() method sets the data at the specified locations of the object(Here, It sets 'msg','uId' and 'TimeStamp')
                     *  Passing null to setValue() will delete the new and Existing data at the specified location.*/

                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("Chats")
                                .child(ReceiverRoom)
                                .child(RandomKey)
//                                .push()
                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        });
                    }
                });
            }
        });

        binding.Attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ImagePicker.Companion.with(ChatDetailActivity.this)
//                        .crop()                    //Crop image(Optional), Check Customization for more option
////                        .cropOval()	    		//Allow dimmed layer to have a circle inside
//                        .galleryOnly()          //We have to define what image provider we want to use
//                        .compress(1024)
//                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
//                        .start(25);

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 25);

            }
        });

        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.Companion.with(ChatDetailActivity.this)
                        .crop()                    //Crop image(Optional), Check Customization for more option
//                        .cropOval()	    		//Allow dimmed layer to have a circle inside
                        .cameraOnly()          //We have to define what image provider we want to use
                        .compress(1024)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start(35);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 25 || requestCode == 35) {
            if (data != null) {
                if (data.getData() != null) {
                    progressDialog.show();
                    Uri file = data.getData();

                    StorageReference reference = storage.getReference().child("Chats").child(calendar.getTimeInMillis() + "");

                    reference.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    progressDialog.dismiss();

                                    String message = binding.etMessage.getText().toString();
                                    Date date = new Date();

                                    MessageModels model = new MessageModels(senderId, message);
                                    model.setTimeStamp(date.getTime());
                                    model.setTime(CurrentTime);
//                                model.setDate(CurrentDate);
                                    model.setMessage("Photo");
                                    model.setImageUrl(uri.toString());

                                    binding.etMessage.setText(""); // The editText will be blank by default

                                    String RandomKey = database.getReference().push().getKey(); /* By creating this , we can set the same key in
        both (Sender as well as Receiver Room) to a respective msg. ForEx:- If "Sumit" send 'hlo' to "Sonia" , then
         the random key for msg hlo by "Sumit" will be same in both sender and receiver room. */

                                    database.getReference().child("Chats").child(SenderRoom)
                                            .push()
                                            .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {

                                        @Override
                                        public void onSuccess(Void unused) {
                                            database.getReference().child("Chats").child(ReceiverRoom)
                                                    .child(RandomKey)
//                                                .push()
                                                    .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                }
                                            });
                                        }
                                    });

                                }
                            });
                        }
                    });
                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        database.getReference().child("Presence").child(auth.getUid()).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        database.getReference().child("Presence").child(auth.getUid()).setValue("Offline");
    }

    // Inflating Menu Items in Main Activity
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu2, menu);   /* The 1st Parameter shows "menu" Resource file in the resources(res)
                    directory.and the 2nd Parameter shows "menu" variable which is defined in"onCreateOptionsMenu" method.*/
        setTheme(R.style.Theme_AppCompat_DayNight_DarkActionBar);
        return true;
    }

    // Adding features to the Menu Items (ForEx:- Setting , LogOut)
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.viewContact:

                Toast.makeText(this, "View Contact", Toast.LENGTH_SHORT).show();
                break;

            case R.id.files:

                Toast.makeText(this, "Files", Toast.LENGTH_SHORT).show();

            case R.id.search:

                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                break;

            case R.id.wallpaper:

                Toast.makeText(this, "Wallpaper", Toast.LENGTH_SHORT).show();
                break;

            case R.id.Clear_Chat:

                database.getReference().child("Chats").
                        child(SenderRoom).
                        setValue(null);

                break;

        }

        return true;
    }

}