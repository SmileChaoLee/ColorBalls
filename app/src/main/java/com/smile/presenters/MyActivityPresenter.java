package com.smile.presenters;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.smile.colorballs.ColorBallsApp;
import com.smile.colorballs.R;
import com.smile.model.GameProperties;
import com.smile.model.GridData;
import com.smile.smilelibraries.player_record_rest.PlayerRecordRest;
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

public class MyActivityPresenter {

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
    public static HashMap<Integer, Bitmap> colorBallMap;
    public static HashMap<Integer, Drawable> colorOvalBallMap;
    public static HashMap<Integer, Drawable> colorNextBallMap;

    private final String NumOfSavedGameFileName = "num_saved_game";

    private final String TAG = "MyActivityPresenter";
    private final String GamePropertiesTag = "GameProperties";
    private final String savedGameFileName = "saved_game";

    private final Activity activity;
    private final ColorBallsApp application;
    private final PresentView presentView;
    private final SoundPoolUtil soundPoolUtil;
    private final Handler bouncyHandler = new Handler(Looper.getMainLooper());
    private final Handler movingBallHandler = new Handler(Looper.getMainLooper());
    private final Handler showingScoreHandler = new Handler(Looper.getMainLooper());

    private int rowCounts, colCounts;
    private GameProperties gameProperties;
    private GridData gridData;

    public interface PresentView {
        ImageView getImageViewById(int id);
        void updateHighestScoreOnUi(int highestScore);
        void updateCurrentScoreOnUi(int score);
        void showMessageOnScreen(String message);
        void dismissShowMessageOnScreen();
        void showSaveScoreAlertDialog(final int entryPoint, final int score);
        void showSaveGameDialog();
        void showingWarningSaveGameDialog(int finalNumOfSaved);
        void showLoadGameDialog();
        void showGameOverDialog();
    }

    public MyActivityPresenter(Activity activity, PresentView presentView) {
        this.activity = activity;
        this.presentView = presentView;
        soundPoolUtil = new SoundPoolUtil(this.activity, R.raw.uhoh);

        colorBallMap = new HashMap<>();
        colorOvalBallMap = new HashMap<>();
        colorNextBallMap = new HashMap<>();
        application = (ColorBallsApp) this.activity.getApplication();
    }

    public void doDrawBallsAndCheckListener(View v) {
        int i, j, id;
        id = v.getId();
        i = id / rowCounts;
        j = id % rowCounts;
        ImageView imageView;
        if (!gameProperties.isBallBouncing()) {
            if (gridData.getCellValue(i, j) != 0) {
                if ((gameProperties.getBouncyBallIndexI() == -1) && (gameProperties.getBouncyBallIndexJ() == -1)) {
                    gameProperties.setBallBouncing(true);
                    drawBouncyBall((ImageView) v, gridData.getCellValue(i, j));
                    gameProperties.setBouncyBallIndexI(i);
                    gameProperties.setBouncyBallIndexJ(j);
                }
            }
        } else {
            // cancel the timer
            if (gridData.getCellValue(i, j) == 0) {
                //   blank cell
                int bouncyBallIndexI = gameProperties.getBouncyBallIndexI();
                int bouncyBallIndexJ = gameProperties.getBouncyBallIndexJ();
                if ((bouncyBallIndexI >= 0) && (bouncyBallIndexJ >= 0)) {
                    if (gridData.canMoveCellToCell(new Point(bouncyBallIndexI, bouncyBallIndexJ), new Point(i, j))) {
                        // cancel the timer
                        gameProperties.setBallBouncing(false);
                        cancelBouncyTimer();

                        gameProperties.setBouncyBallIndexI(-1);
                        gameProperties.setBouncyBallIndexJ(-1);

                        drawBallAlongPath();

                        gameProperties.setUndoEnable(true);
                    } else {
                        //    make a sound
                        if (gameProperties.hasSound()) {
                            soundPoolUtil.playSound();
                        }
                    }
                }
            } else {
                //  cell is not blank
                int bouncyBallIndexI = gameProperties.getBouncyBallIndexI();
                int bouncyBallIndexJ = gameProperties.getBouncyBallIndexJ();
                if ((bouncyBallIndexI >= 0) && (bouncyBallIndexJ >= 0)) {
                    cancelBouncyTimer();
                    imageView = presentView.getImageViewById(bouncyBallIndexI * rowCounts + bouncyBallIndexJ);
                    drawBall(imageView , gridData.getCellValue(bouncyBallIndexI, bouncyBallIndexJ));
                    drawBouncyBall((ImageView) v, gridData.getCellValue(i, j));
                    gameProperties.setBouncyBallIndexI(i);
                    gameProperties.setBouncyBallIndexJ(j);
                }
            }
        }
    }

    public boolean initializeColorBallsGame(int rowCounts, int colCounts, int cellWidth, int cellHeight, Bundle savedInstanceState) {

        createBitmapsAndDrawableResources(cellWidth, cellHeight);

        this.rowCounts = rowCounts;
        this.colCounts = colCounts;
        int highestScore = application.scoreSQLiteDB.readHighestScore();

        boolean isNewGame = true;
        if (savedInstanceState == null) {
            // activity just started so new game
            Log.d(TAG, "initializeColorBallsGame.savedInstanceState is null");
        } else {
            Log.d(TAG, "initializeColorBallsGame.Configuration changed and restore the original UI.");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {    // API 33
                gameProperties = savedInstanceState.getParcelable(GamePropertiesTag, GameProperties.class);
            } else gameProperties = savedInstanceState.getParcelable(GamePropertiesTag);
            if (gameProperties != null) {
                gridData = gameProperties.getGridData();
                if (gridData != null) {
                    isNewGame = false;
                }
            }
        }
        if (isNewGame) {
            Log.d(TAG, "initializeColorBallsGame.new game.");
            gridData = new GridData(this.rowCounts, this.colCounts, NumOfColorsUsedByEasy);
            gameProperties = new GameProperties(gridData);
        }

        ColorBallsApp.isShowingLoadingMessage = gameProperties.isShowingLoadingMessage();
        ColorBallsApp.isProcessingJob = gameProperties.isProcessingJob();

        presentView.updateHighestScoreOnUi(highestScore);
        presentView.updateCurrentScoreOnUi(gameProperties.getCurrentScore());

        displayGameView();
        if (isNewGame) {
            displayGridDataNextCells();
        } else {
            // display the original state before changing configuration
            // need to be tested
            if (ColorBallsApp.isShowingLoadingMessage) {
                presentView.showMessageOnScreen(activity.getString(R.string.loadingString));
            }
            //
            if (gameProperties.isBallMoving()) {
                Log.d(TAG, "initializeColorBallsGame() --> gameProperties.isBallMoving() is true");
                drawBallAlongPath();
            }
            if (gameProperties.isShowingScoreMessage()) {
                Log.d(TAG, "initializeColorBallsGame() --> gameProperties.isShowingScoreMessage() is true");
                ShowScoreRunnable showScoreRunnable = new ShowScoreRunnable(gameProperties.getLastGotScore(), gridData.getLight_line(), gameProperties.isShowNextBallsAfterBlinking());
                showingScoreHandler.post(showScoreRunnable);
                Log.d(TAG,"initializeColorBallsGame() --> showingScoreHandler.post(showScoreRunnable).");
            }
            if (gameProperties.isBallBouncing()) {
                int bouncyBallIndexI = gameProperties.getBouncyBallIndexI();
                int bouncyBallIndexJ = gameProperties.getBouncyBallIndexJ();
                ImageView v = presentView.getImageViewById(bouncyBallIndexI * rowCounts + bouncyBallIndexJ);
                drawBouncyBall(v, gridData.getCellValue(bouncyBallIndexI, bouncyBallIndexJ));
            }

            if (gameProperties.isShowingNewGameDialog()) {
                Log.d(TAG, "initializeColorBallsGame() --> show new game dialog by calling newGame()");
                newGame();
            }

            if (gameProperties.isShowingQuitGameDialog()) {
                Log.d(TAG, "initializeColorBallsGame() --> show quit game dialog by calling quitGame()");
                quitGame();
            }

            if (gameProperties.isShowingSureSaveDialog()) {
                Log.d(TAG, "initializeColorBallsGame() --> isShowingSureSaveDialog()");
                saveGame();
            }

            if (gameProperties.isShowingWarningSaveGameDialog()) {
                Log.d(TAG, "initializeColorBallsGame() --> isShowingWarningSaveGameDialog()");
                presentView.showingWarningSaveGameDialog(readNumberOfSaved());
            }

            if (gameProperties.isShowingSureLoadDialog()) {
                Log.d(TAG, "initializeColorBallsGame() --> isShowingSureLoadDialog()");
                loadGame();
            }

            if (gameProperties.isShowingGameOverDialog()) {
                Log.d(TAG, "initializeColorBallsGame() --> isShowingGameOverDialog()");
                gameOver();
            }
        }

        return isNewGame;
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        gameProperties.setShowingLoadingMessage(ColorBallsApp.isShowingLoadingMessage);
        gameProperties.setProcessingJob(ColorBallsApp.isProcessingJob);
        outState.putParcelable(GamePropertiesTag, gameProperties);
    }

    public boolean completedAll() {
        for (boolean thCompleted : gameProperties.getThreadCompleted()) {
            if (!thCompleted) {
                return false;
            }
        }
        return true;
    }

    public boolean hasSound() {
        return gameProperties.hasSound();
    }

    public void setHasSound(boolean hasSound) {
        gameProperties.setHasSound(hasSound);
    }

    public boolean isEasyLevel() {
        return gameProperties.isEasyLevel();
    }

    public void setEasyLevel(boolean yn) {
        gameProperties.setEasyLevel(yn);
        if (gameProperties.isEasyLevel()) {
            // easy level
            gridData.setNumOfColorsUsed(NumOfColorsUsedByEasy);
        } else {
            // difficult
            gridData.setNumOfColorsUsed(NumOfColorsUsedByDifficult);
        }
    }

    public boolean hasNextBall() {
        return gameProperties.hasNextBall();
    }

    public void setHasNextBall(boolean hasNextBall, boolean isNextBalls) {
        gameProperties.setHasNextBall(hasNextBall);
        if (isNextBalls) {
            displayNextBallsView();
        }
    }

    public void setShowingNewGameDialog(boolean showingNewGameDialog) {
        gameProperties.setShowingNewGameDialog(showingNewGameDialog);
    }

    public void setShowingQuitGameDialog(boolean showingQuitGameDialog) {
        gameProperties.setShowingQuitGameDialog(showingQuitGameDialog);
    }

    public void undoTheLast() {
        if (!gameProperties.isUndoEnable()) {
            return;
        }

        ColorBallsApp.isProcessingJob = true; // started undoing

        gridData.undoTheLast();

        cancelBouncyTimer();
        gameProperties.setBallBouncing(false);
        gameProperties.setBouncyBallIndexI(-1);
        gameProperties.setBouncyBallIndexJ(-1);

        // restore the screen
        displayGameView();

        gameProperties.setCurrentScore(gameProperties.getUndoScore());
        presentView.updateCurrentScoreOnUi(gameProperties.getCurrentScore());

        // completedPath = true;
        gameProperties.setUndoEnable(false);

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
        gameProperties.setShowingSureSaveDialog(isShowingSureSaveDialog);
    }

    public void setShowingSureLoadDialog(boolean isShowingSureLoadDialog) {
        gameProperties.setShowingSureLoadDialog(isShowingSureLoadDialog);
    }

    public void setShowingGameOverDialog(boolean isShowingGameOverDialog) {
        gameProperties.setShowingGameOverDialog(isShowingGameOverDialog);
    }

    public void setShowingWarningSaveGameDialog(boolean isShowingWarningSaveGameDialog) {
        gameProperties.setShowingWarningSaveGameDialog(isShowingWarningSaveGameDialog);
    }

    public void saveScore(String playerName, int score) {
        // removed on 2019-02-20 no global ranking any more
        // use thread to add a record to database (remote database on AWS-EC2)
        Thread restThread = new Thread() {
            @Override
            public void run() {
                try {
                    String webUrl = ColorBallsApp.REST_Website + "/AddOneRecordREST";   // ASP.NET Cor
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("PlayerName", playerName);
                    jsonObject.put("Score", score);
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
        boolean isInTop10 = application.scoreSQLiteDB.isInTop10(score);
        if (isInTop10) {
            // inside top 10
            // record the current score
            application.scoreSQLiteDB.addScore(playerName, score);
            application.scoreSQLiteDB.deleteAllAfterTop10();  // only keep the top 10
        }
        //
    }

    public void newGame() {
        presentView.showSaveScoreAlertDialog(1, gameProperties.getCurrentScore());
    }

    public void quitGame() {
        presentView.showSaveScoreAlertDialog(0, gameProperties.getCurrentScore());
    }

    public void saveGame() {
        presentView.showSaveGameDialog();
    }

    public void loadGame() {
        presentView.showLoadGameDialog();
    }

    public int readNumberOfSaved() {
        int numOfSaved = 0;
        try {
            File inputFile = new File(ColorBallsApp.AppContext.getFilesDir(), NumOfSavedGameFileName);
            FileInputStream fiStream = new FileInputStream(inputFile);
            numOfSaved = fiStream.read();
            fiStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return numOfSaved;
    }

    public boolean startSavingGame(int numOfSaved) {
        Log.d(TAG, "Started to startSavingGame().");

        ColorBallsApp.isProcessingJob = true;
        presentView.showMessageOnScreen(activity.getString(R.string.savingGameString));

        boolean succeeded = true;
        try {
            File outputFile = new File(activity.getFilesDir(), savedGameFileName);
            FileOutputStream foStream = new FileOutputStream(outputFile);
            // save settings
            Log.d(TAG, "startSavingGame.hasSound = " + gameProperties.hasSound());
            if (gameProperties.hasSound()) {
                foStream.write(1);
            } else {
                foStream.write(0);
            }
            Log.d(TAG, "startSavingGame.isEasyLevel = " + gameProperties.isEasyLevel());
            if (gameProperties.isEasyLevel()) {
                foStream.write(1);
            } else {
                foStream.write(0);
            }
            Log.d(TAG, "startSavingGame.hasNextBall = " + gameProperties.hasNextBall());
            if (gameProperties.hasNextBall()) {
                foStream.write(1);
            } else {
                foStream.write(0);
            }
            // save next balls
            // foStream.write(gridData.ballNumOneTime);
            Log.d(TAG, "startSavingGame.ballNumOneTime = " + GridData.ballNumOneTime);
            foStream.write(GridData.ballNumOneTime);
            for (HashMap.Entry<Point, Integer> entry : gridData.getNextCellIndices().entrySet()) {
                Log.d(TAG, "startSavingGame.nextCellIndices.getValue() = " + entry.getValue());
                foStream.write(entry.getValue());
            }
            int sz = gridData.getNextCellIndices().size();
            for (int i = sz; i<NumOfColorsUsedByDifficult; i++) {
                Log.d(TAG, "startSavingGame.nextCellIndices.getValue() = " + 0);
                foStream.write(0);
            }
            Log.d(TAG, "startSavingGame.getNextCellIndices.size() = " + sz);
            foStream.write(sz);
            for (HashMap.Entry<Point, Integer> entry : gridData.getNextCellIndices().entrySet()) {
                Log.d(TAG, "startSavingGame.nextCellIndices.getKey().x = " + entry.getKey().x);
                Log.d(TAG, "startSavingGame.nextCellIndices.getKey().y = " + entry.getKey().y);
                foStream.write(entry.getKey().x);
                foStream.write(entry.getKey().y);
            }
            Log.d(TAG, "startSavingGame.getUndoNextCellIndices().size() = " + gridData.getUndoNextCellIndices().size());
            foStream.write(gridData.getUndoNextCellIndices().size());
            for (HashMap.Entry<Point, Integer> entry : gridData.getUndoNextCellIndices().entrySet()) {
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getKey().x = " + entry.getKey().x);
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getKey().y = " + entry.getKey().y);
                foStream.write(entry.getKey().x);
                foStream.write(entry.getKey().y);
            }
            // save values on 9x9 grid
            for (int i=0; i<rowCounts; i++) {
                for (int j=0; j<colCounts; j++) {
                    Log.d(TAG, "startSavingGame.gridData.getCellValue(i, j) = " + gridData.getCellValue(i, j));
                    foStream.write(gridData.getCellValue(i, j));
                }
            }
            // save current score
            byte[] scoreByte = ByteBuffer.allocate(4).putInt(gameProperties.getCurrentScore()).array();
            Log.d(TAG, "startSavingGame.scoreByte = " + scoreByte);
            foStream.write(scoreByte);
            // save undoEnable
            Log.d(TAG, "startSavingGame.isUndoEnable = " + gameProperties.isUndoEnable());
            if (gameProperties.isUndoEnable()) {
                // can undo
                foStream.write(1);
            } else {
                // no undo
                foStream.write(0);
            }
            Log.d(TAG, "startSavingGame.ballNumOneTime = " + GridData.ballNumOneTime);
            foStream.write(GridData.ballNumOneTime);
            // save undoNextBalls
            for (HashMap.Entry<Point, Integer> entry : gridData.getUndoNextCellIndices().entrySet()) {
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getValue() = " + entry.getValue());
                foStream.write(entry.getValue());
            }
            sz = gridData.getUndoNextCellIndices().size();
            for (int i = sz; i<NumOfColorsUsedByDifficult; i++) {
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getValue() = " + 0);
                foStream.write(0);
            }
            // save backupCells
            for (int i=0; i<rowCounts; i++) {
                for (int j=0; j<colCounts; j++) {
                    Log.d(TAG, "startSavingGame.gridData.getBackupCells()[i][j] = " + gridData.getBackupCells()[i][j]);
                    foStream.write(gridData.getBackupCells()[i][j]);
                }
            }
            byte[] undoScoreByte = ByteBuffer.allocate(4).putInt(gameProperties.getUndoScore()).array();
            Log.d(TAG, "startSavingGame.undoScoreByte = " + undoScoreByte);
            foStream.write(undoScoreByte);
            // end of writing

            numOfSaved++;
            // save numOfSaved back to file (ColorBallsApp.NumOfSavedGameFileName)
            outputFile = new File(ColorBallsApp.AppContext.getFilesDir(), NumOfSavedGameFileName);
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
        presentView.dismissShowMessageOnScreen();

        Log.d(TAG, "startSavingGame.Finished");

        return succeeded;
    }

    public boolean startLoadingGame() {
        ColorBallsApp.isProcessingJob = true;
        presentView.showMessageOnScreen(activity.getString(R.string.loadingGameString));

        boolean succeeded = true;
        boolean hasSound;
        boolean isEasyLevel;
        boolean hasNextBall;
        int ballNumOneTime;
        int[] nextBalls = new int[NumOfColorsUsedByDifficult];
        int[][] gameCells = new int[rowCounts][colCounts];
        int cScore;
        boolean isUndoEnable;
        int[] undoNextBalls = new int[NumOfColorsUsedByDifficult];
        int[][] backupCells = new int[rowCounts][colCounts];
        int unScore = gameProperties.getUndoScore();

        try {
            // clear nextCellIndices and undoNextCellIndices
            gridData.setNextCellIndices(new HashMap<>());
            gridData.setUndoNextCellIndices(new HashMap<>());

            File inputFile = new File(ColorBallsApp.AppContext.getFilesDir(), savedGameFileName);
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
                gridData.addNextCellIndices(new Point(x, y));
            }
            int undoNextCellIndicesSize = fiStream.read();
            Log.d(TAG, "startLoadingGame.getUndoNextCellIndices.size() = " + undoNextCellIndicesSize);
            for (int i=0; i<undoNextCellIndicesSize; i++) {
                int x = fiStream.read();
                int y = fiStream.read();
                Log.d(TAG, "startLoadingGame.undoNextCellIndices.getKey().x = " + x);
                Log.d(TAG, "startLoadingGame.undoNextCellIndices.geyKey().y = " + y);
                gridData.addUndoNextCellIndices(new Point(x, y));
            }
            // load values on 9x9 grid
            for (int i=0; i<rowCounts; i++) {
                for (int j=0; j<colCounts; j++) {
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
                for (int i=0; i<rowCounts; i++) {
                    for (int j=0; j<colCounts; j++) {
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
            // gridData.setNextBalls(nextBalls);
            int kk = 0;
            for (HashMap.Entry<Point, Integer> entry : gridData.getNextCellIndices().entrySet()) {
                entry.setValue(nextBalls[kk++]);
            }
            gridData.setCellValues(gameCells);
            gameProperties.setCurrentScore(cScore);
            gameProperties.setUndoEnable(isUndoEnable);
            // gridData.setUndoNextBalls(undoNextBalls);
            kk = 0;
            for (HashMap.Entry<Point, Integer> entry : gridData.getUndoNextCellIndices().entrySet()) {
                entry.setValue(undoNextBalls[kk++]);
            }
            gridData.setBackupCells(backupCells);
            gameProperties.setUndoScore(unScore);
            // start update UI
            presentView.updateCurrentScoreOnUi(gameProperties.getCurrentScore());
            Log.d(TAG, "startLoadingGame.starting displayGameView().");
            displayGameView();
        } catch (IOException ex) {
            ex.printStackTrace();
            succeeded = false;
        }

        ColorBallsApp.isProcessingJob = false;
        presentView.dismissShowMessageOnScreen();

        return succeeded;
    }

    public void release() {
        cancelBouncyTimer();
        showingScoreHandler.removeCallbacksAndMessages(null);
        movingBallHandler.removeCallbacksAndMessages(null);
        soundPoolUtil.release();
    }

    private void createBitmapsAndDrawableResources(int cellWidth, int cellHeight) {
        if (cellWidth<=0 || cellHeight<=0) {
            throw new IllegalArgumentException("cellWidth and cellHeight must be > 0");
        }

        Resources resources = activity.getResources();

        int nextBallWidth = (int)(cellWidth * 0.5f);
        int nextBallHeight = (int)(cellHeight * 0.5f);
        int ovalBallWidth = (int)(cellWidth * 0.9f);
        int ovalBallHeight = (int)(cellHeight * 0.7f);

        Bitmap bm = BitmapFactory.decodeResource(resources, R.drawable.redball);
        colorBallMap.put(ColorRED, bm);
        Drawable drawable = FontAndBitmapUtil.convertBitmapToDrawable(activity, bm, nextBallWidth, nextBallHeight);
        colorNextBallMap.put(ColorRED, drawable);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(activity, bm, ovalBallWidth, ovalBallHeight);
        colorOvalBallMap.put(ColorRED, drawable);

        bm = BitmapFactory.decodeResource(resources, R.drawable.greenball);
        colorBallMap.put(ColorGREEN, bm);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(activity, bm, nextBallWidth, nextBallHeight);
        colorNextBallMap.put(ColorGREEN, drawable);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(activity, bm, ovalBallWidth, ovalBallHeight);
        colorOvalBallMap.put(ColorGREEN, drawable);

        bm = BitmapFactory.decodeResource(resources, R.drawable.blueball);
        colorBallMap.put(ColorBLUE, bm);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(activity, bm, nextBallWidth, nextBallHeight);
        colorNextBallMap.put(ColorBLUE, drawable);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(activity, bm, ovalBallWidth, ovalBallHeight);
        colorOvalBallMap.put(ColorBLUE, drawable);

        bm = BitmapFactory.decodeResource(resources, R.drawable.magentaball);
        colorBallMap.put(ColorMAGENTA, bm);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(activity, bm, nextBallWidth, nextBallHeight);
        colorNextBallMap.put(ColorMAGENTA, drawable);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(activity, bm, ovalBallWidth, ovalBallHeight);
        colorOvalBallMap.put(ColorMAGENTA, drawable);

        bm = BitmapFactory.decodeResource(resources, R.drawable.yellowball);
        colorBallMap.put(ColorYELLOW, bm);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(activity, bm, nextBallWidth, nextBallHeight);
        colorNextBallMap.put(ColorYELLOW, drawable);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(activity, bm, ovalBallWidth, ovalBallHeight);
        colorOvalBallMap.put(ColorYELLOW, drawable);

        bm = BitmapFactory.decodeResource(resources, R.drawable.cyanball);
        colorBallMap.put(ColorCYAN, bm);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(activity, bm, nextBallWidth, nextBallHeight);
        colorNextBallMap.put(ColorCYAN, drawable);
        drawable = FontAndBitmapUtil.convertBitmapToDrawable(activity, bm, ovalBallWidth, ovalBallHeight);
        colorOvalBallMap.put(ColorCYAN, drawable);
    }

    private void gameOver() {
        presentView.showGameOverDialog();
    }

    private int calculateScore(HashSet<Point> linkedLine) {
        if (linkedLine == null) {
            return 0;
        }

        int[] numBalls = new int[] {0,0,0,0,0,0};
        for (Point point : linkedLine) {
            switch (gridData.getCellValue(point.x, point.y)) {
                case ColorRED:
                    numBalls[0]++;
                    break;
                case ColorGREEN:
                    numBalls[1]++;
                    break;
                case ColorBLUE:
                    numBalls[2]++;
                    break;
                case ColorMAGENTA:
                    numBalls[3]++;
                    break;
                case ColorYELLOW:
                    numBalls[4]++;
                    break;
                case ColorCYAN:
                    numBalls[5]++;
                    break;
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

        if (!gameProperties.isEasyLevel()) {
            // difficult level
            totalScore *= 2;   // double of easy level
        }

        return totalScore;
    }

    private void drawBall(ImageView imageView, int color) {
        imageView.setImageBitmap(colorBallMap.get(color));
    }

    private void drawOval(ImageView imageView,int color) {
        imageView.setImageDrawable(colorOvalBallMap.get(color));
    }

    private void drawNextBall(ImageView imageView,int color) {
        Log.d(TAG, "drawNextBall.color = " + color);
        if (imageView != null) {
            if (gameProperties.hasNextBall()) {
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
        Log.d(TAG, "displayNextBallsView() is called");
        ImageView imageView;
        try {
            for (HashMap.Entry<Point, Integer> entry : gridData.getNextCellIndices().entrySet()) {
                int imageViewId = rowCounts * entry.getKey().x + entry.getKey().y;
                imageView = presentView.getImageViewById(imageViewId);
                drawNextBall(imageView, entry.getValue());
            }
        } catch (Exception ex) {
            Log.d(TAG, "displayNextBallsView exception: ");
            ex.printStackTrace();
        }
    }

    private void displayNextColorBalls() {
        if (gridData.randCells() == 0) {
            // no vacants, so game over
            gameOver();
            return;
        }
        //   display the balls on the nextBallsView
        displayNextBallsView();
    }

    private void clearCell(int i, int j) {
        // int id = i * colCounts + j;
        int id = i * rowCounts + j;
        ImageView imageView = presentView.getImageViewById(id);
        imageView.setImageBitmap(null);
        gridData.setCellValue(i, j, 0);
    }

    private void displayGridDataNextCells() {
        Log.d(TAG,"displayGridDataNextCells");
        int id, n1, n2;
        ImageView imageView;
        boolean hasMoreFive = false;
        HashSet<Point> linkedPoint = new HashSet<>();
        for (HashMap.Entry<Point, Integer> entry : gridData.getNextCellIndices().entrySet()) {
            n1 = entry.getKey().x;
            n2 = entry.getKey().y;
            int ballColor = gridData.getCellValue(n1, n2);
            Log.d(TAG,"displayGridDataNextCells.ballColor = " + ballColor);
            gridData.setCellValue(n1, n2, entry.getValue());
            id = n1 * rowCounts + n2;
            imageView = presentView.getImageViewById(id);
            drawBall(imageView, gridData.getCellValue(n1, n2));
            if (gridData.check_moreThanFive(n1, n2)) {
                hasMoreFive = true;
                for (Point point : gridData.getLight_line()) {
                    if (!linkedPoint.contains(point)) {
                        linkedPoint.add(new Point(point));
                    }
                }
            }
        }

        if (hasMoreFive) {
            gridData.setLight_line(linkedPoint);    // added on 2020-07-13
            gameProperties.setLastGotScore(calculateScore(gridData.getLight_line()));
            gameProperties.setUndoScore(gameProperties.getCurrentScore());
            gameProperties.setCurrentScore(gameProperties.getCurrentScore() + gameProperties.getLastGotScore());
            presentView.updateCurrentScoreOnUi(gameProperties.getCurrentScore());
            ShowScoreRunnable showScoreRunnable = new ShowScoreRunnable(gameProperties.getLastGotScore(), gridData.getLight_line(), true);
            showingScoreHandler.post(showScoreRunnable);
            Log.d(TAG,"displayGridDataNextCells.showingScoreHandler.post(showScoreRunnable).");
        } else {
            displayNextColorBalls();
        }
    }

    private void displayGameGridView() {
        // display the 9 x 9 game view
        ImageView imageView;
        try {
            for (int i = 0; i < rowCounts; i++) {
                for (int j = 0; j < colCounts; j++) {
                    int id = i * rowCounts + j;
                    imageView = presentView.getImageViewById(id);
                    int color = gridData.getCellValue(i, j);
                    if (color == 0) {
                        // imageView.setImageDrawable(null);
                        imageView.setImageBitmap(null);
                    } else {
                        drawBall(imageView, color);
                    }
                }
            }
        } catch (Exception ex) {
            Log.d(TAG, "displayGameGridView exception: ");
            ex.printStackTrace();
        }
    }

    private void displayGameView() {
        // display the 9 x 9 game view
        Log.d(TAG, "displayGameView() --> starting displayGameGridView().");
        displayGameGridView();
        // display the view of next balls
        Log.d(TAG, "displayGameView() --> starting displayNextBallsView().");
        displayNextBallsView();
    }

    private void drawBallAlongPath() {
        Log.d(TAG, "drawBallAlongPath");
        int sizeOfPathPoint = gridData.getPathPoint().size();
        if (sizeOfPathPoint<=0) {
            Log.w(TAG, "drawBallAlongPath.sizeOfPathPoint<=0");
            return;
        }

        final int ii = gridData.getPathPoint().get(0).x;  // the target point
        final int jj = gridData.getPathPoint().get(0).y;  // the target point
        Log.d(TAG, "drawBallAlongPath().ii = " + ii + ", jj = " + jj);
        final int beginI = gridData.getPathPoint().get(sizeOfPathPoint-1).x;
        final int beginJ = gridData.getPathPoint().get(sizeOfPathPoint-1).y;
        final int color = gridData.getCellValue(beginI, beginJ);
        Log.d(TAG, "drawBallAlongPath.gridData.getCellValue(beginI, beginJ) = " + color);

        gameProperties.getThreadCompleted()[0] = false;
        gameProperties.setBallMoving(true);

        final ArrayList<Point> tempList = new ArrayList<>(gridData.getPathPoint());
        Runnable runnablePath = new Runnable() {
            boolean ballYN = true;
            ImageView imageView = null;
            int countDown = tempList.size()*2 - 1;
            @Override
            public synchronized void run() {
                if (countDown >= 2) {   // eliminate start point
                    int i = countDown / 2;
                    imageView = presentView.getImageViewById(tempList.get(i).x * rowCounts + tempList.get(i).y);
                    if (ballYN) {
                        drawBall(imageView, color);
                    } else {
                        imageView.setImageBitmap(null);
                    }
                    ballYN = !ballYN;
                    countDown--;
                    movingBallHandler.postDelayed(this,20);
                    Log.d(TAG,"drawBallAlongPath.ballMovingHandler.postDelayed()");
                } else {
                    // movingBallHandler.removeCallbacksAndMessages(null);
                    clearCell(beginI, beginJ);  // blank the original cell. Added on 2020-09-16
                    ImageView v = presentView.getImageViewById(ii * rowCounts + jj);
                    Log.d(TAG, "drawBallAlongPath.gridData.setCellValue(ii, jj, color) = " + color);
                    gridData.setCellValue(ii, jj, color);
                    drawBall(v, color);
                    gridData.regenerateNextCellIndices(new Point(ii, jj));
                    //  check if there are more than five balls with same color connected together
                    if (gridData.check_moreThanFive(ii, jj)) {
                        gameProperties.setLastGotScore(calculateScore(gridData.getLight_line()));
                        gameProperties.setUndoScore(gameProperties.getCurrentScore());
                        gameProperties.setCurrentScore(gameProperties.getCurrentScore() + gameProperties.getLastGotScore());
                        presentView.updateCurrentScoreOnUi(gameProperties.getCurrentScore());
                        ShowScoreRunnable showScoreRunnable = new ShowScoreRunnable(gameProperties.getLastGotScore(), gridData.getLight_line(), false);
                        showingScoreHandler.post(showScoreRunnable);
                        Log.d(TAG,"drawBallAlongPath.showingScoreHandler.post(showScoreRunnable).");
                    } else {
                        displayGridDataNextCells();   // has a problem
                    }

                    gameProperties.getThreadCompleted()[0] = true;
                    gameProperties.setBallMoving(false);

                    Log.d(TAG,"drawBallAlongPath.run() finished.");
                }
            }
        };
        movingBallHandler.post(runnablePath);
        Log.d(TAG,"drawBallAlongPath.ballMovingHandler.post()");
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
                    bouncyHandler.postDelayed(this, 200);
                } else {
                    v.setImageDrawable(null);
                }
            }
        };
        bouncyHandler.post(bouncyRunnable);
    }

    private void cancelBouncyTimer() {
        bouncyHandler.removeCallbacksAndMessages(null);
        SystemClock.sleep(20);
    }

    private class ShowScoreRunnable implements Runnable {
        private final int lastGotScore;
        private HashSet<Point> hasPoint = null;
        private final boolean isNextBalls;
        private int counter = 0;

        public ShowScoreRunnable(final int lastGotScore, final HashSet<Point> linkedPoint, final boolean isNextBalls) {
            this.lastGotScore = lastGotScore;
            this.isNextBalls = isNextBalls;
            if (linkedPoint != null) {
                hasPoint = new HashSet<>(linkedPoint);
            }

            gameProperties.setShowNextBallsAfterBlinking(this.isNextBalls);
            gameProperties.getThreadCompleted()[1] = false;
            gameProperties.setShowingScoreMessage(true);
        }

        private synchronized void onProgressUpdate(int status) {
            switch (status) {
                case 0:
                    for (Point item : hasPoint) {
                        ImageView v = presentView.getImageViewById(item.x * rowCounts + item.y);
                        drawBall(v, gridData.getCellValue(item.x, item.y));
                    }
                    break;
                case 1:
                    for (Point item : hasPoint) {
                        ImageView v = presentView.getImageViewById(item.x * rowCounts + item.y);
                        drawOval(v, gridData.getCellValue(item.x, item.y));
                    }
                    break;
                case 2:
                    break;
                case 3:
                    //
                    // show the score
                    String scoreString = String.valueOf(lastGotScore);
                    presentView.showMessageOnScreen(scoreString);
                    Log.d(TAG, "ShowScoreRunnable.onProgressUpdate.presentView.showMessageOnScreen(scoreString).");
                    for (Point item : hasPoint) {
                        clearCell(item.x, item.y);
                    }
                    Log.d(TAG, "ShowScoreRunnable.onProgressUpdate.clearCell(item.x, item.y)");
                    // added on 2019-03-30
                    if (isNextBalls) {
                        displayNextColorBalls();
                        Log.d(TAG, "ShowScoreRunnable-->onProgressUpdate()-->displayNextColorBalls()");
                    } else {
                        // added on 2020-07-26 17:06
                        displayNextBallsView();
                        Log.d(TAG, "ShowScoreRunnable-->onProgressUpdate()-->displayNextBallsView()");
                        //
                    }
                    gameProperties.getThreadCompleted()[1] = true;  // user can start input command
                    gameProperties.setShowingScoreMessage(false);
                    Log.d(TAG, "ShowScoreRunnable.onProgressUpdate.gameProperties.setShowingScoreMessage(false)");
                    break;
                case 4:
                    presentView.dismissShowMessageOnScreen();
                    Log.d(TAG, "ShowScoreRunnable.onProgressUpdate.presentView.dismissShowMessageOnScreen().");
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
                counter++;
                if (counter <= twinkleCountDown) {
                    int md = counter % 2; // modulus
                    onProgressUpdate(md);
                    showingScoreHandler.postDelayed(this, 100);
                } else {
                    if (counter == twinkleCountDown+1) {
                        onProgressUpdate(3);    // show score
                        showingScoreHandler.postDelayed(this, 500);
                    } else {
                        // showingScoreHandler.removeCallbacksAndMessages(null);
                        onProgressUpdate(4);    // dismiss showing message
                    }
                }
            }
        }
    }
}
