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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.materialdialogs.MaterialDialog;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.profiles.Profile;
import com.asdoi.gymwen.profiles.ProfileManagement;
import com.asdoi.gymwen.ui.activities.ChoiceActivity;

import org.jetbrains.annotations.NotNull;

public class ProfileActivityFragment extends Fragment {
    private ProfileListAdapter adapter;
    private int preferredProfilePos = ProfileManagement.getPreferredProfilePosition();

    public ProfileActivityFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        adapter = new ProfileListAdapter(getContext(), 0);
        ((ListView) root.findViewById(R.id.profile_list)).setAdapter(adapter);

        root.findViewById(R.id.profile_add_button).setBackgroundColor(ApplicationFeatures.getAccentColor(getContext()));
        root.findViewById(R.id.profile_add_button).setOnClickListener((View v) -> openAddDialog());
        return root;
    }

    private class ProfileListAdapter extends ArrayAdapter<String[]> {

        ProfileListAdapter(@NonNull Context con, int resource) {
            super(con, resource);
        }

        @NotNull
        @Override
        public View getView(int position, @Nullable View convertView, @NotNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_profiles_entry, null);
                // Only apply the first time the view is created
                ATE.apply(convertView.getContext(), convertView);
            }

            return generateView(convertView, position);
        }

        @Override
        public int getCount() {
            return ProfileManagement.getSize();
        }

        @NonNull
        private View generateView(@NonNull View base, int position) {
            Profile p = ProfileManagement.getProfile(position);
            TextView name = base.findViewById(R.id.profilelist_name);
            name.setText(p.getName());

            TextView courses = base.findViewById(R.id.profilelist_courses);
            courses.setText(p.getCourses());

            ImageButton edit = base.findViewById(R.id.profilelist_edit);
            edit.setOnClickListener((View v) -> openEditDialog(position));

            ImageButton delete = base.findViewById(R.id.profilelist_delete);
            delete.setOnClickListener((View v) -> openDeleteDialog(position));

            ImageButton star = base.findViewById(R.id.profilelist_preferred);
            if (position == preferredProfilePos) {
                star.setImageResource(R.drawable.ic_star_black_24dp);
            } else {
                star.setImageResource(R.drawable.ic_star_border_black_24dp);
            }
            star.setOnClickListener((View v) -> setPreferredProfile(position));

            return base;
        }
    }

    private void openAddDialog() {
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
        builder.onPositive((dialog, which) -> {
            Intent mIntent = new Intent(getActivity(), ChoiceActivity.class);
            Bundle extras = new Bundle();
            extras.putBoolean("parents", true);
            if (input.getText().toString().trim().isEmpty())
                extras.putString("name", getContext().getString(R.string.profile_empty_name) + (ProfileManagement.getSize() + 1));
            else
                extras.putString("name", input.getText().toString());
            extras.putBoolean("profileAdd", true);

            mIntent.putExtras(extras);
            getActivity().startActivity(mIntent);
            getActivity().finish();
            dialog.dismiss();
        });

        builder.onNegative((dialog, which) -> dialog.dismiss());

        builder.positiveText(R.string.add);
        builder.negativeText(R.string.cancel);
        builder.negativeColor(ApplicationFeatures.getAccentColor(getContext()));
        builder.positiveColor(ApplicationFeatures.getAccentColor(getContext()));
        builder.build().show();
    }

    private void openEditDialog(int position) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
        builder.title(getString(R.string.profiles_edit));

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
        note.setText(getContext().getString(R.string.set_desc_courses));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 10, 0);
        note.setLayoutParams(params);
        base.addView(note);

        builder.customView(base, true);

        // Set up the buttons
        builder.positiveText(getString(R.string.ok));
        builder.negativeText(getString(R.string.cancel));
        builder.onPositive((dialog, which) -> {
            Profile profile = ProfileManagement.getProfile(position);
            String nameText = name.getText().toString();
            String coursesText = course.getText().toString();
            //Do not enter empty text
            ProfileManagement.editProfile(position, new Profile(coursesText.trim().isEmpty() ? profile.getCourses() : coursesText, nameText.trim().isEmpty() ? profile.getName() : nameText));
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        builder.onNegative((dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void openDeleteDialog(int position) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
        builder.title(getString(R.string.profiles_delete_submit_heading));

        LinearLayout base = new LinearLayout(getContext());
        base.setOrientation(LinearLayout.VERTICAL);

        TextView note = new TextView(getContext());
        Profile p = ProfileManagement.getProfile(position);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(60, 20, 10, 0);
        note.setText(getString(R.string.profiles_delete_message, p.getName()));
        note.setLayoutParams(params);

        base.addView(note);

        builder.customView(base, true);

        builder.positiveText(getString(R.string.yes));
        builder.onPositive((dialog, which) -> {
            ProfileManagement.removeProfile(position);
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        builder.onNegative((dialog, which) -> dialog.dismiss());

        builder.negativeText(getString(R.string.no));

        builder.show();
    }

    private void setPreferredProfile(int position) {
        ProfileManagement.setPreferredProfilePosition(position);
        preferredProfilePos = ProfileManagement.getPreferredProfilePosition();
        adapter.notifyDataSetChanged();
    }

}
