package com.example.whatsapp.Auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.example.whatsapp.databinding.ActivityGetPhoneNumBinding;
import com.rilixtech.widget.countrycodepicker.Country;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

public class GetPhoneNum extends AppCompatActivity {
    ActivityGetPhoneNumBinding binding;
    CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGetPhoneNumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        ccp = findViewById(R.id.ccp);
        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected(Country selectedCountry) {
                Toast.makeText(GetPhoneNum.this, "Updated : " + selectedCountry.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(binding.etPhone.getText().toString().isEmpty() == true) {
                    Toast.makeText(GetPhoneNum.this, "Please enter a Phone NUmber", Toast.LENGTH_SHORT).show();

                }
                else {
                    Intent intent = new Intent(GetPhoneNum.this, OtpActivity.class);
                    intent.putExtra("code", binding.ccp.getSelectedCountryCodeWithPlus());
                    intent.putExtra("phone", binding.etPhone.getText().toString());
                    startActivity(intent);
                }
            }
        });

        binding.myNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(binding.etPhone.getText().toString().isEmpty()))
                Toast.makeText(GetPhoneNum.this, "Number Entered : " +binding.etPhone.getText().toString() , Toast.LENGTH_SHORT).show();

                else
                Toast.makeText(GetPhoneNum.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
            }
        });

    }
}