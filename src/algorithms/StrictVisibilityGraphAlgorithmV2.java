package algorithms;

import grid.GridGraph;

import java.util.ArrayList;

import algorithms.datatypes.Point;
import algorithms.priorityqueue.IndirectHeap;
import algorithms.visibilitygraph.IncrementalVisibilityGraph;
import algorithms.visibilitygraph.IncrementalVisibilityGraphV2;
import algorithms.visibilitygraph.LowerBoundJumpPointSearch;

public class StrictVisibilityGraphAlgorithmV2 extends AStar {
    
    private IncrementalVisibilityGraphV2 visibilityGraph;
    private float pathLength;

    public StrictVisibilityGraphAlgorithmV2(GridGraph graph, int sx, int sy,
            int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }
    


    @Override
    public void computePath() {
        PathFindingAlgorithm algo = new BasicThetaStar(graph, sx, sy, ex, ey);
        
        if (isRecording()) {
            algo.startRecording();
            algo.computePath();
        } else {
            algo.computePath();
        }

        pathLength = algo.getPathLength();
        if (pathLength < 0.001f) {
            return;
        }
        
        LowerBoundJumpPointSearch lowerBoundSearch = new LowerBoundJumpPointSearch(graph, ex ,ey, sx, sy, pathLength);

        if (isRecording()) {
            lowerBoundSearch.startRecording();
            lowerBoundSearch.computePath();
            inheritSnapshotListFrom(lowerBoundSearch);
        } else {
            lowerBoundSearch.computePath();
        }
        
        visibilityGraph = new IncrementalVisibilityGraphV2(graph, sx, sy, ex, ey, pathLength, lowerBoundSearch);
        visibilityGraph.initialise();
        
        distance = new Float[visibilityGraph.size()];
        parent = new int[visibilityGraph.size()];

        initialise(visibilityGraph.startNode());
        visited = new boolean[visibilityGraph.size()];

        startAlgorithm();
    }
    
    private void startAlgorithm() {
        pq = new IndirectHeap<Float>(distance, true);
        pq.heapify();

        int start = pq.popMinIndex(); // pop start.
        visited[start] = true;
        processStart(start);
        
        int finish = visibilityGraph.endNode();
        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            visited[current] = true;
            
            if (current == finish) {
                break;
            }

            process(current);
            
            maybeSaveSearchSnapshot();
        }
    }
    
    private String ns(int nodeIndex) {
        Point p = visibilityGraph.coordinateOf(nodeIndex);
        return nodeIndex+"_"+p.x+","+p.y;
    }
    
    private void processStart(int startIndex) {
        //System.out.println("Process start " + ns(startIndex));
        
        /**  _trbl     _
         *  |_|       |_| tlbr
         *  '-.       .-'
         *     '-. .-'
         *        o
         *     .-' '-.
         *  .-'       '-.
         *  |_|       |_| trbl
         *  tlbr
         */
        ArrayList<int[]> list;

        
        /**  -.
         *      '-.         |
         *      |_|      ^  V
         *   _..--'      |
         */
        list = visibilityGraph.getLineHashX_tlbr(sx);
        relaxFrom(startIndex, sx, sy, list, sy);
        
        list = visibilityGraph.getLineHashX_trbl(sx);
        relaxFrom(startIndex, sx, sy, list, sy);

        list = visibilityGraph.getLineHashX_tlbr(sx);
        relaxTo(startIndex, sx, sy, list, sy);
        
        list = visibilityGraph.getLineHashX_trbl(sx);
        relaxTo(startIndex, sx, sy, list, sy);
        
        int x = sx+1;
        while(true) {
            int y = sy;
            if (!visibilityGraph.withinEllipseBoxBounds(x,y)) break;

            list = visibilityGraph.getLineHashX_tlbr(x);
            relaxFrom(startIndex, sx, sy, list, y);
            list = visibilityGraph.getLineHashX_trbl(x);
            relaxTo(startIndex, sx, sy, list, y);
            
            x++;
        }

        /**        .-
         *     .-'       |
         *     |_|    ^  V
         *     '--._  |
         */
        list = visibilityGraph.getLineHashX_tlbr(sx);
        relaxFrom(startIndex, sx, sy, list, sy);

        list = visibilityGraph.getLineHashX_trbl(sx);
        relaxFrom(startIndex, sx, sy, list, sy);
        
        list = visibilityGraph.getLineHashX_tlbr(sx);
        relaxTo(startIndex, sx, sy, list, sy);

        list = visibilityGraph.getLineHashX_trbl(sx);
        relaxTo(startIndex, sx, sy, list, sy);


        x = sx-1;;
        while(true) {
            int y = sy;
            if (!visibilityGraph.withinEllipseBoxBounds(x,y)) break;

            list = visibilityGraph.getLineHashX_trbl(x);
            relaxFrom(startIndex, sx, sy, list, y);
            list = visibilityGraph.getLineHashX_tlbr(x);
            relaxTo(startIndex, sx, sy, list, y);
            
            x--;
        }
    }
    
    
    private void process(int curr) {
        int par = parent[curr];
        //System.out.println("Process " + ns(curr) + " / " + ns(par));
        
        Point pCurr = visibilityGraph.coordinateOf(curr);
        int currX = pCurr.x;
        int currY = pCurr.y;
        
        Point pPar = visibilityGraph.coordinateOf(par);
        int parX = pPar.x;
        int parY = pPar.y;
        
        if (parX == currX || parY == currY) {
            //return;
        }

        ArrayList<int[]> list;

        if (graph.bottomRightOfBlockedTile(currX, currY)) {
            if (parX < currX) {
                /**      _
                 *      |_|      ^
                 *   _..--'      |
                 */
                list = visibilityGraph.getLineHashX_tlbr(currX);
                relaxFrom(curr, currX, currY, list, currY+1);
                
                list = visibilityGraph.getLineHashX_trbl(currX);
                relaxFrom(curr, currX, currY, list, currY+1);
                
                int x = currX+1;
                while(true) {
                    int y = ceilDivide((currY-parY)*(x-currX), currX-parX) + currY;
                    if (!visibilityGraph.withinEllipseBoxBounds(x,y)) break;

                    list = visibilityGraph.getLineHashX_tlbr(x);
                    relaxFrom(curr, currX, currY, list, y);
                    
                    x++;
                }
                
            } else if (parY > currY) {
                /**      _  /
                 *      |_|/      <=
                 */
                list = visibilityGraph.getLineHashY_tlbr(currY);
                relaxTo(curr, currX, currY, list, currX-1);

                list = visibilityGraph.getLineHashY_trbl(currY);
                relaxTo(curr, currX, currY, list, currX-1);
                
                int y = currY-1;
                while(true) {
                    int x = (currX-parX)*(y-currY)/(currY-parY) + currX;
                    if (!visibilityGraph.withinEllipseBoxBounds(x,y)) break;

                    list = visibilityGraph.getLineHashY_tlbr(y);
                    relaxTo(curr, currX, currY, list, x);
                    
                    y--;
                }
            }
        }


        if (graph.topLeftOfBlockedTile(currX, currY)) {
            if (parY < currY) {
                /**     __
                 *  .--'|_|     =>
                 */
                list = visibilityGraph.getLineHashY_tlbr(currY);
                relaxFrom(curr, currX, currY, list, currX+1);
                
                list = visibilityGraph.getLineHashY_trbl(currY);
                relaxFrom(curr, currX, currY, list, currX+1);
                
                int y = currY+1;
                while(true) {
                    int x = ceilDivide((currX-parX)*(y-currY), (currY-parY)) + currX;
                    if (!visibilityGraph.withinEllipseBoxBounds(x,y)) break;

                    list = visibilityGraph.getLineHashY_tlbr(y);
                    relaxFrom(curr, currX, currY, list, x);
                    
                    y++;
                }
                
            } else if (parX > currX) {
                /**         .-
                 *      .-'     |
                 *      |_|     V
                 */
                list = visibilityGraph.getLineHashX_tlbr(currX);
                relaxTo(curr, currX, currY, list, currY-1);

                list = visibilityGraph.getLineHashX_trbl(currX);
                relaxTo(curr, currX, currY, list, currY-1);

                int x = currX-1;;
                while(true) {
                    int y = (currY-parY)*(x-currX)/(currX-parX) + currY;
                    if (!visibilityGraph.withinEllipseBoxBounds(x,y)) break;

                    list = visibilityGraph.getLineHashX_tlbr(x);
                    relaxTo(curr, currX, currY, list, y);
                    
                    x--;
                }
            }
        }

        
        if (graph.topRightOfBlockedTile(currX, currY)) {
            if (parX < currX) {
                /**  -.
                 *      '-.    |
                 *      |_|    v
                 */
                list = visibilityGraph.getLineHashX_tlbr(currX);
                relaxTo(curr, currX, currY, list, currY-1);
                
                list = visibilityGraph.getLineHashX_trbl(currX);
                relaxTo(curr, currX, currY, list, currY-1);

                int x = currX+1;
                while(true) {
                    int y = (currY-parY)*(x-currX)/(currX-parX) + currY;
                    if (!visibilityGraph.withinEllipseBoxBounds(x,y)) break;

                    list = visibilityGraph.getLineHashX_trbl(x);
                    relaxTo(curr, currX, currY, list, y);
                    
                    x++;
                }
                
            } else if (parY < currY) {
                /**      _
                 *      |_|\      <=
                 *          \
                 */
                list = visibilityGraph.getLineHashY_tlbr(currY);
                relaxTo(curr, currX, currY, list, currX-1);

                list = visibilityGraph.getLineHashY_trbl(currY);
                relaxTo(curr, currX, currY, list, currX-1);
                
                int y = currY+1;
                while(true) {
                    int x = (currX-parX)*(y-currY)/(currY-parY) + currX;
                    if (!visibilityGraph.withinEllipseBoxBounds(x,y)) break;

                    list = visibilityGraph.getLineHashY_trbl(y);
                    relaxTo(curr, currX, currY, list, x);
                    
                    y++;
                }
            }
        }

        
        if (graph.bottomLeftOfBlockedTile(currX, currY)) {
            if (parY > currY) {
                /**      _
                 *  '--_|_|     =>
                 */
                list = visibilityGraph.getLineHashY_tlbr(currY);
                relaxFrom(curr, currX, currY, list, currX+1);
                
                list = visibilityGraph.getLineHashY_trbl(currY);
                relaxFrom(curr, currX, currY, list, currX+1);
                
                int y = currY-1;
                while(true) {
                    int x = ceilDivide((currX-parX)*(y-currY), (currY-parY)) + currX;
                    if (!visibilityGraph.withinEllipseBoxBounds(x,y)) break;

                    list = visibilityGraph.getLineHashY_trbl(y);
                    relaxFrom(curr, currX, currY, list, x);
                    
                    y--;
                }
                
            } else if (parX > currX) {
                /**     _
                 *     |_|    ^
                 *     '--.   |
                 */
                list = visibilityGraph.getLineHashX_tlbr(currX);
                relaxFrom(curr, currX, currY, list, currY+1);

                list = visibilityGraph.getLineHashX_trbl(currX);
                relaxFrom(curr, currX, currY, list, currY+1);

                int x = currX-1;;
                while(true) {
                    int y = ceilDivide((currY-parY)*(x-currX), currX-parX) + currY;
                    if (!visibilityGraph.withinEllipseBoxBounds(x,y)) break;

                    list = visibilityGraph.getLineHashX_trbl(x);
                    relaxFrom(curr, currX, currY, list, y);
                    
                    x--;
                }
            }
        }
    }



    private void relaxTo(int curr, int currX, int currY, ArrayList<int[]> list, int upp) {
        int i = IncrementalVisibilityGraph.seekTo(list, upp);
        boolean passedBefore = false;
        for (; i >= 0; --i) {
            boolean distanceCheck = tryRelax(curr, currX, currY, list.get(i)[0]);
            
            if (distanceCheck) { // making use of convex nature of ellipse.
                passedBefore = true;
            } else if (passedBefore) {
                break;
            }
        }
    }

    private void relaxFrom(int curr, int currX, int currY, ArrayList<int[]> list, int low) {
        int i = IncrementalVisibilityGraph.seekFrom(list, low);
        boolean passedBefore = false;
        for (; i < list.size(); ++i) {
            boolean distanceCheck = tryRelax(curr, currX, currY, list.get(i)[0]);

            if (distanceCheck) { // making use of convex nature of ellipse.
                passedBefore = true;
            } else if (passedBefore) {
                break;
            }
        }
    }
    
    private static int ceilDivide(int n, int d) {
        if (d<0) {
            d = -d;
            n = -n;
        }
        return (n+d-1)/d;
    }
    

    private static final float BUFFER = 0.001f;
    /**
     * Tries to relax.
     * Returns false iff it fails the "withinDistanceBuffer" check.
     */
    private boolean tryRelax(int curr, int currX, int currY, int dest) {        
        // Check 1 - if dest within ellipse between curr and goal
        float remainingDistance = pathLength - distance[curr];
        Point pDest = visibilityGraph.coordinateOf(dest);
        float pathLengthCurrDest = graph.distance(currX, currY, pDest.x, pDest.y);
        float pathLengthDestGoal = visibilityGraph.lowerBoundRemainingDistance(pDest.x, pDest.y);
        boolean isEdgeOfEllipse = pathLengthCurrDest + graph.distance(pDest.x, pDest.y, ex, ey) <= remainingDistance + BUFFER;
        
        // Check 1a - Lower bound distance
        if (pathLengthCurrDest + pathLengthDestGoal > remainingDistance + BUFFER) return isEdgeOfEllipse;
        
        // Check 1b - Upper bound distance
        float pathLengthCurrGoal = visibilityGraph.lowerBoundRemainingDistance(currX, currY);
        if (pathLengthCurrGoal == Float.POSITIVE_INFINITY) pathLengthCurrGoal = pathLengthDestGoal;
        //System.out.println(pathLengthCurrDest + " | " + (Math.abs(pathLengthDestGoal - pathLengthCurrGoal)));
        if (pathLengthCurrDest + BUFFER < Math.abs(pathLengthDestGoal - pathLengthCurrGoal)) return isEdgeOfEllipse;
        
        // Check 2 - visited
        if (visited[dest]) return isEdgeOfEllipse;
        
        // Check 3 - line of sight. (slowest?)
        if (!graph.lineOfSight(currX, currY, pDest.x, pDest.y)) return isEdgeOfEllipse;
        
        if (relax(curr, dest, pathLengthCurrDest)) {
            //System.out.println("Relax " + ns(curr) + " -> " + ns(dest));
            // If relaxation is done.
            //pq.decreaseKey(dest, distance[dest] + heuristic(pDest.x, pDest.y)); // I doubt the heuristic makes a difference.
            pq.decreaseKey(dest, distance[dest]);
        }
        return isEdgeOfEllipse;
    }

    private int pathLength() {
        int length = 0;
        int current = visibilityGraph.endNode();
        while (current != -1) {
            current = parent[current];
            length++;
        }
        return length;
    }
    
    @Override
    public int[][] getPath() {
        if (visibilityGraph == null) {
            return new int[0][];
        }
        
        int length = pathLength();
        int[][] path = new int[length][];
        int current = visibilityGraph.endNode();
        
        int index = length-1;
        while (current != -1) {
            Point point = visibilityGraph.coordinateOf(current);
            int x = point.x;
            int y = point.y;
            
            path[index] = new int[2];
            path[index][0] = x;
            path[index][1] = y;
            
            index--;
            current = parent[current];
        }
        
        return path;
    }

    @Override
    protected int goalParentIndex() {
        return visibilityGraph.endNode();
    }
    
    @Override
    protected Integer[] snapshotEdge(int endIndex) {
        Integer[] edge = new Integer[4];
        int startIndex = parent[endIndex];
        Point startPoint = visibilityGraph.coordinateOf(startIndex);
        Point endPoint = visibilityGraph.coordinateOf(endIndex);
        edge[0] = startPoint.x;
        edge[1] = startPoint.y;
        edge[2] = endPoint.x;
        edge[3] = endPoint.y;
        return edge;
    }

    @Override
    protected Integer[] snapshotVertex(int index) {
        if (visited[index]) {
            Point point = visibilityGraph.coordinateOf(index);
            Integer[] edge = new Integer[2];
            edge[0] = point.x;
            edge[1] = point.y;
            return edge;
        }
        return null;
    }

}
