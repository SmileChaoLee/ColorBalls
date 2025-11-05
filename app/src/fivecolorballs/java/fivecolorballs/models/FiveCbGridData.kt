package fivecolorballs.models

import android.graphics.Point
import android.os.Parcelable
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.constants.WhichBall
import com.smile.colorballs_main.models.ColorBallInfo
import com.smile.colorballs_main.models.GridData
import com.smile.colorballs_main.tools.LogUtil
import fivecolorballs.constants.FiveBallsConstants
import kotlinx.parcelize.Parcelize
import java.util.Random

private val mRandomBall: Random = Random(System.currentTimeMillis()+1000L)

@Parcelize
class FiveCbGridData(
    override val rowCounts: Int = FiveBallsConstants.ROW_COUNTS,
    override val colCounts: Int = FiveBallsConstants.COLUMN_COUNTS,
    override var mNumOfColorsUsed : Int = Constants.NUM_BALLS_USED_EASY,
    override val mCellValues : Array<IntArray> =
        Array(rowCounts) { IntArray(colCounts) },
    override val mLightLine : HashSet<Point> = HashSet(),
    private var mNext4Balls : ArrayList<ColorBallInfo> = ArrayList(),
    val mPathPoint : ArrayList<Point> = ArrayList())
    : GridData(rowCounts, colCounts, mNumOfColorsUsed, mCellValues,
    Array(rowCounts) { IntArray(colCounts) },
    mLightLine) ,Parcelable {

    companion object {
        private const val TAG: String = "FiveCbGridData"
    }

    fun rand4Cells(): ArrayList<ColorBallInfo> {
        LogUtil.i(TAG, "rand4Cells")
        mNext4Balls = ArrayList()
        var bColor: Int
        (0 until FiveBallsConstants.NUM_NEXT_BALLS).forEach { i ->
            bColor = Constants.BallColor[mRandomBall.nextInt(mNumOfColorsUsed)]
            mNext4Balls.add(ColorBallInfo(ballColor = bColor, whichBall = WhichBall.BALL,
                isResize = true))
        }
        return mNext4Balls
    }

    override fun initialize() {
        super.initialize()
        mNext4Balls.clear()
        mPathPoint.clear()
    }

    fun copy(gData: FiveCbGridData): FiveCbGridData {
        LogUtil.i(TAG, "copy")
        val newGridData = FiveCbGridData()
        newGridData.copy(gData)
        newGridData.mNext4Balls = ArrayList(gData.mNext4Balls)
        newGridData.mPathPoint.clear()
        newGridData.mPathPoint.addAll(gData.mPathPoint)

        return newGridData
    }

    fun getNext4Balls(): ArrayList<ColorBallInfo> {
        return mNext4Balls
    }

    fun setNext4Balls(list: ArrayList<ColorBallInfo>) {
        mNext4Balls = ArrayList(list)
    }

    fun setLightLine(lightLine: HashSet<Point>) {
        mLightLine.clear()
        mLightLine.addAll(lightLine)
    }
}