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
        void showGameOverDialog();
        void showSaveScoreAlertDialog(final int entryPoint, final int score);
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
        if (gameProperties.getBouncingStatus() == 0) {
            if (gridData.getCellValue(i, j) != 0) {
                if ((gameProperties.getBouncyBallIndexI() == -1) && (gameProperties.getBouncyBallIndexJ() == -1)) {
                    gameProperties.setBouncingStatus(1);
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
                if ((bouncyBallIndexI >= 0) && (gameProperties.getBouncyBallIndexJ() >= 0)) {
                    if (gridData.canMoveCellToCell(new Point(bouncyBallIndexI, bouncyBallIndexJ), new Point(i, j))) {
                        // cancel the timer
                        gameProperties.setBouncingStatus(0);
                        cancelBouncyTimer();
                        int color = gridData.getCellValue(bouncyBallIndexI, bouncyBallIndexJ);
                        gridData.setCellValue(i, j, color);
                        clearCell(bouncyBallIndexI, bouncyBallIndexJ);

                        gameProperties.setBouncyBallIndexI(-1);
                        gameProperties.setBouncyBallIndexJ(-1);

                        drawBallAlongPath(i,j,color);

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

                    // there bugs here
                    gameProperties.setBouncingStatus(0);    // this statement should be in cancelBouncyTimer()
                    cancelBouncyTimer();
                    gameProperties.setBouncingStatus(1);    // this statement should be in cancelBouncyTimer()
                    //

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
            if (gameProperties.isShowingScoreMessage()) {
                ShowScoreThread showScoreThread = new ShowScoreThread(gridData.getLight_line(), gameProperties.isShowNextBallsAfterBlinking());
                showScoreThread.startShow();
            }
            if (gameProperties.getBouncingStatus() == 1) {
                int bouncyBallIndexI = gameProperties.getBouncyBallIndexI();
                int bouncyBallIndexJ = gameProperties.getBouncyBallIndexJ();
                ImageView v = presentView.getImageViewById(bouncyBallIndexI * rowCounts + bouncyBallIndexJ);
                drawBouncyBall(v, gridData.getCellValue(bouncyBallIndexI, bouncyBallIndexJ));
            }

            if (gameProperties.isShowingNewGameDialog()) {
                Log.d(TAG, "createGameView() --> show new game dialog by calling newGame()");
                newGame();
            }
            if (gameProperties.isShowingQuitGameDialog()) {
                Log.d(TAG, "createGameView() --> show quit game dialog by calling quitGame()");
                quitGame();
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

        // restore the screen
        displayGameView();

        gameProperties.setBouncingStatus(0);
        gameProperties.setBouncyBallIndexI(-1);
        gameProperties.setBouncyBallIndexJ(-1);

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

    private int calculateScore(int numBalls) {
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
            int rate  = 2;
            for (int i=1 ; i<=Math.abs(numBalls-minBalls) ; i++) {
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
        int numOneTime = GridData.ballNumOneTime;

        int[] indexi = gridData.getNextCellIndexI();
        int[] indexj = gridData.getNextCellIndexJ();

        int id, n1, n2;
        ImageView imageView;
        for (int i = 0; i < numOneTime; i++) {
            n1 = indexi[i];
            n2 = indexj[i];
            if ((n1 >= 0) && (n2 >= 0)) {
                // id = n1 * colCounts + n2;
                id = n1 * rowCounts + n2;
                imageView = presentView.getImageViewById(id);
                drawBall(imageView,gridData.getCellValue(n1, n2));
            }
        }

        boolean hasMoreFive = false;
        HashSet<Point> linkedPoint = new HashSet<>();
        for (int i = 0; i < numOneTime; i++) {
            n1 = indexi[i];
            n2 = indexj[i];
            if ((n1 >= 0) && (n2 >= 0)) {
                if (gridData.getCellValue(n1, n2) != 0) {
                    //   has  color in this cell
                    if (gridData.check_moreThanFive(n1, n2) == 1) {
                        hasMoreFive = true;
                        for (Point point : gridData.getLight_line()) {
                            // if (!linkedPoint.contains(point)) {
                            linkedPoint.add(point);
                            // }
                        }
                    }
                }
            }
        }

        if (hasMoreFive) {
            gameProperties.setLastGotScore(calculateScore(linkedPoint.size()));
            ShowScoreThread showScoreThread = new ShowScoreThread(linkedPoint, true);
            showScoreThread.startShow();
        } else {
            // check if game over
            boolean gameOverYn = gridData.getGameOver();
            if (gameOverYn) {
                //  game over
                presentView.showGameOverDialog();
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

    private void drawBallAlongPath(final int ii , final int jj,final int color) {
        if (gridData.getPathPoint().size()<=0) {
            return;
        }
        gameProperties.getThreadCompleted()[0] = false;

        final List<Point> tempList = new ArrayList<>(gridData.getPathPoint());
        final Handler drawHandler = new Handler(Looper.getMainLooper());
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
                        // imageView.setImageDrawable(null);
                        imageView.setImageBitmap(null);
                    }
                    ballYN = !ballYN;
                    countDown--;
                    drawHandler.postDelayed(this,20);
                } else {
                    drawHandler.removeCallbacksAndMessages(null);
                    ImageView v = presentView.getImageViewById(ii * rowCounts + jj);
                    drawBall(v, gridData.getCellValue(ii, jj));
                    //  check if there are more than five balls with same color connected together
                    if (gridData.check_moreThanFive(ii, jj) == 1) {
                        gameProperties.setLastGotScore(calculateScore(gridData.getLight_line().size()));
                        ShowScoreThread showScoreThread = new ShowScoreThread(gridData.getLight_line(), false);
                        showScoreThread.startShow();
                    } else {
                        displayGridDataNextCells();   // has a problem
                    }

                    gameProperties.getThreadCompleted()[0] = true;
                }
            }
        };
        drawHandler.post(runnablePath);
        // added for testing
        /*
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE ) {
            Log.d(TAG, "drawBallAlongPath()-->setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)");
            myActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            Log.d(TAG, "drawBallAlongPath()-->setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)");
            myActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
         */
        //
    }

    private void drawBouncyBall(final ImageView v, final int color) {
        Runnable bouncyRunnable = new Runnable() {
            boolean ballYN = false;
            @Override
            public void run() {
                if (gameProperties.getBouncingStatus() == 1) {
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
        }
        SystemClock.sleep(20);
    }

    private class ShowScoreThread extends Thread {
        private int color = 0;
        private HashSet<Point> hasPoint = null;
        private boolean isNextBalls;

        private final Handler showScoreHandler = new Handler(Looper.getMainLooper());
        private boolean isSynchronizeFinished = false;

        public ShowScoreThread(HashSet<Point> linkedPoint, boolean isNextBalls) {
            this.isNextBalls = isNextBalls;
            if (linkedPoint != null) {
                hasPoint = new HashSet<>(linkedPoint);
                Point point = hasPoint.iterator().next();
                color = gridData.getCellValue(point.x, point.y);
            }
            gameProperties.setShowNextBallsAfterBlinking(this.isNextBalls);
        }

        private synchronized void onPreExecute() {
            gameProperties.getThreadCompleted()[1] = false;
            isSynchronizeFinished = false;
            myActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (showScoreHandler) {
                        isSynchronizeFinished = true;
                        showScoreHandler.notifyAll();
                        Log.d(TAG, "ShowScoreThread-->onPreExecute() --> notifyAll()");
                    }
                }
            });
            synchronized (showScoreHandler) {
                while (!isSynchronizeFinished) {
                    try {
                        Log.d(TAG, "ShowScoreThread-->onPreExecute() --> wait()");
                        showScoreHandler.wait();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "ShowScoreThread-->onPreExecute() wait exception");
                        e.printStackTrace();
                    }
                }
            }

            Log.d(TAG, "ShowScoreThread-->onPreExecute() is finished.");
        }

        private synchronized void doInBackground() {
            if (hasPoint != null) {
                int twinkleCountDown = 5;
                for (int i = 1; i <= twinkleCountDown; i++) {
                    int md = i % 2; // modulus
                    onProgressUpdate(md);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                onProgressUpdate(2);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                onProgressUpdate(3);
            } else {
                Log.d(TAG, "ShowScoreThread-->doInBackground()-->hasPoint is null.");
            }
            Log.d(TAG, "ShowScoreThread-->doInBackground() is finished.");
        }

        private synchronized void onProgressUpdate(int status) {
            if (hasPoint != null) {
                isSynchronizeFinished = false;
                myActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (showScoreHandler) {
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
                                case 3:
                                    // show the score
                                    String scoreString = String.valueOf(gameProperties.getLastGotScore());
                                    presentView.showMessageOnScreen(scoreString);
                                    break;
                            }
                            isSynchronizeFinished = true;
                            showScoreHandler.notifyAll();
                            Log.d(TAG, "ShowScoreThread-->onProgressUpdate() --> notifyAll()");
                        }
                    }
                });
                synchronized (showScoreHandler) {
                    while (!isSynchronizeFinished) {
                        try {
                            Log.d(TAG, "ShowScoreThread-->onProgressUpdate() --> wait()");
                            showScoreHandler.wait();
                        } catch (InterruptedException e) {
                            Log.d(TAG, "ShowScoreThread-->onProgressUpdate() wait exception");
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                Log.d(TAG, "ShowScoreThread-->onProgressUpdate()-->hasPoint is null.");
            }
            Log.d(TAG, "ShowScoreThread-->onProgressUpdate() is finished.");
        }

        private synchronized void onPostExecute() {
            if (hasPoint != null) {
                myActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // clear values of cells
                        for (Point item : hasPoint) {
                            clearCell(item.x, item.y);
                        }
                        // update the UI
                        gameProperties.setUndoScore(gameProperties.getCurrentScore());
                        gameProperties.setCurrentScore(gameProperties.getCurrentScore() + gameProperties.getLastGotScore());
                        presentView.updateCurrentScoreOnUi(gameProperties.getCurrentScore());
                        // hide score ImageView
                        presentView.dismissShowMessageOnScreen();
                        // added on 2019-03-30
                        if (isNextBalls) {
                            displayNextColorBalls();
                        }

                        gameProperties.getThreadCompleted()[1] = true;  // user can start input command
                        gameProperties.setShowingScoreMessage(false);

                        Log.d(TAG, "ShowScoreThread-->onPostExecute()-->hasPoint is not null.");
                        Log.d(TAG, "ShowScoreThread-->onPostExecute() is finished.");
                    }
                });
            } else {
                gameProperties.getThreadCompleted()[1] = true;  // user can start input command
                gameProperties.setShowingScoreMessage(false);

                Log.d(TAG, "ShowScoreThread-->onPostExecute()-->hasPoint is null.");
                Log.d(TAG, "ShowScoreThread-->onPostExecute() is finished.");
            }
        }

        @Override
        public synchronized void run() {
            super.run();
            onPreExecute();
            doInBackground();
            onPostExecute();
        }

        public void startShow() {
            gameProperties.setShowingScoreMessage(true);
            start();
        }
    }
}
