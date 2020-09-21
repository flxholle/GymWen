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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Class that parses the substitution plan, it gets information like title and entries
 */
abstract class Parse {

    //Title
    //Internal parse methods

    /**
     * @param doc          raw HTML-Document (Jsoup), which will be analyzed
     * @param showWeekdays boolean: if the specific day should also be shown, if the date is in the past
     * @param today        The name of today in the specific language
     * @param tomorrow     The name of tomorrow in the specific language
     * @param laterDay     The name of "day is in the past" in the specific language
     * @return an sorted Array of all title information, with the length 3. Like this: new String[]{Date, DateName (Weekday), WeekNr}
     */
    //Date, DateName, WeekNr
    @NonNull
    public static SubstitutionTitle getTitle(Document doc, boolean showWeekdays, String today, String tomorrow, String laterDay, int pastCode, int todayCode, int tomorrowCode, int futureCode) {
        try {
            String title = doc.select("h2.TextUeberschrift").get(0).text();
            String[] titleElements = title.replace("Vertretungsplan für ", "").replace("(", ",").split(",");

            String dateString = titleElements[1].trim();

            SimpleDateFormat parser = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            Date startDate = parser.parse(dateString);

            SimpleDateFormat localeFormat = new SimpleDateFormat("EEEE", Locale.getDefault()); // the dayArray of the week spelled out completely
            String dayOfWeek = localeFormat.format(startDate);

            char weekChar = titleElements[2].trim().replace(")", "").replace("Woche", "").charAt(0);


            SubstitutionTitle day = new SubstitutionTitle(dateString, dayOfWeek, "" + weekChar, -1);

            //Weekday
            Date currentDate = removeTime(new Date());

            if (currentDate.after(startDate)) {
                //If date is in past
                day.setDayOfWeek(showWeekdays ? day.getDayOfWeek() + " " + laterDay : laterDay);
                day.setTitleCode(pastCode);
            } else if (currentDate.equals(startDate)) {
                //If date is today
                day.setDayOfWeek(showWeekdays ? day.getDayOfWeek() + " (" + today + ")" : today);
                day.setTitleCode(todayCode);
            } else {
                //If date is tomorrow
                //Set current date to one day in the future
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);

                //Check if currentdate equals date -> Tomorrow
                currentDate = cal.getTime();
                if (currentDate.equals(startDate)) {
                    day.setDayOfWeek(showWeekdays ? day.getDayOfWeek() + " (" + tomorrow + ")" : tomorrow);
                    day.setTitleCode(tomorrowCode);
                }
            }


            return day;
        } catch (Exception e) {
            e.printStackTrace();
            return new SubstitutionTitle(true);
        }
    }

    /**
     * @param date Date
     * @return param Date with removed time (only the day).
     */
    @NonNull
    private static Date removeTime(@NonNull Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }


    //Substitution plan

    /**
     * @param doc raw HTML-Document (Jsoup), which will be analyzed
     * @return an tow-dimensional String array with every entry of the plan in the html-doc. An entry has the same order like the plan in the doc. My one looks like this: new String[]{class, hour, subject, sit-in, room, moreInformation}
     */
    //All
    @NonNull
    static SubstitutionList getSubstitutionListUnfiltered(@Nullable Document doc) {
        if (doc == null) {
            System.out.println("Document is null");
            return new SubstitutionList(true);
        }

        Elements rows = doc.select("table.TabelleVertretungen tr");

        SubstitutionList entries = new SubstitutionList();
        int courseIndex, hourIndex, subjectIndex, teacherIndex, roomIndex, moreInformationIndex;
        List<String> headline = rows.get(0).select("td").eachText();
        for (int i = 0; i < headline.size(); i++) {
            headline.set(i, headline.get(i).trim());
        }
        courseIndex = headline.indexOf("Klasse");
        hourIndex = headline.indexOf("Stunde");
        subjectIndex = headline.indexOf("Fach");
        teacherIndex = headline.indexOf("Vertretung");
        roomIndex = headline.indexOf("Raum");
        moreInformationIndex = headline.indexOf("Sonstiges");

        for (int i = 1; i < rows.size(); i++) {
            Elements content = rows.get(i).select("td");
            String course = courseIndex >= 0 ? content.get(courseIndex).text().trim() : "";
            String hour = hourIndex >= 0 ? content.get(hourIndex).text().trim() : "";
            String subject = subjectIndex >= 0 ? content.get(subjectIndex).text().trim() : "";
            String teacher = teacherIndex >= 0 ? content.get(teacherIndex).text().trim() : "";
            String room = roomIndex >= 0 ? content.get(roomIndex).text().trim() : "";
            String moreInformation = moreInformationIndex >= 0 ? content.get(moreInformationIndex).text() : "";
            entries.add(
                    new SubstitutionEntry(
                            course,
                            hour,
                            subject,
                            teacher,
                            room,
                            moreInformation
                    )
            );
        }

        return entries;
    }

    /**
     * @param doc        raw HTML-Document (Jsoup), which will be analyzed
     * @param senior     boolean: if active the returned List will sorted differently, like this new String[]{Hours, class, sit-in, room, information, subject}  |  else new String[]{hours, subject, sit-in, room, information, class}
     * @param classNames List: The class names, which the substiution plan should be searched for
     * @return a filtered List of the Subsitution, with only matching classes
     * @see #getSubstitutionListUnfiltered
     */
    //specific
    @NonNull
    static SubstitutionList getSubstitutionListFiltered(@Nullable Document doc, boolean senior, @Nullable ArrayList<String> classNames) {
        if (doc == null || classNames == null) {
            return new SubstitutionList(true);
        }

        SubstitutionList oldList = getSubstitutionListUnfiltered(doc);
        List<SubstitutionEntry> list;
        if (senior)
            list = filterCourses(classNames, oldList);
        else
            list = filterClass(classNames.get(0), oldList);

        return new SubstitutionList(list);
    }

    private static List<SubstitutionEntry> filterClass(String className, SubstitutionList list) {
        List<SubstitutionEntry> newList = new ArrayList<>();
        String classLetter = "" + className.charAt(className.length() - 1);
        String classNumber = className.substring(0, className.length() - 1);
        for (int i = 0; i < list.size(); i++) {
            String entryCourse = list.getEntries().get(i).getCourse();
            if (entryCourse.contains(classLetter) && entryCourse.contains(classNumber))
                newList.add(list.getEntries().get(i));
        }
        return newList;
    }

    private static List<SubstitutionEntry> filterCourses(List<String> courses, SubstitutionList list) {
        List<SubstitutionEntry> newList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (courses.contains(list.getEntries().get(i).getCourse()))
                newList.add(list.getEntries().get(i));
        }
        return newList;
    }
}



