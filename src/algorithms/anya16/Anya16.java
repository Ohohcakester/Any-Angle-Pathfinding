package algorithms.anya16;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import java.awt.geom.Point2D;

import grid.GridGraph;
import algorithms.datatypes.SnapshotItem;
import algorithms.PathFindingAlgorithm;

public class Anya16 extends PathFindingAlgorithm {

    private static final int RES = 10000;
    public ArrayList<SnapshotItem> currSnapshot = new ArrayList<>();
    
    private static AnyaSearch anya = null;
    private static GridGraph storedGraph = null;

    private Path<AnyaNode> pathStartNode = null;

    private static void initialise(GridGraph graph) {
        if (graph == storedGraph) return;
        try {
            BitpackedGrid grid = new BitpackedGrid(graph);
            Anya16.anya = new AnyaSearch(new AnyaExpansionPolicy(grid));
            Anya16.storedGraph = graph;
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        anya.isRecording = false;
    }

    public Anya16(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey);
        initialise(graph);

        anya.snapshotExpand = (a) -> snapshotExpand(a);
        anya.snapshotInsert = (a) -> snapshotInsert(a);
        anya.isRecording = false;

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

        return path;
    }
    
    /**
     * @return directly get path length without computing path.
     * Has to run fast, unlike getPath.
     */
    public float getPathLength() {
        return (float)anya.mb_cost_;
    }

    @Override
    public void startRecording() {
        super.startRecording();
        anya.isRecording = true;
    }
    
    @Override
    public void stopRecording() {
        super.stopRecording();
        anya.isRecording = false;
    }
    
    
    private final void snapshotInsert(AnyaNode anyaNode) {
        AnyaInterval in = anyaNode.interval;

        Integer[] line = new Integer[7];
        line[0] = in.getRow();
        line[1] = (int)(in.getLeft()*RES);
        line[2] = RES;
        line[3] = (int)(in.getRight()*RES);
        line[4] = RES;
        line[5] = (int)anyaNode.root.getX();
        line[6] = (int)anyaNode.root.getY();
        currSnapshot.add(SnapshotItem.generate(line));

        maybeSaveSearchSnapshot();
    }

    private final void snapshotExpand(AnyaNode anyaNode) {
        AnyaInterval in = anyaNode.interval;

        Integer[] line = new Integer[5];
        line[0] = in.getRow();
        line[1] = (int)(in.getLeft()*RES);
        line[2] = RES;
        line[3] = (int)(in.getRight()*RES);
        line[4] = RES;
        currSnapshot.add(SnapshotItem.generate(line));

        maybeSaveSearchSnapshot();
    }

    @Override
    protected List<SnapshotItem> computeSearchSnapshot() {
        return new ArrayList<>(currSnapshot);
    }

    public static void clearMemory() {
        anya = null;
        storedGraph = null;
        System.gc();
    }
}