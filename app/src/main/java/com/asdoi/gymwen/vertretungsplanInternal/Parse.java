package com.asdoi.gymwen.vertretungsplanInternal;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class Parse {

    public static ArrayList<String> generateOberstufeCodes(String[] courseNames) {
        ArrayList<String> returnValue = new ArrayList<>();
        for (String s : courseNames) {
            returnValue.add(s);
        }

        return returnValue;
    }

    public static ArrayList<String> generateClassCodes(String className) {
//        if (className.length() > 3 || className.length() <= 1) {
        if (className.length() <= 1) {
            System.out.println("Wrong class format");
            return null;
        }

        ArrayList<String> returnValue = new ArrayList<String>();

        if (className.length() > 2) {
            returnValue.add("" + className.charAt(0) + className.charAt(1));
            returnValue.add("" + className.charAt(2));
        } else {
            returnValue.add("" + className.charAt(0));
            returnValue.add("" + className.charAt(1));
        }


        return returnValue;
    }

    //specific
    public static String[][] analyzeHTMLCode(ArrayList<String> classNames, boolean oberstufe, Document doc) {

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

    public static String[] putInArray(String value, int postion, String[] array) {
        if (postion > array.length) {
            System.out.println("position größer als Array+1");
            return null;
        }
        String[] returnValue = new String[array.length + 1];
        for (int i = 0; i < postion; i++) {
            returnValue[i] = array[i];
        }
        returnValue[postion] = value;
        for (int i = postion + 1; i < array.length; i++) {
            returnValue[i] = array[i];
        }

        return returnValue;
    }

    public static String[] clearArray(String value, String[] array) {
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(array));
        list.remove(value);
        return list.toArray(new String[0]);
    }

    public static String[] removeValues(String[] array, int length) {
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

    //Title
    public static String[] analyzeHTMLCodeTitle(Document doc) {

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
    public static String[][] analyzeHTMLCodeAll(Document doc) {

        if (doc == null) {
            System.out.println("Authentication failed! at getting all");
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

    static Document dc = null;

    public static String lastAuthString = "";

    //Credits to:
    //https://www.javacodeexamples.com/jsoup-basic-authentication-example/808
    public static Document getDocument(final String strURL) throws Exception {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Document doc;
                /*
                 * User id, password string needs to be in
                 * userid:password format with no space
                 * in between them
                 */
                String authString = VertretungsPlan.strUserId + ":" + VertretungsPlan.strPasword;

//                System.out.println("auth: " + authString);

                //Check if already tried logging in with this authentication and if it failed before, return null
                if (lastAuthString.length() > 1 && lastAuthString.substring(0, lastAuthString.length() - 1).equals(authString) && lastAuthString.charAt(lastAuthString.length() - 1) == 'f') {
                    System.out.println("failed before with same authString");
                    return;
                }

                //encode the authString using base64
                String encodedString =
                        new String(Base64.encodeBase64(authString.getBytes()));

                /*
                 * connect to the website using Jsoup
                 * and provide above value in Authorization header
                 */

                try {
                    doc = Jsoup.connect(strURL)
                            .header("Authorization", "Basic " + encodedString)
                            .get();

                    System.out.println("Logged in using basic authentication");
                    lastAuthString = authString + "t";
                    dc = doc;


                } catch (IOException e) {
//                    e.printStackTrace();
                    lastAuthString = authString + "f";
                    dc = null;
                }


            }
        }).start();

        if (lastAuthString.charAt(lastAuthString.length() - 1) == 'f') {
            return null;
        }

        //Sleep is very important, otherwise doc will be the value it had before
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.getStackTrace();
        }


        return dc;
    }

    static Document dcCheck = null;

    public static boolean checkConnection(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document doc = null;

                try {
                    doc = Jsoup.connect("http://gym-wen.de/startseite").get();

                    System.out.println("Logged in using basic authentication");
                    dcCheck = doc;


                } catch (IOException e) {
//                    e.printStackTrace();
                    dcCheck = null;
                }

            }
        }).start();

        //Sleep is very important, otherwise doc will be the value it had before
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.getStackTrace();
        }

        return dcCheck != null;


    }

//
//    public static Document getDocument() {
//        Document doc = null;
//        new downloadImageTask((Document) doc)
//                .execute("st");
//    }
//
//    private class Test {
//        Document doc = null;
//
//        void start() {
//
//            new downloadImageTask((Document) doc).execute("");
//        }
//    }

//    private class downloadImageTask extends AsyncTask<String, Void, Document> {
//
//        public downloadImageTask() {
//        }
//
//        public interface AsyncResponse {
//            void processFinish(String output);
//        }
//
//        protected Document doInBackground(String... strURL) {
//            Document doc = null;
//
//            String authString = VertretungsPlan.strUserId + ":" + VertretungsPlan.strPasword;
//
////                System.out.println("auth: " + authString);
//
//            //Check if already tried logging in with this authentication and if it failed before, return null
//            if (lastAuthString.length() > 1 && lastAuthString.substring(0, lastAuthString.length() - 1).equals(authString) && lastAuthString.charAt(lastAuthString.length() - 1) == 'f') {
//                System.out.println("failed before with same authString");
//                return null;
//            }
//
//            //encode the authString using base64
//            String encodedString =
//                    new String(Base64.encodeBase64(authString.getBytes()));
//
//
//            try {
//                doc = Jsoup.connect(strURL[0])
//                        .header("Authorization", "Basic " + encodedString)
//                        .get();
//
//                System.out.println("Logged in using basic authentication");
//                lastAuthString = authString + "t";
//                dc = doc;
//
//
//            } catch (IOException e) {
////                    e.printStackTrace();
//                lastAuthString = authString + "f";
//                dc = null;
//            }
//            return doc;
//        }
//
//        protected void onPostExecute(Document result) {
//            doc.setImageBitmap(result);
//        }
//    }


}



