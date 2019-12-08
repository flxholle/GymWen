package com.asdoi.gymwen.main.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.main.ChoiceActivity;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.fragment.app.Fragment;

public class ChoiceActivityFragment extends Fragment implements View.OnClickListener, TextView.OnEditorActionListener {

    private int step = 0;
    private int nextStep = 0;
    private View root;
    private Animation fade;

    private FloatingActionButton fab;

    public ChoiceActivityFragment() {
        super();
    }

    public ChoiceActivityFragment(int step, FloatingActionButton fab) {
        this.step = step;
        this.fab = fab;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        switch (step) {
            default:
            case 1:
                root = inflater.inflate(R.layout.fragment_firstchoice, container, false);
                break;
            case 2:
                root = inflater.inflate(R.layout.fragment_secondchoice, container, false);
                break;
            case 3:
                root = inflater.inflate(R.layout.fragment_thirdchoice, container, false);
                break;
            case 4:
                root = inflater.inflate(R.layout.fragment_fourthchoice, container, false);
                break;
            case 5:
                root = inflater.inflate(R.layout.fragment_fifthchoice, container, false);
        }
        initControls();
        if (step == 5) {
            checkIfEmpty();
        } else {
            deactivateFab();
        }
        fade = AnimationUtils.loadAnimation(getContext(), R.anim.fade);
        return root;
    }

    private void initControls() {
        switch (step) {
            case 1:
                root.findViewById(R.id.choice_button_oberstufe).setOnClickListener(this);
                root.findViewById(R.id.choice_button_5).setOnClickListener(this);
                root.findViewById(R.id.choice_button_6).setOnClickListener(this);
                root.findViewById(R.id.choice_button_7).setOnClickListener(this);
                root.findViewById(R.id.choice_button_8).setOnClickListener(this);
                root.findViewById(R.id.choice_button_9).setOnClickListener(this);
                root.findViewById(R.id.choice_button_10).setOnClickListener(this);
                root.findViewById(R.id.choice_button_parents).setOnClickListener(this);
                ((EditText) root.findViewById(R.id.choice_more_classes)).setOnEditorActionListener(this);
                ((EditText) root.findViewById(R.id.choice_more_classes)).addTextChangedListener(new MyTextWatcher(root.findViewById(R.id.choice_more_classes)));
                break;
            case 2:
                root.findViewById(R.id.choice_button_A).setOnClickListener(this);
                root.findViewById(R.id.choice_button_B).setOnClickListener(this);
                root.findViewById(R.id.choice_button_C).setOnClickListener(this);
                root.findViewById(R.id.choice_button_D).setOnClickListener(this);
                root.findViewById(R.id.choice_button_E).setOnClickListener(this);
                root.findViewById(R.id.choice_button_F).setOnClickListener(this);
                ((EditText) root.findViewById(R.id.choice_more_letters)).setOnEditorActionListener(this);
                ((EditText) root.findViewById(R.id.choice_more_letters)).addTextChangedListener(new MyTextWatcher(root.findViewById(R.id.choice_more_letters)));
                break;
            case 3:
                root.findViewById(R.id.choice_button_1).setOnClickListener(this);
                root.findViewById(R.id.choice_button_2).setOnClickListener(this);
                ((EditText) root.findViewById(R.id.choice_more_course_digits)).setOnEditorActionListener(this);
                ((EditText) root.findViewById(R.id.choice_more_course_digits)).addTextChangedListener(new MyTextWatcher(root.findViewById(R.id.choice_more_course_digits)));
                break;
            case 4:
                ((EditText) root.findViewById(R.id.choice_digit_main_courses)).setOnEditorActionListener(this);
                ((EditText) root.findViewById(R.id.choice_digit_main_courses)).addTextChangedListener(new MyTextWatcher(root.findViewById(R.id.choice_digit_main_courses)));
                break;
            case 5:
                generateStep5();
                root.findViewById(R.id.choice_button_add_more_courses).setOnClickListener(this);
                break;


        }

    }


    int quantitiyCourses = 0;

    private void generateStep5() {
        for (int i = 0; i < VertretungsPlanFeatures.choiceCourseNames.length; i++) {
            if (VertretungsPlanFeatures.choiceCourseNames[i].length < 2 || VertretungsPlanFeatures.choiceCourseNames[i][1] == null || VertretungsPlanFeatures.choiceCourseNames[i][1].trim().isEmpty()) {
                createColumnStep5(VertretungsPlanFeatures.choiceCourseNames[i][0], getString(R.string.anyShort));
            } else {
                createColumnStep5(VertretungsPlanFeatures.choiceCourseNames[i][0], VertretungsPlanFeatures.choiceCourseNames[i][1]);
            }
        }
    }

    private void createColumnStep5(String courseName, String courseShort) {
        LinearLayout basic = root.findViewById(R.id.choice_step5_linear);
        LinearLayout column = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        column.setLayoutParams(params);
        column.setOrientation(LinearLayout.HORIZONTAL);
//            column.setId(i);

        CheckBox box = new CheckBox(getContext());
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        box.setLayoutParams(params);
        box.setGravity(Gravity.CENTER);
        box.setText(courseName);
        box.setId(quantitiyCourses + 130);
        box.setOnClickListener(this);
        box.setChecked(true);

        com.google.android.material.textfield.TextInputLayout inputLayout = new com.google.android.material.textfield.TextInputLayout(getContext());
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        inputLayout.setLayoutParams(params);

        EditText inputText = new EditText(getContext());
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        inputText.setLayoutParams(params);
        inputText.setInputType(InputType.TYPE_CLASS_TEXT);
        inputText.setId(quantitiyCourses + 1300);
        inputText.setOnEditorActionListener(this);
        inputText.setTextColor(Color.BLACK);
//        inputText.setPadding(0, 0, 0, 0);
        inputText.setText(((ChoiceActivity) getActivity()).getCourseFirstDigit() + courseShort + ((ChoiceActivity) getActivity()).getCourseMainDigit());
//        inputText.setHint(((ChoiceActivity) getActivity()).getCourseFirstDigit() + courseShort + ((ChoiceActivity) getActivity()).getCourseMainDigit());
        inputText.addTextChangedListener(new MyTextWatcher(inputText));

        inputLayout.addView(inputText);
        column.addView(box, 0);
        column.addView(inputLayout, 1);
        basic.addView(column, basic.getChildCount() - 1);
        quantitiyCourses++;
    }

    private boolean checkIfEmpty() {
        boolean empty = true;
        for (int i = 0; i < quantitiyCourses; i++) {
            if (!((EditText) root.findViewById(i+1300)).getText().toString().replaceAll(" ", "").isEmpty()) {
                empty = false;
                break;
            }
        }
        if (empty) {
            deactivateFab();
        } else {
            activateFab();
        }
        return empty;
    }


    private void finishOberstufe() {
        if (checkIfEmpty()) {
            Snackbar snackbar = Snackbar
                    .make(root, getString(R.string.oberstufe_empty), Snackbar.LENGTH_LONG);
            snackbar.show();
        } else {
            String oberstufe = "";
            for (int i = 0; i < quantitiyCourses; i++) {
                String course = ((EditText) root.findViewById(i+1300)).getText().toString();
                if (!course.replaceAll(" ", "").isEmpty()) {
                    if (!oberstufe.contains(course)) {
                        oberstufe += course + "#";
                    }
                }
            }
            oberstufe = oberstufe.substring(0, oberstufe.length() - 1);
            ((ChoiceActivity) getActivity()).setCourses(oberstufe);
            ((ChoiceActivity) getActivity()).setFragment(10);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.choice_fab) {
            fabClicked();
        } else if (step == 1) {
            switch (id) {
                case R.id.choice_button_oberstufe:
                    nextStep = 3;
                    break;
                case R.id.choice_button_5:
                    ((ChoiceActivity) getActivity()).setCourses("5");
                    nextStep = 2;
                    break;
                case R.id.choice_button_6:
                    ((ChoiceActivity) getActivity()).setCourses("6");
                    nextStep = 2;
                    break;
                case R.id.choice_button_7:
                    ((ChoiceActivity) getActivity()).setCourses("7");
                    nextStep = 2;
                    break;
                case R.id.choice_button_8:
                    ((ChoiceActivity) getActivity()).setCourses("8");
                    nextStep = 2;
                    break;
                case R.id.choice_button_9:
                    ((ChoiceActivity) getActivity()).setCourses("9");
                    nextStep = 2;
                    break;
                case R.id.choice_button_10:
                    ((ChoiceActivity) getActivity()).setCourses("10");
                    nextStep = 2;
                    break;
                case R.id.choice_button_parents:
//                    nextStep = 6;
//                    ((ChoiceActivity) getActivity()).setParents(true);
                    Snackbar snackbar = Snackbar
                            .make(root, getString(R.string.parent_mode_not_useable), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    break;
            }
        } else if (step == 2) {
            switch (id) {
                //Step 2
                case R.id.choice_button_A:
                    ((ChoiceActivity) getActivity()).setCourses(((ChoiceActivity) getActivity()).getCourses() + "a");
                    nextStep = 10;
                    break;
                case R.id.choice_button_B:
                    ((ChoiceActivity) getActivity()).setCourses(((ChoiceActivity) getActivity()).getCourses() + "b");
                    nextStep = 10;
                    break;
                case R.id.choice_button_C:
                    ((ChoiceActivity) getActivity()).setCourses(((ChoiceActivity) getActivity()).getCourses() + "c");
                    nextStep = 10;
                    break;
                case R.id.choice_button_D:
                    ((ChoiceActivity) getActivity()).setCourses(((ChoiceActivity) getActivity()).getCourses() + "d");
                    nextStep = 10;
                    break;
                case R.id.choice_button_E:
                    ((ChoiceActivity) getActivity()).setCourses(((ChoiceActivity) getActivity()).getCourses() + "e");
                    nextStep = 10;
                    break;
                case R.id.choice_button_F:
                    ((ChoiceActivity) getActivity()).setCourses(((ChoiceActivity) getActivity()).getCourses() + "f");
                    nextStep = 10;
                    break;
            }
        } else if (step == 3) {
            switch (id) {//Step 3
                case R.id.choice_button_1:
                    ((ChoiceActivity) getActivity()).setCourseFirstDigit("1");
                    nextStep = 4;
                    break;
                case R.id.choice_button_2:
                    ((ChoiceActivity) getActivity()).setCourseFirstDigit("2");
                    nextStep = 4;
                    break;
            }
        } else if (step == 4) {

        } else if (step == 5) {
            switch (id) {
                //Step 5:
                case R.id.choice_button_add_more_courses:
                    createColumnStep5(getString(R.string.additional_course), "X");
                    break;
                default:
                    if (id >= 130 && id <= quantitiyCourses + 130) {
                        if (!((CheckBox) root.findViewById(id)).isChecked()) {
                            ((EditText) root.findViewById(id+(1300-130))).setText("");

                        } else {
                            ((EditText) root.findViewById(id+(1300-130))).setText(((ChoiceActivity) getActivity()).getCourseFirstDigit() + getString(R.string.anyShort) + ((ChoiceActivity) getActivity()).getCourseMainDigit());
                        }
                    }
                    break;
            }
        }

        System.out.println("click");
        if (nextStep > step) {
            ((ChoiceActivity) getActivity()).setFragment(nextStep);
        }

    }

    private class MyTextWatcher implements TextWatcher {


        private EditText mEditText;

        public MyTextWatcher(EditText editText) {
            mEditText = editText;
        }

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start,
                                      int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start,
                                  int before, int count) {
            if (s.length() != 0) {
                if (step != 5) {
                    activateFab();
                } else if (step == 5) {
                    ((CheckBox) root.findViewById(mEditText.getId()-(1300-130))).setChecked(true);
                    checkIfEmpty();
                }
            } else {
                if (step != 5) {
                    deactivateFab();
                } else if (step == 5) {
                    ((CheckBox) root.findViewById(mEditText.getId()-(1300-130))).setChecked(false);
                    checkIfEmpty();
                }
            }
        }
    }

    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_DONE) {
            System.out.println("works");
            int id = textView.getId();
            if (step == 1) {
                if (id == R.id.choice_more_classes) {
                    if (!((EditText) root.findViewById(R.id.choice_more_classes)).getText().toString().replaceAll(" ", "").isEmpty()) {
                        activateFab();
                        fabClicked();
                    }
                }
            } else if (step == 2) {
                if (id == R.id.choice_more_letters) {
                    if (!((EditText) root.findViewById(R.id.choice_more_letters)).getText().toString().replaceAll(" ", "").isEmpty()) {
                        activateFab();
                        fabClicked();
                    }
                }
            } else if (step == 3) {
                if (id == R.id.choice_more_course_digits) {
                    if (!((EditText) root.findViewById(R.id.choice_more_course_digits)).getText().toString().replaceAll(" ", "").isEmpty()) {
                        activateFab();
                        fabClicked();
                    }
                }
            } else if (step == 4) {
                if (id == R.id.choice_digit_main_courses) {
                    if (!((EditText) root.findViewById(R.id.choice_digit_main_courses)).getText().toString().replaceAll(" ", "").isEmpty()) {
                        activateFab();
                        fabClicked();
                    }
                }
            }
        }
        return false;
    }

    private void activateFab() {

//        fab.startAnimation(fade);
        fab.setEnabled(true);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(this);
    }

    private void deactivateFab() {
        fab.setEnabled(false);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(this);
    }

    public void fabClicked() {
        System.out.println("clicked fab");
        if (step == 1) {
            if (!((EditText) root.findViewById(R.id.choice_more_classes)).getText().toString().replaceAll(" ", "").isEmpty()) {
                String s = ((EditText) root.findViewById(R.id.choice_more_classes)).getText().toString().replaceAll(" ", "");
                boolean correctType = true;
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (!Character.isDigit(c)) {
                        correctType = false;
                        break;
                    }
                }
                if (correctType) {
                    ((ChoiceActivity) getActivity()).setCourses("" + s);
                    nextStep = 2;
                } else {
                    Snackbar snackbar = Snackbar
                            .make(root, getString(R.string.please_insert_digit), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        } else if (step == 2) {
            if (!((EditText) root.findViewById(R.id.choice_more_letters)).getText().toString().replaceAll(" ", "").isEmpty()) {
                String s = ((EditText) root.findViewById(R.id.choice_more_letters)).getText().toString().replaceAll(" ", "");
                boolean correctType = true;
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (!Character.isLetter(c)) {
                        correctType = false;
                        break;
                    }
                }
                if (correctType) {
                    ((ChoiceActivity) getActivity()).setCourses(((ChoiceActivity) getActivity()).getCourses() + ((EditText) root.findViewById(R.id.choice_more_letters)).getText().toString().replaceAll(" ", ""));
                    nextStep = 10;
                } else {
                    Snackbar snackbar = Snackbar
                            .make(root, getString(R.string.please_insert_letter), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }

        } else if (step == 3) {
            if (!((EditText) root.findViewById(R.id.choice_more_course_digits)).getText().toString().replaceAll(" ", "").isEmpty()) {
                String s = ((EditText) root.findViewById(R.id.choice_more_course_digits)).getText().toString().replaceAll(" ", "");
                boolean correctType = true;
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (!Character.isDigit(c)) {
                        correctType = false;
                        break;
                    }
                }
                if (correctType) {
                    ((ChoiceActivity) getActivity()).setCourseFirstDigit("" + s);
                    nextStep = 4;
                } else {
                    Snackbar snackbar = Snackbar
                            .make(root, getString(R.string.please_insert_digit), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        } else if (step == 4) {
            if (!((EditText) root.findViewById(R.id.choice_digit_main_courses)).getText().toString().replaceAll(" ", "").isEmpty()) {
                String s = ((EditText) root.findViewById(R.id.choice_digit_main_courses)).getText().toString().replaceAll(" ", "");
                boolean correctType = true;
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (!Character.isDigit(c)) {
                        correctType = false;
                        break;
                    }
                }
                if (correctType) {
                    ((ChoiceActivity) getActivity()).setCourseMainDigit("" + s);
                    nextStep = 5;
                } else {
                    Snackbar snackbar = Snackbar
                            .make(root, getString(R.string.please_insert_digit), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        } else if (step == 5) {
            finishOberstufe();
        }

        if (nextStep > step) {
            ((ChoiceActivity) getActivity()).setFragment(nextStep);
        }
    }
}
