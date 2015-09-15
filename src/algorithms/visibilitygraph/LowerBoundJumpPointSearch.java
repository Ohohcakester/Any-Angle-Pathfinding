package algorithms.visibilitygraph;

import grid.GridGraph;
import algorithms.JumpPointSearch;
import algorithms.priorityqueue.ReusableIndirectHeap;

public class LowerBoundJumpPointSearch extends JumpPointSearch {
    
    private static final float APPROXIMATION_RATIO = (float)Math.sqrt(4 - 2*Math.sqrt(2));
    private static final float BUFFER = 0.000001f;
    private final float upperBound;

    public LowerBoundJumpPointSearch(GridGraph graph, int sx, int sy, int ex, int ey, float existingPathLength) {
        super(graph, sx, sy, ex, ey);
        this.upperBound = existingPathLength;
    }


    @Override
    public void computePath() {
        neighboursdX = new int[8];
        neighboursdY = new int[8];
        neighbourCount = 0;
        
        int totalSize = (graph.sizeX+1) * (graph.sizeY+1);

        int start = toOneDimIndex(sx, sy);
        //finish = toOneDimIndex(ex, ey);
        
        pq = new ReusableIndirectHeap(totalSize);
        this.initialiseMemory(totalSize, Float.POSITIVE_INFINITY, -1, false);
        
        initialise(start);
        
        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            if (distance(current) == Float.POSITIVE_INFINITY) {
                maybeSaveSearchSnapshot();
                break;
            }
            setVisited(current, true);

            int x = toTwoDimX(current);
            int y = toTwoDimY(current);
            
            computeNeighbours(current, x, y); // stores neighbours in attribute.

            for (int i=0;i<neighbourCount;++i) {
                int dx = neighboursdX[i];
                int dy = neighboursdY[i];
                
                int successor = jump(x, y, dx, dy);
                if (successor != -1) {
                    tryRelax(current, x, y, successor);
                }
            }
            
            maybeSaveSearchSnapshot();
        }

        maybePostSmooth();
    }

    @Override
    protected void tryRelax(int current, int currX, int currY, int destination) {
        if (visited(destination)) return;
        
        int destX = toTwoDimX(destination);
        int destY = toTwoDimY(destination);
        
        if (relax(current, destination, graph.octileDistance(currX, currY, destX, destY), destX, destY)) {
            // If relaxation is done.
            pq.decreaseKey(destination, distance(destination));
        }
    }
    
    protected boolean relax(int u, int v, float weightUV, int currX, int currY) {
        // return true iff relaxation is done.
        
        float newWeight = distance(u) + weightUV;
        //System.out.println("Relax " + s(u) + "->" + s(v) + " = " + newWeight);
        //if (newWeight / APPROXIMATION_RATIO + graph.distance(currX, currY, ex, ey) > upperBound + BUFFER) return false;
        if (newWeight + graph.distance(currX, currY, ex, ey) > upperBound*APPROXIMATION_RATIO + BUFFER) return false;
        
        if (newWeight <= distance(v)) {
            setDistance(v, newWeight);
            setParent(v, u);
            //maybeSaveSearchSnapshot();
            return true;
        }
        return false;
    }
    
    public float heuristicValue(int x, int y) {
        int index = graph.toOneDimIndex(x, y);
        return distance(index)/APPROXIMATION_RATIO;
    }

    @Override
    protected int jump(int x, int y, int dx, int dy) {
        if (dx < 0) {
            if (dy < 0) {
                return jumpDL(x,y,x,y);
            } else if (dy > 0) {
                return jumpUL(x,y,x,y);
            } else {
                return jumpL(x,y,x,y);
            }
        } else if (dx > 0) {
            if (dy < 0) {
                return jumpDR(x,y,x,y);
            } else if (dy > 0) {
                return jumpUR(x,y,x,y);
            } else {
                return jumpR(x,y,x,y);
            }
        } else {
            if (dy < 0) {
                return jumpD(x,y,x,y);
            } else {
                return jumpU(x,y,x,y);
            }
        }
        
    }

    protected final boolean isOuterCorner(int x, int y) {
        boolean a = graph.isBlocked(x-1, y-1);
        boolean b = graph.isBlocked(x, y-1);
        boolean c = graph.isBlocked(x, y);
        boolean d = graph.isBlocked(x-1, y);
        
        return ((!a && !c) || (!d && !b)) && (a || b || c || d);
        
        /* NOTE:
         *   ___ ___
         *  |   |||||
         *  |...X'''| <-- this is considered a corner in the above definition
         *  |||||___|
         *  
         *  The definition below excludes the above case.
         */
    }
    
    private void maybeUpdateDistance(int parX, int parY, int x, int y) {
        if (!isOuterCorner(x,y)) return;
        //if (isOuterCorner(x,y)) return;

        int u = graph.toOneDimIndex(parX, parY);
        int v = graph.toOneDimIndex(x, y);
        float newWeight = graph.octileDistance(parX, parY, x, y) + distance(u);
        
        if (newWeight < distance(v)) {
            setDistance(v, newWeight);
            setParent(v, -2);
        }
    }

    
    private int jumpDL(int parX, int parY, int x, int y) {
        while(true) {
            x -= 1;
            y -= 1;
            if (graph.isBlocked(x, y)) return -1;
            // diagonal cannot be forced on vertices.
            if (jumpL(parX,parY,x,y) != -1) return toOneDimIndex(x,y);
            if (jumpD(parX,parY,x,y) != -1) return toOneDimIndex(x,y);
            maybeUpdateDistance(parX, parY, x, y);
        }
    }
    
    private int jumpDR(int parX, int parY, int x, int y) {
        while(true) {
            x += 1;
            y -= 1;
            if (graph.isBlocked(x-1, y)) return -1;
            // diagonal cannot be forced on vertices.
            if (jumpD(parX,parY,x,y) != -1) return toOneDimIndex(x,y);
            if (jumpR(parX,parY,x,y) != -1) return toOneDimIndex(x,y);
            maybeUpdateDistance(parX, parY, x, y);
        }
    }
    
    private int jumpUL(int parX, int parY, int x, int y) {
        while(true) {
            x -= 1;
            y += 1;
            if (graph.isBlocked(x, y-1)) return -1;
            // diagonal cannot be forced on vertices.
            if (jumpL(parX,parY,x,y) != -1) return toOneDimIndex(x,y);
            if (jumpU(parX,parY,x,y) != -1) return toOneDimIndex(x,y);
            maybeUpdateDistance(parX, parY, x, y);
        }
    }
    
    private int jumpUR(int parX, int parY, int x, int y) {
        while(true) {
            x += 1;
            y += 1;
            if (graph.isBlocked(x-1, y-1)) return -1;
            // diagonal cannot be forced on vertices.
            if (jumpU(parX,parY,x,y) != -1) return toOneDimIndex(x,y);
            if (jumpR(parX,parY,x,y) != -1) return toOneDimIndex(x,y);
            maybeUpdateDistance(parX, parY, x, y);
        }
    }
    
    private int jumpL(int parX, int parY, int x, int y) {
        while(true) {
            x -= 1;
            if (graph.isBlocked(x, y)) {
                if (graph.isBlocked(x, y-1)) {
                    return -1;
                } else {
                    if (!graph.isBlocked(x-1, y)) return toOneDimIndex(x,y);
                }
            }
            if (graph.isBlocked(x, y-1)) {
                if (!graph.isBlocked(x-1, y-1)) return toOneDimIndex(x,y);
            }
            maybeUpdateDistance(parX, parY, x, y);
        }
    }
    
    private int jumpR(int parX, int parY, int x, int y) {
        while(true) {
            x += 1;
            if (graph.isBlocked(x-1, y)) {
                if (graph.isBlocked(x-1, y-1)) {
                    return -1;
                } else {
                    if (!graph.isBlocked(x, y)) return toOneDimIndex(x,y);
                }
            }
            if (graph.isBlocked(x-1, y-1)) {
                if (!graph.isBlocked(x, y-1)) return toOneDimIndex(x,y);
            }
            maybeUpdateDistance(parX, parY, x, y);
        }
    }
    
    private int jumpD(int parX, int parY, int x, int y) {
        while(true) {
            y -= 1;
            if (graph.isBlocked(x, y)) {
                if (graph.isBlocked(x-1, y)) {
                    return -1;
                } else {
                    if (!graph.isBlocked(x, y-1)) return toOneDimIndex(x,y);
                }
            }
            if (graph.isBlocked(x-1, y)) {
                if (!graph.isBlocked(x-1, y-1)) return toOneDimIndex(x,y);
            }
            maybeUpdateDistance(parX, parY, x, y);
        }
    }
    
    private int jumpU(int parX, int parY, int x, int y) {
        while(true) {
            y += 1;
            if (graph.isBlocked(x, y-1)) {
                if (graph.isBlocked(x-1, y-1)) {
                    return -1;
                } else {
                    if (!graph.isBlocked(x, y)) return toOneDimIndex(x,y);
                }
            }
            if (graph.isBlocked(x-1, y-1)) {
                if (!graph.isBlocked(x-1, y)) return toOneDimIndex(x,y);
            }
            maybeUpdateDistance(parX, parY, x, y);
        }
    }

}
