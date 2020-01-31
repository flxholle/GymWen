package com.asdoi.gymwen.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class FragmentFeatures extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root = inflater.inflate(getArguments().getInt("id"), container, false);

        return root;
    }

    public static Fragment newInstance(int id) {
        Fragment fragment = new FragmentFeatures();
        Bundle bundle = new Bundle();

        bundle.putInt("id", id);

        fragment.setArguments(bundle);
        return fragment;
    }
}
