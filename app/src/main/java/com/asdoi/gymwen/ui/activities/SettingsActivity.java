/*
 * Copyright (c) 2020 Felix Hollederer
 *     This file is part of GymWenApp.
 *
 *     GymWenApp is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GymWenApp is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GymWenApp.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.asdoi.gymwen.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.Profile;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.ui.settingsFragments.SettingsFragmentRoot;
import com.asdoi.gymwen.ui.settingsFragments.SettingsFragmentSignIn;
import com.asdoi.gymwen.util.ShortcutUtils;

import java.util.Objects;

public class SettingsActivity extends ActivityFeatures implements ColorChooserDialog.ColorCallback, PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    public static final String SIGN_IN_SETTINGS = "SignInSettings";

    public int loadedFragments = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragmentRoot())
                .commit();
        loadedFragments = 0;
    }

    public void setupColors() {
        setToolbar(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent i = getIntent();
        if (i != null && i.getAction() != null) {
            switch (i.getAction()) {
                case SIGN_IN_SETTINGS:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.settings, new SettingsFragmentSignIn())
                            .commit();
                    break;
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (loadedFragments == 1) {
            Profile p = ApplicationFeatures.getSelectedProfile();
            String newCourses = PreferenceManager.getDefaultSharedPreferences(this).getString("courses", p.getCourses());
            if (!newCourses.trim().isEmpty()) {
                p.setCourses(newCourses);
                ProfileManagement.editProfile(ApplicationFeatures.getSelectedProfilePosition(), p);
                ProfileManagement.save(true);
            }

            if (Build.VERSION.SDK_INT >= 25) {
                try {
                    ShortcutUtils.Companion.createShortcuts();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            loadedFragments--;
            try {
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.title_activity_settings);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, @NonNull Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();

        try {
            Objects.requireNonNull(getSupportActionBar()).setTitle(pref.getTitle());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        Context context = this;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        switch (dialog.getTitle()) {
            case R.string.color_accent:
                editor.putInt("colorAccent", selectedColor);
                break;
            case R.string.color_primary:
                editor.putInt("colorPrimary", selectedColor);
                recreate();
                break;
        }
        editor.apply();
        dialog.dismiss();
//        recreate(); //Not necessary because SettingsActivity isn't themed
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
        dialog.dismiss();
    }
}