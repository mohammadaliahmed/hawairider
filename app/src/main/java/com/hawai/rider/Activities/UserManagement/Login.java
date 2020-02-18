package com.hawai.rider.Activities.UserManagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.hawai.rider.Activities.MainActivity;
import com.hawai.rider.Models.RiderModel;
import com.hawai.rider.R;
import com.hawai.rider.Utils.CommonUtils;
import com.hawai.rider.Utils.SharedPrefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {


    TextView signup;
    Button login;
    EditText username, password;
    DatabaseReference mDatabase;
    HashMap<String, RiderModel> riderMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signup = findViewById(R.id.signup);
        login = findViewById(R.id.login);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        getDatFromServer();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(Login.this, Register.class));

            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String us = username.getText().toString();
                String pas = password.getText().toString();
                if (username.getText().length() == 0) {
                    username.setError("Enter username");
                } else if (password.getText().length() == 0) {
                    password.setError("Enter password");
                } else {
                    checkUser(us, pas);
                }
            }
        });


    }

    private void checkUser(String us, String pas) {
        if (riderMap.containsKey(us)) {
            if (riderMap.get(us).getPassword().equalsIgnoreCase(pas)) {
                CommonUtils.showToast("Login successfull");
                SharedPrefs.setUserModel(riderMap.get(us));
                startActivity(new Intent(Login.this, MainActivity.class));
                finish();
            } else {
                CommonUtils.showToast("Wrong password");
            }
        } else {
            CommonUtils.showToast("User does not exists");
        }
    }

    private void getDatFromServer() {
        mDatabase.child("Riders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        RiderModel model = snapshot.getValue(RiderModel.class);
                        if (model != null) {
                            riderMap.put(model.getId(), model);
                        }
                    }
                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
