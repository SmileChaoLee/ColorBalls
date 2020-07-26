package com.smile.smilelibraries.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Point;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.smilelibraries.Models.ShowToastMessage;

/**
 * Created by chaolee on 2017-10-24.
 */

public class ScreenUtil {

    public static final String TAG = "ScreenUtil";
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
        /*
        Display display =  ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Log.d(TAG, "display.getSize(size) --> size.x = " + size.x);
        Log.d(TAG,"display.getSize(size) --> size.y = " + size.y);
        */

        Point size = new Point();
        // size.x = context.getResources().getSystem().getDisplayMetrics().widthPixels;
        size.x = Resources.getSystem().getDisplayMetrics().widthPixels;
        // size.y = context.getResources().getSystem().getDisplayMetrics().heightPixels;
        size.y = Resources.getSystem().getDisplayMetrics().heightPixels;

        Log.d(TAG, "Resources.getSystem().getDisplayMetrics() --> size.x = " + size.x);
        Log.d(TAG,"Resources.getSystem().getDisplayMetrics() --> size.y = " + size.y);

        return size;
    }

    public static int dpToPixel(Context context, int dp) {
        /*
        // deprecated
        // Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        */

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();

        float density = displayMetrics.density;

        return  (int)((float)dp * density);
    }

    public static int pixelToDp(Context context, int pixel) {
        /*
        // deprecated
        // Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        */

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        float density = displayMetrics.density;
        int dp = 0;
        if (density != 0) {
            dp = (int) ((float) pixel / density);
        }

        return dp;
    }

    public static float getDefaultTextSizeFromTheme(Context context, int fontSize_Type, @Nullable Integer themeId) {
        /*
        // deprecated
        // Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        */

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        TypedArray typedArray;
        int[] attrs = {android.R.attr.textSize}; // retrieve text size from style.xml
        if (themeId == null) {
            typedArray = context.obtainStyledAttributes(attrs);
        } else {
            typedArray = context.obtainStyledAttributes(themeId, attrs);
        }
        float dimension = typedArray.getDimension(0,0);
        Log.d(TAG, "getDefaultTextSizeFromTheme().dimension = " + dimension);

        float density = displayMetrics.density;
        Log.d(TAG, "getDefaultTextSizeFromTheme().density = " + density);

        float defaultTextFontSize;
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

            /*
            // deprecated
            // Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
            Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
            */

            DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
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

    public static float screenWidthInches(Context context) {
        /*
        // deprecated
        // Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        */

        // DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        float widthPixels = (float)(displayMetrics.widthPixels);
        Log.d(TAG, "widthPixels = " + widthPixels);

        Log.d(TAG, "displayMetrics.density = " + displayMetrics.density);
        Log.d(TAG, "displayMetrics.densityDpi = " + displayMetrics.densityDpi);

        float xdpi = (float)displayMetrics.xdpi;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            xdpi = (float)displayMetrics.densityDpi;
            Log.d(TAG, "xdpi ( =  displayMetrics.densityDpi ) = " + xdpi);
        } else {
            xdpi = (float)displayMetrics.xdpi;
            Log.d(TAG, "xdpi ( =  displayMetrics.xdpi ) = " + xdpi);
        }

        return widthPixels / xdpi;
    }

    public static float screenHeightInches(Context context) {
        /*
        // deprecated
        // Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        */

        // DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        float heightPixels = (float)displayMetrics.heightPixels;
        Log.d(TAG, "heightPixels = " + heightPixels);

        Log.d(TAG, "displayMetrics.density = " + displayMetrics.density);
        Log.d(TAG, "displayMetrics.densityDpi = " + displayMetrics.densityDpi);

        float ydpi;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ydpi = (float) displayMetrics.densityDpi;
            Log.d(TAG, "ydpi (= displayMetrics.densityDpi )  = " + ydpi);
        } else {
            ydpi = (float) displayMetrics.ydpi;
            Log.d(TAG, "ydpi (= displayMetrics.ydpi )  = " + ydpi);
        }

        return heightPixels / ydpi;
    }

    public static float screenSizeInches(Context context) {

        float wInches = screenWidthInches(context);
        Log.d(TAG, "wInches = " + wInches);

        float hInches = screenHeightInches(context);
        Log.d(TAG, "hInches = " + hInches);

        float screenDiagonal = (float) (Math.sqrt( Math.pow(wInches, 2) + Math.pow(hInches, 2 )) );

        Log.d(TAG, "screenDiagonal Size = " + screenDiagonal);

        return screenDiagonal;
    }

    public static boolean isTablet(Context context) {
        double screenDiagonal = screenSizeInches(context);
        return (screenDiagonal >= 7.0);
    }

    public static void resizeMenuTextIconSize(Context wrapper, Menu menu, float fontScale) {
        if (wrapper == null) {
            return;
        }
        if (menu == null) {
            return;
        }

        float defaultMenuTextSize = getDefaultTextSizeFromTheme(wrapper, FontSize_Pixel_Type, null);
        resizeMenuTextIconSize0(wrapper, menu, defaultMenuTextSize, fontScale);
    }

    public static void buildActionViewClassMenu(final Activity activity, final Context wrapper, final Menu menu, final float fontScale, final int fontSize_Type) {

        if (menu == null) {
            return;
        }

        float defaultMenuTextSize = getDefaultTextSizeFromTheme(wrapper, fontSize_Type, null);
        int menuSize = menu.size(); // for the main menu that is always showing

        for (int i = 0; i < menuSize; i++) {
            final MenuItem mItem = menu.getItem(i);

            View view = mItem.getActionView();
            if (view != null) {
                TextView mItemTextView = (TextView) view;
                // mItemTextView.setPadding(0, 0, 10, 0);  // has been set in styles.xml
                mItemTextView.setText(mItem.getTitle());
                resizeTextSize(mItemTextView, defaultMenuTextSize * fontScale, fontSize_Type);
                mItemTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mItem.hasSubMenu()) {
                            Menu subMenu = mItem.getSubMenu();
                            buildMenuInPopupMenuByView(activity, wrapper, subMenu, view, fontScale);
                        } else {
                            activity.onOptionsItemSelected(mItem);
                        }
                    }
                });
            } else {
                SpannableString spanString = new SpannableString(mItem.getTitle().toString());
                int sLen = spanString.length();
                spanString.setSpan(new RelativeSizeSpan(fontScale), 0, sLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mItem.setTitle(spanString);
                if (mItem.hasSubMenu()) {
                    Menu subMenu = mItem.getSubMenu();
                    resizeMenuTextIconSize(wrapper, subMenu, fontScale);
                }
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
        // getView() is deprected in API level 30
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            // resize the font size of toast when under API level 30
            try {
                ViewGroup toastView = (ViewGroup) toast.getView();
                TextView messageTextView = (TextView) toastView.getChildAt(0);
                resizeTextSize(messageTextView, textFontSize, fontSize_Type);
                toast.show();
                Log.d(TAG, "toast.show()");
            } catch (Exception e) {
                Log.d(TAG, "ScreenUtil.showToast() exception occurred");
                e.printStackTrace();
            }

        } else {
            if (context != null) {
                Activity activity = (Activity)context;
                int duration = 2000;    // 2 seconds for Toast.LENGTH_SHORT
                if (showPeriod == Toast.LENGTH_LONG) {
                    duration = 3500;    // 3.5 seconds
                }
                ShowToastMessage.showToast(activity, content, textFontSize, fontSize_Type, duration);
                Log.d(TAG, "ShowToastMessage.showToast()");
            }
        }
    }

    public static void freezeScreenRotation(Activity activity) {
        if (Build.VERSION.SDK_INT >= 18) {
            Log.d(TAG, "SDK version is at least 18, using SCREEN_ORIENTATION_LOCKED");
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        } else {
            int windowOrientation = activity.getDisplay().getRotation();
            boolean isLandscapeDefault = isDeviceDefaultOrientationLandscape(activity);
            Log.d(TAG, "isLandscapeDefault: " + isLandscapeDefault);
            switch (windowOrientation) {
                case Surface.ROTATION_0:
                    if (isLandscapeDefault) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    } else {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    break;
                case Surface.ROTATION_180:
                    if (isLandscapeDefault) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    } else {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    }
                    break;
                case Surface.ROTATION_270:
                    if (isLandscapeDefault) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    } else {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    }
                    break;
                case Surface.ROTATION_90:
                    if (isLandscapeDefault) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    } else {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                    break;
            }
        }
    }

    public static void unfreezeScreenRotation(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    public static boolean isDeviceDefaultOrientationLandscape(Activity activity) {
        Configuration config = activity.getResources().getConfiguration();
        int rotation = activity.getDisplay().getRotation();
        boolean defaultLandscapeAndIsInLandscape = (rotation == Surface.ROTATION_0 ||
                rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE;
        boolean defaultLandscapeAndIsInPortrait = (rotation == Surface.ROTATION_90 ||
                rotation == Surface.ROTATION_270) &&
                config.orientation == Configuration.ORIENTATION_PORTRAIT;

        return defaultLandscapeAndIsInLandscape || defaultLandscapeAndIsInPortrait;
    }

    // private methods

    private static void resizeMenuTextIconSize0(Context context, Menu menu, float defaultMenuTextSize, float fontScale) {
        if (menu == null) {
            return;
        }

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
            Drawable icon = mItem.getIcon();
            if (icon != null) {
                int iconSize = (int)(defaultMenuTextSize * fontScale);
                Bitmap tempBitmap = ((BitmapDrawable) icon).getBitmap();
                Drawable iconDrawable = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(tempBitmap, iconSize, iconSize, true));
                mItem.setIcon(iconDrawable);
            }
            // submenus
            resizeMenuTextIconSize0(context, mItem.getSubMenu(), defaultMenuTextSize, fontScale);
        }
    }
    // end of private methods
}
