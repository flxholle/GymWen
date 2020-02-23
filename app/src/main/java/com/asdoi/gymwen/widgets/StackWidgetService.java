package com.asdoi.gymwen.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.asdoi.gymwen.R;
import com.google.gson.Gson;

public class StackWidgetService extends RemoteViewsService {
    public static final String content_id = "1010";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new VertretungBothRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class VertretungBothRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    Context mContext;
    int mAppWidgetId;

    public VertretungBothRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {
    }

    public void onDestroy() {
    }

    public int getCount() {
        return 2;
    }

    public RemoteViews getViewAt(int position) {
        String[][] inhalt = new String[][]{{"Hallo"}, {"Baum"}};
        switch (position) {
            default:
            case 1:
                //Today
            case 2:
                //Tomorrow
        }
        return generateTableSpecific(mContext, inhalt);
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount() {
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

    RemoteViews generateTableSpecific(Context context, String[][] inhalt) {
        RemoteViews base = new RemoteViews(context.getPackageName(), R.layout.fragment_vertretung);

        if (inhalt != null && inhalt.length > 0) {
            //Overview
//            base.addView(R.id.vertretung_linear_layout_layer1, generateOverviewSpecific());
            //Add Overview to content string

            base.addView(R.id.vertretung_linear_layout_layer1, new RemoteViews(context.getPackageName(), R.layout.listview));

            // Set up the intent that starts the StackViewService, which will
            // provide the views for this collection.
            Intent intent = new Intent(context, SpecificVertretungWidgetService.class);
            // Add the app widget ID to the intent extras.
            intent.putExtra(StackWidgetService.content_id, new Gson().toJson(inhalt));
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            base.setRemoteAdapter(R.id.widget_service_listview, intent);
        }
        return base;
    }
}