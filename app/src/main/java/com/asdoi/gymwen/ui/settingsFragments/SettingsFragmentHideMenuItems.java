package com.asdoi.gymwen.ui.settingsFragments;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.SettingsActivity;

public class SettingsFragmentHideMenuItems extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_hide_menu_items, rootKey);

        ((SettingsActivity) getActivity()).loadedFragments++;

        setBoth();
        Preference myPref = findPreference("menu_filtered");
        myPref.setOnPreferenceClickListener((Preference preference) -> {
            setFilteredUnfiltered();
            return true;
        });

        myPref = findPreference("menu_unfiltered");
        myPref.setOnPreferenceClickListener((Preference preference) -> {
            setFilteredUnfiltered();
            return true;
        });

        myPref = findPreference("show_sections");
        myPref.setOnPreferenceClickListener((Preference preference) -> {
            setBoth();
            return true;
        });
    }

    private void setFilteredUnfiltered() {
        SwitchPreference switchPreference = findPreference("show_sections");
        boolean showSum = !((SwitchPreference) findPreference("menu_filtered")).isChecked() && !((SwitchPreference) findPreference("menu_unfiltered")).isChecked();
        switchPreference.setChecked(!showSum);
    }

    private void setBoth() {
        SwitchPreference switchPreference = findPreference("show_sections");
        boolean showBoth = switchPreference.isChecked();
        findPreference("menu_filtered").setEnabled(showBoth);
        findPreference("menu_unfiltered").setEnabled(showBoth);

        boolean showSum = !((SwitchPreference) findPreference("menu_filtered")).isChecked() && !((SwitchPreference) findPreference("menu_unfiltered")).isChecked();
        if (showSum && showBoth)
            ((SwitchPreference) findPreference("menu_filtered")).setChecked(true);
    }
}