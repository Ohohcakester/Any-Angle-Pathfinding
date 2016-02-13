package main;

import grid.GridGraph;

import java.util.ArrayList;

import javax.swing.JFrame;

import main.analysis.MazeAnalysis;
import uiandio.CloseOnExitWindowListener;
import uiandio.GraphImporter;
import algorithms.datatypes.Point;
import draw.EditorUI;
import draw.VisualiserKeyboardControls;
import draw.VisualiserMouseControls;

public class GridGraphVisualiser {

    public static void run() {
        //loadMaze("custommaze2.txt", "custom");
        loadExisting("sc2_losttemple");
        //loadExisting("baldursgate_AR0402SR");
        //loadExisting("corr2_maze512-2-5");
    }
    
    
    public static void loadMaze(String mazeFileName, String classification) {
        GridGraph gridGraph = GraphImporter.importGraphFromFile(mazeFileName);
        
        int dot = mazeFileName.lastIndexOf('.');
        String mazeName = classification + "_" + mazeFileName.substring(0, dot);
        
        setupMainFrame(gridGraph, mazeName);
    }

    
    public static void loadExisting(String mazeName) {
        GridGraph gridGraph = GraphImporter.loadStoredMaze(mazeName);
        setupMainFrame(gridGraph, mazeName);
    }
    

    /**
     * Spawns the editor visualisation window.
     */
    private static void setupMainFrame(GridGraph gridGraph, String mazeName) {
        ArrayList<ArrayList<Point>> connectedSets = MazeAnalysis.findConnectedSets(gridGraph);
        
        EditorUI editorUI = new EditorUI(gridGraph, connectedSets, mazeName);
        VisualiserMouseControls mouseControls =
                new VisualiserMouseControls(gridGraph, editorUI);
        VisualiserKeyboardControls keyboardControls =
                new VisualiserKeyboardControls(editorUI);
        
        JFrame mainFrame = new JFrame();
        mainFrame.setTitle(mazeName);
        mainFrame.add(editorUI);
        mainFrame.addWindowListener(new CloseOnExitWindowListener());
        mainFrame.getContentPane().addMouseListener(mouseControls);
        mainFrame.addKeyListener(keyboardControls);
        mainFrame.setResizable(false);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
}
