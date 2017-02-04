package draw;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import algorithms.PathFindingAlgorithm;
import algorithms.datatypes.Point;
import algorithms.datatypes.SnapshotItem;
import algorithms.sparsevgs.SparseVisibilityGraphAlgorithm;
import grid.GridGraph;
import grid.StartGoalPoints;
import main.AlgoFunction;
import main.AnyAnglePathfinding;
import main.analysis.MazeAnalysis;
import main.analysis.ProblemAnalysis;
import main.analysis.TwoPoint;
import main.testgen.Stringifier;
import main.testgen.TestDataGenerator;
import main.utility.Utility;
import uiandio.FileIO;

public class EditorUI extends DrawCanvas {
    
    public enum PathComputeMode {
        NO_COMPUTE,
        PATH_ONLY,
        SEARCH_TREE,
    }
    
    private int sx;
    private int sy;
    private int ex;
    private int ey;
    private final GridPointSet pointSet;
    private final AlgoFunction algoFunction;
    private final int[][] connectedComponentIndex;
    private final String mazeName;
    private GridLineSet lineSet;
    private boolean isRealTimePathfinding;
    private PathComputeMode pathComputeMode = PathComputeMode.NO_COMPUTE;
    

    public EditorUI(GridGraph gridGraph, AlgoFunction algoFunction, ArrayList<ArrayList<Point>> connectedSets, String mazeName, StartGoalPoints startGoalPoints) {
        super(gridGraph);
        this.algoFunction = algoFunction;
        this.mazeName = mazeName;
        this.connectedComponentIndex = generateConnectedComponentIndexes(connectedSets);
        pointSet = new GridPointSet();
        pointSet.setMinCircleSize();
        
        if (startGoalPoints == null) {
            sx = -1;
            sy = -1;
            ex = -1;
            ey = -1;
        } else {
            sx = startGoalPoints.sx;
            sy = startGoalPoints.sy;
            ex = startGoalPoints.ex;
            ey = startGoalPoints.ey;
        }
        refreshPoints();
    }
    
    private final int[][] generateConnectedComponentIndexes(ArrayList<ArrayList<Point>> connectedSets) {
        int xCount = gridGraph.sizeX+1;
        int yCount = gridGraph.sizeY+1;
        
        int[][] indexes = new int[yCount][];
        for (int y=0;y<indexes.length;++y) {
            int[] row = new int[xCount];
            Arrays.fill(row, -1);
            indexes[y] = row;
        }
        
        for (int i=0;i<connectedSets.size();++i) {
            for (Point p : connectedSets.get(i)) {
                indexes[p.y][p.x] = i;
            }
        }
        return indexes;
    }

    public void addStartPoint(int x, int y) {
        sx = x;
        sy = y;
        clearPath();
        refreshPoints();
    }

    public void addEndPoint(int x, int y) {
        ex = x;
        ey = y;
        clearPath();
        refreshPoints();
    }

    private void refreshPoints() {
        pointSet.clear();
        if (startEndConnected()) {
            pointSet.addPoint(sx, sy, Color.ORANGE);
            pointSet.addPoint(ex, ey, Color.YELLOW);
            updatePath();
        } else {
            if (sx != -1) {
                pointSet.addPoint(sx, sy, Color.ORANGE);
            }
            if (ex != -1) {
                pointSet.addPoint(ex, ey, Color.RED);
            }
        }
        this.changeSet(pointSet);
    }
    
    public void setPathComputeMode(PathComputeMode mode) {
        this.pathComputeMode = mode;
        refreshPoints();
    }
    
    private void updatePath() {
        switch(pathComputeMode) {
            case NO_COMPUTE:
                clearPath();
                return;
            case PATH_ONLY:
                autoComputePath();
                return;
            case SEARCH_TREE:
                autoComputeSearchTree();
                return;
        }
    }
    
    private void autoComputePath() {
        PathFindingAlgorithm algo = algoFunction.getAlgo(gridGraph, sx, sy, ex, ey);
        algo.computePath();
        int[][] path = algo.getPath();
        
        drawPath(path);
    }
    
    private void autoComputeSearchTree() {
        PathFindingAlgorithm algo = algoFunction.getAlgo(gridGraph, sx, sy, ex, ey);
        algo.computePath();
        List<SnapshotItem> snapshot = algo.getCurrentSearchSnapshot();
        GridObjects gridObjects = GridObjects.create(snapshot);
        this.lineSet = gridObjects.gridLineSet;
        
        this.changeSet(lineSet);
    }

    private void drawPath(int[][] path) {
        lineSet = new GridLineSet();
        for (int i=1;i<path.length;++i) {
            int[] prev = path[i-1];
            int[] curr = path[i];
            lineSet.addLine(prev[0], prev[1], curr[0], curr[1], Color.BLUE);
        }
        this.changeSet(lineSet);
    }
    
    private void clearPath() {
        lineSet = null;
        this.changeSet(lineSet);
    }
    
    private boolean startEndConnected() {
        if (sx == -1 || ey == -1) return false;
        if (connectedComponentIndex[sy][sx] == -1 || connectedComponentIndex[ey][ex] == -1) return false;
        return (connectedComponentIndex[sy][sx] == connectedComponentIndex[ey][ex]);
    }

    public void generatePath() {
        if (sx == -1 || ex == -1) return;
        ArrayList<TwoPoint> tpList = TestDataGenerator.generateTwoPointList(sx, sy, ex, ey);
        TestDataGenerator.generateTestData(gridGraph, tpList, mazeName, false);
    }

    public void generateMazeAnalysis() {
        ArrayList<TwoPoint> tpList = new ArrayList<>();
        TestDataGenerator.generateTestData(gridGraph, tpList, mazeName, true);
    }

    public void printMazeAnalysis() {
        MazeAnalysis mazeAnalysis = new MazeAnalysis(gridGraph);
        System.out.println("=Maze Analysis:=================");
        System.out.println(mazeAnalysis);
    }

    public void printPathAnalysis() {
        if (sx == -1 || ex == -1) return;
        ProblemAnalysis problemAnalysis = ProblemAnalysis.computeFast(gridGraph, sx, sy, ex, ey);
        System.out.println("=Problem Analysis:=================");
        System.out.println(problemAnalysis);
        System.out.println("-Problem Name:---------------------");
        System.out.println(Stringifier.makeProblemName(sx, sy, ex, ey));
        drawPath(problemAnalysis.path);
    }

    public void generateScen() {
        String filePath = AnyAnglePathfinding.PATH_ANALYSISDATA;
        String mapName = mazeName;
        double shortestPath = Utility.computeOptimalPathLengthOnline(gridGraph, new Point(sx,sy), new Point(ex,ey));

        System.out.println("-Writing to folder: " + filePath);
        FileIO.makeDirs(filePath);
        {
            // Generate .map file
            FileIO fileIO = new FileIO(filePath + mapName + ".map");

            fileIO.writeLine("type octile");
            fileIO.writeLine("height " + gridGraph.sizeY);
            fileIO.writeLine("width " + gridGraph.sizeX);
            fileIO.writeLine("map");
            for (int y=0;y<gridGraph.sizeY;++y) {
                StringBuilder sb = new StringBuilder();
                for (int x=0;x<gridGraph.sizeX;++x) {
                    sb.append(gridGraph.isBlocked(x,y) ? "@" : ".");
                }
                fileIO.writeLine(sb.toString());
            }
            fileIO.close();
        }
        {
            // Generate .scen file
            FileIO fileIO = new FileIO(filePath + mapName + ".map.scen");
            
            fileIO.writeLine("version 1");
            String[] s = new String[]{
                "1",
                mapName + ".map",
                gridGraph.sizeX + "",
                gridGraph.sizeY + "",
                sx + "",
                sy + "",
                ex + "",
                ey + "",
                shortestPath + ""
            };
            fileIO.writeLine(String.join("\t", s));
            fileIO.close();
        }
        
        System.out.println("-Write complete.");
    }
    
    public void onRealTimePathfind() {
        isRealTimePathfinding = true;
        if (pathComputeMode == PathComputeMode.NO_COMPUTE) {
            setPathComputeMode(PathComputeMode.PATH_ONLY);
        }
    }
    
    public void offRealTimePathfind() {
        isRealTimePathfinding = false;
    }

    public void realTimePathfindUpdate(int x, int y) {
        if (!isRealTimePathfinding) return;
        addStartPoint(x, y);
    }

}
