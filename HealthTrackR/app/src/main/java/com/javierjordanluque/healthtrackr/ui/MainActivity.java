package com.javierjordanluque.healthtrackr.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.databinding.ActivityMainBinding;
import com.javierjordanluque.healthtrackr.ui.account.AccountFragment;
import com.javierjordanluque.healthtrackr.ui.calendar.CalendarFragment;
import com.javierjordanluque.healthtrackr.ui.home.HomeFragment;
import com.javierjordanluque.healthtrackr.ui.treatments.TreatmentsFragment;

public class MainActivity extends BaseActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpToolbar(getString(R.string.home_title));
        replaceFragment(new HomeFragment());

        binding.navigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.navigation_treatments) {
                replaceFragment(new TreatmentsFragment());
            } else if (itemId == R.id.navigation_calendar) {
                replaceFragment(new CalendarFragment());
            } else if (itemId == R.id.navigation_account) {
                replaceFragment(new AccountFragment());
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }
}