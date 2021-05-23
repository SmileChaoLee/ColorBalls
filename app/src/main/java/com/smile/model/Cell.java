package com.smile.model;

import android.graphics.Point;

public class Cell {
    private final Point coordinate = new Point();
    private Cell parentCell;

    Cell(Point coordinate,Cell parentCell) {
        this.coordinate.set(coordinate.x,coordinate.y);
        this.parentCell = parentCell;
    }

    public Point getCoordinate() {
        return this.coordinate;
    }

    public Cell getParentCell() {
        return this.parentCell;
    }
}
