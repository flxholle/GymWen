package com.asdoi.gymwen.lehrerliste;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Locale;

abstract class Parse {
    protected static String[][] getList(Document doc) {
        if (doc == null) {
            System.out.println("Document is null");
            return null;
        }

        Elements values = doc.select("div#content_left").select("div.csc-default").get(1).select("p.bodytext");

        //Remove first and last entry
        values.remove(0);
        values.remove(values.size() - 1);

        String[] lines = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            lines[i] = values.get(i).toString();
        }

        for (int i = 0; i < lines.length; i++) {
            int indexBegin = 0;
            int indexEnd = 2;
            for (int j = 0; true; j++) {
                String value = values.get(i).toString();
                indexBegin = value.indexOf(">", indexBegin + 1);
                indexEnd = value.indexOf("<", indexEnd + 1);
                if (indexBegin > indexEnd) {
                    break;
                }
                lines[i] = value.substring(indexBegin + 1, indexEnd);
            }
        }

        //Analyze String
        //Kürzel - Nachname - Vorname - Sprechstunde

        String[][] content = new String[lines.length][4];
        for (int i = 0; i < content.length; i++) {
            String line = lines[i];
            //Kürzel
            content[i][0] = line.substring(0, 3);

            //Nachname
            int indexComma = line.indexOf(',');
            content[i][1] = line.substring(4, indexComma);

            //Vorname
            int indexNextWhiteSpace = line.indexOf(' ', indexComma + 2);
            if (line.contains("Dr.")) {
                indexNextWhiteSpace = line.indexOf(' ', indexNextWhiteSpace + 1);
            }
            content[i][2] = line.substring(indexComma + 2, indexNextWhiteSpace);

            //Sprechstunde
            content[i][3] = line.substring(indexNextWhiteSpace + 1);
        }

        return content;
    }

    protected static String[] getTeacher(String search, String[][] listString) {
        for (String[] s : listString) {
            for (String s1 : s) {
                if (s1.equals(search)) {
                    return s;
                }
            }
        }

        return null;
    }

    protected static String[][] getTeachers(String search, String[][] listString) {
        ArrayList<String[]> list = new ArrayList<String[]>();
        for (String[] s : listString) {
            for (int i = 0; i < 3; i++) {
                String s1 = s[i].toUpperCase(Locale.getDefault());
                if (s1.contains(search.toUpperCase(Locale.getDefault()))) {
                    list.add(s);
                    break;
                }
            }
        }

        String[][] returnValue = new String[list.size()][];
        for (int i = 0; i < returnValue.length; i++) {
            returnValue[i] = list.get(i);
        }
        return returnValue;
    }

    protected static String[] getTeacher(String search, Document doc) {
        return getTeacher(search, getList(doc));
    }
}
