package main.utility;

import java.util.ArrayList;
import java.util.Arrays;

import algorithms.Anya;
import algorithms.anya16.Anya16;
import algorithms.PathFindingAlgorithm;
import algorithms.datatypes.Memory;
import algorithms.datatypes.Point;
import algorithms.datatypes.SnapshotItem;
import algorithms.priorityqueue.ReusableIndirectHeap;
import algorithms.sparsevgs.EdgeNLevelSparseVisibilityGraph;
import algorithms.sparsevgs.EdgeNLevelSparseVisibilityGraphAlgorithm;
import algorithms.sparsevgs.EdgeNLevelSparseVisibilityGraphAlgorithmFibHeap;
import algorithms.sparsevgs.LineOfSightScanner;
import algorithms.sparsevgs.SparseVisibilityGraph;
import algorithms.sparsevgs.SparseVisibilityGraphAlgorithmFibHeap;
import algorithms.sparsevgs.VisibilityGraphOptimised;
import algorithms.visibilitygraph.VisibilityGraph;
import grid.GridGraph;
import main.AlgoFunction;
import main.analysis.MazeAnalysis;
import main.graphgeneration.AutomataGenerator;
import main.testgen.StartEndPointData;

public class Utility {

    /**
     * Compute the length of a given path. (Using euclidean distance)
     */
    public static double computePathLength(GridGraph gridGraph, int[][] path) {
        path = removeDuplicatesInPath(path);
        double pathLength = 0;
        for (int i=0; i<path.length-1; i++) {
            pathLength += gridGraph.distance_double(path[i][0], path[i][1],
                            path[i+1][0], path[i+1][1]);
        }
        return pathLength;
    }
    
    public static double computeOptimalPathLengthOnline(GridGraph gridGraph, Point start, Point end) {
        return computeOptimalPathLengthOnline(gridGraph, start.x, start.y, end.x, end.y);
    }
    
    public static double computeOptimalPathLengthOnline(GridGraph gridGraph, int sx, int sy, int ex, int ey) {
        int[][] path = computeOptimalPathOnline(gridGraph, sx, sy, ex, ey);
        return computePathLength(gridGraph, path);
    }
    
    public static int[][] computeOptimalPathOnline(GridGraph gridGraph, int sx, int sy, int ex, int ey) {
        // Optimal Online algorithm.
        PathFindingAlgorithm algo = new Anya16(gridGraph, sx, sy, ex, ey);
        algo.computePath();
        int[][] path = algo.getPath();
        path = removeDuplicatesInPath(path);
        return path;
    }
    
    public static double computeOptimalPathLengthOffline(GridGraph gridGraph, Point start, Point end) {
        return computeOptimalPathLengthOffline(gridGraph, start.x, start.y, end.x, end.y);
    }
    
    public static double computeOptimalPathLengthOffline(GridGraph gridGraph, int sx, int sy, int ex, int ey) {
        int[][] path = computeOptimalPathOffline(gridGraph, sx, sy, ex, ey);
        return computePathLength(gridGraph, path);
    }
    
    public static int[][] computeOptimalPathOffline(GridGraph gridGraph, int sx, int sy, int ex, int ey) {
        // Optimal Offline algorithm. (preprocessing for faster pathfinding)
        PathFindingAlgorithm algo = EdgeNLevelSparseVisibilityGraphAlgorithm.graphReuse(gridGraph, sx, sy, ex, ey);
        algo.computePath();
        int[][] path = algo.getPath();
        path = removeDuplicatesInPath(path);
        return path;
    }
    
    public static ArrayList<StartEndPointData> fixProblemPathLength(GridGraph gridGraph, ArrayList<StartEndPointData> problems) {
        ArrayList<StartEndPointData> fixedProblems = new ArrayList<>();
        for (StartEndPointData problem : problems) {
            //System.out.println(problem.start + " | " + problem.end);
            double shortestPathLength = computeOptimalPathLengthOnline(gridGraph, problem.start, problem.end);
            fixedProblems.add(new StartEndPointData(problem.start, problem.end, shortestPathLength));
            if (shortestPathLength > problem.shortestPath + 0.0001) System.out.println("REPAIRING: " + problem.shortestPath + " -> " + shortestPathLength);
        }
        return fixedProblems;
    }

    public static int[][] removeDuplicatesInPath(int[][] path) {
        if (path.length <= 2) return path;
        
        int[][] newPath = new int[path.length][];
        int index = 0;
        newPath[0] = path[0];
        for (int i=1; i<path.length-1; ++i) {
            if (isCollinear(path[i][0], path[i][1], path[i+1][0], path[i+1][1], newPath[index][0], newPath[index][1])) {
                // skip
            } else {
                index++;
                newPath[index] = path[i];
            }
        }
        index++;
        newPath[index] = path[path.length-1];
        return Arrays.copyOf(newPath, index+1);
    }
    
    private static boolean isCollinear(int x1, int y1, int x2, int y2, int x3, int y3) {
        return (y3-y1)*(x2-x1) == (x3-x1)*(y2-y1);
    }
    
    /**
     * Generates a path between two points on a grid.
     * @return an array of int[2] indicating the coordinates of the path.
     */
    public static int[][] generatePath(AlgoFunction algoFunction, GridGraph gridGraph,
            int sx, int sy, int ex, int ey) {
        PathFindingAlgorithm algo = algoFunction.getAlgo(gridGraph, sx, sy, ex, ey);
        algo.computePath();
        
        int[][] path = algo.getPath();
        return path;
    }
    
    public static boolean isPathTaut(GridGraph gridGraph, int[][] path) {
        int v1 = 0;
        int v2 = 1;
        for (int v3=2; v3<path.length; ++v3) {
            if (!gridGraph.isTaut(path[v1][0], path[v1][1], path[v2][0], path[v2][1], path[v3][0], path[v3][1])) return false;
            ++v1;
            ++v2;
        }
        return true;
    }

    public static boolean isOptimal(double length, double optimalLength) {
        //System.out.println(length + " | " + optimalLength + " | " + ((length - optimalLength) < 0.0001)); 
        return (length - optimalLength) < 0.0001;
    }
    
    public static boolean validateMazeConnectedSetSize(GridGraph gridGraph, float cutoffLargestRatioToRemaining) {
        MazeAnalysis.Options options = new MazeAnalysis.Options();
        options.largestRatioToSecond = true;
        options.largestRatioToRemaining = true;
        MazeAnalysis mazeAnalysis = new MazeAnalysis(gridGraph, options);
        System.out.println(mazeAnalysis.largestRatioToRemaining + " " + mazeAnalysis.largestRatioToSecond);
        return mazeAnalysis.largestRatioToRemaining >= cutoffLargestRatioToRemaining;
    }
    
    public static void cleanUpPreallocatedMemory() {
        Anya.clearMemory();
        Anya16.clearMemory();
        VisibilityGraph.clearMemory();
        SparseVisibilityGraph.clearMemory();
        SparseVisibilityGraphAlgorithmFibHeap.clearMemory();
        Memory.clearMemory();
        ReusableIndirectHeap.clearMemory();
        LineOfSightScanner.clearMemory();
        EdgeNLevelSparseVisibilityGraph.clearMemory();
        EdgeNLevelSparseVisibilityGraphAlgorithmFibHeap.clearMemory();
        VisibilityGraphOptimised.clearMemory();
        SnapshotItem.clearCached();
        //IVGAlgorithm.clearCached();
        
        System.gc(); System.gc();
    }
}
