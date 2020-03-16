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

    public static final int pastCode = 0;
    public static final int todayCode = 1;
    public static final int tomorrowCode = 2;
    public static final int futureCode = 3;

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
     * @see SubstitutionList
     * @see #reCreate
     */
    public SubstitutionPlan(boolean hours, String... courses) {
        reCreate(hours, courses);
    }

    /**
     * @param hours   boolean if it should show the matching hours, like a 1 will be converted to 8:10
     * @param courses The class names, which the substiution plan should be searched for
     * @see SubstitutionList
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
     * @see Parse#getTitle
     */
    //DayArrays
    @Nullable
    public SubstitutionTitle getTitle(boolean today) {
        return Parse.getTitle(today ? todayDoc : tomorrowDoc, showWeekDate(), today(), tomorrow(), laterDay(), pastCode, todayCode, tomorrowCode, futureCode);
    }

    /**
     * @param today: boolean if the title of today or tomorrow should be analyzed
     * @return an sorted String with all the analyzed information separated by " "
     * @see Parse#getTitle
     * @see #getTitle
     */
    @NonNull
    public String getTitleString(boolean today) {
        SubstitutionTitle s = getTitle(today);
        return s.getNoInternet() ? noInternet() : s.toString();
    }


    /**
     * @param today: boolean if the plan of today or tomorrow should be analyzed
     * @return a filtered List of the Subsitution, with only matching classes
     * @see #getAll
     */
    //Substitution plan
    @NonNull
    public SubstitutionList getDay(boolean today) {
        SubstitutionList content;

        if (today) {
            content = Parse.getSubstitutionListFiltered(todayDoc, senior, courses);
        } else {
            content = Parse.getSubstitutionListFiltered(tomorrowDoc, senior, courses);
        }

        content.sortList();

        if (hours) {
            content.changeHourToTime();
        }

        return content;
    }


    /**
     * @param today: boolean if the plan of today or tomorrow should be analyzed
     * @return an tow-dimensional String array with every entry of the plan. An entry has the same order like the plan online. My one looks like this: new String[]{class, hour, subject, sit-in, room, moreInformation}
     */
    @Nullable
    public SubstitutionList getAll(boolean today) {
        try {
            SubstitutionList content;


            if (today) {
                content = Parse.getSubstitutionListUnfiltered(todayDoc);
            } else {
                content = Parse.getSubstitutionListUnfiltered(tomorrowDoc);
            }

            if (hours) {
                content.changeHourToTime();
            }

            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return new SubstitutionList(true);
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
        SubstitutionList oldFilteredToday = getDay(true);
        SubstitutionList oldFilteredTomorrow = getDay(false);

        //Now
        setDocs(now[0], now[1]);
        SubstitutionList nowFilteredToday = getDay(true);
        SubstitutionList nowFilteredTomorrow = getDay(false);

        if (oldFilteredToday.getNoInternet() || oldFilteredTomorrow.getNoInternet() || nowFilteredToday.getNoInternet() || nowFilteredTomorrow.getNoInternet())
            return false; //No internet

        return !(!SubstitutionList.Companion.areListsEqual(oldFilteredToday, nowFilteredTomorrow) || !SubstitutionList.Companion.areListsEqual(oldFilteredTomorrow, nowFilteredTomorrow));
    }

    public boolean hasSthChanged(Document old, Document now) {
        return hasSthChanged(old, now, true);
    }

    public boolean hasSthChanged(Document old, Document now, boolean today) {
        if (today)
            setTodayDoc(old);
        else
            setTomorrowDoc(old);
        SubstitutionList oldFiltered = getDay(today);

        if (today)
            setTodayDoc(now);
        else
            setTomorrowDoc(now);
        SubstitutionList nowFiltered = getDay(today);

        if (oldFiltered.getNoInternet() || nowFiltered.getNoInternet())
            return false; //no internet

        return !SubstitutionList.Companion.areListsEqual(oldFiltered, nowFiltered);
    }
}
