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
