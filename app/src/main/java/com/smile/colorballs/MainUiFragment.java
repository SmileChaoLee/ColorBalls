package com.smile.colorballs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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
import com.smile.model.GridData;
import com.smile.scoresqlite.ScoreSQLite;
import com.smile.utility.ScreenUtl;
import com.smile.utility.SoundUtl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class MainUiFragment extends Fragment {

    // public properties
    public static final String MainUiFragmentTag = "MainUiFragmentTag";
    public final static int MINB = 3;
    public final static int MAXB = 4;

    // private properties for this color balls game
    private OnFragmentInteractionListener mListener;

    private String TAG = "com.smile.colorballs.MainUiFragment";
    private final int nextBallsViewIdStart = 100;
    private final int insideColor0 = 0xFFA4FF13;
    private final int lineColor0 = 0xFFFF1627;

    private ScoreSQLite scoreSQLite = null;
    private Context context = null;
    private MainActivity mainActivity = null;
    private View uiFragmentView = null;
    private FragmentManager fmManager = null;

    private TextView highestScoreView = null;
    private TextView currentScoreView = null;

    private GridData gridData;
    private int nextBallsNumber = 4;
    private int rowCounts = 9;
    private int colCounts = 9;
    private boolean[] threadCompleted =  {true,true,true,true,true,true,true,true,true,true};

    private int indexI = -1, indexJ = -1;   // the array index that the ball has been selected
    private int status = 0; //  no cell selected
    //  one cell with a ball selected
    private boolean undoEnable = false;
    private int highestScore = 0;
    private int currentScore = 0;
    private int undoScore = 0;
    private boolean easyLevel = true;

    private Runnable bouncyRunnable = null; // needed to be tested 2018-0609
    private Handler bouncyHandler = null;   // needed to be tested

    private String yesStr = new String("");
    private String noStr = new String("");
    private String nameStr = new String("");
    private String submitStr = new String("");
    private String cancelStr = new String("");
    private String gameOverStr = new String("");

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

        // string constant
        yesStr = getResources().getString(R.string.yesStr);
        noStr = getResources().getString(R.string.noStr);
        nameStr = getResources().getString(R.string.nameStr);
        submitStr = getResources().getString(R.string.submitStr);
        cancelStr = getResources().getString(R.string.cancelStr);
        gameOverStr = getResources().getString(R.string.gameOverStr);

        System.out.println("MainUiFragment onCreate() is called.");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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

        // starting the game
        // the object gotten from getActivity() in onActivityCreated() is different from gotten in onCreate()
        // this context should be used in all scope, especially in AsyncTask
        context = getActivity();
        mainActivity = (MainActivity)context;
        fmManager = mainActivity.getSupportFragmentManager();

        scoreSQLite = mainActivity.getScoreSQLite();
        highestScore = scoreSQLite.readHighestScore();

        Point size = new Point();
        ScreenUtl.getScreenSize(context, size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        System.out.println("Screen size of this device -> screenWidth = " + screenWidth + ", screenHeight = " + screenHeight);

        int statusBarHeight = ScreenUtl.getStatusBarHeight(context);
        int actionBarHeight = ScreenUtl.getActionBarHeight(context);

        // keep navigation bar
        screenHeight = screenHeight - statusBarHeight - actionBarHeight;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape
            screenWidth = screenWidth / 2;  // for this fragment is half a screenWidth
        }

        /*
        // set the screen size
        LinearLayout.LayoutParams vLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT);
        vLP.width = screenWidth;
        vLP.height = screenHeight;
        uiFragmentView.setLayoutParams(vLP);
        */

        // display the highest score and current score
        highestScoreView = (TextView) uiFragmentView.findViewById(R.id.highestScoreTextView);
        highestScoreView.setWidth(screenHeight/20);
        currentScoreView = (TextView) uiFragmentView.findViewById(R.id.currentScoreTextView);
        currentScoreView.setWidth(screenHeight/20);
        highestScoreView.setText(String.format("%9d", highestScore));
        currentScoreView.setText(String.format("%9d", currentScore));

        // display the view of next balls
        GridLayout nextBallsLayout = (GridLayout) uiFragmentView.findViewById(R.id.nextBallsLayout);
        int nextBallsRow = nextBallsLayout.getRowCount();
        nextBallsNumber = nextBallsLayout.getColumnCount();

        int nextBallsViewWidth = screenWidth * 2 / 3;   // 2-3th of screen width

        LinearLayout.LayoutParams oneNextBallLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        oneNextBallLp.width = nextBallsViewWidth / nextBallsNumber;
        oneNextBallLp.height = screenHeight / 10;  // the layout_weight for height is 1
        oneNextBallLp.gravity = Gravity.CENTER;

        ImageView imageView = null;

        for (int i = 0; i < nextBallsRow; i++) {
            for (int j = 0; j < nextBallsNumber; j++) {
                imageView = new ImageView(mainActivity);
                imageView.setId(nextBallsViewIdStart + (nextBallsNumber * i + j));
                imageView.setClickable(false);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setBackgroundResource(R.drawable.next_ball_background_image);
                nextBallsLayout.addView(imageView, oneNextBallLp);
            }
        }

        // for 9 x 9 grid: main part of this game
        GridLayout gridCellsLayout = (GridLayout) uiFragmentView.findViewById(R.id.gridCellsLayout);
        rowCounts = gridCellsLayout.getRowCount();
        colCounts = gridCellsLayout.getColumnCount();

        /*
        easyLevel = true;   // start with easy level
        gridData = new GridData(rowCounts, colCounts, MINB, MINB);  // easy level (3 balls for next balls)
        */

        int cellWidth = screenWidth / colCounts;
        int eight10thOfHeight = ((int)(screenHeight/10)) * 8;
        if ( screenWidth >  eight10thOfHeight) {
            // if screen width greater than 8-10th of screen height
            cellWidth = eight10thOfHeight / rowCounts;
        }
        int cellHeight = cellWidth;

        LinearLayout.LayoutParams gridLp = (LinearLayout.LayoutParams) gridCellsLayout.getLayoutParams();
        gridLp.width = cellWidth * colCounts;
        gridLp.topMargin = 20;
        gridLp.gravity = Gravity.CENTER;

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
                imId = i * colCounts + j;
                imageView = new ImageView(mainActivity);
                imageView.setId(imId);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setBackgroundResource(R.drawable.boximage);
                imageView.setClickable(true);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(completedAll()) {
                            doDrawBallsAndCheckListener(v);
                        }
                    }
                });
                gridCellsLayout.addView(imageView, imId, oneBallLp);

            }
        }

        Button undoButton = (Button) uiFragmentView.findViewById(R.id.undoButton);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoTheLast();
            }
        });

        Button historyButton = (Button) uiFragmentView.findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // new StartHistoryScore().execute();   // removed on 2018-06-11 at 11:40am
                mainActivity.showScoreHistory();   // added on 2018-06-11
            }
        });

        if (savedInstanceState == null) {
            // start a new game
            easyLevel = true;   // start with easy level
            gridData = new GridData(rowCounts, colCounts, MINB, MINB);  // easy level (3 balls for next balls)
            displayGridDataNextCells();
        } else {
            // display the original state before changing configuration
            displayGameView();

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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private boolean completedAll() {
        for (int i=0 ; i <threadCompleted.length;i++) {
            if (!threadCompleted[i]) {
                return false;
            }
        }
        return true;
    }

    private void displayGridDataNextCells() {

        gridData.randCells();

        ImageView imageView = null;

        int numOneTime = gridData.getBallNumOneTime();

        int[] indexi = gridData.getNextCellIndexI();
        int[] indexj = gridData.getNextCellIndexJ();

        int id, n1, n2;
        for (int i = 0; i < numOneTime; i++) {
            n1 = indexi[i];
            n2 = indexj[i];
            if ((n1 >= 0) && (n2 >= 0)) {
                id = n1 * colCounts + n2;
                imageView = (ImageView) uiFragmentView.findViewById(id);
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
                        AdBuddiz.showAd(mainActivity);
                        // AdBuddiz.RewardedVideo.show(MyActivity.this); // this = current Activity
                    }

                    @Override
                    public void button2OnClick(ModalDialogFragment dialogFragment) {
                        dialogFragment.dismiss();
                        newGame();
                        AdBuddiz.showAd(mainActivity);
                        // AdBuddiz.RewardedVideo.show(MyActivity.this); // this = current Activity
                    }
                });
                Bundle args = new Bundle();
                args.putString("textContent", gameOverStr);
                args.putInt("color", Color.BLUE);
                args.putInt("width", 0);    // wrap_content
                args.putInt("height", 0);   // wrap_content
                args.putInt("numButtons", 2);
                mDialogFragment.setArguments(args);
                mDialogFragment.showDialogFragment(fmManager);
            }
        }

        displayNextColorBalls();
    }

    private void undoTheLast() {

        if (!undoEnable) {
            return;
        }

        ImageView imageView = null;
        int id, n1, n2 , color;

        gridData.undoTheLast();

        // restore the screen
        displayGameView();;

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
        ImageView imageView = (ImageView) uiFragmentView.findViewById(id);
        imageView.setImageResource(R.drawable.boximage);
        gridData.setCellValue(i, j, 0);
    }

    public void doDrawBallsAndCheckListener(View v) {

        int i, j, id;
        id = v.getId();
        i = id / rowCounts;
        j = id % rowCounts;
        ImageView imageView = null;
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

                        SoundUtl.playUhOhSound(context);
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
                    imageView = (ImageView) uiFragmentView.findViewById(indexI * colCounts + indexJ);
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
                if (countDown >= 2) {   // eliminate start point
                    int i = (int) (countDown / 2);
                    imageView = (ImageView) uiFragmentView.findViewById(tempList.get(i).x * colCounts + tempList.get(i).y);
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
        // may need to run runOnUiThread()
        ImageView v = (ImageView) uiFragmentView.findViewById(i * colCounts + j);
        drawBall(v, gridData.getCellValue(i, j));
        if (gridData.check_moreFive(i, j) == 1) {
            //  check if there are more than five balls with same color connected together
            // int numBalls = gridData.getLight_line().size();
            // scoreCalculate(numBalls);
            // twinkleLineBallsAndClearCell(gridData.getLight_line(), 1);
            HashSet<Point> hashPoint = new HashSet<>(gridData.getLight_line());
            // HashSet<Point> hashPoint = new HashSet<Point>();
            // for (Point item : gridData.getLight_line()) {
            //     hashPoint.add(item);
            // }
            threadCompleted[1] = false;
            CalculateScore calculateScore = new CalculateScore();
            calculateScore.execute(hashPoint);
        } else {
            displayGridDataNextCells();   // has a problem
        }
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

        if (!easyLevel) {
            // difficult level
            score = score * 2;   // double of easy level
        }

        return score;
    }

    private void flushALLandBegin() {
        // recreate this Fragment without recreating MainActivity
        FragmentTransaction ft = fmManager.beginTransaction();
        ft.replace(mainActivity.getMainUiLayoutId(), newInstance(), MainUiFragmentTag);
        ft.commit();
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

    private void displayNextBallsView() {
        // display the view of next balls
        ImageView imageView = null;
        int numOneTime = gridData.getBallNumOneTime();
        for (int i = 0; i < numOneTime; i++) {
            imageView = (ImageView) uiFragmentView.findViewById(nextBallsViewIdStart + i);
            drawBall(imageView, gridData.getNextBalls()[i]);
        }
        for (int i = numOneTime; i < nextBallsNumber; i++) {
            imageView = (ImageView) uiFragmentView.findViewById(nextBallsViewIdStart + i);
            imageView.setImageResource(R.drawable.next_ball_background_image);
        }
    }

    private void displayGameGridView() {
        // display the 9 x 9 game view
        ImageView imageView = null;
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 0; j < colCounts; j++) {
                int id = i * rowCounts + j;
                imageView = (ImageView) uiFragmentView.findViewById(id);
                int color = gridData.getCellValue(i, j);
                if (color == 0) {
                    imageView.setImageResource(R.drawable.boximage);
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

    private class StartHistoryScore extends AsyncTask<Void,Integer,ArrayList<Pair<String, Integer>>> {
        private Animation animationText = null;
        private ModalDialogFragment loadingDialog = null;

        public StartHistoryScore() {
            loadingDialog = new ModalDialogFragment();
            Bundle args = new Bundle();
            args.putString("textContent", getResources().getString(R.string.loadScore));
            args.putInt("color", Color.RED);
            args.putInt("width", 0);    // wrap_content
            args.putInt("height", 0);   // wrap_content
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

            loadingDialog.showDialogFragment(fmManager);
        }

        @Override
        protected ArrayList<Pair<String, Integer>> doInBackground(Void... params) {
            int i = 0;
            publishProgress(i);
            // String[] result = scoreSQLite.read10HighestScore();
            ArrayList<Pair<String, Integer>> resultList = scoreSQLite.readTop10ScoreList();

            // wait for one second
            try { Thread.sleep(1000); } catch (InterruptedException ex) { ex.printStackTrace(); }

            i = 1;
            publishProgress(i);

            // return result;
            return resultList;
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
        protected void onPostExecute(ArrayList<Pair<String, Integer>> resultList) {
            if (!isCancelled()) {
                loadingDialog.dismiss();

                ArrayList<Pair<String, Integer>> top10 = scoreSQLite.readTop10ScoreList();
                ArrayList<String> playerNames = new ArrayList<String>();
                ArrayList<Integer> playerScores = new ArrayList<Integer>();
                for (Pair pair : top10) {
                    playerNames.add((String)pair.first);
                    playerScores.add((Integer)pair.second);
                }

                Intent intent = new Intent(context, Top10ScoreActivity.class);
                Bundle extras = new Bundle();
                extras.putStringArrayList("Top10Players", playerNames);
                extras.putIntegerArrayList("Top10Scores", playerScores);
                intent.putExtras(extras);

                startActivity(intent);
            }
            AdBuddiz.showAd(mainActivity);   // added on 2017-10-24
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
            args.putInt("width", 0);    // wrap_content
            args.putInt("height", 0);   // wrap_content
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
                        ImageView v = (ImageView) uiFragmentView.findViewById(item.x * colCounts + item.y);
                        drawBall(v, color);
                    }
                    break;
                case 1:
                    for (Point item : hashPoint) {
                        ImageView v = (ImageView) uiFragmentView.findViewById(item.x * colCounts + item.y);
                        drawOval(v, color);
                    }
                    break;
                case 2:
                    scoreDialog.showDialogFragment(fmManager);
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

    // public methods
    public GridData getGridData() {
        return this.gridData;
    }

    public void displayNextColorBalls() {

        ImageView imageView = null;

        gridData.randColors();  //   next  balls
        //   display the balls on the nextBallsView
        displayNextBallsView();
    }

    public void newGame() {
        recordScore(1);   //   START A NEW GAME
    }

    public void recordScore(final int entryPoint) {
        final EditText et = new EditText(mainActivity);
        et.setTextSize(24);
        // et.setHeight(200);
        et.setTextColor(Color.BLUE);
        et.setBackground(new ColorDrawable(Color.TRANSPARENT));
        et.setHint(nameStr);
        et.setGravity(Gravity.CENTER);
        AlertDialog alertD = new AlertDialog.Builder(mainActivity).create();
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
                    mainActivity.finish();
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

                scoreSQLite.addScore(et.getText().toString(),currentScore);
                if (entryPoint==0) {
                    mainActivity.finish();
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

    public boolean getEasyLevel() {
        return this.easyLevel;
    }
    public void setEasyLevel(boolean yn) {
        this.easyLevel = yn;
    }
}
