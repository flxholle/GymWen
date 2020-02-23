package com.asdoi.gymwen.widgets;

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
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;
import com.google.gson.Gson;

public class SpecificVertretungWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        String[][] content = new Gson().fromJson(intent.getExtras().getString(StackWidgetService.content_id), String[][].class);
        return new VertretungSpecificRemoteViewsFactory(this.getApplicationContext(), content, VertretungFragment.isSonstiges(content));
    }
}

class VertretungSpecificRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    Context context;
    String[][] content;
    boolean sons;
    boolean oberstufe = VertretungsPlanFeatures.getOberstufe();

    public VertretungSpecificRemoteViewsFactory(Context context, String[][] content, boolean sons) {
        this.context = context;
        this.content = content;
        this.sons = sons;
    }

    public void onCreate() {
    }

    public void onDestroy() {
    }

    public int getCount() {
        return 1;
    }

    public RemoteViews getViewAt(int position) {
        if (content.length > 0 || content == null) {
            //Nothing
            return null;
        } else
            return getEntrySpecific(content[position], oberstufe, sons);
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


