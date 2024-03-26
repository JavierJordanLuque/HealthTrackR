package com.javierjordanluque.healthtrackr.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.util.authentication.AuthenticationService;
import com.javierjordanluque.healthtrackr.util.exceptions.AuthenticationException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.util.Objects;

public class LogInActivity extends BaseActivity {
    protected User user;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        emailLayout = findViewById(R.id.emailLayout);
        emailEditText = findViewById(R.id.emailEditText);
        setEditTextListener(emailLayout, emailEditText);

        passwordLayout = findViewById(R.id.passwordLayout);
        passwordEditText = findViewById(R.id.passwordEditText);
        setEditTextListener(passwordLayout, passwordEditText);

        Button logInButton = findViewById(R.id.buttonLogIn);
        logInButton.setOnClickListener(this::logIn);
    }

    public void logIn(View view) {
        hideKeyboard(this);

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        boolean validEmail = isValidEmail(email);
        boolean validPassword = isValidPassword(password);

        if (!validEmail || !validPassword) {
            if (!validEmail)
                emailLayout.setError(getString(R.string.error_invalid_email));
            if (!validPassword)
                passwordLayout.setError(getString(R.string.error_invalid_password));

            return;
        }

        try {
            user = AuthenticationService.login(this, email, password);
        } catch (AuthenticationException exception) {
            if (Objects.equals(exception.getMessage(), getString(R.string.error_incorrect_email))) {
                emailLayout.setError(exception.getMessage());
            } else if (Objects.equals(exception.getMessage(), getString(R.string.error_incorrect_email))) {
                passwordLayout.setError(exception.getMessage());
            } else {
                ExceptionManager.advertiseUI(this, exception.getMessage());
            }
        }
    }

    private boolean isValidEmail(String email) {
        return !email.isEmpty() && email.length() >= 5 && email.length() <= 50;
    }

    private boolean isValidPassword(String password) {
        return !password.isEmpty() && password.length() >= 8 && password.length() <= 60;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_log_in;
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.authentication_login);
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_basic_menu;
    }
}