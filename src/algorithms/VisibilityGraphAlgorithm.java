package algorithms;

import grid.GridGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import algorithms.datatypes.Point;
import algorithms.datatypes.SnapshotItem;
import algorithms.priorityqueue.IndirectHeap;
import algorithms.visibilitygraph.Edge;
import algorithms.visibilitygraph.VisibilityGraph;

public class VisibilityGraphAlgorithm extends AStar {
    protected VisibilityGraph visibilityGraph;
    protected boolean reuseGraph = false;
    protected boolean slowDijkstra = false;
    
    public VisibilityGraphAlgorithm(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }

    public static VisibilityGraphAlgorithm noHeuristic(GridGraph graph, int sx, int sy, int ex, int ey) {
        VisibilityGraphAlgorithm algo = new VisibilityGraphAlgorithm(graph, sx, sy, ex, ey);
        algo.heuristicWeight = 0;
        return algo;
    }

    public static VisibilityGraphAlgorithm graphReuseNoHeuristic(GridGraph graph, int sx, int sy, int ex, int ey) {
        VisibilityGraphAlgorithm algo = new VisibilityGraphAlgorithm(graph, sx, sy, ex, ey);
        algo.reuseGraph = true;
        algo.heuristicWeight = 0;
        return algo;
    }
    
    public static VisibilityGraphAlgorithm graphReuse(GridGraph graph, int sx, int sy, int ex, int ey) {
        VisibilityGraphAlgorithm algo = new VisibilityGraphAlgorithm(graph, sx, sy, ex, ey);
        algo.reuseGraph = true;
        return algo;
    }
    
    public static VisibilityGraphAlgorithm graphReuseSlowDijkstra(GridGraph graph, int sx, int sy, int ex, int ey) {
        VisibilityGraphAlgorithm algo = new VisibilityGraphAlgorithm(graph, sx, sy, ex, ey);
        algo.reuseGraph = true;
        algo.slowDijkstra = true;
        return algo;
    }

    public VisibilityGraph getVisibilityGraph() {
        return visibilityGraph;
    }
    
    @Override
    public void computePath() {
        setupVisibilityGraph();
        
        distance = new Float[visibilityGraph.size()];
        parent = new int[visibilityGraph.size()];

        initialise(visibilityGraph.startNode());
        visited = new boolean[visibilityGraph.size()];
        
        if (slowDijkstra) {
            slowDijkstra();
        } else {
            pqDijkstra();
        }
    }

    protected void setupVisibilityGraph() {
        if (reuseGraph) {
            visibilityGraph = VisibilityGraph.getStoredGraph(graph, sx, sy, ex, ey);
        } else {
            visibilityGraph = new VisibilityGraph(graph, sx, sy, ex, ey);
        }
        
        if (isRecording()) {
            visibilityGraph.setSaveSnapshotFunction(()->saveVisibilityGraphSnapshot());
            visibilityGraph.initialise();
            saveVisibilityGraphSnapshot();
        } else {
            visibilityGraph.initialise();
        }
    }
    
    protected void slowDijkstra() {
        int finish = visibilityGraph.endNode();
        while (true) {
            int current = findMinDistance();
            if (current == -1) {
                break;
            }
            visited[current] = true;
            
            if (current == finish) {
                break;
            }
            
            Iterator<Edge> itr = visibilityGraph.edgeIterator(current);
            while (itr.hasNext()) {
                Edge edge = itr.next();
                if (!visited[edge.dest]) {
                    relax(edge);
                }
            }
            
            maybeSaveSearchSnapshot();
        }
    }
    
    private int findMinDistance() {
        float minDistance = Float.POSITIVE_INFINITY;
        int minIndex = -1;
        for (int i=0; i<distance.length; i++) {
            if (!visited[i] && distance[i] < minDistance) {
                minDistance = distance[i];
                minIndex = i;
            }
        }
        return minIndex;
    }

    protected void pqDijkstra() {
        pq = new IndirectHeap<Float>(distance, true);
        pq.heapify();
        
        int finish = visibilityGraph.endNode();
        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            visited[current] = true;
            
            if (current == finish) {
                break;
            }
            
            Iterator<Edge> itr = visibilityGraph.edgeIterator(current);
            while (itr.hasNext()) {
                Edge edge = itr.next();
                if (!visited[edge.dest] && relax(edge)) {
                    // If relaxation is done.
                    Point dest = visibilityGraph.coordinateOf(edge.dest);
                    pq.decreaseKey(edge.dest, distance[edge.dest] + heuristic(dest.x, dest.y));
                }
            }
            
            maybeSaveSearchSnapshot();
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
    protected int goalParentIndex() {
        return visibilityGraph.endNode();
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
    
    protected void saveVisibilityGraphSnapshot() {
        /*if (!isRecording()) {
            return;
        }*/
        int size = visibilityGraph.size();
        
        List<SnapshotItem> snapshotItemList = new ArrayList<>(size);

        for (int i=0;i<size;i++) {
            Iterator<Edge> iterator = visibilityGraph.edgeIterator(i);
            while (iterator.hasNext()) {
                Edge edge = iterator.next();
                if (edge.source < edge.dest) {
                    Point start = visibilityGraph.coordinateOf(edge.source);
                    Point end = visibilityGraph.coordinateOf(edge.dest);
                    
                    Integer[] path = new Integer[4];
                    path[0] = start.x;
                    path[1] = start.y;
                    path[2] = end.x;
                    path[3] = end.y;
                    
                    SnapshotItem snapshotItem = SnapshotItem.generate(path, Color.GREEN);
                    snapshotItemList.add(snapshotItem);
                }
            }
        }
        addSnapshot(snapshotItemList);
    }
    
    @Override
    public void printStatistics() {
        System.out.println("Nodes: " + visibilityGraph.size());
        System.out.println("Edges (Directed): " + visibilityGraph.computeSumDegrees());
    }
}
