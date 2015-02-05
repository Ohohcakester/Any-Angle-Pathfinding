package main;

import algorithms.PathFindingAlgorithm;
import grid.GridGraph;

public class Utility {

    /**
     * Compute the length of a given path. (Using euclidean distance)
     */
    static float computePathLength(GridGraph gridGraph, int[][] path) {
        float pathLength = 0;
        for (int i=0; i<path.length-1; i++) {
            pathLength += gridGraph.distance(path[i][0], path[i][1],
                            path[i+1][0], path[i+1][1]);
        }
        return pathLength;
    }

    /**
     * Generates a path between two points on a grid.
     * @return an array of int[2] indicating the coordinates of the path.
     */
    static int[][] generatePath(GridGraph gridGraph, int sx, int sy,
            int ex, int ey) {
        PathFindingAlgorithm algo = AnyAnglePathfinding.algoFunction.getAlgo(gridGraph, sx, sy, ex, ey);
        try {
            algo.computePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int[][] path = algo.getPath();
        return path;
    }

}
