package com.javierjordanluque.healthtrackr.ui.treatments.medicines;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Medicine;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.enumerations.AdministrationRoute;
import com.javierjordanluque.healthtrackr.ui.BaseActivity;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;
import com.javierjordanluque.healthtrackr.util.notifications.NotificationScheduler;

import org.w3c.dom.Text;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ModifyMedicineActivity extends BaseActivity {
    private Treatment treatment;
    private Medicine medicine;
    private TextInputLayout layoutInitialDosingTime;
    private EditText editTextDose;
    private Spinner spinnerAdministrationRoute;
    private EditText editTextInitialDosingTime;
    private EditText editTextDosageFrequencyHours;
    private EditText editTextDosageFrequencyMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_medicine);
        setUpToolbar(getString(R.string.medicines_app_bar_title_modify));
        showBackButton(true);

        treatment = getTreatmentFromIntent(getIntent());
        medicine = getMedicineFromIntent(treatment, getIntent());

        TextView textViewName = findViewById(R.id.textViewName);
        textViewName.setText(medicine.getName());

        editTextDose = findViewById(R.id.editTextDose);
        Integer dose = medicine.getDose();
        if (dose != null)
            editTextDose.setText(String.valueOf(dose));

        configureAdministrationRouteSpinner();

        layoutInitialDosingTime = findViewById(R.id.layoutInitialDosingTime);
        editTextInitialDosingTime = findViewById(R.id.editTextInitialDosingTime);
        editTextInitialDosingTime.setOnClickListener(view -> showDateTimePicker(editTextInitialDosingTime));
        setEditTextListener(layoutInitialDosingTime, editTextInitialDosingTime);
        editTextInitialDosingTime.setText(showFormattedDateTime(medicine.getInitialDosingTime()));

        editTextDosageFrequencyHours = findViewById(R.id.editTextDosageFrequencyHours);
        editTextDosageFrequencyHours.setText(String.valueOf(medicine.getDosageFrequencyHours()));
        editTextDosageFrequencyMinutes = findViewById(R.id.editTextDosageFrequencyMinutes);
        editTextDosageFrequencyMinutes.setText(String.valueOf(medicine.getDosageFrequencyMinutes()));
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

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(this::modifyMedicine);
    }

    private void modifyMedicine(View view) {
        hideKeyboard(this);

        ZonedDateTime initialDosingTime = getDateTimeFromEditText(editTextInitialDosingTime);
        boolean validInitialDosingTime = isValidInitialDosingTime(initialDosingTime);

        if (!validInitialDosingTime) {
            layoutInitialDosingTime.setError(getString(R.string.error_invalid_medicine_initial_dosing_time));

            return;
        }

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
            showModifyMedicineNoPreviousNotificationConfirmationDialog(dose, administrationRoute, initialDosingTime, dosageFrequencyHours, dosageFrequencyMinutes);
        } else {
            showModifyMedicineConfirmationDialog(dose, administrationRoute, initialDosingTime, dosageFrequencyHours, dosageFrequencyMinutes);
        }
    }

    private void showModifyMedicineNoPreviousNotificationConfirmationDialog(Integer dose, AdministrationRoute administrationRout, ZonedDateTime initialDosingTime,
                                                              int dosageFrequencyHours, int dosageFrequencyMinutes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.medicines_dialog_message_no_previous_notification_modify))
                .setPositiveButton(getString(R.string.dialog_positive_save), (dialog, id) -> {
                    try {
                        medicine.modifyMedicine(this, dose, administrationRout, initialDosingTime, dosageFrequencyHours, dosageFrequencyMinutes);

                        Toast.makeText(this, getString(R.string.toast_confirmation_save), Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (DBFindException | DBDeleteException | DBUpdateException | DBInsertException exception) {
                        ExceptionManager.advertiseUI(this, exception.getMessage());
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private void showModifyMedicineConfirmationDialog(Integer dose, AdministrationRoute administrationRout, ZonedDateTime initialDosingTime,
                                                      int dosageFrequencyHours, int dosageFrequencyMinutes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_message_save))
                .setPositiveButton(getString(R.string.dialog_positive_save), (dialog, id) -> {
                    try {
                        medicine.modifyMedicine(this, dose, administrationRout, initialDosingTime, dosageFrequencyHours, dosageFrequencyMinutes);

                        Toast.makeText(this, getString(R.string.toast_confirmation_save), Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (DBFindException | DBDeleteException | DBUpdateException | DBInsertException exception) {
                        ExceptionManager.advertiseUI(this, exception.getMessage());
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> dialog.dismiss());
        builder.create().show();
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

        int index = Arrays.asList(AdministrationRoute.values()).indexOf(medicine.getAdministrationRoute());
        spinnerAdministrationRoute.setSelection(index);
    }

    private boolean isValidInitialDosingTime(ZonedDateTime initialDosingTime) {
        return !initialDosingTime.isBefore(treatment.getStartDate()) && (treatment.getEndDate() == null || !initialDosingTime.isAfter(treatment.getEndDate()));
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }
}