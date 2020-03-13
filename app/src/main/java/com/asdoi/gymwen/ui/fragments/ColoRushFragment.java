package com.asdoi.gymwen.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.util.External_Const;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;

import java.util.HashMap;

public class ColoRushFragment extends Fragment implements View.OnClickListener {
    private SliderLayout mDemoSlider;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root = inflater.inflate(R.layout.fragment_colorush, container, false);

        root.findViewById(R.id.colorush_app).setOnClickListener(this);
        root.findViewById(R.id.colorush_online).setOnClickListener(this);

        mDemoSlider = root.findViewById(R.id.slider);

        String[] names = getResources().getStringArray(R.array.color_rush_screenshots);
        HashMap<String, String> url_images = new HashMap<String, String>();
        url_images.put(names[0], "https://gitlab.com/asdoi/colorrush/raw/master/Screenshots/colorushboss1.png?inline=false");
        url_images.put(names[1], "https://gitlab.com/asdoi/colorrush/raw/master/Screenshots/colorushchoosing2.png?inline=false");
        url_images.put(names[2], "https://gitlab.com/asdoi/colorrush/raw/master/Screenshots/colorushlevel1.png?inline=false");
        url_images.put(names[3], "https://gitlab.com/asdoi/colorrush/raw/master/Screenshots/colorushmenu.png?inline=false");

        for (String name : url_images.keySet()) {
            TextSliderView textSliderView = new TextSliderView(getContext());
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(url_images.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit);

            mDemoSlider.addSlider(textSliderView);
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(4000);

        return root;
    }

    @Override
    public void onClick(@NonNull View v) {
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

    @Override
    public void onStop() {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider before activity or fragment is destroyed
        mDemoSlider.stopAutoCycle();
        super.onStop();
    }
}
