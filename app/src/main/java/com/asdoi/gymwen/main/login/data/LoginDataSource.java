package com.asdoi.gymwen.main.login.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.asdoi.gymwen.VertretungsplanInternal.Parse;
import com.asdoi.gymwen.VertretungsplanInternal.VertretungsPlan;
import com.asdoi.gymwen.main.login.data.model.LoggedInUser;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    static boolean signed = true;
    public static String un, pw;

    public Result<LoggedInUser> login(final String username, final String password) {

        try {
            // TODO: handle loggedInUser authentication


            new Thread(new Runnable() {
                @Override
                public void run() {
                    Document doc;
                    /*
                     * User id, password string needs to be in
                     * userid:password format with no space
                     * in between them
                     */
                    String authString = username + ":" + password;

//                System.out.println("auth: " + authString);

                    //Check if already tried logging in with this authentication and if it failed before, return null
//                    if (Parse.lastAuthString.length() > 1 && Parse.lastAuthString.substring(0, Parse.lastAuthString.length() - 1).equals(authString) && Parse.lastAuthString.charAt(Parse.lastAuthString.length() - 1) == 'f') {
//                        System.out.println("failed before with same authString");
//                        return;
//                    } else {

                        //encode the authString using base64
                        String encodedString =
                                new String(Base64.encodeBase64(authString.getBytes()));

                        /*
                         * connect to the website using Jsoup
                         * and provide above value in Authorization header
                         */

                        try {
                            doc = Jsoup.connect(VertretungsPlan.todayURL)
                                    .header("Authorization", "Basic " + encodedString)
                                    .get();

                            System.out.println("Logged in using basic authentication");
                            Parse.lastAuthString = authString + "t";
//                            signed = true;

                            un = username;
                            pw = password;

                        } catch (IOException e) {
                            e.getStackTrace();
                            Parse.lastAuthString = authString + "f";
                            signed = false;
                            return;
                        }


//                    }
                }
            }).start();


            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.getStackTrace();
            }


            LoggedInUser fakeUser =
                    new LoggedInUser(
                            java.util.UUID.randomUUID().toString(),
                            "Schüler");

            if (signed) {
                return new Result.Success<>(fakeUser);
            } else
                return new Result.Error(new IOException("Error logging in"));

        } catch (Exception e) {
            e.getStackTrace();
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    private static class downloadDoc extends AsyncTask<String, Void, Document[]> {
        @Override
        protected Document[] doInBackground(String... strURL) {
            Document[] doc = new Document[strURL.length];
            for (int i = 0; i < strURL.length; i++) {

                String authString = VertretungsPlan.strUserId + ":" + VertretungsPlan.strPasword;

                String lastAuthString = VertretungsPlan.lastAuthString;
                //Check if already tried logging in with this authentication and if it failed before, return null
                if (lastAuthString.length() > 1 && lastAuthString.substring(0, lastAuthString.length() - 1).equals(authString) && lastAuthString.charAt(lastAuthString.length() - 1) == 'f') {
                    System.out.println("failed before with same authString");
                    return doc;
                }

                String encodedString =
                        new String(Base64.encodeBase64(authString.getBytes()));

                try {
                    doc[i] = Jsoup.connect(strURL[i])
                            .header("Authorization", "Basic " + encodedString)
                            .get();

                    System.out.println("Logged in using basic authentication");
                    VertretungsPlan.lastAuthString = authString + "t";


                } catch (IOException e) {
//                    e.printStackTrace();
                    VertretungsPlan.lastAuthString = authString + "f";
                    return null;
                }
            }

            return doc;
        }

        @Override
        protected void onPostExecute(Document[] result) {
//            new createTable().execute(result);

            //Set Document
            setDocs(result);
        }

        private void setDocs(Document[] values) {
            if (values.length == 2) {
                VertretungsPlan.setDocs(values[0], values[1]);
            } else if (values.length == 1) {
                VertretungsPlan.setTodayDoc(values[0]);
            }
        }
    }

    private boolean isNetworkAvailable() {
//        ConnectivityManager connectivityManager
//                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        return false;
    }

    public void logout() {
        // TODO: revoke authentication
    }
}
