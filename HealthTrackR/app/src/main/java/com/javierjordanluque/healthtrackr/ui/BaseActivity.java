package com.javierjordanluque.healthtrackr.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.HealthTrackRApp;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.util.AuthenticationService;
import com.javierjordanluque.healthtrackr.util.NavigationUtils;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

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
            AuthenticationService.logout(this, sessionViewModel.getUserSession());
            AuthenticationService.clearCredentials(this);

            Intent intent = new Intent(this, AuthenticationActivity.class);
            startActivity(intent);
            finish();

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void showBackButton(boolean show) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(show);
    }

    protected static void hideKeyboard(Activity activity) {
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

    protected Object getDateFromEditText(EditText editTextDate, Class<?> type) {
        String dateString = editTextDate.getText().toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Object date = null;
        if (!dateString.isEmpty()) {
            date = LocalDate.parse(dateString, formatter);

            if (type.equals(ZonedDateTime.class))
                date = ZonedDateTime.ofLocal(((LocalDate) date).atStartOfDay(), TimeZone.getDefault().toZoneId(), null);
        }

        return date;
    }

    protected void showDatePickerDialog(EditText editTextDate, String title, boolean setCurrentYear) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_date_picker, null);

        NumberPicker numberPickerDay = dialogView.findViewById(R.id.numberPickerDay);
        NumberPicker numberPickerMonth = dialogView.findViewById(R.id.numberPickerMonth);
        NumberPicker numberPickerYear = dialogView.findViewById(R.id.numberPickerYear);

        numberPickerDay.setMinValue(1);
        numberPickerDay.setMaxValue(31);

        numberPickerMonth.setMinValue(1);
        numberPickerMonth.setMaxValue(12);

        Calendar calendar = Calendar.getInstance();
        numberPickerYear.setMinValue(1900);
        if (setCurrentYear) {
            numberPickerYear.setMaxValue(calendar.get(Calendar.YEAR));
        } else {
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
            monthOfYear = Integer.parseInt(dateParts[1]) - 1;
            year = Integer.parseInt(dateParts[2]);
        }

        numberPickerDay.setValue(dayOfMonth);
        numberPickerMonth.setValue(monthOfYear);
        numberPickerYear.setValue(year);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle(title);
        builder.setPositiveButton(getString(R.string.dialog_accept), (dialog, which) -> {
            editTextDate.setText(showFormattedDate(LocalDate.of(numberPickerYear.getValue(), numberPickerMonth.getValue(), numberPickerDay.getValue())));
        });
        builder.setNegativeButton(getString(R.string.dialog_cancel), null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
