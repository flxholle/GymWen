package com.ulan.timetable.adapters;

import android.util.SparseBooleanArray;
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

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.util.PreferenceUtil;
import com.ulan.timetable.model.Note;
import com.ulan.timetable.utils.AlertDialogsHelper;
import com.ulan.timetable.utils.DbHelper;

import java.util.ArrayList;
import java.util.Objects;


/**
 * Created by Ulan on 28.09.2018.
 */
public class NotesAdapter extends ArrayAdapter<Note> {

    private final AppCompatActivity mActivity;
    private final int mResource;
    private final ArrayList<Note> notelist;
    private Note note;
    private final ListView mListView;

    private static class ViewHolder {
        TextView title;
        ImageView popup;
        CardView cardView;
    }

    public NotesAdapter(@NonNull AppCompatActivity activity, ListView listView, int resource, @NonNull ArrayList<Note> objects) {
        super(activity, resource, objects);
        mActivity = activity;
        mListView = listView;
        mResource = resource;
        notelist = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String title = Objects.requireNonNull(getItem(position)).getTitle();
        String text = Objects.requireNonNull(getItem(position)).getText();
        int color = Objects.requireNonNull(getItem(position)).getColor();

        note = new Note(title, text, color);
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.titlenote);
            holder.popup = convertView.findViewById(R.id.popupbtn);
            holder.cardView = convertView.findViewById(R.id.notes_cardview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText(note.getTitle());
        holder.cardView.setCardBackgroundColor(note.getColor());
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
                            db.deleteNoteById(getItem(position));
                            db.updateNote(getItem(position));
                            notelist.remove(position);
                            notifyDataSetChanged();
                        });
                        return true;
                    } else if (itemId == R.id.edit_popup) {
                        final View alertLayout = mActivity.getLayoutInflater().inflate(R.layout.timetable_dialog_add_note, null);
                        AlertDialogsHelper.getEditNoteDialog(mActivity, alertLayout, notelist, mListView, position);
                        notifyDataSetChanged();
                        return true;
                    }
                    return

                            onMenuItemClick(item);
                }
            });
            popup.show();
        });

        hidePopUpMenu(holder);

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public ArrayList<Note> getNoteList() {
        return notelist;
    }

    public Note getNote() {
        return note;
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