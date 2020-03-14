package com.asdoi.gymwen.teacherlist;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.util.External_Const;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Objects;

/**
 * Meta-Class that creates more useabilty for the Parse-Class
 *
 * @see Parse
 * Array with length for, with content of new String[]{Kürzel, Nachname, Vorname, Sprechstunde}
 */

public abstract class Teacherlist {
    private static Document list;

    /**
     * @return the full teacherlist
     * @see Parse#getList
     * new String[]{Kürzel, Nachname, Vorname, Sprechstunde}
     */
    @Nullable
    public static String[][] liste() {
        if (list == null) {
            return null;
        }

        return Parse.getList(list);
    }


    /**
     * @param search a String, which is one of the four parts of the list-Array
     * @return if no match is found: null  |  else: the first match
     * @see #liste
     * @see Parse#getTeacher
     */
    @Nullable
    public static String[] getTeacher(String search) {
        return Parse.getTeacher(search, Objects.requireNonNull(liste()));
    }

    /**
     * @param search a String, which is one of the four parts of the list-Array
     * @return if no match is found: null  |  else: all matches in array
     * @see #liste
     * @see Parse#getTeachers
     */
    @NonNull
    public static String[][] getTeachers(@NonNull String search) {
        return Parse.getTeachers(search, Objects.requireNonNull(liste()));
    }

    /**
     * @param doc sets the HTML-Doc (Jsoup) with the raw-html-code
     */
    public static void setDoc(Document doc) {
        list = doc;
    }

    /**
     * @return gets the doc
     */
    private static Document getDoc() {
        return list;
    }

    /**
     * @return checks if doc isn't null, which means it hasn't been downloaded
     */
    public static boolean isDownloaded() {
        return list != null;
    }

    /**
     * Saves the docs to sharedPreferences, for offline use
     */
    public static void saveDoc() {
        if (!isDownloaded())
            return;

        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).edit();
        String doc = getDoc().toString();
        prefsEditor.putString("docList", doc);
        prefsEditor.commit();
    }

    /**
     * reloads the docs from sharedPreferences
     *
     * @return returns true if it succeeded, false if not
     * @see #saveDoc
     */
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

    /**
     * @param query a String, which is one of the four parts of the list-Array
     * @return boolen if the query equals the AOL-Shortcut
     * @see #liste
     */
    public static boolean isAOL(@NonNull String query) {
        return query.equals(External_Const.AOLShort);
    }
}
