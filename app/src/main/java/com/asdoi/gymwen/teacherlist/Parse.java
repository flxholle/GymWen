package com.asdoi.gymwen.teacherlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Locale;

/**
 * Class that parses the teacherlist
 */
abstract class Parse {
    /**
     * @param doc raw HTML-Document (Jsoup), which will be analyzed
     * @return an two-dimensal String-Array with all teachers in it, one teacher entry is like this: String[]{Kürzel, Nachname, Vorname, Sprechstunde}
     * @see TeacherlistFeatures
     */
    @NotNull
    static TeacherList getList(@Nullable Document doc) {
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
            TeacherList teacherList = new TeacherList();
            for (int i = 0; i < lines.length; i++) {
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
                teacherList.add(new TeacherListEntry(content[i][0], content[i][1], content[i][2], content[i][3]));
            }

            return teacherList;
        } catch (Exception e) {
            e.printStackTrace();
            return new TeacherList(true);
        }
    }

    /**
     * @param search     String that should be found in the listString (normally a part of the teacher-array, like the Kürzel)
     * @param listString The list in which it should search for the query (normally the teacherlist)
     * @return if no match is found: null  |  else: the first match
     */
    @Nullable
    static TeacherListEntry getTeacher(@NonNull String search, @NonNull TeacherList listString) {
        if (listString.getNoInternet())
            return null;
        for (TeacherListEntry s : listString.getEntries()) {
            if (search.equalsIgnoreCase(s.getShort()))
                return s;
            else if (search.equalsIgnoreCase(s.getName()))
                return s;
            else if (search.equalsIgnoreCase(s.getFirst_name()))
                return s;
            else if (search.equalsIgnoreCase(s.getMeeting()))
                return s;
        }

        //If not found
        return null;
    }

    /**
     * @param search     String that should be found in the listString (normally a part of the teacher-array, like the Kürzel)
     * @param listString The list in which it should search for the query (normally the teacherlist)
     * @return if no match is found: null  |  else: all matches in array
     * @see TeacherlistFeatures
     */
    @NonNull
    static TeacherList getTeachers(@NonNull String search, @NonNull TeacherList listString) {
        TeacherList list = new TeacherList();
        for (TeacherListEntry s : listString.getEntries()) {

            String s1 = s.getShort().toUpperCase(Locale.getDefault());
            if (s1.contains(search.toUpperCase(Locale.getDefault()))) {
                list.add(s);
                break;
            }

            s1 = s.getName().toUpperCase(Locale.getDefault());
            if (s1.contains(search.toUpperCase(Locale.getDefault()))) {
                list.add(s);
                break;
            }

            s1 = s.getFirst_name().toUpperCase(Locale.getDefault());
            if (s1.contains(search.toUpperCase(Locale.getDefault()))) {
                list.add(s);
                break;
            }

        }

        return list;
    }


    /**
     * @param doc same like getTeachers just with a Document as parameter
     * @see #getTeachers
     */
    @Nullable
    protected static TeacherListEntry getTeacher(@NonNull String search, Document doc) {
        return getTeacher(search, getList(doc));
    }
}
