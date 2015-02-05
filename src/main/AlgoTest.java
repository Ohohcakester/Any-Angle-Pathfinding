package main;

import grid.GridGraph;
import main.graphgeneration.DefaultGenerator;
import main.graphgeneration.GraphInfo;
import uiandio.FileIO;
import algorithms.AStar;
import algorithms.Anya;
import algorithms.BasicThetaStar;
import algorithms.BreadthFirstSearch;
import algorithms.PathFindingAlgorithm;
import algorithms.VisibilityGraphAlgorithm;

public class AlgoTest {
    
    public static void run() {
        runTestAllAlgos();
    }
    
    /**
     * Run tests for all the algorithms, and outputs them to the file.<br>
     * Please look into this method for more information.<br>
     * Comment out the algorithms you don't want to test.
     */
    private static void runTestAllAlgos() {
        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new AStar(gridGraph, sx, sy, ex, ey);
        runTests("AStar_TI");
        
        AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new BreadthFirstSearch(gridGraph, sx, sy, ex, ey);
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
        runTests("VisibilityGraph_REUSE_SLOWDIJKSTRA");

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
        
        TestDataLibrary library = new TestDataLibrary(index, pathLengthClass);
        GraphInfo graphInfo = library.getGraphInfo();
        GridGraph gridGraph = DefaultGenerator.generateSeededGraphOnly(
                graphInfo.seed, graphInfo.sizeX, graphInfo.sizeY, graphInfo.ratio);
        System.out.println("Percentage Blocked:" + gridGraph.getPercentageBlocked());
        
        FileIO fileIO = new FileIO(AnyAnglePathfinding.PATH_TESTDATA_NAME + filename + ".txt");
        //FileIO fileIO = new FileIO(PATH_TESTDATA_NAME + "test.txt");
        fileIO.writeLine("Algorithm", "Maze", "ComputedPath", "OptimalPath", "PathLengthRatio", "Time", "TimeSD", "Start", "End", "Trails");
        /*double sumTime = 0;
        double totalRatio = 1;
        
        int nDataTested = 0;*/
    
        TestResult tempRes = AlgoTest.testAlgorithm(gridGraph, 0,0,1,0, 1, 1);
        System.out.println("Preprocess time: " + tempRes.time);
        while (library.hasNextData()) {
            StartEndPointData data = library.getNextData();
            
            TestResult testResult = AlgoTest.testAlgorithm(gridGraph, data.start.x,
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
