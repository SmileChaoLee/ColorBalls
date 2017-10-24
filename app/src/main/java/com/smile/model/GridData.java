package com.smile.model;

/**
 * Created by lee on 9/19/2014.
 */

import android.graphics.Color;
import android.graphics.Point;
import android.text.format.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class GridData {
    private int rowCounts=0,colCounts=0;
    private int cellValue[][];
    private int backupCell[][];
    private int maxBalls = 5;
    private int maxBallsOneTime = 3;
    private int minBallsOneTime = 3;
    private int ballNumOneTime = 3;
    private int undoNumOneTime = ballNumOneTime;
    private int numColors = 5;
    private int[] ballColor = new int[] {Color.RED,Color.GREEN,Color.BLUE,Color.MAGENTA,Color.YELLOW};
    private int[] nextBalls = null;
    private int[] nextCellIndexI = null;
    private int[] nextCellIndexJ = null;

    private int[] undoBalls = null;

    private int ballNumCompleted = 5;

    private List<Point> Light_line;
    private List<Point> noColorList;
    private List<Point> pathPoint;

    private Random random = null;

    private Point startCell = new Point(0,0);  // startCell.x ---> no. of row , startCell.y ---> no. of column
    private Point endCell  = new Point(0,0);
    private Point lastFoundCell = new Point(0,0);

    private int numOfTotalBalls = 0;
    private boolean gameOver = false;

    // cellValue[i][j] = 0 ------> empty
    // cellValue[i][j] = 1 ------> for  RED ball
    // cellValue[i][j] = 2 ------> for GREEN ball
    // cellValue[i][j] = 3 ------> for BLUE ball
    // cellValue[i][j] = 4 ------> for MAGENTA ball
    // cellValue[i][j] = 5 ------> for YELLOW ball

    public GridData() {
        setRowCounts(9);
        setColCounts(9);
        setMinBallsOneTime(3);
        setMaxBallsOneTime(3);
        initGridData();
    }

    public GridData(int rowCounts , int colCounts , int minBallsOneTime , int maxBallsOneTime) {
        setRowCounts(rowCounts);
        setColCounts(colCounts);
        setMinBallsOneTime(minBallsOneTime);
        setMaxBallsOneTime(maxBallsOneTime);
        initGridData();
    }

    public void setRowCounts(int rowCounts) {
        this.rowCounts = rowCounts;
    }

    public void setColCounts(int colCounts) {
        this.colCounts = colCounts;
    }

    public void setMinBallsOneTime(int minBallsOneTime) {
        this.minBallsOneTime = minBallsOneTime;
    }

    public void setMaxBallsOneTime(int maxBallsOneTime) {
        this.maxBallsOneTime = maxBallsOneTime;
    }

    public void setCellValue(int i,int j , int value) {
        cellValue[i][j] = value;
    }

    public int getCellValue(int i , int j) {
        return this.cellValue[i][j];
    }

    public void initGridData() {

        nextBalls = new int[maxBalls];
        nextCellIndexI = new int[maxBalls];
        nextCellIndexJ = new int[maxBalls];

        undoBalls = new int[maxBalls];

        cellValue = new int[rowCounts][colCounts];
        backupCell = new int[rowCounts][colCounts];
        Light_line = new ArrayList<Point>();
        noColorList = new ArrayList<Point>();
        pathPoint   = new ArrayList<Point>();

        for (int i=0 ; i<rowCounts ; i++) {
            for (int j=0 ; j<colCounts ; j++) {
                cellValue[i][j] = 0;
                backupCell[i][j] = 0;
            }
        }

        gameOver = false;   // new Game
        numOfTotalBalls = 0;

        Time now = new Time();
        now.setToNow();
        random = new Random(now.toMillis(false));

        randColors();
        randCells();
    }

    public void randColors() {

        ballNumOneTime = random.nextInt(maxBallsOneTime-minBallsOneTime+1) + minBallsOneTime;

        for (int i=0 ; i<ballNumOneTime ; i++) {
            int nn = random.nextInt(numColors);
            nextBalls[i] = ballColor[nn];
        }
    }

    public void backupNextBalls() {
        undoNumOneTime = ballNumOneTime;
        for (int i=0 ; i<ballNumOneTime ; i++) {
            undoBalls[i] = nextBalls[i];
        }
    }

    public int[] getNextBalls() {
        return nextBalls;
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

            if (cellValue[n1][n2] == 0) {
                nextCellIndexI[i] = n1;
                nextCellIndexJ[i] = n2;
                cellValue[n1][n2] = nextBalls[i];
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
        for (int i=0 ; i<ballNumOneTime ; i++) {
            nextBalls[i] = undoBalls[i];
        }

        restoreCellValue();

        numOfTotalBalls = getTotalBalls();
    }

    public int getTotalBalls() {
        int nb=0;
        noColorList.clear();
        for (int i=0 ; i<rowCounts ; i++) {
            for (int j=0 ; j<colCounts; j++) {
                if (cellValue[i][j] != 0) {
                    nb++;
                } else {
                    noColorList.add(new Point(i,j));
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

    public List<Point> getLight_line() {
        return this.Light_line;
    }

    public int getBallNumOneTime() {
        return this.ballNumOneTime;
    }

    public int check_moreFive(int x,int y) {

        Light_line.clear();
        Light_line.add(new Point(x,y));

        int  num_b,first_j,end_j,first_i,end_i ;
        int  i , j ;
        int  firstResult=0,secondResult=0,thirdResult=0,forthResult=0;
        int  cellColor;

        List<Point> tempList = new ArrayList<Point>();
        tempList.clear();
        // tempList.add(new Point(x,y));

        cellColor = cellValue[x][y];

        //first
        first_i = Math.max(x-(rowCounts-1),0);
        end_i   = Math.min(x+(rowCounts-1),(rowCounts-1));
        num_b = 0 ;

        for (i=x-1;i>=first_i;i--) {
            if (cellValue[i][y] == (cellColor)) {
                num_b++ ;
                // Light_line.add(new Point(i,y));
                tempList.add(new Point(i,y));
            }
            else {
                i = first_i ;
            }
        }

        if (num_b>=(ballNumCompleted-1)) {
            // end
            for (Point temp : tempList) {
                Light_line.add(temp);
            }
            tempList.clear();
            firstResult = 1;
        }

        for (i=x+1;i<=end_i;i++) {
            if (cellValue[i][y] == cellColor) {
                num_b++ ;
                // Light_line.add(new Point(i,y));
                tempList.add(new Point(i,y));
            }
            else {
                i = end_i ;
            }
        }

        if (num_b>=(ballNumCompleted-1)) {
            // end
            for (Point temp : tempList) {
                Light_line.add(temp);
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
            if (cellValue[i][j] == cellColor)
            {
                num_b++ ;
                // Light_line.add(new Point(i,j));
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
                Light_line.add(temp);
            }
            tempList.clear();
            secondResult = 1;
        }

        for (i=x+1, j= y-1 ;(i<=end_i)&&(j>=end_j) ; i++,j--) {
            if (cellValue[i][j] == cellColor) {
                num_b++ ;
                // Light_line.add(new Point(i,j));
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
                Light_line.add(temp);
            }
            secondResult = 1;
        }
        tempList.clear();

        //third
        first_j = Math.min(y+(colCounts-1), colCounts-1 ) ;
        end_j   = Math.max(y-(colCounts-1) , 0) ;

        num_b = 0 ;
        for (j=y+1;j<=first_j;j++) {
            if (cellValue[x][j] == cellColor) {
                num_b++ ;
                // Light_line.add(new Point(x,j));
                tempList.add(new Point(x,j));
            }
            else {
                j = first_j ;
            }
        }

        if (num_b>=(ballNumCompleted-1)) {
            // end
            for (Point temp : tempList) {
                Light_line.add(temp);
            }
            tempList.clear();
            thirdResult = 1;
        }

        for (j=y-1;j>=end_j;j--) {
            if (cellValue[x][j] == cellColor)
            {
                num_b++ ;
                // Light_line.add(new Point(x,j));
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
                Light_line.add(temp);
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
            if (cellValue[i][j] == cellColor) {
                num_b++ ;
                // Light_line.add(new Point(i,j));
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
                Light_line.add(temp);
            }
            tempList.clear();
            forthResult = 1;
        }

        for (i=x-1, j= y-1 ; (i>=end_i)&&(j>=end_j) ; i--,j--) {
            if (cellValue[i][j] == cellColor) {
                num_b++ ;
                // Light_line.add(new Point(i,j));
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
                Light_line.add(temp);
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

    public void backupCellValue() {
        for (int i=0 ; i<rowCounts ; i++) {
            for (int j=0 ; j<colCounts ; j++) {
                backupCell[i][j] = cellValue[i][j];
            }
        }
    }

    public void restoreCellValue() {
        for (int i=0 ; i<rowCounts ; i++) {
            for (int j=0 ; j<colCounts ; j++) {
                cellValue[i][j] = backupCell[i][j];
            }
        }
    }

    private void getNoColorList() {
        noColorList.clear();
        for (int i=0 ; i<rowCounts ; i++) {
            for (int j=0 ; j<colCounts ; j++) {
                if (cellValue[i][j] == 0) {
                    noColorList.add(new Point(i,j));
                }
            }
        }
    }

    private void setStartCell(Point startCell) {
        this.startCell = startCell;
    }

    private void setEndCell(Point endCell) {
        this.endCell = endCell;
    }

    public boolean moveCellToCell(Point sourcePoint, Point targetPoint) {
        boolean result = false;

        if (cellValue[sourcePoint.x][sourcePoint.y] == 0) {
            return result;
        }
        if (cellValue[targetPoint.x][targetPoint.y] != 0) {
            return result;
        }

        backupNextBalls();
        backupCellValue();
        getNoColorList();

        // pathPoint.clear();   // removed at 11:43 pm on 2017-10-19

        result = findPath(sourcePoint,targetPoint);

        for (Point temp : noColorList) {
            cellValue[temp.x][temp.y] = 0;
        }

        return result;
    }

    private boolean findPath(Point source,Point target) {

        boolean found = false;

        Vector<Vector> vPath  = new Vector();
        Vector<CellOfMatrix> parent  = new Vector();
        parent.addElement(new CellOfMatrix(source,null));
        vPath.addElement(parent);

        CellOfMatrix cell = new CellOfMatrix(new Point(0,0),null);

        while(!found && (parent.size()!=0)) {
            Vector<CellOfMatrix> vTemp = new Vector();
            for(int i=0; i<parent.size(); i++) {
                cell = (CellOfMatrix)parent.elementAt(i);
                found = addCell(vTemp, cell,  0,  1,target);
                if (found) break;
                found = addCell(vTemp, cell,  0, -1,target);
                if (found) break;
                found = addCell(vTemp, cell,  1,  0,target);
                if (found) break;
                found = addCell(vTemp, cell, -1,  0,target);
                if (found) break;
            }

            vPath.addElement(vTemp);

            // parent = new Vector<CellOfMatrix>(vTemp);
            parent = vTemp;

            // System.out.println("vp size = "+vp.size());
            // System.out.println("parent size = " + parent.size());
        }

        pathPoint.clear();  // added at 10:43 pm on 2017-10-19
        if (found) {
            int vLength = vPath.size();
            Vector v = (Vector)vPath.elementAt(vLength-1);
            CellOfMatrix c = (CellOfMatrix)((v.elementAt(v.size()-1)));
            if (c!=null) {
                for (int i = vLength-1 ; i>=0; i--) {
                    pathPoint.add(c.getCoordinate());
                    c = c.getParentCell();
                }
            } else {
                // System.out.println("CellOfMatrix c is null");
            }

        }

        return found;
    }

    /*
	precond:  level number
	postcond: new constructed level and updated matrix
    */

    private boolean addCell(Vector vector, CellOfMatrix parent, int dx, int dy,Point target) {
        Point pTemp = new Point(parent.getCoordinate());
        pTemp.set(pTemp.x+dx,pTemp.y+dy);

        if ( (pTemp.x>=0&&pTemp.x<rowCounts)&&(pTemp.y>=0&&pTemp.y<colCounts) && (cellValue[pTemp.x][pTemp.y]==0) ) {

            // System.out.println("pTemp.x and rowCounts = "+pTemp.x+"  "+rowCounts);
            // System.out.println("pTemp.y and colCounts = "+pTemp.y+"  "+colCounts);
            // System.out.println("cellValue = "+cellValue[pTemp.x][pTemp.y]);

            CellOfMatrix temp = new CellOfMatrix(pTemp,parent);
            vector.addElement(temp);
            cellValue[pTemp.x][pTemp.y] = -1;
        }
        return (pTemp.equals(target));
    }
}


class CellOfMatrix {
    private Point coordinate = new Point(0,0);
    private CellOfMatrix parentCell = null;

    CellOfMatrix(Point coordinate,CellOfMatrix parentCell) {
        this.coordinate.set(coordinate.x,coordinate.y);
        this.parentCell = parentCell;
    }

    public Point getCoordinate() {
        return this.coordinate;
    }

    public CellOfMatrix getParentCell() {
        return this.parentCell;
    }
}

