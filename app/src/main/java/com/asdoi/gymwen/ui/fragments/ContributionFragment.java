package com.asdoi.gymwen.ui.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.R;

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
        return root;
    }

}
