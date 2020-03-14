package com.asdoi.gymwen.substitutionplan;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.util.PreferenceUtil;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * An object which filters the substitution plan and creates easier access to the methods of the parse-class
 *
 * @see Parse
 */
public class SubstitutionPlan {

    private boolean senior;
    @Nullable
    private
    ArrayList<String> courses;
    private boolean hours;
    private Document todayDoc;
    private Document tomorrowDoc;

    public SubstitutionPlan() {
    }

    /**
     * @param hours   boolean if it should show the matching hours, like a 1 will be converted to 8:10
     * @param courses The class names, which the substiution plan should be searched for
     * @see #getMatchingTime
     * @see #reCreate
     */
    public SubstitutionPlan(boolean hours, String... courses) {
        reCreate(hours, courses);
    }

    /**
     * @param hours   boolean if it should show the matching hours, like a 1 will be converted to 8:10
     * @param courses The class names, which the substiution plan should be searched for
     * @see #getMatchingTime
     */
    public void reCreate(boolean hours, @NonNull String... courses) {
        this.senior = courses.length > 1;
        this.courses = generateCourseList(courses);
        this.hours = hours;
    }

    private static Context context = ApplicationFeatures.getContext();

    /**
     * @param value Context for the Strings
     */
    public void setContext(Context value) {
        context = value;
    }

    //Strings in specific languages, @see getTitleArray
    @NonNull
    private static String laterDay() {
        return context.getString(R.string.day_past);
    }

    @NonNull
    private static String noSubstitutionAll() {
        return context.getString(R.string.nothing_all);
    }

    @NonNull
    private static String noSubstitution() {
        return context.getString(R.string.nothing);
    }

    @NonNull
    private static String noInternet() {
        return context.getString(R.string.noInternetConnection);
    }

    @NonNull
    private static String today() {
        return context.getString(R.string.today);
    }

    @NonNull
    private static String tomorrow() {
        return context.getString(R.string.tomorrow);
    }

    private static boolean showWeekDate() {
        return PreferenceUtil.showWeekDate();
    }


    /**
     * @param today: boolean if the title of today or tomorrow should be analyzed
     * @return an sorted Array of all title information, with the length 3. Like this: new String[]{Date, DateName (Weekday), WeekNr}  |  If day is in the past it returns an array of length 2, like this new String[]{Date, DateName (Weekday)}
     * @see Parse#getTitleDayCode
     */
    //DayArrays
    @Nullable
    public String[] getTitleArray(boolean today) {
        String[] s = Parse.getTitleArraySorted(today ? todayDoc : tomorrowDoc, showWeekDate(), today(), tomorrow(), laterDay());
        if (s == null) {
            return new String[]{"", "", ""};
        }
        return s;
    }

    public int getTitleCodeValue(boolean today, int pastCode, int todayCode, int tomorrowCode, int futureCode) {
        return Parse.getTitleDayCode(today ? todayDoc : tomorrowDoc, pastCode, todayCode, tomorrowCode, futureCode);
    }

    /**
     * @param today: boolean if the title of today or tomorrow should be analyzed
     * @return an sorted String with all the analyzed information separated by " "
     * @see Parse#getTitleStringSorted
     * @see #getTitleArray
     */
    @NonNull
    public String getTitleString(boolean today) {
        String s = Parse.getTitleStringSorted(today ? todayDoc : tomorrowDoc, showWeekDate(), today(), tomorrow(), laterDay());
        return s == null ? noInternet() : s;
    }


    /**
     * @param today: boolean if the plan of today or tomorrow should be analyzed
     * @return a filtered List of the Subsitution, with only matching classes
     * @see #getAll
     */
    //Substitution plan
    @Nullable
    public String[][] getDay(boolean today) {
        try {
            String[][] content = null;

            if (today) {
                content = Parse.getSubstitutionList(todayDoc, senior, courses);
            } else {
                content = Parse.getSubstitutionList(tomorrowDoc, senior, courses);
            }

            if (content != null) {
                try {
                    content = sortArray(content);
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }

            if (hours) {
                content = changeToTime(content);
            }

            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public String getDayString(boolean today) {
        try {
            String[][] content = getDay(today);

            if (content == null) {
                return noSubstitution();
            }

            StringBuilder todayString = new StringBuilder();

            //System.out.println verarbeitung
            String[] spalten = new String[content.length];
            for (int i = 0; i < content.length; i++) {
                spalten[i] = "";
                for (int j = 0; j < 5; j++) {
                    spalten[i] += content[i][j] + " ";
                    todayString.append(content[i][j]).append(" ");
                }
                todayString.append("\n");
            }
            for (String s : spalten) {
                System.out.println(s);
            }

            return todayString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @param today: boolean if the plan of today or tomorrow should be analyzed
     * @return an tow-dimensional String array with every entry of the plan. An entry has the same order like the plan online. My one looks like this: new String[]{class, hour, subject, sit-in, room, moreInformation}
     */
    @Nullable
    public String[][] getAll(boolean today) {
        try {
            String[][] content = null;


            if (today) {
                content = Parse.getSubstitutionList(todayDoc);
            } else {
                content = Parse.getSubstitutionList(tomorrowDoc);
            }

            if (hours) {
                content = changeToTime(content);
            }

            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public String getAllString(boolean today) {
        try {
            String[][] content = getAll(today);

            if (content == null) {
                return noSubstitutionAll();
            }

            StringBuilder todayString = new StringBuilder();
            String[] spalten = new String[content.length];
            for (int i = 0; i < content.length; i++) {
                spalten[i] = "";
                for (int j = 0; j < 5; j++) {
                    spalten[i] += content[i][j] + " ";
                    todayString.append(content[i][j]).append(" ");
                }
                todayString.append("\n");
            }
            for (String s : spalten) {
                System.out.println(s);
            }

            return todayString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @NonNull
    private String[][] sortArray(@NonNull String[][] value) {
        int[] numbers = new int[value.length];
        for (int i = 0; i < value.length; i++) {
            numbers[i] = Integer.parseInt(value[i][1]);
        }
        Arrays.sort(numbers);

        String[][] returnValue = new String[value.length][value[0].length];
        int j = 0;
        while (j < numbers.length) {
            for (int i = 0; i < value.length; i++) {
                if (j < numbers.length) {
                    if (("" + numbers[j]).equals(value[i][1])) {

                        returnValue[j] = value[i];
                        j++;
                    }
                }
            }
        }

        return returnValue;
    }


    /**
     * @param value a substitution plan array with hours as the second entry
     * @return a plan array with all hours replaced with their matching times
     */
    //Times
    @Nullable
    private String[][] changeToTime(@Nullable String[][] value) {
        if (value == null)
            return null;

        for (int i = 0; i < value.length; i++) {
            try {
                value[i][1] = getMatchingTime(Integer.parseInt(value[i][1]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return value;
    }

    /**
     * @param lesson the lesson
     * @return the matching time
     */
    @NonNull
    private String getMatchingTime(int lesson) {
        switch (lesson) {
            case 1:
                return "8:10";
            case 2:
                return "8:55";
            case 3:
                return "9:55";
            case 4:
                return "10:40";
            case 5:
                return "11:40";
            case 6:
                return "12:25";
            case 7:
                return "13:15";
            case 8:
                return "14:00";
            case 9:
                return "14:45";
            case 10:
                return "15:30";
            case 11:
                return "16:15";
            default:
                //Breaks are excluded
                return ("" + (((45 * lesson) + (8 * 60) + 10) / 60)).replaceAll(",", ".");
        }
    }


    public boolean getSenior() {
        return senior;
    }

    @Nullable
    public static String[][] summarizeArray(@Nullable String[][] value, int index, @NonNull String separator) {
        if (value == null || value.length <= 0)
            return value;

        ArrayList<String[]> newValuesList = new ArrayList<>();
        for (int i = 0; i < value.length; i++) {
            if (newValuesList.size() == 0) {
                newValuesList.add(value[i]);
                continue;
            }

            String[] before = newValuesList.get(newValuesList.size() - 1);
            boolean check = checkArrays(before, value[i], index);


            if (check) {
                int valuesAddedToBefore = before[index].indexOf(separator);
                before[index] = before[index].substring(0, valuesAddedToBefore > 0 ? valuesAddedToBefore : before[index].length()) + separator + value[i][index];
                newValuesList.set(newValuesList.size() - 1, before);
            } else {
                newValuesList.add(value[i]);
            }
        }

        String[][] returnValue = new String[newValuesList.size()][value[0].length];
        for (int i = 0; i < returnValue.length; i++) {
            returnValue[i] = newValuesList.get(i);
        }
        return returnValue;
    }

    private static boolean checkArrays(@NonNull String[] value1, String[] value2, int index) {
        boolean check = true;
        for (int j = 0; j < value1.length; j++) {
            if (j != index && !value1[j].equals(value2[j])) {
                check = false;
                break;
            }
        }
        return check;
    }

    //Documents
    public void setTodayDoc(Document value) {
        todayDoc = value;
    }

    public void setTomorrowDoc(Document value) {
        tomorrowDoc = value;
    }

    /**
     * @param today    Document of the today plan (Jsoup)
     * @param tomorrow Document of the tomorrow plan (Jsoup)
     */
    public void setDocs(Document today, Document tomorrow) {
        setTodayDoc(today);
        setTomorrowDoc(tomorrow);
    }

    public Document getDoc(boolean today) {
        if (today)
            return todayDoc;
        else
            return tomorrowDoc;
    }

    /**
     * @return a boolean if the docs are not null, which means they were already downloaded
     */
    public boolean areDocsDownloaded() {
        return getDoc(true) != null && getDoc(false) != null;
    }


    //Courses
    @Nullable
    public ArrayList<String> getCourses() {
        return courses;
    }

    @Nullable
    private static ArrayList<String> generateCourseList(@NonNull String... courses) {
        if (courses.length == 1) {
            return generateClassCodes(courses[0]);
        }
        return generateSeniorCodes(courses);
    }

    @NonNull
    private static ArrayList<String> generateSeniorCodes(@NonNull String[] courseNames) {
        ArrayList<String> returnValue = new ArrayList<>();
        returnValue.addAll(Arrays.asList(courseNames));

        return returnValue;
    }

    @Nullable
    private static ArrayList<String> generateClassCodes(@NonNull String className) {
//        if (className.length() > 3 || className.length() <= 1) {
        if (className.length() <= 1) {
            System.out.println("Wrong class format");
            return null;
        }

        ArrayList<String> returnValue = new ArrayList<>();

        if (className.length() > 2) {
            returnValue.add(("" + className.charAt(0) + className.charAt(1)).trim());
            returnValue.add(("" + className.charAt(2)).trim());
        } else {
            returnValue.add(("" + className.charAt(0)).trim());
            returnValue.add(("" + className.charAt(1)).trim());
        }

        return returnValue;
    }


    //Check if sth has changed in filtered
    public boolean hasSthChanged(Document[] old) {
        return hasSthChanged(old, new Document[]{todayDoc, tomorrowDoc});
    }

    public boolean hasSthChanged(Document[] old, Document[] now) {
        if (old.length < 2 || now.length < 2)
            return false;
        //Old
        setDocs(old[0], old[1]);
        String[][] oldFilteredToday = getDay(true);
        String[][] oldFilteredTomorrow = getDay(false);

        //Now
        setDocs(now[0], now[1]);
        String[][] nowFilteredToday = getDay(true);
        String[][] nowFilteredTomorrow = getDay(false);

        if (oldFilteredToday == null || oldFilteredTomorrow == null || nowFilteredToday == null || nowFilteredTomorrow == null)
            return false; //No internet

        return !(!PlanUtils.Companion.areArraysEqual(oldFilteredToday, nowFilteredTomorrow) || !PlanUtils.Companion.areArraysEqual(oldFilteredTomorrow, nowFilteredTomorrow));
    }
}
