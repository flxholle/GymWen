package com.asdoi.gymwen.ui.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures;
import com.asdoi.gymwen.ui.activities.ChoiceActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pd.chocobar.ChocoBar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChoiceActivityFragment extends Fragment implements View.OnClickListener, TextView.OnEditorActionListener {

    private int step = 0;
    private int nextStep = 0;
    private View root;
    private Context context;

    private FloatingActionButton fab;

    private ChoiceActivity mainActivity;

    public ChoiceActivityFragment() {
        super();
    }

    public ChoiceActivityFragment(int step, FloatingActionButton fab) {
        this.step = step;
        this.fab = fab;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity = ((ChoiceActivity) getActivity());
        context = getContext();
        switch (step) {
            default:
            case 1:
                root = inflater.inflate(R.layout.fragment_firstchoice, container, false);
                if (mainActivity.getParents()) {
                    root.findViewById(R.id.choice_button_parents).setVisibility(View.GONE);
                    addSpinner();
                }
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
                root.findViewById(R.id.choice_button_add_more_courses).setBackgroundColor(ApplicationFeatures.getAccentColor(getContext()));
        }
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        initControls();
        if (step == 5) {
            checkIfEmpty();
        } else {
            deactivateFab();
        }
    }

    private void initControls() {
        switch (step) {
            case 1:
                root.findViewById(R.id.choice_button_senior).setOnClickListener(this);
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
        for (int i = 0; i < SubstitutionPlanFeatures.choiceCourseNames.length; i++) {
            if (SubstitutionPlanFeatures.choiceCourseNames[i].length < 2 || SubstitutionPlanFeatures.choiceCourseNames[i][1] == null || SubstitutionPlanFeatures.choiceCourseNames[i][1].trim().isEmpty()) {
                createColumnStep5(SubstitutionPlanFeatures.choiceCourseNames[i][0], getString(R.string.anyShort));
            } else {
                createColumnStep5(SubstitutionPlanFeatures.choiceCourseNames[i][0], SubstitutionPlanFeatures.choiceCourseNames[i][1]);
            }
        }
    }

    private void createColumnStep5(String courseName, String courseShort) {
        LinearLayout basic = root.findViewById(R.id.choice_step5_linear);
        LinearLayout column = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        column.setLayoutParams(params);
        column.setOrientation(LinearLayout.HORIZONTAL);
//            column.setId(i);

        CheckBox box = new CheckBox(context);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        box.setLayoutParams(params);
        box.setGravity(Gravity.CENTER);
        box.setText(courseName);
        box.setId(quantitiyCourses + 130);
        if (android.os.Build.VERSION.SDK_INT >= 21)
            box.setButtonTintList(ColorStateList.valueOf(ApplicationFeatures.getAccentColor(getContext())));
        box.setOnClickListener(this);
        box.setChecked(true);

        com.google.android.material.textfield.TextInputLayout inputLayout = new com.google.android.material.textfield.TextInputLayout(context);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        inputLayout.setLayoutParams(params);

        EditText inputText = new EditText(context);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        inputText.setLayoutParams(params);
        inputText.setInputType(InputType.TYPE_CLASS_TEXT);
        inputText.setId(quantitiyCourses + 1300);
        inputText.setOnEditorActionListener(this);
        inputText.setTextColor(ApplicationFeatures.getTextColorPrimary(context));
        inputText.setText(mainActivity.getCourseFirstDigit() + courseShort + mainActivity.getCourseMainDigit());
//        inputText.setHint(mainActivity.getCourseFirstDigit() + courseShort + mainActivity.getCourseMainDigit());
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
            if (!((EditText) root.findViewById(i + 1300)).getText().toString().replaceAll(" ", "").isEmpty()) {
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


    private void finishSenior() {
        if (checkIfEmpty()) {
            ChocoBar.builder().setActivity(getActivity()).setText(getString(R.string.senior_empty)).setDuration(ChocoBar.LENGTH_LONG).build().show();
        } else {
            String senior = "";
            for (int i = 0; i < quantitiyCourses; i++) {
                String course = ((EditText) root.findViewById(i + 1300)).getText().toString();
                if (!course.replaceAll(" ", "").isEmpty()) {
                    if (!senior.contains(course)) {
                        senior += course + "#";
                    }
                }
            }
            senior = senior.substring(0, senior.length() - 1);
            mainActivity.setCourses(senior);
            mainActivity.setFragment(10);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.choice_fab) {
            fabClicked();
        } else if (step == 1) {
            switch (id) {
                case R.id.choice_button_senior:
                    nextStep = 3;
                    break;
                case R.id.choice_button_5:
                    mainActivity.setCourses("5");
                    nextStep = 2;
                    break;
                case R.id.choice_button_6:
                    mainActivity.setCourses("6");
                    nextStep = 2;
                    break;
                case R.id.choice_button_7:
                    mainActivity.setCourses("7");
                    nextStep = 2;
                    break;
                case R.id.choice_button_8:
                    mainActivity.setCourses("8");
                    nextStep = 2;
                    break;
                case R.id.choice_button_9:
                    mainActivity.setCourses("9");
                    nextStep = 2;
                    break;
                case R.id.choice_button_10:
                    mainActivity.setCourses("10");
                    nextStep = 2;
                    break;
                case R.id.choice_button_parents:
                    mainActivity.setParents(true);
                    openAddDialog();
                    root.findViewById(R.id.choice_button_parents).setVisibility(View.GONE);
//                    Snackbar snackbar = Snackbar
//                            .make(root, getString(R.string.parent_mode_not_useable), Snackbar.LENGTH_LONG);
//                    snackbar.show();
                    return;
            }
        } else if (step == 2) {
            switch (id) {
                //Step 2
                case R.id.choice_button_A:
                    mainActivity.setCourses(mainActivity.getCourses() + "a");
                    nextStep = 10;
                    break;
                case R.id.choice_button_B:
                    mainActivity.setCourses(mainActivity.getCourses() + "b");
                    nextStep = 10;
                    break;
                case R.id.choice_button_C:
                    mainActivity.setCourses(mainActivity.getCourses() + "c");
                    nextStep = 10;
                    break;
                case R.id.choice_button_D:
                    mainActivity.setCourses(mainActivity.getCourses() + "d");
                    nextStep = 10;
                    break;
                case R.id.choice_button_E:
                    mainActivity.setCourses(mainActivity.getCourses() + "e");
                    nextStep = 10;
                    break;
                case R.id.choice_button_F:
                    mainActivity.setCourses(mainActivity.getCourses() + "f");
                    nextStep = 10;
                    break;
            }
        } else if (step == 3) {
            switch (id) {//Step 3
                case R.id.choice_button_1:
                    mainActivity.setCourseFirstDigit("1");
                    nextStep = 4;
                    break;
                case R.id.choice_button_2:
                    mainActivity.setCourseFirstDigit("2");
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
                            ((EditText) root.findViewById(id + (1300 - 130))).setText("");

                        } else {
                            ((EditText) root.findViewById(id + (1300 - 130))).setText(mainActivity.getCourseFirstDigit() + getString(R.string.anyShort) + mainActivity.getCourseMainDigit());
                        }
                    }
                    break;
            }
        }

        if (nextStep > step) {
            mainActivity.setFragment(nextStep);
        }

    }

    public void openAddDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(context.getString(R.string.profiles_add));

        // Set up the input
        final EditText input = new EditText(context);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.customView(input, true);

        // Set up the buttons
        builder.positiveText(context.getString(R.string.ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NotNull MaterialDialog dialog, @NotNull DialogAction which) {
                        if (input.getText().toString().trim().isEmpty())
                            mainActivity.setName(context.getString(R.string.profile_empty_name));
                        else
                            mainActivity.setName(input.getText().toString());
                        addSpinner();
                        dialog.dismiss();
                    }
                });
        builder.negativeText(context.getString(R.string.cancel))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NotNull MaterialDialog dialog, @NotNull DialogAction which) {
                        mainActivity.setName(context.getString(R.string.profile_empty_name));
                        dialog.dismiss();
                        addSpinner();
                    }
                });

        builder.show();
    }

    private void addSpinner() {
        getActivity().findViewById(R.id.choice_spinner_relative).setBackgroundColor(ApplicationFeatures.getPrimaryColor(getContext()));

        Spinner parentSpinner = mainActivity.findViewById(R.id.choice_parent_spinner);
        parentSpinner.setVisibility(View.VISIBLE);
        parentSpinner.setEnabled(true);
        List<String> list = new ArrayList<String>();
        list.add(mainActivity.getName());
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        parentSpinner.setAdapter(dataAdapter);
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
                    ((CheckBox) root.findViewById(mEditText.getId() - (1300 - 130))).setChecked(true);
                    checkIfEmpty();
                }
            } else {
                if (step != 5) {
                    deactivateFab();
                } else if (step == 5) {
                    ((CheckBox) root.findViewById(mEditText.getId() - (1300 - 130))).setChecked(false);
                    checkIfEmpty();
                }
            }
        }
    }

    @Override
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
                    mainActivity.setCourses("" + s);
                    nextStep = 2;
                } else {
                    ChocoBar.builder().setActivity(getActivity()).setText(getString(R.string.please_insert_digit)).setDuration(ChocoBar.LENGTH_LONG).build().show();
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
                    mainActivity.setCourses(mainActivity.getCourses() + ((EditText) root.findViewById(R.id.choice_more_letters)).getText().toString().replaceAll(" ", ""));
                    nextStep = 10;
                } else {
                    ChocoBar.builder().setActivity(getActivity()).setText(getString(R.string.please_insert_letter)).setDuration(ChocoBar.LENGTH_LONG).build().show();
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
                    mainActivity.setCourseFirstDigit("" + s);
                    nextStep = 4;
                } else {
                    ChocoBar.builder().setActivity(getActivity()).setText(getString(R.string.please_insert_digit)).setDuration(ChocoBar.LENGTH_LONG).build().show();
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
                    mainActivity.setCourseMainDigit("" + s);
                    nextStep = 5;
                } else {
                    ChocoBar.builder().setActivity(getActivity()).setText(getString(R.string.please_insert_digit)).setDuration(ChocoBar.LENGTH_LONG).build().show();
                }
            }
        } else if (step == 5) {
            finishSenior();
        }

        if (nextStep > step) {
            mainActivity.setFragment(nextStep);
        }
    }
}
