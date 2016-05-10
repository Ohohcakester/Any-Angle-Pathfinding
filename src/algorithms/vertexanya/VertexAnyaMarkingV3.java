package algorithms.vertexanya;

import grid.GridGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import algorithms.PathFindingAlgorithm;
import algorithms.anya.Fraction;
import algorithms.datatypes.Memory;
import algorithms.datatypes.SnapshotItem;
import algorithms.priorityqueue.FastVariableSizeIndirectHeap;
import algorithms.priorityqueue.ReusableIndirectHeap;

public class VertexAnyaMarkingV3 extends PathFindingAlgorithm {
    private static final float EPSILON = 0.0001f;
    
    private final Fraction RIGHT_END;
    private final Fraction LEFT_END;
    
    private ScanInterval[] states;
    private int[] checkArray;
    private FastVariableSizeIndirectHeap statesPQ;
    private ReusableIndirectHeap vertexPQ;
    private Memory memory;
    
    private int start;
    private int finish;

    public VertexAnyaMarkingV3(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey);
        RIGHT_END = new Fraction(graph.sizeX+1);
        LEFT_END = new Fraction(-1);
    }

    @Override
    public void computePath() {
        int totalSize = (graph.sizeX+1) * (graph.sizeY+1);
        this.initialiseMemory(totalSize, Float.POSITIVE_INFINITY, -1, false);

        start = graph.toOneDimIndex(sx, sy);
        finish = graph.toOneDimIndex(ex, ey);

        vertexPQ = new ReusableIndirectHeap(totalSize);
        statesPQ = new FastVariableSizeIndirectHeap();
        states = new ScanInterval[11];
        checkArray = new int[11];
        
        memory.setDistance(start, 0);
        memory.setParent(start, -1);
        memory.setVisited(start, true);
        if (start == finish) return;
        generateStartSuccessors();

        while (!vertexPQ.isEmpty() || !statesPQ.isEmpty()) {
            if (!vertexPQ.isEmpty() && (statesPQ.isEmpty() || vertexPQ.getMinValue() + EPSILON < statesPQ.getMinValue())) {
                // Pop vertex
                maybeSaveSearchSnapshot();
                int current = vertexPQ.popMinIndex();
                memory.setVisited(current, true);
                
                if (current == finish) {
                    // Done
                    break;
                }
                //System.out.println("Generate " + toTwoDimX(current) + ", " + toTwoDimY(current) + " from " + toTwoDimX(memory.parent(current)) + ", " + toTwoDimY(memory.parent(current)));
                generateSuccessors(current, memory.parent(current));
            } else {
                // Pop state
                maybeSaveSearchSnapshot();
                int currentID = statesPQ.popMinIndex();
                ScanInterval currState = states[currentID];
                //System.out.println("Generate " + currState);

                generateSuccessors(currState);
            }
        }
    }

    private final void addToOpen(int baseIndex, ScanInterval successor) {
        //System.out.println("ADDTOOPEN " + successor);
        // set heuristic and f-value
        float hValue = heuristic(successor);
        float fValue = memory.distance(baseIndex) + hValue;
        
        int handle = statesPQ.insert(fValue);
        if (handle >= states.length) {
            states = Arrays.copyOf(states, states.length*2);
        }
        states[handle] = successor;
    }
    
    private final void tryRelax(int parentIndex, int parX, int parY, int x, int y) {
        //System.out.println("RELAX " + x + ", " + y);
        // return true iff relaxation is done.
        int targetIndex = graph.toOneDimIndex(x, y);
        if (memory.visited(targetIndex)) return;
        
        float newWeight = memory.distance(parentIndex) + graph.distance(parX, parY, x, y);
        if (newWeight < memory.distance(targetIndex)) {
            if (graph.isOuterCorner(x, y) || (y == ey && x == ex)) {
                memory.setParent(targetIndex, parentIndex);
                vertexPQ.decreaseKey(targetIndex, newWeight + graph.distance(x, y, ex, ey));
            }
        }
    }
    
   
    private final void generateSuccessors(ScanInterval currState) {
        exploreState(currState);
    }

    private final void generateStartSuccessors() {
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

            this.generateUpwards(leftExtent, rightExtent, sx, sy, sy, true, true);
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

            this.generateDownwards(leftExtent, rightExtent, sx, sy, sy, true, true);
        }

        // Search leftwards
        if (!topRightOfBlocked || !bottomRightOfBlocked) {
            leftAnyExtentMarking(sx, sy, 0, start);
        }

        // Search rightwards
        if (!topLeftOfBlocked || !bottomLeftOfBlocked) {
            rightAnyExtentMarking(sx, sy, 0, start);
        }
    }
    

    /**
     * Assumption: We are at an outer corner. One of six cases:
     *   BR        BL        TR        TL       TRBL      TLBR
     * XXX|         |XXX      :         :         |XXX   XXX|
     * XXX|...   ...|XXX   ___:...   ...:___   ___|XXX   XXX|___
     *    :         :      XXX|         |XXX   XXX|         |XXX
     *    :         :      XXX|         |XXX   XXX|         |XXX
     *    
     * Assumption: We are also entering from a taut direction.
     * dx > 0, dy > 0 : BR TL
     * dx > 0, dy < 0 : BL TR
     * dx < 0, dy < 0 : BR TL
     * dx < 0, dy > 0 : BL TR
     */
    private final void generateSuccessors(int current, int parent) {
        int baseX = graph.toTwoDimX(current);
        int baseY = graph.toTwoDimY(current);
        int dx = baseX - graph.toTwoDimX(parent);
        int dy = baseY - graph.toTwoDimY(parent);
        
        boolean rightwardsSearch = false;
        boolean leftwardsSearch = false;
        
        if (dx > 0) {
            // Moving rightwards
            if (dy > 0) {
                if (graph.bottomLeftOfBlockedTile(baseX, baseY)) return;
                //    P
                //   /
                //  B
                boolean brOfBlocked = graph.bottomRightOfBlockedTile(baseX, baseY);
                boolean tlOfBlocked = graph.topLeftOfBlockedTile(baseX, baseY);
                
                int rightBound = rightUpExtent(baseX,baseY);
                Fraction leftExtent;
                Fraction rightExtent;
                
                if (brOfBlocked && tlOfBlocked) {
                    //  |
                    //  |___
                    
                    leftExtent = new Fraction(baseX);
                    rightExtent = new Fraction(rightBound);
                    
                    rightwardsSearch = true;
                } else if (brOfBlocked) {
                    //  | /
                    //  |/
                    
                    leftExtent = new Fraction(baseX);
                    rightExtent = new Fraction(baseX*dy + dx, dy);
                    if (!rightExtent.isLessThanOrEqual(rightBound)) { // rightBound < rightExtent
                        rightExtent = new Fraction(rightBound);
                    }
                    
                } else { // tlOfBlocked
                    //   /
                    //  /__
                    
                    leftExtent = new Fraction(baseX*dy + dx, dy);
                    rightExtent = new Fraction(rightBound);
                    
                    rightwardsSearch = true;
                }
                
                if (leftExtent.isLessThanOrEqual(rightExtent)) {
                    this.generateUpwards(leftExtent, rightExtent, baseX, baseY, baseY, true, true);
                }
                
            } else if (dy < 0) {
                if (graph.topLeftOfBlockedTile(baseX, baseY)) return;
                //  B
                //   \
                //    P
                boolean trOfBlocked = graph.topRightOfBlockedTile(baseX, baseY);
                boolean blOfBlocked = graph.bottomLeftOfBlockedTile(baseX, baseY);
                
                int rightBound = rightDownExtent(baseX,baseY);
                Fraction leftExtent;
                Fraction rightExtent;
                
                if (trOfBlocked && blOfBlocked) {
                    //  ____
                    //  |
                    //  |
                    
                    leftExtent = new Fraction(baseX);
                    rightExtent = new Fraction(rightBound);
                    
                    rightwardsSearch = true;
                } else if (trOfBlocked) {
                    //  .
                    //  |\
                    //  | \
                    
                    leftExtent = new Fraction(baseX);
                    rightExtent = new Fraction(baseX*-dy + dx, -dy);
                    if (!rightExtent.isLessThanOrEqual(rightBound)) { // rightBound < rightExtent
                        rightExtent = new Fraction(rightBound);
                    }
                    
                } else { // blOfBlocked
                    //  ___
                    //  \
                    //   \
                    leftExtent = new Fraction(baseX*-dy + dx, -dy);
                    rightExtent = new Fraction(rightBound);
                    
                    rightwardsSearch = true;
                }
                
                if (leftExtent.isLessThanOrEqual(rightExtent)) {
                    this.generateDownwards(leftExtent, rightExtent, baseX, baseY, baseY, true, true);
                }
                
                
            } else { // dy == 0
                
                // B--P

                if (graph.bottomRightOfBlockedTile(baseX, baseY)) {
                    // |
                    // |___

                    Fraction leftExtent = new Fraction(baseX);
                    Fraction rightExtent = new Fraction(rightUpExtent(baseX,baseY));
                    this.generateUpwards(leftExtent, rightExtent, baseX, baseY, baseY, true, true);
                    
                } else if (graph.topRightOfBlockedTile(baseX, baseY)) { // topRightOfBlockedTile
                    // ____
                    // |
                    // |

                    Fraction leftExtent = new Fraction(baseX);
                    Fraction rightExtent = new Fraction(rightDownExtent(baseX,baseY));
                    this.generateDownwards(leftExtent, rightExtent, baseX, baseY, baseY, true, true);
                }
                
                rightwardsSearch = true;
            }
            
        } else if (dx < 0) {
            // Moving leftwards
            
            if (dy > 0) {
                if (graph.bottomRightOfBlockedTile(baseX, baseY)) return;
                //  P
                //   \
                //    B
                boolean blOfBlocked = graph.bottomLeftOfBlockedTile(baseX, baseY);
                boolean trOfBlocked = graph.topRightOfBlockedTile(baseX, baseY);
                
                int leftBound = leftUpExtent(baseX,baseY);
                Fraction leftExtent;
                Fraction rightExtent;
                
                if (blOfBlocked && trOfBlocked) {
                    //     |
                    //  ___|
                    
                    leftExtent = new Fraction(leftBound);
                    rightExtent = new Fraction(baseX);
                    
                    leftwardsSearch = true;
                } else if (blOfBlocked) {
                    //  \ |
                    //   \|
                    
                    leftExtent = new Fraction(baseX*dy + dx, dy);
                    rightExtent = new Fraction(baseX);
                    if (leftExtent.isLessThan(leftBound)) { // leftExtent < leftBound
                        leftExtent = new Fraction(leftBound);
                    }
                    
                } else { // trOfBlocked
                    //   \
                    //  __\
                    
                    leftExtent = new Fraction(leftBound);
                    rightExtent = new Fraction(baseX*dy + dx, dy);
                    
                    leftwardsSearch = true;
                }
                
                if (leftExtent.isLessThanOrEqual(rightExtent)) {
                    this.generateUpwards(leftExtent, rightExtent, baseX, baseY, baseY, true, true);
                }
                
            } else if (dy < 0) {
                if (graph.topRightOfBlockedTile(baseX, baseY)) return;
                //    B
                //   /
                //  P
                boolean tlOfBlocked = graph.topLeftOfBlockedTile(baseX, baseY);
                boolean brOfBlocked = graph.bottomRightOfBlockedTile(baseX, baseY);
                
                int leftBound = leftDownExtent(baseX,baseY);
                Fraction leftExtent;
                Fraction rightExtent;
                
                if (tlOfBlocked && brOfBlocked) {
                    //  ____
                    //     |
                    //     |
                    
                    leftExtent = new Fraction(leftBound);
                    rightExtent = new Fraction(baseX);
                    
                    leftwardsSearch = true;
                } else if (tlOfBlocked) {
                    //   /|
                    //  / |
                    
                    leftExtent = new Fraction(baseX*-dy + dx, -dy);
                    rightExtent = new Fraction(baseX);
                    if (leftExtent.isLessThan(leftBound)) { // leftExtent < leftBound
                        leftExtent = new Fraction(leftBound);
                    }
                    
                } else { // brOfBlocked
                    //  ___
                    //    /
                    //   /
                    
                    leftExtent = new Fraction(leftBound);
                    rightExtent = new Fraction(baseX*-dy + dx, -dy);
                    
                    leftwardsSearch = true;
                }
                
                if (leftExtent.isLessThanOrEqual(rightExtent)) {
                    this.generateDownwards(leftExtent, rightExtent, baseX, baseY, baseY, true, true);
                }
                
                
            } else { // dy == 0
                
                // P--B

                if (graph.bottomLeftOfBlockedTile(baseX, baseY)) {
                    //    |
                    // ___|

                    Fraction leftExtent = new Fraction(leftUpExtent(baseX,baseY));
                    Fraction rightExtent = new Fraction(baseX);
                    this.generateUpwards(leftExtent, rightExtent, baseX, baseY, baseY, true, true);
                    
                } else if (graph.topLeftOfBlockedTile(baseX, baseY)) {
                    // ____
                    //    |
                    //    |

                    Fraction leftExtent = new Fraction(leftDownExtent(baseX,baseY));
                    Fraction rightExtent = new Fraction(baseX);
                    this.generateDownwards(leftExtent, rightExtent, baseX, baseY, baseY, true, true);
                }
                
                leftwardsSearch = true;
                
            }
        } else { // dx == 0
            // Direct upwards or direct downwards.
            if (dy > 0) {
                // Direct upwards
                
                //  P
                //  |
                //  B
                
                if (graph.topLeftOfBlockedTile(baseX, baseY)) {
                    // |
                    // |___

                    Fraction leftExtent = new Fraction(baseX);
                    Fraction rightExtent = new Fraction(rightUpExtent(baseX,baseY));
                    this.generateUpwards(leftExtent, rightExtent, baseX, baseY, baseY, true, true);

                    rightwardsSearch = true;
                    
                } else if (graph.topRightOfBlockedTile(baseX, baseY)) {
                    //    |
                    // ___|

                    Fraction leftExtent = new Fraction(leftUpExtent(baseX,baseY));
                    Fraction rightExtent = new Fraction(baseX);
                    this.generateUpwards(leftExtent, rightExtent, baseX, baseY, baseY, true, true);

                    leftwardsSearch = true;
                    
                } else {
                    int y = baseY+1;
                    if (!graph.bottomLeftOfBlockedTile(baseX, y) || !graph.bottomRightOfBlockedTile(baseX, y)) {
                        Fraction x = new Fraction(baseX);
                        addToOpen(current, new ScanInterval(baseX, baseY, y, x, x, ScanInterval.BOTH_INCLUSIVE));
                    }
                }
                
            } else { // dy < 0
                // Direct downwards
                
                //  B
                //  |
                //  P

                if (graph.bottomLeftOfBlockedTile(baseX, baseY)) {
                    // ____
                    // |
                    // |

                    Fraction leftExtent = new Fraction(baseX);
                    Fraction rightExtent = new Fraction(rightDownExtent(baseX,baseY));
                    this.generateDownwards(leftExtent, rightExtent, baseX, baseY, baseY, true, true);
                    
                    rightwardsSearch = true;
                    
                } else if (graph.bottomRightOfBlockedTile(baseX, baseY)) {
                    // ____
                    //    |
                    //    |

                    Fraction leftExtent = new Fraction(leftDownExtent(baseX,baseY));
                    Fraction rightExtent = new Fraction(baseX);
                    this.generateDownwards(leftExtent, rightExtent, baseX, baseY, baseY, true, true);

                    leftwardsSearch = true;
                    
                } else {
                    int y = baseY-1;
                    if (!graph.topLeftOfBlockedTile(baseX, y) || !graph.topRightOfBlockedTile(baseX, y)) {
                        Fraction x = new Fraction(baseX);
                        addToOpen(current, new ScanInterval(baseX, baseY, y, x, x, ScanInterval.BOTH_INCLUSIVE));
                    }
                }
            }
        }
        
        // Direct Search Left
        if (leftwardsSearch) {
            // Direct Search Left
            // Assumption: Not blocked towards left.
            leftAnyExtentMarking(baseX, baseY, memory.distance(current), current);
        }
        
        if (rightwardsSearch) {
            // Direct Search Right
            // Assumption: Not blocked towards right.
            rightAnyExtentMarking(baseX, baseY, memory.distance(current), current);
        }
    }
    
    private final void exploreState(ScanInterval currState) {
        int baseX = currState.baseX;
        int baseY = currState.baseY;
        boolean leftInclusive = (currState.inclusive & ScanInterval.LEFT_INCLUSIVE) != 0;
        boolean rightInclusive = (currState.inclusive & ScanInterval.RIGHT_INCLUSIVE) != 0;
        //System.out.println("POP " + currState);

        Fraction xL = currState.xL;
        Fraction xR = currState.xR;
        
        if (currState.y > baseY) {
            // Generate Upwards
            /*
             * =======      =====    =====
             *  \   /       / .'      '. \
             *   \ /   OR  /.'    OR    '.\
             *    B       B                B
             */

            // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
            int dy = currState.y - baseY;
            Fraction leftProjection = xL.minus(baseX).multiplyDivide(dy+1, dy).plus(baseX);

            int leftBound;
            if (xL.isWholeNumber() && graph.bottomRightOfBlockedTile(xL.n, currState.y)) leftBound = xL.n;
            else leftBound = leftUpExtent(xL.ceil(), currState.y);
            
            if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
                leftProjection = new Fraction(leftBound);
                leftInclusive = true;
            }

            // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
            Fraction rightProjection = xR.minus(baseX).multiplyDivide(dy+1, dy).plus(baseX);
            
            int rightBound;
            if (xR.isWholeNumber() && graph.bottomLeftOfBlockedTile(xR.n, currState.y)) rightBound = xR.n;
            else rightBound = rightUpExtent(xR.floor(), currState.y);

            if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
                rightProjection = new Fraction(rightBound);
                rightInclusive = true;
            }

            // Call Generate
            if (leftInclusive && rightInclusive) {
                if (leftProjection.isLessThanOrEqual(rightProjection)) {
                    generateUpwards(leftProjection, rightProjection, baseX, baseY, currState.y, true, true);
                }
            }
            else if (leftProjection.isLessThan(rightProjection)) {
                generateUpwards(leftProjection, rightProjection, baseX, baseY, currState.y, leftInclusive, rightInclusive);
            }
        }
        else {
            // Generate downwards
            /*
             *    B       B                B
             *   / \   OR  \'.    OR    .'/
             *  /   \       \ '.      .' /
             * =======      =====    =====
             */

            // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
            int dy = baseY - currState.y; 
            Fraction leftProjection = currState.xL.minus(baseX).multiplyDivide(dy+1, dy).plus(baseX);
            
            int leftBound;
            if (currState.xL.isWholeNumber() && graph.topRightOfBlockedTile(currState.xL.n, currState.y)) leftBound = currState.xL.n;
            else leftBound = leftDownExtent(currState.xL.ceil(), currState.y);
            
            if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
                leftProjection = new Fraction(leftBound);
                leftInclusive = true;
            }

            // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
            Fraction rightProjection = currState.xR.minus(baseX).multiplyDivide(dy+1, dy).plus(baseX);

            int rightBound;
            if (currState.xR.isWholeNumber() && graph.topLeftOfBlockedTile(currState.xR.n, currState.y)) rightBound = currState.xR.n;
            else rightBound = rightDownExtent(currState.xR.floor(), currState.y);
            
            if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
                rightProjection = new Fraction(rightBound);
                rightInclusive = true;
            }

            // Call Generate
            if (leftInclusive && rightInclusive) {
                if (leftProjection.isLessThanOrEqual(rightProjection)) {
                    generateDownwards(leftProjection, rightProjection, baseX, baseY, currState.y, true, true);
                }
            }
            else if (leftProjection.isLessThan(rightProjection)) {
                generateDownwards(leftProjection, rightProjection, baseX, baseY, currState.y, leftInclusive, rightInclusive);
            }
        }
    }

    private final int leftUpExtent(int xL, int y) {
        boolean val = graph.bottomRightOfBlockedTile(xL, y);
        do {
            --xL;
        } while (xL > 0 && graph.bottomRightOfBlockedTile(xL, y) == val);
        return xL;
    }

    private final int leftDownExtent(int xL, int y) {
        boolean val = graph.topRightOfBlockedTile(xL, y);
        do {
            --xL;
        } while (xL > 0 && graph.topRightOfBlockedTile(xL, y) == val);
        return xL;
    }
    
    private final int leftAnyExtentMarking(int baseX, int y, float baseDistance, int baseIndex) {
        int xL = baseX;
        boolean trVal = graph.topRightOfBlockedTile(xL, y);
        boolean brVal = graph.bottomRightOfBlockedTile(xL, y);
        do {
            --xL;
            //baseDistance += 1;
            this.tryRelax(baseIndex, baseX, y, xL, y);
        } while (xL > 0 && (graph.topRightOfBlockedTile(xL, y) == trVal) && (graph.bottomRightOfBlockedTile(xL, y) == brVal));
        return xL;
    }

    private final int rightUpExtent(int xR, int y) {
        boolean val = graph.bottomLeftOfBlockedTile(xR, y);
        do {
            ++xR;
        } while (xR < sizeX && graph.bottomLeftOfBlockedTile(xR, y) == val);
        return xR;
    }

    private final int rightDownExtent(int xR, int y) {
        boolean val = graph.topLeftOfBlockedTile(xR, y);
        do {
            ++xR;
        } while (xR < sizeX && graph.topLeftOfBlockedTile(xR, y) == val);
        return xR;
    }

    private final int rightAnyExtentMarking(int baseX, int y, float baseDistance, int baseIndex) {
        int xR = baseX;
        boolean tlVal = graph.topLeftOfBlockedTile(xR, y);
        boolean blVal = graph.bottomLeftOfBlockedTile(xR, y);
        do {
            ++xR;
            //baseDistance += 1;
            this.tryRelax(baseIndex, baseX, y, xR, y);
        } while (xR > 0 && (graph.topLeftOfBlockedTile(xR, y) == tlVal) && (graph.bottomLeftOfBlockedTile(xR, y) == blVal));
        return xR;
    }

    private final void generateUpwards(Fraction leftBound, Fraction rightBound, int baseX, int baseY, int currY, boolean leftInclusive, boolean rightInclusive) {
        //generateAndSplitIntervals(
        trimIntervals(
                currY + 1, currY + 1,
                baseX, baseY,
                leftBound, rightBound,
                leftInclusive, rightInclusive);
    }

    private final void generateDownwards(Fraction leftBound, Fraction rightBound, int baseX, int baseY, int currY, boolean leftInclusive, boolean rightInclusive) {
        //generateAndSplitIntervals(
        trimIntervals(
                currY - 2, currY - 1,
                baseX, baseY,
                leftBound, rightBound,
                leftInclusive, rightInclusive);
    }
    

    // post: return result >= xL
    private final int getLocusIntersectionRight(int currX, int dx, int dy, float dg) {
        // dg := g(parent) - g(target)
        float denominator = 2*(dx-dg);
        if (denominator <= 0) return sizeX + 1;
        int sqDist = dx*dx+dy*dy;
        if ((dg < 0) && (dg*dg >= sqDist)) return currX;
        float trimAmount = (sqDist - dg*dg) / denominator;
        //assert trimAmount > -1;
        return currX + (int)trimAmount;
    }
    
    // post: return result <= xR
    private final int getLocusIntersectionLeft(int currX, int dx, int dy, float dg) {
        // dg := g(parent) - g(target)
        float denominator = 2*(dx+dg);
        if (denominator >= 0) return -1;
        int sqDist = dx*dx+dy*dy;
        if ((dg < 0) && (dg*dg >= sqDist)) return currX;
        float trimAmount = (sqDist - dg*dg) / denominator;
        //assert trimAmount < 1;
        return currX - (int)(-trimAmount);
    }
    
    private final void trimIntervals(int checkY, int newY, int baseX, int baseY, Fraction leftBound, Fraction rightBound, boolean leftInclusive, boolean rightInclusive) {
        int baseIndex = graph.toOneDimIndex(baseX, baseY);
        float baseDistance = memory.distance(baseIndex);
        int leftCeil = leftBound.ceil();
        int rightFloor = rightBound.floor();
        int arrayOffset = leftCeil - 1;
        int rightEnd = rightFloor + 1;
        int size = rightFloor - leftCeil + 1 + 2;
        final int rightEndIndex = size - 1;
        
        { // Initialise checkArray. Original: checkArray = new int[size];
            
            // checkArray is used to mark intervals.
            // Reinitialise checkArray. Note: we assume that checkArray is always filled with zeros.
            // After using checkArray to mark intervals, we clean up after access by setting the entries back to 0. (See below)
            
            if (checkArray.length < size) {
                // Use doubling to expand checkArray size for amortised runtime.
                int targetSize = checkArray.length;
                while (targetSize < size) targetSize *= 2;
                checkArray = new int[targetSize];
            }
        }
        
                
        for (int x = leftCeil; x <= rightFloor; ++x) {
            int index = graph.toOneDimIndex(x, newY);
            float dg = baseDistance - memory.distance(index);
            int dx = baseX - x;
            int dy = baseY - newY;
            
            int leftCut = getLocusIntersectionLeft(x, dx, dy, dg);
            leftCut = (leftCut < arrayOffset) ? 0 : (leftCut - arrayOffset);
            checkArray[leftCut]++;
            
            int rightCut = getLocusIntersectionRight(x, dx, dy, dg);
            rightCut = (rightCut > rightEnd) ? rightEndIndex : (rightCut - arrayOffset);
            checkArray[rightCut]--;
        }
        //System.out.println(Arrays.toString(checkArray));
        
        Fraction left = leftBound;
        Fraction right;
        boolean blocked = checkArray[0] != 0;
        int intervalCount = checkArray[0];
        checkArray[0] = 0; // clean up after yourself
        
        for (int i=1;i<rightEndIndex;++i) {
            intervalCount += checkArray[i];
            checkArray[i] = 0; // clean up after yourself.
            //assert intervalCount >= 0;
            if (intervalCount > 0) { // is blocked
                if (!blocked) {
                    blocked = true;
                    right = new Fraction(i + arrayOffset);
                    generateAndSplitIntervals(checkY, newY, baseX, baseY, left, right, leftInclusive, true);
                }
            } else {
                if (blocked) {
                    left = new Fraction(i + arrayOffset);
                    leftInclusive = true;
                }
                blocked = false;
            }
        }
        checkArray[rightEndIndex] = 0; // clean up after yourself

        if (intervalCount == 0) { // unblocked
            right = rightBound;
            generateAndSplitIntervals(checkY, newY, baseX, baseY, left, right, leftInclusive, rightInclusive);
        }
    }
    
    /**
     * Called by generateUpwards / Downwards.
     * Note: Unlike Anya, 0-length intervals are possible.
     */
    private final void generateAndSplitIntervals(int checkY, int newY, int baseX, int baseY, Fraction leftBound, Fraction rightBound, boolean leftInclusive, boolean rightInclusive) {
        int baseIndex = graph.toOneDimIndex(baseX, baseY);
        Fraction left = leftBound;
        Fraction right;
        int currX = left.ceil();
        int rightCeilMinusOne = rightBound.ceil() - 1;
        
        leftInclusive = (leftInclusive && leftBound.isWholeNumber() && graph.isOuterCorner(leftBound.n, newY)) ? false : true;
        rightInclusive = (rightInclusive && rightBound.isWholeNumber() && graph.isOuterCorner(rightBound.n, newY)) ? false : true;
        
        // Note: we never need to add the endpoints as degenerate intervals.
        // This is because the endpoint vertices will be relaxed, and so will can be expanded.
        boolean blocked = graph.isBlocked(currX-1, checkY);
        while(currX <= rightCeilMinusOne) {
            tryRelax(baseIndex, baseX, baseY, currX, newY);
            
            if (graph.isBlocked(currX, checkY)) {
                if (!blocked) {
                    blocked = true;
                    right = new Fraction(currX);
                    int inclusive = leftInclusive ? ScanInterval.BOTH_INCLUSIVE : ScanInterval.RIGHT_INCLUSIVE;
                    addToOpen(baseIndex, new ScanInterval(baseX, baseY, newY, left, right, inclusive));
                }
            } else {
                if (blocked) {
                    left = new Fraction(currX);
                    leftInclusive = true;
                }
                blocked = false;
            }
            
            ++currX;
        }
        
        if (rightBound.isWholeNumber()) {
            tryRelax(baseIndex, baseX, baseY, currX, newY);
        }
        
        // Last interval is added here.
        if (!graph.isBlocked(currX-1, checkY)) {
            int inclusive = (leftInclusive ? ScanInterval.LEFT_INCLUSIVE : 0) | (rightInclusive ? ScanInterval.RIGHT_INCLUSIVE : 0); 
            addToOpen(baseIndex, new ScanInterval(baseX, baseY, newY, left, rightBound, inclusive));
        }
    }
    
    private final float heuristic(ScanInterval currState) {
        int baseX = currState.baseX;
        int baseY = currState.baseY;
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
        int length = 0;
        int current = finish;
        while (current != -1) {
            current = memory.parent(current);
            length++;
        }
        return length;
    }
    
    @Override
    public int[][] getPath() {
        int length = pathLength();
        int[][] path = new int[length][];
        int current = finish;
        
        int index = length-1;
        while (current != -1) {
            int x = toTwoDimX(current);
            int y = toTwoDimY(current);
            
            path[index] = new int[2];
            path[index][0] = x;
            path[index][1] = y;
            
            index--;
            current = memory.parent(current);
        }

        return path;
    }

    @Override
    public float getPathLength() {
        int current = finish;
        if (current == -1) return -1;
        
        float pathLength = 0;
        
        int prevX = toTwoDimX(current);
        int prevY = toTwoDimY(current);
        current = memory.parent(current);
        
        while (current != -1) {
            int x = toTwoDimX(current);
            int y = toTwoDimY(current);
            
            pathLength += graph.distance(x, y, prevX, prevY);
            
            current = memory.parent(current);
            prevX = x;
            prevY = y;
        }
        
        return pathLength;
    }
    

    @Override
    protected List<SnapshotItem> computeSearchSnapshot() {
        ArrayList<SnapshotItem> list = new ArrayList<>(states.length);

        for (ScanInterval in : states) {
            // y, xLn, xLd, xRn, xRd, px, py
            if (in == null) continue;
            
            Integer[] line = new Integer[7];
            line[0] = in.y;
            line[1] = in.xL.n;
            line[2] = in.xL.d;
            line[3] = in.xR.n;
            line[4] = in.xR.d;
            line[5] = in.baseX;
            line[6] = in.baseY;
            list.add(SnapshotItem.generate(line));
        }
        
        if (!statesPQ.isEmpty()) {
            int index = statesPQ.getMinIndex();
            ScanInterval in = states[index];

            Integer[] line = new Integer[5];
            line[0] = in.y;
            line[1] = in.xL.n;
            line[2] = in.xL.d;
            line[3] = in.xR.n;
            line[4] = in.xR.d;
            list.add(SnapshotItem.generate(line));
        }
        
        List<SnapshotItem> list2 = super.computeSearchSnapshot();
        list2.addAll(list);
        return list2;
    }

}