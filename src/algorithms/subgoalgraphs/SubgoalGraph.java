package algorithms.subgoalgraphs;

import java.util.Arrays;

import grid.GridGraph;

public class SubgoalGraph {
    
    private static SubgoalGraph storedGraph;
    private static GridGraph graph;

    private final int xLength;
    private final int yLength;
    
    private final int[][] leftExtents;  // Indexed by arr[y][x]
    private final int[][] rightExtents; // Indexed by arr[y][x]
    private final int[][] upExtents;    // Indexed by arr[x][y]
    private final int[][] downExtents;  // Indexed by arr[x][y]
    
    public final int[][] nodeIndex;    // Indexed by arr[y][x]
    
    public int[] xPositions;
    public int[] yPositions;
    public int[][] neighbours;
    public int[] nNeighbours;
    public boolean[] hasEdgeToGoal;  // A boolean flag indicating whether there is an edge to the goal node.
    private int size;
    
    private int originalSize;
    public int startIndex;
    public int endIndex;
    
    private SubgoalGraph() {
        xLength = graph.sizeX+1;
        yLength = graph.sizeY+1;
        
        leftExtents = new int[yLength][];
        rightExtents = new int[yLength][];
        upExtents = new int[xLength][];
        downExtents = new int[xLength][];
        
        nodeIndex = new int[yLength][];

        xPositions = new int[11];
        yPositions = new int[11];
        size = 0;
    }
    
    public static final SubgoalGraph initialiseNew(GridGraph graph) {
        if (SubgoalGraph.graph == graph) {
            return storedGraph;
        }
        SubgoalGraph.graph = graph;
        SubgoalGraph subgoalGraph = SubgoalGraph.storedGraph = new SubgoalGraph();
        subgoalGraph.initialiseGraph();
        return subgoalGraph;
    }
    
    private final void computeClearances() {
        // EXAMPLE:
        //                     .-- is subgoal
        // Left Extents:       |
        // ___________________ | _______
        // |   |XXX|   |   |   V   |   | 
        // |   |XXX|   |   |   .   |   |
        // 0---1---0---1---2---3---1---2
        // |   |XXX|   |   |   |XXX|XXX|
        // |___|XXX|___|___|___|XXX|XXX|
        
        int extent;
        for (int y=0;y<yLength;++y) {
            
            int[] leftExtent = new int[xLength];
            extent = 0;
            for (int x=0;x<xLength;++x) {
                if (graph.bottomRightOfBlockedTile(x, y) && graph.topRightOfBlockedTile(x, y)) {
                    extent = 0;
                }
                leftExtent[x] = extent;
                ++extent;
                
                if (nodeIndex[y][x] != -1) {
                    // if is subgoal
                    extent = 1;
                }
            }
            
            int[] rightExtent = new int[xLength];
            extent = 0;
            for (int x=xLength-1;x>=0;--x) {
                if (graph.bottomLeftOfBlockedTile(x, y) && graph.topLeftOfBlockedTile(x, y)) {
                    extent = 0;
                }
                rightExtent[x] = extent;
                ++extent;
                
                if (nodeIndex[y][x] != -1) {
                    // if is subgoal
                    extent = 1;
                }
            }
            
            leftExtents[y] = leftExtent;
            rightExtents[y] = rightExtent;
        }

        for (int x=0;x<xLength;++x) {
            int[] downExtent = new int[yLength];
            extent = 0;
            for (int y=0;y<yLength;++y) {
                if (graph.topLeftOfBlockedTile(x, y) && graph.topRightOfBlockedTile(x, y)) {
                    extent = 0;
                }
                downExtent[y] = extent;
                ++extent;
                
                if (nodeIndex[y][x] != -1) {
                    // if is subgoal
                    extent = 1;
                }
            }
            
            int[] upExtent = new int[yLength];
            extent = 0;
            for (int y=yLength-1;y>=0;--y) {
                if (graph.bottomLeftOfBlockedTile(x, y) && graph.bottomRightOfBlockedTile(x, y)) {
                    extent = 0;
                }
                upExtent[y] = extent;
                ++extent;
                
                if (nodeIndex[y][x] != -1) {
                    // if is subgoal
                    extent = 1;
                }
            }
            
            downExtents[x] = downExtent;
            upExtents[x] = upExtent;
        }
    }

    private void initialiseSubgoalNodes() {
        for (int y=0;y<yLength;++y) {
            int[] nodes = new int[xLength];
            Arrays.fill(nodes, -1);
            nodeIndex[y] = nodes;
        }
        
        size = 0;
        for (int y=0;y<yLength;++y) {
            for (int x=0;x<xLength;++x) {
                if (graph.isOuterCorner(x, y)) {
                    // Expand arrays if needed.
                    if (xPositions.length <= size) {
                        int newLength = xPositions.length*2;
                        xPositions = Arrays.copyOf(xPositions, newLength);
                        yPositions = Arrays.copyOf(yPositions, newLength);
                    }
                    
                    // Add new subgoal.
                    xPositions[size] = x;
                    yPositions[size] = y;
                    nodeIndex[y][x] = size;
                    
                    ++size;
                }
            }
        }
        
        // Note: reserve twp extra slot for the start and end nodes.
        originalSize = size;
        int maxSize = maxSize();
        
        // Resize array to fit.
        xPositions = Arrays.copyOf(xPositions, maxSize);
        yPositions = Arrays.copyOf(yPositions, maxSize);
        
        
        neighbours = new int[maxSize][];
        nNeighbours = new int[maxSize];        // Default value = 0
        hasEdgeToGoal = new boolean[maxSize];  // Default value = false
        
        for (int i=0; i<maxSize; ++i) {
            neighbours[i] = new int[11];
        }
    }
    
    public final void addStartAndEnd(int sx, int sy, int ex, int ey) {
        
        // START:
        if (nodeIndex[sy][sx] == -1) {
            startIndex = size;
            ++size;
            
            nodeIndex[sy][sx] = startIndex;
            xPositions[startIndex] = sx;
            yPositions[startIndex] = sy;
            nNeighbours[startIndex] = 0;
            computeNeighbours(startIndex);
        } else {
            startIndex = nodeIndex[sy][sx];
        }
        
        // END:
        if (nodeIndex[ey][ex] == -1) {
            endIndex = size;
            ++size;
            
            nodeIndex[ey][ex] = endIndex;
            xPositions[endIndex] = ex;
            yPositions[endIndex] = ey;
            nNeighbours[endIndex] = 0;
            computeNeighbours(endIndex);
            
            // Mark all hasEdgeToGoal vertices.
            markHasEdgeToGoal(true);
        } else {
            endIndex = nodeIndex[ey][ex];
        }
        
    }
    
    public final void restoreOriginalGraph() {
        if (startIndex >= originalSize) {
            int sx = xPositions[startIndex];
            int sy = yPositions[startIndex];
            nodeIndex[sy][sx] = -1;
        }
        
        if (endIndex >= originalSize) {
            int ex = xPositions[endIndex];
            int ey = yPositions[endIndex];
            nodeIndex[ey][ex] = -1;
            
            // Reset hasEdgeToGoal array.
            markHasEdgeToGoal(false);
            // PostCondition: hasEdgeToGoal has all values == false.
        }
        
        
        // Reset size.
        size = originalSize;
        
        startIndex = -1; // Not needed. Just to catch errors.
        endIndex = -1;   // Not needed. Just to catch errors.
    }

    private void markHasEdgeToGoal(boolean value) {
        int[] endNeighbours = neighbours[endIndex];
        int n = nNeighbours[endIndex];
        for (int i=0;i<n;++i) {
            hasEdgeToGoal[endNeighbours[i]] = value;
        }
    }
    
    private final void initialiseGraph() {
        initialiseSubgoalNodes();
        computeClearances();
        
        for (int i=0;i<size;++i) {
            computeNeighbours(i);
        }
    }
    
    private final void computeNeighbours(int index) {
        int x = xPositions[index];
        int y = yPositions[index];
        
        // getDirectHReachable
        
        // For cardinal directions [UDLR]
        {
            int tx = x - leftExtents[y][x];
            if (nodeIndex[y][tx] != -1) {
                addNeighbour(index, tx, y);
            }
            
            tx = x + rightExtents[y][x];
            if (nodeIndex[y][tx] != -1) {
                addNeighbour(index, tx, y);
            }
            
            int ty = y + upExtents[x][y];
            if (nodeIndex[ty][x] != -1) {
                addNeighbour(index, x, ty);
            }
            
            ty = y - downExtents[x][y];
            if (nodeIndex[ty][x] != -1) {
                addNeighbour(index, x, ty);
            }
        }
        
        // START: computeNeighbours for diagonal directions
        {
            int diag, dx, dy, diagX, diagY;

            // START: Top-Left
            {
                // Check top left
                diag = 0; dx = -1; dy = 1;
                {
                    diagX = x;
                    diagY = y;
                    while(!graph.bottomRightOfBlockedTile(diagX,diagY)) {
                        ++diag;
                        diagX += dx;
                        diagY += dy;
                        if (nodeIndex[diagY][diagX] != -1) {
                            // Is subgoal
                            addNeighbour(index, diagX, diagY);
                            --diag;
                            break;
                        }
                    }
                }
                
                // TL : Top
                {
                    diagX = x; diagY = y;
                    int max = upExtents[x][y];
                    if (nodeIndex[y+max][x] != -1) --max;
                    for (int i = 1; i <= diag; ++i) {
                        diagX += dx;
                        diagY += dy;
                        int extent = upExtents[diagX][diagY];
                        if (extent <= max) {
                            if (nodeIndex[diagY+extent][diagX] != -1) {
                                // Is subgoal
                                addNeighbour(index, diagX, diagY+extent);
                                --extent;
                            }
                            max = extent;
                        }
                    }
                }
                
                // TL : Left
                {
                    diagX = x; diagY = y;
                    int max = leftExtents[y][x];
                    if (nodeIndex[y][x-max] != -1) --max;
                    for (int i = 1; i <= diag; ++i) {
                        diagX += dx;
                        diagY += dy;
                        int extent = leftExtents[diagY][diagX];
                        if (extent <= max) {
                            if (nodeIndex[diagY][diagX-extent] != -1) {
                                // Is subgoal
                                addNeighbour(index, diagX-extent, diagY);
                                --extent;
                            }
                            max = extent;
                        }
                    }
                }
            }
            // END: Top-Left
            

            // START: Top-Right
            {
                // Check top right
                diag = 0; dx = 1; dy = 1;
                {
                    diagX = x;
                    diagY = y;
                    while(!graph.bottomLeftOfBlockedTile(diagX,diagY)) {
                        ++diag;
                        diagX += dx;
                        diagY += dy;
                        if (nodeIndex[diagY][diagX] != -1) {
                            // Is subgoal
                            addNeighbour(index, diagX, diagY);
                            --diag;
                            break;
                        }
                    }
                }
                
                // TR : Top
                {
                    diagX = x; diagY = y;
                    int max = upExtents[x][y];
                    if (nodeIndex[y+max][x] != -1) --max;
                    for (int i = 1; i <= diag; ++i) {
                        diagX += dx;
                        diagY += dy;
                        int extent = upExtents[diagX][diagY];
                        if (extent <= max) {
                            if (nodeIndex[diagY+extent][diagX] != -1) {
                                // Is subgoal
                                addNeighbour(index, diagX, diagY+extent);
                                --extent;
                            }
                            max = extent;
                        }
                    }
                }
                
                // TR : Right
                {
                    diagX = x; diagY = y;
                    int max = rightExtents[y][x];
                    if (nodeIndex[y][x+max] != -1) --max;
                    for (int i = 1; i <= diag; ++i) {
                        diagX += dx;
                        diagY += dy;
                        int extent = rightExtents[diagY][diagX];
                        if (extent <= max) {
                            if (nodeIndex[diagY][diagX+extent] != -1) {
                                // Is subgoal
                                addNeighbour(index, diagX+extent, diagY);
                                --extent;
                            }
                            max = extent;
                        }
                    }
                }
            }
            // END: Top-Right

            // START: Bottom-Left
            {
                // Check bottom left
                diag = 0; dx = -1; dy = -1;
                {
                    diagX = x;
                    diagY = y;
                    while(!graph.topRightOfBlockedTile(diagX,diagY)) {
                        ++diag;
                        diagX += dx;
                        diagY += dy;
                        if (nodeIndex[diagY][diagX] != -1) {
                            // Is subgoal
                            addNeighbour(index, diagX, diagY);
                            --diag;
                            break;
                        }
                    }
                }
                
                // BL : Bottom
                {
                    diagX = x; diagY = y;
                    int max = downExtents[x][y];
                    if (nodeIndex[y-max][x] != -1) --max;
                    for (int i = 1; i <= diag; ++i) {
                        diagX += dx;
                        diagY += dy;
                        int extent = downExtents[diagX][diagY];
                        if (extent <= max) {
                            if (nodeIndex[diagY-extent][diagX] != -1) {
                                // Is subgoal
                                addNeighbour(index, diagX, diagY-extent);
                                --extent;
                            }
                            max = extent;
                        }
                    }
                }
                
                // BL : Left
                {
                    diagX = x; diagY = y;
                    int max = leftExtents[y][x];
                    if (nodeIndex[y][x-max] != -1) --max;
                    for (int i = 1; i <= diag; ++i) {
                        diagX += dx;
                        diagY += dy;
                        int extent = leftExtents[diagY][diagX];
                        if (extent <= max) {
                            if (nodeIndex[diagY][diagX-extent] != -1) {
                                // Is subgoal
                                addNeighbour(index, diagX-extent, diagY);
                                --extent;
                            }
                            max = extent;
                        }
                    }
                }
            }
            // END: Bottom-Left

            // START: Bottom-Right
            {
                // Check bottom right
                diag = 0; dx = 1; dy = -1;
                {
                    diagX = x;
                    diagY = y;
                    while(!graph.topLeftOfBlockedTile(diagX,diagY)) {
                        ++diag;
                        diagX += dx;
                        diagY += dy;
                        if (nodeIndex[diagY][diagX] != -1) {
                            // Is subgoal
                            addNeighbour(index, diagX, diagY);
                            --diag;
                            break;
                        }
                    }
                }
                
                // BR : Bottom
                {
                    diagX = x; diagY = y;
                    int max = downExtents[x][y];
                    if (nodeIndex[y-max][x] != -1) --max;
                    for (int i = 1; i <= diag; ++i) {
                        diagX += dx;
                        diagY += dy;
                        int extent = downExtents[diagX][diagY];
                        if (extent <= max) {
                            if (nodeIndex[diagY-extent][diagX] != -1) {
                                // Is subgoal
                                addNeighbour(index, diagX, diagY-extent);
                                --extent;
                            }
                            max = extent;
                        }
                    }
                }
                
                // BR : Right
                {
                    diagX = x; diagY = y;
                    int max = rightExtents[y][x];
                    if (nodeIndex[y][x+max] != -1) --max;
                    for (int i = 1; i <= diag; ++i) {
                        diagX += dx;
                        diagY += dy;
                        int extent = rightExtents[diagY][diagX];
                        if (extent <= max) {
                            if (nodeIndex[diagY][diagX+extent] != -1) {
                                // Is subgoal
                                addNeighbour(index, diagX+extent, diagY);
                                --extent;
                            }
                            max = extent;
                        }
                    }
                }
            }
            // END: Bottom-Right
        }
        // END: computeNeighbours for diagonal directions
    }
    
    private final void addNeighbour(int index, int nX, int nY) {
        assert nodeIndex[nY][nX] != -1;
        if (nNeighbours[index] >= neighbours[index].length) {
            neighbours[index] = Arrays.copyOf(neighbours[index], neighbours[index].length*2);
        }
        neighbours[index][nNeighbours[index]] = nodeIndex[nY][nX];
        ++nNeighbours[index];
    }
    
    public final int size() { return size; }
    public final int maxSize() { return originalSize + 2; }
}
