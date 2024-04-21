package com.javierjordanluque.healthtrackr.ui.treatments;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.MedicalAppointment;
import com.javierjordanluque.healthtrackr.models.Medicine;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.enumerations.TreatmentCategory;
import com.javierjordanluque.healthtrackr.ui.BaseActivity;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.time.ZonedDateTime;
import java.util.Arrays;

public class ModifyTreatmentActivity extends BaseActivity {
    private Treatment treatment;
    private TextInputLayout layoutTitle;
    private TextInputLayout layoutStartDate;
    private TextInputLayout layoutEndDate;
    private TextInputLayout layoutDiagnosis;
    private EditText editTextTitle;
    private EditText editTextStartDate;
    private EditText editTextEndDate;
    private Spinner spinnerCategory;
    private EditText editTextDiagnosis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_treatment);
        setUpToolbar(getString(R.string.treatments_app_bar_title_modify));
        showBackButton(true);

        treatment = getTreatmentFromIntent(getIntent());

        layoutTitle = findViewById(R.id.layoutTitle);
        editTextTitle = findViewById(R.id.editTextTitle);
        setEditTextListener(layoutTitle, editTextTitle);
        editTextTitle.setText(treatment.getTitle());

        layoutStartDate = findViewById(R.id.layoutStartDate);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextStartDate.setOnClickListener(view -> showDatePickerDialog(editTextStartDate, getString(R.string.treatments_dialog_message_start_date), false));
        setEditTextListener(layoutStartDate, editTextStartDate);
        editTextStartDate.setText(showFormattedDate(treatment.getStartDate()));

        layoutEndDate = findViewById(R.id.layoutEndDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);
        ZonedDateTime endDate = treatment.getEndDate();
        if (endDate != null) {
            editTextEndDate.setFocusableInTouchMode(true);
            editTextEndDate.setText(showFormattedDate(endDate));
        }
        editTextEndDate.setOnClickListener(view -> showDatePickerDialog(editTextEndDate, getString(R.string.treatments_dialog_message_end_date), false));

        Activity activity = this;
        editTextEndDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutEndDate.setError(null);
                if (s.length() > 0) {
                    editTextEndDate.setFocusableInTouchMode(true);
                } else {
                    hideKeyboard(activity);
                    editTextEndDate.clearFocus();
                    editTextEndDate.setFocusableInTouchMode(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        configureCategorySpinner();

        layoutDiagnosis = findViewById(R.id.layoutDiagnosis);
        editTextDiagnosis = findViewById(R.id.editTextDiagnosis);
        setEditTextListener(layoutDiagnosis, editTextDiagnosis);
        String diagnosis = treatment.getDiagnosis();
        if (diagnosis != null)
            editTextDiagnosis.setText(diagnosis);

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(this::modifyTreatment);
    }

    private void modifyTreatment(View view) {
        hideKeyboard(this);

        String title = editTextTitle.getText().toString().trim();
        String startDateString = editTextStartDate.getText().toString().trim();
        String diagnosis = editTextDiagnosis.getText().toString().trim();

        boolean validTitle = isValidTitle(title);
        boolean validStartDate = isValidStartDate(startDateString);
        boolean validDiagnosis = isValidDiagnosis(diagnosis);

        if (!validTitle || !validStartDate || !validDiagnosis) {
            if (!validTitle)
                layoutTitle.setError(getString(R.string.error_invalid_treatment_title));
            if (!validStartDate)
                layoutStartDate.setError(getString(R.string.error_invalid_treatment_start_date));
            if (!validDiagnosis)
                layoutDiagnosis.setError(getString(R.string.error_invalid_treatment_diagnosis));

            return;
        }

        ZonedDateTime startDate = (ZonedDateTime) getDateFromEditText(editTextStartDate, ZonedDateTime.class);
        ZonedDateTime endDate = null;
        if (!editTextEndDate.getText().toString().isEmpty()) {
            endDate = (ZonedDateTime) getDateFromEditText(editTextEndDate, ZonedDateTime.class);
            if (!isValidEndDate(startDate, endDate)) {
                layoutEndDate.setError(getString(R.string.error_invalid_treatment_end_date));

                return;
            }
        }

        if (!isValidStartDateGivenDependencies(startDate)) {
            showInvalidStartDateDialog();
            layoutStartDate.setError(getString(R.string.error_invalid_treatment_start_date_given_dependencies));

            return;
        }
        if (!isValidEndDateGivenDependencies(endDate)) {
            showInvalidEndDateDialog();
            layoutEndDate.setError(getString(R.string.error_invalid_treatment_end_date_given_dependencies));

            return;
        }

        TreatmentCategory category = getCategoryFromSpinner();

        if (diagnosis.isEmpty())
            diagnosis = null;

        showModifyTreatmentConfirmationDialog(title, startDate, endDate, diagnosis, category);
    }

    private void showModifyTreatmentConfirmationDialog(String title, ZonedDateTime startDate, ZonedDateTime endDate, String diagnosis, TreatmentCategory category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_message_save))
                .setPositiveButton(getString(R.string.dialog_positive_save), (dialog, id) -> {
                    try {
                        treatment.modifyTreatment(this, title, startDate, endDate, diagnosis, category);
                        Toast.makeText(this, getString(R.string.toast_confirmation_save), Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (DBFindException | DBDeleteException | DBUpdateException | DBInsertException exception) {
                        ExceptionManager.advertiseUI(this, exception.getMessage());
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private void showInvalidStartDateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.treatments_dialog_message_invalid_start_date))
                .setPositiveButton(getString(R.string.dialog_positive_ok), (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private void showInvalidEndDateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.treatments_dialog_message_invalid_end_date))
                .setPositiveButton(getString(R.string.dialog_positive_ok), (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private TreatmentCategory getCategoryFromSpinner() {
        String[] categoryOptions = getResources().getStringArray(R.array.treatments_array_category);
        String selectedCategory = spinnerCategory.getSelectedItem().toString();

        if (selectedCategory.equals(categoryOptions[0])) {
            return TreatmentCategory.MEDICAL;
        } else if (selectedCategory.equals(categoryOptions[1])) {
            return TreatmentCategory.PHARMACOLOGICAL;
        } else if (selectedCategory.equals(categoryOptions[2])) {
            return TreatmentCategory.PHYSIOTHERAPY;
        } else if (selectedCategory.equals(categoryOptions[3])) {
            return TreatmentCategory.REHABILITATION;
        } else if (selectedCategory.equals(categoryOptions[4])) {
            return TreatmentCategory.PSYCHOLOGICAL;
        } else if (selectedCategory.equals(categoryOptions[5])) {
            return TreatmentCategory.PREVENTIVE;
        } else if (selectedCategory.equals(categoryOptions[6])) {
            return TreatmentCategory.CHRONIC;
        } else {
            return TreatmentCategory.ALTERNATIVE;
        }
    }

    private void configureCategorySpinner() {
        spinnerCategory = findViewById(R.id.spinnerCategory);

        String[] categoryOptions = getResources().getStringArray(R.array.treatments_array_category);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        int index = Arrays.asList(TreatmentCategory.values()).indexOf(treatment.getCategory());
        spinnerCategory.setSelection(index);
    }

    private boolean isValidTitle(String title) {
        return !title.isEmpty() && title.length() <= 50;
    }

    private boolean isValidStartDate(String startDate) {
        return !startDate.isEmpty();
    }

    private boolean isValidStartDateGivenDependencies(ZonedDateTime startDate) {
        try {
            for (Medicine medicine : treatment.getMedicines(this)) {
                if (medicine.getInitialDosingTime().isBefore(startDate))
                    return false;
            }

            for (MedicalAppointment appointment : treatment.getAppointments(this)) {
                if (appointment.getDateTime().isBefore(startDate))
                    return false;
            }
        } catch (Exception exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }

        return true;
    }

    private boolean isValidEndDate(ZonedDateTime startDate, ZonedDateTime endDate) {
        return endDate.isAfter(startDate);
    }

    private boolean isValidEndDateGivenDependencies(ZonedDateTime endDate) {
        if (endDate != null) {
            try {
                for (Medicine medicine : treatment.getMedicines(this)) {
                    if (medicine.getInitialDosingTime().isAfter(endDate))
                        return false;
                }

                for (MedicalAppointment appointment : treatment.getAppointments(this)) {
                    if (appointment.getDateTime().isAfter(endDate))
                        return false;
                }
            } catch (Exception exception) {
                ExceptionManager.advertiseUI(this, exception.getMessage());
            }
        }

        return true;
    }

    private boolean isValidDiagnosis(String diagnosis) {
        return diagnosis.length() <= 200;
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }
}
