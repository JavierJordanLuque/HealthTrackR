package com.javierjordanluque.healthtrackr.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.databinding.ActivityMainBinding;
import com.javierjordanluque.healthtrackr.ui.account.AccountFragment;
import com.javierjordanluque.healthtrackr.ui.calendar.CalendarFragment;
import com.javierjordanluque.healthtrackr.ui.treatments.TreatmentFragment;
import com.javierjordanluque.healthtrackr.ui.treatments.TreatmentsFragment;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends BaseActivity implements OnToolbarChangeListener {
    ActivityMainBinding binding;
    private final String CURRENT_FRAGMENT = "currentFragment";
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
            currentFragment = new TreatmentsFragment();
            replaceFragment(currentFragment);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
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