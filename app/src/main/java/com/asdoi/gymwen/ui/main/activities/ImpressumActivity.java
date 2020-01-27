package com.asdoi.gymwen.ui.main.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.main.fragments.ContributionFragment;
import com.asdoi.gymwen.ui.main.fragments.ImpressumFragment;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

public class ImpressumActivity extends ActivityFeatures implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impressum);
        if (getSupportActionBar() != null)   //null check
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show backgroundShape button
    }

    @Override
    protected void onStart() {
        super.onStart();

        findViewById(R.id.OnlinePrivacy).setOnClickListener(this);
        findViewById(R.id.OnlinePrivacy).setOnClickListener(this);
    }

    boolean contribution = false;

    @Override
    public void onBackPressed() {
        if (contribution) {
            contribution = false;
            try {
                getSupportActionBar().setTitle(getString(R.string.impressum_headline));
            } catch (Exception e) {
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.impressumFragment, new ImpressumFragment()).commit();
            return;
        }
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.AboutLibs:
            case R.id.AboutLibsImageButton:
                Intent intent = new LibsBuilder()
                        .withActivityTitle(getString(R.string.impressum_AboutLibs_Title))
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
            case R.id.OnlinePrivacyImageButton:
                tabIntent("http://www.gym-wen.de/startseite/impressum/");
                break;
            case R.id.SourceCode:
            case R.id.SourceCodeImageButton:
                tabIntent("https://gitlab.com/asdoi/GymWen");
                break;
            case R.id.shareApp:
                share();
                break;
            case R.id.Attribution:
            case R.id.AttributionImageButton:
                contribution = true;
                try {
                    getSupportActionBar().setTitle(getString(R.string.impressum_attribution));
                } catch (Exception e) {
                }
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.impressumFragment, new ContributionFragment()).commit();
                break;
        }
    }

    private void share() {
        String link = "https://gitlab.com/asdoi/gymwenreleases/blob/master/GymWenApp.apk";
        String message = getString(R.string.share_app_message) + " " + link;
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, message);
        i.setType("text/plain");
        startActivity(Intent.createChooser(i, getString(R.string.share_app)));
    }
}
