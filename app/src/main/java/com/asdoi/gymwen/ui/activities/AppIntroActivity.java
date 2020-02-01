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
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_one_title), getString(R.string.intro_one_desc), R.mipmap.gymlogo, ContextCompat.getColor(this, R.color.intro_background_one)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_two_title), getString(R.string.intro_two_desc), R.drawable.intro_img_2, ContextCompat.getColor(this, R.color.intro_background_two)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_three_title), getString(R.string.intro_three_desc), R.drawable.intro_img_3, ContextCompat.getColor(this, R.color.intro_background_three)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_four_title), getString(R.string.intro_four_desc), R.drawable.intro_img_4, ContextCompat.getColor(this, R.color.intro_background_four)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_five_title), getString(R.string.intro_five_desc), R.drawable.intro_img_5, ContextCompat.getColor(this, R.color.intro_background_five)));

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
