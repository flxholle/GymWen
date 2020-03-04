package com.asdoi.gymwen.ui.fragments;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.util.PreferenceUtil;

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
        if (PreferenceUtil.isBetaEnabled()) {
            myPref = findPreference("primaryColor");
            myPref.setVisible(true);
            myPref.setOnPreferenceClickListener((Preference p) -> {
                new ColorChooserDialog.Builder(getContext(), R.string.color_primary)
                        .accentMode(false)
                        .allowUserColorInput(true)
                        .allowUserColorInputAlpha(false)
                        .preselect(ApplicationFeatures.getPrimaryColor(getContext()))
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
                        .preselect(ApplicationFeatures.getAccentColor(getContext()))
                        .show(getActivity());
                return true;
            });
        } else {
            myPref = findPreference("primaryColor");
            myPref.setVisible(false);
            myPref = findPreference("accentColor");
            myPref.setVisible(false);
        }

        setBorder();
        myPref = findPreference("show_borders");
        myPref.setOnPreferenceClickListener((Preference preference) -> {
            setBorder();
            return true;
        });
    }

    private void setBorder() {
        boolean showNotif = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("show_borders", false) && !PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("hide_gesamt", false);
        findPreference("show_border_specific").setEnabled(showNotif);
    }
}
