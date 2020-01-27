package com.asdoi.gymwen.ui.main.fragments;

import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.lehrerliste.Lehrerliste;

/**
 * A simple {@link Fragment} subclass.
 */
public class LehrerlisteFragment extends Fragment {
    private static String[][] teacherList;
    private ViewGroup base;
    private ListView teacherListView;


    public LehrerlisteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_lehrerliste, container, false);

        base = root.findViewById(R.id.teacher_list_base);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        ActivityFeatures.createLoadingPanel(base);

        new Thread(() -> {
            ApplicationFeatures.downloadLehrerDoc();
            teacherList = Lehrerliste.liste();
            createLayout();
        }).start();
    }


    private void createLayout() {
        getActivity().runOnUiThread(() -> {
            clear();

            if (teacherList == null) {
                TextView title = createTitleLayout();
                title.setText(getContext().getString(R.string.noInternetConnection));
                return;
            }

            LinearLayout base2 = new LinearLayout(getContext());
            base2.setOrientation(LinearLayout.VERTICAL);
            base2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            base2.addView(createSearchLayout(), 0);
            base.addView(base2);

            teacherListView = new ListView(getContext());
            teacherListView.setAdapter(new TeacherListAdapter(getContext(), 0));
            teacherListView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            base.addView(teacherListView);

        });
    }

    private void clear() {
        base.removeAllViews();
    }

    TextView createTitleLayout() {
        TextView textView = new TextView(ApplicationFeatures.getContext());
        textView.setTextColor(ApplicationFeatures.getTextColorPrimary());
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        base.addView(textView);
        return textView;
    }

    com.google.android.material.textfield.TextInputLayout createSearchLayout() {
        com.google.android.material.textfield.TextInputLayout inputLayout = new com.google.android.material.textfield.TextInputLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        inputLayout.setLayoutParams(params);

        EditText inputText = new EditText(getContext());
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        inputText.setLayoutParams(params);
        inputText.setInputType(InputType.TYPE_CLASS_TEXT);
        inputText.setTextColor(ApplicationFeatures.getTextColorSecondary());
        inputText.setHint(getString(R.string.teacher_search_teacher_list));
        inputText.addTextChangedListener(new TextWatcher() {
            String before = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(before)) {
                    if (charSequence.length() > 0) {
                        teacherList = Lehrerliste.getTeachers("" + charSequence);
                    } else {
                        teacherList = Lehrerliste.liste();
                    }
                    before = charSequence.toString();
                    ((BaseAdapter) teacherListView.getAdapter()).notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        inputLayout.addView(inputText);

        return inputLayout;
    }

    private class TeacherListAdapter extends ArrayAdapter<String[]> {

        public TeacherListAdapter(Context con, int resource) {
            super(con, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_lehrerliste_entry, null);
            }

            return ((ActivityFeatures) getActivity()).getTeacherView(convertView, teacherList[position]);
        }

        @Override
        public int getCount() {
            return teacherList.length;
        }
    }

}
