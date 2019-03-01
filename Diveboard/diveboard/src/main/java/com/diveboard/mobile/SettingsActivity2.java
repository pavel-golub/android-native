package com.diveboard.mobile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity2 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_dive);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.pref_container, new SettingsFragment())
                .commit();
    }
}