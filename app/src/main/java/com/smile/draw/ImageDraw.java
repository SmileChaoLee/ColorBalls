package com.smile.draw;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.smile.colorballs.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lee on 9/19/2014.
 */

public class ImageDraw {
    private ImageView imageView;
    private ViewGroup.LayoutParams imageViewLp;
    private Canvas canvas;

    private Paint paint = new Paint();
    private Rect rectOutside = new Rect();
    private Rect rectInside = new Rect();
    private Point gridSquare = new Point();
    private int radius = 0;
    private Point centerOfCircle = new Point();
    private int insideColor = 0xFF88DABA;
    private int lineColor = 0xFFCF12FF;
    private int drawColor = 0;

    private int gridRows = 0 , gridColumns = 0;
    private int gridWidth = 0 , gridHeight = 0;
    private int status = 0;

    TimerTask timerBounceBall = null;
    Timer bouncyBallTimer = null;

    public ImageDraw() {
        super();
        status = 0;
    }

    public ImageDraw(ImageView imageView , int gridRows , int gridColumns , int insideColor , int lineColor) {
        super();

        setImageViewAndLayoutParams(imageView);
        setGridRows(gridRows);
        setGridColumns(gridColumns);

        gridWidth = imageViewLp.width;
        gridHeight = imageViewLp.height;

        Bitmap bitmap = Bitmap.createBitmap(gridWidth , gridHeight , Bitmap.Config.ARGB_8888);
        setCanvas(new Canvas(bitmap));
        imageView.setImageBitmap(bitmap);

        setDrawColor(insideColor);

        setCoordinate();
        setRadiusAndCenter();

        this.insideColor = insideColor;
        this.lineColor = lineColor;

        status = 1;  // construction successfully
    }

    public void setImageViewAndLayoutParams(ImageView imageView) {
        this.imageView   = imageView;
        this.imageViewLp = imageView.getLayoutParams();
    }

    public void setDrawColor(int color) {
        this.drawColor = color;
    }

    public int getDrawColor() {
        return this.drawColor;
    }

    public void setGridRows(int gridRows) {
        this.gridRows = gridRows;
    }

    public void setGridColumns(int gridColumns) {
        this.gridColumns = gridColumns;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public void setCoordinate() {
        int diff=5;
        Rect rect = new Rect(0,0,gridWidth,gridHeight);
        rectOutside = rect;
        rectInside.top = rect.top + diff;
        rectInside.left = rect.left + diff;
        rectInside.right = rect.right - diff;
        rectInside.bottom = rect.bottom - diff;
        gridSquare = new Point(gridWidth/gridColumns , gridHeight/gridRows);
    }

    public void setRadiusAndCenter() {
        int centerDiff = 10;
        radius = Math.min(gridSquare.x,gridSquare.y)/2;
        radius = Math.abs(radius-centerDiff);
        centerOfCircle.x = gridSquare.x/2;
        centerOfCircle.y = gridSquare.y/2;
    }

    public void Rectangle(int lColor , int color) {
        paint.setColor(lColor);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRect(this.rectOutside, paint);

        paint.setColor(color);
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(this.rectInside, paint);
    }

    public void Line(float x1,float y1,float x2,float y2) {
        paint.setColor(lineColor);
        paint.setStrokeWidth(5);
        canvas.drawLine(x1,y1,x2,y2,paint);
    }

    public void circleInsideSquare(int color) {
        paint.setColor(color);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(centerOfCircle.x, centerOfCircle.y , radius , paint);
    }

    public void drawBallByFormula(int color) {
        int red,green,blue,diff=3;
        red = Color.red(color);
        green = Color.green(color);
        blue = Color.blue(color);

        paint.setStrokeWidth(1);
        for (int i=0 ; i<radius ; i++) {
            red = Math.abs(red-diff);
            green = Math.abs(green-diff);
            blue = Math.abs(blue-diff);
            paint.setColor(Color.rgb(red,green,blue));
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(centerOfCircle.x , centerOfCircle.y,i,paint);
        }
    }

    public void drawBall(int color) {
        switch (color) {
            case Color.RED:
                imageView.setImageResource(R.drawable.redball);
                break;
            case Color.GREEN:
                imageView.setImageResource(R.drawable.greenball);
                break;
            case Color.BLUE:
                imageView.setImageResource(R.drawable.blueball);
                break;
            case Color.MAGENTA:
                imageView.setImageResource(R.drawable.magentaball);
                break;
            case Color.YELLOW:
                imageView.setImageResource(R.drawable.yellowball);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_launcher);
                break;
        }
    }

    public void drawOvalByFormula(int color) {
        RectF rectf = new RectF(0,0,0,0);
        int maxLength = Math.max(rectInside.right-rectInside.left , rectInside.bottom-rectInside.top);

        int red,green,blue,diff=3;
        red = Color.red(color);
        green = Color.green(color);
        blue = Color.blue(color);

        paint.setStrokeWidth(1);
        for (int i=0 ; i<maxLength ; i++) {
            red = Math.abs(red-diff);
            green = Math.abs(green-diff);
            blue = Math.abs(blue-diff);

            rectf.left = centerOfCircle.x - i;
            if (rectf.left < rectInside.left) {
                rectf.left = rectInside.left;
            }
            rectf.top = centerOfCircle.y - i;
            if (rectf.top < rectInside.top) {
                rectf.top = rectInside.top;
            }
            rectf.right = centerOfCircle.x + i;
            if (rectf.right > rectInside.right) {
                rectf.right = rectInside.right;
            }
            rectf.bottom = centerOfCircle.y + i;
            if (rectf.bottom > rectInside.bottom) {
                rectf.bottom = rectInside.bottom;
            }

            paint.setColor(Color.rgb(red,green,blue));
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawOval(rectf , paint);
        }
    }

    public void drawOval(int color) {
        switch (color) {
            case Color.RED:
                imageView.setImageResource(R.drawable.redball_o);
                break;
            case Color.GREEN:
                imageView.setImageResource(R.drawable.greenball_o);
                break;
            case Color.BLUE:
                imageView.setImageResource(R.drawable.blueball_o);
                break;
            case Color.MAGENTA:
                imageView.setImageResource(R.drawable.magentaball_o);
                break;
            case Color.YELLOW:
                imageView.setImageResource(R.drawable.yellowball_o);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_launcher);
                break;
        }
    }

    public void drawBase() {

        Rectangle(lineColor,insideColor);
        for (float i=0 ; i<rectInside.right ; i=i+gridSquare.x) {
            Line(i+gridSquare.x,rectInside.top,i+gridSquare.x,rectInside.bottom);
        }

        for (float j=0 ; j<rectInside.bottom ; j=j+gridSquare.y) {
            Line(rectInside.left,j+gridSquare.y,rectInside.right,j+gridSquare.y);
        }

    }

    public  void updateCoordinate() {
        int xinc = 1;
        int yinc = 1;

        if ( (centerOfCircle.x+radius) > rectInside.right) {
            xinc = -xinc;
        } else if (( centerOfCircle.x-radius) < rectInside.left) {
            xinc = -xinc;
        }

        if ( (centerOfCircle.y+radius) > rectInside.bottom) {
            yinc = -yinc;
        } else if (( centerOfCircle.y-radius) < rectInside.top) {
            yinc = -yinc;
        }
    }

    public void clearCellImage() {
        // circleInsideSquare(insideColor);
        imageView.setImageResource(R.drawable.boximage);
    }
}
