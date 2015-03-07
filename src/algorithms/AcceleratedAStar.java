package algorithms;

import grid.GridGraph;

import java.util.ArrayList;
import java.util.List;

import algorithms.priorityqueue.IndirectHeap;

public class AcceleratedAStar extends AStar {
    private List<Integer> closed;
    private int[][] maxRanges;
    
    public AcceleratedAStar(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
        postSmoothingOn = false;
    }

    @Override
    public void computePath() {
        int totalSize = (graph.sizeX+1) * (graph.sizeY+1);

        int start = toOneDimIndex(sx, sy);
        finish = toOneDimIndex(ex, ey);
        
        distance = new Float[totalSize];
        parent = new int[totalSize];
        maxRanges = graph.computeMaxDownLeftRanges(); // O(size of gridGraph) computation. See actual method.
        
        initialise(start);
        visited = new boolean[totalSize];
        
        closed = new ArrayList<Integer>();
        
        pq = new IndirectHeap<Float>(distance, true);
        pq.heapify();
        
        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            if (current == finish || distance[current] == Float.POSITIVE_INFINITY) {
                maybeSaveSearchSnapshot();
                break;
            }
            visited[current] = true;
            closed.add(current);

            int x = toTwoDimX(current);
            int y = toTwoDimY(current);

            int maxSquare = detectMaxSquare(x, y);
            
            if (maxSquare == 0) {
                relaxSuccessorsSizeZero(current, x, y);
            } else {
                relaxSuccessors(current, x, y, maxSquare);
            }

            maybeSaveSearchSnapshot();
        }
        
        maybePostSmooth();
    }

    private void relaxSuccessorsSizeZero(int current, int x, int y) {
        boolean[] udlr = new boolean[4];
        if (!graph.isBlocked(x-1, y-1)) { // bottom left
            udlr[2] = true;
            udlr[1] = true;
        }
        if (!graph.isBlocked(x, y-1)) { // bottom right
            udlr[3] = true;
            udlr[1] = true;
        }
        if (!graph.isBlocked(x-1, y)) { // top left
            udlr[2] = true;
            udlr[0] = true;
        }
        if (!graph.isBlocked(x, y)) { // top right
            udlr[3] = true;
            udlr[0] = true;
        }
        if (udlr[0])
            generateVertex(current, x, y, x, y+1);
        if (udlr[1])
            generateVertex(current, x, y, x, y-1);
        if (udlr[2])
            generateVertex(current, x, y, x-1, y);
        if (udlr[3])
            generateVertex(current, x, y, x+1, y);
    }

    private void relaxSuccessors(int current, int x, int y, int squareSize) {
        generateVertex(current, x, y, x, y+squareSize);
        generateVertex(current, x, y, x, y-squareSize);
        generateVertex(current, x, y, x+squareSize, y);
        generateVertex(current, x, y, x-squareSize, y);
        
    }

    private void generateVertex(int current, int currentX, int currentY, int x, int y) {
        int destination = toOneDimIndex(x,y);
        if (visited[destination])
            return;
        
        boolean fValueUpdated = false;
        
        /*if (relax(current, destination, weight(currentX, currentY, x, y))) {
            // If relaxation is done.
            fValueUpdated = true;
        }*/
        
        if (processNode(current, destination, x, y)) {
            fValueUpdated = true;
        }

        if (fValueUpdated) {
            pq.decreaseKey(destination, distance[destination] + heuristic(x,y));
        }
    }
    
    private boolean processNode(int current, int destination, int destX, int destY) {
        boolean changed = false;
        for (int fromNode : closed) {
            int fromX = toTwoDimX(fromNode);
            int fromY = toTwoDimY(fromNode);
            float newFValue = distance[fromNode] + weight(fromX, fromY, destX, destY);
            if (newFValue < distance[destination]) {
                if (graph.lineOfSight(fromX, fromY, destX, destY)) {
                    distance[destination] = newFValue;
                    parent[destination] = fromNode;
                    changed = true;
                }
            }
        }
        return changed;
    }

    /**
     * <pre>
     * returns the size of the max square at (x,y). can possibly return 0.
     * 1: XX
     *    XX
     * 
     * 2: XXX
     *    XXX
     *    XXX
     * </pre>
     */
    private int detectMaxSquare(int x, int y) {
        // This is the newer, O(n) method.
        int lower = 0;
        int upper = getMaxSize(x,y);
        int newUpper;
        int i = x-y+sizeY;
        int j = Math.min(x, y);
        if (upper <= lower) return 0;

        while (true) {
            newUpper = checkUpperBoundNew(i,j,lower);
            if (newUpper < upper) upper = newUpper;
            if (upper <= lower) break;

            newUpper = checkUpperBoundNew(i,j,-1-lower);
            if (newUpper < upper) upper = newUpper;
            if (upper <= lower) break;
            
            lower++;
            if (upper <= lower) break;
        }
        return lower;
    }
    
    private int detectMaxSquareOld(int x, int y) {
        // This is the older, O(n^2) method.
        int size = 1;
        int maxSizePlusOne = getMaxSize(x, y) + 1;
        while (size < maxSizePlusOne && !hasBlockedTileOnPerimeter(x, y, size)) {
            size++;
        }
        return size-1;
    }
    
    /**
     * <pre>
     *          _______  This function returns the upper bound detected by
     *         |   |k=1| the a leftward and downward search.
     *         |___|___| k is the number of steps moved in the up-right direction.
     *         |k=0|   | k = 0 the the square directly top-right of grid point (x,y).
     *  _______.___|___|
     * |   |-1 |(x,y)    
     * |___|___|  point of concern
     * |-2 |   |
     * |___|___|
     * </pre>
     */
    private int checkUpperBoundNew(int i, int j, int k) {
        return maxRanges[i][j + k] - k;
    }
    
    /**
     * Compares the tile with the end point to set an upper bound on the size.
     */
    private int getMaxSize(int x, int y) {
        return Math.max(Math.abs(x-ex), Math.abs(y-ey));
    }
    
    /**
     *  ___________
     * |X|X|X|X|X|X| size = 3
     * |X|_|_|_|_|X|
     * |X|_|_|_|_|X| <-- checks the nodes in the perimeter of size = size
     * |X|_|_|_|_|X|     returns true iff all of them are unblocked.
     * |X|_|_|_|_|X|
     * |X|X|X|X|X|X|
     */
    private boolean hasBlockedTileOnPerimeter(int x, int y, int size) {
        int leftX = x-size;
        int rightX = x+size-1;
        int downY = y-size;
        int upY = y+size-1;
        
        for (int i=leftX; i<=rightX; i++) {
            if (graph.isBlocked(i, upY)) {
                return true;
            }
            if (graph.isBlocked(i, downY)) {
                return true;
            }
        }
        for (int i=downY+1; i<upY; i++) {
            if (graph.isBlocked(leftX, i)) {
                return true;
            }
            if (graph.isBlocked(rightX, i)) {
                return true;
            }
        }
        return false;
    }
    
}
