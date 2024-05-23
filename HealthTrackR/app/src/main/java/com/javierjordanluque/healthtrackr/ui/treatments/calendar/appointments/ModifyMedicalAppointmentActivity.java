package com.javierjordanluque.healthtrackr.ui.treatments.calendar.appointments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Location;
import com.javierjordanluque.healthtrackr.models.MedicalAppointment;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.ui.BaseActivity;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.time.ZonedDateTime;

public class ModifyMedicalAppointmentActivity extends BaseActivity {
    private Treatment treatment;
    private MedicalAppointment medicalAppointment;
    private TextInputLayout layoutDateTime;
    private TextInputLayout layoutSubject;
    private ConstraintLayout layoutLocation;
    private EditText editTextDateTime;
    private EditText editTextSubject;
    private EditText editTextLatitude;
    private EditText editTextLongitude;
    private TextView textViewLocationError;
    private ImageView imageViewLocationError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_medical_appointment);
        setUpToolbar(getString(R.string.medical_appointment_app_bar_title_modify));
        showBackButton(true);

        treatment = getTreatmentFromIntent(getIntent());
        medicalAppointment = getMedicalAppointmentFromIntent(treatment, getIntent());

        layoutDateTime = findViewById(R.id.layoutDateTime);
        editTextDateTime = findViewById(R.id.editTextDateTime);
        editTextDateTime.setOnClickListener(view -> showDateTimePicker(editTextDateTime, medicalAppointment.getDateTime(), treatment.getStartDate(), treatment.getEndDate()));
        editTextDateTime.setText(showFormattedDateTime(medicalAppointment.getDateTime()));

        layoutSubject = findViewById(R.id.layoutSubject);
        editTextSubject = findViewById(R.id.editTextSubject);
        setEditTextListener(layoutSubject, editTextSubject);
        String subject = medicalAppointment.getSubject();
        if (subject != null)
            editTextSubject.setText(subject);

        textViewLocationError = findViewById(R.id.textViewLocationError);
        imageViewLocationError = findViewById(R.id.imageViewLocationError);

        layoutLocation = findViewById(R.id.layoutLocation);
        editTextLatitude = findViewById(R.id.editTextLatitude);
        editTextLatitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textViewLocationError.setVisibility(View.INVISIBLE);
                imageViewLocationError.setVisibility(View.INVISIBLE);
                layoutLocation.setBackgroundResource(R.drawable.form_layout_container_filled);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        editTextLongitude = findViewById(R.id.editTextLongitude);
        editTextLongitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textViewLocationError.setVisibility(View.INVISIBLE);
                imageViewLocationError.setVisibility(View.INVISIBLE);
                layoutLocation.setBackgroundResource(R.drawable.form_layout_container_filled);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Location location = medicalAppointment.getLocation();
        if (location != null) {
            editTextLatitude.setText(String.valueOf(location.getLatitude()));
            editTextLongitude.setText(String.valueOf(location.getLongitude()));
        }

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(this::modifyMedicalAppointment);
    }

    private void modifyMedicalAppointment(View view) {
        hideKeyboard(this);

        String subject = editTextSubject.getText().toString().trim();
        String latitude = editTextLatitude.getText().toString().trim();
        String longitude = editTextLongitude.getText().toString().trim();

        boolean validDateTime = isValidDateTime(editTextDateTime.getText().toString().trim());
        boolean validSubject = isValidSubject(subject);
        boolean validLocation = isValidLocation(latitude, longitude);

        if (!validDateTime || !validSubject || !validLocation) {
            if (!validDateTime)
                layoutDateTime.setError(getString(R.string.error_invalid_medical_appointment_date_time));
            if (!validSubject)
                layoutSubject.setError(getString(R.string.error_invalid_medical_appointment_subject));
            if (!validLocation) {
                textViewLocationError.setVisibility(View.VISIBLE);
                imageViewLocationError.setVisibility(View.VISIBLE);
                layoutLocation.setBackgroundResource(R.drawable.form_layout_container_outlined_error);
            }

            return;
        }

        ZonedDateTime dateTime = getDateTimeFromEditText(editTextDateTime);

        if (subject.isEmpty())
            subject = null;

        Location location = null;
        if (!latitude.isEmpty() && !longitude.isEmpty())
            location = new Location(Double.parseDouble(latitude), Double.parseDouble(longitude));

        showModifyMedicalAppointmentConfirmationDialog(dateTime, subject, location);
    }

    private void showModifyMedicalAppointmentConfirmationDialog(ZonedDateTime dateTime, String subject, Location location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_message_save))
                .setPositiveButton(getString(R.string.button_save), (dialog, id) -> {
                    try {
                        medicalAppointment.modifyMedicalAppointment(this, dateTime, subject, location);

                        Toast.makeText(this, getString(R.string.toast_confirmation_save), Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (DBFindException | DBDeleteException | DBUpdateException | DBInsertException exception) {
                        ExceptionManager.advertiseUI(this, exception.getMessage());
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> dialog.dismiss());
        builder.show();
    }

    private boolean isValidDateTime(String dateTime) {
        return !dateTime.isEmpty();
    }

    private boolean isValidSubject(String subject) {
        return subject.length() <= 200;
    }

    private boolean isValidLocation(String latitude, String longitude) {
        boolean isValidLocation;
        if (latitude.isEmpty() && longitude.isEmpty()) {
            isValidLocation = true;
        } else if (latitude.isEmpty() || longitude.isEmpty()) {
            isValidLocation = false;
        } else {
            try {
                double lat = Double.parseDouble(latitude);
                double lon = Double.parseDouble(longitude);

                isValidLocation = lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
            } catch (NumberFormatException exception) {
                isValidLocation = false;
            }
        }

        return isValidLocation;
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }
}
