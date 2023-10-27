package com.smile.colorballs.model

import android.graphics.Point
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Cell internal constructor(val coordinate: Point, val parentCell: Cell?) : Parcelable