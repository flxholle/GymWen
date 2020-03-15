package com.asdoi.gymwen.profiles;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class ProfileManagement {
    @NonNull
    private static ArrayList<Profile> profileList = new ArrayList<>();
    private final static char splitChar = '%';
    private int preferredProfile;

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
        profileList = new ArrayList<>();
        for (String s : profiles) {
            try {
                Profile p = Profile.parse(s);
                if (p != null)
                    addProfile(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isLoaded() {
        return getProfileListNames() != null;
    }

    public static void save(boolean apply) {
        StringBuilder all = new StringBuilder();
        for (Profile p : profileList) {
            all.append(p.toString()).append(splitChar);
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("profiles", all.toString());
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
}
