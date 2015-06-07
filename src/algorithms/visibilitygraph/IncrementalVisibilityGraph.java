package algorithms.visibilitygraph;

import grid.GridGraph;

import java.util.ArrayList;
import java.util.Arrays;

import algorithms.datatypes.Point;

public class IncrementalVisibilityGraph extends RestrictedVisibilityGraph {
    
    int xMin, xMax, yMin, yMax;

    // Top left and bottom right.
    private ArrayList<ArrayList<int[]>> lineHashX_tlbr; // stores index, y pairs. Hashed by X coordinate. i.e. VERTICAL
    private ArrayList<ArrayList<int[]>> lineHashY_tlbr; // stores index, x pairs. Hashed by Y coordinate. i.e. HORIZONTAL
    // Bottom left and top right.
    private ArrayList<ArrayList<int[]>> lineHashX_trbl; // stores index, y pairs. Hashed by X coordinate. i.e. VERTICAL
    private ArrayList<ArrayList<int[]>> lineHashY_trbl; // stores index, x pairs. Hashed by Y coordinate. i.e. HORIZONTAL
    private int lineHashXOffset;
    private int lineHashYOffset;

    public IncrementalVisibilityGraph(GridGraph graph, int sx, int sy, int ex,
            int ey, float pathLength) {
        super(graph, sx, sy, ex, ey, pathLength);
    }
    

    @Override
    public void initialise() {
        nodeList = new ArrayList<>();
        
        addNodes();
        addStartAndEnd(sx, sy, ex, ey);
    }

    @Override
    protected void addNodes() {
        GridEllipse ellipse = new GridEllipse(sx, sy, ex, ey, pathLength);
        
        yOffset = ellipse.yMin;
        xMin = Integer.MAX_VALUE;
        xMax = Integer.MIN_VALUE;
        for (int i=0; i<ellipse.xLeft.length; ++i) {
            int left = ellipse.xLeft[i];
            int right = ellipse.xRight[i];
            if (left < xMin) xMin = left;
            if (right > xMax) xMax = right;
        }
        yMin = ellipse.yMin;
        yMax = ellipse.yMax;
        
        initialiseLineHash(xMin, xMax, yMin, yMax);
        
        
        nodeIndex = new int[ellipse.yMax - ellipse.yMin + 1][];
        xOffsets = new int[nodeIndex.length];
        
        for (int y=0;y<nodeIndex.length;y++) {
            nodeIndex[y] = new int[ellipse.xRight[y] - ellipse.xLeft[y] + 1];
            xOffsets[y] = ellipse.xLeft[y];
            for (int x=0; x<nodeIndex[y].length; x++) {
                int xAbs = x + xOffsets[y];
                int yAbs = y + yOffset;

                if (xAbs == sx && yAbs == sy) {
                    startIndex = nodeIndex[y][x] = assignNodeAndHash(xAbs, yAbs, false, false);
                } else if (xAbs == ex && yAbs == ey) {
                    endIndex = nodeIndex[y][x] = assignNodeAndHash(xAbs, yAbs, true, true);
                } else if (isCorner(xAbs, yAbs)) {
                    boolean tlbr = graph.isBlocked(xAbs, yAbs-1) || graph.isBlocked(xAbs-1, yAbs);
                    boolean trbl = graph.isBlocked(xAbs-1, yAbs-1) || graph.isBlocked(xAbs, yAbs);
                    
                    nodeIndex[y][x] = assignNodeAndHash(xAbs, yAbs, tlbr, trbl);
                } else {
                    nodeIndex[y][x] = -1;
                }
            }
        }
        //printAll();
    }

    public boolean withinEllipseBoxBounds(int x, int y) {
        return (x >= xMin && y >= yMin && x <= xMax && y <= yMax);
    }


    /*public boolean inEllipse(int x, int y) {
        y -= yOffset;
        if (y < 0 || y >= xOffsets.length) return false;
        x -= xOffsets[y];
        if (x < 0 || x >= nodeIndex[y].length) return false;
        return true;
    }*/

    private void initialiseLineHash(int xMin, int xMax, int yMin, int yMax) {
        lineHashXOffset = xMin;
        lineHashYOffset = yMin;

        lineHashX_tlbr = new ArrayList<>();
        lineHashY_tlbr = new ArrayList<>();
        lineHashX_trbl = new ArrayList<>();
        lineHashY_trbl = new ArrayList<>();
        for (int x=xMin; x<=xMax; ++x) {
            lineHashX_tlbr.add(new ArrayList<>());
            lineHashX_trbl.add(new ArrayList<>());
        }
        for (int y=yMin; y<=yMax; ++y) {
            lineHashY_tlbr.add(new ArrayList<>());
            lineHashY_trbl.add(new ArrayList<>());
        }
    }

    public float straightLineDistance(int node1, int node2) {
        Point p1 = nodeList.get(node1);
        Point p2 = nodeList.get(node2);
        return graph.distance(p1.x, p1.y, p2.x, p2.y);
    }

    public float straightLineDistance(int node1, int x, int y) {
        Point p1 = nodeList.get(node1);
        return graph.distance(p1.x, p1.y, x, y);
    }
    
    public ArrayList<int[]> getLineHashX_tlbr(int x) {
        return lineHashX_tlbr.get(x-lineHashXOffset);
    }
    
    public ArrayList<int[]> getLineHashY_tlbr(int y) {
        return lineHashY_tlbr.get(y-lineHashYOffset);
    }
    
    public ArrayList<int[]> getLineHashX_trbl(int x) {
        return lineHashX_trbl.get(x-lineHashXOffset);
    }
    
    public ArrayList<int[]> getLineHashY_trbl(int y) {
        return lineHashY_trbl.get(y-lineHashYOffset);
    }
    
    public ArrayList<Point> getAllNodes() {
        return nodeList;
    }

    /**
     * Returns the index of the first item in the right tail. Binary Search.
     */
    public static int seekFrom(ArrayList<int[]> list, int low) {
        int a = 0;
        int b = list.size();
        
        while(a<b) {
            int mid = (a+b)/2;
            int get = list.get(mid)[1];
            if (low == get) {
                return mid;
            }
            else if (low < get) {
                b = mid;
            } else {
                a = mid+1;
            }
        }
        return a;
    }

    /**
     * Returns the index of the last item in the left tail. Binary Search.
     */
    public static int seekTo(ArrayList<int[]> list, int upp) {
        int a = -1;
        int b = list.size()-1;
        
        while(a<b) {
            int mid = (a+b+1)/2;
            int get = list.get(mid)[1];
            if (upp == get) {
                return mid;
            }
            else if (upp < get) {
                b = mid-1;
            } else {
                a = mid;
            }
        }
        return b;
    }

    /**
     * This should be called in the order of increasing y, then increasing x.
     */
    protected int assignNodeAndHash(int xAbs, int yAbs, boolean tlbr, boolean trbl) {
        int index = nodeList.size();
        nodeList.add(new Point(xAbs,yAbs));
        //int index = assignNodeIndex(xAbs, yAbs);

        if (tlbr) {
            lineHashX_tlbr.get(xAbs-lineHashXOffset).add(new int[]{index,yAbs});
            lineHashY_tlbr.get(yAbs-lineHashYOffset).add(new int[]{index,xAbs});
        }
        if (trbl) {
            lineHashX_trbl.get(xAbs-lineHashXOffset).add(new int[]{index,yAbs});
            lineHashY_trbl.get(yAbs-lineHashYOffset).add(new int[]{index,xAbs});
        }
        
        return index;
    }

    /*protected final int assignNodeIndex(int x, int y) {
        int index = nodeList.size();
        nodeList.add(new Point(x,y));
        return index;
    }*/

    
    @Override
    protected int indexOf(int x, int y) {
        int yRel = y-yOffset;
        return nodeIndex[yRel][x-xOffsets[yRel]];
    }
    
    @Override
    protected boolean isNode(int x, int y) {
        int yRel = y-yOffset;
        return nodeIndex[yRel][x-xOffsets[yRel]] != -1;
    }
    
    private void printAll() {
        ArrayList<int[]> list;
        
        p("X - TLBR ");
        for (int x=0; x<lineHashX_tlbr.size(); ++x) {
            int absX = x+lineHashXOffset;
            list = getLineHashX_tlbr(absX);
            for (int[] y : list) {
                p(absX, y[1]);
            }
        }
        pl();
        
        p("X - TRBL ");
        for (int x=0; x<lineHashX_trbl.size(); ++x) {
            int absX = x+lineHashXOffset;
            list = getLineHashX_trbl(absX);
            for (int[] y : list) {
                p(absX, y[1]);
            }
        }
        pl();
        
        p("Y - TLBR ");
        for (int y=0; y<lineHashY_tlbr.size(); ++y) {
            int absY = y+lineHashYOffset;
            list = getLineHashY_tlbr(absY);
            for (int[] x : list) {
                p(x[1], absY);
            }
        }
        pl();
        
        p("Y - TRBL ");
        for (int y=0; y<lineHashY_trbl.size(); ++y) {
            int absY = y+lineHashYOffset;
            list = getLineHashY_trbl(absY);
            for (int[] x : list) {
                p(x[1], absY);
            }
        }
        pl();
    }

    private void p(Object m) {
        System.out.print(m);
    }

    private void pl() {
        System.out.println();
    }

    private void p(int x, int y) {
        System.out.print(x+","+y + " | ");
    }

    
    public static void main(String[] args) {
        ArrayList<int[]> arr = new ArrayList<>();
        for (int i=0; i<50; i += 3) {
            arr.add(new int[]{arr.size(),i});
        }
        for (int[] i : arr) {
            System.out.print(Arrays.toString(i) + "-"); 
        }
        System.out.println();
        for (int val = 43; val < 55; ++val) {
            int t = seekTo(arr, val);
            System.out.println(val + " To : " + t);
            //int f = seekFrom(arr, val);
            //System.out.println(val + " From : " + f);
        }
    }
}
