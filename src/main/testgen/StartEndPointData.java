package main.testgen;

import algorithms.datatypes.Point;

public class StartEndPointData {
    public final Point start;
    public final Point end;
    public final double shortestPath;
    
    public StartEndPointData(Point start, Point end, double shortestPath) {
        this.start = start;
        this.end = end;
        this.shortestPath = shortestPath;
    }
}