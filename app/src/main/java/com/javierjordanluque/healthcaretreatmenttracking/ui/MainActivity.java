package com.javierjordanluque.healthcaretreatmenttracking.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.javierjordanluque.healthcaretreatmenttracking.R;
import com.javierjordanluque.healthcaretreatmenttracking.db.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}