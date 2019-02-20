package com.smile.smilepublicclasseslibrary.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by chaolee on 2017-10-24.
 */

public class ScreenUtil {

    public static final int fontSize_Sp_Type = 1;
    public static final int FontSize_Dip_Type = 2;
    public static final int FontSize_Pixel_Type = 3;

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

    public static int dpToPixel(Context context, int dp) {
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        float density = displayMetrics.density;
        int pixels = (int)((float)dp * density);

        return pixels;
    }

    public static int pixelToDp(Context context, int pixel) {
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        float density = displayMetrics.density;
        int dp = 0;
        if (density != 0) {
            dp = (int) ((float) pixel / density);
        }

        return dp;
    }

    public static float getDefaultTextSizeFromTheme(Context context, int fontSize_Type, @Nullable Integer themeId) {
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        TypedArray typedArray;
        int[] attrs = {android.R.attr.textSize}; // retrieve text size from style.xml
        if (themeId == null) {
            typedArray = context.obtainStyledAttributes(attrs);
        } else {
            typedArray = context.obtainStyledAttributes(themeId, attrs);
        }
        float dimension = typedArray.getDimension(0,0);
        System.out.println("getDefaultTextSizeFromTheme().dimension = " + dimension);

        float density = displayMetrics.density;
        System.out.println("getDefaultTextSizeFromTheme().density = " + density);

        float defaultTextFontSize = dimension / density;
        if (fontSize_Type == FontSize_Pixel_Type) {
            defaultTextFontSize = dimension;
        } else {

            defaultTextFontSize = dimension / density;
        }
        typedArray.recycle();

        return defaultTextFontSize;
    }

    public static float suitableFontSize(Context context, float defaultFontSize, int fontSize_Type, float baseScreenWidth) {

        // this method based on Nexus 5 device
        // 1080 x 1776 pixels
        // screen width in inches = 2.25
        // screen height in inches = 3.7
        // screen size = 4.330415725176914 inches (diagonal)

        float fontSize;
        if (fontSize_Type == FontSize_Pixel_Type) {
            fontSize = 75; // default font size in pixels setting for Nexus 5
        } else {
            fontSize = 24; // default font size in sp setting for Nexus 5
        }
        if (defaultFontSize > 0) {
            fontSize = defaultFontSize;
        }

        float fontScale = suitableFontScale(context, fontSize_Type, baseScreenWidth);
        fontSize = fontSize * fontScale;

        return fontSize;
    }

    public static float suitableFontScale(Context context, int fontSize_Type, float baseScreenWidth) {

        // this method based on Nexus 5 device
        // 1080 x 1776 pixels
        // screen width in inches = 2.25
        // screen height in inches = 3.7
        // screen size = 4.330415725176914 inches (diagonal)

        float fontScale;
        if (fontSize_Type == FontSize_Pixel_Type) {
            float baseWidthPixels = 1080.0f;    // the width (pixels) of Nexus 5
            if (baseScreenWidth > 0) {
                baseWidthPixels = baseScreenWidth;
            }

            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);

            float wPixels = displayMetrics.widthPixels;
            float hPixels = displayMetrics.heightPixels;
            fontScale = wPixels / baseWidthPixels;
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                fontScale = hPixels / baseWidthPixels;
            }
        } else {
            float baseWidthSize = 2.25f;    // the width in inches of Nexus 5
            if (baseScreenWidth > 0) {
                baseWidthSize = baseScreenWidth;
            }

            float wInches = screenWidthInches(context);
            float hInches = screenHeightInches(context);
            fontScale = wInches / baseWidthSize;
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                fontScale = hInches / baseWidthSize;
            }
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

    public static void buildActionViewClassMenu(final Activity activity, final Context wrapper, final Menu menu, final float fontScale, final int fontSize_Type) {

        if (menu == null) {
            return;
        }

        float originalMenuTextSize;
        int menuSize = menu.size(); // for the main menu that is always showing

        for (int i = 0; i < menuSize; i++) {
            final MenuItem mItem = menu.getItem(i);

            View view = mItem.getActionView();
            if (view != null) {
                TextView mItemTextView = (TextView) view;
                // mItemTextView.setPadding(0, 0, 10, 0);  // has been set in styles.xml
                mItemTextView.setText(mItem.getTitle());
                originalMenuTextSize = mItemTextView.getTextSize();
                ScreenUtil.resizeTextSize(mItemTextView, originalMenuTextSize * fontScale, fontSize_Type);
                System.out.println("originalMenuTextSize = " + originalMenuTextSize);
                System.out.println("originalMenuTextSize * fontScale = " + originalMenuTextSize * fontScale);
                mItemTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mItem.hasSubMenu()) {
                            Menu subMenu = mItem.getSubMenu();
                            ScreenUtil.buildMenuInPopupMenuByView(activity, wrapper, subMenu, view, fontScale);
                        } else {
                            activity.onOptionsItemSelected(mItem);
                        }
                    }
                });
            }
        }
    }

    public static void buildMenuInPopupMenuByView(final Activity activity, final Context wrapper, Menu menu, View view, final float fontScale) {

        if (menu == null) {
            return;
        }

        PopupMenu popupMenu = new PopupMenu(wrapper, view);

        final int menuSize = menu.size();
        for (int j = 0; j<menuSize; j++) {
            MenuItem menuItem = menu.getItem(j);
            String title = menuItem.getTitle().toString();
            int menuItemId = menuItem.getItemId();
            popupMenu.getMenu().add(1, menuItemId, j, title);

            // resize text font size of popup menu items
            MenuItem popupItem = popupMenu.getMenu().getItem(j);
            SpannableString spanString = new SpannableString(popupItem.getTitle().toString());
            int sLen = spanString.length();
            spanString.setSpan(new RelativeSizeSpan(fontScale), 0, sLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            popupItem.setTitle(spanString);
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return activity.onOptionsItemSelected(menuItem);
            }
        });

        popupMenu.show();
    }

    public static void resizeTextSize(TextView textView, float textFontSize, int fontSize_Type) {
        switch (fontSize_Type) {
            case FontSize_Pixel_Type:
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textFontSize);
                break;
            case FontSize_Dip_Type:
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);
                break;
            case fontSize_Sp_Type:
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textFontSize);
                break;
            default:
                textView.setTextSize(textFontSize);
                break;
        }

    }

    public static void showToast(Context context, String content, float textFontSize, int fontSize_Type, int showPeriod) {
        Toast toast = Toast.makeText(context, content, showPeriod);
        ViewGroup toastView = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) toastView.getChildAt(0);
        ScreenUtil.resizeTextSize(messageTextView, textFontSize, fontSize_Type);
        toast.show();
    }
}
