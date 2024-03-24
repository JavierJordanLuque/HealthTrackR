package com.javierjordanluque.healthtrackr.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.javierjordanluque.healthtrackr.R;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        MaterialToolbar toolbar = (MaterialToolbar) appBarLayout.getChildAt(0);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(getString(R.string.authentication_sign_up));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_basic_menu, menu);
        return true;
    }
}