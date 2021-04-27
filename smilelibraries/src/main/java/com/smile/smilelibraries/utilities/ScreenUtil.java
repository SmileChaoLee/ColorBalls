package com.smile.smilelibraries.utilities;

import android.app.Activity;
import android.content.Context;
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
    public static final int FontSize_Sp_Type = 1;
    public static final int FontSize_Dip_Type = 2;
    public static final int FontSize_Pixel_Type = 3;

    public static int getStatusBarHeight(Activity activity) {
        int height = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    public static int getNavigationBarHeight(Activity activity) {
        int height = 0;
        int resourceId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    public static int getActionBarHeight(Activity activity) {
        int height = 0;
        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize,tv, true))
        {
            height = TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        }
        return height;
    }

    public static Point getScreenSize(Activity activity) {
        // exclude the height of Navigation Bar
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int top = activity.getWindowManager().getCurrentWindowMetrics().getBounds().top;
            int left = activity.getWindowManager().getCurrentWindowMetrics().getBounds().left;
            int bottom = activity.getWindowManager().getCurrentWindowMetrics().getBounds().bottom;
            int right = activity.getWindowManager().getCurrentWindowMetrics().getBounds().right;
            Log.d(TAG, "right - left = " + (right - left));
            Log.d(TAG, "bottom - top = " + (bottom - top));
            size.x = right - left;
            size.y = bottom - top;
            if (size.x > size.y) {
                size.x = size.x - getNavigationBarHeight(activity);
            } else {
                size.y = size.y - getNavigationBarHeight(activity);
            }
        } else {
            size.x = activity.getResources().getSystem().getDisplayMetrics().widthPixels;
            // size.x = Resources.getSystem().getDisplayMetrics().widthPixels;
            size.y = activity.getResources().getSystem().getDisplayMetrics().heightPixels;
            // size.y = Resources.getSystem().getDisplayMetrics().heightPixels;
        }

        Log.d(TAG,"size.x = " + size.x);
        Log.d(TAG,"size.y = " + size.y);

        return size;
    }

    public static int dpToPixel(Activity activity, int dp) {
        // DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        DisplayMetrics displayMetrics = activity.getResources().getSystem().getDisplayMetrics();

        float density = displayMetrics.density;

        return  (int)((float)dp * density);
    }

    public static int pixelToDp(Activity activity, int pixel) {
        // DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        DisplayMetrics displayMetrics = activity.getResources().getSystem().getDisplayMetrics();
        float density = displayMetrics.density;
        int dp = 0;
        if (density != 0) {
            dp = (int) ((float) pixel / density);
        }

        return dp;
    }

    public static float getDefaultTextSizeFromTheme(Context context, int fontSize_Type, @Nullable Integer themeId) {
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

    public static float suitableFontSize(Activity activity, float defaultFontSize, int fontSize_Type) {
        return suitableFontSize(activity, defaultFontSize, fontSize_Type, 0);
    }

    public static float suitableFontSize(Activity activity, float defaultFontSize, int fontSize_Type, float baseScreenWidth) {
        float fontSize;
        if (fontSize_Type == FontSize_Pixel_Type) {
            fontSize = 75; // default font size in pixels setting for Nexus 5
        } else {
            fontSize = 24; // default font size in sp setting for Nexus 5
        }
        if (defaultFontSize > 0) {
            fontSize = defaultFontSize;
        }

        float fontScale = suitableFontScale(activity, fontSize_Type, baseScreenWidth);
        fontSize = fontSize * fontScale;

        return fontSize;
    }

    public static float suitableFontScale(Activity activity, int fontSize_Type) {
        return suitableFontScale(activity, fontSize_Type, 0);
    }

    public static float suitableFontScale(Activity activity, int fontSize_Type, float baseScreenWidth) {
        float fontScale;
        if (fontSize_Type == FontSize_Pixel_Type) {
            float baseWidthPixels = 1080.0f;    // the width (pixels) of Nexus 5
            if (baseScreenWidth > 0) {
                baseWidthPixels = baseScreenWidth;
            }
            // DisplayMetrics displayMetrics = activity.getResources().getSystem().getDisplayMetrics();
            // float wPixels = displayMetrics.widthPixels;
            // float hPixels = displayMetrics.heightPixels;
            Point size = getScreenSize(activity);
            float wPixels = size.x;
            float hPixels = size.y;
            fontScale = wPixels / baseWidthPixels;
            if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                fontScale = hPixels / baseWidthPixels;
            }
        } else {
            float baseWidthSize = 2.25f;    // the width in inches of Nexus 5
            if (baseScreenWidth > 0) {
                baseWidthSize = baseScreenWidth;
            }

            float wInches = screenWidthInches(activity);
            float hInches = screenHeightInches(activity);
            fontScale = wInches / baseWidthSize;
            if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                fontScale = hInches / baseWidthSize;
            }
        }

        return fontScale;
    }

    public static float screenWidthInches(Activity activity) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        //float widthPixels = (float)(displayMetrics.widthPixels);

        Point size = getScreenSize(activity);
        float widthPixels = size.x;
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

    public static float screenHeightInches(Activity activity) {

        // DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        // float heightPixels = (float)displayMetrics.heightPixels;

        Point size = getScreenSize(activity);
        float heightPixels = size.y;
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

    public static float screenSizeInches(Activity activity) {

        float wInches = screenWidthInches(activity);
        Log.d(TAG, "wInches = " + wInches);

        float hInches = screenHeightInches(activity);
        Log.d(TAG, "hInches = " + hInches);

        float screenDiagonal = (float) (Math.sqrt( Math.pow(wInches, 2) + Math.pow(hInches, 2 )) );

        Log.d(TAG, "screenDiagonal Size = " + screenDiagonal);

        return screenDiagonal;
    }

    public static boolean isTablet(Activity activity) {
        double screenDiagonal = screenSizeInches(activity);
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

    public static void buildActionViewClassMenu(final Activity activity, final Activity wrapper, final Menu menu, final float fontScale, final int fontSize_Type) {

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

    public static void buildMenuInPopupMenuByView(final Activity activity, final Activity wrapper, Menu menu, View view, final float fontScale) {

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
            case FontSize_Sp_Type:
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textFontSize);
                break;
            default:
                textView.setTextSize(textFontSize);
                break;
        }
    }

    public static void showToast(Activity activity, String content, float textFontSize, int fontSize_Type, int showPeriod) {
        Toast toast = Toast.makeText(activity, content, showPeriod);
        // getView() is deprecated in API level 30
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
            if (activity != null) {
                int duration = 2000;    // 2 seconds for Toast.LENGTH_SHORT
                if (showPeriod == Toast.LENGTH_LONG) {
                    duration = 3500;    // 3.5 seconds
                }
                ShowToastMessage.showToast(activity, content, textFontSize, fontSize_Type, duration);
                Log.d(TAG, "ShowToastMessage.showToast()");
            }
        }
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
