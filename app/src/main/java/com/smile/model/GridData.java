package com.smile.model;

/**
 * Created by lee on 9/19/2014.
 */

import android.graphics.Color;
import android.graphics.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class GridData {
    // 10->RED, 20->GREEN, 30->BLUE, 40->MAGENTA, 50->YELLOW
    public static final int ColorRED = 10;
    public static final int ColorGREEN = 20;
    public static final int ColorBLUE = 30;
    public static final int ColorMAGENTA = 40;
    public static final int ColorYELLOW = 50;
    public static final int MaxBalls = 5;
    private final int[] ballColor = new int[] {ColorRED, ColorGREEN, ColorBLUE, ColorMAGENTA, ColorYELLOW};
    private int rowCounts=0,colCounts=0;
    private int cellValues[][];
    private int backupCells[][];
    private int maxBallsOneTime = 3;
    private int minBallsOneTime = 3;
    private int ballNumOneTime = 3;
    private int undoNumOneTime = ballNumOneTime;
    private int[] nextBalls = null;
    private int[] nextCellIndexI = null;
    private int[] nextCellIndexJ = null;

    private int[] undoNextBalls = null;

    private final int ballNumCompleted = 5;

    private HashSet<Point> Light_line;
    private List<Point> pathPoint;

    private Random random = null;

    private int numOfTotalBalls = 0;
    private boolean gameOver = false;

    public GridData(int rowCounts , int colCounts , int minBallsOneTime , int maxBallsOneTime) {
        setRowCounts(rowCounts);
        setColCounts(colCounts);
        setMinBallsOneTime(minBallsOneTime);
        setMaxBallsOneTime(maxBallsOneTime);
        initGridData();
    }

    private void setRowCounts(int rowCounts) {
        this.rowCounts = rowCounts;
    }

    private void setColCounts(int colCounts) {
        this.colCounts = colCounts;
    }

    public void setMinBallsOneTime(int minBallsOneTime) {
        this.minBallsOneTime = minBallsOneTime;
    }

    public void setMaxBallsOneTime(int maxBallsOneTime) {
        this.maxBallsOneTime = maxBallsOneTime;
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

    private void initGridData() {

        nextBalls = new int[MaxBalls];
        nextCellIndexI = new int[MaxBalls];
        nextCellIndexJ = new int[MaxBalls];

        undoNextBalls = new int[MaxBalls];

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

    public void randColors() {

        ballNumOneTime = random.nextInt(maxBallsOneTime-minBallsOneTime+1) + minBallsOneTime;

        for (int i=0 ; i<ballNumOneTime ; i++) {
            int nn = random.nextInt(MaxBalls);
            nextBalls[i] = ballColor[nn];
        }
    }

    private void backupNextBalls() {
        undoNumOneTime = ballNumOneTime;
        undoNextBalls = nextBalls.clone();
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

        ballNumOneTime = undoNumOneTime;

        nextBalls = undoNextBalls.clone();

        restoreCellValues();

        numOfTotalBalls = getTotalBalls();
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

    public int getBallNumOneTime() {
        return this.ballNumOneTime;
    }
    public void setBallNumOneTime(int ballNumOneTime) {
        this.ballNumOneTime = ballNumOneTime;
    }
    public int getUndoNumOneTime() {
        return this.undoNumOneTime;
    }
    public void setUndoNumOneTime(int undoNumOneTime) {
        this.undoNumOneTime = undoNumOneTime;
    }

    public int check_moreFive(int x,int y) {

        Light_line.clear();
        Light_line.add(new Point(x,y));

        int  num_b,first_j,end_j,first_i,end_i ;
        int  i , j ;
        int  firstResult=0,secondResult=0,thirdResult=0,forthResult=0;
        int  cellColor;

        List<Point> tempList = new ArrayList<>();
        tempList.clear();

        cellColor = cellValues[x][y];

        //first
        first_i = Math.max(x-(rowCounts-1),0);
        end_i   = Math.min(x+(rowCounts-1),(rowCounts-1));
        num_b = 0 ;

        for (i=x-1;i>=first_i;i--) {
            if (cellValues[i][y] == (cellColor)) {
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

    private void backupCellValues() {

        for (int i=0 ; i<rowCounts ; i++) {
            backupCells[i] = cellValues[i].clone();
        }
    }

    private void restoreCellValues() {
        for (int i=0 ; i<rowCounts ; i++) {
            cellValues[i] = backupCells[i].clone();
        }
    }

    public boolean moveCellToCell(Point sourcePoint, Point targetPoint) {
        boolean result = false;

        if (cellValues[sourcePoint.x][sourcePoint.y] == 0) {
            return result;
        }
        if (cellValues[targetPoint.x][targetPoint.y] != 0) {
            return result;
        }

        backupNextBalls();
        backupCellValues();

        result = findPath(sourcePoint,targetPoint);

        return result;
    }

    private boolean findPath(Point source,Point target) {

        boolean found = false;

        HashSet<Point> traversed = new HashSet<>();

        Vector<Vector<Cell>> vPath  = new Vector<>();
        Vector<Cell> cellVector0  = new Vector<>();
        cellVector0.addElement(new Cell(source,null));
        vPath.addElement(cellVector0);

        while(!found && (cellVector0.size()!=0)) {
            Vector<Cell> vTemp = new Vector<>();
            for(int i=0; i<cellVector0.size(); i++) {
                Cell cell = cellVector0.elementAt(i);
                found = addCell(vTemp, cell,  0,  1,target, traversed);
                if (found) break;
                found = addCell(vTemp, cell,  0, -1,target, traversed);
                if (found) break;
                found = addCell(vTemp, cell,  1,  0,target, traversed);
                if (found) break;
                found = addCell(vTemp, cell, -1,  0,target, traversed);
                if (found) break;
            }

            vPath.addElement(vTemp);
            cellVector0 = vTemp;
        }

        pathPoint.clear();  // added at 10:43 pm on 2017-10-19
        if (found) {
            int vLength = vPath.size();
            Vector<Cell> v =  vPath.elementAt(vLength-1);
            Cell c = v.elementAt(v.size()-1);
            if (c!=null) {
                for (int i = vLength-1 ; i>=0; i--) {
                    pathPoint.add(c.getCoordinate());
                    c = c.getParentCell();
                }
            }
        }

        return found;
    }

    private boolean addCell(Vector<Cell> vector, Cell parent, int dx, int dy, Point target, HashSet<Point> traversed) {
        Point pTemp = new Point(parent.getCoordinate());
        pTemp.set(pTemp.x+dx,pTemp.y+dy);

        if (!traversed.contains(pTemp)) {
            // has not been checked
            if ((pTemp.x >= 0 && pTemp.x < rowCounts) && (pTemp.y >= 0 && pTemp.y < colCounts) && (cellValues[pTemp.x][pTemp.y] == 0)) {
                Cell temp = new Cell(pTemp, parent);
                vector.addElement(temp);
                traversed.add(pTemp);
            }
        }

        return (pTemp.equals(target));
    }
}


class Cell {
    private final Point coordinate = new Point();
    private Cell parentCell;

    Cell(Point coordinate,Cell parentCell) {
        this.coordinate.set(coordinate.x,coordinate.y);
        this.parentCell = parentCell;
    }

    public Point getCoordinate() {
        return this.coordinate;
    }

    public Cell getParentCell() {
        return this.parentCell;
    }
}

