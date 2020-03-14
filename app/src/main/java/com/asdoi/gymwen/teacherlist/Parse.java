package com.asdoi.gymwen.teacherlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Class that parses the teacherlist
 */
abstract class Parse {
    /**
     * @param doc raw HTML-Document (Jsoup), which will be analyzed
     * @return an two-dimensal String-Array with all teachers in it, one teacher entry is like this: String[]{Kürzel, Nachname, Vorname, Sprechstunde}
     * @see Teacherlist
     */
    @Nullable
    static String[][] getList(@Nullable Document doc) {
        if (doc == null) {
            System.out.println("Document is null");
            return null;
        }

        try {
            Elements values = doc.select("div#content_left").select("div.csc-default").get(1).select("p.bodytext");

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
                int indexNextWhiteSpace = line.indexOf(',', indexComma + 2);
//            if (line.contains("Dr.")) {
//                content[i][2] = line.substring(indexComma + 2 + "Dr. ".length(), indexNextWhiteSpace);
//            } else {
                content[i][2] = line.substring(indexComma + 2, indexNextWhiteSpace);
//            }

                //Sprechstunde
                content[i][3] = line.substring(indexNextWhiteSpace + 1);
            }

            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param search     String that should be found in the listString (normally a part of the teacher-array, like the Kürzel)
     * @param listString The list in which it should search for the query (normally the teacherlist)
     * @return if no match is found: null  |  else: the first match
     */
    @Nullable
    static String[] getTeacher(String search, @NonNull String[][] listString) {
        for (String[] s : listString) {
            for (String s1 : s) {
                if (s1.equalsIgnoreCase(search)) {
                    return s;
                }
            }
        }

        return null;
    }

    /**
     * @param search     String that should be found in the listString (normally a part of the teacher-array, like the Kürzel)
     * @param listString The list in which it should search for the query (normally the teacherlist)
     * @return if no match is found: null  |  else: all matches in array
     * @see Teacherlist
     */
    @NonNull
    static String[][] getTeachers(@NonNull String search, @NonNull String[][] listString) {
        ArrayList<String[]> list = new ArrayList<>();
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


    /**
     * @param doc same like getTeachers just with a Document as parameter
     * @see #getTeachers
     */
    @Nullable
    protected static String[] getTeacher(String search, Document doc) {
        return getTeacher(search, getList(doc));
    }
}
