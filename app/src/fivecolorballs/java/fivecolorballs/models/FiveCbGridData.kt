package fivecolorballs.models

import android.graphics.Point
import android.os.Parcelable
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.models.GridData
import com.smile.colorballs_main.tools.LogUtil
import fivecolorballs.constants.FiveBallsConstants
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

    init {
        randNext4Balls()
    }

    fun randNext4Balls() {
        LogUtil.i(TAG, "randNext4Balls")
        next4Balls = ArrayList()
        var bColor: Int
        (0 until FiveBallsConstants.NUM_NEXT_BALLS).forEach { i ->
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