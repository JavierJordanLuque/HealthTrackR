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

        if (savedInstanceState == null) {
            String[] credentials = AuthenticationService.getCredentials(this);
            String email = (credentials != null) ? credentials[0] : null;
            String password = (credentials != null) ? credentials[1] : null;

            if (email != null && password != null) {
                try {
                    User user = AuthenticationService.login(this, email, password);

                    Intent intent = new Intent(this, MainActivity.class);
                    ((HealthTrackRApp) getApplication()).getSessionViewModel().setUserSession(user);
                    startActivity(intent);
                    finish();
                } catch (AuthenticationException exception) {
                    ExceptionManager.advertiseUI(this, exception.getMessage());
                }
            }
        }

        Button buttonLogIn = findViewById(R.id.buttonLogIn);
        buttonLogIn.setOnClickListener((view) -> {
            Intent intent = new Intent(this, LogInActivity.class);
            startActivity(intent);
        });

        Button buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener((view) -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
