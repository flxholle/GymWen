package com.asdoi.gymwen.ui.settingsFragments;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.SettingsActivity;

import java.util.Objects;

public class SettingsFragmentSubstitution extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_substitutionplan, rootKey);

        ((SettingsActivity) Objects.requireNonNull(getActivity())).loadedFragments++;

        setFullNames();
        Preference myPref = findPreference("show_full_names");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference preference) -> {
            setFullNames();
            return true;
        });

        setSummarize();
        myPref = findPreference("summarize");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference pref) -> {
            setSummarize();
            return true;
        });
    }

    private void setFullNames() {
        boolean showNotif = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("show_full_names", false) && !PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("hide_gesamt", false);
        Objects.requireNonNull(findPreference("show_full_names_specific")).setEnabled(showNotif);
    }

    private void setSummarize() {
        boolean showNotif = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("summarize", true) && !PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).getBoolean("hide_gesamt", false);
        Objects.requireNonNull(findPreference("summarize_old")).setEnabled(showNotif);
    }
}
