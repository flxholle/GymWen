package com.ulan.timetable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.ulan.timetable.activities.MainActivity;

public class TimeTableBuilder {
    public static String CUSTOM_THEME = "customTheme";
    public static String DB_NAME = "dbName";

    private int customTheme = -1;
    private static String dbName = "databaseName";


    public TimeTableBuilder(String dbName) {
        TimeTableBuilder.dbName = dbName;
    }

    public TimeTableBuilder withActivityTheme(int theme) {
        customTheme = theme;
        return this;
    }

    public TimeTableBuilder setDBName(String value) {
        TimeTableBuilder.dbName = value;
        return this;
    }

    /**
     * intent() method to build and create the intent with the set params
     *
     * @return the intent to start the activity
     */
    public Intent intent(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        i.putExtra(CUSTOM_THEME, customTheme);
        i.putExtra(DB_NAME, dbName);

        return i;
    }

    /**
     * start() method to start the application
     */
    public void start(Context ctx) {
        Intent i = intent(ctx);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }


    public static String getDBName(Activity activity) {
        try {
            String name = activity.getIntent().getExtras().getString(DB_NAME, null);
            if (name == null)
                name = activity.getParentActivityIntent().getExtras().getString(DB_NAME, null);

            if (name == null) {
                return dbName;
            } else {
                return name;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dbName;
    }

    public static String getDBName() {
        return dbName;
    }
}
