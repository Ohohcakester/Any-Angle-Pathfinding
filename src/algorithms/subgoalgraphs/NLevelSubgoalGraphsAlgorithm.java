package algorithms.subgoalgraphs;

import grid.GridGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import algorithms.PathFindingAlgorithm;
import algorithms.datatypes.Memory;
import algorithms.datatypes.SnapshotItem;
import algorithms.priorityqueue.ReusableIndirectHeap;

public class NLevelSubgoalGraphsAlgorithm extends PathFindingAlgorithm {
    NLevelSubgoalGraph subgoalGraph;
    ReusableIndirectHeap pq;
    int startIndex;
    int endIndex;
    int[][] path;

    public NLevelSubgoalGraphsAlgorithm(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey);
    }


    @Override
    public void computePath() {
        //NLevelSubgoalGraph.clearMemory();
        //NLevelSubgoalGraph.snapshotFunction = ()->maybeSaveSearchSnapshot();
        subgoalGraph = NLevelSubgoalGraph.initialiseNew(graph, 9999);
        //subgoalGraph.initialiseGraph();
        if (tryDirectPath()) {
            return;
        }
        subgoalGraph.addStartAndEnd(sx, sy, ex, ey);
        maybeSaveSearchSnapshot();
        maybeSaveSearchSnapshot();

        int size = (sizeX+1)*(sizeY+1);
        
        startIndex = subgoalGraph.startIndex;
        endIndex = subgoalGraph.endIndex;
        
        initialiseMemory(size, Float.POSITIVE_INFINITY, -1, false);
        
        pq = new ReusableIndirectHeap(subgoalGraph.size(), subgoalGraph.maxSize());
        initialise(startIndex);
        
        int[] xPositions = subgoalGraph.xPositions;
        int[] yPositions = subgoalGraph.yPositions;
        boolean[] isRelevantSubgoal = subgoalGraph.isRelevantSubgoal;

        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            Memory.setVisited(current, true);
            
            if (current == endIndex) break;

            int currX = xPositions[current];
            int currY = yPositions[current];
            //System.out.println("POP " + current + " : " + currX + ", " + currY);

            int[] neighbours = subgoalGraph.neighbours[current];
            int nNeighbours = subgoalGraph.nNeighbours[current];
            
            for (int i=0;i<nNeighbours;++i) {
                int target = neighbours[i];
                if (!isRelevantSubgoal[target] || Memory.visited(target)) continue;

                int targetX = xPositions[target];
                int targetY = yPositions[target];

                tryRelax(current, currX, currY, target, targetX, targetY);
            }
            if (subgoalGraph.hasEdgeToGoal[current]) {
                tryRelax(current, currX, currY, endIndex, ex, ey);
            }

            maybeSaveSearchSnapshot();
        }

        computeOctilePath();
        subgoalGraph.restoreOriginalGraph();
    }

    private final boolean tryDirectPath() {
        int absdx = Math.abs(ex - sx);
        int absdy = Math.abs(ey - sy);
        int midX = -1, midY = -1;
        boolean hasMidpoint = false;

        if (absdy > absdx) {
            if (absdx != 0) {
                midX = sx;
                midY = (ey > sy) ? (ey-absdx) : (ey+absdx);
                hasMidpoint = true;
            }
        } else if (absdy < absdx) {
            if (absdy != 0) {
                midX = (ex > sx) ? (ex-absdy) : (ex+absdy);
                midY = sy;
                hasMidpoint = true;
            }
        }

        if (hasMidpoint) {
            // Two-step octile path.
            if (graph.lineOfSight(sx, sy, midX, midY) && graph.lineOfSight(midX, midY, ex, ey)) {
                path = new int[3][];
                path[0] = new int[]{sx,sy};
                path[1] = new int[]{midX,midY};
                path[2] = new int[]{ex,ey};
                return true;
            }
            return false;
        } else {
            // Either horizontal, vertical or diagonal.
            if (graph.lineOfSight(sx, sy, ex, ey)) {
                path = new int[2][];
                path[0] = new int[]{sx,sy};
                path[1] = new int[]{ex,ey};
                return true;
            }
            return false;
        }
    }

    private void initialise(int s) {
        Memory.setDistance(s, 0);
        pq.decreaseKey(s, 0);
    }
    
    private final void computeOctilePath() {
        if (Memory.parent(endIndex) == -1) return; // no path.
        
        int maxLength = (int)(Memory.distance(endIndex)+1);
        int[][] tempPath = new int[maxLength][];
        int length = 0;
        
        int current = endIndex;
        tempPath[length++] = new int[]{ex,ey};
        
        while (current != startIndex) {
            int parent = Memory.parent(current);
            length = subgoalGraph.checker.appendHReachablePath(current, parent, tempPath, length);
            current = parent;
        }
        
        // Reverse array and shrink to correct size.
        path = new int[length][];
        for (int i=0;i<length;++i) {
            path[i] = tempPath[length-i-1];
        }
    }

    private void tryRelax(int current, int currX, int currY, int target, int tarX, int tarY) {
        float distance = Memory.distance(current) + graph.octileDistance(currX, currY, tarX, tarY);
        if (distance < Memory.distance(target)) {
            Memory.setDistance(target, distance);
            Memory.setParent(target, current);
            pq.decreaseKey(target, distance + graph.octileDistance(tarX, tarY, ex, ey));
        }
    }

    private int pathNumberOfHops() {
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
        if (path == null) return new int[0][];
        else return path;
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
    
    Color[] vertexColours = new Color[] {
        Color.BLACK,
        Color.RED,
        Color.YELLOW,
        Color.GREEN,
        Color.CYAN,
        Color.BLUE,
        Color.MAGENTA,
        Color.WHITE,
    };
    int subgoalGraphSnapshotSaved = 2;
    @Override
    protected List<SnapshotItem> computeSearchSnapshot() {
        if (subgoalGraphSnapshotSaved <= 0) {
            return super.computeSearchSnapshot();
        }
        --subgoalGraphSnapshotSaved;
        
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
                
                Color color = vertexColours[Math.min(Math.min(subgoalGraph.levels[i], subgoalGraph.levels[neighbour]), vertexColours.length-1)];
                SnapshotItem snapshotItem = SnapshotItem.generate(path, color);
                snapshotItemList.add(snapshotItem);
            }
            
            Integer[] vert = new Integer[2];
            vert[0] = x1;
            vert[1] = y1;

            SnapshotItem snapshotItem = null;
            if (subgoalGraphSnapshotSaved > 0) {
                int index = Math.min(subgoalGraph.levels[i], vertexColours.length-1);
                snapshotItem = SnapshotItem.generate(vert, vertexColours[index]);
            }
            else {
                int index = Math.min(subgoalGraph.levels[i], vertexColours.length-1);
                if (subgoalGraph.isRelevantSubgoal[i]) index = 0;
                snapshotItem = SnapshotItem.generate(vert, vertexColours[index]);
            }
            snapshotItemList.add(snapshotItem);
        }
        
        return snapshotItemList;
    }

    @Override
    protected Integer[] snapshotEdge(int v) {
        Integer[] edge = new Integer[4];
        int u = Memory.parent(v);
        edge[0] = subgoalGraph.xPositions[u];
        edge[1] = subgoalGraph.yPositions[u];
        edge[2] = subgoalGraph.xPositions[v];
        edge[3] = subgoalGraph.yPositions[v];
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
