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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.FragmentManager;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.fragments.WebsiteActivityFragment;
import com.pd.chocobar.ChocoBar;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WebsiteActivity extends ActivityFeatures implements View.OnClickListener {
    @Nullable
    public ArrayList<String> history = new ArrayList<>();

    private static String[][] con;
    private Document doc;
    private boolean intentActivationEnabled = true;
    @NonNull
    private final List<String> homeOfPagesIndexes = Arrays.asList("http://www.gym-wen.de/schulleben/",
            "http://www.gym-wen.de/schulleben/projekte/",
            "http://www.gym-wen.de/schulleben/ereignisse/",
            "http://www.gym-wen.de/schulleben/exkursionen/",
            "http://www.gym-wen.de/information/unsere-schule/",
            "http://www.gym-wen.de/information/",
            "http://www.gym-wen.de/startseite/",
            "http://www.gym-wen.de/schulleben/archiv/",
            "http://www.gym-wen.de/schulleben/archiv/unser-schuljahr-201516/",
            "http://www.gym-wen.de/schulleben/archiv/unser-schuljahr-201415/",
            "http://www.gym-wen.de/schulleben/archiv/unser-schuljahr-201314/",
            "http://www.gym-wen.de/schulleben/archiv/unser-schuljahr-201213/",
            "http://www.gym-wen.de/information/schulanmeldung/",
            "http://www.gym-wen.de/startseite/kontaktdaten/",
            "http://www.gym-wen.de/information/unsere-schule/wahlkursangebot/",
            "http://www.gym-wen.de/angebote/interessante-links/",
            "http://www.gym-wen.de/material/",
            "http://www.gym-wen.de/material/fachmaterialien/kunst/",
            "http://www.gym-wen.de/material/formulare-merkblaetter/",
            "http://www.gym-wen.de/material/corporate-design/",
            "http://www.gym-wen.de/material/fachmaterialien/",
            "http://www.gym-wen.de/probleme/",
            "http://www.gym-wen.de/startseite/navigation/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_website);
        start();
    }

    public void setupColors() {
        setToolbar(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((Toolbar) findViewById(R.id.toolbar)).setNavigationIcon(R.drawable.ic_clear_black_24dp);
        }
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        ApplicationFeatures.websiteHistorySaveInstance = history;
    }

    @Override
    public void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        history = ApplicationFeatures.websiteHistorySaveInstance;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_website, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        ApplicationFeatures.websiteHistorySaveInstance = null;
//        Intent i = new Intent(this, MainActivity.class);
//        startActivity(i);
        finish();
        super.onSupportNavigateUp();
        return true;
    }

    public void start() {

        if (!ApplicationFeatures.isNetworkAvailable()) {
            ChocoBar.builder().setActivity(this)
                    .setActionText(getString(R.string.ok))
                    .setText(getString(R.string.noInternetConnection))
                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                    .setActionClickListener((View v) -> finish())
                    .orange()
                    .show();
            return;
        }

        try {
            String intentURL = getIntent().getStringExtra("url");
            if (intentURL == null) {
                Uri data = getIntent().getData();
                intentURL = data.getHost() + data.getPath();
            }
            if (intentURL != null && intentActivationEnabled) {
                loadPage(intentURL);
                intentActivationEnabled = false;
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ApplicationFeatures.websiteHistorySaveInstance == null) {
            HomepageLoad();
        } else {
            history = ApplicationFeatures.websiteHistorySaveInstance;
            if (history.size() > 0)
                loadPage(history.get(history.size() - 1));
            else
                HomepageLoad();
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_open_in_browser) {
            /*String url = history.get(history.size()-1);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);*/
            try {
                openInTabIntent(history.get(history.size() - 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (item.getItemId() == R.id.action_share || item.getItemId() == R.id.action_share2) {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_TEXT, history.get(history.size() - 1));
            i.setType("text/plan");
            startActivity(Intent.createChooser(i, getString(R.string.share_link)));
        }
        return super.onOptionsItemSelected(item);
    }

    private void HomepageLoad() {
        loadPage("http://www.gym-wen.de/startseite/");
//        loadPage("http://www.gym-wen.de/information/unsere-schule/schulzweige/");
    }

    private void HomeOfPages(String url) {


        //Get Elements
        Elements values = new Elements();
        Elements v0 = doc.select("div.tx-t3sheaderslider-pi1");
        Elements v1 = doc.select("div.csc-default");
        Elements v2 = doc.select("div.csc-frame");
        values.add(v0.get(0));
        values.addAll(v1);
        values.addAll(v2);

        String[][] content = new String[values.size()][4];
        for (int i = 0; i < content.length; i++) {
            for (int j = 0; j < content[0].length; j++) {
                content[i][j] = "";
            }
        }

        //Generate content
        String whole = "";
        for (int i = 0; i < content.length; i++) {
            Elements text = values.get(i).select("div.csc-textpic-text");
            if (text.size() > 0) {
                Elements titleElements = text.get(0).select("h1");
                if (titleElements.size() > 0) {
                    //Title
                    whole = titleElements.get(0).toString();
                    content[i][1] = HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n", "");
                }

                Elements descriptionElements = text.get(0).select("p.bodytext");
                if (descriptionElements.size() > 0) {
                    //Description
                    whole = descriptionElements.get(0).toString();
                    content[i][2] = HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n", "");

                }
            }

            Elements linkElements = values.get(i).select("a");
            if (linkElements.size() > 0) {
                whole = linkElements.get(0).toString();
                int beginIndex = whole.indexOf("href=");
                int endIndex = whole.indexOf("\"", beginIndex + "href=".length() + 1);
                if (beginIndex > 0 && endIndex > 0 && beginIndex - endIndex < 0) {
                    content[i][3] = whole.substring(beginIndex + "href=".length() + 1, endIndex);
                }

                if (!content[i][3].substring(0, "http".length()).equals("http")) {
                    content[i][3] = "http://gym-wen.de/" + content[i][3];
                }
            }

            Elements imgElements = values.get(i).select("img");
            if (imgElements.size() > 0) {
                //Images
                whole = imgElements.get(0).toString();
                int beginIndex = whole.indexOf("src=");
                int endIndex = whole.indexOf("\"", beginIndex + "src=".length() + 1);
                if (beginIndex > 0 && endIndex > 0 && beginIndex - endIndex < 0) {
                    content[i][0] = whole.substring(beginIndex + "src=".length() + 1, endIndex);
                }

                                /*if (whole.indexOf("src=") > 0 && whole.indexOf("width") > 0 && whole.indexOf("src=") - whole.indexOf("width") < 0) {
                                    content[i][0] = whole.substring(whole.indexOf("src=") + "src=".length() + 1, whole.indexOf("width") - 2);
                                } else if (whole.indexOf("src=") > 0 && whole.indexOf("alt") > 0 && whole.indexOf("src=") - whole.indexOf("alt") < 0) {
                                    content[i][0] = whole.substring(whole.indexOf("src=") + "src=".length() + 1, whole.indexOf("alt") - 2);
                                } else {
                                    content[i][0] = whole;
                                }*/

                if (!content[i][0].substring(0, "http".length()).equals("http")) {
                    content[i][0] = "http://gym-wen.de/" + content[i][0];
                }

            }
        }

        //Trim content
        ArrayList<String[]> trimmedContentList = new ArrayList<>();

//                trimmedContentList.add(new String[]{"http://www.gym-wen.de/fileadmin/user_upload/logo.jpg", "Gymnasium Wendelstein", "Startseite", ""});


        trimmedContentList.add(content[0]);
        for (int i = 1; i < content.length; i++) {
            if ((!content[i][0].trim().isEmpty() && !content[i][3].trim().isEmpty()) ||
                    !content[i][1].trim().isEmpty() ||
                    !content[i][2].trim().isEmpty()) {
                trimmedContentList.add(content[i]);
            }
        }

        String[][] trimmedContent = new String[trimmedContentList.size()][trimmedContentList.get(0).length];
        for (int i = 0; i < trimmedContentList.size(); i++) {
            trimmedContent[i] = trimmedContentList.get(i);
        }

        content = trimmedContent;


        overwriteContent(content);
        loadFragment(1);
    }

    private void ContentPages(final String url) {

        //Get Elements
        Elements values = doc.select("#content_wrap");

        ArrayList<String[]> contentList = new ArrayList<>();

        //Generate content
        String whole = "";
        for (int i = 0; i < values.size(); i++) {
            Element text = values.get(i);
            String[] littleCon = new String[4];
            Arrays.fill(littleCon, "");

            if (text != null) {
                Elements titleElements = text.select("div.csc-header");
                if (titleElements.size() > 0) {
                    //Title
                    whole = titleElements.get(0).toString();
                    littleCon[1] = HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n", "");
                }

                Elements descriptionElements = text.select("p.bodytext");
                for (int j = 0; j < descriptionElements.size(); j++) {

                    //Description
                    whole = descriptionElements.get(j).toString();
                    String des = HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n", "");
                    if (des.equals("\n")) {
                        des = "";
                    }
                    littleCon[2] += des + "\n";

                }
            }

            littleCon[3] = getLink(values.get(i));

            if (!littleCon[1].isEmpty() || !littleCon[2].isEmpty()) {
                contentList.add(littleCon);
                littleCon = new String[4];
                Arrays.fill(littleCon, "");
            }

            Elements imgElements = values.get(i).select("img");
            for (int j = 0; j < imgElements.size(); j++) {
                //Images
                whole = imgElements.get(j).toString();
                int beginIndex = whole.indexOf("src=");
                int endIndex = whole.indexOf("\"", beginIndex + "src=".length() + 1);
                if (beginIndex > 0 && endIndex > 0 && beginIndex - endIndex < 0) {
                    littleCon[0] = whole.substring(beginIndex + "src=".length() + 1, endIndex);
                }

                if (!littleCon[0].substring(0, "http".length()).equals("http")) {
                    littleCon[0] = "http://gym-wen.de/" + littleCon[0];
                }

                if (!littleCon[0].isEmpty()) {
                    contentList.add(littleCon);
                    littleCon = new String[4];
                    Arrays.fill(littleCon, "");
                }
            }

        }

        //Create Content Array
        String[][] content = new String[contentList.size()][4];
        for (int i = 0; i < contentList.size(); i++) {
            content[i] = contentList.get(i);
        }

        //HeadImgLink
        Elements header = doc.select("div.tx-t3sheaderslider-pi1");
        Elements img = header.select("img");
        String imgLink = "";
        if (img.size() > 0) {
            //Images
            whole = img.get(0).toString();
            int beginIndex = whole.indexOf("src=");
            int endIndex = whole.indexOf("\"", beginIndex + "src=".length() + 1);
            if (beginIndex > 0 && endIndex > 0 && beginIndex - endIndex < 0) {
                imgLink = whole.substring(beginIndex + "src=".length() + 1, endIndex);
            }

            if (!imgLink.substring(0, "http".length()).equals("http")) {
                imgLink = "http://gym-wen.de/" + imgLink;
            }

        }

        //Trim content
        ArrayList<String[]> trimmedContentList = new ArrayList<>();
        trimmedContentList.add(new String[]{imgLink, "", "", ""});
        for (int i = 0; i < content.length; i++) {
            if (!content[i][0].isEmpty() || !content[i][1].isEmpty() || !content[i][2].isEmpty() || !content[i][3].isEmpty()) {
                trimmedContentList.add(content[i]);
            }
        }

        String[][] trimmedContent = new String[trimmedContentList.size()][trimmedContentList.get(0).length];
        for (int i = 0; i < trimmedContentList.size(); i++) {
            trimmedContent[i] = trimmedContentList.get(i);
        }

        content = trimmedContent;


        overwriteContent(content);
        loadFragment(2);

    }

    private void ContentPagesMixed(final String url) {


        //Get Elements
        Elements values = new Elements();
        Elements v0 = doc.select("div.tx-t3sheaderslider-pi1");
        Elements v1 = doc.select("div.csc-default");
        Elements v2 = doc.select("div.csc-frame");
        values.add(v0.get(0));
        values.addAll(v1);
        values.addAll(v2);

        ArrayList<String[]> content = new ArrayList<>();

        //Generate content
        String whole = "";
        for (int i = 0; i < values.size(); i++) {
            Element currentValue = values.get(i);
            if (currentValue != null) {
                Elements titleElements = currentValue.select("h1");
                for (int j = 0; j < titleElements.size(); j++) {
                    //Title
                    String link = getLink(currentValue);

                    whole = titleElements.get(j).toString();
                    content.add(new String[]{"", HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n", ""), "", link});

                }

                Elements descriptionElements = currentValue.select("p.bodytext");
                if (descriptionElements.size() > 0) {
                    String[] s = new String[4];
                    Arrays.fill(s, "");
                    for (int j1 = 0; j1 < descriptionElements.size(); j1++) {
                        //Description
                        whole = descriptionElements.get(j1).toString();
                        s[2] += HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n\n", "\n");
                    }
                    content.add(s);
                }


                Elements imgElements = values.get(i).select("img");
                String link = getLink(currentValue);
                for (int j = 0; j < imgElements.size(); j++) {
                    //Images
                    whole = imgElements.get(j).toString();
                    int beginIndex = whole.indexOf("src=");
                    int endIndex = whole.indexOf("\"", beginIndex + "src=".length() + 1);
                    if (beginIndex > 0 && endIndex > 0 && beginIndex - endIndex < 0) {
                        content.add(new String[]{whole.substring(beginIndex + "src=".length() + 1, endIndex), "", "", link});
                    }

                    if (!content.get(content.size() - 1)[0].substring(0, "http".length()).equals("http")) {
                        content.get(content.size() - 1)[0] = "http://gym-wen.de/" + content.get(content.size() - 1)[0];
                    }

                }
            }

        }
        String[][] contentArray = new String[content.size()][];
        for (int i = 0; i < content.size(); i++) {
            contentArray[i] = content.get(i);
        }

        overwriteContent(contentArray);

        loadFragment(3);
    }

    @NonNull
    private String getLink(@NonNull Element e) {
        String link = "";
        Elements linkElements = e.select("a");
        if (linkElements.size() > 0) {
            String whole = linkElements.get(0).toString();
            int beginIndex = whole.indexOf("href=");
            int endIndex = whole.indexOf("\"", beginIndex + "href=".length() + 1);
            if (beginIndex > 0 && endIndex > 0 && beginIndex - endIndex < 0) {
                link = whole.substring(beginIndex + "href=".length() + 1, endIndex);
            }

            if (!link.substring(0, "http".length()).equals("http")) {
                link = "http://gym-wen.de/" + link;
            }
        }
        return link;
    }

    @Override
    public void onBackPressed() {


        // Check if the key event was the Back button and if there's history

        //If image is expanded
        if (WebsiteActivityFragment.isExpanded) {
            try {
                //Not working
                WebsiteActivityFragment.expandImage.performClick();
                WebsiteActivityFragment.expandImage.callOnClick();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(this, getString(R.string.tap_picture), Toast.LENGTH_SHORT).show();
            return;
        }

        if (history.size() >= 2) {
            String url = history.get(history.size() - 2);
            //Remove last two Sites, because the side that will be loaded will also be added by loadSite()
            history.remove(history.size() - 1);
            history.remove(history.size() - 1);
            loadPage(url);
        } else {
            onSupportNavigateUp();
            super.onBackPressed();
        }

    }

    private void setWebsiteTitle(@NonNull Document doc) {
        String whole = doc.select("head").select("title").toString();
        final String title = HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n", "");

        runOnUiThread(() -> getSupportActionBar().setTitle(title));
    }

    private void loadFragment(final int pageCode) {
        try {
            WebsiteActivityFragment f = new WebsiteActivityFragment(con, pageCode);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.website_host, f).commit();
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show());
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void overwriteContent(String[][] c) {
        con = c;
    }

    @Override
    public void onClick(@NonNull View view) {
        int id = view.getId();
        if (id < con.length && id > 0) {
            loadPage(con[id][3]);
        }
    }

    public void loadPage(@NonNull String url) {
        if (!url.trim().isEmpty()) {

            final String formattedUrl = ApplicationFeatures.urlToRightFormat(url);
            final Context context = this;
            if (ApplicationFeatures.isURLValid(formattedUrl) && formattedUrl.contains("http://www.gym-wen.de/")) {
                (new Thread(() -> {
                    boolean isHTML = false;
                    try {
                        doc = Jsoup.connect(formattedUrl).get();
                        isHTML = true;
                    } catch (Exception e) {
                        e.getStackTrace();
                        isHTML = false;
                    }


                    if (isHTML) {
                        history.add(formattedUrl);
                        setWebsiteTitle(doc);
                        //Check Site
                        if (homeOfPagesIndexes.contains(formattedUrl)) {
                            HomeOfPages(formattedUrl);
                        } else {
                            ContentPagesMixed(formattedUrl);
                        }
                    } else {
                        try {
                            openInTabIntent(formattedUrl);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })).start();
            } else {
                openInTabIntent(formattedUrl);
            }
        }
    }

    private void openInTabIntent(@NonNull String url) {
        if (url.charAt(url.length() - 1) == '/')
            url = url.substring(0, url.length() - 1);
        tabIntent(url);
    }
}