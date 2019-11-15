package com.asdoi.gymwen.main.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asdoi.gymwen.R;
import com.asdoi.gymwen.main.WebsiteActivity;

import java.io.InputStream;

import androidx.fragment.app.Fragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class WebsiteActivityFragment extends Fragment implements View.OnClickListener {
    private View root;
    private Context context;
    private String[][] content;
    private LinearLayout basic;
    private int pageCode;
    WebsiteActivity buttonCall;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        loadHomeOfPages();
//        loadContentPages("http://www.gym-wen.de/schulleben/exkursionen/hc-bei-der-insights-x/");
        root = inflater.inflate(R.layout.fragment_website, container, false);
        context = getActivity();

        basic = root.findViewById(R.id.website_linear);
        buttonCall = (WebsiteActivity) getActivity();
        loadSite();
        return root;
    }

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

    public void loadHomeOfPages() {
        basic.removeAllViewsInLayout();

        float columnBottomDp = 10f;
        float columnLeftRightDp = 5f;
        String backgroundColor = "#80f2f2f2";
        float imageMarginDp = 10f;
        float rightMarginDp = 5f;
        int titleColor = Color.BLACK;
        int descriptionColor = Color.BLACK;

        for (int i = 0; i < content.length; i++) {
            //Create Views
            LinearLayout.LayoutParams columnViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            columnViewParams.setMargins((int) convertDpToPx(context, columnLeftRightDp), 0, (int) convertDpToPx(context, columnLeftRightDp), (int) convertDpToPx(context, columnBottomDp));
            LinearLayout column = new LinearLayout(context);
            column.setOnClickListener(buttonCall);
            column.setLayoutParams(columnViewParams);
            column.setOrientation(LinearLayout.HORIZONTAL);
            column.setBackgroundColor(Color.parseColor(backgroundColor));
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

            TextView descriptionView = new TextView(context);
            descriptionView.setLayoutParams(textViewparams);

            TextView linkView = new TextView(context);
            linkView.setLayoutParams(textViewparams);

            String imageUrl = content[i][0];
//                                System.out.println(url);
//                            WebView webView = findViewById(R.id.web_image);
//                            webView.setWebViewClient(new WebViewClient());
//                            webView.getSettings().setLoadsImagesAutomatically(true);
//                            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//                            webView.loadData(loadData, "text/html", "UTF-8");


            ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            imageViewParams.setMargins((int) convertDpToPx(context, imageMarginDp), (int) convertDpToPx(context, imageMarginDp), 0, (int) convertDpToPx(context, imageMarginDp));
            imageView.setLayoutParams(imageViewParams);


            //Set Views to values

            new DownloadImageTask(imageView)
                    .execute(imageUrl);


            String title = content[i][1];
            titleView.setText(title);
            titleView.setTextSize(22);
            titleView.setTextColor(titleColor);
            titleView.setTypeface(Typeface.DEFAULT_BOLD);
            titleView.setGravity(Gravity.LEFT);

            if (title.isEmpty()) {
                titleView.setVisibility(View.GONE);
            }


            String description = content[i][2];
            descriptionView.setText(description);
            descriptionView.setTextSize(14);
            descriptionView.setTextColor(descriptionColor);
            descriptionView.setTypeface(Typeface.DEFAULT);
            descriptionView.setGravity(Gravity.LEFT);

            if (description.isEmpty()) {
                descriptionView.setVisibility(View.GONE);
            }

            String link = content[i][3];
//                            linkView.setText(link);
            linkView.setTextSize(8);
            linkView.setTextColor(Color.BLUE);
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

    public void loadContentPages() {
        basic.removeAllViewsInLayout();

        float columnBottomDp = 10f;
        float columnLeftRightDp = 5f;
        String backgroundColor = "#80FFFFFF";
        float imageMarginDp = 10f;
        float rightMarginDp = 5f;
        int titleColor = Color.BLACK;
        int descriptionColor = Color.BLACK;

        for (int i = 0; i < content.length; i++) {
            //Create Views
            LinearLayout.LayoutParams columnViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            columnViewParams.setMargins((int) convertDpToPx(context, columnLeftRightDp), 0, (int) convertDpToPx(context, columnLeftRightDp), (int) convertDpToPx(context, columnBottomDp));
            LinearLayout column = new LinearLayout(context);
//            column.setOnClickListener(buttonCall);
            column.setLayoutParams(columnViewParams);
            column.setOrientation(LinearLayout.HORIZONTAL);
            column.setBackgroundColor(Color.parseColor(backgroundColor));
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

            TextView descriptionView = new TextView(context);
            descriptionView.setLayoutParams(textViewparams);

            TextView linkView = new TextView(context);
            linkView.setLayoutParams(textViewparams);

            String imageUrl = content[i][0];
//                                System.out.println(url);
//                            WebView webView = findViewById(R.id.web_image);
//                            webView.setWebViewClient(new WebViewClient());
//                            webView.getSettings().setLoadsImagesAutomatically(true);
//                            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//                            webView.loadData(loadData, "text/html", "UTF-8");


            ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            imageViewParams.setMargins((int) convertDpToPx(context, imageMarginDp), (int) convertDpToPx(context, imageMarginDp), 0, (int) convertDpToPx(context, imageMarginDp));
            imageView.setLayoutParams(imageViewParams);
            imageView.setOnClickListener(this);


            //Set Views to values

            new DownloadImageTask(imageView)
                    .execute(imageUrl);


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
            descriptionView.setGravity(Gravity.LEFT);

            if (description.isEmpty()) {
                descriptionView.setVisibility(View.GONE);
            }

            String link = content[i][3];
//                            linkView.setText(link);
            linkView.setTextSize(8);
            linkView.setTextColor(Color.BLUE);
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

    public void loadContentPagesMixed() {
        basic.removeAllViewsInLayout();

        float columnBottomDp = 10f;
        float columnLeftRightDp = 5f;
        String backgroundColor = "#80FFFFFF";
        float imageMarginDp = 10f;
        float rightMarginDp = 5f;
        int titleColor = Color.BLACK;
        int descriptionColor = Color.BLACK;

        for (int i = 0; i < content.length; i++) {
            //Create Views
            LinearLayout.LayoutParams columnViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            columnViewParams.setMargins((int) convertDpToPx(context, columnLeftRightDp), 0, (int) convertDpToPx(context, columnLeftRightDp), (int) convertDpToPx(context, columnBottomDp));
            LinearLayout column = new LinearLayout(context);
            column.setOnClickListener(buttonCall);
            column.setLayoutParams(columnViewParams);
            column.setOrientation(LinearLayout.HORIZONTAL);
            column.setBackgroundColor(Color.parseColor(backgroundColor));
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

            TextView descriptionView = new TextView(context);
            descriptionView.setLayoutParams(textViewparams);

            TextView linkView = new TextView(context);
            linkView.setLayoutParams(textViewparams);

            String imageUrl = content[i][0];
//                                System.out.println(url);
//                            WebView webView = findViewById(R.id.web_image);
//                            webView.setWebViewClient(new WebViewClient());
//                            webView.getSettings().setLoadsImagesAutomatically(true);
//                            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//                            webView.loadData(loadData, "text/html", "UTF-8");


            ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            imageViewParams.setMargins((int) convertDpToPx(context, imageMarginDp), (int) convertDpToPx(context, imageMarginDp), 0, (int) convertDpToPx(context, imageMarginDp));
            imageView.setLayoutParams(imageViewParams);
            if (i == 0)
                imageView.setOnClickListener(this);


            //Set Views to values

            new DownloadImageTask(imageView)
                    .execute(imageUrl);


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
            descriptionView.setGravity(Gravity.LEFT);

            if (description.isEmpty()) {
                descriptionView.setVisibility(View.GONE);
            }

            String link = content[i][3];
//                            linkView.setText(link);
            linkView.setTextSize(8);
            linkView.setTextColor(Color.BLUE);
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

    private float convertDpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == -1) {
            //Goto Home of Projects
            String url = ((WebsiteActivity) getActivity()).history.get(((WebsiteActivity) getActivity()).history.size() - 1);
            String first = url.substring("http://".length() + 1);
            String second = url.substring(first.indexOf('/'));
            int charOfsecondSlash = second.indexOf('/') + 1 + "http://".length() + 2 + first.indexOf('/') + 1;
            ((WebsiteActivity) getActivity()).loadPage(url.substring(0, charOfsecondSlash));
        } else {
            buttonCall.onClick(view);
        }
        //TODO: Expand images
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            if (!urldisplay.trim().isEmpty()) {
                Bitmap mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
//                Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
                return mIcon11;
            }
            return null;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
