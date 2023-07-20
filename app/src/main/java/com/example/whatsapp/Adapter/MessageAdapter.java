package com.example.whatsapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapp.R;
import com.example.whatsapp.Models.MessageModels;
import com.example.whatsapp.databinding.SampleDeleteDialogBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter {
    /* We always apply conditions according to "Sender" because :
        1). we know that every User is a sender and he/she sees the messages that he send or receives.
        2). Sender is only one.
        3). It's Id is extracted Easily from Auth.
    */

    ArrayList<MessageModels> list;
    Context context;
    String SenderRoom;
    String ReceiverRoom;

    public MessageAdapter(ArrayList<MessageModels> list, Context context, String senderRoom, String receiverRoom) {
        // We got Sender and Receiver Room from ChatDetailActivity.
        this.list = list;
        this.context = context;
        SenderRoom = senderRoom;
        ReceiverRoom = receiverRoom;
    }

    int SenderViewType = 1;
    int ReceiverViewType = 2;

    @Override
    // Identifying ViewType
    public int getItemViewType(int position) {
        if (list.get(position).getUid().equals(FirebaseAuth.getInstance().getUid())) {
            // It will matches the current SignIn account Id with the msgId(It is extracted by getting the id of the user who sends the msg)
            /* Means : Agr msg send krne wale ki Id current SignedIn user se match ho gyi iska mtlb hai ki vo sender Hai,
               to uska msg green colour k box me appear hoga. */
            return SenderViewType;
        } else
            return ReceiverViewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SenderViewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_reciever, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // ViewHolder describes the position of item in RecyclerView.
        MessageModels messageModels = list.get(position);

        /* In OnCreateViewHolder() , we come to know ki konsa ViewHolder lena hai , so now we have to set the views of
         * that viewHolder in the RecyclerView According to Positions. */
        if (holder.getClass() == SenderViewHolder.class) {
//            ((SenderViewHolder)holder).senderTime.setText(messageModels.getTimeStamp().toString());

            if (messageModels.getMessage().equals("Photo")) {   // If picture is sent
                ((SenderViewHolder) holder).image.setVisibility(View.VISIBLE);
                ((SenderViewHolder) holder).senderMsg.setVisibility(View.GONE);
                Picasso.get().load(messageModels.getImageUrl()).placeholder(R.drawable.ic_placeholder).into(((SenderViewHolder) holder).image);
                ((SenderViewHolder) holder).senderTime.setText(messageModels.getTime());
            } else {  // If simple text is sent
                ((SenderViewHolder) holder).senderMsg.setText(messageModels.getMessage());
                ((SenderViewHolder) holder).senderTime.setText(messageModels.getTime());
            }

            // Deleting a message on Sender side
            ((SenderViewHolder) holder).itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.sample_delete_dialog, null);
                    SampleDeleteDialogBinding DialogBinding = SampleDeleteDialogBinding.bind(view);    // Custom Dialog

                    AlertDialog alertDialog = new AlertDialog.Builder(context)  // Setting Alert Dialog on our Custom Dialog
                            .setView(DialogBinding.getRoot())
                            .setCancelable(false)
                            .create();
                    alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    alertDialog.getWindow().setWindowAnimations(R.style.Animation);

                    if (messageModels.getMessage().equals("This message is removed.")){
                        DialogBinding.everyone.setVisibility(View.GONE);
                    }
                    if (messageModels.getMessage().equals("Photo")){
                        DialogBinding.everyone.setVisibility(View.GONE);
                    }

                    DialogBinding.delete.setOnClickListener(new View.OnClickListener() {
                        // The message is only deleted on Sender Side.
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Chats")
                                    .child(SenderRoom)
                                    .child(messageModels.getMessageId())
                                    .setValue(null);

                            alertDialog.dismiss();
                        }
                    });

                    DialogBinding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    DialogBinding.everyone.setOnClickListener(new View.OnClickListener() {
                        // The message will be deleted on both Sender as well as Receiver Side.
                        @Override
                        public void onClick(View v) {

                                messageModels.setMessage("This message is removed.");

                                FirebaseDatabase.getInstance().getReference()
                                        .child("Chats")
                                        .child(SenderRoom)
                                        .child(messageModels.getMessageId())
                                        .setValue(messageModels);

                                FirebaseDatabase.getInstance().getReference()
                                        .child("Chats")
                                        .child(ReceiverRoom)
                                        .child(messageModels.getMessageId())
                                        .setValue(messageModels);

                                alertDialog.dismiss();
                            }
                    });

                    alertDialog.show();
                    return false;
                }
            });
        } else {
            //  ((ReceiverViewHolder)holder).ReceiverTime.setText(messageModels.getTimeStamp().toString());

            if (messageModels.getMessage().equals("Photo")) {   // If picture is Received
                ((ReceiverViewHolder) holder).Rimage.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder) holder).ReceiverMsg.setVisibility(View.GONE);
                Picasso.get().load(messageModels.getImageUrl()).placeholder(R.drawable.ic_placeholder).into(((ReceiverViewHolder) holder).Rimage);
                ((ReceiverViewHolder) holder).ReceiverTime.setText(messageModels.getTime());
            } else {  // If simple text is Received
                ((ReceiverViewHolder) holder).ReceiverMsg.setText(messageModels.getMessage());
                ((ReceiverViewHolder) holder).ReceiverTime.setText(messageModels.getTime());
            }

            // Deleting a message on Receiver side
            ((ReceiverViewHolder)holder).itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.sample_delete_dialog, null);
                    SampleDeleteDialogBinding DialogBinding = SampleDeleteDialogBinding.bind(view);    // Custom Dialog

                    AlertDialog alertDialog = new AlertDialog.Builder(context)  // Setting Alert Dialog on our Custom Dialog
                            .setView(DialogBinding.getRoot())
                            .setCancelable(false)
                            .create();
                    alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    alertDialog.getWindow().setWindowAnimations(R.style.Animation);

                    DialogBinding.delete.setOnClickListener(new View.OnClickListener() {
                        // The message is only deleted on Sender Side.
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Chats")
                                    .child(SenderRoom)
                                    .child(messageModels.getMessageId())
                                    .setValue(null);

                            alertDialog.dismiss();
                        }
                    });

                    DialogBinding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    DialogBinding.everyone.setVisibility(View.GONE);

                    alertDialog.show();
                    return false;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView senderMsg, senderTime;
        ImageView image;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderMsg);
            senderTime = itemView.findViewById(R.id.senderTime);
            image = itemView.findViewById(R.id.SentImg);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView ReceiverMsg, ReceiverTime;
        ImageView Rimage;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            ReceiverMsg = itemView.findViewById(R.id.ReceiverMsg);
            ReceiverTime = itemView.findViewById(R.id.ReceiverTime);
            Rimage = itemView.findViewById(R.id.ReceivedImg);
        }
    }
}