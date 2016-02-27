package algorithms;

import grid.GridGraph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import algorithms.anya.Fraction;
import algorithms.anya.Interval;
import algorithms.bst.Node;
import algorithms.datatypes.Point;
import algorithms.priorityqueue.FastVariableSizeIndirectHeap;

public class AnyaNew extends PathFindingAlgorithm {

    private interface CheckFunction {
        public boolean check(int x, int y);
    }
    
    private AnyaState goalState;
    private AnyaState[] states;
    private FastVariableSizeIndirectHeap pq;
    private HashMap<AnyaState, Integer> existingStates;

    private final Fraction leftBoundary;
    private final Fraction rightBoundary;
    
    public AnyaNew(GridGraph graph, int sizeX, int sizeY, int sx, int sy,
            int ex, int ey) {
        super(graph, sizeX, sizeY, sx, sy, ex, ey);
        leftBoundary = new Fraction(0);
        rightBoundary = new Fraction(graph.sizeX);
    }

    @Override
    public void computePath() {
        existingStates = new HashMap<>();
        pq = new FastVariableSizeIndirectHeap();
        states = new AnyaState[11];
        goalState = null;

        
        while (!pq.isEmpty()) {
            float dist = pq.getMinValue();
            int currentID = pq.popMinIndex();
            AnyaState currState = states[currentID];
            
            if (isGoalState(currState)) {
                goalState = currState;
                break;
            }

            generateSuccessors(currState);
        }
    }

    private boolean isGoalState(AnyaState state) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    //addSuccessor(source, AnyaState.createUnobservableSuccessor(xL, xR, y, pivot, source));
    
    private void addSuccessor(AnyaState source, AnyaState successor) {
        Integer existingHandle = existingStates.get(successor);
        if (existingHandle == null) {
            addToOpen(successor);
        } else {
            relaxExisting(source, existingHandle);
        }
    }
    
    private void addToOpen(AnyaState successor) {
        int handle = pq.insert(successor.fValue);
        if (handle >= states.length) {
            states = Arrays.copyOf(states, states.length*2);
        }
        successor.handle = handle;
        states[handle] = successor;
        existingStates.put(successor, handle);
    }
    
    private void relaxExisting(AnyaState source, int existingHandle) {
        AnyaState successor = states[existingHandle];
        
        int dx = successor.basePoint.x - source.basePoint.x;
        int dy = successor.basePoint.y - source.basePoint.y;
        float newgValue = source.gValue + (float)Math.sqrt(dx*dx+dy*dy);
        
        if (newgValue < successor.gValue) {
            successor.gValue = newgValue;
            successor.fValue = newgValue + successor.hValue;
            pq.decreaseKey(existingHandle, successor.fValue);
        }
    }
    
    
    private void computeClearances() {
        
        
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
                    exploreUpwards(new Point(xL,y), leftBound, currState.xL);
                }
            } else if (graph.topLeftOfBlockedTile(xL, y)) {
                if (!graph.topRightOfBlockedTile(xL, y)) {
                    /* ----- P========B
                     *       |XXXXXXXX|
                     * ----- |XXXXXXXX|
                     */
                    Fraction leftBound = new Fraction(leftDownExtent(xL, y)); 
                    exploreDownwards(new Point(xL,y), leftBound, currState.xL);
                }
            }
            
            if (!graph.bottomRightOfBlockedTile(xL, y) || !graph.topRightOfBlockedTile(xL, y)) {
                int leftBound = leftAnyExtent(xL, y);
                exploreLeftwards(basePoint, leftBound, xL);
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
                    exploreUpwards(new Point(xR,y), currState.xR, rightBound);
                }
            } else if (graph.topRightOfBlockedTile(xR, y)) {
                if (!graph.topLeftOfBlockedTile(xR, y)) {
                    /*  B========P -----
                     *  |XXXXXXXX|
                     *  |XXXXXXXX| -----
                     */
                    Fraction rightBound = rightDownExtent(xR, y); 
                    exploreDownwards(new Point(xR,y), currState.xR, rightBound);
                }
            }
            
            if (!graph.bottomLeftOfBlockedTile(xR, y) || !graph.topLeftOfBlockedTile(xR, y)) {
                int rightBound = rightAnyExtent(xR, y);
                exploreRightwards(basePoint, xR, rightBound);
            }
            
        }
    }
    


    private void explorefromBelow(AnyaState currState, Point basePoint) {
        // Note: basePoint.y < currState.y

        assert basePoint.y < currState.y;
        
        if (graph.topLeftOfBlockedTile(currState.xL.floor(), currState.y)) {
            // Is Blocked Above
            if (currState.xL.isWholeNumber()) {
                int xL = currState.xL.n;
                if (xL < basePoint.x) {
                    /* 
                     * .-----|XXXXXXX
                     *  '.   |XXXXXXXX
                     *    '. |XXXXXXXX
                     *      'P======= 
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

                    exploreUpwards(basePoint, leftProjection, currState.xL);
                }
            }

            if (currState.xR.isWholeNumber()) {
                int xR = currState.xR.n;
                if (basePoint.x < xR) {
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
                    
                    exploreUpwards(basePoint, currState.xR, rightProjection);
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
            
            int leftBound = leftUpExtent(currState.xL.floor(), currState.y);
            if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
                leftProjection = new Fraction(leftBound);
            }

            // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
            Fraction rightProjection = currState.xR.minus(basePoint.x).multiplyDivide(dy+1, dy).plus(basePoint.x);
            
            int rightBound = rightUpExtent(currState.xR.ceil(), currState.y);
            if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
                rightProjection = new Fraction(rightBound);
            }
            
            exploreUpwards(basePoint, leftProjection, rightProjection);
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

                int leftBound = leftAnyExtent(xL, currState.y);
                exploreLeftwards(new Point(xL,currState.y), leftBound, xL);
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

                int rightBound = rightAnyExtent(xR, currState.y);
                exploreRightwards(new Point(xR,currState.y), xR, rightBound);
            }
        }
    }

    private void explorefromAbove(AnyaState currState, Point basePoint) {
        throw new UnsupportedOperationException("Not implemented yet! Finalise exploreFromBelow first.");
    }
    
    /// === GENERATE SUCCESSORS - PATTERNS - END ===
    
    /// === GENERATE SUCCESSORS - UTILITY - START ===

    private int leftUpExtent(int xL, int y) {
        // TODO Auto-generated method stub
        return 0;
    }

    private int leftDownExtent(int xL, int y) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    private int leftAnyExtent(int xL, int y) {
        // TODO Auto-generated method stub
        return 0;
    }

    private int rightUpExtent(int xR, int y) {
        // TODO Auto-generated method stub
        return 0;
    }

    private Fraction rightDownExtent(int xR, int y) {
        // TODO Auto-generated method stub
        return null;
    }

    private int rightAnyExtent(int xR, int y) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    private void exploreLeftwards(Point basePoint, int leftBound, int rightBound) {
        // TODO Auto-generated method stub
        
    }

    private void exploreRightwards(Point basePoint, int leftBound, int rightBound) {
        // TODO Auto-generated method stub
        
    }

    private void exploreUpwards(Point basePoint, Fraction leftBound, Fraction rightBound) {
        // TODO Auto-generated method stub
        
    }
    
    private void exploreDownwards(Point basePoint, Fraction leftBound, Fraction rightBound) {
        // TODO Auto-generated method stub
        
    }
    
    /// === GENERATE SUCCESSORS - UTILITY - END ===
    
    
    @Override
    public int[][] getPath() {
        if (goalState == null) return null; // Fail
        
        // Start from goalState and traverse backwards.
        return null;
    }

    @Override
    protected float getPathLength() {
        // TODO Auto-generated method stub
        return 0;
    }

}


class AnyaState {
    public final Fraction xL;
    public final Fraction xR;
    public final int y;
    public final Point basePoint;
    
    public final float hValue;
    public float fValue;
    public float gValue;
    public AnyaState parent;
    public int handle;

    private AnyaState(Fraction xL, Fraction xR, int y, Point basePoint, float gValue, AnyaState parent) {
        this.xL = xL;
        this.xR = xR;
        this.y = y;
        this.basePoint = basePoint;
        
        this.gValue = gValue;
        this.hValue = heuristic();
        this.fValue = gValue + hValue;
        this.parent = parent;
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

    private float heuristic() {
        return 0;
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
}