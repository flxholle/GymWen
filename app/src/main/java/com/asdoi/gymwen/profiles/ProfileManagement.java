package com.asdoi.gymwen.profiles;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;

import java.util.ArrayList;

public class ProfileManagement {
    private static ArrayList<Profile> profileList = new ArrayList<>();

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

    public static int profileQuantity() {
        return profileList.size();
    }

    public static void reload() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
        String pref = sharedPref.getString("profiles", "");
        String[] profiles = pref.split("$");
        for (String s : profiles) {
            try {
                Profile p = Profile.parse(s);
                if (p != null)
                    addProfile(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        addProfile(new Profile("6b", "asd"));
    }

    public static void save() {
        String all = "";
        for (Profile p : profileList) {
            all += p.toString() + "$";
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("profiles", all).commit();
    }


}
