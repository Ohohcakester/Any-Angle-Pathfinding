package algorithms.subgoalgraphs;

import grid.GridGraph;

import java.util.Arrays;

import algorithms.datatypes.Memory;

public class HReachabilityChecker {
    private final Memory.Context memoryContext = new Memory.Context();
    private final int[] xPositions;
    private final int[] yPositions;
    private final GridGraph graph;
    private final int size;
    
    private int[] queue;
    private int qSize;
    
    public HReachabilityChecker(int[] xPositions, int[] yPositions, GridGraph graph) {
        this.xPositions = xPositions;
        this.yPositions = yPositions;
        this.graph = graph;
        this.size = (graph.sizeX+1)*(graph.sizeY+1);
        this.queue = new int[11];
    }


    private static final int sign(int value) {
        return ( (value > 0) ? 1 : ((value < 0) ? -1 : 0) );
    }
    
    /**
     * Does a DFS search to find whether n1 is h-reachable from n2.
     * Note: uses the Memory class.
     * This function is horrible and I hate it.
     */
    public boolean isHReachable(int n1, int n2) {
        Memory.initialise(size, 0, 0, false);
        int x1 = xPositions[n1];
        int y1 = yPositions[n1];
        int x2 = xPositions[n2];
        int y2 = yPositions[n2];
        
        // Direction 1 (diagonal)
        int absdx = Math.abs(x1-x2);
        int absdy = Math.abs(y1-y2);
        int dx = sign(x2-x1);
        int dy = sign(y2-y1);

        int curr = 0;
        queue[0] = x1;
        queue[1] = y1;
        qSize = 2;
        
        if (absdx == 0 || absdy == 0 || (absdx == absdy)) return graph.lineOfSight(x1, y1, x2, y2);
        
        int c_diagX, c_diagY, c_carX, c_carY; // obstacle to check for the diagonal and cardinal directions respectively.
        /**
         *  dx dy  c_diagX, c_diagY
         *   1  1     0        0
         *   1 -1     0       -1
         *  -1  1    -1        0
         *  -1 -1    -1       -1
         */
        int car_dx, car_dy;
        c_diagX = (dx > 0) ? 0 : -1;
        c_diagY = (dy > 0) ? 0 : -1;
        
        int maxCardinal;
        
        if (absdx < absdy) {
            // Directions: (dx,dy) and (0,dy)
            car_dx = 0;
            car_dy = dy;
            
            c_carX = (dx < 0) ? 0 : -1;
            c_carY = c_diagY;
            //maxCardinal = absdy;
            maxCardinal = absdy - absdx;
        } else {
            // Directions: (dx,dy) and (dx,0)
            car_dx = dx;
            car_dy = 0;
            
            c_carX = c_diagX;
            c_carY = (dy < 0) ? 0 : -1;
            maxCardinal = absdx - absdy;
            //maxCardinal = absdx;
        }
        
        while(curr < qSize) {
            int x = queue[curr++];
            int y = queue[curr++];
            
            if (x == x2 && y == y2) return true;
            boolean diagonalBlocked = graph.isBlocked(x + c_diagX, y + c_diagY);
            boolean cardinalBlocked = diagonalBlocked && graph.isBlocked(x + c_carX, y + c_carY);
            
            if (!diagonalBlocked && (x != x2 && y != y2)) {
                addToQueue(x+dx, y+dy);
            }
            
            if (!cardinalBlocked && (Math.abs(dx*(x-x1) - dy*(y-y1)) != maxCardinal)) {
                addToQueue(x+car_dx, y+car_dy);
            }
        }
        return false;
    }

    private final void addToQueue(int x, int y) {
        int index = graph.toOneDimIndex(x, y);
        if (Memory.visited(index)) return;
        if (qSize+1 >= queue.length) queue = Arrays.copyOf(queue, queue.length*2);
        queue[qSize++] = x;
        queue[qSize++] = y;
        Memory.setVisited(index, true);
    }

    private final void addToQueue(int index, int x, int y) {
        if (Memory.visited(index)) return;
        if (qSize+1 >= queue.length) queue = Arrays.copyOf(queue, queue.length*2);
        queue[qSize++] = x;
        queue[qSize++] = y;
        Memory.setVisited(index, true);
    }
    

    /**
     * Does a DFS search to compute an octile path from n1 to n2.
     * n2 is assumed to be h-reachable from n1.
     * Note: uses the Memory class.
     * This function is horrible and I hate it.
     * Appends the vertices to the path array. Returns the new size.
     */
    public int appendHReachablePath(int n1, int n2, int[][] path, int length) {
        int x1 = xPositions[n1];
        int y1 = yPositions[n1];
        int x2 = xPositions[n2];
        int y2 = yPositions[n2];
        
        // Direction 1 (diagonal)
        int absdx = Math.abs(x1-x2);
        int absdy = Math.abs(y1-y2);
        int dx = sign(x2-x1);
        int dy = sign(y2-y1);

        int curr = 0;
        queue[0] = x1;
        queue[1] = y1;
        qSize = 2;
        
        if (absdx == 0 || absdy == 0 || (absdx == absdy)) {
            path[length] = new int[]{x2,y2};
            return length+1;
        }
        
        int c_diagX, c_diagY, c_carX, c_carY; // obstacle to check for the diagonal and cardinal directions respectively.
        /**
         *  dx dy  c_diagX, c_diagY
         *   1  1     0        0
         *   1 -1     0       -1
         *  -1  1    -1        0
         *  -1 -1    -1       -1
         */
        int car_dx, car_dy;
        c_diagX = (dx > 0) ? 0 : -1;
        c_diagY = (dy > 0) ? 0 : -1;
        
        int maxCardinal;
        int nAppendedElements;
        
        if (absdx < absdy) {
            // Directions: (dx,dy) and (0,dy)
            car_dx = 0;
            car_dy = dy;
            
            c_carX = (dx < 0) ? 0 : -1;
            c_carY = c_diagY;
            maxCardinal = absdy - absdx;
            nAppendedElements = absdy;
        } else {
            // Directions: (dx,dy) and (dx,0)
            car_dx = dx;
            car_dy = 0;
            
            c_carX = c_diagX;
            c_carY = (dy < 0) ? 0 : -1;
            maxCardinal = absdx - absdy;
            nAppendedElements = absdx;
        }

        Memory.Context temp = new Memory.Context();
        Memory.saveContext(temp);
        Memory.loadContext(memoryContext);
        Memory.initialise(size, 0, 0, false);
        while(curr < qSize) {
            int x = queue[curr++];
            int y = queue[curr++];
            
            if (x == x2 && y == y2) break;
            boolean diagonalBlocked = graph.isBlocked(x + c_diagX, y + c_diagY);
            boolean cardinalBlocked = diagonalBlocked && graph.isBlocked(x + c_carX, y + c_carY);
            int currIndex = graph.toOneDimIndex(x, y);
            
            if (!diagonalBlocked && (x != x2 && y != y2)) {
                int nextIndex = graph.toOneDimIndex(x+dx, y+dy);
                addToQueue(nextIndex, x+dx, y+dy);
                Memory.setParent(nextIndex, currIndex);
            }
            
            if (!cardinalBlocked && (Math.abs(dx*(x-x1) - dy*(y-y1)) != maxCardinal)) {
                int nextIndex = graph.toOneDimIndex(x+car_dx, y+car_dy);
                addToQueue(nextIndex, x+car_dx, y+car_dy);
                Memory.setParent(nextIndex, currIndex);
            }
        }

        int newSize = length + nAppendedElements;
        int currIndex = graph.toOneDimIndex(x2, y2);
        for (int i = newSize - 1; i >= length; --i) {
            path[i] = new int[]{graph.toTwoDimX(currIndex), graph.toTwoDimY(currIndex)};
            currIndex = Memory.parent(currIndex);
        }
        
        Memory.saveContext(memoryContext);
        Memory.loadContext(temp);
        return newSize;
    }
}
