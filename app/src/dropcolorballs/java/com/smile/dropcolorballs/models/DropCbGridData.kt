package com.smile.dropcolorballs.models

import android.graphics.Point
import android.os.Parcelable
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.models.GridData
import com.smile.colorballs_main.tools.LogUtil
import com.smile.dropcolorballs.constants.DropBallsConstants
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class DropCbGridData(
    override val rowCounts: Int = DropBallsConstants.ROW_COUNTS,
    override val colCounts: Int = DropBallsConstants.COLUMN_COUNTS,
    override var mNumOfColorsUsed : Int = Constants.NUM_BALLS_USED_DIFF,
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
        private const val TAG: String = "DropCbGridData"
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
        (0 until DropBallsConstants.NUM_NEXT_BALLS).forEach { _ ->
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

    fun moreThan3VerHorDia(set: HashSet<Point>): Boolean {
        LogUtil.i(TAG, "moreThan3VerHorDia")
        var result = false
        addUpLightLine.clear()
        for (p in set) {
            if (moreThanNumVerHorDia(p.x, p.y, 3)) {
                addUpLightLine.addAll(mLightLine)
                result = true
            }
        }
        return result
    }

    fun moreThanNum(gameLevel: Int, theSet: HashSet<Point>) = when(gameLevel) {
        Constants.GAME_LEVEL_1 -> moreThan3NABOR(theSet)
        Constants.GAME_LEVEL_2 -> moreThan3VerHorDia(theSet)
        Constants.GAME_LEVEL_3 -> moreThan3VerHorDia(theSet)
        Constants.GAME_LEVEL_4 -> moreThan3VerHorDia(theSet)
        Constants.GAME_LEVEL_5 -> moreThan3NABOR(theSet)
        else -> moreThan3NABOR(theSet)
    }

    fun canCrashAgain(gameLevel: Int) = when(gameLevel) {
        Constants.GAME_LEVEL_1 -> crashColorBallsNABOR()
        Constants.GAME_LEVEL_2 -> crashColorBallsVerHorDia()
        Constants.GAME_LEVEL_3 -> crashColorBallsVerHorDia()
        Constants.GAME_LEVEL_4 -> crashColorBallsVerHorDia()
        Constants.GAME_LEVEL_5 -> crashColorBallsVerHorDia()
        else -> crashColorBallsNABOR()
    }

    fun crashColorBallsNABOR(): Boolean {
        LogUtil.i(TAG, "crashColorBallsNABOR")
        val tempSet = crashColorBalls()
        return moreThan3NABOR(tempSet)
    }

    fun crashColorBallsVerHorDia(): Boolean {
        LogUtil.i(TAG, "crashColorBallsVerHorDia")
        val tempSet = crashColorBalls()
        return moreThan3VerHorDia(tempSet)
    }

    private fun crashColorBalls(): HashSet<Point> {
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
        return tempSet
    }

    fun copy(gData: DropCbGridData): DropCbGridData {
        LogUtil.i(TAG, "copy")
        val newGridData = DropCbGridData()
        newGridData.copy(gData)
        newGridData.runningBalls = ArrayList(gData.runningBalls)
        newGridData.next4Balls = ArrayList(gData.next4Balls)
        newGridData.pathPoint = ArrayList(gData.pathPoint)

        return newGridData
    }
}