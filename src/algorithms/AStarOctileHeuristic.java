package algorithms;

import grid.GridGraph;

public class AStarOctileHeuristic extends AStar {
    private static final float SQRT_TWO_MINUS_ONE = (float)(Math.sqrt(2) - 1);
    
    public AStarOctileHeuristic(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }

    /**
     * Octile distance:
     *   min(dx,dy)*sqrt(2) + (max(dx,dy)-min(dx,dy))
     * = min(dx,dy)*(sqrt(2)-1) + max(dx,dy)
     */
    protected float heuristic(int x, int y) {
        int dx = x-ex;
        int dy = y-ey;
        if (dx<0) dx = -dx;
        if (dy<0) dy = -dy;
        
        int min = dx;
        int max = dy;
        if (dy < dx) {
            min = dy;
            max = dx;
        }
        
        return min*SQRT_TWO_MINUS_ONE + max;
    }
}
