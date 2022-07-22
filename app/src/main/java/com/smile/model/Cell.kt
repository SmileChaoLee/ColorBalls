package com.smile.model

import android.graphics.Point

class Cell internal constructor(coordinate: Point, parentCell: Cell?) {
    val coordinate = Point()
    val parentCell: Cell?

    init {
        this.coordinate[coordinate.x] = coordinate.y
        this.parentCell = parentCell
    }
}