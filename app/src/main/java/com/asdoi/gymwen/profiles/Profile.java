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


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class Profile {
    private String courses;
    private String name;
    private static final char splitChar = '@';
    public static final String coursesSeparator = "#";

    public Profile(@NonNull String courses, String name) {
        setCourses(courses);
        this.name = name;
    }

    public String getCourses() {
        return courses;
    }

    @NonNull
    public String[] getCoursesArray() {
        return getCourses().split(coursesSeparator);
    }

    public void setCourses(@NonNull String courses) {
        this.courses = courses.trim();
    }

    void addCourse(@NonNull String course) {
        courses = courses + coursesSeparator + course.trim();
    }

    void removeCourse(String course) {
        ArrayList<String> courses = new ArrayList<>(Arrays.asList(getCoursesArray()));
        courses.remove(course);
        StringBuilder newCourses = new StringBuilder();
        for (String s : courses) {
            newCourses.append(s.trim());
            newCourses.append(coursesSeparator);
        }
        newCourses.replace(newCourses.lastIndexOf(coursesSeparator), newCourses.length(), "");
        setCourses(newCourses.toString());
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String toString() {
        return name + splitChar + courses;
    }

    @Nullable
    public static Profile parse(@NonNull String s) {
        String[] sArray = s.split("" + splitChar);
        if (sArray.length >= 2) {
            if (sArray[0].trim().isEmpty() || sArray[1].trim().isEmpty())
                return null;
            else
                return new Profile(sArray[1], sArray[0]);
        }
        return null;
    }

    public boolean isSenior() {
        return getCourses().split(coursesSeparator).length > 1;
    }
}
