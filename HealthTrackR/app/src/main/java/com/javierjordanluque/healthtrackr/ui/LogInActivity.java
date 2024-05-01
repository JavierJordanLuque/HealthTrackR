package com.javierjordanluque.healthtrackr.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.util.AuthenticationService;
import com.javierjordanluque.healthtrackr.util.exceptions.AuthenticationException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.util.Objects;

public class LogInActivity extends BaseActivity {
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private CheckBox checkBoxRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        setUpToolbar(getString(R.string.authentication_app_bar_title_log_in));
        showBackButton(true);

        layoutEmail = findViewById(R.id.layoutEmail);
        editTextEmail = findViewById(R.id.editTextEmail);
        setEditTextListener(layoutEmail, editTextEmail);

        layoutPassword = findViewById(R.id.layoutPassword);
        editTextPassword = findViewById(R.id.editTextPassword);
        setEditTextListener(layoutPassword, editTextPassword);

        checkBoxRemember = findViewById(R.id.checkBoxRemember);

        Button buttonLogIn = findViewById(R.id.buttonLogIn);
        buttonLogIn.setOnClickListener(this::logIn);
    }

    private void logIn(View view) {
        hideKeyboard(this);

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        boolean validEmail = isValidEmail(email);
        boolean validPassword = isValidPassword(password);

        if (!validEmail || !validPassword) {
            if (!validEmail)
                layoutEmail.setError(getString(R.string.error_invalid_email));
            if (!validPassword)
                layoutPassword.setError(getString(R.string.error_invalid_password));

            return;
        }

        try {
            User user = AuthenticationService.login(this, email, password);

            if (checkBoxRemember.isChecked()) {
                AuthenticationService.saveCredentials(this, email, password);
            } else {
                AuthenticationService.clearCredentials(this);
            }

            Intent intent = new Intent(this, MainActivity.class);
            sessionViewModel.setUserSession(user);
            startActivity(intent);
            finish();
        } catch (AuthenticationException exception) {
            if (Objects.equals(exception.getMessage(), getString(R.string.error_incorrect_email))) {
                showIncorrectEmailDialog();
                layoutEmail.setError(exception.getMessage());
            } else if (Objects.equals(exception.getMessage(), getString(R.string.error_incorrect_password))) {
                layoutPassword.setError(exception.getMessage());
            } else {
                ExceptionManager.advertiseUI(this, exception.getMessage());
            }
        }
    }

    private void showIncorrectEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.error_dialog_message_incorrect_email))
                .setPositiveButton(getString(R.string.authentication_create_account), (dialog, id) -> {
                    Intent intent = new Intent(this, SignUpActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton(getString(R.string.dialog_negative_try_again), (dialog, id) -> dialog.dismiss());
        builder.show();
    }

    private boolean isValidEmail(String email) {
        return !email.isEmpty() && email.length() >= 5 && email.length() <= 50;
    }

    private boolean isValidPassword(String password) {
        return !password.isEmpty() && password.length() >= 8 && password.length() <= 60;
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_basic_menu;
    }
}