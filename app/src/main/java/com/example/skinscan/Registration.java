package com.example.skinscan;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registration extends AppCompatActivity {

    DatabaseReference databaseReference;

    TextInputEditText etRegEmail;
    TextInputEditText etRegPassword;

    TextInputEditText etRegPhoneNumber;

    TextInputEditText etRegUsername;

    TextView tvLoginHere;
    Button btnRegister;

    TextInputEditText etRegAge;
    RadioButton radioMale;
    RadioButton radioFemale;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users"); // Reference to your "users" node in the database

        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPass);
        etRegAge = findViewById(R.id.etRegAge);
        radioMale = findViewById(R.id.radioMale);
        radioFemale = findViewById(R.id.radioFemale);
        tvLoginHere = findViewById(R.id.tvLoginHere);
        btnRegister = findViewById(R.id.btnRegister);
        etRegPhoneNumber = findViewById(R.id.etRegPhoneNumber);
        etRegUsername = findViewById(R.id.etRegusername);

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(view ->{
            createUser();
        });

        tvLoginHere.setOnClickListener(view ->{
            startActivity(new Intent(Registration.this, LoginActivity.class));
        });
    }

    private void createUser(){
        String email = etRegEmail.getText().toString();
        String password = etRegPassword.getText().toString();
        String age = etRegAge.getText().toString();
        String phoneNumber = etRegPhoneNumber.getText().toString();
        String username = etRegUsername.getText().toString();
        String gender = "";

        if (radioMale.isChecked()) {
            gender = "Male";
        } else if (radioFemale.isChecked()) {
            gender = "Female";
        }

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(age) || TextUtils.isEmpty(gender)){
            etRegEmail.setError("Email cannot be empty");
            etRegEmail.requestFocus();

            etRegPassword.setError("Password cannot be empty");
            etRegPassword.requestFocus();

            etRegAge.setError(" ");



        }else{
            final String finalAge = age;
            final String finalGender = gender;
            final String finalname = username;
            final String finalphoneno = phoneNumber;
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(age)
                                .build();
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // Age added to user profile successfully
                                        // You can handle further actions here
                                        // Save additional data to the database
                                        String userId = user.getUid();
                                        DatabaseReference currentUserDB = databaseReference.child(userId);
                                        currentUserDB.child("name").setValue(finalname);
                                        currentUserDB.child("phonenumber").setValue(finalphoneno);
                                        currentUserDB.child("age").setValue(finalAge);
                                        currentUserDB.child("gender").setValue(finalGender);

                                        Toast.makeText(Registration.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Registration.this, LoginActivity.class));
                                    }
                                });

                        // Store gender in the database or user profile as needed
                        // ...
                    }
                } else {
                    // Handle registration failure
                }
            });
        }
    }

}