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

package com.asdoi.gymwen.profiles;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class ProfileManagement {
    @NonNull
    private static ArrayList<Profile> profileList = new ArrayList<>();
    private final static char splitChar = '%';
    private static int preferredProfile;

    public static Profile getProfile(int pos) {
        return profileList.get(pos);
    }

    public static void addProfile(Profile k) {
        profileList.add(k);
    }

    public static void editProfile(int position, Profile newP) {
        profileList.remove(position);
        profileList.add(position, newP);
    }

    public static void removeProfile(int position) {
        profileList.remove(position);
    }

    public static int getSize() {
        return profileList.size();
    }

    @NonNull
    public static ArrayList<Profile> getProfileList() {
        return profileList;
    }

    public static void reload() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
        String pref = sharedPref.getString("profiles", "");
        String[] profiles = pref.split("" + splitChar);
        ArrayList<Profile> pList = new ArrayList<>();
        for (String s : profiles) {
            try {
                Profile p = Profile.parse(s);
                if (p != null)
                    pList.add(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        profileList = pList;
        preferredProfile = sharedPref.getInt("preferred_position", 0);
    }

    public static boolean isUninit() {
        return getProfileList() == null || getProfileList().size() == 0;
    }

    public static void initProfiles() {
        if (isUninit())
            reload();
    }

    public static void save(boolean apply) {
        StringBuilder all = new StringBuilder();
        for (Profile p : profileList) {
            all.append(p.toString()).append(splitChar);
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("profiles", all.toString());
        editor.putInt("preferred_position", preferredProfile);
        if (apply)
            editor.apply();
        else
            editor.commit();
    }

    public static boolean isMoreThanOneProfile() {
        return getSize() > 1;
    }

    @NonNull
    public static ArrayList<String> getProfileListNames() {
        ArrayList<String> a = new ArrayList<>();
        for (Profile p : profileList) {
            a.add(p.getName());
        }
        return a;
    }

    public static boolean addCourseToProfile(int pos, @NonNull String course) {
        if (pos < 0 || pos >= getSize())
            return false;

        Profile p = getProfile(pos);
        //If course isn't already in profile
        if (!Arrays.asList(p.getCoursesArray()).contains(course)) {
            p.addCourse(course);
            editProfile(pos, p);
            return true;
        }
        return false;
    }

    public static boolean removeFromProfile(int pos, String course) {
        if (pos < 0 || pos >= getSize())
            return false;

        Profile p = getProfile(pos);
        //If course isn't already in profile
        if (Arrays.asList(p.getCoursesArray()).contains(course)) {
            if (p.getCoursesArray().length > 1) {
                p.removeCourse(course);
                editProfile(pos, p);
                return true;
            }
        }
        return false;
    }

    public static void setPreferredProfilePosition(int value) {
        if (value == preferredProfile)
            preferredProfile = -1;
        else
            preferredProfile = value;
    }

    public static boolean checkPreferredProfile() {
        if (preferredProfile >= getSize()) {
            setPreferredProfilePosition(0);
            return false;
        }
        return true;
    }

    public static int getPreferredProfilePosition() {
        return preferredProfile;
    }

    public static int loadPreferredProfilePosition() {
        if (preferredProfile < 0 || preferredProfile >= getSize())
            return 0;
        return preferredProfile;
    }

    @Nullable
    public static Profile getPreferredProfile() {
        int pos = getPreferredProfilePosition();
        if (pos < 0 || pos >= getSize())
            return null;
        return getProfile(pos);
    }
}
