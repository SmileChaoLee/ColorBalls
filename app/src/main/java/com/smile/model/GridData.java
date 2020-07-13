package com.smile.model;

/**
 * Created by lee on 9/19/2014.
 */

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.smile.colorballs.ColorBallsApp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class GridData implements Parcelable {
    public static final int ballNumOneTime = 3;
    private static final String TAG = "GridData";

    private int ballNumCompleted = 5;
    private int rowCounts=0, colCounts=0;
    private int cellValues[][];
    private int backupCells[][];
    private int[] nextBalls = null;
    private int[] nextCellIndexI = null;
    private int[] nextCellIndexJ = null;
    private int[] undoNextBalls = null;

    private HashSet<Point> Light_line;
    private List<Point> pathPoint;

    private Random random = null;

    private int numOfTotalBalls = 0;
    private int numOfColorsUsed = 5;    // 5 colors for easy level, 6 colors for difficult level
    private boolean gameOver = false;

    public GridData(int rowCounts , int colCounts, int numOfColorsUsed) {
        this.rowCounts = rowCounts;
        this.colCounts = colCounts;
        this.numOfColorsUsed = numOfColorsUsed;

        nextBalls = new int[ColorBallsApp.NumOfColorsUsedByDifficult];
        nextCellIndexI = new int[ColorBallsApp.NumOfColorsUsedByDifficult];
        nextCellIndexJ = new int[ColorBallsApp.NumOfColorsUsedByDifficult];
        undoNextBalls = new int[ColorBallsApp.NumOfColorsUsedByDifficult];
        cellValues = new int[rowCounts][colCounts];
        backupCells = new int[rowCounts][colCounts];
        Light_line = new HashSet<>();
        pathPoint   = new ArrayList<>();
        for (int i=0 ; i<rowCounts ; i++) {
            for (int j=0 ; j<colCounts ; j++) {
                cellValues[i][j] = 0;
                backupCells[i][j] = 0;
            }
        }
        gameOver = false;   // new Game
        numOfTotalBalls = 0;
        random = new Random(System.currentTimeMillis());
        randColors();
    }

    public void setCellValue(int i,int j , int value) {
        cellValues[i][j] = value;
    }
    public int getCellValue(int i , int j) {
        return this.cellValues[i][j];
    }
    public void setCellValues(int[][] cellValues) {
        for (int i=0; i<rowCounts; i++) {
            this.cellValues[i] = cellValues[i].clone();
        }
    }
    public int[][] getCellValues() {
        return this.cellValues;
    }

    public void randColors() {
        for (int i=0 ; i<ballNumOneTime ; i++) {
            // int nn = random.nextInt(ColorBallsApp.MaxBalls);
            int nn = random.nextInt(numOfColorsUsed);
            nextBalls[i] = ColorBallsApp.ballColor[nn];
        }
    }
    public int[] getNextBalls() {
        return this.nextBalls;
    }
    public void setNextBalls(int[] nextBalls) {
        this.nextBalls = nextBalls.clone();
    }
    public int[] getUndoNextBalls() {
        return this.undoNextBalls;
    }
    public void setUndoNextBalls(int[] undoNextBalls) {
        this.undoNextBalls = undoNextBalls.clone();
    }
    public int[][] getBackupCells() {
        return backupCells;
    }
    public void setBackupCells(int[][] backupCells) {
        for (int i=0 ; i<rowCounts ; i++) {
            this.backupCells[i] = backupCells[i].clone();
        }
    }
    public int getNumOfColorsUsed() {
        return this.numOfColorsUsed;
    }
    public void setNumOfColorsUsed(int numOfColorsUsed) {
        this.numOfColorsUsed = numOfColorsUsed;
    }

    public void randCells() {
        int n1,n2;
        for (int i=0 ; i<ballNumOneTime ; i++) {
            nextCellIndexI[i] = -1;
            nextCellIndexJ[i] = -1;
        }

        for (int i=0 ; i<ballNumOneTime ; i++) {
            n1 = random.nextInt(rowCounts);
            n2 = random.nextInt(colCounts);

            if (cellValues[n1][n2] == 0) {
                nextCellIndexI[i] = n1;
                nextCellIndexJ[i] = n2;
                cellValues[n1][n2] = nextBalls[i];
            } else {
                i--;
            }

            numOfTotalBalls = getTotalBalls();
            if (numOfTotalBalls >= (rowCounts*colCounts)) {
                i = ballNumOneTime;
            }
        }
    }

    public void undoTheLast() {

        nextBalls = undoNextBalls.clone();

        // restore CellValues;
        for (int i=0 ; i<rowCounts ; i++) {
            cellValues[i] = backupCells[i].clone();
        }

        numOfTotalBalls = getTotalBalls();
    }

    public boolean getGameOver() {
        numOfTotalBalls = getTotalBalls();
        if (numOfTotalBalls >= (rowCounts*colCounts)) {
            // Game over
            gameOver = true;
        } else {
            gameOver = false;
        }
        return gameOver;
    }

    public int[] getNextCellIndexI() {
        return this.nextCellIndexI;
    }

    public int[] getNextCellIndexJ()  {
        return this.nextCellIndexJ;
    }

    public HashSet<Point> getLight_line() {
        return this.Light_line;
    }

    public void setLight_line(HashSet<Point> light_line) {
        if (light_line != null ) {
            this.Light_line.clear();
            for (Point point : light_line) {
                this.Light_line.add(point);
            }
        }
    }

    public int check_moreThanFive(int x,int y) {

        Light_line.clear();
        Light_line.add(new Point(x,y));

        int  num_b,first_j,end_j,first_i,end_i ;
        int  i , j ;
        int  firstResult=0,secondResult=0,thirdResult=0,forthResult=0;
        int  cellColor;

        List<Point> tempList = new ArrayList<>();
        // tempList.clear();    // no need, removed on 2019-06-17

        cellColor = cellValues[x][y];

        //first
        first_i = Math.max(x-(rowCounts-1),0);
        end_i   = Math.min(x+(rowCounts-1),(rowCounts-1));
        num_b = 0 ;

        for (i=x-1;i>=first_i;i--) {
            if (cellValues[i][y] == cellColor) {
                num_b++ ;
                tempList.add(new Point(i,y));
            }
            else {
                i = first_i ;
            }
        }

        if (num_b>=(ballNumCompleted-1)) {
            // end
            for (Point temp : tempList) {
                if (!Light_line.contains(temp)) {
                    Light_line.add(temp);
                }
            }
            tempList.clear();
            firstResult = 1;
        }

        for (i=x+1;i<=end_i;i++) {
            if (cellValues[i][y] == cellColor) {
                num_b++ ;
                tempList.add(new Point(i,y));
            }
            else {
                i = end_i ;
            }
        }

        if (num_b>=(ballNumCompleted-1)) {
            // end
            for (Point temp : tempList) {
                if (!Light_line.contains(temp)) {
                    Light_line.add(temp);
                }
            }
            firstResult = 1;
        }
        tempList.clear();

        //second
        first_i = Math.max(x-(rowCounts-1), 0) ;
        end_i   = Math.min(x+(rowCounts-1), rowCounts - 1) ;

        first_j = Math.min(y+(colCounts-1) , colCounts - 1) ;
        end_j   = Math.max(y-(colCounts-1) , 0) ;

        num_b = 0 ;
        for (i=x-1, j=y+1;(i>=first_i)&&(j<=first_j) ;i--,j++) {
            if (cellValues[i][j] == cellColor)
            {
                num_b++ ;
                tempList.add(new Point(i,j));
            }
            else
            {
                i = first_i ;
                j = first_j ;
            }
        }

        if (num_b>=(ballNumCompleted-1)) {
            // end
            for (Point temp : tempList) {
                if (!Light_line.contains(temp)) {
                    Light_line.add(temp);
                }
            }
            tempList.clear();
            secondResult = 1;
        }

        for (i=x+1, j= y-1 ;(i<=end_i)&&(j>=end_j) ; i++,j--) {
            if (cellValues[i][j] == cellColor) {
                num_b++ ;
                tempList.add(new Point(i,j));
            }
            else {
                i = end_i ;
                j = end_j ;
            }
        }

        if (num_b>=(ballNumCompleted-1)) {
            // end
            for (Point temp : tempList) {
                if (!Light_line.contains(temp)) {
                    Light_line.add(temp);
                }
            }
            secondResult = 1;
        }
        tempList.clear();

        //third
        first_j = Math.min(y+(colCounts-1), colCounts-1 ) ;
        end_j   = Math.max(y-(colCounts-1) , 0) ;

        num_b = 0 ;
        for (j=y+1;j<=first_j;j++) {
            if (cellValues[x][j] == cellColor) {
                num_b++ ;
                tempList.add(new Point(x,j));
            }
            else {
                j = first_j ;
            }
        }

        if (num_b>=(ballNumCompleted-1)) {
            // end
            for (Point temp : tempList) {
                if (!Light_line.contains(temp)) {
                    Light_line.add(temp);
                }
            }
            tempList.clear();
            thirdResult = 1;
        }

        for (j=y-1;j>=end_j;j--) {
            if (cellValues[x][j] == cellColor)
            {
                num_b++ ;
                tempList.add(new Point(x,j));
            }
            else
            {
                j = end_j ;
            }
        }

        if (num_b>=(ballNumCompleted-1)) {
            // end
            for (Point temp : tempList) {
                if (!Light_line.contains(temp)) {
                    Light_line.add(temp);
                }
            }
            thirdResult = 1;
        }
        tempList.clear();

        //forth
        first_i = Math.min(x+(rowCounts-1), rowCounts-1) ;
        end_i   = Math.max(x-(rowCounts-1), 0) ;

        first_j = Math.min(y+(colCounts-1) , colCounts - 1) ;
        end_j   = Math.max(y-(colCounts-1) , 0) ;

        num_b = 0 ;
        for (i=x+1, j=y+1 ; (i<=first_i)&&(j<=first_j) ; i++,j++) {
            if (cellValues[i][j] == cellColor) {
                num_b++ ;
                tempList.add(new Point(i,j));
            }
            else {
                i = first_i ;
                j = first_j ;
            }
        }

        if (num_b>=(ballNumCompleted-1)) {
            // end
            for (Point temp : tempList) {
                if (!Light_line.contains(temp)) {
                    Light_line.add(temp);
                }
            }
            tempList.clear();
            forthResult = 1;
        }

        for (i=x-1, j= y-1 ; (i>=end_i)&&(j>=end_j) ; i--,j--) {
            if (cellValues[i][j] == cellColor) {
                num_b++ ;
                tempList.add(new Point(i,j));
            }
            else {
                i = end_i ;
                j = end_j ;
            }
        }

        if (num_b>=(ballNumCompleted-1)) {
            // end
            for (Point temp : tempList) {
                if (!Light_line.contains(temp)) {
                    Light_line.add(temp);
                }
            }
            forthResult = 1;
        }
        tempList.clear();

        if ( (firstResult==1) || (secondResult==1) || (thirdResult==1) || (forthResult==1) ) {
            return 1;
        } else {
            Light_line.clear();
            return 0;
        }
    }

    public List<Point> getPathPoint() {
        return pathPoint;
    }

    public boolean canMoveCellToCell(Point sourcePoint, Point targetPoint) {
        boolean result = false;

        if (cellValues[sourcePoint.x][sourcePoint.y] == 0) {
            return result;
        }
        if (cellValues[targetPoint.x][targetPoint.y] != 0) {
            return result;
        }

        undoNextBalls = nextBalls.clone();
        // backup CellValues;
        for (int i=0 ; i<rowCounts ; i++) {
            backupCells[i] = cellValues[i].clone();
        }

        result = findPath(sourcePoint,targetPoint);

        return result;
    }

    private int getTotalBalls() {
        int nb=0;
        // noColorList.clear();
        for (int i=0 ; i<rowCounts ; i++) {
            for (int j=0 ; j<colCounts; j++) {
                if (cellValues[i][j] != 0) {
                    nb++;
                }
            }
        }
        return nb;
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

        pathPoint.clear();  // added at 10:43 pm on 2017-10-19
        if (found) {
            Log.d(TAG, "shortestPathLength = " + shortestPathLength);
            Log.d(TAG, "lastCellStack.size() = " + lastCellStack.size());
            Cell c = lastCellStack.pop();
            /*
            if (c!=null) {
                for (int i = shortestPathLength-1 ; i>=0; i--) { // ok but no beginning point
                    pathPoint.add(c.getCoordinate());
                    c = c.getParentCell();
                }
            }
            */
            while (c!=null) {
                pathPoint.add(c.getCoordinate());
                c = c.getParentCell();
            }
            int sizePathPoint = pathPoint.size();
            Log.d(TAG, "pathPoint.size() = " + sizePathPoint);
            if (sizePathPoint>0) {
                Log.d(TAG, "pathPoint(0) = " + pathPoint.get(0));
                Log.d(TAG, "pathPoint(pathPoint.size()-1) = " + pathPoint.get(sizePathPoint - 1));
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
            if ((pTemp.x >= 0 && pTemp.x < rowCounts) && (pTemp.y >= 0 && pTemp.y < colCounts) && (cellValues[pTemp.x][pTemp.y] == 0)) {
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
        dest.writeInt(this.ballNumCompleted);
        dest.writeInt(this.rowCounts);
        dest.writeInt(this.colCounts);

        // dest.writeParcelable(this.cellValues, flags); // error
        int numOfArrays = this.cellValues.length;
        dest.writeInt(numOfArrays); // save number of arrays
        for (int i = 0; i < numOfArrays; i++) {
            dest.writeIntArray(this.cellValues[i]);
        }
        //

        // dest.writeParcelable(this.backupCells, flags); // error
        numOfArrays = this.backupCells.length;
        dest.writeInt(numOfArrays); // save number of arrays
        for (int i = 0; i < numOfArrays; i++) {
            dest.writeIntArray(this.backupCells[i]);
        }
        //

        dest.writeIntArray(this.nextBalls);
        dest.writeIntArray(this.nextCellIndexI);
        dest.writeIntArray(this.nextCellIndexJ);
        dest.writeIntArray(this.undoNextBalls);

        // dest.writeSerializable(this.Light_line); // IOException
        int sizeOfHashSet = this.Light_line.size();
        dest.writeInt(sizeOfHashSet);
        for (Point point:this.Light_line) {
            dest.writeParcelable(point, flags);
        }
        //

        dest.writeTypedList(this.pathPoint);
        dest.writeSerializable(this.random);
        dest.writeInt(this.numOfTotalBalls);
        dest.writeInt(this.numOfColorsUsed);
        dest.writeByte(this.gameOver ? (byte) 1 : (byte) 0);
    }

    protected GridData(Parcel in) {
        this.ballNumCompleted = in.readInt();
        this.rowCounts = in.readInt();
        this.colCounts = in.readInt();

        // this.cellValues = in.readParcelable(int[][].class.getClassLoader()); // error
        int numOfArrays = in.readInt();
        this.cellValues = new int[numOfArrays][];
        for (int i=0; i<numOfArrays; i++) {
            this.cellValues[i] = in.createIntArray();
        }
        //

        // this.backupCells = in.readParcelable(int[][].class.getClassLoader());
        numOfArrays = in.readInt();
        this.backupCells = new int[numOfArrays][];
        for (int i=0; i<numOfArrays; i++) {
            this.backupCells[i] = in.createIntArray();
        }
        //

        this.nextBalls = in.createIntArray();
        this.nextCellIndexI = in.createIntArray();
        this.nextCellIndexJ = in.createIntArray();
        this.undoNextBalls = in.createIntArray();

        // this.Light_line = (HashSet<Point>) in.readSerializable();
        this.Light_line = new HashSet<>();
        int sizeOfHashSet = in.readInt();
        for (int i=0; i<sizeOfHashSet; i++) {
            Point point = in.readParcelable(Point.class.getClassLoader());
            this.Light_line.add(point);
        }
        //

        this.pathPoint = in.createTypedArrayList(Point.CREATOR);
        this.random = (Random) in.readSerializable();
        this.numOfTotalBalls = in.readInt();
        this.numOfColorsUsed = in.readInt();
        this.gameOver = in.readByte() != 0;
    }

    public static final Creator<GridData> CREATOR = new Creator<GridData>() {
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

