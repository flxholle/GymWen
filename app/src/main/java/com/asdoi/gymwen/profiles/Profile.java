package com.asdoi.gymwen.profiles;


public class Profile {
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
        return name + '@' + courses;
    }

    public static Profile parse(String s) {
        String[] sArray = s.split("@");
        if (sArray.length >= 2) {
            return new Profile(sArray[1], sArray[0]);
        }
        return null;
    }
}
