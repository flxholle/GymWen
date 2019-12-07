package com.asdoi.gymwen.main;

import android.os.Bundle;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;

import androidx.appcompat.widget.Toolbar;

public class AboutAuthorActivity extends ActivityFeatures {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_author);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
