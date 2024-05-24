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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Location;
import com.javierjordanluque.healthtrackr.models.MedicalAppointment;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.ui.BaseActivity;
import com.javierjordanluque.healthtrackr.ui.MainActivity;
import com.javierjordanluque.healthtrackr.util.PermissionManager;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;
import com.javierjordanluque.healthtrackr.util.notifications.NotificationScheduler;

import java.time.ZonedDateTime;

public class AddMedicalAppointmentActivity extends BaseActivity {
    private Treatment treatment;
    private MedicalAppointment medicalAppointment;
    private TextInputLayout layoutDateTime;
    private TextInputLayout layoutSubject;
    private SwitchMaterial switchLocation;
    private TextInputLayout layoutPlace;
    private ConstraintLayout layoutLocationDisplay;
    private ConstraintLayout layoutLocation;
    private EditText editTextDateTime;
    private EditText editTextSubject;
    private EditText editTextPlace;
    private EditText editTextLatitude;
    private EditText editTextLongitude;
    private TextView textViewLocationError;
    private ImageView imageViewLocationError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medical_appointment);
        setUpToolbar(getString(R.string.medical_appointment_app_bar_title_add));
        showBackButton(true);

        treatment = getTreatmentFromIntent(getIntent());

        layoutDateTime = findViewById(R.id.layoutDateTime);
        editTextDateTime = findViewById(R.id.editTextDateTime);
        editTextDateTime.setOnClickListener(view -> showDateTimePicker(editTextDateTime, null, treatment.getStartDate(), treatment.getEndDate()));
        setEditTextListener(layoutDateTime, editTextDateTime);

        layoutSubject = findViewById(R.id.layoutSubject);
        editTextSubject = findViewById(R.id.editTextSubject);
        setEditTextListener(layoutSubject, editTextSubject);

        layoutPlace = findViewById(R.id.layoutPlace);
        editTextPlace = findViewById(R.id.editTextPlace);

        textViewLocationError = findViewById(R.id.textViewLocationError);
        imageViewLocationError = findViewById(R.id.imageViewLocationError);

        layoutLocationDisplay = findViewById(R.id.layoutLocationDisplay);
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
                layoutLocation.setBackgroundResource(R.drawable.form_layout_container_outlined);
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
                layoutLocation.setBackgroundResource(R.drawable.form_layout_container_outlined);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        switchLocation = findViewById(R.id.switchLocation);
        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                layoutPlace.setVisibility(View.VISIBLE);
                layoutLocationDisplay.setVisibility(View.GONE);
            } else {
                layoutPlace.setVisibility(View.GONE);
                layoutLocationDisplay.setVisibility(View.VISIBLE);
            }
        });

        Button buttonAddMedicalAppointment = findViewById(R.id.buttonAddMedicalAppointment);
        buttonAddMedicalAppointment.setOnClickListener(this::addMedicalAppointment);
    }

    private void addMedicalAppointment(View view) {
        hideKeyboard(this);

        String subject = editTextSubject.getText().toString().trim();
        String latitude = editTextLatitude.getText().toString().trim();
        String longitude = editTextLongitude.getText().toString().trim();

        boolean validDateTime = isValidDateTime(editTextDateTime.getText().toString().trim());
        boolean validSubject = isValidSubject(subject);
        boolean validLocation = !switchLocation.isChecked() || isValidLocation(latitude, longitude);

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
        if (!switchLocation.isChecked()) {
            String place = editTextPlace.getText().toString().trim();
            if (!place.isEmpty())
                location = new Location(place, null, null);
        } else {
            if (!latitude.isEmpty() && !longitude.isEmpty())
                location = new Location(null, Double.parseDouble(latitude), Double.parseDouble(longitude));
        }

        try {
            medicalAppointment = new MedicalAppointment(this, treatment, dateTime, subject, location);

            if (PermissionManager.hasNotificationPermission(this)) {
                scheduleNotification();
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionManager.REQUEST_CODE_PERMISSION_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleNotification();
                openNextActivity();
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
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> openNextActivity());
        builder.show();
    }

    private final ActivityResultLauncher<Intent> notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
            scheduleNotification();
        openNextActivity();
    });

    private void scheduleNotification() {
        try {
            medicalAppointment.scheduleAppointmentNotification(this, NotificationScheduler.PREVIOUS_DEFAULT_MINUTES);
        } catch (DBInsertException exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }
    }

    private void openNextActivity() {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.class.getSimpleName(), MedicalAppointmentFragment.class.getName());

        Bundle bundle = new Bundle();
        bundle.putLong(Treatment.class.getSimpleName(), treatment.getId());
        bundle.putLong(MedicalAppointment.class.getSimpleName(), medicalAppointment.getId());
        intent.putExtras(bundle);

        setResult(RESULT_OK, intent);

        finish();
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
