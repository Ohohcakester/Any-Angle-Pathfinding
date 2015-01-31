package algorithms;

import grid.GridGraph;
import algorithms.visibilitygraph.RestrictedVisibilityGraph;


/**
 * STEP 1: Use Theta* (or some other algo) to find an upper bound L for the
 *  shortest path length from s to t.
 * 
 * STEP 2: Draw an ellipse with s and t as the focal point and "string length" L.
 * 
 * STEP 3: Select all points within the ellipse and construct a visibility graph.
 * 
 * STEP 4: Use Dijkstra's algorithm on the visibility graph.
 */
public class RestrictedVisibilityGraphAlgorithm extends VisibilityGraphAlgorithm {

    public RestrictedVisibilityGraphAlgorithm(GridGraph graph, int sx, int sy,
            int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }


    @Override
    public void computePath() {
        PathFindingAlgorithm algo = new BasicThetaStar(graph, sx ,sy, ex, ey);
        
        if (false && isRecording()) {
            algo.startRecording();
            algo.computePath();
            inheritSnapshotListFrom(algo);
        } else {
            algo.computePath();
        }

        float pathLength = algo.getPathLength();
        if (pathLength < 0.001f) {
            return;
        }
        
        visibilityGraph = new RestrictedVisibilityGraph(graph, sx, sy, ex, ey, pathLength);
        
        if (isRecording()) {
            visibilityGraph.setSaveSnapshotFunction(()->saveVisibilityGraphSnapshot());
            visibilityGraph.initialise();
            saveVisibilityGraphSnapshot();
        } else {
            visibilityGraph.initialise();
        }
        
        distance = new Float[visibilityGraph.size()];
        parent = new int[visibilityGraph.size()];

        initialise(visibilityGraph.startNode());
        visited = new boolean[visibilityGraph.size()];
        
        if (slowDijkstra) {
            slowDijkstra();
        } else {
            pqDijkstra();
        }
    }

    @Override
    public int[][] getPath() {
        if (visibilityGraph == null) {
            return new int[0][];
        }
        return super.getPath();
    }

    @Override
    public void printStatistics() {
        if (visibilityGraph == null) {
            System.out.println("No Visibility Graph computed");
        } else {
            super.printStatistics();
        }
    }
    
}
