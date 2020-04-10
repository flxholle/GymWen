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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Class that parses the substitution plan, it gets information like title and entries
 */
abstract class Parse {

    //Title
    //Internal parse methods

    /**
     * @param doc raw HTML-Document (Jsoup), which will be analyzed
     * @return an array with all information of the title of the substitution plan, with the length 3!
     * @see SubstitutionPlan where it will be sorted in method getTitleArray()
     */
    @NonNull
    private static String[] getTitleArrayUnsorted(@Nullable Document doc) {

        if (doc == null) {
//            System.out.println("Authentication failed! at getting Title");
            return new String[]{""};
        }

        try {
            Elements values = doc.select("h2");
            String[] matches = new String[values.size()];

            for (int i = 0; i < values.size(); i++) {
                Element elem = values.get(i);
                matches[i] = elem.text();
            }

            String title;
            if (matches.length > 1) {
                return new String[]{""};
            } else {
                title = matches[0];
            }

            //Analyze String
            title = title.replaceAll("Substitutionplan für ", "");

            return title.split(",");
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{""};
        }
    }

    /**
     * @param doc raw HTML-Document (Jsoup), which will be analyzed
     * @return an unsorted String with all the analyzed information separated by " "
     * @see #getTitleArrayUnsorted
     */
    @Nullable
    private static String getTitleAsStringUnsorted(Document doc) {
        try {
            String[] dayTitle = Parse.getTitleArrayUnsorted(doc);
            StringBuilder returnValue = new StringBuilder();
            for (String s : dayTitle) {
                returnValue.append(s).append(" ");
            }
            if ((returnValue.length() == 0) || returnValue.toString().replace(" ", "").isEmpty())
                return null;

            return returnValue.substring(0, returnValue.length() - 1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param doc           raw HTML-Document (Jsoup), which will be analyzed
     * @param showWeekdates boolean: if the specific day should also be shown, if the date is in the past
     * @param today         The name of today in the specific language
     * @param tomorrow      The name of tomorrow in the specific language
     * @param laterDay      The name of "day is in the past" in the specific language
     * @return an sorted Array of all title information, with the length 3. Like this: new String[]{Date, DateName (Weekday), WeekNr}
     * @see #getTitleArrayUnsorted
     */
    //Date, DateName, WeekNr
    @NonNull
    private static SubstitutionTitle getTitleWithoutCode(Document doc, boolean showWeekdates, String today, String tomorrow, String laterDay) {
        try {
            SubstitutionTitle day = new SubstitutionTitle();

            String dayString = getTitleAsStringUnsorted(doc);
            if (dayString == null) {
                return new SubstitutionTitle(true);
            }
            char[] dayArray = dayString.toCharArray();

            //Date
            int start = -1, end = -1;

            for (int i = 0; i < dayArray.length; i++) {
                if (Character.isDigit(dayArray[i])) {
                    start = i;
                    break;
                }
            }
            if (start < 0 || start == dayArray.length - 1)
                return new SubstitutionTitle(true);

            for (int i = start + 1; i < dayArray.length; i++) {
                if (!Character.isDigit(dayArray[i]) && dayArray[i] != '.') {
                    end = i;
                    break;
                }
            }

            day.setDate(dayString.substring(start, end));


            //Weekday
            try {
                DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                SimpleDateFormat simpleDateformat = new SimpleDateFormat("EEEE", Locale.getDefault()); // the dayArray of the week spelled out completely

                Date startDate = removeTime(df.parse(day.getDate()));

                day.setDayOfWeek(simpleDateformat.format(startDate));
                Date currentDate = removeTime(new Date());

                if (currentDate.after(startDate)) {
                    //If date is in past
//                    return new String[]{day[0], showWeekdates ? day[1] + " " + laterDay : laterDay};
                    day.setDayOfWeek(showWeekdates ? day.getDayOfWeek() + " " + laterDay : laterDay);
                } else if (currentDate.equals(startDate)) {
                    //If date is today
                    day.setDayOfWeek(showWeekdates ? day.getDayOfWeek() + " (" + today + ")" : today);
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
                        day.setDayOfWeek(showWeekdates ? day.getDayOfWeek() + " (" + tomorrow + ")" : tomorrow);
                    }
                }

            } catch (ParseException e) {
                day.setDayOfWeek(dayString.substring(0, start - 1));
            }


            //Week Number
            if (end == dayArray.length - 1)
                return day;

            try {
                start = dayString.indexOf("Woche") + "Woche".length();
            } catch (
                    Exception e) {
                for (int i = end + 1; i < dayArray.length; i++) {
                    if (Character.isLetter(dayArray[i])) {
                        start = i;
                        break;
                    }
                }
            }

            if (start < 0 || start == dayArray.length - 1)
                return day;


            try {
                end = start + 1;
                day.setWeek(dayString.substring(start, end));
            } catch (
                    Exception e) {
                end = -1;
                for (int i = start + 1; i < dayArray.length; i++) {
                    if (!Character.isLetter(dayArray[i])) {
                        end = i;
                        break;
                    }
                }
                day.setWeek(dayString.substring(start, end));
            }

            return day;
        } catch (Exception e) {
            e.printStackTrace();
            return new SubstitutionTitle(true);
        }
    }


    //Methods that should be used
    @NonNull
    static SubstitutionTitle getTitle(Document doc, boolean showWeekdates, String today, String tomorrow, String laterDay, int pastCode, int todayCode, int tomorrowCode, int futureCode) {
        SubstitutionTitle day = getTitleWithoutCode(doc, showWeekdates, today, tomorrow, laterDay);
        try {
            //Weekday
            try {
                DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                Date startDate = removeTime(df.parse(day.getDate()));

                Date currentDate = removeTime(new Date());

                if (currentDate.after(startDate)) {
                    //If date is in past
//                    return new String[]{day[0], showWeekdates ? day[1] + " " + laterDay : laterDay};
                    day.setTitleCode(pastCode);
                    return day;
                } else if (currentDate.equals(startDate)) {
                    //If date is today
                    day.setTitleCode(todayCode);
                    return day;
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
                        day.setTitleCode(tomorrowCode);
                        return day;
                    }
                }

            } catch (ParseException e) {
                day.setTitleCode(futureCode);
                return day;
            }
            day.setTitleCode(futureCode);
        } catch (Exception e) {
            e.printStackTrace();
            day.setTitleCode(pastCode);
            return day;
        }
        return day;
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

        Elements values = doc.select("tr");
        int columnNr = 6;

        String[] docContent = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            docContent[i] = "" + values.get(i);
        }

        String[][] line = new String[docContent.length - 2][20];
        for (int i = 2; i < line.length + 2; i++) {
            int indexBegin = 0;
            int indexEnd = 2;

            for (int j = 0; true; j++) {
                line[i - 2][j] = "";
                indexBegin = docContent[i].indexOf(">", indexBegin + 1);
                indexEnd = docContent[i].indexOf("<", indexEnd + 1);
                if (indexBegin > indexEnd) {
                    break;
                }
                line[i - 2][j] = docContent[i].substring(indexBegin + 1, indexEnd);

            }
            line[i - 2] = removeValues(line[i - 2], columnNr);
        }


        //Analyze String
        String[][] content = new String[line.length][columnNr];

        for (int i = 0; i < content.length; i++) {
            for (int j = 0; j < content[0].length; j++) {
                if (line[i][j] == null) {
                    content[i][j] = "";
                } else {
                    content[i][j] = line[i][j];
                }
            }
        }

        SubstitutionList substitutionList = new SubstitutionList();
        for (String[] con : content) {
            SubstitutionEntry entry = new SubstitutionEntry(con[0], con[1], con[2], con[3], con[4], con[5]);
            substitutionList.add(entry);
        }

        return substitutionList;
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

        Elements values = doc.select("tr");

        String[] docContent = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            docContent[i] = "" + values.get(i);
        }

        int columNr = 6;

        String[][] line = new String[docContent.length - 2][20];
        for (int i = 2; i < line.length + 2; i++) {
//            Element elem = values.get(i);
//            System.out.println(elem.text());
            int indexBegin = 0;
            int indexEnd = 2;
            for (int j = 0; true; j++) {
                line[i - 2][j] = "";

//                System.out.println(docContent[i].indexOf(">", indexBegin) + 1);
//                System.out.println(docContent[i].indexOf("<", indexEnd));
                indexBegin = docContent[i].indexOf(">", indexBegin + 1);
                indexEnd = docContent[i].indexOf("<", indexEnd + 1);
                if (indexBegin > indexEnd) {
                    break;
                }
                line[i - 2][j] = docContent[i].substring(indexBegin + 1, indexEnd);
//                System.out.println(line[i - 2][j]);

            }
            line[i - 2] = removeValues(line[i - 2], columNr);
        }

        for (int i = 0; i < line.length; i++) {
            line[i][0] = line[i][0].trim();
        }

        //Analyze String

        String[][] content = new String[line.length][columNr];

        for (int i = 0; i < content.length; i++) {
            for (int j = 0; j < content[0].length; j++) {
                if (line[i][j] == null) {
                    content[i][j] = "";
                } else {
                    content[i][j] = line[i][j];
                }
            }
        }


        String[][] yourContent = new String[line.length][columNr];
        int j = 0;
        for (String[] strings : content) {
            if (senior) {
                if (classNames.contains("" + strings[0].trim())) {
                    yourContent[j] = strings;
                    j++;
                }
            } else if (strings[0].length() > 1) {
                //For courses like 10c
                //System.out.println(content[i][0].charAt(1));
                if (!Character.isLetter(strings[0].charAt(1))) {
                    if (classNames.contains("" + strings[0].charAt(0) + strings[0].charAt(1))) {
                        for (int z = 2; z < strings[0].length(); z++) {
                            if (classNames.contains("" + strings[0].charAt(z))) {
                                yourContent[j] = strings;
                                j++;
                                break;
                            }
                        }
                    }
                }
                //For courses like 9b
                else {
                    if (classNames.contains("" + strings[0].charAt(0))) {
                        for (int z = 1; z < strings[0].length(); z++) {
                            if (classNames.contains("" + strings[0].charAt(z))) {
                                yourContent[j] = strings;
                                j++;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (j == 0) {
            return new SubstitutionList();
        }

        String[][] trimmedContent = new String[j][columNr];
        System.arraycopy(yourContent, 0, trimmedContent, 0, j);

        SubstitutionList substitutionList = new SubstitutionList();
        for (String[] con : trimmedContent) {
            SubstitutionEntry entry = new SubstitutionEntry(con[0], con[1], con[2], con[3], con[4], con[5]);
            substitutionList.add(entry);
        }

        return substitutionList;
    }

    /**
     * @param array  String array form which the values after the given length should be deleted
     * @param length the length the returned array should have
     * @return array with all entries after the length parameter deleted
     */
    @NonNull
    private static String[] removeValues(@NonNull String[] array, int length) {
        int multplier = 2;
        String[] returnValue = new String[length];
        int j = 0;
        //remove every second value
        for (int i = 1; i < array.length; i += multplier) {
            returnValue[j] = array[i];
            if (j == returnValue.length - 1) {
                break;
            } else {
                j++;
            }
        }

        return returnValue;
    }
}



