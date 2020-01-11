package com.asdoi.gymwen.vertretungsplan;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;

import org.jsoup.nodes.Document;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

class Vertretungsplan {

    boolean oberstufe;
    ArrayList<String> courses;
    boolean hours;
    Document todayDoc;
    Document tomorrowDoc;

    public Vertretungsplan() {
    }

    public Vertretungsplan(boolean hours, String... courses) {
        reCreate(hours, courses);
    }

    public void reCreate(boolean hours, String... courses) {
        this.oberstufe = courses.length > 1;
        this.courses = generateCourseList(courses);
        this.hours = hours;
    }

    private static String laterDay() {
        return ApplicationFeatures.getContext().getString(R.string.day_past);
    }

    private static String noVertretungAll() {
        return ApplicationFeatures.getContext().getString(R.string.nothing_all);
    }

    private static String noVertretung() {
        return ApplicationFeatures.getContext().getString(R.string.nothing);
    }

    private static String noInternet() {
        return ApplicationFeatures.getContext().getString(R.string.noInternetConnection);
    }

    private static String today() {
        return ApplicationFeatures.getContext().getString(R.string.today);
    }

    private static String tomorrow() {
        return ApplicationFeatures.getContext().getString(R.string.tomorrow);
    }

    private static boolean showWeekDate() {
        return ApplicationFeatures.showWeekDate();
    }


    //DayArrays
    private String getTitleStringRaw(boolean today) {
        String[] dayTitle = Parse.getTitle(today ? todayDoc : tomorrowDoc);
        String returnValue = "";
        if (dayTitle == null || dayTitle.equals("")) {
            return noInternet();
        }
        for (String s : dayTitle) {
            returnValue += s + " ";
        }
        if (returnValue.isEmpty() || returnValue.replace(" ", "").isEmpty())
            return noInternet();

        return returnValue.substring(0, returnValue.length() - 1);
    }

    //Date, DateName, WeekNr
    public String[] getTitleArray(boolean today) {
        String[] day = new String[3];
        String dayString = getTitleStringRaw(today);
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
            return null;

        for (int i = start + 1; i < dayArray.length; i++) {
            if (!Character.isDigit(dayArray[i]) && dayArray[i] != '.') {
                end = i;
                break;
            }
        }

        day[0] = dayString.substring(start, end);


        //Weekday
        try {
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            SimpleDateFormat simpleDateformat = new SimpleDateFormat("EEEE", Locale.getDefault()); // the dayArray of the week spelled out completely

            Date startDate = removeTime(df.parse(day[0]));

            day[1] = simpleDateformat.format(startDate);
            Date currentDate = removeTime(new Date());

            if (currentDate.after(startDate)) {
                //If date is in past
                return new String[]{day[0], showWeekDate() ? day[1] + " " + laterDay() : laterDay()};
            } else if (currentDate.equals(startDate)) {
                //If date is today
                day[1] = today();
            } else {
                //If date is tomorrow
                //Set current date to one day in the future
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 2);

                //Check if currentdate equals date -> Tomorrow
                currentDate = cal.getTime();
                if (currentDate.equals(startDate)) {
                    day[1] = showWeekDate() ? day[1] + " (" + tomorrow() + ")" : tomorrow();
                }
            }

        } catch (ParseException e) {
            day[1] = dayString.substring(0, start - 1);
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
            day[2] = dayString.substring(start, end);
        } catch (
                Exception e) {
            end = -1;
            for (int i = start + 1; i < dayArray.length; i++) {
                if (!Character.isLetter(dayArray[i])) {
                    end = i;
                    break;
                }
            }
            day[2] = dayString.substring(start, end);
        }

        return day;
    }

    private static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public String getTitleString(boolean today) {
        String[] dayTitle = getTitleArray(today);
        if (dayTitle == null || dayTitle.equals("") || dayTitle.length <= 0) {
            return noInternet();
        }

        String returnValue = dayTitle[0];
        switch (dayTitle.length) {
            default:
            case 3:
                for (int i = 1; i < dayTitle.length; i++) {
                    returnValue += ", " + dayTitle[i];
                }
                break;
            case 2:
                returnValue = dayTitle[1] + " " + dayTitle[0];
        }

        if (returnValue.isEmpty() || returnValue.replace(" ", "").isEmpty())
            return noInternet();

        return returnValue;
    }

    public String[][] getDay(boolean today) {
        String[][] inhalt = null;

        if (today) {
            inhalt = Parse.getList(todayDoc, oberstufe, courses);
        } else {
            inhalt = Parse.getList(tomorrowDoc, oberstufe, courses);
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
    }

    public String getDayString(boolean today) {
        String[][] inhalt = getDay(today);

        if (inhalt == null) {
            return noVertretung();
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

    }

    public String[][] getAll(boolean today) {
        String[][] inhalt = null;


        if (today) {
            inhalt = Parse.getList(todayDoc);
        } else {
            inhalt = Parse.getList(tomorrowDoc);
        }

        if (hours) {
            inhalt = changeToTime(inhalt);
        }

        return inhalt;
    }

    public String getAllString(boolean today) {
        String[][] inhalt = getAll(today);

        if (inhalt == null) {
            return noVertretungAll();
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


    public boolean getOberstufe() {
        return oberstufe;
    }


    //Documents
    public void setTodayDoc(Document value) {
        todayDoc = value;
    }

    public void setTomorrowDoc(Document value) {
        tomorrowDoc = value;
    }

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
        return generateOberstufeCodes(courses);
    }

    private static ArrayList<String> generateOberstufeCodes(String[] courseNames) {
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
