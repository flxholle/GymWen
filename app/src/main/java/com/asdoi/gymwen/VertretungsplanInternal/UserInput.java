package com.asdoi.gymwen.VertretungsplanInternal;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Arrays;

public class UserInput {

    boolean oberstufe;
    String[] courseNames;


    ArrayList<String> names;

    Document todayDoc;
    Document tomorrowDoc;

    public UserInput() {
    }

    public UserInput(boolean oberstufe,
                     String[] courseNames,
                     String className) {
        reCreate(oberstufe, courseNames, className);
    }

    public void reCreate(boolean oberstufe,
                         String[] courseNames,
                         String className) {
        this.oberstufe = oberstufe;


        //Set the Attributes through graphical UserInterface

        if (oberstufe) {
            names = Parse.generateOberstufeCodes(courseNames);
        } else {
            names = Parse.generateClassCodes(className);
        }
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
//        refresh();
        String[][] inhalt = null;


        if (today) {
            inhalt = Parse.analyzeHTMLCode(names, oberstufe, todayDoc);
        } else {
            inhalt = Parse.analyzeHTMLCode(names, oberstufe, tomorrowDoc);
        }

        /*if (inhalt != null) {
            Arrays.sort(inhalt, new Comparator<String[]>() {
                public int compare(String[] o1, String[] o2) {
                    return Double.compare(Double.parseDouble(o2[1]), Double.parseDouble(o1[1])) == Double.parseDouble(o2[1]) ? (int) Double.parseDouble(o1[1]) : (int) Double.parseDouble(o2[1]);
                }
            });
        }*/
        if (inhalt != null) {
            try {
                inhalt = sortArray(inhalt);
            } catch (Exception e) {
                e.getStackTrace();
            }
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

    public String[] getTitle(boolean today) {
        if (today)
            return Parse.analyzeHTMLCodeTitle(todayDoc);
        else
            return Parse.analyzeHTMLCodeTitle(tomorrowDoc);
    }

    public boolean getOberstufe() {
        return oberstufe;
    }

//    public void refresh() {
////        while (todayDoc == null || tomorrowDoc == null) {
//        if (Parse.checkConnection()) {
//            try {
//                todayDoc = Parse.getDocument(VertretungsPlan.todayURL);
//                tomorrowDoc = Parse.getDocument(VertretungsPlan.tomorrowURL);
//            } catch (Exception e) {
//                e.getStackTrace();
//            }
//        } else {
//            refresh();
//        }
////        }
//    }

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
