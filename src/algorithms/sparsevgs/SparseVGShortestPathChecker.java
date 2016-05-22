package algorithms.sparsevgs;

import grid.GridGraph;

import java.util.Arrays;

import algorithms.datatypes.Memory;
import algorithms.priorityqueue.ReusableIndirectHeap;

public final class SparseVGShortestPathChecker {
    private static final float EPSILON = 0.00001f;
    
    private final GridGraph graph;
    private ReusableIndirectHeap pq;
    private final int gridSize;
    
    private final LineOfSightScanner scanner;

    private final int[][] nodeIndex;
    private final VertexNLevelSVGNode[] nodes;
    private final int graphSize;
    private final int graphMaxSize;

    private int[] reachableVerticesFromGoal;
    private int nReachableVerticesFromGoal;
    
    public SparseVGShortestPathChecker(VertexNLevelSparseVisibilityGraph visibilityGraph, GridGraph graph, LineOfSightScanner losScanner) {
        this.graph = graph;
        this.gridSize = (graph.sizeX+1)*(graph.sizeY+1);
        
        this.scanner = losScanner;
        
        this.nodeIndex = visibilityGraph.getNodeIndexMap();
        this.nodes = visibilityGraph.getAllNodes();
        this.graphSize = visibilityGraph.size();
        this.graphMaxSize = visibilityGraph.maxSize();
        
        reachableVerticesFromGoal = new int[11];
        nReachableVerticesFromGoal = 0;
    }
    
    private void computeReachableVerticesFromGoal(int ex, int ey) {
        scanner.computeAllVisibleTautSuccessors(ex, ey);
        nReachableVerticesFromGoal = scanner.nSuccessors;
        
        int arraySize = reachableVerticesFromGoal.length;
        while (arraySize < nReachableVerticesFromGoal) {
            arraySize *= 2;
        }
        if (arraySize > reachableVerticesFromGoal.length) {
            reachableVerticesFromGoal = Arrays.copyOf(reachableVerticesFromGoal, arraySize);
        }
        
        for (int i=0;i<nReachableVerticesFromGoal;++i) {
            int x = scanner.successorsX[i];
            int y = scanner.successorsY[i];
            reachableVerticesFromGoal[i] = nodeIndex[y][x];
        }
    }
    
    private void markReachableFromGoal(boolean value) {
        for (int i=0;i<nReachableVerticesFromGoal;++i) {
            nodes[reachableVerticesFromGoal[i]].hasEdgeToGoal = value;
        }
    }

    private static boolean isCollinearAndBetween(int x1, int y1, int x2, int y2, int x3, int y3) {
        return ((y3-y1)*(x2-x1) == (x3-x1)*(y2-y1)) && 
                (
                  ((x1<x3 && x3<x2) || (x2<x3 && x3<x1)) ||
                  ((y1<y3 && y3<y2) || (y2<y3 && y3<y1))
                );
    }
    
    /**
     * Finds the shortest path between n1 and n2, using only vertices with level == maxLevel, excluding v.
     */
    public final boolean hasShorterAnyAnglePath(int n1, int n2, int v) {
        VertexNLevelSVGNode node1 = nodes[n1];
        int x1 = node1.x;
        int y1 = node1.y;
        VertexNLevelSVGNode node2 = nodes[n2];
        int x2 = node2.x;
        int y2 = node2.y;
        
        VertexNLevelSVGNode nodeV = nodes[v];
        int cx = nodeV.x;
        int cy = nodeV.y;
        
        if (isCollinearAndBetween(x1,y1,x2,y2,cx,cy)) return false;
        if (graph.lineOfSight(x1, y1, x2, y2)) return true;
        computeReachableVerticesFromGoal(x2, y2);
        markReachableFromGoal(true);
        
        
        
        float upperBound = graph.distance(x1, y1, cx, cy) + graph.distance(cx, cy, x2, y2) + EPSILON;

        pq = new ReusableIndirectHeap(graphSize, graphMaxSize);
        Memory.initialise(gridSize, Float.POSITIVE_INFINITY, -1, false);
        Memory.setVisited(n1, true);
        Memory.setDistance(n1, 0);
        pq.decreaseKey(n1, 0);
        scanner.computeAllVisibleTautSuccessors(x1, y1);
        
        
        boolean result = false;
        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            Memory.setVisited(current, true);
            
            
            if (Memory.distance(current) > upperBound) {throw new UnsupportedOperationException("shouldn't happen");}
                //{result=false; break;} // have to check this first.
            if (current == n2) {result=true; break;} // now we know distance(current) <= upperBound.

            VertexNLevelSVGNode curr = nodes[current];

            int[] neighbours = curr.outgoingEdges;
            float[] weights = curr.edgeWeights;
            int nNeighbours = curr.nEdges;
            
            for (int i=0;i<nNeighbours;++i) {
                int target = neighbours[i];
                if (target == v || Memory.visited(target)) continue;

                VertexNLevelSVGNode tar = nodes[target];
                
                // Relax
                float distance = Memory.distance(current) + weights[i];
                if (distance < upperBound && distance < Memory.distance(target)) {
                    Memory.setDistance(target, distance);
                    pq.decreaseKey(target, distance + graph.distance(tar.x, tar.y, x2, y2));
                }
            }
            if (curr.hasEdgeToGoal) { /// TODO: TRY USING PARENT INSTEAD. BETTER ENCAPSULATION / LESS MEMORY USED
                VertexNLevelSVGNode tar = nodes[n2];
                
                // Relax
                float distance = Memory.distance(current) + graph.distance(curr.x, curr.y, x2, y2);
                if (distance < upperBound && distance < Memory.distance(n2)) {
                    Memory.setDistance(n2, distance);
                    pq.decreaseKey(n2, distance);
                }
                
            }
        }
        
        markReachableFromGoal(false);
        return result;
    }
}
