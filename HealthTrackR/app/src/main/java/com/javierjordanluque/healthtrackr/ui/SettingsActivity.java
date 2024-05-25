package com.javierjordanluque.healthtrackr.ui;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.javierjordanluque.healthtrackr.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class SettingsActivity extends BaseActivity {
    private String localeCode;
    private LinkedHashMap<String, String> localeOptions;
    private boolean isSpinnerInitialization = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setUpToolbar(getString(R.string.settings_app_bar_title));
        showBackButton(true);

        setLocaleCode();
        configureLocaleSpinner();

        Button buttonResetSettings = findViewById(R.id.buttonResetSettings);
        buttonResetSettings.setOnClickListener(view -> showResetSettingsConfirmationDialog());
    }

    private void showResetSettingsConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.settings_dialog_message_reset))
                .setPositiveButton(getString(R.string.settings_dialog_positive_reset), (dialog, id) -> {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());

                    Toast.makeText(this, getString(R.string.toast_confirmation_save), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> dialog.dismiss());
        builder.show();
    }

    private void configureLocaleSpinner() {
        Spinner spinnerLanguage = findViewById(R.id.spinnerLocale);
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSpinnerInitialization) {
                    isSpinnerInitialization = false;
                    return;
                }

                String selectedLanguage = localeOptions.get(parent.getItemAtPosition(position).toString());
                if (selectedLanguage != null && !selectedLanguage.equals(localeCode)) {
                    localeCode = selectedLanguage;
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(selectedLanguage));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.settings_array_locale));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);
        spinnerLanguage.postDelayed(() -> spinnerLanguage.setSelection(new ArrayList<>(localeOptions.values()).indexOf(localeCode)), 1);
    }

    private void setLocaleCode() {
        String[] languageOptions = getResources().getStringArray(R.array.settings_array_locale);
        localeOptions = new LinkedHashMap<>();
        localeOptions.put(languageOptions[0], "da");
        localeOptions.put(languageOptions[1], "de");
        localeOptions.put(languageOptions[2], "en");
        localeOptions.put(languageOptions[3], "es");
        localeOptions.put(languageOptions[4], "fr");
        localeOptions.put(languageOptions[5], "it");
        localeOptions.put(languageOptions[6], "pt");
        localeOptions.put(languageOptions[7], "ru");
        localeOptions.put(languageOptions[8], "sv");

        localeCode = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        if (localeCode.isEmpty()) {
            localeCode = Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();

            if (!localeOptions.containsValue(localeCode))
                localeCode = "en";
        }
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_settings_menu;
    }
}
