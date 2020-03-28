/*
 * Copyright (c) 2020 Felix Hollederer
 *     This file is part of GymWenApp.
 *
 *     GymWenApp is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GymWenApp is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GymWenApp.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.asdoi.gymwen.ui.activities;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.view.View;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.widgets.SummaryWidget;

public class SummaryWidgetActivity extends SubstitutionWidgetActivity {
    @Override
    public void onStart() {
        super.onStart();
        findViewById(R.id.fab).setOnClickListener((View v) -> {
            savePref();

            new Thread(() -> {
                ApplicationFeatures.downloadSubstitutionplanDocsAlways(true, true);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
                SummaryWidget.Companion.updateAppWidget(getContext(), appWidgetManager, appWidgetId);

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }).start();

        });
    }
}
