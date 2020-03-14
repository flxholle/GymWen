package com.asdoi.gymwen.ui.settingsFragments;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.SettingsActivity;

import java.util.Objects;

public class SettingsFragmentHideMenuItems extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_hide_menu_items, rootKey);

        ((SettingsActivity) Objects.requireNonNull(getActivity())).loadedFragments++;

        Preference myPref = findPreference("show_sections");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference preference) -> {
            boolean checked = ((SwitchPreference) preference).isChecked();
            SwitchPreference days = findPreference("show_days");
            Objects.requireNonNull(days).setChecked(!checked);

            days = findPreference("show_no_sections");
            Objects.requireNonNull(days).setChecked(false);

            showBoth();
            checkFilteredUnfiltered();
            return true;
        });

        myPref = findPreference("show_no_sections");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference preference) -> {
            SwitchPreference days = findPreference("show_days");
            Objects.requireNonNull(days).setChecked(!((SwitchPreference) preference).isChecked());

            days = findPreference("show_sections");
            Objects.requireNonNull(days).setChecked(false);

            showBoth();
            checkFilteredUnfiltered();
            return true;
        });

        showBoth();
        checkFilteredUnfiltered();
        myPref = findPreference("menu_filtered");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference preference) -> {
            setFilteredUnfiltered();
            return true;
        });

        myPref = findPreference("menu_unfiltered");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference preference) -> {
            setFilteredUnfiltered();
            return true;
        });


        myPref = findPreference("show_days");
        Objects.requireNonNull(myPref).setOnPreferenceClickListener((Preference pref) -> {
            SwitchPreference separate = findPreference("show_sections");
            Objects.requireNonNull(separate).setChecked(!((SwitchPreference) pref).isChecked());

            SwitchPreference days = findPreference("show_no_sections");
            Objects.requireNonNull(days).setChecked(false);

            showBoth();
            checkFilteredUnfiltered();
            return true;
        });
    }

    private void setFilteredUnfiltered() {
        SwitchPreference switchPreference = findPreference("show_sections");
        boolean showBothFalse = !((SwitchPreference) Objects.requireNonNull(findPreference("menu_filtered"))).isChecked() && !((SwitchPreference) Objects.requireNonNull(findPreference("menu_unfiltered"))).isChecked();
        Objects.requireNonNull(switchPreference).setChecked(!showBothFalse);
        showBoth();
        if (showBothFalse)
            ((SwitchPreference) Objects.requireNonNull(findPreference("show_days"))).setChecked(true);
    }

    private void checkFilteredUnfiltered() {
        boolean bothFalse = !((SwitchPreference) Objects.requireNonNull(findPreference("menu_filtered"))).isChecked() && !((SwitchPreference) Objects.requireNonNull(findPreference("menu_unfiltered"))).isChecked();
        if (bothFalse)
            ((SwitchPreference) Objects.requireNonNull(findPreference("menu_filtered"))).setChecked(true);
    }

    private void showBoth() {
        SwitchPreference switchPreference = findPreference("show_sections");
        boolean showBoth = Objects.requireNonNull(switchPreference).isChecked();
        Objects.requireNonNull(findPreference("menu_filtered")).setEnabled(showBoth);
        Objects.requireNonNull(findPreference("menu_unfiltered")).setEnabled(showBoth);
    }

}