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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;

public class ImportActivity extends ActivityFeatures {

    @Override
    public void setupColors() {

    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            //uri = intent.getStringExtra("URI");
            Uri uri2 = intent.getData();
            String uri = uri2.getEncodedPath() + "  complete: " + uri2.toString();
//            TextView textView = (TextView)findViewById(R.id.textView);
//            textView.setText(uri);
            // now you call whatever function your app uses
            // to consume the txt file whose location you now know
        } else {
            String TAG = "TagOpenTxt";
            Log.d(TAG, "intent was something else: " + action);
        }
    }
}
