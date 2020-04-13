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

package com.ulan.timetable.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.substitutionplan.SubstitutionEntry;
import com.asdoi.gymwen.substitutionplan.SubstitutionList;
import com.ulan.timetable.model.Week;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class WeekUtils {
    @NonNull
    public static ArrayList<Week> compareSubstitutionAndWeeks(Context context, @NonNull ArrayList<Week> weeks, SubstitutionList entries, boolean senior) {
        boolean empty = weeks.isEmpty();
        if (!entries.getNoInternet()) {
            for (int i = 0; i < entries.getEntries().size(); i++) {
                SubstitutionEntry entry = entries.getEntries().get(i);
                int color = ContextCompat.getColor(context, entry.isNothing() ? R.color.notification_icon_background_omitted : R.color.notification_icon_background_substitution);
                String subject = senior ? entry.getCourse() : entry.getSubject();
                String teacher = entry.getTeacher();
                String room = entry.getRoom();
                String begin = entry.getMatchingBeginTime("-");
                if (begin.length() < 5)
                    begin = "0" + begin;

                String end = entry.getMatchingEndTime("-");
                if (end.length() < 5)
                    end = "0" + end;

                Week weekEntry = new Week(subject, teacher, room, begin, end, color, false);
                weekEntry.setMoreInfos(entry.getMoreInformation());
//                if (!entry.getMoreInformation().trim().isEmpty())
//                    subject = subject + " (" + entry.getMoreInformation() + ")";

                if (empty) {
                    weeks.add(weekEntry);
                } else {
                    for (int j = 0; j < weeks.size(); j++) {
                        Week week = weeks.get(j);
                        boolean beginIsBeforeFrom = begin.compareToIgnoreCase(week.getFromTime()) < 0;
                        boolean beginIsFrom = begin.equalsIgnoreCase(week.getFromTime());
                        boolean beginIsBeforeTo = begin.compareToIgnoreCase(week.getToTime()) < 0;

                        boolean endIsAfterFrom = end.compareToIgnoreCase(week.getFromTime()) > 0;
                        boolean endIsTo = end.equalsIgnoreCase(week.getToTime());
                        boolean endIsAfterTo = end.compareToIgnoreCase(week.getToTime()) > 0;

                        if (beginIsBeforeFrom) {
                            if (endIsAfterFrom) {
                                if (endIsAfterTo) {
                                    //Check next, remove
                                    checkNext(weeks, begin, end, weekEntry, j);
                                    weeks.remove(j);
                                } else {
                                    if (endIsTo) {
                                        //replace
                                        weeks.remove(j);
                                        weeks.add(j, weekEntry);
                                    } else {
                                        //split
                                        split(weeks, begin, end, weekEntry, j, week, beginIsFrom);
                                    }
                                }
                            } else {
                                //add before
                                weeks.add(j, weekEntry);
                            }
                        } else {
                            if (beginIsFrom) {
                                if (endIsAfterTo) {
                                    //Check next, remove
                                    checkNext(weeks, begin, end, weekEntry, j);
                                } else {
                                    if (endIsTo) {
                                        //replace
                                        weeks.remove(j);
                                        weeks.add(j, weekEntry);
                                    } else {
                                        //split
                                        split(weeks, begin, end, weekEntry, j, week, true);
                                    }
                                }
                            } else {
                                if (beginIsBeforeTo) {
                                    if (endIsAfterTo) {
                                        //check next, split
                                        checkNext(weeks, begin, end, weekEntry, j);

                                        week.setToTime(begin);
//                                        week.setEditable(false);
                                        weeks.set(j, week);
                                    } else {
                                        if (endIsTo) {
                                            //split
                                            split(weeks, begin, end, weekEntry, j, week, false);
                                        } else {
                                            //split3
                                            Week week2 = new Week(week.getSubject(), week.getTeacher(), week.getRoom(), week.getFromTime(), week.getToTime(), week.getColor(), false);
                                            week.setToTime(begin);
                                            week2.setFromTime(end);

                                            weeks.set(j, week);
                                            weeks.add(j + 1, weekEntry);
                                            weeks.add(j + 2, week2);
                                        }
                                    }
                                } else {
                                    //check next
                                    if (j >= weeks.size() - 1) {
                                        weeks.add(weekEntry);
                                        break;
                                    }
                                    continue;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        return sortWeekList(weeks);
    }

    private static void split(@NonNull ArrayList<Week> weeks, String begin, String end, Week weekEntry, int j, @NonNull Week week, boolean beginIsFrom) {
        if (beginIsFrom) {
            week.setFromTime(end);
//            week.setEditable(false);
            weeks.set(j, weekEntry);
            weeks.add(j + 1, week);
        } else {
            week.setToTime(begin);
//            week.setEditable(false);
            weeks.set(j, week);
            weeks.add(j + 1, weekEntry);
        }
    }

    private static void checkNext(@NonNull ArrayList<Week> weeks, String begin, @NonNull String end, Week weekEntry, int j) {
        for (int j2 = j; j2 < weeks.size(); j2++) {
            Week week = weeks.get(j2);
            boolean endIsAfterFrom = end.compareToIgnoreCase(week.getFromTime()) > 0;
            boolean endIsBeforeTo = end.compareToIgnoreCase(week.getFromTime()) < 0;

            if (j2 >= weeks.size() - 1) {
                weeks.add(weekEntry);
                break;
            }

            if (endIsAfterFrom) {
                if (endIsBeforeTo) {
                    split(weeks, begin, end, weekEntry, j, week, true);
                } else {
                    //check next, remove
                    weeks.remove(j2);
                    continue;
                }
            } else {
                //add before
                weeks.add(j2, weekEntry);
            }
            break;
        }
    }

    public static Week getNextWeek(ArrayList<Week> weeks) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 1);
        String hour = "" + calendar.get(Calendar.HOUR_OF_DAY);
        if (hour.length() < 2)
            hour = "0" + hour;
        String minutes = "" + calendar.get(Calendar.MINUTE);
        if (minutes.length() < 2)
            minutes = "0" + minutes;
        String now = hour + ":" + minutes;

        for (int i = 0; i < weeks.size(); i++) {
            Week week = weeks.get(i);
            if ((now.compareToIgnoreCase(week.getFromTime()) >= 0 && now.compareToIgnoreCase(week.getToTime()) <= 0) || now.compareToIgnoreCase(week.getToTime()) <= 0) {
                return week;
            }
        }
        return null;
    }

    @NonNull
    public static ArrayList<Week> sortWeekList(@NonNull ArrayList<Week> weeks) {
        Collections.sort(weeks, (o1, o2) -> o1.getFromTime().compareToIgnoreCase(o2.getFromTime()));
        return weeks;
    }
}
