package com.smile.presenters;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.smile.colorballs.ColorBallsApp;
import com.smile.colorballs.MyActivity;
import com.smile.colorballs.R;
import com.smile.model.GameProperties;
import com.smile.model.GridData;
import com.smile.smilelibraries.player_record_rest.PlayerRecordRest;
import com.smile.smilelibraries.utilities.SoundPoolUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MyActivityPresenter {

    public static final int nextBallsViewIdStart = 100;

    private final String TAG = "MyActivityPresenter";
    private final String GamePropertiesTag = "GameProperties";
    private final String savedGameFileName = "saved_game";

    private final Context context;
    private final PresentView presentView;
    private final Activity myActivity;
    private final SoundPoolUtil soundPoolUtil;
    private final Handler bouncyHandler = new Handler(Looper.getMainLooper());
    private final Handler movingBallHandler = new Handler(Looper.getMainLooper());
    private final Handler showingScoreHandler = new Handler(Looper.getMainLooper());

    private int rowCounts, colCounts;
    private GameProperties gameProperties;
    private GridData gridData;
    private int highestScore;

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

    public MyActivityPresenter(Context context, PresentView presentView) {
        this.context = context;
        this.presentView = presentView;
        this.myActivity = (MyActivity)context;
        soundPoolUtil = new SoundPoolUtil(this.context, R.raw.uhoh);
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

                        // moved drawBallAlongPath() on 2020-07-12
                        /*
                        int color = gridData.getCellValue(bouncyBallIndexI, bouncyBallIndexJ);
                        gridData.setCellValue(i, j, color);
                        clearCell(bouncyBallIndexI, bouncyBallIndexJ);
                        */

                        gameProperties.setBouncyBallIndexI(-1);
                        gameProperties.setBouncyBallIndexJ(-1);

                        // drawBallAlongPath(i,j,color);
                        drawBallAlongPath();

                        gameProperties.setUndoEnable(true);
                    } else {
                        //    make a sound
                        if (gameProperties.isHasSound()) {
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

    public boolean initializeColorBallsGame(int rowCounts, int colCounts, Bundle savedInstanceState) {
        this.rowCounts = rowCounts;
        this.colCounts = colCounts;
        if (ColorBallsApp.ScoreSQLiteDB != null) {
            highestScore = ColorBallsApp.ScoreSQLiteDB.readHighestScore();
        }
        boolean isNewGame = true;
        if (savedInstanceState == null) {
            // activity just started so new game
            Log.d(TAG, "Created new game.");
            gridData = new GridData(this.rowCounts, this.colCounts, ColorBallsApp.NumOfColorsUsedByEasy);
            gameProperties = new GameProperties(gridData);
        } else {
            Log.d(TAG, "Configuration changed and restore the original UI.");
            isNewGame = false;
            gameProperties = savedInstanceState.getParcelable(GamePropertiesTag);
            gridData = gameProperties.getGridData();
        }
        ColorBallsApp.isShowingLoadingMessage = gameProperties.isShowingLoadingMessage();
        ColorBallsApp.isProcessingJob = gameProperties.isProcessingJob();

        presentView.updateHighestScoreOnUi(highestScore);
        presentView.updateCurrentScoreOnUi(gameProperties.getCurrentScore());

        if (isNewGame) {
            displayGameView();
            displayGridDataNextCells();
        } else {
            // display the original state before changing configuration
            displayGameView();
            // need to be tested
            if (ColorBallsApp.isShowingLoadingMessage) {
                presentView.showMessageOnScreen(context.getString(R.string.loadingString));
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

    public boolean getIsEasyLevel() {
        return gameProperties.isEasyLevel();
    }

    public void setIsEasyLevel(boolean yn) {
        gameProperties.setEasyLevel(yn);
        if (gameProperties.isEasyLevel()) {
            // easy level
            gridData.setNumOfColorsUsed(ColorBallsApp.NumOfColorsUsedByEasy);
        } else {
            // difficult
            gridData.setNumOfColorsUsed(ColorBallsApp.NumOfColorsUsedByDifficult);
        }
    }

    public boolean getHasSound() {
        return gameProperties.isHasSound();
    }

    public void setHasSound(boolean hasSound) {
        gameProperties.setHasSound(hasSound);
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
        boolean isInTop10 = ColorBallsApp.ScoreSQLiteDB.isInTop10(score);
        if (isInTop10) {
            // inside top 10
            // record the current score
            ColorBallsApp.ScoreSQLiteDB.addScore(playerName, score);
            ColorBallsApp.ScoreSQLiteDB.deleteAllAfterTop10();  // only keep the top 10
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
            File inputFile = new File(ColorBallsApp.AppContext.getFilesDir(), ColorBallsApp.NumOfSavedGameFileName);
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
        presentView.showMessageOnScreen(context.getString(R.string.savingGameString));

        boolean succeeded = true;
        try {
            File outputFile = new File(context.getFilesDir(), savedGameFileName);
            FileOutputStream foStream = new FileOutputStream(outputFile);
            // save settings
            if (gameProperties.isHasSound()) {
                foStream.write(1);
            } else {
                foStream.write(0);
            }
            if (gameProperties.isEasyLevel()) {
                foStream.write(1);
            } else {
                foStream.write(0);
            }
            // save next balls
            // foStream.write(gridData.ballNumOneTime);
            foStream.write(GridData.ballNumOneTime);
            for (int i=0; i<ColorBallsApp.NumOfColorsUsedByDifficult; i++) {
                foStream.write(gridData.getNextBalls()[i]);
            }
            // save values on 9x9 grid
            for (int i=0; i<rowCounts; i++) {
                for (int j=0; j<colCounts; j++) {
                    foStream.write(gridData.getCellValue(i, j));
                }
            }
            // save current score
            byte[] scoreByte = ByteBuffer.allocate(4).putInt(gameProperties.getCurrentScore()).array();
            foStream.write(scoreByte);
            // save undoEnable
            if (gameProperties.isUndoEnable()) {
                // can undo
                foStream.write(1);
                // foStream.write(gridData.ballNumOneTime);
                foStream.write(GridData.ballNumOneTime);
                // save undoNextBalls
                for (int i=0; i<ColorBallsApp.NumOfColorsUsedByDifficult; i++) {
                    foStream.write(gridData.getUndoNextBalls()[i]);
                }
                // save backupCells
                for (int i=0; i<rowCounts; i++) {
                    for (int j=0; j<colCounts; j++) {
                        foStream.write(gridData.getBackupCells()[i][j]);
                    }
                }
                byte[] undoScoreByte = ByteBuffer.allocate(4).putInt(gameProperties.getUndoScore()).array();
                foStream.write(undoScoreByte);
                // end of writing

                numOfSaved++;
                // save numOfSaved back to file (ColorBallsApp.NumOfSavedGameFileName)
                outputFile = new File(ColorBallsApp.AppContext.getFilesDir(), ColorBallsApp.NumOfSavedGameFileName);
                foStream = new FileOutputStream(outputFile);
                foStream.write(numOfSaved);
                foStream.close();
                //
            } else {
                // no undo
                foStream.write(0);
                // end of writing
            }
            foStream.close();
            Log.d(TAG, "Succeeded to startSavingGame().");
        } catch (IOException ex) {
            ex.printStackTrace();
            succeeded = false;
            Log.d(TAG, "Failed to startSavingGame().");
        }

        ColorBallsApp.isProcessingJob = false;
        presentView.dismissShowMessageOnScreen();

        Log.d(TAG, "startSavingGame() finished");

        return succeeded;
    }

    public boolean startLoadingGame() {
        ColorBallsApp.isProcessingJob = true;
        presentView.showMessageOnScreen(context.getString(R.string.loadingGameString));

        boolean succeeded = true;
        boolean soundYn = gameProperties.isHasSound();
        boolean easyYn = gameProperties.isEasyLevel();
        int ballNumOneTime;
        int[] nextBalls = new int[ColorBallsApp.NumOfColorsUsedByDifficult];
        int[][] gameCells = new int[rowCounts][colCounts];
        int cScore = gameProperties.getCurrentScore();
        boolean undoYn = gameProperties.isUndoEnable();
        int[] undoNextBalls = new int[ColorBallsApp.NumOfColorsUsedByDifficult];
        int[][] backupCells = new int[rowCounts][colCounts];
        int unScore = gameProperties.getUndoScore();

        try {
            File inputFile = new File(ColorBallsApp.AppContext.getFilesDir(), savedGameFileName);
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
            for (int i=0; i<ColorBallsApp.NumOfColorsUsedByDifficult; i++) {
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
                // undoNumOneTime = fiStream.read();
                fiStream.read();
                for (int i=0; i<ColorBallsApp.NumOfColorsUsedByDifficult; i++) {
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

        ColorBallsApp.isProcessingJob = false;
        presentView.dismissShowMessageOnScreen();

        if (succeeded) {
            // reflesh Main UI with loaded data
            setHasSound(soundYn);
            setIsEasyLevel(easyYn);
            gridData.setNextBalls(nextBalls);
            gridData.setCellValues(gameCells);
            gameProperties.setCurrentScore(cScore);
            gameProperties.setUndoEnable(undoYn);
            gridData.setUndoNextBalls(undoNextBalls);
            gridData.setBackupCells(backupCells);
            gameProperties.setUndoScore(unScore);
            // start update UI
            presentView.updateCurrentScoreOnUi(gameProperties.getCurrentScore());
            displayGameView();
        }

        return succeeded;
    }

    public void release() {
        cancelBouncyTimer();
        if (showingScoreHandler != null) {
            showingScoreHandler.removeCallbacksAndMessages(null);
        }
        if (movingBallHandler != null) {
            movingBallHandler.removeCallbacksAndMessages(null);
        }
        if (soundPoolUtil != null) {
            soundPoolUtil.release();
        }
    }

    private void gameOver() {
        presentView.showGameOverDialog();
    }

    private int calculateScore(int numBalls) {
        // 5 balls --> 5
        // 6 balls --> 5 + (6-5)*2
        // 7 balls --> 5 + (6-5)*2 + (7-5)*2
        // 8 balls --> 5 + (6-5)*2 + (7-5)*2 + (8-5)*2
        // n balls --> 5 + (6-5)*2 + (7-5)*2 + ... + (n-5)*2
        int minBalls = 5;
        int minScore = 5;
        int score = minScore;
        int extraBalls = numBalls - minBalls;
        if (extraBalls > 0) {
            // greater than 5 balls
            int rate  = 2;
            for (int i=1 ; i<=extraBalls ; i++) {
                // rate = 2;   // added on 2018-10-02
                score += i * rate ;
            }
        }

        if (!gameProperties.isEasyLevel()) {
            // difficult level
            score = score * 2;   // double of easy level
        }

        return score;
    }

    private void drawBall(ImageView imageView, int color) {
        imageView.setImageBitmap(ColorBallsApp.colorBallMap.get(color));
    }

    private void drawOval(ImageView imageView,int color) {
        imageView.setImageBitmap(ColorBallsApp.colorOvalBallMap.get(color));
    }

    private void displayNextBallsView() {
        // display the view of next balls
        ImageView imageView;
        // int numOneTime = gridData.ballNumOneTime;
        int numOneTime = GridData.ballNumOneTime;
        for (int i = 0; i < numOneTime; i++) {
            imageView = presentView.getImageViewById(nextBallsViewIdStart + i);
            drawBall(imageView, gridData.getNextBalls()[i]);
        }
    }

    private void displayNextColorBalls() {
        gridData.randColors();  //   next  balls
        //   display the balls on the nextBallsView
        displayNextBallsView();
    }

    private void clearCell(int i, int j) {
        // int id = i * colCounts + j;
        int id = i * rowCounts + j;
        ImageView imageView = presentView.getImageViewById(id);
        // imageView.setImageDrawable(null);
        imageView.setImageBitmap(null);
        gridData.setCellValue(i, j, 0);
    }

    private void displayGridDataNextCells() {
        gridData.randCells();
        int id, n1, n2;
        ImageView imageView;
        boolean hasMoreFive = false;
        HashSet<Point> linkedPoint = new HashSet<>();
        for (Point nextCellIndex : gridData.getNextCellIndex()) {
            n1 = nextCellIndex.x;
            n2 = nextCellIndex.y;
            id = n1 * rowCounts + n2;
            imageView = presentView.getImageViewById(id);
            drawBall(imageView,gridData.getCellValue(n1, n2));
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
            // gameProperties.setLastGotScore(calculateScore(linkedPoint.size()));    // removed on 2020-07-13
            gameProperties.setLastGotScore(calculateScore(gridData.getLight_line().size()));
            gameProperties.setUndoScore(gameProperties.getCurrentScore());
            gameProperties.setCurrentScore(gameProperties.getCurrentScore() + gameProperties.getLastGotScore());
            presentView.updateCurrentScoreOnUi(gameProperties.getCurrentScore());
            // ShowScoreRunnable showScoreRunnable = new ShowScoreRunnable(gameProperties.getLastGotScore(), linkedPoint, true);
            ShowScoreRunnable showScoreRunnable = new ShowScoreRunnable(gameProperties.getLastGotScore(), gridData.getLight_line(), true);
            showingScoreHandler.post(showScoreRunnable);
            Log.d(TAG,"displayGridDataNextCells() --> showingScoreHandler.post(showScoreRunnable).");

            // added for testing
            /*
            Configuration configuration = context.getResources().getConfiguration();
            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Log.d(TAG, "displayGridDataNextCells()-->setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)");
                myActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                Log.d(TAG, "displayGridDataNextCells()-->setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)");
                myActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            */
            //
        } else {
            // check if game over
            boolean gameOverYn = gridData.getGameOver();
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
        Log.d(TAG, "rowCounts = " + rowCounts);
        Log.d(TAG, "colCounts = " + colCounts);
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
    }

    private void displayGameView() {

        // display the view of next balls
        displayNextBallsView();

        // display the 9 x 9 game view
        displayGameGridView();
    }

    // private void drawBallAlongPath(final int i , final int j, final int col) {
    private void drawBallAlongPath() {
        int sizeOfPathPoint = gridData.getPathPoint().size();
        if (sizeOfPathPoint<=0) {
            return;
        }

        final int ii = gridData.getPathPoint().get(0).x;  // the target point
        final int jj = gridData.getPathPoint().get(0).y;  // the target point
        final int beginI = gridData.getPathPoint().get(sizeOfPathPoint-1).x;
        final int beginJ = gridData.getPathPoint().get(sizeOfPathPoint-1).y;
        final int color = gridData.getCellValue(beginI, beginJ);
        Log.d(TAG,"drawBallAlongPath() --> gridData.getCellValue(gridData.getPathPoint().get(sizeOfPathPoint-1).x, gridData.getPathPoint().get(sizeOfPathPoint-1).y) = " + color);
        gameProperties.getThreadCompleted()[0] = false;
        gameProperties.setBallMoving(true);

        final List<Point> tempList = new ArrayList<>(gridData.getPathPoint());
        Runnable runnablePath = new Runnable() {
            boolean ballYN = true;
            ImageView imageView = null;
            int countDown = tempList.size()*2 - 1;
            @Override
            public void run() {
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
                    Log.d(TAG,"drawBallAlongPath() --> ballMovingHandler.postDelayed()");
                } else {
                    movingBallHandler.removeCallbacksAndMessages(null);

                    clearCell(beginI, beginJ);
                    ImageView v = presentView.getImageViewById(ii * rowCounts + jj);
                    gridData.setCellValue(ii, jj, color);
                    drawBall(v, color);
                    // drawBall(v, gridData.getCellValue(ii, jj));

                    //  check if there are more than five balls with same color connected together
                    if (gridData.check_moreThanFive(ii, jj)) {
                        gameProperties.setLastGotScore(calculateScore(gridData.getLight_line().size()));
                        gameProperties.setUndoScore(gameProperties.getCurrentScore());
                        gameProperties.setCurrentScore(gameProperties.getCurrentScore() + gameProperties.getLastGotScore());
                        presentView.updateCurrentScoreOnUi(gameProperties.getCurrentScore());
                        ShowScoreRunnable showScoreRunnable = new ShowScoreRunnable(gameProperties.getLastGotScore(), gridData.getLight_line(), false);
                        showingScoreHandler.post(showScoreRunnable);
                        Log.d(TAG,"drawBallAlongPath() --> showingScoreHandler.post(showScoreRunnable).");
                    } else {
                        displayGridDataNextCells();   // has a problem
                        Log.d(TAG,"drawBallAlongPath() --> displayGridDataNextCells().");
                    }

                    gameProperties.getThreadCompleted()[0] = true;
                    gameProperties.setBallMoving(false);

                    Log.d(TAG,"drawBallAlongPath() --> run() finished.");
                }
            }
        };
        movingBallHandler.post(runnablePath);
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
                    bouncyHandler.postDelayed(this, 200);
                } else {
                    // v.setImageResource(R.drawable.boximage);
                    v.setImageDrawable(null);
                }
            }
        };
        bouncyHandler.post(bouncyRunnable);
    }

    private void cancelBouncyTimer() {
        if (bouncyHandler != null) {
            bouncyHandler.removeCallbacksAndMessages(null);
        }
        SystemClock.sleep(20);
    }

    private class ShowScoreRunnable implements Runnable {
        private final int color;
        private final int lastGotScore;
        private HashSet<Point> hasPoint = null;
        private boolean isNextBalls;

        private int twinkleCountDown = 5;
        private int counter = 0;

        public ShowScoreRunnable(int lastGotScore, HashSet<Point> linkedPoint, boolean isNextBalls) {
            this.lastGotScore = lastGotScore;
            this.isNextBalls = isNextBalls;
            int colorTmp = 0;
            if (linkedPoint != null) {
                hasPoint = new HashSet<>(linkedPoint);
                Point point = hasPoint.iterator().next();
                colorTmp = gridData.getCellValue(point.x, point.y);
            }
            color = colorTmp;

            gameProperties.setShowNextBallsAfterBlinking(this.isNextBalls);
            gameProperties.getThreadCompleted()[1] = false;
            gameProperties.setShowingScoreMessage(true);
        }

        private synchronized void onProgressUpdate(int status) {
            switch (status) {
                case 0:
                    for (Point item : hasPoint) {
                        ImageView v = presentView.getImageViewById(item.x * rowCounts + item.y);
                        drawBall(v, color);
                    }
                    break;
                case 1:
                    for (Point item : hasPoint) {
                        ImageView v = presentView.getImageViewById(item.x * rowCounts + item.y);
                        drawOval(v, color);
                    }
                    break;
                case 2:
                    break;
                case 3:
                    // show the score
                    String scoreString = String.valueOf(lastGotScore);
                    presentView.showMessageOnScreen(scoreString);
                    for (Point item : hasPoint) {
                        clearCell(item.x, item.y);
                    }
                    // added on 2019-03-30
                    if (isNextBalls) {
                        displayNextColorBalls();
                    }
                    //
                    gameProperties.getThreadCompleted()[1] = true;  // user can start input command
                    gameProperties.setShowingScoreMessage(false);
                    break;
                case 4:
                    presentView.dismissShowMessageOnScreen();
                    break;
            }
        }

        @Override
        public synchronized void run() {
            if (hasPoint == null) {
                Log.d(TAG, "ShowScoreRunnable-->onProgressUpdate()-->hasPoint is null.");
                showingScoreHandler.removeCallbacksAndMessages(null);
            } else {
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
                        showingScoreHandler.removeCallbacksAndMessages(null);
                        onProgressUpdate(4);    // dismiss showing message
                    }
                }
            }
        }
    }
}