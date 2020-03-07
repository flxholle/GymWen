package com.asdoi.gymwen.substitutionplan;

import android.content.Context;

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

    boolean senior;
    ArrayList<String> courses;
    boolean hours;
    Document todayDoc;
    Document tomorrowDoc;

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
    public void reCreate(boolean hours, String... courses) {
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
    private static String laterDay() {
        return context.getString(R.string.day_past);
    }

    private static String noSubstitutionAll() {
        return context.getString(R.string.nothing_all);
    }

    private static String noSubstitution() {
        return context.getString(R.string.nothing);
    }

    private static String noInternet() {
        return context.getString(R.string.noInternetConnection);
    }

    private static String today() {
        return context.getString(R.string.today);
    }

    private static String tomorrow() {
        return context.getString(R.string.tomorrow);
    }

    private static boolean showWeekDate() {
        return PreferenceUtil.showWeekDate();
    }


    /**
     * @param today: boolean if the title of today or tomorrow should be analyzed
     * @return an sorted Array of all title information, with the length 3. Like this: new String[]{Date, DateName (Weekday), WeekNr}
     * @see Parse#getTitleArraySorted
     */
    //DayArrays
    //Date, DateName, WeekNr
    public String[] getTitleArray(boolean today) {
        String[] s = Parse.getTitleArraySorted(today ? todayDoc : tomorrowDoc, showWeekDate(), today(), tomorrow(), laterDay());
        if (s == null) {
            return new String[]{"", "", "", ""};
        }
        return s;
    }

    /**
     * @param today: boolean if the title of today or tomorrow should be analyzed
     * @return an sorted String with all the analyzed information separated by " "
     * @see Parse#getTitleStringSorted
     * @see #getTitleArray
     */
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
    public String[][] getDay(boolean today) {
        try {
            String[][] inhalt = null;

            if (today) {
                inhalt = Parse.getSubstitutionList(todayDoc, senior, courses);
            } else {
                inhalt = Parse.getSubstitutionList(tomorrowDoc, senior, courses);
            }

            if (inhalt != null) {
                try {
                    inhalt = sortArray(inhalt);
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }

            if (hours) {
                inhalt = changeToTime(inhalt);
            }

            return inhalt;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getDayString(boolean today) {
        try {
            String[][] inhalt = getDay(today);

            if (inhalt == null) {
                return noSubstitution();
            }

            String todayString = "";

            //System.out.println verarbeitung
            String[] spalten = new String[inhalt.length];
            for (int i = 0; i < inhalt.length; i++) {
                spalten[i] = "";
                for (int j = 0; j < 5; j++) {
                    spalten[i] += inhalt[i][j] + " ";
                    todayString += inhalt[i][j] + " ";
                }
                todayString += "\n";
            }
            for (String s : spalten) {
                System.out.println(s);
            }

            return todayString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @param today: boolean if the plan of today or tomorrow should be analyzed
     * @return an tow-dimensional String array with every entry of the plan. An entry has the same order like the plan online. My one looks like this: new String[]{class, hour, subject, sit-in, room, moreInformation}
     */
    public String[][] getAll(boolean today) {
        try {
            String[][] inhalt = null;


            if (today) {
                inhalt = Parse.getSubstitutionList(todayDoc);
            } else {
                inhalt = Parse.getSubstitutionList(tomorrowDoc);
            }

            if (hours) {
                inhalt = changeToTime(inhalt);
            }

            return inhalt;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getAllString(boolean today) {
        try {
            String[][] inhalt = getAll(today);

            if (inhalt == null) {
                return noSubstitutionAll();
            }

            String todayString = "";
            String[] spalten = new String[inhalt.length];
            for (int i = 0; i < inhalt.length; i++) {
                spalten[i] = "";
                for (int j = 0; j < 5; j++) {
                    spalten[i] += inhalt[i][j] + " ";
                    todayString += inhalt[i][j] + " ";
                }
                todayString += "\n";
            }
            for (String s : spalten) {
                System.out.println(s);
            }

            return todayString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String[][] sortArray(String[][] value) {
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
     * @param value a substituion plan array with hours as the second entry
     * @return a plan array with all hours replaced with their matching times
     */
    //Times
    private String[][] changeToTime(String[][] value) {
        if (value == null)
            return null;

        for (int i = 0; i < value.length; i++) {
            try {
                value[i][1] = getMatchingTime(Integer.parseInt(value[i][1]));
            } catch (Exception e) {
            }
        }

        return value;
    }

    /**
     * @param lesson the lesson
     * @return the matching time
     */
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
    public ArrayList<String> getCourses() {
        return courses;
    }

    private static ArrayList<String> generateCourseList(String... courses) {
        if (courses.length == 1) {
            return generateClassCodes(courses[0]);
        }
        return generateSeniorCodes(courses);
    }

    private static ArrayList<String> generateSeniorCodes(String[] courseNames) {
        ArrayList<String> returnValue = new ArrayList<>();
        for (String s : courseNames) {
            returnValue.add(s);
        }

        return returnValue;
    }

    private static ArrayList<String> generateClassCodes(String className) {
//        if (className.length() > 3 || className.length() <= 1) {
        if (className.length() <= 1) {
            System.out.println("Wrong class format");
            return null;
        }

        ArrayList<String> returnValue = new ArrayList<String>();

        if (className.length() > 2) {
            returnValue.add("" + className.charAt(0) + className.charAt(1));
            returnValue.add("" + className.charAt(2));
        } else {
            returnValue.add("" + className.charAt(0));
            returnValue.add("" + className.charAt(1));
        }


        return returnValue;
    }
}
