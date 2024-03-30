package com.javierjordanluque.healthtrackr.ui;

import static com.javierjordanluque.healthtrackr.ui.BaseActivity.PREFS_EMAIL;
import static com.javierjordanluque.healthtrackr.ui.BaseActivity.PREFS_NAME;
import static com.javierjordanluque.healthtrackr.ui.BaseActivity.PREFS_PASSWORD;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

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

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = sharedPreferences.getString(PREFS_EMAIL, null);
        String password = sharedPreferences.getString(PREFS_PASSWORD, null);

        if (email != null && password != null) {
            try {
                User user = AuthenticationService.login(this, email, password);

                Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } catch (AuthenticationException exception) {
                ExceptionManager.advertiseUI(this, exception.getMessage());
            }
        }

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
