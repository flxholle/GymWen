package com.ulan.timetable;

import android.content.Context;
import android.content.Intent;

import com.asdoi.gymwen.substitutionplan.SubstitutionPlan;
import com.ulan.timetable.activities.MainActivity;
import com.ulan.timetable.utils.DBUtil;

public class TimeTableBuilder {
    public static final String CUSTOM_THEME = "customTheme";
    public static final String DB_NAME = "dbName";
    public static final String PROFILE_POS = "profilepos";
    public static final String SUBSTITUTIONPLANDOC_TODAY = "substitutionplandoctoday";
    public static final String SUBSTITUTIONPLANDOC_TOMORROW = "substitutionplandoctomorrow";

    private int customTheme = -1;
    private String dbName;
    private SubstitutionPlan substitutionPlan;
    private int profilePos;


    public TimeTableBuilder(int pos) {
        setDBName(pos);
    }

    public TimeTableBuilder withActivityTheme(int theme) {
        customTheme = theme;
        return this;
    }

    public TimeTableBuilder setDBName(int value) {
        dbName = DBUtil.database_prefix + value;
        profilePos = value;
        return this;
    }

    public TimeTableBuilder setSubstitutionplan(SubstitutionPlan value) {
        substitutionPlan = value;
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
        i.putExtra(PROFILE_POS, profilePos);
        i.putExtra(SUBSTITUTIONPLANDOC_TODAY, substitutionPlan.getTodayDoc().toString());
        i.putExtra(SUBSTITUTIONPLANDOC_TOMORROW, substitutionPlan.getTomorrowDoc().toString());

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
