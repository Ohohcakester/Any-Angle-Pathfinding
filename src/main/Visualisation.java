package main;

import grid.GridAndGoals;
import grid.GridGraph;
import grid.StartGoalPoints;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import algorithms.PathFindingAlgorithm;
import algorithms.datatypes.SnapshotItem;
import uiandio.CloseOnExitWindowListener;
import draw.DrawCanvas;
import draw.GridLineSet;
import draw.GridObjects;
import draw.KeyToggler;

public class Visualisation {
    
    public static void run() {
        traceAlgorithm();
    }


    /**
     * Conducts a trace of the current algorithm
     */
    private static void traceAlgorithm() {
        AnyAnglePathfinding.setDefaultAlgoFunction();           // choose an algorithm (go into this method to choose)
        GridAndGoals gridAndGoals = AnyAnglePathfinding.loadMaze();   // choose a grid (go into this method to choose)
        
        // Call this to record and display the algorithm in operation.
        displayAlgorithmOperation(gridAndGoals.gridGraph, gridAndGoals.startGoalPoints);
    }
    
    /**
     * Records the algorithm, the final path computed, and displays a trace of the algorithm.<br>
     * Note: the algorithm used is the one specified in the algoFunction.
     * Use setDefaultAlgoFunction to choose the algorithm.
     * @param gridGraph the grid to operate on.
     */
    private static void displayAlgorithmOperation(GridGraph gridGraph, StartGoalPoints p) {
        GridLineSet gridLineSet = new GridLineSet();
        
        int[][] path = Utility.generatePath(gridGraph, p.sx, p.sy, p.ex, p.ey);
        
        for (int i=0; i<path.length-1; i++) {
            gridLineSet.addLine(path[i][0], path[i][1],
                    path[i+1][0], path[i+1][1], Color.BLUE);
        }
        float pathLength = Utility.computePathLength(gridGraph, path);
        System.out.println("Path Length: " + pathLength);
    
        System.out.println(Arrays.deepToString(path));
    
        LinkedList<GridObjects> lineSetList = recordAlgorithmOperation(gridGraph, p.sx, p.sy, p.ex, p.ey);
        lineSetList.addFirst(new GridObjects(gridLineSet, null));
        DrawCanvas drawCanvas = new DrawCanvas(gridGraph, gridLineSet);
        drawCanvas.setStartAndEnd(p.sx, p.sy, p.ex, p.ey);
        
        setupMainFrame(drawCanvas, lineSetList);
    }

    /**
     * Records a trace of the current algorithm into a LinkedList of GridObjects.
     */
    private static LinkedList<GridObjects> recordAlgorithmOperation (
            GridGraph gridGraph, int sx, int sy, int ex, int ey) {
        PathFindingAlgorithm algo = AnyAnglePathfinding.algoFunction.getAlgo(gridGraph, sx, sy, ex, ey);
        algo.startRecording();
        try {
            algo.computePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        algo.stopRecording();
        algo.printStatistics();
        LinkedList<List<SnapshotItem>> snapshotList = algo.retrieveSnapshotList();
        LinkedList<GridObjects> gridObjectsList = new LinkedList<>();
        for (List<SnapshotItem> snapshot : snapshotList) {
            gridObjectsList.add(GridObjects.create(snapshot));
        }
        return gridObjectsList;
    }

    /**
     * Spawns the visualisation window for the algorithm.
     */
    private static void setupMainFrame(DrawCanvas drawCanvas, LinkedList<GridObjects> gridObjectsList) {
        KeyToggler keyToggler = new KeyToggler(drawCanvas, gridObjectsList);
        
        JFrame mainFrame = new JFrame();
        mainFrame.add(drawCanvas);
        mainFrame.addKeyListener(keyToggler);
        mainFrame.addWindowListener(new CloseOnExitWindowListener());
        mainFrame.setResizable(false);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
}
