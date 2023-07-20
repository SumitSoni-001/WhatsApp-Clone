package com.example.whatsapp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.whatsapp.Adapter.UsersAdapter;
import com.example.whatsapp.Models.UsersModel;
import com.example.whatsapp.databinding.FragmentChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ChatFragment extends Fragment {

    public ChatFragment() {
        // Required empty public constructor
    }

    FragmentChatBinding binding;
    FirebaseDatabase database;
    ArrayList<UsersModel> list = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater , container , false);

        database = FirebaseDatabase.getInstance();

        // Setting Adapter on the Chats Recycler View.
        UsersAdapter adapter = new UsersAdapter(list , getContext());
        binding.chatRCV.setAdapter(adapter);

        // Setting Layout Manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.chatRCV.setLayoutManager(layoutManager);

        binding.chatRCV.showShimmerAdapter();

        // The code below helps us to get all the users to the activity from database directly.
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                /* A DataSnapshot acts like a detailed table of content , providing the user with accessible copies
                of data that they can rollback(i.e Undo) to.  */

                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){ // It will get the child(i.e Users) from database until the last user is accessed.
                    UsersModel users = dataSnapshot.getValue(UsersModel.class);
                    users.setUserId(dataSnapshot.getKey()); // We get the Values on the basis of Id (i.e "Uid") , because it is unique.

                    if (!FirebaseAuth.getInstance().getUid().equals(users.getUserId()))
                    {
                        // Now, The User who has SignedIn is not shown.
                        list.add(users);
                    }
                }
                binding.chatRCV.hideShimmerAdapter();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return binding.getRoot();
    }
}