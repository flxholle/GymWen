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

package com.asdoi.gymwen.ui.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.appthemeengine.ATE;
import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.teacherlist.TeacherList;
import com.asdoi.gymwen.teacherlist.TeacherlistFeatures;
import com.asdoi.gymwen.ui.activities.TeacherListActivity;
import com.asdoi.gymwen.util.PreferenceUtil;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

/**
 * A simple {@link Fragment} subclass.
 */
public class TeacherListFragment extends Fragment {
    @Nullable
    private static TeacherList teacherList;
    private ViewGroup base;
    @Nullable
    private ListView teacherListView;
    @Nullable
    private Context context;

    private String teacherQuery;

    public static TeacherListFragment newInstance(String searchTeacher) {
        Bundle args = new Bundle();
        args.putString(TeacherListActivity.SEARCH_TEACHER, searchTeacher);

        TeacherListFragment fragment = new TeacherListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public TeacherListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_teacherlist, container, false);

        base = root.findViewById(R.id.teacher_list_base);
        context = getContext();

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((ActivityFeatures) getActivity()).createLoadingPanel(base);
        try {
            teacherQuery = getArguments().getString(TeacherListActivity.SEARCH_TEACHER, null);
        } catch (Exception e) {
        }

        new Thread(() -> {
            ApplicationFeatures.downloadTeacherlistDoc();
            teacherList = TeacherlistFeatures.liste();
            createLayout();
        }).start();
    }


    private void createLayout() {
        getActivity().runOnUiThread(() -> {
            clear();

            if (teacherList.getNoInternet()) {
                TextView title = createTitleLayout();
                title.setText(context.getString(R.string.noInternetConnection));
                return;
            }

            LinearLayout base2 = new LinearLayout(context);
            base2.setOrientation(LinearLayout.VERTICAL);
            base2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            TextInputLayout searchInput = createSearchLayout(null);
            if (teacherQuery != null && !teacherQuery.trim().isEmpty()) {
                teacherList = TeacherlistFeatures.getTeachers(teacherQuery);
                searchInput = createSearchLayout(teacherQuery);
            }

            base2.addView(searchInput, 0);
            base.addView(base2);
            teacherListView = new ListView(context);
            teacherListView.setAdapter(new TeacherListAdapter(context, 0));
            teacherListView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            if (PreferenceUtil.isSwipeToRefresh()) {
                SwipeRefreshLayout swipeRefreshLayout = new SwipeRefreshLayout(context);
                swipeRefreshLayout.setOnRefreshListener(() -> ((TeacherListActivity) getActivity()).onOptionsItemSelected(R.id.action_refresh));
                swipeRefreshLayout.addView(teacherListView);
                swipeRefreshLayout.setColorSchemeColors(Color.BLUE, Color.YELLOW, Color.BLUE);
                base.addView(swipeRefreshLayout);
            } else
                base.addView(teacherListView);

        });
    }

    private void clear() {
        base.removeAllViews();
    }

    @NonNull
    private TextView createTitleLayout() {
        TextView textView = new TextView(context);
        textView.setTextColor(ApplicationFeatures.getTextColorPrimary(context));
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        base.addView(textView);
        return textView;
    }

    @NonNull
    private com.google.android.material.textfield.TextInputLayout createSearchLayout(String
                                                                                             query) {
        com.google.android.material.textfield.TextInputLayout inputLayout = new com.google.android.material.textfield.TextInputLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        inputLayout.setLayoutParams(params);

        EditText inputText = new EditText(context);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (query != null && !query.trim().isEmpty()) {
            inputText.setText(query);
        }
        inputText.setLayoutParams(params);
        inputText.setInputType(InputType.TYPE_CLASS_TEXT);
        inputText.setTextColor(ApplicationFeatures.getTextColorSecondary(context));
        inputText.setHint(getString(R.string.teacher_search_teacher_list));
        inputText.addTextChangedListener(new TextWatcher() {
            @NonNull
            String before = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(@NonNull CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(before)) {
                    if (charSequence.length() > 0) {
                        teacherList = TeacherlistFeatures.getTeachers("" + charSequence);
                    } else {
                        teacherList = TeacherlistFeatures.liste();
                    }
                    before = charSequence.toString();
                    ((BaseAdapter) teacherListView.getAdapter()).notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        inputLayout.addView(inputText);

        return inputLayout;
    }

    private class TeacherListAdapter extends ArrayAdapter<String[]> {

        TeacherListAdapter(@NonNull Context con, int resource) {
            super(con, resource);
        }

        @NotNull
        @Override
        public View getView(int position, @Nullable View convertView, @NotNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_teacherlist_entry, null);
                // Only apply the first time the view is created
                ATE.apply(convertView.getContext(), convertView);
            }

            return ((ActivityFeatures) getActivity()).getTeacherView(convertView, teacherList.getEntries().get(position));
        }

        @Override
        public int getCount() {
            return teacherList.size();
        }
    }

}
