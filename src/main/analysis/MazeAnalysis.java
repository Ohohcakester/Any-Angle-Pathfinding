package main.analysis;

import grid.GridGraph;
import grid.ReachableNodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import algorithms.datatypes.Point;

public class MazeAnalysis {
    
    public final int sizeX;
    public final int sizeY;
    public final int nBlocked;
    public final float blockDensity;
    //public float averageOpenSpaceSize;
    
    public final boolean hasSqueezableCorners;
    public final ArrayList<ArrayList<Point>> connectedSets;
    public final ArrayList<Point> largestConnectedSet;
    
    public MazeAnalysis(GridGraph gridGraph) {
        this.sizeX = gridGraph.sizeX;
        this.sizeY = gridGraph.sizeY;
        this.nBlocked = gridGraph.getNumBlocked();
        this.blockDensity = (float)nBlocked / (sizeX*sizeY);
        this.hasSqueezableCorners = checkHasSqueezableCorners(gridGraph);
        this.connectedSets = findConnectedSets(gridGraph);
        this.largestConnectedSet = getLargestSet(connectedSets);
        
    }
    
    public static boolean checkHasSqueezableCorners(GridGraph gridGraph) {
        for (int y=1; y<gridGraph.sizeY; y++) {
            for (int x=1; x<gridGraph.sizeX; x++) {
                if (gridGraph.bottomLeftOfBlockedTile(x, y) && gridGraph.topRightOfBlockedTile(x, y)) {
                    return true;
                }
                if (gridGraph.bottomRightOfBlockedTile(x, y) && gridGraph.topLeftOfBlockedTile(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static ArrayList<Point> getLargestSet(ArrayList<ArrayList<Point>> sets) {
        ArrayList<Point> largestSet = null;
        int largestSize = 0;
        
        for (ArrayList<Point> set : sets) {
            if (set.size() > largestSize) {
                largestSet = set;
                largestSize = set.size();
            }
        }
        
        return largestSet;
    }
    
    public static ArrayList<ArrayList<Point>> findConnectedSets(GridGraph gridGraph) {
        HashSet<Point> hashSet = new HashSet<>();
        ArrayList<ArrayList<Point>> connectedSets = new ArrayList<>();
        
        for (int y=0; y<=gridGraph.sizeY; y++) {
            for (int x=0; x<=gridGraph.sizeX; x++) {
                Point point = new Point(x, y);
                if (!hashSet.contains(point)) {
                    ArrayList<Point> list;
                    list = ReachableNodes.computeReachable(gridGraph, x, y);
                    hashSet.addAll(list);
                    if (list.size() > 1) {
                        connectedSets.add(list);
                    }
                }
            }
        }
        // sort in descending order.
        Collections.sort(connectedSets, (set1, set2) -> set2.size() - set1.size());
        return connectedSets;
    }
}