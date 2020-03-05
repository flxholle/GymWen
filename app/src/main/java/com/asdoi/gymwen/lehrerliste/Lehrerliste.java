package com.asdoi.gymwen.lehrerliste;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class Lehrerliste {
    public static final String listUrl = "http://www.gym-wen.de/information/sprechstunden/";
    private static Document list;

    public static String AOLShort = "AOL";

    public static String[][] liste() {
        if (list == null) {
            return null;
        }

        return Parse.getList(list);
    }

    public static String[] getTeacher(String search) {
        return Parse.getTeacher(search, liste());
    }

    public static String[][] getTeachers(String search) {
        return Parse.getTeachers(search, liste());
    }

    public static void setDoc(Document doc) {
        list = doc;
    }

    public static Document getDoc() {
        return list;
    }

    public static boolean isDownloaded() {
        return list != null;
    }

    public static void saveDoc() {
        if (!isDownloaded())
            return;

        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).edit();
        String doc = getDoc().toString();
        prefsEditor.putString("docList", doc);
        prefsEditor.commit();
    }

    public static boolean reloadDoc() {
        try {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
            String docString1 = sharedPref.getString("docList", "");
            Document doc1 = Jsoup.parse(docString1);

            if (!docString1.trim().isEmpty()) {
                setDoc(doc1);
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isAOL(String query) {
        return query.equals(AOLShort);
    }
}
