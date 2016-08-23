package main;

import grid.GridAndGoals;
import grid.GridGraph;
import grid.StartGoalPoints;

import java.util.ArrayList;

import javax.swing.JFrame;

import main.analysis.MazeAnalysis;
import main.mazes.MazeAndTestCases;
import main.mazes.StoredTestMazes;
import uiandio.CloseOnExitWindowListener;
import uiandio.GraphImporter;
import algorithms.datatypes.Point;
import draw.EditorUI;
import draw.VisualiserKeyboardControls;
import draw.VisualiserMouseControls;

public class GridGraphVisualiser {

    public static void run() {
        //loadExisting("sc1_EbonLakes");
        //loadExisting("sc1_GreenerPastures");
        loadDefault("default");
        //loadMaze("custommaze2.txt", "custom");
        //loadMaze("lineOfSightTest.txt", "custom");
        //loadExisting("sc2_blisteringsands");
        //loadExisting("sc2_losttemple");
        //loadExisting("baldursgate_AR0402SR");
        //loadExisting("corr2_maze512-2-5");
    }
    
    
    public static void loadMaze(String mazeFileName, String classification) {
        GridGraph gridGraph = GraphImporter.importGraphFromFile(mazeFileName);
        
        int dot = mazeFileName.lastIndexOf('.');
        String mazeName = classification + "_" + mazeFileName.substring(0, dot);
        
        setupMainFrame(gridGraph, mazeName, null);
    }

    
    public static void loadExisting(String mazeName) {
        GridGraph gridGraph = GraphImporter.loadStoredMaze(mazeName);
        setupMainFrame(gridGraph, mazeName, null);
    }


    private static void loadStored(MazeAndTestCases mazeAndTestCases) {
        GridGraph gridGraph = mazeAndTestCases.gridGraph;
        String mazeName = mazeAndTestCases.mazeName;
        
        setupMainFrame(gridGraph, mazeName, null);
    }
    
    public static void loadDefault(String mazeName) {
        GridAndGoals gridAndGoals = AnyAnglePathfinding.loadMaze();
        GridGraph gridGraph = gridAndGoals.gridGraph;
        StartGoalPoints startGoalPoints = gridAndGoals.startGoalPoints;
        setupMainFrame(gridGraph, mazeName, startGoalPoints);
    }
    

    /**
     * Spawns the editor visualisation window.
     * @param startGoalPoints 
     */
    private static void setupMainFrame(GridGraph gridGraph, String mazeName, StartGoalPoints startGoalPoints) {
        AlgoFunction algoFunction = AnyAnglePathfinding.setDefaultAlgoFunction();
        ArrayList<ArrayList<Point>> connectedSets = MazeAnalysis.findConnectedSetsFast(gridGraph);
        
        EditorUI editorUI = new EditorUI(gridGraph, algoFunction, connectedSets, mazeName, startGoalPoints);
        VisualiserMouseControls mouseControls =
                new VisualiserMouseControls(gridGraph, editorUI);
        VisualiserKeyboardControls keyboardControls =
                new VisualiserKeyboardControls(editorUI);
        
        JFrame mainFrame = new JFrame();
        mainFrame.setTitle(mazeName);
        mainFrame.add(editorUI);
        mainFrame.addWindowListener(new CloseOnExitWindowListener());
        mainFrame.getContentPane().addMouseListener(mouseControls);
        mainFrame.getContentPane().addMouseMotionListener(mouseControls);
        mainFrame.addKeyListener(keyboardControls);
        mainFrame.setResizable(false);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
}
