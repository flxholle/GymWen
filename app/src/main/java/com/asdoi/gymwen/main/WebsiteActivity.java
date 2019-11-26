package com.asdoi.gymwen.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.FragmentManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.VertretungsplanInternal.VertretungsPlan;
import com.asdoi.gymwen.main.Fragments.WebsiteActivityFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class    WebsiteActivity extends AppCompatActivity implements View.OnClickListener {
    public ArrayList<String> history = new ArrayList<>();

    private static String[][] con;
    private WebsiteActivityFragment f;
    private Document doc;
    private boolean intentActivationEnabled = true;
    private List<String> homeOfPagesIndexes = Arrays.asList("http://www.gym-wen.de/schulleben/",
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button

        // Preload custom tabs service for improved performance
        // This is optional but recommended
//        getApplication().registerActivityLifecycleCallbacks(new CustomTabsActivityLifecycleCallbacks());
    }

    private static boolean isURLValid(String url) {
        boolean isValid = true;
        try {
            URL u = new URL(url); // this would check for the protocol
            u.toURI(); // does the extra checking required for validation of URI
        } catch (Exception e) {
            isValid = false;
        }
        return isValid;
    }

    private static String urlToRightFormat(String url) {
        //Set URL to right format
        if (!url.substring(0, 3).equals("www") && !url.substring(0, 4).equals("http")) {
            url = "http://www." + url;
        }
        if (url.substring(0, 3).equals("www")) {
            url = "http://" + url;
        }
        if (!url.contains("http://www.")) {
            url = "http://www." + url.substring("http://".length());
        }
        if (url.charAt(url.length() - 1) != '/')
            url += "/";
        return url;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        VertretungsPlan.historySaveInstance = history;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        history = VertretungsPlan.historySaveInstance;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_website, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        VertretungsPlan.historySaveInstance = null;
//        Intent i = new Intent(this, MainActivity.class);
//        startActivity(i);
        finish();
        super.onSupportNavigateUp();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

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
        }catch (Exception e){

        }

        if (VertretungsPlan.historySaveInstance == null) {
            HomepageLoad();
        } else {
            history = VertretungsPlan.historySaveInstance;
            if (history.size() > 0)
                loadPage(history.get(history.size() - 1));
            else
                HomepageLoad();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_open_in_browser) {
            /*String url = history.get(history.size()-1);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);*/
            try {
                ApplicationFeatures.tabIntent(history.get(history.size() - 1));
            }
            catch (Exception e){}
        } else if (item.getItemId() == R.id.action_share || item.getItemId() == R.id.action_share2 ) {
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
        for (int i = 0; i < v1.size(); i++) {
            values.add(v1.get(i));
        }
        for (int i = 0; i < v2.size(); i++) {
            values.add(v2.get(i));
        }

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
        ArrayList<String[]> trimmedContentList = new ArrayList<String[]>();

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
            for (int j = 0; j < littleCon.length; j++) {
                littleCon[j] = "";
            }

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
                for (int j = 0; j < littleCon.length; j++) {
                    littleCon[j] = "";
                }
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
                    for (int j1 = 0; j1 < littleCon.length; j1++) {
                        littleCon[j1] = "";
                    }
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
        ArrayList<String[]> trimmedContentList = new ArrayList<String[]>();
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
        for (int i = 0; i < v1.size(); i++) {
            values.add(v1.get(i));
        }
        for (int i = 0; i < v2.size(); i++) {
            values.add(v2.get(i));
        }

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
                    for (int j = 0; j < s.length; j++) {
                        s[j] = "";
                    }
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

    private String getLink(Element e) {
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
//        if (myWebView.canGoBack()) {
//            myWebView.goBack();
//            return;
//        }

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

    private void setWebsiteTitle(Document doc) {
        String whole = doc.select("head").select("title").toString();
        final String title = HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n", "");

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                getSupportActionBar().setTitle(title);
            }
        });
    }

    private void loadFragment(final int pageCode) {
        f = new WebsiteActivityFragment(con, pageCode);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.website_host, f).commit();
    }

    private void overwriteContent(String[][] c) {
        con = c;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id < con.length && id > 0) {
            loadPage(con[id][3]);
        }
    }

    public void loadPage(String url) {
        if (!url.trim().isEmpty()) {

            final String urlFinal = urlToRightFormat(url);

            if (isURLValid(urlFinal) && urlFinal.contains("http://www.gym-wen.de/")) {
                (new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isHTML = false;
                        try {
                            doc = Jsoup.connect(urlFinal).get();
                            isHTML = true;
                        } catch (Exception e) {
                            e.getStackTrace();
                            isHTML = false;
                        }


                        if (isHTML) {
                            history.add(urlFinal);
                            setWebsiteTitle(doc);
                            //Check Site
                            if (homeOfPagesIndexes.contains(urlFinal)) {
                                HomeOfPages(urlFinal);
                            } else {
                                ContentPagesMixed(urlFinal);
                            }
                        } else {
                            try{
                                ApplicationFeatures.tabIntent(urlFinal);
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                })).start();
            } else {
                ApplicationFeatures.tabIntent(urlFinal);
            }
        }
    }
}