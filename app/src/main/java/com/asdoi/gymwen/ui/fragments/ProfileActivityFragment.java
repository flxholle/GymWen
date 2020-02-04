package com.asdoi.gymwen.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.Profile;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.ui.activities.ChoiceActivity;

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

        root.findViewById(R.id.profile_add_button).setBackgroundColor(ApplicationFeatures.getAccentColor(getContext()));
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
            return ProfileManagement.sizeProfiles();
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
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.title(getString(R.string.profiles_add));

        // Set up the input
        final EditText input = new EditText(getContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(getString(R.string.name));
        input.setHighlightColor(ApplicationFeatures.getAccentColor(getContext()));
//        input.setColor
        builder.customView(input, true);

        // Set up the buttons
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(MaterialDialog dialog, DialogAction which) {
                Intent mIntent = new Intent(getActivity(), ChoiceActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean("parents", true);
                if (input.getText().toString().trim().isEmpty())
                    extras.putString("name", getContext().getString(R.string.profile_empty_name) + (ProfileManagement.sizeProfiles() + 1));
                else
                    extras.putString("name", input.getText().toString());
                extras.putBoolean("profileAdd", true);

                mIntent.putExtras(extras);
                getActivity().startActivity(mIntent);
                getActivity().finish();
            }
        });

        builder.positiveText(R.string.add);
        builder.negativeText(R.string.cancel);
        builder.negativeColor(ApplicationFeatures.getAccentColor(getContext()));
        builder.positiveColor(ApplicationFeatures.getAccentColor(getContext()));
        builder.build().show();
    }

    public void openEditDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.profiles_edit));

        // Set up the input
        LinearLayout base = new LinearLayout(getContext());
        base.setOrientation(LinearLayout.VERTICAL);

        EditText name = new EditText(getContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.setText(ProfileManagement.getProfile(position).getName());
        name.setHint(R.string.name);
        base.addView(name);

        // Set up the input
        EditText course = new EditText(getContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        course.setText(ProfileManagement.getProfile(position).getCourses());
        course.setHint(getString(R.string.profile_courses));
        base.addView(course);

        TextView note = new TextView(getContext());
        note.setText(getContext().getString(R.string.set_courses_summary));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 10, 0);
        note.setLayoutParams(params);
        base.addView(note);

        builder.setView(base);

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Profile profile = ProfileManagement.getProfile(position);
                String nameText = name.getText().toString();
                String coursesText = course.getText().toString();
                ProfileManagement.editProfile(position, new Profile(coursesText.trim().isEmpty() ? profile.getCourses() : coursesText, nameText.trim().isEmpty() ? profile.getName() : nameText));
                adapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), (DialogInterface d, int w) -> {

        });

        builder.show();
    }

    public void openDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.profiles_delete_submit_heading));

        LinearLayout base = new LinearLayout(getContext());
        base.setOrientation(LinearLayout.VERTICAL);

        TextView note = new TextView(getContext());
        Profile p = ProfileManagement.getProfile(position);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(60, 20, 10, 0);
        note.setText(getString(R.string.profiles_delete_message, p.getName()));
        note.setLayoutParams(params);

        base.addView(note);

        builder.setView(base);

        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ProfileManagement.removeProfile(position);
                adapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton(getString(R.string.no), (DialogInterface d, int w) -> {

        });

        builder.show();
    }

}
