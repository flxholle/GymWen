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

package com.asdoi.gymwen.util;

import androidx.annotation.NonNull;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;

import java.util.Arrays;
import java.util.List;

/**
 * Class that saves constants that can change in the future
 */
public abstract class External_Const {

    public final static String office_TelNr = "+49 9171 818 800";
    public static final String author_mail = "GymWenApp@t-online.de";

    //App packageNames
    public final static String[] cafeteria_packageName = new String[]{"de.eezzy.admin.apnr40"};
    public final static String[] notes_packageNames = new String[]{"com.simplemobiletools.notes", "com.simplemobiletools.notes.pro", "com.samsung.android.app.notes"};
    public static final String[] publicTransport_packageNames = new String[]{"de.schildbach.oeffi", "com.mdv.VGNCompanion", "de.hafas.android.db"};
    public static final String[] excel_packageNames = new String[]{"com.microsoft.office.excel", "org.documentfoundation.libreoffice", "cn.wps.moffice_eng"};


    //Links
    public final static String forms_Link = "http://www.gym-wen.de/material/formulare-merkblaetter/";
    public final static String mebis_Link = "https://lernplattform.mebis.bayern.de/my/";
    public final static String cafeteria_Link = "https://www.kitafino.de/sys_k2/index.php?action=bestellen";
    public final static String shop_Link = "http://shop.apromote-werbemittel.de/";
    public final static String claxss_Link = "https://gym-wendelstein.schule-eltern.info/infoline/claxss";
    public static final String news_Link = "http://schuelerzeitung-gym-wen.de/";
    public static final String podcast_Link = "https://www.machdeinradio.de/kanal/wendelsteiner-welle/";
    public static final String rss_feed_Link = "https://asdoi.gitlab.io/rss-feeds/GymWenStartseite.xml";
    public static final String rss_feed_Link_2 = "https://asdoi.gitlab.io/rss-feeds/asdoiNews.xml";


    //About App
    public static final String GITLAB = "https://gitlab.com/asdoi/GymWen/";
    public static final String WEBSITE = "https://asdoi.gitlab.io/";
    public static final String BUGSITE = "https://gitlab.com/asdoi/GymWen/issues";
    public static final String DOWNLOAD_LINK = "https://asdoi.gitlab.io/gymwenapp.html";


    //Download Apps
    public static final String downloadApp_publicTransport = "https://f-droid.org/de/packages/de.schildbach.oeffi/";
    public static final String downloadApp_libreoffice = "https://f-droid.org/de/packages/org.documentfoundation.libreoffice/";

    public final static String downloadGradesTable = "https://gitlab.com/asdoi/Overview-about-your-grades/raw/master/Gesamtes_Notenbild.xlsx?inline=false";


    //ColoRush
    public static final String downloadApp_colorush = "https://asdoi.gitlab.io/colorush.html";
    public static final String[] coloRush_packageNames = new String[]{"com.JUF.ColoRush"};
    public static final String colorush_online = "https://asdoi.gitlab.io/colorushweb/";

    //Mebis App
    public static final String mebis_packageName = "com.asdoi.mebis";
    public static final String mebis_download_link = "https://gitlab.com/asdoi/MebisApp/-/raw/mebis/app/release/mebis.apk";

    //SubstitutionPlan
    public static final String todayURL = "http://gym-wen.de/vp/heute.htm";
    public static final String tomorrowURL = "http://gym-wen.de/vp/morgen.htm";

    //TeacherlistFeatures
    public static final String teacherlistUrl = "http://www.gym-wen.de/information/sprechstunden/";
    public static final String AOLShort = "AOL";
    public static final String MAIL_ENDING = "@gym-wendelstein.de";
    public static final String[] nothing = new String[]{"entfällt", "entf", ApplicationFeatures.getContext().getString(R.string.missing_short)};

    //Geo location (Uri parse String)
    public static final String location = "geo:49.34600,11.15838?q=Gymnasium%20Wendelstein";

    //Updater
    public static final String UPDATER_JSON = "https://gitlab.com/asdoi/gymwenreleases/raw/master/UpdaterFile.json";
    public static final String APK_DOWNLOAD = "https://gitlab.com/asdoi/gymwenreleases/raw/master/GymWenApp.apk";
    public static final String APK_DOWNLOAD_PAGE = "https://gitlab.com/asdoi/gymwenreleases/blob/master/GymWenApp.apk";

    //App Registration (User census)
    public static final String REGISTER_URL = "https://asdoi.gitlab.io/hit_counter.html";


    //Website Activity
    public static final String homepage = "http://www.gym-wen.de/startseite/";
    public static final String website_navigation = "http://www.gym-wen.de/startseite/navigation/";
    @NonNull
    public static final List<String> homeOfPagesIndexes = Arrays.asList("http://www.gym-wen.de/schulleben/",
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

    public static final String page_start = "http://www.gym-wen.de/";
    public static final String page_start_2 = "http://gym-wen.de/";
}
