package main.analysis;

import grid.GridGraph;
import main.Utility;
import algorithms.VisibilityGraphAlgorithm;
import algorithms.visibilitygraph.BFSVisibilityGraph;
import algorithms.visibilitygraph.VisibilityGraph;

public class ProblemAnalysis {
    
    public final int sx, sy, ex, ey;
    public final float shortestPathLength;
    public final float straightLineDistance;
    public final float directness;
    public final float distanceCoverage;
    public final float minMapCoverage;
    
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

    public static float computerMinMapCoverage(float shortestPathLength, int sizeX,
            int sizeY) {
        return shortestPathLength/(float)Math.sqrt(sizeX*sizeX + sizeY*sizeY);
    }


    public static float computeDistanceCoverage(float straightLineDistance,
            int sizeX, int sizeY) {
        return straightLineDistance/(float)Math.sqrt(sizeX*sizeX + sizeY*sizeY);
    }


    public static float computeDirectness(float shortestPathLength,
            float straightLineDistance) {
        return shortestPathLength / straightLineDistance;
    }

    public int computeMinHeadingChanges(GridGraph gridGraph) {
        BFSVisibilityGraph algo = BFSVisibilityGraph.graphReuse(gridGraph, sx, sy, ex, ey);
        algo.computePath();
        return algo.getPath().length;
    }
}