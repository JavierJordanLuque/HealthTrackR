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

public class SignUpActivity extends BaseActivity {
    protected User user;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout repeatPasswordLayout;
    private TextInputLayout fullNameLayout;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;
    private EditText fullNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        emailLayout = findViewById(R.id.emailLayout);
        emailEditText = findViewById(R.id.emailEditText);
        setEditTextListener(emailLayout, emailEditText);

        passwordLayout = findViewById(R.id.passwordLayout);
        passwordEditText = findViewById(R.id.passwordEditText);
        setEditTextListener(passwordLayout, passwordEditText);

        repeatPasswordLayout = findViewById(R.id.repeatPasswordLayout);
        repeatPasswordEditText = findViewById(R.id.repeatPasswordEditText);
        setEditTextListener(repeatPasswordLayout, repeatPasswordEditText);

        fullNameLayout = findViewById(R.id.fullNameLayout);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        setEditTextListener(fullNameLayout, fullNameEditText);

        Button signUpButton = findViewById(R.id.buttonSignUp);
        signUpButton.setOnClickListener(this::signUp);
    }

    public void signUp(View view) {
        hideKeyboard(this);

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String repeatPassword = repeatPasswordEditText.getText().toString().trim();
        String fullName = fullNameEditText.getText().toString().trim();

        boolean validEmail = isValidEmail(email);
        boolean validPassword = isValidPassword(password);
        boolean validRepeatPassword = isValidRepeatPassword(password, repeatPassword);
        boolean validFullName = isValidFullName(fullName);

        if (!validEmail || !validPassword || !validRepeatPassword || !validFullName) {
            if (!validEmail)
                emailLayout.setError(getString(R.string.error_invalid_email));
            if (!validPassword)
                passwordLayout.setError(getString(R.string.error_invalid_password));
            if (!validRepeatPassword)
                repeatPasswordLayout.setError(getString(R.string.error_invalid_repeat_password));
            if (!validFullName)
                fullNameLayout.setError(getString(R.string.error_invalid_full_name));

            return;
        }

        try {
            user = AuthenticationService.register(this, email, password, fullName);
        } catch (AuthenticationException exception) {
            if (Objects.equals(exception.getMessage(), getString(R.string.error_existing_email))) {
                emailLayout.setError(exception.getMessage());
            } else if (Objects.equals(exception.getMessage(), getString(R.string.authentication_password_requirements))) {
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

    private boolean isValidRepeatPassword(String password, String repeatPassword) {
        return password.equals(repeatPassword);
    }

    private boolean isValidFullName(String fullName) {
        return !fullName.isEmpty() && fullName.length() <= 50;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_sign_up;
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.authentication_signup);
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_basic_menu;
    }
}