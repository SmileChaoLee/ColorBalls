package com.smile.colorballs;

// import android.app.AlertDialog;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.purplebrain.adbuddiz.sdk.AdBuddiz;
// import com.purplebrain.adbuddiz.sdk.AdBuddizRewardedVideoDelegate;
// import com.purplebrain.adbuddiz.sdk.AdBuddizRewardedVideoError;
// import com.smile.dao.ScoreMySQL; // removed on 2017-10-18
import com.smile.dao.ScoreSQLite;
import com.smile.draw.ImageDraw;
import com.smile.model.GridData;
import com.smile.utility.ScreenUtl;
import com.smile.utility.SoundUtl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static android.view.View.OnClickListener;

// public class MyActivity extends ActionBarActivity { // ActionBarActivity is deprecated
public class MyActivity extends AppCompatActivity {

    private int screenWidth = 0;
    private int screenHeight = 0;

    // private FrameLayout frameLayout0 = null; // removed on 2017-10-21

    private TextView highestScoreView = null;
    private TextView currentScoreView = null;

    private ImageView nextBallsView = null;
    private ImageDraw nextBallsImageDraw = null;
    private int nextBallsViewWidth = 0;
    private int nextBallsViewHeight = 0;
    private int nextBallsRow = 1;
    private int nextBallsNumber = 4;
    private int nextBallsViewIdStart = 100;
    private int insideColor0 = 0xFFA4FF13;
    private int lineColor0 = 0xFFFF1627;

    private ImageView gridCellsView = null;
    private GridLayout gridCellsLayout = null;
    private int gridWidth = 760;
    private int gridHeight = 800;
    private int rowCounts = 9;
    private int colCounts = 9;
    private int MINB = 3, MAXB = 4;
    private int minBalls = MINB, maxBalls = MINB;
    MenuItem registerMenuItemEasy = null;
    MenuItem registerMenuItemDifficult = null;

    private int highestScore = 0;
    private int currentScore = 0;
    private int undoScore = 0;

    private GridData gridData;
    private ScoreSQLite scoreSQLite = null;
    // private ScoreMySQL scoreMySQL = null;    // removed on 2017-10-18

    private Runnable bouncyRunnable = null;
    private Handler bouncyHandler = null;

    private boolean[] threadCompleted =  {true,true,true,true,true,true,true,true,true,true};

    // private int autoRotate = 1;

    private int indexI = -1, indexJ = -1;   // the array index that the ball has been selected
    private int status = 0; //  no cell selected
    //  one cell with a ball selected

    private boolean undoEnable = false;

    private String yesStr = new String("");
    private String noStr = new String("");
    private String nameStr = new String("");
    private String submitStr = new String("");
    private String cancelStr = new String("");
    private String gameOverStr = new String("");
    private String undoStr = new String("");
    private String historyStr = new String("");

    final private String packageName = new String("package:com.smile.colorballs");

    private FragmentManager fmManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my);

        // removed on 2017-10-24
        // autoRotate = android.provider.Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        // the following is very important for JDBC connector
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        fmManager = getFragmentManager();

        // string constant
        yesStr = getResources().getString(R.string.yesStr);
        noStr = getResources().getString(R.string.noStr);
        nameStr = getResources().getString(R.string.nameStr);
        submitStr = getResources().getString(R.string.submitStr);
        cancelStr = getResources().getString(R.string.cancelStr);
        gameOverStr = getResources().getString(R.string.gameOverStr);
        undoStr = getResources().getString(R.string.undoStr);
        historyStr = getResources().getString(R.string.historyStr);

        scoreSQLite = new ScoreSQLite(MyActivity.this);
        highestScore = scoreSQLite.readHighestScore();

        // scoreMySQL = new ScoreMySQL(MyActivity.this);    // removed on 2017-10-18

        indexI = -1;
        indexJ = -1;
        status = 0;

        // frameLayout0 = (FrameLayout) findViewById(R.id.frameLayout0);    // removed on 2017-10-21
        Point size = new Point();

        // Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        // Display display = getWindowManager().getDefaultDisplay();
        // display.getSize(size);
        ScreenUtl.getScreenSize(MyActivity.this,size);
        screenWidth = size.x;
        screenHeight = size.y;

        // following are for testing

        int statusBarHeight = ScreenUtl.getStatusBarHeight(MyActivity.this);
        int actionBarHeight = ScreenUtl.getActionBarHeight(MyActivity.this);

        // keep navigation bar
        screenHeight = screenHeight - statusBarHeight - actionBarHeight;

        gridWidth = screenWidth;
        gridHeight = gridWidth;

        int minButtonHeight = 30;
        float nextBallPart = 2.0f / 3.0f;  // 3.0f / 4.0f
        float scoreTextPart = 1.0f - nextBallPart;

        nextBallsViewWidth = (int) (((float) gridWidth * nextBallPart));
        nextBallsViewHeight = (int) ((float) nextBallsViewWidth / 4.0);

        // int buttonAreaHeight = 60;
        int buttonAreaHeight = screenHeight - gridHeight - nextBallsViewHeight;
        if (buttonAreaHeight < minButtonHeight) {
            // if the height of button area is less than 30 pixels
            // then set it to 30
            buttonAreaHeight = minButtonHeight;
            // shrink the height of next balls view
            nextBallsViewHeight = screenHeight - gridHeight - buttonAreaHeight;
        }
        if (nextBallsViewHeight < minButtonHeight) {
            // min height is 30
            // adjust the gridHeight
            gridHeight = gridHeight - (minButtonHeight - nextBallsViewHeight);
            nextBallsViewHeight = minButtonHeight;
        }

        /*  removed on 2017-10-24
        int tempHeight = gridHeight + nextBallsViewHeight + buttonAreaHeight;
        if (tempHeight > screenHeight) {
            // width of used screen is greater than height of used screen
            gridHeight = screenHeight - nextBallsViewHeight - buttonAreaHeight;
            gridWidth = gridHeight;
            nextBallsViewWidth = (int) (((float) gridWidth * nextBallPart));
            nextBallsViewHeight = nextBallsViewWidth / 4;
        }
        */

        if (gridHeight < (10 * 9)) {
            return;
        }

        highestScoreView = (TextView) findViewById(R.id.highestScoreTextView);

        ViewGroup.LayoutParams textLP = highestScoreView.getLayoutParams();
        highestScoreView.setWidth((int) ((float) gridWidth * scoreTextPart));
        highestScoreView.setHeight(nextBallsViewHeight / 2);

        currentScoreView = (TextView) findViewById(R.id.currentScoreTextView);
        currentScoreView.setWidth((int) ((float) gridWidth * scoreTextPart));
        currentScoreView.setHeight(nextBallsViewHeight / 2);

        nextBallsView = (ImageView) findViewById(R.id.nextBallsView);
        ViewGroup.LayoutParams nextBallsViewLp = nextBallsView.getLayoutParams();
        nextBallsViewLp.width = (nextBallsViewWidth / nextBallsNumber) * nextBallsNumber;
        nextBallsViewLp.height = nextBallsViewHeight;

        GridLayout nextBallsLayout = (GridLayout) findViewById(R.id.nextBallsLayout);
        ViewGroup.LayoutParams nextBallsLp = nextBallsLayout.getLayoutParams();
        nextBallsLp.width = nextBallsViewLp.width;
        nextBallsLp.height = nextBallsViewLp.height;
        nextBallsLayout.setRowCount(nextBallsRow);
        nextBallsLayout.setColumnCount(nextBallsNumber);

        LinearLayout.LayoutParams oneNextBallLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        oneNextBallLp.width = nextBallsLp.width / nextBallsNumber;
        oneNextBallLp.height = nextBallsLp.height / nextBallsRow;
        oneNextBallLp.gravity = Gravity.CENTER;

        ImageView imageView = null;

        nextBallsImageDraw = new ImageDraw(nextBallsView, nextBallsRow, nextBallsNumber, insideColor0, lineColor0);
        nextBallsImageDraw.drawBase();

        for (int i = 0; i < nextBallsRow; i++) {
            for (int j = 0; j < nextBallsNumber; j++) {
                imageView = new ImageView(this);
                imageView.setId(nextBallsViewIdStart + (nextBallsNumber * i + j));
                imageView.setClickable(false);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                nextBallsLayout.addView(imageView, oneNextBallLp);
            }
        }

        highestScoreView.setText(String.format("%9d", highestScore));
        currentScoreView.setText(String.format("%9d", currentScore));

        int cellWidth = gridWidth / colCounts;
        int cellHeight = gridHeight / rowCounts;

        gridData = new GridData(rowCounts, colCounts, minBalls, maxBalls);

        gridCellsView = (ImageView) findViewById(R.id.gridCellsView);
        ViewGroup.LayoutParams gridCellsViewLp = gridCellsView.getLayoutParams();
        gridCellsViewLp.width = cellWidth * colCounts;      // gridWidth
        gridCellsViewLp.height = cellHeight * rowCounts;    // gridHeight

        gridCellsLayout = (GridLayout) findViewById(R.id.gridCellsLayout);
        ViewGroup.LayoutParams gridCellsLp = gridCellsLayout.getLayoutParams();
        gridCellsLp.width = gridCellsViewLp.width;
        gridCellsLp.height = gridCellsViewLp.height;
        gridCellsLayout.setRowCount(rowCounts);
        gridCellsLayout.setColumnCount(colCounts);

        LinearLayout.LayoutParams oneBallLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        oneBallLp.width = cellWidth;
        oneBallLp.height = cellHeight;
        oneBallLp.gravity = Gravity.CENTER;

        // set LinearLayout for buttons
        LinearLayout bLayout = (LinearLayout) findViewById(R.id.linearlayout_for_buttons_in_mainactivity);
        ViewGroup.LayoutParams buttonsLp = bLayout.getLayoutParams();
        buttonsLp.width = gridWidth;
        int areaHeight = buttonAreaHeight - 20;   // not too close to navigation bar
        buttonsLp.height = areaHeight;

        if (areaHeight > 200) {
            // if the height of this area more than 150 * 2 pixels
            // then divide it to tw part, 1st part for buttons, 2nd part for ads or others
            buttonsLp.height = 200;
            System.out.println("the height of button area is 200 pixels.");
        }

        Button undoButton = (Button) findViewById(R.id.undoButton);
        undoButton.setTextColor(Color.RED);
        undoButton.setTypeface(undoButton.getTypeface(),Typeface.BOLD_ITALIC);
        undoButton.setTextSize(20);
        undoButton.setText(undoStr);
        undoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                undoTheLast();
                AdBuddiz.showAd(MyActivity.this);   // added on 2017-11-11
            }
        });

        Button historyButton = (Button) findViewById(R.id.historyButton);
        historyButton.setTextColor(Color.RED);
        historyButton.setTypeface(historyButton.getTypeface(), Typeface.BOLD_ITALIC);
        historyButton.setTextSize(20);
        historyButton.setText(historyStr);
        historyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                new StartHistoryScore().execute();
            }
        });

        // removed globalButton on 2017-10-18 at 00:54 am by Chao Lee
        /*
        Button globalButton = (Button) findViewById(R.id.globalButton);
        globalButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                threadCompleted[7] = false;

                new startGlobalRanking().execute();

                threadCompleted[7] = true;
            }
        });
        */


        for (int i = 0; i < rowCounts; i++) {
            for (int j = 0; j < colCounts; j++) {
                imageView = new ImageView(this);
                imageView.setId((i * colCounts + j));
                imageView.setClickable(true);

                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                imageView.setBackgroundResource(R.drawable.boximage);
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(completedAll()) {
                            doDrawBallsAndCheckListener(v);
                        }
                    }
                });
                gridCellsLayout.addView(imageView, oneBallLp);
            }
        }

        displayGridDataNextCells();

        // for AdBuddiz ads
        AdBuddiz.setPublisherKey("57c7153c-35dd-488a-beaa-3cae8b3ab668");
        AdBuddiz.cacheAds(this); // this = current Activity
        // AdBuddiz.RewardedVideo.fetch(this); // this = current Activity
    }

    private boolean completedAll() {
        for (int i=0 ; i <threadCompleted.length;i++) {
            if (!threadCompleted[i]) {
                return false;
            }
        }
        return true;
    }

    /*
    class delegate implements AdBuddizRewardedVideoDelegate {
        @Override
        public void didComplete() {
        }

        // optional
        public void didFetch() {       // a video is ready to be displayed
        }
        public void didFail(AdBuddizRewardedVideoError error) { // SDK was unable to fetch or show a video
        }
        public void didNotComplete() { // an error happened during video playback
        }
    }
    */

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        // removed on 2017-10-24
        /*
        Context ctx = MyActivity.this;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (android.os.Build.VERSION.SDK_INT >=23) {
            if (!Settings.System.canWrite(this)) {
                final Object lock = new Object();
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + ctx.getPackageName()));
                startActivity(intent);
            }
        }

        try {
            android.provider.Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, autoRotate);
            System.out.println("onResume --> Succeeded to set screen rotation setting.");
        } catch (Exception e) {
            System.out.println("onResume --> Failed to set screen rotation setting.");
            e.printStackTrace();
        }
        */

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // removeed on 2017-10-24
        /*
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                if (Settings.System.canWrite(MyActivity.this)) {
                    android.provider.Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, autoRotate);
                }
            } else {
                // under Api Level 23, no need to ask permission
                android.provider.Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, autoRotate);
            }
            System.out.println("onDestroy()--> Succeeded to set screen rotation setting.");
        } catch (Exception e) {
            System.out.println("onDestroy()--> Failed to set screen rotation setting.");
            e.printStackTrace();
        }
        */
    }

    @Override
    public void onBackPressed() {
        // capture the event of back button when it is pressed
        // change back button behavior
        finish();
    }

    private void setScreenOrientation(int ot) {
        switch (ot) {
            case Configuration.ORIENTATION_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        MenuItem registerMenuItemEndGame = menu.findItem(R.id.quitGame);
        registerMenuItemEndGame.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        MenuItem registerMenuItemNewGame = menu.findItem(R.id.newGame);
        registerMenuItemNewGame.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        MenuItem registerMenuItemOption = menu.findItem(R.id.option);
        registerMenuItemOption.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        registerMenuItemEasy = menu.findItem(R.id.easyLevel);
        registerMenuItemDifficult = menu.findItem(R.id.difficultLevel);

        if (maxBalls == MAXB) {
            registerMenuItemDifficult.setChecked(true);
            registerMenuItemEasy.setChecked(false);
        }
        else {
            registerMenuItemDifficult.setChecked(false);
            registerMenuItemEasy.setChecked(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.quitGame) {

            // removed on 2017-10-24
            // Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, autoRotate);

            recordScore(0);   //   from   END PROGRAM

            AdBuddiz.showAd(this);
            // AdBuddiz.RewardedVideo.show(this); // this = current Activity

            return true;
        }
        if (id == R.id.newGame) {
            newGame();
            AdBuddiz.showAd(this);
            return true;
        }
        if (id == R.id.easyLevel) {
            minBalls = MINB;
            maxBalls = MINB;
            item.setChecked(true);
            registerMenuItemDifficult.setChecked(false);
            gridData.setMinBallsOneTime(minBalls);
            gridData.setMaxBallsOneTime(maxBalls);
            displayNextColorBalls();

            AdBuddiz.showAd(this);
            // AdBuddiz.showAd(this);
            // AdBuddiz.RewardedVideo.show(this); // this = current Activity

            return true;
        }
        if (id == R.id.difficultLevel) {
            minBalls = MINB;
            maxBalls = MAXB;
            item.setChecked(true);
            registerMenuItemEasy.setChecked(false);
            gridData.setMinBallsOneTime(minBalls);
            gridData.setMaxBallsOneTime(maxBalls);
            displayNextColorBalls();

            AdBuddiz.showAd(this);
            // AdBuddiz.showAd(this);
            // AdBuddiz.RewardedVideo.show(this); // this = current Activity

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void displayGridDataNextCells() {

        gridData.randCells();

        ImageView imageView = null;
        // ImageDraw imageDraw = null;  // removed on 2018-01-02

        int numOneTime = gridData.getBallNumOneTime();

        int[] indexi = gridData.getNextCellIndexI();
        int[] indexj = gridData.getNextCellIndexJ();

        int id, n1, n2;
        for (int i = 0; i < numOneTime; i++) {
            n1 = indexi[i];
            n2 = indexj[i];
            if ((n1 >= 0) && (n2 >= 0)) {
                id = n1 * colCounts + n2;
                imageView = (ImageView) findViewById(id);
                drawBall(imageView,gridData.getCellValue(n1, n2));
            }
        }

        HashSet<Point> hashPoint = new HashSet<Point>();
        boolean hasMoreFive = false;
        for (int i = 0; i < numOneTime; i++) {
            n1 = indexi[i];
            n2 = indexj[i];
            if ((n1 >= 0) && (n2 >= 0)) {
                if (gridData.getCellValue(n1, n2) != 0) {
                    //   has  color in this cell
                    if (gridData.check_moreFive(n1, n2) == 1) {
                        hasMoreFive = true;
                        for (Point item : gridData.getLight_line()) {
                            if (!hashPoint.contains(item)) {
                                // does not contains then add into
                                hashPoint.add(item);
                            }
                        }
                    }
                }
            }
        }

        if (hasMoreFive) {
            threadCompleted[1] = false;
            CalculateScore calculateScore = new CalculateScore();
            calculateScore.execute(hashPoint);
        } else {
            // check if game over
            if (gridData.getGameOver()) {
                //  game over
                final ModalDialogFragment mDialogFragment = new ModalDialogFragment(new ModalDialogFragment.DialogButtonListener() {
                    @Override
                    public void button1OnClick(ModalDialogFragment dialogFragment) {
                        dialogFragment.dismiss();
                        recordScore(0);   //   Ending the game
                        AdBuddiz.showAd(MyActivity.this);
                        // AdBuddiz.RewardedVideo.show(MyActivity.this); // this = current Activity
                    }

                    @Override
                    public void button2OnClick(ModalDialogFragment dialogFragment) {
                        dialogFragment.dismiss();
                        newGame();
                        AdBuddiz.showAd(MyActivity.this);
                        // AdBuddiz.RewardedVideo.show(MyActivity.this); // this = current Activity
                    }
                });
                Bundle args = new Bundle();
                args.putString("textContent", gameOverStr);
                args.putInt("color", Color.BLUE);
                args.putInt("width", 300);
                args.putInt("height", 200);
                args.putInt("numButtons", 2);
                mDialogFragment.setArguments(args);
                mDialogFragment.showDialogFragment(MyActivity.this);

                // removed on 2018-01-02 for testing
                /*
                final TextView tv = new TextView(MyActivity.this);
                tv.setTextSize(40);
                tv.setTypeface(Typeface.DEFAULT);
                tv.setTextColor(Color.BLUE);
                tv.setText(gameOverStr);
                tv.setGravity(Gravity.CENTER);
                AlertDialog alertDialog = new AlertDialog.Builder(MyActivity.this).create();
                alertDialog.setTitle(null);
                alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                alertDialog.setCancelable(false);
                alertDialog.setView(tv);
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, noStr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        recordScore(0);   //   Ending the game

                        AdBuddiz.showAd(MyActivity.this);
                        // AdBuddiz.RewardedVideo.show(MyActivity.this); // this = current Activity
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, yesStr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        newGame();

                        AdBuddiz.showAd(MyActivity.this);
                        // AdBuddiz.RewardedVideo.show(MyActivity.this); // this = current Activity
                    }
                });

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        setDialogStyle(dialog);
                    }
                });

                alertDialog.show();
                */
            }
        }

        displayNextColorBalls();
    }

    private void displayNextColorBalls() {

        ImageView imageView = null;
        ImageDraw imageDraw = null;

        gridData.randColors();  //   next  balls
        //   display the balls on the nextBallsView
        int numOneTime = gridData.getBallNumOneTime();
        for (int i = 0 ; i < numOneTime ; i++) {
            imageView = (ImageView) findViewById(nextBallsViewIdStart + i);
            imageDraw = new ImageDraw(imageView, 1, 1 , insideColor0 , lineColor0);
            imageDraw.drawBall(gridData.getNextBalls()[i]);
        }
        for (int i = numOneTime ; i<nextBallsNumber ; i++) {
            imageView = (ImageView) findViewById(nextBallsViewIdStart + i);
            imageDraw = new ImageDraw(imageView, 1, 1 , insideColor0 , lineColor0);
            // imageDraw.clearCellImage();
            imageDraw.circleInsideSquare(insideColor0);
        }
    }

    private void undoTheLast() {

        if (!undoEnable) {
            return;
        }

        ImageView imageView = null;
        ImageDraw imageDraw = null;
        int id, n1, n2 , color;

        // completedPath = false;

        gridData.undoTheLast();

        int numOneTime = gridData.getBallNumOneTime();
        for (int i = 0; i < numOneTime; i++) {
            imageView = (ImageView) findViewById(nextBallsViewIdStart + i);
            imageDraw = new ImageDraw(imageView, 1, 1, insideColor0, lineColor0);
            imageDraw.drawBall(gridData.getNextBalls()[i]);
        }
        for (int i = numOneTime; i < nextBallsNumber; i++) {
            imageView = (ImageView) findViewById(nextBallsViewIdStart + i);
            imageDraw = new ImageDraw(imageView, 1, 1, insideColor0, lineColor0);
            imageDraw.circleInsideSquare(insideColor0);
        }

        // restore the screen
        for (int i = 0; i<rowCounts ; i++) {
            for (int j=0 ; j<colCounts ; j++ ) {
                id = i * rowCounts + j;
                imageView = (ImageView)findViewById(id);
                color = gridData.getCellValue(i,j);
                if (color == 0) {
                    imageView.setImageResource(R.drawable.boximage);
                } else {
                    drawBall(imageView , color);
                }
            }
        }

        status = 0;
        indexI = -1;
        indexJ = -1;

        /*  removed on 2017-10-21
        undoindexI = -1;
        undoindexJ = -1;
        */

        currentScore = undoScore;
        currentScoreView.setText( String.format("%9d",currentScore));

        // completedPath = true;
        undoEnable = false;
    }

    public void clearCell(int i, int j) {
        int id = i * colCounts + j;
        ImageView imageView = (ImageView) findViewById(id);
        imageView.setImageResource(R.drawable.boximage);
        gridData.setCellValue(i, j, 0);
    }

    public void doDrawBallsAndCheckListener(View v) {

        int i, j, id;
        id = v.getId();
        i = id / rowCounts;
        j = id % rowCounts;
        ImageView imageView = null;
        ImageDraw imageDraw = null;
        if (status == 0) {
            if (gridData.getCellValue(i, j) != 0) {
                if ((indexI == -1) && (indexJ == -1)) {
                    status = 1;
                    drawBouncyBall((ImageView) v, gridData.getCellValue(i, j));
                    indexI = i;
                    indexJ = j;
                }
            }
        } else {
            // cancel the timer
            if (gridData.getCellValue(i, j) == 0) {
                //   blank cell
                if ((indexI >= 0) && (indexJ >= 0)) {
                    if (gridData.moveCellToCell(new Point(indexI, indexJ), new Point(i, j))) {
                        // cancel the timer
                        status = 0;
                        cancelBouncyTimer();
                        int color = gridData.getCellValue(indexI, indexJ);
                        gridData.setCellValue(i, j, color);
                        clearCell(indexI, indexJ);

                        indexI = -1;
                        indexJ = -1;

                        drawBallAlongPath(i,j,color);

                        undoEnable = true;
                    } else {
                        //    make a sound
                        // removed for testing on 2017-10-18. the following sound maker would crash the app
                        /*
                        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
                        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
                        tone.release();
                        */

                        SoundUtl.playUhOhSound(MyActivity.this);
                        // SoundUtl.playTone();
                        // SoundUtl.playTone1();
                    }
                }
            } else {
                //  cell is not blank
                if ((indexI >= 0) && (indexJ >= 0)) {
                    status = 0;
                    cancelBouncyTimer();
                    status = 1;
                    imageView = (ImageView) findViewById(indexI * colCounts + indexJ);
                    drawBall(imageView , gridData.getCellValue(indexI, indexJ));
                    drawBouncyBall((ImageView) v, gridData.getCellValue(i, j));
                    indexI = i;
                    indexJ = j;
                }
            }
        }
    }

    private void drawBouncyBall(final ImageView v, final int color) {
        bouncyHandler = new Handler();
        bouncyRunnable = new Runnable() {
            boolean ballYN = false;
            @Override
            public void run() {
                if (status == 1) {
                    if (color != 0) {
                        if (ballYN) {
                            drawBall(v , color);
                        } else {
                            drawOval(v , color);
                        }
                        ballYN = !ballYN;
                        bouncyHandler.postDelayed(this, 200);
                    } else {
                        v.setImageResource(R.drawable.boximage);
                    }
                } else {
                    cancelBouncyTimer();
                }
            }
        };
        bouncyHandler.post(bouncyRunnable);
    }

    private void cancelBouncyTimer() {
        if (bouncyHandler != null) {
            bouncyHandler.removeCallbacksAndMessages(null);
            // bouncyHandler.removeCallbacks(bouncyRunnable);
            bouncyRunnable = null;
            bouncyHandler = null;
        }
        SystemClock.sleep(20);
    }

    // removed at 16:56 on 2017-12-31
    /*
    private void twinkleLineBallsAndClearCell(final List<Point> Light_line,final int arrIndex) {
        if (Light_line.size()<=0) {
            return;
        }

        final int color = gridData.getCellValue(Light_line.get(0).x, Light_line.get(0).y);

        final List<Point> listTemp = new ArrayList<Point>();
        for (Point temp : Light_line) {
            gridData.setCellValue(temp.x , temp.y , 0);  // set no ball in the cells, to prevent
            listTemp.add(temp);                          // used by others before finish this method
        }

        final Handler twinkleHandler = new Handler();
        Runnable twinkleRunnable = new Runnable() {
            boolean ballYN = false;
            int twinkleCountDown = 5;
            boolean twinkleBallYN = false;
            @Override
            public void run() {
                threadCompleted[arrIndex] = false;
                if (twinkleCountDown >= 0) {
                    if (twinkleCountDown > 0) {
                        if (twinkleBallYN) {
                            for (Point item : listTemp) {
                                ImageView v = (ImageView) findViewById(item.x * colCounts + item.y);
                                drawBall(v, color);
                            }
                        } else {
                            for (Point item : listTemp) {
                                ImageView v = (ImageView) findViewById(item.x * colCounts + item.y);
                                drawOval(v, color);
                            }
                        }
                    } else {
                        for (Point item : listTemp) {
                            clearCell(item.x, item.y);
                        }
                    }
                    twinkleHandler.postDelayed(this, 200);
                    twinkleBallYN = !twinkleBallYN;
                    twinkleCountDown--;
                } else {
                    twinkleHandler.removeCallbacksAndMessages(null);
                    threadCompleted[arrIndex] = true;
                }
            }
        };
        twinkleHandler.post(twinkleRunnable);
    }
    */

    private void drawBallAlongPath(final int ii , final int jj,final int color) {
        if (gridData.getPathPoint().size()<=0) {
            return;
        }

        final List<Point> tempList = new ArrayList<Point>(gridData.getPathPoint());
        final Handler drawHandler = new Handler();
        Runnable runnablePath = new Runnable() {
            boolean ballYN = true;
            ImageView imageView = null;
            int countDown = tempList.size()*2 - 1;
            @Override
            public void run() {
                threadCompleted[0] = false;
                // System.out.println("outside run, countDown = " + countDown);
                if (countDown >= 2) {   // eliminate start point
                    int i = (int) (countDown / 2);
                    // System.out.println("inside run, countDown = " + countDown);
                    // System.out.println("inside run, i = " + i);
                    imageView = (ImageView) findViewById(tempList.get(i).x * colCounts + tempList.get(i).y);
                    if (ballYN) {
                        drawBall(imageView, color);
                    } else {
                        imageView.setImageResource(R.drawable.boximage);
                    }
                    ballYN = !ballYN;
                    countDown--;
                    drawHandler.postDelayed(this,20);
                } else {
                    drawHandler.removeCallbacksAndMessages(null);
                    //   do the next
                    threadCompleted[0] = true;
                    doNextAction(ii,jj);
                }
            }
        };
        drawHandler.post(runnablePath);
    }

    private void doNextAction(final int i,final int j) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ImageView v = (ImageView) findViewById(i * colCounts + j);
                drawBall(v, gridData.getCellValue(i, j));
                if (gridData.check_moreFive(i, j) == 1) {
                    //  check if there are more than five balls with same color connected together
                    // int numBalls = gridData.getLight_line().size();
                    // scoreCalculate(numBalls);
                    // twinkleLineBallsAndClearCell(gridData.getLight_line(), 1);
                    HashSet<Point> hashPoint = new HashSet<Point>();
                    for (Point item : gridData.getLight_line()) {
                        hashPoint.add(item);
                    }
                    threadCompleted[1] = false;
                    CalculateScore calculateScore = new CalculateScore();
                    calculateScore.execute(hashPoint);
                } else {
                    // gridData.randCells();    // moved to displayGridDataNextCells() on 2018-01-02
                    displayGridDataNextCells();   // has a problem

                    // moved to displayGridDataNextCells() on 2018-01-02
                    /*
                    if (gridData.getGameOver()) {
                        //  game over
                        final TextView tv = new TextView(MyActivity.this);
                        tv.setTextSize(40);
                        tv.setTypeface(Typeface.DEFAULT);
                        tv.setTextColor(Color.BLUE);
                        tv.setText(gameOverStr);
                        tv.setGravity(Gravity.CENTER);
                        AlertDialog alertDialog = new AlertDialog.Builder(MyActivity.this).create();
                        alertDialog.setTitle(null);
                        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        alertDialog.setCancelable(false);
                        alertDialog.setView(tv);
                        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, noStr, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                recordScore(0);   //   Ending the game

                                AdBuddiz.showAd(MyActivity.this);
                                // AdBuddiz.RewardedVideo.show(MyActivity.this); // this = current Activity
                            }
                        });
                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, yesStr, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                newGame();

                                AdBuddiz.showAd(MyActivity.this);
                                // AdBuddiz.RewardedVideo.show(MyActivity.this); // this = current Activity
                            }
                        });

                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                setDialogStyle(dialog);
                            }
                        });

                        alertDialog.show();
                    }
                    */

                }
            }
        });
    }

    private int scoreCalculate(int numBalls) {
        int minScore = 5;
        int score = 0;
        if (numBalls <= minScore) {
            score = minScore;
        } else {
            score = 0;
            int rate  = 1;
            for (int i=1 ; i<=Math.abs(numBalls-minScore) ; i++) {
                rate = rate * 2;
                score = score + i*rate ;
            }
            score = score + minScore;
        }

        if (maxBalls == MAXB) {
            // difficult level
            score = score * 2;   // double of easy level
        }

        // undoScore = currentScore;
        // currentScore = currentScore + score;
        // currentScoreView.setText(String.format("%9d", currentScore));

        return score;
    }

    private void newGame() {
        recordScore(1);   //   START A NEW GAME
    }

    private void flushALLandBegin() {

        // added at 00:41 on 2017-10-20
        if (Build.VERSION.SDK_INT >= 11) {
            recreate(); // recreate the original activity
        } else {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }


    private void setDialogStyle(DialogInterface dialog) {
        AlertDialog dlg = (AlertDialog)dialog;
        dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dlg.getWindow().setDimAmount(0.0f); // no dim for background screen
        dlg.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        dlg.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        float fontSize = 20;
        Button nBtn = dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
        nBtn.setTextSize(fontSize);
        nBtn.setTypeface(Typeface.DEFAULT_BOLD);
        nBtn.setTextColor(Color.RED);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)nBtn.getLayoutParams();
        layoutParams.weight = 10;
        nBtn.setLayoutParams(layoutParams);

        Button pBtn = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
        pBtn.setTextSize(fontSize);
        pBtn.setTypeface(Typeface.DEFAULT_BOLD);
        pBtn.setTextColor(Color.rgb(0x00,0x64,0x00));
        pBtn.setLayoutParams(layoutParams);
    }

    private void recordScore(final int entryPoint) {
        final EditText et = new EditText(MyActivity.this);
        et.setTextSize(24);
        // et.setHeight(200);
        et.setTextColor(Color.BLUE);
        et.setBackground(new ColorDrawable(Color.TRANSPARENT));
        et.setHint(nameStr);
        et.setGravity(Gravity.CENTER);
        AlertDialog alertD = new AlertDialog.Builder(MyActivity.this).create();
        alertD.setTitle(null);
        alertD.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertD.setCancelable(false);
        alertD.setView(et);
        alertD.setButton(DialogInterface.BUTTON_NEGATIVE, cancelStr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (entryPoint==0) {
                    //  END PROGRAM
                    finish();
                } else if (entryPoint==1) {
                    //  NEW GAME
                    flushALLandBegin();
                }
            }
        });
        alertD.setButton(DialogInterface.BUTTON_POSITIVE, submitStr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // removed on 2017-10-18 at 01:02 am, no global ranking any more
                /*
                // create a thread for MySQL process to add score into Table
                Thread threadMySQL = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        scoreMySQL.addHighestScore(et.getText().toString(), currentScore);
                    }
                });
                threadMySQL.start();
                */

                scoreSQLite.addScore(et.getText().toString(),currentScore);
                if (entryPoint==0) {
                    finish();
                } else if (entryPoint==1) {
                    //  NEW GAME
                    flushALLandBegin();
                }
            }
        });

        alertD.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                setDialogStyle(dialog);
            }
        });

        alertD.show();
    }

    private void drawBall(ImageView imageView,int color) {
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

    private void drawOval(ImageView imageView,int color) {
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

    private class StartHistoryScore extends AsyncTask<Void,Integer,String[]> {
        private Animation animationText = null;
        private ModalDialogFragment loadingDialog = null;

        public StartHistoryScore() {
            loadingDialog = new ModalDialogFragment();
            Bundle args = new Bundle();
            args.putString("textContent", getResources().getString(R.string.loadScore));
            args.putInt("color", Color.RED);
            args.putInt("width", 300);
            args.putInt("height", 200);
            args.putInt("numButtons", 0);
            loadingDialog.setArguments(args);
        }

        @Override
        protected void onPreExecute() {

            animationText = new AlphaAnimation(0.0f,1.0f);
            animationText.setDuration(300);
            animationText.setStartOffset(0);
            animationText.setRepeatMode(Animation.REVERSE);
            animationText.setRepeatCount(Animation.INFINITE);

            // loadingDialog.show(fmManager,"History");
            loadingDialog.showDialogFragment(MyActivity.this);
        }

        @Override
        protected String[] doInBackground(Void... params) {
            int i = 0;
            publishProgress(i);
            String[] result = scoreSQLite.read10HighestScore();

            // wait for one second
            try { Thread.sleep(1000); } catch (InterruptedException ex) { ex.printStackTrace(); }

            i = 1;
            publishProgress(i);

            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            if (!isCancelled()) {
                TextView textLoad = loadingDialog.getText_shown();
                if (progress[0] == 0) {
                    if (animationText != null) {
                        textLoad.startAnimation(animationText);
                    }
                } else {
                    if (animationText != null) {
                        textLoad.clearAnimation();
                        animationText = null;
                    }
                    textLoad.setText("");
                }
            }
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (!isCancelled()) {

                loadingDialog.dismiss();

                Intent i = new Intent(getApplicationContext(), HistoryActivity.class);
                Bundle extras = new Bundle();
                extras.putStringArray("resultStr", result);
                i.putExtras(extras);
                startActivity(i);
            }
            AdBuddiz.showAd(MyActivity.this);   // added on 2017-10-24
        }
    }

    private class CalculateScore extends AsyncTask<HashSet<Point>,Integer,String[]> {

        private int numBalls = 0;
        private int color = 0;
        private HashSet<Point> hashPoint = null;
        private int score = 0;

        private ModalDialogFragment scoreDialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(HashSet<Point>... params) {

            hashPoint = params[0];
            if (hashPoint == null) {
                return null;
            }

            numBalls = hashPoint.size();

            Iterator<Point> itr = hashPoint.iterator();
            if (itr.hasNext()) {
                Point point = itr.next();
                color = gridData.getCellValue(point.x, point.y);
            }

            threadCompleted[1] = false;

            score = scoreCalculate(numBalls);

            scoreDialog = new ModalDialogFragment();
            Bundle args = new Bundle();
            args.putString("textContent", ""+score);
            args.putInt("color", Color.BLUE);
            args.putInt("width", 300);
            args.putInt("height", 100);
            args.putInt("numButtons", 0);
            scoreDialog.setArguments(args);

            int twinkleCountDown = 5;
            for (int i=1; i<=twinkleCountDown; i++) {
                int md = i % 2; // modulus
                publishProgress(md);
                try { Thread.sleep(100); } catch (InterruptedException ex) { ex.printStackTrace(); }
            }
            publishProgress(2);
            try { Thread.sleep(500); } catch (InterruptedException ex) { ex.printStackTrace(); }

            publishProgress(3);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... status) {
            switch (status[0]) {
                case 0:
                    for (Point item : hashPoint) {
                        ImageView v = (ImageView) findViewById(item.x * colCounts + item.y);
                        drawBall(v, color);
                    }
                    break;
                case 1:
                    for (Point item : hashPoint) {
                        ImageView v = (ImageView) findViewById(item.x * colCounts + item.y);
                        drawOval(v, color);
                    }
                    break;
                case 2:
                    scoreDialog.showDialogFragment(MyActivity.this);
                    break;
                case 3:
                    scoreDialog.dismiss();
                    break;
            }

            return ;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            // clear values of cells
            for (Point item : hashPoint) {
                clearCell(item.x, item.y);
            }

            // update the UI
            undoScore = currentScore;
            currentScore = currentScore + score;
            currentScoreView.setText(String.format("%9d", currentScore));

            threadCompleted[1] = true;
        }
    }
}




