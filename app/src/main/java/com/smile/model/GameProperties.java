package com.smile.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.smile.colorballs.ColorBallsApp;

public class GameProperties implements Parcelable {
    private boolean isShowingLoadingMessage;
    private boolean isShowingSavingGameMessage;
    private boolean isShowingLoadingGameMessage;
    private boolean isProcessingJob;
    private boolean isShowingNewGameDialog;
    private boolean isShowingQuitGameDialog;
    private boolean[] threadCompleted;
    private int bouncyBallIndexI;
    private int bouncyBallIndexJ;
    private int bouncingStatus;
    private boolean undoEnable;
    private int currentScore;
    private int undoScore;
    private boolean isEasyLevel;
    private boolean hasSound;
    private GridData gridData;

    public boolean isShowingLoadingMessage() {
        return isShowingLoadingMessage;
    }

    public void setShowingLoadingMessage(boolean showingLoadingMessage) {
        isShowingLoadingMessage = showingLoadingMessage;
    }

    public boolean isShowingSavingGameMessage() {
        return isShowingSavingGameMessage;
    }

    public void setShowingSavingGameMessage(boolean showingSavingGameMessage) {
        isShowingSavingGameMessage = showingSavingGameMessage;
    }

    public boolean isShowingLoadingGameMessage() {
        return isShowingLoadingGameMessage;
    }

    public void setShowingLoadingGameMessage(boolean showingLoadingGameMessage) {
        isShowingLoadingGameMessage = showingLoadingGameMessage;
    }

    public boolean isProcessingJob() {
        return isProcessingJob;
    }

    public void setProcessingJob(boolean processingJob) {
        isProcessingJob = processingJob;
    }

    public boolean isShowingNewGameDialog() {
        return isShowingNewGameDialog;
    }

    public void setShowingNewGameDialog(boolean isShowingRecordScoreDialog) {
        this.isShowingNewGameDialog = isShowingRecordScoreDialog;
    }

    public boolean isShowingQuitGameDialog() {
        return isShowingQuitGameDialog;
    }

    public void setShowingQuitGameDialog(boolean isShowingQuitGameDialog) {
        this.isShowingQuitGameDialog = isShowingQuitGameDialog;
    }

    public boolean[] getThreadCompleted() {
        return threadCompleted;
    }

    public void setThreadCompleted(boolean[] threadCompleted) {
        this.threadCompleted = threadCompleted;
    }

    public int getBouncyBallIndexI() {
        return bouncyBallIndexI;
    }

    public void setBouncyBallIndexI(int bouncyBallIndexI) {
        this.bouncyBallIndexI = bouncyBallIndexI;
    }

    public int getBouncyBallIndexJ() {
        return bouncyBallIndexJ;
    }

    public void setBouncyBallIndexJ(int bouncyBallIndexJ) {
        this.bouncyBallIndexJ = bouncyBallIndexJ;
    }

    public int getBouncingStatus() {
        return bouncingStatus;
    }

    public void setBouncingStatus(int bouncingStatus) {
        this.bouncingStatus = bouncingStatus;
    }

    public boolean isUndoEnable() {
        return undoEnable;
    }

    public void setUndoEnable(boolean undoEnable) {
        this.undoEnable = undoEnable;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public int getUndoScore() {
        return undoScore;
    }

    public void setUndoScore(int undoScore) {
        this.undoScore = undoScore;
    }

    public boolean isEasyLevel() {
        return isEasyLevel;
    }

    public void setEasyLevel(boolean easyLevel) {
        isEasyLevel = easyLevel;
    }

    public boolean isHasSound() {
        return hasSound;
    }

    public void setHasSound(boolean hasSound) {
        this.hasSound = hasSound;
    }

    public GridData getGridData() {
        return gridData;
    }

    public void setGridData(GridData gridData) {
        this.gridData = gridData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isShowingLoadingMessage ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isShowingSavingGameMessage ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isShowingLoadingGameMessage ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isProcessingJob ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isShowingNewGameDialog ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isShowingQuitGameDialog ? (byte) 1 : (byte) 0);
        dest.writeBooleanArray(this.threadCompleted);
        dest.writeInt(this.bouncyBallIndexI);
        dest.writeInt(this.bouncyBallIndexJ);
        dest.writeInt(this.bouncingStatus);
        dest.writeByte(this.undoEnable ? (byte) 1 : (byte) 0);
        dest.writeInt(this.currentScore);
        dest.writeInt(this.undoScore);
        dest.writeByte(this.isEasyLevel ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasSound ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.gridData, flags);
    }

    public GameProperties(int rowCounts, int colCounts) {
        isShowingLoadingMessage = false;
        isShowingSavingGameMessage = false;
        isShowingLoadingGameMessage = false;
        isProcessingJob = false;
        isShowingNewGameDialog = false;
        isShowingQuitGameDialog = false;
        threadCompleted = new boolean[] {true,true,true,true,true,true,true,true,true,true};
        bouncyBallIndexI = -1;
        bouncyBallIndexJ = -1;  // the array index that the ball has been selected
        bouncingStatus = 0;     //  no cell selected
        undoEnable = false;
        currentScore = 0;
        undoScore = 0;
        isEasyLevel = true; // start with easy level
        hasSound = true;    // has sound effect
        gridData = new GridData(rowCounts, colCounts, ColorBallsApp.NumOfColorsUsedByEasy);
    }

    protected GameProperties(Parcel in) {
        this.isShowingLoadingMessage = in.readByte() != 0;
        this.isShowingSavingGameMessage = in.readByte() != 0;
        this.isShowingLoadingGameMessage = in.readByte() != 0;
        this.isProcessingJob = in.readByte() != 0;
        this.isShowingNewGameDialog = in.readByte() != 0;
        this.isShowingQuitGameDialog = in.readByte() != 0;
        this.threadCompleted = in.createBooleanArray();
        this.bouncyBallIndexI = in.readInt();
        this.bouncyBallIndexJ = in.readInt();
        this.bouncingStatus = in.readInt();
        this.undoEnable = in.readByte() != 0;
        this.currentScore = in.readInt();
        this.undoScore = in.readInt();
        this.isEasyLevel = in.readByte() != 0;
        this.hasSound = in.readByte() != 0;
        this.gridData = in.readParcelable(GridData.class.getClassLoader());
    }

    public static final Creator<GameProperties> CREATOR = new Creator<GameProperties>() {
        @Override
        public GameProperties createFromParcel(Parcel source) {
            return new GameProperties(source);
        }

        @Override
        public GameProperties[] newArray(int size) {
            return new GameProperties[size];
        }
    };
}