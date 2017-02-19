package algorithms;

import java.util.Arrays;

import java.awt.geom.Point2D;
import pgraph.alg.Path;

import grid.GridGraph;

import pgraph.alg.AnyaSearch;
import pgraph.anya.AnyaExpansionPolicy;
import pgraph.anya.AnyaInterval;
import pgraph.anya.AnyaNode;
import pgraph.grid.BitpackedGrid;
//import pgraph.grid.BitpackedGridExpansionPolicy;


public class AnyaAlgorithm extends PathFindingAlgorithm {

    private static AnyaSearch anya = null;
    private static GridGraph storedGraph = null;

    private Path<AnyaNode> pathStartNode = null;

    private static void initialise(GridGraph graph) {
        if (graph == storedGraph) return;
        try {
            BitpackedGrid grid = new BitpackedGrid(graph);
            AnyaAlgorithm.anya = new AnyaSearch(new AnyaExpansionPolicy(grid));
            AnyaAlgorithm.storedGraph = graph;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AnyaAlgorithm(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey);
        initialise(graph);

        AnyaNode start = new AnyaNode(null, new AnyaInterval(0, 0, 0), 0, 0);
        AnyaNode target = new AnyaNode(null, new AnyaInterval(0, 0, 0), 0, 0);
        anya.mb_start_ = start;
        anya.mb_target_ = target;

        start.root.setLocation(sx, sy);
        start.interval.init(sx, sx, sy);
        target.root.setLocation(ex, ey);
        target.interval.init(ex, ex, ey);
    }

    /**
     * Call this to compute the path.
     */
    public void computePath() {
        pathStartNode = anya.search(anya.mb_start_, anya.mb_target_);
        //pathLength = anya.mb_cost_;
    }

    /**
     * @return retrieve the path computed by the algorithm
     */
    public int[][] getPath() {
        int length = 0;
        Path<AnyaNode> current = pathStartNode;
        while (current != null) {
            current = current.getNext();
            ++length;
        }
        int[][] path = new int[length][];

        current = pathStartNode;
        int i = 0; 
        while (current != null) {
            Point2D.Double p = current.getVertex().root;
            path[i] = new int[]{(int)p.getX(), (int)p.getY()};
            current = current.getNext();
            ++i;
        }

        for (int j=0;j<path.length;++j) {
            System.out.println(Arrays.toString(path[j]));
        }

        return path;
    }
    
    /**
     * @return directly get path length without computing path.
     * Has to run fast, unlike getPath.
     */
    public float getPathLength() {
        return (float)anya.mb_cost_;
    }
    


}