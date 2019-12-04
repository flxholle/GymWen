package com.asdoi.gymwen.vertretungsplanInternal;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Arrays;

public class UserInput {

    boolean oberstufe;
    String[] courseNames;


    ArrayList<String> names;

    boolean hours;

    Document todayDoc;
    Document tomorrowDoc;

    public UserInput() {
    }

    public UserInput(boolean oberstufe, String[] courseNames, String className, boolean hours) {
        reCreate(oberstufe, courseNames, className, hours);
    }

    public void reCreate(boolean oberstufe, String[] courseNames, String className, boolean hours) {
        this.oberstufe = oberstufe;


        //Set the Attributes through graphical UserInterface

        if (oberstufe) {
            names = Parse.generateOberstufeCodes(courseNames);
        } else {
            names = Parse.generateClassCodes(className);
        }

        this.hours = hours;
    }

    public String getDay(boolean today) {
        String[][] inhalt = generateDayArray(today);

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

    public String getDayAll(boolean today) {
        String[][] inhalt = generateDayAllArray(today);

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

    public String[][] generateDayArray(boolean today) {
        String[][] inhalt = null;

        if (today) {
            inhalt = Parse.analyzeHTMLCode(names, oberstufe, todayDoc);
        } else {
            inhalt = Parse.analyzeHTMLCode(names, oberstufe, tomorrowDoc);
        }

        if (inhalt != null) {
            try {
                inhalt = sortArray(inhalt);
            } catch (Exception e) {
                e.getStackTrace();
            }
        }

        if (hours) {
            inhalt = changeHours(inhalt);
        }

        return inhalt;
    }

    public String[][] generateDayAllArray(boolean today) {
        String[][] inhalt = null;


        if (today) {
            inhalt = Parse.analyzeHTMLCodeAll(todayDoc);
        } else {
            inhalt = Parse.analyzeHTMLCodeAll(tomorrowDoc);
        }

        if (hours) {
            inhalt = changeHours(inhalt);
        }

        return inhalt;
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

    private String[][] changeHours(String[][] value) {
        if (value == null || value.length < 0)
            return null;

        for (int i = 0; i < value.length; i++) {
            try {
                value[i][1] = getMatchingHour(Integer.parseInt(value[i][1]));
            } catch (Exception e) {
            }
        }

        return value;
    }

    private String getMatchingHour(int lesson) {
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

    public String[] getTitle(boolean today) {
        if (today)
            return Parse.analyzeHTMLCodeTitle(todayDoc);
        else
            return Parse.analyzeHTMLCodeTitle(tomorrowDoc);
    }

    public boolean getOberstufe() {
        return oberstufe;
    }

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

    public void getDocumentsFromSettings() {
      /*  SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(TestApplication.getInstance());
        Gson gson = new Gson();
        String json = sharedPref.getString("todayDoc", "");
        todayDoc = gson.fromJson(json, Document.class);
        json = sharedPref.getString("tomorrowDoc", "");
        tomorrowDoc = gson.fromJson(json, Document.class);*/
    }

    public void setDocumentsToSettings() {
        /*SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(TestApplication.getInstance());
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(todayDoc);
        prefsEditor.putString("todayDoc", json);
        json = gson.toJson(tomorrowDoc);
        prefsEditor.putString("tomorrowDoc", json);
        prefsEditor.commit();*/

    }

    public ArrayList getNames() {
        return names;
    }
}
