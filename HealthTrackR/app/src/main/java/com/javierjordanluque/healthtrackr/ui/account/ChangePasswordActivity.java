package com.javierjordanluque.healthtrackr.ui.account;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.ui.BaseActivity;
import com.javierjordanluque.healthtrackr.util.AuthenticationService;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.util.Objects;

public class ChangePasswordActivity extends BaseActivity {
    private User user;
    private TextInputLayout layoutCurrentPassword;
    private TextInputLayout layoutNewPassword;
    private TextInputLayout layoutRepeatNewPassword;
    private EditText editTextCurrentPassword;
    private EditText editTextNewPassword;
    private EditText editTextRepeatNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        setUpToolbar(getString(R.string.account_app_bar_title_change_password));
        showBackButton(true);

        user = sessionViewModel.getUserSession();

        layoutCurrentPassword = findViewById(R.id.layoutCurrentPassword);
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        setEditTextListener(layoutCurrentPassword, editTextCurrentPassword);

        layoutNewPassword = findViewById(R.id.layoutNewPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        setEditTextListener(layoutNewPassword, editTextNewPassword);

        layoutRepeatNewPassword = findViewById(R.id.layoutRepeatNewPassword);
        editTextRepeatNewPassword = findViewById(R.id.editTextRepeatNewPassword);
        setEditTextListener(layoutRepeatNewPassword, editTextRepeatNewPassword);

        Button buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonChangePassword.setOnClickListener(this::changePassword);
    }

    private void changePassword(View view) {
        hideKeyboard(this);

        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String repeatNewPassword = editTextRepeatNewPassword.getText().toString().trim();

        boolean validCurrentPassword = isValidPassword(currentPassword);
        boolean validNewPassword = isValidPassword(newPassword);
        boolean validRepeatNewPassword = isValidRepeatPassword(newPassword, repeatNewPassword);

        if (!validCurrentPassword || !validNewPassword || !validRepeatNewPassword) {
            if (!validCurrentPassword)
                layoutCurrentPassword.setError(getString(R.string.error_invalid_password));
            if (!validNewPassword)
                layoutNewPassword.setError(getString(R.string.error_invalid_password));
            if (!validRepeatNewPassword)
                layoutRepeatNewPassword.setError(getString(R.string.error_invalid_repeat_password));

            return;
        }

        showChangePasswordConfirmationDialog(currentPassword, newPassword);
    }

    private void showChangePasswordConfirmationDialog(String currentPassword, String newPassword) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.account_dialog_message_change_password))
                .setPositiveButton(getString(R.string.dialog_positive_change), (dialog, id) -> {
                    try {
                        user.changePassword(this, currentPassword, newPassword);
                        AuthenticationService.clearCredentials(this);

                        Toast.makeText(this, getString(R.string.account_toast_confirmation_change_password), Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (Exception exception) {
                        dialog.dismiss();
                        if (Objects.equals(exception.getMessage(), getString(R.string.authentication_helper_password))) {
                            layoutNewPassword.setError(exception.getMessage());
                        } else if (Objects.equals(exception.getMessage(), getString(R.string.error_incorrect_password))) {
                            layoutCurrentPassword.setError(exception.getMessage());
                        } else {
                            ExceptionManager.advertiseUI(this, exception.getMessage());
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private boolean isValidPassword(String password) {
        return !password.isEmpty() && password.length() >= 8 && password.length() <= 60;
    }

    private boolean isValidRepeatPassword(String password, String repeatPassword) {
        return password.equals(repeatPassword);
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }
}