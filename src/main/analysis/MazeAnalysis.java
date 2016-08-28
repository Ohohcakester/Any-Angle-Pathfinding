package main.analysis;

import java.util.ArrayList;
import java.util.Collections;

import algorithms.datatypes.Point;
import grid.BlockedIslandSearch;
import grid.GridGraph;
import grid.ReachableNodes;
import grid.ReachableNodesFast;

public class MazeAnalysis {
    
    public int sizeX;
    public int sizeY;
    public int nBlocked;
    public float blockDensity;
    public float averageOpenSpaceSize;
    public float averageBlockedIslandSize;
    public float averageFloatingBlockedIslandSize;
    public float largestRatioToSecond;
    public float largestRatioToRemaining;
    
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
        this.connectedSets = findConnectedSetsFast(gridGraph);
        this.largestConnectedSet = getLargestSet(connectedSets);
        this.largestRatioToSecond = getLargestComponentRatioToSecondLargest(connectedSets);
        this.largestRatioToRemaining = getLargestComponentRatioToRemaining(connectedSets);
        this.averageOpenSpaceSize = computeAverageMaxSquare(gridGraph);
        this.averageBlockedIslandSize = computeAverageBlockedIslandSize(gridGraph, true);
        this.averageFloatingBlockedIslandSize = computeAverageBlockedIslandSize(gridGraph, false);
    }

    public MazeAnalysis(GridGraph gridGraph, Options o) {
        
        if (o.sizeX) this.sizeX = gridGraph.sizeX;
        if (o.sizeY) this.sizeY = gridGraph.sizeY;
        if (o.nBlocked||o.blockDensity) this.nBlocked = gridGraph.getNumBlocked();
        if (o.blockDensity) this.blockDensity = (float)nBlocked / (sizeX*sizeY);
        if (o.hasSqueezableCorners) this.hasSqueezableCorners = checkHasSqueezableCorners(gridGraph);
        if (o.connectedSets||o.largestConnectedSet||o.largestRatioToSecond||o.largestRatioToRemaining) this.connectedSets = findConnectedSetsFast(gridGraph);
        if (o.largestConnectedSet) this.largestConnectedSet = getLargestSet(connectedSets);
        if (o.largestRatioToSecond) this.largestRatioToSecond = getLargestComponentRatioToSecondLargest(connectedSets);
        if (o.largestRatioToRemaining) this.largestRatioToRemaining = getLargestComponentRatioToRemaining(connectedSets);
        if (o.averageOpenSpaceSize) this.averageOpenSpaceSize = computeAverageMaxSquare(gridGraph);
        if (o.averageBlockedIslandSize) this.averageBlockedIslandSize = computeAverageBlockedIslandSize(gridGraph, true);
        if (o.averageFloatingBlockedIslandSize) this.averageFloatingBlockedIslandSize = computeAverageBlockedIslandSize(gridGraph, false);
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
    
    public static float getLargestComponentRatioToSecondLargest(ArrayList<ArrayList<Point>> sets) {
        int largestSize = 0;
        int secondLargestSize = 0;
        
        for (ArrayList<Point> set : sets) {
            int size = set.size();
            if (size > secondLargestSize) {
                if (size > largestSize) {
                    secondLargestSize = largestSize;
                    largestSize = size;
                } else {
                    secondLargestSize = size;
                }
            }
        }
        
        return (float)largestSize / secondLargestSize;
    }

    
    public static float getLargestComponentRatioToRemaining(ArrayList<ArrayList<Point>> sets) {
        int totalSize = 0;
        int largestSize = 0;
        
        for (ArrayList<Point> set : sets) {
            int size = set.size();
            if (size > largestSize) {
                largestSize = size;
            }
            totalSize += size;
        }
        
        return (float)largestSize / (totalSize-largestSize);
    }

    public static ArrayList<ArrayList<Point>> findConnectedSets(GridGraph gridGraph) {
        //HashSet<Point> hashSet = new HashSet<>();
        boolean[] visited = new boolean[(gridGraph.sizeX+1)*(gridGraph.sizeY+1)];
        ArrayList<ArrayList<Point>> connectedSets = new ArrayList<>();
        
        for (int y=0; y<=gridGraph.sizeY; y++) {
            for (int x=0; x<=gridGraph.sizeX; x++) {
                if (visited[gridGraph.toOneDimIndex(x, y)]) continue;
                ArrayList<Point> list;
                list = ReachableNodes.computeReachable(gridGraph, x, y, visited);
                if (list.size() > 1) {
                    connectedSets.add(list);
                }
            }
        }
        // sort in descending order.
        Collections.sort(connectedSets, (set1, set2) -> set2.size() - set1.size());
        return connectedSets;
    }
    
    public static ArrayList<ArrayList<Point>> findConnectedSetsFast(GridGraph gridGraph) {
        //HashSet<Point> hashSet = new HashSet<>();
        boolean[] visited = new boolean[(gridGraph.sizeX+1)*(gridGraph.sizeY+1)];
        ArrayList<ArrayList<Point>> connectedSets = new ArrayList<>();
        
        ReachableNodesFast reachable = new ReachableNodesFast(gridGraph);
        for (int y=0; y<=gridGraph.sizeY; y++) {
            for (int x=0; x<=gridGraph.sizeX; x++) {
                if (visited[gridGraph.toOneDimIndex(x, y)]) continue;
                ArrayList<Point> list;
                list = reachable.computeReachable(visited, x, y);
                if (list.size() > 1) {
                    connectedSets.add(list);
                }
            }
        }
        // sort in descending order.
        Collections.sort(connectedSets, (set1, set2) -> set2.size() - set1.size());
        return connectedSets;
    }
    
    public static float computeAverageMaxSquare(GridGraph gridGraph) {
        long total = 0;
        long count = 0;
        
        int[][] maxRanges = gridGraph.computeMaxDownLeftRanges();
        for (int y=0; y<gridGraph.sizeY; ++y) {
            for (int x=0; x<gridGraph.sizeX; ++x) {
                if (gridGraph.isUnblockedCoordinate(x, y)) {
                    count += 1;
                    int maxSquare = detectMaxSquare(maxRanges, gridGraph.sizeY, x, y);
                    total += maxSquare;
                }
            }
        }
        if (count == 0) {
            return 0;
        }
        return (float)((double)total/count);
    }

    private static float computeAverageBlockedIslandSize(GridGraph gridGraph, boolean acceptBorderIslands) {

        boolean[] visited = new boolean[gridGraph.sizeX*gridGraph.sizeY];
        int nIslands = 0;
        int totalIslandSizes = 0;
        
        BlockedIslandSearch reachable = new BlockedIslandSearch(gridGraph);
        int sizeX = gridGraph.sizeX;
        int sizeY = gridGraph.sizeY;
        for (int y=0; y<sizeY; y++) {
            for (int x=0; x<sizeX; x++) {
                if (visited[y*sizeX + x]) continue;
                int islandSize = reachable.computeIslandSize(visited, x, y, acceptBorderIslands);
                
                if (islandSize == 0) continue;
                totalIslandSizes += islandSize;
                nIslands++;
            }
        }

        if (nIslands == 0) return 0;
        return (float)totalIslandSizes / nIslands;
    }

    /**
     * <pre>
     * returns the size of the max square at (x,y). can possibly return 0.
     * Method copied from AcceleratedAStar.
     * 1: XX
     *    XX
     * 
     * 2: XXX
     *    XXX
     *    XXX
     * </pre>
     */
    private static int detectMaxSquare(int[][] maxRanges, int sizeY, int x, int y) {
        // This is the newer, O(n) method.
        int lower = 0;
        int upper = Integer.MAX_VALUE;
        int newUpper;
        int i = x-y+sizeY;
        int j = Math.min(x, y);
        if (upper <= lower) return 0;

        while (true) {
            newUpper = checkUpperBoundNew(maxRanges, i,j,lower);
            if (newUpper < upper) upper = newUpper;
            if (upper <= lower) break;

            newUpper = checkUpperBoundNew(maxRanges, i,j,-1-lower);
            if (newUpper < upper) upper = newUpper;
            if (upper <= lower) break;
            
            lower++;
            if (upper <= lower) break;
        }
        return lower;
    }

    /**
     * <pre>
     *          _______  This function returns the upper bound detected by
     *         |   |k=1| the a leftward and downward search.
     *         |___|___| k is the number of steps moved in the up-right direction.
     *         |k=0|   | k = 0 the the square directly top-right of grid point (x,y).
     *  _______.___|___|
     * |   |-1 |(x,y)    
     * |___|___|  point of concern
     * |-2 |   |
     * |___|___|
     * </pre>
     */
    private static int checkUpperBoundNew(int[][] maxRange, int i, int j, int k) {
        return maxRange[i][j + k] - k;
    }
    
    public void addParameter(StringBuilder sb, String name, Object value){
        sb.append(name + ": " + value.toString()).append("\n");
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        addParameter(sb, "sizeX", sizeX);
        addParameter(sb, "sizeY", sizeY);
        addParameter(sb, "nTiles", sizeX*sizeY);
        addParameter(sb, "nBlocked", nBlocked);
        addParameter(sb, "blockDensity", blockDensity);
        addParameter(sb, "averageOpenSpaceSize", averageOpenSpaceSize);
        addParameter(sb, "averageBlockedIslandSize", averageBlockedIslandSize);
        addParameter(sb, "averageFloatingBlockedIslandSize", averageFloatingBlockedIslandSize);
        addParameter(sb, "largestRatioToSecond", largestRatioToSecond);
        addParameter(sb, "largestRatioToRemaining", largestRatioToRemaining);
        addParameter(sb, "hasSqueezableCorners", hasSqueezableCorners);
        addParameter(sb, "largestConnectedSetSize", largestConnectedSet.size());
        addParameter(sb, "numConnectedSets", connectedSets.size());
        addParameter(sb, "connectedSetSizes", computeConnectedSetSizeList());
        return sb.toString();
    }

    public static class Options {
        public boolean sizeX;
        public boolean sizeY;
        public boolean nBlocked;
        public boolean blockDensity;
        public boolean averageOpenSpaceSize;
        public boolean hasSqueezableCorners;
        public boolean connectedSets;
        public boolean largestConnectedSet;
        public boolean averageBlockedIslandSize;
        public boolean averageFloatingBlockedIslandSize;
        public boolean largestRatioToSecond;
        public boolean largestRatioToRemaining;
        
        public Options (String...options) {
            for (String option : options) {
                switch(option.toLowerCase()) {
                    case "sizex": sizeX=true;break;
                    case "sizey": sizeY=true;break;
                    case "nblocked": nBlocked=true;break;
                    case "blockdensity": blockDensity=true;break;
                    case "averageopenspacesize": averageOpenSpaceSize=true;break;
                    case "hassqueezablecorners": hasSqueezableCorners=true;break;
                    case "connectedsets": connectedSets=true;break;
                    case "largestconnectedset": largestConnectedSet=true;break;
                    case "averageBlockedIslandSize": averageBlockedIslandSize=true;break;
                    case "averageFloatingBlockedIslandSize": averageFloatingBlockedIslandSize=true;break;
                    case "largestRatioToSecond": largestRatioToSecond=true;break;
                    case "largestRatioToRemaining": largestRatioToRemaining=true;break;
                }
            }
        }
        
    }

    public ArrayList<Integer> computeConnectedSetSizeList() {
        ArrayList<Integer> list = new ArrayList<>();
        for (ArrayList<Point> connectedSet : connectedSets) {
            list.add(connectedSet.size());
        }
        return list;
    }
}