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

public class VertexAnya extends PathFindingAlgorithm {
    private static final float EPSILON = 0.000001f;
    
    private ScanInterval[] states;
    private FastVariableSizeIndirectHeap statesPQ;
    private ReusableIndirectHeap vertexPQ;
    private Memory memory;
    
    private int start;
    private int finish;

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
    
    public VertexAnya(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey);
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
        
        computeExtents();
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

    private void addToOpen(int baseIndex, ScanInterval successor) {
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
    
    private void tryRelax(int parentIndex, int parX, int parY, int x, int y) {
        //System.out.println("RELAX " + x + ", " + y);
        // return true iff relaxation is done.
        int targetIndex = graph.toOneDimIndex(x, y);
        if (memory.visited(targetIndex)) return;
        
        float newWeight = memory.distance(parentIndex) + graph.distance(parX, parY, x, y);
        if (newWeight < memory.distance(targetIndex)) {
            memory.setDistance(targetIndex, newWeight);
            memory.setParent(targetIndex, parentIndex);
            vertexPQ.decreaseKey(targetIndex, newWeight + graph.distance(x, y, ex, ey));
        }
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
            int x = leftAnyExtent(sx, sy);
            int y = sy;
            if (y == ey && x <= ex && ex <= sx) {
                tryRelax(start, sx, sy, ex, ey);
            } else if (!(graph.topRightOfBlockedTile(x, y) && graph.bottomRightOfBlockedTile(x, y))) {
                tryRelax(start, sx, sy, x, y);
            }
        }

        // Search rightwards
        if (!topLeftOfBlocked || !bottomLeftOfBlocked) {
            int x = rightAnyExtent(sx, sy);
            int y = sy;
            if (y == ey && sx <= ex && ex <= x) {
                tryRelax(start, sx, sy, ex, ey);
            } else if (!(graph.topLeftOfBlockedTile(x, y) && graph.bottomLeftOfBlockedTile(x, y))) {
                tryRelax(start, sx, sy, x, y);
            }
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
            int x = leftAnyExtent(baseX, baseY);
            int y = baseY;
            if (y == ey && x <= ex && ex <= baseX) {
                tryRelax(current, baseX, baseY, ex, ey);
            } else if (!graph.topRightOfBlockedTile(x, y) || !graph.bottomRightOfBlockedTile(x, y)) {
                tryRelax(current, baseX, baseY, x, y);
            }
        }
        
        if (rightwardsSearch) {
            // Direct Search Right
            // Assumption: Not blocked towards right.
            int x = rightAnyExtent(baseX, baseY);
            int y = baseY;
            if (y == ey && baseX <= ex && ex <= x) {
                tryRelax(current, baseX, baseY, ex, ey);
            } else if (!graph.topLeftOfBlockedTile(x, y) || !graph.bottomLeftOfBlockedTile(x, y)) { 
                tryRelax(current, baseX, baseY, x, y);
            }
        }
    }
    

    private final void generateSuccessors(ScanInterval currState) {
        int baseX = currState.baseX;
        int baseY = currState.baseY;
        int baseIndex = graph.toOneDimIndex(baseX, baseY);
        boolean leftInclusive = (currState.inclusive & ScanInterval.LEFT_INCLUSIVE) != 0;
        boolean rightInclusive = (currState.inclusive & ScanInterval.RIGHT_INCLUSIVE) != 0;
        //System.out.println("POP " + currState);

        boolean zeroLengthInterval = currState.xR.isEqualTo(currState.xL);
        //boolean leftSuccessorAdded = false;
        //boolean rightSuccessorAdded = false;
        
        if (currState.y > baseY) {
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
                
                if (x <= baseX && topRightOfBlockedTile && !bottomRightOfBlockedTile) {
                    tryRelax(baseIndex, baseX, baseY, x, y);
                    leftInclusive = false;
                }
                else if (baseX <= x && bottomRightOfBlockedTile && !topRightOfBlockedTile) {
                    tryRelax(baseIndex, baseX, baseY, x, y);
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
                
                if (x <= baseX && bottomLeftOfBlockedTile && !topLeftOfBlockedTile) {
                    if (leftInclusive || !zeroLengthInterval) {
                        tryRelax(baseIndex, baseX, baseY, x, y);
                        rightInclusive = false;
                    }
                }
                else if (baseX <= x && topLeftOfBlockedTile  && !bottomLeftOfBlockedTile) {
                    if (leftInclusive || !zeroLengthInterval) {
                        tryRelax(baseIndex, baseX, baseY, x, y);
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
            int dy = currState.y - baseY;
            Fraction leftProjection = currState.xL.minus(baseX).multiplyDivide(dy+1, dy).plus(baseX);

            int leftBound = leftUpExtent(currState.xL.ceil(), currState.y);
            if (currState.xL.isWholeNumber() && graph.bottomRightOfBlockedTile(currState.xL.n, currState.y)) leftBound = currState.xL.n;
            
            if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
                leftProjection = new Fraction(leftBound);
                leftInclusive = true;
            }

            // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
            Fraction rightProjection = currState.xR.minus(baseX).multiplyDivide(dy+1, dy).plus(baseX);
            
            int rightBound = rightUpExtent(currState.xR.floor(), currState.y);
            if (currState.xR.isWholeNumber() && graph.bottomLeftOfBlockedTile(currState.xR.n, currState.y)) rightBound = currState.xR.n;

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
                
                if (x <= baseX && bottomRightOfBlockedTile && !topRightOfBlockedTile) {
                    tryRelax(baseIndex, baseX, baseY, x, y);
                    leftInclusive = false;
                }
                else if (baseX <= x && topRightOfBlockedTile && !bottomRightOfBlockedTile) {
                    tryRelax(baseIndex, baseX, baseY, x, y);
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
                
                if (x <= baseX && topLeftOfBlockedTile && !bottomLeftOfBlockedTile) {
                    if (leftInclusive || !zeroLengthInterval) {
                        tryRelax(baseIndex, baseX, baseY, x, y);
                        rightInclusive = false;
                    }
                }
                else if (baseX <= x && bottomLeftOfBlockedTile  && !topLeftOfBlockedTile) {
                    if (leftInclusive || !zeroLengthInterval) {
                        tryRelax(baseIndex, baseX, baseY, x, y);
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
            int dy = baseY - currState.y; 
            Fraction leftProjection = currState.xL.minus(baseX).multiplyDivide(dy+1, dy).plus(baseX);
            
            int leftBound = leftDownExtent(currState.xL.ceil(), currState.y);
            if (currState.xL.isWholeNumber() && graph.topRightOfBlockedTile(currState.xL.n, currState.y)) leftBound = currState.xL.n;
            
            if (leftProjection.isLessThan(leftBound)) { // leftProjection < leftBound
                leftProjection = new Fraction(leftBound);
                leftInclusive = true;
            }

            // (Px-Bx)*(Py-By+1)/(Py-By) + Bx
            Fraction rightProjection = currState.xR.minus(baseX).multiplyDivide(dy+1, dy).plus(baseX);

            int rightBound = rightDownExtent(currState.xR.floor(), currState.y);
            if (currState.xR.isWholeNumber() && graph.topLeftOfBlockedTile(currState.xR.n, currState.y)) rightBound = currState.xR.n;
            
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

    private final void generateUpwards(Fraction leftBound, Fraction rightBound, int baseX, int baseY, int currY, boolean leftInclusive, boolean rightInclusive) {
        generateAndSplitIntervals(
                currY + 2, currY + 1,
                baseX, baseY,
                leftBound, rightBound,
                leftInclusive, rightInclusive);
    }

    private final void generateDownwards(Fraction leftBound, Fraction rightBound, int baseX, int baseY, int currY, boolean leftInclusive, boolean rightInclusive) {
        generateAndSplitIntervals(
                currY - 1, currY - 1,
                baseX, baseY,
                leftBound, rightBound,
                leftInclusive, rightInclusive);
    }
    
    /**
     * Called by generateUpwards / Downwards.
     * Note: Unlike Anya, 0-length intervals are possible.
     */
    private final void generateAndSplitIntervals(int checkY, int newY, int baseX, int baseY, Fraction leftBound, Fraction rightBound, boolean leftInclusive, boolean rightInclusive) {
        int baseIndex = graph.toOneDimIndex(baseX, baseY);
        Fraction left = leftBound;
        int leftFloor = left.floor();
        
        if (newY == ey && leftBound.isLessThanOrEqual(ex) && !rightBound.isLessThan(ex)) {
            this.tryRelax(baseIndex, baseX, baseY, ex, ey);
        }

        // Up: !bottomRightOfBlockedTile && bottomLeftOfBlockedTile
        if (leftInclusive && left.isWholeNumber() && !graph.isBlocked(leftFloor-1, checkY-1) && graph.isBlocked(leftFloor, checkY-1)) {
            addToOpen(baseIndex, new ScanInterval(baseX, baseY, newY, left, left, ScanInterval.BOTH_INCLUSIVE));
        }

        // Divide up the intervals.
        while(true) {
            int right = rightDownExtents[checkY][leftFloor]; // it's actually rightDownExtents for exploreDownwards. (thus we use checkY = currY - 2)
            if (rightBound.isLessThanOrEqual(right)) break; // right < rightBound            
            
            // Only push unblocked ( bottomRightOfBlockedTile )
            if (!graph.isBlocked(right-1, checkY-1)) {
                addToOpen(baseIndex, new ScanInterval(baseX, baseY, newY, left, new Fraction(right), leftInclusive ? ScanInterval.BOTH_INCLUSIVE : ScanInterval.RIGHT_INCLUSIVE));
            }
            
            leftFloor = right;
            left = new Fraction(leftFloor);
            leftInclusive = true;
        }

        // The last interval will always be here.
        // if !bottomLeftOfBlockedTile(leftFloor, checkY)
        if (!graph.isBlocked(leftFloor, checkY-1)) {
            int inclusive = (leftInclusive ? ScanInterval.LEFT_INCLUSIVE : 0) | (rightInclusive ? ScanInterval.RIGHT_INCLUSIVE : 0); 
            addToOpen(baseIndex, new ScanInterval(baseX, baseY, newY, left, rightBound, inclusive));
        } else {
            // The possibility of there being one degenerate interval at the end. ( !bottomLeftOfBlockedTile(xR, checkY) )
            if (rightInclusive && rightBound.isWholeNumber() && !graph.isBlocked(rightBound.n, checkY-1)) {
                addToOpen(baseIndex, new ScanInterval(baseX, baseY, newY, rightBound, rightBound, ScanInterval.BOTH_INCLUSIVE));
            }
        }
    }
    
    
    private void computeExtents() {
        // graph.isBlocked(x,y) is the same as graph.bottomLeftOfBlockedTile(x,y)
        initialiseUpExtents(graph);
        
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

    private float heuristic(ScanInterval currState) {
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


final class ScanInterval {
    public static final int BOTH_EXCLUSIVE = 0x0;
    public static final int LEFT_INCLUSIVE = 0x1;
    public static final int RIGHT_INCLUSIVE = 0x2;
    public static final int BOTH_INCLUSIVE = 0x3; // LEFT_INCLUSIVE | RIGHT_INCLUSIVE
    
    final int baseX;
    final int baseY;
    final int y;
    final Fraction xL;
    final Fraction xR;
    final int inclusive;
    
    public ScanInterval(int baseX, int baseY, int y, Fraction xL, Fraction xR, int inclusive) {
        this.baseX = baseX;
        this.baseY = baseY;
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