package com.ulan.timetable;

import android.content.Context;
import android.content.Intent;

import com.ulan.timetable.activities.MainActivity;
import com.ulan.timetable.utils.DBUtil;

public class TimeTableBuilder {
    public static String CUSTOM_THEME = "customTheme";
    public static String DB_NAME = "dbName";

    private int customTheme = -1;
    private String dbName;


    public TimeTableBuilder(int pos) {
        setDBName(pos);
    }

    public TimeTableBuilder withActivityTheme(int theme) {
        customTheme = theme;
        return this;
    }

    public TimeTableBuilder setDBName(int value) {
        dbName = DBUtil.database_prefix + value;
        return this;
    }

    /**
     * intent() method to build and create the intent with the set params
     *
     * @return the intent to start the activity
     */
    public Intent intent(Context context, Class cl) {
        Intent i = new Intent(context, cl);
        i.putExtra(CUSTOM_THEME, customTheme);
        i.putExtra(DB_NAME, dbName);

        return i;
    }

    /**
     * start() method to start the application
     */
    public void start(Context ctx) {
        Intent i = intent(ctx, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    public void start(Context ctx, Class cls) {
        Intent i = intent(ctx, cls);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }


}
