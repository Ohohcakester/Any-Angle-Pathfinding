package main.analysis;

import grid.GridGraph;

import java.util.ArrayList;
import java.util.Random;

import algorithms.datatypes.Point;

public class MazeAnalyser {
    public final MazeAnalysis mazeAnalysis;
    public final ArrayList<ProblemAnalysis> problemList;

    public MazeAnalyser(GridGraph gridGraph, int nProblems) {
        System.out.println("-Starting Maze Analysis: " +gridGraph.sizeX+"x"+gridGraph.sizeY);
        mazeAnalysis = new MazeAnalysis(gridGraph);

        System.out.println("-Starting Problem Generation");
        ArrayList<TwoPoint> twoPointList = generateProblems(mazeAnalysis, nProblems);

        problemList = conductProblemAnalysis(gridGraph, twoPointList);
        
        System.out.println();
        System.out.println("-Analysis Complete");
    }

    public MazeAnalyser(GridGraph gridGraph, ArrayList<TwoPoint> twoPointList, boolean analyseMaze) {
        if (analyseMaze) {
            System.out.println("-Starting Maze Analysis: " +gridGraph.sizeX+"x"+gridGraph.sizeY);
            mazeAnalysis = new MazeAnalysis(gridGraph);
        } else {
            mazeAnalysis = null;
        }
        
        problemList = conductProblemAnalysis(gridGraph, twoPointList);
        
        System.out.println();
        System.out.println("-Analysis Complete");
    }

    protected ArrayList<ProblemAnalysis> conductProblemAnalysis(GridGraph gridGraph, ArrayList<TwoPoint> twoPointList) {
        System.out.println("-Starting Problem Analysis");
        ArrayList<ProblemAnalysis> list = new ArrayList<>();
        for (TwoPoint tp : twoPointList) {
            ProblemAnalysis problem = new ProblemAnalysis(gridGraph,
                    tp.p1.x, tp.p1.y, tp.p2.x, tp.p2.y);
            list.add(problem);
            System.out.print(tp + " | ");
        }
        return list;
    }
    

    private ArrayList<TwoPoint> generateProblems(MazeAnalysis mazeAnalysis,
            int nProblems) {
        Random rand = new Random();
        ArrayList<TwoPoint> problemList = new ArrayList<>();
        
        int chances = nProblems; // prevent infinite loop        
        for (int i=0; i<nProblems; i++) {
            TwoPoint tp = generateProblem(rand, mazeAnalysis);
            if (problemList.contains(tp)) {
                if (chances > 0) {
                    chances--;
                    i--;
                }
            } else {
                problemList.add(tp);
            }
        }
        
        return problemList;
    }
    
    private TwoPoint generateProblem(Random rand, MazeAnalysis mazeAnalysis) {
        int nTraversableNodes = 0;
        for (ArrayList<Point> list : mazeAnalysis.connectedSets) {
            nTraversableNodes += list.size();
        }
        int index = rand.nextInt(nTraversableNodes);
        int listIndex = 0;
        for (int i=0; i< mazeAnalysis.connectedSets.size(); i++) {
            int size = mazeAnalysis.connectedSets.get(i).size();
            if (index >= size) {
                index -= size;
            } else {
                listIndex = i;
                break;
            }
        }
        
        ArrayList<Point> list = mazeAnalysis.connectedSets.get(listIndex);
        int index2 = rand.nextInt(list.size()-1);
        if (index2 == index) {
            index2 = list.size()-1;
        }
        
        Point p1 = list.get(index);
        Point p2 = list.get(index2);
        return new TwoPoint(p1, p2);
    }
}