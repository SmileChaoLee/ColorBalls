package com.smile.colorballs.models

import android.graphics.Point
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Random

@Parcelize
class GridDataKt private constructor(
    private val mRowCounts : Int,
    private val mColCounts : Int,
    private val mNumOfColorsUsed : Int,
    private val mCellValues : Array<IntArray>,
    private val mBackupCells : Array<IntArray>,
    private val mNextCellIndices : HashMap<Point, Int>,
    private val mUndoNextCellIndices : HashMap<Point, Int>,
    private val mLightLine : HashSet<Point>,
    private val mPathPoint : ArrayList<Point>,
    private val mRandom: Random? = null,
    private var mBallNumCompleted : Int)  : Parcelable {
        constructor(rowCounts : Int,
                    colCounts : Int,
                    numOfColorsUsed: Int) : this(
            rowCounts, colCounts, numOfColorsUsed,
            Array(rowCounts) { IntArray(colCounts){0} },
            Array(rowCounts) { IntArray(colCounts){0} },
            HashMap(),
            HashMap(),
            HashSet(),
            ArrayList(),
            Random(System.currentTimeMillis()),
            5)

    companion object {
        private const val TAG: String = "GridData"
    }
}