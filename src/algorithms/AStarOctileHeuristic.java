package algorithms;

import grid.GridGraph;

public class AStarOctileHeuristic extends AStarStaticMemory {
    public AStarOctileHeuristic(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }

    public static AStarOctileHeuristic postSmooth(GridGraph graph, int sx, int sy, int ex, int ey) {
        AStarOctileHeuristic algo = new AStarOctileHeuristic(graph, sx, sy, ex, ey);
        algo.postSmoothingOn = true;
        return algo;
    }

    protected float heuristic(int x, int y) {
        return graph.octileDistance(x, y, ex, ey);
    }
}
