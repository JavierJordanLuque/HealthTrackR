package com.javierjordanluque.healthtrackr.ui.treatments.calendar.appointments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.MedicalAppointment;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.ui.BaseActivity;
import com.javierjordanluque.healthtrackr.util.PermissionManager;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;
import com.javierjordanluque.healthtrackr.util.notifications.MedicalAppointmentNotification;
import com.javierjordanluque.healthtrackr.util.notifications.NotificationScheduler;

import java.util.concurrent.TimeUnit;

public class ModifyMedicalAppointmentNotificationActivity extends BaseActivity {
    private MedicalAppointment medicalAppointment;
    private ConstraintLayout constraintLayoutPreviousNotificationTime;
    private EditText editTextPreviousNotificationTimeHours;
    private EditText editTextPreviousNotificationTimeMinutes;
    private SwitchMaterial switchPreviousNotificationStatus;
    private int previousNotificationTimeHours;
    private int previousNotificationTimeMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_medical_appointment_notification);
        setUpToolbar(getString(R.string.medical_appointment_app_bar_title_modify_notification));
        showBackButton(true);

        Treatment treatment = getTreatmentFromIntent(getIntent());
        medicalAppointment = getMedicalAppointmentFromIntent(treatment, getIntent());

        TextView textViewDateTime = findViewById(R.id.textViewDateTime);
        textViewDateTime.setText(showFormattedDateTime(medicalAppointment.getDateTime()));

        constraintLayoutPreviousNotificationTime = findViewById(R.id.constraintLayoutPreviousNotificationTime);

        editTextPreviousNotificationTimeHours = findViewById(R.id.editTextPreviousNotificationTimeHours);
        editTextPreviousNotificationTimeHours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        editTextPreviousNotificationTimeMinutes = findViewById(R.id.editTextPreviousNotificationTimeMinutes);
        editTextPreviousNotificationTimeMinutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    char firstChar = s.charAt(0);
                    if (firstChar < '0' || firstChar > '5') {
                        s.clear();
                    }
                }
            }
        });

        switchPreviousNotificationStatus = findViewById(R.id.switchPreviousNotificationStatus);

        MedicalAppointmentNotification previousNotification = null;
        try {
            previousNotification = medicalAppointment.getNotification(this);
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }

        if (previousNotification != null) {
            switchPreviousNotificationStatus.setChecked(true);
            setPreviousNotificationTime(previousNotification);
        }

        MedicalAppointmentNotification finalPreviousNotification = previousNotification;
        switchPreviousNotificationStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                constraintLayoutPreviousNotificationTime.setVisibility(View.GONE);
            } else {
                setPreviousNotificationTime(finalPreviousNotification);
            }
        });

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(this::modifyMedicalAppointmentNotification);
    }

    private void modifyMedicalAppointmentNotification(View view) {
        hideKeyboard(this);

        boolean previousNotificationStatus = switchPreviousNotificationStatus.isChecked();

        if (previousNotificationStatus) {
            String previousNotificationTimeHoursString = editTextPreviousNotificationTimeHours.getText().toString().trim();
            int previousNotificationTimeHours = 0;
            if (!previousNotificationTimeHoursString.isEmpty())
                previousNotificationTimeHours = Integer.parseInt(previousNotificationTimeHoursString);

            String previousNotificationTimeMinutesString = editTextPreviousNotificationTimeMinutes.getText().toString().trim();
            int previousNotificationTimeMinutes = 0;
            if (!previousNotificationTimeMinutesString.isEmpty())
                previousNotificationTimeMinutes = Integer.parseInt(previousNotificationTimeMinutesString);

            showModifyMedicalAppointmentNotificationConfirmationDialog(previousNotificationTimeHours, previousNotificationTimeMinutes, true);
        } else {
            showModifyMedicalAppointmentNotificationConfirmationDialog(Integer.MIN_VALUE, Integer.MIN_VALUE, false);
        }
    }

    private void showModifyMedicalAppointmentNotificationConfirmationDialog(int previousNotificationTimeHours, int previousNotificationTimeMinutes,
                                                                            boolean previousNotificationStatus) {
        this.previousNotificationTimeHours = previousNotificationTimeHours;
        this.previousNotificationTimeMinutes = previousNotificationTimeMinutes;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_message_save))
                .setPositiveButton(getString(R.string.button_save), (dialog, id) -> {
                    if (previousNotificationStatus) {
                        if (PermissionManager.hasNotificationPermission(this)) {
                            makeMedicalAppointmentNotificationModification(true);
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PermissionManager.REQUEST_CODE_PERMISSION_POST_NOTIFICATIONS);
                            } else {
                                showNotificationPermissionDeniedDialog();
                            }
                        }
                    } else {
                        makeMedicalAppointmentNotificationModification(false);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionManager.REQUEST_CODE_PERMISSION_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeMedicalAppointmentNotificationModification(true);
            } else {
                showNotificationPermissionDeniedDialog();
            }
        }
    }

    private void showNotificationPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.medical_appointment_dialog_denied_notification_permission))
                .setPositiveButton(getString(R.string.snackbar_action_more), (dialog, id) -> {
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    notificationPermissionLauncher.launch(intent);
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> {
                    makeMedicalAppointmentNotificationModification(false);

                    dialog.dismiss();
                    finish();
                });
        builder.show();
    }

    private final ActivityResultLauncher<Intent> notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> makeMedicalAppointmentNotificationModification(PermissionManager.hasNotificationPermission(this)));

    private void makeMedicalAppointmentNotificationModification(boolean previousNotificationStatus) {
        try {
            medicalAppointment.modifyMedicalAppointmentNotification(this, previousNotificationTimeHours, previousNotificationTimeMinutes,
                    previousNotificationStatus);

            Toast.makeText(this, getString(R.string.toast_confirmation_save), Toast.LENGTH_SHORT).show();
            finish();
        } catch (DBInsertException | DBDeleteException | DBFindException exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }
    }

    private void setPreviousNotificationTime(MedicalAppointmentNotification previousNotification) {
        long previousTotalMinutes;

        if (previousNotification != null) {
            previousTotalMinutes = TimeUnit.MILLISECONDS.toMinutes(medicalAppointment.getDateTime().toInstant().toEpochMilli() -
                    previousNotification.getTimestamp());
        } else {
            previousTotalMinutes = TimeUnit.MILLISECONDS.toMinutes(NotificationScheduler.PREVIOUS_DEFAULT_MINUTES);
        }
        constraintLayoutPreviousNotificationTime.setVisibility(View.VISIBLE);

        long hours = previousTotalMinutes / 60;
        long minutes = previousTotalMinutes % 60;

        editTextPreviousNotificationTimeHours.setText(String.valueOf(hours));
        editTextPreviousNotificationTimeMinutes.setText(String.valueOf(minutes));
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }
}
