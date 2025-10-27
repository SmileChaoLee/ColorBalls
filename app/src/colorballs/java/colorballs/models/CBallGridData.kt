package colorballs.models

import android.graphics.Point
import android.os.Parcelable
import colorballs.constants.CbConstants
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.constants.WhichGame
import com.smile.colorballs_main.models.Cell
import com.smile.colorballs_main.models.GridData
import com.smile.colorballs_main.tools.LogUtil
import kotlinx.parcelize.Parcelize
import java.util.Random
import java.util.Stack
import kotlin.collections.HashMap
import kotlin.math.min

private val mRandomIndex: Random = Random(System.currentTimeMillis())
private val mRandomBall: Random = Random(System.currentTimeMillis()+1000L)

@Parcelize
class CBallGridData(
    override val rowCounts: Int = CbConstants.ROW_COUNTS,
    override val colCounts: Int = CbConstants.COLUMN_COUNTS,
    override var mNumOfColorsUsed : Int = Constants.NUM_BALLS_USED_EASY,
    override val mCellValues : Array<IntArray> =
        Array(rowCounts) { IntArray(colCounts) },
    override val mBackupCells : Array<IntArray> =
        Array(rowCounts) { IntArray(colCounts) },
    override val mLightLine : HashSet<Point> = HashSet(),
    private var mUndoNextCellIndices : HashMap<Point, Int> = HashMap(),
    private var mNextCellIndices : HashMap<Point, Int> = HashMap(),
    val mPathPoint : ArrayList<Point> = ArrayList())
    : GridData(rowCounts, colCounts, mNumOfColorsUsed
        ,mCellValues, mBackupCells, mLightLine) ,Parcelable {

    fun randThreeCells(): Int {
        LogUtil.i(TAG, "randThreeCells")
        mNextCellIndices.clear()
        return generateNextCellIndices(0, null)
    }

    fun randomBarriersAndCells() {
        LogUtil.i(TAG, "randomBarriersAndCells")
        // randomly generate barriers in 9x9 grid
        val set: HashSet<Point> = HashSet()
        var row: Int
        var col: Int
        var point: Point
        var loopNum = 0
        while (loopNum < CbConstants.NUM_BARRIERS) {
            row = mRandomIndex.nextInt(rowCounts)
            col = mRandomIndex.nextInt(colCounts)
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
        LogUtil.i(TAG, "randomGrid")
        // randomly generate some color balls and distribute in the grid
        randThreeCells()
    }

    fun initialize(whichGame: WhichGame) {
        super.initialize()
        mNextCellIndices.clear()
        mUndoNextCellIndices.clear()
        mPathPoint.clear()
        when(whichGame) {
            WhichGame.NO_BARRIER -> {   // no barriers
                randThreeCells()
            }
            WhichGame.HAS_BARRIER -> {   // has barriers
                randomBarriersAndCells()
            }
            WhichGame.RESOLVE_GRID -> {
                randomGrid()
            }
            WhichGame.REMOVE_BALLS -> { /* do nothing*/ }
        }
    }

    fun copy(gData: CBallGridData): CBallGridData {
        LogUtil.i(TAG, "copy")
        val newGridData = CBallGridData()
        newGridData.copy(gData)
        newGridData.mNextCellIndices.clear()
        newGridData.mNextCellIndices.putAll(gData.mNextCellIndices)
        newGridData.mUndoNextCellIndices.clear()
        newGridData.mUndoNextCellIndices.putAll(gData.mUndoNextCellIndices)
        newGridData.mPathPoint.clear()
        newGridData.mPathPoint.addAll(gData.mPathPoint)

        return newGridData
    }

    override fun undoTheLast() {
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

    fun setNextCellIndices(nextCellIndices: HashMap<Point, Int>) {
        mNextCellIndices.clear()
        mNextCellIndices.putAll(nextCellIndices)
    }

    fun setUndoNextCellIndices(undoNextCellIndices: HashMap<Point, Int>) {
        mUndoNextCellIndices.clear()
        mUndoNextCellIndices.putAll(undoNextCellIndices)
    }

    fun addNextCellIndices(point: Point) {
        mNextCellIndices[point] = mCellValues[point.x][point.y]
    }

    fun addUndoNextCellIndices(point: Point) {
        mUndoNextCellIndices[point] = mCellValues[point.x][point.y]
    }

    fun setLightLine(lightLine: HashSet<Point>) {
        mLightLine.clear()
        mLightLine.addAll(lightLine)
    }

    private fun generateNextCellIndices(cColor: Int, exclusiveCell: Point?): Int {
        LogUtil.d(TAG, "generateNextCellIndices.cColor = $cColor")
        // find the all vacant cell that are not occupied by color balls
        var vacantSize = 0
        for (i in 0 until rowCounts) {
            for (j in 0 until colCounts) {
                if (mCellValues[i][j] == 0) {
                    vacantSize++
                }
            }
        }
        LogUtil.d(TAG, "generateNextCellIndices.vacantSize = $vacantSize")
        if (vacantSize == 0) {
            return 0 // no vacant so game over
        }

        val maxLoop = if (cColor != 0) 1 else vacantSize
        var k = 0
        var row: Int
        var col: Int
        var ballColor: Int
        var point: Point
        val loopNum = min(CbConstants.BALL_NUM_ONE_TIME.toDouble(), maxLoop.toDouble())
        while (k < loopNum && mNextCellIndices.size < vacantSize) {
            row = mRandomIndex.nextInt(rowCounts)
            col = mRandomIndex.nextInt(colCounts)
            if (mCellValues[row][col] == 0) {
                point = Point(row, col)
                ballColor = Constants.BallColor[mRandomBall.nextInt(mNumOfColorsUsed)]
                if (mNextCellIndices.containsKey(point) || point == exclusiveCell) continue
                mNextCellIndices[point] = ballColor
                k++
            }
        }

        return vacantSize
    }

    fun regenerateNextCellIndices(point: Point?) {
        LogUtil.d(TAG, "regenerateNextCellIndices")
        if (mNextCellIndices.containsKey(point)) {
            // if nextCellIndices contains the target cell
            // generate only one next cell index
            mNextCellIndices.remove(point)?.let {
                LogUtil.d(TAG, "regenerateNextCellIndices.cellColor = $it")
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
        // pTemp[pTemp.x + dx] = pTemp.y + dy
        pTemp.x = pTemp.x + dx
        pTemp.y = pTemp.y + dy
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
            } while (cellStack.isNotEmpty())

            cellStack = tempStack
        }
        // val lastCellStack = cellStack

        mPathPoint.clear() // added at 10:43 pm on 2017-10-19
        if (found) {
            LogUtil.d(TAG, "shortestPathLength = $shortestPathLength")
            LogUtil.d(TAG, "cellStack.size() = " + cellStack.size)
            var c = cellStack.pop()
            while (c != null) {
                mPathPoint.add(Point(c.coordinate))
                c = c.parentCell
            }
            val sizePathPoint = mPathPoint.size
            LogUtil.d(TAG, "pathPoint.size() = $sizePathPoint")
            if (sizePathPoint > 0) {
                LogUtil.d(TAG, "pathPoint(0) = " + mPathPoint[0])
                LogUtil.d(TAG, "pathPoint(pathPoint.size()-1) = " + mPathPoint[sizePathPoint - 1])
            } else {
                found = false
            }
        }

        return found
    }

    private fun findPath_1(source: Point, target: Point): Boolean {
        if ((source.x < 0 || source.x >= rowCounts) ||
            (source.y < 0 || source.y >= colCounts)) {
            // excess the range
            return false
        }
        if ((target.x < 0 || target.x >= rowCounts) ||
            (target.y < 0 || target.y >= colCounts)) {
            // excess the range
            return false
        }

        var found = false
        val tempPoint = Point()
        var shortestPathLength = 0
        val traversed = HashSet<Point>()
        var cellStack = Stack<Cell>()
        cellStack.add(Cell(source, null))
        while (!found && cellStack.isNotEmpty()) {
            shortestPathLength++
            val tempStack = Stack<Cell>()
            do {
                val cell = cellStack.pop()
                val coordinate = cell.coordinate
                // up
                tempPoint.x = coordinate.x - 1
                tempPoint.y = coordinate.y
                if (tempPoint.x >= 0 && !traversed.contains(tempPoint) &&
                    mCellValues[tempPoint.x][tempPoint.y] == 0) {
                    // valid cell (empty cell)
                    tempStack.add(Cell(Point(tempPoint), cell))
                    traversed.add(Point(tempPoint))
                    if (tempPoint == target) {
                        found = true
                        break
                    }
                }
                // down
                tempPoint.x = coordinate.x + 1
                tempPoint.y = coordinate.y
                if (tempPoint.x < rowCounts && !traversed.contains(tempPoint) &&
                    mCellValues[tempPoint.x][tempPoint.y] == 0) {
                    tempStack.add(Cell(Point(tempPoint), cell))
                    traversed.add(Point(tempPoint))
                    if (tempPoint == target) {
                        found = true
                        break
                    }
                }
                // right
                tempPoint.x = coordinate.x
                tempPoint.y = coordinate.y + 1
                if (tempPoint.y < colCounts && !traversed.contains(tempPoint) &&
                    mCellValues[tempPoint.x][tempPoint.y] == 0) {
                    tempStack.add(Cell(Point(tempPoint), cell))
                    traversed.add(Point(tempPoint))
                    if (tempPoint == target) {
                        found = true
                        break
                    }
                }
                // left
                tempPoint.x = coordinate.x
                tempPoint.y = coordinate.y - 1
                if (tempPoint.y >= 0 && !traversed.contains(tempPoint) &&
                    mCellValues[tempPoint.x][tempPoint.y] == 0) {
                    tempStack.add(Cell(Point(tempPoint), cell))
                    traversed.add(Point(tempPoint))
                    if (tempPoint == target) {
                        found = true
                        break
                    }
                }
            } while(cellStack.isNotEmpty())
            cellStack = tempStack
        }

        mPathPoint.clear()
        if (found) {
            LogUtil.d(TAG, "findPath_1.shortestPathLength = $shortestPathLength")
            LogUtil.d(TAG, "findPath_1.cellStack.size = ${cellStack.size}")
            var tempCell = cellStack.pop()
            while (tempCell != null) {
                mPathPoint.add(tempCell.coordinate)
                tempCell = tempCell.parentCell
            }
            val sizePathPoint = mPathPoint.size
            LogUtil.d(TAG, "findPath_1.mPathPoint.size() = $sizePathPoint")
            if (sizePathPoint > 0) {
                LogUtil.d(TAG, "findPath_1.mPathPoint(0) = " + mPathPoint[0])
                LogUtil.d(TAG, "findPath_1.mPathPoint(mPathPoint.size()-1) = " + mPathPoint[sizePathPoint - 1])
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
        LogUtil.i(TAG, "checkMoreThanFive.x = $x, y = $y")
        return checkMoreThanNumber(x, y, 5)
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
        // LogUtil.d(TAG, "check_moreThanFive() --> cellColor = " + cellColor);

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