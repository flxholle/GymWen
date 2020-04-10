package com.ulan.timetable.adapters;

import android.content.Intent;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;

import com.afollestad.appthemeengine.ATE;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.RoomPlanActivity;
import com.asdoi.gymwen.ui.activities.TeacherListActivity;
import com.asdoi.gymwen.util.External_Const;
import com.asdoi.gymwen.util.PreferenceUtil;
import com.ulan.timetable.model.Exam;
import com.ulan.timetable.utils.AlertDialogsHelper;
import com.ulan.timetable.utils.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by Ulan on 17.12.2018.
 */
public class ExamsAdapter extends ArrayAdapter<Exam> {

    private final AppCompatActivity mActivity;
    private final int mResource;
    private final ArrayList<Exam> examlist;
    private Exam exam;
    private final ListView mListView;

    private static class ViewHolder {
        TextView subject;
        TextView teacher;
        TextView room;
        TextView date;
        TextView time;
        CardView cardView;
        ImageView popup;
    }

    public ExamsAdapter(@NonNull AppCompatActivity activity, ListView listView, int resource, @NonNull ArrayList<Exam> objects) {
        super(activity, resource, objects);
        mActivity = activity;
        mListView = listView;
        mResource = resource;
        examlist = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String subject = Objects.requireNonNull(getItem(position)).getSubject();
        String teacher = Objects.requireNonNull(getItem(position)).getTeacher();
        String room = Objects.requireNonNull(getItem(position)).getRoom();
        String date = Objects.requireNonNull(getItem(position)).getDate();
        String time = Objects.requireNonNull(getItem(position)).getTime();
        int color = Objects.requireNonNull(getItem(position)).getColor();

        exam = new Exam(subject, teacher, date, time, room, color);
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.subject = convertView.findViewById(R.id.subjectexams);
            holder.teacher = convertView.findViewById(R.id.teacherexams);
            holder.room = convertView.findViewById(R.id.roomexams);
            holder.date = convertView.findViewById(R.id.dateexams);
            holder.time = convertView.findViewById(R.id.timeexams);
            holder.cardView = convertView.findViewById(R.id.exams_cardview);
            holder.popup = convertView.findViewById(R.id.popupbtn);
            convertView.setTag(holder);
            // Only apply the first time the view is created
            ATE.apply(convertView.getContext(), convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.subject.setText(exam.getSubject());

        TeacherListActivity.removeTeacherClick(holder.teacher, getContext());
        holder.teacher.setText(exam.getTeacher());
        if (!Arrays.asList(External_Const.nothing).contains(exam.getTeacher()))
            TeacherListActivity.teacherClick(holder.teacher, exam.getTeacher(), PreferenceUtil.isFullTeacherNames(), mActivity);

        holder.room.setText(exam.getRoom());
        holder.room.setOnClickListener((View v) -> {
            Intent intent = new Intent(getContext(), RoomPlanActivity.class);
            intent.putExtra(RoomPlanActivity.SELECT_ROOM, holder.room.getText());
            mActivity.startActivity(intent);
        });
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        holder.room.setBackgroundResource(outValue.resourceId);

        holder.date.setText(exam.getDate());
        holder.time.setText(exam.getTime());
        holder.cardView.setCardBackgroundColor(exam.getColor());
        holder.popup.setOnClickListener(v -> {
            ContextThemeWrapper theme = new ContextThemeWrapper(mActivity, PreferenceUtil.isDark() ? R.style.Widget_AppCompat_PopupMenu : R.style.Widget_AppCompat_Light_PopupMenu);
            final PopupMenu popup = new PopupMenu(theme, holder.popup);
            final DbHelper db = new DbHelper(mActivity);
            popup.getMenuInflater().inflate(R.menu.timetable_popup_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.delete_popup) {
                        AlertDialogsHelper.getDeleteDialog(getContext(), () -> {
                            db.deleteExamById(getItem(position));
                            db.updateExam(getItem(position));
                            examlist.remove(position);
                            notifyDataSetChanged();
                        });
                        return true;
                    } else if (itemId == R.id.edit_popup) {
                        final View alertLayout = mActivity.getLayoutInflater().inflate(R.layout.timetable_dialog_add_exam, null);
                        AlertDialogsHelper.getEditExamDialog(mActivity, alertLayout, examlist, mListView, position);
                        notifyDataSetChanged();
                        return true;
                    }
                    return onMenuItemClick(item);
                }
            });
            popup.show();
        });

        hidePopUpMenu(holder);

        convertView.findViewById(R.id.line).setBackgroundColor(ApplicationFeatures.getTextColorPrimary(getContext()));

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public ArrayList<Exam> getExamList() {
        return examlist;
    }

    public Exam getExam() {
        return exam;
    }

    private void hidePopUpMenu(@NonNull ViewHolder holder) {
        SparseBooleanArray checkedItems = mListView.getCheckedItemPositions();
        if (checkedItems.size() > 0) {
            for (int i = 0; i < checkedItems.size(); i++) {
                int key = checkedItems.keyAt(i);
                if (checkedItems.get(key)) {
                    holder.popup.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            holder.popup.setVisibility(View.VISIBLE);
        }
    }
}
