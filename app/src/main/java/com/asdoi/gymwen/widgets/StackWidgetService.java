package com.asdoi.gymwen.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.Profile;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.ui.fragments.VertretungFragment;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;
import com.asdoi.gymwen.vertretungsplan.Vertretungsplan;

import static com.asdoi.gymwen.ApplicationFeatures.coursesCheck;

public class StackWidgetService extends RemoteViewsService {
    public static final String content_id = "1010";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new VertretungBothRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class VertretungBothRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    Context context;
    int mAppWidgetId;
    int calledTimes = 0;

    public VertretungBothRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {
    }

    public void onDestroy() {
    }

    public int getCount() {
        return 1;
    }

    public RemoteViews getViewAt(int position) {
//        if (calledTimes >= 2)
//            return new RemoteViews(context.getPackageName(), R.layout.linearlayout);
//        calledTimes++;
//        downloadVertretungsplanDocs(true, false);
        if (!ProfileManagement.isLoaded())
            ProfileManagement.reload();
        if (!coursesCheck(false))
//            return getTitleText(context, context.getString(R.string.noInternetConnection));
            return getTitleText(context, context.getString(R.string.noInternetConnection));
        if (VertretungsPlanFeatures.getTodayTitle().equals(ApplicationFeatures.getContext().getString(R.string.noInternetConnection))) {
            return getTitleText(context, context.getString(R.string.noInternetConnection));
        }

        RemoteViews linearLayout = new RemoteViews(context.getPackageName(), R.layout.linearlayout);
        Profile p = ApplicationFeatures.getSelectedProfile();
        Vertretungsplan temp = VertretungsPlanFeatures.createTempVertretungsplan(ApplicationFeatures.isHour(), p.getCourses().split("#"));

        boolean oberstufe = temp.getOberstufe();

        //Today
        String[][] inhaltToday = temp.getDay(true);
        if (inhaltToday == null)
            return getTitleText(context, context.getString(R.string.noInternetConnection));
        else if (inhaltToday.length <= 0)
            linearLayout.addView(R.id.widget2_linearlayout, getTitleText(context, context.getString(R.string.nothing)));
        else {
            boolean sonstiges = VertretungFragment.isSonstiges(inhaltToday);
            linearLayout.addView(R.id.widget2_linearlayout, getEntrySpecific(VertretungFragment.generateHeadline(context, sonstiges, oberstufe, false), oberstufe, sonstiges));
            for (int i = 0; i < inhaltToday.length; i++) {
                linearLayout.addView(R.id.widget2_linearlayout, getEntrySpecific(inhaltToday[i], oberstufe, sonstiges));
            }
        }


        //Tomorrow
        String[][] inhaltTomorrow = temp.getDay(false);

        if (inhaltTomorrow == null)
            return getTitleText(context, context.getString(R.string.noInternetConnection));
        else if (inhaltTomorrow.length <= 0)
            linearLayout.addView(R.id.widget2_linearlayout, getTitleText(context, context.getString(R.string.nothing)));
        else {
            boolean sonstiges = VertretungFragment.isSonstiges(inhaltTomorrow);
            linearLayout.addView(R.id.widget2_linearlayout, getEntrySpecific(VertretungFragment.generateHeadline(context, sonstiges, oberstufe, false), oberstufe, sonstiges));
            for (int i = 0; i < inhaltTomorrow.length; i++) {
                linearLayout.addView(R.id.widget2_linearlayout, getEntrySpecific(inhaltTomorrow[i], oberstufe, sonstiges));
            }
        }

        return linearLayout;
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount() {
        //LinearLayout and TextView
        return 2;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {

    }

    //From VertretungFragment
    private RemoteViews getEntrySpecific(String[] entry, boolean oberstufe, boolean sonstiges) {
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.list_vertretung_specific_entry);

        view.setTextViewText(R.id.vertretung_specific_entry_textViewHour, entry[1]);
        view.setTextColor(R.id.vertretung_specific_entry_textViewHour, ApplicationFeatures.getAccentColor(context));

        view.setTextViewText(R.id.vertretung_specific_entry_textViewSubject, oberstufe ? entry[0] : entry[2]);

        view.setTextColor(R.id.vertretung_specific_entry_textViewRoom, ApplicationFeatures.getAccentColor(context));


        if (!(entry[3].equals("entfällt") || entry[3].equals("entf"))) {
            view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, 18);
            view.setTextViewText(R.id.vertretung_specific_entry_textViewTeacher, entry[3]);

            view.setViewVisibility(R.id.vertretung_specific_entry_textViewRoom, View.VISIBLE);

            SpannableString content = new SpannableString(entry[4]);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            view.setTextViewText(R.id.vertretung_specific_entry_textViewRoom, content);
        } else {

            SpannableString content = new SpannableString(entry[3]);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            view.setTextViewText(R.id.vertretung_specific_entry_textViewTeacher, content);
            view.setTextViewTextSize(R.id.vertretung_specific_entry_textViewTeacher, TypedValue.COMPLEX_UNIT_SP, 28);
            view.setTextColor(R.id.vertretung_specific_entry_textViewTeacher, ApplicationFeatures.getAccentColor(context));

            view.setTextViewText(R.id.vertretung_specific_entry_textViewTeacher, content);
            view.setViewVisibility(R.id.vertretung_specific_entry_textViewRoom, View.GONE);
        }

        view.setViewVisibility(R.id.vertretung_specific_entry_textViewOther, sonstiges ? View.VISIBLE : View.GONE);
        view.setTextViewText(R.id.vertretung_specific_entry_textViewOther, entry[5]);

        view.setTextViewText(R.id.vertretung_specific_entry_textViewOther, oberstufe ? entry[2] : entry[0]);

        return view;
    }

    private RemoteViews getTitleText(Context context, String text) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.title_textview);
        views.setTextColor(R.id.title_textview, ApplicationFeatures.getTextColorPrimary(context));
        views.setTextViewText(R.id.title_textview, text);
        return views;
    }
}
