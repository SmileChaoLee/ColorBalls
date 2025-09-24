package com.smile.colorballs.ballsremover.models

import android.graphics.Point
import android.os.Parcelable
import android.util.Log
import com.smile.colorballs.ballsremover.constants.BallsRemoverConstants
import kotlinx.parcelize.Parcelize
import java.util.Random
import java.util.Stack

private val mRandom: Random = Random(System.currentTimeMillis())

@Parcelize
class GridData(
    private var mNumOfColorsUsed : Int = BallsRemoverConstants.NUM_BALLS_USED_EASY,
    private val mCellValues : Array<IntArray> =
        Array(BallsRemoverConstants.ROW_COUNTS) { IntArray(BallsRemoverConstants.COLUMN_COUNTS){0} },
    private val mBackupCells : Array<IntArray> =
        Array(BallsRemoverConstants.ROW_COUNTS) { IntArray(BallsRemoverConstants.COLUMN_COUNTS){0} },
    private val mLightLine : HashSet<Point> = HashSet()) : Parcelable {

    fun initialize() {
        for (i in 0 until BallsRemoverConstants.ROW_COUNTS) {
            for (j in 0 until BallsRemoverConstants.COLUMN_COUNTS) {
                mCellValues[i][j] = 0
                mBackupCells[i][j] = 0
            }
        }
        mLightLine.clear()
    }

    private fun generateColumnBalls(column: Int) {
        Log.d(TAG, "generateColumnBalls.column = $column")
        for (i in 0 until BallsRemoverConstants.ROW_COUNTS) {
            val nn = mRandom.nextInt(mNumOfColorsUsed)
            mCellValues[i][column] = BallsRemoverConstants.BallColor[nn]
        }
    }

    fun generateColorBalls() {
        for (j in 0 until BallsRemoverConstants.COLUMN_COUNTS) {
            generateColumnBalls(j)
        }
    }

    private fun needShiftColumn(fillColumn: Boolean) {
        Log.d(TAG, "needShiftColumn.fillColumn = $fillColumn")
        var columnLeft = BallsRemoverConstants.COLUMN_COUNTS
        var j = BallsRemoverConstants.COLUMN_COUNTS - 1
        while (j >= 0 && columnLeft > 0) {
            if (mCellValues[BallsRemoverConstants.ROW_COUNTS-1][j] == 0) {
                Log.d(TAG, "needShiftColumn." +
                        "mCellValues[${BallsRemoverConstants.ROW_COUNTS-1}][$j] = 0")
                // this column is empty then shift column
                for (k in j downTo 1) {
                    Log.d(TAG, "needShiftColumn.k = $k")
                    for (i in 0 until BallsRemoverConstants.ROW_COUNTS) {
                        mCellValues[i][k] = mCellValues[i][k - 1]
                    }
                }
                if (fillColumn) generateColumnBalls(0)
                else {
                    for (row in 0 until BallsRemoverConstants.ROW_COUNTS) {
                        mCellValues[row][0] = 0
                    }
                }
            } else {
                j--
            }
            columnLeft--
        }
    }

    fun setNumBallsUsed(num: Int) {
        mNumOfColorsUsed = num
    }

    fun refreshColorBalls(fillColumn: Boolean) {
        Log.d(TAG, "refreshColorBalls.mLightLine.size = ${mLightLine.size}")
        val list = ArrayList<Point>(mLightLine)
        list.sortWith { p1: Point, p2: Point ->
            p1.x.compareTo(p2.x)
        }
        val rowMap = HashMap<Int, HashSet<Int>>()
        var columnSet = HashSet<Int>()
        list.forEach { point ->
            if (!rowMap.containsKey(point.x)) {
                columnSet = HashSet()
            }
            columnSet.add(point.y)
            rowMap[point.x] = columnSet
        }
        Log.d(TAG, "refreshColorBalls.rowMap.size = ${rowMap.size}")
        rowMap.forEach { (key, set) ->
            set.forEach { column ->
                for (i in key downTo 1) {
                    mCellValues[i][column] = mCellValues[i-1][column]
                }
                mCellValues[0][column] = 0
            }
        }
        // Check if needs to shift columns
        needShiftColumn(fillColumn)
    }

    fun copy(gData: GridData): GridData {
        Log.d(TAG, "copy")
        val newGridData = GridData()
        for (i in 0 until BallsRemoverConstants.ROW_COUNTS) {
            System.arraycopy(gData.mCellValues[i], 0, newGridData.mCellValues[i],
                0, gData.mCellValues[i].size)
        }
        for (i in 0 until BallsRemoverConstants.ROW_COUNTS) {
            System.arraycopy(gData.mBackupCells[i], 0, newGridData.mBackupCells[i],
                0, gData.mBackupCells[i].size)
        }
        newGridData.mLightLine.clear()
        newGridData.mLightLine.addAll(gData.mLightLine)

        return newGridData
    }

    fun getCellValue(i: Int, j: Int): Int {
        return mCellValues[i][j]
    }

    fun getLightLine(): HashSet<Point> {
        return mLightLine
    }

    fun undoTheLast() {
        Log.d(TAG, "undoTheLast")
        // restore CellValues;
        for (i in 0 until BallsRemoverConstants.ROW_COUNTS) {
            System.arraycopy(mBackupCells[i], 0, mCellValues[i],
                0, mBackupCells[i].size)
        }
    }

    fun getBackupCells(): Array<IntArray> {
        return mBackupCells
    }

    fun setCellValues(cellValues: Array<IntArray>) {
        for (i in 0 until BallsRemoverConstants.ROW_COUNTS) {
            System.arraycopy(cellValues[i], 0, mCellValues[i],
                0, cellValues[i].size)
        }
    }

    fun setBackupCells(backupCells: Array<IntArray>) {
        for (i in 0 until BallsRemoverConstants.ROW_COUNTS) {
            System.arraycopy(backupCells[i], 0, mBackupCells[i],
                0, backupCells[i].size)
        }
    }

    fun setCellValue(i: Int, j: Int, value: Int) {
        mCellValues[i][j] = value
    }

    private fun addCellToStack(
        stack: Stack<Point>,
        current: Point,
        dx: Int,
        dy: Int,
        traversed: java.util.HashSet<Point>
    ) {
        val pTemp = Point(current)
        pTemp[pTemp.x + dx] = pTemp.y + dy
        if (!traversed.contains(pTemp)) {
            // has not been checked
            if ((pTemp.x in 0..<BallsRemoverConstants.ROW_COUNTS)
                && (pTemp.y in 0..<BallsRemoverConstants.COLUMN_COUNTS)
                && (mCellValues[pTemp.x][pTemp.y]
                        == mCellValues[current.x][current.y])) {
                stack.push(pTemp)
                traversed.add(pTemp)
            }
        }
    }

    private fun allConnectBalls(source: Point) {
        mLightLine.clear()
        val traversed = HashSet<Point>()
        var cellStack = Stack<Point>()
        cellStack.push(source)
        while (cellStack.size != 0) {
            val tempStack = Stack<Point>()
            while (cellStack.size != 0) {
                val currCell = cellStack.pop()
                mLightLine.add(currCell)
                addCellToStack(tempStack, currCell, 0, 1, traversed)
                addCellToStack(tempStack, currCell, 0, -1, traversed)
                addCellToStack(tempStack, currCell, 1, 0, traversed)
                addCellToStack(tempStack, currCell, -1, 0, traversed)
            }
            cellStack = tempStack
        }
    }

    fun backupCells() {
        Log.d(TAG, "backupCells")
        // backup CellValues;
        for (i in 0 until BallsRemoverConstants.ROW_COUNTS) {
            System.arraycopy(mCellValues[i], 0, mBackupCells[i],
                0, mCellValues[i].size)
        }
    }

    fun checkMoreThanTwo(x: Int, y: Int): Boolean {
        Log.d(TAG, "checkMoreThanTwo.x = $x, y = $y")
        allConnectBalls(Point(x , y))
        if (mLightLine.size >= BallsRemoverConstants.BALL_NUM_COMPLETED) {
            return true
        }
        mLightLine.clear()
        return false
    }

    fun isGameOver(): Boolean {
        for (i in 0 until BallsRemoverConstants.ROW_COUNTS) {
            for (j in 0 until BallsRemoverConstants.COLUMN_COUNTS) {
                if (mCellValues[i][j] != 0) {
                    if (checkMoreThanTwo(i, j)) return false
                }
            }
        }
        return true
    }

    companion object {
        private const val TAG: String = "GridData"
    }
}