package com.smile.colorballs.models

import android.graphics.Point
import android.os.Parcelable
import android.util.Log
import androidx.compose.ui.unit.Constraints
import com.smile.colorballs.constants.Constants
import kotlinx.parcelize.Parcelize
import java.util.Random
import java.util.Stack
import kotlin.math.min

private val rowCounts = Constants.ROW_COUNTS
private val colCounts = Constants.COLUMN_COUNTS
private val mRandomCell: Random = Random(System.currentTimeMillis())
private val mRandomBall: Random = Random(System.currentTimeMillis()+1000L)

@Parcelize
class GridData(
    private var mNumOfColorsUsed : Int = Constants.NUM_EASY,
    private val mCellValues : Array<IntArray> =
        Array(rowCounts) { IntArray(colCounts) },
    private val mBackupCells : Array<IntArray> =
        Array(rowCounts) { IntArray(colCounts) },
    private var mNextCellIndices : HashMap<Point, Int> = HashMap(),
    private var mUndoNextCellIndices : HashMap<Point, Int> = HashMap(),
    private val mLightLine : HashSet<Point> = HashSet(),
    private val mPathPoint : ArrayList<Point> = ArrayList()) : Parcelable {

    fun randThreeCells(): Int {
        Log.d(TAG, "randThreeCells")
        mNextCellIndices.clear()
        return generateNextCellIndices(0, null)
    }

    fun randomBarriersAndCells() {
        Log.d(TAG, "randomBarriersAndCells")
        // randomly generate barriers in 9x9 grid
        val set: HashSet<Point> = HashSet()
        var nn = 0
        var point: Point
        var loopNum = 0
        while (loopNum < Constants.NUM_BARRIERS) {
            nn = mRandomCell.nextInt(rowCounts * colCounts)
            // nn = row * rowCounts + col
            val row = nn / rowCounts
            val col = nn % rowCounts
            point = Point(row, col)
            if (!set.contains(point)) {
                set.add(Point(row, col))
                mCellValues[row][col] = Constants.COLOR_BARRIER
                loopNum++
            }
        }
        randThreeCells()
    }

    fun randomGrid() {
        Log.d(TAG, "randomGrid")
        // randomly generate some color balls and distribute in the grid
        randThreeCells()
    }

    fun initialize(whichGame: Int) {
        mNumOfColorsUsed = Constants.NUM_EASY
        for (i in 0 until rowCounts) {
            for (j in 0 until rowCounts) {
                mCellValues[i][j] = 0
            }
        }
        for (i in 0 until rowCounts) {
            for (j in 0 until rowCounts) {
                mBackupCells[i][j] = 0
            }
        }
        mNextCellIndices.clear()
        mUndoNextCellIndices.clear()
        mLightLine.clear()
        mPathPoint.clear()
        when(whichGame) {
            0-> {   // no barriers
                randThreeCells()
            }
            1-> {   // has barriers
                randomBarriersAndCells()
            }
            else -> {
                randomGrid()
            }
        }
    }

    fun copy(gData: GridData): GridData {
        Log.d(TAG, "copy")
        val newGridData = GridData()
        newGridData.mNumOfColorsUsed = gData.mNumOfColorsUsed
        for (i in 0 until rowCounts) {
            System.arraycopy(gData.mCellValues[i], 0, newGridData.mCellValues[i],
                0, gData.mCellValues[i].size)
        }
        for (i in 0 until rowCounts) {
            System.arraycopy(gData.mBackupCells[i], 0, newGridData.mBackupCells[i],
                0, gData.mBackupCells[i].size)
        }
        newGridData.mNextCellIndices.clear()
        newGridData.mNextCellIndices.putAll(gData.mNextCellIndices)
        newGridData.mUndoNextCellIndices.clear()
        newGridData.mUndoNextCellIndices.putAll(gData.mUndoNextCellIndices)
        newGridData.mLightLine.clear()
        newGridData.mLightLine.addAll(gData.mLightLine)
        newGridData.mPathPoint.clear()
        newGridData.mPathPoint.addAll(gData.mPathPoint)

        return newGridData
    }

    fun getCellValue(i: Int, j: Int): Int {
        return mCellValues[i][j]
    }

    fun getLightLine(): HashSet<Point> {
        return mLightLine
    }

    fun setNumOfColorsUsed(numOfColorsUsed: Int) {
        mNumOfColorsUsed = numOfColorsUsed
    }

    fun undoTheLast() {
        // mNextCellIndices = HashMap(mUndoNextCellIndices)
        mNextCellIndices.clear()
        mNextCellIndices.putAll(mUndoNextCellIndices)
        // restore CellValues;
        for (i in 0 until rowCounts) {
            System.arraycopy(mBackupCells[i], 0, mCellValues[i],
                0, mBackupCells[i].size)
        }
    }

    fun getNextCellIndices(): HashMap<Point, Int> {
        return mNextCellIndices
    }

    fun getUndoNextCellIndices(): HashMap<Point, Int> {
        return mUndoNextCellIndices
    }

    fun getBackupCells(): Array<IntArray> {
        return mBackupCells
    }

    fun setNextCellIndices(nextCellIndices: HashMap<Point, Int>) {
        // mNextCellIndices = HashMap(nextCellIndices)
        mNextCellIndices.clear()
        mNextCellIndices.putAll(nextCellIndices)
    }

    fun setUndoNextCellIndices(undoNextCellIndices: HashMap<Point, Int>) {
        // mUndoNextCellIndices = HashMap(undoNextCellIndices)
        mUndoNextCellIndices.clear()
        mUndoNextCellIndices.putAll(undoNextCellIndices)
    }

    fun addNextCellIndices(point: Point) {
        mNextCellIndices[point] = mCellValues[point.x][point.y]
    }

    fun addUndoNextCellIndices(point: Point) {
        mUndoNextCellIndices[point] = mCellValues[point.x][point.y]
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

    fun setLightLine(lightLine: HashSet<Point>) {
        mLightLine.clear()
        mLightLine.addAll(lightLine)
        /*
        for (point in lightLine) {
            mLightLine.add(Point(point))
        }
        */
    }

    fun getPathPoint(): ArrayList<Point> {
        return mPathPoint
    }

    private fun generateNextCellIndices(cColor: Int, exclusiveCell: Point?): Int {
        Log.d(TAG, "generateNextCellIndices.cColor = $cColor")
        // find the all vacant cell that are not occupied by color balls
        val vacantCellList = java.util.ArrayList<Point>()
        for (i in 0 until rowCounts) {
            for (j in 0 until colCounts) {
                if (mCellValues[i][j] == 0) {
                    vacantCellList.add(Point(i, j))
                }
            }
        }
        val vacantSize = vacantCellList.size
        Log.d(TAG, "generateNextCellIndices.vacantSize = $vacantSize")
        if (vacantSize == 0) {
            return 0 // no vacant so game over
        }

        val maxLoop = if (cColor != 0) 1 else vacantSize
        var k = 0
        var ballColor: Int
        var point: Point
        val loopNum = min(Constants.BALL_NUM_ONE_TIME.toDouble(), maxLoop.toDouble())
        while (k < loopNum && mNextCellIndices.size < vacantSize) {
            point = vacantCellList[mRandomCell.nextInt(vacantSize)]
            ballColor = Constants.BallColor[mRandomBall.nextInt(mNumOfColorsUsed)]
            if (mNextCellIndices.containsKey(point) || point.equals(exclusiveCell)) continue
            mNextCellIndices[point] = ballColor
            k++
        }

        return vacantSize
    }

    fun regenerateNextCellIndices(point: Point?) {
        Log.d(TAG, "regenerateNextCellIndices")
        if (mNextCellIndices.containsKey(point)) {
            // if nextCellIndices contains the target cell
            // generate only one next cell index
            mNextCellIndices.remove(point)?.let {
                Log.d(TAG, "regenerateNextCellIndices.cellColor = $it")
                generateNextCellIndices(it, point)
            }
        }
    }

    private fun addCellToStack(
        stack: Stack<Cell>,
        parent: Cell,
        dx: Int,
        dy: Int,
        target: Point,
        traversed: java.util.HashSet<Point>
    ): Boolean {
        val pTemp = Point(parent.coordinate)
        pTemp[pTemp.x + dx] = pTemp.y + dy
        if (!traversed.contains(pTemp)) {
            // has not been checked
            if ((pTemp.x in 0..<rowCounts) && (pTemp.y in 0..<colCounts)
                && (mCellValues[pTemp.x][pTemp.y] == 0)) {
                val temp = Cell(pTemp, parent)
                stack.push(temp)
                traversed.add(pTemp)
            }
        }

        return (pTemp == target)
    }

    private fun findPath(source: Point, target: Point): Boolean {
        val traversed = HashSet<Point>()
        var cellStack = Stack<Cell>()
        cellStack.push(Cell(source, null))

        var shortestPathLength = 0 // the length of the shortest path
        var found = false
        while (!found && (cellStack.isNotEmpty())) {
            shortestPathLength++
            val tempStack = Stack<Cell>()
            do {
                val tempCell = cellStack.pop()
                found = addCellToStack(tempStack, tempCell, 0, 1, target, traversed)
                if (found) break
                found = addCellToStack(tempStack, tempCell, 0, -1, target, traversed)
                if (found) break
                found = addCellToStack(tempStack, tempCell, 1, 0, target, traversed)
                if (found) break
                found = addCellToStack(tempStack, tempCell, -1, 0, target, traversed)
                if (found) break
            } while (cellStack.size != 0)

            cellStack = tempStack
        }
        // val lastCellStack = cellStack

        mPathPoint.clear() // added at 10:43 pm on 2017-10-19
        if (found) {
            Log.d(TAG, "shortestPathLength = $shortestPathLength")
            Log.d(TAG, "cellStack.size() = " + cellStack.size)
            var c = cellStack.pop()
            while (c != null) {
                mPathPoint.add(Point(c.coordinate))
                c = c.parentCell
            }
            val sizePathPoint = mPathPoint.size
            Log.d(TAG, "pathPoint.size() = $sizePathPoint")
            if (sizePathPoint > 0) {
                Log.d(TAG, "pathPoint(0) = " + mPathPoint[0])
                Log.d(TAG, "pathPoint(pathPoint.size()-1) = " + mPathPoint[sizePathPoint - 1])
            } else {
                found = false
            }
        }

        return found
    }

    fun canMoveCellToCell(sourcePoint: Point, targetPoint: Point): Boolean {
        if (mCellValues[sourcePoint.x][sourcePoint.y] == 0) {
            return false
        }
        if (mCellValues[targetPoint.x][targetPoint.y] != 0) {
            return false
        }

        // mUndoNextCellIndices = HashMap(mNextCellIndices)
        mUndoNextCellIndices.clear()
        mUndoNextCellIndices.putAll(mNextCellIndices)
        // backup CellValues;
        for (i in 0 until rowCounts) {
            System.arraycopy(mCellValues[i], 0, mBackupCells[i],
                0, mCellValues[i].size)
        }

        return findPath(sourcePoint, targetPoint)
    }

    fun checkMoreThanFive(x: Int, y: Int): Boolean {
        Log.d(TAG, "checkMoreThanFive.x = $x, y = $y")
        return checkMoreThanNumber(x, y, Constants.BALL_NUM_COMPLETED)
    }

    fun checkMoreThanThree(x: Int, y: Int): Boolean {
        Log.d(TAG, "checkMoreThanThere.x = $x, y = $y")
        return checkMoreThanNumber(x, y, Constants.BALL_NUM_COMPLETED-2)
    }

    private fun checkMoreThanNumber(x: Int, y: Int, checkNum: Int): Boolean {
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
        // Log.d(TAG, "check_moreThanFive() --> cellColor = " + cellColor);

        //first
        var first_i = 0
        var end_i = rowCounts - 1
        var num_b = 0

        i = x - 1
        while (i >= first_i) {
            if (mCellValues[i][y] == cellColor) {
                num_b++
                tempList.add(Point(i, y))
            } else {
                i = first_i
            }
            i--
        }

        if (num_b >= (checkNum - 1)) {
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
        while (i <= end_i) {
            if (mCellValues[i][y] == cellColor) {
                num_b++
                tempList.add(Point(i, y))
            } else {
                i = end_i
            }
            i++
        }

        if (num_b >= (checkNum - 1)) {
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
        first_i = 0
        end_i = rowCounts - 1

        var first_j = colCounts - 1
        var end_j = 0

        num_b = 0
        i = x - 1
        j = y + 1
        while ((i >= first_i) && (j <= first_j)) {
            if (mCellValues[i][j] == cellColor) {
                num_b++
                tempList.add(Point(i, j))
            } else {
                i = first_i
                j = first_j
            }
            i--
            j++
        }

        if (num_b >= (checkNum - 1)) {
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
        while ((i <= end_i) && (j >= end_j)) {
            if (mCellValues[i][j] == cellColor) {
                num_b++
                tempList.add(Point(i, j))
            } else {
                i = end_i
                j = end_j
            }
            i++
            j--
        }

        if (num_b >= (checkNum - 1)) {
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
        first_j = colCounts - 1
        end_j = 0

        num_b = 0
        j = y + 1
        while (j <= first_j) {
            if (mCellValues[x][j] == cellColor) {
                num_b++
                tempList.add(Point(x, j))
            } else {
                j = first_j
            }
            j++
        }

        if (num_b >= (checkNum - 1)) {
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
        while (j >= end_j) {
            if (mCellValues[x][j] == cellColor) {
                num_b++
                tempList.add(Point(x, j))
            } else {
                j = end_j
            }
            j--
        }

        if (num_b >= (checkNum - 1)) {
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
        first_i = rowCounts - 1
        end_i = 0

        first_j = colCounts - 1
        end_j = 0

        num_b = 0
        i = x + 1
        j = y + 1
        while ((i <= first_i) && (j <= first_j)) {
            if (mCellValues[i][j] == cellColor) {
                num_b++
                tempList.add(Point(i, j))
            } else {
                i = first_i
                j = first_j
            }
            i++
            j++
        }

        if (num_b >= (checkNum - 1)) {
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
        while ((i >= end_i) && (j >= end_j)) {
            if (mCellValues[i][j] == cellColor) {
                num_b++
                tempList.add(Point(i, j))
            } else {
                i = end_i
                j = end_j
            }
            i--
            j--
        }

        if (num_b >= (checkNum - 1)) {
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

    companion object {
        private const val TAG: String = "GridData"
    }
}