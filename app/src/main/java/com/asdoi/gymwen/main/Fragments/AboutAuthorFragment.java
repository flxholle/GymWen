package com.asdoi.gymwen.main.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.asdoi.gymwen.R;
import com.marcoscg.easyabout.EasyAboutFragment;
import com.marcoscg.easyabout.helpers.AboutItemBuilder;
import com.marcoscg.easyabout.items.AboutCard;
import com.marcoscg.easyabout.items.NormalAboutItem;
import com.marcoscg.easyabout.items.PersonAboutItem;
/*import com.marcoscg.licenser.License;
import com.marcoscg.licenser.LicenserDialog;
import com.marcoscg.licenser.Library;*/

public class AboutAuthorFragment extends EasyAboutFragment {

    @Override
    protected void configureFragment(final Context context, View rootView, Bundle savedInstanceState) {
        addCard(new AboutCard.Builder(context)
                .addItem(AboutItemBuilder.generateAppTitleItem(context)
                        .setSubtitle(R.string.about_author_sub))
                .addItem(AboutItemBuilder.generateAppVersionItem(context, true)
                        .setIcon(R.drawable.ic_info_black_24dp))
                .addItem(new NormalAboutItem.Builder(context)
                        .setTitle(R.string.about_licenses)
                        .setIcon(R.drawable.ic_description_black_24dp)
                        .setOnClickListener((View v) -> {
//                            showLicensesDialog(getContext());
                        })
                        .build())
                .build());

        addCard(new AboutCard.Builder(context)
                .setTitle(R.string.about_author_title)
                .addItem(new PersonAboutItem.Builder(context)
                        .setTitle(R.string.about_author_name)
                        .setIcon(R.drawable.ic_asdoicolor)
                        .build())
                .addItem(AboutItemBuilder.generateLinkItem(context, "https://gitlab")
                        .setTitle("Fork on GitHub")
                        .setIcon(R.drawable.ic_gitlab))
                .addItem(AboutItemBuilder.generateLinkItem(context, "http://www.marcoscg.com")
                        .setTitle("Visit my website")
                        .setIcon(R.drawable.ic_web_black_24dp))
                .addItem(AboutItemBuilder.generateEmailItem(context, "marcoscgdev@gmail.com")
                        .setTitle("Send me an email")
                        .setIcon(R.drawable.ic_email_black_24dp))
                .build());

        addCard(new AboutCard.Builder(context)
                .setTitle("Support")
/*                .addItem(AboutItemBuilder.generatePlayStoreItem(context)
                        .setTitle("Rate application")
                        .setIcon(R.drawable.ic_star_black_24dp))*/
                .addItem(AboutItemBuilder.generateLinkItem(context, "https://github.com/marcoscgdev/EasyAbout/issues/new")
                        .setTitle("Report bugs")
                        .setIcon(R.drawable.ic_bug_report_black_24dp))
                .build());
    }

    /*public static void showLicensesDialog(Context context) {
        new LicenserDialog(context)
                .setTitle("Licenses")
                .setLibrary(new Library("Android Support Libraries",
                        "https://developer.android.com/topic/libraries/support-library/index.html",
                        License.APACHE))
                .setLibrary(new Library("Easy About",
                        "https://github.com/marcoscgdev/EasyAbout",
                        License.MIT))
                .setLibrary(new Library("Licenser",
                        "https://github.com/marcoscgdev/Licenser",
                        License.MIT))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }*/
}
