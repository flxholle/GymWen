package com.asdoi.gymwen.ui.main.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.Profile;
import com.asdoi.gymwen.profiles.ProfileManagement;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProfileActivityFragment extends Fragment {
    private ProfileListAdapter adapter;

    public ProfileActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        adapter = new ProfileListAdapter(getContext(), 0);
        ((ListView) root.findViewById(R.id.profile_list)).setAdapter(adapter);

        root.findViewById(R.id.profile_add_button).setOnClickListener((View v) -> {
            openAddDialog();
        });

        return root;
    }

    private class ProfileListAdapter extends ArrayAdapter<String[]> {

        public ProfileListAdapter(Context con, int resource) {
            super(con, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_profiles_entry, null);
            }

            return generateView(convertView, position);
        }

        @Override
        public int getCount() {
            return ProfileManagement.profileQuantity();
        }

        private View generateView(View base, int position) {
            Profile p = ProfileManagement.getProfile(position);
            TextView name = base.findViewById(R.id.profilelist_name);
            name.setText(p.getName());

            TextView courses = base.findViewById(R.id.profilelist_courses);
            courses.setText(p.getCourses());

            ImageButton edit = base.findViewById(R.id.profilelist_edit);
            edit.setOnClickListener((View v) -> {
                openEditDialog(position);
            });

            ImageButton delete = base.findViewById(R.id.profilelist_delete);
            delete.setOnClickListener((View v) -> {
                openDeleteDialog(position);
            });

            return base;
        }
    }

    public void openAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Profile hinzufügen");

        // Set up the input
        final EditText input = new EditText(getContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Hinzufügen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    public void openEditDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Profil bearbeiten");

        // Set up the input
        LinearLayout base = new LinearLayout(getContext());
        base.setOrientation(LinearLayout.VERTICAL);

        EditText name = new EditText(getContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.setText(ProfileManagement.getProfile(position).getName());
        base.addView(name);

        // Set up the input
        EditText course = new EditText(getContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        course.setText(ProfileManagement.getProfile(position).getCourses());
        base.addView(course);

        TextView note = new TextView(getContext());
        note.setText(getContext().getString(R.string.set_courses_summary));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 10, 0);
        note.setLayoutParams(params);
        base.addView(note);

        builder.setView(base);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Profile profile = ProfileManagement.getProfile(position);
                String nameText = name.getText().toString();
                String coursesText = course.getText().toString();
                ProfileManagement.editProfile(position, new Profile(coursesText.trim().isEmpty() ? profile.getCourses() : coursesText, nameText.trim().isEmpty() ? profile.getName() : nameText));
                adapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancel", (DialogInterface d, int w) -> {

        });

        builder.show();
    }

    public void openDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Bestätigen");

        LinearLayout base = new LinearLayout(getContext());
        base.setOrientation(LinearLayout.VERTICAL);

        TextView note = new TextView(getContext());
        Profile p = ProfileManagement.getProfile(position);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 10, 0);
        note.setText("Soll der Nutzer " + p.getName() + " gelöscht werden?");
        note.setLayoutParams(params);

        base.addView(note);

        builder.setView(base);

        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ProfileManagement.removeProfile(position);
                adapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Nein", (DialogInterface d, int w) -> {

        });

        builder.show();
    }

}
