package com.javierjordanluque.healthtrackr.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.HealthTrackRApp;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Medicine;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.util.AuthenticationService;
import com.javierjordanluque.healthtrackr.util.NavigationUtils;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public abstract class BaseActivity extends AppCompatActivity {
    public SessionViewModel sessionViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HealthTrackRApp healthTrackRApp = (HealthTrackRApp) getApplication();
        sessionViewModel = healthTrackRApp.getSessionViewModel();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    protected abstract int getMenu();

    protected void setUpToolbar(String toolbarTitle) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(toolbarTitle);

        toolbar.setOnLongClickListener(view -> {
            Toast.makeText(this, getSupportActionBar().getTitle(), Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    public void setToolbarTitle(String title) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(getMenu(), menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.menuHelp) {
            NavigationUtils.openUserManual(this);
            return true;
        } else if (itemId == R.id.menuSignOut) {
            showSignOutConfirmationDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void showSignOutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.account_dialog_message_sign_out))
                .setPositiveButton(getString(R.string.account_sign_out), (dialog, id) -> {
                    AuthenticationService.logout(this, sessionViewModel.getUserSession());
                    AuthenticationService.clearCredentials(this);

                    Intent intent = new Intent(this, AuthenticationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    public Treatment getTreatmentFromBundle(Bundle bundle) {
        User user = sessionViewModel.getUserSession();

        if (user != null) {
            if (bundle != null) {
                long treatmentId = bundle.getLong(Treatment.class.getSimpleName());
                try {
                    for (Treatment treatment : user.getTreatments(this)) {
                        if (treatment.getId() == treatmentId) {
                            return treatment;
                        }
                    }
                } catch (DBFindException exception) {
                    ExceptionManager.advertiseUI(this, exception.getMessage());
                }
            }
        }

        return null;
    }

    public Medicine getMedicineFromBundle(Treatment treatment, Bundle bundle) {
        if (bundle != null) {
            long medicineId = bundle.getLong(Medicine.class.getSimpleName());
            try {
                for (Medicine medicine : treatment.getMedicines(this)) {
                    if (medicine.getId() == medicineId) {
                        return medicine;
                    }
                }
            } catch (DBFindException exception) {
                ExceptionManager.advertiseUI(this, exception.getMessage());
            }
        }

        return null;
    }

    public Treatment getTreatmentFromIntent(Intent intent) {
        User user = sessionViewModel.getUserSession();

        if (user != null) {
            if (intent != null) {
                long treatmentId = intent.getLongExtra(Treatment.class.getSimpleName(), -1);
                try {
                    for (Treatment treatment : user.getTreatments(this)) {
                        if (treatment.getId() == treatmentId) {
                            return treatment;
                        }
                    }
                } catch (DBFindException exception) {
                    ExceptionManager.advertiseUI(this, exception.getMessage());
                }
            }
        }

        return null;
    }

    public Medicine getMedicineFromIntent(Treatment treatment, Intent intent) {
        if (intent != null) {
            long medicineId = intent.getLongExtra(Medicine.class.getSimpleName(), -1);
            try {
                for (Medicine medicine : treatment.getMedicines(this)) {
                    if (medicine.getId() == medicineId) {
                        return medicine;
                    }
                }
            } catch (DBFindException exception) {
                ExceptionManager.advertiseUI(this, exception.getMessage());
            }
        }

        return null;
    }

    public void showBackButton(boolean show) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(show);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = activity.getCurrentFocus();

        if (inputMethodManager != null && currentFocus != null)
            inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }

    protected void setEditTextListener(TextInputLayout textLayout, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public String showFormattedList(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            stringBuilder.append(list.get(i));
            if (i < list.size() - 1)
                stringBuilder.append(", ");
        }

        return stringBuilder.toString();
    }

    public String showFormattedDate(Object date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String formattedDate = null;
        if (date instanceof LocalDate) {
            LocalDate localDate = (LocalDate) date;
            formattedDate = localDate.format(formatter);
        } else if (date instanceof ZonedDateTime) {
            ZonedDateTime zonedDateTime = (ZonedDateTime) date;
            formattedDate = zonedDateTime.format(formatter);
        } else {
            try {
                throw new IllegalArgumentException("Failed to format date of an object of type (" + date.getClass().getSimpleName() + ")");
            } catch (IllegalArgumentException exception) {
                ExceptionManager.advertiseUI(this, exception.getMessage());
            }
        }

        return formattedDate;
    }

    public String showFormattedDateTime(ZonedDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy '" + getString(R.string.date_at_time) + "' HH:mm");

        return dateTime.format(formatter);
    }

    public void showDatePickerDialog(EditText editTextDate, String title, boolean setBirthDate) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_date_picker, null);

        NumberPicker numberPickerDay = dialogView.findViewById(R.id.numberPickerDay);
        NumberPicker numberPickerMonth = dialogView.findViewById(R.id.numberPickerMonth);
        NumberPicker numberPickerYear = dialogView.findViewById(R.id.numberPickerYear);

        numberPickerDay.setMinValue(1);
        numberPickerMonth.setMinValue(1);
        numberPickerMonth.setMaxValue(12);

        Calendar calendar = Calendar.getInstance();
        if (setBirthDate) {
            numberPickerYear.setMinValue(1900);
            numberPickerYear.setMaxValue(calendar.get(Calendar.YEAR));
        } else {
            numberPickerYear.setMinValue(2000);
            numberPickerYear.setMaxValue(calendar.get(Calendar.YEAR) + 10);
        }

        String dateString = editTextDate.getText().toString().trim();

        int dayOfMonth, monthOfYear, year;
        if (dateString.isEmpty()) {
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            monthOfYear = calendar.get(Calendar.MONTH) + 1;
            year = calendar.get(Calendar.YEAR);
        } else {
            String[] dateParts = dateString.split("/");

            dayOfMonth = Integer.parseInt(dateParts[0]);
            monthOfYear = Integer.parseInt(dateParts[1]);
            year = Integer.parseInt(dateParts[2]);
        }

        numberPickerDay.setMaxValue(getMaxDayOfMonth(monthOfYear, year));
        numberPickerDay.setValue(dayOfMonth);
        numberPickerMonth.setValue(monthOfYear);
        numberPickerYear.setValue(year);

        numberPickerMonth.setOnValueChangedListener((picker, oldVal, newVal) -> numberPickerDay.setMaxValue(getMaxDayOfMonth(newVal, numberPickerYear.getValue())));

        numberPickerYear.setOnValueChangedListener((picker, oldVal, newVal) -> numberPickerDay.setMaxValue(getMaxDayOfMonth(numberPickerMonth.getValue(), newVal)));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle(title);
        builder.setPositiveButton(getString(R.string.dialog_positive_confirm), (dialog, which) ->
                editTextDate.setText(showFormattedDate(LocalDate.of(numberPickerYear.getValue(), numberPickerMonth.getValue(), numberPickerDay.getValue()))));
        builder.setNegativeButton(getString(R.string.dialog_negative_cancel), null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public Object getDateFromEditText(EditText editTextDate, Class<?> type) {
        String dateString = editTextDate.getText().toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Object date = null;
        if (!dateString.isEmpty()) {
            date = LocalDate.parse(dateString, formatter);

            if (type.equals(ZonedDateTime.class))
                date = ZonedDateTime.ofLocal(((LocalDate) date).atStartOfDay(), ZoneId.systemDefault(), null);
        }

        return date;
    }

    private int getMaxDayOfMonth(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public void setTreatmentLayoutStatus(Treatment treatment, ImageView imageViewStatus, TextView textViewStatus) {
        if (!treatment.isStarted()) {
            imageViewStatus.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_status_pending));
            imageViewStatus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            imageViewStatus.setContentDescription(getString(R.string.content_description_status_pending));
            textViewStatus.setText(getString(R.string.treatments_status_pending));
        } else if (treatment.isStarted() && !treatment.isFinished()) {
            imageViewStatus.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_status_in_progress));
            imageViewStatus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.amber)));
            imageViewStatus.setContentDescription(getString(R.string.content_description_status_in_progress));
            textViewStatus.setText(getString(R.string.treatments_status_in_progress));
        } else if (treatment.isFinished()) {
            imageViewStatus.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_status_finished));
            imageViewStatus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green)));
            imageViewStatus.setContentDescription(getString(R.string.content_description_status_finished));
            textViewStatus.setText(getString(R.string.treatments_status_finished));
        }
    }

    public String formatTimeDifference(long timeDifferenceMillis) {
        long days = TimeUnit.MILLISECONDS.toDays(timeDifferenceMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis) % 60;

        if (TimeUnit.MILLISECONDS.toSeconds(timeDifferenceMillis) % 60 >= 30)
            minutes++;

        if (minutes == 60) {
            minutes = 0;
            hours++;
        }

        if (hours == 24) {
            hours = 0;
            days++;
        }

        String timeDifferenceString = "";
        if (days == 0 && hours == 0 && minutes == 0) {
            timeDifferenceString = getString(R.string.medicines_now);
        } else {
            if (days > 0)
                timeDifferenceString += getResources().getQuantityString(R.plurals.medicines_days, (int) days, days) + " ";
            if (hours > 0)
                timeDifferenceString += getResources().getQuantityString(R.plurals.medicines_hours, (int) hours, hours) + " ";
            if (minutes > 0)
                timeDifferenceString += getResources().getQuantityString(R.plurals.medicines_minutes, (int) minutes, minutes);
        }

        return timeDifferenceString.trim();
    }

    public void showTreatmentFinishedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.error_finished_treatment);
        builder.setPositiveButton(R.string.dialog_positive_ok, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    protected void showDateTimePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePickerDialog.OnDateSetListener) (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (TimePickerDialog.OnTimeSetListener) (view1, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy '" + getString(R.string.date_at_time) + "' HH:mm", Locale.getDefault());
                        editText.setText(sdf.format(calendar.getTime()));
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    protected ZonedDateTime getDateTimeFromEditText(EditText editTextDate) {
        String dateTimeString = editTextDate.getText().toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy '" + getString(R.string.date_at_time) + "' HH:mm");

        ZonedDateTime dateTime = null;
        if (!dateTimeString.isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);
            dateTime = ZonedDateTime.ofLocal(localDateTime, ZoneId.systemDefault(), null);
        }

        return dateTime;
    }
}
