package main.analysis;

import grid.GridGraph;
import grid.ReachableNodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import algorithms.datatypes.Point;

public class MazeAnalysis {
    
    public int sizeX;
    public int sizeY;
    public int nBlocked;
    public float blockDensity;
    //public float averageOpenSpaceSize;
    
    public boolean hasSqueezableCorners;
    public ArrayList<ArrayList<Point>> connectedSets;
    public ArrayList<Point> largestConnectedSet;
    
    public Options options;

    public MazeAnalysis(GridGraph gridGraph) {
        this.sizeX = gridGraph.sizeX;
        this.sizeY = gridGraph.sizeY;
        this.nBlocked = gridGraph.getNumBlocked();
        this.blockDensity = (float)nBlocked / (sizeX*sizeY);
        this.hasSqueezableCorners = checkHasSqueezableCorners(gridGraph);
        this.connectedSets = findConnectedSets(gridGraph);
        this.largestConnectedSet = getLargestSet(connectedSets);
    }
    
    private MazeAnalysis(GridGraph gridGraph, Options o) {
        if (o.sizeX) this.sizeX = gridGraph.sizeX;
        if (o.sizeY) this.sizeY = gridGraph.sizeY;
        if (o.nBlocked) this.nBlocked = gridGraph.getNumBlocked();
        if (o.blockDensity) this.blockDensity = (float)nBlocked / (sizeX*sizeY);
        if (o.hasSqueezableCorners) this.hasSqueezableCorners = checkHasSqueezableCorners(gridGraph);
        if (o.connectedSets) this.connectedSets = findConnectedSets(gridGraph);
        if (o.largestConnectedSet) this.largestConnectedSet = getLargestSet(connectedSets);
        this.options = o;
    }
    
    public static MazeAnalysis options(GridGraph gridGraph, String... options) {
        return new MazeAnalysis(gridGraph, new Options(options));
    }
    
    public static boolean checkHasSqueezableCorners(GridGraph gridGraph) {
        for (int y=1; y<gridGraph.sizeY; y++) {
            for (int x=1; x<gridGraph.sizeX; x++) {
                if (gridGraph.bottomLeftOfBlockedTile(x, y) && gridGraph.topRightOfBlockedTile(x, y) &&
                        !gridGraph.bottomRightOfBlockedTile(x, y) && !gridGraph.topLeftOfBlockedTile(x, y)) {
                    return true;
                }
                if (gridGraph.bottomRightOfBlockedTile(x, y) && gridGraph.topLeftOfBlockedTile(x, y) &&
                        !gridGraph.bottomLeftOfBlockedTile(x, y) && !gridGraph.topRightOfBlockedTile(x, y)) {
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

    public static class Options {
        public boolean sizeX;
        public boolean sizeY;
        public boolean nBlocked;
        public boolean blockDensity;
        public boolean hasSqueezableCorners;
        public boolean connectedSets;
        public boolean largestConnectedSet;
        
        public Options (String...options) {
            for (String option : options) {
                switch(option.toLowerCase()) {
                    case "sizex": sizeX=true;break;
                    case "sizey": sizeY=true;break;
                    case "nblocked": nBlocked=true;break;
                    case "blockdensity": blockDensity=true;break;
                    case "hassqueezablecorners": hasSqueezableCorners=true;break;
                    case "connectedsets": connectedSets=true;break;
                    case "largestconnectedset": largestConnectedSet=true;break;
                }
            }
        }
    }
}