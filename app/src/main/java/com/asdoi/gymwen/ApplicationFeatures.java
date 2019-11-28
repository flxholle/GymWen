package com.asdoi.gymwen;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;

import com.asdoi.gymwen.main.ChoiceActivity;
import com.asdoi.gymwen.main.SignInActivity;
import com.asdoi.gymwen.services.NotificationService;
import com.asdoi.gymwen.vertretungsplanInternal.VertretungsPlan;
import com.asdoi.gymwen.widgets.VertretungsplanWidget;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;
import org.acra.annotation.AcraToast;
import org.acra.data.StringFormat;
import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;

@AcraCore(buildConfigClass = BuildConfig.class,
        reportFormat = StringFormat.JSON)
@AcraMailSender(mailTo = "GymWenApp@t-online.de")
@AcraDialog(resText = R.string.acra_dialog_text,
        resCommentPrompt = R.string.acra_dialog_content,
        resTheme = R.style.AppTheme,
        resTitle = R.string.acra_dialog_title)
@AcraToast(resText = R.string.acra_toast)

public class ApplicationFeatures extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        ACRA.init(this);
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static Context getContext() {
        return mContext;
    }

    public static boolean initSettings(boolean isWidget, boolean signIn) {
        Context context = getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean signedIn = sharedPref.getBoolean("signed", false);

        if (signedIn) {
            boolean oberstufe = sharedPref.getBoolean("oberstufe", true);
            String courses = sharedPref.getString("courses", "");
            if (courses.trim().isEmpty()) {
                Intent i = new Intent(context, ChoiceActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                return signedIn;
            }
            VertretungsPlan.setup(oberstufe, courses.split("#"), courses);

//            System.out.println("settings: " + oberstufe + courses);

            String username = sharedPref.getString("username", "");
            String password = sharedPref.getString("password", "");

            VertretungsPlan.signin(username, password);
            if (!isWidget) {
                proofeNotification();
                updateMyWidgets();
            }
        } else if (signIn) {
            Intent i = new Intent(context, SignInActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        return signedIn;
    }

    public static void proofeNotification() {
        Context context = getContext();
        Intent intent = new Intent(context, NotificationService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        try {
        context.stopService(intent);
        context.startService(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public static void updateMyWidgets() {
        Context context = getContext();
        AppWidgetManager man = AppWidgetManager.getInstance(context);
        int[] ids = man.getAppWidgetIds(new ComponentName(context, VertretungsplanWidget.class));
        Intent updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(VertretungsplanWidget.WIDGET_ID_KEY, ids);
        context.sendBroadcast(updateIntent);
    }

    public static Runnable downloadRunnable(final boolean isWidget, final boolean signIn) {
        return new Runnable() {
            @Override
            public void run() {
                downloadDocs(isWidget, signIn);
            }
        };
    }

    public static void downloadDocs(boolean isWidget, boolean signIn) {

        //DownloadDocs
        if (!VertretungsPlan.areDocsDownloaded() && ApplicationFeatures.isNetworkAvailable()) {
            if (!ApplicationFeatures.initSettings(true, signIn)) {
                return;
            }
            String[] strURL = new String[]{VertretungsPlan.todayURL, VertretungsPlan.tomorrowURL};
            Document[] doc = new Document[strURL.length];
            for (int i = 0; i < 2; i++) {

                String authString = VertretungsPlan.strUserId + ":" + VertretungsPlan.strPasword;

                String lastAuthString = VertretungsPlan.lastAuthString;
                //Check if already tried logging in with this authentication and if it failed before, return null
                if (lastAuthString.length() > 1 && lastAuthString.substring(0, lastAuthString.length() - 1).equals(authString) && lastAuthString.charAt(lastAuthString.length() - 1) == 'f') {
                    System.out.println("failed before with same authString");
                    //return doc;
                }

                String encodedString =
                        new String(Base64.encodeBase64(authString.getBytes()));

                try {
                    doc[i] = Jsoup.connect(strURL[i])
                            .header("Authorization", "Basic " + encodedString)
                            .get();

                    VertretungsPlan.lastAuthString = authString + "t";


                } catch (IOException e) {
                    e.printStackTrace();
                    VertretungsPlan.lastAuthString = authString + "f";
                    return;
                }
            }
            VertretungsPlan.setDocs(doc[0], doc[1]);
            if (!isWidget) {
                proofeNotification();
                updateMyWidgets();
            }
        }
    }

    public static class downloadDocsTask extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... params) {
            if (params == null || params.length < 2) {
                if (params.length == 1)
                    params = new Boolean[]{params[0], true};
            }
            downloadDocs(params[0], params[1]);
            return null;
        }
    }

    public static class downloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public downloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            if (!urldisplay.trim().isEmpty()) {
                Bitmap mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
//                Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
                return mIcon11;
            }
            return null;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public static Bitmap vectorToBitmap(@DrawableRes int resVector) {
        Context context = getContext();
        Drawable drawable = AppCompatResources.getDrawable(context, resVector);
        Bitmap b = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        drawable.setBounds(0, 0, c.getWidth(), c.getHeight());
        drawable.draw(c);
        return b;
    }
}
