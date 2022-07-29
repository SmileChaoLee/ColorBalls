package com.smile.smilelibraries.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

public class FontAndBitmapUtil {

    public static Bitmap getBitmapFromBitmapWithText(Bitmap orgBitmap, String caption, int textColor) {

        if (orgBitmap == null) {
            return null;
        }

        Bitmap bm = Bitmap.createBitmap(orgBitmap);
        Bitmap.Config bitmapConfig = bm.getConfig();
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        bm = bm.copy(bitmapConfig, true);   // convert to mutable
        Canvas canvas = new Canvas(bm);

        // draw start button
        TextPaint paint = new TextPaint();
        paint.setColor(textColor);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);

        Rect bounds = new Rect();
        float fontSize = 3.0f;
        paint.setTextSize(fontSize);    // fontSize can be any number except 0
        paint.getTextBounds(caption, 0, caption.length(), bounds);
        float fontSize1 = fontSize * canvas.getWidth() / (float) bounds.width();
        float fontSize2 = fontSize * canvas.getHeight() / (float) bounds.height();
        fontSize = Math.min(fontSize1, fontSize2);

        paint.setTextSize(fontSize);

        // for align.CENTER
        Paint.FontMetrics fm = new Paint.FontMetrics();
        paint.getFontMetrics(fm);
        float textWidth = canvas.getWidth();
        float textHeight = canvas.getHeight() - (fm.ascent + fm.descent);
        canvas.drawText(caption, textWidth / 2, textHeight / 2, paint);

        return bm;

    }

    public static Bitmap getBitmapFromResourceWithText(Context context, int resourceId, String caption, int textColor) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        return getBitmapFromBitmapWithText(bm, caption, textColor);
    }

    public static Drawable convertBitmapToDrawable(Context context, Bitmap bm, int width, int height) {
        if (bm == null) {
            return null;
        }
        if (width<=0 || height<=0) {
            throw new IllegalArgumentException("width scale and height scale must be > 0.");
        }

        return new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bm, width, height, true));
    }

    public static Drawable resizeDrawable(Context context, Drawable drawable, int width, int height) {
        if (drawable == null) {
            return null;
        }
        if (width<=0 || height<=0) {
            throw new IllegalArgumentException("width scale and height scale must be > 0.");
        }

        Bitmap bm = ((BitmapDrawable)drawable).getBitmap();

        return new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bm, width, height, true));
    }

    public static Bitmap rotateDrawable(Context context, int drawable, float degree) {
        Bitmap bmpOriginal = BitmapFactory.decodeResource(context.getResources(), drawable);
        Bitmap bmResult = Bitmap.createBitmap(bmpOriginal.getWidth(), bmpOriginal.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(bmResult);
        tempCanvas.rotate(degree, bmpOriginal.getWidth()/2, bmpOriginal.getHeight()/2);
        tempCanvas.drawBitmap(bmpOriginal, 0, 0, null);
        return bmResult;
    }
}
