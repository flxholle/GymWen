package com.asdoi.gymwen.ui.fragments;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;

public class SettingsFragmentDesign extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_design, rootKey);

        ListPreference mp = findPreference("theme");
        mp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mp.setValue(newValue + "");
                getActivity().recreate();
                return false;
            }
        });

        Preference myPref;
        if (ApplicationFeatures.isBetaEnabled()) {
            myPref = findPreference("primaryColor");
            myPref.setVisible(true);
            myPref.setOnPreferenceClickListener((Preference p) -> {
                new ColorChooserDialog.Builder(getContext(), R.string.color_primary)
                        .accentMode(false)
                        .allowUserColorInput(true)
                        .allowUserColorInputAlpha(false)
                        .show(getActivity());
                return true;
            });

            myPref = findPreference("accentColor");
            myPref.setVisible(true);
            myPref.setOnPreferenceClickListener((Preference p) -> {
                new ColorChooserDialog.Builder(getContext(), R.string.color_accent)
                        .accentMode(false)
                        .allowUserColorInput(true)
                        .allowUserColorInputAlpha(false)
                        .show(getActivity());
                return true;
            });
        } else {
            myPref = findPreference("primaryColor");
            myPref.setVisible(false);
            myPref = findPreference("accentColor");
            myPref.setVisible(false);
        }
    }
}
