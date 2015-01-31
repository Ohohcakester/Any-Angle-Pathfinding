package algorithms;

import grid.GridGraph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import algorithms.anya.Fraction;
import algorithms.anya.Interval;
import algorithms.anya.IntervalFValueComparator;
import algorithms.bst.AVLTree;
import algorithms.bst.Node;
import algorithms.datatypes.Point;
import algorithms.datatypes.SnapshotItem;


/**
 * Anya Observations:
 * 1) Every root point must occur at a corner of a blocked tile or the start point. 
 *   - possible exception: consecutive adjacent intervals on same row.
 * 
 * 2) If a root is on the same row (y-coordinate) as the interval, and the
 *    root is not the start point, then the root must be one of the endpoints.
 *
 */
public class Anya extends PathFindingAlgorithm {
    
    private interface CheckFunction {
        public boolean check(int x, int y);
    }
    
    private ArrayList<ExploredInterval> exploredList;
    private ArrayList<ExploredInterval> recordList;

    private ArrayList<AVLTree<Interval>> intervalSets;
    private float[] distance;
    private Interval[] pointInterval;
    
    private Interval start;
    private Interval finish;
    private PriorityQueue<Interval> pq;
    
    private final Fraction rightBoundary;
    private final Fraction leftBoundary;
    
    public Anya(GridGraph graph, int sx, int sy, int ex,
            int ey) {
        super(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey);
        leftBoundary = new Fraction(0);
        rightBoundary = new Fraction(graph.sizeX);
    }
    

    @Override
    public void computePath() {
        int totalSize = (graph.sizeX+1) * (graph.sizeY+1);
        initialiseIndirectHeap();
        initialiseIntervalSets();
        pointInterval = new Interval[totalSize];
        distance = new float[totalSize];
        for (int i=0;i<totalSize;i++) {
            distance[i] = Float.POSITIVE_INFINITY;
        }
        setDistance(sx, sy, 0, start);
        
        while (finish == null && !pq.isEmpty()) {
            Interval interval = pq.poll();
            if (interval.fValue == Float.POSITIVE_INFINITY) {
                break;
            }
            explore(interval);
        }
    }
    
    private void initialiseIndirectHeap() {
        pq = new PriorityQueue<>(new IntervalFValueComparator());
    }
    
    private void explore(Interval interval) {
        //System.out.println("EXPLORE "+interval + " PARENT = " + interval.parent);
        tryRecordExploredInterval(interval.y, interval.xL, interval.xR, interval.parent);
        assert !interval.visited;
        assert interval.fValue != Float.POSITIVE_INFINITY;
        assert interval.parent != null;
        //count(10, () -> breakPoint());
        
        interval.visited = true;
        
        if (containsFinishPoint(interval)) {
            finish = interval;
            return;
        }
        
        Point curParent = interval.parent;
        
        // explore 1 step up, 1 step down
        if (curParent.y == interval.y) {
            exploreFromSameLevel(interval, curParent);
            
        } else if (curParent.y < interval.y) {
            exploreUpwards(interval, curParent);
            
        } else if (curParent.y > interval.y) {
            exploreDownwards(interval, curParent);
        }
    }

    private void exploreFromSameLevel(Interval interval, Point curParent) {
        //if (interval.xL.isLessThan(curParent.x)) {
            searchLeftDirect(interval, curParent);
        //}
        //if (isLessThan(curParent.x, interval.xR)) {
            searchRightDirect(interval, curParent);
        //}

        int yUp = curParent.y+1;
        int yDown = curParent.y-1;
        
        if (interval.xL.isWholeNumber()) {
            int pivotX = interval.xL.n;
            int pivotY = interval.y;
            Point pivot = new Point(pivotX, pivotY);
            if (curParent.equals(pivot)) { // case: root is at pivot...?
                if (pivot.equals(start.parent)) {
                    if (yUp <= graph.sizeY+1) {
                        Fraction currX = new Fraction(pivotX);
                        exploreUpBothWays(interval, pivot, currX);
                    }
                    if (yDown >= 0) {
                        Fraction currX = new Fraction(pivotX);
                        exploreDownBothWays(interval, pivot, currX);
                    }
                }
            } else if (yUp <= graph.sizeY+1 && bottomLeftOfBlockedTile(pivotX, pivotY)) {
                // Pivot up
                setGValueOfLeftEndpoint(interval);
                exploreUpLeft(interval, pivot, interval.xL, leftBoundary);
                
            } else if (yDown >= 0 && topLeftOfBlockedTile(pivotX, pivotY)) { // cannot happen simultaneously...?
                // Pivot down
                setGValueOfLeftEndpoint(interval);
                exploreDownLeft(interval, pivot, interval.xL, leftBoundary);
            }
        }
        if (interval.xR.isWholeNumber()) {
            int pivotX = interval.xR.n;
            int pivotY = interval.y;
            Point pivot = new Point(pivotX, pivotY);
            if (curParent.equals(pivot)) { // case: root is at pivot...?
                /*if (!interval.xL.equals(interval.xR)) {
                    if (yUp <= graph.sizeY+1) {
                        Fraction currX = new Fraction(pivotX);
                        exploreUpBothWays(interval, pivot, currX);
                    }
                    if (yDown >= 0) {
                        Fraction currX = new Fraction(pivotX);
                        exploreDownBothWays(interval, pivot, currX);
                    }
                }*/
            } else if (yUp <= graph.sizeY+1 && bottomRightOfBlockedTile(pivotX, pivotY)) {
                // Pivot up
                setGValueOfRightEndpoint(interval);
                exploreUpRight(interval, pivot, interval.xR, rightBoundary);
                
            } else if (yDown >= 0 && topRightOfBlockedTile(pivotX, pivotY)) { // cannot happen simultaneously...?
                // Pivot down
                setGValueOfRightEndpoint(interval);
                exploreDownRight(interval, pivot, interval.xR, rightBoundary);
            }
        }
    }

    private void exploreUpwards(Interval interval, Point curParent) {
        boolean isUpwards = true;
        
        int yUp = interval.y + 1;
        if (yUp > graph.sizeY+1) {
            return;
        }
        Fraction xL = null;
        Fraction xR = null;
        
        //if (yUp <= graph.sizeY+1) {
        boolean blockedAbove;
        if (bottomLeftOfBlockedTile(interval.xL.floor(), interval.y)) { // blocked above.
            blockedAbove = true;
            xL = interval.xL;
            xR = interval.xR;
        } else {
            blockedAbove = false;
            // explore up.
            int dy = interval.y - curParent.y;
            
            xL = interval.xL.minus(curParent.x);
            xL = xL.multiplyDivide(dy+1,dy); // *= (dy+1)/dy
            xL = xL.plus(curParent.x);
                
            xR = interval.xR.minus(curParent.x);
            xR = xR.multiplyDivide(dy+1,dy); // *= (dy+1)/dy
            xR = xR.plus(curParent.x);

            if (xL.isLessThan(interval.xL)) { // xL < interval.xL
                xL = restrictLeft(interval.xL, yUp, xL, isUpwards);
            } else {
                xL = restrictRight(interval.xR, yUp, xL, isUpwards);
            }
            if (!xR.isLessThanOrEqual(interval.xR)) { // xR > interval.xR
                xR = restrictRight(interval.xR, yUp, xR, isUpwards);
            } else {
                xR = restrictLeft(interval.xL, yUp, xR, isUpwards);
            }

            processDirectSuccessors(interval, yUp, xL, xR, isUpwards);
        }
 //     }
        if (interval.xL.isWholeNumber()) {
            int pivotX = interval.xL.n;
            int pivotY = interval.y;
            if (blockedAbove) {
                if (!bottomRightOfBlockedTile(pivotX, pivotY)) {
                    setGValueOfLeftEndpoint(interval);
                    Point pivot = new Point(pivotX, pivotY);

                    int dy = pivotY - curParent.y;
                    int dx = pivotX - curParent.x;
                    Fraction xLimit = new Fraction(pivot.x*dy + dx, dy); // pivot.x + dx*(1/dy)

                    exploreUpLeft(interval, pivot, xL, xLimit);
                }
                
            } else if (topRightOfBlockedTile(pivotX, pivotY) != bottomRightOfBlockedTile(pivotX, pivotY)) { //XOR
                setGValueOfLeftEndpoint(interval);
                Point pivot = new Point(pivotX, pivotY);
                
                if (topRightOfBlockedTile(pivotX, pivotY)) {
                    searchLeft(interval, pivot);
                }
                //if (yUp <= graph.sizeY+1) {
                exploreUpLeft(interval, pivot, xL, leftBoundary);
                //}
            }
        }
        if (interval.xR.isWholeNumber()) {
            int pivotX = interval.xR.n;
            int pivotY = interval.y;
            if (bottomRightOfBlockedTile(pivotX, pivotY)) {
                setGValueOfRightEndpoint(interval);
                Point pivot = new Point(pivotX, pivotY);

                int dy = pivotY - curParent.y;
                int dx = pivotX - curParent.x;
                Fraction xLimit = new Fraction(pivot.x*dy + dx, dy); // pivot.x + dx*(1/dy)
                
                exploreUpRight(interval, pivot, xR, xLimit);
                
            } else if (topLeftOfBlockedTile(pivotX, pivotY) != bottomLeftOfBlockedTile(pivotX, pivotY)) { //XOR
                setGValueOfRightEndpoint(interval);
                Point pivot = new Point(pivotX, pivotY);

                if (topLeftOfBlockedTile(pivotX, pivotY)) {
                    searchRight(interval, pivot);
                }
                //if (yUp <= graph.sizeY+1) {
                exploreUpRight(interval, pivot, xR, rightBoundary);
                //}
            }
        }
    }

    private void exploreDownwards(Interval interval, Point curParent) {
        boolean isUpwards = false;
        
        int yDown = interval.y - 1;
        if (yDown < 0) {
            return;
        }
        Fraction xL = null;
        Fraction xR = null;
        
        if (topLeftOfBlockedTile(interval.xL.floor(), interval.y)) { // blocked above.
            xL = interval.xL;
            xR = interval.xR;
        } else {
            // explore down.
            int dy = curParent.y - interval.y;
            
            xL = interval.xL.minus(curParent.x);
            xL = xL.multiplyDivide(dy+1,dy); // *= (dy+1)/dy
            xL = xL.plus(curParent.x);
            
            xR = interval.xR.minus(curParent.x);
            xR = xR.multiplyDivide(dy+1,dy); // *= (dy+1)/dy
            xR = xR.plus(curParent.x);

            if (xL.isLessThan(interval.xL)) { // xL < interval.xL
                xL = restrictLeft(interval.xL, yDown, xL, isUpwards);
            } else {
                xL = restrictRight(interval.xR, yDown, xL, isUpwards);
            }
            if (!xR.isLessThanOrEqual(interval.xR)) { // xR > interval.xR
                xR = restrictRight(interval.xR, yDown, xR, isUpwards);
            } else {
                xR = restrictLeft(interval.xL, yDown, xR, isUpwards);
            }

            processDirectSuccessors(interval, yDown, xL, xR, isUpwards);
        }
        if (interval.xL.isWholeNumber()) {
            int pivotX = interval.xL.n;
            int pivotY = interval.y;
            if (topLeftOfBlockedTile(pivotX, pivotY)) {
                setGValueOfLeftEndpoint(interval);
                Point pivot = new Point(pivotX, pivotY);

                int dy = curParent.y - pivotY;
                int dx = pivotX - curParent.x;
                Fraction xLimit = new Fraction(pivot.x*dy + dx, dy); // pivot.x + dx*(1/dy)
                
                exploreDownLeft(interval, pivot, xL, xLimit);
                
            } else if (bottomRightOfBlockedTile(pivotX, pivotY) != topRightOfBlockedTile(pivotX, pivotY)) { //XOR
                setGValueOfLeftEndpoint(interval);
                Point pivot = new Point(pivotX, pivotY);
                
                if (bottomRightOfBlockedTile(pivotX, pivotY)) {
                    searchLeft(interval, pivot);
                }
                exploreDownLeft(interval, pivot, xL, leftBoundary);
            }
        }
        if (interval.xR.isWholeNumber()) {
            int pivotX = interval.xR.n;
            int pivotY = interval.y;
            if (topRightOfBlockedTile(pivotX, pivotY)) {
                setGValueOfRightEndpoint(interval);
                Point pivot = new Point(pivotX, pivotY);

                int dy = curParent.y - pivotY;
                int dx = pivotX - curParent.x;
                Fraction xLimit = new Fraction(pivot.x*dy + dx, dy); // pivot.x + dx*(1/dy)
                
                exploreDownRight(interval, pivot, xR, xLimit);
                
            } else if (bottomLeftOfBlockedTile(pivotX, pivotY) != topLeftOfBlockedTile(pivotX, pivotY)) { //XOR
                setGValueOfRightEndpoint(interval);
                Point pivot = new Point(pivotX, pivotY);

                if (bottomLeftOfBlockedTile(pivotX, pivotY)) {
                    searchRight(interval, pivot);
                }
                exploreDownRight(interval, pivot, xR, rightBoundary);
            }
        }
    }

    private void exploreUpLeft(Interval interval, Point pivot, Fraction fromX, Fraction toX) {
        if (!fromX.isLessThanOrEqual(rightBoundary)) { // if fromX > rightBoundary
            fromX = rightBoundary;
        }
        int yUp = pivot.y+1;
        Fraction leftEnd = restrictLeft(fromX, yUp, toX, true);
        if (!leftEnd.isLessThan(fromX)) return;

        LinkedList<Fraction> splitList = splitByBlocksAbove(yUp, leftEnd, fromX);

        Fraction intervalLeft = null;
        for (Fraction intervalRight : splitList) {
            if (intervalLeft == null) {
                intervalLeft = intervalRight;
            } else {
                relaxUsingPoint(pivot, yUp, intervalLeft, intervalRight);
                intervalLeft = intervalRight;
            }
        }
    }

    private void exploreUpRight(Interval interval, Point pivot, Fraction fromX, Fraction toX) {
        if (fromX.isLessThan(leftBoundary)) { // if fromX < leftBoundary
            fromX = leftBoundary;
        }
        int yUp = pivot.y+1;
        Fraction rightEnd = restrictRight(fromX, yUp, toX, true);
        if (!fromX.isLessThan(rightEnd)) return;

        LinkedList<Fraction> splitList = splitByBlocksAbove(yUp, fromX, rightEnd);

        Fraction intervalLeft = null;
        for (Fraction intervalRight : splitList) {
            if (intervalLeft == null) {
                intervalLeft = intervalRight;
            } else {
                relaxUsingPoint(pivot, yUp, intervalLeft, intervalRight);
                intervalLeft = intervalRight;
            }
        }
    }

    private void exploreDownLeft(Interval interval, Point pivot, Fraction fromX, Fraction toX) {
        if (toX.isLessThan(leftBoundary)) {
            toX = leftBoundary;
        }
        
        int yDown = pivot.y-1;
        Fraction leftEnd = restrictLeft(fromX, yDown, toX, false);
        if (!leftEnd.isLessThan(fromX)) return;

        LinkedList<Fraction> splitList = splitByBlocksBelow(yDown, leftEnd, fromX);

        Fraction intervalLeft = null;
        for (Fraction intervalRight : splitList) {
            if (intervalLeft == null) {
                intervalLeft = intervalRight;
            } else {
                relaxUsingPoint(pivot, yDown, intervalLeft, intervalRight);
                intervalLeft = intervalRight;
            }
        }
    }

    private void exploreDownRight(Interval interval, Point pivot, Fraction fromX, Fraction toX) {
        if (rightBoundary.isLessThan(toX)) {
            toX = rightBoundary;
        }
        
        int yDown = pivot.y-1;
        Fraction rightEnd = restrictRight(fromX, yDown, toX, false);
        if (!fromX.isLessThan(rightEnd)) return;

        LinkedList<Fraction> splitList = splitByBlocksBelow(yDown, fromX, rightEnd);

        Fraction intervalLeft = null;
        for (Fraction intervalRight : splitList) {
            if (intervalLeft == null) {
                intervalLeft = intervalRight;
            } else {
                relaxUsingPoint(pivot, yDown, intervalLeft, intervalRight);
                intervalLeft = intervalRight;
            }
        }
    }
    
    private void exploreUpBothWays(Interval interval, Point pivot, Fraction currX) {
        int yUp = pivot.y+1;
        

        Fraction xL = this.restrictLeft(currX, yUp, leftBoundary, true);
        Fraction xR = this.restrictRight(currX, yUp, rightBoundary, true);
        if (!xL.isLessThan(xR)) return;

        LinkedList<Fraction> splitList = splitByBlocksAbove(yUp, xL, xR);

        Fraction intervalLeft = null;
        for (Fraction intervalRight : splitList) {
            if (intervalLeft == null) {
                intervalLeft = intervalRight;
            } else {
                relaxUsingPoint(pivot, yUp, intervalLeft, intervalRight);
                intervalLeft = intervalRight;
            }
        }
    }
    
    private void exploreDownBothWays(Interval interval, Point pivot, Fraction currX) {
        int yDown = pivot.y-1;
        

        Fraction xL = this.restrictLeft(currX, yDown, leftBoundary, false);
        Fraction xR = this.restrictRight(currX, yDown, rightBoundary, false);
        if (!xL.isLessThan(xR)) return;

        LinkedList<Fraction> splitList = splitByBlocksBelow(yDown, xL, xR);

        Fraction intervalLeft = null;
        for (Fraction intervalRight : splitList) {
            if (intervalLeft == null) {
                intervalLeft = intervalRight;
            } else {
                relaxUsingPoint(pivot, yDown, intervalLeft, intervalRight);
                intervalLeft = intervalRight;
            }
        }
    }
    

    private void searchLeft(Interval interval, Point pivot) {
        Fraction pivotX = new Fraction(pivot.x);
        Interval left = new Interval(pivot.y, pivotX, pivotX);
        Node<Interval> fromNode = intervalSets.get(pivot.y).search(left);
        
        if (fromNode == null) {
            return;
        }
        
        left = fromNode.getData();
        assert left.xR.equals(pivotX);
        if (left.visited) {
            return;
        }
        
        Fraction xL = leftSearchTillCorner(pivotX, pivot.y, left.xL);
        Fraction xR = pivotX;

        //float distance = getDistance(pivot);
        //float fValue = distance;// - xL.minus(pivot.x).toFloat(); // distance + (pivot.X - xL)
        tryRelax(pivot.y, xL, xR, pivot);
    }

    
    private void searchRight(Interval interval, Point pivot) {
        Fraction pivotX = new Fraction(pivot.x);
        Interval right = new Interval(pivot.y, pivotX, pivotX);
        //System.out.println(intervalSets.get(pivot.y).inorderToString());
        Node<Interval> fromNode = intervalSets.get(pivot.y).search(right);
        assert fromNode != null;
        
        if (fromNode.getNext() == null) {
            return;
        }
        
        right = fromNode.getNext().getData();
        assert right.xL.equals(pivotX);
        if (right.visited) {
            return;
        }

        Fraction xL = pivotX;
        Fraction xR = rightSearchTillCorner(pivotX, pivot.y, right.xR);

        //float distance = getDistance(pivot);
        //float fValue = distance;// + xR.minus(pivot.x).toFloat(); // distance + (xR - pivot.x)
        tryRelax(pivot.y, xL, xR, pivot);
    }

    private void searchLeftDirect(Interval interval, Point pivot) {
        Fraction leftEndpoint = interval.xL;
        Interval left = new Interval(pivot.y, leftEndpoint, leftEndpoint);
        //left.isStartPoint = interval.isStartPoint;
        
        Node<Interval> fromNode = intervalSets.get(pivot.y).search(left);

        if (fromNode == null) {
            return;
        }
        
        left = fromNode.getData();
        assert left.xR.equals(leftEndpoint);
        if (left.visited) {
            return;
        }
        
        Fraction xL = leftSearchTillCorner(leftEndpoint, pivot.y, left.xL);
        Fraction xR = leftEndpoint;

        //float distance = getDistance(pivot);
        //float fValue = distance - xR.minus(pivot.x).toFloat(); // distance + (pivot.X - xR)
        tryRelax(pivot.y, xL, xR, pivot);
    }

    private void searchRightDirect(Interval interval, Point pivot) {
        Fraction rightEndpoint = interval.xR;
        Interval right = new Interval(pivot.y, rightEndpoint, rightEndpoint);
        right.isStartPoint = interval.isStartPoint;

        Node<Interval> fromNode = intervalSets.get(pivot.y).search(right);
        assert fromNode != null;
        
        if (fromNode.getNext() == null) {
            return;
        }
        
        right = fromNode.getNext().getData();
        assert right.xL.equals(rightEndpoint);
        if (right.visited) {
            return;
        }

        Fraction xL = rightEndpoint;
        Fraction xR = rightSearchTillCorner(rightEndpoint, pivot.y, right.xR);

        //float distance = getDistance(pivot);
        //float fValue = distance + xL.minus(pivot.x).toFloat(); // distance + (xL - pivot.x)
        tryRelax(pivot.y, xL, xR, pivot);
    }

    private void setGValueOfLeftEndpoint(Interval interval) {
        if (interval.xL.isWholeNumber()) {
            int x = interval.xL.n;
            int y = interval.y;
            int parentX = interval.parent.x;
            int parentY = interval.parent.y;
            float gValue = getDistance(parentX, parentY);
            gValue += distance2D(x, y, parentX, parentY);
            setDistance(x, y, gValue, interval);
        }
    }
    
    private void setGValueOfRightEndpoint(Interval interval) {
        if (interval.xR.isWholeNumber()) {
            int x = interval.xR.n;
            int y = interval.y;
            int parentX = interval.parent.x;
            int parentY = interval.parent.y;
            float gValue = getDistance(parentX, parentY);
            gValue += distance2D(x, y, parentX, parentY);
            setDistance(x, y, gValue, interval);
        }
    }
    
    /**
     * Goes left until it hits a corner of a tile, then returns.
     */
    private Fraction leftSearchTillCorner(Fraction fromXL, int y, Fraction toXL) {
        int fromLceil = fromXL.ceil();
        int toLfloor = toXL.floor();

        CheckFunction currentSideBlocked; // represents up/down.
        CheckFunction otherSideBlocked;   // represents opposite vertical direction of currentSideBlocked
        
        boolean currentlyBlocked;
        if (topRightOfBlockedTile(fromLceil, y)) {
            if (bottomRightOfBlockedTile(fromLceil, y)) {
                //completely blocked.
                return fromXL;
            } else {
                currentSideBlocked = (a,b) -> topRightOfBlockedTile(a,b);
                otherSideBlocked = (a,b) -> bottomRightOfBlockedTile(a,b);
                currentlyBlocked = true;
            }
        } else {
            if (bottomRightOfBlockedTile(fromLceil, y)) {
                currentSideBlocked = (a,b) -> bottomRightOfBlockedTile(a,b);
                otherSideBlocked = (a,b) -> topRightOfBlockedTile(a,b);
                currentlyBlocked = true;
            } else {
                currentSideBlocked = (a,b) -> rightOfBlockedTile(a,b);
                otherSideBlocked = (a,b) -> false;
                currentlyBlocked = false;
            }
        }
        
        fromLceil--;
        while (fromLceil > toLfloor) {
            if (otherSideBlocked.check(fromLceil, y)) {
                return new Fraction(fromLceil);
            }
            
            boolean isBlocked = currentSideBlocked.check(fromLceil, y);
            if (currentlyBlocked != isBlocked) {
                return new Fraction(fromLceil);
            }
            fromLceil--;
        }
        return toXL;
    }

    /**
     * Goes Right until it hits a corner of a tile, then returns.
     */
    private Fraction rightSearchTillCorner(Fraction fromXR, int y, Fraction toXR) {
        int fromRfloor = fromXR.floor();
        int toRceil = toXR.ceil();
        
        CheckFunction currentSideBlocked; // represents up/down.
        CheckFunction otherSideBlocked;   // represents opposite vertical direction of currentSideBlocked
        
        boolean currentlyBlocked;
        if (topLeftOfBlockedTile(fromRfloor, y)) {
            if (bottomLeftOfBlockedTile(fromRfloor, y)) {
                //completely blocked.
                return fromXR;
            } else {
                currentSideBlocked = (a,b) -> topLeftOfBlockedTile(a,b);
                otherSideBlocked = (a,b) -> bottomLeftOfBlockedTile(a,b);
                currentlyBlocked = true;
            }
        } else {
            if (bottomLeftOfBlockedTile(fromRfloor, y)) {
                currentSideBlocked = (a,b) -> bottomLeftOfBlockedTile(a,b);
                otherSideBlocked = (a,b) -> topLeftOfBlockedTile(a,b);
                currentlyBlocked = true;
            } else {
                currentSideBlocked = (a,b) -> leftOfBlockedTile(a,b);
                otherSideBlocked = (a,b) -> false;
                currentlyBlocked = false;
            }
        }

        fromRfloor++;
        while (fromRfloor < toRceil) {
            if (otherSideBlocked.check(fromRfloor, y)) {
                return new Fraction(fromRfloor);
            }
            
            boolean isBlocked = currentSideBlocked.check(fromRfloor, y);
            if (currentlyBlocked != isBlocked) {
                return new Fraction(fromRfloor);
            }
            fromRfloor++;
        }
        return toXR;
    }

    /**
     * Searches left until it hits a blocked tile.
     * Returns when it first hits a blocked tile. otherwise it returns toXL.
     * above iff checking up.
     */
    private Fraction restrictLeft(Fraction fromXL, int yUpDown, Fraction toXL, boolean above) {
        CheckFunction isBlocked;
        if (above) {
            isBlocked = (x,y) -> topRightOfBlockedTile(x,y);
        } else {
            isBlocked = (x,y) -> bottomRightOfBlockedTile(x,y);
        }
        
        int fromLfloor = fromXL.floor();
        int toLfloor = toXL.floor();
        
        while (fromLfloor > toLfloor) {
            if (isBlocked.check(fromLfloor, yUpDown)) {
                return new Fraction(fromLfloor);
            }
            fromLfloor--;
        }
        return toXL;
    }

    /**
     * Searches right until it hits a blocked tile.
     * Returns when it first hits a blocked tile. otherwise it returns toXR.
     * above iff checking up.
     */
    private Fraction restrictRight(Fraction fromXR, int currY, Fraction toXR, boolean above) {
        CheckFunction isBlocked;
        if (above) {
            isBlocked = (x,y) -> topLeftOfBlockedTile(x,y);
        } else {
            isBlocked = (x,y) -> bottomLeftOfBlockedTile(x,y);
        }
        
        int fromRceil = fromXR.ceil();
        int toRceil = toXR.ceil();

        while (fromRceil < toRceil) {
            if (isBlocked.check(fromRceil, currY)) {
                return new Fraction(fromRceil);
            }
            fromRceil++;
        }
        return toXR;
    }
    
    
    private void processDirectSuccessors(Interval interval, int currY,
            Fraction xL, Fraction xR, boolean above) {
        
        // Now we split the intervals by blocks.
        LinkedList<Fraction> splitList = splitByBlocks(currY, xL, xR, above);
        
        Fraction intervalLeft = null;
        for (Fraction intervalRight : splitList) {
            if (intervalLeft == null) {
                intervalLeft = intervalRight;
            } else {
                directRelax(interval, currY, intervalLeft, intervalRight);
                intervalLeft = intervalRight;
            }
        }
    }
    

    private void directRelax(Interval interval, int yUp, Fraction intervalLeft,
            Fraction intervalRight) {
        Point curParent = interval.parent;
        relaxUsingPoint(curParent, yUp, intervalLeft, intervalRight);
    }

    private void relaxUsingPoint(Point pivot, int y, Fraction xL, Fraction xR) {
        //float distance = computePointToIntervalDistance(y, xL, xR, pivot);
        //float fValue = distance + getDistance(pivot);
        tryRelax(y, xL, xR, pivot);
    }

    private float computePointToIntervalDistance(int yUp, Fraction intervalLeft,
            Fraction intervalRight, Point point) {
        if (isLessThan(point.x, intervalLeft)) {
            // xL as target
            return Fraction.length(intervalLeft.minus(point.x), yUp - point.y);
        } else if (isLessThanOrEqual(point.x, intervalRight)) {
            // vertically up
            float distance = yUp - point.y;
            if (distance<0) distance = -distance;
            return distance;
        } else {
            // xR as target.
            return Fraction.length(intervalRight.minus(point.x), yUp - point.y);
        }
    }

    private LinkedList<Fraction> splitByBlocksAbove(int yUp, Fraction xL, Fraction xR) {
        return splitByBlocks(yUp, xL, xR, true);
    }

    private LinkedList<Fraction> splitByBlocksBelow(int yDown, Fraction xL, Fraction xR) {
        return splitByBlocks(yDown, xL, xR, false);
    }

    private LinkedList<Fraction> splitByBlocks(int currY, Fraction xL, Fraction xR, boolean above) {
        CheckFunction currentlyBlocked;
        if (above) {
            currentlyBlocked = (a,b) -> bottomLeftOfBlockedTile(a,b);
        } else {
            currentlyBlocked = (a,b) -> topLeftOfBlockedTile(a,b);
        }
        
        LinkedList<Fraction> splitList = new LinkedList<>();
        
        splitList.offer(xL);
        
        if (currY >= graph.sizeY+1) { // there's nothing above.
            splitList.offer(xR);
            return splitList;
        }
                
        int leftFloor = xL.floor();
        int rightFloor = xR.floor();
        boolean currentBlocked = currentlyBlocked.check(leftFloor, currY);
        
        int current = leftFloor+1;
        while (current <= rightFloor) {
            boolean isBlocked = currentlyBlocked.check(current, currY);
            if (currentBlocked != isBlocked) {
                splitList.offer(new Fraction(current));
                currentBlocked = isBlocked;
            }
            current++;
        }
        splitList.offer(xR);
        
        return splitList;
    }

    private void initialiseIntervalSets() {
        intervalSets = new ArrayList<>(graph.sizeY+1);
        for (int i=0; i<graph.sizeY+1; i++) {
            AVLTree<Interval> avlTree = new AVLTree<>();
            intervalSets.add(avlTree);
            if (i != sy) {
                Interval interval = new Interval(i, leftBoundary, rightBoundary);
                pq.add(interval);
                avlTree.insert(interval);
            } else {
                Fraction startX = new Fraction(sx);
                start = new Interval(i, sx);
                pq.add(start);
                
                if (!startX.equals(leftBoundary)) {
                    Interval interval = new Interval(i, leftBoundary, startX);
                    pq.add(interval);
                    avlTree.insert(interval);
                }
                avlTree.insert(start);
                if (!startX.equals(rightBoundary)) {
                    Interval interval = new Interval(i, startX, rightBoundary);
                    pq.add(interval);
                    avlTree.insert(interval);
                }
            }
        }
    }
    
    /*private void printTree(AVLTree<Interval> intervalSet) { // DEBUGGING TOOL
        System.out.println(intervalSet.inorderToString());
        Node<Interval> a = intervalSet.getFirst();
        while (a != null){
            System.out.print(a+ " ");
            a = a.getNext();
        }
        System.out.println();
        a = intervalSet.getLast();
        while (a != null){
            System.out.print(a+ " ");
            a = a.getPrev();
            }
        System.out.println();
    }*/
    
    private void tryRelax(int y, Fraction xL, Fraction xR, Point newParent) {
        if (!xL.isLessThan(xR)) return;
        float gValue = getDistance(newParent);
        
        AVLTree<Interval> intervalSet = intervalSets.get(y);
        Interval left = new Interval(y,xL,xL);
        Interval right = new Interval(y,xR,xR);
        
        Node<Interval> leftMost = intervalSet.ceiling(left);
        while (left.xL.equals(leftMost.getData().xR)) leftMost = leftMost.getNext();
        Node<Interval> rightMost = intervalSet.ceiling(right);
        if (rightMost == null) rightMost = intervalSet.getLast();

        // STEP 1: TRIM VISITED INTERVALS FROM ENDS
        while (leftMost.getData().visited || leftMost.getData().fValue < gValue) {
            // while leftMost.visited && leftMost <= rightMost
            left = leftMost.getData();
            leftMost = leftMost.getNext();
            if (leftMost == null || !leftMost.getData().isLessThanOrEqual(rightMost.getData())) {
                return;
            }
        }
        while (rightMost.getData().visited || leftMost.getData().fValue < gValue) {
            // while rightMost.visited && leftMost <= rightMost
            rightMost = rightMost.getPrev();
            right = rightMost.getData();
            if (rightMost == null || !leftMost.getData().isLessThanOrEqual(rightMost.getData())) {
                return;
            }
        }

        // Nothing to relax.
        if (leftMost == null || rightMost == null || !leftMost.getData().isLessThanOrEqual(rightMost.getData())) {
            return;
        }
        
        // now left.xR = left boundary of unvisited, right.xR = right boundary of unvisited.
        // also, subset is non-empty.
        // STEP 2: INTERVAL SPLITTING. Note: we do not split visited nodes.
        Fraction x1_L = null;
        Fraction x2_L = null;
        Fraction x3_L = null;
        Fraction x1_R = null;
        Fraction x2_R = null;
        Fraction x3_R = null;

        // Check whether left requires splitting
        if (!left.xR.equals(leftMost.getData().xL)) { // left.xR != leftMost.xL
            Interval first = leftMost.getData();
            
            // This means first.xL < left.xR < first.xR
            if (!(first.xL.isLessThan(left.xR) && left.xR.isLessThan(first.xR)))
                throw new UnsupportedOperationException();
            assert first.xL.isLessThan(left.xR) && left.xR.isLessThan(first.xR);
            x1_L = first.xL;
            x2_L = left.xR;
            x3_L = first.xR;
        }
        // Check whether right requires splitting
        if (!right.xR.equals(rightMost.getData().xR)) { // right.xR != last.xR
            Interval last = rightMost.getData();
            
            // This means last.xL < right.xR < last.xR
            assert last.xL.isLessThan(right.xR) && right.xR.isLessThan(last.xR);
            x1_R = last.xL;
            x2_R = right.xR;
            x3_R = last.xR;
        }
        
        if (x1_L != null && x1_R != null && x1_L.equals(x1_R)) { // Special case. Both left and right in same interval.
            assert x3_L.equals(x3_R);
            assert x2_L.isLessThan(x2_R);
            
            // We split this single interval into three.
            float fValue = leftMost.getData().fValue;
            Point parent = leftMost.getData().parent;
            // x1_L=x1_R < x2_L < x2_R < x3_L=x3_R
            Interval in1 = new Interval(y, x1_L, x2_L, fValue, parent);
            Interval in2 = new Interval(y, x2_L, x2_R, fValue, parent);
            Interval in3 = new Interval(y, x2_R, x3_R, fValue, parent);
            //tryRecomputeFValue(in1);
            //tryRecomputeFValue(in2);
            //tryRecomputeFValue(in3);
            intervalSet.delete(in3); // original interval has same xR as in3.
            pq.remove(in3);

            intervalSet.insert(in1);
            intervalSet.insert(in2);
            intervalSet.insert(in3);
            pq.add(in1);
            pq.add(in2);
            pq.add(in3);
            
            assert leftMost.equals(rightMost);
            leftMost = intervalSet.search(in2);
            rightMost = leftMost;
        } else {
            if (x1_L != null) {
                // We split a single interval into two.
                float fValue = leftMost.getData().fValue;
                Point parent = leftMost.getData().parent;
                // x1_L < x2_L < x3_L
                Interval in1 = new Interval(y, x1_L, x2_L, fValue, parent);
                Interval in2 = new Interval(y, x2_L, x3_L, fValue, parent);
                //tryRecomputeFValue(in1);
                //tryRecomputeFValue(in2);
                
                boolean replaceRightmost = rightMost.getData().equals(in2);
                
                intervalSet.delete(in2); // original interval has same xR as in2.
                boolean result = pq.remove(in2); assert result;

                intervalSet.insert(in1);
                intervalSet.insert(in2);
                pq.add(in1);
                pq.add(in2);
                
                leftMost = intervalSet.search(in2);
                if (replaceRightmost) {
                    assert x1_R == null;
                    rightMost = leftMost;
                }
            }
            if (x1_R != null) {
                // We split a single interval into two.
                float fValue = rightMost.getData().fValue;
                Point parent = rightMost.getData().parent;
                // x1_R < x2_R < x3_R
                Interval in1 = new Interval(y, x1_R, x2_R, fValue, parent);
                Interval in2 = new Interval(y, x2_R, x3_R, fValue, parent);
                //tryRecomputeFValue(in1);
                //tryRecomputeFValue(in2);
                
                boolean replaceLeftmost = leftMost.getData().equals(in2);
                
                intervalSet.delete(in2); // original interval has same xR as in2.
                boolean result = pq.remove(in2); assert result;
                
                intervalSet.insert(in1);
                intervalSet.insert(in2);
                pq.add(in1);
                pq.add(in2);
                right = in1;

                rightMost = intervalSet.search(in1);
                if (replaceLeftmost) {
                    assert x1_L == null;
                    leftMost = rightMost;
                }
            }
        }
        
        // Step 3: RELAXATION - FINALLY
        while (leftMost != null && leftMost.getData().isLessThanOrEqual(rightMost.getData())) {
            Interval interval = leftMost.getData();
            if (!interval.visited) {
                tryRelax(interval, newParent, gValue);
            }
            leftMost = leftMost.getNext();
        }
        
    }
    
    
    private void tryRelax(Interval interval, Point newParent, float gValue) {
        float distance = computePointToIntervalDistance(interval.y, interval.xL, interval.xR, newParent);
        float newFValue = gValue + distance;
        if (newFValue < interval.fValue) {
            //System.out.println("RELAX: " + interval + "from " + interval.fValue + " --> " + newFValue); 
            
            boolean result = pq.remove(interval); assert result;
            interval.fValue = newFValue;
            interval.parent = newParent;
            pq.add(interval);
            tryRecordInterval(interval.y, interval.xL, interval.xR, interval.parent);
        }
    }
    
    private void tryRecomputeFValue(Interval interval) {
        if (interval.fValue == Float.POSITIVE_INFINITY) {
            return;
        }
        float gValue = getDistance(interval.parent);
        float distance = computePointToIntervalDistance(interval.y, interval.xL, interval.xR, interval.parent);
        interval.fValue = gValue + distance;
    }
    
    private boolean containsFinishPoint(Interval interval) {
        if (ey != interval.y) return false; // ey == y
        if (interval.xR.isLessThan(ex)) return false; // ex <= xR
        return (interval.xL.isLessThanOrEqual(ex)); // xL <= ex
    }

    private void setDistance(int x, int y, float value, Interval interval) {
        //System.out.println("SET: " + x+","+y + " from " + distance[toOneDimIndex(x, y)] + " --> " + value); 
        if (value < distance[toOneDimIndex(x, y)]) {
            int index = toOneDimIndex(x, y);
            distance[index] = value;
            pointInterval[index] = interval;
        }
    }

    private float getDistance(int x, int y) {
        assert distance[toOneDimIndex(x, y)] != Float.POSITIVE_INFINITY : x + "," + y;
        return distance[toOneDimIndex(x, y)];
    }

    private float getDistance(Point point) {
        return getDistance(point.x, point.y);
    }

    private float distance2D(int x, int y, int parentX, int parentY) {
        int dx = parentX-x;
        int dy = parentY-y;
        return (float)Math.sqrt((dx*dx) + (dy*dy));
    }
    
    private boolean isLessThan(int x, Fraction o) {
        return x*o.d < o.n;
    }
    
    private boolean isLessThanOrEqual(int x, Fraction o) {
        return x*o.d <= o.n;
    }
    
    private boolean topRightOfBlockedTile(int x, int y) {
        return graph.isBlocked(x-1, y-1);
    }

    private boolean topLeftOfBlockedTile(int x, int y) {
        return graph.isBlocked(x, y-1);
    }

    private boolean bottomRightOfBlockedTile(int x, int y) {
        return graph.isBlocked(x-1, y);
    }

    private boolean bottomLeftOfBlockedTile(int x, int y) {
        return graph.isBlocked(x, y);
    }

    private boolean rightOfBlockedTile(int x, int y) {
        return bottomRightOfBlockedTile(x,y) || topRightOfBlockedTile(x,y);
    }

    private boolean leftOfBlockedTile(int x, int y) {
        return bottomLeftOfBlockedTile(x,y) || topLeftOfBlockedTile(x,y);
    }
    
    public int[][] emptyPath() {
        return new int[0][];
    }

    @Override
    public int[][] getPath() {
        LinkedList<int[]> pathList = new LinkedList<>();
        Interval currentInterval = finish;
        if (currentInterval == null) {
            return emptyPath();
        }

        int[] node = new int[2];
        node[0] = ex;
        node[1] = ey;
        pathList.add(node);
        
        Point current = currentInterval.parent;
        while (!currentInterval.isStartPoint) {
            currentInterval = pointInterval[toOneDimIndex(current.x, current.y)];

            node = new int[2];
            node[0] = current.x;
            node[1] = current.y;
            pathList.add(node);
            
            current = currentInterval.parent;
        }
        
        int[][] path = new int[pathList.size()][];
        return pathList.toArray(path);
    }

    protected float getPathLength() {
        throw new UnsupportedOperationException("Not implemented yet...");
    }
    
    private void tryRecordInterval(int y, Fraction xL, Fraction xR, Point parent) {
        if (recordList != null) {
            recordList.add(new ExploredInterval(xR,xL,y, parent));
            maybeSaveSearchSnapshot();
        }
    }
    private void tryRecordExploredInterval(int y, Fraction xL, Fraction xR, Point parent) {
        if (exploredList != null) {
            exploredList.add(new ExploredInterval(xR,xL,y, parent));
            maybeSaveSearchSnapshot();
        }
    }
    
    @Override
    public void startRecording() {
        super.startRecording();
        exploredList = new ArrayList<>();
        recordList = new ArrayList<>();
    }

    @Override
    protected List<SnapshotItem> computeSearchSnapshot() {
        ArrayList<SnapshotItem> list = new ArrayList<>(exploredList.size());

        for (ExploredInterval in : recordList) {
            // y, xLn, xLd, xRn, xRd, px, py
            
            Integer[] line = new Integer[7];
            line[0] = in.y;
            line[1] = in.xL.n;
            line[2] = in.xL.d;
            line[3] = in.xR.n;
            line[4] = in.xR.d;
            line[5] = in.parent.x;
            line[6] = in.parent.y;
            list.add(new SnapshotItem(line));
        }
        for (ExploredInterval in : exploredList) {
            // y, xLn, xLd, xRn, xRd, px, py
            Integer[] line = new Integer[5];
            line[0] = in.y;
            line[1] = in.xL.n;
            line[2] = in.xL.d;
            line[3] = in.xR.n;
            line[4] = in.xR.d;
            list.add(new SnapshotItem(line));
        }
        
        return list;
    }

    /**
     * Debugging tool : abruptly terminate the algorithm.
     */
    private void crash() {
        throw new UnsupportedOperationException("Terminated");
    }

    /**
     * Debugging tool : set a break point.
     */
    private void breakPoint() {
    }
    
    private int counter = 0;
    /**
     * Debugging tool : call the function (e.g. break point) after this method
     * has been called triggerAtThisNumber times.
     */
    private void count(int triggerAtThisNumber, Runnable function) {
        counter++;
        if (counter == triggerAtThisNumber) {
            //System.out.println("Trigger: " + counter);
            function.run();
        } else {
            //System.out.println("Count: " + counter);
        }
    }
}

class ExploredInterval {
    public final Fraction xL;
    public final Fraction xR;
    public final int y;
    public final Point parent;
    
    public ExploredInterval(Fraction xL, Fraction xR, int y, Point parent) {
        this.xL = xL;
        this.xR = xR;
        this.y = y;
        this.parent = parent;
    }
}