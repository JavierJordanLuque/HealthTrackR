package com.javierjordanluque.healthtrackr.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.app.NavUtils;

import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.util.AuthenticationService;
import com.javierjordanluque.healthtrackr.util.exceptions.AuthenticationException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.util.Objects;

public class SignUpActivity extends BaseActivity {
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
        setContentView( R.layout.activity_sign_up);
        setUpToolbar(getString(R.string.authentication_signup));
        showBackButton(true);

        emailLayout = findViewById(R.id.layoutEmail);
        emailEditText = findViewById(R.id.editTextEmail);
        setEditTextListener(emailLayout, emailEditText);

        passwordLayout = findViewById(R.id.layoutPassword);
        passwordEditText = findViewById(R.id.editTextPassword);
        setEditTextListener(passwordLayout, passwordEditText);

        repeatPasswordLayout = findViewById(R.id.layoutRepeatPassword);
        repeatPasswordEditText = findViewById(R.id.editTextRepeatPassword);
        setEditTextListener(repeatPasswordLayout, repeatPasswordEditText);

        firstNameLayout = findViewById(R.id.layoutFirstName);
        firstNameEditText = findViewById(R.id.editTextFirstName);
        setEditTextListener(firstNameLayout, firstNameEditText);

        lastNameLayout = findViewById(R.id.layoutLastName);
        lastNameEditText = findViewById(R.id.editTextLastName);
        setEditTextListener(lastNameLayout, lastNameEditText);

        Button createAccountButton = findViewById(R.id.buttonCreateAccount);
        createAccountButton.setOnClickListener(this::signUp);
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
            User user = AuthenticationService.register(this, email, password, firstName, lastName);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(User.class.getSimpleName(), user);
            startActivity(intent);
            finish();
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
    protected int getMenu() {
        return R.menu.toolbar_basic_menu;
    }

    @Override
    protected void handleBackButtonAction() {
        NavUtils.navigateUpFromSameTask(this);
    }
}