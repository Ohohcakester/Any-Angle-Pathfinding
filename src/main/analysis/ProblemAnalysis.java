package main.analysis;

import algorithms.PathFindingAlgorithm;
import algorithms.VisibilityGraphAlgorithm;
import algorithms.visibilitygraph.BFSVisibilityGraph;
import algorithms.visibilitygraph.VisibilityGraph;
import grid.GridGraph;
import main.utility.Utility;

public class ProblemAnalysis {
    
    public final int sx, sy, ex, ey;
    public final double shortestPathLength;
    public final double straightLineDistance;
    public final double directness;
    public final double distanceCoverage;
    public final double minMapCoverage;
    
    public final int shortestPathHeadingChanges;
    public final int minHeadingChanges;
    
    public final int[][] path;

    public ProblemAnalysis(int sx, int sy, int ex, int ey,
            double shortestPathLength, double straightLineDistance,
            double directness, double distanceCoverage, double minMapCoverage,
            int shortestPathHeadingChanges, int minHeadingChanges, int[][] path) {
        this.sx = sx;
        this.sy = sy;
        this.ex = ex;
        this.ey = ey;
        this.shortestPathLength = shortestPathLength;
        this.straightLineDistance = straightLineDistance;
        this.directness = directness;
        this.distanceCoverage = distanceCoverage;
        this.minMapCoverage = minMapCoverage;
        this.shortestPathHeadingChanges = shortestPathHeadingChanges;
        this.minHeadingChanges = minHeadingChanges;
        this.path = path;
    }
    
    public static ProblemAnalysis computeSlow(GridGraph gridGraph, int sx, int sy, int ex, int ey) {
        VisibilityGraphAlgorithm algo = VisibilityGraphAlgorithm.graphReuse(gridGraph, sx, sy, ex, ey);
        algo.computePath();
        int[][] path = algo.getPath();
        int sizeX = gridGraph.sizeX;
        int sizeY = gridGraph.sizeY;
        
        double shortestPathLength = Utility.computePathLength(gridGraph, path);
        double straightLineDistance = gridGraph.distance(sx, sy, ex, ey);
        double directness = computeDirectness(shortestPathLength, straightLineDistance);
        double distanceCoverage = computeDistanceCoverage(straightLineDistance, sizeX, sizeY);
        double minMapCoverage = computerMinMapCoverage(shortestPathLength, sizeX, sizeY);
        
        int shortestPathHeadingChanges = path.length;
        VisibilityGraph visibilityGraph = algo.getVisibilityGraph();
        int minHeadingChanges = computeMinHeadingChanges(gridGraph,sx,sy,ex,ey);
        
        return new ProblemAnalysis(sx, sy, ex, ey, shortestPathLength, straightLineDistance, directness, distanceCoverage, minMapCoverage, shortestPathHeadingChanges, minHeadingChanges, path);
    }
    
    public static ProblemAnalysis computeFast(GridGraph gridGraph, int sx, int sy, int ex, int ey) {
        int sizeX = gridGraph.sizeX;
        int sizeY = gridGraph.sizeY;
        
        int[][] path = Utility.computeOptimalPathOnline(gridGraph, sx, sy, ex, ey);
        double shortestPathLength = Utility.computePathLength(gridGraph, path);
        double straightLineDistance = gridGraph.distance(sx, sy, ex, ey);
        double directness = computeDirectness(shortestPathLength, straightLineDistance);
        double distanceCoverage = computeDistanceCoverage(straightLineDistance, sizeX, sizeY);
        double minMapCoverage = computerMinMapCoverage(shortestPathLength, sizeX, sizeY);
        
        int shortestPathHeadingChanges = path.length;
        int minHeadingChanges = -1;
        
        return new ProblemAnalysis(sx, sy, ex, ey, shortestPathLength, straightLineDistance, directness, distanceCoverage, minMapCoverage, shortestPathHeadingChanges, minHeadingChanges, path);
    }
    
    public static ProblemAnalysis computePathOnly(GridGraph gridGraph, int sx, int sy, int ex, int ey) {
        int[][] path = Utility.computeOptimalPathOnline(gridGraph, sx, sy, ex, ey);
        double shortestPathLength = Utility.computePathLength(gridGraph, path);
        double straightLineDistance = -1;
        double directness = -1;
        double distanceCoverage = -1;
        double minMapCoverage = -1;
        
        int shortestPathHeadingChanges = -1;
        int minHeadingChanges = -1;
        
        return new ProblemAnalysis(sx, sy, ex, ey, shortestPathLength, straightLineDistance, directness, distanceCoverage, minMapCoverage, shortestPathHeadingChanges, minHeadingChanges, path);
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

    public static int computeMinHeadingChanges(GridGraph gridGraph, int sx, int sy, int ex, int ey) {
        BFSVisibilityGraph algo = BFSVisibilityGraph.graphReuse(gridGraph, sx, sy, ex, ey);
        algo.computePath();
        return algo.getPath().length;
    }

    @Override
    public String toString() {
        return "sx = " + sx + 
                "\nsy = " + sy +
                "\nex = " + ex +
                "\ney = " + ey +
                "\nshortestPathLength = " + shortestPathLength +
                "\nstraightLineDistance = " + straightLineDistance +
                "\ndirectness = " + directness +
                "\ndistanceCoverage = " + distanceCoverage +
                "\nminMapCoverage = " + minMapCoverage +
                "\nshortestPathHeadingChanges = " + shortestPathHeadingChanges +
                "\nminHeadingChanges = " + (minHeadingChanges==-1?"Not Computed":minHeadingChanges);
    }
}