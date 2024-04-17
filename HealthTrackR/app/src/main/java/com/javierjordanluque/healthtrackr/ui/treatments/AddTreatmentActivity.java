package com.javierjordanluque.healthtrackr.ui.treatments;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.models.enumerations.TreatmentCategory;
import com.javierjordanluque.healthtrackr.ui.BaseActivity;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.time.ZonedDateTime;

public class AddTreatmentActivity extends BaseActivity {
    private User user;
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
        setContentView(R.layout.activity_add_treatment);
        setUpToolbar(getString(R.string.treatments_app_bar_title_add));
        showBackButton(true);

        user = sessionViewModel.getUserSession();

        layoutTitle = findViewById(R.id.layoutTitle);
        editTextTitle = findViewById(R.id.editTextTitle);
        setEditTextListener(layoutTitle, editTextTitle);

        layoutStartDate = findViewById(R.id.layoutStartDate);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextStartDate.setOnClickListener(view -> showDatePickerDialog(editTextStartDate, getString(R.string.treatments_dialog_message_start_date), false));
        setEditTextListener(layoutStartDate, editTextStartDate);

        layoutEndDate = findViewById(R.id.layoutEndDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);
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

        Button buttonAddTreatment = findViewById(R.id.buttonAddTreatment);
        buttonAddTreatment.setOnClickListener(this::addTreatment);
    }

    private void addTreatment(View view) {
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

        TreatmentCategory category = getCategoryFromSpinner();

        if (diagnosis.isEmpty())
            diagnosis = null;

        try {
            new Treatment(this, user, title, startDate, endDate, diagnosis, category);
            finish();
        } catch (DBInsertException exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }
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
        spinnerCategory.setSelection(0);
    }

    private boolean isValidTitle(String title) {
        return !title.isEmpty() && title.length() <= 50;
    }

    private boolean isValidStartDate(String startDate) {
        return !startDate.isEmpty();
    }

    private boolean isValidEndDate(ZonedDateTime startDate, ZonedDateTime endDate) {
        return endDate.isAfter(startDate);
    }

    private boolean isValidDiagnosis(String diagnosis) {
        return diagnosis.length() <= 200;
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }
}
