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
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutRepeatPassword;
    private TextInputLayout layoutFirstName;
    private TextInputLayout layoutLastName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextRepeatPassword;
    private EditText editTextFirstName;
    private EditText editTextLastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_sign_up);
        setUpToolbar(getString(R.string.authentication_app_bar_title_sign_up));
        showBackButton(true);

        layoutEmail = findViewById(R.id.layoutEmail);
        editTextEmail = findViewById(R.id.editTextEmail);
        setEditTextListener(layoutEmail, editTextEmail);

        layoutPassword = findViewById(R.id.layoutPassword);
        editTextPassword = findViewById(R.id.editTextPassword);
        setEditTextListener(layoutPassword, editTextPassword);

        layoutRepeatPassword = findViewById(R.id.layoutRepeatPassword);
        editTextRepeatPassword = findViewById(R.id.editTextRepeatPassword);
        setEditTextListener(layoutRepeatPassword, editTextRepeatPassword);

        layoutFirstName = findViewById(R.id.layoutFirstName);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        setEditTextListener(layoutFirstName, editTextFirstName);

        layoutLastName = findViewById(R.id.layoutLastName);
        editTextLastName = findViewById(R.id.editTextLastName);
        setEditTextListener(layoutLastName, editTextLastName);

        Button buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        buttonCreateAccount.setOnClickListener(this::signUp);
    }

    private void signUp(View view) {
        hideKeyboard(this);

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String repeatPassword = editTextRepeatPassword.getText().toString().trim();
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();

        boolean validEmail = isValidEmail(email);
        boolean validPassword = isValidPassword(password);
        boolean validRepeatPassword = isValidRepeatPassword(password, repeatPassword);
        boolean validFirstName = isValidFirstName(firstName);
        boolean validLastName = isValidLastName(lastName);

        if (!validEmail || !validPassword || !validRepeatPassword || !validFirstName || !validLastName) {
            if (!validEmail)
                layoutEmail.setError(getString(R.string.error_invalid_email));
            if (!validPassword)
                layoutPassword.setError(getString(R.string.error_invalid_password));
            if (!validRepeatPassword)
                layoutRepeatPassword.setError(getString(R.string.error_invalid_repeat_password));
            if (!validFirstName)
                layoutFirstName.setError(getString(R.string.error_invalid_first_name));
            if (!validLastName)
                layoutLastName.setError(getString(R.string.error_invalid_last_name));

            return;
        }

        try {
            User user = AuthenticationService.register(this, email, password, firstName, lastName);

            Intent intent = new Intent(this, MainActivity.class);
            sessionViewModel.setUserSession(user);
            startActivity(intent);
            finish();
        } catch (AuthenticationException exception) {
            if (Objects.equals(exception.getMessage(), getString(R.string.error_existing_email)) ||
                    Objects.equals(exception.getMessage(), getString(R.string.error_invalid_email_requirements))) {
                layoutEmail.setError(exception.getMessage());
            } else if (Objects.equals(exception.getMessage(), getString(R.string.authentication_helper_password))) {
                layoutPassword.setError(exception.getMessage());
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
        return !firstName.isEmpty();
    }

    private boolean isValidLastName(String lastName) {
        return !lastName.isEmpty();
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_basic_menu;
    }
}