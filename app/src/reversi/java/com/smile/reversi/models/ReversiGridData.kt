package com.smile.reversi.models

import android.graphics.Point
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.models.GridData
import com.smile.reversi.constants.ReversiConstants
import kotlinx.parcelize.Parcelize

@Parcelize
class ReversiGridData : GridData(ReversiConstants.ROW_COUNTS, ReversiConstants.COLUMN_COUNTS) {

    companion object {
        private const val TAG = "ReversiGridData"
        const val COMPUTER_PLAYER = Constants.COLOR_BLUE
        const val HUMAN_PLAYER = Constants.COLOR_RED
    }

    override fun initialize() {
        super.initialize()
        // starting position: (3,3)=blue(white), (3,4)=red(black), (4,3)=red, (4,4)=blue
        val r = HUMAN_PLAYER
        val b = COMPUTER_PLAYER
        mCellValues[3][3] = b
        mCellValues[3][4] = r
        mCellValues[4][3] = r
        mCellValues[4][4] = b
    }

    private fun opponent(player: Int) = if (player == HUMAN_PLAYER) COMPUTER_PLAYER else HUMAN_PLAYER

    // returns list of points to flip for move at (x,y) for color; empty if invalid
    fun flipsForMove(x: Int, y: Int, player: Int): List<Point> {
        val flips = ArrayList<Point>()
        if (mCellValues[x][y] != 0) return flips
        val dirs = arrayOf(
            intArrayOf(1, 0), intArrayOf(-1, 0), intArrayOf(0, 1), intArrayOf(0, -1),
            intArrayOf(1, 1), intArrayOf(1, -1), intArrayOf(-1, 1), intArrayOf(-1, -1)
        )
        val opp = opponent(player)
        for (d in dirs) {
            var i = x + d[0]
            var j = y + d[1]
            val temp = ArrayList<Point>()
            var found = false
            while (i in 0 until rowCounts && j in 0 until colCounts) {
                val v = mCellValues[i][j]
                if (v == opp) {
                    temp.add(Point(i, j))
                } else if (v == player) {
                    if (temp.isNotEmpty()) found = true
                    break
                } else {
                    break
                }
                i += d[0]
                j += d[1]
            }
            if (found) flips.addAll(temp)
        }
        return flips
    }

    fun getValidMoves(player: Int): List<Point> {
        val list = ArrayList<Point>()
        for (i in 0 until rowCounts) for (j in 0 until colCounts) {
            if (mCellValues[i][j] == 0) {
                if (flipsForMove(i, j, player).isNotEmpty()) list.add(Point(i, j))
            }
        }
        return list
    }

    fun placePiece(x: Int, y: Int, color: Int): Boolean {
        val flips = flipsForMove(x, y, color)
        if (flips.isEmpty()) return false
        mCellValues[x][y] = color
        for (p in flips) mCellValues[p.x][p.y] = color
        return true
    }

    fun countPlayer(player: Int): Int {
        var c = 0
        for (i in 0 until rowCounts) for (j in 0 until colCounts) if (mCellValues[i][j] == player) c++
        return c
    }

    override fun isGameOver(): Boolean {
        val humanMoves = getValidMoves(HUMAN_PLAYER)
        val computerMoves = getValidMoves(COMPUTER_PLAYER)
        return humanMoves.isEmpty() && computerMoves.isEmpty()
    }
}