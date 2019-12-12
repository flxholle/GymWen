package com.asdoi.gymwen.lehrerliste;

import org.jsoup.nodes.Document;

public abstract class Lehrerliste {
    public static final String listUrl = "http://www.gym-wen.de/information/sprechstunden/";
    private static Document list;

    public static String[][] liste() {
        if (list == null) {
            return null;
        }

        return Parse.getList(list);
    }

    public static String[] getTeacher(String search) {
        return Parse.getTeacher(search, liste());
    }

    public static void setDoc(Document doc) {
        list = doc;
    }

    public static boolean isDownloaded() {
        return list != null;
    }
}
