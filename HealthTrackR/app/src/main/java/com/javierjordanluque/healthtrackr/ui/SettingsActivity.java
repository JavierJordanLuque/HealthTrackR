package com.javierjordanluque.healthtrackr.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.os.LocaleListCompat;

import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.util.Settings;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class SettingsActivity extends BaseActivity {
    private String localeCode;
    private LinkedHashMap<String, String> localeOptions;
    private boolean isSpinnerInitialization = true;
    private RadioButton radioButtonSystemDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setUpToolbar(getString(R.string.settings_app_bar_title));
        showBackButton(true);

        setLocaleCode();
        configureLocaleSpinner();

        configureRadioGroupTheme();

        Button buttonResetSettings = findViewById(R.id.buttonResetSettings);
        buttonResetSettings.setOnClickListener(view -> showResetSettingsConfirmationDialog());

        Button buttonAutostart = findViewById(R.id.buttonAutostart);
        buttonAutostart.setOnClickListener(view -> openAutostartSettings());
    }

    private void showResetSettingsConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.settings_dialog_message_reset))
                .setPositiveButton(getString(R.string.settings_dialog_positive_reset), (dialog, id) -> {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());

                    Settings.clearSettings(this);
                    radioButtonSystemDefault.setChecked(true);

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

    private void configureRadioGroupTheme() {
        radioButtonSystemDefault = findViewById(R.id.radioButtonSystemDefault);
        RadioGroup radioGroupTheme = findViewById(R.id.radioGroupTheme);
        RadioButton radioButtonLight = findViewById(R.id.radioButtonLight);
        RadioButton radioButtonDark = findViewById(R.id.radioButtonDark);

        int systemDefaultMode = Resources.getSystem().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (systemDefaultMode == Configuration.UI_MODE_NIGHT_NO) {
            radioButtonSystemDefault.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.drawable.ic_light_mode), null);
        } else {
            radioButtonSystemDefault.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.drawable.ic_dark_mode), null);
        }

        int appTheme = AppCompatDelegate.getDefaultNightMode();
        if (appTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            radioButtonLight.setChecked(true);
        } else if (appTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            radioButtonDark.setChecked(true);
        } else {
            radioButtonSystemDefault.setChecked(true);
        }

        radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonLight && appTheme != AppCompatDelegate.MODE_NIGHT_NO) {
                Settings.saveTheme(this, Settings.LIGHT);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.radioButtonDark && appTheme != AppCompatDelegate.MODE_NIGHT_YES) {
                Settings.saveTheme(this, Settings.DARK);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else if (checkedId == R.id.radioButtonSystemDefault && appTheme != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                Settings.saveTheme(this, Settings.SYSTEM_DEFAULT);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        });
    }

    /** @noinspection SpellCheckingInspection*/
    private void openAutostartSettings() {
        Intent[] AUTO_START_INTENTS = {
                new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
                new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity")),
                new Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT),
                new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
                new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
                new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
                new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
                new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
                new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
                new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
                new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")).setData(
                        Uri.parse("mobilemanager://function/entry/AutoStart"))
        };

        boolean intentMatches = false;
        for (Intent intent : AUTO_START_INTENTS){
            if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                intentMatches = true;

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getResources().getConfiguration().getLocales().get(0).getLanguage());
                startActivity(intent);
                break;
            }
        }

        if (!intentMatches) {
            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getResources().getConfiguration().getLocales().get(0).getLanguage());
            startActivity(intent);
        }
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_settings_menu;
    }
}
