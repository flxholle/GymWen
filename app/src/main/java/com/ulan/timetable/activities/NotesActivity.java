package com.ulan.timetable.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.ulan.timetable.TimeTableBuilder;
import com.ulan.timetable.adapters.NotesAdapter;
import com.ulan.timetable.databaseUtils.DBUtil;
import com.ulan.timetable.databaseUtils.DbHelper;
import com.ulan.timetable.model.Note;
import com.ulan.timetable.utils.AlertDialogsHelper;

import java.util.ArrayList;

public class NotesActivity extends ActivityFeatures {

    @NonNull
    public static final String KEY_NOTE = "note";
    @NonNull
    private final AppCompatActivity context = this;
    private ListView listView;
    private DbHelper db;
    private NotesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_notes);
        initAll();
    }

    @Override
    public void setupColors() {
        setToolbar(true);
        if (Build.VERSION.SDK_INT >= 21)
            findViewById(R.id.fab).setBackgroundTintList(ColorStateList.valueOf(ApplicationFeatures.getAccentColor(this)));
    }

    private void initAll() {
        setupAdapter();
        setupListViewMultiSelect();
        setupCustomDialog();
    }

    private void setupAdapter() {
        db = new DbHelper(context);
        listView = findViewById(R.id.notelist);
        adapter = new NotesAdapter(NotesActivity.this, listView, R.layout.timetable_listview_notes_adapter, db.getNote());
        listView.setAdapter(adapter);
        int profilePos = DBUtil.getProfilePosition(this);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(context, NoteInfoActivity.class);
            intent.putExtra(TimeTableBuilder.PROFILE_POS, profilePos);
            intent.putExtra(KEY_NOTE, adapter.getNoteList().get(position));
            startActivity(intent);
        });
    }

    private void setupListViewMultiSelect() {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(@NonNull ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount = listView.getCheckedItemCount();
                mode.setTitle(checkedCount + " " + getResources().getString(R.string.selected));
                if (checkedCount == 0) mode.finish();
            }

            @Override
            public boolean onCreateActionMode(@NonNull ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.timetable_toolbar_action_mode, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(@NonNull final ActionMode mode, @NonNull MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    ArrayList<Note> removelist = new ArrayList<>();
                    SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                    for (int i = 0; i < checkedItems.size(); i++) {
                        int key = checkedItems.keyAt(i);
                        if (checkedItems.get(key)) {
                            db.deleteNoteById(adapter.getItem(key));
                            removelist.add(adapter.getNoteList().get(key));
                        }
                    }
                    adapter.getNoteList().removeAll(removelist);
                    db.updateNote(adapter.getNote());
                    adapter.notifyDataSetChanged();
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
    }

    private void setupCustomDialog() {
        final View alertLayout = getLayoutInflater().inflate(R.layout.timetable_dialog_add_note, null);
        AlertDialogsHelper.getAddNoteDialog(NotesActivity.this, alertLayout, adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.clear();
        adapter.addAll(db.getNote());
        adapter.notifyDataSetChanged();
    }
}
