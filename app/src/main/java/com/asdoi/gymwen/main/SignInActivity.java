package com.asdoi.gymwen.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.VertretungsplanInternal.VertretungsPlan;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    ProgressDialog progDialog;
    Button signInButton;
    String username;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        signInButton = findViewById(R.id.signin_login);
        signInButton.setOnClickListener(this);
        signInButton.setEnabled(true);

        if (signedInBefore()) {
            Intent intent = new Intent(this, ChoiceActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        try {
            username = ((EditText) findViewById(R.id.signin_username)).getText().toString();
            password = ((EditText) findViewById(R.id.signin_password)).getText().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        signInButton.setEnabled(false);
        progDialog = ProgressDialog.show(this, "Bitte Warten", "Anmeldedaten werden geprüft", true);
        progDialog.setCancelable(false);
        checkData(username, password);
    }


    private void checkData(final String username, final String password) {
        (new Thread(new Runnable() {
            boolean signedIn;
            Document doc = null;

            @Override
            public void run() {
                String authString = username + ":" + password;
                String encodedString =
                        new String(Base64.encodeBase64(authString.getBytes()));
                try {
                    doc = Jsoup.connect(VertretungsPlan.todayURL)
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

    void signIn(boolean successful) {
        progDialog.dismiss();
//        if(successful)
        signInSuccess();
//        else
//            signInFailure();
    }

    void signInSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), R.string.login_success, Toast.LENGTH_SHORT).show();
                setSettings(username, password);
            }
        });
        Intent intent = new Intent(this, ChoiceActivity.class);
        startActivity(intent);
        finish();
    }

    void signInFailure() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
                signInButton.setEnabled(true);
//                ((EditText) findViewById(R.id.signin_username)).setText("");
                ((EditText) findViewById(R.id.signin_password)).setText("");
            }
        });
    }

    void setSettings(String username, String password) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("username", username);
        editor.putString("password", password);
        editor.putBoolean("signed", true);
        editor.commit();
    }

    boolean signedInBefore() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getBoolean("signed", false);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

}
