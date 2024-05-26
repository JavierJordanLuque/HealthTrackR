package com.javierjordanluque.healthtrackr.ui.treatments.medicines;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Medicine;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.ui.BaseActivity;
import com.javierjordanluque.healthtrackr.util.PermissionManager;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;
import com.javierjordanluque.healthtrackr.util.notifications.MedicationNotification;
import com.javierjordanluque.healthtrackr.util.notifications.NotificationScheduler;

import java.util.concurrent.TimeUnit;

public class ModifyMedicineNotificationsActivity extends BaseActivity {
    private Medicine medicine;
    private ConstraintLayout constraintLayoutPreviousNotificationTime;
    private EditText editTextPreviousNotificationTimeHours;
    private EditText editTextPreviousNotificationTimeMinutes;
    private SwitchMaterial switchPreviousNotificationStatus;
    private SwitchMaterial switchDosingNotificationStatus;
    private TextView textViewPreviousNotificationTimeHelper;
    private TextView textViewPreviousNotificationTimeError;
    private ImageView imageViewPreviousNotificationTimeError;
    private ConstraintLayout layoutPreviousNotificationTime;
    private int previousNotificationTimeHours;
    private int previousNotificationTimeMinutes;
    private boolean previousNotificationStatus;
    private boolean dosingNotificationStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_medicine_notifications);
        setUpToolbar(getString(R.string.medicines_app_bar_title_modify_notifications));
        showBackButton(true);

        Treatment treatment = getTreatmentFromIntent(getIntent());
        medicine = getMedicineFromIntent(treatment, getIntent());

        TextView textViewName = findViewById(R.id.textViewName);
        textViewName.setText(medicine.getName());

        constraintLayoutPreviousNotificationTime = findViewById(R.id.constraintLayoutPreviousNotificationTime);
        textViewPreviousNotificationTimeHelper = findViewById(R.id.textViewPreviousNotificationTimeHelper);
        textViewPreviousNotificationTimeError = findViewById(R.id.textViewPreviousNotificationTimeError);
        imageViewPreviousNotificationTimeError = findViewById(R.id.imageViewPreviousNotificationTimeError);
        layoutPreviousNotificationTime = findViewById(R.id.layoutPreviousNotificationTime);

        editTextPreviousNotificationTimeHours = findViewById(R.id.editTextPreviousNotificationTimeHours);
        editTextPreviousNotificationTimeHours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textViewPreviousNotificationTimeError.setVisibility(View.GONE);
                imageViewPreviousNotificationTimeError.setVisibility(View.INVISIBLE);
                layoutPreviousNotificationTime.setBackgroundResource(R.drawable.form_layout_container_filled);
                textViewPreviousNotificationTimeHelper.setVisibility(View.VISIBLE);
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
                textViewPreviousNotificationTimeError.setVisibility(View.GONE);
                imageViewPreviousNotificationTimeError.setVisibility(View.INVISIBLE);
                layoutPreviousNotificationTime.setBackgroundResource(R.drawable.form_layout_container_filled);
                textViewPreviousNotificationTimeHelper.setVisibility(View.VISIBLE);
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
        switchDosingNotificationStatus = findViewById(R.id.switchDosingNotificationStatus);

        MedicationNotification previousNotification = null;
        try {
            for (MedicationNotification notification : medicine.getNotifications(this)) {
                if (notification.getTimestamp() != medicine.getInitialDosingTime().toInstant().toEpochMilli()) {
                    switchPreviousNotificationStatus.setChecked(true);

                    previousNotification = notification;
                    setPreviousNotificationTime(notification);
                } else {
                    switchDosingNotificationStatus.setChecked(true);
                }
            }
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }

        MedicationNotification finalPreviousNotification = previousNotification;
        switchPreviousNotificationStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                constraintLayoutPreviousNotificationTime.setVisibility(View.GONE);
            } else {
                setPreviousNotificationTime(finalPreviousNotification);
            }
        });

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(this::modifyMedicineNotifications);
    }

    private void modifyMedicineNotifications(View view) {
        hideKeyboard(this);

        boolean previousNotificationStatus = switchPreviousNotificationStatus.isChecked();
        boolean dosingNotificationStatus = switchDosingNotificationStatus.isChecked();

        if (previousNotificationStatus) {
            String previousNotificationTimeHoursString = editTextPreviousNotificationTimeHours.getText().toString().trim();
            int previousNotificationTimeHours = 0;
            if (!previousNotificationTimeHoursString.isEmpty())
                previousNotificationTimeHours = Integer.parseInt(previousNotificationTimeHoursString);

            String previousNotificationTimeMinutesString = editTextPreviousNotificationTimeMinutes.getText().toString().trim();
            int previousNotificationTimeMinutes = 0;
            if (!previousNotificationTimeMinutesString.isEmpty())
                previousNotificationTimeMinutes = Integer.parseInt(previousNotificationTimeMinutesString);

            if (!isValidPreviousNotificationTime(previousNotificationTimeHours, previousNotificationTimeMinutes)) {
                textViewPreviousNotificationTimeHelper.setVisibility(View.GONE);
                textViewPreviousNotificationTimeError.setVisibility(View.VISIBLE);
                imageViewPreviousNotificationTimeError.setVisibility(View.VISIBLE);
                layoutPreviousNotificationTime.setBackgroundResource(R.drawable.form_layout_container_filled_error);

                return;
            }

            showModifyMedicineNotificationsConfirmationDialog(previousNotificationTimeHours, previousNotificationTimeMinutes, true, dosingNotificationStatus);
        } else {
            showModifyMedicineNotificationsConfirmationDialog(Integer.MIN_VALUE, Integer.MIN_VALUE, false, dosingNotificationStatus);
        }
    }

    private void showModifyMedicineNotificationsConfirmationDialog(int previousNotificationTimeHours, int previousNotificationTimeMinutes,
                                                                   boolean previousNotificationStatus, boolean dosingNotificationStatus) {
        this.previousNotificationTimeHours = previousNotificationTimeHours;
        this.previousNotificationTimeMinutes = previousNotificationTimeMinutes;
        this.previousNotificationStatus = previousNotificationStatus;
        this.dosingNotificationStatus = dosingNotificationStatus;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_message_save))
                .setPositiveButton(getString(R.string.button_save), (dialog, id) -> {
                    if (previousNotificationStatus || dosingNotificationStatus) {
                        if (PermissionManager.hasNotificationPermission(this)) {
                            makeMedicineNotificationsModification(previousNotificationStatus, dosingNotificationStatus);
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PermissionManager.REQUEST_CODE_PERMISSION_POST_NOTIFICATIONS);
                            } else {
                                showNotificationPermissionDeniedDialog();
                            }
                        }
                    } else {
                        makeMedicineNotificationsModification(false, false);
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
                makeMedicineNotificationsModification(previousNotificationStatus, dosingNotificationStatus);
            } else {
                showNotificationPermissionDeniedDialog();
            }
        }
    }

    private void showNotificationPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.medicines_dialog_denied_notification_permission))
                .setPositiveButton(getString(R.string.snackbar_action_more), (dialog, id) -> {
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getResources().getConfiguration().getLocales().get(0).getLanguage());
                    notificationPermissionLauncher.launch(intent);
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> {
                    makeMedicineNotificationsModification(false, false);

                    dialog.dismiss();
                    finish();
                });
        builder.show();
    }

    private final ActivityResultLauncher<Intent> notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (PermissionManager.hasNotificationPermission(this)) {
                    makeMedicineNotificationsModification(previousNotificationStatus, dosingNotificationStatus);
                } else {
                    makeMedicineNotificationsModification(false, false);
                }
            });
    
    private void makeMedicineNotificationsModification(boolean previousNotificationStatus, boolean dosingNotificationStatus) {
        try {
            medicine.modifyMedicineNotifications(this, previousNotificationTimeHours, previousNotificationTimeMinutes,
                    previousNotificationStatus, dosingNotificationStatus);

            Toast.makeText(this, getString(R.string.toast_confirmation_save), Toast.LENGTH_SHORT).show();
            finish();
        } catch (DBInsertException | DBDeleteException | DBFindException exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }
    }

    private void setPreviousNotificationTime(MedicationNotification previousNotification) {
        long previousTotalMinutes;

        if (previousNotification != null) {
            previousTotalMinutes = TimeUnit.MILLISECONDS.toMinutes(medicine.getInitialDosingTime().toInstant().toEpochMilli() -
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

    private boolean isValidPreviousNotificationTime(int previousNotificationTimeHours, int previousNotificationTimeMinutes) {
        int dosageFrequencyHours = medicine.getDosageFrequencyHours();
        int dosageFrequencyMinutes = medicine.getDosageFrequencyMinutes();

        if (dosageFrequencyHours == 0 && dosageFrequencyMinutes == 0)
            return true;

        int totalPreviousNotificationTimeMinutes = (int) (TimeUnit.HOURS.toMinutes(previousNotificationTimeHours) + previousNotificationTimeMinutes);
        int totalDosageFrequencyMinutes = (int) (TimeUnit.HOURS.toMinutes(dosageFrequencyHours) + dosageFrequencyMinutes);

        return totalPreviousNotificationTimeMinutes < totalDosageFrequencyMinutes;
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }
}
