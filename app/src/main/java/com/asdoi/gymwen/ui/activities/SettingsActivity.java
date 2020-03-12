package com.asdoi.gymwen.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.github.javiersantos.appupdater.enums.Display;

public class SettingsActivity extends ActivityFeatures implements ColorChooserDialog.ColorCallback, PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    public int loadedFragments = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        loadedFragments = 0;
    }

    public void setupColors() {
        setToolbar(true);
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
            p.setCourses(PreferenceManager.getDefaultSharedPreferences(this).getString("courses", p.getCourses()));
            ProfileManagement.editProfile(ApplicationFeatures.getSelectedProfilePosition(), p);
            ProfileManagement.save(true);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            loadedFragments--;
            try {
                getSupportActionBar().setTitle(R.string.title_activity_settings);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
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
            getSupportActionBar().setTitle(pref.getTitle());
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
                break;
        }
        editor.apply();
        dialog.dismiss();
        recreate();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
        dialog.dismiss();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_root, rootKey);

            ((SettingsActivity) getActivity()).loadedFragments++;

            Preference myPref = findPreference("language");
            myPref.setOnPreferenceClickListener((Preference p) -> {
                ApplicationFeatures.getLanguageSwitcher().showChangeLanguageDialog(getActivity());
                return true;
            });
            myPref.setSummary(ApplicationFeatures.getLanguageSwitcher().getCurrentLocale().toString());

            myPref = findPreference("updates");
            myPref.setOnPreferenceClickListener((Preference p) -> {
                ((ActivityFeatures) getActivity()).checkUpdates(Display.DIALOG, true);
                return true;
            });
        }
    }
}