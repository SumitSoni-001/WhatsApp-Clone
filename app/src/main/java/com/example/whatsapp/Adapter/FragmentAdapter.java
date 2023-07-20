package com.example.whatsapp.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.whatsapp.Fragments.CallsFragment;
import com.example.whatsapp.Fragments.ChatFragment;
import com.example.whatsapp.Fragments.StatusFragment;

import java.time.format.TextStyle;

public class FragmentAdapter extends FragmentPagerAdapter {
    public FragmentAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // Calling Fragments
        switch(position){     // This switch is used to define the position of the Fragments.

            case 0: return new ChatFragment();
            case 1: return new StatusFragment();
            case 2: return new CallsFragment();
            default: return new ChatFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;   // Because there are 3 Fragments.
    }

    public CharSequence getPageTitle(int Position){
        String title = null;
                                // Setting Titles of the Tabs present in TabLayout in Main Activity.
        if(Position == 0){
            title = "Chats";
        }
        if (Position == 1){
            title = "Status";
        }
        if (Position == 2){
            title = "Calls";
        }

        return title;
    }
}
