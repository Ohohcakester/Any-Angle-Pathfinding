package draw;

import grid.GridGraph;

import java.awt.Color;
import java.util.ArrayList;

import main.analysis.ProblemAnalysis;
import main.analysis.TwoPoint;
import main.testgen.Stringifier;
import main.testgen.TestDataGenerator;
import algorithms.datatypes.Point;

public class EditorUI extends DrawCanvas {
    private int sx;
    private int sy;
    private int ex;
    private int ey;
    private final GridPointSet pointSet;
    private final ArrayList<ArrayList<Point>> connectedSets;
    private final String mazeName;

    public EditorUI(GridGraph gridGraph, ArrayList<ArrayList<Point>> connectedSets, String mazeName) {
        super(gridGraph);
        this.mazeName = mazeName;
        this.connectedSets = connectedSets;
        pointSet = new GridPointSet();
        pointSet.setMinCircleSize();
        sx = sy = ex = ey = -1;
    }
    
    public void addStartPoint(int x, int y) {
        sx = x;
        sy = y;
        refreshPoints();
    }

    public void addEndPoint(int x, int y) {
        ex = x;
        ey = y;
        refreshPoints();
    }

    private void refreshPoints() {
        pointSet.clear();
        if (startEndConnected()) {
            pointSet.addPoint(sx, sy, Color.ORANGE);
            pointSet.addPoint(ex, ey, Color.YELLOW);
            
        } else {
            if (sx != -1) {
                pointSet.addPoint(sx, sy, Color.ORANGE);
            }
            if (sy != -1) {
                pointSet.addPoint(ex, ey, Color.RED);
            }
        }
        this.changeSet(pointSet);
    }
    
    private boolean startEndConnected() {
        if (sx == -1 || sy == -1) return false;
        
        Point start = new Point(sx, sy);
        Point end = new Point(ex, ey);
        for (int i=0; i<connectedSets.size(); i++) {
            ArrayList<Point> currentSet = connectedSets.get(i);
            if (currentSet.contains(start)) {
                return currentSet.contains(end);
            } else if (currentSet.contains(end)) {
                return false;
            }
        }
        return false;
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

    public void printPathAnalysis() {
        if (sx == -1 || ex == -1) return;
        ProblemAnalysis problemAnalysis = new ProblemAnalysis(gridGraph, sx, sy, ex, ey);
        System.out.println("=Problem Analysis:=================");
        System.out.println(problemAnalysis);
        System.out.println("-Problem Name:---------------------");
        System.out.println(Stringifier.makeProblemName(sx, sy, ex, ey));
    }

}
