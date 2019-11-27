package com.asdoi.gymwen.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.preference.PreferenceFragmentCompat;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.vertretungsplanInternal.VertretungsPlan;

public class SettingsActivity extends ActivityFeatures implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setSettings();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setSettings();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

    @Override
    public void onClick(View v){
//        if(v.getId() == R.id.settings_choice){
//            initSettings();
//            Intent i = new Intent(this, ChoiceActivity.class);
//            startActivity(i);
//            return;
//        }
    }

    public void setSettings() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean oberstufe = sharedPref.getBoolean("oberstufe", false);
        String courses = sharedPref.getString("courses", "");
        if (courses.trim().isEmpty()) {
            Intent i = new Intent(this, ChoiceActivity.class);
            startActivity(i);
            return;
        }
        oberstufe = courses.split("#").length > 1;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("oberstufe", oberstufe);
        editor.commit();

        VertretungsPlan.setup(oberstufe, courses.split("#"), courses);

        String username = sharedPref.getString("username", "");
        String password = sharedPref.getString("password", "");
        VertretungsPlan.signin(username, password);

        System.out.println("settings: " + oberstufe + courses);
//        System.out.println("login " + username + password + "real " + VertretungsPlan.strUserId + VertretungsPlan.strPasword);
    }
}