package algorithms;

import grid.GridGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import algorithms.anya.Fraction;
import algorithms.datatypes.Point;
import algorithms.datatypes.SnapshotItem;
import algorithms.priorityqueue.FastVariableSizeIndirectHeap;

public class Anya extends PathFindingAlgorithm {

    private AnyaState goalState;
    private AnyaState[] states;
    private FastVariableSizeIndirectHeap pq;
    private HashMap<AnyaState, Integer> existingStates;

    private static int[][] rightDownExtents;
    private static int[][] leftDownExtents;
    
    public static void initialiseUpExtents(GridGraph graph) {
        // Don't reinitialise if graph is the same size as the last time.
        if (rightDownExtents != null && graph.sizeY+2 == rightDownExtents.length && graph.sizeX+1 == rightDownExtents[0].length) return;

        rightDownExtents = new int[graph.sizeY+2][];
        leftDownExtents = new int[graph.sizeY+2][];
        for (int y=0;y<graph.sizeY+2;++y) {
            rightDownExtents[y] = new int[graph.sizeX+1];
            leftDownExtents[y] = new int[graph.sizeX+1];
        }
    }
    
    public Anya(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey);
    }

    @Override
    public void computePath() {
        existingStates = new HashMap<>();
        pq = new FastVariableSizeIndirectHeap();
        states = new AnyaState[11];
        goalState = null;
        
        computeExtents();
        generateStartingStates();
        
        while (!pq.isEmpty()) {
            maybeSaveSearchSnapshot();
            int currentID = pq.popMinIndex();
            AnyaState currState = states[currentID];
            currState.visited = true;
            
            //System.out.println("Explore " + currState + " :: " + currState.fValue);
            // Check if goal state.
            if (currState.y == ey && currState.xL.isLessThanOrEqual(ex) && !currState.xR.isLessThan(ex)) {
                goalState = currState;
                break;
            }

            generateSuccessors(currState);
        }
    }
    
    private void generateStartingStates() {
        boolean bottomLeftOfBlocked = graph.bottomLeftOfBlockedTile(sx, sy);
        boolean bottomRightOfBlocked = graph.bottomRightOfBlockedTile(sx, sy);
        boolean topLeftOfBlocked = graph.topLeftOfBlockedTile(sx, sy);
        boolean topRightOfBlocked = graph.topRightOfBlockedTile(sx, sy);
        
        Point start = new Point(sx,sy);
        
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

            this.generateUpwardsStart(leftExtent, rightExtent, start);
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

            this.generateDownwardsStart(leftExtent, rightExtent, start);
        }

        // Generate left
        if (!topRightOfBlocked || !bottomRightOfBlocked) {
            this.generateSameLevelStart(start, leftAnyExtent(sx,sy), sx);
        }

        // Generate right
        if (!topLeftOfBlocked || !bottomLeftOfBlocked) {
            this.generateSameLevelStart(start, sx, rightAnyExtent(sx,sy));
        }
    }

    private void addSuccessor(AnyaState source, AnyaState successor) {
        Integer existingHandle = existingStates.get(successor);
        if (existingHandle == null) {
            addToOpen(successor);
        } else {
            relaxExisting(source, successor, existingHandle);
        }
        //maybeSaveSearchSnapshot();
    }
    
    private void addToOpen(AnyaState successor) {
        // set heuristic and f-value
        successor.hValue = heuristic(successor);
        successor.fValue = successor.gValue + successor.hValue;
        
        int handle = pq.insert(successor.fValue);
        if (handle >= states.length) {
            states = Arrays.copyOf(states, states.length*2);
        }
        states[handle] = successor;
        existingStates.put(successor, handle);

        //System.out.println("Generate " + successor + " -> " + handle);
    }
    
    private void relaxExisting(AnyaState source, AnyaState successorCopy, int existingHandle) {
        AnyaState successor = states[existingHandle];
        if (successor.visited) return;
        
        int dx = successor.basePoint.x - source.basePoint.x;
        int dy = successor.basePoint.y - source.basePoint.y;
        float newgValue = source.gValue + (float)Math.sqrt(dx*dx+dy*dy);
        
        if (newgValue < successor.gValue) {
            successor.gValue = newgValue;
            successor.fValue = newgValue + successor.hValue;
            successor.parent = successorCopy.parent;
            pq.decreaseKey(existingHandle, successor.fValue);
            
            //System.out.println("Relax " + successor + " : " + successor.fValue);
        }
        //else System.out.println("Failed to relax " + successor + ": " + successor.fValue);
    }
    
    
    private void computeExtents() {
        // graph.isBlocked(x,y) is the same as graph.bottomLeftOfBlockedTile(x,y)
        Anya.initialiseUpExtents(graph);
        
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
    
    
    
    /// === GENERATE SUCCESSORS - PATTERNS - START ===
    
    private void generateSuccessors(AnyaState currState) {
        Point basePoint = currState.basePoint;
        
        if (basePoint.y == currState.y) {
            exploreFromSameLevel(currState, basePoint);
        } else if (basePoint.y < currState.y) {
            explorefromBelow(currState, basePoint);
        } else {
            explorefromAbove(currState, basePoint);
        }
    }
    
    private void exploreFromSameLevel(AnyaState currState, Point basePoint) {
        // Note: basePoint.y == currState.y
        // Note: basePoint == currState.basePoint
        // Property 1: basePoint is not strictly between the two endpoints of the interval.
        // Property 2: the endpoints of the interval are integers. 

        assert basePoint.y == currState.y;
        assert currState.xL.isWholeNumber();
        assert currState.xR.isWholeNumber();
        
        int y = basePoint.y;
        
        if (currState.xR.n <= basePoint.x) { // currState.xR <= point.x  (explore left)
            int xL = currState.xL.n;
            if (graph.bottomLeftOfBlockedTile(xL, y)) {
                if (!graph.bottomRightOfBlockedTile(xL, y)) {
                    /* ----- |XXXXXXXX|
                     *       |XXXXXXXX|
                     * ----- P========B
                     */
                    Fraction leftBound = new Fraction(leftUpExtent(xL, y)); 
                    generateUpwardsUnobservable(new Point(xL,y), leftBound, currState.xL, currState);
                }
            } else if (graph.topLeftOfBlockedTile(xL, y)) {
                if (!graph.topRightOfBlockedTile(xL, y)) {
                    /* ----- P========B
                     *       |XXXXXXXX|
                     * ----- |XXXXXXXX|
                     */
                    Fraction leftBound = new Fraction(leftDownExtent(xL, y)); 
                    generateDownwardsUnobservable(new Point(xL,y), leftBound, currState.xL, currState);
                }
            }
            
            if (!graph.bottomRightOfBlockedTile(xL, y) || !graph.topRightOfBlockedTile(xL, y)) {
                int leftBound = leftAnyExtent(xL, y);
                generateSameLevelObservable(leftBound, xL, currState);
            }
            
        } else { // point.x <= currState.xL  (explore right)
            assert basePoint.x <= currState.xL.n;

            int xR = currState.xR.n;
            if (graph.bottomRightOfBlockedTile(xR, y)) {
                if (!graph.bottomLeftOfBlockedTile(xR, y)) {
                    /*  |XXXXXXXX| -----
                     *  |XXXXXXXX|
                     *  B========P -----
                     */
                    Fraction rightBound = new Fraction(rightUpExtent(xR, y)); 
                    generateUpwardsUnobservable(new Point(xR,y), currState.xR, rightBound, currState);
                }
            } else if (graph.topRightOfBlockedTile(xR, y)) {
                if (!graph.topLeftOfBlockedTile(xR, y)) {
                    /*  B========P -----
                     *  |XXXXXXXX|
                     *  |XXXXXXXX| -----
                     */
                    Fraction rightBound = new Fraction(rightDownExtent(xR, y)); 
                    generateDownwardsUnobservable(new Point(xR,y), currState.xR, rightBound, currState);
                }
            }
            
            if (!graph.bottomLeftOfBlockedTile(xR, y) || !graph.topLeftOfBlockedTile(xR, y)) {
                int rightBound = rightAnyExtent(xR, y);
                generateSameLevelObservable(xR, rightBound, currState);
            }
            
        }
    }
    


    private void explorefromBelow(AnyaState currState, Point basePoint) {
        // Note: basePoint.y < currState.y
        // Note: basePoint == currState.basePoint

        assert basePoint.y < currState.y;
        
        if (graph.bottomLeftOfBlockedTile(currState.xL.floor(), currState.y)) {
            // Is Blocked Above
            if (currState.xL.isWholeNumber()) {
                int xL = currState.xL.n;
                if (xL < basePoint.x && !graph.bottomRightOfBlockedTile(xL, currState.y)) {
                    /* 
                     * .-----|XXXXXXX
                     *  '.   |XXXXXXXX
                     *    '. |XXXXXXXX
                     *      'P========
                     *        '.    ? 
                     *          '. ? 
                     *            B  
                     */
                    
                    // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
                    int dy = currState.y - basePoint.y; 
                    Fraction leftProjection = new Fraction((xL-basePoint.x)*(dy+1), dy).plus(basePoint.x);

                    int leftBound = leftUpExtent(xL, currState.y);
                    if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
                        leftProjection = new Fraction(leftBound);
                    }
                    
                    generateUpwardsUnobservable(new Point(xL,currState.y), leftProjection, currState.xL, currState);
                }
            }

            if (currState.xR.isWholeNumber()) {
                int xR = currState.xR.n;
                if (basePoint.x < xR && !graph.bottomLeftOfBlockedTile(xR, currState.y)) {
                    /* 
                     *  XXXXXXX|-----.
                     * XXXXXXXX|   .'
                     * XXXXXXXX| .'
                     * ========P' 
                     *  ?    .'
                     *   ? .'
                     *    B
                     */

                    // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
                    int dy = currState.y - basePoint.y; 
                    Fraction rightProjection = new Fraction((xR-basePoint.x)*(dy+1), dy).plus(basePoint.x);

                    int rightBound = rightUpExtent(xR, currState.y);
                    if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
                        rightProjection = new Fraction(rightBound);
                    }
                    
                    generateUpwardsUnobservable(new Point(xR,currState.y), currState.xR, rightProjection, currState);
                }
            }
            
            
        } else {
            // Is not Blocked Above
            /*
             * =======      =====    =====
             *  \   /       / .'      '. \
             *   \ /   OR  /.'    OR    '.\
             *    B       B                B
             */

            // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
            int dy = currState.y - basePoint.y; 
            Fraction leftProjection = currState.xL.minus(basePoint.x).multiplyDivide(dy+1, dy).plus(basePoint.x);
            
            int leftBound = leftUpExtent(currState.xL.floor()+1, currState.y);
            if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
                leftProjection = new Fraction(leftBound);
            }

            // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
            Fraction rightProjection = currState.xR.minus(basePoint.x).multiplyDivide(dy+1, dy).plus(basePoint.x);
            
            int rightBound = rightUpExtent(currState.xR.ceil()-1, currState.y);
            if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
                rightProjection = new Fraction(rightBound);
            }

            if (leftProjection.isLessThan(rightProjection)) {
                generateUpwardsObservable(leftProjection, rightProjection, currState);
            }
        }
        

        if (currState.xL.isWholeNumber()) {
            int xL = currState.xL.n;
            if (graph.topRightOfBlockedTile(xL, currState.y) && !graph.bottomRightOfBlockedTile(xL, currState.y)) {
                /*
                 * .------P======
                 * |XXXXXX|\   /
                 * |XXXXXX| \ /
                 *           B
                 */
                Point pivot = new Point(xL,currState.y);
                
                {
                    int leftBound = leftAnyExtent(xL, currState.y);
                    generateSameLevelUnobservable(pivot, leftBound, xL, currState);
                }

                {
                    int dy = currState.y - basePoint.y; 
                    Fraction leftProjection = new Fraction((xL-basePoint.x)*(dy+1), dy).plus(basePoint.x);
                    
                    int leftBound = leftUpExtent(xL, currState.y);
                    if (!leftProjection.isLessThanOrEqual(leftBound)) { // leftBound < leftProjection
                        this.generateUpwardsUnobservable(pivot, new Fraction(leftBound), leftProjection, currState);
                    }
                }
            }
        }

        if (currState.xR.isWholeNumber()) {
            int xR = currState.xR.n;
            if (graph.topLeftOfBlockedTile(xR, currState.y) && !graph.bottomLeftOfBlockedTile(xR, currState.y)) {
                /*
                 * ======P------.
                 *  \   /|XXXXXX|
                 *   \ / |XXXXXX|
                 *    B
                 */
                Point pivot = new Point(xR,currState.y);
                
                {
                    int rightBound = rightAnyExtent(xR, currState.y);
                    generateSameLevelUnobservable(new Point(xR,currState.y), xR, rightBound, currState);
                }

                {
                    int dy = currState.y - basePoint.y; 
                    Fraction rightProjection = new Fraction((xR-basePoint.x)*(dy+1), dy).plus(basePoint.x);
                    int rightBound = rightUpExtent(xR, currState.y);
                    if (rightProjection.isLessThan(rightBound)) { // rightProjection < rightBound
                        this.generateUpwardsUnobservable(pivot, rightProjection, new Fraction(rightBound), currState);
                    }
                }
            }
        }
    }

    private void explorefromAbove(AnyaState currState, Point basePoint) {
        // Note: basePoint.y > currState.y
        // Note: basePoint == currState.basePoint

        assert basePoint.y > currState.y;

        if (graph.topLeftOfBlockedTile(currState.xL.floor(), currState.y)) {
            // Is Blocked Below
            if (currState.xL.isWholeNumber()) {
                int xL = currState.xL.n;
                if (xL < basePoint.x && !graph.topRightOfBlockedTile(xL, currState.y)) {
                    /* 
                     *            B  
                     *          .' ? 
                     *        .'    ? 
                     *      .P========
                     *    .' |XXXXXXXX
                     *  .'   |XXXXXXXX
                     * '-----|XXXXXXX
                     */
                    
                    // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
                    int dy = basePoint.y - currState.y; 
                    Fraction leftProjection = new Fraction((xL-basePoint.x)*(dy+1), dy).plus(basePoint.x);

                    int leftBound = leftDownExtent(xL, currState.y);
                    if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
                        leftProjection = new Fraction(leftBound);
                    }

                    generateDownwardsUnobservable(new Point(xL,currState.y), leftProjection, currState.xL, currState);
                }
            }

            if (currState.xR.isWholeNumber()) {
                int xR = currState.xR.n;
                if (basePoint.x < xR && !graph.topLeftOfBlockedTile(xR, currState.y)) {
                    /* 
                     *    B
                     *   ? '.
                     *  ?    '.
                     * ========P. 
                     * XXXXXXXX| '.
                     * XXXXXXXX|   '.
                     *  XXXXXXX|-----'
                     */

                    // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
                    int dy = basePoint.y - currState.y; 
                    Fraction rightProjection = new Fraction((xR-basePoint.x)*(dy+1), dy).plus(basePoint.x);

                    int rightBound = rightDownExtent(xR, currState.y);
                    if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
                        rightProjection = new Fraction(rightBound);
                    }
                    
                    generateDownwardsUnobservable(new Point(xR,currState.y), currState.xR, rightProjection, currState);
                }
            }

        } else {
            // Is not Blocked Below
            /*
             *    B       B                B
             *   / \   OR  \'.    OR    .'/
             *  /   \       \ '.      .' /
             * =======      =====    =====
             */

            // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
            int dy = basePoint.y - currState.y; 
            Fraction leftProjection = currState.xL.minus(basePoint.x).multiplyDivide(dy+1, dy).plus(basePoint.x);
            
            int leftBound = leftDownExtent(currState.xL.floor()+1, currState.y);
            if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
                leftProjection = new Fraction(leftBound);
            }

            // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
            Fraction rightProjection = currState.xR.minus(basePoint.x).multiplyDivide(dy+1, dy).plus(basePoint.x);
            
            int rightBound = rightDownExtent(currState.xR.ceil()-1, currState.y);
            if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
                rightProjection = new Fraction(rightBound);
            }
            
            if (leftProjection.isLessThan(rightProjection)) {
                generateDownwardsObservable(leftProjection, rightProjection, currState);
            }
        }
        

        if (currState.xL.isWholeNumber()) {
            int xL = currState.xL.n;
            if (graph.bottomRightOfBlockedTile(xL, currState.y) && !graph.topRightOfBlockedTile(xL, currState.y)) {
                /*
                 *           B
                 * |XXXXXX| / \
                 * |XXXXXX|/   \
                 * '------P======
                 */
                Point pivot = new Point(xL,currState.y);
                
                {
                    int leftBound = leftAnyExtent(xL, currState.y);
                    generateSameLevelUnobservable(pivot, leftBound, xL, currState);
                }

                {
                    int dy = basePoint.y - currState.y; 
                    Fraction leftProjection = new Fraction((xL-basePoint.x)*(dy+1), dy).plus(basePoint.x);
                    
                    int leftBound = leftDownExtent(xL, currState.y);
                    if (!leftProjection.isLessThanOrEqual(leftBound)) { // leftBound < leftProjection
                        this.generateDownwardsUnobservable(pivot, new Fraction(leftBound), leftProjection, currState);
                    }
                }
            }
        }

        if (currState.xR.isWholeNumber()) {
            int xR = currState.xR.n;
            if (graph.bottomLeftOfBlockedTile(xR, currState.y) && !graph.topLeftOfBlockedTile(xR, currState.y)) {
                /*
                 *    B
                 *   / \ |XXXXXX|
                 *  /   \|XXXXXX|
                 * ======P------'
                 */
                Point pivot = new Point(xR,currState.y);
                
                {
                    int rightBound = rightAnyExtent(xR, currState.y);
                    generateSameLevelUnobservable(new Point(xR,currState.y), xR, rightBound, currState);
                }

                {
                    int dy = basePoint.y - currState.y; 
                    Fraction rightProjection = new Fraction((xR-basePoint.x)*(dy+1), dy).plus(basePoint.x);
                    int rightBound = rightDownExtent(xR, currState.y);
                    if (rightProjection.isLessThan(rightBound)) { // rightProjection < rightBound
                        this.generateDownwardsUnobservable(pivot, rightProjection, new Fraction(rightBound), currState);
                    }
                }
            }
        }
    }
    
    /// === GENERATE SUCCESSORS - PATTERNS - END ===
    
    /// === GENERATE SUCCESSORS - UTILITY - START ===

    private int leftUpExtent(int xL, int y) {
        return leftDownExtents[y+1][xL];
    }

    private int leftDownExtent(int xL, int y) {
        return leftDownExtents[y][xL];
    }
    
    private int leftAnyExtent(int xL, int y) {
        return Math.max(leftDownExtents[y][xL], leftDownExtents[y+1][xL]);
    }

    private int rightUpExtent(int xR, int y) {
        return rightDownExtents[y+1][xR];
    }

    private int rightDownExtent(int xR, int y) {
        return rightDownExtents[y][xR];
    }

    private int rightAnyExtent(int xR, int y) {
        return Math.min(rightDownExtents[y][xR], rightDownExtents[y+1][xR]);
    }

    /**
     * Can be used for exploreLeftwards or exploreRightwards.
     * This function will not split intervals.
     */
    private void generateSameLevelObservable(int leftBound, int rightBound, AnyaState source) {
        addSuccessor(source,
                AnyaState.createObservableSuccessor(new Fraction(leftBound), new Fraction(rightBound), source.y, source));
    }

    /**
     * Can be used for exploreLeftwards or exploreRightwards.
     * This function will not split intervals.
     */
    private void generateSameLevelUnobservable(Point basePoint, int leftBound, int rightBound, AnyaState source) {
        addSuccessor(source,
                AnyaState.createUnobservableSuccessor(new Fraction(leftBound), new Fraction(rightBound), source.y, basePoint, source));
    }

    /**
     * Can be used for exploreLeftwards or exploreRightwards.
     * This function will not split intervals.
     */
    private void generateSameLevelStart(Point start, int leftBound, int rightBound) {
        addSuccessor(null,
                AnyaState.createStartState(new Fraction(leftBound), new Fraction(rightBound), start.y, start));
    }

    private void generateUpwardsUnobservable(Point basePoint, Fraction leftBound, Fraction rightBound, AnyaState source) {
        generateAndSplitIntervals(
                source.y + 2, source.y + 1,
                basePoint,
                leftBound, rightBound,
                source);
    }
    
    private void generateUpwardsObservable(Fraction leftBound, Fraction rightBound, AnyaState source) {
        generateAndSplitIntervals(
                source.y + 2, source.y + 1,
                null,
                leftBound, rightBound,
                source);
    }
    
    private void generateUpwardsStart(Fraction leftBound, Fraction rightBound, Point start) {
        generateAndSplitIntervals(
                start.y + 2, start.y + 1,
                start,
                leftBound, rightBound,
                null);
    }

    private void generateDownwardsUnobservable(Point basePoint, Fraction leftBound, Fraction rightBound, AnyaState source) {
        generateAndSplitIntervals(
                source.y - 1, source.y - 1,
                basePoint,
                leftBound, rightBound,
                source);
    }
    
    private void generateDownwardsObservable(Fraction leftBound, Fraction rightBound, AnyaState source) {
        generateAndSplitIntervals(
                source.y - 1, source.y - 1,
                null,
                leftBound, rightBound,
                source);
    }
    
    private void generateDownwardsStart(Fraction leftBound, Fraction rightBound, Point start) {
        generateAndSplitIntervals(
                start.y - 1, start.y - 1,
                start,
                leftBound, rightBound,
                null);
    }
    
    /**
     * Called by generateUpwards / Downwards.
     * basePoint is null if observable. Not null if unobservable.
     * source is null if start state.
     * 
     * This is used to avoid repeated code in generateUpwardsUnobservable, generateUpwardsObservable,
     * // generateDownwardsUnobservable, generateDownwardsObservable, generateDownwardsStart, generateDownwardsStart.
     */
    private void generateAndSplitIntervals(int checkY, int newY, Point basePoint, Fraction leftBound, Fraction rightBound, AnyaState source) {
        Fraction left = leftBound;
        int leftFloor = left.floor();

        // Divide up the intervals.
        while(true) {
            int right = rightDownExtents[checkY][leftFloor]; // it's actually rightDownExtents for exploreDownwards. (thus we use checkY = currY - 2)
            if (rightBound.isLessThanOrEqual(right)) break; // right < rightBound            
            
            if (basePoint == null) {
                addSuccessor(source, AnyaState.createObservableSuccessor(left, new Fraction(right), newY, source));
            } else {
                if (source == null) {
                    addSuccessor(null, AnyaState.createStartState(left, new Fraction(right), newY, basePoint));
                } else {
                    addSuccessor(source, AnyaState.createUnobservableSuccessor(left, new Fraction(right), newY, basePoint, source));
                }
            }
            
            leftFloor = right;
            left = new Fraction(leftFloor);
        }
        
        if (basePoint == null) {
            addSuccessor(source, AnyaState.createObservableSuccessor(left, rightBound, newY, source));
        } else {
            if (source == null) {
                addSuccessor(null, AnyaState.createStartState(left, rightBound, newY, basePoint));
            } else {
                addSuccessor(source, AnyaState.createUnobservableSuccessor(left, rightBound, newY, basePoint, source));
            }
        }
    }
    
    /// === GENERATE SUCCESSORS - UTILITY - END ===
    


    private float heuristic(AnyaState currState) {
        int baseX = currState.basePoint.x;
        int baseY = currState.basePoint.y;
        Fraction xL = currState.xL;
        Fraction xR = currState.xR;

        // Special case: base, goal, interval all on same row.
        if (currState.y == baseY && currState.y == ey) {

            // Case 1: base and goal on left of interval.
            // baseX < xL && ex < xL
            if (!xL.isLessThanOrEqual(baseX) && !xL.isLessThanOrEqual(ex)) {
                return 2*xL.toFloat() - baseX - ex; // (xL-baseX) + (xL-ex);
            }
            
            // Case 2: base and goal on right of interval.
            // xR < baseX && xR < ex
            else if (xR.isLessThan(baseX) && xR.isLessThan(ex)) {
                return baseX + ex - 2*xL.toFloat(); // (baseX-xL) + (ex-xL)
            }
            
            // Case 3: Otherwise, the direct path from base to goal will pass through the interval.
            else {
                return Math.abs(baseX - ex);
            }
        }

    
        int dy1 = baseY - currState.y;
        int dy2 = ey - currState.y;
        
        // If goal and base on same side of interval, reflect goal about interval -> ey2.
        int ey2 = ey;
        if (dy1 * dy2 > 0) ey2 = 2*currState.y - ey;
        
        /*  E
         *   '.
         * ----X----- <--currState.y
         *      '.
         *        B
         */
        // (ey-by)/(ex-bx) = (cy-by)/(cx-bx)
        // cx = bx + (cy-by)(ex-bx)/(ey-by)
        
        // Find the pivot point on the interval for shortest path from base to goal.
        float intersectX = baseX + (float)(currState.y - baseY)*(ex - baseX)/(ey2-baseY);
        float xlf = xL.toFloat();
        float xrf = xR.toFloat();
        
        // Snap to endpoints of interval if intersectX it lies outside interval.
        if (intersectX < xlf) intersectX = xlf;
        if (intersectX > xrf) intersectX = xrf;
        
        {
            // Return sum of euclidean distances. (base~intersection~goal)
            float dx1 = intersectX - baseX;
            float dx2 = intersectX - ex;
            
            return (float)(Math.sqrt(dx1*dx1+dy1*dy1) + Math.sqrt(dx2*dx2+dy2*dy2));
        }
    }
    

    private int pathLength() {
        int length = 1;
        AnyaState current = goalState;
        while (current != null) {
            current = current.parent;
            length++;
        }
        return length;
    }
    
    @Override
    public int[][] getPath() {
        if (goalState == null) return new int[0][]; // Fail

        // Start from goalState and traverse backwards.
        int length = pathLength();
        int[][] path = new int[length][];
        AnyaState current = goalState;

        path[length-1] = new int[2];
        path[length-1][0] = ex;
        path[length-1][1] = ey;
        
        int index = length-2;
        while (current != null) {
            path[index] = new int[2];
            path[index][0] = current.basePoint.x;
            path[index][1] = current.basePoint.y;
            
            index--;
            current = current.parent;
        }
        
        return path;
    }

    @Override
    public float getPathLength() {
        if (goalState == null) return -1; // Fail

        // Start from goalState and traverse backwards.
        double pathLength = 0;
        int currX = ex;
        int currY = ey;
        AnyaState current = goalState;

        while (current != null) {
            int nextX = current.basePoint.x;
            int nextY = current.basePoint.y;
            
            pathLength += graph.distance_double(currX,currY,nextX,nextY);
            current = current.parent;
            currX = nextX;
            currY = nextY;
        }
        
        return (float)pathLength;
    }
    

    @Override
    protected List<SnapshotItem> computeSearchSnapshot() {
        ArrayList<SnapshotItem> list = new ArrayList<>(states.length);

        for (AnyaState in : states) {
            // y, xLn, xLd, xRn, xRd, px, py
            if (in == null) continue;
            
            Integer[] line = new Integer[7];
            line[0] = in.y;
            line[1] = in.xL.n;
            line[2] = in.xL.d;
            line[3] = in.xR.n;
            line[4] = in.xR.d;
            line[5] = in.basePoint.x;
            line[6] = in.basePoint.y;
            list.add(SnapshotItem.generate(line));
        }
        
        if (!pq.isEmpty()) {
            int index = pq.getMinIndex();
            AnyaState in = states[index];

            Integer[] line = new Integer[5];
            line[0] = in.y;
            line[1] = in.xL.n;
            line[2] = in.xL.d;
            line[3] = in.xR.n;
            line[4] = in.xR.d;
            list.add(SnapshotItem.generate(line));
        }
        
        return list;
    }

    public static void clearMemory() {
        leftDownExtents = null;
        rightDownExtents = null;
        System.gc();
    }

}


class AnyaState {
    public final Fraction xL;
    public final Fraction xR;
    public final int y;
    public final Point basePoint;
    
    public float hValue;
    public float fValue;
    public float gValue;
    public AnyaState parent;
    public boolean visited;

    private AnyaState(Fraction xL, Fraction xR, int y, Point basePoint, float gValue, AnyaState parent) {
        this.xL = xL;
        this.xR = xR;
        this.y = y;
        this.basePoint = basePoint;
        
        this.gValue = gValue;
        this.parent = parent;
        this.visited = false;
    }

    public static AnyaState createStartState(Fraction xL, Fraction xR, int y, Point start) {
        return new AnyaState(xL,xR,y,
                start,
                0f,
                null);
    }
    
    public static AnyaState createObservableSuccessor(Fraction xL, Fraction xR, int y, AnyaState sourceInterval) {
        return new AnyaState(xL,xR,y,
                sourceInterval.basePoint,
                sourceInterval.gValue,
                sourceInterval.parent);
    }

    public static AnyaState createUnobservableSuccessor(Fraction xL, Fraction xR, int y, Point basePoint, AnyaState sourceInterval) {
        int dx = basePoint.x - sourceInterval.basePoint.x;
        int dy = basePoint.y - sourceInterval.basePoint.y;
        return new AnyaState(xL,xR,y,
                basePoint,
                sourceInterval.gValue + (float)Math.sqrt(dx*dx+dy*dy),
                sourceInterval);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // Removed null checks.
        result = prime * result + basePoint.hashCode();
        result = prime * result + xL.hashCode();
        result = prime * result + xR.hashCode();
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        // Removed type checks. Removed null checks.
        AnyaState other = (AnyaState) obj;
        if (!xL.equals(other.xL)) return false;
        if (!xR.equals(other.xR)) return false;
        if (y != other.y) return false;
        if (!basePoint.equals(other.basePoint)) return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "(" + xL + " " + xR + ") - " + y;   
    }
}