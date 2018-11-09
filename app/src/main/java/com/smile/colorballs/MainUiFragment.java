package com.smile.colorballs;

import com.smile.smilepublicclasseslibrary.alertdialogfragment.*;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smile.Service.MyGlobalTop10IntentService;
import com.smile.Service.MyTop10ScoresIntentService;
import com.smile.model.GridData;
import com.smile.smilepublicclasseslibrary.player_record_rest.PlayerRecordRest;
import com.smile.smilepublicclasseslibrary.showing_instertitial_ads_utility.ShowingInterstitialAdsUtil;
import com.smile.utility.FontAndBitmapUtil;
import com.smile.utility.ScreenUtil;
import com.smile.utility.SoundUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainUiFragment extends Fragment {

    // public properties
    public static final String MainUiFragmentTag = new String("MainUiFragmentTag");
    public static final int MINB = 3;
    public static final int MAXB = 4;

    // private properties for this color balls game
    private OnFragmentInteractionListener mListener;

    private final String TAG = new String("com.smile.colorballs.MainUiFragment");
    private final String GameOverDialogTag = "GameOverDialogFragment";
    private final int nextBallsViewIdStart = 100;
    private final String savedGameFileName = "saved_game";

    private Context context = null;
    private MyActivity myActivity = null;
    private View uiFragmentView = null;
    private ImageView scoreImageView = null;

    private Runnable bouncyRunnable; // needed to be tested 2018-0609
    private Handler bouncyHandler;   // needed to be tested
    private GridData gridData;

    private TextView currentScoreView;

    private int nextBallsNumber = 4;
    private int rowCounts = 9;
    private int colCounts = 9;
    private int cellWidth = 0;
    private int cellHeight = 0;
    private final boolean[] threadCompleted =  {true,true,true,true,true,true,true,true,true,true};

    private int bouncyBallIndexI = -1, bouncyBallIndexJ = -1;   // the array index that the ball has been selected
    private int bouncingStatus = 0; //  no cell selected
    //  one cell with a ball selected
    private boolean undoEnable = false;
    private int currentScore = 0;
    private int undoScore = 0;
    private boolean isEasyLevel = true;

    private String yesStr = new String("");
    private String noStr = new String("");
    private String nameStr = new String("");
    private String submitStr = new String("");
    private String cancelStr = new String("");
    private String gameOverStr = new String("");
    private String loadingString;
    private String savingGameString;
    private int fontSizeForText = 24;   // default
    private float dialog_widthFactor = 1.0f;
    private float dialog_heightFactor = 1.0f;
    private float dialogFragment_widthFactor = dialog_widthFactor;
    private float dialogFragment_heightFactor = dialog_heightFactor;

    private boolean hasSound = true;    // has sound effect

    private boolean isShowingLoadingMessage;
    private boolean isShowingSavingGameMessage;
    private boolean isProcessingJob;

    public MainUiFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MainUiFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainUiFragment newInstance() {
        MainUiFragment fragment = new MainUiFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            mListener = new OnFragmentInteractionListener() {
                @Override
                public void onFragmentInteraction(Uri uri) {
                    System.out.println("must implement OnFragmentInteractionListener --> Uri = " + uri);
                }
            };
        }

        System.out.println("MainUiFragment onAttach() is called.");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);    // retain install of fragment when recreate

        // if (savedInstanceState == null) { // removed if statement on 2018-10-25
            // string constant
            yesStr = ColorBallsApp.AppResources.getString(R.string.yesStr);
            noStr = ColorBallsApp.AppResources.getString(R.string.noStr);
            nameStr = ColorBallsApp.AppResources.getString(R.string.nameStr);
            submitStr = ColorBallsApp.AppResources.getString(R.string.submitStr);
            cancelStr = ColorBallsApp.AppResources.getString(R.string.cancelStr);
            gameOverStr = ColorBallsApp.AppResources.getString(R.string.gameOverStr);
            loadingString = ColorBallsApp.AppResources.getString(R.string.loadingString);
            savingGameString = ColorBallsApp.AppResources.getString(R.string.savingGameString);
        // }

        System.out.println("MainUiFragment onCreate() is called.");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        System.out.println("MainUiFragment onCreateView() is called.");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_for_main_ui_fragment, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println("MainUiFragment onViewCreated() is called.");
        uiFragmentView = view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        System.out.println("MainUiFragment onActivityCreated() is called.");

        // starting the game
        // the object gotten from getActivity() in onActivityCreated() is different from gotten in onCreate()
        // this context should be used in all scope, especially in AsyncTask
        context = getActivity();
        myActivity = (MyActivity)context;

        fontSizeForText = myActivity.getFontSizeForText();
        dialog_widthFactor = myActivity.getDialog_widthFactor();
        dialog_heightFactor = myActivity.getDialog_heightFactor();
        dialogFragment_widthFactor = myActivity.getDialogFragment_widthFactor();
        dialogFragment_heightFactor = myActivity.getDialogFragment_heightFactor();

        Point size = new Point();
        ScreenUtil.getScreenSize(context, size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        System.out.println("Screen size of this device -> screenWidth = " + screenWidth + ", screenHeight = " + screenHeight);

        int statusBarHeight = ScreenUtil.getStatusBarHeight(context);
        int actionBarHeight = ScreenUtil.getActionBarHeight(context);

        // keep navigation bar
        screenHeight = screenHeight - statusBarHeight - actionBarHeight;

        if (ColorBallsApp.AppResources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape
            screenWidth = screenWidth / 2;  // for this fragment is half a screenWidth
        }

        float height_weightSum_uiFragmentView = 11;    // default
        try {
            LinearLayout linearLay = (LinearLayout) uiFragmentView;
            float temp = linearLay.getWeightSum();
            if (temp != 0) {
                height_weightSum_uiFragmentView = temp;
            }
        } catch (Exception ex) {
            System.out.println("Getting weightSum of Layout for uiFragmentView was failed.");
            ex.printStackTrace();
        }

        LinearLayout scoreNextBallsLayout = uiFragmentView.findViewById(R.id.score_next_balls_layout);
        float width_weightSum_scoreNextBallsLayout = scoreNextBallsLayout.getWeightSum();
        if (width_weightSum_scoreNextBallsLayout == 0) {
            width_weightSum_scoreNextBallsLayout = 5;   // default
        }
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) scoreNextBallsLayout.getLayoutParams();
        float height_weight_scoreNextBallsLayout = layoutParams.weight;
        if (height_weight_scoreNextBallsLayout == 0) {
            height_weight_scoreNextBallsLayout = 1; //default
        }

        // display the highest score and current score
        currentScoreView = uiFragmentView.findViewById(R.id.currentScoreTextView);
        currentScoreView.setTextSize(fontSizeForText);
        currentScoreView.setText(String.format(Locale.getDefault(), "%9d", currentScore));

        // display the view of next balls
        GridLayout nextBallsLayout = uiFragmentView.findViewById(R.id.nextBallsLayout);
        int nextBallsRow = nextBallsLayout.getRowCount();
        nextBallsNumber = nextBallsLayout.getColumnCount();
        layoutParams = (LinearLayout.LayoutParams)nextBallsLayout.getLayoutParams();
        float width_weight_nextBalls = layoutParams.weight;

        int nextBallsViewWidth = (int)((float)screenWidth * width_weight_nextBalls / width_weightSum_scoreNextBallsLayout);   // 3-5th of screen width

        LinearLayout.LayoutParams oneNextBallLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        oneNextBallLp.width = nextBallsViewWidth / nextBallsNumber;
        // the layout_weight for height is 1
        oneNextBallLp.height = (int)((float)screenHeight * height_weight_scoreNextBallsLayout / height_weightSum_uiFragmentView);
        oneNextBallLp.gravity = Gravity.CENTER;

        ImageView imageView = null;

        for (int i = 0; i < nextBallsRow; i++) {
            for (int j = 0; j < nextBallsNumber; j++) {
                imageView = new ImageView(myActivity);
                imageView.setId(nextBallsViewIdStart + (nextBallsNumber * i + j));
                imageView.setClickable(false);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setBackgroundResource(R.drawable.next_ball_background_image);
                nextBallsLayout.addView(imageView, oneNextBallLp);
            }
        }

        FrameLayout gridPartFrameLayout = uiFragmentView.findViewById(R.id.gridPartFrameLayout);
        LinearLayout.LayoutParams frameLp = (LinearLayout.LayoutParams) gridPartFrameLayout.getLayoutParams();
        float height_weight_gridCellsLayout = frameLp.weight;

        // for 9 x 9 grid: main part of this game
        GridLayout gridCellsLayout = uiFragmentView.findViewById(R.id.gridCellsLayout);
        rowCounts = gridCellsLayout.getRowCount();
        colCounts = gridCellsLayout.getColumnCount();
        // LinearLayout.LayoutParams gridLp = (LinearLayout.LayoutParams) gridCellsLayout.getLayoutParams();
        // float height_weight_gridCellsLayout = gridLp.weight;
        if (height_weight_gridCellsLayout == 0) {
            height_weight_gridCellsLayout = 8;  // default
        }

        cellWidth = screenWidth / colCounts;
        int eight10thOfHeight = (int)( (float)screenHeight / height_weightSum_uiFragmentView * height_weight_gridCellsLayout);
        if ( screenWidth >  eight10thOfHeight) {
            // if screen width greater than 8-10th of screen height
            cellWidth = eight10thOfHeight / rowCounts;
        }
        cellHeight = cellWidth;

        /* removed ont 2018-10-02 to test
        gridLp.width = cellWidth * colCounts;
        gridLp.topMargin = 20;
        gridLp.gravity = Gravity.CENTER;
        */

        // added on 2018-10-02 to test and it works
        // setting the width and the height of GridLayout by using the FrameLayout that is on top of it
        frameLp.width = cellWidth * colCounts;
        frameLp.topMargin = 20;
        frameLp.gravity = Gravity.CENTER;
        //

        LinearLayout.LayoutParams oneBallLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        oneBallLp.width = cellWidth;
        oneBallLp.height = cellHeight;
        oneBallLp.gravity = Gravity.CENTER;

        // set listener for each ImageView
        // ImageView imageView;
        int imId = 0;
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 0; j < colCounts; j++) {
                // imId = i * colCounts + j;
                imId = i * rowCounts + j;
                imageView = new ImageView(myActivity);
                imageView.setId(imId);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setBackgroundResource(R.drawable.boximage);
                imageView.setClickable(true);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ( (completedAll()) && (!isShowingLoadingMessage) && (!isProcessingJob) ) {
                            doDrawBallsAndCheckListener(v);
                        }
                    }
                });
                gridCellsLayout.addView(imageView, imId, oneBallLp);

            }
        }

        // For testing to display score using ImageView
        scoreImageView = uiFragmentView.findViewById(R.id.scoreImageView);
        scoreImageView.setVisibility(View.GONE);
        //

        Button undoButton = uiFragmentView.findViewById(R.id.undoButton);
        undoButton.setTextSize(fontSizeForText);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isProcessingJob) {
                    undoTheLast();
                }
            }
        });

        Button top10Button = uiFragmentView.findViewById(R.id.top10Button);
        top10Button.setTextSize(fontSizeForText);
        top10Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isProcessingJob) {
                    showTop10ScoreHistory();   // added on 2018-06-11
                }
            }
        });

        Button globalTop10Button = uiFragmentView.findViewById(R.id.globalTop10Button);
        globalTop10Button.setTextSize(fontSizeForText);
        globalTop10Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isProcessingJob) {
                    showGlobalTop10History();
                }
            }
        });

        if ( (savedInstanceState == null) || (gridData == null) ) {
            // start a new game (no savedInstanceState)
            // or gridData is null (for some unknown reason)

            isShowingLoadingMessage = false;
            isShowingSavingGameMessage = false;
            isProcessingJob = false;

            isEasyLevel = true;     // start with easy level
            gridData = new GridData(rowCounts, colCounts, MINB, MINB);  // easy level (3 balls for next balls)
            displayGridDataNextCells();

        } else {
            // fragment recreated (keep the original state)
            // display the original state before changing configuration
            displayGameView();

            if (isShowingLoadingMessage) {
                showLoadingMessage();
            }
            if (isShowingSavingGameMessage) {
                showSavingGameMessage();
            }

            if (bouncingStatus == 1) {
                // ImageView v = uiFragmentView.findViewById(bouncyBallIndexI * colCounts + bouncyBallIndexJ);
                ImageView v = uiFragmentView.findViewById(bouncyBallIndexI * rowCounts + bouncyBallIndexJ);
                drawBouncyBall(v, gridData.getCellValue(bouncyBallIndexI, bouncyBallIndexJ));
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        SoundUtil.releaseMediaPlayer();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private boolean completedAll() {
        for (boolean thCompleted : threadCompleted) {
            if (!thCompleted) {
                return false;
            }
        }
        return true;
    }

    private void displayGridDataNextCells() {

        gridData.randCells();
        int numOneTime = gridData.getBallNumOneTime();

        int[] indexi = gridData.getNextCellIndexI();
        int[] indexj = gridData.getNextCellIndexJ();

        int id, n1, n2;
        ImageView imageView = null;
        for (int i = 0; i < numOneTime; i++) {
            n1 = indexi[i];
            n2 = indexj[i];
            if ((n1 >= 0) && (n2 >= 0)) {
                // id = n1 * colCounts + n2;
                id = n1 * rowCounts + n2;
                imageView = uiFragmentView.findViewById(id);
                drawBall(imageView,gridData.getCellValue(n1, n2));
            }
        }

        Point hashPoint[] = new Point[0];
        boolean hasMoreFive = false;
        for (int i = 0; i < numOneTime; i++) {
            n1 = indexi[i];
            n2 = indexj[i];
            if ((n1 >= 0) && (n2 >= 0)) {
                if (gridData.getCellValue(n1, n2) != 0) {
                    //   has  color in this cell
                    if (gridData.check_moreFive(n1, n2) == 1) {
                        hasMoreFive = true;
                        int arraySize = gridData.getLight_line().size();
                        hashPoint = new Point[arraySize];
                        int index = 0;
                        for (Point p : gridData.getLight_line()) {
                            hashPoint[index] = p;
                            ++index;
                        }
                    }
                }
            }
        }

        boolean gameOverYn = false;
        if (hasMoreFive) {
            threadCompleted[1] = false;
            CalculateScore calculateScore = new CalculateScore();
            calculateScore.execute(hashPoint);
        } else {
            // check if game over
            gameOverYn = gridData.getGameOver();
            // if (gridData.getGameOver()) {
            if (gameOverYn) {
                //  game over
                AlertDialogFragment gameOverDialog = AlertDialogFragment.newInstance(gameOverStr, fontSizeForText * dialogFragment_widthFactor
                        , Color.BLUE, 0, 0, 2, new AlertDialogFragment.DialogButtonListener() {
                    @Override
                    public void noButtonOnClick(AlertDialogFragment dialogFragment) {
                        // dialogFragment.dismiss();
                        dialogFragment.dismissAllowingStateLoss();
                        recordScore(0);   //   Ending the game
                    }

                    @Override
                    public void okButtonOnClick(AlertDialogFragment dialogFragment) {
                        // dialogFragment.dismiss();
                        dialogFragment.dismissAllowingStateLoss();
                        newGame();
                    }
                });
                /*
                Bundle args = new Bundle();
                args.putString("textContent", gameOverStr);
                args.putFloat("textSize", fontSizeForText * dialogFragment_widthFactor);
                args.putInt("color", Color.BLUE);
                args.putInt("width", 0);    // wrap_content
                args.putInt("height", 0);   // wrap_content
                args.putInt("numButtons", 2);
                gameOverDialog.setArguments(args);
                */
                gameOverDialog.show(getActivity().getSupportFragmentManager(), GameOverDialogTag);

                System.out.println("gameOverDialog.show() has been called.");
            }
        }

        if (!gameOverYn) {
            // game has not been over yet
            displayNextColorBalls();
        }
    }

    private void undoTheLast() {

        if (!undoEnable) {
            return;
        }

        isProcessingJob = true; // started undoing

        gridData.undoTheLast();

        // restore the screen
        displayGameView();

        bouncingStatus = 0;
        bouncyBallIndexI = -1;
        bouncyBallIndexJ = -1;

        currentScore = undoScore;
        currentScoreView.setText( String.format(Locale.getDefault(), "%9d",currentScore));

        // completedPath = true;
        undoEnable = false;

        isProcessingJob = false;    // finished
    }

    private void clearCell(int i, int j) {
        // int id = i * colCounts + j;
        int id = i * rowCounts + j;
        ImageView imageView = uiFragmentView.findViewById(id);
        // imageView.setImageResource(R.drawable.boximage);
        imageView.setImageDrawable(null);
        // imageView.setImageBitmap(null);
        gridData.setCellValue(i, j, 0);
    }

    private void doDrawBallsAndCheckListener(View v) {

        int i, j, id;
        id = v.getId();
        i = id / rowCounts;
        j = id % rowCounts;
        ImageView imageView = null;
        if (bouncingStatus == 0) {
            if (gridData.getCellValue(i, j) != 0) {
                if ((bouncyBallIndexI == -1) && (bouncyBallIndexJ == -1)) {
                    bouncingStatus = 1;
                    drawBouncyBall((ImageView) v, gridData.getCellValue(i, j));
                    bouncyBallIndexI = i;
                    bouncyBallIndexJ = j;
                }
            }
        } else {
            // cancel the timer
            if (gridData.getCellValue(i, j) == 0) {
                //   blank cell
                if ((bouncyBallIndexI >= 0) && (bouncyBallIndexJ >= 0)) {
                    if (gridData.moveCellToCell(new Point(bouncyBallIndexI, bouncyBallIndexJ), new Point(i, j))) {
                        // cancel the timer
                        bouncingStatus = 0;
                        cancelBouncyTimer();
                        int color = gridData.getCellValue(bouncyBallIndexI, bouncyBallIndexJ);
                        gridData.setCellValue(i, j, color);
                        clearCell(bouncyBallIndexI, bouncyBallIndexJ);

                        bouncyBallIndexI = -1;
                        bouncyBallIndexJ = -1;

                        drawBallAlongPath(i,j,color);

                        undoEnable = true;
                    } else {
                        //    make a sound
                        if (hasSound) {
                            SoundUtil.playSound(context, R.raw.uhoh);
                        }
                    }
                }
            } else {
                //  cell is not blank
                if ((bouncyBallIndexI >= 0) && (bouncyBallIndexJ >= 0)) {
                    bouncingStatus = 0;
                    cancelBouncyTimer();
                    bouncingStatus = 1;
                    // imageView = (ImageView) uiFragmentView.findViewById(bouncyBallIndexI * colCounts + bouncyBallIndexJ);
                    imageView = uiFragmentView.findViewById(bouncyBallIndexI * rowCounts + bouncyBallIndexJ);
                    drawBall(imageView , gridData.getCellValue(bouncyBallIndexI, bouncyBallIndexJ));
                    drawBouncyBall((ImageView) v, gridData.getCellValue(i, j));
                    bouncyBallIndexI = i;
                    bouncyBallIndexJ = j;
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
                if (bouncingStatus == 1) {
                    if (color != 0) {
                        if (ballYN) {
                            drawBall(v , color);
                        } else {
                            drawOval(v , color);
                        }
                        ballYN = !ballYN;
                        bouncyHandler.postDelayed(this, 200);
                    } else {
                        // v.setImageResource(R.drawable.boximage);
                        v.setImageDrawable(null);
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

    private void drawBallAlongPath(final int ii , final int jj,final int color) {
        if (gridData.getPathPoint().size()<=0) {
            return;
        }

        final List<Point> tempList = new ArrayList<>(gridData.getPathPoint());
        final Handler drawHandler = new Handler();
        Runnable runnablePath = new Runnable() {
            boolean ballYN = true;
            ImageView imageView = null;
            int countDown = tempList.size()*2 - 1;
            @Override
            public void run() {
                threadCompleted[0] = false;
                if (countDown >= 2) {   // eliminate start point
                    int i = countDown / 2;
                    // imageView = (ImageView) uiFragmentView.findViewById(tempList.get(i).x * colCounts + tempList.get(i).y);
                    imageView = uiFragmentView.findViewById(tempList.get(i).x * rowCounts + tempList.get(i).y);
                    if (ballYN) {
                        drawBall(imageView, color);
                    } else {
                        // imageView.setImageResource(R.drawable.boximage);
                        imageView.setImageDrawable(null);
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
        // may need to run runOnUiThread()
        // ImageView v = (ImageView) uiFragmentView.findViewById(i * colCounts + j);
        ImageView v = uiFragmentView.findViewById(i * rowCounts + j);
        drawBall(v, gridData.getCellValue(i, j));
        if (gridData.check_moreFive(i, j) == 1) {
            //  check if there are more than five balls with same color connected together
            // int numBalls = gridData.getLight_line().size();
            // scoreCalculate(numBalls);
            // twinkleLineBallsAndClearCell(gridData.getLight_line(), 1);
            int arraySize = gridData.getLight_line().size();
            Point hashPoint[] = new Point[arraySize];
            int index = 0;
            for (Point p : gridData.getLight_line()) {
                hashPoint[index] = p;
                ++index;
            }

            threadCompleted[1] = false;
            CalculateScore calculateScore = new CalculateScore();
            calculateScore.execute(hashPoint);
        } else {
            displayGridDataNextCells();   // has a problem
        }
    }

    private int scoreCalculate(int numBalls) {
        // 5 balls --> 5
        // 6 balls --> 5 + (6-5)*2
        // 7 balls --> 5 + (6-5)*2 + (7-5)*2
        // 8 balls --> 5 + (6-5)*2 + (7-5)*2 + (8-5)*2
        // n balls --> 5 + (6-5)*2 + (7-5)*5 + ... + (n-5)*2
        int minBalls = 5;
        int minScore = 5;
        int score = minScore;
        if (numBalls > minScore) {
            // greater than 5 balls
            int rate  = 1;
            for (int i=1 ; i<=Math.abs(numBalls-minBalls) ; i++) {
                // rate = rate * 2; // removed on 2018-10-02
                rate = 2;   // added on 2018-10-02
                score += i * rate ;
            }
        }

        if (!isEasyLevel) {
            // difficult level
            score = score * 2;   // double of easy level
        }

        return score;
    }

    private void flushALLandBegin() {
        /* cannot use FragmentManager to recreate Fragment because MyActivity will not have new instance of Fragment
        // recreate this Fragment without recreating MyActivity
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(myActivity.getMainUiLayoutId(), newInstance(), MainUiFragmentTag);
        ft.commit();
        */

        // must use the following to let MyActivity have new instance of Fragment
        // recreate MyActivity like new start (no savedInstanceState)
        // Intent intent = myActivity.getIntent();
        // myActivity.finish();
        // startActivity(intent);

        myActivity.reStartApplication();   // restart the game
    }

    private void setDialogStyle(DialogInterface dialog) {
        AlertDialog dlg = (AlertDialog)dialog;
        dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dlg.getWindow().setDimAmount(0.0f); // no dim for background screen
        dlg.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        dlg.getWindow().setBackgroundDrawableResource(R.drawable.dialog_board_image);

        float fontSize = fontSizeForText * dialog_widthFactor;
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

    private void drawBall(ImageView imageView,int color) {
        switch (color) {
            case GridData.ColorRED:
                imageView.setImageResource(R.drawable.redball);
                break;
            case GridData.ColorGREEN:
                imageView.setImageResource(R.drawable.greenball);
                break;
            case GridData.ColorBLUE:
                imageView.setImageResource(R.drawable.blueball);
                break;
            case GridData.ColorMAGENTA:
                imageView.setImageResource(R.drawable.magentaball);
                break;
            case GridData.ColorYELLOW:
                imageView.setImageResource(R.drawable.yellowball);
                break;
            default:
                imageView.setImageDrawable(null);
                break;
        }
    }

    private void drawOval(ImageView imageView,int color) {
        switch (color) {
            case GridData.ColorRED:
                imageView.setImageResource(R.drawable.redball_o);
                break;
            case GridData.ColorGREEN:
                imageView.setImageResource(R.drawable.greenball_o);
                break;
            case GridData.ColorBLUE:
                imageView.setImageResource(R.drawable.blueball_o);
                break;
            case GridData.ColorMAGENTA:
                imageView.setImageResource(R.drawable.magentaball_o);
                break;
            case GridData.ColorYELLOW:
                imageView.setImageResource(R.drawable.yellowball_o);
                break;
            default:
                imageView.setImageDrawable(null);
                break;
        }
    }

    private void displayNextBallsView() {
        // display the view of next balls
        ImageView imageView = null;
        int numOneTime = gridData.getBallNumOneTime();
        for (int i = 0; i < numOneTime; i++) {
            imageView = uiFragmentView.findViewById(nextBallsViewIdStart + i);
            drawBall(imageView, gridData.getNextBalls()[i]);
        }
        for (int i = numOneTime; i < nextBallsNumber; i++) {
            imageView = uiFragmentView.findViewById(nextBallsViewIdStart + i);
            // imageView.setImageResource(R.drawable.next_ball_background_image);
            imageView.setImageDrawable(null);
        }
    }

    private void displayGameGridView() {
        // display the 9 x 9 game view
        ImageView imageView = null;
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 0; j < colCounts; j++) {
                int id = i * rowCounts + j;
                imageView = uiFragmentView.findViewById(id);
                int color = gridData.getCellValue(i, j);
                if (color == 0) {
                    // imageView.setImageResource(R.drawable.boximage);
                    imageView.setImageDrawable(null);
                } else {
                    drawBall(imageView, color);
                }
            }
        }
    }

    private void displayGameView() {

        // display the view of next balls
        displayNextBallsView();

        // display the 9 x 9 game view
        displayGameGridView();
    }

    private GridData getGridData() {
        return this.gridData;
    }

    private void displayNextColorBalls() {
        gridData.randColors();  //   next  balls
        //   display the balls on the nextBallsView
        displayNextBallsView();
    }

    private void showInterstitialAdAndNewGameOrQuit(final int entryPoint) {
        ShowingInterstitialAdsUtil.ShowAdAsyncTask showAdAsyncTask =
                ColorBallsApp.InterstitialAd.new ShowAdAsyncTask(myActivity, entryPoint, new ShowingInterstitialAdsUtil.AfterDismissFunctionOfShowAd() {
            @Override
            public void executeAfterDismissAds(int endPoint) {
                isProcessingJob = false;
                if (entryPoint==0) {
                    //  END PROGRAM
                    myActivity.quitApplication();
                } else if (entryPoint==1) {
                    //  NEW GAME
                    flushALLandBegin();
                }
            }
        });
        showAdAsyncTask.execute();
    }

    private void showTop10ScoreHistory() {
        isProcessingJob = true;
        showLoadingMessage();
        Intent myIntentService = new Intent(myActivity, MyTop10ScoresIntentService.class);
        myActivity.startService(myIntentService);
    }
    private void showGlobalTop10History() {
        isProcessingJob = true;
        showLoadingMessage();
        Intent myIntentService = new Intent(myActivity, MyGlobalTop10IntentService.class);
        String webUrl = ColorBallsApp.REST_Website + "/GetTop10PlayerscoresREST";  // ASP.NET Core
        webUrl += "?gameId=" + ColorBallsApp.GameId;   // parameters
        myIntentService.putExtra("WebUrl", webUrl);
        myActivity.startService(myIntentService);
    }
    private void showSavingGameMessage() {
        isShowingSavingGameMessage = true;
        float fontSize = fontSizeForText;
        double factor = 1.5;
        int bmWidth = (int)(fontSize * savingGameString.length() * factor);
        int bmHeight = (int)(fontSize * factor * 6.0);
        Bitmap dialog_board_image = BitmapFactory.decodeResource(ColorBallsApp.AppResources, R.drawable.dialog_board_image);
        Bitmap bm = Bitmap.createScaledBitmap(dialog_board_image, bmWidth, bmHeight, false );  // scale
        Bitmap loadingBitmap = FontAndBitmapUtil.getBitmapFromBitmapWithText(bm, savingGameString, Color.RED);
        scoreImageView.setVisibility(View.VISIBLE);
        scoreImageView.setImageBitmap(loadingBitmap);
    }
    private void dismissSavingGameMessage() {
        isShowingSavingGameMessage = false;
        scoreImageView.setImageBitmap(null);
        scoreImageView.setVisibility(View.GONE);
    }

    // public methods
    public void newGame() {
        recordScore(1);   //   START A NEW GAME
    }
    public void saveGame() {
        isProcessingJob = true;

        // showSavingGameMessage();

        boolean succeeded = true;
        File outputFile = new File(ColorBallsApp.AppContext.getFilesDir(), savedGameFileName);
        try {
            FileOutputStream foStream = new FileOutputStream(outputFile);
            // save settings
            if (hasSound) {
                foStream.write(1);
            } else {
                foStream.write(0);
            }
            if (isEasyLevel) {
                foStream.write(1);
            } else {
                foStream.write(0);
            }
            // save next balls
            foStream.write(gridData.getBallNumOneTime());
            for (int i=0; i<GridData.MaxBalls; i++) {
                foStream.write(gridData.getNextBalls()[i]);
            }
            // save values on 9x9 grid
            for (int i=0; i<rowCounts; i++) {
                for (int j=0; j<colCounts; j++) {
                    foStream.write(gridData.getCellValue(i, j));
                }
            }
            // save current score
            byte[] scoreByte = ByteBuffer.allocate(4).putInt(currentScore).array();
            foStream.write(scoreByte);
            // save undoEnable
            if (undoEnable) {
                // can undo
                foStream.write(1);
                foStream.write(gridData.getUndoNumOneTime());
                // save undoNextBalls
                for (int i=0; i<GridData.MaxBalls; i++) {
                    foStream.write(gridData.getUndoNextBalls()[i]);
                }
                // save backupCells
                for (int i=0; i<rowCounts; i++) {
                    for (int j=0; j<colCounts; j++) {
                        foStream.write(gridData.getBackupCells()[i][j]);
                    }
                }
                byte[] undoScoreByte = ByteBuffer.allocate(4).putInt(undoScore).array();
                foStream.write(undoScoreByte);
                // end of writing
            } else {
                // no undo
                foStream.write(0);
                // end of writing
            }

            foStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            succeeded = false;
        }

        String textContent;
        if (succeeded) {
            textContent = "Saved game successfully.";
        } else {
            textContent = "Failed to save game.";
        }
        AlertDialogFragment gameSavedDialog = AlertDialogFragment.newInstance(textContent, fontSizeForText * dialogFragment_widthFactor
                , Color.BLUE, 0, 0, 1, new AlertDialogFragment.DialogButtonListener() {
                    @Override
                    public void noButtonOnClick(AlertDialogFragment dialogFragment) {
                        dialogFragment.dismissAllowingStateLoss();
                    }

                    @Override
                    public void okButtonOnClick(AlertDialogFragment dialogFragment) {
                        dialogFragment.dismissAllowingStateLoss();
                    }
                });
        gameSavedDialog.show(getActivity().getSupportFragmentManager(), "GameSavedDialogTag");

        // dismissSavingGameMessage();
        isProcessingJob = false;
    }
    public void loadGame() {
        isProcessingJob = true;

        boolean succeeded = true;
        boolean soundYn;
        boolean easyYn;
        int ballNumOneTime;
        int[] nextBalls = new int[GridData.MaxBalls];
        int[][] gameCells = new int[rowCounts][colCounts];
        int cScore;
        boolean undoYn;
        int undoNumOneTime;
        int[] undoNextBalls = new int[GridData.MaxBalls];
        int[][] backupCells = new int[rowCounts][colCounts];
        int unScore;

        File inputFile = new File(ColorBallsApp.AppContext.getFilesDir(), savedGameFileName);
        try {
            FileInputStream fiStream = new FileInputStream(inputFile);
            int bValue = fiStream.read();
            if (bValue == 1) {
                // has sound
                Log.i(TAG, "FileInputStream Read: Game has sound");
                soundYn = true;
            } else {
                // has no sound
                Log.i(TAG, "FileInputStream Read: Game has no sound");
                soundYn = true;
            }
            bValue = fiStream.read();
            if (bValue == 1) {
                // easy level
                Log.i(TAG, "FileInputStream Read: Game is easy level");
                easyYn = true;

            } else {
                // difficult level
                Log.i(TAG, "FileInputStream Read: Game is difficult level");
                easyYn = false;
            }
            ballNumOneTime = fiStream.read();
            Log.i(TAG, "FileInputStream Read: Game has " + ballNumOneTime + " next balls");
            int ballValue;
            for (int i=0; i<GridData.MaxBalls; i++) {
                nextBalls[i] = fiStream.read();
                Log.i(TAG, "FileInputStream Read: Next ball value = " + nextBalls[i]);
            }
            for (int i=0; i<rowCounts; i++) {
                for (int j=0; j<colCounts; j++) {
                    gameCells[i][j] = fiStream.read();
                    Log.i(TAG, "FileInputStream Read: Value of ball at (" + i + ", " + j + ") = " + gameCells[i][j]);
                }
            }
            // reading current score
            byte[] scoreByte = new byte[4];
            fiStream.read(scoreByte);
            cScore = ByteBuffer.wrap(scoreByte).getInt();
            Log.i(TAG, "FileInputStream Read: Current score = " + cScore);
            // reading undoEnable
            bValue = fiStream.read();
            if (bValue == 1) {
                // has undo data
                Log.i(TAG, "FileInputStream Read: Game has undo data");
                undoYn = true;
                undoNumOneTime = fiStream.read();
                for (int i=0; i<GridData.MaxBalls; i++) {
                    undoNextBalls[i] = fiStream.read();
                }
                // save backupCells
                for (int i=0; i<rowCounts; i++) {
                    for (int j=0; j<colCounts; j++) {
                        backupCells[i][j] = fiStream.read();
                    }
                }
                byte[] undoScoreByte = new byte[4];
                fiStream.read(undoScoreByte);
                unScore = ByteBuffer.wrap(undoScoreByte).getInt();
                Log.i(TAG, "FileInputStream Read: undoScore = " + unScore);
            } else {
                // does not has undo data
                Log.i(TAG, "FileInputStream Read: Game does not has undo data");
                undoYn = false;
            }
            fiStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            succeeded = false;
        }
        if (succeeded) {
            // reset the game by data read
        } else {
            // show failed message
        }

        isProcessingJob = false;
    }

    public void recordScore(final int entryPoint) {

        isProcessingJob = true;

        final EditText et = new EditText(myActivity);
        et.setTextSize(fontSizeForText);
        et.setTextColor(Color.BLUE);
        // et.setBackground(new ColorDrawable(Color.TRANSPARENT));
        // et.setBackgroundColor(Color.TRANSPARENT);
        et.setHint(nameStr);
        et.setGravity(Gravity.CENTER);
        AlertDialog alertD = new AlertDialog.Builder(myActivity).create();
        alertD.setTitle(null);
        alertD.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertD.setCancelable(false);
        alertD.setView(et);
        alertD.setButton(DialogInterface.BUTTON_NEGATIVE, cancelStr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showInterstitialAdAndNewGameOrQuit(entryPoint);
            }
        });
        alertD.setButton(DialogInterface.BUTTON_POSITIVE, submitStr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // use thread to add a record to database (remote database on AWS-EC2)
                Thread restThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            String webUrl = new String(ColorBallsApp.REST_Website + "/AddOneRecordREST");   // ASP.NET Cor
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("PlayerName", et.getText().toString());
                            jsonObject.put("Score", currentScore);
                            jsonObject.put("GameId", ColorBallsApp.GameId);
                            PlayerRecordRest.addOneRecord(webUrl, jsonObject);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Log.d(TAG, "Failed to add one record to Playerscore table.");
                        }
                    }
                };
                restThread.start();

                // modified on 2018-11-07
                boolean isInTop10 = ColorBallsApp.ScoreSQLiteDB.isInTop10(currentScore);
                if (isInTop10) {
                    // inside top 10
                    // record the current score
                    ColorBallsApp.ScoreSQLiteDB.addScore(et.getText().toString(),currentScore);
                    ColorBallsApp.ScoreSQLiteDB.deleteAllAfterTop10();  // only keep the top 10
                }
                //

                showInterstitialAdAndNewGameOrQuit(entryPoint);
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

    public boolean getIsEasyLevel() {
        return this.isEasyLevel;
    }

    public void setIsEasyLevel(boolean yn) {
        this.isEasyLevel = yn;
        if (this.isEasyLevel) {
            // easy level
            getGridData().setMinBallsOneTime(MainUiFragment.MINB);
            getGridData().setMaxBallsOneTime(MainUiFragment.MINB);
        } else {
            // difficult
            getGridData().setMinBallsOneTime(MainUiFragment.MINB);
            getGridData().setMaxBallsOneTime(MainUiFragment.MAXB);

        }
        displayNextColorBalls();
    }

    public boolean getHasSound() {
        return hasSound;
    }
    public void setHasSound(boolean hasSound) {
        this.hasSound = hasSound;
    }
    public ImageView getScoreImageView() {
        return scoreImageView;
    }
    public boolean getIsProcessingJob() {
        return isProcessingJob;
    }
    public void setIsProcessingJob(boolean isProcessingJob) {
        this.isProcessingJob = isProcessingJob;
    }
    public void showLoadingMessage() {
        isShowingLoadingMessage = true;
        float fontSize = fontSizeForText;
        double factor = 1.5;
        int bmWidth = (int)(fontSize * loadingString.length() * factor);
        int bmHeight = (int)(fontSize * factor * 6.0);
        Bitmap dialog_board_image = BitmapFactory.decodeResource(ColorBallsApp.AppResources, R.drawable.dialog_board_image);
        Bitmap bm = Bitmap.createScaledBitmap(dialog_board_image, bmWidth, bmHeight, false );  // scale
        Bitmap loadingBitmap = FontAndBitmapUtil.getBitmapFromBitmapWithText(bm, loadingString, Color.RED);
        scoreImageView.setVisibility(View.VISIBLE);
        scoreImageView.setImageBitmap(loadingBitmap);
    }
    public void dismissShowingLoadingMessage() {
        isShowingLoadingMessage = false;
        scoreImageView.setImageBitmap(null);
        scoreImageView.setVisibility(View.GONE);
    }

    private class CalculateScore extends AsyncTask<Point, Integer, String[]> {

        // private final String ScoreDialogTag = new String("ScoreDialogFragment");
        private int numBalls = 0;
        private int color = 0;
        private int score = 0;
        private Point[] hashPoint = null;

        private Bitmap scoreBitmap;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            threadCompleted[1] = false;
            scoreImageView.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(Point... params) {

            hashPoint = params;
            if (hashPoint == null) {
                return null;
            }

            numBalls = hashPoint.length;
            score = scoreCalculate(numBalls);
            String scoreString = String.valueOf(score);
            double factor = 0.8;
            int bmWidth = (int)(cellWidth * scoreString.length() * factor);
            int bmHeight = (int)(cellHeight * factor * 2.0);
            Bitmap dialog_board_image = BitmapFactory.decodeResource(ColorBallsApp.AppResources, R.drawable.dialog_board_image);
            Bitmap bm = Bitmap.createScaledBitmap(dialog_board_image, bmWidth, bmHeight, false );  // scale
            scoreBitmap = FontAndBitmapUtil.getBitmapFromBitmapWithText(bm, scoreString, Color.BLACK);

            color = gridData.getCellValue(hashPoint[0].x, hashPoint[0].y);

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
                        ImageView v = uiFragmentView.findViewById(item.x * rowCounts + item.y);
                        drawBall(v, color);
                    }
                    break;
                case 1:
                    for (Point item : hashPoint) {
                        ImageView v = uiFragmentView.findViewById(item.x * rowCounts + item.y);
                        drawOval(v, color);
                    }
                    break;
                case 2:
                case 3:
                    scoreImageView.setImageBitmap(scoreBitmap);
                    break;
            }

            return ;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            scoreImageView.setImageBitmap(scoreBitmap);

            // clear values of cells
            for (Point item : hashPoint) {
                clearCell(item.x, item.y);
            }

            // update the UI
            undoScore = currentScore;
            currentScore = currentScore + score;
            currentScoreView.setText(String.format(Locale.getDefault(), "%9d", currentScore));

            threadCompleted[1] = true;

            // hide score ImageView
            scoreImageView.setImageBitmap(null);
            scoreImageView.setVisibility(View.GONE);
        }
    }
}
