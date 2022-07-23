package com.smile.model

import android.graphics.Point
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Cell internal constructor(val coordinate: Point, var color: Int, val parentCell: Cell?) : Parcelable