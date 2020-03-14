package com.asdoi.gymwen.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProfileActivity extends ActivityFeatures {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(ApplicationFeatures.getAccentColor(this)));
        fab.setOnClickListener(view -> onBackPressed());
    }

    public void setupColors() {
        setToolbar(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        ApplicationFeatures.resetSelectedProfile();
        ProfileManagement.save(true);
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
