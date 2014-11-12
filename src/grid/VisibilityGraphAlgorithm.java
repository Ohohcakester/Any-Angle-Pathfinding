package grid;

import grid.anya.Point;
import grid.visibilitygraph.Edge;
import grid.visibilitygraph.VisibilityGraph;

import java.util.Iterator;

public class VisibilityGraphAlgorithm extends AStar {
    private VisibilityGraph visibilityGraph;
    
    public VisibilityGraphAlgorithm(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }

    @Override
    public void computePath() {
        visibilityGraph = new VisibilityGraph(graph, sx, sy, ex, ey);
        distance = new Float[visibilityGraph.size()];
        parent = new int[visibilityGraph.size()];

        initialise(visibilityGraph.startNode());
        visited = new boolean[visibilityGraph.size()];
        
        pq = new IndirectHeap<Float>(distance, true);
        pq.heapify();
        
        int finish = visibilityGraph.endNode();
        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            visited[current] = true;
            super.maybeSaveSearchSnapshot();
            if (current == finish) {
                break;
            }
            
            Iterator<Edge> itr = visibilityGraph.edgeIterator(current);
            while (itr.hasNext()) {
                Edge edge = itr.next();
                if (!visited[edge.dest] && relax(edge)) {
                    // If relaxation is done.
                    pq.decreaseKey(edge.dest, distance[edge.dest]);
                }
            }
        }
        
    }
    
    protected final boolean relax(Edge edge) {
        // return true iff relaxation is done.
        return relax(edge.source, edge.dest, edge.weight);
    }

    protected final boolean relax(int u, int v, float weightUV) {
        // return true iff relaxation is done.
        float newWeight = distance[u] + weightUV;
        if (newWeight < distance[v]) {
            distance[v] = newWeight;
            parent[v] = u;
            return true;
        }
        return false;
    }
    

    private int pathLength() {
        int length = 0;
        int current = visibilityGraph.endNode();
        while (current != -1) {
            current = parent[current];
            length++;
        }
        return length;
    }

    @Override
    public int[][] getPath() {
        int length = pathLength();
        int[][] path = new int[length][];
        int current = visibilityGraph.endNode();
        
        int index = length-1;
        while (current != -1) {
            Point point = visibilityGraph.coordinateOf(current);
            int x = point.x;
            int y = point.y;
            
            path[index] = new int[2];
            path[index][0] = x;
            path[index][1] = y;
            
            index--;
            current = parent[current];
        }
        
        return path;
    }

    @Override
    protected Integer[] snapshotEdge(int endIndex) {
        Integer[] edge = new Integer[4];
        int startIndex = parent[endIndex];
        Point startPoint = visibilityGraph.coordinateOf(startIndex);
        Point endPoint = visibilityGraph.coordinateOf(endIndex);
        edge[0] = startPoint.x;
        edge[1] = startPoint.y;
        edge[2] = endPoint.x;
        edge[3] = endPoint.y;
        return edge;
    }

    @Override
    protected Integer[] snapshotVertex(int index) {
        if (selected(index)) {
            Point point = visibilityGraph.coordinateOf(index);
            Integer[] edge = new Integer[2];
            edge[0] = point.x;
            edge[1] = point.y;
            return edge;
        }
        return null;
    }
}
