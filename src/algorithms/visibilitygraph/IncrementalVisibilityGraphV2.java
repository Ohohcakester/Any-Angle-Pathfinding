package algorithms.visibilitygraph;

import grid.GridGraph;

import java.util.ArrayList;

import algorithms.datatypes.Point;

public class IncrementalVisibilityGraphV2 extends IncrementalVisibilityGraph {
    
    private ArrayList<Float> heuristicList;
    private LowerBoundJumpPointSearch lowerBoundSearch;

    public IncrementalVisibilityGraphV2(GridGraph graph, int sx, int sy,
            int ex, int ey, float pathLength, LowerBoundJumpPointSearch lowerBoundSearch) {
        super(graph, sx, sy, ex, ey, pathLength);
        this.lowerBoundSearch = lowerBoundSearch;
    }
    
    @Override
    public void initialise() {
        nodeList = new ArrayList<>();
        heuristicList = new ArrayList<>();
        
        addNodes();
        addStartAndEnd(sx, sy, ex, ey);
    }

    private float heuristic(int x, int y) {
        float sld = graph.distance(x, y, ex, ey);
        float lowerBoundDistance = lowerBoundSearch.heuristicValue(x, y);
        return Math.max(sld, lowerBoundDistance);
    }


    /**
     * This should be called in the order of increasing y, then increasing x.
     */
    @Override
    protected int assignNodeAndHash(int xAbs, int yAbs, boolean tlbr, boolean trbl) {
        int index = nodeList.size();
        nodeList.add(new Point(xAbs,yAbs));
        heuristicList.add(heuristic(xAbs, yAbs));
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

    public float lowerBoundRemainingDistance(int x, int y) {
        return heuristicList.get(indexOf(x, y));
    }
   
}
