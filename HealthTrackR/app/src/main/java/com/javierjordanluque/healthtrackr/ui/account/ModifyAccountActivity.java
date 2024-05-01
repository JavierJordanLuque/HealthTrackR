package com.javierjordanluque.healthtrackr.ui.account;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
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
import com.javierjordanluque.healthtrackr.models.Allergy;
import com.javierjordanluque.healthtrackr.models.PreviousMedicalCondition;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.models.enumerations.BloodType;
import com.javierjordanluque.healthtrackr.models.enumerations.Gender;
import com.javierjordanluque.healthtrackr.ui.BaseActivity;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModifyAccountActivity extends BaseActivity {
    private User user;
    private TextInputLayout layoutFirstName;
    private TextInputLayout layoutLastName;
    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextBirthDate;
    private Spinner spinnerGender;
    private Spinner spinnerBloodType;
    private EditText editTextAllergies;
    private EditText editTextPreviousMedicalConditions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_account);
        setUpToolbar(getString(R.string.account_app_bar_title_modify));
        showBackButton(true);

        user = sessionViewModel.getUserSession();

        layoutFirstName = findViewById(R.id.layoutFirstName);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        setEditTextListener(layoutFirstName, editTextFirstName);
        editTextFirstName.setText(user.getFirstName());

        layoutLastName = findViewById(R.id.layoutLastName);
        editTextLastName = findViewById(R.id.editTextLastName);
        setEditTextListener(layoutLastName, editTextLastName);
        editTextLastName.setText(user.getLastName());

        editTextBirthDate = findViewById(R.id.editTextBirthDate);
        LocalDate birthDate = user.getBirthDate();
        if (birthDate != null) {
            editTextBirthDate.setFocusableInTouchMode(true);
            editTextBirthDate.setText(showFormattedDate(birthDate));
        }
        editTextBirthDate.setOnClickListener(view -> showDatePickerDialog(editTextBirthDate, getString(R.string.account_dialog_message_birth_date), true));

        Activity activity = this;
        editTextBirthDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    editTextBirthDate.setFocusableInTouchMode(true);
                } else {
                    hideKeyboard(activity);
                    editTextBirthDate.clearFocus();
                    editTextBirthDate.setFocusableInTouchMode(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        configureGenderSpinner();
        configureBloodTypeSpinner();

        editTextAllergies = findViewById(R.id.editTextAllergies);
        try {
            List<Allergy> allergies = user.getAllergies(this);

            if (allergies != null && !allergies.isEmpty()) {
                List<String> allergiesNames = new ArrayList<>();
                for (Allergy allergy : allergies)
                    allergiesNames.add(allergy.getName());

                editTextAllergies.setText(showFormattedList(allergiesNames));
            }
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }

        editTextPreviousMedicalConditions = findViewById(R.id.editTextPreviousMedicalConditions);
        try {
            List<PreviousMedicalCondition> conditions = user.getConditions(this);

            if (conditions != null && !conditions.isEmpty()) {
                List<String> conditionsNames = new ArrayList<>();
                for (PreviousMedicalCondition condition : conditions)
                    conditionsNames.add(condition.getName());

                editTextPreviousMedicalConditions.setText(showFormattedList(conditionsNames));
            }
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }

        Button buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonChangePassword.setOnClickListener(view -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(this::modifyAccount);
    }

    private void modifyAccount(View view) {
        hideKeyboard(this);

        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();

        boolean validFirstName = isValidFirstName(firstName);
        boolean validLastName = isValidLastName(lastName);

        if (!validFirstName || !validLastName) {
            if (!validFirstName)
                layoutFirstName.setError(getString(R.string.error_invalid_first_name));
            if (!validLastName)
                layoutLastName.setError(getString(R.string.error_invalid_last_name));

            return;
        }

        LocalDate birthDate = null;
        if (!editTextBirthDate.getText().toString().trim().isEmpty())
            birthDate = (LocalDate) getDateFromEditText(editTextBirthDate, LocalDate.class);

        Gender gender = getGenderFromSpinner();
        BloodType bloodType = getBloodTypeFromSpinner();
        List<Allergy> allergies = getAllergiesFromEditText();
        List<PreviousMedicalCondition> conditions = getPreviousMedicalConditionsFromEditText();

       showModifyAccountConfirmationDialog(firstName, lastName, birthDate, gender, bloodType, allergies, conditions);
    }

    private void showModifyAccountConfirmationDialog(String firstName, String lastName, LocalDate birthDate, Gender gender, BloodType bloodType,
                                                     List<Allergy> allergies, List<PreviousMedicalCondition> conditions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_message_save))
                .setPositiveButton(getString(R.string.dialog_positive_save), (dialog, id) -> {
                    try {
                        user.modifyUser(this, firstName, lastName, birthDate, gender, bloodType, allergies, conditions);
                        Toast.makeText(this, getString(R.string.toast_confirmation_save), Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (DBDeleteException | DBInsertException | DBUpdateException exception) {
                        ExceptionManager.advertiseUI(this, exception.getMessage());
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> dialog.dismiss());
        builder.show();
    }

    private Gender getGenderFromSpinner() {
        String[] genderOptions = getResources().getStringArray(R.array.account_array_gender);
        String selectedGender = spinnerGender.getSelectedItem().toString();

        if (selectedGender.equals(genderOptions[0])) {
            return Gender.MALE;
        } else if (selectedGender.equals(genderOptions[1])) {
            return Gender.FEMALE;
        } else {
            return Gender.UNSPECIFIED;
        }
    }

    private BloodType getBloodTypeFromSpinner() {
        String[] bloodTypeOptions = getResources().getStringArray(R.array.account_array_blood_type);
        String selectedBloodType = spinnerBloodType.getSelectedItem().toString();

        if (selectedBloodType.equals(bloodTypeOptions[0])) {
            return BloodType.A_POSITIVE;
        } else if (selectedBloodType.equals(bloodTypeOptions[1])) {
            return BloodType.A_NEGATIVE;
        } else if (selectedBloodType.equals(bloodTypeOptions[2])) {
            return BloodType.B_POSITIVE;
        } else if (selectedBloodType.equals(bloodTypeOptions[3])) {
            return BloodType.B_NEGATIVE;
        } else if (selectedBloodType.equals(bloodTypeOptions[4])) {
            return BloodType.AB_POSITIVE;
        } else if (selectedBloodType.equals(bloodTypeOptions[5])) {
            return BloodType.AB_NEGATIVE;
        } else if (selectedBloodType.equals(bloodTypeOptions[6])) {
            return BloodType.O_POSITIVE;
        } else if (selectedBloodType.equals(bloodTypeOptions[7])) {
            return BloodType.O_NEGATIVE;
        } else {
            return BloodType.UNSPECIFIED;
        }
    }

    private List<Allergy> getAllergiesFromEditText() {
        List<Allergy> allergies = new ArrayList<>();
        String allergiesText = editTextAllergies.getText().toString().trim();

        if (!allergiesText.isEmpty()) {
            String[] allergyArray = allergiesText.split(",");
            for (String allergyName : allergyArray) {
                String trimmedAllergyName = allergyName.trim();
                if (!trimmedAllergyName.isEmpty()) {
                    allergies.add(new Allergy(user, trimmedAllergyName));
                }
            }
        }

        return allergies;
    }

    private List<PreviousMedicalCondition> getPreviousMedicalConditionsFromEditText() {
        List<PreviousMedicalCondition> conditions = new ArrayList<>();
        String conditionsText = editTextPreviousMedicalConditions.getText().toString().trim();

        if (!conditionsText.isEmpty()) {
            String[] conditionArray = conditionsText.split(",");
            for (String conditionName : conditionArray) {
                String trimmedConditionName = conditionName.trim();
                if (!trimmedConditionName.isEmpty()) {
                    conditions.add(new PreviousMedicalCondition(user, trimmedConditionName));
                }
            }
        }

        return conditions;
    }

    private void configureGenderSpinner() {
        spinnerGender = findViewById(R.id.spinnerGender);

        String[] genderOptions = getResources().getStringArray(R.array.account_array_gender);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        if (user.getGender() != null) {
            int index = Arrays.asList(Gender.values()).indexOf(user.getGender());
            spinnerGender.setSelection(index);
        } else {
            spinnerGender.setSelection(genderOptions.length - 1);
        }
    }

    private void configureBloodTypeSpinner() {
        spinnerBloodType = findViewById(R.id.spinnerBloodType);

        String[] bloodTypeOptions = getResources().getStringArray(R.array.account_array_blood_type);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bloodTypeOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodType.setAdapter(adapter);

        if (user.getBloodType() != null) {
            int index = Arrays.asList(BloodType.values()).indexOf(user.getBloodType());
            spinnerBloodType.setSelection(index);
        } else {
            spinnerBloodType.setSelection(bloodTypeOptions.length - 1);
        }
    }

    private boolean isValidFirstName(String firstName) {
        return !firstName.isEmpty() && firstName.length() <= 50;
    }

    private boolean isValidLastName(String lastName) {
        return !lastName.isEmpty() && lastName.length() <= 50;
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }
}
