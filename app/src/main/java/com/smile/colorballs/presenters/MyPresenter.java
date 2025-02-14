package com.smile.colorballs.presenters;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.os.BundleCompat;

import com.smile.colorballs.ColorBallsApp;
import com.smile.colorballs.constants.Constants;
import com.smile.colorballs.R;
import com.smile.colorballs.interfaces.PresentView;
import com.smile.colorballs.models.GameProperties;
import com.smile.colorballs.models.GridData;
import com.smile.smilelibraries.player_record_rest.PlayerRecordRest;
import com.smile.smilelibraries.scoresqlite.ScoreSQLite;
import com.smile.smilelibraries.utilities.FontAndBitmapUtil;
import com.smile.smilelibraries.utilities.SoundPoolUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MyPresenter {
    public static final int NumOfColorsUsedByEasy = 5;          // 5 colors for easy level
    public static final int NumOfColorsUsedByDifficult = 6;    // 6 colors for difficult level
    // 10->RED, 20->GREEN, 30->BLUE, 40->MAGENTA, 50->YELLOW, 60->Cyan
    public static final int ColorRED = 10;
    public static final int ColorGREEN = 20;
    public static final int ColorBLUE = 30;
    public static final int ColorMAGENTA = 40;
    public static final int ColorYELLOW = 50;
    public static final int ColorCYAN = 60;
    public static final int[] ballColor = new int[] {ColorRED, ColorGREEN, ColorBLUE, ColorMAGENTA, ColorYELLOW, ColorCYAN};
    public static HashMap<Integer, Drawable> colorBallMap;
    public static HashMap<Integer, Drawable> colorOvalBallMap;
    public static HashMap<Integer, Drawable> colorNextBallMap;
    private final String NumOfSavedGameFileName = "num_saved_game";
    private final String TAG = "MyPresenter";
    private final String GamePropertiesTag = "GameProperties";
    private final String savedGameFileName = "saved_game";
    private final Activity mActivity;
    private final PresentView mPresentView;
    private final SoundPoolUtil soundPoolUtil;
    private AnimationDrawable bouncyAnimation;
    private final Handler movingBallHandler = new Handler(Looper.getMainLooper());
    private final Handler showingScoreHandler = new Handler(Looper.getMainLooper());
    private int mRowCounts, mColCounts;
    private GameProperties mGameProperties;
    private GridData mGridData;

    private interface ShowScoreCallback {
        void sCallback();
    }

    public MyPresenter(Activity activity) {
        mActivity = activity;
        mPresentView = (PresentView) mActivity;
        soundPoolUtil = new SoundPoolUtil(mActivity, R.raw.uhoh);
        colorBallMap = new HashMap<>();
        colorOvalBallMap = new HashMap<>();
        colorNextBallMap = new HashMap<>();
    }

    public void setRowCounts(int rowCounts) {
        mRowCounts = rowCounts;
    }

    public void setColCounts(int colCounts) {
        mColCounts = colCounts;
    }

    public void drawBallsAndCheckListener(View v) {
        int id = v.getId();
        int i = getRow(id);
        int j = getColumn(id);
        ImageView imageView;
        if (!mGameProperties.isBallBouncing()) {
            if (mGridData.getCellValue(i, j) != 0) {
                if ((mGameProperties.getBouncyBallIndexI() == -1) && (mGameProperties.getBouncyBallIndexJ() == -1)) {
                    mGameProperties.setBallBouncing(true);
                    drawBouncyBall((ImageView) v, mGridData.getCellValue(i, j));
                    mGameProperties.setBouncyBallIndexI(i);
                    mGameProperties.setBouncyBallIndexJ(j);
                }
            }
        } else {
            // cancel the timer
            if (mGridData.getCellValue(i, j) == 0) {
                //   blank cell
                int bouncyBallIndexI = mGameProperties.getBouncyBallIndexI();
                int bouncyBallIndexJ = mGameProperties.getBouncyBallIndexJ();
                if ((bouncyBallIndexI >= 0) && (bouncyBallIndexJ >= 0)) {
                    if (mGridData.canMoveCellToCell(new Point(bouncyBallIndexI, bouncyBallIndexJ), new Point(i, j))) {
                        // cancel the timer
                        mGameProperties.setBallBouncing(false);
                        stopBouncyAnimation();
                        mGameProperties.setBouncyBallIndexI(-1);
                        mGameProperties.setBouncyBallIndexJ(-1);
                        drawBallAlongPath();
                        mGameProperties.setUndoEnable(true);
                    } else {
                        //    make a sound
                        if (mGameProperties.hasSound()) {
                            soundPoolUtil.playSound();
                        }
                    }
                }
            } else {
                //  cell is not blank
                int bouncyBallIndexI = mGameProperties.getBouncyBallIndexI();
                int bouncyBallIndexJ = mGameProperties.getBouncyBallIndexJ();
                if ((bouncyBallIndexI >= 0) && (bouncyBallIndexJ >= 0)) {
                    stopBouncyAnimation();
                    imageView = mPresentView.getImageViewById(getImageId(bouncyBallIndexI, bouncyBallIndexJ));
                    drawBall(imageView , mGridData.getCellValue(bouncyBallIndexI, bouncyBallIndexJ));
                    drawBouncyBall((ImageView) v, mGridData.getCellValue(i, j));
                    mGameProperties.setBouncyBallIndexI(i);
                    mGameProperties.setBouncyBallIndexJ(j);
                }
            }
        }
    }

    public boolean initializeColorBallsGame(int cellWidth, int cellHeight, Bundle savedInstanceState) {

        createBitmapsAndDrawableResources(cellWidth, cellHeight);

        ScoreSQLite scoreSQLiteDB = new ScoreSQLite(mActivity.getApplicationContext());
        int highestScore = scoreSQLiteDB.readHighestScore();
        scoreSQLiteDB.close();

        boolean isNewGame = true;
        if (savedInstanceState == null) {
            // activity just started so new game
            Log.d(TAG, "initializeColorBallsGame.savedInstanceState is null");
        } else {
            Log.d(TAG, "initializeColorBallsGame.Configuration changed and restore the original UI.");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {    // API 33
                mGameProperties = BundleCompat.getParcelable(savedInstanceState,
                        GamePropertiesTag, GameProperties.class);
            } else mGameProperties = savedInstanceState.getParcelable(GamePropertiesTag);
            if (mGameProperties != null) {
                mGridData = mGameProperties.getGridData();
                if (mGridData != null) {
                    isNewGame = false;
                }
            }
        }
        if (isNewGame) {
            Log.d(TAG, "initializeColorBallsGame.new game.");
            mGridData = new GridData(mRowCounts, mColCounts, NumOfColorsUsedByEasy);
            mGameProperties = new GameProperties(mGridData);
        }

        ColorBallsApp.isShowingLoadingMessage = mGameProperties.isShowingLoadingMessage();
        ColorBallsApp.isProcessingJob = mGameProperties.isProcessingJob();

        mPresentView.updateHighestScoreOnUi(highestScore);
        mPresentView.updateCurrentScoreOnUi(mGameProperties.getCurrentScore());

        displayGameView();
        if (isNewGame) {
            displayGridDataNextCells();
        } else {
            // display the original state before changing configuration
            // need to be tested
            if (ColorBallsApp.isShowingLoadingMessage) {
                mPresentView.showMessageOnScreen(mActivity.getString(R.string.loadingStr));
            }
            //
            if (mGameProperties.isBallMoving()) {
                Log.d(TAG, "initializeColorBallsGame.gameProperties.isBallMoving() is true");
                drawBallAlongPath();
            }
            if (mGameProperties.isShowingScoreMessage()) {
                Log.d(TAG, "initializeColorBallsGame.gameProperties.isShowingScoreMessage() is true");
                ShowScoreRunnable showScoreRunnable = new ShowScoreRunnable(mGameProperties.getLastGotScore(),
                        mGridData.getLightLine(), mGameProperties.isShowNextBallsAfterBlinking(),
                        new ShowScoreCallback() {
                            @Override
                            public void sCallback() {
                                lastPartOfInitialGame();
                            }
                        });
                Log.d(TAG,"initializeColorBallsGame.showingScoreHandler.post().");
                showingScoreHandler.post(showScoreRunnable);
                Log.d(TAG,"initializeColorBallsGame.showingScoreHandler.post() run.");
            } else {
                lastPartOfInitialGame();
            }
        }
        return isNewGame;
    }

    private void lastPartOfInitialGame() {
        if (mGameProperties.isBallBouncing()) {
            int bouncyBallIndexI = mGameProperties.getBouncyBallIndexI();
            int bouncyBallIndexJ = mGameProperties.getBouncyBallIndexJ();
            ImageView v = mPresentView.getImageViewById(getImageId(bouncyBallIndexI, bouncyBallIndexJ));
            drawBouncyBall(v, mGridData.getCellValue(bouncyBallIndexI, bouncyBallIndexJ));
        }
        if (mGameProperties.isShowingNewGameDialog()) {
            Log.d(TAG, "initializeColorBallsGame.show new game dialog by calling newGame()");
            newGame();
        }
        if (mGameProperties.isShowingQuitGameDialog()) {
            Log.d(TAG, "initializeColorBallsGame.show quit game dialog by calling quitGame()");
            quitGame();
        }
        if (mGameProperties.isShowingSureSaveDialog()) {
            Log.d(TAG, "initializeColorBallsGame.isShowingSureSaveDialog()");
            saveGame();
        }
        if (mGameProperties.isShowingSureLoadDialog()) {
            Log.d(TAG, "initializeColorBallsGame.isShowingSureLoadDialog()");
            loadGame();
        }
        if (mGameProperties.isShowingGameOverDialog()) {
            Log.d(TAG, "initializeColorBallsGame.isShowingGameOverDialog()");
            gameOver();
        }
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        mGameProperties.setShowingLoadingMessage(ColorBallsApp.isShowingLoadingMessage);
        mGameProperties.setProcessingJob(ColorBallsApp.isProcessingJob);
        outState.putParcelable(GamePropertiesTag, mGameProperties);
    }

    public boolean completedAll() {
        for (boolean thCompleted : mGameProperties.getThreadCompleted()) {
            if (!thCompleted) {
                return false;
            }
        }
        return true;
    }

    public boolean hasSound() {
        return mGameProperties.hasSound();
    }

    public void setHasSound(boolean hasSound) {
        mGameProperties.setHasSound(hasSound);
    }

    public boolean isEasyLevel() {
        return mGameProperties.isEasyLevel();
    }

    public void setEasyLevel(boolean yn) {
        mGameProperties.setEasyLevel(yn);
        if (mGameProperties.isEasyLevel()) {
            // easy level
            mGridData.setNumOfColorsUsed(NumOfColorsUsedByEasy);
        } else {
            // difficult
            mGridData.setNumOfColorsUsed(NumOfColorsUsedByDifficult);
        }
    }

    public boolean hasNextBall() {
        return mGameProperties.hasNextBall();
    }

    public void setHasNextBall(boolean hasNextBall, boolean isNextBalls) {
        mGameProperties.setHasNextBall(hasNextBall);
        if (isNextBalls) {
            displayNextBallsView();
        }
    }

    public void setShowingNewGameDialog(boolean showingNewGameDialog) {
        mGameProperties.setShowingNewGameDialog(showingNewGameDialog);
    }

    public void setShowingQuitGameDialog(boolean showingQuitGameDialog) {
        mGameProperties.setShowingQuitGameDialog(showingQuitGameDialog);
    }

    public void undoTheLast() {
        if (!mGameProperties.isUndoEnable()) {
            return;
        }

        ColorBallsApp.isProcessingJob = true; // started undoing

        mGridData.undoTheLast();

        stopBouncyAnimation();
        mGameProperties.setBallBouncing(false);
        mGameProperties.setBouncyBallIndexI(-1);
        mGameProperties.setBouncyBallIndexJ(-1);

        // restore the screen
        displayGameView();

        mGameProperties.setCurrentScore(mGameProperties.getUndoScore());
        mPresentView.updateCurrentScoreOnUi(mGameProperties.getCurrentScore());

        // completedPath = true;
        mGameProperties.setUndoEnable(false);

        ColorBallsApp.isProcessingJob = false;    // finished
    }

    public void setSaveScoreAlertDialogState(int entryPoint, boolean state) {
        ColorBallsApp.isProcessingJob = state;
        if (entryPoint== 1) {
            // new game
            setShowingNewGameDialog(state);
        } else {
            // quit game
            setShowingQuitGameDialog(state);
        }
    }

    public void setShowingSureSaveDialog(boolean isShowingSureSaveDialog) {
        mGameProperties.setShowingSureSaveDialog(isShowingSureSaveDialog);
    }

    public void setShowingSureLoadDialog(boolean isShowingSureLoadDialog) {
        mGameProperties.setShowingSureLoadDialog(isShowingSureLoadDialog);
    }

    public void setShowingGameOverDialog(boolean isShowingGameOverDialog) {
        mGameProperties.setShowingGameOverDialog(isShowingGameOverDialog);
    }

    public void setShowingWarningSaveGameDialog(boolean isShowingWarningSaveGameDialog) {
        mGameProperties.setShowingWarningSaveGameDialog(isShowingWarningSaveGameDialog);
    }

    public void saveScore(String playerName, int score) {
        // use thread to add a record to remote database
        Thread restThread = new Thread() {
            @Override
            public void run() {
                try {
                    // ASP.NET Core
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("PlayerName", playerName);
                    jsonObject.put("Score", score);
                    jsonObject.put("GameId", Constants.GAME_ID);
                    PlayerRecordRest.addOneRecord(jsonObject);
                    Log.d(TAG, "saveScore.Succeeded to add one record to remote.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.d(TAG, "saveScore.Failed to add one record to remote.");
                }
            }
        };
        restThread.start();

        ScoreSQLite scoreSQLiteDB = new ScoreSQLite(mActivity.getApplicationContext());
        boolean isInTop10 = scoreSQLiteDB.isInTop10(score);
        if (isInTop10) {
            // inside top 10
            // record the current score
            scoreSQLiteDB.addScore(playerName, score);
            scoreSQLiteDB.deleteAllAfterTop10();  // only keep the top 10
        }
        scoreSQLiteDB.close();
    }

    public void newGame() {
        mPresentView.showSaveScoreAlertDialog(1, mGameProperties.getCurrentScore());
    }

    public void quitGame() {
        mPresentView.showSaveScoreAlertDialog(0, mGameProperties.getCurrentScore());
    }

    public void saveGame() {
        mPresentView.showSaveGameDialog();
    }

    public void loadGame() {
        mPresentView.showLoadGameDialog();
    }

    public int readNumberOfSaved() {
        Log.d(TAG, "readNumberOfSaved.activity = " + mActivity);
        int numOfSaved = 0;
        try {
            File inputFile = new File(mActivity.getFilesDir(), NumOfSavedGameFileName);
            FileInputStream fiStream = new FileInputStream(inputFile);
            numOfSaved = fiStream.read();
            fiStream.close();
        } catch (IOException ex) {
            Log.d(TAG, "readNumberOfSaved.IOException");
            ex.printStackTrace();
        }
        return numOfSaved;
    }

    public boolean startSavingGame(int numOfSaved) {
        Log.d(TAG, "startSavingGame");

        ColorBallsApp.isProcessingJob = true;
        mPresentView.showMessageOnScreen(mActivity.getString(R.string.savingGameStr));

        boolean succeeded = true;
        try {
            File outputFile = new File(mActivity.getFilesDir(), savedGameFileName);
            FileOutputStream foStream = new FileOutputStream(outputFile);
            // save settings
            Log.d(TAG, "startSavingGame.hasSound = " + mGameProperties.hasSound());
            if (mGameProperties.hasSound()) {
                foStream.write(1);
            } else {
                foStream.write(0);
            }
            Log.d(TAG, "startSavingGame.isEasyLevel = " + mGameProperties.isEasyLevel());
            if (mGameProperties.isEasyLevel()) {
                foStream.write(1);
            } else {
                foStream.write(0);
            }
            Log.d(TAG, "startSavingGame.hasNextBall = " + mGameProperties.hasNextBall());
            if (mGameProperties.hasNextBall()) {
                foStream.write(1);
            } else {
                foStream.write(0);
            }
            // save next balls
            // foStream.write(gridData.ballNumOneTime);
            Log.d(TAG, "startSavingGame.ballNumOneTime = " + GridData.mBallNumOneTime);
            foStream.write(GridData.mBallNumOneTime);
            for (HashMap.Entry<Point, Integer> entry : mGridData.getNextCellIndices().entrySet()) {
                Log.d(TAG, "startSavingGame.nextCellIndices.getValue() = " + entry.getValue());
                foStream.write(entry.getValue());
            }
            int sz = mGridData.getNextCellIndices().size();
            for (int i = sz; i<NumOfColorsUsedByDifficult; i++) {
                Log.d(TAG, "startSavingGame.nextCellIndices.getValue() = " + 0);
                foStream.write(0);
            }
            Log.d(TAG, "startSavingGame.getNextCellIndices.size() = " + sz);
            foStream.write(sz);
            for (HashMap.Entry<Point, Integer> entry : mGridData.getNextCellIndices().entrySet()) {
                Log.d(TAG, "startSavingGame.nextCellIndices.getKey().x = " + entry.getKey().x);
                Log.d(TAG, "startSavingGame.nextCellIndices.getKey().y = " + entry.getKey().y);
                foStream.write(entry.getKey().x);
                foStream.write(entry.getKey().y);
            }
            Log.d(TAG, "startSavingGame.getUndoNextCellIndices().size() = " + mGridData.getUndoNextCellIndices().size());
            foStream.write(mGridData.getUndoNextCellIndices().size());
            for (HashMap.Entry<Point, Integer> entry : mGridData.getUndoNextCellIndices().entrySet()) {
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getKey().x = " + entry.getKey().x);
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getKey().y = " + entry.getKey().y);
                foStream.write(entry.getKey().x);
                foStream.write(entry.getKey().y);
            }
            // save values on 9x9 grid
            for (int i = 0; i< mRowCounts; i++) {
                for (int j = 0; j< mColCounts; j++) {
                    Log.d(TAG, "startSavingGame.gridData.getCellValue(i, j) = " + mGridData.getCellValue(i, j));
                    foStream.write(mGridData.getCellValue(i, j));
                }
            }
            // save current score
            byte[] scoreByte = ByteBuffer.allocate(4).putInt(mGameProperties.getCurrentScore()).array();
            Log.d(TAG, "startSavingGame.scoreByte = " + scoreByte);
            foStream.write(scoreByte);
            // save undoEnable
            Log.d(TAG, "startSavingGame.isUndoEnable = " + mGameProperties.isUndoEnable());
            if (mGameProperties.isUndoEnable()) {
                // can undo
                foStream.write(1);
            } else {
                // no undo
                foStream.write(0);
            }
            Log.d(TAG, "startSavingGame.ballNumOneTime = " + GridData.mBallNumOneTime);
            foStream.write(GridData.mBallNumOneTime);
            // save undoNextBalls
            for (HashMap.Entry<Point, Integer> entry : mGridData.getUndoNextCellIndices().entrySet()) {
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getValue() = " + entry.getValue());
                foStream.write(entry.getValue());
            }
            sz = mGridData.getUndoNextCellIndices().size();
            for (int i = sz; i<NumOfColorsUsedByDifficult; i++) {
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getValue() = " + 0);
                foStream.write(0);
            }
            // save backupCells
            for (int i = 0; i< mRowCounts; i++) {
                for (int j = 0; j< mColCounts; j++) {
                    Log.d(TAG, "startSavingGame.gridData.getBackupCells()[i][j] = " + mGridData.getBackupCells()[i][j]);
                    foStream.write(mGridData.getBackupCells()[i][j]);
                }
            }
            byte[] undoScoreByte = ByteBuffer.allocate(4).putInt(mGameProperties.getUndoScore()).array();
            Log.d(TAG, "startSavingGame.undoScoreByte = " + undoScoreByte);
            foStream.write(undoScoreByte);
            // end of writing

            numOfSaved++;
            // save numOfSaved back to file (ColorBallsApp.NumOfSavedGameFileName)
            Log.d(TAG, "startSavingGame.creating outputFile.");
            outputFile = new File(mActivity.getFilesDir(), NumOfSavedGameFileName);
            foStream = new FileOutputStream(outputFile);
            foStream.write(numOfSaved);
            foStream.close();
            //
            Log.d(TAG, "startSavingGame.Succeeded.");
        } catch (IOException ex) {
            ex.printStackTrace();
            succeeded = false;
            Log.d(TAG, "startSavingGame.Failed.");
        }

        ColorBallsApp.isProcessingJob = false;
        mPresentView.dismissShowMessageOnScreen();

        Log.d(TAG, "startSavingGame.Finished");

        return succeeded;
    }

    public boolean startLoadingGame() {
        Log.d(TAG, "startLoadingGame");
        ColorBallsApp.isProcessingJob = true;
        mPresentView.showMessageOnScreen(mActivity.getString(R.string.loadingGameStr));

        boolean succeeded = true;
        boolean hasSound;
        boolean isEasyLevel;
        boolean hasNextBall;
        int ballNumOneTime;
        int[] nextBalls = new int[NumOfColorsUsedByDifficult];
        int[][] gameCells = new int[mRowCounts][mColCounts];
        int cScore;
        boolean isUndoEnable;
        int[] undoNextBalls = new int[NumOfColorsUsedByDifficult];
        int[][] backupCells = new int[mRowCounts][mColCounts];
        int unScore = mGameProperties.getUndoScore();

        try {
            // clear nextCellIndices and undoNextCellIndices
            mGridData.setNextCellIndices(new HashMap<>());
            mGridData.setUndoNextCellIndices(new HashMap<>());

            Log.d(TAG, "startLoadingGame.Creating inputFile");
            File inputFile = new File(mActivity.getFilesDir(), savedGameFileName);
            long fileSizeInByte = inputFile.length();
            Log.d(TAG, "startLoadingGame.File size = " + fileSizeInByte);
            FileInputStream fiStream = new FileInputStream(inputFile);
            // game sound
            int bValue = fiStream.read();
            hasSound = bValue==1;
            Log.d(TAG, "startLoadingGame.hasSound = " + hasSound);
            // game level
            bValue = fiStream.read();
            isEasyLevel = bValue==1;
            Log.d(TAG, "startLoadingGame.isEasyLevel = " + isEasyLevel);
            // next balls
            bValue = fiStream.read();
            hasNextBall = bValue==1;
            Log.d(TAG, "startLoadingGame.hasNextBall = " + hasNextBall);
            ballNumOneTime = fiStream.read();
            Log.i(TAG, "startLoadingGame.ballNumOneTime = " + ballNumOneTime);
            for (int i=0; i<NumOfColorsUsedByDifficult; i++) {
                nextBalls[i] = fiStream.read();
                Log.d(TAG, "startLoadingGame.nextCellIndices.cell.getColor() = " + nextBalls[i]);
            }
            int nextCellIndicesSize = fiStream.read();
            Log.d(TAG, "startLoadingGame.getNextCellIndices.size() = " + nextCellIndicesSize);
            for (int i=0; i<nextCellIndicesSize; i++) {
                int x = fiStream.read();
                int y = fiStream.read();
                Log.d(TAG, "startLoadingGame.nextCellIndices.getKey().x = " + x);
                Log.d(TAG, "startLoadingGame.nextCellIndices.getKey().y = " + y);
                mGridData.addNextCellIndices(new Point(x, y));
            }
            int undoNextCellIndicesSize = fiStream.read();
            Log.d(TAG, "startLoadingGame.getUndoNextCellIndices.size() = " + undoNextCellIndicesSize);
            for (int i=0; i<undoNextCellIndicesSize; i++) {
                int x = fiStream.read();
                int y = fiStream.read();
                Log.d(TAG, "startLoadingGame.undoNextCellIndices.getKey().x = " + x);
                Log.d(TAG, "startLoadingGame.undoNextCellIndices.geyKey().y = " + y);
                mGridData.addUndoNextCellIndices(new Point(x, y));
            }
            // load values on 9x9 grid
            for (int i = 0; i< mRowCounts; i++) {
                for (int j = 0; j< mColCounts; j++) {
                    gameCells[i][j] = fiStream.read();
                    Log.d(TAG, "startLoadingGame.gridData.getCellValue(i, j) = " + gameCells[i][j]);
                }
            }
            // reading current score
            byte[] scoreByte = new byte[4];
            fiStream.read(scoreByte);
            Log.d(TAG, "startLoadingGame.scoreByte = " + scoreByte);
            cScore = ByteBuffer.wrap(scoreByte).getInt();
            // reading undoEnable
            bValue = fiStream.read();
            isUndoEnable = bValue==1;
            Log.d(TAG, "startLoadingGame.isUndoEnable = " + isUndoEnable);
            if (isUndoEnable) {
                ballNumOneTime = fiStream.read();
                Log.d(TAG, "startLoadingGame.ballNumOneTime = " + ballNumOneTime);
                for (int i=0; i<NumOfColorsUsedByDifficult; i++) {
                    undoNextBalls[i] = fiStream.read();
                    Log.d(TAG, "startLoadingGame.undoNextCellIndices.getValue() = " + undoNextBalls[i]);
                }
                // save backupCells
                for (int i = 0; i< mRowCounts; i++) {
                    for (int j = 0; j< mColCounts; j++) {
                        backupCells[i][j] = fiStream.read();
                        Log.d(TAG, "startLoadingGame.gridData.getBackupCells()[i][j] = " + backupCells[i][j]);
                    }
                }
                byte[] undoScoreByte = new byte[4];
                fiStream.read(undoScoreByte);
                Log.d(TAG, "startLoadingGame.undoScoreByte = " + undoScoreByte);
                unScore = ByteBuffer.wrap(undoScoreByte).getInt();
            }
            fiStream.close();

            // refresh Main UI with loaded data
            setHasSound(hasSound);
            setEasyLevel(isEasyLevel);
            setHasNextBall(hasNextBall, false);
            // mGridData.setNextBalls(nextBalls);
            int kk = 0;
            for (HashMap.Entry<Point, Integer> entry : mGridData.getNextCellIndices().entrySet()) {
                entry.setValue(nextBalls[kk++]);
            }
            mGridData.setCellValues(gameCells);
            mGameProperties.setCurrentScore(cScore);
            mGameProperties.setUndoEnable(isUndoEnable);
            // mGridData.setUndoNextBalls(undoNextBalls);
            kk = 0;
            for (HashMap.Entry<Point, Integer> entry : mGridData.getUndoNextCellIndices().entrySet()) {
                entry.setValue(undoNextBalls[kk++]);
            }
            mGridData.setBackupCells(backupCells);
            mGameProperties.setUndoScore(unScore);
            // start update UI
            mPresentView.updateCurrentScoreOnUi(mGameProperties.getCurrentScore());
            Log.d(TAG, "startLoadingGame.starting displayGameView().");
            displayGameView();
        } catch (IOException ex) {
            ex.printStackTrace();
            succeeded = false;
        }

        ColorBallsApp.isProcessingJob = false;
        mPresentView.dismissShowMessageOnScreen();

        return succeeded;
    }

    public void release() {
        stopBouncyAnimation();
        showingScoreHandler.removeCallbacksAndMessages(null);
        movingBallHandler.removeCallbacksAndMessages(null);
        soundPoolUtil.release();
    }

    private void createBitmapsAndDrawableResources(int cellWidth, int cellHeight) {
        if (cellWidth<=0 || cellHeight<=0) {
            throw new IllegalArgumentException("cellWidth and cellHeight must be > 0");
        }

        Resources resources = mActivity.getResources();

        int nextBallWidth = (int)(cellWidth * 0.5f);
        int nextBallHeight = (int)(cellHeight * 0.5f);
        int ovalBallWidth = (int)(cellWidth * 0.9f);
        int ovalBallHeight = (int)(cellHeight * 0.7f);

        Drawable drawable = ContextCompat.getDrawable(mActivity, R.drawable.redball);
        colorBallMap.put(ColorRED, drawable);
        Bitmap bm = BitmapFactory.decodeResource(resources, R.drawable.redball);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(mActivity, bm, nextBallWidth, nextBallHeight);
        colorNextBallMap.put(ColorRED, drawable);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(mActivity, bm, ovalBallWidth, ovalBallHeight);
        colorOvalBallMap.put(ColorRED, drawable);

        drawable = ContextCompat.getDrawable(mActivity, R.drawable.greenball);
        colorBallMap.put(ColorGREEN, drawable);
        bm = BitmapFactory.decodeResource(resources, R.drawable.greenball);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(mActivity, bm, nextBallWidth, nextBallHeight);
        colorNextBallMap.put(ColorGREEN, drawable);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(mActivity, bm, ovalBallWidth, ovalBallHeight);
        colorOvalBallMap.put(ColorGREEN, drawable);

        drawable = ContextCompat.getDrawable(mActivity, R.drawable.blueball);
        colorBallMap.put(ColorBLUE, drawable);
        bm = BitmapFactory.decodeResource(resources, R.drawable.blueball);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(mActivity, bm, nextBallWidth, nextBallHeight);
        colorNextBallMap.put(ColorBLUE, drawable);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(mActivity, bm, ovalBallWidth, ovalBallHeight);
        colorOvalBallMap.put(ColorBLUE, drawable);

        drawable = ContextCompat.getDrawable(mActivity, R.drawable.magentaball);
        colorBallMap.put(ColorMAGENTA, drawable);
        bm = BitmapFactory.decodeResource(resources, R.drawable.magentaball);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(mActivity, bm, nextBallWidth, nextBallHeight);
        colorNextBallMap.put(ColorMAGENTA, drawable);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(mActivity, bm, ovalBallWidth, ovalBallHeight);
        colorOvalBallMap.put(ColorMAGENTA, drawable);

        drawable = ContextCompat.getDrawable(mActivity, R.drawable.yellowball);
        colorBallMap.put(ColorYELLOW, drawable);
        bm = BitmapFactory.decodeResource(resources, R.drawable.yellowball);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(mActivity, bm, nextBallWidth, nextBallHeight);
        colorNextBallMap.put(ColorYELLOW, drawable);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(mActivity, bm, ovalBallWidth, ovalBallHeight);
        colorOvalBallMap.put(ColorYELLOW, drawable);

        drawable = ContextCompat.getDrawable(mActivity, R.drawable.cyanball);
        colorBallMap.put(ColorCYAN, drawable);
        bm = BitmapFactory.decodeResource(resources, R.drawable.cyanball);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(mActivity, bm, nextBallWidth, nextBallHeight);
        colorNextBallMap.put(ColorCYAN, drawable);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(mActivity, bm, ovalBallWidth, ovalBallHeight);
        colorOvalBallMap.put(ColorCYAN, drawable);
    }

    private void gameOver() {
        mPresentView.showGameOverDialog();
    }

    private int calculateScore(HashSet<Point> linkedLine) {
        if (linkedLine == null) {
            return 0;
        }

        int[] numBalls = new int[] {0,0,0,0,0,0};
        for (Point point : linkedLine) {
            switch (mGridData.getCellValue(point.x, point.y)) {
                case ColorRED -> numBalls[0]++;
                case ColorGREEN -> numBalls[1]++;
                case ColorBLUE -> numBalls[2]++;
                case ColorMAGENTA -> numBalls[3]++;
                case ColorYELLOW -> numBalls[4]++;
                case ColorCYAN -> numBalls[5]++;
            }
        }
        // 5 balls --> 5
        // 6 balls --> 5 + (6-5)*2
        // 7 balls --> 5 + (6-5)*2 + (7-5)*2
        // 8 balls --> 5 + (6-5)*2 + (7-5)*2 + (8-5)*2
        // n balls --> 5 + (6-5)*2 + (7-5)*2 + ... + (n-5)*2

        int minScore = 5;
        int totalScore = 0;
        for (int numBall : numBalls) {
            if (numBall >= 5) {
                int score = minScore;
                int extraBalls = numBall - minScore;
                if (extraBalls > 0) {
                    // greater than 5 balls
                    int rate = 2;
                    for (int i = 1; i <= extraBalls; i++) {
                        // rate = 2;   // added on 2018-10-02
                        score += i * rate;
                    }
                }
                totalScore += score;
            }
        }

        if (!mGameProperties.isEasyLevel()) {
            // difficult level
            totalScore *= 2;   // double of easy level
        }

        return totalScore;
    }

    private void drawBall(ImageView imageView, int color) {
        imageView.setImageDrawable(colorBallMap.get(color));
    }

    private void drawOval(ImageView imageView,int color) {
        imageView.setImageDrawable(colorOvalBallMap.get(color));
    }

    private void drawNextBall(ImageView imageView,int color) {
        Log.d(TAG, "drawNextBall.color = " + color);
        if (imageView != null) {
            if (mGameProperties.hasNextBall()) {
                imageView.setImageDrawable(colorNextBallMap.get(color));
            } else {
                imageView.setImageDrawable(null);
            }
        } else {
            Log.w(TAG, "drawNextBall.imageView = null");
        }
    }

    private void displayNextBallsView() {
        // display the view of next balls
        Log.d(TAG, "displayNextBallsView");
        ImageView imageView;
        try {
            for (HashMap.Entry<Point, Integer> entry : mGridData.getNextCellIndices().entrySet()) {
                int imageViewId = getImageId(entry.getKey().x, entry.getKey().y);
                imageView = mPresentView.getImageViewById(imageViewId);
                drawNextBall(imageView, entry.getValue());
            }
        } catch (Exception ex) {
            Log.d(TAG, "displayNextBallsView.Exception: ");
            ex.printStackTrace();
        }
    }

    private void displayNextColorBalls() {
        if (mGridData.randCells() == 0) {
            // no vacant, so game over
            gameOver();
            return;
        }
        //   display the balls on the nextBallsView
        displayNextBallsView();
    }

    private void clearCell(int i, int j) {
        int id = getImageId(i, j);
        ImageView imageView = mPresentView.getImageViewById(id);
        imageView.setImageBitmap(null);
        mGridData.setCellValue(i, j, 0);
    }

    private void displayGridDataNextCells() {
        Log.d(TAG,"displayGridDataNextCells");
        int id, n1, n2;
        ImageView imageView;
        boolean hasMoreFive = false;
        HashSet<Point> linkedPoint = new HashSet<>();
        for (HashMap.Entry<Point, Integer> entry : mGridData.getNextCellIndices().entrySet()) {
            n1 = entry.getKey().x;
            n2 = entry.getKey().y;
            mGridData.setCellValue(n1, n2, entry.getValue());
            id = getImageId(n1, n2);
            imageView = mPresentView.getImageViewById(id);
            drawBall(imageView, mGridData.getCellValue(n1, n2));
            if (mGridData.check_moreThanFive(n1, n2)) {
                hasMoreFive = true;
                for (Point point : mGridData.getLightLine()) {
                    if (!linkedPoint.contains(point)) {
                        linkedPoint.add(new Point(point));
                    }
                }
            }
        }

        if (hasMoreFive) {
            mGridData.setLightLine(linkedPoint);    // added on 2020-07-13
            mGameProperties.setLastGotScore(calculateScore(mGridData.getLightLine()));
            mGameProperties.setUndoScore(mGameProperties.getCurrentScore());
            mGameProperties.setCurrentScore(mGameProperties.getCurrentScore() + mGameProperties.getLastGotScore());
            mPresentView.updateCurrentScoreOnUi(mGameProperties.getCurrentScore());
            ShowScoreRunnable showScoreRunnable = new ShowScoreRunnable(mGameProperties.getLastGotScore(), mGridData.getLightLine(),
                    true, new ShowScoreCallback() {
                @Override
                public void sCallback() {
                    Log.d(TAG, "ShowScoreCallback.sCallback.Do nothing.");
                }
            });
            showingScoreHandler.post(showScoreRunnable);
            Log.d(TAG,"displayGridDataNextCells.post(showScoreRunnable) run.");
        } else {
            displayNextColorBalls();
        }
    }

    private void displayGameGridView() {
        // display the 9 x 9 game view
        Log.d(TAG, "displayGameView");
        ImageView imageView;
        try {
            for (int i = 0; i < mRowCounts; i++) {
                for (int j = 0; j < mColCounts; j++) {
                    int id = getImageId(i, j);
                    imageView = mPresentView.getImageViewById(id);
                    int color = mGridData.getCellValue(i, j);
                    if (color == 0) {
                        // imageView.setImageDrawable(null);
                        imageView.setImageBitmap(null);
                    } else {
                        drawBall(imageView, color);
                    }
                }
            }
        } catch (Exception ex) {
            Log.d(TAG, "displayGameGridView.Exception: ");
            ex.printStackTrace();
        }
    }

    private void displayGameView() {
        // display the 9 x 9 game view
        Log.d(TAG, "displayGameView");
        displayGameGridView();
        // display the view of next balls
        displayNextBallsView();
    }

    private void drawBallAlongPath() {
        int sizeOfPathPoint = mGridData.getPathPoint().size();
        if (sizeOfPathPoint == 0) {
            Log.w(TAG, "drawBallAlongPath.sizeOfPathPoint == 0");
            return;
        }

        final int targetI = mGridData.getPathPoint().get(0).x;  // the target point
        final int targetJ = mGridData.getPathPoint().get(0).y;  // the target point
        Log.d(TAG, "drawBallAlongPath.targetI = " + targetI + ", targetJ = " + targetJ);
        final int beginI = mGridData.getPathPoint().get(sizeOfPathPoint-1).x;
        final int beginJ = mGridData.getPathPoint().get(sizeOfPathPoint-1).y;
        final int color = mGridData.getCellValue(beginI, beginJ);
        Log.d(TAG, "drawBallAlongPath.color = " + color);

        final ArrayList<Point> tempList = new ArrayList<>(mGridData.getPathPoint());
        Runnable runnablePath = new Runnable() {
            boolean ballYN = true;
            ImageView imageView = null;
            int countDown = tempList.size()*2 - 1;
            @Override
            public synchronized void run() {
                mGameProperties.getThreadCompleted()[0] = false;
                mGameProperties.setBallMoving(true);
                if (countDown >= 2) {   // eliminate start point
                    int i = countDown / 2;
                    imageView = mPresentView.getImageViewById(getImageId(tempList.get(i).x, tempList.get(i).y));
                    if (ballYN) {
                        drawBall(imageView, color);
                    } else {
                        imageView.setImageBitmap(null);
                    }
                    ballYN = !ballYN;
                    countDown--;
                    movingBallHandler.postDelayed(this,20);
                } else {
                    clearCell(beginI, beginJ);  // blank the original cell. Added on 2020-09-16
                    ImageView v = mPresentView.getImageViewById(getImageId(targetI, targetJ));
                    mGridData.setCellValue(targetI, targetJ, color);
                    drawBall(v, color);
                    mGridData.regenerateNextCellIndices(new Point(targetI, targetJ));
                    //  check if there are more than five balls with same color connected together
                    if (mGridData.check_moreThanFive(targetI, targetJ)) {
                        mGameProperties.setLastGotScore(calculateScore(mGridData.getLightLine()));
                        mGameProperties.setUndoScore(mGameProperties.getCurrentScore());
                        mGameProperties.setCurrentScore(mGameProperties.getCurrentScore() + mGameProperties.getLastGotScore());
                        mPresentView.updateCurrentScoreOnUi(mGameProperties.getCurrentScore());
                        Log.d(TAG,"drawBallAlongPath.showScoreRunnable()");
                        ShowScoreRunnable showScoreRunnable = new ShowScoreRunnable(mGameProperties.getLastGotScore(), mGridData.getLightLine(),
                                false, new ShowScoreCallback() {
                            @Override
                            public void sCallback() {
                                Log.d(TAG,"drawBallAlongPath.ShowScoreCallback.sCallback");
                                mGameProperties.getThreadCompleted()[0] = true;
                                mGameProperties.setBallMoving(false);
                                Log.d(TAG,"drawBallAlongPath.run() finished.");
                            }
                        });
                        showingScoreHandler.post(showScoreRunnable);
                        Log.d(TAG,"drawBallAlongPath.showScoreRunnable() run");
                    } else {
                        displayGridDataNextCells();   // has a problem
                        mGameProperties.getThreadCompleted()[0] = true;
                        mGameProperties.setBallMoving(false);
                        Log.d(TAG,"drawBallAlongPath.run() finished.");
                    }
                }
            }
        };
        movingBallHandler.post(runnablePath);
    }

    private void drawBouncyBall(final ImageView v, final int color) {
        if (v == null) {
            Log.e(TAG, "drawBouncyBall.v is null, color = " + color);
            return;
        }
        bouncyAnimation = new AnimationDrawable();
        bouncyAnimation.setOneShot(false);
        bouncyAnimation.addFrame(colorBallMap.get(color), 200);
        bouncyAnimation.addFrame(colorOvalBallMap.get(color), 200);
        v.setImageDrawable(bouncyAnimation);
        bouncyAnimation.start();
    }

    private void stopBouncyAnimation() {
        if (bouncyAnimation != null && bouncyAnimation.isRunning()) {
            bouncyAnimation.stop();
        }
    }

    public int getImageId(int row, int column) {
        // Log.d(TAG, "getImageId.row = " + row + ", column = " + column );
        return row * mRowCounts + column;
    }

    private int getRow(int imageId) {
        return imageId / mRowCounts;
    }

    private int getColumn(int imageId) {
        return imageId % mRowCounts;
    }

    private class ShowScoreRunnable implements Runnable {
        private final int mLastGotScore;
        private final boolean isNextBalls;
        private HashSet<Point> hasPoint = null;
        private int mCounter = 0;
        private ShowScoreCallback showScoreCallback;

        public ShowScoreRunnable(final int lastGotScore, final HashSet<Point> linkedPoint,
                                 final boolean nextBalls,
                                 ShowScoreCallback callback) {
            Log.d(TAG, "ShowScoreRunnable");
            mLastGotScore = lastGotScore;
            isNextBalls = nextBalls;
            showScoreCallback = callback;
            if (linkedPoint != null) {
                hasPoint = new HashSet<>(linkedPoint);
            }
            mGameProperties.setShowNextBallsAfterBlinking(isNextBalls);
            mGameProperties.getThreadCompleted()[1] = false;
            mGameProperties.setShowingScoreMessage(true);
        }

        private synchronized void onProgressUpdate(int status) {
            switch (status) {
                case 0:
                    for (Point item : hasPoint) {
                        ImageView v = mPresentView.getImageViewById(getImageId(item.x, item.y));
                        drawBall(v, mGridData.getCellValue(item.x, item.y));
                    }
                    break;
                case 1:
                    for (Point item : hasPoint) {
                        ImageView v = mPresentView.getImageViewById(getImageId(item.x, item.y));
                        drawOval(v, mGridData.getCellValue(item.x, item.y));
                    }
                    break;
                case 2:
                    break;
                case 3:
                    // show the score
                    String scoreString = String.valueOf(mLastGotScore);
                    mPresentView.showMessageOnScreen(scoreString);
                    for (Point item : hasPoint) {
                        clearCell(item.x, item.y);
                    }
                    if (isNextBalls) {
                        Log.d(TAG, "ShowScoreRunnable.onProgressUpdate.displayNextColorBalls");
                        displayNextColorBalls();
                    } else {
                        Log.d(TAG, "ShowScoreRunnable.onProgressUpdate.displayNextBallsView");
                        displayNextBallsView();
                    }
                    mGameProperties.getThreadCompleted()[1] = true;  // user can start input command
                    mGameProperties.setShowingScoreMessage(false);
                    break;
                case 4:
                    Log.d(TAG, "ShowScoreRunnable.onProgressUpdate.dismissShowMessageOnScreen.");
                    mPresentView.dismissShowMessageOnScreen();
                    break;
            }
        }

        @Override
        public synchronized void run() {
            if (hasPoint == null) {
                Log.d(TAG, "ShowScoreRunnable.run().hasPoint is null.");
                showingScoreHandler.removeCallbacksAndMessages(null);
            } else {
                final int twinkleCountDown = 5;
                mCounter++;
                Log.d(TAG, "ShowScoreRunnable.run().mCounter = " + mCounter);
                if (mCounter <= twinkleCountDown) {
                    int md = mCounter % 2; // modulus
                    onProgressUpdate(md);
                    showingScoreHandler.postDelayed(this, 100);
                } else {
                    if (mCounter == twinkleCountDown+1) {
                        onProgressUpdate(3);    // show score
                        showingScoreHandler.postDelayed(this, 500);
                    } else {
                        showingScoreHandler.removeCallbacksAndMessages(null);
                        onProgressUpdate(4);    // dismiss showing message
                        mGameProperties.getThreadCompleted()[1] = true;
                        mGameProperties.setShowingScoreMessage(false);
                        showScoreCallback.sCallback();
                    }
                }
            }
        }
    }
}
