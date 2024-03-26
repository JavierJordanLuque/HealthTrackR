package com.javierjordanluque.healthtrackr.ui;

import android.os.Bundle;

import com.javierjordanluque.healthtrackr.R;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_home;
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.home_home);
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }
}