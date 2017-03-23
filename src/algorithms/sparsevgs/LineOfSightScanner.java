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
public final class LineOfSightScanner {
    public static ArrayList<List<SnapshotItem>> snapshotList = new ArrayList<>();
    private static ArrayList<SnapshotItem> snapshots = new ArrayList<>();
    private static int snapshot_sx;
    private static int snapshot_sy;
    
    private final GridGraph graph;
    private final int sizeX;
    private final int sizeY;
    
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
    
    private static final void clearSuccessors() {
        nSuccessors = 0;
    }
    
    private static final void stackPush(LOSInterval interval) {
        if (intervalStackSize >= intervalStack.length) {
            intervalStack = Arrays.copyOf(intervalStack, intervalStack.length*2);
        }
        intervalStack[intervalStackSize] = interval;
        ++intervalStackSize;
        
        //addToSnapshot(interval); // Uncomment for debugging.
    }
    
    private static final void addToSnapshot(LOSInterval interval) {
        snapshots.add(SnapshotItem.generate(new Integer[]{interval.y, interval.xL.n, interval.xL.d, interval.xR.n, interval.xR.d, snapshot_sx, snapshot_sy}, Color.GREEN));
        snapshotList.add(new ArrayList<SnapshotItem>(snapshots));
    }
    
    public static final void clearSnapshots() {
        snapshotList.clear();
        snapshots.clear();
    }
    
    private static final LOSInterval stackPop() {
        LOSInterval temp = intervalStack[intervalStackSize-1];
        --intervalStackSize;
        intervalStack[intervalStackSize] = null;
        return temp;
    }
    
    private static final void clearStack() {
        intervalStackSize = 0;
    }
    
    private static final void addSuccessor(int x, int y) {
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
    public final void computeAllVisibleSuccessors(int sx, int sy) {
        snapshot_sx=sx;snapshot_sy=sy;
        clearSuccessors();
        clearStack();

        generateStartingStates(sx, sy);
        exploreStatesNonTaut(sx, sy);
    }

    /**
     * Stores results in successorsX, successorsY and nSuccessors. 
     */
    public final void computeAllVisibleTautSuccessors(int sx, int sy) {
        snapshot_sx=sx;snapshot_sy=sy;
        clearSuccessors();
        clearStack();

        generateStartingStates(sx, sy);
        exploreStates(sx, sy);
    }

    /**
     * Stores results in successorsX, successorsY and nSuccessors. 
     */
    public final void computeAllVisibleTwoWayTautSuccessors(int sx, int sy) {
        snapshot_sx=sx;snapshot_sy=sy;
        clearSuccessors();
        clearStack();

        generateTwoWayTautStartingStates(sx, sy);
        exploreStates(sx, sy);
    }
    
    /**
     * Stores results in successorsX, successorsY and nSuccessors. 
     * We are moving in direction dx, dy
     */
    public final void computeAllVisibleIncrementalTautSuccessors(int sx, int sy, int dx, int dy) {
        snapshot_sx=sx;snapshot_sy=sy;
        clearSuccessors();
        clearStack();

        generateIncrementalTautStartingStates(sx, sy, dx, dy);
        exploreStates(sx, sy);
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
    private final void generateIncrementalTautStartingStates(int sx, int sy, int dx, int dy) {
        boolean rightwardsSearch = false;
        boolean leftwardsSearch = false;
        
        if (dx > 0) {
            // Moving rightwards
            if (dy > 0) {
                //    P
                //   /
                //  B
                boolean brOfBlocked = graph.bottomRightOfBlockedTile(sx, sy);
                boolean tlOfBlocked = graph.topLeftOfBlockedTile(sx, sy);
                
                int rightBound = rightUpExtent(sx,sy);
                Fraction leftExtent;
                Fraction rightExtent;
                
                if (brOfBlocked && tlOfBlocked) {
                    //  |
                    //  |___
                    
                    leftExtent = new Fraction(sx);
                    rightExtent = new Fraction(rightBound);
                    
                    rightwardsSearch = true;
                } else if (brOfBlocked) {
                    //  | /
                    //  |/
                    
                    leftExtent = new Fraction(sx);
                    rightExtent = new Fraction(sx*dy + dx, dy);
                    if (!rightExtent.isLessThanOrEqual(rightBound)) { // rightBound < rightExtent
                        rightExtent = new Fraction(rightBound);
                    }
                    
                } else { // tlOfBlocked
                    //   /
                    //  /__
                    
                    leftExtent = new Fraction(sx*dy + dx, dy);
                    rightExtent = new Fraction(rightBound);
                    
                    rightwardsSearch = true;
                }
                
                if (leftExtent.isLessThanOrEqual(rightExtent)) {
                    this.generateUpwards(leftExtent, rightExtent, sx, sy, sy, true, true);
                }
                
            } else if (dy < 0) {
                //  B
                //   \
                //    P
                boolean trOfBlocked = graph.topRightOfBlockedTile(sx, sy);
                boolean blOfBlocked = graph.bottomLeftOfBlockedTile(sx, sy);
                
                int rightBound = rightDownExtent(sx,sy);
                Fraction leftExtent;
                Fraction rightExtent;
                
                if (trOfBlocked && blOfBlocked) {
                    //  ____
                    //  |
                    //  |
                    
                    leftExtent = new Fraction(sx);
                    rightExtent = new Fraction(rightBound);
                    
                    rightwardsSearch = true;
                } else if (trOfBlocked) {
                    //  .
                    //  |\
                    //  | \
                    
                    leftExtent = new Fraction(sx);
                    rightExtent = new Fraction(sx*-dy + dx, -dy);
                    if (!rightExtent.isLessThanOrEqual(rightBound)) { // rightBound < rightExtent
                        rightExtent = new Fraction(rightBound);
                    }
                    
                } else { // blOfBlocked
                    //  ___
                    //  \
                    //   \
                    leftExtent = new Fraction(sx*-dy + dx, -dy);
                    rightExtent = new Fraction(rightBound);
                    
                    rightwardsSearch = true;
                }
                
                if (leftExtent.isLessThanOrEqual(rightExtent)) {
                    this.generateDownwards(leftExtent, rightExtent, sx, sy, sy, true, true);
                }
                
                
            } else { // dy == 0
                
                // B--P

                if (graph.bottomRightOfBlockedTile(sx, sy)) {
                    // |
                    // |___

                    Fraction leftExtent = new Fraction(sx);
                    Fraction rightExtent = new Fraction(rightUpExtent(sx,sy));
                    this.generateUpwards(leftExtent, rightExtent, sx, sy, sy, true, true);
                    
                } else if (graph.topRightOfBlockedTile(sx, sy)) { // topRightOfBlockedTile
                    // ____
                    // |
                    // |

                    Fraction leftExtent = new Fraction(sx);
                    Fraction rightExtent = new Fraction(rightDownExtent(sx,sy));
                    this.generateDownwards(leftExtent, rightExtent, sx, sy, sy, true, true);
                }
                
                rightwardsSearch = true;
            }
            
        } else if (dx < 0) {
            // Moving leftwards
            
            if (dy > 0) {
                //  B
                //   \
                //    P
                boolean blOfBlocked = graph.bottomLeftOfBlockedTile(sx, sy);
                boolean trOfBlocked = graph.topRightOfBlockedTile(sx, sy);
                
                int leftBound = leftUpExtent(sx,sy);
                Fraction leftExtent;
                Fraction rightExtent;
                
                if (blOfBlocked && trOfBlocked) {
                    //     |
                    //  ___|
                    
                    leftExtent = new Fraction(leftBound);
                    rightExtent = new Fraction(sx);
                    
                    leftwardsSearch = true;
                } else if (blOfBlocked) {
                    //  \ |
                    //   \|
                    
                    leftExtent = new Fraction(sx*dy + dx, dy);
                    rightExtent = new Fraction(sx);
                    if (leftExtent.isLessThan(leftBound)) { // leftExtent < leftBound
                        leftExtent = new Fraction(leftBound);
                    }
                    
                } else { // trOfBlocked
                    //   \
                    //  __\
                    
                    leftExtent = new Fraction(leftBound);
                    rightExtent = new Fraction(sx*dy + dx, dy);
                    
                    leftwardsSearch = true;
                }
                
                if (leftExtent.isLessThanOrEqual(rightExtent)) {
                    this.generateUpwards(leftExtent, rightExtent, sx, sy, sy, true, true);
                }
                
            } else if (dy < 0) {
                //    B
                //   /
                //  P
                boolean tlOfBlocked = graph.topLeftOfBlockedTile(sx, sy);
                boolean brOfBlocked = graph.bottomRightOfBlockedTile(sx, sy);
                
                int leftBound = leftDownExtent(sx,sy);
                Fraction leftExtent;
                Fraction rightExtent;
                
                if (tlOfBlocked && brOfBlocked) {
                    //  ____
                    //     |
                    //     |
                    
                    leftExtent = new Fraction(leftBound);
                    rightExtent = new Fraction(sx);
                    
                    leftwardsSearch = true;
                } else if (tlOfBlocked) {
                    //   /|
                    //  / |
                    
                    leftExtent = new Fraction(sx*-dy + dx, -dy);
                    rightExtent = new Fraction(sx);
                    if (leftExtent.isLessThan(leftBound)) { // leftExtent < leftBound
                        leftExtent = new Fraction(leftBound);
                    }
                    
                } else { // brOfBlocked
                    //  ___
                    //    /
                    //   /
                    
                    leftExtent = new Fraction(leftBound);
                    rightExtent = new Fraction(sx*-dy + dx, -dy);
                    
                    leftwardsSearch = true;
                }
                
                if (leftExtent.isLessThanOrEqual(rightExtent)) {
                    this.generateDownwards(leftExtent, rightExtent, sx, sy, sy, true, true);
                }
                
                
            } else { // dy == 0
                
                // P--B

                if (graph.bottomLeftOfBlockedTile(sx, sy)) {
                    //    |
                    // ___|

                    Fraction leftExtent = new Fraction(leftUpExtent(sx,sy));
                    Fraction rightExtent = new Fraction(sx);
                    this.generateUpwards(leftExtent, rightExtent, sx, sy, sy, true, true);
                    
                } else if (graph.topLeftOfBlockedTile(sx, sy)) {
                    // ____
                    //    |
                    //    |

                    Fraction leftExtent = new Fraction(leftDownExtent(sx,sy));
                    Fraction rightExtent = new Fraction(sx);
                    this.generateDownwards(leftExtent, rightExtent, sx, sy, sy, true, true);
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
                
                if (graph.topLeftOfBlockedTile(sx, sy)) {
                    // |
                    // |___

                    Fraction leftExtent = new Fraction(sx);
                    Fraction rightExtent = new Fraction(rightUpExtent(sx,sy));
                    this.generateUpwards(leftExtent, rightExtent, sx, sy, sy, true, true);

                    rightwardsSearch = true;
                    
                } else if (graph.topRightOfBlockedTile(sx, sy)) {
                    //    |
                    // ___|

                    Fraction leftExtent = new Fraction(leftUpExtent(sx,sy));
                    Fraction rightExtent = new Fraction(sx);
                    this.generateUpwards(leftExtent, rightExtent, sx, sy, sy, true, true);

                    leftwardsSearch = true;
                    
                } else {
                    Fraction x = new Fraction(sx);
                    stackPush(new LOSInterval(sy+1, x, x, LOSInterval.BOTH_INCLUSIVE));
                }
                
            } else { // dy < 0
                // Direct downwards
                
                //  B
                //  |
                //  P

                if (graph.bottomLeftOfBlockedTile(sx, sy)) {
                    // ____
                    // |
                    // |

                    Fraction leftExtent = new Fraction(sx);
                    Fraction rightExtent = new Fraction(rightDownExtent(sx,sy));
                    this.generateDownwards(leftExtent, rightExtent, sx, sy, sy, true, true);
                    
                    rightwardsSearch = true;
                    
                } else if (graph.bottomRightOfBlockedTile(sx, sy)) {
                    // ____
                    //    |
                    //    |

                    Fraction leftExtent = new Fraction(leftDownExtent(sx,sy));
                    Fraction rightExtent = new Fraction(sx);
                    this.generateDownwards(leftExtent, rightExtent, sx, sy, sy, true, true);

                    leftwardsSearch = true;
                    
                } else {
                    Fraction x = new Fraction(sx);
                    stackPush(new LOSInterval(sy-1, x, x, LOSInterval.BOTH_INCLUSIVE));
                }
            }
        }
        
        // Direct Search Left
        if (leftwardsSearch) {
            // Direct Search Left
            // Assumption: Not blocked towards left.
            addSuccessor(leftAnyExtent(sx, sy),sy); 
        }
        
        if (rightwardsSearch) {
            // Direct Search Right
            // Assumption: Not blocked towards right.
            addSuccessor(rightAnyExtent(sx, sy),sy);
        }
        
        
        
    }

    /**
     * Assumption: We are at an outer corner. One of six cases:
     *   BR        BL        TR        TL       TRBL      TLBR
     * XXX|         |XXX      :         :         |XXX   XXX|
     * XXX|...   ...|XXX   ___:...   ...:___   ___|XXX   XXX|___
     *    :         :      XXX|         |XXX   XXX|         |XXX
     *    :         :      XXX|         |XXX   XXX|         |XXX
     */
    private final void generateTwoWayTautStartingStates(int sx, int sy) {
        boolean bottomLeftOfBlocked = graph.bottomLeftOfBlockedTile(sx, sy);
        boolean bottomRightOfBlocked = graph.bottomRightOfBlockedTile(sx, sy);
        boolean topLeftOfBlocked = graph.topLeftOfBlockedTile(sx, sy);
        boolean topRightOfBlocked = graph.topRightOfBlockedTile(sx, sy);

        // Generate up-left direction
        if (topRightOfBlocked || bottomLeftOfBlocked) {
            Fraction leftExtent = new Fraction(leftUpExtent(sx,sy));
            Fraction rightExtent = new Fraction(sx);

            this.generateUpwards(leftExtent, rightExtent, sx, sy, sy, true, true);
        }
        
        // Generate up-right direction
        if (bottomRightOfBlocked || topLeftOfBlocked) {
            Fraction leftExtent = new Fraction(sx);
            Fraction rightExtent = new Fraction(rightUpExtent(sx,sy));

            this.generateUpwards(leftExtent, rightExtent, sx, sy, sy, true, true);
        }

        // Generate down-left direction
        if (bottomRightOfBlocked || topLeftOfBlocked) {
            Fraction leftExtent = new Fraction(leftDownExtent(sx,sy));
            Fraction rightExtent = new Fraction(sx);

            this.generateDownwards(leftExtent, rightExtent, sx, sy, sy, true, true);
        }
        
        // Generate down-right direction
        if (topRightOfBlocked || bottomLeftOfBlocked) {
            Fraction leftExtent = new Fraction(sx);
            Fraction rightExtent = new Fraction(rightDownExtent(sx,sy));

            this.generateDownwards(leftExtent, rightExtent, sx, sy, sy, true, true);
        }
        
        // Search leftwards
        if (!topRightOfBlocked || !bottomRightOfBlocked) {
            int x = leftAnyExtent(sx, sy);
            int y = sy;
            if (!(graph.topRightOfBlockedTile(x, y) && graph.bottomRightOfBlockedTile(x, y))) {
                addSuccessor(x,y);
            }
        }

        // Search rightwards
        if (!topLeftOfBlocked || !bottomLeftOfBlocked) {
            int x = rightAnyExtent(sx, sy);
            int y = sy;
            if (!(graph.topLeftOfBlockedTile(x, y) && graph.bottomLeftOfBlockedTile(x, y))) {
                addSuccessor(x,y);
            }
        }
    }

    private final void generateStartingStates(int sx, int sy) {
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
            int x = leftAnyExtent(sx, sy);
            int y = sy;
            if (!(graph.topRightOfBlockedTile(x, y) && graph.bottomRightOfBlockedTile(x, y))) {
                addSuccessor(x,y);
            }
        }

        // Search rightwards
        if (!topLeftOfBlocked || !bottomLeftOfBlocked) {
            int x = rightAnyExtent(sx, sy);
            int y = sy;
            if (!(graph.topLeftOfBlockedTile(x, y) && graph.bottomLeftOfBlockedTile(x, y))) {
                addSuccessor(x,y);
            }
        }
    }
    
    private final void exploreStates(int sx, int sy) {
        while (intervalStackSize > 0) {
            LOSInterval currState = stackPop();
            boolean leftInclusive = (currState.inclusive & LOSInterval.LEFT_INCLUSIVE) != 0;
            boolean rightInclusive = (currState.inclusive & LOSInterval.RIGHT_INCLUSIVE) != 0;
            //System.out.println("POP " + currState);

            boolean zeroLengthInterval = currState.xR.isEqualTo(currState.xL);
            
            if (currState.y > sy) {
                // Upwards
                
                // Insert endpoints if integer.
                if (leftInclusive && currState.xL.isWholeNumber()) {
                    /* The two cases   _
                     *  _             |X|
                     * |X|'.           ,'
                     *      '.       ,'
                     *        B     B
                     */
                    
                    int x = currState.xL.n;
                    int y = currState.y;
                    boolean topRightOfBlockedTile = graph.topRightOfBlockedTile(x, y);
                    boolean bottomRightOfBlockedTile = graph.bottomRightOfBlockedTile(x, y);
                    
                    if (x <= sx && topRightOfBlockedTile && !bottomRightOfBlockedTile) {
                        addSuccessor(x, y);
                        leftInclusive = false;
                    }
                    else if (sx <= x && bottomRightOfBlockedTile && !topRightOfBlockedTile) {
                        addSuccessor(x, y);
                        leftInclusive = false;
                    }
                }
                if (rightInclusive && currState.xR.isWholeNumber()) {
                    /*   _   The two cases
                     *  |X|             _
                     *  '.           ,'|X|
                     *    '.       ,'
                     *      B     B
                     */
                    
                    int x = currState.xR.n;
                    int y = currState.y;
                    boolean bottomLeftOfBlockedTile = graph.bottomLeftOfBlockedTile(x, y);
                    boolean topLeftOfBlockedTile = graph.topLeftOfBlockedTile(x, y);
                    
                    if (x <= sx && bottomLeftOfBlockedTile && !topLeftOfBlockedTile) {
                        if (leftInclusive || !zeroLengthInterval) {
                            addSuccessor(x, y);
                            rightInclusive = false;
                        }
                    }
                    else if (sx <= x && topLeftOfBlockedTile  && !bottomLeftOfBlockedTile) {
                        if (leftInclusive || !zeroLengthInterval) {
                            addSuccessor(x, y);
                            rightInclusive = false;
                        }
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

                int leftBound = leftUpExtent(currState.xL.ceil(), currState.y);
                if (currState.xL.isWholeNumber() && graph.bottomRightOfBlockedTile(currState.xL.n, currState.y)) leftBound = currState.xL.n;
                
                if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
                    leftProjection = new Fraction(leftBound);
                    leftInclusive = true;
                }

                // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
                Fraction rightProjection = currState.xR.minus(sx).multiplyDivide(dy+1, dy).plus(sx);
                
                int rightBound = rightUpExtent(currState.xR.floor(), currState.y);
                if (currState.xR.isWholeNumber() && graph.bottomLeftOfBlockedTile(currState.xR.n, currState.y)) rightBound = currState.xR.n;

                if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
                    rightProjection = new Fraction(rightBound);
                    rightInclusive = true;
                }

                // Call Generate
                if (leftInclusive && rightInclusive) {
                    if (leftProjection.isLessThanOrEqual(rightProjection)) {
                        generateUpwards(leftProjection, rightProjection, sx, sy, currState.y, true, true);
                    }
                }
                else if (leftProjection.isLessThan(rightProjection)) {
                    generateUpwards(leftProjection, rightProjection, sx, sy, currState.y, leftInclusive, rightInclusive);
                }
            }
            else {
                // Upwards
                
                // Insert endpoints if integer.
                if (leftInclusive && currState.xL.isWholeNumber()) {
                    /* The two cases
                     *        B     B
                     *  _   ,'       '.
                     * |X|.'           '.
                     *                |X|
                     */
                    
                    int x = currState.xL.n;
                    int y = currState.y;
                    boolean bottomRightOfBlockedTile = graph.bottomRightOfBlockedTile(x, y);
                    boolean topRightOfBlockedTile = graph.topRightOfBlockedTile(x, y);
                    
                    if (x <= sx && bottomRightOfBlockedTile && !topRightOfBlockedTile) {
                        addSuccessor(x, y);
                        leftInclusive = false;
                    }
                    else if (sx <= x && topRightOfBlockedTile && !bottomRightOfBlockedTile) {
                        addSuccessor(x, y);
                        leftInclusive = false;
                    }
                }
                if (rightInclusive && currState.xR.isWholeNumber()) {
                    /*       The two cases
                     *      B     B
                     *    .'       '.   _
                     *  .'           '.|X|
                     *  |X|
                     */
                    
                    int x = currState.xR.n;
                    int y = currState.y;
                    boolean topLeftOfBlockedTile = graph.topLeftOfBlockedTile(x, y);
                    boolean bottomLeftOfBlockedTile = graph.bottomLeftOfBlockedTile(x, y);
                    
                    if (x <= sx && topLeftOfBlockedTile && !bottomLeftOfBlockedTile) {
                        if (leftInclusive || !zeroLengthInterval) {
                            addSuccessor(x, y);
                            rightInclusive = false;
                        }
                    }
                    else if (sx <= x && bottomLeftOfBlockedTile  && !topLeftOfBlockedTile) {
                        if (leftInclusive || !zeroLengthInterval) {
                            addSuccessor(x, y);
                            rightInclusive = false;
                        }
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
                
                int leftBound = leftDownExtent(currState.xL.ceil(), currState.y);
                if (currState.xL.isWholeNumber() && graph.topRightOfBlockedTile(currState.xL.n, currState.y)) leftBound = currState.xL.n;
                
                if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
                    leftProjection = new Fraction(leftBound);
                    leftInclusive = true;
                }

                // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
                Fraction rightProjection = currState.xR.minus(sx).multiplyDivide(dy+1, dy).plus(sx);

                int rightBound = rightDownExtent(currState.xR.floor(), currState.y);
                if (currState.xR.isWholeNumber() && graph.topLeftOfBlockedTile(currState.xR.n, currState.y)) rightBound = currState.xR.n;
                
                if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
                    rightProjection = new Fraction(rightBound);
                    rightInclusive = true;
                }

                // Call Generate
                if (leftInclusive && rightInclusive) {
                    if (leftProjection.isLessThanOrEqual(rightProjection)) {
                        generateDownwards(leftProjection, rightProjection, sx, sy, currState.y, true, true);
                    }
                }
                else if (leftProjection.isLessThan(rightProjection)) {
                    generateDownwards(leftProjection, rightProjection, sx, sy, currState.y, leftInclusive, rightInclusive);
                }
            }
        }
    }
    
    

    private final void exploreStatesNonTaut(int sx, int sy) {
        while (intervalStackSize > 0) {
            LOSInterval currState = stackPop();
            boolean leftInclusive = (currState.inclusive & LOSInterval.LEFT_INCLUSIVE) != 0;
            boolean rightInclusive = (currState.inclusive & LOSInterval.RIGHT_INCLUSIVE) != 0;
            //System.out.println("POP " + currState);

            boolean zeroLengthInterval = currState.xR.isEqualTo(currState.xL);
            
            if (currState.y > sy) {
                // Upwards
                
                // Insert endpoints if integer.
                if (leftInclusive && currState.xL.isWholeNumber()) {
                    /* The two cases   _
                     *  _             |X|
                     * |X|'.           ,'
                     *      '.       ,'
                     *        B     B
                     */
                    
                    int x = currState.xL.n;
                    int y = currState.y;
                    
                    if (graph.isOuterCorner(x, y)) {
                        addSuccessor(x, y);
                        leftInclusive = false;
                    }
                }
                if (rightInclusive && currState.xR.isWholeNumber()) {
                    /*   _   The two cases
                     *  |X|             _
                     *  '.           ,'|X|
                     *    '.       ,'
                     *      B     B
                     */
                    
                    int x = currState.xR.n;
                    int y = currState.y;
                    
                    if (graph.isOuterCorner(x, y)) {
                        addSuccessor(x, y);
                        rightInclusive = false;
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

                int leftBound = leftUpExtent(currState.xL.ceil(), currState.y);
                if (currState.xL.isWholeNumber() && graph.bottomRightOfBlockedTile(currState.xL.n, currState.y)) leftBound = currState.xL.n;
                
                if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
                    leftProjection = new Fraction(leftBound);
                    leftInclusive = true;
                }

                // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
                Fraction rightProjection = currState.xR.minus(sx).multiplyDivide(dy+1, dy).plus(sx);
                
                int rightBound = rightUpExtent(currState.xR.floor(), currState.y);
                if (currState.xR.isWholeNumber() && graph.bottomLeftOfBlockedTile(currState.xR.n, currState.y)) rightBound = currState.xR.n;

                if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
                    rightProjection = new Fraction(rightBound);
                    rightInclusive = true;
                }

                // Call Generate
                if (leftInclusive && rightInclusive) {
                    if (leftProjection.isLessThanOrEqual(rightProjection)) {
                        generateUpwards(leftProjection, rightProjection, sx, sy, currState.y, true, true);
                    }
                }
                else if (leftProjection.isLessThan(rightProjection)) {
                    generateUpwards(leftProjection, rightProjection, sx, sy, currState.y, leftInclusive, rightInclusive);
                }
            }
            else {
                // Upwards
                
                // Insert endpoints if integer.
                if (leftInclusive && currState.xL.isWholeNumber()) {
                    /* The two cases
                     *        B     B
                     *  _   ,'       '.
                     * |X|.'           '.
                     *                |X|
                     */
                    
                    int x = currState.xL.n;
                    int y = currState.y;
                    
                    if (graph.isOuterCorner(x, y)) {
                        addSuccessor(x, y);
                        leftInclusive = false;
                    }
                }
                if (rightInclusive && currState.xR.isWholeNumber()) {
                    /*       The two cases
                     *      B     B
                     *    .'       '.   _
                     *  .'           '.|X|
                     *  |X|
                     */
                    
                    int x = currState.xR.n;
                    int y = currState.y;
                    if (graph.isOuterCorner(x, y)) {
                        addSuccessor(x, y);
                        rightInclusive = false;
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
                
                int leftBound = leftDownExtent(currState.xL.ceil(), currState.y);
                if (currState.xL.isWholeNumber() && graph.topRightOfBlockedTile(currState.xL.n, currState.y)) leftBound = currState.xL.n;
                
                if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
                    leftProjection = new Fraction(leftBound);
                    leftInclusive = true;
                }

                // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
                Fraction rightProjection = currState.xR.minus(sx).multiplyDivide(dy+1, dy).plus(sx);

                int rightBound = rightDownExtent(currState.xR.floor(), currState.y);
                if (currState.xR.isWholeNumber() && graph.topLeftOfBlockedTile(currState.xR.n, currState.y)) rightBound = currState.xR.n;
                
                if (!rightProjection.isLessThanOrEqual(rightBound)) { // rightBound < rightProjection
                    rightProjection = new Fraction(rightBound);
                    rightInclusive = true;
                }

                // Call Generate
                if (leftInclusive && rightInclusive) {
                    if (leftProjection.isLessThanOrEqual(rightProjection)) {
                        generateDownwards(leftProjection, rightProjection, sx, sy, currState.y, true, true);
                    }
                }
                else if (leftProjection.isLessThan(rightProjection)) {
                    generateDownwards(leftProjection, rightProjection, sx, sy, currState.y, leftInclusive, rightInclusive);
                }
            }
        }
    }
    
    
    
    

    private final int leftUpExtent(int xL, int y) {
        return xL > sizeX ? sizeX : leftDownExtents[y+1][xL];
    }

    private final int leftDownExtent(int xL, int y) {
        return xL > sizeX ? sizeX : leftDownExtents[y][xL];
    }
    
    private final int leftAnyExtent(int xL, int y) {
        return Math.max(leftDownExtents[y][xL], leftDownExtents[y+1][xL]);
    }

    private final int rightUpExtent(int xR, int y) {
        return xR < 0 ? 0 : rightDownExtents[y+1][xR];
    }

    private final int rightDownExtent(int xR, int y) {
        return xR < 0 ? 0 : rightDownExtents[y][xR];
    }

    private final int rightAnyExtent(int xR, int y) {
        return Math.min(rightDownExtents[y][xR], rightDownExtents[y+1][xR]);
    }

    private final void generateUpwards(Fraction leftBound, Fraction rightBound, int sx, int sy, int currY, boolean leftInclusive, boolean rightInclusive) {
        generateAndSplitIntervals(
                currY + 2, currY + 1,
                sx, sy,
                leftBound, rightBound,
                leftInclusive, rightInclusive);
    }

    private final void generateDownwards(Fraction leftBound, Fraction rightBound, int sx, int sy, int currY, boolean leftInclusive, boolean rightInclusive) {
        generateAndSplitIntervals(
                currY - 1, currY - 1,
                sx, sy,
                leftBound, rightBound,
                leftInclusive, rightInclusive);
    }
    
    /**
     * Called by generateUpwards / Downwards.
     * Note: Unlike Anya, 0-length intervals are possible.
     */
    private final void generateAndSplitIntervals(int checkY, int newY, int sx, int sy, Fraction leftBound, Fraction rightBound, boolean leftInclusive, boolean rightInclusive) {
        Fraction left = leftBound;
        int leftFloor = left.floor();

        // Up: !bottomRightOfBlockedTile && bottomLeftOfBlockedTile
        if (leftInclusive && left.isWholeNumber() && !graph.isBlocked(leftFloor-1, checkY-1) && graph.isBlocked(leftFloor, checkY-1)) {
            stackPush(new LOSInterval(newY, left, left, LOSInterval.BOTH_INCLUSIVE));
        }

        // Divide up the intervals.
        while(true) {
            int right = rightDownExtents[checkY][leftFloor]; // it's actually rightDownExtents for exploreDownwards. (thus we use checkY = currY - 2)
            if (rightBound.isLessThanOrEqual(right)) break; // right < rightBound            
            
            // Only push unblocked ( bottomRightOfBlockedTile )
            if (!graph.isBlocked(right-1, checkY-1)) {
                stackPush(new LOSInterval(newY, left, new Fraction(right), leftInclusive ? LOSInterval.BOTH_INCLUSIVE : LOSInterval.RIGHT_INCLUSIVE));
            }
            
            leftFloor = right;
            left = new Fraction(leftFloor);
            leftInclusive = true;
        }

        // The last interval will always be here.
        // if !bottomLeftOfBlockedTile(leftFloor, checkY)
        if (!graph.isBlocked(leftFloor, checkY-1)) {
            int inclusive = (leftInclusive ? LOSInterval.LEFT_INCLUSIVE : 0) | (rightInclusive ? LOSInterval.RIGHT_INCLUSIVE : 0); 
            stackPush(new LOSInterval(newY, left, rightBound, inclusive));
        } else {
            // The possibility of there being one degenerate interval at the end. ( !bottomLeftOfBlockedTile(xR, checkY) )
            if (rightInclusive && rightBound.isWholeNumber() && !graph.isBlocked(rightBound.n, checkY-1)) {
                stackPush(new LOSInterval(newY, rightBound, rightBound, LOSInterval.BOTH_INCLUSIVE));
            }
        }
    }

    public static void clearMemory() {
        snapshotList.clear();
        snapshots.clear();
        rightDownExtents = null;
        leftDownExtents = null;
        intervalStack = null;
        successorsX = null;
        successorsY = null;
        System.gc();
    }
    
}



final class LOSInterval {
    public static final int BOTH_EXCLUSIVE = 0x0;
    public static final int LEFT_INCLUSIVE = 0x1;
    public static final int RIGHT_INCLUSIVE = 0x2;
    public static final int BOTH_INCLUSIVE = 0x3; // LEFT_INCLUSIVE | RIGHT_INCLUSIVE
    
    
    final int y;
    final Fraction xL;
    final Fraction xR;
    final int inclusive;
    
    public LOSInterval(int y, Fraction xL, Fraction xR, int inclusive) {
        this.y = y;
        this.xL = xL;
        this.xR = xR;
        this.inclusive = inclusive;
    }
    
    @Override
    public final String toString() {
        return ((inclusive & LEFT_INCLUSIVE) == 0 ? "(" : "[") + xL + ", " + xR + ((inclusive & RIGHT_INCLUSIVE) == 0 ? ")" : "]") + "|" + y;
    }
}