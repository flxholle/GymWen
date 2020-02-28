package com.asdoi.gymwen.widgets;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.core.content.ContextCompat;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.Profile;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.ui.fragments.VertretungFragment;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;
import com.asdoi.gymwen.vertretungsplan.Vertretungsplan;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class VertretungWidgetService extends RemoteViewsService {
    public static final String content_id = "1010";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new com.asdoi.gymwen.widgets.RemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class RemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private boolean noInternet = false;
    private String[][] inhaltToday;
    private String today;
    private boolean nothingToday = false;
    private boolean sonstigesToday = false;
    private String[][] inhaltTomorrow;
    private String tomorrow;
    private boolean nothingTomorrow = false;
    private boolean sonstigesTomorrow = false;
    private boolean oberstufe;
    private String profileName;

    private ArrayList<Integer> profileLenghts;

    protected RemoteViewsFactory(Context context, Intent intent) {
        this.context = context;
    }

    public void onCreate() {
        if (!ProfileManagement.isLoaded())
            ProfileManagement.reload();
//        if (!coursesCheck(false))
//            noInternet = true;
        profileLenghts = new ArrayList<Integer>();
        for (int i = 0; i < ProfileManagement.sizeProfiles(); i++) {
            onCreate(ProfileManagement.getProfile(i));
            profileLenghts.add(getLengthOfThisProfile() + 1);
        }
        if (profileLenghts.size() == 1)
            profileLenghts.set(0, profileLenghts.get(0) - 1);
        onCreate(ProfileManagement.getProfile(0));

    }

    private void reset() {
        inhaltToday = null;
        today = "";
        nothingToday = false;
        sonstigesToday = false;
        inhaltTomorrow = null;
        tomorrow = "";
        nothingTomorrow = false;
        sonstigesTomorrow = false;
        oberstufe = false;
        profileName = "";
    }

    private void onCreate(Profile p) {
        reset();
        Vertretungsplan temp = VertretungsPlanFeatures.createTempVertretungsplan(ApplicationFeatures.isHour(), p.getCourses().split(Profile.coursesSeparator));

        oberstufe = temp.getOberstufe();
        profileName = p.getName();

        //Today
        inhaltToday = temp.getDay(true);
//        nothingToday = false;
        if (inhaltToday == null)
            noInternet = true;
        else {
            today = temp.getTitleString(true);
            noInternet = false;

            if (inhaltToday.length <= 0)
                nothingToday = true;
            else
                sonstigesToday = VertretungFragment.isSonstiges(inhaltToday);
        }


        //Tomorrow
        inhaltTomorrow = temp.getDay(false);
//        nothingTomorrow = false;
        if (inhaltTomorrow == null)
            noInternet = true;
        else {
            tomorrow = temp.getTitleString(false);
            noInternet = false;

            if (inhaltTomorrow.length <= 0)
                nothingTomorrow = true;
            else
                sonstigesTomorrow = VertretungFragment.isSonstiges(inhaltTomorrow);
        }
    }

    public void onDestroy() {
    }

    private int getLengthOfThisProfile() {
        if (noInternet)
            return 1;
        else
            return (nothingToday ? 2 : inhaltToday.length + 2) + (nothingTomorrow ? 2 : inhaltTomorrow.length + 2);
    }

    public int getCount() {
        if (noInternet)
            return 1;
        int returnValue = 0;
        for (int i : profileLenghts) {
            returnValue += i;
        }
        return returnValue;
    }

    private static final int nothingCode = -7;
    private static final int isInTodayArray = -3;
    private static final int isInTomorrowArray = -4;
    private static final int isTodayHeadline = -5;
    private static final int isTomorrowHeadline = -6;
    private static final int isTodayDay = -8;
    private static final int isTomorrowDay = -9;
    private static final int isProfileHeadline = -10;

    public RemoteViews getViewAt(int position) {
        if (noInternet)
            return getTitleText(context, context.getString(R.string.noInternetConnection));

        int profilePos = checkProfilePosition(position);
        int parsePos = parsePosition(profilePos);
        switch (parsePos) {
            case nothingCode:
                return getNothing(context, context.getString(R.string.nothing));
            case isTodayHeadline:
                return getHeadline(generateHeadline(context, true, oberstufe), sonstigesToday);
            case isTomorrowHeadline:
                return getHeadline(generateHeadline(context, true, oberstufe), sonstigesTomorrow);
            default:
            case isInTodayArray:
                return getEntrySpecific(inhaltToday[profilePos - 2], oberstufe, sonstigesToday);
            case isInTomorrowArray:
                return getEntrySpecific(inhaltTomorrow[profilePos - inhaltToday.length - 4], oberstufe, sonstigesTomorrow);
            case isTodayDay:
                return getTitleText(context, today);
            case isTomorrowDay:
                return getTitleText(context, tomorrow);
            case isProfileHeadline:
                return getTitleText(context, profileName);
        }
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        onCreate();
    }

    private int parsePosition(int position) {
        int nothingTodayCode = nothingCode;
        int nothingTomorrowCode = nothingCode;

        if (position == isProfileHeadline)
            return position;

        if (nothingToday) {
            if (position == 0)
                return isTodayDay;
            else if (position == 1)
                return nothingTodayCode;
            else if (position == 2)
                return isTomorrowDay;
            else if (position == 3) {
                if (nothingTomorrow)
                    return nothingTomorrowCode;
                else
                    return isTomorrowHeadline;
            } else
                return isInTomorrowArray;
        } else if (nothingTomorrow) {
            if (position == 0)
                return isTodayDay;
            else if (position == 1)
                return isTodayHeadline;
            else if (position < inhaltToday.length + 2)
                return isInTodayArray;
            else if (position == inhaltToday.length + 2)
                return isTodayDay;
            else
                return nothingTomorrowCode;
        } else {
            if (position == 0)
                return isTodayDay;
            else if (position == 1)
                return isTodayHeadline;
            else if (position < inhaltToday.length + 2)
                return isInTodayArray;
            else if (position == inhaltToday.length + 2)
                return isTomorrowDay;
            else if (position == inhaltToday.length + 3)
                return isTomorrowHeadline;
            else
                return isInTomorrowArray;
        }
    }

    private int selectedProfile = 0;

    private int checkProfilePosition(int position) {
        int range = 0;
        for (int i = 0; i < profileLenghts.size(); i++) {
            range += profileLenghts.get(0);
            //Cause position is counting from 0, the range has to start at 0
            if (position < range) {
                if (i != selectedProfile) {
                    onCreate(ProfileManagement.getProfile(i));
                    selectedProfile = i;
                }
                break;
            }
        }
        //Cause position is counting from 0, the range has to start at 0
//        range--;

//        if (position <= range) {
        int pos = position;
        for (int i = 0; i < selectedProfile; i++) {
            pos -= profileLenghts.get(i);
        }

        if (pos == 0 && profileLenghts.size() > 1)
            return isProfileHeadline;
        else if (profileLenghts.size() > 1)
            return pos - 1;
        else
            return pos;
//        } else {
//            onCreate(ProfileManagement.getProfile(selectedProfile + 1));
//            selectedProfile++;
//            return isProfileHeadline;
//        }
    }

    //From VertretungFragment
    private RemoteViews getTitleText(@NotNull Context context, String text) {
        RemoteViews view = getRemoteViews(context);
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewHour, View.GONE);
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewSubject, View.GONE);
        view.setTextViewText(R.id.vertretung_specific_entry_textViewTeacher, text);
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, 25);
        view.setTextColor(R.id.vertretung_specific_entry_textViewTeacher, VertretungWidgetProvider.textColorPrimary);
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewRoom, View.GONE);
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewOther, View.GONE);
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewClass, View.GONE);
//        view.setOnClickPendingIntent(R.id.widget_entry_linear, VertretungWidgetProvider.getPendingSelfIntent(context, VertretungWidgetProvider.WIDGET_ON_CLICK));
        return view;
    }

    private RemoteViews getHeadline(@NotNull String[] headline, boolean sonstiges) {
        RemoteViews view = getRemoteViews(context);

        int textSize = 17;

        view.setTextViewText(R.id.vertretung_specific_entry_textViewHour, headline[0]);
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewHour, TypedValue.COMPLEX_UNIT_SP, textSize);

        view.setTextViewText(R.id.vertretung_specific_entry_textViewSubject, headline[1]);
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewSubject, TypedValue.COMPLEX_UNIT_SP, textSize);

        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, textSize);
        view.setTextViewText(R.id.vertretung_specific_entry_textViewTeacher, headline[2]);

        view.setTextViewText(R.id.vertretung_specific_entry_textViewRoom, headline[3]);
        view.setTextColor(R.id.vertretung_specific_entry_textViewRoom, VertretungWidgetProvider.textColorSecondary);
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewRoom, TypedValue.COMPLEX_UNIT_SP, textSize);

        view.setViewVisibility(R.id.vertretung_specific_entry_textViewOther, sonstiges ? View.VISIBLE : View.GONE);
        view.setTextViewText(R.id.vertretung_specific_entry_textViewOther, headline[4]);
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewOther, TypedValue.COMPLEX_UNIT_SP, textSize);

        view.setTextViewText(R.id.vertretung_specific_entry_textViewClass, headline[5]);
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewClass, TypedValue.COMPLEX_UNIT_SP, 10);

//        view.setOnClickPendingIntent(R.id.widget_entry_linear, VertretungWidgetProvider.getPendingSelfIntent(context, VertretungWidgetProvider.WIDGET_ON_CLICK));
        return view;
    }

    private RemoteViews getEntrySpecific(String[] entry, boolean oberstufe, boolean sonstiges) {
        RemoteViews view = getRemoteViews(context);

        view.setTextViewText(R.id.vertretung_specific_entry_textViewHour, entry[1]);
        view.setTextViewText(R.id.vertretung_specific_entry_textViewSubject, oberstufe ? entry[0] : entry[2]);

        if (!(entry[3].equals("entfällt") || entry[3].equals("entf"))) {
            view.setTextViewText(R.id.vertretung_specific_entry_textViewTeacher, entry[3]);

            view.setViewVisibility(R.id.vertretung_specific_entry_textViewRoom, View.VISIBLE);

            SpannableString content = new SpannableString(entry[4]);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            view.setTextViewText(R.id.vertretung_specific_entry_textViewRoom, content);
        } else {
            SpannableString content = new SpannableString(entry[3]);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            view.setTextViewText(R.id.vertretung_specific_entry_textViewTeacher, content);
//            view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, 20);
            view.setTextColor(R.id.vertretung_specific_entry_textViewTeacher, ContextCompat.getColor(context, R.color.colorAccent));
//            view.setViewVisibility(R.id.vertretung_specific_entry_textViewRoom, View.GONE);
        }

        view.setViewVisibility(R.id.vertretung_specific_entry_textViewOther, sonstiges ? View.VISIBLE : View.GONE);
        view.setTextViewText(R.id.vertretung_specific_entry_textViewOther, entry[5]);

        view.setTextViewText(R.id.vertretung_specific_entry_textViewClass, oberstufe ? entry[2] : entry[0]);

//        view.setOnClickPendingIntent(R.id.widget_entry_linear, VertretungWidgetProvider.getPendingSelfIntent(context, VertretungWidgetProvider.WIDGET_ON_CLICK));
        return view;
    }

    @NotNull
    private RemoteViews getRemoteViews(@NotNull Context context) {
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_list_entry);
        resetView(view);
        return view;
    }

    private RemoteViews getNothing(@NotNull Context context, String text) {
        RemoteViews view = getRemoteViews(context);
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewHour, View.GONE);
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewSubject, View.GONE);
        view.setTextViewText(R.id.vertretung_specific_entry_textViewTeacher, text);
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, 21);
        view.setTextColor(R.id.vertretung_specific_entry_textViewTeacher, VertretungWidgetProvider.textColorSecondary);
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewRoom, View.GONE);
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewOther, View.GONE);
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewClass, View.GONE);
//        view.setOnClickPendingIntent(R.id.widget_entry_linear, VertretungWidgetProvider.getPendingSelfIntent(context, VertretungWidgetProvider.WIDGET_ON_CLICK));
        return view;
    }

    private void resetView(RemoteViews view) {
        view.setViewVisibility(R.id.vertretung_specific_entry_textViewHour, View.VISIBLE);
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewHour, TypedValue.COMPLEX_UNIT_SP, 36);
        view.setTextViewText(R.id.vertretung_specific_entry_textViewHour, "");
        view.setTextColor(R.id.vertretung_specific_entry_textViewHour, Color.WHITE);

        view.setViewVisibility(R.id.vertretung_specific_entry_textViewSubject, View.VISIBLE);
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewSubject, TypedValue.COMPLEX_UNIT_SP, 18);
        view.setTextViewText(R.id.vertretung_specific_entry_textViewSubject, "");
        view.setTextColor(R.id.vertretung_specific_entry_textViewSubject, VertretungWidgetProvider.textColorSecondary);

        view.setViewVisibility(R.id.vertretung_specific_entry_textViewTeacher, View.VISIBLE);
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, 14);
        view.setTextViewText(R.id.vertretung_specific_entry_textViewTeacher, "");
        view.setTextColor(R.id.vertretung_specific_entry_textViewTeacher, VertretungWidgetProvider.textColorSecondary);

        view.setViewVisibility(R.id.vertretung_specific_entry_textViewRoom, View.VISIBLE);
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewRoom, TypedValue.COMPLEX_UNIT_SP, 24);
        view.setTextViewText(R.id.vertretung_specific_entry_textViewRoom, "");
        view.setTextColor(R.id.vertretung_specific_entry_textViewRoom, ContextCompat.getColor(context, R.color.colorAccent));

        view.setViewVisibility(R.id.vertretung_specific_entry_textViewOther, View.VISIBLE);
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewOther, TypedValue.COMPLEX_UNIT_SP, 16);
        view.setTextViewText(R.id.vertretung_specific_entry_textViewOther, "");
        view.setTextColor(R.id.vertretung_specific_entry_textViewOther, VertretungWidgetProvider.textColorSecondary);

        view.setViewVisibility(R.id.vertretung_specific_entry_textViewClass, View.VISIBLE);
        view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewClass, TypedValue.COMPLEX_UNIT_SP, 12);
        view.setTextViewText(R.id.vertretung_specific_entry_textViewClass, "");
        view.setTextColor(R.id.vertretung_specific_entry_textViewClass, VertretungWidgetProvider.textColorSecondary);

    }

    private static String[] generateHeadline(Context context, boolean isShort, boolean oberstufe) {
        String[] headline;

        if (oberstufe) {
            headline = new String[]{isShort ? context.getString(R.string.hours_short_three) : context.getString(R.string.hours), isShort ? context.getString(R.string.courses_short) : context.getString(R.string.courses), isShort ? context.getString(R.string.teacher_short) : context.getString(R.string.teacher), isShort ? context.getString(R.string.room_short) : context.getString(R.string.room), context.getString(R.string.other_short), context.getString(R.string.subject)};
        } else {
            headline = new String[]{isShort ? context.getString(R.string.hours_short_three) : context.getString(R.string.hours), context.getString(R.string.subject), isShort ? context.getString(R.string.teacher_short) : context.getString(R.string.teacher), isShort ? context.getString(R.string.room_short) : context.getString(R.string.room), context.getString(R.string.other_short), isShort ? context.getString(R.string.classes_short) : context.getString(R.string.classes)};
        }

        return headline;
    }
}
