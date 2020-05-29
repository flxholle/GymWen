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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.util.PreferenceUtil;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class SignInActivity extends ActivityFeatures implements View.OnClickListener {
    private ViewGroup loading;
    private Button signInButton;
    private String username;
    private String password;
    private boolean autoUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        signInButton = findViewById(R.id.signin_login);
        signInButton.setOnClickListener(this);
        signInButton.setEnabled(true);
        loading = findViewById(R.id.signIn_loading);
        loading.setVisibility(View.INVISIBLE);

        if (signedInBefore()) {
            Intent intent = new Intent(this, ChoiceActivity.class);
            startActivity(intent);
            finish();
        }

        findViewById(R.id.signin_url_settings_button).setOnClickListener((View v) -> {
            Intent i = new Intent(this, SettingsActivity.class);
            i.setAction(SettingsActivity.SIGN_IN_SETTINGS);
            startActivity(i);
        });
    }

    public void setupColors() {
        setToolbar(false);
    }

    @Override
    public void onClick(View v) {
        try {
            username = ((EditText) findViewById(R.id.signin_username)).getText().toString();
            password = ((EditText) findViewById(R.id.signin_password)).getText().toString();
            autoUpdate = ((CheckBox) findViewById(R.id.signin_auto_update_check_box)).isChecked();
        } catch (Exception e) {
            e.printStackTrace();
        }
        signInButton.setEnabled(false);
        loading.setVisibility(View.VISIBLE);
        checkData(username, password);
    }


    private void checkData(final String username, final String password) {
        if (!ApplicationFeatures.isNetworkAvailable()) {
            runOnUiThread(() -> {
                Toast.makeText(getApplicationContext(), getString(R.string.noInternetConnection), Toast.LENGTH_SHORT).show();
                loading.setVisibility(View.INVISIBLE);
                signInButton.setEnabled(true);
            });
            return;
        }
        (new Thread(new Runnable() {
            boolean signedIn;
            @Nullable
            Document doc = null;

            @Override
            public void run() {
                String authString = username + ":" + password;
                String encodedString =
                        new String(Base64.encodeBase64(authString.getBytes()));
                try {
                    doc = Jsoup.connect(PreferenceUtil.getTodayURL(requireContext()))
                            .header("Authorization", "Basic " + encodedString)
                            .get();
                    signedIn = true;
                } catch (Exception e) {
                    signedIn = false;
                    e.getStackTrace();
                }
                signIn(signedIn);
            }
        })).start();
    }

    private void signIn(boolean successful) {
        if (successful)
            signInSuccess();
        else
            signInFailure();
    }

    private void signInSuccess() {
        runOnUiThread(() -> {
            loading.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), R.string.login_success, Toast.LENGTH_SHORT).show();
            setSettings(username, password, autoUpdate);
        });
        Intent intent = new Intent(this, ChoiceActivity.class);
        startActivity(intent);
        finish();
    }

    private void signInFailure() {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
            loading.setVisibility(View.INVISIBLE);
            signInButton.setEnabled(true);
//                ((EditText) findViewById(R.id.signin_username)).setText("");
            ((EditText) findViewById(R.id.signin_password)).setText("");
        });
    }

    private void setSettings(String username, String password, boolean autoUpdate) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("username", username);
        editor.putString("password", password);
        editor.putBoolean("signed", true);
        editor.putBoolean("auto_update", autoUpdate);
        editor.apply();
    }

    private boolean signedInBefore() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getBoolean("signed", false);
    }


    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

}
