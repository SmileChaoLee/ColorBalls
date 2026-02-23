package com.smile.fivecolorballs.presenters;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.smile.colorballs_main.constants.Constants;
import com.smile.colorballs_main.tools.LogUtil;
import com.smile.fivecolorballs.constants.FiveBallsConstants;
import com.smile.fivecolorballs.models.MyGameProp;
import com.smile.fivecolorballs.models.MyGridData;
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest;
import com.smile.smilelibraries.scoresqlite.ScoreSQLite;
import com.smile.smilelibraries.utilities.SoundPoolUtil;

public class MyPresenter {

    public static final int nextBallsViewIdStart = 100;

    private final static String TAG = "MyPresenter";
    private final static String GAME_PROP_TAG = "GamePropState";
    private final static String SAVED_GAME_FILENAME = "saved_game";
    private final MyPresentView mPresentView;
    private final SoundPoolUtil mSoundPool;
    private final Handler mBouncyHandler = new Handler(Looper.getMainLooper());
    private final Handler mMovingBallHandler = new Handler(Looper.getMainLooper());
    private final Handler mShowingScoreHandler = new Handler(Looper.getMainLooper());

    private int mRowCounts, mColCounts;
    private MyGameProp mGameProp;
    private MyGridData mGridData;

    public interface MyPresentView {
        String getLoadingStr();
        String geSavingGameStr();
        String getLoadingGameStr();
        String getSureToSaveGameStr();
        String getSureToLoadGameStr();
        String getSaveScoreStr();
        SoundPoolUtil soundPool();
        ScoreSQLite getScoreDatabase();
        FileInputStream fileInputStream(@NotNull String fileName);
        FileOutputStream fileOutputStream(@NotNull String fileName);
        HashMap<Integer, Bitmap> getColorBallMap();
        HashMap<Integer, Bitmap> getColorOvalBallMap();

        ImageView getImageViewById(int id);
        void updateHighestScoreOnUi(int highestScore);
        void updateCurrentScoreOnUi(int score);
        void showMessageOnScreen(String message);
        void dismissShowMessageOnScreen();
        void showSaveScoreAlertDialog(final int entryPoint);
        void showSaveGameDialog();
        void showLoadGameDialog();
        void showGameOverDialog();
    }

    public MyPresenter(MyPresentView presentView) {
        mPresentView = presentView;
        mSoundPool = mPresentView.soundPool();
    }

    // new added methods
    public boolean isProcessingJob() {
        return mGameProp.isProcessingJob();
    }

    public void doDrawBallsAndCheckListener(View v) {
        int i, j, id;
        id = v.getId();
        i = id / mRowCounts;
        j = id % mRowCounts;
        ImageView imageView;
        if (!mGameProp.isBallBouncing()) {
            if (mGridData.getCellValue(i, j) != 0) {
                if ((mGameProp.getBouncyBallIndexI() == -1) && (mGameProp.getBouncyBallIndexJ() == -1)) {
                    mGameProp.setBallBouncing(true);
                    drawBouncyBall((ImageView) v, mGridData.getCellValue(i, j));
                    mGameProp.setBouncyBallIndexI(i);
                    mGameProp.setBouncyBallIndexJ(j);
                }
            }
        } else {
            // cancel the timer
            if (mGridData.getCellValue(i, j) == 0) {
                //   blank cell
                int bouncyBallIndexI = mGameProp.getBouncyBallIndexI();
                int bouncyBallIndexJ = mGameProp.getBouncyBallIndexJ();
                if ((bouncyBallIndexI >= 0) && (bouncyBallIndexJ >= 0)) {
                    if (mGridData.canMoveCellToCell(new Point(bouncyBallIndexI, bouncyBallIndexJ), new Point(i, j))) {
                        // cancel the timer
                        mGameProp.setBallBouncing(false);
                        cancelBouncyTimer();
                        mGameProp.setBouncyBallIndexI(-1);
                        mGameProp.setBouncyBallIndexJ(-1);
                        drawBallAlongPath();
                        mGameProp.setUndoEnable(true);
                    } else {
                        //    make a sound
                        if (mGameProp.isHasSound()) {
                            mSoundPool.playSound();
                        }
                    }
                }
            } else {
                //  cell is not blank
                int bouncyBallIndexI = mGameProp.getBouncyBallIndexI();
                int bouncyBallIndexJ = mGameProp.getBouncyBallIndexJ();
                if ((bouncyBallIndexI >= 0) && (bouncyBallIndexJ >= 0)) {
                    cancelBouncyTimer();
                    imageView = mPresentView.getImageViewById(bouncyBallIndexI * mRowCounts + bouncyBallIndexJ);
                    drawBall(imageView , mGridData.getCellValue(bouncyBallIndexI, bouncyBallIndexJ));
                    drawBouncyBall((ImageView) v, mGridData.getCellValue(i, j));
                    mGameProp.setBouncyBallIndexI(i);
                    mGameProp.setBouncyBallIndexJ(j);
                }
            }
        }
    }

    public boolean initializeColorBallsGame(int rowCounts, int colCounts, Bundle savedInstanceState) {
        mRowCounts = rowCounts;
        mColCounts = colCounts;
        ScoreSQLite scoreDb = mPresentView.getScoreDatabase();
        int highestScore = scoreDb.readHighestScore();
        boolean isNewGame;
        if (savedInstanceState == null) {
            // activity just started so new game
            Log.d(TAG, "Created new game.");
            isNewGame = true;
            mGridData = new MyGridData(mRowCounts, mColCounts, Constants.NUM_BALLS_USED_EASY);
            mGameProp = new MyGameProp(mGridData);
        } else {
            Log.d(TAG, "Configuration changed and restore the original UI.");
            mGameProp = savedInstanceState.getParcelable(GAME_PROP_TAG);
            if (mGameProp != null) {
                isNewGame = false;
                mGridData = mGameProp.getGridData();
            } else {
                isNewGame = true;
                mGridData = new MyGridData(mRowCounts, mColCounts, Constants.NUM_BALLS_USED_EASY);
                mGameProp = new MyGameProp(mGridData);
            }
        }

        mPresentView.updateHighestScoreOnUi(highestScore);
        mPresentView.updateCurrentScoreOnUi(mGameProp.getCurrentScore());

        displayGameView();
        if (isNewGame) {
            displayGridDataNextCells();
        }

        return isNewGame;
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(GAME_PROP_TAG, mGameProp);
    }

    public boolean getIsEasyLevel() {
        return mGameProp.isEasyLevel();
    }

    public void setIsEasyLevel(boolean yn) {
        mGameProp.setEasyLevel(yn);
        if (mGameProp.isEasyLevel()) {
            // easy level
            mGridData.setNumOfColorsUsed(Constants.NUM_BALLS_USED_EASY);
        } else {
            // difficult
            mGridData.setNumOfColorsUsed(Constants.NUM_BALLS_USED_DIFF);
        }
    }

    public boolean getHasSound() {
        return mGameProp.isHasSound();
    }

    public void setHasSound(boolean hasSound) {
        mGameProp.setHasSound(hasSound);
    }

    public void setShowingNewGameDialog(boolean showingNewGameDialog) {
        mGameProp.setShowingNewGameDialog(showingNewGameDialog);
    }

    public void setShowingQuitGameDialog(boolean showingQuitGameDialog) {
        mGameProp.setShowingQuitGameDialog(showingQuitGameDialog);
    }

    public void undoTheLast() {
        if (!mGameProp.isUndoEnable()) {
            return;
        }
        mGameProp.setProcessingJob(true); // started undoing
        mGridData.undoTheLast();
        cancelBouncyTimer();
        mGameProp.setBallBouncing(false);
        mGameProp.setBouncyBallIndexI(-1);
        mGameProp.setBouncyBallIndexJ(-1);
        // restore the screen
        displayGameView();
        mGameProp.setCurrentScore(mGameProp.getUndoScore());
        mPresentView.updateCurrentScoreOnUi(mGameProp.getCurrentScore());
        mGameProp.setUndoEnable(false);
        mGameProp.setProcessingJob(false);  // finished
    }

    public void setSaveScoreAlertDialogState(int entryPoint, boolean state) {
        if (entryPoint== 1) {
            // new game
            setShowingNewGameDialog(state);
        } else {
            // quit game
            setShowingQuitGameDialog(state);
        }
    }

    public void setShowingSureSaveDialog(boolean isShowingSureSaveDialog) {
        mGameProp.setShowingSureSaveDialog(isShowingSureSaveDialog);
    }

    public void setShowingSureLoadDialog(boolean isShowingSureLoadDialog) {
        mGameProp.setShowingSureLoadDialog(isShowingSureLoadDialog);
    }

    public void setShowingGameOverDialog(boolean isShowingGameOverDialog) {
        mGameProp.setShowingGameOverDialog(isShowingGameOverDialog);
    }

    public void setShowingWarningSaveGameDialog(boolean isShowingWarningSaveGameDialog) {
        mGameProp.setShowingWarningSaveGameDialog(isShowingWarningSaveGameDialog);
    }

    public void saveScore(String playerName) {
        // removed on 2019-02-20 no global ranking any more
        // use thread to add a record to database (remote database)
        int score = mGameProp.getCurrentScore();
        Thread restThread = new Thread() {
            @Override
            public void run() {
                try {
                    // ASP.NET Cor
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("PlayerName", playerName);
                    jsonObject.put("Score", score);
                    jsonObject.put("GameId", FiveBallsConstants.FIVE_COLOR_BALLS_ID);
                    PlayerRecordRest.addOneRecord(jsonObject);
                } catch (Exception ex) {
                    LogUtil.e(TAG, "saveScore.Exception: ", ex);
                }
            }
        };
        restThread.start();

        ScoreSQLite scoreDb = mPresentView.getScoreDatabase();
        boolean isInTop10 = scoreDb.isInTop10(score);
        if (isInTop10) {
            // inside top 10
            // record the current score
            scoreDb.addScore(playerName, score);
            scoreDb.deleteAllAfterTop10();  // only keep the top 10
        }
    }

    public void newGame() {
        mPresentView.showSaveScoreAlertDialog(1);
    }

    public void quitGame() {
        mPresentView.showSaveScoreAlertDialog(0);
    }

    public void saveGame() {
        mPresentView.showSaveGameDialog();
    }

    public void loadGame() {
        mPresentView.showLoadGameDialog();
    }

    public boolean startSavingGame() {
        LogUtil.d(TAG, "startSavingGame");
        mGameProp.setProcessingJob(true);
        mPresentView.showMessageOnScreen(mPresentView.geSavingGameStr());
        boolean succeeded = false;
        try {
            FileOutputStream foStream = mPresentView.fileOutputStream(SAVED_GAME_FILENAME);
            if (foStream != null) {
                // save settings
                if (mGameProp.isHasSound()) {
                    foStream.write(1);
                } else {
                    foStream.write(0);
                }
                if (mGameProp.isEasyLevel()) {
                    foStream.write(1);
                } else {
                    foStream.write(0);
                }
                // save next balls
                foStream.write(FiveBallsConstants.BALL_NUM_ONE_TIME);
                for (int i = 0; i < Constants.NUM_BALLS_USED_DIFF; i++) {
                    foStream.write(mGridData.getNextBalls()[i]);
                }
                // save values on 9x9 grid
                for (int i = 0; i < mRowCounts; i++) {
                    for (int j = 0; j < mColCounts; j++) {
                        foStream.write(mGridData.getCellValue(i, j));
                    }
                }
                // save current score
                byte[] scoreByte = ByteBuffer.allocate(4).putInt(mGameProp.getCurrentScore()).array();
                foStream.write(scoreByte);
                // save undoEnable
                if (mGameProp.isUndoEnable()) {
                    // can undo
                    Log.d(TAG, "startSavingGame.can undo");
                    foStream.write(1);
                    foStream.write(FiveBallsConstants.BALL_NUM_ONE_TIME);
                    // save undoNextBalls
                    for (int i = 0; i < Constants.NUM_BALLS_USED_DIFF; i++) {
                        foStream.write(mGridData.getUndoNextBalls()[i]);
                    }
                    // save backupCells
                    for (int i = 0; i < mRowCounts; i++) {
                        for (int j = 0; j < mColCounts; j++) {
                            foStream.write(mGridData.getBackupCells()[i][j]);
                        }
                    }
                    byte[] undoScoreByte = ByteBuffer.allocate(4).putInt(mGameProp.getUndoScore()).array();
                    foStream.write(undoScoreByte);
                } else {
                    Log.d(TAG, "startSavingGame.no undo");
                    // no undo
                    foStream.write(0);
                }
                // end of writing
                foStream.close();
                succeeded = true;
                Log.d(TAG, "startSavingGame.Succeeded");
            }
        } catch (IOException ex) {
            LogUtil.e(TAG, "startSavingGame.IOException: ", ex);
        }

        mGameProp.setProcessingJob(false);
        mPresentView.dismissShowMessageOnScreen();

        Log.d(TAG, "startSavingGame() finished");

        return succeeded;
    }

    public boolean startLoadingGame() {
        Log.i(TAG, "startLoadingGame");
        mGameProp.setProcessingJob(true);
        mPresentView.showMessageOnScreen(mPresentView.getLoadingGameStr());

        boolean soundYn = mGameProp.isHasSound();
        boolean easyYn = mGameProp.isEasyLevel();
        int ballNumOneTime;
        int[] nextBalls = new int[Constants.NUM_BALLS_USED_DIFF];
        int[][] gameCells = new int[mRowCounts][mColCounts];
        int cScore = mGameProp.getCurrentScore();
        boolean undoYn = mGameProp.isUndoEnable();
        int[] undoNextBalls = new int[Constants.NUM_BALLS_USED_DIFF];
        int[][] backupCells = new int[mRowCounts][mColCounts];
        int unScore = mGameProp.getUndoScore();

        boolean succeeded = false;
        try {
            FileInputStream fiStream = mPresentView.fileInputStream(SAVED_GAME_FILENAME);
            if (fiStream != null) {
                int bValue = fiStream.read();
                if (bValue == 1) {
                    // has sound
                    Log.i(TAG, "startLoadingGame.Game has sound");
                    soundYn = true;
                } else {
                    // has no sound
                    Log.i(TAG, "startLoadingGame.Game has no sound");
                    soundYn = false;
                }
                bValue = fiStream.read();
                if (bValue == 1) {
                    // easy level
                    Log.i(TAG, "startLoadingGame.Game is easy level");
                    easyYn = true;

                } else {
                    // difficult level
                    Log.i(TAG, "startLoadingGame.Game is difficult level");
                    easyYn = false;
                }
                ballNumOneTime = fiStream.read();
                Log.i(TAG, "startLoadingGame.Game has " + ballNumOneTime + " next balls");
                for (int i = 0; i < Constants.NUM_BALLS_USED_DIFF; i++) {
                    nextBalls[i] = fiStream.read();
                    Log.i(TAG, "startLoadingGame.Next ball value = " + nextBalls[i]);
                }
                for (int i = 0; i < mRowCounts; i++) {
                    for (int j = 0; j < mColCounts; j++) {
                        gameCells[i][j] = fiStream.read();
                        Log.i(TAG, "startLoadingGame.Value of ball at (" + i + ", " + j + ") = " + gameCells[i][j]);
                    }
                }
                // reading current score
                byte[] scoreByte = new byte[4];
                fiStream.read(scoreByte);
                cScore = ByteBuffer.wrap(scoreByte).getInt();
                Log.i(TAG, "startLoadingGame.Current score = " + cScore);
                // reading undoEnable
                bValue = fiStream.read();
                if (bValue == 1) {
                    // has undo data
                    Log.i(TAG, "startLoadingGame.Game has undo data");
                    undoYn = true;
                    // undoNumOneTime = fiStream.read();
                    fiStream.read();
                    for (int i = 0; i < Constants.NUM_BALLS_USED_DIFF; i++) {
                        undoNextBalls[i] = fiStream.read();
                    }
                    // save backupCells
                    for (int i = 0; i < mRowCounts; i++) {
                        for (int j = 0; j < mColCounts; j++) {
                            backupCells[i][j] = fiStream.read();
                        }
                    }
                    byte[] undoScoreByte = new byte[4];
                    fiStream.read(undoScoreByte);
                    unScore = ByteBuffer.wrap(undoScoreByte).getInt();
                    Log.i(TAG, "startLoadingGame.undoScore = " + unScore);
                } else {
                    // does not has undo data
                    Log.i(TAG, "startLoadingGame.Game does not has undo data");
                    undoYn = false;
                }
                fiStream.close();
                succeeded = true;
            }
        } catch (IOException ex) {
            LogUtil.e(TAG, "startLoadingGame.IOException: ", ex);
        }

        mPresentView.dismissShowMessageOnScreen();

        if (succeeded) {
            // refresh Main UI with loaded data
            setHasSound(soundYn);
            setIsEasyLevel(easyYn);
            mGridData.setNextBalls(nextBalls);
            mGridData.setCellValues(gameCells);
            mGameProp.setCurrentScore(cScore);
            mGameProp.setUndoEnable(undoYn);
            mGridData.setUndoNextBalls(undoNextBalls);
            mGridData.setBackupCells(backupCells);
            mGameProp.setUndoScore(unScore);
            // start update UI
            mPresentView.updateCurrentScoreOnUi(mGameProp.getCurrentScore());
            displayGameView();
        }

        mGameProp.setProcessingJob(false);

        return succeeded;
    }

    public void release() {
        cancelBouncyTimer();
        mShowingScoreHandler.removeCallbacksAndMessages(null);
        mMovingBallHandler.removeCallbacksAndMessages(null);
        mSoundPool.release();
    }

    private void gameOver() {
        mPresentView.showGameOverDialog();
    }

    private int calculateScore(int numBalls) {
        // 5 balls --> 5
        // 6 balls --> 5 + (6-5)*2
        // 7 balls --> 5 + (6-5)*2 + (7-5)*2
        // 8 balls --> 5 + (6-5)*2 + (7-5)*2 + (8-5)*2
        // n balls --> 5 + (6-5)*2 + (7-5)*2 + ... + (n-5)*2
        int minBalls = 5;
        int score = 5;
        int extraBalls = numBalls - minBalls;
        if (extraBalls > 0) {
            // greater than 5 balls
            int rate  = 2;
            for (int i=1 ; i<=extraBalls ; i++) {
                // rate = 2;   // added on 2018-10-02
                score += i * rate ;
            }
        }

        if (!mGameProp.isEasyLevel()) {
            // difficult level
            score = score * 2;   // double of easy level
        }

        return score;
    }

    private void drawBall(ImageView imageView, int color) {
        imageView.setImageBitmap(mPresentView.getColorBallMap().get(color));
    }

    private void drawOval(ImageView imageView,int color) {
        imageView.setImageBitmap(mPresentView.getColorOvalBallMap().get(color));
    }

    private void displayNextBallsView() {
        // display the view of next balls
        ImageView imageView;
        // int numOneTime = gridData.ballNumOneTime;
        int numOneTime = FiveBallsConstants.BALL_NUM_ONE_TIME;
        for (int i = 0; i < numOneTime; i++) {
            imageView = mPresentView.getImageViewById(nextBallsViewIdStart + i);
            drawBall(imageView, mGridData.getNextBalls()[i]);
        }
    }

    private void displayNextColorBalls() {
        mGridData.randColors();  //   next  balls
        //   display the balls on the nextBallsView
        displayNextBallsView();
    }

    private void clearCell(int i, int j) {
        // int id = i * colCounts + j;
        int id = i * mRowCounts + j;
        ImageView imageView = mPresentView.getImageViewById(id);
        // imageView.setImageDrawable(null);
        imageView.setImageBitmap(null);
        mGridData.setCellValue(i, j, 0);
    }

    private void displayGridDataNextCells() {
        mGridData.randCells();
        int id, n1, n2;
        ImageView imageView;
        boolean hasMoreFive = false;
        HashSet<Point> linkedPoint = new HashSet<>();
        for (Point nextCellIndex : mGridData.getNextCellIndex()) {
            n1 = nextCellIndex.x;
            n2 = nextCellIndex.y;
            id = n1 * mRowCounts + n2;
            imageView = mPresentView.getImageViewById(id);
            drawBall(imageView, mGridData.getCellValue(n1, n2));
            if (mGridData.check_moreThanFive(n1, n2)) {
                hasMoreFive = true;
                for (Point point : mGridData.getLight_line()) {
                    if (!linkedPoint.contains(point)) {
                        linkedPoint.add(new Point(point));
                    }
                }
            }
        }

        if (hasMoreFive) {
            mGridData.setLight_line(linkedPoint);    // added on 2020-07-13
            mGameProp.setLastGotScore(calculateScore(mGridData.getLight_line().size()));
            mGameProp.setUndoScore(mGameProp.getCurrentScore());
            mGameProp.setCurrentScore(mGameProp.getCurrentScore() + mGameProp.getLastGotScore());
            mPresentView.updateCurrentScoreOnUi(mGameProp.getCurrentScore());
            ShowScoreRunnable showScoreRunnable = new ShowScoreRunnable(mGameProp.getLastGotScore(), mGridData.getLight_line(), true);
            mShowingScoreHandler.post(showScoreRunnable);
            Log.d(TAG,"displayGridDataNextCells() --> showingScoreHandler.post(showScoreRunnable).");
        } else {
            // check if game over
            boolean gameOverYn = mGridData.getGameOver();
            if (gameOverYn) {
                //  game over
                gameOver();
            } else {
                // game has not been over yet
                displayNextColorBalls();
            }
        }
    }

    private void displayGameGridView() {
        // display the 9 x 9 game view
        ImageView imageView;
        for (int i = 0; i < mRowCounts; i++) {
            for (int j = 0; j < mColCounts; j++) {
                int id = i * mRowCounts + j;
                imageView = mPresentView.getImageViewById(id);
                int color = mGridData.getCellValue(i, j);
                if (color == 0) {
                    imageView.setImageBitmap(null);
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

    private void drawBallAlongPath() {
        if (mGridData.getPathPoint().isEmpty()) return;
        int sizeOfPathPoint = mGridData.getPathPoint().size();
        final int ii = mGridData.getPathPoint().get(0).x;  // the target point
        final int jj = mGridData.getPathPoint().get(0).y;  // the target point
        final int beginI = mGridData.getPathPoint().get(sizeOfPathPoint-1).x;
        final int beginJ = mGridData.getPathPoint().get(sizeOfPathPoint-1).y;
        final int color = mGridData.getCellValue(beginI, beginJ);

        mGameProp.getThreadCompleted()[0] = false;
        mGameProp.setBallMoving(true);

        clearCell(beginI, beginJ);

        final List<Point> tempList = new ArrayList<>(mGridData.getPathPoint());
        Runnable runnablePath = new Runnable() {
            boolean ballYN = true;
            ImageView imageView = null;
            int countDown = tempList.size()*2 - 1;
            @Override
            public synchronized void run() {
                if (countDown >= 2) {   // eliminate start point
                    int i = countDown / 2;
                    imageView = mPresentView.getImageViewById(tempList.get(i).x * mRowCounts + tempList.get(i).y);
                    if (ballYN) {
                        drawBall(imageView, color);
                    } else {
                        imageView.setImageBitmap(null);
                    }
                    ballYN = !ballYN;
                    countDown--;
                    mMovingBallHandler.postDelayed(this,20);
                    Log.d(TAG,"drawBallAlongPath.ballMovingHandler.postDelayed()");
                } else {
                    // mMovingBallHandler.removeCallbacksAndMessages(null);
                    ImageView v = mPresentView.getImageViewById(ii * mRowCounts + jj);
                    mGridData.setCellValue(ii, jj, color);
                    drawBall(v, color);
                    //  check if there are more than five balls with same color connected together
                    if (mGridData.check_moreThanFive(ii, jj)) {
                        mGameProp.setLastGotScore(calculateScore(mGridData.getLight_line().size()));
                        mGameProp.setUndoScore(mGameProp.getCurrentScore());
                        mGameProp.setCurrentScore(mGameProp.getCurrentScore() + mGameProp.getLastGotScore());
                        mPresentView.updateCurrentScoreOnUi(mGameProp.getCurrentScore());
                        ShowScoreRunnable showScoreRunnable = new ShowScoreRunnable(mGameProp.getLastGotScore(),
                                mGridData.getLight_line(), false);
                        mShowingScoreHandler.post(showScoreRunnable);
                        Log.d(TAG,"drawBallAlongPath.showingScoreHandler.post(showScoreRunnable).");
                    } else {
                        displayGridDataNextCells();   // has a problem
                        Log.d(TAG,"drawBallAlongPath.displayGridDataNextCells().");
                    }

                    mGameProp.getThreadCompleted()[0] = true;
                    mGameProp.setBallMoving(false);

                    Log.d(TAG,"drawBallAlongPath() --> run() finished.");
                }
            }
        };
        mMovingBallHandler.post(runnablePath);
        Log.d(TAG,"drawBallAlongPath() --> ballMovingHandler.post()");
    }

    private void drawBouncyBall(final ImageView v, final int color) {
        Runnable bouncyRunnable = new Runnable() {
            boolean ballYN = false;
            @Override
            public void run() {
                if (color != 0) {
                    if (ballYN) {
                        drawBall(v , color);
                    } else {
                        drawOval(v , color);
                    }
                    ballYN = !ballYN;
                    mBouncyHandler.postDelayed(this, 200);
                } else {
                    // v.setImageResource(R.drawable.boximage);
                    v.setImageDrawable(null);
                }
            }
        };
        mBouncyHandler.post(bouncyRunnable);
    }

    private void cancelBouncyTimer() {
        mBouncyHandler.removeCallbacksAndMessages(null);
        SystemClock.sleep(20);
    }

    private class ShowScoreRunnable implements Runnable {
        private final int color;
        private final int lastGotScore;
        private HashSet<Point> hasPoint = null;
        private final boolean isNextBalls;
        private int counter = 0;

        public ShowScoreRunnable(int lastGotScore, HashSet<Point> linkedPoint, boolean isNextBalls) {
            this.lastGotScore = lastGotScore;
            this.isNextBalls = isNextBalls;
            int colorTmp = 0;
            if (linkedPoint != null) {
                hasPoint = new HashSet<>(linkedPoint);
                Point point = hasPoint.iterator().next();
                colorTmp = mGridData.getCellValue(point.x, point.y);
            }
            color = colorTmp;

            mGameProp.setShowNextBallsAfterBlinking(this.isNextBalls);
            mGameProp.getThreadCompleted()[1] = false;
            mGameProp.setShowingScoreMessage(true);
        }

        private synchronized void onProgressUpdate(int status) {
            switch (status) {
                case 0:
                    for (Point item : hasPoint) {
                        ImageView v = mPresentView.getImageViewById(item.x * mRowCounts + item.y);
                        drawBall(v, color);
                    }
                    break;
                case 1:
                    for (Point item : hasPoint) {
                        ImageView v = mPresentView.getImageViewById(item.x * mRowCounts + item.y);
                        drawOval(v, color);
                    }
                    break;
                case 2:
                    break;
                case 3:
                    //
                    // show the score
                    String scoreString = String.valueOf(lastGotScore);
                    LogUtil.d(TAG, "ShowScoreRunnable.onProgressUpdate.showMessageOnScreen");
                    mPresentView.showMessageOnScreen(scoreString);
                    LogUtil.d(TAG, "ShowScoreRunnable.onProgressUpdate.clearCell");
                    for (Point item : hasPoint) {
                        clearCell(item.x, item.y);
                    }
                    // added on 2019-03-30
                    if (isNextBalls) {
                        LogUtil.d(TAG, "ShowScoreRunnable.onProgressUpdate.displayNextColorBalls");
                        displayNextColorBalls();
                    }
                    //
                    mGameProp.getThreadCompleted()[1] = true;  // user can start input command
                    LogUtil.d(TAG, "ShowScoreRunnable.onProgressUpdate.setShowingScoreMessage");
                    mGameProp.setShowingScoreMessage(false);
                    break;
                case 4:
                    LogUtil.d(TAG, "ShowScoreRunnable.onProgressUpdate.dismissShowMessageOnScreen().");
                    mPresentView.dismissShowMessageOnScreen();
                    break;
            }
        }

        @Override
        public synchronized void run() {
            if (hasPoint == null) {
                LogUtil.d(TAG, "ShowScoreRunnable.run.hasPoint is null.");
                mShowingScoreHandler.removeCallbacksAndMessages(null);
            } else {
                counter++;
                int twinkleCountDown = 5;
                if (counter <= twinkleCountDown) {
                    int md = counter % 2; // modulus
                    onProgressUpdate(md);
                    mShowingScoreHandler.postDelayed(this, 100);
                } else {
                    if (counter == twinkleCountDown +1) {
                        onProgressUpdate(3);    // show score
                        mShowingScoreHandler.postDelayed(this, 500);
                    } else {
                        onProgressUpdate(4);    // dismiss showing message
                    }
                }
            }
        }
    }
}
