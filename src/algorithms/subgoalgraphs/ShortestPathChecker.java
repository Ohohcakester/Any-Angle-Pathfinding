package algorithms.subgoalgraphs;

import grid.GridGraph;
import algorithms.datatypes.Memory;
import algorithms.priorityqueue.ReusableIndirectHeap;

public final class ShortestPathChecker {
    private static final float EPSILON = 0.00001f;
    
    private final GridGraph graph;
    private ReusableIndirectHeap pq;
    private final int gridSize;
    
    private final int[] xPositions;
    private final int[] yPositions;
    private final int graphSize;
    private final int graphMaxSize;
    private final int[] levels;
    private final int maxLevel;
    private final int[][] graphNeighbours;
    private final int[] graphNumNeighbours;

    
    public ShortestPathChecker(NLevelSubgoalGraph subgoalGraph, GridGraph graph) {
        this.graph = graph;
        this.gridSize = (graph.sizeX+1)*(graph.sizeY+1);
        
        this.xPositions = subgoalGraph.xPositions;
        this.yPositions = subgoalGraph.yPositions;
        this.graphSize = subgoalGraph.size();
        this.graphMaxSize = subgoalGraph.maxSize();
        this.levels = subgoalGraph.levels;
        this.maxLevel = subgoalGraph.maxLevel;
        this.graphNeighbours = subgoalGraph.neighbours;
        this.graphNumNeighbours = subgoalGraph.nNeighbours;
    }

    public ShortestPathChecker(AnyAngleNLevelSubgoalGraph subgoalGraph, GridGraph graph) {
        this.graph = graph;
        this.gridSize = (graph.sizeX+1)*(graph.sizeY+1);
        
        this.xPositions = subgoalGraph.xPositions;
        this.yPositions = subgoalGraph.yPositions;
        this.graphSize = subgoalGraph.size();
        this.graphMaxSize = subgoalGraph.maxSize();
        this.levels = subgoalGraph.levels;
        this.maxLevel = subgoalGraph.maxLevel;
        this.graphNeighbours = subgoalGraph.neighbours;
        this.graphNumNeighbours = subgoalGraph.nNeighbours;
    }

    /**
     * Finds the shortest path between n1 and n2, using only vertices with level == maxLevel, excluding v.
     */
    public final boolean hasShorterGlobalPath(int n1, int n2, int v) {
        Memory.initialise(gridSize, Float.POSITIVE_INFINITY, -1, false);
        
        int x1 = xPositions[n1];
        int y1 = yPositions[n1];
        int x2 = xPositions[n2];
        int y2 = yPositions[n2];
        int cx = xPositions[v];
        int cy = yPositions[v];
        
        float upperBound = graph.octileDistance(x1, y1, cx, cy) + graph.octileDistance(cx, cy, x2, y2);

        pq = new ReusableIndirectHeap(graphSize, graphMaxSize);
        Memory.setDistance(n1, 0);
        pq.decreaseKey(n1, 0);
        
        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            Memory.setVisited(current, true);
            
            
            if (Memory.distance(current) > upperBound + EPSILON) return false; // have to check this first.
            if (current == n2) return true; // now we know distance(current) <= upperBound.

            int currX = xPositions[current];
            int currY = yPositions[current];

            int[] neighbours = graphNeighbours[current];
            int nNeighbours = graphNumNeighbours[current];
            
            for (int i=0;i<nNeighbours;++i) {
                int target = neighbours[i];
                if (levels[target] != maxLevel || target == v || Memory.visited(target)) continue;

                int targetX = xPositions[target];
                int targetY = yPositions[target];

                // Relax
                float distance = Memory.distance(current) + graph.octileDistance(currX, currY, targetX, targetY);
                if (distance < Memory.distance(target)) {
                    Memory.setDistance(target, distance);
                    pq.decreaseKey(target, distance + graph.octileDistance(targetX, targetY, x2, y2));
                }
            }
        }
        
        return false;
    }
    

    /**
     * Finds the shortest path between n1 and n2, using only vertices with level == maxLevel, excluding v.
     */
    public final boolean hasShorterAnyAnglePath(int n1, int n2, int v) {
        Memory.initialise(gridSize, Float.POSITIVE_INFINITY, -1, false);
        
        int x1 = xPositions[n1];
        int y1 = yPositions[n1];
        int x2 = xPositions[n2];
        int y2 = yPositions[n2];
        int cx = xPositions[v];
        int cy = yPositions[v];
        
        float upperBound = graph.distance(x1, y1, cx, cy) + graph.distance(cx, cy, x2, y2);

        pq = new ReusableIndirectHeap(graphSize, graphMaxSize);
        Memory.setDistance(n1, 0);
        pq.decreaseKey(n1, 0);
        
        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            Memory.setVisited(current, true);
            
            
            if (Memory.distance(current) > upperBound + EPSILON) return false; // have to check this first.
            if (current == n2) return true; // now we know distance(current) <= upperBound.

            int currX = xPositions[current];
            int currY = yPositions[current];

            int[] neighbours = graphNeighbours[current];
            int nNeighbours = graphNumNeighbours[current];
            
            for (int i=0;i<nNeighbours;++i) {
                int target = neighbours[i];
                if (levels[target] != maxLevel || target == v || Memory.visited(target)) continue;

                int targetX = xPositions[target];
                int targetY = yPositions[target];

                // Relax
                float distance = Memory.distance(current) + graph.distance(currX, currY, targetX, targetY);
                if (distance < Memory.distance(target)) {
                    Memory.setDistance(target, distance);
                    pq.decreaseKey(target, distance + graph.distance(targetX, targetY, x2, y2));
                }
            }
        }
        
        return false;
    }
}
