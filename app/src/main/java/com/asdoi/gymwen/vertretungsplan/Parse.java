package com.asdoi.gymwen.vertretungsplan;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

abstract class Parse {

    //Title
    protected static String[] getTitle(Document doc) {

        if (doc == null) {
            System.out.println("Authentication failed! at getting Title");
            return new String[]{""};
        }

        Elements values = doc.select("h2");
        String[] matches = new String[values.size()];

        for (int i = 0; i < values.size(); i++) {
            Element elem = values.get(i);
//            System.out.println(elem.text());
            matches[i] = elem.text();
        }

        String title;
        if (matches.length > 1) {
            return new String[]{""};
        } else {
            title = matches[0];
        }

        //Analyze String
        title = title.replaceAll("Vertretungsplan für ", "");

        return title.split(",");
    }

    //All
    protected static String[][] getList(Document doc) {

        if (doc == null) {
            System.out.println("Document is null");
            return null;
        }

        Elements values = doc.select("tr");
        int columNr = 6;

        String[] docInhalt = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            docInhalt[i] = "" + values.get(i);
        }

        String[][] line = new String[docInhalt.length - 2][20];
        for (int i = 2; i < line.length + 2; i++) {
//            Element elem = values.get(i);
//            System.out.println(elem.text());
            int indexBegin = 0;
            int indexEnd = 2;
            for (int j = 0; 0 == 0; j++) {
                line[i - 2][j] = "";

//                System.out.println(docInhalt[i].indexOf(">", indexBegin) + 1);
//                System.out.println(docInhalt[i].indexOf("<", indexEnd));
                indexBegin = docInhalt[i].indexOf(">", indexBegin + 1);
                indexEnd = docInhalt[i].indexOf("<", indexEnd + 1);
                if (indexBegin > indexEnd) {
                    break;
                }
                line[i - 2][j] = docInhalt[i].substring(indexBegin + 1, indexEnd);
//                System.out.println(line[i - 2][j]);

            }
            line[i - 2] = removeValues(line[i - 2], columNr);
        }


        //Analyze String
        String[][] inhalt = new String[line.length][columNr];

        for (int i = 0; i < inhalt.length; i++) {
            for (int j = 0; j < inhalt[0].length; j++) {
                if (line[i][j] == null) {
                    inhalt[i][j] = "";
                } else {
                    inhalt[i][j] = line[i][j];
                }
            }
        }

        return inhalt;
    }

    //specific
    protected static String[][] getList(Document doc, boolean oberstufe, ArrayList<String> classNames) {

        if (doc == null) {
            System.out.println("Authentication failed! at getting Classes");
            return null;
        }

        Elements values = doc.select("tr");

        String[] docInhalt = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            docInhalt[i] = "" + values.get(i);
        }

        int columNr = 6;

        String[][] line = new String[docInhalt.length - 2][20];
        for (int i = 2; i < line.length + 2; i++) {
//            Element elem = values.get(i);
//            System.out.println(elem.text());
            int indexBegin = 0;
            int indexEnd = 2;
            for (int j = 0; true; j++) {
                line[i - 2][j] = "";

//                System.out.println(docInhalt[i].indexOf(">", indexBegin) + 1);
//                System.out.println(docInhalt[i].indexOf("<", indexEnd));
                indexBegin = docInhalt[i].indexOf(">", indexBegin + 1);
                indexEnd = docInhalt[i].indexOf("<", indexEnd + 1);
                if (indexBegin > indexEnd) {
                    break;
                }
                line[i - 2][j] = docInhalt[i].substring(indexBegin + 1, indexEnd);
//                System.out.println(line[i - 2][j]);

            }
            line[i - 2] = removeValues(line[i - 2], columNr);
        }

        for (int i = 0; i < line.length; i++) {
            line[i][0] = line[i][0].trim();
        }

        //Analyze String

        String[][] inhalt = new String[line.length][columNr];

        for (int i = 0; i < inhalt.length; i++) {
            for (int j = 0; j < inhalt[0].length; j++) {
                if (line[i][j] == null) {
                    inhalt[i][j] = "";
                } else {
                    inhalt[i][j] = line[i][j];
                }
            }
        }


        String[][] yourInhalt = new String[line.length][columNr];
        int j = 0;
        for (int i = 0; i < inhalt.length; i++) {
            if (oberstufe) {
                if (classNames.contains("" + inhalt[i][0])) {
                    yourInhalt[j] = inhalt[i];
                    j++;
                }
            } else if (inhalt[i][0].length() > 1) {
                //For courses like 10c
                //System.out.println(inhalt[i][0].charAt(1));
                if (!Character.isLetter(inhalt[i][0].charAt(1))) {
                    if (classNames.contains("" + inhalt[i][0].charAt(0) + inhalt[i][0].charAt(1))) {
                        for (int z = 2; z < inhalt[i][0].length(); z++) {
                            if (classNames.contains("" + inhalt[i][0].charAt(z))) {
                                yourInhalt[j] = inhalt[i];
                                j++;
                                break;
                            }
                        }
                    }
                }
                //For courses like 9b
                else {
                    if (classNames.contains("" + inhalt[i][0].charAt(0))) {
                        for (int z = 1; z < inhalt[i][0].length(); z++) {
                            if (classNames.contains("" + inhalt[i][0].charAt(z))) {
                                yourInhalt[j] = inhalt[i];
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

        String[][] trimmedInhalt = new String[j][columNr];
        for (int i = 0; i < j; i++) {
            trimmedInhalt[i] = yourInhalt[i];
        }


        return trimmedInhalt;

    }

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



