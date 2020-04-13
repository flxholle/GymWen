package com.ulan.timetable.utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.asdoi.gymwen.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.ulan.timetable.adapters.ExamsAdapter;
import com.ulan.timetable.adapters.FragmentsTabAdapter;
import com.ulan.timetable.adapters.HomeworksAdapter;
import com.ulan.timetable.adapters.NotesAdapter;
import com.ulan.timetable.adapters.WeekAdapter;
import com.ulan.timetable.fragments.WeekdayFragment;
import com.ulan.timetable.model.Exam;
import com.ulan.timetable.model.Homework;
import com.ulan.timetable.model.Note;
import com.ulan.timetable.model.Week;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import petrov.kristiyan.colorpicker.ColorPicker;


/**
 * Created by Ulan on 22.10.2018.
 */
public class AlertDialogsHelper {

    public static void getEditSubjectDialog(@NonNull final AppCompatActivity activity, @NonNull final View alertLayout, @NonNull final ListView listView, @NonNull final Week week) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText subject = alertLayout.findViewById(R.id.subject_dialog);
        editTextHashs.put(R.string.subject, subject);
        final EditText teacher = alertLayout.findViewById(R.id.teacher_dialog);
//        editTextHashs.put(R.string.teacher, teacher);
        final EditText room = alertLayout.findViewById(R.id.room_dialog);
//        editTextHashs.put(R.string.room, room);
        final TextView from_time = alertLayout.findViewById(R.id.from_time);
        final TextView to_time = alertLayout.findViewById(R.id.to_time);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(week.getColor(), Color.WHITE, Color.BLACK));

        subject.setText(week.getSubject());
        teacher.setText(week.getTeacher());
        room.setText(week.getRoom());
        from_time.setText(week.getFromTime());
        to_time.setText(week.getToTime());
        select_color.setBackgroundColor(week.getColor() != 0 ? week.getColor() : Color.WHITE);

        from_time.setOnClickListener(v -> {
            int mHour = Integer.parseInt(week.getFromTime().substring(0, week.getFromTime().indexOf(":")));
            int mMinute = Integer.parseInt(week.getFromTime().substring(week.getFromTime().indexOf(":") + 1));
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute) -> {
                        from_time.setText(String.format("%02d:%02d", hourOfDay, minute));
                        week.setFromTime(String.format("%02d:%02d", hourOfDay, minute));
                    }, mHour, mMinute, true);
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });

        to_time.setOnClickListener(v -> {
            int mHour = Integer.parseInt(week.getToTime().substring(0, week.getToTime().indexOf(":")));
            int mMinute = Integer.parseInt(week.getToTime().substring(week.getToTime().indexOf(":") + 1));
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute1) -> {
                        to_time.setText(String.format("%02d:%02d", hourOfDay, minute1));
                        week.setToTime(String.format("%02d:%02d", hourOfDay, minute1));
                    }, mHour, mMinute, true);
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });

        select_color.setOnClickListener(v -> {
            ColorPicker colorPicker = new ColorPicker(activity);
            colorPicker.setRoundColorButton(true);
            colorPicker.setDefaultColorButton(((ColorDrawable) select_color.getBackground()).getColor());
            colorPicker.setColors(ColorPalette.PRIMARY_COLORS);
            colorPicker.setTitle(activity.getString(R.string.choose_color));
            colorPicker.show();
            colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                @Override
                public void onChooseColor(int position, int color) {
                    if (color != 0) {
                        select_color.setBackgroundColor(color);
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                    }
                }

                @Override
                public void onCancel() {
                    // put code
                }
            });
        });


        DbHelper dbHelper = new DbHelper(activity);
        ArrayList<Week> alreadyInsertedWeeks = WeekUtils.getAllWeeks(dbHelper);

        subject.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            event != null &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed()) {
                            // the user is done typing.
                            //AutoFill other fields
                            for (Week w : alreadyInsertedWeeks) {
                                if (w.getSubject().equalsIgnoreCase(v.getText().toString())) {
                                    if (teacher.getText().toString().trim().isEmpty())
                                        teacher.setText(w.getTeacher());
                                    if (room.getText().toString().trim().isEmpty())
                                        room.setText(w.getRoom());
                                    select_color.setBackgroundColor(w.getColor());
                                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                                }
                            }

                            return true;
                        }
                    }
                    return false;
                }
        );
        subject.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                for (Week w : alreadyInsertedWeeks) {
                    if (w.getSubject().equalsIgnoreCase(((EditText) v).getText().toString())) {
                        if (teacher.getText().toString().trim().isEmpty())
                            teacher.setText(w.getTeacher());
                        if (room.getText().toString().trim().isEmpty())
                            room.setText(w.getRoom());
                        select_color.setBackgroundColor(w.getColor());
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                    }
                }
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.edit_subject);
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.show();

        cancel.setOnClickListener(v -> {
            subject.getText().clear();
            teacher.getText().clear();
            room.getText().clear();
            from_time.setText(R.string.select_start_time);
            to_time.setText(R.string.select_end_time);
            select_color.setBackgroundColor(Color.WHITE);
            subject.requestFocus();
            dialog.dismiss();
        });

        save.setOnClickListener(v -> {
            if (TextUtils.isEmpty(subject.getText()) /*|| TextUtils.isEmpty(teacher.getText()) || TextUtils.isEmpty(room.getText())*/) {
                for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                    if (TextUtils.isEmpty(entry.getValue().getText())) {
                        entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                        entry.getValue().requestFocus();
                    }
                }
            } else if (!from_time.getText().toString().matches(".*\\d+.*") || !to_time.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.time_error, Snackbar.LENGTH_LONG).show();
            } else {
                DbHelper db = new DbHelper(activity);
                WeekAdapter weekAdapter = (WeekAdapter) listView.getAdapter(); // In order to get notifyDataSetChanged() method.
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                week.setSubject(subject.getText().toString());
                week.setTeacher(teacher.getText().toString());
                week.setRoom(room.getText().toString());
                week.setColor(buttonColor.getColor());
                db.updateWeek(week);
                weekAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }

    public static void getAddSubjectDialog(@NonNull final AppCompatActivity activity, @NonNull final View alertLayout, @NonNull final FragmentsTabAdapter adapter, @NonNull final ViewPager viewPager) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText subject = alertLayout.findViewById(R.id.subject_dialog);
        subject.requestFocus();
        editTextHashs.put(R.string.subject, subject);
        final EditText teacher = alertLayout.findViewById(R.id.teacher_dialog);
//        editTextHashs.put(R.string.teacher, teacher);
        final EditText room = alertLayout.findViewById(R.id.room_dialog);
//        editTextHashs.put(R.string.room, room);
        final TextView from_time = alertLayout.findViewById(R.id.from_time);
        final TextView to_time = alertLayout.findViewById(R.id.to_time);
        final Button select_color = alertLayout.findViewById(R.id.select_color);

        final Week week = new Week();

        from_time.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int mHour = c.get(Calendar.HOUR_OF_DAY);
            int mMinute = c.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute) -> {
                        from_time.setText(String.format("%02d:%02d", hourOfDay, minute));
                        week.setFromTime(String.format("%02d:%02d", hourOfDay, minute));
                    }, mHour, mMinute, true);
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });

        to_time.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute1) -> {
                        to_time.setText(String.format("%02d:%02d", hourOfDay, minute1));
                        week.setToTime(String.format("%02d:%02d", hourOfDay, minute1));
                    }, hour, minute, true);
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });

        select_color.setOnClickListener(v -> {
            ColorPicker colorPicker = new ColorPicker(activity);
            colorPicker.setRoundColorButton(true);
            colorPicker.setDefaultColorButton(((ColorDrawable) select_color.getBackground()).getColor());
            colorPicker.setColors(ColorPalette.PRIMARY_COLORS);
            colorPicker.setTitle(activity.getString(R.string.choose_color));
            colorPicker.show();
            colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                @Override
                public void onChooseColor(int position, int color) {
                    if (color != 0) {
                        select_color.setBackgroundColor(color);
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                    }
                }

                @Override
                public void onCancel() {
                    // put code
                }
            });

        });

        DbHelper dbHelper = new DbHelper(activity);
        ArrayList<Week> alreadyInsertedWeeks = WeekUtils.getAllWeeks(dbHelper);

        subject.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            event != null &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed()) {
                            // the user is done typing.
                            //AutoFill other fields
                            for (Week w : alreadyInsertedWeeks) {
                                if (w.getSubject().equalsIgnoreCase(v.getText().toString())) {
                                    if (teacher.getText().toString().trim().isEmpty())
                                        teacher.setText(w.getTeacher());
                                    if (room.getText().toString().trim().isEmpty())
                                        room.setText(w.getRoom());
                                    select_color.setBackgroundColor(w.getColor());
                                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                                }
                            }

                            return true;
                        }
                    }
                    return false;
                }
        );
        subject.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                for (Week w : alreadyInsertedWeeks) {
                    if (w.getSubject().equalsIgnoreCase(((EditText) v).getText().toString())) {
                        if (teacher.getText().toString().trim().isEmpty())
                            teacher.setText(w.getTeacher());
                        if (room.getText().toString().trim().isEmpty())
                            room.setText(w.getRoom());
                        select_color.setBackgroundColor(w.getColor());
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                    }
                }
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.add_subject);
        alert.setCancelable(false);
        Button cancel = alertLayout.findViewById(R.id.cancel);
        Button submit = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(view -> dialog.show());

        cancel.setOnClickListener(v -> {
            subject.getText().clear();
            teacher.getText().clear();
            room.getText().clear();
            from_time.setText(R.string.select_start_time);
            to_time.setText(R.string.select_end_time);
            select_color.setBackgroundColor(Color.WHITE);
            subject.requestFocus();
            dialog.dismiss();
        });

        submit.setOnClickListener(v -> {
            if (TextUtils.isEmpty(subject.getText()) /*|| TextUtils.isEmpty(teacher.getText()) || TextUtils.isEmpty(room.getText())*/) {
                for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                    if (TextUtils.isEmpty(entry.getValue().getText())) {
                        entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                        entry.getValue().requestFocus();
                    }
                }
            } else if (!from_time.getText().toString().matches(".*\\d+.*") || !to_time.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.time_error, Snackbar.LENGTH_LONG).show();
            } else {
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                week.setSubject(subject.getText().toString());
                week.setFragment(((WeekdayFragment) adapter.getItem(viewPager.getCurrentItem())).getKey());
                week.setTeacher(teacher.getText().toString());
                week.setRoom(room.getText().toString());
                week.setColor(buttonColor.getColor());
                dbHelper.insertWeek(week);
                adapter.notifyDataSetChanged();
                subject.getText().clear();
                teacher.getText().clear();
                room.getText().clear();
                from_time.setText(R.string.select_start_time);
                to_time.setText(R.string.select_end_time);
                select_color.setBackgroundColor(Color.WHITE);
                subject.requestFocus();
                dialog.dismiss();
            }
        });
    }

    public static void getEditHomeworkDialog(@NonNull final AppCompatActivity activity,
                                             @NonNull final View alertLayout, @NonNull final ArrayList<Homework> adapter,
                                             @NonNull final ListView listView, int listposition) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText subject = alertLayout.findViewById(R.id.subjecthomework);
        editTextHashs.put(R.string.subject, subject);
        final EditText description = alertLayout.findViewById(R.id.descriptionhomework);
        editTextHashs.put(R.string.description, description);
        final TextView date = alertLayout.findViewById(R.id.datehomework);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Homework homework = adapter.get(listposition);

        subject.setText(homework.getSubject());
        description.setText(homework.getDescription());
        date.setText(homework.getDate());
        select_color.setBackgroundColor(homework.getColor() != 0 ? homework.getColor() : Color.WHITE);
        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(homework.getColor(), Color.WHITE, Color.BLACK));

        date.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int mYear = calendar.get(Calendar.YEAR);
            int mMonth = calendar.get(Calendar.MONTH);
            int mdayofMonth = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(activity, (view, year, month, dayOfMonth) -> {
                date.setText(String.format("%02d-%02d-%02d", year, month + 1, dayOfMonth));
                homework.setDate(String.format("%02d-%02d-%02d", year, month + 1, dayOfMonth));
            }, mYear, mMonth, mdayofMonth);
            datePickerDialog.setTitle(R.string.choose_date);
            datePickerDialog.show();
        });

        select_color.setOnClickListener(v -> {
            ColorPicker colorPicker = new ColorPicker(activity);
            colorPicker.setRoundColorButton(true);
            colorPicker.setDefaultColorButton(((ColorDrawable) select_color.getBackground()).getColor());
            colorPicker.setColors(ColorPalette.PRIMARY_COLORS);
            colorPicker.setTitle(activity.getString(R.string.choose_color));
            colorPicker.show();
            colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                @Override
                public void onChooseColor(int position, int color) {
                    if (color != 0) {
                        select_color.setBackgroundColor(color);
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                    }
                }

                @Override
                public void onCancel() {
                    // put code
                }
            });
        });

        DbHelper dbHelper = new DbHelper(activity);
        ArrayList<Week> alreadyInsertedWeeks = WeekUtils.getAllWeeks(dbHelper);

        subject.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            event != null &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed()) {
                            // the user is done typing.
                            //AutoFill other fields
                            for (Week w : alreadyInsertedWeeks) {
                                if (w.getSubject().equalsIgnoreCase(v.getText().toString())) {
                                    select_color.setBackgroundColor(w.getColor());
                                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
//                                    date.setText(DBUtil.getNextOccurenceOfSubject(dbHelper, w.getSubject()));
//                                    homework.setDate(DBUtil.getNextOccurenceOfSubject(dbHelper, w.getSubject()));
                                }
                            }

                            return true;
                        }
                    }
                    return false;
                }
        );
        subject.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                for (Week w : alreadyInsertedWeeks) {
                    if (w.getSubject().equalsIgnoreCase(((EditText) v).getText().toString())) {
                        select_color.setBackgroundColor(w.getColor());
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
//                                    date.setText(DBUtil.getNextOccurenceOfSubject(dbHelper, w.getSubject()));
//                                    homework.setDate(DBUtil.getNextOccurenceOfSubject(dbHelper, w.getSubject()));
                    }
                }
            }
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.edit_homework);
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.show();

        cancel.setOnClickListener(v -> {
            subject.getText().clear();
            description.getText().clear();
            select_color.setBackgroundColor(Color.WHITE);
            subject.requestFocus();
            dialog.dismiss();
        });

        save.setOnClickListener(v -> {
            if (TextUtils.isEmpty(subject.getText()) || TextUtils.isEmpty(description.getText())) {
                for (Map.Entry<Integer, EditText> editText : editTextHashs.entrySet()) {
                    if (TextUtils.isEmpty(editText.getValue().getText())) {
                        editText.getValue().setError(activity.getResources().getString(editText.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                        editText.getValue().requestFocus();
                    }
                }
            } /*else if (!date.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.deadline_snackbar, Snackbar.LENGTH_LONG).show();
            }*/ else {
                HomeworksAdapter homeworksAdapter = (HomeworksAdapter) listView.getAdapter();
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                homework.setSubject(subject.getText().toString());
                homework.setDescription(description.getText().toString());
                homework.setColor(buttonColor.getColor());
                dbHelper.updateHomework(homework);
                homeworksAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }

    public static void getAddHomeworkDialog(@NonNull final AppCompatActivity activity,
                                            @NonNull final View alertLayout, @NonNull final HomeworksAdapter adapter) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText subject = alertLayout.findViewById(R.id.subjecthomework);
        editTextHashs.put(R.string.subject, subject);
        subject.requestFocus();
        final EditText description = alertLayout.findViewById(R.id.descriptionhomework);
        editTextHashs.put(R.string.description, description);
        final TextView date = alertLayout.findViewById(R.id.datehomework);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Homework homework = new Homework();

        date.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int mYear = calendar.get(Calendar.YEAR);
            int mMonth = calendar.get(Calendar.MONTH);
            int mdayofMonth = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(activity, (view, year, month, dayOfMonth) -> {
                date.setText(String.format("%02d-%02d-%02d", year, month + 1, dayOfMonth));
                homework.setDate(String.format("%02d-%02d-%02d", year, month + 1, dayOfMonth));
            }, mYear, mMonth, mdayofMonth);
            datePickerDialog.setTitle(R.string.choose_date);
            datePickerDialog.show();
        });

        select_color.setOnClickListener(v -> {
            ColorPicker colorPicker = new ColorPicker(activity);
            colorPicker.setRoundColorButton(true);
            colorPicker.setDefaultColorButton(((ColorDrawable) select_color.getBackground()).getColor());
            colorPicker.setColors(ColorPalette.PRIMARY_COLORS);
            colorPicker.setTitle(activity.getString(R.string.choose_color));
            colorPicker.show();
            colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                @Override
                public void onChooseColor(int position, int color) {
                    if (color != 0) {
                        select_color.setBackgroundColor(color);
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                    }
                }

                @Override
                public void onCancel() {
                    // put code
                }
            });
        });

        DbHelper dbHelper = new DbHelper(activity);
        ArrayList<Week> alreadyInsertedWeeks = WeekUtils.getAllWeeks(dbHelper);

        subject.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            event != null &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed()) {
                            // the user is done typing.
                            //AutoFill other fields
                            for (Week w : alreadyInsertedWeeks) {
                                if (w.getSubject().equalsIgnoreCase(v.getText().toString())) {
                                    select_color.setBackgroundColor(w.getColor());
                                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
//                                    date.setText(DBUtil.getNextOccurenceOfSubject(dbHelper, w.getSubject()));
//                                    homework.setDate(DBUtil.getNextOccurenceOfSubject(dbHelper, w.getSubject()));
                                }
                            }

                            return true;
                        }
                    }
                    return false;
                }
        );
        subject.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                for (Week w : alreadyInsertedWeeks) {
                    if (w.getSubject().equalsIgnoreCase(((EditText) v).getText().toString())) {
                        select_color.setBackgroundColor(w.getColor());
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
//                                    date.setText(DBUtil.getNextOccurenceOfSubject(dbHelper, w.getSubject()));
//                                    homework.setDate(DBUtil.getNextOccurenceOfSubject(dbHelper, w.getSubject()));
                    }
                }
            }
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.add_homework);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(view -> dialog.show());

        cancel.setOnClickListener(v -> {
            subject.getText().clear();
            description.getText().clear();
            select_color.setBackgroundColor(Color.WHITE);
            subject.requestFocus();
            dialog.dismiss();
        });

        save.setOnClickListener(v -> {
            if (TextUtils.isEmpty(subject.getText()) || TextUtils.isEmpty(description.getText())) {
                for (Map.Entry<Integer, EditText> editText : editTextHashs.entrySet()) {
                    if (TextUtils.isEmpty(editText.getValue().getText())) {
                        editText.getValue().setError(activity.getResources().getString(editText.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                        editText.getValue().requestFocus();
                    }
                }
            }/* else if (!date.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.deadline_snackbar, Snackbar.LENGTH_LONG).show();
            }*/ else {
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                homework.setSubject(subject.getText().toString());
                homework.setDescription(description.getText().toString());
                homework.setColor(buttonColor.getColor());
                dbHelper.insertHomework(homework);

                adapter.clear();
                adapter.addAll(dbHelper.getHomework());
                adapter.notifyDataSetChanged();

                subject.getText().clear();
                description.getText().clear();
                date.setText(R.string.choose_date);
                select_color.setBackgroundColor(Color.WHITE);
                subject.requestFocus();
                dialog.dismiss();
            }
        });
    }

    public static void getEditNoteDialog(@NonNull final AppCompatActivity activity,
                                         @NonNull final View alertLayout, @NonNull final ArrayList<Note> adapter,
                                         @NonNull final ListView listView, int listposition) {
        final EditText title = alertLayout.findViewById(R.id.titlenote);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Note note = adapter.get(listposition);
        title.setText(note.getTitle());
        select_color.setBackgroundColor(note.getColor() != 0 ? note.getColor() : Color.WHITE);
        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(note.getColor(), Color.WHITE, Color.BLACK));

        select_color.setOnClickListener(v -> {
            ColorPicker colorPicker = new ColorPicker(activity);
            colorPicker.setRoundColorButton(true);
            colorPicker.setDefaultColorButton(((ColorDrawable) select_color.getBackground()).getColor());
            colorPicker.setColors(ColorPalette.PRIMARY_COLORS);
            colorPicker.setTitle(activity.getString(R.string.choose_color));
            colorPicker.show();
            colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                @Override
                public void onChooseColor(int position, int color) {
                    if (color != 0) {
                        select_color.setBackgroundColor(color);
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                    }
                }

                @Override
                public void onCancel() {
                    // put code
                }
            });
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.edit_note);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.show();

        cancel.setOnClickListener(v -> {
            title.getText().clear();
            select_color.setBackgroundColor(Color.WHITE);
            dialog.dismiss();
        });

        save.setOnClickListener(v -> {
            if (TextUtils.isEmpty(title.getText())) {
                title.setError(activity.getResources().getString(R.string.title_error));
                title.requestFocus();
            } else {
                DbHelper dbHelper = new DbHelper(activity);
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                note.setTitle(title.getText().toString());
                note.setColor(buttonColor.getColor());
                dbHelper.updateNote(note);
                NotesAdapter notesAdapter = (NotesAdapter) listView.getAdapter();
                notesAdapter.notifyDataSetChanged();

                dialog.dismiss();
            }
        });
    }

    public static void getAddNoteDialog(@NonNull final AppCompatActivity activity,
                                        @NonNull final View alertLayout, @NonNull final NotesAdapter adapter) {
        final EditText title = alertLayout.findViewById(R.id.titlenote);
        title.requestFocus();
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        final Note note = new Note();

        select_color.setOnClickListener(v -> {
            ColorPicker colorPicker = new ColorPicker(activity);
            colorPicker.setRoundColorButton(true);
            colorPicker.setDefaultColorButton(((ColorDrawable) select_color.getBackground()).getColor());
            colorPicker.setColors(ColorPalette.PRIMARY_COLORS);
            colorPicker.setTitle(activity.getString(R.string.choose_color));
            colorPicker.show();
            colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                @Override
                public void onChooseColor(int position, int color) {
                    if (color != 0) {
                        select_color.setBackgroundColor(color);
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                    }
                }

                @Override
                public void onCancel() {
                    // put code
                }
            });
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.add_note);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(view -> dialog.show());

        cancel.setOnClickListener(v -> {
            title.getText().clear();
            select_color.setBackgroundColor(Color.WHITE);
            dialog.dismiss();
        });

        save.setOnClickListener(v -> {
            if (TextUtils.isEmpty(title.getText())) {
                title.setError(activity.getResources().getString(R.string.title_error));
                title.requestFocus();
            } else {
                DbHelper dbHelper = new DbHelper(activity);
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                note.setTitle(title.getText().toString());
                note.setColor(buttonColor.getColor());
                dbHelper.insertNote(note);

                adapter.clear();
                adapter.addAll(dbHelper.getNote());
                adapter.notifyDataSetChanged();

                title.getText().clear();
                select_color.setBackgroundColor(Color.WHITE);
                dialog.dismiss();
            }
        });
    }

    public static void getEditExamDialog(
            @NonNull final AppCompatActivity activity, @NonNull final View alertLayout,
            @NonNull final ArrayList<Exam> adapter, @NonNull final ListView listView,
            int listposition) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText subject = alertLayout.findViewById(R.id.subjectexam_dialog);
        editTextHashs.put(R.string.subject, subject);
        final EditText teacher = alertLayout.findViewById(R.id.teacherexam_dialog);
//        editTextHashs.put(R.string.teacher, teacher);
        final EditText room = alertLayout.findViewById(R.id.roomexam_dialog);
//        editTextHashs.put(R.string.room, room);
        final TextView date = alertLayout.findViewById(R.id.dateexam_dialog);
        final TextView time = alertLayout.findViewById(R.id.timeexam_dialog);
        final Button select_color = alertLayout.findViewById(R.id.select_color);

        final Exam exam = adapter.get(listposition);

        subject.setText(exam.getSubject());
        teacher.setText(exam.getTeacher());
        room.setText(exam.getRoom());
        date.setText(exam.getDate());
        time.setText(exam.getTime());
        select_color.setBackgroundColor(exam.getColor());
        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(exam.getColor(), Color.WHITE, Color.BLACK));

        date.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int mYear = calendar.get(Calendar.YEAR);
            int mMonth = calendar.get(Calendar.MONTH);
            int mdayofMonth = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(activity, (view, year, month, dayOfMonth) -> {
                date.setText(String.format("%02d-%02d-%02d", year, month + 1, dayOfMonth));
                exam.setDate(String.format("%02d-%02d-%02d", year, month + 1, dayOfMonth));
            }, mYear, mMonth, mdayofMonth);
            datePickerDialog.setTitle(R.string.choose_date);
            datePickerDialog.show();
        });

        time.setOnClickListener(v -> {
            int mHour = Integer.parseInt(exam.getTime().substring(0, exam.getTime().indexOf(":")));
            int mMinute = Integer.parseInt(exam.getTime().substring(exam.getTime().indexOf(":") + 1));
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute) -> {
                        time.setText(String.format("%02d:%02d", hourOfDay, minute));
                        exam.setTime(String.format("%02d:%02d", hourOfDay, minute));
                    }, mHour, mMinute, true);
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });


        select_color.setOnClickListener(v -> {
            ColorPicker colorPicker = new ColorPicker(activity);
            colorPicker.setRoundColorButton(true);
            colorPicker.setDefaultColorButton(((ColorDrawable) select_color.getBackground()).getColor());
            colorPicker.setColors(ColorPalette.PRIMARY_COLORS);
            colorPicker.setTitle(activity.getString(R.string.choose_color));
            colorPicker.show();
            colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                @Override
                public void onChooseColor(int position, int color) {
                    if (color != 0) {
                        select_color.setBackgroundColor(color);
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                    }
                }

                @Override
                public void onCancel() {
                    // put code
                }
            });
        });


        DbHelper dbHelper = new DbHelper(activity);
        ArrayList<Week> alreadyInsertedWeeks = WeekUtils.getAllWeeks(dbHelper);

        subject.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            event != null &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed()) {
                            // the user is done typing.
                            //AutoFill other fields
                            for (Week w : alreadyInsertedWeeks) {
                                if (w.getSubject().equalsIgnoreCase(v.getText().toString())) {
                                    if (teacher.getText().toString().trim().isEmpty())
                                        teacher.setText(w.getTeacher());
                                    if (room.getText().toString().trim().isEmpty())
                                        room.setText(w.getRoom());
                                    select_color.setBackgroundColor(w.getColor());
                                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                                }
                            }

                            return true;
                        }
                    }
                    return false;
                }
        );
        subject.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                for (Week w : alreadyInsertedWeeks) {
                    if (w.getSubject().equalsIgnoreCase(((EditText) v).getText().toString())) {
                        if (teacher.getText().toString().trim().isEmpty())
                            teacher.setText(w.getTeacher());
                        if (room.getText().toString().trim().isEmpty())
                            room.setText(w.getRoom());
                        select_color.setBackgroundColor(w.getColor());
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                    }
                }
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(activity.getResources().getString(R.string.add_exam));
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.show();

        cancel.setOnClickListener(v -> {
            subject.getText().clear();
            teacher.getText().clear();
            room.getText().clear();
            select_color.setBackgroundColor(Color.WHITE);
            subject.requestFocus();
            dialog.dismiss();
        });

        save.setOnClickListener(v -> {
            if (TextUtils.isEmpty(subject.getText())/* || TextUtils.isEmpty(teacher.getText()) || TextUtils.isEmpty(room.getText())*/) {
                for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                    if (TextUtils.isEmpty(entry.getValue().getText())) {
                        entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                        entry.getValue().requestFocus();
                    }
                }
            } else if (!date.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.date_error, Snackbar.LENGTH_LONG).show();
            } /*else if (!time.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.time_error, Snackbar.LENGTH_LONG).show();
            }*/ else {
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                exam.setSubject(subject.getText().toString());
                exam.setTeacher(teacher.getText().toString());
                exam.setRoom(room.getText().toString());
                exam.setColor(buttonColor.getColor());

                dbHelper.updateExam(exam);

                ExamsAdapter examsAdapter = (ExamsAdapter) listView.getAdapter();
                examsAdapter.notifyDataSetChanged();

                dialog.dismiss();
            }
        });
    }

    public static void getAddExamDialog(
            @NonNull final AppCompatActivity activity,
            @NonNull final View alertLayout, @NonNull final ExamsAdapter adapter) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText subject = alertLayout.findViewById(R.id.subjectexam_dialog);
        editTextHashs.put(R.string.subject, subject);
        subject.requestFocus();
        final EditText teacher = alertLayout.findViewById(R.id.teacherexam_dialog);
//        editTextHashs.put(R.string.teacher, teacher);
        final EditText room = alertLayout.findViewById(R.id.roomexam_dialog);
//        editTextHashs.put(R.string.room, room);
        final TextView date = alertLayout.findViewById(R.id.dateexam_dialog);
        final TextView time = alertLayout.findViewById(R.id.timeexam_dialog);
        final Button select_color = alertLayout.findViewById(R.id.select_color);

        final Exam exam = new Exam();

        date.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int mYear = calendar.get(Calendar.YEAR);
            int mMonth = calendar.get(Calendar.MONTH);
            int mdayofMonth = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(activity, (view, year, month, dayOfMonth) -> {
                date.setText(String.format("%02d-%02d-%02d", year, month + 1, dayOfMonth));
                exam.setDate(String.format("%02d-%02d-%02d", year, month + 1, dayOfMonth));
            }, mYear, mMonth, mdayofMonth);
            datePickerDialog.setTitle(R.string.choose_date);
            datePickerDialog.show();
        });

        time.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int mHour = c.get(Calendar.HOUR_OF_DAY);
            int mMinute = c.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute) -> {
                        time.setText(String.format("%02d:%02d", hourOfDay, minute));
                        exam.setTime(String.format("%02d:%02d", hourOfDay, minute));
                    }, mHour, mMinute, true);
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });

        select_color.setOnClickListener(v -> {
            ColorPicker colorPicker = new ColorPicker(activity);
            colorPicker.setRoundColorButton(true);
            colorPicker.setDefaultColorButton(((ColorDrawable) select_color.getBackground()).getColor());
            colorPicker.setColors(ColorPalette.PRIMARY_COLORS);
            colorPicker.setTitle(activity.getString(R.string.choose_color));
            colorPicker.show();
            colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                @Override
                public void onChooseColor(int position, int color) {
                    if (color != 0) {
                        select_color.setBackgroundColor(color);
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                    }
                }

                @Override
                public void onCancel() {
                    // put code
                }
            });
        });

        DbHelper dbHelper = new DbHelper(activity);
        ArrayList<Week> alreadyInsertedWeeks = WeekUtils.getAllWeeks(dbHelper);

        subject.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            event != null &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed()) {
                            // the user is done typing.
                            //AutoFill other fields
                            for (Week w : alreadyInsertedWeeks) {
                                if (w.getSubject().equalsIgnoreCase(v.getText().toString())) {
                                    if (teacher.getText().toString().trim().isEmpty())
                                        teacher.setText(w.getTeacher());
                                    if (room.getText().toString().trim().isEmpty())
                                        room.setText(w.getRoom());
                                    select_color.setBackgroundColor(w.getColor());
                                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                                }
                            }

                            return true;
                        }
                    }
                    return false;
                }
        );
        subject.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                for (Week w : alreadyInsertedWeeks) {
                    if (w.getSubject().equalsIgnoreCase(((EditText) v).getText().toString())) {
                        if (teacher.getText().toString().trim().isEmpty())
                            teacher.setText(w.getTeacher());
                        if (room.getText().toString().trim().isEmpty())
                            room.setText(w.getRoom());
                        select_color.setBackgroundColor(w.getColor());
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                    }
                }
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(activity.getResources().getString(R.string.add_exam));
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(view -> dialog.show());

        cancel.setOnClickListener(v -> {
            subject.getText().clear();
            teacher.getText().clear();
            room.getText().clear();
            select_color.setBackgroundColor(Color.WHITE);
            subject.requestFocus();
            dialog.dismiss();
        });

        save.setOnClickListener(v -> {
            if (TextUtils.isEmpty(subject.getText()) /*|| TextUtils.isEmpty(teacher.getText()) || TextUtils.isEmpty(room.getText())*/) {
                for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                    if (TextUtils.isEmpty(entry.getValue().getText())) {
                        entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                        entry.getValue().requestFocus();
                    }
                }
            } else if (!date.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.date_error, Snackbar.LENGTH_LONG).show();
            } /*else if (!time.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.time_error, Snackbar.LENGTH_LONG).show();
            }*/ else {
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                exam.setSubject(subject.getText().toString());
                exam.setTeacher(teacher.getText().toString());
                exam.setRoom(room.getText().toString());
                exam.setColor(buttonColor.getColor());

                dbHelper.insertExam(exam);

                adapter.clear();
                adapter.addAll(dbHelper.getExam());
                adapter.notifyDataSetChanged();

                subject.getText().clear();
                teacher.getText().clear();
                room.getText().clear();
                date.setText(R.string.choose_date);
                time.setText(R.string.select_time);
                select_color.setBackgroundColor(Color.WHITE);
                subject.requestFocus();
                dialog.dismiss();
            }
        });
    }

    public static void getDeleteDialog(Context context, Runnable
            runnable) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(context.getString(R.string.profiles_delete_submit_heading));


        builder.positiveText(context.getString(R.string.yes));
        builder.onPositive((dialog, which) -> {
            runnable.run();
            dialog.dismiss();
        });

        builder.onNegative((dialog, which) -> dialog.dismiss());

        builder.negativeText(context.getString(R.string.no));

        builder.show();
    }
}
