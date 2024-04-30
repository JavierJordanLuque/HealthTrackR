package com.javierjordanluque.healthtrackr.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.databinding.ActivityMainBinding;
import com.javierjordanluque.healthtrackr.db.repositories.NotificationRepository;
import com.javierjordanluque.healthtrackr.models.Medicine;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.ui.account.AccountFragment;
import com.javierjordanluque.healthtrackr.ui.calendar.CalendarFragment;
import com.javierjordanluque.healthtrackr.ui.treatments.TreatmentFragment;
import com.javierjordanluque.healthtrackr.ui.treatments.TreatmentsFragment;
import com.javierjordanluque.healthtrackr.ui.treatments.medicines.MedicineFragment;
import com.javierjordanluque.healthtrackr.ui.treatments.medicines.MedicinesFragment;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;
import com.javierjordanluque.healthtrackr.util.notifications.MedicationNotification;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends BaseActivity implements OnToolbarChangeListener {
    ActivityMainBinding binding;
    public static final String CURRENT_FRAGMENT = "currentFragment";
    public Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpToolbar(getString(R.string.treatments_app_bar_title));
        showBackButton(false);

        if (savedInstanceState != null)
            currentFragment = getSupportFragmentManager().getFragment(savedInstanceState, CURRENT_FRAGMENT);

        if (currentFragment == null) {
            if (getIntent().hasExtra(CURRENT_FRAGMENT)) {
                String fragmentClassName = getIntent().getStringExtra(CURRENT_FRAGMENT);
                if (fragmentClassName != null) {
                    if (fragmentClassName.equals(MedicineFragment.class.getName())) {
                        showMedicineFragmentFromNotification(fragmentClassName);
                    } else {
                        //showMedicalAppointmentFragmentFromNotification(fragmentClassName);
                    }
                }
            } else {
                currentFragment = new TreatmentsFragment();
                replaceFragment(currentFragment);
            }
        }

        int selectedItem;
        if (currentFragment instanceof CalendarFragment) {
            selectedItem = R.id.navigation_calendar;
        } else if (currentFragment instanceof AccountFragment) {
            selectedItem = R.id.navigation_account;
        } else {
            selectedItem = R.id.navigation_treatments;
        }
        binding.navigationView.setSelectedItemId(selectedItem);

        AtomicInteger easterEggCounter = new AtomicInteger(0);
        binding.navigationView.setOnItemSelectedListener(item -> {
            boolean swappedFragment = false;
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_treatments) {
                if (!(currentFragment instanceof TreatmentsFragment)) {
                    currentFragment = new TreatmentsFragment();
                    swappedFragment = true;
                    easterEggCounter.set(0);
                } else {
                    easterEggCounter.set(easterEggCounter.get() + 1);
                    easterEgg(easterEggCounter.get());
                }
            } else if (itemId == R.id.navigation_calendar) {
                if (!(currentFragment instanceof CalendarFragment)) {
                    currentFragment = new CalendarFragment();
                    swappedFragment = true;
                }
            } else if (itemId == R.id.navigation_account) {
                if (!(currentFragment instanceof AccountFragment)) {
                    currentFragment = new AccountFragment();
                    swappedFragment = true;
                }
            }

            if (swappedFragment) {
                getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                replaceFragment(currentFragment);
            }

            return true;
        });
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public ActivityResultLauncher<Intent> fragmentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();

                    if (data != null && data.hasExtra(this.getClass().getSimpleName())) {
                        String fragmentClassName = data.getStringExtra(this.getClass().getSimpleName());

                        if (fragmentClassName != null) {
                            try {
                                Fragment fragment = (Fragment) Class.forName(fragmentClassName).newInstance();
                                fragment.setArguments(data.getExtras());

                                replaceFragment(fragment);
                            } catch (IllegalAccessException | InstantiationException | ClassNotFoundException exception) {
                                ExceptionManager.advertiseUI(this, exception.getMessage());
                            }
                        }
                    }
                }
            });

    private void showMedicineFragmentFromNotification(String fragmentClassName) {
        try {
            Fragment fragment = (Fragment) Class.forName(fragmentClassName).newInstance();
            NotificationRepository notificationRepository = new NotificationRepository(this);
            MedicationNotification medicationNotification = (MedicationNotification) notificationRepository.findById(getIntent().getLongExtra(MedicationNotification.class.getSimpleName(), -1));

            if (medicationNotification != null) {
                sessionViewModel.setUserSession(medicationNotification.getMedicine().getTreatment().getUser());

                Fragment treatmentsFragment = new TreatmentsFragment();
                replaceFragment(treatmentsFragment);

                Fragment treatmentFragment = new TreatmentFragment();
                Bundle bundle = new Bundle();
                bundle.putLong(Treatment.class.getSimpleName(), medicationNotification.getMedicine().getTreatment().getId());
                treatmentFragment.setArguments(bundle);
                replaceFragment(treatmentFragment);

                Fragment medicinesFragment = new MedicinesFragment();
                bundle = new Bundle();
                bundle.putLong(Treatment.class.getSimpleName(), medicationNotification.getMedicine().getTreatment().getId());
                medicinesFragment.setArguments(bundle);
                replaceFragment(medicinesFragment);

                bundle = new Bundle();
                bundle.putLong(Treatment.class.getSimpleName(), medicationNotification.getMedicine().getTreatment().getId());
                bundle.putLong(Medicine.class.getSimpleName(), medicationNotification.getMedicine().getId());
                fragment.setArguments(bundle);
                replaceFragment(fragment);
            }
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException | DBFindException exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }
    }

    public void setTreatmentLayoutStatus(Treatment treatment, ImageView imageViewStatus, TextView textViewStatus) {
        if (treatment.isPending()) {
            imageViewStatus.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_status_pending));
            imageViewStatus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            imageViewStatus.setContentDescription(getString(R.string.content_description_status_pending));
            textViewStatus.setText(getString(R.string.treatments_status_pending));
        } else if (treatment.isInProgress()) {
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            Fragment lastFragment = fragmentManager.findFragmentById(R.id.frameLayout);
            currentFragment = lastFragment;
            if (lastFragment != null) {
                getSupportFragmentManager().putFragment(outState, CURRENT_FRAGMENT, currentFragment);
            } else {
                currentFragment = new TreatmentFragment();
            }
        }
    }

    @Override
    public void onTitleChanged(String newTitle) {
        setToolbarTitle(newTitle);
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (!(currentFragment instanceof TreatmentsFragment || currentFragment instanceof CalendarFragment || currentFragment instanceof AccountFragment)) {
            super.onBackPressed();
        }
    }

    private void easterEgg(int easterEggCounter) {
        if (easterEggCounter == 5) {
            Toast.makeText(this, getString(R.string.easter_egg_5_message), Toast.LENGTH_SHORT).show();
        } else if (easterEggCounter == 10) {
            Toast.makeText(this, getString(R.string.easter_egg_10_message), Toast.LENGTH_SHORT).show();
        } else if (easterEggCounter == 15) {
            Toast.makeText(this, getString(R.string.easter_egg_15_message), Toast.LENGTH_SHORT).show();
        } else if (easterEggCounter == 20 ) {
            Toast.makeText(this, getString(R.string.easter_egg_20_message), Toast.LENGTH_SHORT).show();
        } else if (easterEggCounter == 25) {
            Toast.makeText(this, getString(R.string.easter_egg_25_message), Toast.LENGTH_SHORT).show();
        } else if (easterEggCounter > 25 && easterEggCounter % 10 == 0) {
            Toast.makeText(this, getString(R.string.easter_egg_30_message), Toast.LENGTH_SHORT).show();
        }
    }
}