package main;

import grid.GridAndGoals;
import grid.GridGraph;
import grid.StartGoalPoints;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import uiandio.GraphImporter;
import algorithms.datatypes.SnapshotItem;
import draw.DrawCanvas;
import draw.GridLineSet;
import draw.GridObjects;

public class TextOutputVisualisation {

    public static void run() {
        loadDefault();
        //loadFromFile("anyacont2b.txt");
    }

    private static void loadFromFile(String mazeFileName) {
        String textData = readStandardInput();

        GridGraph gridGraph = GraphImporter.importGraphFromFile(mazeFileName);
        StartGoalPoints p = new StartGoalPoints(0,0,0,0);
        displayTextVisualisation(gridGraph, p, textData);
    }

    private static void loadDefault() {
        String textData = readStandardInput();

        GridAndGoals gridAndGoals = AnyAnglePathfinding.loadMaze();
        GridGraph gridGraph = gridAndGoals.gridGraph;
        StartGoalPoints p = gridAndGoals.startGoalPoints;
        displayTextVisualisation(gridGraph, p, textData);
    }
    
    private static String readStandardInput() {
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        String line = sc.nextLine();
        while (!"#".equals(line)) {
            sb.append(line);
            sb.append("\n");
            line = sc.nextLine();
        }
        return sb.toString();
    }

    private static void displayTextVisualisation(GridGraph gridGraph, StartGoalPoints p, String textData) {
        GridLineSet gridLineSet = new GridLineSet();
        
        String[] args = textData.split("\n");
        Integer[][] paths = new Integer[args.length][];
        for (int i=0;i<args.length;++i) {
            String[] tokens = args[i].split(" ");
            Integer[] path = new Integer[tokens.length];
            for (int j=0;j<tokens.length;++j) {
                path[j] = Integer.parseInt(tokens[j]);
            }
            paths[i] = path;
        }

        ArrayList<GridObjects> lineSetList = new ArrayList<>();
        for (int i=0;i<args.length;++i) {
            List<SnapshotItem> snapshot = new ArrayList<>();
            for (int j=0;j<=i;++j) {
                Color col = Color.red;
                if (j == i) col = Color.green;
                
                SnapshotItem e = SnapshotItem.generate(paths[j], col);
                snapshot.add(e);
            }
            
            lineSetList.add(GridObjects.create(snapshot));
        }
        
        lineSetList.add(new GridObjects(gridLineSet, null));
        DrawCanvas drawCanvas = new DrawCanvas(gridGraph, gridLineSet);
        drawCanvas.setStartAndEnd(p.sx, p.sy, p.ex, p.ey);
        
        Visualisation.setupMainFrame(drawCanvas, lineSetList);
    }
}
