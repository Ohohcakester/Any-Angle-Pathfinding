package main.analysis;

import grid.GridGraph;

import java.util.ArrayList;
import java.util.Random;

import algorithms.datatypes.Point;

public class MazeAnalyser {
    public final MazeAnalysis mazeAnalysis;
    public final ArrayList<ProblemAnalysis> problemList;

    public MazeAnalyser(GridGraph gridGraph, int nProblems) {
        System.out.println("-Starting Maze Analysis");
        mazeAnalysis = new MazeAnalysis(gridGraph);

        System.out.println("-Starting Problem Generation");
        ArrayList<TwoPoint> twoPointList = generateProblems(mazeAnalysis, nProblems);
        
        System.out.println("-Starting Problem Analysis");
        problemList = new ArrayList<>();
        for (TwoPoint tp : twoPointList) {
            ProblemAnalysis problem = new ProblemAnalysis(gridGraph,
                    tp.p1.x, tp.p1.y, tp.p2.x, tp.p2.y);
            problemList.add(problem);
            System.out.print(tp + " | ");
        }
        System.out.println();
        System.out.println("-Analysis Complete");
    }
    
    private class TwoPoint {
        public final Point p1, p2;
        public TwoPoint (Point p1, Point p2) {
            this.p1 = p1; this.p2 = p2;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TwoPoint)) return false;
            TwoPoint other = (TwoPoint)obj;
            if (p1.equals(other.p1) && p2.equals(other.p2)) {
                return true;
            }
            if (p1.equals(other.p2) && p2.equals(other.p1)) {
                return true;
            }
            return false;
        }
        
        @Override
        public String toString() {
            return p1.x + " " + p1.y + " " + p2.x + " " + p2.y;
        }
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