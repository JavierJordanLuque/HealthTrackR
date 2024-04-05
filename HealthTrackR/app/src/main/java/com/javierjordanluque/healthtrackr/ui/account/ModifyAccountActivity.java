package com.javierjordanluque.healthtrackr.ui.account;

import android.os.Bundle;

import androidx.activity.OnBackPressedDispatcher;

import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.ui.BaseActivity;

public class ModifyAccountActivity extends BaseActivity {
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_account);
        setUpToolbar(getString(R.string.account_modify_title));
        showBackButton(true);
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }

    @Override
    protected void handleBackButtonAction() {
        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.onBackPressed();
    }
}