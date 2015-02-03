package main;
import grid.GridAndGoals;
import grid.GridGraph;
import grid.ReachableNodes;
import grid.StartGoalPoints;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import main.graphgeneration.DefaultGenerator;
import main.graphgeneration.GraphInfo;
import uiandio.CloseOnExitWindowListener;
import uiandio.FileIO;
import uiandio.GraphImporter;
import algorithms.AStar;
import algorithms.AcceleratedAStar;
import algorithms.Anya;
import algorithms.BasicThetaStar;
import algorithms.BreadthFirstSearch;
import algorithms.PathFindingAlgorithm;
import algorithms.VisibilityGraphAlgorithm;
import algorithms.anya.Fraction;
import algorithms.datatypes.Point;
import algorithms.datatypes.SnapshotItem;
import algorithms.visibilitygraph.VisibilityGraph;
import draw.DrawCanvas;
import draw.GridLineSet;
import draw.GridObjects;
import draw.GridPointSet;
import draw.KeyToggler;

/**
 * Instructions: Look for the main method.
 * We can either run tests or trace the algorithm.
 * 
 * When tracing algorithms,
 * Choose the maze in loadMaze();
 * Choose the algorithm in setDefaultAlgoFunction();
 * 
 * The tracing / experimentation functions are detailed in the traceAlgorithm() method.
 */
public class AnyAnglePathfinding {    
    private static String PATH_TESTDATA_NAME = "testdata/";


    private static AlgoFunction algoFunction; // The algorithm is stored in this function.

    
    public static void main(String[] args) { // uncomment the one you need to use.
//        runTestAllAlgos();
//        testVisibilityGraphSize();
//        testAbilityToFindGoal();
        traceAlgorithm();
    }

    /**
     * Conducts a trace of the current algorithm
     */
    private static void traceAlgorithm() {
        setDefaultAlgoFunction();           // choose an algorithm (go into this method to choose)
        GridAndGoals gridAndGoals = loadMaze();   // choose a grid (go into this method to choose)

        // This is how to generate test data for the grid. (Use the VisibilityGraph algorithm to generate optimal path lengths)
//        ArrayList<Point> points = ReachableNodes.computeReachable(gridGraph, 5, 5);
//        System.out.println(points.size());
//
//        generateRandomTestDataAndPrint(gridGraph);

        //This is how to conduct a running time / path length test for tha algorithm:
//        TestResult test1 = testAlgorithm(gridGraph, sx, sy, ex, ey, 1, 1);
//        System.out.println(test1);
//        TestResult test2 = testAlgorithm(gridGraph, sx, sy, ex, ey, 30, 25);
//        System.out.println(test2);
        
        // Call this to record and display the algorithm in operation.
        displayAlgorithmOperation(gridAndGoals.gridGraph, gridAndGoals.startGoalPoints);
    }

    /**
     * Choose a maze. (a gridGraph setting)
     */
    private static GridAndGoals loadMaze() {
        int choice = 1; // Adjust this to choose a maze.
        
        switch(choice) {
            case 0 : {// UNSEEDED
                int unblockedRatio = 9;      // chance of spawning a cluster of blocked tiles is 1 in unblockedRatio.
                int sizeX = 20;               // x-axis size of grid
                int sizeY = 20;               // y-axis size of grid

                int sx = 2;                   // x-coordinate of start point
                int sy = 14;                  // y-coordinate of start point
                int ex = 13;                  // x-coordinate of goal point
                int ey = 4;                   // y-coordinate of goal point
                return DefaultGenerator.generateUnseeded(sizeX, sizeY, unblockedRatio, sx, sy, ex, ey);
            }
            case 1 : { // SEEDED
                int unblockedRatio = 9;      // chance of spawning a cluster of blocked tiles is 1 in unblockedRatio.
                int seed = 567069235;        // seed for the random.
                
                int sizeX = 20;              // x-axis size of grid
                int sizeY = 20;              // y-axis size of grid

                int sx = 2;                  // x-coordinate of start point
                int sy = 14;                 // y-coordinate of start point
                int ex = 13;                 // x-coordinate of goal point
                int ey = 4;                  // y-coordinate of goal point
                return DefaultGenerator.generateSeeded(seed, sizeX, sizeY, unblockedRatio, sx, sy, ex, ey);
            }
            case 2 :
                return importGraphFromFile("maze.txt", 25, 17, 2, 9);
            case 3 :
                return DefaultGenerator.generateSeeded(-98783479, 40, 40, 7, 1, 4, 18, 18); // maze 3
            case 4 :
                return DefaultGenerator.generateSeeded(-565315494, 15, 15, 9, 1, 2, 1, 13); // maze 2
            case 5 :
                return DefaultGenerator.generateSeeded(53, 15, 15, 9, 0, 0, 10, 14); // maze 1
            case 6 :
                return DefaultGenerator.generateSeeded(-159182402, 15, 15, 9, 1, 1, 13, 12); // anya previously gave incorrect path
            case 7 :
                return importGraphFromFile("maze14x11.txt", 0, 0, 10, 10); // Maze to contradict Theta* / A*
            case 8 :
                return importGraphFromFile("mazeWCS.txt", 2, 0, 28, 25); // Worst Case Scenario path length.
            case 9 :
                return DefaultGenerator.generateSeeded(-410889275, 15, 15, 7, 0, 1, 10, 12); // maze 4
            case 10 :
                return importGraphFromFile("mazeThetaWCS.txt", 0, 0, 28, 13); // Worst Case Scenario for Theta*
            case 11 :
                return importGraphFromFile("mazeReuseWCS.txt", 1, 28, 0, 27); // Worst Case Scenario for Visibility Graph reuse.
            case 12 :
                return importGraphFromFile("anyaCont2.txt", 1, 6, 9, 1); // anya gives incorrect path
            case 13 :
                return DefaultGenerator.generateSeeded(-524446332, 20, 20, 10, 2, 19, 17, 2); // anya gives incorrect path.
            case 14 :
                return DefaultGenerator.generateSeeded(-1155797147, 47, 32, 38, 46, 30, 20, 1); // issue for Strict Theta*
            default :
                return null;
        }
    }
    
    /**
     * Choose an algorithm.
     */
    private static void setDefaultAlgoFunction() {
        int choice = 8; // adjust this to choose an algorithm
        
        switch (choice) {
            case 1 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new AStar(gridGraph, sx, sy, ex, ey);
                break;
            case 2 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new BreadthFirstSearch(gridGraph, sx, sy, ex, ey);
                break;
            case 3 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> BreadthFirstSearch.postSmooth(gridGraph, sx, sy, ex, ey);
                break;
            case 4 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> AStar.postSmooth(gridGraph, sx, sy, ex, ey);
                break;
            case 5 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> AStar.dijkstra(gridGraph, sx, sy, ex, ey);
                break;
            case 6 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new Anya(gridGraph, sx, sy, ex, ey);
                break;
            case 7 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new VisibilityGraphAlgorithm(gridGraph, sx, sy, ex, ey);
                break;
            case 8 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new BasicThetaStar(gridGraph, sx, sy, ex, ey);
                break;
            case 9 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> BasicThetaStar.noHeuristic(gridGraph, sx, sy, ex, ey);
                break;
            case 10 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> BasicThetaStar.postSmooth(gridGraph, sx, sy, ex, ey);
                break;
            case 11 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new AcceleratedAStar(gridGraph, sx, sy, ex, ey);
                break;
            case 12 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> VisibilityGraphAlgorithm.graphReuse(gridGraph, sx, sy, ex, ey);
                break;
            case 13 :
                algoFunction = null; // reserved
                //algoFunction = (gridGraph, sx, sy, ex, ey) -> new AdjustmentThetaStar(gridGraph, sx, sy, ex, ey);
                break;
            case 14 :
                algoFunction = null; // reserved
                //algoFunction = (gridGraph, sx, sy, ex, ey) -> new StrictThetaStar(gridGraph, sx, sy, ex, ey);
                break;
            case 15 :
                algoFunction = null; // reserved
                //algoFunction = (gridGraph, sx, sy, ex, ey) -> StrictThetaStar.noHeuristic(gridGraph, sx, sy, ex, ey);
                break;
        }
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
            int[][] path = generatePath(gridGraph, s.x, s.y, f.x, f.y);
            if (path.length >= 2) {
                float len = computePathLength(gridGraph, path);
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
        int[][] path = generatePath(gridGraph, p.sx, p.sy, p.ex, p.ey);
        return path.length > 1;
    }

    /**
     * Records the algorithm, the final path computed, and displays a trace of the algorithm.<br>
     * Note: the algorithm used is the one specified in the algoFunction.
     * Use setDefaultAlgoFunction to choose the algorithm.
     * @param gridGraph the grid to operate on.
     */
    private static void displayAlgorithmOperation(GridGraph gridGraph, StartGoalPoints p) {
        GridLineSet gridLineSet = new GridLineSet();
        
        int[][] path = generatePath(gridGraph, p.sx, p.sy, p.ex, p.ey);
        
        for (int i=0; i<path.length-1; i++) {
            gridLineSet.addLine(path[i][0], path[i][1],
                    path[i+1][0], path[i+1][1], Color.BLUE);
        }
        float pathLength = computePathLength(gridGraph, path);
        System.out.println("Path Length: " + pathLength);

        System.out.println(Arrays.deepToString(path));

        LinkedList<GridObjects> lineSetList = recordAlgorithmOperation(gridGraph, p.sx, p.sy, p.ex, p.ey);
        lineSetList.addFirst(new GridObjects(gridLineSet, null));
        DrawCanvas drawCanvas = new DrawCanvas(gridGraph, gridLineSet);
        drawCanvas.setStartAndEnd(p.sx, p.sy, p.ex, p.ey);
        
        setupMainFrame(drawCanvas, lineSetList);
    }

    /**
     * Compute the length of a given path. (Using euclidean distance)
     */
    private static float computePathLength(GridGraph gridGraph, int[][] path) {
        float pathLength = 0;
        for (int i=0; i<path.length-1; i++) {
            pathLength += gridGraph.distance(path[i][0], path[i][1],
                            path[i+1][0], path[i+1][1]);
        }
        return pathLength;
    }
    
    /**
     * Import a graph from a file in the AnyAnglePathFinding directory,
     * and also set the start and goal points.
     */
    private static GridAndGoals importGraphFromFile(String filename, int sx, int sy, int ex, int ey) {
    	GridGraph gridGraph = importGraphFromFile(filename);
        
    	return new GridAndGoals(gridGraph, sx, sy, ex, ey);
    }

    /**
     * Import a graph from a file in the AnyAnglePathFinding directory.
     * Look into the GraphImporter documentation for details on how to create a grid file.
     */
    private static GridGraph importGraphFromFile(String filename) {
        GridGraph gridGraph;
        GraphImporter graphImporter = new GraphImporter(filename);
        gridGraph = graphImporter.retrieve();
        return gridGraph;
    }

    /**
     * Tells the algorithm to compute a path. returns nothing.
     * Used to test how long the algorithm takes to complete the computation.
     */
    private static void testAlgorithmSpeed(GridGraph gridGraph, int sx, int sy,
            int ex, int ey) {
        PathFindingAlgorithm algo = algoFunction.getAlgo(gridGraph, sx, sy, ex, ey);
        algo.computePath();
    }

    /**
     * Generates a path between two points on a grid.
     * @return an array of int[2] indicating the coordinates of the path.
     */
    private static int[][] generatePath(GridGraph gridGraph, int sx, int sy,
            int ex, int ey) {
        PathFindingAlgorithm algo = algoFunction.getAlgo(gridGraph, sx, sy, ex, ey);
        try {
            algo.computePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int[][] path = algo.getPath();
        return path;
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

            testAndAddLine(x1,y1,x2,y2,gridGraph,gridLineSet);
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

    /**
     * Run tests for all the algorithms, and outputs them to the file.<br>
     * Please look into this method for more information.<br>
     * Comment out the algorithms you don't want to test.
     */
    private static void runTestAllAlgos() {
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> new AStar(gridGraph, sx, sy, ex, ey);
//        runTests("AStar_TI");
//        
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> new BreadthFirstSearch(gridGraph, sx, sy, ex, ey);
//        runTests("BFS");
//        
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> BreadthFirstSearch.postSmooth(gridGraph, sx, sy, ex, ey);
//        runTests("BFS-PS");
//        
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> AStar.postSmooth(gridGraph, sx, sy, ex, ey);
//        runTests("AStar-PS_TI");
//        
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> AStar.dijkstra(gridGraph, sx, sy, ex, ey);
//        runTests("Dijkstra");
        
        // Warning: Anya is unstable
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> new Anya(gridGraph, sx, sy, ex, ey);
//        runTests("Anya");

//        algoFunction = (gridGraph, sx, sy, ex, ey) -> new BasicThetaStar(gridGraph, sx, sy, ex, ey);  
//        runTests("BasicThetaStar_TI");

//        algoFunction = (gridGraph, sx, sy, ex, ey) -> BasicThetaStar.postSmooth(gridGraph, sx, sy, ex, ey);  
//        runTests("BasicThetaStar-PS_TI");
        
        // Warning: VisibilityGraph is slow
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> new VisibilityGraphAlgorithm(gridGraph, sx, sy, ex, ey);
//        runTests("VisibilityGraph");
//        
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> VisibilityGraphAlgorithm.graphReuse(gridGraph, sx, sy, ex, ey);
//        runTests("VisibilityGraph_REUSE");
        
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> VisibilityGraphAlgorithm.graphReuseSlowDijkstra(gridGraph, sx, sy, ex, ey);
//        runTests("VisibilityGraph_REUSE_SLOWDIJKSTRA");

        
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> new StrictThetaStar(gridGraph, sx, sy, ex, ey);
//        runTests("StrictThetaStar_5b");

        
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> StrictThetaStar.noHeuristic(gridGraph, sx, sy, ex, ey);
//        runTests("StrictThetaStar_NoHeuristic_5");
        
    }
    

    /**
     * Runs tests for the specified algorithm. Uses the runTest method, which
     * will output the test results to the file.
     * @param algoName The name of the algorithm to be used in the file.
     */
    private static void runTests(String algoName) {
        //runTest(algoName, 4, PathLengthClass.LONGEST);
        for (int i=1; i<=8; i++) {
            runTest(algoName, i, PathLengthClass.ALL);
        }
    }
    
    /**
     * Runs a test stated in the TestDataLibrary, and outputs the test result to a file.
     * @param algoName The name of the algorithm to be used in the file.
     * @param index The index of the test to use (which maze)
     * @param pathLengthClass The category of path lengths to use in the test.
     * Refer to TestDataLibrary for more information.
     */
    private static void runTest(String algoName, int index, PathLengthClass pathLengthClass) {
        String filename = algoName + "_Maze" + index + "_" + pathLengthClass.name();
        System.out.println("RUNNING TEST: " +  filename);
        
        TestDataLibrary library = new TestDataLibrary(index, pathLengthClass);
        GraphInfo graphInfo = library.getGraphInfo();
        GridGraph gridGraph = DefaultGenerator.generateSeededGraphOnly(
                graphInfo.seed, graphInfo.sizeX, graphInfo.sizeY, graphInfo.ratio);
        System.out.println("Percentage Blocked:" + gridGraph.getPercentageBlocked());
        
        FileIO fileIO = new FileIO(PATH_TESTDATA_NAME + filename + ".txt");
        //FileIO fileIO = new FileIO(PATH_TESTDATA_NAME + "test.txt");
        fileIO.writeLine("Algorithm", "Maze", "ComputedPath", "OptimalPath", "PathLengthRatio", "Time", "TimeSD", "Start", "End", "Trails");
        /*double sumTime = 0;
        double totalRatio = 1;
        
        int nDataTested = 0;*/

        TestResult tempRes = testAlgorithm(gridGraph, 0,0,1,0, 1, 1);
        System.out.println("Preprocess time: " + tempRes.time);
        while (library.hasNextData()) {
            StartEndPointData data = library.getNextData();
            
            TestResult testResult = testAlgorithm(gridGraph, data.start.x,
                    data.start.y, data.end.x, data.end.y, 10, graphInfo.nTrials);
            
            boolean valid = (testResult.pathLength > 0.00001f);
            
            double ratio = testResult.pathLength/data.shortestPath;

            /*if (valid) {
                nDataTested++;
                sumTime += testResult.time;
                totalRatio += ratio;
            }*/

            String algorithm = algoName;
            String maze = index + "";
            String pathLength = testResult.pathLength + "";
            String shortestPathLength = data.shortestPath + "";
            String pathLengthRatio = ratio + "";
            String time = testResult.time + "";
            String timeSD = testResult.timeSD + "";
            String start = data.start + "";
            String end = data.end + "";
            String nTrials = graphInfo.nTrials + "";
            
            if (!valid) {
                pathLength = "N/A";
                pathLengthRatio = "N/A";
            }
            
            fileIO.writeLine(algorithm, maze, pathLength, shortestPathLength, pathLengthRatio, time, timeSD, start, end, nTrials);
            fileIO.flush();
        }
        /*fileIO.writeLine("");
        
        double averageTime = sumTime / nDataTested;
        double averageRatio = totalRatio/nDataTested;
        float lowestLength = library.getLowestComputedLength();
        float highestLength = library.getHighestComputedLength();
        float meanComputedLength = library.getMeanComputedLength();
        float meanOverallLength = library.getOverallMeanLength();

        fileIO.writeLine("<<Overall Stats>>");
        fileIO.writeLine("Average Time: ", averageTime+"");
        fileIO.writeLine("Average Path Length Ratio: ", averageRatio+"");

        fileIO.writeLine("");
        fileIO.writeLine("<<Dataset Stats>>");
        fileIO.writeLine("Index " + index + ", Type " + pathLengthClass.name());
        fileIO.writeLine("Shortest Length of Test Data: ", lowestLength+"");
        fileIO.writeLine("Shortest Length of Test Data: ", highestLength+"");
        fileIO.writeLine("Average Length of Test Data: ", meanComputedLength+"");
        fileIO.writeLine("Average Length of all Test Data in maze " + index + ": ", meanOverallLength+"");
        */
        fileIO.close();
    }
    
    /**
     * Conducts a running time / path length test on the current algorithm on
     * a specified gridGraph. The algorithm used is the algorithm stored in
     * algoFunction.
     * 
     * @param gridGraph the grid to run the algorithm on.
     * @param startX x-coordinate of start point
     * @param startY y-coordinate of start point
     * @param endX x-coordinate of goal point
     * @param endY y-coordinate of goal point
     * @param sampleSize The size of the sample to use. Used in computing
     * Standard Deviation.
     * @param nTrials The number of times the algorithm is run per test. For
     * example, if sampleSize is 10 and nTrials is 50, the algorithm is run 50
     * times for each of the 10 tests to get 10 results. This is used for test
     * cases which possbly last shorter than a millisecond, making it hard to
     * record the running time of we only ran the algorithm once per record.
     * @return
     */
    private static TestResult testAlgorithm(GridGraph gridGraph,
            int startX, int startY, int endX, int endY, int sampleSize, int nTrials) {

        int[] data = new int[sampleSize];
        
        int sum = 0;
        long sumSquare = 0;
        
        //sampleSize = 0;// UNCOMMENT TO TEST PATH LENGTH ONLY
        for (int s = 0; s < sampleSize; s++) {
            long start = System.currentTimeMillis();
            for (int i=0;i<nTrials;i++) {
                testAlgorithmSpeed(gridGraph, startX, startY, endX, endY);
            }
            long end = System.currentTimeMillis();
            System.gc();
            
            data[s] = (int)(end-start);
            
            sum += data[s];
            sumSquare += data[s]*data[s];
        }
        
        double mean = (double)sum / nTrials / sampleSize;
        double secondMomentTimesN = (double)sumSquare / nTrials / nTrials;
        double sampleVariance = (secondMomentTimesN - sampleSize*(mean*mean)) / (sampleSize - 1);
        double standardDeviation = Math.sqrt(sampleVariance);

        int[][] path = generatePath(gridGraph, startX, startY, endX, endY);
        float pathLength = computePathLength(gridGraph, path);
        
        TestResult testResult = new TestResult(sampleSize, mean, standardDeviation, pathLength);
        return testResult;
    }
    
    private static void testAbilityToFindGoal() {
        setDefaultAlgoFunction();
        
        AlgoFunction currentAlgo = algoFunction;
        AlgoFunction bfs = (gridGraph, sx, sy, ex, ey) -> new BreadthFirstSearch(gridGraph, sx, sy, ex, ey);

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
            algoFunction = bfs;
            int[][] path = generatePath(gridGraph, sx, sy, ex, ey);
            float pathLength = computePathLength(gridGraph, path);
            boolean bfsValid = (pathLength > 0.00001f);

            algoFunction = currentAlgo;
            path = generatePath(gridGraph, sx, sy, ex, ey);
            pathLength = computePathLength(gridGraph, path);
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
        FileIO fileIO = new FileIO(PATH_TESTDATA_NAME + "VisibilityGraphSizes.txt");
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
    

    /**
     * Records a trace of the current algorithm into a LinkedList of GridObjects.
     */
    private static LinkedList<GridObjects> recordAlgorithmOperation (
            GridGraph gridGraph, int sx, int sy, int ex, int ey) {
        PathFindingAlgorithm algo = algoFunction.getAlgo(gridGraph, sx, sy, ex, ey);
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
            gridObjectsList.add(createGridObjects(snapshot));
        }
        return gridObjectsList;
    }
    
    private static Color or(Color color, Color original) {
        return (original==null?color:original);
    }
    
    /**
     * Convert a snapshot of an algorithm into a GridObjects instance.
     */
    private static GridObjects createGridObjects(List<SnapshotItem> snapshot) {
        GridLineSet gridLineSet = new GridLineSet();
        GridPointSet gridPointSet = new GridPointSet();
        
        for (SnapshotItem item : snapshot) {
            Integer[] path = item.path;
            Color color = item.color;
            if (path.length == 4) {
                gridLineSet.addLine(path[0], path[1], path[2], path[3], or(Color.RED,color));
            } else if (path.length == 2) {
                gridPointSet.addPoint(path[0], path[1], or(Color.BLUE,color));
            } else if (path.length == 7) {
                // y, xLn, xLd, xRn, xRd, px, py
                Fraction y = new Fraction (path[0]);
                Fraction xL = new Fraction(path[1], path[2]);
                Fraction xR = new Fraction(path[3], path[4]);
                Fraction xMid = xR.minus(xL).multiplyDivide(1, 2).plus(xL);
                Fraction px = new Fraction (path[5]);
                Fraction py = new Fraction (path[6]);
                gridLineSet.addLine(px, py, xL, y, or(Color.CYAN,color));
                gridLineSet.addLine(px, py, xMid, y, or(Color.CYAN,color));
                gridLineSet.addLine(px, py, xR, y, or(Color.CYAN,color));
                gridLineSet.addLine(xL, y, xR, y, or(Color.RED,color));
                gridPointSet.addPoint(path[5], path[6], or(Color.BLUE,color));
            } else if (path.length == 5) {
                Fraction y = new Fraction (path[0]);
                Fraction xL = new Fraction(path[1], path[2]);
                Fraction xR = new Fraction(path[3], path[4]);
                gridLineSet.addLine(xL, y, xR, y, or(Color.GREEN,color));
            }
        }
        return new GridObjects(gridLineSet,gridPointSet);
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
    
    
    interface AlgoFunction {
        public abstract PathFindingAlgorithm getAlgo(GridGraph gridGraph, int sx, int sy, int ex, int ey);
    }
}


class TestResult {
    public final int timesRan;
    public final double time;
    public final double timeSD;
    public final float pathLength;
    
    public TestResult(int timesRan, double time, double timeSD, float pathLength) {
        this.timesRan = timesRan;
        this.time = time;
        this.timeSD = timeSD;
        this.pathLength = pathLength;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Times ran: " + timesRan).append("\n");
        sb.append("Mean Time (ms): " + time + " (+/-" + timeSD + ")").append("\n");
        sb.append("Path length: " + pathLength).append("\n");
        
        return sb.toString();
    }
}