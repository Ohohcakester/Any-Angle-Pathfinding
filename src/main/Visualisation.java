package main;

import grid.GridAndGoals;
import grid.GridGraph;
import grid.StartGoalPoints;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import main.utility.TimeCounter;
import main.utility.Utility;
import uiandio.CloseOnExitWindowListener;
import algorithms.PathFindingAlgorithm;
import algorithms.datatypes.SnapshotItem;
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
        AlgoFunction algo = AnyAnglePathfinding.setDefaultAlgoFunction();           // choose an algorithm (go into this method to choose)
        GridAndGoals gridAndGoals = AnyAnglePathfinding.loadMaze();   // choose a grid (go into this method to choose)
        
        gridAndGoals.validateStartAndEndPoints();
        
        // Call this to record and display the algorithm in operation.
        displayAlgorithmOperation(algo, gridAndGoals.gridGraph, gridAndGoals.startGoalPoints);
    }
    
    /**
     * Records the algorithm, the final path computed, and displays a trace of the algorithm.<br>
     * Note: the algorithm used is the one specified in the algoFunction.
     * Use setDefaultAlgoFunction to choose the algorithm.
     * @param gridGraph the grid to operate on.
     */
    private static void displayAlgorithmOperation(AlgoFunction algo, GridGraph gridGraph, StartGoalPoints p) {
        GridLineSet gridLineSet = new GridLineSet();
        try {
            {Random r = new Random(2);
            //for (int i=0;i<1000;++i)Utility.generatePath(algo, gridGraph, r.nextInt(gridGraph.sizeX), r.nextInt(gridGraph.sizeY), r.nextInt(gridGraph.sizeX), r.nextInt(gridGraph.sizeY));
            TimeCounter.reset();}
            
            long startT = System.nanoTime();
            int[][] path = Utility.generatePath(algo, gridGraph, p.sx, p.sy, p.ex, p.ey);
            long endT = System.nanoTime();
            TimeCounter.print();
            System.out.println("Runtime: " + ((endT-startT)/1000000.));
            for (int i=0; i<path.length-1; i++) {
                gridLineSet.addLine(path[i][0], path[i][1],
                        path[i+1][0], path[i+1][1], Color.BLUE);
            }
            double pathLength = Utility.computePathLength(gridGraph, path);
            System.out.println("Path Length: " + pathLength);
            
            boolean isTaut = Utility.isPathTaut(gridGraph, path);
            System.out.println("Is Taut: " + (isTaut ? "YES" : "NO"));
        
            //System.out.println(Arrays.deepToString(path));
        } catch (Exception e) {
            System.out.println("Exception occurred during algorithm operation!");
            e.printStackTrace();
        }
        
        ArrayList<GridObjects> lineSetList = recordAlgorithmOperation(algo, gridGraph, p.sx, p.sy, p.ex, p.ey);
        lineSetList.add(new GridObjects(gridLineSet, null));
        DrawCanvas drawCanvas = new DrawCanvas(gridGraph, gridLineSet);
        drawCanvas.setStartAndEnd(p.sx, p.sy, p.ex, p.ey);
        
        setupMainFrame(drawCanvas, lineSetList);
    }

    /**
     * Records a trace of the current algorithm into a LinkedList of GridObjects.
     */
    private static ArrayList<GridObjects> recordAlgorithmOperation (AlgoFunction algoFunction, 
            GridGraph gridGraph, int sx, int sy, int ex, int ey) {
        PathFindingAlgorithm algo = algoFunction.getAlgo(gridGraph, sx, sy, ex, ey);
        algo.startRecording();
        try {
            algo.computePath();
        } catch (Exception e) {
            System.out.println("Exception occurred during algorithm operation!");
            e.printStackTrace();
        }
        algo.stopRecording();
        algo.printStatistics();
        ArrayList<List<SnapshotItem>> snapshotList = algo.retrieveSnapshotList();
        ArrayList<GridObjects> gridObjectsList = new ArrayList<>();
        for (List<SnapshotItem> snapshot : snapshotList) {
            gridObjectsList.add(GridObjects.create(snapshot));
        }
        return gridObjectsList;
    }

    /**
     * Spawns the visualisation window for the algorithm.
     */
    protected static void setupMainFrame(DrawCanvas drawCanvas, ArrayList<GridObjects> gridObjectsList) {
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
