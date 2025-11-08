package com.smile.colorballs_main.models

import android.graphics.Point
import android.os.Parcelable
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.tools.LogUtil
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Random
import java.util.Stack

@Parcelize
open class GridData(
    open val rowCounts: Int = 0,
    open val colCounts: Int = 0,
    open var mNumOfColorsUsed : Int = Constants.NUM_BALLS_USED_EASY,
    open val mCellValues : Array<IntArray> =
        Array(rowCounts) { IntArray(colCounts) },
    open val mBackupCells : Array<IntArray> =
        Array(rowCounts) { IntArray(colCounts) },
    open val mLightLine : HashSet<Point> = HashSet()): Parcelable {

    companion object {
        private const val TAG: String = "GridData"
    }

    @IgnoredOnParcel
    val mRandom: Random = Random(System.currentTimeMillis())

    open fun initialize() {
        for (i in 0 until rowCounts) {
            for (j in 0 until colCounts) {
                mCellValues[i][j] = 0
                mBackupCells[i][j] = 0
            }
        }
        mLightLine.clear()
    }

    private fun generateColumnBalls(column: Int) {
        LogUtil.i(TAG, "generateColumnBalls.column = $column")
        for (i in 0 until rowCounts) {
            val nn = mRandom.nextInt(mNumOfColorsUsed)
            mCellValues[i][column] = Constants.BallColor[nn]
        }
    }

    fun generateColorBalls() {
        for (j in 0 until colCounts) {
            generateColumnBalls(j)
        }
    }

    private fun needShiftColumn(fillColumn: Boolean) {
        LogUtil.i(TAG, "needShiftColumn.fillColumn = $fillColumn")
        var columnLeft = colCounts
        var j = colCounts - 1
        while (j >= 0 && columnLeft > 0) {
            if (mCellValues[rowCounts-1][j] == 0) {
                LogUtil.d(TAG, "needShiftColumn." +
                        "mCellValues[${rowCounts-1}][$j] = 0")
                // this column is empty then shift column
                for (k in j downTo 1) {
                    LogUtil.d(TAG, "needShiftColumn.k = $k")
                    for (i in 0 until rowCounts) {
                        mCellValues[i][k] = mCellValues[i][k - 1]
                    }
                }
                if (fillColumn) generateColumnBalls(0)
                else {
                    for (row in 0 until rowCounts) {
                        mCellValues[row][0] = 0
                    }
                }
            } else {
                j--
            }
            columnLeft--
        }
    }

    fun setNumOfColorsUsed(numOfColorsUsed: Int) {
        mNumOfColorsUsed = numOfColorsUsed
    }

    fun refreshColorBalls(fillColumn: Boolean) {
        LogUtil.i(TAG, "refreshColorBalls.mLightLine.size = ${mLightLine.size}")
        val list = ArrayList<Point>(mLightLine)
        list.sortWith { p1: Point, p2: Point ->
            p1.x.compareTo(p2.x)
        }
        for (p in list) {
            for (i in p.x downTo 1) {
                mCellValues[i][p.y] = mCellValues[i-1][p.y]
            }
            mCellValues[0][p.y] = 0
        }
        // Check if needs to shift columns
        needShiftColumn(fillColumn)
    }

    fun refreshColorBalls_old(fillColumn: Boolean) {
        LogUtil.i(TAG, "refreshColorBalls.mLightLine.size = ${mLightLine.size}")
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
        LogUtil.d(TAG, "refreshColorBalls.rowMap.size = ${rowMap.size}")
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
        LogUtil.d(TAG, "copy")
        val newGridData = GridData(gData.rowCounts, gData.colCounts)
        newGridData.mNumOfColorsUsed = gData.mNumOfColorsUsed
        for (i in 0 until rowCounts) {
            System.arraycopy(gData.mCellValues[i], 0, newGridData.mCellValues[i],
                0, gData.mCellValues[i].size)
        }
        for (i in 0 until rowCounts) {
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

    open fun undoTheLast() {
        LogUtil.i(TAG, "undoTheLast")
        // restore CellValues;
        for (i in 0 until rowCounts) {
            System.arraycopy(mBackupCells[i], 0, mCellValues[i],
                0, mBackupCells[i].size)
        }
    }

    fun getBackupCells(): Array<IntArray> {
        return mBackupCells
    }

    fun setCellValues(cellValues: Array<IntArray>) {
        for (i in 0 until rowCounts) {
            System.arraycopy(cellValues[i], 0, mCellValues[i],
                0, cellValues[i].size)
        }
    }

    fun setBackupCells(backupCells: Array<IntArray>) {
        for (i in 0 until rowCounts) {
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
        // pTemp[pTemp.x + dx] = pTemp.y + dy
        pTemp.x += dx
        pTemp.y += dy
        if (!traversed.contains(pTemp)) {
            // has not been checked
            if ((pTemp.x in 0..<rowCounts)
                && (pTemp.y in 0..<colCounts)
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
        while (cellStack.isNotEmpty()) {
            val tempStack = Stack<Point>()
            do {
                val currCell = cellStack.pop()
                mLightLine.add(currCell)
                // right
                addCellToStack(tempStack, currCell, 0, 1, traversed)
                // left
                addCellToStack(tempStack, currCell, 0, -1, traversed)
                // down
                addCellToStack(tempStack, currCell, 1, 0, traversed)
                // up
                addCellToStack(tempStack, currCell, -1, 0, traversed)
            } while (cellStack.isNotEmpty())
            cellStack = tempStack
        }
    }

    fun backupCells() {
        LogUtil.i(TAG, "backupCells")
        // backup CellValues;
        for (i in 0 until rowCounts) {
            System.arraycopy(mCellValues[i], 0, mBackupCells[i],
                0, mCellValues[i].size)
        }
    }

    fun checkMoreThanTwo(x: Int, y: Int): Boolean {
        LogUtil.i(TAG, "checkMoreThanTwo.x = $x, y = $y")
        val ballNumCompleted = 2
        allConnectBalls(Point(x , y))
        if (mLightLine.size >= ballNumCompleted) {
            return true
        }
        mLightLine.clear()
        return false
    }

    open fun isGameOver(): Boolean {
        for (i in 0 until rowCounts) {
            for (j in 0 until colCounts) {
                if (mCellValues[i][j] != 0) {
                    if (checkMoreThanTwo(i, j)) return false
                }
            }
        }
        return true
    }
}