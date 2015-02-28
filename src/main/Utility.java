package main;

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
