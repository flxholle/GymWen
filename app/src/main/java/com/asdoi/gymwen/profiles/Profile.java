package com.asdoi.gymwen.profiles;


public class Profile {
    private String courses;
    private String name;
    private final static char splitChar = '@';

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
        return name + splitChar + courses;
    }

    public static Profile parse(String s) {
        String[] sArray = s.split("" + splitChar);
        if (sArray.length >= 2) {
            if (sArray[1].trim().isEmpty() || sArray[2].trim().isEmpty())
                return null;
            else
                return new Profile(sArray[1], sArray[0]);
        }
        return null;
    }
}
