package algorithms.sparsevgs;

import grid.GridGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import algorithms.anya.Fraction;
import algorithms.datatypes.SnapshotItem;

/**
 * Singleton. Do not make multiple simultaneous copies of this class or use in parallel code.
 */
public class LineOfSightScanner {
    public static ArrayList<List<SnapshotItem>> snapshotList = new ArrayList<>();
    private static ArrayList<SnapshotItem> snapshots = new ArrayList<>();
    
    private GridGraph graph;
    private int sizeX;
    private int sizeY;
    
    private static int[][] rightDownExtents;
    private static int[][] leftDownExtents;
    private static LOSInterval[] intervalStack;
    private static int intervalStackSize;
    
    public static int[] successorsX;
    public static int[] successorsY;
    public static int nSuccessors;

    private static void initialiseExtents(GridGraph graph) {
        // Don't reinitialise if graph is the same size as the last time.
        if (rightDownExtents != null && graph.sizeY+2 == rightDownExtents.length && graph.sizeX+1 == rightDownExtents[0].length) return;

        rightDownExtents = new int[graph.sizeY+2][];
        leftDownExtents = new int[graph.sizeY+2][];
        for (int y=0;y<graph.sizeY+2;++y) {
            rightDownExtents[y] = new int[graph.sizeX+1];
            leftDownExtents[y] = new int[graph.sizeX+1];
        }
    }
    
    private static void initialiseStack() {
        if (intervalStack != null) return;
        intervalStack = new LOSInterval[11];
        intervalStackSize = 0;
    }
    
    private static void initialiseSuccessorList() {
        if (successorsX != null) return;
        successorsX = new int[11];
        successorsY = new int[11];
        nSuccessors = 0;
    }
    
    private static void clearSuccessors() {
        nSuccessors = 0;
    }
    
    private static void stackPush(LOSInterval interval) {
        if (intervalStackSize >= intervalStack.length) {
            intervalStack = Arrays.copyOf(intervalStack, intervalStack.length*2);
        }
        intervalStack[intervalStackSize] = interval;
        ++intervalStackSize;
        
        //addToSnapshot(interval); // Uncomment for debugging.
    }
    
    private static void addToSnapshot(LOSInterval interval) {
        snapshots.add(SnapshotItem.generate(new Integer[]{interval.y, interval.xL.n, interval.xL.d, interval.xR.n, interval.xR.d}, Color.GREEN));
        snapshotList.add(new ArrayList<SnapshotItem>(snapshots));
    }
    
    public static void clearSnapshots() {
        snapshotList.clear();
        snapshots.clear();
    }
    
    private static LOSInterval stackPop() {
        LOSInterval temp = intervalStack[intervalStackSize-1];
        --intervalStackSize;
        intervalStack[intervalStackSize] = null;
        return temp;
    }
    
    private static void clearStack() {
        intervalStackSize = 0;
    }
    
    private static void addSuccessor(int x, int y) {
        if (nSuccessors >= successorsX.length) {
            successorsX = Arrays.copyOf(successorsX, successorsX.length*2);
            successorsY = Arrays.copyOf(successorsY, successorsY.length*2);
        }
        successorsX[nSuccessors] = x;
        successorsY[nSuccessors] = y;
        ++nSuccessors;
    }
    
    public LineOfSightScanner(GridGraph gridGraph) {
        initialiseExtents(gridGraph);
        initialiseSuccessorList();
        initialiseStack();
        
        graph = gridGraph;
        sizeX = graph.sizeX;
        sizeY = graph.sizeY;
        computeExtents();
    }
    
    private void computeExtents() {
        // graph.isBlocked(x,y) is the same as graph.bottomLeftOfBlockedTile(x,y)
        LineOfSightScanner.initialiseExtents(graph);
        
        for (int y=0;y<sizeY+2;++y) {
            boolean lastIsBlocked = true;
            int lastX = -1;
            for (int x=0;x<=sizeX;++x) {
                leftDownExtents[y][x] = lastX; 
                if (graph.isBlocked(x, y-1) != lastIsBlocked) {
                    lastX = x;
                    lastIsBlocked = !lastIsBlocked;
                }
            }
            lastIsBlocked = true;
            lastX = sizeX+1;
            for (int x=sizeX;x>=0;--x) {
                rightDownExtents[y][x] = lastX; 
                if (graph.isBlocked(x-1, y-1) != lastIsBlocked) {
                    lastX = x;
                    lastIsBlocked = !lastIsBlocked;
                }
            }
        }
    }
    
    /**
     * Stores results in successorsX, successorsY and nSuccessors. 
     */
    public void computeAllVisibleTautSuccessors(int sx, int sy) {
        clearSuccessors();
        clearStack();

        generateStartingStates(sx, sy);
        exploreStates(sx, sy);
    }
    

    private void generateStartingStates(int sx, int sy) {
        boolean bottomLeftOfBlocked = graph.bottomLeftOfBlockedTile(sx, sy);
        boolean bottomRightOfBlocked = graph.bottomRightOfBlockedTile(sx, sy);
        boolean topLeftOfBlocked = graph.topLeftOfBlockedTile(sx, sy);
        boolean topRightOfBlocked = graph.topRightOfBlockedTile(sx, sy);
        
        // Generate up
        if (!bottomLeftOfBlocked || !bottomRightOfBlocked) {
            Fraction leftExtent, rightExtent;
            
            if (bottomLeftOfBlocked) {
                // Explore up-left
                leftExtent = new Fraction(leftUpExtent(sx, sy));
                rightExtent = new Fraction(sx);
            } else if (bottomRightOfBlocked) {
                // Explore up-right
                leftExtent = new Fraction(sx);
                rightExtent = new Fraction(rightUpExtent(sx, sy));
            } else {
                // Explore up-left-right
                leftExtent = new Fraction(leftUpExtent(sx, sy));
                rightExtent = new Fraction(rightUpExtent(sx, sy));
            }

            this.generateUpwards(leftExtent, rightExtent, sx, sy, sy);
        }

        // Generate down
        if (!topLeftOfBlocked || !topRightOfBlocked) {
            Fraction leftExtent, rightExtent;
            
            if (topLeftOfBlocked) {
                // Explore down-left
                leftExtent = new Fraction(leftDownExtent(sx, sy));
                rightExtent = new Fraction(sx);
            } else if (topRightOfBlocked) {
                // Explore down-right
                leftExtent = new Fraction(sx);
                rightExtent = new Fraction(rightDownExtent(sx, sy));
            } else {
                // Explore down-left-right
                leftExtent = new Fraction(leftDownExtent(sx, sy));
                rightExtent = new Fraction(rightDownExtent(sx, sy));
            }

            this.generateDownwards(leftExtent, rightExtent, sx, sy, sy);
        }

        // Search leftwards
        if (!topRightOfBlocked || !bottomRightOfBlocked) {
            int x = sx;
            int y = sy;
            while (true) {
                x = leftAnyExtent(x,y);
                if (graph.topRightOfBlockedTile(x, y) && graph.bottomRightOfBlockedTile(x, y)) break;
                if (graph.topLeftOfBlockedTile(x, y) || graph.bottomLeftOfBlockedTile(x, y)) addSuccessor(x,y);
            }
        }

        // Search rightwards
        if (!topLeftOfBlocked || !bottomLeftOfBlocked) {
            int x = sx;
            int y = sy;
            while (true) {
                x = rightAnyExtent(x,y);
                if (graph.topLeftOfBlockedTile(x, y) && graph.bottomLeftOfBlockedTile(x, y)) break;
                if (graph.topRightOfBlockedTile(x, y) || graph.bottomRightOfBlockedTile(x, y)) addSuccessor(x,y);
            }
        }
    }
    
    private void exploreStates(int sx, int sy) {
        while (intervalStackSize > 0) {
            LOSInterval currState = stackPop();

            boolean zeroLengthInterval = currState.xR.isEqualTo(currState.xL);
            boolean leftSideAdded = false;
            
            if (currState.y > sy) {
                // Upwards
                
                // Insert endpoints if integer.
                if (currState.xL.isWholeNumber()) {
                    /* The two cases   _
                     *  _             |X|
                     * |X|'.           ,'
                     *      '.       ,'
                     *        B     B
                     */
                    
                    int x = currState.xL.n;
                    int y = currState.y;
                    if (x <= sx && graph.topRightOfBlockedTile(x, y) && !graph.bottomRightOfBlockedTile(x, y)) {
                        addSuccessor(x, y);
                        leftSideAdded = true;
                    }
                    else if (sx < x && graph.bottomRightOfBlockedTile(x, y)) {
                        addSuccessor(x, y);
                        leftSideAdded = true;
                    }
                }
                if (currState.xR.isWholeNumber()) {
                    /*   _   The two cases
                     *  |X|             _
                     *  '.           ,'|X|
                     *    '.       ,'
                     *      B     B
                     */
                    
                    int x = currState.xR.n;
                    int y = currState.y;
                    if (x < sx && graph.bottomLeftOfBlockedTile(x, y)) {
                        if (!zeroLengthInterval || !leftSideAdded) addSuccessor(x, y);
                    }
                    else if (sx <= x && graph.topLeftOfBlockedTile(x, y)  && !graph.bottomLeftOfBlockedTile(x, y)) {
                        if (!zeroLengthInterval || !leftSideAdded) addSuccessor(x, y);
                    }
                }
                
                
                
                // Generate Upwards
                /*
                 * =======      =====    =====
                 *  \   /       / .'      '. \
                 *   \ /   OR  /.'    OR    '.\
                 *    B       B                B
                 */

                // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
                int dy = currState.y - sy; 
                Fraction leftProjection = currState.xL.minus(sx).multiplyDivide(dy+1, dy).plus(sx);
                
                int leftBound = leftUpExtent(currState.xL.floor()+1, currState.y);
                if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
                    leftProjection = new Fraction(leftBound);
                }

                // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
                Fraction rightProjection = currState.xR.minus(sx).multiplyDivide(dy+1, dy).plus(sx);
                
                int rightBound = rightUpExtent(currState.xR.ceil()-1, currState.y);
                if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
                    rightProjection = new Fraction(rightBound);
                }

                if (leftProjection.isLessThanOrEqual(rightProjection)) {
                    generateUpwards(leftProjection, rightProjection, sx, sy, currState.y);
                }
            }
            else {
                // Upwards
                
                // Insert endpoints if integer.
                if (currState.xL.isWholeNumber()) {
                    /* The two cases
                     *        B     B
                     *  _   ,'       '.
                     * |X|.'           '.
                     *                |X|
                     */
                    
                    int x = currState.xL.n;
                    int y = currState.y;
                    if (x <= sx && graph.bottomRightOfBlockedTile(x, y) && !graph.topRightOfBlockedTile(x, y)) {
                        addSuccessor(x, y);
                        leftSideAdded = true;
                    }
                    else if (sx < x && graph.topRightOfBlockedTile(x, y)) {
                        addSuccessor(x, y);
                        leftSideAdded = true;
                    }
                }
                if (currState.xR.isWholeNumber()) {
                    /*       The two cases
                     *      B     B
                     *    .'       '.   _
                     *  .'           '.|X|
                     *  |X|
                     */
                    
                    int x = currState.xR.n;
                    int y = currState.y;
                    if (x < sx && graph.topLeftOfBlockedTile(x, y)) {
                        if (!zeroLengthInterval || !leftSideAdded) addSuccessor(x, y);
                    }
                    else if (sx <= x && graph.bottomLeftOfBlockedTile(x, y)  && !graph.topLeftOfBlockedTile(x, y)) {
                        if (!zeroLengthInterval || !leftSideAdded) addSuccessor(x, y);
                    }
                }
                
                
                
                // Generate downwards
                /*
                 *    B       B                B
                 *   / \   OR  \'.    OR    .'/
                 *  /   \       \ '.      .' /
                 * =======      =====    =====
                 */

                // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
                int dy = sy - currState.y; 
                Fraction leftProjection = currState.xL.minus(sx).multiplyDivide(dy+1, dy).plus(sx);
                
                int leftBound = leftDownExtent(currState.xL.floor()+1, currState.y);
                if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
                    leftProjection = new Fraction(leftBound);
                }

                // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
                Fraction rightProjection = currState.xR.minus(sx).multiplyDivide(dy+1, dy).plus(sx);
                
                int rightBound = rightDownExtent(currState.xR.ceil()-1, currState.y);
                if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
                    rightProjection = new Fraction(rightBound);
                }
                
                if (leftProjection.isLessThanOrEqual(rightProjection)) {
                    generateDownwards(leftProjection, rightProjection, sx, sy, currState.y);
                }
            }
            
            
        }
    }

    private int leftUpExtent(int xL, int y) {
        return xL > sizeX ? sizeX : leftDownExtents[y+1][xL];
    }

    private int leftDownExtent(int xL, int y) {
        return xL > sizeX ? sizeX : leftDownExtents[y][xL];
    }
    
    private int leftAnyExtent(int xL, int y) {
        return Math.max(leftDownExtents[y][xL], leftDownExtents[y+1][xL]);
    }

    private int rightUpExtent(int xR, int y) {
        return xR < 0 ? 0 : rightDownExtents[y+1][xR];
    }

    private int rightDownExtent(int xR, int y) {
        return xR < 0 ? 0 : rightDownExtents[y][xR];
    }

    private int rightAnyExtent(int xR, int y) {
        return Math.min(rightDownExtents[y][xR], rightDownExtents[y+1][xR]);
    }

    private void generateUpwards(Fraction leftBound, Fraction rightBound, int sx, int sy, int currY) {
        generateAndSplitIntervals(
                currY + 2, currY + 1,
                sx, sy,
                leftBound, rightBound);
    }

    private void generateDownwards(Fraction leftBound, Fraction rightBound, int sx, int sy, int currY) {
        generateAndSplitIntervals(
                currY - 1, currY - 1,
                sx, sy,
                leftBound, rightBound);
    }
    
    /**
     * Called by generateUpwards / Downwards.
     * Note: Unlike Anya, 0-length intervals are possible.
     */
    private void generateAndSplitIntervals(int checkY, int newY, int sx, int sy, Fraction leftBound, Fraction rightBound) {
        Fraction left = leftBound;
        int leftFloor = left.floor();

        // Up: !bottomRightOfBlockedTile && bottomLeftOfBlockedTile
        if (left.isWholeNumber() && !graph.isBlocked(leftFloor-1, checkY-1) && graph.isBlocked(leftFloor, checkY-1)) {
            stackPush(new LOSInterval(newY, left, left));
        }

        // Divide up the intervals.
        while(true) {
            int right = rightDownExtents[checkY][leftFloor]; // it's actually rightDownExtents for exploreDownwards. (thus we use checkY = currY - 2)
            if (rightBound.isLessThan(right)) break; // right <= rightBound            
            
            // Only push unblocked ( bottomRightOfBlockedTile )
            if (!graph.isBlocked(right-1, checkY-1)) {
                stackPush(new LOSInterval(newY, left, new Fraction(right)));
            }
            
            leftFloor = right;
            left = new Fraction(leftFloor);
        }

        // right > rightBound
        // if !bottomLeftOfBlockedTile(leftFloor, checkY)
        if (!graph.isBlocked(leftFloor, checkY-1)) {
            stackPush(new LOSInterval(newY, left, rightBound));
        }
    }
    
}



class LOSInterval {
    final int y;
    final Fraction xL;
    final Fraction xR;
    
    public LOSInterval(int y, Fraction xL, Fraction xR) {
        this.y = y;
        this.xL = xL;
        this.xR = xR;
    }
    
    @Override
    public final String toString() {
        return (y + " [" + xL + ", " + xR + "]");
    }
}