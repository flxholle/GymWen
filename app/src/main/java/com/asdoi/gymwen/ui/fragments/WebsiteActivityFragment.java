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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.ui.activities.WebsiteActivity;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.Objects;

/**
 * A placeholder fragment containing a simple view.
 */
public class WebsiteActivityFragment extends Fragment implements View.OnClickListener {
    private View root;
    @Nullable
    private Context context;
    private String[][] content;
    private LinearLayout basic;
    private int pageCode;
    @Nullable
    private WebsiteActivity websiteActivity;

    private int shortAnimationDuration;
    @Nullable
    private Animator currentAnimator;
    public static PhotoView expandImage;
    public static boolean isExpanded;

    public WebsiteActivityFragment() {
    }

    /*pageCodes:
    0 = Load Nothing
    1 = Homepage
    2 = Schulleben
     */

    public WebsiteActivityFragment(String[][] con, int pageCode) {
        this.content = con;
        this.pageCode = pageCode;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        loadHomeOfPages();
//        loadContentPages("http://www.gym-wen.de/schulleben/exkursionen/hc-bei-der-insights-x/");
        root = inflater.inflate(R.layout.fragment_website, container, false);
        context = getActivity();

        root.setBackgroundColor(ApplicationFeatures.getWebPageBackgroundColor(requireContext()));

        basic = root.findViewById(R.id.website_linear);
        websiteActivity = (WebsiteActivity) getActivity();

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loadSite();

        return root;
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//    }

    public void changeSite(String[][] con, int pageCode) {
        this.content = con;
        this.pageCode = pageCode;
        loadSite();
    }

    private void loadSite() {
        switch (pageCode) {
            case 1:
                loadHomeOfPages();
                break;
            case 2:
//                loadContentPages();
//                break;
            case 3:
                loadContentPagesMixed();
            default:
                break;
        }
    }

    private void loadHomeOfPages() {
        basic.removeAllViewsInLayout();

        float columnBottomDp = 10f;
        float columnLeftRightDp = 5f;
        int backgroundColor = ApplicationFeatures.getBackgroundColor(requireContext());
        float imageMarginDp = 10f;
        float rightMarginDp = 5f;
        int titleColor = ApplicationFeatures.getTextColorPrimary(requireContext());
        int descriptionColor = ApplicationFeatures.getTextColorPrimary(context);
        int linkColor = ApplicationFeatures.getLinkColor(context);

        for (int i = 0; i < content.length; i++) {
            //Create Views
            LinearLayout.LayoutParams columnViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            columnViewParams.setMargins((int) convertDpToPx(context, columnLeftRightDp), 0, (int) convertDpToPx(context, columnLeftRightDp), (int) convertDpToPx(context, columnBottomDp));
            LinearLayout column = new LinearLayout(context);
            column.setOnClickListener(websiteActivity);
            column.setLayoutParams(columnViewParams);
            column.setOrientation(LinearLayout.HORIZONTAL);
            column.setBackgroundColor(backgroundColor);
            column.setId(i);

            LinearLayout.LayoutParams rightViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rightViewParams.setMargins((int) convertDpToPx(context, imageMarginDp), (int) convertDpToPx(context, rightMarginDp), (int) convertDpToPx(context, rightMarginDp), (int) convertDpToPx(context, rightMarginDp));
            LinearLayout rightSide = new LinearLayout(context);
            rightSide.setLayoutParams(rightViewParams);
            rightSide.setOrientation(LinearLayout.VERTICAL);

            ViewGroup.LayoutParams restViewParams = new ViewGroup.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout rest = new LinearLayout(context);
            rest.setLayoutParams(restViewParams);
            rest.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams textViewparams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.5f
            );
            TextView titleView = new TextView(context);
            titleView.setLayoutParams(textViewparams);
//            titleView.setTextIsSelectable(true);

            TextView descriptionView = new TextView(context);
            descriptionView.setLayoutParams(textViewparams);
//            descriptionView.setTextIsSelectable(true);

            TextView linkView = new TextView(context);
            linkView.setLayoutParams(textViewparams);

            String imageUrl = content[i][0];
//                                System.out.println(url);
//                            WebView webView = findViewById(R.id.web_image);
//                            webView.setWebViewClient(new WebViewClient());
//                            webView.getSettings().setLoadsImagesAutomatically(true);
//                            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//                            webView.loadData(loadData, "text/html", "UTF-8");


//            ImageView imageView = new ImageView(context);
            PhotoView imageView = new PhotoView(context);
            LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            imageViewParams.setMargins((int) convertDpToPx(context, imageMarginDp), (int) convertDpToPx(context, imageMarginDp), 0, (int) convertDpToPx(context, imageMarginDp));
            imageView.setLayoutParams(imageViewParams);
            imageView.setOnClickListener((View v) -> {
                Bitmap bitmap = ((BitmapDrawable) ((ImageView) v).getDrawable()).getBitmap();
                zoomImageFromThumb(v, bitmap);
            });


            //Set Views to values

            new ApplicationFeatures.DownloadImageTask(imageView).execute(imageUrl);


            String title = content[i][1];
            titleView.setText(title);
            titleView.setTextSize(22);
            titleView.setTextColor(titleColor);
            titleView.setTypeface(Typeface.DEFAULT_BOLD);
            titleView.setGravity(Gravity.START);

            if (title.isEmpty()) {
                titleView.setVisibility(View.GONE);
            }


            String description = content[i][2];
            descriptionView.setText(description);
            descriptionView.setTextSize(14);
            descriptionView.setTextColor(descriptionColor);
            descriptionView.setTypeface(Typeface.DEFAULT);
            descriptionView.setGravity(Gravity.START);

            if (description.isEmpty()) {
                descriptionView.setVisibility(View.GONE);
            }

            String link = content[i][3];
//                            linkView.setText(link);
            linkView.setTextSize(8);
            linkView.setTextColor(linkColor);
            linkView.setTypeface(Typeface.DEFAULT);
            linkView.setGravity(Gravity.CENTER);


            //Add Values to basic
            rest.addView(descriptionView, 0);
            rest.addView(linkView, 1);
            rightSide.addView(titleView, 0);
            rightSide.addView(rest, 1);
            column.addView(imageView, 0);
            column.addView(rightSide, 1);
            basic.addView(column);
        }
    }

    private void loadContentPagesMixed() {
        basic.removeAllViewsInLayout();

        float columnBottomDp = 10f;
        float columnLeftRightDp = 5f;
        int backgroundColor = ApplicationFeatures.getBackgroundColor(requireContext());
        float imageMarginDp = 10f;
        float rightMarginDp = 5f;
        int titleColor = ApplicationFeatures.getTextColorPrimary(requireContext());
        int descriptionColor = ApplicationFeatures.getTextColorPrimary(context);
        int linkColor = ApplicationFeatures.getLinkColor(context);

        for (int i = 0; i < content.length; i++) {
            //Create Views
            LinearLayout.LayoutParams columnViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            columnViewParams.setMargins((int) convertDpToPx(context, columnLeftRightDp), 0, (int) convertDpToPx(context, columnLeftRightDp), (int) convertDpToPx(context, columnBottomDp));
            LinearLayout column = new LinearLayout(context);
            column.setOnClickListener(websiteActivity);
            column.setLayoutParams(columnViewParams);
            column.setOrientation(LinearLayout.HORIZONTAL);
            column.setBackgroundColor(backgroundColor);
            column.setId(i);

            LinearLayout.LayoutParams rightViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rightViewParams.setMargins((int) convertDpToPx(context, imageMarginDp), (int) convertDpToPx(context, rightMarginDp), (int) convertDpToPx(context, rightMarginDp), (int) convertDpToPx(context, rightMarginDp));
            LinearLayout rightSide = new LinearLayout(context);
            rightSide.setLayoutParams(rightViewParams);
            rightSide.setOrientation(LinearLayout.VERTICAL);

            ViewGroup.LayoutParams restViewParams = new ViewGroup.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout rest = new LinearLayout(context);
            rest.setLayoutParams(restViewParams);
            rest.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams textViewparams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.5f
            );
            TextView titleView = new TextView(context);
            titleView.setLayoutParams(textViewparams);
            titleView.setTextIsSelectable(true);

            TextView descriptionView = new TextView(context);
            descriptionView.setLayoutParams(textViewparams);
            descriptionView.setTextIsSelectable(true);

            TextView linkView = new TextView(context);
            linkView.setLayoutParams(textViewparams);

            String imageUrl = content[i][0];
//                                System.out.println(url);
//                            WebView webView = findViewById(R.id.web_image);
//                            webView.setWebViewClient(new WebViewClient());
//                            webView.getSettings().setLoadsImagesAutomatically(true);
//                            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//                            webView.loadData(loadData, "text/html", "UTF-8");


            PhotoView imageView = new PhotoView(context);
            LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            imageViewParams.setMargins((int) convertDpToPx(context, imageMarginDp), (int) convertDpToPx(context, imageMarginDp), 0, (int) convertDpToPx(context, imageMarginDp));
            imageView.setLayoutParams(imageViewParams);
            imageView.setOnClickListener((View v) -> {
                Bitmap bitmap = ((BitmapDrawable) ((ImageView) v).getDrawable()).getBitmap();
                zoomImageFromThumb(v, bitmap);
            });


            //Set Views to values

            new ApplicationFeatures.DownloadImageTask(imageView).execute(imageUrl);


            String title = content[i][1];
            titleView.setText(title);
            titleView.setTextSize(22);
            titleView.setTextColor(titleColor);
            titleView.setTypeface(Typeface.DEFAULT_BOLD);
            titleView.setGravity(Gravity.CENTER);

            if (title.isEmpty()) {
                titleView.setVisibility(View.GONE);
            }


            String description = content[i][2];
            descriptionView.setText(description);
            descriptionView.setTextSize(14);
            descriptionView.setTextColor(descriptionColor);
            descriptionView.setTypeface(Typeface.DEFAULT);
            descriptionView.setGravity(Gravity.START);

            if (description.isEmpty()) {
                descriptionView.setVisibility(View.GONE);
            }

            String link = content[i][3];
//                            linkView.setText(link);
            linkView.setTextSize(8);
            linkView.setTextColor(linkColor);
            linkView.setTypeface(Typeface.DEFAULT);
            linkView.setGravity(Gravity.CENTER);


            //Add Values to basic
            rest.addView(descriptionView, 0);
            rest.addView(linkView, 1);
            rightSide.addView(titleView, 0);
            rightSide.addView(rest, 1);
            column.addView(imageView, 0);
            column.addView(rightSide, 1);
            basic.addView(column);
        }
    }

    private static float convertDpToPx(@NonNull Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    @Override
    public void onClick(@NonNull View view) {
        int id = view.getId();
        if (id == -1) {
            //Goto Home of Projects
            String url = Objects.requireNonNull(((WebsiteActivity) requireActivity()).history).get(Objects.requireNonNull(((WebsiteActivity) getActivity()).history).size() - 1);
            String first = url.substring("http://".length() + 1);
            String second = url.substring(first.indexOf('/'));
            int charOfsecondSlash = second.indexOf('/') + 1 + "http://".length() + 2 + first.indexOf('/') + 1;
            ((WebsiteActivity) getActivity()).loadPage(url.substring(0, charOfsecondSlash));
        } else {
            Objects.requireNonNull(websiteActivity).onClick(view);
        }
    }

    private void zoomImageFromThumb(@NonNull final View thumbView, Bitmap image) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final PhotoView expandedImageView = root.findViewById(R.id.expanded_image);
        expandedImageView.setImageBitmap(image);
//        expandImage.setImageResource(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        root.findViewById(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(view -> {
            if (currentAnimator != null) {
                currentAnimator.cancel();
            }

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            AnimatorSet set1 = new AnimatorSet();
            set1.play(ObjectAnimator
                    .ofFloat(expandedImageView, View.X, startBounds.left))
                    .with(ObjectAnimator
                            .ofFloat(expandedImageView,
                                    View.Y, startBounds.top))
                    .with(ObjectAnimator
                            .ofFloat(expandedImageView,
                                    View.SCALE_X, startScaleFinal))
                    .with(ObjectAnimator
                            .ofFloat(expandedImageView,
                                    View.SCALE_Y, startScaleFinal));
            set1.setDuration(shortAnimationDuration);
            set1.setInterpolator(new DecelerateInterpolator());
            set1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    thumbView.setAlpha(1f);
                    expandedImageView.setVisibility(View.GONE);
                    currentAnimator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    thumbView.setAlpha(1f);
                    expandedImageView.setVisibility(View.GONE);
                    currentAnimator = null;
                }
            });
            set1.start();
            currentAnimator = set1;

            isExpanded = false;
        });
        isExpanded = true;
        expandImage = expandedImageView;
    }
}
