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
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.substitutionplan.SubstitutionEntry;
import com.asdoi.gymwen.substitutionplan.SubstitutionList;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlan;
import com.asdoi.gymwen.substitutionplan.SubstitutionPlanFeatures;
import com.asdoi.gymwen.substitutionplan.SubstitutionTitle;
import com.asdoi.gymwen.teacherlist.TeacherListEntry;
import com.asdoi.gymwen.teacherlist.TeacherlistFeatures;
import com.asdoi.gymwen.ui.activities.MainActivity;
import com.asdoi.gymwen.ui.activities.RoomPlanActivity;
import com.asdoi.gymwen.util.External_Const;
import com.asdoi.gymwen.util.PreferenceUtil;
import com.pd.chocobar.ChocoBar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class SubstitutionFragment extends Fragment implements View.OnClickListener {
    private View root;
    @Nullable
    private Context context;
    private boolean today;
    private boolean all;
    private boolean atOneGlance;
    private boolean changeViewPagerTitles; //In MainActivity

    public static final int Instance_AtOneGlance = 0;
    public static final int Instance_Today = 1;
    public static final int Instance_Tomorrow = 2;
    public static final int Instance_Today_All = 3;
    public static final int Instance_Tomorrow_All = 4;

    private static final String TODAY = "today";
    private static final String ALL = "all";
    private static final String ATONEGLANCE = "both";
    private static final String VIEWPAGERTITLES = "titles";

    public static boolean changedSectionsPagerAdapterTitles = false;

    @NonNull
    public static SubstitutionFragment newInstance(int state) {
        return newInstance(state, true);
    }

    @NonNull
    public static SubstitutionFragment newInstance(int state, boolean viewPagerTitles) {
        SubstitutionFragment fragment = new SubstitutionFragment();
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
        bundle.putBoolean(ATONEGLANCE, both);
        bundle.putBoolean(VIEWPAGERTITLES, viewPagerTitles);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void update(boolean all) {
        // this method will be called for every fragment in viewpager
        // so check if update is for this fragment
        if (all != this.all) {
            this.all = all;
        }
        //Loading Panel
        ((ActivityFeatures) requireActivity()).createLoadingPanel(root.findViewById(R.id.substitution_frame));
        refreshAndTable();

    }

    public void updateDay(boolean day) {
        today = day;
        //Loading Pabel
        ((ActivityFeatures) requireActivity()).createLoadingPanel(root.findViewById(R.id.substitution_frame));
        refreshAndTable();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            today = requireArguments().getBoolean(TODAY);
            all = requireArguments().getBoolean(ALL);
            atOneGlance = getArguments().getBoolean(ATONEGLANCE);
            changeViewPagerTitles = getArguments().getBoolean(VIEWPAGERTITLES);
        } catch (Exception e) {
            //No Arguments set
            today = false;
            all = false;
            atOneGlance = true;
            changeViewPagerTitles = true;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        root = inflater.inflate(R.layout.fragment_substitution, container, false);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        //Loading Pabel
        ((ActivityFeatures) requireActivity()).createLoadingPanel(root.findViewById(R.id.substitution_frame));

        refreshAndTable();
    }

    private void refreshAndTable() {
        new Thread(() -> {
            ApplicationFeatures.downloadSubstitutionplanDocs(false, true);
            try {
                SubstitutionTitle todayTitle = SubstitutionPlanFeatures.getTodayTitle();
                SubstitutionTitle tomorrowTitle = SubstitutionPlanFeatures.getTomorrowTitle();
                requireActivity().runOnUiThread(() -> {
                    try {
                        if (!changedSectionsPagerAdapterTitles && SubstitutionPlanFeatures.areDocsDownloaded() && changeViewPagerTitles) {
                            MainActivity.SectionsPagerAdapter spa = ((MainActivity) requireActivity()).sectionsPagerAdapter;
                            spa.setTitles(todayTitle.getDayOfWeek(), tomorrowTitle.getDayOfWeek());
                            spa.notifyDataSetChanged();
                            changedSectionsPagerAdapterTitles = true;
                        }
                        //Update menu Items for days
                        if (!todayTitle.getDayOfWeek().trim().isEmpty()) {
                            ((MainActivity) requireActivity()).setTodayMenuItemTitle(todayTitle.getDayOfWeek() + ", " + todayTitle.getDate());
                            ((MainActivity) getActivity()).setTomorrowMenuItemTitle(tomorrowTitle.getDayOfWeek() + ", " + tomorrowTitle.getDate());
                        }
                    } catch (Exception ignore) {
                    }
                });
                generateTable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.main_fab:
                share();
                break;
        }
    }


    //Share
    private void share() {
        String message = "";

        today = true;
        message += shareMessage(true);
        today = false;
        message += shareMessage(false);

        String footprint = getString(R.string.footprint);
        message += footprint;

        if (SubstitutionPlanFeatures.getTodayTitle().getNoInternet()) {
            //Toast.makeText(getActivity(), "Du bist nicht mit dem Internet verbunden!",Toast.LENGTH_LONG).show();
            ChocoBar.builder().setActivity(requireActivity()).setText(getString(R.string.noInternet)).setDuration(ChocoBar.LENGTH_LONG).orange().show();
            return;
        }

        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, message);
        i.setType("text/plain");
        startActivity(Intent.createChooser(i, getString(R.string.share_substitution_plan)));
    }

    @NonNull
    private String shareMessage(boolean withCourses) {
        StringBuilder message = new StringBuilder();
        SubstitutionList content;
        String title;
        boolean summarize = PreferenceUtil.isSummarizeUp();

        if (today) {
            content = summarize ? SubstitutionPlanFeatures.getTodaySummarized() : SubstitutionPlanFeatures.getToday();
            title = SubstitutionPlanFeatures.getTodayTitleString();
        } else {
            content = summarize ? SubstitutionPlanFeatures.getTomorrowSummarized() : SubstitutionPlanFeatures.getTomorrow();
            title = SubstitutionPlanFeatures.getTomorrowTitleString();
        }
        StringBuilder classes = new StringBuilder();

        if (content.getNoInternet()) {
            return "";
        }


        if (SubstitutionPlanFeatures.getSenior()) {
            ArrayList<String> courses = SubstitutionPlanFeatures.getNames();
            for (int i = 0; i < Objects.requireNonNull(courses).size() - 1; i++) {
                classes.append(courses.get(i)).append(", ");
            }
            classes.append(courses.get(courses.size() - 1));
            if (content.size() == 0) {
                message = new StringBuilder(requireContext().getString(R.string.share_msg_nothing_at) + " " + title + (withCourses ? " (" + requireContext().getString(R.string.share_msg_for_courses) + " " + classes + ")\n" : "\n"));
                return message.toString();
            } else
                message = new StringBuilder(requireContext().getString(R.string.share_msg_substitution_at) + " " + title + ":\n");
        } else {
            ArrayList<String> names = SubstitutionPlanFeatures.getNames();
            for (int i = 0; i < Objects.requireNonNull(names).size(); i++) {
                classes.append(names.get(i));
            }
            if (content.size() == 0) {
                message = new StringBuilder(requireContext().getString(R.string.share_msg_nothing_at) + " " + title + (withCourses ? " (" + classes + ")\n" : "\n"));
                return message.toString();
            } else
                message = new StringBuilder(requireContext().getString(R.string.share_msg_substitution_at) + " " + title + (withCourses ? " (" + classes + "):\n" : ":\n"));
        }

        String freespace = "    ";
        if (SubstitutionPlanFeatures.getSenior()) {
            for (SubstitutionEntry line : content.getEntries()) {
                if (line.isNothing()) {
                    message.append(freespace).append(line.getHour()).append(". ").append(requireContext().getString(R.string.share_msg_nothing_hour_senior)).append(" ").append(line.getCourse()).append("\n");
                } else {
                    message.append(freespace).append(line.getHour()).append(". ").append(requireContext().getString(R.string.share_msg_hour_senior)).append(" ").append(line.getCourse()).append(" ").append(context.getString(R.string.share_msg_in_room)).append(" ").append(line.getRoom()).append(" ").append(context.getString(R.string.with_teacher)).append(" ").append(line.getTeacher()).append(", ").append(line.getMoreInformation()).append("\n");
                }
            }
        } else {
            for (SubstitutionEntry line : content.getEntries()) {
                if (line.isNothing()) {
                    message.append(freespace).append(line.getHour()).append(". ").append(requireContext().getString(R.string.share_msg_nothing_hour)).append("\n");
                } else {
                    message.append(freespace).append(line.getHour()).append(". ").append(requireContext().getString(R.string.share_msg_hour)).append(" ").append(line.getSubject()).append(" ").append(context.getString(R.string.share_msg_in_room)).append(" ").append(line.getRoom()).append(" ").append(context.getString(R.string.with_teacher)).append(" ").append(line.getTeacher()).append(", ").append(line.getMoreInformation()).append("\n");
                }
            }
        }


        return message.toString();
    }


    //TeacherSearch
    private void teacherClick(@NonNull TextView view, @NonNull String teacherQuery, boolean showBorders, boolean fullNames) {
        if (TeacherlistFeatures.isAOL(teacherQuery))
            return;
        int padding = 0;
        if (showBorders) {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.background_shape);
            try {
                Drawable wrappedDrawable = DrawableCompat.wrap(Objects.requireNonNull(drawable));
                DrawableCompat.setTint(wrappedDrawable, ApplicationFeatures.getTextColorPrimary(requireContext()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            view.setBackground(drawable);
            padding = 7;
        } else {
            TypedValue outValue = new TypedValue();
            requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            view.setBackgroundResource(outValue.resourceId);
        }
        view.setPadding(padding, padding, padding, padding);


        if (fullNames) {
            new Thread(() -> {
                ApplicationFeatures.downloadTeacherlistDoc();
                try {
                    requireActivity().runOnUiThread(() -> {
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
                    ApplicationFeatures.downloadTeacherlistDoc();
                    try {
                        requireActivity().runOnUiThread(() -> teacherSearch(teacherQuery));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                teacherSearch(teacherQuery);
            }

        });
    }

    private void removeTeacherClick(@NonNull View view) {
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
        view.setBackgroundResource(0);
        view.setClickable(false);
        view.setOnClickListener(null);
    }

    private void teacherSearch(@NonNull String query) {
        new Thread(() -> {
            try {
                ApplicationFeatures.downloadTeacherlistDoc();
                if (TeacherlistFeatures.liste().getNoInternet())
                    throw new Exception();
                requireActivity().runOnUiThread(() -> ((ViewGroup) root.findViewById(R.id.substitution_frame)).addView(((ActivityFeatures) requireActivity()).createTeacherView(Objects.requireNonNull(TeacherlistFeatures.getTeacher(query)))));
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    if (!TeacherlistFeatures.isDownloaded()) {
                        ChocoBar.builder().setActivity(requireActivity())
                                .setText(getString(R.string.noInternet))
                                .setDuration(ChocoBar.LENGTH_LONG)
                                .orange()
                                .show();
                    } else {
                        ChocoBar.builder().setActivity(requireActivity())
                                .setText(getString(R.string.teacher_no_teacher_found))
                                .setDuration(ChocoBar.LENGTH_LONG)
                                .red()
                                .show();
                    }
                });
            }
        }).start();
    }

    @Nullable
    public static String getMatchingTeacher(@NonNull String query) {
        String teacher = null;
        try {
            ApplicationFeatures.downloadTeacherlistDoc();
            TeacherListEntry response = TeacherlistFeatures.getTeacher(query);
            teacher = Objects.requireNonNull(response).getName();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return teacher;
    }


    //Generating Table
    private boolean senior;
    @Nullable
    private
    SubstitutionList content;
    private String title;
    @Nullable
    private
    SubstitutionTitle titleObject;
    private int titleCode;
    private boolean miscellaneous;

    private void generateTable() {
        senior = SubstitutionPlanFeatures.getSenior();
        LinearLayout base = new LinearLayout(context);
        base.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        base.setOrientation(LinearLayout.VERTICAL);
        boolean old = PreferenceUtil.isOldDesign();
        boolean summarize = PreferenceUtil.isSummarizeUp();
        if (old)
            summarize = PreferenceUtil.isSummarizeUp() && PreferenceUtil.isSummarizeOld();
        boolean oldTitle = PreferenceUtil.isOldTitle();

        if (atOneGlance) {
            int titleCodeToday = SubstitutionPlanFeatures.getTodayTitleCode();
            int titleCodeTomorrow = SubstitutionPlanFeatures.getTomorrowTitleCode();
            //Hide days in the past and today after 18 o'clock
            boolean showToday = !PreferenceUtil.isIntelligentHide() || !SubstitutionPlanFeatures.getTodayTitle().isTitleCodeInPast();
            boolean showTomorrow = !PreferenceUtil.isIntelligentHide() || !SubstitutionPlanFeatures.getTomorrowTitle().isTitleCodeInPast();

            if (!showToday && !showTomorrow) {
                if (titleCodeToday == SubstitutionPlan.todayCode)
                    showToday = true;
                else
                    showTomorrow = true;
            }

            if (showToday) {
                titleCode = titleCodeToday;
                content = summarize ? SubstitutionPlanFeatures.getTodaySummarized() : SubstitutionPlanFeatures.getToday();
                title = SubstitutionPlanFeatures.getTodayTitleString();
                titleObject = SubstitutionPlanFeatures.getTodayTitle();
                miscellaneous = isMiscellaneous(content);
                generateTop(base, oldTitle);
                generateTableSpecific(base, old, false);
            }
            if ((!showToday || !content.getNoInternet()) && showTomorrow) {
                titleCode = titleCodeTomorrow;
                content = summarize ? SubstitutionPlanFeatures.getTomorrowSummarized() : SubstitutionPlanFeatures.getTomorrow();
                title = SubstitutionPlanFeatures.getTomorrowTitleString();
                titleObject = SubstitutionPlanFeatures.getTomorrowTitle();
                miscellaneous = isMiscellaneous(content);
                generateTop(base, oldTitle);
                generateTableSpecific(base, old, false);
            }
        } else if (all) {
            if (today) {
                content = summarize ? SubstitutionPlanFeatures.getTodayAllSummarized() : SubstitutionPlanFeatures.getTodayAll();
                title = SubstitutionPlanFeatures.getTodayTitleString();
                titleObject = SubstitutionPlanFeatures.getTodayTitle();
                titleCode = SubstitutionPlanFeatures.getTodayTitleCode();
            } else {
                content = summarize ? SubstitutionPlanFeatures.getTomorrowAllSummarized() : SubstitutionPlanFeatures.getTomorrowAll();
                title = SubstitutionPlanFeatures.getTomorrowTitleString();
                titleObject = SubstitutionPlanFeatures.getTomorrowTitle();
                titleCode = SubstitutionPlanFeatures.getTomorrowTitleCode();
            }
            String missing_short = getString(R.string.missing_short);
            for (String s : External_Const.nothing) {
                content = Objects.requireNonNull(content).replaceAll(s, missing_short);
            }
            miscellaneous = isMiscellaneous(Objects.requireNonNull(content));
            generateTop(base, true);
            generateTableAll(base);
        } else {
            if (today) {
                content = summarize ? SubstitutionPlanFeatures.getTodaySummarized() : SubstitutionPlanFeatures.getToday();
                title = SubstitutionPlanFeatures.getTodayTitleString();
                titleObject = SubstitutionPlanFeatures.getTodayTitle();
                titleCode = SubstitutionPlanFeatures.getTodayTitleCode();
            } else {
                content = summarize ? SubstitutionPlanFeatures.getTomorrowSummarized() : SubstitutionPlanFeatures.getTomorrow();
                title = SubstitutionPlanFeatures.getTomorrowTitleString();
                titleObject = SubstitutionPlanFeatures.getTomorrowTitle();
                titleCode = SubstitutionPlanFeatures.getTomorrowTitleCode();
            }
            miscellaneous = isMiscellaneous(content);
            generateTop(base, oldTitle);
            generateTableSpecific(base, old, PreferenceUtil.isSwipeToRefresh() && PreferenceUtil.isSwipeToRefreshFiltered());
        }
        requireActivity().runOnUiThread(() -> {
            clear();
            ((LinearLayout) root.findViewById(R.id.substitution_linear_layout_layer1)).addView(base);
        });
    }

    private void clear() {
        ActivityFeatures.removeLoadingPanel((ViewGroup) root);
//        ((ViewGroup) root.findViewById(R.id.substitution_frame)).removeView(root.findViewWithTag("vertretung_loading"));
        LinearLayout base = root.findViewById(R.id.substitution_linear_layout_layer1);
        base.removeAllViews();
    }

    //Top (Date or noInternet, etc.)
    private void generateTop(@NonNull ViewGroup base, boolean old) {

        if (old) {
            TextView titleView = createTitleLayout();
            base.addView(titleView);
            if (Objects.requireNonNull(content).getNoInternet()) {
                titleView.setText(requireContext().getString(R.string.noInternetConnection));
                return;
            } else
                titleView.setText(title);

            if (content.size() == 0) {
                TextView tv = new TextView(context);
                tv.setTextColor(ApplicationFeatures.getTextColorSecondary(requireContext()));
                tv.setText(requireContext().getString(R.string.nothing));
                tv.setTextSize(20);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setGravity(Gravity.CENTER);
                base.addView(tv);
            }

        } else {
            if (Objects.requireNonNull(content).getNoInternet()) {
                ViewGroup titleView = createTitleLayoutNewDesign(requireContext().getString(R.string.noInternetConnection), "", Color.GRAY, Color.WHITE);
                base.addView(titleView);
            } else {
                int bgColor;
                int textColor;
                if (titleCode == SubstitutionPlan.todayCode) {
                    bgColor = ContextCompat.getColor(requireContext(), R.color.today);
                    textColor = ContextCompat.getColor(requireContext(), R.color.today_text);
                } else if (titleCode == SubstitutionPlan.tomorrowCode) {
                    bgColor = ContextCompat.getColor(requireContext(), R.color.tomorrow);
                    textColor = ContextCompat.getColor(requireContext(), R.color.tomorrow_text);
                } else if (titleCode == SubstitutionPlan.futureCode) {
                    bgColor = ContextCompat.getColor(requireContext(), R.color.future);
                    textColor = ContextCompat.getColor(requireContext(), R.color.future_text);
                } else {
                    bgColor = ContextCompat.getColor(requireContext(), R.color.past);
                    textColor = ContextCompat.getColor(requireContext(), R.color.past_text);
                }
                ViewGroup titleView = createTitleLayoutNewDesign(Objects.requireNonNull(titleObject).getDayOfWeek(), titleObject.getDate() + ", " + titleObject.getWeek(), bgColor, textColor);
                base.addView(titleView);

                if (content.size() == 0) {
                    bgColor = ContextCompat.getColor(getContext(), R.color.nothing);
                    textColor = ContextCompat.getColor(getContext(), R.color.nothing_text);
                    titleView = createTitleLayoutNewDesign("", requireContext().getString(R.string.nothing), bgColor, textColor);
                    base.addView(titleView);
                }

            }
        }
    }

    //Title Layouts
    @NonNull
    private TextView createTitleLayout() {
        TextView textView = new TextView(context);
        textView.setTextColor(ApplicationFeatures.getTextColorPrimary(requireContext()));
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return textView;
    }

    @NonNull
    private ViewGroup createTitleLayoutNewDesign(@NonNull String day, @NonNull String description, @ColorInt int backgroundColor, @ColorInt int textColor) {
        View v = getLayoutInflater().inflate(R.layout.substitution_title_new, null);
        v.findViewById(R.id.substitution_new_background).setBackgroundColor(backgroundColor);

        SpannableString dayUnderlined = new SpannableString(day);
        dayUnderlined.setSpan(new UnderlineSpan(), 0, dayUnderlined.length(), 0);

        ((TextView) v.findViewById(R.id.substitution_new_title)).setText(dayUnderlined);
        ((TextView) v.findViewById(R.id.substitution_new_title)).setTextColor(textColor);
        ((TextView) v.findViewById(R.id.substitution_new_title_desc)).setText(description);
        ((TextView) v.findViewById(R.id.substitution_new_title_desc)).setTextColor(textColor);
        if (description.trim().isEmpty()) {
            v.findViewById(R.id.substitution_new_title_desc).setVisibility(View.GONE);
        }
        if (day.trim().isEmpty()) {
            v.findViewById(R.id.substitution_new_title).setVisibility(View.GONE);
        }
        return (ViewGroup) v;
    }

    //Other functions important for displaying headline
    public static boolean isMiscellaneous(@NonNull SubstitutionList content) {
        if (content.getNoInternet())
            return false;
        for (int i = 0; i < content.getEntries().size(); i++) {
            if (!content.getEntries().get(i).getMoreInformation().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static String[] generateHeadline(@NonNull Context context, boolean miscellaneous, boolean senior, boolean all) {
        String[] headline;

        if (all) {
            headline = new String[]{context.getString(R.string.classes), miscellaneous ? context.getString(R.string.hours_short) : context.getString(R.string.hours), context.getString(R.string.subject), miscellaneous ? context.getString(R.string.teacher_short) : context.getString(R.string.teacher), miscellaneous ? context.getString(R.string.room_short) : context.getString(R.string.room), miscellaneous ? context.getString(R.string.miscellaneous_short) : ""};
        } else if (senior) {
            headline = new String[]{miscellaneous ? context.getString(R.string.hours_short_three) : context.getString(R.string.hours), context.getString(R.string.courses), miscellaneous ? context.getString(R.string.teacher_short) : context.getString(R.string.teacher), context.getString(R.string.room), miscellaneous ? context.getString(R.string.miscellaneous_short) : "", context.getString(R.string.subject)};
        } else {
            headline = new String[]{miscellaneous ? context.getString(R.string.hours_short_three) : context.getString(R.string.hours), context.getString(R.string.subject), miscellaneous ? context.getString(R.string.teacher_short) : context.getString(R.string.teacher), context.getString(R.string.room), miscellaneous ? context.getString(R.string.miscellaneous_short) : "", context.getString(R.string.classes)};
        }

        return headline;
    }


    //Body
    private void generateTableAll(@NonNull ViewGroup base) {

        if (!Objects.requireNonNull(content).getNoInternet() && content.size() > 0) {
            //Overview
            base.addView(generateOverviewAll());

            //Content
            substitutionListView = new ListView(context);
            substitutionListView.setAdapter(new SubstitutionListAdapterAll(requireContext(), 0, content, miscellaneous));
            substitutionListView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            //Swipe to Refresh
            if (PreferenceUtil.isSwipeToRefresh()) {
                SwipeRefreshLayout swipeRefreshLayout = new SwipeRefreshLayout(requireContext());
                swipeRefreshLayout.setOnRefreshListener(() -> ((MainActivity) requireActivity()).onNavigationItemSelected(R.id.action_refresh));
                swipeRefreshLayout.addView(substitutionListView);
                swipeRefreshLayout.setColorSchemeColors(Color.BLUE, Color.YELLOW, Color.BLUE);
                swipeRefreshLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                base.addView(swipeRefreshLayout);
            } else
                base.addView(substitutionListView);
        }
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    @NonNull
    private View generateOverviewAll() {
        String[] headline = generateHeadline(requireContext(), miscellaneous, senior, true);
        LinearLayout base = new LinearLayout(context);
        base.setOrientation(LinearLayout.HORIZONTAL);
        base.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        int length = headline.length;
        if (!miscellaneous)
            length--;
        for (int i = 0; i < length; i++) {
            LinearLayout.LayoutParams params;
            TextView hour = createBlankTextView();

            switch (i) {
                case 0:
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, requireContext().getResources().getInteger(R.integer.substitution_all_course));
                    hour.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    break;
                case 1:
                    if (PreferenceUtil.isHour()) {
                        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_all_hour_long));
                    } else if (PreferenceUtil.isSummarizeUp()) {
                        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_all_hour_summary));
                    } else {
                        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_all_hour));
                    }
                    break;
                case 2:
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_all_subject));
                    break;
                case 3:
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_all_teacher));
                    break;
                case 4:
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_all_room));
                    break;
                case 5:
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_all_other));
                    break;
                default:
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_all_room));
            }

            params.setMargins(3, 3, 3, 3);
            hour.setLayoutParams(params);
            hour.setText(headline[i]);
            base.addView(hour);
        }

        return base;
    }

    private void generateTableSpecific(@NonNull ViewGroup base, boolean old, boolean swipeRefresh) {
        if (!Objects.requireNonNull(content).getNoInternet() && content.size() > 0) {
            //Content
            substitutionListView = new ListView(context);
            substitutionListView.setAdapter(new SubstitutionListAdapterSpecific(requireContext(), 0, content, miscellaneous, old));
            substitutionListView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            if (!old) {
                substitutionListView.setDivider(null);
            } else {
                //Overview
                base.addView(generateOverviewSpecific());
            }

            //Swipe to Refresh
            if (swipeRefresh) {
                SwipeRefreshLayout swipeRefreshLayout = new SwipeRefreshLayout(requireContext());
                swipeRefreshLayout.setOnRefreshListener(() -> ((MainActivity) requireActivity()).onNavigationItemSelected(R.id.action_refresh));
                swipeRefreshLayout.addView(substitutionListView);
                swipeRefreshLayout.setColorSchemeColors(Color.BLUE, Color.YELLOW, Color.BLUE);
                swipeRefreshLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                base.addView(swipeRefreshLayout);
            } else
                base.addView(substitutionListView);

        }
    }

    @NonNull
    private View generateOverviewSpecific() {
        String[] headline = generateHeadline(requireContext(), miscellaneous, senior, false);

        LinearLayout base = new LinearLayout(context);
        base.setOrientation(LinearLayout.HORIZONTAL);
        base.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams params;
        if (PreferenceUtil.isSummarizeOld())
            params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, requireContext().getResources().getInteger(R.integer.substitution_specific_entry_hour_summary));
        else
            params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, requireContext().getResources().getInteger(R.integer.substitution_specific_entry_hour));
        params.setMargins(3, 3, 3, 3);
        TextView hour = createBlankTextView();
        hour.setLayoutParams(params);
        hour.setText(headline[0]);
        base.addView(hour);

        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_specific_entry_subject));
        params.setMargins(3, 3, 3, 3);
        TextView subject = createBlankTextView();
        subject.setLayoutParams(params);
        subject.setText(headline[1]);
        base.addView(subject);

        if (PreferenceUtil.isHour()) {
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_specific_entry_hour_long)));
            subject.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_specific_entry_subject_long)));
        } else {
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_specific_entry_hour)));
            subject.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_specific_entry_subject)));
        }
        if (PreferenceUtil.isSummarizeOld()) {
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_specific_entry_hour_summary)));
        }

        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_specific_entry_teacher));
        TextView teacher = createBlankTextView();
        teacher.setLayoutParams(params);
        teacher.setText(headline[2]);
        base.addView(teacher);

        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_specific_entry_room));
        params.setMargins(3, 3, 3, 3);
        TextView room = createBlankTextView();
        room.setLayoutParams(params);
        room.setText(headline[3]);
        base.addView(room);

        if (miscellaneous) {
            params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_specific_entry_other));
            params.setMargins(3, 3, 3, 3);
            TextView other = createBlankTextView();
            other.setLayoutParams(params);
            other.setText(headline[4]);
            base.addView(other);
        }

        params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, context.getResources().getInteger(R.integer.substitution_specific_entry_course));
        params.setMargins(3, 3, 3, 3);
        TextView course = createBlankTextView();
        course.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
        course.setTypeface(course.getTypeface(), Typeface.NORMAL);
        course.setLayoutParams(params);
        course.setText(headline[5]);
        base.addView(course);

        return base;
    }

    @NonNull
    private TextView createBlankTextView() {
        TextView hour = new TextView(context);
        hour.setTypeface(hour.getTypeface(), Typeface.BOLD);
        hour.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        hour.setTextColor(ApplicationFeatures.getTextColorSecondary(requireContext()));
        hour.setGravity(Gravity.CENTER);
        return hour;
    }

    @Nullable
    private ListView substitutionListView;

    //All ListView
    private class SubstitutionListAdapterAll extends ArrayAdapter<String[]> {
        final SubstitutionList content;
        final boolean sons;

        SubstitutionListAdapterAll(@NonNull Context con, int resource, SubstitutionList content, boolean sons) {
            super(con, resource);
            this.content = content;
            this.sons = sons;
        }

        @NotNull
        @Override
        public View getView(int position, @Nullable View convertView, @NotNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_substitution_all_entry, null);
            }
            return getEntryAll(convertView, content.getEntries().get(position), sons);
        }

        @Override
        public int getCount() {
            return content.size();
        }
    }

    @NonNull
    private View getEntryAll(@NonNull View view, @NonNull SubstitutionEntry entry,
                             boolean miscellaneous) {
        TextView course = view.findViewById(R.id.substitution_all_entry_textViewCourse);
        course.setText(entry.getCourse());
        course.setOnClickListener((View v) -> showAddPopup(course, entry.getCourse()));

        TextView hour = view.findViewById(R.id.substitution_all_entry_textViewHour);
        hour.setText(entry.getHour());

        if (PreferenceUtil.isHour()) {
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, requireContext().getResources().getInteger(R.integer.substitution_all_hour_long)));
        } else if (PreferenceUtil.isSummarizeUp()) {
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, requireContext().getResources().getInteger(R.integer.substitution_all_hour_summary)));
        } else {
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, requireContext().getResources().getInteger(R.integer.substitution_all_hour)));
        }

        TextView subject = view.findViewById(R.id.substitution_all_entry_textViewSubject);
        subject.setText(entry.getSubject());

        TextView teacher = view.findViewById(R.id.substitution_all_entry_textViewTeacher);
        teacher.setText(entry.getTeacher());

        removeTeacherClick(teacher);
        if (!entry.isNothing())
            teacherClick(teacher, entry.getTeacher(), !ApplicationFeatures.getBooleanSettings("show_border_specific", true) && ApplicationFeatures.getBooleanSettings("show_borders", false), !PreferenceUtil.isFullTeacherNamesSpecific() && PreferenceUtil.isFullTeacherNames());
        else
            teacher.setText(entry.getTeacher());

        TextView room = view.findViewById(R.id.substitution_all_entry_textViewRoom);
        room.setText(entry.getRoom());
        room.setOnClickListener((View v) -> {
            Intent intent = new Intent(getContext(), RoomPlanActivity.class);
            intent.putExtra(RoomPlanActivity.SELECT_ROOM, entry.getRoom());
            startActivity(intent);
        });
        TypedValue outValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        room.setBackgroundResource(outValue.resourceId);

        TextView other = view.findViewById(R.id.substitution_all_entry_textViewOther);
        other.setVisibility(View.VISIBLE);
        if (miscellaneous) {
            other.setText(entry.getMoreInformation());
        } else {
            other.setVisibility(View.GONE);
        }

        return view;
    }

    //Pop up menu for adding course to profile
    private void showAddPopup(@NonNull View v, @NonNull String course) {
        ContextThemeWrapper theme = new ContextThemeWrapper(getActivity(), PreferenceUtil.isDark() ? R.style.Widget_AppCompat_PopupMenu : R.style.Widget_AppCompat_Light_PopupMenu);
        PopupMenu popup = new PopupMenu(theme, v);
        popup.setOnMenuItemClickListener((MenuItem item) -> {
            switch (item.getItemId()) {
                case R.id.action_add_course:
                    addToProfile(course);
                    break;
            }
            return true;
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.popup_substitution_fragment_add_course_menu, popup.getMenu());
        popup.show();
    }

    private void addToProfile(@NonNull String course) {
        if (ApplicationFeatures.addCourseToSelectedProfile(course.trim())) {
            ProfileManagement.save(true);
            ((MainActivity) requireActivity()).onNavigationItemSelected(R.id.action_refresh, "");
        }
    }


    //Specific ListView
    private class SubstitutionListAdapterSpecific extends ArrayAdapter<String[]> {
        final SubstitutionList content;
        final boolean sons;
        final boolean old;

        SubstitutionListAdapterSpecific(@NonNull Context con, int resource, SubstitutionList content, boolean sons, boolean old) {
            super(con, resource);
            this.content = content;
            this.sons = sons;
            this.old = old;
        }

        @NotNull
        @Override
        public View getView(int position, @Nullable View convertView, @NotNull ViewGroup parent) {
            if (old) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_substitution_specific_entry, null);
                }
                return getEntrySpecific(convertView, content.getEntries().get(position), senior, sons);
            } else {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_substitution_specific_card, null);
                }
                return getEntrySpecificNewDesign(convertView, content.getEntries().get(position), !content.getEntries().get(position).getMoreInformation().trim().isEmpty());
            }
        }

        @Override
        public int getCount() {
            return content.size();
        }

    }

    @NonNull
    private View getEntrySpecific(@NonNull View view, @NonNull SubstitutionEntry entry,
                                  boolean senior, boolean miscellaneous) {
        TextView hour = view.findViewById(R.id.substitution_specific_entry_textViewHour);
        hour.setText(entry.getHour());
        hour.setBackgroundColor(ApplicationFeatures.getAccentColor(requireContext()));

        TextView subject = view.findViewById(R.id.substitution_specific_entry_textViewSubject);
        if (senior) {
            subject.setText(entry.getCourse());
            subject.setOnClickListener((View v) -> showRemovePopup(subject, entry.getCourse()));
        } else {
            subject.setText(entry.getSubject());
        }

        if (PreferenceUtil.isHour()) {
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, requireContext().getResources().getInteger(R.integer.substitution_specific_entry_hour_long)));
            subject.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_specific_entry_subject_long)));
            hour.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        } else {
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, requireContext().getResources().getInteger(R.integer.substitution_specific_entry_hour)));
            subject.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_specific_entry_subject)));
            hour.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
        }
        if (PreferenceUtil.isSummarizeOld()) {
            hour.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_specific_entry_hour_summary)));
        }

        TextView teacher = view.findViewById(R.id.substitution_specific_entry_textViewTeacher);

        TextView room = view.findViewById(R.id.substitution_specific_entry_textViewRoom);
        room.setTextColor(ApplicationFeatures.getAccentColor(context));
        room.setOnClickListener(null);


        if (!entry.isNothing()) {
            teacher.setGravity(Gravity.CENTER);
            teacher.setTextColor(subject.getTextColors());
            teacher.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_specific_entry_teacher)));
            teacher.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            teacherClick(teacher, entry.getTeacher(), ApplicationFeatures.getBooleanSettings("show_borders", true), PreferenceUtil.isFullTeacherNames());

            room.setVisibility(View.VISIBLE);
            room.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_specific_entry_room)));

            SpannableString content = new SpannableString(entry.getRoom());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            room.setText(content);
            room.setOnClickListener((View v) -> {
                Intent intent = new Intent(getContext(), RoomPlanActivity.class);
                intent.putExtra(RoomPlanActivity.SELECT_ROOM, entry.getRoom());
                startActivity(intent);
            });
            TypedValue outValue = new TypedValue();
            requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            room.setBackgroundResource(outValue.resourceId);
        } else {
            removeTeacherClick(teacher);
            teacher.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, context.getResources().getInteger(R.integer.substitution_specific_entry_teacher) + context.getResources().getInteger(R.integer.substitution_specific_entry_room)));

            SpannableString content = new SpannableString(entry.getTeacher());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            teacher.setText(content);
            teacher.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
            teacher.setTextColor(ApplicationFeatures.getAccentColor(context));

            room.setVisibility(View.GONE);
        }

        TextView other = view.findViewById(R.id.substitution_specific_entry_textViewOther);
        other.setVisibility(miscellaneous ? View.VISIBLE : View.GONE);
        other.setText(entry.getMoreInformation());

        TextView course = view.findViewById(R.id.substitution_specific_entry_textViewClass);
        if (senior) {
            course.setText(entry.getSubject());
        } else {
            course.setText(entry.getCourse());
            //Only useful for senior with more than one course
            /*subject.setOnClickListener((View v) -> {
                showRemovePopup(subject, entry[0]);
            });*/
        }

        return view;
    }

    @NonNull
    private View getEntrySpecificNewDesign(@NonNull View view, @NonNull SubstitutionEntry entry,
                                           boolean miscellaneous) {
        TextView course = view.findViewById(R.id.substitution_card_entry_textViewClass);
        course.setText(entry.getCourse());
        course.setOnClickListener((View v) -> showRemovePopup(course, entry.getCourse()));

        TextView hour = view.findViewById(R.id.substitution_card_entry_textViewHour);
        hour.setText(entry.getHour());

        if (PreferenceUtil.isHour()) {
            hour.setTextSize(TypedValue.COMPLEX_UNIT_SP, 42);
        } else {
            hour.setTextSize(TypedValue.COMPLEX_UNIT_SP, 52);
        }

        TextView subject = view.findViewById(R.id.substitution_card_entry_textViewSubject);
        removeTeacherClick(subject);

        TextView teacher = view.findViewById(R.id.substitution_card_entry_textViewTeacher);

        TextView room = view.findViewById(R.id.substitution_card_entry_textViewRoom);
        room.setVisibility(View.VISIBLE);

        TextView other = view.findViewById(R.id.substitution_card_entry_textViewOther);

        CardView card = view.findViewById(R.id.list_substituion_widget_card);

        if (!entry.isNothing()) {
            card.setBackgroundColor(card.getCardBackgroundColor().getDefaultColor());
            if (!entry.getSubject().trim().isEmpty()) {
                teacher.setVisibility(View.VISIBLE);
                teacher.setGravity(Gravity.CENTER);
                teacher.setTextColor(subject.getTextColors());
                teacherClick(teacher, entry.getTeacher(), ApplicationFeatures.getBooleanSettings("show_borders", false), PreferenceUtil.isFullTeacherNames());

                subject.setText(entry.getSubject() + " " + requireContext().getString(R.string.with_teacher) + " ");
            } else {
                teacherClick(subject, entry.getTeacher(), ApplicationFeatures.getBooleanSettings("show_borders", false), PreferenceUtil.isFullTeacherNames());
                subject.setText(entry.getTeacher());
            }

            if (!entry.getRoom().trim().isEmpty()) {
                SpannableString content = new SpannableString("in " + entry.getRoom());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                room.setText(content);
                room.setOnClickListener((View v) -> {
                    Intent intent = new Intent(getContext(), RoomPlanActivity.class);
                    intent.putExtra(RoomPlanActivity.SELECT_ROOM, entry.getRoom());
                    startActivity(intent);
                });
                TypedValue outValue = new TypedValue();
                requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                room.setBackgroundResource(outValue.resourceId);
            } else {
                room.setVisibility(View.GONE);
                room.setOnClickListener(null);
            }
        } else {
            if (PreferenceUtil.getGeneralTheme() == R.style.AppTheme_Light) {
                card.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.nothing_background_light));
            } else
                card.setBackgroundColor(Color.RED);
            removeTeacherClick(teacher);
            teacher.setVisibility(View.GONE);

            subject.setText(entry.getSubject());

            SpannableString content = new SpannableString(entry.getTeacher());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            room.setText(content);
        }


        other.setVisibility(View.VISIBLE);
        if (miscellaneous) {
            other.setText(entry.getMoreInformation());
        } else {
            other.setVisibility(View.GONE);
        }

        return view;
    }

    //Pop up menu for adding course to profile
    private void showRemovePopup(@NonNull View v, @NonNull String course) {
        if (ApplicationFeatures.getSelectedProfile().getCoursesArray().length <= 1)
            return;
        ContextThemeWrapper theme = new ContextThemeWrapper(getActivity(), PreferenceUtil.isDark() ? R.style.Widget_AppCompat_PopupMenu : R.style.Widget_AppCompat_Light_PopupMenu);
        PopupMenu popup = new PopupMenu(theme, v);
        popup.setOnMenuItemClickListener((MenuItem item) -> {
            switch (item.getItemId()) {
                case R.id.action_remove_course:
                    removeFromProfile(course);
                    break;
            }
            return true;
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.popup_substitution_fragment_remove_course_menu, popup.getMenu());
        popup.show();
    }

    private void removeFromProfile(@NonNull String course) {
        if (ApplicationFeatures.removeFromSelectedProfile(course.trim())) {
            ProfileManagement.save(true);
            ((MainActivity) requireActivity()).onNavigationItemSelected(R.id.action_refresh, "");
        }
    }

}
