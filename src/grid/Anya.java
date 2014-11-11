package grid;

import grid.anya.Fraction;
import grid.anya.Interval;
import grid.anya.Point;
import grid.bst.AVLTree;
import grid.bst.Node;

import java.util.LinkedList;

public class Anya extends PathFindingAlgorithm{

    private AVLTree<Interval>[] intervalSets;
    private float[] distance;
    
    private Interval start;
    
    private final Fraction rightBoundary;
    private final Fraction leftBoundary;
    
    public Anya(GridGraph graph, int sizeX, int sizeY, int sx, int sy, int ex,
            int ey) {
        super(graph, sizeX, sizeY, sx, sy, ex, ey);
        leftBoundary = new Fraction(0);
        rightBoundary = new Fraction(sizeY+1);
        
    }

    @Override
    public void computePath() {
        int totalSize = (graph.sizeX+1) * (graph.sizeY+1);
        initialiseIntervalSets();
        distance = new float[totalSize];
        for (int i=0;i<totalSize;i++) {
            distance[i] = Float.POSITIVE_INFINITY;
        }
        setDistance(sx, sy, 0);
    }
    
    private void explore(Interval interval) {
        assert !interval.visited;
        interval.visited = true;
        
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
        if (interval.xL.isLessThan(curParent.x)) {
            searchLeftDirect(interval, curParent);
        }
        if (isLessThan(curParent.x, interval.xR)) {
            searchRightDirect(interval, curParent);
        }

        int yUp = curParent.y+1;
        int yDown = curParent.y-1;
        
        if (interval.xL.isWholeNumber()) {
            int pivotX = interval.xL.n;
            int pivotY = interval.y;
            Point pivot = new Point(pivotX, pivotY);
            if (curParent.equals(pivot)) { // case: root is at pivot...?
                if (yUp <= graph.sizeY+1) {
                    Fraction currX = new Fraction(pivotX);
                    exploreUpBothWays(interval, pivot, currX);
                }
                if (yDown >= 0) {
                    Fraction currX = new Fraction(pivotX);
                    exploreDownBothWays(interval, pivot, currX);
                }
            } else if (yUp <= graph.sizeY+1 && bottomLeftOfBlockedTile(pivotX, pivotY)) {
                // Pivot up
                setGValueOfLeftEndpoint(interval);
                exploreUpLeft(interval, pivot, interval.xL);
                
            } else if (yDown >= 0 && topLeftOfBlockedTile(pivotX, pivotY)) { // cannot happen simultaneously...?
                // Pivot down
                setGValueOfLeftEndpoint(interval);
                exploreDownLeft(interval, pivot, interval.xL);
            }
        }
        if (interval.xR.isWholeNumber()) {
            int pivotX = interval.xR.n;
            int pivotY = interval.y;
            Point pivot = new Point(pivotX, pivotY);
            if (curParent.equals(pivot)) { // case: root is at pivot...?
                if (!interval.xL.equals(interval.xR)) {
                    if (yUp <= graph.sizeY+1) {
                        Fraction currX = new Fraction(pivotX);
                        exploreUpBothWays(interval, pivot, currX);
                    }
                    if (yDown >= 0) {
                        Fraction currX = new Fraction(pivotX);
                        exploreDownBothWays(interval, pivot, currX);
                    }
                }
            } else if (yUp <= graph.sizeY+1 && bottomRightOfBlockedTile(pivotX, pivotY)) {
                // Pivot up
                setGValueOfRightEndpoint(interval);
                exploreUpRight(interval, pivot, interval.xR);
                
            } else if (yDown >= 0 && topRightOfBlockedTile(pivotX, pivotY)) { // cannot happen simultaneously...?
                // Pivot down
                setGValueOfRightEndpoint(interval);
                exploreDownRight(interval, pivot, interval.xR);
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
        if (bottomLeftOfBlockedTile(interval.xL.floor(), interval.y)) { // blocked above.
            xL = interval.xL;
            xR = interval.xR;
        } else {
            // explore up.
            int dy = interval.y - curParent.y;
            
            xL = interval.xL.minus(curParent.x);
            xL = xL.multiplyDivide(dy+1,dy); // *= (dy+1)/dy
            xL = xL.plus(curParent.x);
                
            xR = interval.xR.minus(curParent.x);
            xR = xR.multiplyDivide(dy+1,dy); // *= (dy+1)/dy
            xR = xR.plus(curParent.x);
   
            xL = restrictLeft(interval.xL, yUp, xL, isUpwards);
            xR = restrictRight(interval.xR, yUp, xR, isUpwards);
                
            processDirectSuccessors(interval, yUp, xL, xR, isUpwards);
        }
 //     }
        if (interval.xL.isWholeNumber()) {
            int pivotX = interval.xL.n;
            int pivotY = interval.y;
            if (bottomLeftOfBlockedTile(pivotX, pivotY)) {
                setGValueOfLeftEndpoint(interval);
                Point pivot = new Point(pivotX, pivotY);
                exploreUpLeft(interval, pivot, xL);
                
            } else if (topRightOfBlockedTile(pivotX, pivotY) != bottomRightOfBlockedTile(pivotX, pivotY)) { //XOR
                setGValueOfLeftEndpoint(interval);
                Point pivot = new Point(pivotX, pivotY);
                
                if (topRightOfBlockedTile(pivotX, pivotY)) {
                    searchLeft(interval, pivot);
                }
                //if (yUp <= graph.sizeY+1) {
                exploreUpLeft(interval, pivot, xL);
                //}
            }
            
        }
        if (interval.xR.isWholeNumber()) {
            int pivotX = interval.xR.n;
            int pivotY = interval.y;
            if (bottomRightOfBlockedTile(pivotX, pivotY)) {
                setGValueOfLeftEndpoint(interval);
                Point pivot = new Point(pivotX, pivotY);
                exploreUpRight(interval, pivot, xR);
                
            } else if (topLeftOfBlockedTile(pivotX, pivotY) != bottomLeftOfBlockedTile(pivotX, pivotY)) { //XOR
                setGValueOfRightEndpoint(interval);
                Point pivot = new Point(pivotX, pivotY);

                if (topLeftOfBlockedTile(pivotX, pivotY)) {
                    searchRight(interval, pivot);
                }
                //if (yUp <= graph.sizeY+1) {
                exploreUpRight(interval, pivot, xR);
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
        
        if (topLeftOfBlockedTile(interval.xL.floor(), interval.y)) { // blocked below.
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
   
            xL = restrictLeft(interval.xL, yDown, xL, isUpwards);
            xR = restrictRight(interval.xR, yDown, xR, isUpwards);

            processDirectSuccessors(interval, yDown, xL, xR, isUpwards);
        }
        if (interval.xL.isWholeNumber()) {
            int pivotX = interval.xL.n;
            int pivotY = interval.y;
            if (topLeftOfBlockedTile(pivotX, pivotY)) {
                setGValueOfLeftEndpoint(interval);
                Point pivot = new Point(pivotX, pivotY);
                exploreDownLeft(interval, pivot, xL);
                
            } else if (bottomRightOfBlockedTile(pivotX, pivotY) != topRightOfBlockedTile(pivotX, pivotY)) { //XOR
                setGValueOfLeftEndpoint(interval);
                Point pivot = new Point(pivotX, pivotY);
                
                if (bottomRightOfBlockedTile(pivotX, pivotY)) {
                    searchLeft(interval, pivot);
                }
                exploreDownLeft(interval, pivot, xL);
            }
        }
        if (interval.xR.isWholeNumber()) {
            int pivotX = interval.xR.n;
            int pivotY = interval.y;
            if (topRightOfBlockedTile(pivotX, pivotY)) {
                setGValueOfRightEndpoint(interval);
                Point pivot = new Point(pivotX, pivotY);
                exploreDownRight(interval, pivot, xR);
                
            } else if (bottomLeftOfBlockedTile(pivotX, pivotY) != topLeftOfBlockedTile(pivotX, pivotY)) { //XOR
                setGValueOfRightEndpoint(interval);
                Point pivot = new Point(pivotX, pivotY);

                if (bottomLeftOfBlockedTile(pivotX, pivotY)) {
                    searchRight(interval, pivot);
                }
                exploreDownRight(interval, pivot, xR);
            }
        }
    }

    private void exploreUpLeft(Interval interval, Point pivot, Fraction xL) {
        int yUp = pivot.y+1;
        Fraction leftEnd = restrictLeft(xL, yUp, leftBoundary, true);
        if (!leftEnd.isLessThan(xL)) return;

        LinkedList<Fraction> splitList = splitByBlocksAbove(yUp, leftEnd, xL);

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

    private void exploreUpRight(Interval interval, Point pivot, Fraction xR) {
        int yUp = pivot.y+1;
        Fraction rightEnd = restrictRight(xR, yUp, rightBoundary, true);
        if (!xR.isLessThan(rightEnd)) return;

        LinkedList<Fraction> splitList = splitByBlocksAbove(yUp, xR, rightEnd);

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

    private void exploreDownLeft(Interval interval, Point pivot, Fraction xL) {
        int yDown = pivot.y-1;
        Fraction leftEnd = restrictLeft(xL, yDown, leftBoundary, false);
        if (!leftEnd.isLessThan(xL)) return;

        LinkedList<Fraction> splitList = splitByBlocksAbove(yDown, leftEnd, xL);

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

    private void exploreDownRight(Interval interval, Point pivot, Fraction xR) {
        int yDown = pivot.y-1;
        Fraction rightEnd = restrictRight(xR, yDown, rightBoundary, false);
        if (!xR.isLessThan(rightEnd)) return;

        LinkedList<Fraction> splitList = splitByBlocksBelow(yDown, xR, rightEnd);

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
        Interval left = new Interval(pivot.y, pivotX, pivotX, 0);
        Node<Interval> fromNode = intervalSets[pivot.y].search(left);
        
        if (fromNode == null) {
            return;
        }
        
        left = fromNode.getData();
        assert left.xR == pivotX;
        if (left.visited) {
            return;
        }
        
        Fraction xL = leftSearchTillCorner(pivotX, pivot.y, left.xL);
        Fraction xR = pivotX;

        float distance = getDistance(pivot);
        float fValue = distance - xL.minus(pivot.x).toFloat(); // distance + (pivot.X - xL)
        tryRelax(pivot.y, xL, xR, pivot, fValue);
    }

    
    private void searchRight(Interval interval, Point pivot) {
        Fraction pivotX = new Fraction(pivot.x);
        Interval right = new Interval(pivot.y, pivotX, pivotX, 0);
        Node<Interval> fromNode = intervalSets[pivot.y].search(right);
        assert fromNode != null;
        
        if (fromNode.getNext() == null) {
            return;
        }
        
        right = fromNode.getNext().getData();
        assert right.xL == pivotX;
        if (right.visited) {
            return;
        }

        Fraction xL = pivotX;
        Fraction xR = rightSearchTillCorner(pivotX, pivot.y, right.xR);

        float distance = getDistance(pivot);
        float fValue = distance + xR.minus(pivot.x).toFloat(); // distance + (xR - pivot.x)
        tryRelax(pivot.y, xL, xR, pivot, fValue);
    }

    private void searchLeftDirect(Interval interval, Point pivot) {
        Fraction leftEndpoint = interval.xL;
        Interval left = new Interval(pivot.y, leftEndpoint, leftEndpoint, 0);
        Node<Interval> fromNode = intervalSets[pivot.y].search(left);
        
        if (fromNode == null) {
            return;
        }
        
        left = fromNode.getData();
        assert left.xR == leftEndpoint;
        if (left.visited) {
            return;
        }
        
        Fraction xL = leftSearchTillCorner(leftEndpoint, pivot.y, left.xL);
        Fraction xR = leftEndpoint;

        float distance = getDistance(pivot);
        float fValue = distance - xL.minus(pivot.x).toFloat(); // distance + (pivot.X - xL)
        tryRelax(pivot.y, xL, xR, pivot, fValue);
    }

    private void searchRightDirect(Interval interval, Point pivot) {
        Fraction rightEndpoint = interval.xR;
        Interval right = new Interval(pivot.y, rightEndpoint, rightEndpoint, 0);
        Node<Interval> fromNode = intervalSets[pivot.y].search(right);
        assert fromNode != null;
        
        if (fromNode.getNext() == null) {
            return;
        }
        
        right = fromNode.getNext().getData();
        assert right.xL == rightEndpoint;
        if (right.visited) {
            return;
        }

        Fraction xL = rightEndpoint;
        Fraction xR = rightSearchTillCorner(rightEndpoint, pivot.y, right.xR);

        float distance = getDistance(pivot);
        float fValue = distance + xR.minus(pivot.x).toFloat(); // distance + (xR - pivot.x)
        tryRelax(pivot.y, xL, xR, pivot, fValue);
    }

    private void setGValueOfLeftEndpoint(Interval interval) {
        if (interval.xL.isWholeNumber()) {
            int x = interval.xL.n;
            int y = interval.y;
            int parentX = interval.parent.x;
            int parentY = interval.parent.y;
            float gValue = getDistance(parentX, parentY);
            gValue += distance2D(x, y, parentX, parentY);
            setDistance(x, y, gValue);
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
            setDistance(x, y, gValue);
        }
    }
    
    /**
     * Goes left until it hits a corner of a tile, then returns.
     */
    private Fraction leftSearchTillCorner(Fraction fromXL, int y, Fraction toXL) {
        int fromLceil = fromXL.ceil();
        int toLfloor = toXL.floor();
        
        boolean currentlyBlocked = rightOfBlockedTile(fromLceil, y);
        fromLceil--;
        
        while (fromLceil > toLfloor) {
            boolean isBlocked = rightOfBlockedTile(fromLceil, y);
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
        
        boolean currentlyBlocked = rightOfBlockedTile(fromRfloor, y);
        fromRfloor++;
        
        while (fromRfloor < toRceil) {
            boolean isBlocked = rightOfBlockedTile(fromRfloor, y);
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
     * above <=> checking up.
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
     * above <=> checking up.
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
        float distance = computePointToIntervalDistance(y, xL, xR, pivot);
        float fValue = distance + getDistance(pivot);
        tryRelax(y, xL, xR, pivot, fValue);
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
        intervalSets = (AVLTree<Interval>[])(new Object[graph.sizeY+1]);
        for (int i=0; i<intervalSets.length; i++) {
            intervalSets[i] = new AVLTree<>();
            if (i != sy) {
                intervalSets[i].insert(new Interval(i, leftBoundary, rightBoundary, Float.POSITIVE_INFINITY));
            } else {
                Fraction startX = new Fraction(sx);
                start = new Interval(i, sx);
                
                if (!startX.equals(leftBoundary)) {
                    intervalSets[i].insert(new Interval(i, leftBoundary, startX, Float.POSITIVE_INFINITY));
                }
                intervalSets[i].insert(start);
                if (!startX.equals(rightBoundary)) {
                    intervalSets[i].insert(new Interval(i, startX, rightBoundary, Float.POSITIVE_INFINITY));
                }
            }
        }
    }
    
    
    private void tryRelax(int y, Fraction xL, Fraction xR, Point newParent, float newFValue) {
        if (xL.equals(xR)) return;
        
        AVLTree<Interval> intervalSet = intervalSets[y];
        Interval left = new Interval(y,xL,xL, 0);
        Interval right = new Interval(y,xR,xR, 0);

        Node<Interval> leftMost = intervalSet.ceiling(left);
        if (left.equals(leftMost)) leftMost = leftMost.getNext();
        Node<Interval> rightMost = intervalSet.ceiling(right);

        // STEP 1: TRIM VISITED INTERVALS FROM ENDS
        while (leftMost.getData().visited || leftMost.getData().fValue < newFValue) {
            // while leftMost.visited && leftMost <= rightMost
            left = leftMost.getData();
            leftMost = leftMost.getNext();
            if (leftMost == null || !leftMost.getData().isLessThanOrEqual(rightMost.getData())) {
                return;
            }
        }
        while (rightMost.getData().visited || leftMost.getData().fValue < newFValue) {
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
            // x1_L=x1_R < x2_L < x2_R < x3_L=x3_R
            Interval in1 = new Interval(y, x1_L, x2_L, fValue);
            Interval in2 = new Interval(y, x2_L, x2_R, fValue);
            Interval in3 = new Interval(y, x2_R, x3_R, fValue);
            intervalSet.delete(in3); // original interval has same xR as in3.

            intervalSet.insert(in1);
            intervalSet.insert(in2);
            intervalSet.insert(in3);
            
            assert leftMost.equals(rightMost);
            leftMost = intervalSet.search(in2);
            rightMost = leftMost;
        } else {
            if (x1_L != null) {
                // We split a single interval into two.
                float fValue = leftMost.getData().fValue;
                // x1_L < x2_L < x3_L
                Interval in1 = new Interval(y, x1_L, x2_L, fValue);
                Interval in2 = new Interval(y, x2_L, x3_L, fValue);
                
                intervalSet.delete(in2); // original interval has same xR as in2.

                intervalSet.insert(in1);
                intervalSet.insert(in2);
                
                leftMost = intervalSet.search(in2);
                if (rightMost.getData().equals(in2)) {
                    assert x1_R == null;
                    rightMost = leftMost;
                }
            }
            if (x1_R != null) {
                // We split a single interval into two.
                float fValue = rightMost.getData().fValue;
                // x1_R < x2_R < x3_R
                Interval in1 = new Interval(y, x1_R, x2_R, fValue);
                Interval in2 = new Interval(y, x2_R, x3_R, fValue);
                intervalSet.delete(in2); // original interval has same xR as in2.

                intervalSet.insert(in1);
                intervalSet.insert(in2);
                right = in1;

                rightMost = intervalSet.search(in1);
                if (leftMost.getData().equals(in2)) {
                    assert x1_L == null;
                    leftMost = rightMost;
                }
            }
        }
        
        // Step 3: RELAXATION - FINALLY
        while (leftMost.getData().isLessThanOrEqual(rightMost.getData())) {
            Interval interval = leftMost.getData();
            if (!interval.visited) {
                tryRelax(interval, newParent, newFValue);
            }
        }
        
    }
    
    
    private void tryRelax(Interval interval, Point newParent, float newFValue) {
        if (newFValue < interval.fValue) {
            interval.fValue = newFValue;
            interval.parent = newParent;
        }
    }

    private void setDistance(int x, int y, float value) {
        assert distance[toOneDimIndex(x, y)] == Float.POSITIVE_INFINITY;
        distance[toOneDimIndex(x, y)] = value;
    }

    private float getDistance(int x, int y) {
        assert distance[toOneDimIndex(x, y)] != Float.POSITIVE_INFINITY;
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
        return graph.isBlocked(x-1, y);
    }

    private boolean rightOfBlockedTile(int x, int y) {
        return bottomRightOfBlockedTile(x,y) || topRightOfBlockedTile(x,y);
    }

    private boolean leftOfBlockedTile(int x, int y) {
        return bottomLeftOfBlockedTile(x,y) || topLeftOfBlockedTile(x,y);
    }
    
    private interface CheckFunction {
        public boolean check(int x, int y);
    }
}