package com.asdoi.gymwen.ui.main.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * @author Karim Abou Zeid (kabouzeid) from VinylMusicPlayer
 */
@SuppressWarnings("FieldCanBeLocal")
public class AboutActivity extends ActivityFeatures implements View.OnClickListener {

    private static String GITLAB = "https://gitlab.com/asdoi/GymWen/";

    private static String WEBSITE = "https://asdoi.gitlab.io/";

    private static String BUGSITE = "https://gitlab.com/asdoi/GymWen/issues";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.app_version)
    TextView appVersion;
    @BindView(R.id.changelog)
    LinearLayout changelog;
    @BindView(R.id.intro)
    LinearLayout intro;
    @BindView(R.id.licenses)
    LinearLayout licenses;
    @BindView(R.id.write_an_email)
    LinearLayout writeAnEmail;
    @BindView(R.id.fork_on_github)
    LinearLayout forkOnGitHub;
    @BindView(R.id.visit_website)
    LinearLayout visitWebsite;
    @BindView(R.id.report_bugs)
    LinearLayout reportBugs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
//        setDrawUnderStatusbar();


//        setStatusbarColorAuto();
//        setNavigationbarColorAuto();
//        setTaskDescriptionColorAuto();


    }

    @Override
    public void onStart() {
        super.onStart();
        ButterKnife.bind(this);
        setUpViews();
    }

    private void setUpViews() {
        setUpToolbar();
        setUpAppVersion();
        setUpOnClickListeners();
    }

    private void setUpToolbar() {
//        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpAppVersion() {
        appVersion.setText(getCurrentVersionName(this));
    }

    private void setUpOnClickListeners() {
        changelog.setOnClickListener(this);
        intro.setOnClickListener(this);
        licenses.setOnClickListener(this);
        forkOnGitHub.setOnClickListener(this);
        visitWebsite.setOnClickListener(this);
        reportBugs.setOnClickListener(this);
        writeAnEmail.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static String getCurrentVersionName(@NonNull final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "Unkown";
    }

    @Override
    public void onClick(View v) {
        if (v == changelog) {
            showChangelogCK(false);
        } else if (v == licenses) {
            showLicenseDialog();
        } else if (v == intro) {
//            startActivity(new Intent(this, AppIntroActivity.class));
        } else if (v == forkOnGitHub) {
            tabIntent(GITLAB);
        } else if (v == visitWebsite) {
            tabIntent(WEBSITE);
        } else if (v == reportBugs) {
            tabIntent(BUGSITE);
        } else if (v == writeAnEmail) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:GymWenApp@t-online.de"));
            intent.putExtra(Intent.EXTRA_EMAIL, "GymWenApp@t-online.de");
            intent.putExtra(Intent.EXTRA_SUBJECT, "GymWenApp");
            startActivity(Intent.createChooser(intent, "E-Mail"));
        }
    }


    private void showLicenseDialog() {
//        new LicensesDialog.Builder(this)
//                .setNotices(R.raw.notices)
//                .setTitle(R.string.licenses)
//                .setNoticesCssStyle(getString(R.string.license_dialog_style)
//                        .replace("{bg-color}", ThemeSingleton.get().darkTheme ? "424242" : "ffffff")
//                        .replace("{text-color}", ThemeSingleton.get().darkTheme ? "ffffff" : "000000")
//                        .replace("{license-bg-color}", ThemeSingleton.get().darkTheme ? "535353" : "eeeeee")
//                )
//                .setIncludeOwnLicense(true)
//                .build()
//                .show();
    }
}
