package com.kevalshetta.ChatApp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.kevalshetta.buddiesgram.R;

public class SettingsActivity extends AppCompatActivity {

    private Button logout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();

        logout = findViewById(R.id.sett_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                SendUserToLoginActivity();
            }
        });
    }

    private void SendUserToLoginActivity() {
        startActivity(new Intent(SettingsActivity.this,LoginActivity.class));
        finish();
    }
}