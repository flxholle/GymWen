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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.WebsiteActivity;

import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class WebsiteSearchFragment extends Fragment {
    private ListView listView;
    private List<WebsiteSearchLink> content;
    private List<WebsiteSearchLink> contentAll;

    private View root;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_search_template, container, false);
        listView = root.findViewById(R.id.search_template_list);
        listView.setVisibility(View.GONE);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((ActivityFeatures) getActivity()).createLoadingPanel((ViewGroup) root);

        new Thread(() -> {
            if (contentAll == null)
                content = getLinks();
            else
                content = contentAll;
            contentAll = content;
            getActivity().runOnUiThread(() -> {
                createLayout(root);
            });
        }).start();
    }

    private void createLayout(View root) {
        ((ActivityFeatures) getActivity()).removeLoadingPanel((ViewGroup) root);
        listView.setVisibility(View.VISIBLE);
        listView.setAdapter(new SearchListAdapter(getContext(), 0));

        ((EditText) root.findViewById(R.id.search_template_input)).addTextChangedListener(new TextWatcher() {
            @NonNull
            String before = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(@NonNull CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(before)) {
                    if (charSequence.length() > 0) {
                        search("" + charSequence);
                    } else {
                        content = contentAll;
                    }
                    before = charSequence.toString();
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private List<WebsiteSearchLink> getLinks() {
        String url = "http://www.gym-wen.de/startseite/navigation/";
        Document doc = ApplicationFeatures.downloadDoc(url);
        if (doc == null) {
            return new ArrayList<>(0);
        }
        try {
            ArrayList<WebsiteSearchLink> con = new ArrayList<WebsiteSearchLink>();
            Elements values = doc.select("div.csc-sitemap").select("a[href]");
            for (int i = 0; i < values.size(); i++) {
                String whole = values.get(i).toString();

                String link = null;
                int beginIndex = whole.indexOf("href=");
                int endIndex = whole.indexOf("\"", beginIndex + "href=".length() + 1);
                if (beginIndex > 0 && endIndex > 0 && beginIndex - endIndex < 0) {
                    link = whole.substring(beginIndex + "href=".length() + 1, endIndex);
                }
                if (link != null && !link.substring(0, "http".length()).equals("http")) {
                    link = "http://gym-wen.de/" + link;
                }

                String name = HtmlCompat.fromHtml(whole, 0).toString().replaceAll("\n", "");

                int level = link.replace("http://gym-wen.de/", "").split("/").length;

                if (!name.trim().isEmpty()) {
                    con.add(new WebsiteSearchLink(name, link, level));
                }
            }

            return con;

        } catch (Exception e) {
            return new ArrayList<>(0);
        }
    }

    private void search(String query) {
        List<WebsiteSearchLink> all = contentAll;
        List<WebsiteSearchLink> matches = new ArrayList<>(0);
        for (WebsiteSearchLink n : all) {
            if (n.getName().toUpperCase().contains(query.toUpperCase())) {
                matches.add(n);
            }
        }
        content = matches;
    }

    private class SearchListAdapter extends ArrayAdapter<String[]> {

        SearchListAdapter(@NonNull Context con, int resource) {
            super(con, resource);
        }

        @NotNull
        @Override
        public View getView(int position, @Nullable View convertView, @NotNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_website_search_entry, null);
            }

            return createView(convertView, position);
        }

        @Override
        public int getCount() {
            return content.size();
        }

        private View createView(View view, int position) {
            WebsiteSearchLink link = content.get(position);

            view.findViewById(R.id.website_search_level1).setVisibility(View.GONE);
            view.findViewById(R.id.website_search_level2).setVisibility(View.GONE);
            view.findViewById(R.id.website_search_level3).setVisibility(View.GONE);

            ImageButton button = view.findViewById(R.id.website_search_tab_intent);
            button.setVisibility(View.GONE);

            TextView usedView;
            switch (link.getLevel()) {
                case 1:
                    usedView = view.findViewById(R.id.website_search_level1);
                    break;
                case 2:
                    usedView = view.findViewById(R.id.website_search_level2);
                    button.setVisibility(View.VISIBLE);
                    break;
                default:
                case 3:
                    usedView = view.findViewById(R.id.website_search_level3);
                    break;
            }

            usedView.setVisibility(View.VISIBLE);
            usedView.setText(link.getName());

            if (link.getLink() != null) {
                usedView.setOnClickListener((View v) -> {
                    ((WebsiteActivity) getActivity()).loadPage(link.getLink());
                    getActivity().invalidateOptionsMenu();
                });
                TypedValue outValue = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                usedView.setBackgroundResource(outValue.resourceId);

                button.setOnClickListener((View v) -> {
                    ((ActivityFeatures) getActivity()).tabIntent(link.getLink());
                });
            }

            return view;
        }
    }

    private class WebsiteSearchLink {
        String name;
        String link;
        int level;

        WebsiteSearchLink(String name, String link, int level) {
            this.name = name;
            this.link = link;
            this.level = level;
        }

        public String getName() {
            return name;
        }

        public String getLink() {
            return link;
        }

        public int getLevel() {
            return level;
        }
    }
}
