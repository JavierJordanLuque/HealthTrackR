package com.javierjordanluque.healthtrackr.ui.treatments.guidelines;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Guideline;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.ui.BaseActivity;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.util.List;

public class AddGuidelineActivity extends BaseActivity {
    private Treatment treatment;
    private TextInputLayout layoutTitle;
    private TextInputLayout layoutDescription;
    private EditText editTextTitle;
    private EditText editTextDescription;
    private Spinner spinnerNumOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_guideline);
        setUpToolbar(getString(R.string.guidelines_app_bar_title_add));
        showBackButton(true);

        treatment = getTreatmentFromIntent(getIntent());

        layoutTitle = findViewById(R.id.layoutTitle);
        editTextTitle = findViewById(R.id.editTextTitle);
        setEditTextListener(layoutTitle, editTextTitle);

        layoutDescription = findViewById(R.id.layoutDescription);
        editTextDescription = findViewById(R.id.editTextDescription);
        setEditTextListener(layoutDescription, editTextDescription);

        configureNumOrderSpinner();

        Button buttonAddGuideline = findViewById(R.id.buttonAddGuideline);
        buttonAddGuideline.setOnClickListener(this::addGuideline);
    }

    private void addGuideline(View view) {
        hideKeyboard(this);

        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        boolean validTitle = isValidTitle(title);
        boolean validDescription = isValidDescription(description);

        if (!validTitle || !validDescription) {
            if (!validTitle)
                layoutTitle.setError(getString(R.string.error_invalid_guideline_title));
            if (!validDescription)
                layoutDescription.setError(getString(R.string.error_invalid_guideline_description));

            return;
        }

        if (description.isEmpty())
            description = null;

        int numOrder = getNumOrderFromSpinner();

        try {
            new Guideline(this, treatment, title, description, numOrder);
            finish();
        } catch (DBInsertException | DBFindException | DBUpdateException exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }
    }

    private int getNumOrderFromSpinner() {
        return Integer.parseInt(spinnerNumOrder.getSelectedItem().toString());
    }

    private void configureNumOrderSpinner() {
        spinnerNumOrder = findViewById(R.id.spinnerNumOrder);

        try {
            List<Guideline> guidelines = treatment.getGuidelines(this);

            String[] numOrderOptions = new String[guidelines.size() + 1];
            for (int i = 0; i < numOrderOptions.length; i++)
                numOrderOptions[i] = String.valueOf(i + 1);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, numOrderOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerNumOrder.setAdapter(adapter);
            spinnerNumOrder.setSelection(numOrderOptions.length - 1);
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }
    }

    private boolean isValidTitle(String title) {
        return !title.isEmpty();
    }

    private boolean isValidDescription(String description) {
        return description.length() <= 300;
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }
}