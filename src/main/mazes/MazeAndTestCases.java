package main.mazes;

import java.util.ArrayList;

import grid.GridGraph;
import main.testgen.StartEndPointData;

public class MazeAndTestCases {
    public final String mazeName;
    public final GridGraph gridGraph;
    public final ArrayList<StartEndPointData> problems;
    
    public MazeAndTestCases(String mazeName, GridGraph gridGraph, ArrayList<StartEndPointData> problems) {
        this.mazeName = mazeName;
        this.gridGraph = gridGraph;
        this.problems = problems;
    }
}
