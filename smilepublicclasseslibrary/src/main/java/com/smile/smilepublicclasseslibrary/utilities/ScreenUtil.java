package com.smile.smilepublicclasseslibrary.utilities;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.WindowManager;

/**
 * Created by chaolee on 2017-10-24.
 */

public class ScreenUtil {

    public static int getStatusBarHeight(Context context) {
        int height = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = context.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    public static int getNavigationBarHeight(Context context) {
        int height = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = context.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    public static int getActionBarHeight(Context context) {
        int height = 0;
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize,tv, true))
        {
            height = TypedValue.complexToDimensionPixelSize(tv.data,context.getResources().getDisplayMetrics());
        }
        return height;
    }

    public static Point getScreenSize(Context context) {
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        // Display display =  ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return size;
    }

    public static float getDefaultTextSizeFromTheme(Context context, int themeId) {
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        int[] attrs = {android.R.attr.textSize}; // retrieve text size from style.xml
        TypedArray typedArray = context.obtainStyledAttributes(themeId, attrs);
        float dimension = typedArray.getDimension(0,0);
        float density = displayMetrics.density;
        float defaultTextFontSize = dimension / density;
        typedArray.recycle();

        return defaultTextFontSize;
    }

    public static float getDefaultTextSizeFromTheme(Context context) {
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        int[] attrs = {android.R.attr.textSize}; // retrieve text size from style.xml
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        float dimension = typedArray.getDimension(0,0);
        float density = displayMetrics.density;
        float defaultTextFontSize = dimension / density;
        typedArray.recycle();

        return defaultTextFontSize;
    }

    public static float suitableFontSize(Context context, float defaultFontSize, float baseScreenWidthSize) {

        // this method based on Nexus 5 device
        // 1080 x 1776 pixels
        // screen width in inches = 2.25
        // screen height in inches = 3.7
        // screen size = 4.330415725176914 inches (diagonal)

        float fontSize = 24; // default font size setting for Nexus 5
        if (defaultFontSize > 0) {
            fontSize = defaultFontSize;
        }

        float fontScale = suitableFontScale(context, baseScreenWidthSize);
        fontSize = fontSize * fontScale;

        return fontSize;
    }

    public static float suitableFontScale(Context context, float baseScreenWidthSize) {

        // this method based on Nexus 5 device
        // 1080 x 1776 pixels
        // screen width in inches = 2.25
        // screen height in inches = 3.7
        // screen size = 4.330415725176914 inches (diagonal)

        float baseWidthSize = 2.25f;    // the width in inches of Nexus 5
        if (baseScreenWidthSize > 0) {
            baseWidthSize = baseScreenWidthSize;
        }

        float wInches = screenWidthInches(context);
        float hInches = screenHeightInches(context);
        float fontScale = wInches / baseWidthSize;
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fontScale = hInches / baseWidthSize;
        }

        return fontScale;
    }

    public static void adjustFontScale( Context context, Configuration configuration, float scale) {
        configuration.fontScale = scale;
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        displayMetrics.scaledDensity = configuration.fontScale * displayMetrics.density;
        context.getResources().updateConfiguration(configuration, displayMetrics);
    }

    public static float screenWidthInches(Context context) {
        // Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        float wInches = (float)(displayMetrics.widthPixels) / (float)(displayMetrics.xdpi);

        return wInches;
    }

    public static float screenHeightInches(Context context) {
        // Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        float hInches = (float)(displayMetrics.heightPixels) / (float)(displayMetrics.ydpi);

        return hInches;
    }

    public static float screenSizeInches(Context context) {
        // Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        float wInches = (float)(displayMetrics.widthPixels) / (float)(displayMetrics.xdpi);
        float hInches = (float)(displayMetrics.heightPixels) / (float)(displayMetrics.ydpi);

        float screenDiagonal = (float) (Math.sqrt(Math.pow(wInches, 2) + Math.pow(hInches, 2)) );

        return screenDiagonal;
    }

    public static boolean isTablet(Context context)
    {
        double screenDiagonal = screenSizeInches(context);
        return (screenDiagonal >= 7.0);
    }

    public static void resizeMenuTextSize(Menu menu, float fontScale) {
        if (menu != null) {
            int mSize = menu.size();
            MenuItem mItem;
            SpannableString spanString;
            int sLen;
            for (int i=0; i<mSize; i++) {
                mItem = menu.getItem(i);
                spanString = new SpannableString(mItem.getTitle().toString());
                sLen = spanString.length();
                spanString.setSpan(new RelativeSizeSpan(fontScale), 0, sLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mItem.setTitle(spanString);

                // submenus
                resizeMenuTextSize(mItem.getSubMenu(), fontScale);
            }
        }
    }
}
