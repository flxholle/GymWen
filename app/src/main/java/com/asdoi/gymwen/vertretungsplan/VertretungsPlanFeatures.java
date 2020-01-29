package com.asdoi.gymwen.vertretungsplan;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;

public abstract class VertretungsPlanFeatures {
    public static String strUserId = "";
    public static String strPasword = "";

    public static final String todayURL = "http://gym-wen.de/vp/heute.htm";
    public static final String tomorrowURL = "http://gym-wen.de/vp/morgen.htm";

    //ChoiceActivity -> Step 5
    public static final String[][] choiceCourseNames = new String[][]{{ApplicationFeatures.getContext().getString(R.string.math), ApplicationFeatures.getContext().getString(R.string.mathShort)},
            {ApplicationFeatures.getContext().getString(R.string.german), ApplicationFeatures.getContext().getString(R.string.germanShort)},
            {ApplicationFeatures.getContext().getString(R.string.history), ApplicationFeatures.getContext().getString(R.string.historyShort)},
            {ApplicationFeatures.getContext().getString(R.string.social_education), ApplicationFeatures.getContext().getString(R.string.social_educationShort)},
            {ApplicationFeatures.getContext().getString(R.string.PE), ApplicationFeatures.getContext().getString(R.string.PEShort)},
            {ApplicationFeatures.getContext().getString(R.string.Religious_education), ApplicationFeatures.getContext().getString(R.string.Religious_educationShort)},
            {ApplicationFeatures.getContext().getString(R.string.english), ApplicationFeatures.getContext().getString(R.string.englishShort)},
            {ApplicationFeatures.getContext().getString(R.string.france), ApplicationFeatures.getContext().getString(R.string.franceShort)},
            {ApplicationFeatures.getContext().getString(R.string.latin), ApplicationFeatures.getContext().getString(R.string.latinShort)},
            {ApplicationFeatures.getContext().getString(R.string.spanish), ApplicationFeatures.getContext().getString(R.string.spanishShort)},
            {ApplicationFeatures.getContext().getString(R.string.biology), ApplicationFeatures.getContext().getString(R.string.biologyShort)},
            {ApplicationFeatures.getContext().getString(R.string.chemistry), ApplicationFeatures.getContext().getString(R.string.chemistryShort)},
            {ApplicationFeatures.getContext().getString(R.string.physics), ApplicationFeatures.getContext().getString(R.string.physicsShort)},
            {ApplicationFeatures.getContext().getString(R.string.programming), ApplicationFeatures.getContext().getString(R.string.programmingShort)},
            {ApplicationFeatures.getContext().getString(R.string.geography), ApplicationFeatures.getContext().getString(R.string.geographyShort)},
            {ApplicationFeatures.getContext().getString(R.string.finance), ApplicationFeatures.getContext().getString(R.string.financeShort)},
            {ApplicationFeatures.getContext().getString(R.string.art), ApplicationFeatures.getContext().getString(R.string.artShort)},
            {ApplicationFeatures.getContext().getString(R.string.music), ApplicationFeatures.getContext().getString(R.string.musicShort)},
            {ApplicationFeatures.getContext().getString(R.string.W_Seminar), ApplicationFeatures.getContext().getString(R.string.W_SeminarShort)},
            {ApplicationFeatures.getContext().getString(R.string.P_Seminar), ApplicationFeatures.getContext().getString(R.string.P_SeminarShort)},
            {ApplicationFeatures.getContext().getString(R.string.profile_subject), ApplicationFeatures.getContext().getString(R.string.profile_subjectShort)}
    };

    private static Vertretungsplan vertretungsplan = new Vertretungsplan();

    public static void setContext(Context context){
        vertretungsplan.setContext(context);
    }

    public static void setup(boolean hours, String... courses) {
        if (vertretungsplan == null) {
            vertretungsplan = new Vertretungsplan(hours, courses);
        } else {
            vertretungsplan.reCreate(hours, courses);
        }
    }

    public static Vertretungsplan createTempVertretungsplan(boolean hours, String... courses) {
        Vertretungsplan temp = new Vertretungsplan(hours, courses);
        temp.setDocs(vertretungsplan.getDoc(true), vertretungsplan.getDoc(false));
        return temp;
    }

    public static void signin(String username, String password) {
        strUserId = username;
        strPasword = password;
    }

    public static boolean isUninit() {
        return vertretungsplan == null;
    }

    public static String[][] getTomorrowArray() {
        return vertretungsplan.getDay(false);
    }

    public static String[][] getTodayArray() {
        return vertretungsplan.getDay(true);
    }

    public static String[][] getTodayArrayAll() {
        return vertretungsplan.getAll(true);
    }

    public static String[][] getTomorrowArrayAll() {
        return vertretungsplan.getAll(false);
    }

    public static String getTodayTitle() {
        return vertretungsplan.getTitleString(true);
    }

    public static String getTomorrowTitle() {
        return vertretungsplan.getTitleString(false);
    }

    public static String[] getTodayTitleArray() {
        return vertretungsplan.getTitleArray(true);
    }

    public static String[] getTomorrowTitleArray() {
        return vertretungsplan.getTitleArray(false);
    }

    public static boolean getOberstufe() {
        return vertretungsplan.getOberstufe();
    }

    public static void setTodayDoc(Document value) {
        vertretungsplan.setTodayDoc(value);
    }

    public static void setTomorrowDoc(Document value) {
        vertretungsplan.setTomorrowDoc(value);
    }

    public static void setDocs(Document today, Document tomorrow) {
        vertretungsplan.setDocs(today, tomorrow);
    }

    public static boolean areDocsDownloaded() {
        return vertretungsplan.areDocsDownloaded();
    }

    public static void saveDocs() {
        if (!areDocsDownloaded())
            return;

        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).edit();
        String doc = vertretungsplan.getDoc(true).toString();
        prefsEditor.putString("doc1", doc);

        String doc2 = vertretungsplan.getDoc(false).toString();
        prefsEditor.putString("doc2", doc2);

        prefsEditor.commit();
    }

    public static boolean reloadDocs() {
        try {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
            String docString1 = sharedPref.getString("doc1", "");
            Document doc1 = Jsoup.parse(docString1);

            String docString2 = sharedPref.getString("doc2", "");
            Document doc2 = Jsoup.parse(docString2);

            if (!docString1.trim().isEmpty() && !docString2.trim().isEmpty()) {
                setDocs(doc1, doc2);
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static ArrayList<String> getNames() {
        return vertretungsplan.getCourses();
    }
}
