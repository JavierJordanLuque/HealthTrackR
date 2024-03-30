package com.javierjordanluque.healthtrackr.ui;

import android.os.Bundle;

import com.javierjordanluque.healthtrackr.R;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.home_title);
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }
}