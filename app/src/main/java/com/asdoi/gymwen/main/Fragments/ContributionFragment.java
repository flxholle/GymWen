package com.asdoi.gymwen.main.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asdoi.gymwen.R;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContributionFragment extends Fragment {


    public ContributionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root = inflater.inflate(R.layout.fragment_contribution, container, false);
        try {
            getContext().getTheme().applyStyle(getActivity().getPackageManager().getActivityInfo(getActivity().getComponentName(), 0).getThemeResource(), true);
        } catch (Exception e) {
        }
//        TextView t2 = (TextView) root.findViewById(R.id.attribution_textView);
//        t2.setMovementMethod(LinkMovementMethod.getInstance());
        return root;
    }

}
