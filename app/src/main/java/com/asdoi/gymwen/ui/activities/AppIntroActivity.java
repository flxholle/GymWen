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

package com.asdoi.gymwen.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.R;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;


public class AppIntroActivity extends AppIntro2 {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setSlideOverAnimation();
        showSkipButton(false);
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_one_title), null, getString(R.string.intro_one_desc), null, R.mipmap.gymlogo, ContextCompat.getColor(this, R.color.intro_background_one), 0, 0));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_two_title), null, getString(R.string.intro_two_desc), null, R.drawable.intro_img_2, ContextCompat.getColor(this, R.color.intro_background_two), 0, 0));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_three_title), null, getString(R.string.intro_three_desc), null, R.drawable.intro_img_3, ContextCompat.getColor(this, R.color.intro_background_three), 0, 0));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_four_title), null, getString(R.string.intro_four_desc), null, R.drawable.intro_img_4, ContextCompat.getColor(this, R.color.intro_background_four), 0, 0));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_five_title), null, getString(R.string.intro_five_desc), null, R.drawable.intro_img_5, ContextCompat.getColor(this, R.color.intro_background_five), 0, 0));

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SharedPreferences sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("intro", true);
        editor.apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
