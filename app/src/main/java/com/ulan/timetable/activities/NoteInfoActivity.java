package com.ulan.timetable.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
import com.ulan.timetable.model.Note;
import com.ulan.timetable.utils.DbHelper;

public class NoteInfoActivity extends ActivityFeatures {

    private DbHelper db;
    @Nullable
    private Note note;
    private EditText text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_note_info);
        setupIntent();
    }

    @Override
    public void setupColors() {
        setToolbar(true);
    }

    private void setupIntent() {
        db = new DbHelper(NoteInfoActivity.this);
        note = (Note) getIntent().getSerializableExtra(NotesActivity.KEY_NOTE);
        text = findViewById(R.id.edittextNote);
        if (note.getText() != null) {
            text.setText(note.getText());
        }
    }

    @Override
    public void onBackPressed() {
        note.setText(text.getText().toString());
        db.updateNote(note);
        Toast.makeText(NoteInfoActivity.this, getResources().getString(R.string.saved), Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                note.setText(text.getText().toString());
                db.updateNote(note);
                Toast.makeText(NoteInfoActivity.this, getResources().getString(R.string.saved), Toast.LENGTH_SHORT).show();
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
