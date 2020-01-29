package com.asdoi.gymwen.ui.main.fragments;


import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ImpressumFragment extends Fragment {

    public ImpressumFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_impressum, container, false);
        String datenschutz = getString(R.string.privacy);

        String license = getString(R.string.license);

        TextView textView = root.findViewById(R.id.impressum_datenschutz);
        if (Build.VERSION.SDK_INT > 24)
            textView.setText(Html.fromHtml(datenschutz, Html.FROM_HTML_MODE_LEGACY));
        else
            textView.setText(Html.fromHtml(datenschutz));

        textView = root.findViewById(R.id.impressum_license);
        if (Build.VERSION.SDK_INT > 24)
            textView.setText(Html.fromHtml(license, Html.FROM_HTML_MODE_LEGACY));
        else
            textView.setText(Html.fromHtml(license));

        return root;
    }

}
