/*
 * Copyright (c) 2020 Felix Hollederer
 *     This file is part of GymWenApp.
 *
 *     GymWenApp is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GymWenApp is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GymWenApp.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.asdoi.gymwen.teacherlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.nodes.Document;

import java.util.List;
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
    static TeacherList getList(@Nullable Document doc) {
        if (doc == null) {
            System.out.println("Document is null");
            return null;
        }

        try {
            List<String> values = doc.select("div#content_left").select("div.csc-default").get(1).select("p.bodytext").eachText();
            String[] lines = values.toArray(new String[]{});

            //Analyze String
            //Kürzel - Nachname - Vorname - Sprechstunde

            String[][] content = new String[lines.length][4];
            TeacherList teacherList = new TeacherList();
            for (int i = 0; i < lines.length; i++) {
                try {
                    String line = lines[i];
                    //Kürzel
                    content[i][0] = line.substring(0, 3);

                    //Nachname
                    int indexComma = line.indexOf(',');
                    content[i][1] = line.substring(4, indexComma);

                    //Vorname
                    int indexNextWhiteSpace = line.indexOf(' ', indexComma + 2);
                    if (line.contains("Dr.")) {
                        indexNextWhiteSpace = line.indexOf(' ', indexComma + 2 + "Dr. ".length());
                        content[i][2] = line.substring(indexComma + 2 + "Dr. ".length(), indexNextWhiteSpace);
                        content[i][1] = "Dr. " + content[i][1];
                    } else {
                        content[i][2] = line.substring(indexComma + 2, indexNextWhiteSpace);
                    }

                    //Sprechstunde
                    content[i][3] = line.substring(indexNextWhiteSpace + 1);
                    teacherList.add(new TeacherListEntry(content[i][0], content[i][1], content[i][2], content[i][3]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                continue;
            }

            s1 = s.getName().toUpperCase(Locale.getDefault());
            if (s1.contains(search.toUpperCase(Locale.getDefault()))) {
                list.add(s);
                continue;
            }

            s1 = s.getFirst_name().toUpperCase(Locale.getDefault());
            if (s1.contains(search.toUpperCase(Locale.getDefault()))) {
                list.add(s);
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
