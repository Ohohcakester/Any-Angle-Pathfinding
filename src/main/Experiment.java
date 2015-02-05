package main;

import grid.GridGraph;
import grid.ReachableNodes;
import grid.StartGoalPoints;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import main.graphgeneration.DefaultGenerator;
import uiandio.FileIO;
import algorithms.BreadthFirstSearch;
import algorithms.datatypes.Point;
import algorithms.visibilitygraph.VisibilityGraph;
import draw.GridLineSet;

public class Experiment {
    
    public static void run() {
        testVisibilityGraphSize();
//        testAbilityToFindGoal();
//        other();
    }
    
    /**
     * Custom code for experiments.
     */
    public static void other() {
        // This is how to generate test data for the grid. (Use the VisibilityGraph algorithm to generate optimal path lengths)
//      ArrayList<Point> points = ReachableNodes.computeReachable(gridGraph, 5, 5);
//      System.out.println(points.size());
//
//      generateRandomTestDataAndPrint(gridGraph);

      //This is how to conduct a running time / path length test for tha algorithm:
//      TestResult test1 = testAlgorithm(gridGraph, sx, sy, ex, ey, 1, 1);
//      System.out.println(test1);
//      TestResult test2 = testAlgorithm(gridGraph, sx, sy, ex, ey, 30, 25);
//      System.out.println(test2);
    }

    /**
     * Generates and prints out random test data for the gridGraph in question. <br>
     * Note: the algorithm used is the one specified in the algoFunction.
     * Use setDefaultAlgoFunction to choose the algorithm.
     * @param gridGraph the grid to test.
     */
    private static void generateRandomTestDataAndPrint(GridGraph gridGraph) {
        ArrayList<Point> points = ReachableNodes.computeReachable(gridGraph, 5, 5);
    
        LinkedList<Integer> startX = new LinkedList<>();
        LinkedList<Integer> startY = new LinkedList<>();
        LinkedList<Integer> endX = new LinkedList<>();
        LinkedList<Integer> endY = new LinkedList<>();
        LinkedList<Float> length = new LinkedList<>();
        
        int size = points.size();
        System.out.println("Points: " + size);
        
        for (int i=0; i<100; i++) {
            Random random = new Random();
            int first = random.nextInt(size);
            int last = random.nextInt(size-1);
            if (last == first) last = size-1; // prevent first and last from being the same
    
            Point s = points.get(first);
            Point f = points.get(last);
            int[][] path = Utility.generatePath(gridGraph, s.x, s.y, f.x, f.y);
            if (path.length >= 2) {
                float len = Utility.computePathLength(gridGraph, path);
                startX.offer(s.x);
                startY.offer(s.y);
                endX.offer(f.x);
                endY.offer(f.y);
                length.offer(len);
            }
            if (i%10 == 0) System.out.println("Computed: " + i);
        }
        System.out.println(startX);
        System.out.println(startY);
        System.out.println(endX);
        System.out.println(endY);
        System.out.println(length);
    }

    /**
     * Returns true iff there is a path from the start to the end. Uses the current algorithm to check.<br>
     * Note: the algorithm used is the one specified in the algoFunction.
     * Use setDefaultAlgoFunction to choose the algorithm.
     */
    private static boolean hasSolution(GridGraph gridGraph, StartGoalPoints p) {
        int[][] path = Utility.generatePath(gridGraph, p.sx, p.sy, p.ex, p.ey);
        return path.length > 1;
    }

    /**
     * Generates random lines on the map. Used to test whether the line-of-sight
     * algorithm is correct. Returns a gridLineSet containing all the test lines.
     */
    private static GridLineSet generateRandomTestLines(GridGraph gridGraph,
            int amount) {
        GridLineSet gridLineSet = new GridLineSet();
        
        Random rand = new Random();
        for (int i=0; i<amount; i++) {
            int x1 = rand.nextInt(gridGraph.sizeX);
            int y1 = rand.nextInt(gridGraph.sizeY);
            int x2 = rand.nextInt(gridGraph.sizeX);
            int y2 = rand.nextInt(gridGraph.sizeY);
    
            Experiment.testAndAddLine(x1,y1,x2,y2,gridGraph,gridLineSet);
        }
        
        return gridLineSet;
    }

    /**
     * Tests a set of coordinates for line-of-sight. Adds a green line to the 
     * gridLineSet if there is line-of-sight between (x1,y1) and (x2,y2).
     * Adds a red line otherwise.
     */
    private static void testAndAddLine(int x1, int y1, int x2, int y2,
            GridGraph gridGraph, GridLineSet gridLineSet) {
        
        if (gridGraph.lineOfSight(x1, y1, x2, y2)) {
            gridLineSet.addLine(x1, y1, x2, y2, Color.GREEN);
        } else {
            gridLineSet.addLine(x1, y1, x2, y2, Color.RED);
        }
    }

    private static void testAbilityToFindGoal() {
        AnyAnglePathfinding.setDefaultAlgoFunction();
        
        AnyAnglePathfinding.AlgoFunction currentAlgo = AnyAnglePathfinding.algoFunction;
        AnyAnglePathfinding.AlgoFunction bfs = (gridGraph, sx, sy, ex, ey) -> new BreadthFirstSearch(gridGraph, sx, sy, ex, ey);
    
        Random seedRand = new Random(2);
        int initial = seedRand.nextInt();
        for (int i=0; i<500000; i++) {
            int sizeX = seedRand.nextInt(70) + 10;
            int sizeY = seedRand.nextInt(70) + 10;
            int seed = i+initial;
            int ratio = seedRand.nextInt(40) + 5;
            
            int max = (sizeX+1)*(sizeY+1);
            int p1 = seedRand.nextInt(max);
            int p2 = seedRand.nextInt(max-1);
            if (p2 == p1) {
                p2 = max-1;
            }
            
            int sx = p1%(sizeX+1);
            int sy = p1/(sizeX+1);
            int ex = p2%(sizeX+1);
            int ey = p2/(sizeX+1);
    
            GridGraph gridGraph = DefaultGenerator.generateSeededGraphOnly(seed, sizeX, sizeY, ratio);
            AnyAnglePathfinding.algoFunction = bfs;
            int[][] path = Utility.generatePath(gridGraph, sx, sy, ex, ey);
            float pathLength = Utility.computePathLength(gridGraph, path);
            boolean bfsValid = (pathLength > 0.00001f);
    
            AnyAnglePathfinding.algoFunction = currentAlgo;
            path = Utility.generatePath(gridGraph, sx, sy, ex, ey);
            pathLength = Utility.computePathLength(gridGraph, path);
            boolean algoValid = (pathLength > 0.00001f);
            
            if (bfsValid != algoValid) {
                System.out.println("============");
                System.out.println("Discrepancy Discovered!");
                System.out.println("Seed = " + seed +" , Ratio = " + ratio + " , Size: x=" + sizeX + " y=" + sizeY);
                System.out.println("Start = " + sx + "," + sy + "  End = " + ex + "," + ey);
                System.out.println("BFSValid: " + bfsValid + " , AlgoValid: " + algoValid);
                System.out.println("============");
                throw new UnsupportedOperationException("DISCREPANCY!!");
            } else {
                System.out.println("OK: Seed = " + seed +" , Ratio = " + ratio + " , Size: x=" + sizeX + " y=" + sizeY);
            }
        }
        
    }

    /**
     * Tests random generated maps of various sizes and obstacle densities for
     * the size of the visibility graphs.<br>
     * The results are output to the file VisibilityGraphSizes.txt.
     */
    private static void testVisibilityGraphSize() {
        FileIO fileIO = new FileIO(AnyAnglePathfinding.PATH_TESTDATA_NAME + "VisibilityGraphSizes.txt");
        Random seedGenerator = new Random();
        
        fileIO.writeLine("Seed", "Size", "%Blocked", "Vertices", "Edges (Directed)");
        for (int i=0; i<30; i++) {
            for (int r=0; r<3; r++) {
                int currentRatio = (r == 0 ? 7 : (r == 1 ? 15 : 50));
                        
                int currentSize = 10 + i*10;
                int currentSeed = seedGenerator.nextInt();
                
                GridGraph gridGraph = DefaultGenerator.generateSeededGraphOnly(currentSeed, currentSize, currentSize, currentRatio, 0, 0, currentSize, currentSize);
                VisibilityGraph vGraph = new VisibilityGraph(gridGraph, 0, 0, currentSize, currentSize);
                vGraph.initialise();
                
                String seedString = currentSeed + "";
                String sizeString = currentSize + "";
                String ratioString = gridGraph.getPercentageBlocked()*100f + "";
                String verticesString = vGraph.size() + "";
                String edgesString = vGraph.computeSumDegrees() + "";
                
                fileIO.writeLine(seedString, sizeString, ratioString, verticesString, edgesString);
                fileIO.flush();
            }
        }
        fileIO.close();
    }

}
