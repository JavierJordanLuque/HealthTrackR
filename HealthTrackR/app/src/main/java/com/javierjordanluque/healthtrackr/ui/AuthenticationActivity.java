package com.javierjordanluque.healthtrackr.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.javierjordanluque.healthtrackr.HealthTrackRApp;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.util.AuthenticationService;
import com.javierjordanluque.healthtrackr.util.exceptions.AuthenticationException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

public class AuthenticationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        Object[] credentials = AuthenticationService.getCredentials(this);
        String email = null;
        String password = null;
        if (credentials != null) {
            email = (String) credentials[0];
            password = (String) credentials[1];
        }

        if (email != null && password != null) {
            try {
                User user = AuthenticationService.login(this, email, password);

                Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
                ((HealthTrackRApp) getApplication()).getSessionViewModel().setUserSession(user);
                startActivity(intent);
                finish();
            } catch (AuthenticationException exception) {
                ExceptionManager.advertiseUI(this, exception.getMessage());
            }
        }

        Button buttonLogIn = findViewById(R.id.buttonLogIn);
        buttonLogIn.setOnClickListener((view) -> {
            Intent intent = new Intent(AuthenticationActivity.this, LogInActivity.class);
            startActivity(intent);
        });

        Button buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener((view) -> {
            Intent intent = new Intent(AuthenticationActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
