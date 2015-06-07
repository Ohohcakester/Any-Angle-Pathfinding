package main;

import java.util.Arrays;

import grid.GridGraph;
import main.AnyAnglePathfinding.AlgoFunction;
import algorithms.PathFindingAlgorithm;

public class Utility {

    /**
     * Compute the length of a given path. (Using euclidean distance)
     */
    public static float computePathLength(GridGraph gridGraph, int[][] path) {
        float pathLength = 0;
        for (int i=0; i<path.length-1; i++) {
            pathLength += gridGraph.distance(path[i][0], path[i][1],
                            path[i+1][0], path[i+1][1]);
        }
        return pathLength;
    }

    static int[][] generatePath(GridGraph gridGraph, int sx, int sy,
            int ex, int ey) {
        return generatePath(AnyAnglePathfinding.algoFunction, gridGraph, sx, sy, ex, ey);
    }

    static int[][] removeDuplicatesInPath(int[][] path) {
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
    static int[][] generatePath(AlgoFunction algoFunction, GridGraph gridGraph,
            int sx, int sy, int ex, int ey) {
        PathFindingAlgorithm algo = algoFunction.getAlgo(gridGraph, sx, sy, ex, ey);
        try {
            algo.computePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int[][] path = algo.getPath();
        return path;
    }

}
