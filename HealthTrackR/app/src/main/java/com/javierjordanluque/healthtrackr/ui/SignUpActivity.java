package com.javierjordanluque.healthtrackr.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.util.AuthenticationService;
import com.javierjordanluque.healthtrackr.util.exceptions.AuthenticationException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.util.Objects;

public class SignUpActivity extends BaseActivity {
    protected User user;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout repeatPasswordLayout;
    private TextInputLayout firstNameLayout;
    private TextInputLayout lastNameLayout;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;
    private EditText firstNameEditText;
    private EditText lastNameEditText;

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

        firstNameLayout = findViewById(R.id.firstNameLayout);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        setEditTextListener(firstNameLayout, firstNameEditText);

        lastNameLayout = findViewById(R.id.lastNameLayout);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        setEditTextListener(lastNameLayout, lastNameEditText);

        Button signUpButton = findViewById(R.id.buttonSignUp);
        signUpButton.setOnClickListener(this::signUp);
    }

    public void signUp(View view) {
        hideKeyboard(this);

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String repeatPassword = repeatPasswordEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();

        boolean validEmail = isValidEmail(email);
        boolean validPassword = isValidPassword(password);
        boolean validRepeatPassword = isValidRepeatPassword(password, repeatPassword);
        boolean validFirstName = isValidFirstName(firstName);
        boolean validLastName = isValidLastName(lastName);

        if (!validEmail || !validPassword || !validRepeatPassword || !validFirstName || !validLastName) {
            if (!validEmail)
                emailLayout.setError(getString(R.string.error_invalid_email));
            if (!validPassword)
                passwordLayout.setError(getString(R.string.error_invalid_password));
            if (!validRepeatPassword)
                repeatPasswordLayout.setError(getString(R.string.error_invalid_repeat_password));
            if (!validFirstName)
                firstNameLayout.setError(getString(R.string.error_invalid_first_name));
            if (!validLastName)
                lastNameLayout.setError(getString(R.string.error_invalid_last_name));

            return;
        }

        try {
            user = AuthenticationService.register(this, email, password, firstName, lastName);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } catch (AuthenticationException exception) {
            if (Objects.equals(exception.getMessage(), getString(R.string.error_existing_email)) ||
                    Objects.equals(exception.getMessage(), getString(R.string.error_invalid_email_requirements))) {
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

    private boolean isValidFirstName(String firstName) {
        return !firstName.isEmpty() && firstName.length() <= 50;
    }

    private boolean isValidLastName(String lastName) {
        return !lastName.isEmpty() && lastName.length() <= 50;
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