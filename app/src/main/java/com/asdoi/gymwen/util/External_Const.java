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

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;

/**
 * Class that saves constants that can change in the future
 */
public abstract class External_Const {

    public final static String office_TelNr = "+49 9171 818 800";

    //App packageNames
    public final static String[] cafeteria_packageName = new String[]{"de.eezzy.admin.apnr40"};
    public final static String[] notes_packageNames = new String[]{"com.simplemobiletools.notes", "com.simplemobiletools.notes.pro", "com.samsung.android.app.notes"};
    public final static String[] timetable_packageNames = new String[]{"juliushenke.smarttt"};
    public static final String[] coloRush_packageNames = new String[]{"com.JUF.ColoRush"};
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

    public static final String colorush_online = "https://asdoi.gitlab.io/colorushweb/";


    //Download Apps
    public final static String downloadApp_notes = "https://f-droid.org/de/packages/com.simplemobiletools.notes.pro/";
    public final static String downloadApp_timetable = "https://apt.izzysoft.de/fdroid/index/apk/juliushenke.smarttt";
    public static final String downloadApp_colorush = "https://gitlab.com/asdoi/colorrush/blob/master/Apk/ColoRush.apk";
    public static final String downloadApp_publicTransport = "https://f-droid.org/de/packages/de.schildbach.oeffi/";
    public static final String downloadApp_libreoffice = "https://f-droid.org/de/packages/org.documentfoundation.libreoffice/";


    //SubstitutionPlan
    public static final String todayURL = "http://gym-wen.de/vp/heute.htm";
    public static final String tomorrowURL = "http://gym-wen.de/vp/morgen.htm";
    public static final String[] nothing = new String[]{"entfällt", "entf", ApplicationFeatures.getContext().getString(R.string.missing_short)};

    //TeacherlistFeatures
    public static final String teacherlistUrl = "http://www.gym-wen.de/information/sprechstunden/";
    public static final String AOLShort = "AOL";
    public static final String MAIL_ENDING = "@gym-wendelstein.de";

    //Geo location (Uri parse String)
    public static final String location = "geo:49.34600,11.15838?q=Gymnasium%20Wendelstein";

    //Updater
    public static final String UPDATER_JSON = "https://gitlab.com/asdoi/gymwenreleases/raw/master/UpdaterFile.json";
    public static final String APK_DOWNLOAD = "https://gitlab.com/asdoi/gymwenreleases/raw/master/GymWenApp.apk";
    public static final String APK_DOWNLOAD_PAGE = "https://gitlab.com/asdoi/gymwenreleases/blob/master/GymWenApp.apk";

    //App Registration (User census)
    public static final String REGISTER_URL = "https://asdoi.gitlab.io/hit_counter.html";
}
