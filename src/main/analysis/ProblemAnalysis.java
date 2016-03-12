package main.analysis;

import grid.GridGraph;
import main.utility.Utility;
import algorithms.VisibilityGraphAlgorithm;
import algorithms.visibilitygraph.BFSVisibilityGraph;
import algorithms.visibilitygraph.VisibilityGraph;

public class ProblemAnalysis {
    

    public final int sx, sy, ex, ey;
    public final double shortestPathLength;
    public final double straightLineDistance;
    public final double directness;
    public final double distanceCoverage;
    public final double minMapCoverage;
    
    public final int shortestPathHeadingChanges;
    public final int minHeadingChanges;
    
    
    public ProblemAnalysis(GridGraph gridGraph, int sx, int sy, int ex, int ey) {
        this.sx = sx;
        this.sy = sy;
        this.ex = ex;
        this.ey = ey;
        
        VisibilityGraphAlgorithm algo = VisibilityGraphAlgorithm.graphReuse(gridGraph, sx, sy, ex, ey);
        algo.computePath();
        int[][] path = algo.getPath();
        int sizeX = gridGraph.sizeX;
        int sizeY = gridGraph.sizeY;
        
        shortestPathLength = Utility.computePathLength(gridGraph, path);
        straightLineDistance = gridGraph.distance(sx, sy, ex, ey);
        directness = computeDirectness(shortestPathLength, straightLineDistance);
        distanceCoverage = computeDistanceCoverage(straightLineDistance, sizeX, sizeY);
        minMapCoverage = computerMinMapCoverage(shortestPathLength, sizeX, sizeY);
        
        shortestPathHeadingChanges = path.length;
        VisibilityGraph visibilityGraph = algo.getVisibilityGraph();
        minHeadingChanges = computeMinHeadingChanges(gridGraph);
    }

    public static double computerMinMapCoverage(double shortestPathLength, int sizeX,
            int sizeY) {
        return shortestPathLength/Math.sqrt(sizeX*sizeX + sizeY*sizeY);
    }


    public static double computeDistanceCoverage(double straightLineDistance,
            int sizeX, int sizeY) {
        return straightLineDistance/Math.sqrt(sizeX*sizeX + sizeY*sizeY);
    }


    public static double computeDirectness(double shortestPathLength,
            double straightLineDistance) {
        return shortestPathLength / straightLineDistance;
    }

    public int computeMinHeadingChanges(GridGraph gridGraph) {
        BFSVisibilityGraph algo = BFSVisibilityGraph.graphReuse(gridGraph, sx, sy, ex, ey);
        algo.computePath();
        return algo.getPath().length;
    }

    @Override
    public String toString() {
        return "sx=" + sx + 
                "\nsy=" + sy +
                "\nex=" + ex +
                "\ney=" + ey +
                "\nshortestPathLength=" + shortestPathLength +
                "\nstraightLineDistance=" + straightLineDistance +
                "\ndirectness=" + directness +
                "\ndistanceCoverage=" + distanceCoverage +
                "\nminMapCoverage=" + minMapCoverage +
                "\nshortestPathHeadingChanges=" + shortestPathHeadingChanges +
                "\nminHeadingChanges=" + minHeadingChanges;
    }
}