package com.example.whatsapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapp.OtherActivities.MainActivity;
import com.example.whatsapp.R;
import com.example.whatsapp.Models.StatusModel;
import com.example.whatsapp.Models.UserStatusModel;
import com.example.whatsapp.databinding.SampleStatusBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.viewHolder> {

    ArrayList<UserStatusModel> userStatusList;
    Context context;

    public StatusAdapter(ArrayList<UserStatusModel> userStatusList, Context context) {
        this.userStatusList = userStatusList;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_status , parent , false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        UserStatusModel userStatusModel = userStatusList.get(position);
        StatusModel statusModel = new StatusModel();

        // To show the last status updated on Circular Status View In statusFragment.
        StatusModel lastStatus = userStatusModel.getStatuses().get(userStatusModel.getStatuses().size() - 1);
        Picasso.get().load(lastStatus.getImageUrl()).into(holder.binding.circleImg);

        holder.binding.tvName.setText(userStatusModel.getUsername());

        holder.binding.circularStatusView.setPortionsCount(userStatusModel.getStatuses().size());

//        holder.binding.LastUpdated.setText(userStatusModel.getLastUpdated().toString());

      /*  if (userStatus.areAllSeen()) {
            //set all portions color
            holder.binding.circularStatusView.setPortionsColor(R.color.lightGrey);
        } else {
            for (int i = 0; i < userStatusList.size(); i++) {
                UserStatusModel status = userStatusList.get(i);
                int color = status.isSeen() ? R.color.lightGrey : R.color.colorPrimary;
                //set specific color for every portion
                holder.binding.circularStatusView.setPortionColorForIndex(i, color);
            }

        }*/

        holder.binding.StatusLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, "Long Pressed", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        holder.binding.StatusLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<MyStory> myStories = new ArrayList<>();

                for(StatusModel status : userStatusModel.getStatuses()){
                    myStories.add(new MyStory(status.getImageUrl()));
                }

                new StoryView.Builder(((MainActivity)context).getSupportFragmentManager())
                        .setStoriesList(myStories) // Required
                        .setStoryDuration(10000) // Default is 2000 Millis (2 Seconds)
                        .setTitleText(userStatusModel.getUsername()) // Default is Hidden
                        .setSubtitleText(statusModel.getCurrentTime()) // Default is Hidden
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
    }

    @Override
    public int getItemCount() {
        return userStatusList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{

        SampleStatusBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = SampleStatusBinding.bind(itemView);
        }
    }

}
