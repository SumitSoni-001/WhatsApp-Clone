package com.example.whatsapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapp.OtherActivities.ChatDetailActivity;
import com.example.whatsapp.R;
import com.example.whatsapp.Models.UsersModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    ArrayList<UsersModel> list;
    Context context;

    public UsersAdapter(ArrayList<UsersModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        UsersModel users = list.get(position);
        Picasso.get().load(users.getProfilePic()).placeholder(R.drawable.user2).into(holder.image);
        holder.userName.setText(users.getUsername());

        // Getting Last Message
        /* We know that the last message is recognised by the sender , so firstly we reach to senderRoom, then we
           arrange the chat child's in descending order using "orderByChild()" method in reference to 'timeStamp'
           because time is an Integer value. Then we set the limit to get only one message from database.
           Atlast , calling SingleValueEventListener , we set the msg accessed from database on Lastmsg textView. */
        FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(/* Sender Room */FirebaseAuth.getInstance().getUid() + users.getUserId())
                .orderByChild("timeStamp")
                .limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChildren()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                holder.lastMessage.setText(snapshot1.child("message").getValue().toString());
                                holder.lastMsgTime.setText(snapshot1.child("time").getValue(String.class));
                            }
                        } else
                            holder.lastMessage.setText("Tap to chat");
//                            holder.lastMsgTime.setText("");
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        if (holder.lastMessage.getText().equals("Tap to chat"))
        {
            holder.lastMsgTime.setText("");
        }

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra("userId", users.getUserId());
                intent.putExtra("dp", users.getProfilePic());
                intent.putExtra("name", users.getUsername());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }      // This adapter class contains the information of users that we see in chat section.


    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView userName, lastMessage, lastMsgTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.username);
            lastMessage = itemView.findViewById(R.id.lastMsg);
            lastMsgTime = itemView.findViewById(R.id.lastMsgTime);
        }
    }

}
