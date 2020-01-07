package com.asdoi.gymwen;

import java.util.ArrayList;

public class ProfileManagement {
    private static ArrayList<Profile> profileList;

    public static Profile getProfile(int pos) {
        return profileList.get(pos);
    }

    public static void addProfile(Profile k) {
        profileList.add(k);
    }

    public static int profileQuantity() {
        return profileList.size();
    }


    public static class Profile {
        private String courses;
        private String name;

        public Profile(String courses, String name) {
            setCourses(courses);
            setName(name);
        }

        public String getCourses() {
            return courses;
        }

        public void setCourses(String courses) {
            this.courses = courses;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return '@' + name + '@' + courses;
        }
    }
}
