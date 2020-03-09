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

        Preference myPref = findPreference("show_sections");
        myPref.setOnPreferenceClickListener((Preference preference) -> {
            SwitchPreference days = findPreference("show_days");
            days.setChecked(!((SwitchPreference) preference).isChecked());
            showBoth();
            checkFilteredUnfiltered();
            return true;
        });

        showBoth();
        checkFilteredUnfiltered();
        setFilteredUnfiltered();
        myPref = findPreference("menu_filtered");
        myPref.setOnPreferenceClickListener((Preference preference) -> {
            setFilteredUnfiltered();
            return true;
        });

        myPref = findPreference("menu_unfiltered");
        myPref.setOnPreferenceClickListener((Preference preference) -> {
            setFilteredUnfiltered();
            return true;
        });


        myPref = findPreference("show_days");
        myPref.setOnPreferenceClickListener((Preference pref) -> {
            SwitchPreference separate = findPreference("show_sections");
            separate.setChecked(!((SwitchPreference) pref).isChecked());
            showBoth();
            checkFilteredUnfiltered();
            return true;
        });
    }

    private void setFilteredUnfiltered() {
        SwitchPreference switchPreference = findPreference("show_sections");
        boolean showBothFalse = !((SwitchPreference) findPreference("menu_filtered")).isChecked() && !((SwitchPreference) findPreference("menu_unfiltered")).isChecked();
        switchPreference.setChecked(!showBothFalse);
        showBoth();
        if (showBothFalse)
            ((SwitchPreference) findPreference("show_days")).setChecked(true);
    }

    private void checkFilteredUnfiltered() {
        boolean bothFalse = !((SwitchPreference) findPreference("menu_filtered")).isChecked() && !((SwitchPreference) findPreference("menu_unfiltered")).isChecked();
        if (bothFalse)
            ((SwitchPreference) findPreference("menu_filtered")).setChecked(true);
    }

    private void showBoth() {
        SwitchPreference switchPreference = findPreference("show_sections");
        boolean showBoth = switchPreference.isChecked();
        findPreference("menu_filtered").setEnabled(showBoth);
        findPreference("menu_unfiltered").setEnabled(showBoth);
    }

}