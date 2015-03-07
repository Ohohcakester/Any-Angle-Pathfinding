package main.testgen;

import algorithms.datatypes.Point;

public class StartEndPointData {
    public final Point start;
    public final Point end;
    public final float shortestPath;
    
    public StartEndPointData(Point start, Point end, float shortestPath) {
        this.start = start;
        this.end = end;
        this.shortestPath = shortestPath;
    }
}