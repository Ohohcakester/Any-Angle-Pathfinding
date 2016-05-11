package grid;

import java.util.ArrayList;

import algorithms.BreadthFirstSearch;
import algorithms.datatypes.Point;

public class ReachableNodes extends BreadthFirstSearch {

    private ReachableNodes(GridGraph graph, int sx, int sy, int ex,
            int ey) {
        super(graph, sx, sy, ex, ey);
    }
    
    /**
     * Computes the set of all nodes reachable from (sx,sy) by an unblocked path.
     * @param graph the grid to use.
     * @param sx x-coordinate of root node
     * @param sy y-coordinate of root node
     * @return An ArrayList of Point objects (nodes reachable from (sx,sy) via an unblocked path).
     */
    public static ArrayList<Point> computeReachable(GridGraph graph, int sx, int sy) {
        ReachableNodes nodes = new ReachableNodes(graph, sx, sy, -10, -10);
        ArrayList<Point> list = new ArrayList<>();

        nodes.computePath();
        for (int i=0; i<nodes.visited.length; i++) {
            if (nodes.visited[i]) {
                int x = nodes.toTwoDimX(i);
                int y = nodes.toTwoDimY(i);
                list.add(new Point(x, y));
            }
        }
        
        return list;
    }
    
    /**
     * Similar to computeReachable, but this one also helps to mark the markVisited[] boolean array.
     */
    public static ArrayList<Point> computeReachable(GridGraph graph, int sx, int sy, boolean[] markVisited) {
        ReachableNodes nodes = new ReachableNodes(graph, sx, sy, -10, -10);
        ArrayList<Point> list = new ArrayList<>();

        nodes.computePath();
        for (int i=0; i<nodes.visited.length; i++) {
            if (nodes.visited[i]) {
                int x = nodes.toTwoDimX(i);
                int y = nodes.toTwoDimY(i);
                list.add(new Point(x, y));
                markVisited[i] = true;
            }
        }
        
        return list;
    }
}
