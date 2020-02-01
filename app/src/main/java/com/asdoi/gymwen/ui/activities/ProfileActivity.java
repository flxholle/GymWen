package com.asdoi.gymwen.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

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
        setToolbar(true);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(ApplicationFeatures.getAccentColor(this)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ApplicationFeatures.resetSelectedProfile();
                ProfileManagement.save(true);
                Intent i = new Intent(ApplicationFeatures.getContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        ApplicationFeatures.resetSelectedProfile();
        ProfileManagement.save(true);
        Intent i = new Intent(ApplicationFeatures.getContext(), MainActivity.class);
        startActivity(i);
        finish();
    }
}
