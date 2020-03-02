package com.asdoi.gymwen.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.util.External_Const;

public class ColoRushFragment extends Fragment implements View.OnClickListener {
    private View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        root = inflater.inflate(R.layout.fragment_colorush, container, false);

        root.findViewById(R.id.colorush_app).setOnClickListener(this);
        root.findViewById(R.id.colorush_online).setOnClickListener(this);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            String[] imageUrls = new String[]{"https://gitlab.com/asdoi/colorrush/raw/master/Screenshots/colorushboss1.png?inline=false",
                    "https://gitlab.com/asdoi/colorrush/raw/master/Screenshots/colorushchoosing2.png?inline=false",
                    "https://gitlab.com/asdoi/colorrush/raw/master/Screenshots/colorushlevel1.png?inline=false",
                    "https://gitlab.com/asdoi/colorrush/raw/master/Screenshots/colorushmenu.png?inline=false"};
            new ApplicationFeatures.downloadImageTask(root.findViewById(R.id.colorush_image1)).execute(imageUrls[0]);
            new ApplicationFeatures.downloadImageTask(root.findViewById(R.id.colorush_image2)).execute(imageUrls[1]);
            new ApplicationFeatures.downloadImageTask(root.findViewById(R.id.colorush_image3)).execute(imageUrls[2]);
            new ApplicationFeatures.downloadImageTask(root.findViewById(R.id.colorush_image4)).execute(imageUrls[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.colorush_app:
                //Check the two notes versions
                if (!((ActivityFeatures) getActivity()).startApp(External_Const.coloRush_packageNames))
                    ((ActivityFeatures) getActivity()).tabIntent(External_Const.downloadApp_colorush);
                break;
            case R.id.colorush_online:
                ((ActivityFeatures) getActivity()).tabIntent(External_Const.colorush_online);
                break;
        }
    }
}
