package com.example.whatsapp.OtherActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.whatsapp.Adapter.FragmentAdapter;
import com.example.whatsapp.Auth.SignUpActivity;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference("message");     // Extra

        binding.viewpager.setAdapter(new FragmentAdapter(getSupportFragmentManager())); // Adapter is set to viewPager because a view pager switch between many Fragments.
        binding.tabLayout.setupWithViewPager(binding.viewpager);    // The Tab Layout shows the fragments in reference of their positions in view pager.

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
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu , menu);   /* The 1st Parameter shows "menu" Resource file in the resources(res)
                    directory.and the 2nd Parameter shows "menu" variable which is defined in"onCreateOptionsMenu" method.*/
        return true;
    }

    // Adding features to the Menu Items (ForEx:- Setting , LogOut)
    public boolean onOptionsItemSelected(@NonNull MenuItem item){

        switch (item.getItemId()){

            case R.id.Search:

                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                break;

            case R.id.GroupChat:

                Toast.makeText(this, "GroupChat", Toast.LENGTH_SHORT).show();
                break;

            case R.id.Setting:

                startActivity(new Intent(MainActivity.this , SettingActivity.class));
                break;

            case R.id.Logout:

                FirebaseAuth.getInstance().signOut();

                // After Logging Out , the app will take us to login Page.
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
                finish();
                break;
        }

        return true;
    }
}