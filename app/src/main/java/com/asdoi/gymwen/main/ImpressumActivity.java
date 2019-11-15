package com.asdoi.gymwen.main;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.asdoi.gymwen.R;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import saschpe.android.customtabs.CustomTabsHelper;
import saschpe.android.customtabs.WebViewFallback;

public class ImpressumActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impressum);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
    }

    @Override
    protected void onStart(){
        super.onStart();

        findViewById(R.id.AboutLibs).setOnClickListener(this);
        findViewById(R.id.OnlinePrivacy).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.AboutLibs:
                Intent intent = new LibsBuilder()
                        .withActivityTitle(getString(R.string.AboutLibs_Title))
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withFields(R.string.class.getFields())
                        .withAutoDetect(true)
                        .withAboutIconShown(true)
                        .withLicenseShown(true)
                        .withAboutDescription(getString(R.string.subtitle))
                        .withAboutAppName(getString(R.string.app_name))
                        .intent(this);

                startActivity(intent);
                break;
            case R.id.OnlinePrivacy:
                tabIntent("http://www.gym-wen.de/startseite/impressum/");
                break;
        }
    }

    private void tabIntent(String url) {
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .addDefaultShareMenuItem()
                    .setToolbarColor(getResources()
                            .getColor(R.color.colorPrimary))
                    .setShowTitle(true)
                    .setCloseButtonIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back_white_24dp))
                    .build();

            // This is optional but recommended
            CustomTabsHelper.addKeepAliveExtra(this, customTabsIntent.intent);

            // This is where the magic happens...
            CustomTabsHelper.openCustomTab(this, customTabsIntent,
                    Uri.parse(url),
                    new WebViewFallback());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
