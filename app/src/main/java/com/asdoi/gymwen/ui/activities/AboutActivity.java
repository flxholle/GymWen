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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * @author Karim Abou Zeid (kabouzeid) from VinylMusicPlayer
 */
@SuppressWarnings("FieldCanBeLocal")
public class AboutActivity extends ActivityFeatures implements View.OnClickListener {

    @NonNull
    private static final String GITLAB = "https://gitlab.com/asdoi/GymWen/";

    @NonNull
    private static final String WEBSITE = "https://asdoi.gitlab.io/";

    @NonNull
    private static final String BUGSITE = "https://gitlab.com/asdoi/GymWen/issues";

    @Nullable
    @BindView(R.id.app_version)
    TextView appVersion;
    @Nullable
    @BindView(R.id.share)
    LinearLayout share;
    @Nullable
    @BindView(R.id.changelog)
    LinearLayout changelog;
    @Nullable
    @BindView(R.id.intro)
    LinearLayout intro;
    @Nullable
    @BindView(R.id.fork_on_github)
    LinearLayout forkOnGitHub;
    @Nullable
    @BindView(R.id.privacy)
    LinearLayout privacy;
    @Nullable
    @BindView(R.id.licenses)
    LinearLayout licenses;
    @Nullable
    @BindView(R.id.image_sources)
    LinearLayout image_sources;
    @Nullable
    @BindView(R.id.libs)
    LinearLayout libs;

    @Nullable
    @BindView(R.id.write_an_email)
    LinearLayout writeAnEmail;
    @Nullable
    @BindView(R.id.visit_website)
    LinearLayout visitWebsite;
    @Nullable
    @BindView(R.id.colorush)
    LinearLayout colorush;

    @Nullable
    @BindView(R.id.report_bugs)
    LinearLayout reportBugs;

    @Nullable
    @BindView(R.id.imprint)
    LinearLayout imprint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.about_frame, new Fragment1()).commit();
//        setDrawUnderStatusbar();


//        setStatusbarColorAuto();
//        setNavigationbarColorAuto();
//        setTaskDescriptionColorAuto();


    }

    public void setupColors() {
        setToolbar(true);
    }

    public static class Fragment1 extends Fragment {
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.content_about, container, false);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        ButterKnife.bind(this);
        setUpViews();
    }

    private void setUpViews() {
        setUpAppVersion();
        setUpOnClickListeners();
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
        share.setOnClickListener(this);
        privacy.setOnClickListener(this);
        image_sources.setOnClickListener(this);
        libs.setOnClickListener(this);
        colorush.setOnClickListener(this);
        imprint.setOnClickListener(this);
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
            String license = getString(R.string.gnu_license);

            if (Build.VERSION.SDK_INT > 24)
                license = Html.fromHtml(license, Html.FROM_HTML_MODE_LEGACY).toString();
            else
                license = Html.fromHtml(license).toString();

            SpannableString s = new SpannableString(license);
            Linkify.addLinks(s, Linkify.WEB_URLS);

            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_description_white_24dp);
            try {
                Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(wrappedDrawable, ApplicationFeatures.getTextColorPrimary(getContext()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            new MaterialDialog.Builder(getContext())
                    .title(getString(R.string.licenses))
                    .content(s)

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .onPositive((dialog, which) -> dialog.dismiss())
                    .positiveText(R.string.ok)
                    .icon(drawable)
                    .show();
        } else if (v == intro) {
            startActivity(new Intent(this, AppIntroActivity.class));
            finish();
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
        } else if (v == colorush) {
            final String downloadSite = "https://gitlab.com/asdoi/colorrush/blob/master/Apk/ColoRush.apk";
            tabIntent(downloadSite);
        } else if (v == share) {
            share();
        } else if (v == privacy) {
            String datenschutz = getString(R.string.privacy);

            if (Build.VERSION.SDK_INT > 24)
                datenschutz = Html.fromHtml(datenschutz, Html.FROM_HTML_MODE_LEGACY).toString();
            else
                datenschutz = Html.fromHtml(datenschutz).toString();

            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_fingerprint_black_24dp);
            try {
                Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(wrappedDrawable, ApplicationFeatures.getTextColorPrimary(getContext()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            new MaterialDialog.Builder(getContext())
                    .title(getString(R.string.menu_privacy))
                    .content(datenschutz)

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .onPositive((dialog, which) -> dialog.dismiss())
                    .positiveText(R.string.ok)
                    .icon(drawable)
                    .show();
        } else if (v == image_sources) {
            String sources = getString(R.string.credits);

            if (Build.VERSION.SDK_INT > 24)
                sources = Html.fromHtml(sources, Html.FROM_HTML_MODE_LEGACY).toString();
            else
                sources = Html.fromHtml(sources).toString();
            sources = sources.replaceAll("\n\n", "\n");

            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_image_black_24dp);
            try {
                Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(wrappedDrawable, ApplicationFeatures.getTextColorPrimary(getContext()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            final TextView message = new TextView(getContext());
            final SpannableString s = new SpannableString(sources);
            Linkify.addLinks(s, Linkify.WEB_URLS);
            message.setText(s);
            message.setMovementMethod(LinkMovementMethod.getInstance());

            new MaterialDialog.Builder(getContext())
                    .title(getString(R.string.image_sources))
                    .cancelable(true)
                    .positiveText(R.string.ok)
                    .onPositive((dialog, which) -> dialog.dismiss())
                    .icon(drawable)
                    .customView(message, true)
                    .show();

        } else if (v == libs) {
            Intent intent = new LibsBuilder()
                    .withActivityTitle(getString(R.string.impressum_AboutLibs_Title))
                    .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                    .withFields(R.string.class.getFields())
                    .withAutoDetect(true)
                    .withAboutIconShown(true)
                    .withLicenseShown(true)
                    .withAboutDescription(getString(R.string.subtitle))
                    .withAboutAppName(getString(R.string.app_name))
                    .withActivityTheme(R.style.AboutLibsTheme)
                    .intent(this);

            startActivity(intent);
        } else if (v == imprint) {
            String sources = getString(R.string.imprint_text);

            if (Build.VERSION.SDK_INT > 24)
                sources = Html.fromHtml(sources, Html.FROM_HTML_MODE_LEGACY).toString();
            else
                sources = Html.fromHtml(sources).toString();
            sources = sources.replaceAll("\n\n", "\n");

            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_credit_card_black_24dp);
            try {
                Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(wrappedDrawable, ApplicationFeatures.getTextColorPrimary(getContext()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            final TextView message = new TextView(getContext());
            final SpannableString s = new SpannableString(sources);
            Linkify.addLinks(s, Linkify.WEB_URLS);
            message.setText(s);
            message.setMovementMethod(LinkMovementMethod.getInstance());

            new MaterialDialog.Builder(getContext())
                    .title(getString(R.string.imprint))
                    .cancelable(true)
                    .positiveText(R.string.ok)
                    .onPositive((dialog, which) -> dialog.dismiss())
                    .icon(drawable)
                    .customView(message, true)
                    .show();
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
