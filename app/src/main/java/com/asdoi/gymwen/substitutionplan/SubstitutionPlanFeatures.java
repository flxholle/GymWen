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

package com.asdoi.gymwen.substitutionplan;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.util.PreferenceUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;

/**
 * An abstract class which organizes the substitution plan object for easier access, not necessary
 */
public abstract class SubstitutionPlanFeatures {
    @NonNull
    private static final SubstitutionPlan substitutionPlan = new SubstitutionPlan(false, "1A");

    public static void setContext(Context context) {
        SubstitutionPlan.setContext(context);
    }

    public static void setup(boolean hours, String... courses) {
        substitutionPlan.reCreate(hours, courses);
    }

    @NonNull
    public static SubstitutionPlan createTempSubstitutionplan(boolean hours, String[] courses) {
        if (!isUninit() && PreferenceUtil.isOfflineMode())
            reloadDocs();
        SubstitutionPlan temp = new SubstitutionPlan(hours, courses);
        temp.setDocs(substitutionPlan.getDoc(true), substitutionPlan.getDoc(false));
        return temp;
    }

    public static boolean isUninit() {
        return substitutionPlan.getTodayDoc() == null && substitutionPlan.getTomorrowDoc() == null;
    }

    @NonNull
    public static SubstitutionList getToday() {
        return substitutionPlan.getToday();
    }

    @NonNull
    public static SubstitutionList getTomorrow() {
        return substitutionPlan.getTomorrow();
    }

    @NonNull
    public static SubstitutionList getTodaySummarized() {
        return substitutionPlan.getTodaySummarized();
    }

    @NonNull
    public static SubstitutionList getTomorrowSummarized() {
        return substitutionPlan.getTomorrowSummarized();
    }

    @Nullable
    public static SubstitutionList getTodayAll() {
        return substitutionPlan.getTodayAll();
    }

    @Nullable
    public static SubstitutionList getTomorrowAll() {
        return substitutionPlan.getTomorrowAll();
    }

    @NonNull
    public static SubstitutionList getTodayAllSummarized() {
        return substitutionPlan.getTodayAllSummarized();
    }

    @NonNull
    public static SubstitutionList getTomorrowAllSummarized() {
        return substitutionPlan.getTomorrowAllSummarized();
    }

    @NonNull
    public static String getTodayTitleString() {
        return substitutionPlan.getTodayTitleString();
    }

    @NonNull
    public static String getTomorrowTitleString() {
        return substitutionPlan.getTomorrowTitleString();
    }

    @NonNull
    public static SubstitutionTitle getTodayTitle() {
        return substitutionPlan.getTodayTitle();
    }

    @NonNull
    public static SubstitutionTitle getTomorrowTitle() {
        return substitutionPlan.getTomorrowTitle();
    }

    public static int getTodayTitleCode() {
        return substitutionPlan.getTodayTitleCode();
    }

    public static int getTomorrowTitleCode() {
        return substitutionPlan.getTomorrowTitleCode();
    }

    public static boolean getSenior() {
        return substitutionPlan.getSenior();
    }

    public static void setTodayDoc(Document value) {
        substitutionPlan.setTodayDoc(value);
    }

    public static void setTomorrowDoc(Document value) {
        substitutionPlan.setTomorrowDoc(value);
    }

    public static void setDocs(Document today, Document tomorrow) {
        substitutionPlan.setDocs(today, tomorrow);
    }

    public static Document getTodayDoc() {
        return substitutionPlan.getTodayDoc();
    }

    public static Document getTomorrowDoc() {
        return substitutionPlan.getTomorrowDoc();
    }

    @NonNull
    public static Document[] getDocs() {
        return new Document[]{getTodayDoc(), getTomorrowDoc()};
    }

    public static boolean areDocsDownloaded() {
        return substitutionPlan.areDocsDownloaded();
    }

    /**
     * saves the docs to SharedPreferences for offline use
     */
    public static void saveDocs() {
        if (!areDocsDownloaded())
            return;

        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext()).edit();
        String doc = substitutionPlan.getDoc(true).toString();
        prefsEditor.putString("doc1", doc);

        String doc2 = substitutionPlan.getDoc(false).toString();
        prefsEditor.putString("doc2", doc2);

        prefsEditor.commit();
    }

    /**
     * @return a boolean if the reloading of the docs was successful, also reloads the docs from sharedPreferences
     * @see #saveDocs
     */
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

    @Nullable
    public static ArrayList<String> getNames() {
        return substitutionPlan.getCourses();
    }
}
