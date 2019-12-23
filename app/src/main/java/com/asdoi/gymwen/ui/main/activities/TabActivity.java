package com.asdoi.gymwen.ui.main.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.main.fragments.LehrerlisteFragment;
import com.asdoi.gymwen.ui.main.fragments.VertretungFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class TabActivity extends ActivityFeatures {
    SectionsPagerAdapter sectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_nav);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), false, new String[]{getString(R.string.menu_today), getString(R.string.menu_tomorrow)});
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        navView.setOnNavigationItemSelectedListener((@NonNull MenuItem item) -> {
            switch (item.getItemId()) {
                default:
                case R.id.navigation_home:
                    findViewById(R.id.view_pager).setVisibility(View.VISIBLE);
                    findViewById(R.id.tabs).setVisibility(View.VISIBLE);
                    findViewById(R.id.nav_host_fragment).setVisibility(View.GONE);
                    sectionsPagerAdapter.setAll(false);
                    sectionsPagerAdapter.notifyDataSetChanged();
                    break;
                case R.id.navigation_dashboard:
                    findViewById(R.id.view_pager).setVisibility(View.VISIBLE);
                    findViewById(R.id.tabs).setVisibility(View.VISIBLE);
                    findViewById(R.id.nav_host_fragment).setVisibility(View.GONE);
                    sectionsPagerAdapter.setAll(true);
                    sectionsPagerAdapter.notifyDataSetChanged();
                    break;
                case R.id.navigation_notifications:
                    findViewById(R.id.view_pager).setVisibility(View.GONE);
                    findViewById(R.id.tabs).setVisibility(View.GONE);
                    findViewById(R.id.nav_host_fragment).setVisibility(View.VISIBLE);
                    LehrerlisteFragment fragment = new LehrerlisteFragment();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
                    break;
            }
            return true;
        });
    }


    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        String[] tab_titles;
        boolean all;

        SectionsPagerAdapter(FragmentManager fm, boolean all, String[] titles) {
            super(fm);
            this.tab_titles = titles;
            setAll(all);
        }

        void setAll(boolean v) {
            all = v;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            position++;
            return VertretungFragment.newInstance(all ? position + 2 : position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tab_titles[position];
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return tab_titles.length;
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof VertretungFragment) {
                ((VertretungFragment) object).update(all);
            }

            //don't return POSITION_NONE, avoid fragment recreation.
            return super.getItemPosition(object);
        }
    }
}