package algorithms.subgoalgraphs;

import grid.GridGraph;
import algorithms.datatypes.Memory;
import algorithms.priorityqueue.ReusableIndirectHeap;

public final class ShortestPathChecker {
    private static final float EPSILON = 0.00001f;
    private final NLevelSubgoalGraph subgoalGraph;
    private final GridGraph graph;
    private ReusableIndirectHeap pq;
    private final int size;
    
    public ShortestPathChecker(NLevelSubgoalGraph subgoalGraph, GridGraph graph) {
        this.subgoalGraph = subgoalGraph;
        this.graph = graph;
        this.size = (graph.sizeX+1)*(graph.sizeY+1);
    }
    
    /**
     * Finds the shortest path between n1 and n2, using only vertices with level == maxLevel, excluding v.
     */
    public final boolean hasShorterGlobalPath(int n1, int n2, int v) {
        Memory.initialise(size, Float.POSITIVE_INFINITY, -1, false);
        
        int x1 = subgoalGraph.xPositions[n1];
        int y1 = subgoalGraph.yPositions[n1];
        int x2 = subgoalGraph.xPositions[n2];
        int y2 = subgoalGraph.yPositions[n2];
        int cx = subgoalGraph.xPositions[v];
        int cy = subgoalGraph.yPositions[v];
        
        float upperBound = graph.octileDistance(x1, y1, cx, cy) + graph.octileDistance(cx, cy, x2, y2);

        pq = new ReusableIndirectHeap(subgoalGraph.size(), subgoalGraph.maxSize());
        Memory.setDistance(n1, 0);
        pq.decreaseKey(n1, 0);
        
        int[] xPositions = subgoalGraph.xPositions;
        int[] yPositions = subgoalGraph.yPositions;
        int[] levels = subgoalGraph.levels;
        int maxLevel = subgoalGraph.maxLevel;
        
        int n2x = xPositions[n2];
        int n2y = yPositions[n2];

        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            Memory.setVisited(current, true);
            
            
            if (Memory.distance(current) > upperBound + EPSILON) return false; // have to check this first.
            if (current == n2) return true; // now we know distance(current) <= upperBound.

            int currX = xPositions[current];
            int currY = yPositions[current];

            int[] neighbours = subgoalGraph.neighbours[current];
            int nNeighbours = subgoalGraph.nNeighbours[current];
            
            for (int i=0;i<nNeighbours;++i) {
                int target = neighbours[i];
                if (levels[target] != maxLevel || target == v || Memory.visited(target)) continue;

                int targetX = xPositions[target];
                int targetY = yPositions[target];

                // Relax
                float distance = Memory.distance(current) + graph.octileDistance(currX, currY, targetX, targetY);
                if (distance < Memory.distance(target)) {
                    Memory.setDistance(target, distance);
                    pq.decreaseKey(target, distance + graph.octileDistance(targetX, targetY, n2x, n2y));
                }
            }
        }
        
        return false;
    }
}
