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
import com.asdoi.gymwen.ui.fragments.VertretungFragment;

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
        //For both today and tomorrow:
        String[][] inhalt = new String[][]{{"Hallo"}, {"Baum"}};
        boolean oberstufe = false;
        return generateTableSpecific(context, inhalt, oberstufe);
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
    }

    RemoteViews generateTableSpecific(Context context, String[][] inhalt, boolean oberstufe) {
        RemoteViews base = new RemoteViews(context.getPackageName(), R.layout.fragment_vertretung);

        boolean sonstiges = VertretungFragment.isSonstiges(inhalt);


        if (inhalt != null && inhalt.length > 0) {
            //Overview
//            base.addView(R.id.vertretung_linear_layout_layer1, generateOverviewSpecific());
            //Add Overview to content string

            for (int i = 0; i < inhalt.length; i++) {
                base.addView(R.id.vertretung_linear_layout_layer1, getEntrySpecific(inhalt[i], oberstufe, sonstiges));
            }

        }
        return base;
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
}
