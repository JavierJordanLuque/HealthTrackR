package com.javierjordanluque.healthtrackr.ui;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.databinding.ActivityMainBinding;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.ui.account.AccountFragment;
import com.javierjordanluque.healthtrackr.ui.calendar.CalendarFragment;
import com.javierjordanluque.healthtrackr.ui.treatments.TreatmentsFragment;

public class MainActivity extends BaseActivity implements OnToolbarChangeListener {
    ActivityMainBinding binding;
    private Fragment currentFragment;
    private final String CURRENT_FRAGMENT = "currentFragment";
    public static final String FRAGMENT_ID = "fragmentId";
    public static final int TREATMENTS_FRAGMENT_ID = 1;
    public static final int CALENDAR_FRAGMENT_ID = 2;
    public static final int ACCOUNT_FRAGMENT_ID = 3;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = getIntent().getParcelableExtra(User.class.getSimpleName());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpToolbar(getString(R.string.treatments_title));
        showBackButton(false);

        if (savedInstanceState != null) {
            currentFragment = getSupportFragmentManager().getFragment(savedInstanceState, CURRENT_FRAGMENT);
        } else {
            int fragmentId = getIntent().getIntExtra(FRAGMENT_ID, TREATMENTS_FRAGMENT_ID);
            switch (fragmentId) {
                case CALENDAR_FRAGMENT_ID:
                    currentFragment = new CalendarFragment();
                    break;
                case ACCOUNT_FRAGMENT_ID:
                    currentFragment = new AccountFragment();
                    break;
                default:
                    currentFragment = new TreatmentsFragment();
                    break;
            }
        }
        replaceFragment(currentFragment);

        int selectedItem;
        if (currentFragment instanceof CalendarFragment) {
            selectedItem = R.id.navigation_calendar;
        } else if (currentFragment instanceof AccountFragment) {
            selectedItem = R.id.navigation_account;
        } else {
            selectedItem = R.id.navigation_treatments;
        }
        binding.navigationView.setSelectedItemId(selectedItem);

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
        Bundle bundle = new Bundle();
        bundle.putParcelable(User.class.getSimpleName(), user);
        fragment.setArguments(bundle);

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
    protected User getUser() {
        return user;
    }

    @Override
    protected void handleBackButtonAction() {
    }
}