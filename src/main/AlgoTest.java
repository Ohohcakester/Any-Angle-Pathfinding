package main;

import grid.GridGraph;

import java.util.ArrayList;

import main.AnyAnglePathfinding.AlgoFunction;
import main.analysis.TwoPoint;
import main.testgen.PathLengthClass;
import main.testgen.StandardMazes;
import main.testgen.StartEndPointData;
import main.testgen.TestDataGenerator;
import main.testgen.TestDataLibrary;
import uiandio.FileIO;
import uiandio.GraphImporter;
import algorithms.AStar;
import algorithms.AcceleratedAStar;
import algorithms.BasicThetaStar;
import algorithms.PathFindingAlgorithm;
import algorithms.RestrictedVisibilityGraphAlgorithm;
import algorithms.StrictThetaStar;
import algorithms.StrictVisibilityGraphAlgorithm;
import algorithms.VisibilityGraphAlgorithm;

public class AlgoTest {
    
    public static void run() {
        //runTestAllAlgos();

        AlgoFunction aStar = (a,b,c,d,e) -> new AStar(a,b,c,d,e);
        AlgoFunction thetaStar = (a,b,c,d,e) -> new BasicThetaStar(a,b,c,d,e);
        AlgoFunction strictThetaStar = (a,b,c,d,e) -> new StrictThetaStar(a,b,c,d,e);
        AlgoFunction sVGA = (a,b,c,d,e) -> new StrictVisibilityGraphAlgorithm(a,b,c,d,e);
        AlgoFunction rVGA = (a,b,c,d,e) -> new RestrictedVisibilityGraphAlgorithm(a,b,c,d,e);
        AlgoFunction accAStar = (a,b,c,d,e) -> new AcceleratedAStar(a,b,c,d,e);
        AlgoFunction vgReuse = (a,b,c,d,e) -> VisibilityGraphAlgorithm.graphReuse(a,b,c,d,e);
        AlgoFunction vga = (a,b,c,d,e) -> new VisibilityGraphAlgorithm(a,b,c,d,e);

//        testSequence(vgReuse, "Visibility Graphs - Graph Reuse");
        testSequence(vga, "A* on Visibility Graphs");
//      testSequence(aStar, "8-directional A*");
//        testSequence(thetaStar, "Basic Theta*");
//        testSequence(strictThetaStar, "Strict Theta*");
//        testSequence(sVGA, "Strict Visibility Graphs");
//        testSequence(rVGA, "Restricted Visibility Graphs");
//        testSequence(accAStar, "Accelerated A*");
        
        //AlgoFunction select = accAStar;

        /*System.out.println("Low Density");
        testOnMaze("def_iHHLNUOB_iMJ_iMJ_iSB", select, printAverage);*/
        
        //testOnGraph(DefaultGenerator.generateSeededGraphOnly(567069235, 100, 100, 15),
        //        toTwoPointlist(15,14,37,79), select, printAverage);

//        System.out.println("Low Density");
//        testOnMaze("def_iO2GZNB_iSB_iSB_iSB", select, printAverage);
//        System.out.println("High Density");
//        testOnMaze("def_i3GRHWMD_iSB_iSB_iH", select, printAverage);

        /*System.out.println("Low Density - 6% - 500x500");
        testOnMaze("def_iIRXXUKC_iUP_iUP_iSB", select, printAverage);
        System.out.println("Medium Density - 20% - 500x500");
        testOnMaze("def_iOMJ14Z_iUP_iUP_iP", select, printAverage);
        System.out.println("High Density - 40% - 500x500");
        testOnMaze("def_iREPZHKB_iUP_iUP_iH", select, printAverage);*/
    }
    
    public static void testSequence(AlgoFunction algo, String name) {
        System.out.println("=== Testing " + name + " ===");

        System.out.println("<< GAME MAPS >>");
        
//        System.out.println("sc2_steppesofwar - 164x164 - spacious");
//        testOnMazeData("sc2_steppesofwar", algo, printAverageData(10, 5));
//        System.out.println("sc2_losttemple - 132x131");
//        testOnMazeData("sc2_losttemple", algo, printAverageData(10, 5));
//        System.out.println("sc2_extinction - 164x164 - less spacious");
//        testOnMazeData("sc2_extinction", algo, printAverageData(10, 5));
//
//        System.out.println("baldursgate_AR0070SR 124x134");
//        testOnMazeData("baldursgate_AR0070SR", algo, printAverageData(10, 5));
//        System.out.println("baldursgate_AR0705SR - 100x86 - less spacious");
//        testOnMazeData("baldursgate_AR0705SR", algo, printAverageData(10, 5));
//        System.out.println("baldursgate_AR0418SR - 84x75 - spacious");
//        testOnMazeData("baldursgate_AR0418SR", algo, printAverageData(10, 5));
//
//        System.out.println("wc3_icecrown - 512x512 (spacious)");
//        testOnMazeData("wc3_icecrown", algo, printAverageData(5, 4));
//        System.out.println("wc3_dragonfire - 512x512 (less spacious)");
//        testOnMazeData("wc3_dragonfire", algo, printAverageData(5, 4));
//
//        System.out.println("<< GENERATED MAPS >>");
//        
//        System.out.println("Low Density - 6% - 50x50");
//        testOnMazeData("def_iCUZANYD_iSB_iSB_iSB", algo, printAverageData(20, 10));
//        System.out.println("Medium Density - 20% - 50x50");
//        testOnMazeData("def_i10VA3PD_iSB_iSB_iP", algo, printAverageData(20, 10));
//        System.out.println("High Density - 40% - 50x50");
//        testOnMazeData("def_i3ML5FBD_iSB_iSB_iH", algo, printAverageData(20, 10));
//
//        System.out.println("Low Density - 6% - 300x300");
//        testOnMazeData("def_iHHLNUOB_iMJ_iMJ_iSB", algo, printAverageData(5, 4));
//        System.out.println("Medium Density - 20% - 300x300");
//        testOnMazeData("def_iZLPIX5B_iMJ_iMJ_iP", algo, printAverageData(5, 4));
//        System.out.println("High Density - 40% - 300x300");
//        testOnMazeData("def_iVVJKDR_iMJ_iMJ_iH", algo, printAverageData(5, 4));
//
//        System.out.println("Low Density - 6% - 500x500");
//        testOnMazeData("def_iIRXXUKC_iUP_iUP_iSB", algo, printAverageData(3, 3));
//        System.out.println("Medium Density - 20% - 500x500");
//        testOnMazeData("def_iOMJ14Z_iUP_iUP_iP", algo, printAverageData(3, 3));
//        System.out.println("High Density - 40% - 500x500");
//        testOnMazeData("def_iREPZHKB_iUP_iUP_iH", algo, printAverageData(3, 3));

        System.out.println("obst10_random512-10-7 - 10% - 512x512");
        testOnMazeData("obst10_random512-10-7", algo, printAverageData(3, 3));
        System.out.println("obst40_random512-40-7 - 67% - 512x512");
        testOnMazeData("def_iOMJ14Z_iUP_iUP_iP", algo, printAverageData(3, 3));

        System.out.println("=== FINISHED TEST FOR " + name + " ===");
        System.out.println();
    }
    
    public static ArrayList<TwoPoint> toTwoPointlist(int...points) {
        return TestDataGenerator.generateTwoPointList(points);
    }
    
    public static void testOnMaze(String mazeName, AlgoFunction algoFunction, TestFunction test) {
        ArrayList<TwoPoint> problems = GraphImporter.loadStoredMazeProblems(mazeName);
        testOnMaze(mazeName, problems, algoFunction, test);
    }
    
    public static void testOnMaze(String mazeName, ArrayList<TwoPoint> problems, AlgoFunction algoFunction, TestFunction test) {
        GridGraph gridGraph = GraphImporter.loadStoredMaze(mazeName);
        test.test(mazeName, gridGraph, problems, algoFunction);
    }
    
    public static void testOnGraph(GridGraph gridGraph, ArrayList<TwoPoint> problems, AlgoFunction algoFunction, TestFunction test) {
        test.test("undefined", gridGraph, problems, algoFunction);
    }
    
    public static void testOnMazeData(String mazeName, AlgoFunction algoFunction, TestFunctionData test) {
        ArrayList<StartEndPointData> problems = GraphImporter.loadStoredMazeProblemData(mazeName);
        testOnMazeData(mazeName, problems, algoFunction, test);
    }
    
    public static void testOnMazeData(String mazeName, ArrayList<StartEndPointData> problems, AlgoFunction algoFunction, TestFunctionData test) {
        GridGraph gridGraph = GraphImporter.loadStoredMaze(mazeName);
        test.test(mazeName, gridGraph, problems, algoFunction);
    }
    
    public static void testOnGraphData(GridGraph gridGraph, ArrayList<StartEndPointData> problems, AlgoFunction algoFunction, TestFunctionData test) {
        test.test("undefined", gridGraph, problems, algoFunction);
    }
    
    
    private static final TestFunction printAverage = (mazeName, gridGraph, problems, algoFunction) -> {
        int sampleSize = 10;
        int nTrials = 5;
        
        TestResult[] testResults = new TestResult[problems.size()];
        int index = 0;
        for (TwoPoint problem : problems) {
            testResults[index] = testAlgorithm(gridGraph, algoFunction, problem, sampleSize, nTrials);
            index++;
        }
        double totalMean = 0;
        double totalSD = 0;
        double totalPathLength = 0;
        for (TestResult testResult : testResults) {
            totalMean += testResult.time;
            totalSD += testResult.timeSD;
            totalPathLength += testResult.pathLength;
        }
        int nResults = testResults.length;


        System.out.println("Sample Size: " + sampleSize + " x " + nTrials + " trials");
        System.out.println("Average Time: " + (totalMean/nResults));
        System.out.println("Average SD: " + (totalSD/nResults));
        System.out.println("Average Path Length: " + (totalPathLength/nResults));
    };
    

    
    private static final TestFunctionData printAverageData(int sampleSize, int nTrials) {
        return (mazeName, gridGraph, problems, algoFunction) -> {
    
            double sum = 0;
            double sumSquare = 0;
            double totalPathLength = 0;
            
            int nResults = 0;
            for (StartEndPointData problem : problems) {
                TwoPoint tp = new TwoPoint(problem.start, problem.end);
                TestResult testResult = testAlgorithm(gridGraph, algoFunction, tp, sampleSize, nTrials);
                
                sum += testResult.time;
                sumSquare += testResult.time*testResult.time;
                totalPathLength += testResult.pathLength / problem.shortestPath;
                
                nResults++;
            }
            
            double mean = (double)sum / nResults;
            double secondMomentTimesN = (double)sumSquare;
            double sampleVariance = (secondMomentTimesN - nResults*(mean*mean)) / (nResults - 1);
            double standardDeviation = Math.sqrt(sampleVariance);

            System.out.println("Sample Size: " + sampleSize + " x " + nTrials + " trials");
            System.out.println("Average Time: " + mean);
            System.out.println("Standard Dev: " + standardDeviation);
            System.out.println("Average Path Length: " + (totalPathLength/nResults));
            System.out.println();
        };
    }


    private static TestResult testAlgorithmTime(GridGraph gridGraph,
            AlgoFunction algoFunction, TwoPoint tp, int sampleSize, int nTrials) {
    
        int[] data = new int[sampleSize];
        
        int sum = 0;
        long sumSquare = 0;

        int startX = tp.p1.x;
        int startY = tp.p1.y;
        int endX = tp.p2.x;
        int endY = tp.p2.y;
        
        for (int s = 0; s < sampleSize; s++) {
            long start = System.currentTimeMillis();
            for (int i=0;i<nTrials;i++) {
                AlgoTest.testAlgorithmSpeed(algoFunction, gridGraph, startX, startY, endX, endY);
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
    
        TestResult testResult = new TestResult(sampleSize, mean, standardDeviation, -1f);
        return testResult;
    }

    private static TestResult testAlgorithmPathLength(GridGraph gridGraph,
            AlgoFunction algoFunction, TwoPoint tp) {
    
        int[][] path = Utility.generatePath(algoFunction, gridGraph,
                tp.p1.x, tp.p1.y, tp.p2.x, tp.p2.y);
        float pathLength = Utility.computePathLength(gridGraph, path);
        
        TestResult testResult = new TestResult(-1, -1, -1, pathLength);
        return testResult;
    }
    
    private static TestResult testAlgorithm(GridGraph gridGraph,
            AlgoFunction algoFunction, TwoPoint tp, int sampleSize, int nTrials) {
        TestResult pathLength = testAlgorithmPathLength(gridGraph,algoFunction,tp);
        TestResult time = testAlgorithmTime(gridGraph,algoFunction,tp,sampleSize,nTrials);
        return new TestResult(time.timesRan, time.time, time.timeSD, pathLength.pathLength);
    }
    
    
    
    
    
    
    /**
     * Run tests for all the algorithms, and outputs them to the file.<br>
     * Please look into this method for more information.<br>
     * Comment out the algorithms you don't want to test.
     */
    private static void runTestAllAlgos() {
//        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new AStar(gridGraph, sx, sy, ex, ey);
//        runTests("AStar_TI");
//        
//        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new BreadthFirstSearch(gridGraph, sx, sy, ex, ey);
//        runTests("BFS");
//        
//        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> BreadthFirstSearch.postSmooth(gridGraph, sx, sy, ex, ey);
//        runTests("BFS-PS");
//        
//        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> AStar.postSmooth(gridGraph, sx, sy, ex, ey);
//        runTests("AStar-PS_TI");
//        
//        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> AStar.dijkstra(gridGraph, sx, sy, ex, ey);
//        runTests("Dijkstra");
//        
//        // Warning: Anya is unstable
//        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new Anya(gridGraph, sx, sy, ex, ey);
//        runTests("Anya");
//
//        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new BasicThetaStar(gridGraph, sx, sy, ex, ey);  
//        runTests("BasicThetaStar_TI");
//
//        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> BasicThetaStar.postSmooth(gridGraph, sx, sy, ex, ey);  
//        runTests("BasicThetaStar-PS_TI");
//
        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> VisibilityGraphAlgorithm.graphReuse(gridGraph, sx, sy, ex, ey);
        runTests("VisibilityGraph_REUSE");

        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new RestrictedVisibilityGraphAlgorithm(gridGraph, sx, sy, ex, ey);
        runTests("RestrictedVisibilityGraph");
        
        // Warning: VisibilityGraph is slow
        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new VisibilityGraphAlgorithm(gridGraph, sx, sy, ex, ey);
        runTests("VisibilityGraph");
        
//        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> VisibilityGraphAlgorithm.graphReuseSlowDijkstra(gridGraph, sx, sy, ex, ey);
//        runTests("VisibilityGraph_REUSE_SLOWDIJKSTRA");

//        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new StrictThetaStar(gridGraph, sx, sy, ex, ey);
//        runTests("StrictThetaStar_5b");
//
//        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> StrictThetaStar.noHeuristic(gridGraph, sx, sy, ex, ey);
//        runTests("StrictThetaStar_NoHeuristic_5");
        
    }

    /**
     * Runs tests for the specified algorithm. Uses the runTest method, which
     * will output the test results to the file.
     * @param algoName The name of the algorithm to be used in the file.
     */
    private static void runTests(String algoName) {
        //runTest(algoName, 4, PathLengthClass.LONGEST);
        for (int i=1; i<=6; i++) {
            AlgoTest.runTest(algoName, i, PathLengthClass.ALL);
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
        
        TestDataLibrary library = new StandardMazes(index, pathLengthClass);
        GridGraph gridGraph = library.generateGraph();
        
        System.out.println("Percentage Blocked:" + gridGraph.getPercentageBlocked());
        
        FileIO fileIO = new FileIO(AnyAnglePathfinding.PATH_TESTDATA + filename + ".txt");
        fileIO.writeRow("Algorithm", "Maze", "ComputedPath", "OptimalPath", "PathLengthRatio", "Time", "TimeSD", "Start", "End", "Trails");

        TestResult tempRes = AlgoTest.testAlgorithm(gridGraph, 0, 0, 1, 0, 1, 1);
        System.out.println("Preprocess time: " + tempRes.time);
        
        while (library.hasNextData()) {
            StartEndPointData data = library.getNextData();
            
            TestResult testResult = AlgoTest.testAlgorithm(gridGraph, data.start.x,
                    data.start.y, data.end.x, data.end.y, 10, library.getNTrials());
            
            boolean valid = (testResult.pathLength > 0.00001f);
            double ratio = testResult.pathLength/data.shortestPath;
    
            String algorithm = algoName;
            String maze = index + "";
            String pathLength = testResult.pathLength + "";
            String shortestPathLength = data.shortestPath + "";
            String pathLengthRatio = ratio + "";
            String time = testResult.time + "";
            String timeSD = testResult.timeSD + "";
            String start = data.start + "";
            String end = data.end + "";
            String nTrials = library.getNTrials() + "";
            
            if (!valid) {
                pathLength = "N/A";
                pathLengthRatio = "N/A";
            }
            
            fileIO.writeRow(algorithm, maze, pathLength, shortestPathLength, pathLengthRatio, time, timeSD, start, end, nTrials);
            fileIO.flush();
        }
        
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
                AlgoTest.testAlgorithmSpeed(gridGraph, startX, startY, endX, endY);
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
    
        int[][] path = Utility.generatePath(gridGraph, startX, startY, endX, endY);
        float pathLength = Utility.computePathLength(gridGraph, path);
        
        TestResult testResult = new TestResult(sampleSize, mean, standardDeviation, pathLength);
        return testResult;
    }

    /**
     * Tells the algorithm to compute a path. returns nothing.
     * Used to test how long the algorithm takes to complete the computation.
     */
    private static void testAlgorithmSpeed(GridGraph gridGraph, int sx, int sy,
            int ex, int ey) {
        PathFindingAlgorithm algo = AnyAnglePathfinding.algoFunction.getAlgo(gridGraph, sx, sy, ex, ey);
        algo.computePath();
    }

    private static void testAlgorithmSpeed(AlgoFunction algoFunction, GridGraph gridGraph, int sx, int sy,
            int ex, int ey) {
        PathFindingAlgorithm algo = algoFunction.getAlgo(gridGraph, sx, sy, ex, ey);
        algo.computePath();
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


interface TestFunction {
    public void test(String mazeName, GridGraph gridGraph,
            ArrayList<TwoPoint> problemSet, AlgoFunction algoFunction);
}

interface TestFunctionData {
    public void test(String mazeName, GridGraph gridGraph,
            ArrayList<StartEndPointData> problemSet, AlgoFunction algoFunction);
}
