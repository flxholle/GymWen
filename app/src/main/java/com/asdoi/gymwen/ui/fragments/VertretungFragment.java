package com.asdoi.gymwen.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.lehrerliste.Lehrerliste;
import com.asdoi.gymwen.ui.activities.MainActivity;
import com.asdoi.gymwen.util.PreferenceUtil;
import com.asdoi.gymwen.vertretungsplan.VertretungsPlanFeatures;
import com.pd.chocobar.ChocoBar;

import java.util.ArrayList;

public class VertretungFragment extends Fragment implements View.OnClickListener {
    private View root;
    private Context context;
    private boolean today;
    private boolean all;
    private boolean both;

    public static final int Instance_Both = 0;
    public static final int Instance_Today = 1;
    public static final int Instance_Tomorrow = 2;
    public static final int Instance_Today_All = 3;
    public static final int Instance_Tomorrow_All = 4;

    private static final String TODAY = "today";
    private static final String ALL = "all";
    private static final String BOTH = "both";

    public static boolean changedSectionsPagerAdapterTitles = false;

    public static VertretungFragment newInstance(int state) {
        VertretungFragment fragment = new VertretungFragment();
        Bundle bundle = new Bundle();

        boolean today = false;
        boolean all = false;
        boolean both = false;

        switch (state) {
            case 1:
                //Today
                today = true;
                break;
            case 3:
                //Today All
                today = true;
                all = true;
                break;
            case 2:
                //Tomorrow
                break;
            case 4:
                //Tomorrow All
                all = true;
                break;
            case 0:
            default:
                //Both
                both = true;
                break;

        }
        bundle.putBoolean(TODAY, today);
        bundle.putBoolean(ALL, all);
        bundle.putBoolean(BOTH, both);
        MainActivity.vertretungFragmentState = state;
        fragment.setArguments(bundle);
        return fragment;
    }

    public void update(boolean all) {
        // this method will be called for every fragment in viewpager
        // so check if update is for this fragment
        if (all != this.all) {
            this.all = all;
        }
        //Loading Pabel
        ((ActivityFeatures) getActivity()).createLoadingPanel(root.findViewById(R.id.vertretung_frame));
        refreshAndTable();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            today = getArguments().getBoolean(TODAY);
            all = getArguments().getBoolean(ALL);
            both = getArguments().getBoolean(BOTH);
        } catch (Exception e) {
            //No Arguments set
            today = false;
            all = false;
            both = true;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        root = inflater.inflate(R.layout.fragment_vertretung, container, false);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        //Loading Pabel
        ((ActivityFeatures) getActivity()).createLoadingPanel(root.findViewById(R.id.vertretung_frame));

        refreshAndTable();
    }

    private void refreshAndTable() {
        new Thread(() -> {
            ApplicationFeatures.downloadVertretungsplanDocs(false, true);
            try {
                getActivity().runOnUiThread(() -> {
                    try {
                        if (!changedSectionsPagerAdapterTitles && VertretungsPlanFeatures.areDocsDownloaded()) {
                            MainActivity.SectionsPagerAdapter spa = ((MainActivity) getActivity()).sectionsPagerAdapter;
                            spa.setTitles(VertretungsPlanFeatures.getTodayTitleArray()[1], VertretungsPlanFeatures.getTomorrowTitleArray()[1]);
                            spa.notifyDataSetChanged();
                            changedSectionsPagerAdapterTitles = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    generateTable();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_fab:
                share();
                break;
        }
    }


    //Share
    private void share() {
        String message = shareMessage(true);
        String footprint = getString(R.string.footprint);
        message += footprint;

        if (VertretungsPlanFeatures.getTodayTitle().equals("Keine Internetverbindung!")) {
            //Toast.makeText(getActivity(), "Du bist nicht mit dem Internet verbunden!",Toast.LENGTH_LONG).show();
            ChocoBar.builder().setActivity(getActivity()).setText(getString(R.string.noInternet)).setDuration(ChocoBar.LENGTH_LONG).orange().show();
            return;
        }

        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, message);
        i.setType("text/plain");
        startActivity(Intent.createChooser(i, getString(R.string.share_vertretung)));
    }

    private String shareMessage(boolean withCourses) {
        String message = "";
        String[][] inhalt = null;
        String title = "";

        if (both) {
            both = false;
            today = true;
            message += shareMessage(true);
            today = false;
            message += shareMessage(false);
            return message;
        }
        //Deactivated cause of ViewPager
        /*else if (today) {
            inhalt = VertretungsPlanFeatures.getTodayArray();
            title = VertretungsPlanFeatures.getTodayTitle();
        } else {
            inhalt = VertretungsPlanFeatures.getTomorrowArray();
            title = VertretungsPlanFeatures.getTomorrowTitle();
        }*/


        String classes = "";

        if (inhalt == null) {
            return "";
        }

        if (VertretungsPlanFeatures.getOberstufe()) {
            ArrayList<String> courses = VertretungsPlanFeatures.getNames();
            for (int i = 0; i < courses.size() - 1; i++) {
                classes += courses.get(i) + ", ";
            }
            classes += courses.get(courses.size() - 1);
            if (inhalt.length == 0) {
                message = context.getString(R.string.share_msg_nothing_at) + " " + title + (withCourses ? " (" + context.getString(R.string.share_msg_for_courses) + " " + classes + ")\n" : "\n");
                return message;
            } else
                message = context.getString(R.string.share_msg_vertretung_at) + " " + title + ":\n";
        } else {
            ArrayList<String> names = VertretungsPlanFeatures.getNames();
            for (int i = 0; i < names.size(); i++) {
                classes += names.get(i) + "";
            }
            if (inhalt.length == 0) {
                message = context.getString(R.string.share_msg_nothing_at) + " " + title + (withCourses ? " (" + classes + ")\n" : "\n");
                return message;
            } else
                message = context.getString(R.string.share_msg_vertretung_at) + " " + title + (withCourses ? " (" + classes + "):\n" : ":\n");
        }

        String freespace = "    ";
        if (VertretungsPlanFeatures.getOberstufe()) {
            for (String[] line : inhalt) {
                if (VertretungsPlanFeatures.isNothing(line[3])) {
                    message += freespace + line[1] + ". " + context.getString(R.string.share_msg_nothing_hour_oberstufe) + " " + line[0] + "\n";
                } else {
                    message += freespace + line[1] + ". " + context.getString(R.string.share_msg_hour_oberstufe) + " " + line[0] + " " + context.getString(R.string.share_msg_in_room) + " " + line[4] + " " + context.getString(R.string.with_teacher) + " " + line[3] + ", " + line[5] + "\n";
                }
            }
        } else {
            for (String[] line : inhalt) {
                if (VertretungsPlanFeatures.isNothing(line[3])) {
                    message += freespace + line[1] + ". " + context.getString(R.string.share_msg_nothing_hour) + "\n";
                } else {
                    message += freespace + line[1] + ". " + context.getString(R.string.share_msg_hour) + " " + line[0] + " " + context.getString(R.string.share_msg_in_room) + " " + line[4] + " " + context.getString(R.string.with_teacher) + " " + line[3] + ", " + line[5] + "\n";
                }
            }
        }
        return message;
    }


    //TeacherSearch
    void teacherClick(TextView view, String teacherQuery, boolean showBorders, boolean fullNames) {
        if (VertretungsPlanFeatures.isNothing(teacherQuery) || Lehrerliste.isAOL(teacherQuery))
            return;
        int padding = 0;
        if (showBorders) {
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.background_shape);
            try {
                Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(wrappedDrawable, ApplicationFeatures.getTextColorPrimary(context));
            } catch (Exception e) {
                e.printStackTrace();
            }
            view.setBackground(drawable);
            padding = 7;
        } else {
            view.setBackgroundResource(android.R.drawable.list_selector_background);
        }
        view.setPadding(padding, padding, padding, padding);


        if (fullNames) {
            new Thread(() -> {
                ApplicationFeatures.downloadLehrerDoc();
                try {
                    getActivity().runOnUiThread(() -> {
                        String match = getMatchingTeacher(teacherQuery);
                        if (match != null)
                            view.setText(match);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            view.setText(teacherQuery);
        }


        view.setClickable(true);
        view.setOnClickListener((View v) -> {
            if (ApplicationFeatures.isNetworkAvailable()) {
                new Thread(() -> {
                    ApplicationFeatures.downloadLehrerDoc();
                    try {
                        getActivity().runOnUiThread(() -> {
                            teacherSearch(teacherQuery);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                teacherSearch(teacherQuery);
            }

        });
    }

    void removeTeacherClick(View view) {
        view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        view.setBackgroundResource(0);
        view.setClickable(false);
        view.setOnClickListener(null);
    }

    void teacherSearch(String query) {
        try {
            ApplicationFeatures.downloadLehrerDoc();
            createTeacherView(Lehrerliste.getTeacher(query));
        } catch (NullPointerException e) {
            e.printStackTrace();
            if (!Lehrerliste.isDownloaded()) {
                ChocoBar.builder().setActivity(getActivity())
                        .setText(getString(R.string.noInternet))
                        .setDuration(ChocoBar.LENGTH_LONG)
                        .orange()
                        .show();
            } else {
                ChocoBar.builder().setActivity(getActivity())
                        .setText(getString(R.string.teacher_no_teacher_found))
                        .setDuration(ChocoBar.LENGTH_LONG)
                        .red()
                        .show();
            }
        }
    }

    private void createTeacherView(String[] teacher) {
        LinearLayout base = new LinearLayout(context);
        base.setOrientation(LinearLayout.VERTICAL);
        base.setGravity(Gravity.CENTER);
        base.setBackgroundColor(ApplicationFeatures.getTextColorSecondary(context));
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        base.setLayoutParams(params);
        base.setId(ApplicationFeatures.vertretung_teacher_view_id);
        base.setOnClickListener((View v) -> {
            try {
                ((ViewGroup) v.getParent()).removeView(v);
            } catch (NullPointerException e) {
                v.setVisibility(View.GONE);
            }
        });

        ViewGroup teacherEntry = new LinearLayout(context);
        ViewStub viewStub = new ViewStub(context);
        viewStub.setLayoutResource(R.layout.list_lehrerliste_entry);
        teacherEntry.addView(viewStub);
        viewStub.inflate();
        teacherEntry = (ViewGroup) ((MainActivity) getActivity()).getTeacherView(teacherEntry, teacher);

        LinearLayout background = new LinearLayout(context);
        params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        background.setLayoutParams(params);
        background.setBackgroundColor(ApplicationFeatures.getBackgroundColor(context));

        background.addView(teacherEntry);
        base.addView(background);
        ((ViewGroup) root.findViewById(R.id.vertretung_frame)).addView(base);
    }

    String getMatchingTeacher(String query) {
        String teacher = null;
        try {
            ApplicationFeatures.downloadLehrerDoc();
            String[] response = Lehrerliste.getTeacher(query);
            teacher = response[1];
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return teacher;
    }


    //Generating Table
    boolean oberstufe;
    String[][] inhalt;
    String title;
    boolean sonstiges;

    void generateTable() {
        clear();

        oberstufe = VertretungsPlanFeatures.getOberstufe();

        LinearLayout rootView = root.findViewById(R.id.vertretung_linear_layout_layer1);

        if (both) {
            inhalt = VertretungsPlanFeatures.getTodayArray();
            title = VertretungsPlanFeatures.getTodayTitle();
            sonstiges = isSonstiges(inhalt);

//            generateScrollView(rootView);

            generateTop(rootView);
            generateTableSpecific(rootView);

            if (inhalt != null) {
                inhalt = VertretungsPlanFeatures.getTomorrowArray();
                title = VertretungsPlanFeatures.getTomorrowTitle();
                sonstiges = isSonstiges(inhalt);
//                LinearLayout tomorrow = new LinearLayout(context);
//                tomorrow.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                TextView title = generateTop(rootView, true);
                ListView content = generateTableSpecific(rootView, true);
                content.addHeaderView(title);
//                tomorrow.addView(title);
//                tomorrow.addView(content);

                vertretungListView.addFooterView(content);
            }
        } else if (all) {
            if (today) {
                inhalt = VertretungsPlanFeatures.getTodayArrayAll();
                title = VertretungsPlanFeatures.getTodayTitle();
            } else {
                inhalt = VertretungsPlanFeatures.getTomorrowArrayAll();
                title = VertretungsPlanFeatures.getTomorrowTitle();
            }
            String missing_short = getString(R.string.missing_short);
            for (String s : VertretungsPlanFeatures.getNothing()) {
                inhalt = replaceAll(inhalt, s, missing_short);
            }
            sonstiges = isSonstiges(inhalt);
            generateTop(rootView);
            generateTableAll(rootView);
        } else {
            if (today) {
                inhalt = VertretungsPlanFeatures.getTodayArray();
                title = VertretungsPlanFeatures.getTodayTitle();
            } else {
                inhalt = VertretungsPlanFeatures.getTomorrowArray();
                title = VertretungsPlanFeatures.getTomorrowTitle();
            }
            sonstiges = isSonstiges(inhalt);
            generateTop(rootView);
            generateTableSpecific(rootView);
        }
    }

    public void clear() {
        ((ViewGroup) root.findViewById(R.id.vertretung_frame)).removeView(root.findViewWithTag("vertretung_loading"));
        LinearLayout base = root.findViewById(R.id.vertretung_linear_layout_layer1);
        base.removeAllViews();
    }

    LinearLayout generateScrollView(LinearLayout addToScroll) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        NestedScrollView s = new NestedScrollView(context);
        s.setLayoutParams(params);

        LinearLayout l = addToScroll;
        l.setLayoutParams(params);

        ViewGroup parent = (ViewGroup) l.getParent();
        parent.removeAllViews();

        parent.addView(s);
        s.addView(l);

        return l;
    }

    String[][] replaceAll(String[][] value, String regex, String replace) {
        if (value == null) {
            return value;
        }
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[i].length; j++) {
                if (value[i][j].equals(regex))
                    value[i][j] = replace;
            }
        }
        return value;
    }

    void generateTop(ViewGroup base) {
        generateTop(base, false);
    }

    //Top (Date or noInternet, etc.)
    TextView generateTop(ViewGroup base, boolean returnIt) {
        TextView titleView = createTitleLayout();
        if (!returnIt)
            base.addView(titleView);

        if (inhalt == null) {
            titleView.setText(context.getString(R.string.noInternetConnection));
            return titleView;
        } else {
            titleView.setText(title);
        }

        if (inhalt.length == 0) {
            TextView tv = new TextView(context);
            tv.setTextColor(ApplicationFeatures.getTextColorSecondary(context));
            tv.setText(context.getString(R.string.nothing));
            tv.setTextSize(20);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            if (!returnIt)
                base.addView(tv);
        }

        return titleView;
    }

    TextView createTitleLayout() {
        TextView textView = new TextView(context);
        textView.setTextColor(ApplicationFeatures.getTextColorPrimary(context));
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return textView;
    }

    public static boolean isSonstiges(String[][] inhalt) {
        if (inhalt == null)
            return false;
        for (int i = 0; i < inhalt.length; i++) {
            if (!inhalt[i][5].trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static String[] generateHeadline(Context context, boolean sonstiges, boolean oberstufe, boolean all) {
        String[] headline;

        if (all) {
            headline = new String[]{context.getString(R.string.classes), sonstiges ? context.getString(R.string.hours_short) : context.getString(R.string.hours), context.getString(R.string.subject), context.getString(R.string.teacher_short), sonstiges ? context.getString(R.string.room_short) : context.getString(R.string.room), context.getString(R.string.other)};
        } else if (oberstufe) {
            headline = new String[]{sonstiges ? context.getString(R.string.hours_short_three) : context.getString(R.string.hours), context.getString(R.string.courses), sonstiges ? context.getString(R.string.teacher_short) : context.getString(R.string.teacher), context.getString(R.string.room), sonstiges ? context.getString(R.string.other_short) : "", context.getString(R.string.subject)};
        } else {
            headline = new String[]{sonstiges ? context.getString(R.string.hours_short_three) : context.getString(R.string.hours), context.getString(R.string.subject), sonstiges ? context.getString(R.string.teacher_short) : context.getString(R.string.teacher), context.getString(R.string.room), sonstiges ? context.getString(R.string.other_short) : "", context.getString(R.string.classes)};
        }

        return headline;
    }


    //Body
    void generateTableAll(ViewGroup base) {
        if (inhalt != null && inhalt.length > 0) {
            //Overview
            base.addView(generateOverviewAll());

            //Content
            vertretungListView = new ListView(context);
            vertretungListView.setAdapter(new VertretungListAdapterAll(context, 0, inhalt, sonstiges));
            vertretungListView.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            base.addView(vertretungListView);
        }
    }

    View generateOverviewAll() {
        String[] headline = generateHeadline(context, sonstiges, oberstufe, true);
        LinearLayout base = new LinearLayout(context);
        base.setOrientation(LinearLayout.HORIZONTAL);
        base.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        for (int i = 0; i < headline.length; i++) {
            LinearLayout.LayoutParams params;
            TextView hour = createBlankTextView();

            switch (i) {
                case 0:
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_all_course));
                    hour.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    break;
                case 1:
                    if (PreferenceUtil.isHour()) {
                        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_all_hour_long));
                    } else {
                        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_all_hour));
                    }
                    break;
                case 2:
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_all_subject));
                    break;
                case 3:
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_all_teacher));
                    break;
                case 4:
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_all_room));
                    break;
                case 5:
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_all_other));
                    break;
                default:
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_all_room));
            }

            params.setMargins(3, 3, 3, 3);
            hour.setLayoutParams(params);
            hour.setText(headline[i]);
            base.addView(hour);
        }

        return base;
    }

    void generateTableSpecific(ViewGroup base) {
        generateTableSpecific(base, false);
    }

    ListView generateTableSpecific(ViewGroup base, boolean returnIt) {
        ListView listView;
        if (inhalt != null && inhalt.length > 0) {
            //Content
            listView = new ListView(context);
            listView.setAdapter(new VertretungListAdapterSpecific(context, 0, inhalt, sonstiges));
            listView.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            if (!PreferenceUtil.isOldDesign()) {
                listView.setDivider(null);
            } else {
                //Overview
                listView.addView(generateOverviewSpecific());
            }


            if (returnIt)
                return listView;
            else {
                vertretungListView = listView;
                base.addView(vertretungListView);
            }
        }
        return null;
    }

    View generateOverviewSpecific() {
        String[] headline = generateHeadline(context, sonstiges, oberstufe, false);

        LinearLayout base = new LinearLayout(context);
        base.setOrientation(LinearLayout.HORIZONTAL);
        base.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_hour));
        params.setMargins(3, 3, 3, 3);
        TextView hour = createBlankTextView();
        hour.setLayoutParams(params);
        hour.setText(headline[0]);
        base.addView(hour);

        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_subject));
        params.setMargins(3, 3, 3, 3);
        TextView subject = createBlankTextView();
        subject.setLayoutParams(params);
        subject.setText(headline[1]);
        base.addView(subject);

        if (PreferenceUtil.isHour()) {
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_hour_long)));
            subject.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_subject_long)));
        } else {
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_hour)));
            subject.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_subject)));
        }

        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_teacher));
        TextView teacher = createBlankTextView();
        teacher.setLayoutParams(params);
        teacher.setText(headline[2]);
        base.addView(teacher);

        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_room));
        params.setMargins(3, 3, 3, 3);
        TextView room = createBlankTextView();
        room.setLayoutParams(params);
        room.setText(headline[3]);
        base.addView(room);

        if (sonstiges) {
            params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_other));
            params.setMargins(3, 3, 3, 3);
            TextView other = createBlankTextView();
            other.setLayoutParams(params);
            other.setText(headline[4]);
            base.addView(other);
        }

        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_course));
        params.setMargins(3, 3, 3, 3);
        TextView course = createBlankTextView();
        course.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
        course.setTypeface(course.getTypeface(), Typeface.NORMAL);
        course.setLayoutParams(params);
        course.setText(headline[5]);
        base.addView(course);

        return base;
    }

    TextView createBlankTextView() {
        TextView hour = new TextView(context);
        hour.setTypeface(hour.getTypeface(), Typeface.BOLD);
        hour.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        hour.setTextColor(ApplicationFeatures.getTextColorSecondary(context));
        hour.setGravity(Gravity.CENTER);
        return hour;
    }

    private ListView vertretungListView;

    //All ListView
    private class VertretungListAdapterAll extends ArrayAdapter<String[]> {
        String[][] content;
        boolean sons;

        public VertretungListAdapterAll(Context con, int resource, String[][] content, boolean sons) {
            super(con, resource);
            this.content = content;
            this.sons = sons;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_vertretung_all_entry, null);
            }
            return getEntryAll(convertView, content[position], sons);
        }

        @Override
        public int getCount() {
            return content.length;
        }
    }

    private View getEntryAll(View view, String[] entry, boolean sonstiges) {
        TextView course = view.findViewById(R.id.vertretung_all_entry_textViewCourse);
        course.setText(entry[0]);

        TextView hour = view.findViewById(R.id.vertretung_all_entry_textViewHour);
        hour.setText(entry[1]);

        if (PreferenceUtil.isHour()) {
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_all_hour_long)));
        } else {
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_all_hour)));
        }

        TextView subject = view.findViewById(R.id.vertretung_all_entry_textViewSubject);
        subject.setText(entry[2]);

        TextView teacher = view.findViewById(R.id.vertretung_all_entry_textViewTeacher);

        removeTeacherClick(teacher);
        if (!VertretungsPlanFeatures.isNothing(entry[3]))
            teacherClick(teacher, entry[3], !ApplicationFeatures.getBooleanSettings("show_border_specific", true) && ApplicationFeatures.getBooleanSettings("show_borders", false), !PreferenceUtil.isFullTeacherNamesSpecific() && PreferenceUtil.isFullTeacherNames());
        else
            teacher.setText(entry[3]);

        TextView room = view.findViewById(R.id.vertretung_all_entry_textViewRoom);
        room.setText(entry[4]);

        TextView other = view.findViewById(R.id.vertretung_all_entry_textViewOther);
        other.setVisibility(View.VISIBLE);
        if (sonstiges) {
            other.setText(entry[5]);
        } else {
            other.setVisibility(View.GONE);
        }

        return view;
    }


    //Specific ListView
    private class VertretungListAdapterSpecific extends ArrayAdapter<String[]> {
        String[][] content;
        boolean sons;

        VertretungListAdapterSpecific(Context con, int resource, String[][] content, boolean sons) {
            super(con, resource);
            this.content = content;
            this.sons = sons;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (PreferenceUtil.isOldDesign()) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_vertretung_specific_entry, null);
                }
                return getEntrySpecific(convertView, content[position], oberstufe, sons);
            } else {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_vertretung_specific_card, null);
                }
                return getEntrySpecificNew(convertView, content[position], !content[position][5].trim().isEmpty());
            }
        }

        @Override
        public int getCount() {
            return content.length;
        }

    }

    public View getEntrySpecific(View view, String[] entry, boolean oberstufe, boolean sonstiges) {
        TextView hour = view.findViewById(R.id.vertretung_specific_entry_textViewHour);
        hour.setText(entry[1]);
        hour.setBackgroundColor(ApplicationFeatures.getAccentColor(context));

        TextView subject = view.findViewById(R.id.vertretung_specific_entry_textViewSubject);
        subject.setText(oberstufe ? entry[0] : entry[2]);

        if (PreferenceUtil.isHour()) {
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_hour_long)));
            subject.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_subject_long)));
            hour.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        } else {
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_hour)));
            subject.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_subject)));
            hour.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
        }

        TextView teacher = view.findViewById(R.id.vertretung_specific_entry_textViewTeacher);

        TextView room = view.findViewById(R.id.vertretung_specific_entry_textViewRoom);
        room.setTextColor(ApplicationFeatures.getAccentColor(context));


        if (!VertretungsPlanFeatures.isNothing(entry[3])) {
            teacher.setGravity(Gravity.CENTER);
            teacher.setTextColor(subject.getTextColors());
            teacher.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_teacher)));
            teacher.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            teacherClick(teacher, entry[3], ApplicationFeatures.getBooleanSettings("show_borders", true), PreferenceUtil.isFullTeacherNames());

            room.setVisibility(View.VISIBLE);
            room.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_room)));

            SpannableString content = new SpannableString(entry[4]);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            room.setText(content);
        } else {
            removeTeacherClick(teacher);
            teacher.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.vertretung_specific_entry_teacher) + context.getResources().getInteger(R.integer.vertretung_specific_entry_room)));

            SpannableString content = new SpannableString(entry[3]);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            teacher.setText(content);
            teacher.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
            teacher.setTextColor(ApplicationFeatures.getAccentColor(context));

            room.setVisibility(View.GONE);
        }

        TextView other = view.findViewById(R.id.vertretung_specific_entry_textViewOther);
        other.setVisibility(sonstiges ? View.VISIBLE : View.GONE);
        other.setText(entry[5]);

        TextView course = view.findViewById(R.id.vertretung_specific_entry_textViewClass);
        course.setText(oberstufe ? entry[2] : entry[0]);

        return view;
    }

    private View getEntrySpecificNew(View view, String[] entry, boolean sonstiges) {
        TextView course = view.findViewById(R.id.vertretung_card_entry_textViewClass);
        course.setText(entry[0]);

        TextView hour = view.findViewById(R.id.vertretung_card_entry_textViewHour);
        hour.setText(entry[1]);

        if (PreferenceUtil.isHour()) {
            hour.setTextSize(TypedValue.COMPLEX_UNIT_SP, 42);
        } else {
            hour.setTextSize(TypedValue.COMPLEX_UNIT_SP, 52);
        }

        TextView subject = view.findViewById(R.id.vertretung_card_entry_textViewSubject);

        TextView teacher = view.findViewById(R.id.vertretung_card_entry_textViewTeacher);

        TextView room = view.findViewById(R.id.vertretung_card_entry_textViewRoom);

        if (!VertretungsPlanFeatures.isNothing(entry[3])) {
            if (!entry[2].trim().isEmpty()) {
                teacher.setVisibility(View.VISIBLE);
                teacher.setGravity(Gravity.CENTER);
                teacher.setTextColor(subject.getTextColors());
                teacherClick(teacher, entry[3], ApplicationFeatures.getBooleanSettings("show_borders", false), PreferenceUtil.isFullTeacherNames());

                subject.setText(entry[2] + " " + context.getString(R.string.with_teacher) + " ");
            } else {
                teacherClick(subject, entry[3], ApplicationFeatures.getBooleanSettings("show_borders", false), PreferenceUtil.isFullTeacherNames());
                subject.setText(entry[3]);
            }

            SpannableString content = new SpannableString("in " + entry[4]);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            room.setText(content);
        } else {
            removeTeacherClick(view);
            teacher.setVisibility(View.GONE);

            subject.setText(entry[2]);

            SpannableString content = new SpannableString(entry[3]);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            room.setText(content);
        }


        TextView other = view.findViewById(R.id.vertretung_card_entry_textViewOther);
        other.setVisibility(View.VISIBLE);
        if (sonstiges) {
            other.setText(entry[5]);
        } else {
            other.setVisibility(View.GONE);
        }

        return view;
    }
}
