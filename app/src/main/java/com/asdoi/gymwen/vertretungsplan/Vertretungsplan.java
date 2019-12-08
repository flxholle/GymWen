package com.asdoi.gymwen.vertretungsplan;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Arrays;

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


    //DayArrays
    public String[] getTitle(boolean today) {
        if (today)
            return Parse.getTitle(todayDoc);
        else
            return Parse.getTitle(tomorrowDoc);
    }

    public String getTitleString(boolean today) {
        String[] dayTitle = getTitle(today);
        String returnValue = "";
        if (dayTitle == null || dayTitle.equals("")) {
            return ApplicationFeatures.getContext().getString(R.string.noInternetConnection);
        }
        for (String s : dayTitle) {
            returnValue += s + " ";
        }
        if (returnValue.isEmpty() || returnValue.replace(" ", "").isEmpty())
            return ApplicationFeatures.getContext().getString(R.string.noInternetConnection);

        return returnValue.substring(0, returnValue.length() - 1);
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
            System.out.println("Es entfällt nichts für dich");
            return "Es entfällt nichts für dich";
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
            System.out.println("Es entfällt nichts für den gesamten Tag");
            return "Es entfällt nichts für den gesamten Tag";
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
    public ArrayList getCourses() {
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
