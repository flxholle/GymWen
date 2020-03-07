package com.asdoi.gymwen.substitutionplan;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Class that parses the substitution plan, it gets information like title and entries
 */
abstract class Parse {

    //Title

    /**
     * @param doc raw HTML-Document (Jsoup), which will be analyzed
     * @return an array with all information of the title of the substitution plan, with the length 3!
     * @see SubstitutionPlan where it will be sorted in method getTitleArray()
     */
    protected static String[] getTitleArrayUnsorted(Document doc) {

        if (doc == null) {
//            System.out.println("Authentication failed! at getting Title");
            return new String[]{""};
        }

        try {
            Elements values = doc.select("h2");
            String[] matches = new String[values.size()];

            for (int i = 0; i < values.size(); i++) {
                Element elem = values.get(i);
                matches[i] = elem.text();
            }

            String title;
            if (matches.length > 1) {
                return new String[]{""};
            } else {
                title = matches[0];
            }

            //Analyze String
            title = title.replaceAll("Substitutionplan für ", "");

            return title.split(",");
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{""};
        }
    }

    /**
     * @param doc raw HTML-Document (Jsoup), which will be analyzed
     * @return an unsorted String with all the analyzed information separated by " "
     * @see #getTitleArrayUnsorted
     */
    protected static String getTitleAsStringUnsorted(Document doc) {
        try {
            String[] dayTitle = Parse.getTitleArrayUnsorted(doc);
            String returnValue = "";
            if (dayTitle == null || dayTitle.equals("")) {
                return null;
            }
            for (String s : dayTitle) {
                returnValue += s + " ";
            }
            if (returnValue.isEmpty() || returnValue.replace(" ", "").isEmpty())
                return null;

            return returnValue.substring(0, returnValue.length() - 1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param doc           raw HTML-Document (Jsoup), which will be analyzed
     * @param showWeekdates boolean: if the specific day should also be shown, if the date is in the past
     * @param today         The name of today in the specific language
     * @param tomorrow      The name of tomorrow in the specific language
     * @param laterDay      The name of "day is in the past" in the specific language
     * @return an sorted Array of all title information, with the length 3. Like this: new String[]{Date, DateName (Weekday), WeekNr}
     * @see #getTitleArrayUnsorted
     */
    //Date, DateName, WeekNr
    protected static String[] getTitleArraySorted(Document doc, boolean showWeekdates, String today, String tomorrow, String laterDay) {
        try {
            String[] day = new String[3];
            String dayString = getTitleAsStringUnsorted(doc);
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
                    return new String[]{day[0], showWeekdates ? day[1] + " " + laterDay : laterDay};
                } else if (currentDate.equals(startDate)) {
                    //If date is today
                    day[1] = today;
                } else {
                    //If date is tomorrow
                    //Set current date to one day in the future
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);

                    //Check if currentdate equals date -> Tomorrow
                    currentDate = cal.getTime();
                    if (currentDate.equals(startDate)) {
                        day[1] = showWeekdates ? day[1] + " (" + tomorrow + ")" : tomorrow;
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param doc           raw HTML-Document (Jsoup), which will be analyzed
     * @param showWeekdates boolean: if the specific day should also be shown, if the date is in the past
     * @param today         The name of today in the specific language
     * @param tomorrow      The name of tomorrow in the specific language
     * @param laterDay      The name of "day is in the past" in the specific language
     * @return an sorted String with all the analyzed information separated by " "
     * @see #getTitleArraySorted
     */
    protected static String getTitleStringSorted(Document doc, boolean showWeekdates, String today, String tomorrow, String laterDay) {
        try {
            String[] dayTitle = getTitleArraySorted(doc, showWeekdates, today, tomorrow, laterDay);
            if (dayTitle == null || dayTitle.equals("") || dayTitle.length <= 0) {
                return null;
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
                return null;

            return returnValue;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param date Date
     * @return param Date with removed time (only the day).
     */
    private static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }


    //Substitution plan

    /**
     * @param doc raw HTML-Document (Jsoup), which will be analyzed
     * @return an tow-dimensional String array with every entry of the plan in the html-doc. An entry has the same order like the plan in the doc. My one looks like this: new String[]{class, hour, subject, sit-in, room, moreInformation}
     */
    //All
    protected static String[][] getSubstitutionList(Document doc) {

        if (doc == null) {
            System.out.println("Document is null");
            return null;
        }

        Elements values = doc.select("tr");
        int columnNr = 6;

        String[] docContent = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            docContent[i] = "" + values.get(i);
        }

        String[][] line = new String[docContent.length - 2][20];
        for (int i = 2; i < line.length + 2; i++) {
            int indexBegin = 0;
            int indexEnd = 2;

            for (int j = 0; 0 == 0; j++) {
                line[i - 2][j] = "";
                indexBegin = docContent[i].indexOf(">", indexBegin + 1);
                indexEnd = docContent[i].indexOf("<", indexEnd + 1);
                if (indexBegin > indexEnd) {
                    break;
                }
                line[i - 2][j] = docContent[i].substring(indexBegin + 1, indexEnd);

            }
            line[i - 2] = removeValues(line[i - 2], columnNr);
        }


        //Analyze String
        String[][] content = new String[line.length][columnNr];

        for (int i = 0; i < content.length; i++) {
            for (int j = 0; j < content[0].length; j++) {
                if (line[i][j] == null) {
                    content[i][j] = "";
                } else {
                    content[i][j] = line[i][j];
                }
            }
        }

        return content;
    }

    /**
     * @param doc        raw HTML-Document (Jsoup), which will be analyzed
     * @param senior  boolean: if active the returned List will sorted differently, like this new String[]{Hours, class, sit-in, room, information, subject}  |  else new String[]{hours, subject, sit-in, room, information, class}
     * @param classNames List: The class names, which the substiution plan should be searched for
     * @return a filtered List of the Subsitution, with only matching classes
     * @see #getSubstitutionList
     */
    //specific
    protected static String[][] getSubstitutionList(Document doc, boolean senior, ArrayList<String> classNames) {
        if (doc == null || classNames == null) {
            return null;
        }

        Elements values = doc.select("tr");

        String[] docContent = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            docContent[i] = "" + values.get(i);
        }

        int columNr = 6;

        String[][] line = new String[docContent.length - 2][20];
        for (int i = 2; i < line.length + 2; i++) {
//            Element elem = values.get(i);
//            System.out.println(elem.text());
            int indexBegin = 0;
            int indexEnd = 2;
            for (int j = 0; true; j++) {
                line[i - 2][j] = "";

//                System.out.println(docContent[i].indexOf(">", indexBegin) + 1);
//                System.out.println(docContent[i].indexOf("<", indexEnd));
                indexBegin = docContent[i].indexOf(">", indexBegin + 1);
                indexEnd = docContent[i].indexOf("<", indexEnd + 1);
                if (indexBegin > indexEnd) {
                    break;
                }
                line[i - 2][j] = docContent[i].substring(indexBegin + 1, indexEnd);
//                System.out.println(line[i - 2][j]);

            }
            line[i - 2] = removeValues(line[i - 2], columNr);
        }

        for (int i = 0; i < line.length; i++) {
            line[i][0] = line[i][0].trim();
        }

        //Analyze String

        String[][] content = new String[line.length][columNr];

        for (int i = 0; i < content.length; i++) {
            for (int j = 0; j < content[0].length; j++) {
                if (line[i][j] == null) {
                    content[i][j] = "";
                } else {
                    content[i][j] = line[i][j];
                }
            }
        }


        String[][] yourContent = new String[line.length][columNr];
        int j = 0;
        for (int i = 0; i < content.length; i++) {
            if (senior) {
                if (classNames.contains("" + content[i][0])) {
                    yourContent[j] = content[i];
                    j++;
                }
            } else if (content[i][0].length() > 1) {
                //For courses like 10c
                //System.out.println(content[i][0].charAt(1));
                if (!Character.isLetter(content[i][0].charAt(1))) {
                    if (classNames.contains("" + content[i][0].charAt(0) + content[i][0].charAt(1))) {
                        for (int z = 2; z < content[i][0].length(); z++) {
                            if (classNames.contains("" + content[i][0].charAt(z))) {
                                yourContent[j] = content[i];
                                j++;
                                break;
                            }
                        }
                    }
                }
                //For courses like 9b
                else {
                    if (classNames.contains("" + content[i][0].charAt(0))) {
                        for (int z = 1; z < content[i][0].length(); z++) {
                            if (classNames.contains("" + content[i][0].charAt(z))) {
                                yourContent[j] = content[i];
                                j++;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (j == 0) {
            return new String[0][columNr];
        }

        String[][] trimmedContent = new String[j][columNr];
        for (int i = 0; i < j; i++) {
            trimmedContent[i] = yourContent[i];
        }


        return trimmedContent;


    }

    /**
     * @param array  String array form which the values after the given length should be deleted
     * @param length the length the returned array should have
     * @return array with all entries after the length parameter deleted
     */
    private static String[] removeValues(String[] array, int length) {
        int multplier = 2;
        String[] returnValue = new String[length];
        int j = 0;
        //remove every second value
        for (int i = 1; i < array.length; i += multplier) {
            returnValue[j] = array[i];
            if (j == returnValue.length - 1) {
                break;
            } else {
                j++;
            }
        }

        return returnValue;
    }
}



