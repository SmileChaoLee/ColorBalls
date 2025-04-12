package com.smile.colorballs.models;

import android.graphics.Point;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.smile.colorballs.constants.Constants;
import com.smile.colorballs.presenters.MyPresenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class GridData implements Parcelable {
    private static final String TAG = "GridData";

    public static final int mBallNumOneTime = 3;
    private int mBallNumCompleted = 5;
    private final int mRowCounts, mColCounts;
    private final int[][] mCellValues;
    private final int[][] mBackupCells;
    private HashMap<Point, Integer> mNextCellIndices;
    private HashMap<Point, Integer> mUndoNextCellIndices;
    private final HashSet<Point> mLightLine;
    private final ArrayList<Point> mPathPoint;

    private final Random mRandom;

    // 5 colors for easy level, 6 colors for difficult level
    private int mNumOfColorsUsed;

    public GridData(int rowCounts , int colCounts, int numOfColorsUsed) {
        mRowCounts = rowCounts;
        mColCounts = colCounts;
        mNumOfColorsUsed = numOfColorsUsed;

        mCellValues = new int[rowCounts][colCounts];
        mBackupCells = new int[rowCounts][colCounts];
        mNextCellIndices = new HashMap<>();
        mUndoNextCellIndices = new HashMap<>();
        mLightLine = new HashSet<>();
        mPathPoint = new ArrayList<>();
        for (int i=0 ; i<rowCounts ; i++) {
            Arrays.fill(mCellValues[i],0);
            Arrays.fill(mBackupCells[i],0);
        }
        mRandom = new Random(System.currentTimeMillis());

        // next ball colors and their positions
        randCells();
        //
    }

    public void setCellValue(int i,int j , int value) {
        mCellValues[i][j] = value;
    }
    public int getCellValue(int i , int j) {
        return mCellValues[i][j];
    }
    public void setCellValues(int[][] cellValues) {
        for (int i = 0; i< mRowCounts; i++) {
            System.arraycopy(cellValues[i], 0, mCellValues[i], 0, cellValues[i].length);
        }
    }

    public int[][] getBackupCells() {
        return mBackupCells;
    }
    public void setBackupCells(int[][] backupCells) {
        for (int i = 0; i< mRowCounts; i++) {
            System.arraycopy(backupCells[i], 0, mBackupCells[i], 0, backupCells[i].length);
        }
    }

    public void setNumOfColorsUsed(int numOfColorsUsed) {
        mNumOfColorsUsed = numOfColorsUsed;
    }

    public int randCells() {
        mNextCellIndices.clear();
        return generateNextCellIndices(0);
    }

    public void regenerateNextCellIndices(Point point) {
        Log.d(TAG, "regenerateNextCellIndices");
        if (mNextCellIndices.containsKey(point)) {
            // if nextCellIndices contains the target cell
            Integer cellColor = mNextCellIndices.remove(point);
            Log.d(TAG, "regenerateNextCellIndices.cellColor = " + cellColor);
            // generate only one next cell index
            generateNextCellIndices(cellColor);
        }
    }

    private int generateNextCellIndices(Integer cColor) {
        Log.d(TAG, "generateNextCellIndices.cColor = " + cColor );
        // find the all vacant cell that are not occupied by color balls
        final ArrayList<Point> vacantCellList = new ArrayList<>();
        for (int i = 0; i< mRowCounts; i++) {
            for (int j = 0; j< mColCounts; j++) {
                if (mCellValues[i][j] == 0) {
                    vacantCellList.add(new Point(i, j));
                }
            }
        }
        int vacantSize = vacantCellList.size();
        Log.d(TAG, "generateNextCellIndices.vacantSize = " + vacantSize);
        if (vacantSize == 0) {
            return 0;   // no vacant so game over
        }

        int maxLoop = cColor!=0? 1: vacantSize;
        int k = 0, n1, nn;
        Point point;
        while (k<Math.min(mBallNumOneTime, maxLoop)) {
            n1 = mRandom.nextInt(vacantSize);
            point = vacantCellList.get(n1);
            if (!mNextCellIndices.containsKey(point)) {
                nn = mRandom.nextInt(mNumOfColorsUsed);
                mNextCellIndices.put(vacantCellList.get(n1), Constants.BallColor[nn]);
                k++;
            }
        }
        int numOfTotalBalls = mRowCounts * mColCounts - vacantSize;
        Log.d(TAG, "generateNextCellIndices.k = " + k + ", numOfTotalBalls = " + numOfTotalBalls);
        return vacantSize;
    }

    public void undoTheLast() {
        mNextCellIndices = new HashMap<>(mUndoNextCellIndices);
        // restore CellValues;
        for (int i = 0; i< mRowCounts; i++) {
            System.arraycopy(mBackupCells[i], 0, mCellValues[i], 0, mBackupCells[i].length);
        }
    }

    public HashMap<Point, Integer> getNextCellIndices() {
        return mNextCellIndices;
    }
    public void setNextCellIndices(HashMap<Point, Integer> nextCellIndices) {
        mNextCellIndices = new HashMap<>(nextCellIndices);
    }
    public void addNextCellIndices(Point point) {
        mNextCellIndices.put(point, mCellValues[point.x][point.y]);
    }

    public HashMap<Point,Integer> getUndoNextCellIndices() {
        return mUndoNextCellIndices;
    }
    public void setUndoNextCellIndices(HashMap<Point, Integer> undoNextCellIndices) {
        mUndoNextCellIndices = new HashMap<>(undoNextCellIndices);
    }
    public void addUndoNextCellIndices(Point point) {
        mUndoNextCellIndices.put(point, mCellValues[point.x][point.y]);
    }

    public HashSet<Point> getLightLine() {
        return mLightLine;
    }

    public void setLightLine(HashSet<Point> lightLine) {
        if (lightLine != null ) {
            mLightLine.clear();
            for (Point point : lightLine) {
                mLightLine.add(new Point(point));
            }
        }
    }

    public boolean check_moreThanFive(int x, int y) {

        Log.d(TAG,"check_moreThanFive.x = " + x + ", y = " + y) ;

        mLightLine.clear();
        mLightLine.add(new Point(x,y));

        int  num_b,first_j,end_j,first_i,end_i ;
        int  i , j ;
        int  firstResult=0,secondResult=0,thirdResult=0,forthResult=0;
        int  cellColor;

        List<Point> tempList = new ArrayList<>();

        cellColor = mCellValues[x][y];
        // Log.d(TAG, "check_moreThanFive() --> cellColor = " + cellColor);

        //first
        first_i = 0;
        end_i   = mRowCounts - 1;
        num_b = 0 ;

        for (i=x-1;i>=first_i;i--) {
            if (mCellValues[i][y] == cellColor) {
                num_b++ ;
                tempList.add(new Point(i,y));
            }
            else {
                i = first_i ;
            }
        }

        if (num_b>=(mBallNumCompleted -1)) {
            // end
            for (Point temp : tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(new Point(temp));
                }
            }
            tempList.clear();
            firstResult = 1;
        }

        for (i=x+1;i<=end_i;i++) {
            if (mCellValues[i][y] == cellColor) {
                num_b++ ;
                tempList.add(new Point(i,y));
            } else {
                i = end_i ;
            }
        }

        if (num_b>=(mBallNumCompleted -1)) {
            // end
            for (Point temp : tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(new Point(temp));
                }
            }
            firstResult = 1;
        }
        tempList.clear();

        //second
        first_i = 0;
        end_i   = mRowCounts - 1;

        first_j = mColCounts - 1;
        end_j   = 0;

        num_b = 0 ;
        for (i=x-1, j=y+1;(i>=first_i)&&(j<=first_j) ;i--,j++) {
            if (mCellValues[i][j] == cellColor) {
                num_b++ ;
                tempList.add(new Point(i,j));
            } else {
                i = first_i ;
                j = first_j ;
            }
        }

        if (num_b>=(mBallNumCompleted -1)) {
            // end
            for (Point temp : tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(new Point(temp));
                }
            }
            tempList.clear();
            secondResult = 1;
        }

        for (i=x+1, j= y-1 ;(i<=end_i)&&(j>=end_j) ; i++,j--) {
            if (mCellValues[i][j] == cellColor) {
                num_b++ ;
                tempList.add(new Point(i,j));
            } else {
                i = end_i ;
                j = end_j ;
            }
        }

        if (num_b>=(mBallNumCompleted -1)) {
            // end
            for (Point temp : tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(new Point(temp));
                }
            }
            secondResult = 1;
        }
        tempList.clear();

        //third
        first_j = mColCounts - 1;
        end_j   = 0;

        num_b = 0 ;
        for (j=y+1;j<=first_j;j++) {
            if (mCellValues[x][j] == cellColor) {
                num_b++ ;
                tempList.add(new Point(x,j));
            } else {
                j = first_j ;
            }
        }

        if (num_b>=(mBallNumCompleted -1)) {
            // end
            for (Point temp : tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(new Point(temp));
                }
            }
            tempList.clear();
            thirdResult = 1;
        }

        for (j=y-1;j>=end_j;j--) {
            if (mCellValues[x][j] == cellColor) {
                num_b++ ;
                tempList.add(new Point(x,j));
            } else {
                j = end_j ;
            }
        }

        if (num_b>=(mBallNumCompleted -1)) {
            // end
            for (Point temp : tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(new Point(temp));
                }
            }
            thirdResult = 1;
        }
        tempList.clear();

        //forth
        first_i = mRowCounts - 1;
        end_i   = 0;

        first_j = mColCounts - 1;
        end_j   = 0;

        num_b = 0 ;
        for (i=x+1, j=y+1 ; (i<=first_i)&&(j<=first_j) ; i++,j++) {
            if (mCellValues[i][j] == cellColor) {
                num_b++ ;
                tempList.add(new Point(i,j));
            } else {
                i = first_i ;
                j = first_j ;
            }
        }

        if (num_b>=(mBallNumCompleted -1)) {
            // end
            for (Point temp : tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(new Point(temp));
                }
            }
            tempList.clear();
            forthResult = 1;
        }

        for (i=x-1, j= y-1 ; (i>=end_i)&&(j>=end_j) ; i--,j--) {
            if (mCellValues[i][j] == cellColor) {
                num_b++ ;
                tempList.add(new Point(i,j));
            } else {
                i = end_i ;
                j = end_j ;
            }
        }

        if (num_b>=(mBallNumCompleted -1)) {
            // end
            for (Point temp : tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(new Point(temp));
                }
            }
            forthResult = 1;
        }
        tempList.clear();

        if ( (firstResult==1) || (secondResult==1) || (thirdResult==1) || (forthResult==1) ) {
            return true;
        } else {
            mLightLine.clear();
            return false;
        }
    }

    public ArrayList<Point> getPathPoint() {
        return mPathPoint;
    }

    public boolean canMoveCellToCell(Point sourcePoint, Point targetPoint) {
        if (mCellValues[sourcePoint.x][sourcePoint.y] == 0) {
            return false;
        }
        if (mCellValues[targetPoint.x][targetPoint.y] != 0) {
            return false;
        }

        mUndoNextCellIndices = new HashMap<>(mNextCellIndices);
        // backup CellValues;
        for (int i = 0; i< mRowCounts; i++) {
            System.arraycopy(mCellValues[i], 0, mBackupCells[i],0, mCellValues[i].length);
        }

        return findPath(sourcePoint,targetPoint);
    }

    private boolean findPath(Point source,Point target) {

        HashSet<Point> traversed = new HashSet<>();

        Stack<Cell> lastCellStack;
        Stack<Cell> cellStack = new Stack<>();
        cellStack.push(new Cell(source,null));

        int shortestPathLength = 0; // the length of the shortest path
        boolean found = false;
        while(!found && (cellStack.size()!=0)) {
            shortestPathLength++;
            Stack<Cell> tempStack = new Stack<>();
            do {
                Cell tempCell = cellStack.pop();
                found = addCellToStack(tempStack, tempCell,  0,  1,target, traversed);
                if (found) break;
                found = addCellToStack(tempStack, tempCell,  0, -1,target, traversed);
                if (found) break;
                found = addCellToStack(tempStack, tempCell,  1,  0,target, traversed);
                if (found) break;
                found = addCellToStack(tempStack, tempCell, -1,  0,target, traversed);
                if (found) break;
            } while (cellStack.size()!=0);

            cellStack = tempStack;
        }
        lastCellStack = cellStack;

        mPathPoint.clear();  // added at 10:43 pm on 2017-10-19
        if (found) {
            Log.d(TAG, "shortestPathLength = " + shortestPathLength);
            Log.d(TAG, "lastCellStack.size() = " + lastCellStack.size());
            Cell c = lastCellStack.pop();
            while (c!=null) {
                mPathPoint.add(new Point(c.getCoordinate()));
                c = c.getParentCell();
            }
            int sizePathPoint = mPathPoint.size();
            Log.d(TAG, "pathPoint.size() = " + sizePathPoint);
            if (sizePathPoint>0) {
                Log.d(TAG, "pathPoint(0) = " + mPathPoint.get(0));
                Log.d(TAG, "pathPoint(pathPoint.size()-1) = " + mPathPoint.get(sizePathPoint - 1));
            } else {
                found = false;
            }
        }

        return found;
    }

    private boolean addCellToStack(Stack<Cell> stack, Cell parent, int dx, int dy, Point target, HashSet<Point> traversed) {
        Point pTemp = new Point(parent.getCoordinate());
        pTemp.set(pTemp.x+dx,pTemp.y+dy);
        if (!traversed.contains(pTemp)) {
            // has not been checked
            if ((pTemp.x >= 0 && pTemp.x < mRowCounts) && (pTemp.y >= 0 && pTemp.y < mColCounts) && (mCellValues[pTemp.x][pTemp.y] == 0)) {
                Cell temp = new Cell(pTemp, parent);
                stack.push(temp);
                traversed.add(pTemp);
            }
        }

        return (pTemp.equals(target));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mBallNumCompleted);
        dest.writeInt(mRowCounts);
        dest.writeInt(mColCounts);

        // dest.writeParcelable(mCellValues, flags); // error
        int numOfArrays = mCellValues.length;
        dest.writeInt(numOfArrays); // save number of arrays
        for (int i = 0; i < numOfArrays; i++) {
            dest.writeIntArray(mCellValues[i]);
        }

        // dest.writeParcelable(mBackupCells, flags); // error
        numOfArrays = mBackupCells.length;
        dest.writeInt(numOfArrays); // save number of arrays
        for (int i = 0; i < numOfArrays; i++) {
            dest.writeIntArray(mBackupCells[i]);
        }

        int sizeOfHashSet = mNextCellIndices.size();
        dest.writeInt(sizeOfHashSet);
        for (HashMap.Entry<Point, Integer> entry : mNextCellIndices.entrySet()) {
            dest.writeParcelable(entry.getKey(), flags);
            dest.writeInt(entry.getValue());
        }
        sizeOfHashSet = mUndoNextCellIndices.size();
        dest.writeInt(sizeOfHashSet);
        for (HashMap.Entry<Point, Integer> entry : mUndoNextCellIndices.entrySet()) {
            dest.writeParcelable(entry.getKey(), flags);
            dest.writeInt(entry.getValue());
        }

        sizeOfHashSet = mLightLine.size();
        dest.writeInt(sizeOfHashSet);
        for (Point point:mLightLine) {
            dest.writeParcelable(point, flags);
        }

        dest.writeTypedList(mPathPoint);
        dest.writeSerializable(mRandom);
        dest.writeInt(mNumOfColorsUsed);
    }

    protected GridData(Parcel in) {
        mBallNumCompleted = in.readInt();
        mRowCounts = in.readInt();
        mColCounts = in.readInt();

        int numOfArrays = in.readInt();
        mCellValues = new int[numOfArrays][];
        for (int i=0; i<numOfArrays; i++) {
            mCellValues[i] = in.createIntArray();
        }

        numOfArrays = in.readInt();
        mBackupCells = new int[numOfArrays][];
        for (int i=0; i<numOfArrays; i++) {
            mBackupCells[i] = in.createIntArray();
        }

        mNextCellIndices = new HashMap<>();
        int sizeOfHashSet = in.readInt();
        Point point;
        for (int i=0; i<sizeOfHashSet; i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                point = in.readParcelable(Point.class.getClassLoader(), Point.class);
            } else point = in.readParcelable(Point.class.getClassLoader());
            int cellColor = in.readInt();
            mNextCellIndices.put(point, cellColor);
        }

        mUndoNextCellIndices = new HashMap<>();
        sizeOfHashSet = in.readInt();
        for (int i=0; i<sizeOfHashSet; i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                point = in.readParcelable(Point.class.getClassLoader(), Point.class);
            } else point = in.readParcelable(Point.class.getClassLoader());
            int cellColor = in.readInt();
            mUndoNextCellIndices.put(point, cellColor);
        }

        mLightLine = new HashSet<>();
        sizeOfHashSet = in.readInt();
        for (int i=0; i<sizeOfHashSet; i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                point = in.readParcelable(Point.class.getClassLoader(), Point.class);
            } else point = in.readParcelable(Point.class.getClassLoader());
            mLightLine.add(point);
        }

        mPathPoint = in.createTypedArrayList(Point.CREATOR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mRandom = in.readSerializable(Random.class.getClassLoader(), Random.class);
        } else mRandom = (Random) in.readSerializable();
        mNumOfColorsUsed = in.readInt();
    }

    public static final Creator<GridData> CREATOR = new Creator<>() {
        @Override
        public GridData createFromParcel(Parcel source) {
            return new GridData(source);
        }

        @Override
        public GridData[] newArray(int size) {
            return new GridData[size];
        }
    };
}

