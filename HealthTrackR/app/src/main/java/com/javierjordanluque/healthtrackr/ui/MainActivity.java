package com.javierjordanluque.healthtrackr.ui;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.databinding.ActivityMainBinding;
import com.javierjordanluque.healthtrackr.ui.account.AccountFragment;
import com.javierjordanluque.healthtrackr.ui.calendar.CalendarFragment;
import com.javierjordanluque.healthtrackr.ui.treatments.TreatmentsFragment;

public class MainActivity extends BaseActivity implements OnToolbarChangeListener {
    ActivityMainBinding binding;
    private Fragment currentFragment;
    private final String CURRENT_FRAGMENT = "currentFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpToolbar(getString(R.string.treatments_title));
        showBackButton(false);

        if (savedInstanceState != null) {
            currentFragment = getSupportFragmentManager().getFragment(savedInstanceState, CURRENT_FRAGMENT);
        } else {
            currentFragment = new TreatmentsFragment();
        }
        replaceFragment(currentFragment);

        binding.navigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_treatments) {
                currentFragment = new TreatmentsFragment();
            } else if (itemId == R.id.navigation_calendar) {
                currentFragment = new CalendarFragment();
            } else if (itemId == R.id.navigation_account) {
                currentFragment = new AccountFragment();
            }
            replaceFragment(currentFragment);

            return true;
        });

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fragmentManager = getSupportFragmentManager();
                int backStackEntryCount = fragmentManager.getBackStackEntryCount();
                if (backStackEntryCount > 0) {
                    FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(backStackEntryCount - 1);
                    String fragmentTag = backStackEntry.getName();
                    Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
                    if (fragment != null) {
                        replaceFragment(fragment);
                        fragmentManager.popBackStack();
                        return;
                    }
                }
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, CURRENT_FRAGMENT, currentFragment);
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
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
    protected void handleBackButtonAction() {
    }
}