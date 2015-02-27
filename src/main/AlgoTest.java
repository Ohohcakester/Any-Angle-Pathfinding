package main;

import grid.GridGraph;

import java.util.ArrayList;

import main.AnyAnglePathfinding.AlgoFunction;
import main.analysis.TwoPoint;
import main.testdata.PathLengthClass;
import main.testdata.StandardMazes;
import main.testdata.StartEndPointData;
import main.testdata.TestDataGenerator;
import main.testdata.TestDataLibrary;
import uiandio.FileIO;
import uiandio.GraphImporter;
import algorithms.AStar;
import algorithms.AcceleratedAStar;
import algorithms.BasicThetaStar;
import algorithms.PathFindingAlgorithm;

public class AlgoTest {
    
    public static void run() {
        //runTestAllAlgos();
        
        AlgoFunction thetaStar = (a,b,c,d,e) -> new BasicThetaStar(a,b,c,d,e);
        AlgoFunction accAStar = (a,b,c,d,e) -> new AcceleratedAStar(a,b,c,d,e);
        
        AlgoFunction select = thetaStar;
        
        System.out.println("Low Density");
        testOnMaze("def_iO2GZNB_iSB_iSB_iSB", select, printAverage);
        System.out.println("High Density");
        testOnMaze("def_i3GRHWMD_iSB_iSB_iH", select, printAverage);
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
    
    
    private static final TestFunction printAverage = (mazeName, gridGraph, problems, algoFunction) -> {
        int sampleSize = 20;
        int nTrials = 10;
        
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

        System.out.println("Sample Size: " + sampleSize);
        System.out.println("Average Time: " + (totalMean/nResults));
        System.out.println("Average SD: " + (totalSD/nResults));
        System.out.println("Average Path Length: " + (totalPathLength/nResults));
    };


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
        TestResult time = testAlgorithmTime(gridGraph,algoFunction,tp,sampleSize,nTrials);
        TestResult pathLength = testAlgorithmPathLength(gridGraph,algoFunction,tp);
        return new TestResult(time.timesRan, time.time, time.timeSD, pathLength.pathLength);
    }
    
    
    
    
    
    
    /**
     * Run tests for all the algorithms, and outputs them to the file.<br>
     * Please look into this method for more information.<br>
     * Comment out the algorithms you don't want to test.
     */
    private static void runTestAllAlgos() {
        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new AStar(gridGraph, sx, sy, ex, ey);
        runTests("AStar_TI");
        
        /*AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new BreadthFirstSearch(gridGraph, sx, sy, ex, ey);
        runTests("BFS");
        
        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> BreadthFirstSearch.postSmooth(gridGraph, sx, sy, ex, ey);
        runTests("BFS-PS");
        
        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> AStar.postSmooth(gridGraph, sx, sy, ex, ey);
        runTests("AStar-PS_TI");
        
        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> AStar.dijkstra(gridGraph, sx, sy, ex, ey);
        runTests("Dijkstra");
        
        // Warning: Anya is unstable
        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new Anya(gridGraph, sx, sy, ex, ey);
        runTests("Anya");

        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new BasicThetaStar(gridGraph, sx, sy, ex, ey);  
        runTests("BasicThetaStar_TI");

        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> BasicThetaStar.postSmooth(gridGraph, sx, sy, ex, ey);  
        runTests("BasicThetaStar-PS_TI");
        
        // Warning: VisibilityGraph is slow
        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new VisibilityGraphAlgorithm(gridGraph, sx, sy, ex, ey);
        runTests("VisibilityGraph");
        
        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> VisibilityGraphAlgorithm.graphReuse(gridGraph, sx, sy, ex, ey);
        runTests("VisibilityGraph_REUSE");
        
        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> VisibilityGraphAlgorithm.graphReuseSlowDijkstra(gridGraph, sx, sy, ex, ey);
        runTests("VisibilityGraph_REUSE_SLOWDIJKSTRA");*/

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
        for (int i=1; i<=8; i++) {
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
            ArrayList<TwoPoint> problemSet, AlgoFunction algoFunction);
}