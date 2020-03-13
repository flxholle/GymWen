package com.asdoi.gymwen.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;

public class ImportActivity extends ActivityFeatures {

    @NonNull
    private String TAG = "TagOpenTxt";
    @NonNull
    private String uri = "";
    @Nullable
    private Uri uri2;

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
            uri2 = intent.getData();
            uri = uri2.getEncodedPath() + "  complete: " + uri2.toString();
//            TextView textView = (TextView)findViewById(R.id.textView);
//            textView.setText(uri);
            // now you call whatever function your app uses
            // to consume the txt file whose location you now know
        } else {
            Log.d(TAG, "intent was something else: " + action);
        }
    }
}
