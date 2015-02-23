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
        loadMaze("maze14x11.txt");
    }
    
    public static void loadMaze(String mazeName) {
        GridGraph gridGraph = GraphImporter.importGraphFromFile(mazeName);
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
