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

        adapter = new ProfileListAdapter(requireContext(), 0);
        ((ListView) root.findViewById(R.id.profile_list)).setAdapter(adapter);

        requireActivity().findViewById(R.id.fab).setOnClickListener((View v) -> openAddDialog());
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
            }

            if (position < ProfileManagement.getSize())
                return generateProfileView(convertView, position);
            else
                return generateInfoView(convertView);
        }

        @Override
        public int getCount() {
            return ProfileManagement.getSize() + 1;
        }

        @NonNull
        private View generateProfileView(@NonNull View base, int position) {
            Profile p = ProfileManagement.getProfile(position);
            TextView name = base.findViewById(R.id.profilelist_name);
            name.setTextSize(22);
            name.setText(p.getName());

            TextView courses = base.findViewById(R.id.profilelist_courses);
            courses.setVisibility(View.VISIBLE);
            courses.setText(p.getCourses());

            ImageButton edit = base.findViewById(R.id.profilelist_edit);
            edit.setVisibility(View.VISIBLE);
            edit.setOnClickListener((View v) -> openEditDialog(position));

            ImageButton delete = base.findViewById(R.id.profilelist_delete);
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener((View v) -> openDeleteDialog(position));

            ImageButton star = base.findViewById(R.id.profilelist_preferred);
            star.setVisibility(View.VISIBLE);
            if (position == preferredProfilePos) {
                star.setImageResource(R.drawable.ic_star_black_24dp);
            } else {
                star.setImageResource(R.drawable.ic_star_border_black_24dp);
            }
            star.setOnClickListener((View v) -> setPreferredProfile(position));

            return base;
        }

        private View generateInfoView(View base) {
            base.findViewById(R.id.profilelist_edit).setVisibility(View.GONE);
            base.findViewById(R.id.profilelist_delete).setVisibility(View.GONE);
            base.findViewById(R.id.profilelist_preferred).setVisibility(View.GONE);
            TextView name = base.findViewById(R.id.profilelist_name);
            name.setText(getString(R.string.preferred_profile_explanation));
            name.setTextSize(12);
            TextView courses = base.findViewById(R.id.profilelist_courses);
            courses.setVisibility(View.GONE);
            return base;
        }
    }

    private void openAddDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(requireActivity());
        builder.title(getString(R.string.profiles_add));

        // Set up the input
        final EditText input = new EditText(requireContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(getString(R.string.name));
        input.setHighlightColor(ApplicationFeatures.getAccentColor(requireContext()));
//        input.setColor
        builder.customView(input, true);

        // Set up the buttons
        builder.onPositive((dialog, which) -> {
            Intent mIntent = new Intent(requireActivity(), ChoiceActivity.class);
            Bundle extras = new Bundle();
            extras.putBoolean("parents", true);
            if (input.getText().toString().trim().isEmpty())
                extras.putString("name", requireContext().getString(R.string.profile_empty_name) + (ProfileManagement.getSize() + 1));
            else
                extras.putString("name", input.getText().toString());
            extras.putBoolean("profileAdd", true);

            mIntent.putExtras(extras);
            requireActivity().startActivity(mIntent);
            requireActivity().finish();
            dialog.dismiss();
        });

        builder.onNegative((dialog, which) -> dialog.dismiss());

        builder.positiveText(R.string.add);
        builder.negativeText(R.string.cancel);
        builder.negativeColor(ApplicationFeatures.getAccentColor(requireContext()));
        builder.positiveColor(ApplicationFeatures.getAccentColor(requireContext()));
        builder.build().show();
    }

    private void openEditDialog(int position) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(requireContext());
        builder.title(getString(R.string.profiles_edit));

        // Set up the input
        LinearLayout base = new LinearLayout(requireContext());
        base.setOrientation(LinearLayout.VERTICAL);

        EditText name = new EditText(requireContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.setText(ProfileManagement.getProfile(position).getName());
        name.setHint(R.string.name);
        base.addView(name);

        // Set up the input
        EditText course = new EditText(requireContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        course.setText(ProfileManagement.getProfile(position).getCourses());
        course.setHint(getString(R.string.profile_courses));
        base.addView(course);

        TextView note = new TextView(requireContext());
        note.setText(requireContext().getString(R.string.set_desc_courses));
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
        Profile p = ProfileManagement.getProfile(position);
        new MaterialDialog.Builder(requireContext())
                .title(getString(R.string.profiles_delete_submit_heading))
                .content(getString(R.string.profiles_delete_message, p.getName()))
                .positiveText(getString(R.string.yes))
                .onPositive((dialog, which) -> {
                    ProfileManagement.removeProfile(position);
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                })
                .onNegative((dialog, which) -> dialog.dismiss())
                .negativeText(getString(R.string.no))
                .show();
    }

    private void setPreferredProfile(int position) {
        ProfileManagement.setPreferredProfilePosition(position);
        preferredProfilePos = ProfileManagement.getPreferredProfilePosition();
        adapter.notifyDataSetChanged();
    }

}
