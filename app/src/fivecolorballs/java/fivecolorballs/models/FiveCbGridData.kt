package fivecolorballs.models

import android.graphics.Point
import android.os.Parcelable
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.models.GridData
import com.smile.colorballs_main.tools.LogUtil
import fivecolorballs.constants.FiveBallsConstants
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class FiveCbGridData(
    override val rowCounts: Int = FiveBallsConstants.ROW_COUNTS,
    override val colCounts: Int = FiveBallsConstants.COLUMN_COUNTS,
    override var mNumOfColorsUsed : Int = Constants.NUM_BALLS_USED_EASY,
    override val mCellValues : Array<IntArray> =
        Array(rowCounts) { IntArray(colCounts) },
    override val mBackupCells: Array<IntArray> =
        Array(rowCounts) { IntArray(colCounts) },
    override val mLightLine : HashSet<Point> = HashSet(),
    var runningBalls : ArrayList<Int> = ArrayList(),
    var next4Balls : ArrayList<Int> = ArrayList(),
    var pathPoint : ArrayList<Point> = ArrayList())
    : GridData(rowCounts, colCounts, mNumOfColorsUsed, mCellValues,
    mBackupCells, mLightLine) ,Parcelable {

    companion object {
        private const val TAG: String = "FiveCbGridData"
    }

    @IgnoredOnParcel
    val addUpLightLine = HashSet<Point>()

    init {
        randNext4Balls()
    }

    fun randNext4Balls() {
        LogUtil.i(TAG, "randNext4Balls")
        next4Balls = ArrayList()
        var bColor: Int
        (0 until FiveBallsConstants.NUM_NEXT_BALLS).forEach { _ ->
            bColor = Constants.BallColor[mRandom.nextInt(mNumOfColorsUsed)]
            next4Balls.add(bColor)
        }
    }

    override fun initialize() {
        super.initialize()
        setNextRunning()
        pathPoint = ArrayList()
    }

    fun setNextRunning() {
        runningBalls = ArrayList(next4Balls)
        randNext4Balls()
    }

    fun moreThan3NABOR(set: HashSet<Point>): Boolean {
        LogUtil.i(TAG, "moreThan3NABOR")
        var result = false
        addUpLightLine.clear()
        for (p in set) {
            if (moreThanNumNABOR(p.x, p.y, 3)) {
                addUpLightLine.addAll(mLightLine)
                result = true
            }
        }
        return result
    }

    fun crashColorBalls(): Boolean {
        LogUtil.i(TAG, "crashColorBalls")
        crashColorBalls(addUpLightLine)

        val colSet = HashSet<Int>()
        for (p in addUpLightLine) {
            colSet.add(p.y)
        }
        val tempSet = HashSet<Point>()
        for (col in colSet) {
            for (row in rowCounts-1 downTo 0) {
                if (mCellValues[row][col] != 0) {
                    tempSet.add(Point(row, col))
                }
            }
        }
        LogUtil.i(TAG, "crashColorBalls.tempSet.size = ${tempSet.size}")
        if (tempSet.isEmpty()) return false
        return moreThan3NABOR(tempSet)
    }

    fun copy(gData: FiveCbGridData): FiveCbGridData {
        LogUtil.i(TAG, "copy")
        val newGridData = FiveCbGridData()
        newGridData.copy(gData)
        newGridData.runningBalls = ArrayList(gData.runningBalls)
        newGridData.next4Balls = ArrayList(gData.next4Balls)
        newGridData.pathPoint = ArrayList(gData.pathPoint)

        return newGridData
    }
}