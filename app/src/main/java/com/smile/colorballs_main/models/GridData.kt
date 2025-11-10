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

    fun crashColorBalls(sourceSet: HashSet<Point>) {
        LogUtil.i(TAG, "crashColorBalls.sourceSet.size = ${sourceSet.size}")
        val list = ArrayList<Point>(sourceSet)
        list.sortWith { p1: Point, p2: Point ->
            p1.x.compareTo(p2.x)
        }
        for (p in list) {
            for (i in p.x downTo 1) {
                mCellValues[i][p.y] = mCellValues[i-1][p.y]
            }
            mCellValues[0][p.y] = 0
        }
    }

    fun refreshColorBalls(fillColumn: Boolean) {
        LogUtil.i(TAG, "refreshColorBalls.mLightLine.size = ${mLightLine.size}")
        crashColorBalls(mLightLine)
        // Check if needs to shift columns
        needShiftColumn(fillColumn)
    }

    fun refreshColorBallsOld(fillColumn: Boolean) {
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

    fun moreThanNumNABOR(x: Int, y: Int, connectNum: Int): Boolean {
        LogUtil.i(TAG, "moreThanNumNABOR.x = $x, y = $y, connectNum = $connectNum")
        allConnectBalls(Point(x , y))
        if (mLightLine.size >= connectNum) {
            return true
        }
        mLightLine.clear()
        return false
    }

    fun moreThan2NABOR(x: Int, y: Int): Boolean {
        return moreThanNumNABOR(x, y, 2)
    }

    open fun isGameOver(): Boolean {
        for (i in 0 until rowCounts) {
            for (j in 0 until colCounts) {
                if (mCellValues[i][j] != 0) {
                    if (moreThan2NABOR(i, j)) return false
                }
            }
        }
        return true
    }

    fun moreThanNumVerHorDia(x: Int, y: Int, checkNum: Int): Boolean {
        mLightLine.clear()
        mLightLine.add(Point(x, y))
        var i: Int
        var j: Int
        var firstResult = 0
        var secondResult = 0
        var thirdResult = 0
        var forthResult = 0

        val tempList: MutableList<Point> = ArrayList()
        val cellColor = mCellValues[x][y]
        // LogUtil.d(TAG, "check_moreThanFive() --> cellColor = " + cellColor);

        //first
        var firstI = 0
        var endI = rowCounts - 1
        var numB = 0

        i = x - 1
        while (i >= firstI) {
            if (mCellValues[i][y] == cellColor) {
                numB++
                tempList.add(Point(i, y))
            } else {
                i = firstI
            }
            i--
        }

        if (numB >= (checkNum - 1)) {
            // end
            for (temp in tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(Point(temp))
                }
            }
            tempList.clear()
            firstResult = 1
        }

        i = x + 1
        while (i <= endI) {
            if (mCellValues[i][y] == cellColor) {
                numB++
                tempList.add(Point(i, y))
            } else {
                i = endI
            }
            i++
        }

        if (numB >= (checkNum - 1)) {
            // end
            for (temp in tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(Point(temp))
                }
            }
            firstResult = 1
        }
        tempList.clear()

        //second
        firstI = 0
        endI = rowCounts - 1

        var firstJ = colCounts - 1
        var endJ = 0

        numB = 0
        i = x - 1
        j = y + 1
        while ((i >= firstI) && (j <= firstJ)) {
            if (mCellValues[i][j] == cellColor) {
                numB++
                tempList.add(Point(i, j))
            } else {
                i = firstI
                j = firstJ
            }
            i--
            j++
        }

        if (numB >= (checkNum - 1)) {
            // end
            for (temp in tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(Point(temp))
                }
            }
            tempList.clear()
            secondResult = 1
        }

        i = x + 1
        j = y - 1
        while ((i <= endI) && (j >= endJ)) {
            if (mCellValues[i][j] == cellColor) {
                numB++
                tempList.add(Point(i, j))
            } else {
                i = endI
                j = endJ
            }
            i++
            j--
        }

        if (numB >= (checkNum - 1)) {
            // end
            for (temp in tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(Point(temp))
                }
            }
            secondResult = 1
        }
        tempList.clear()

        //third
        firstJ = colCounts - 1
        endJ = 0

        numB = 0
        j = y + 1
        while (j <= firstJ) {
            if (mCellValues[x][j] == cellColor) {
                numB++
                tempList.add(Point(x, j))
            } else {
                j = firstJ
            }
            j++
        }

        if (numB >= (checkNum - 1)) {
            // end
            for (temp in tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(Point(temp))
                }
            }
            tempList.clear()
            thirdResult = 1
        }

        j = y - 1
        while (j >= endJ) {
            if (mCellValues[x][j] == cellColor) {
                numB++
                tempList.add(Point(x, j))
            } else {
                j = endJ
            }
            j--
        }

        if (numB >= (checkNum - 1)) {
            // end
            for (temp in tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(Point(temp))
                }
            }
            thirdResult = 1
        }
        tempList.clear()

        //forth
        firstI = rowCounts - 1
        endI = 0

        firstJ = colCounts - 1
        endJ = 0

        numB = 0
        i = x + 1
        j = y + 1
        while ((i <= firstI) && (j <= firstJ)) {
            if (mCellValues[i][j] == cellColor) {
                numB++
                tempList.add(Point(i, j))
            } else {
                i = firstI
                j = firstJ
            }
            i++
            j++
        }

        if (numB >= (checkNum - 1)) {
            // end
            for (temp in tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(Point(temp))
                }
            }
            tempList.clear()
            forthResult = 1
        }

        i = x - 1
        j = y - 1
        while ((i >= endI) && (j >= endJ)) {
            if (mCellValues[i][j] == cellColor) {
                numB++
                tempList.add(Point(i, j))
            } else {
                i = endI
                j = endJ
            }
            i--
            j--
        }

        if (numB >= (checkNum - 1)) {
            // end
            for (temp in tempList) {
                if (!mLightLine.contains(temp)) {
                    mLightLine.add(Point(temp))
                }
            }
            forthResult = 1
        }
        tempList.clear()

        if ((firstResult == 1) || (secondResult == 1) || (thirdResult == 1) || (forthResult == 1)) {
            return true
        } else {
            mLightLine.clear()
            return false
        }
    }
}