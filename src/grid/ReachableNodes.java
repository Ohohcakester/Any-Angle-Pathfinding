package grid;

import grid.anya.Point;

import java.util.ArrayList;
import java.util.LinkedList;

public class ReachableNodes extends BreadthFirstSearch {

    private ReachableNodes(GridGraph graph, int sx, int sy, int ex,
            int ey) {
        super(graph, sx, sy, ex, ey);
    }
    
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

}
