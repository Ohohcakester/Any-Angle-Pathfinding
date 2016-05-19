package algorithms.subgoalgraphs;

import grid.GridGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import algorithms.PathFindingAlgorithm;
import algorithms.datatypes.Memory;
import algorithms.datatypes.SnapshotItem;
import algorithms.priorityqueue.ReusableIndirectHeap;

public class StrictAnyAngleSubgoalGraphsAlgorithm extends PathFindingAlgorithm {
    private static float PENALTY_VALUE = 0.27f;
    
    private SubgoalGraph subgoalGraph;
    private ReusableIndirectHeap pq;
    private int startIndex;
    private int endIndex;
    private int[] xPositions;
    private int[] yPositions;
    private boolean hasDirectPath;

    public StrictAnyAngleSubgoalGraphsAlgorithm(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey);
    }


    @Override
    public void computePath() {
        subgoalGraph = SubgoalGraph.initialiseNew(graph);
        if (graph.lineOfSight(sx, sy, ex, ey)) {
            hasDirectPath = true;
            return;
        }
        subgoalGraph.addStartAndEnd(sx, sy, ex, ey);
        maybeSaveSearchSnapshot();
        
        int size = (sizeX+1)*(sizeY+1);
        
        startIndex = subgoalGraph.startIndex;
        endIndex = subgoalGraph.endIndex;
        
        initialiseMemory(size, Float.POSITIVE_INFINITY, -1, false);
        
        pq = new ReusableIndirectHeap(subgoalGraph.size(), subgoalGraph.maxSize());
        initialise(startIndex);
        
        xPositions = subgoalGraph.xPositions;
        yPositions = subgoalGraph.yPositions;

        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            tryFixPenaltyValue(current);
            Memory.setVisited(current, true);
            
            if (current == endIndex) break;

            int currX = xPositions[current];
            int currY = yPositions[current];
            //System.out.println("POP " + current + " : " + currX + ", " + currY);

            int[] neighbours = subgoalGraph.neighbours[current];
            int nNeighbours = subgoalGraph.nNeighbours[current];
            
            for (int i=0;i<nNeighbours;++i) {
                int target = neighbours[i];
                if (Memory.visited(target)) continue;

                int targetX = xPositions[target];
                int targetY = yPositions[target];

                tryRelax(current, currX, currY, target, targetX, targetY);
            }
            if (subgoalGraph.hasEdgeToGoal[current]) {
                tryRelax(current, currX, currY, endIndex, ex, ey);
            }

            maybeSaveSearchSnapshot();
        }

        subgoalGraph.restoreOriginalGraph();
    }

    private final void tryFixPenaltyValue(int current) {
        int parent = Memory.parent(current);
        if (parent < 0 && parent != -1) {
            parent = parent - Integer.MIN_VALUE;
            int parX = xPositions[parent];
            int parY = yPositions[parent];
            int currX = xPositions[current];
            int currY = yPositions[current];
            
            Memory.setParent(current, parent);
            Memory.setDistance(current, Memory.distance(parent) + graph.distance(parX, parY, currX, currY));
        }
    }
    
    private final void initialise(int s) {
        Memory.setDistance(s, 0);
        pq.decreaseKey(s, 0);
    }

    /**
     * Uses Strict Theta* vertex expansions
     */
    private final void tryRelax(int current, int currX, int currY, int target, int tarX, int tarY) {
        int parent = Memory.parent(current);
        
        if (parent != -1 && graph.lineOfSight(xPositions[parent], yPositions[parent], tarX, tarY)) {
            float distance = Memory.distance(parent) + graph.distance(xPositions[parent], yPositions[parent], tarX, tarY);
            if (!isTaut(target, parent)) {
                distance += PENALTY_VALUE;
                parent += Integer.MIN_VALUE;
            }
            if (distance < Memory.distance(target)) {
                Memory.setDistance(target, distance);
                Memory.setParent(target, parent);
                pq.decreaseKey(target, distance + graph.distance(tarX, tarY, ex, ey));
            }
        } else {
            float distance = Memory.distance(current) + graph.distance(currX, currY, tarX, tarY);
            if (!isTaut(target, current)) {
                distance += PENALTY_VALUE;
                parent += Integer.MIN_VALUE;
            }
            if (distance < Memory.distance(target)) {
                Memory.setDistance(target, distance);
                Memory.setParent(target, current);
                pq.decreaseKey(target, distance + graph.distance(tarX, tarY, ex, ey));
            }
        }
    }

    /**
     * Checks whether the path v, u, p=parent(u) is taut.
     */
    private final boolean isTaut(int v, int u) {
        int p = Memory.parent(u); // assert u != -1
        if (p == -1) return true;
        int x1 = xPositions[v];
        int y1 = yPositions[v];
        int x2 = xPositions[u];
        int y2 = yPositions[u];
        int x3 = xPositions[p];
        int y3 = yPositions[p];
        return graph.isTaut(x1, y1, x2, y2, x3, y3);
    }

    private final int pathNumberOfHops() {
        int length = 0;
        int current = endIndex;
        while (current != -1) {
            current = Memory.parent(current);
            length++;
        }
        return length;
    }

    @Override
    public int[][] getPath() {
        if (hasDirectPath) return new int[][] {{sx,sy},{ex,ey}};
        int length = pathNumberOfHops();
        int[][] path = new int[length][];
        int current = endIndex;
        
        int index = length-1;
        while (current != -1) {
            path[index] = new int[2];
            path[index][0] = xPositions[current];
            path[index][1] = yPositions[current];
            
            index--;
            current = Memory.parent(current);
        }
        
        return path;
    }

    @Override
    public float getPathLength() {
        return 0;
    }

    /**
     * Used by snapshot computation
     */
    @Override
    protected int goalParentIndex() {
        return endIndex;
    }
    
    private boolean subgoalGraphSnapshotSaved = false;
    @Override
    protected List<SnapshotItem> computeSearchSnapshot() {
        if (subgoalGraphSnapshotSaved) {
            return super.computeSearchSnapshot();
        }
        subgoalGraphSnapshotSaved = true;
        
        int size = subgoalGraph.size();
        List<SnapshotItem> snapshotItemList = new ArrayList<>(size);

        for (int i=0;i<size;i++) {
            int x1 = subgoalGraph.xPositions[i];
            int y1 = subgoalGraph.yPositions[i];
            
            int[] neighbours = subgoalGraph.neighbours[i];
            int nNeighbours = subgoalGraph.nNeighbours[i];
            
            for (int j=0;j<nNeighbours;++j) {
                int neighbour = neighbours[j];
                int x2 = subgoalGraph.xPositions[neighbour];
                int y2 = subgoalGraph.yPositions[neighbour];
                
                Integer[] path = new Integer[4];
                path[0] = x1;
                path[1] = y1;
                path[2] = x2;
                path[3] = y2;
                
                SnapshotItem snapshotItem = SnapshotItem.generate(path, Color.GREEN);
                snapshotItemList.add(snapshotItem);
            }
        }
        
        return snapshotItemList;
    }

    @Override
    protected Integer[] snapshotEdge(int v) {
        Integer[] edge = new Integer[4];
        int u = Memory.parent(v);
        
        edge[2] = subgoalGraph.xPositions[v];
        edge[3] = subgoalGraph.yPositions[v];
        if (u < 0) {
            edge[0] = edge[2];
            edge[1] = edge[3];
        }
        else{
            edge[0] = subgoalGraph.xPositions[u];
            edge[1] = subgoalGraph.yPositions[u];
        }
        return edge;
    }

    @Override
    protected Integer[] snapshotVertex(int index) {
        if (selected(index)) {
            Integer[] vertex = new Integer[2];
            vertex[0] = subgoalGraph.xPositions[index];
            vertex[1] = subgoalGraph.yPositions[index];
            return vertex;
        }
        return null;
    }
}
