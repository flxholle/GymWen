package com.asdoi.gymwen.VertretungsplanInternal;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;

import org.jsoup.nodes.Document;

import java.util.ArrayList;

public abstract class VertretungsPlan {
    public static String strUserId = "";
    public static String strPasword = "";

    public static String todayURL = "http://gym-wen.de/vp/heute.htm";
    public static String tomorrowURL = "http://gym-wen.de/vp/morgen.htm";

    public static String lastAuthString = "";

    public static ArrayList<String> historySaveInstance;

    //ChoiceActivity -> Step 5
//    public static String[][] choiceCourseNames = new String[][]{{"Mathe", "m"}, {"Deutsch", "d"}, {"Geschichte", "g"}, {"Sozialkunde", "sk"}, {"Sport", "spo"}, {"Religionslehre"}, {"Englisch", "e"},{"Französisch", "f"}, {"Latein", "l"}, {"Spanisch", "sp"}, {"Biologie", "b"}, {"Chemie", "c"}, {"Physik", "ph"}, {"Informatik", "inf"}, {"Geographie", "geo"}, {"Wirtschaft und Recht", "wr"}, {"Kunst", "ku"}, {"Musik", "mu"}, {"W-Seminar","W_"}, {"P-Seminar","P_"}, {"Profilfach"}};

    public static String[][] choiceCourseNames = new String[][]{{ApplicationFeatures.getContext().getString(R.string.math), ApplicationFeatures.getContext().getString(R.string.mathShort)},
            {ApplicationFeatures.getContext().getString(R.string.german), ApplicationFeatures.getContext().getString(R.string.germanShort)},
            {ApplicationFeatures.getContext().getString(R.string.social_education), ApplicationFeatures.getContext().getString(R.string.social_educationShort)},
            {ApplicationFeatures.getContext().getString(R.string.PE), ApplicationFeatures.getContext().getString(R.string.PEShort)},
            {ApplicationFeatures.getContext().getString(R.string.Religious_education), ApplicationFeatures.getContext().getString(R.string.Religious_educationShort)},
            {ApplicationFeatures.getContext().getString(R.string.english), ApplicationFeatures.getContext().getString(R.string.englishShort)},
            {ApplicationFeatures.getContext().getString(R.string.france), ApplicationFeatures.getContext().getString(R.string.franceShort)},
            {ApplicationFeatures.getContext().getString(R.string.latin), ApplicationFeatures.getContext().getString(R.string.latinShort)},
            {ApplicationFeatures.getContext().getString(R.string.spanish), ApplicationFeatures.getContext().getString(R.string.spanishShort)},
            {ApplicationFeatures.getContext().getString(R.string.biology), ApplicationFeatures.getContext().getString(R.string.biologyShort)},
            {ApplicationFeatures.getContext().getString(R.string.chemistry), ApplicationFeatures.getContext().getString(R.string.chemistryShort)},
            {ApplicationFeatures.getContext().getString(R.string.physics), ApplicationFeatures.getContext().getString(R.string.physicsShort)},
            {ApplicationFeatures.getContext().getString(R.string.programming), ApplicationFeatures.getContext().getString(R.string.programmingShort)},
            {ApplicationFeatures.getContext().getString(R.string.geography), ApplicationFeatures.getContext().getString(R.string.geographyShort)},
            {ApplicationFeatures.getContext().getString(R.string.finance), ApplicationFeatures.getContext().getString(R.string.financeShort)},
            {ApplicationFeatures.getContext().getString(R.string.art), ApplicationFeatures.getContext().getString(R.string.artShort)},
            {ApplicationFeatures.getContext().getString(R.string.music), ApplicationFeatures.getContext().getString(R.string.musicShort)},
            {ApplicationFeatures.getContext().getString(R.string.W_Seminar), ApplicationFeatures.getContext().getString(R.string.W_SeminarShort)},
            {ApplicationFeatures.getContext().getString(R.string.P_Seminar), ApplicationFeatures.getContext().getString(R.string.P_SeminarShort)},
            {ApplicationFeatures.getContext().getString(R.string.profile_subject), ApplicationFeatures.getContext().getString(R.string.profile_subjectShort)}
    };
    public static boolean checkedAtNetworkChange = false;

    private static UserInput ui = new UserInput();

    public static void setup(boolean oberstufe, String[] courseNames, String className) {
        if (ui == null) {
            ui = new UserInput(oberstufe, courseNames, className);
        }
        else{
            ui.reCreate(oberstufe, courseNames, className);
        }
//        System.out.println("HI " + className);
    }

    public static void signin(String username, String password) {
        strUserId = username;
        strPasword = password;
    }

    public static boolean isUninit(){
        return ui == null;
    }

    public static String[][] getTomorrowArray() {
        return ui.generateDayArray(false);
    }

    public static String[][] getTodayArray() {
        return ui.generateDayArray(true);
    }

    public static String[][] getTodayArrayAll() {
        return ui.generateDayAllArray(true);
    }

    public static String[][] getTomorrowArrayAll() {
        return ui.generateDayAllArray(false);
    }

    public static String getTodayTitle() {
        String returnValue = "";
        if (ui.getTitle(true) == null || ui.getTitle(true).equals("")) {
            return ApplicationFeatures.getContext().getString(R.string.noInternetConnection);
        }
        for (String s : ui.getTitle(true)) {
            returnValue += s + " ";
        }
        if (returnValue.isEmpty() || returnValue.replace(" ", "").isEmpty())
            return ApplicationFeatures.getContext().getString(R.string.noInternetConnection);
        return returnValue.substring(0, returnValue.length() - 1);
    }

    public static String getTomorrowTitle() {
        String returnValue = "";
        if (ui.getTitle(false) == null || ui.getTitle(false).equals("")) {
            return ApplicationFeatures.getContext().getString(R.string.noInternetConnection);
        }
        for (String s : ui.getTitle(false)) {
            returnValue += s + " ";
        }
        if (returnValue.isEmpty() || returnValue.replace(" ", "").isEmpty())
            return ApplicationFeatures.getContext().getString(R.string.noInternetConnection);
        return returnValue.substring(0, returnValue.length() - 1);
    }

    public static String[] getTodayTitleArray() {
        return ui.getTitle(true);
    }

    public static String[] getTomorrowTitleArray() {
        return ui.getTitle(false);
    }

    public static boolean getOberstufe() {
        return ui.getOberstufe();
    }

    public static void refresh() {
//        ui.refresh();
    }

    public static void setTodayDoc(Document value){
        ui.setTodayDoc(value);
    }

    public static void setTomorrowDoc(Document value){
        ui.setTomorrowDoc(value);
    }

    public static void setDocs(Document today,Document tomorrow){
        ui.setDocs(today,tomorrow);
    }

    public static boolean areDocsDownloaded(){
        return ui.getDoc(true) != null && ui.getDoc(false) != null;
    }

    public static void saveDocs() {
//        ui.setDocumentsToSettings();
    }

    public static void reloadDocs() {
//        ui.getDocumentsFromSettings();
    }

    public static ArrayList getNames(){
        return ui.getNames();
    }

}
