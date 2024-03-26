package com.javierjordanluque.healthtrackr.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.javierjordanluque.healthtrackr.R;

public class AuthenticationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        Button signUpButton = findViewById(R.id.buttonSignUp);
        signUpButton.setOnClickListener((view) -> {
            Intent intent = new Intent(AuthenticationActivity.this, SignUpActivity.class);

            startActivity(intent);
        });

        Button logInButton = findViewById(R.id.buttonLogIn);
        logInButton.setOnClickListener((view) -> {
            Intent intent = new Intent(AuthenticationActivity.this, LogInActivity.class);

            startActivity(intent);
        });
    }
}
