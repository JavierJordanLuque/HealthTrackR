package com.javierjordanluque.healthtrackr.ui.treatments.medicines;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Medicine;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.enumerations.AdministrationRoute;
import com.javierjordanluque.healthtrackr.ui.BaseActivity;
import com.javierjordanluque.healthtrackr.ui.MainActivity;
import com.javierjordanluque.healthtrackr.util.PermissionManager;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;
import com.javierjordanluque.healthtrackr.util.notifications.NotificationScheduler;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public class AddMedicineActivity extends BaseActivity {
    private Treatment treatment;
    private Medicine medicine;
    private TextInputLayout layoutName;
    private TextInputLayout layoutInitialDosingTime;
    private EditText editTextName;
    private EditText editTextActiveSubstance;
    private EditText editTextDose;
    private Spinner spinnerAdministrationRoute;
    private EditText editTextInitialDosingTime;
    private EditText editTextDosageFrequencyHours;
    private EditText editTextDosageFrequencyMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);
        setUpToolbar(getString(R.string.medicines_app_bar_title_add));
        showBackButton(true);

        treatment = getTreatmentFromIntent(getIntent());

        layoutName = findViewById(R.id.layoutName);
        editTextName = findViewById(R.id.editTextName);
        setEditTextListener(layoutName, editTextName);

        editTextActiveSubstance = findViewById(R.id.editTextActiveSubstance);
        editTextDose = findViewById(R.id.editTextDose);

        configureAdministrationRouteSpinner();

        layoutInitialDosingTime = findViewById(R.id.layoutInitialDosingTime);
        editTextInitialDosingTime = findViewById(R.id.editTextInitialDosingTime);
        editTextInitialDosingTime.setOnClickListener(view -> showDateTimePicker(editTextInitialDosingTime, null, treatment.getStartDate(), treatment.getEndDate()));
        setEditTextListener(layoutInitialDosingTime, editTextInitialDosingTime);

        editTextDosageFrequencyHours = findViewById(R.id.editTextDosageFrequencyHours);
        editTextDosageFrequencyHours.setText(String.valueOf(0));
        editTextDosageFrequencyMinutes = findViewById(R.id.editTextDosageFrequencyMinutes);
        editTextDosageFrequencyMinutes.setText(String.valueOf(0));
        editTextDosageFrequencyMinutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

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

        Button buttonAddMedicine = findViewById(R.id.buttonAddMedicine);
        buttonAddMedicine.setOnClickListener(this::addMedicine);
    }

    private void addMedicine(View view) {
        hideKeyboard(this);

        String name = editTextName.getText().toString().trim();

        boolean validName = isValidName(name);
        boolean validInitialDosingTime = isValidInitialDosingTime(editTextInitialDosingTime.getText().toString().trim());

        if (!validName || !validInitialDosingTime) {
            if (!validName)
                layoutName.setError(getString(R.string.error_invalid_medicine_name));
            if (!validInitialDosingTime)
                layoutInitialDosingTime.setError(getString(R.string.error_invalid_medicine_initial_dosing_time));

            return;
        }

        ZonedDateTime initialDosingTime = getDateTimeFromEditText(editTextInitialDosingTime);

        String activeSubstance = editTextActiveSubstance.getText().toString().trim();
        if (activeSubstance.isEmpty())
            activeSubstance = null;

        String doseString = editTextDose.getText().toString().trim();
        Integer dose = null;
        if (!doseString.isEmpty())
            dose = Integer.parseInt(doseString);

        AdministrationRoute administrationRoute = getAdministrationRouteFromSpinner();

        String dosageFrequencyHoursString = editTextDosageFrequencyHours.getText().toString().trim();
        int dosageFrequencyHours = 0;
        if (!dosageFrequencyHoursString.isEmpty())
            dosageFrequencyHours = Integer.parseInt(editTextDosageFrequencyHours.getText().toString().trim());

        String dosageFrequencyMinutesString = editTextDosageFrequencyMinutes.getText().toString().trim();
        int dosageFrequencyMinutes = 0;
        if (!dosageFrequencyMinutesString.isEmpty())
            dosageFrequencyMinutes = Integer.parseInt(editTextDosageFrequencyMinutes.getText().toString().trim());

        if ((dosageFrequencyHours != 0 || dosageFrequencyMinutes != 0) && TimeUnit.HOURS.toMinutes(dosageFrequencyHours) + dosageFrequencyMinutes <= NotificationScheduler.PREVIOUS_DEFAULT_MINUTES) {
            showAddMedicineNoPreviousNotificationConfirmationDialog(treatment, name, activeSubstance, dose, administrationRoute, initialDosingTime, dosageFrequencyHours, dosageFrequencyMinutes);
        } else {
            try {
                medicine = new Medicine(this, treatment, name, activeSubstance, dose, administrationRoute, initialDosingTime, dosageFrequencyHours, dosageFrequencyMinutes);

                if (PermissionManager.hasNotificationPermission(this)) {
                    medicine.schedulePreviousMedicationNotification(this, NotificationScheduler.PREVIOUS_DEFAULT_MINUTES);
                    medicine.scheduleMedicationNotification(this);

                    openNextActivity();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PermissionManager.REQUEST_CODE_PERMISSION_POST_NOTIFICATIONS);
                    } else {
                        showNotificationPermissionDeniedDialog();
                    }
                }
            } catch (DBInsertException | DBDeleteException exception) {
                ExceptionManager.advertiseUI(this, exception.getMessage());
            }
        }
    }

    private void showAddMedicineNoPreviousNotificationConfirmationDialog(Treatment treatment, String name, String activeSubstance, Integer dose, AdministrationRoute administrationRoute,
                                                              ZonedDateTime initialDosingTime, int dosageFrequencyHours, int dosageFrequencyMinutes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.medicines_dialog_message_no_previous_notification_add))
                .setPositiveButton(getString(R.string.dialog_positive_add), (dialog, id) -> {
                    try {
                        medicine = new Medicine(this, treatment, name, activeSubstance, dose, administrationRoute, initialDosingTime, dosageFrequencyHours, dosageFrequencyMinutes);

                        if (PermissionManager.hasNotificationPermission(this)) {
                            scheduleNotifications();
                            openNextActivity();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PermissionManager.REQUEST_CODE_PERMISSION_POST_NOTIFICATIONS);
                            } else {
                                showNotificationPermissionDeniedDialog();
                            }
                        }
                    } catch (DBInsertException exception) {
                        ExceptionManager.advertiseUI(this, exception.getMessage());
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionManager.REQUEST_CODE_PERMISSION_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleNotifications();
                openNextActivity();
            } else {
                showNotificationPermissionDeniedDialog();
            }
        }
    }

    private final ActivityResultLauncher<Intent> notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
                    scheduleNotifications();
                openNextActivity();
            });

    private void showNotificationPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.medicines_dialog_notification_permission))
                .setPositiveButton(getString(R.string.snackbar_action_more), (dialog, id) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    notificationPermissionLauncher.launch(intent);
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> openNextActivity());
        builder.create().show();
    }

    private void scheduleNotifications() {
        try {
            medicine.schedulePreviousMedicationNotification(this, NotificationScheduler.PREVIOUS_DEFAULT_MINUTES);
            medicine.scheduleMedicationNotification(this);
        } catch (DBInsertException | DBDeleteException exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }
    }

    private void openNextActivity() {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.class.getSimpleName(), MedicineFragment.class.getName());

        Bundle bundle = new Bundle();
        bundle.putLong(Treatment.class.getSimpleName(), treatment.getId());
        bundle.putLong(Medicine.class.getSimpleName(), medicine.getId());
        intent.putExtras(bundle);

        setResult(RESULT_OK, intent);

        finish();
    }

    private AdministrationRoute getAdministrationRouteFromSpinner() {
        String[] administrationRouteOptions = getResources().getStringArray(R.array.medicines_array_administration_route);
        String selectedAdministrationRoute = spinnerAdministrationRoute.getSelectedItem().toString();

        if (selectedAdministrationRoute.equals(administrationRouteOptions[0])) {
            return AdministrationRoute.ORAL;
        } else if (selectedAdministrationRoute.equals(administrationRouteOptions[1])) {
            return AdministrationRoute.TOPICAL;
        } else if (selectedAdministrationRoute.equals(administrationRouteOptions[2])) {
            return AdministrationRoute.PARENTERAL;
        } else if (selectedAdministrationRoute.equals(administrationRouteOptions[3])) {
            return AdministrationRoute.INHALATION;
        } else if (selectedAdministrationRoute.equals(administrationRouteOptions[4])) {
            return AdministrationRoute.OPHTHALMIC;
        } else if (selectedAdministrationRoute.equals(administrationRouteOptions[5])) {
            return AdministrationRoute.OTIC;
        } else if (selectedAdministrationRoute.equals(administrationRouteOptions[6])) {
            return AdministrationRoute.NASAL;
        } else if (selectedAdministrationRoute.equals(administrationRouteOptions[7])) {
            return AdministrationRoute.RECTAL;
        } else {
            return AdministrationRoute.UNSPECIFIED;
        }
    }

    private void configureAdministrationRouteSpinner() {
        spinnerAdministrationRoute = findViewById(R.id.spinnerAdministrationRoute);

        String[] administrationRouteOptions = getResources().getStringArray(R.array.medicines_array_administration_route);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, administrationRouteOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdministrationRoute.setAdapter(adapter);
        spinnerAdministrationRoute.setSelection(adapter.getCount() - 1);
    }

    private boolean isValidName(String title) {
        return !title.isEmpty() && title.length() <= 50;
    }

    private boolean isValidInitialDosingTime(String initialDosingTime) {
        return !initialDosingTime.isEmpty();
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }
}