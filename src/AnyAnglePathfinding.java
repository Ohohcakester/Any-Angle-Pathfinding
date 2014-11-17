import grid.AStar;
import grid.Anya;
import grid.BasicThetaStar;
import grid.BreadthFirstSearch;
import grid.GridGraph;
import grid.PathFindingAlgorithm;
import grid.ReachableNodes;
import grid.SnapshotItem;
import grid.VisibilityGraphAlgorithm;
import grid.anya.Fraction;
import grid.anya.Point;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import draw.DrawCanvas;
import draw.GridLineSet;
import draw.GridObjects;
import draw.GridPointSet;
import draw.KeyToggler;

/**
 * Test Plan:
 * Each of them has : near / far
 * 1) 30x30, ratio 7
 * 2) 30x30, ratio 15
 * 3) 30x30, ratio 50
 * 4) 100x100, ratio 7
 * 5) 100x100, ratio 15
 * 6) 100x100, ratio 50
 * 7) 500x500, ratio 7
 * 8) 500x500, ratio 50
 * 
 * @author Oh
 *
 */
public class AnyAnglePathfinding {
    
    private static String PATH_TESTDATA_NAME = "testdata/";

    private static int unblockedRatio = 13;
    private static boolean seededRandom = true;
    private static int seed = 1320146292;
    
    public static Random rand = new Random();
    private static int sizeX = 40;
    private static int sizeY = 40;

    private static int sx = 6;
    private static int sy = 16;
    private static int ex = 38;
    private static int ey = 27;
    
    private static AlgoFunction algoFunction;

    private static GridGraph loadMaze() {
        int choice = 10;
        
        switch(choice) {
            case 0 :
                return generateSeededRandomGraph();
            case 1 :
                return importGraphFromFile("maze.txt");
            case 2 :
                return generateSeededRandomGraph(-98783479, 40, 40, 7, 1, 4, 18, 18); // maze 3
            case 3 :
                return generateSeededRandomGraph(-565315494, 15, 15, 9, 1, 2, 1, 13); // maze 2
            case 4 :
                return generateSeededRandomGraph(53, 15, 15, 9, 0, 0, 10, 14); // maze 1
            case 5 :
                return generateSeededRandomGraph(-159182402, 15, 15, 9, 1, 1, 13, 12); // contradict anya
            case 6 :
                return importGraphFromFile("maze14x11.txt", 0, 0, 10, 10); // Maze to contradict Theta* / A*
            case 7 :
                return importGraphFromFile("mazeWCS.txt", 2, 0, 28, 25); // Worst Case Scenario path length.
            case 8 :
                return generateSeededRandomGraph(-410889275, 15, 15, 7, 0, 1, 10, 12); // maze 4
            case 9 :
                return importGraphFromFile("mazeThetaWCS.txt", 0, 0, 28, 13); // Worst Case Scenario for Theta*
            case 10 :
                return importGraphFromFile("mazeReuseWCS.txt", 1, 28, 0, 27); // Worst Case Scenario for Visibility Graph reuse.
            default :
                return null;
        }
    }
    
    private static void setDefaultAlgoFunction() {
        int choice = 7;
        
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
        }
    }
    
    
    public static void main(String[] args) {
        setDefaultAlgoFunction();
//        runTestAllAlgos();
        GridGraph gridGraph = loadMaze();
//
//        ArrayList<Point> points = ReachableNodes.computeReachable(gridGraph, 5, 5);
//        System.out.println(points.size());
//        
//        printGraphData(gridGraph);
        
//        GridLineSet gridLineSet = generateRandomTestLines(gridGraph, 100);

//        TestResult test1 = testAlgorithm(gridGraph, sx, sy, ex, ey, 1, 1);
//        System.out.println(test1);
        TestResult test1 = testAlgorithm(gridGraph, sx, sy, ex, ey, 3, 1);
        TestResult test2 = testAlgorithm(gridGraph, sx, sy, ex, ey, 10, 1);
        System.out.println(test2);
        
        
        displayAlgorithmOperation(gridGraph);
    }

    private static void printGraphData(GridGraph gridGraph) {
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
    
    private static boolean hasSolution(GridGraph gridGraph) {
        int[][] path = generatePath(gridGraph, sx, sy, ex, ey);
        return path.length > 1;
    }

    private static void displayAlgorithmOperation(GridGraph gridGraph) {
        /*GridLineSet gridLineSet2 = new GridLineSet();
        
        int[][] path2 = new int[][]{{5, 6}, {5, 7}, {9, 10}, {14, 15}, {22, 21}, {24, 25}, {32, 25}, {34, 24}, {35, 20}, {37, 20}, {43, 24}, {45, 28}, {52, 33}, {54, 42}, {67, 46}, {71, 48}, {75, 52}, {77, 55}, {78, 61}, {81, 71}, {82, 71}, {86, 67}};

        
        for (int i=0; i<path2.length-1; i++) {
            gridLineSet2.addLine(path2[i][0], path2[i][1],
                    path2[i+1][0], path2[i+1][1], Color.BLUE);
        }
        float pathLength2 = computePathLength(gridGraph, path2);
        System.out.println("Path Length: " + pathLength2);

        System.out.println(Arrays.deepToString(path2));*/
        
        GridLineSet gridLineSet = new GridLineSet();
        
        int[][] path = generatePath(gridGraph, sx, sy, ex, ey);
        
        for (int i=0; i<path.length-1; i++) {
            gridLineSet.addLine(path[i][0], path[i][1],
                    path[i+1][0], path[i+1][1], Color.BLUE);
        }
        float pathLength = computePathLength(gridGraph, path);
        System.out.println("Path Length: " + pathLength);

        System.out.println(Arrays.deepToString(path));

        //LinkedList<GridObjects> lineSetList = new LinkedList<GridObjects>();
        LinkedList<GridObjects> lineSetList = recordAlgorithmOperation(gridGraph, sx, sy, ex, ey);
        lineSetList.addLast(new GridObjects(gridLineSet, null));
        //lineSetList.addLast(new GridObjects(gridLineSet2, null));
        DrawCanvas drawCanvas = new DrawCanvas(gridGraph, gridLineSet);
        
        setupMainFrame(drawCanvas, lineSetList);
    }

    private static float computePathLength(GridGraph gridGraph, int[][] path) {
        float pathLength = 0;
        for (int i=0; i<path.length-1; i++) {
            pathLength += gridGraph.distance(path[i][0], path[i][1],
                            path[i+1][0], path[i+1][1]);
        }
        return pathLength;
    }
    
    private static GridGraph importGraphFromFile(String filename, int _sx, int _sy, int _ex, int _ey) {
        sx = _sx;
        sy = _sy;
        ex = _ex;
        ey = _ey;
        return importGraphFromFile(filename);
    }
    
    private static GridGraph generateSeededRandomGraph(int _seed, int _sizeX, int _sizeY, int _ratio, int _sx, int _sy, int _ex, int _ey) {
        seededRandom = true;
        seed =_seed;
        sizeX = _sizeX;
        sizeY = _sizeY;
        unblockedRatio = _ratio;
        sx = _sx;
        sy = _sy;
        ex = _ex;
        ey = _ey;
        return generateSeededRandomGraph();
    }

    private static GridGraph importGraphFromFile(String filename) {
        GridGraph gridGraph;
        GraphImporter graphImporter = new GraphImporter(filename);
        gridGraph = graphImporter.retrieve();
        return gridGraph;
    }

    private static GridGraph generateSeededRandomGraph() {
        GridGraph gridGraph = new GridGraph(sizeX, sizeY);

        if (!seededRandom) {
            seed = rand.nextInt();
            System.out.println("Starting random with random seed = " + seed);
        } else {
            System.out.println("Starting random with predefined seed = " + seed);
        }
        rand = new Random(seed);
        
        generateRandomBlockMap(gridGraph, unblockedRatio);
        fillCorners(gridGraph);

        gridGraph.trySetBlocked(sx, sy, false);
        gridGraph.trySetBlocked(ex, ey, false);
        return gridGraph;
    }


    private static void testAlgorithmSpeed(GridGraph gridGraph, int sx, int sy,
            int ex, int ey) {
        PathFindingAlgorithm algo = algoFunction.getAlgo(gridGraph, sx, sy, ex, ey);
        algo.computePath();
    }

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

    private static TestResult testAlgorithm(GridGraph gridGraph,
            int startX, int startY, int endX, int endY, int sampleSize, int nTrials) {

        
        int[] data = new int[sampleSize];
        
        int sum = 0;
        long sumSquare = 0;
        
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

    private static GridLineSet generateRandomTestLines(GridGraph gridGraph,
            int amount) {
        GridLineSet gridLineSet = new GridLineSet();
        
        for (int i=0; i<amount; i++) {
            int x1 = rand.nextInt(sizeX);
            int y1 = rand.nextInt(sizeY);
            int x2 = rand.nextInt(sizeX);
            int y2 = rand.nextInt(sizeY);

            testAndAddLine(x1,y1,x2,y2,gridGraph,gridLineSet);
        }
        
        return gridLineSet;
    }

    private static void generateRandomMap(GridGraph gridGraph, int frequency) {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                gridGraph.setBlocked(x, y, rand.nextInt()%frequency == 0);
            }
        }
    }

    private static void fillCorners(GridGraph gridGraph) {
        boolean didSomething = true;;
        while (didSomething) {
            didSomething = false;
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    if (gridGraph.isBlocked(x, y)) {
                        if (gridGraph.isValidBlock(x+1, y+1) && gridGraph.isBlocked(x+1, y+1)) {
                            if (!gridGraph.isBlocked(x+1, y) && !gridGraph.isBlocked(x, y+1)) {
                                if (rand.nextBoolean())
                                    gridGraph.setBlocked(x+1, y, true);
                                else
                                    gridGraph.setBlocked(x, y+1, true);
                                didSomething = true;
                            }
                        }

                        if (gridGraph.isValidBlock(x-1, y+1) && gridGraph.isBlocked(x-1, y+1)) {
                            if (!gridGraph.isBlocked(x-1, y) && !gridGraph.isBlocked(x, y+1)) {
                                if (rand.nextBoolean())
                                    gridGraph.setBlocked(x-1, y, true);
                                else
                                    gridGraph.setBlocked(x, y+1, true);
                                didSomething = true;
                            }
                        }
                    }
                }
            }
        }
    }


    private static void generateRandomBlockMap(GridGraph gridGraph, int frequency) {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                if (rand.nextInt(frequency) == 0) {
                    switch(rand.nextInt(3)) {
                        case 0:
                            gridGraph.trySetBlocked(x, y, true);
                            gridGraph.trySetBlocked(x, y+1, true);
                            gridGraph.trySetBlocked(x+1, y, true);
                            gridGraph.trySetBlocked(x+1, y+1, true);
                            break;
                        case 1:
                            gridGraph.trySetBlocked(x, y-1, true);
                            gridGraph.trySetBlocked(x, y, true);
                            gridGraph.trySetBlocked(x, y+1, true);
                            break;
                        case 2:
                            gridGraph.trySetBlocked(x-1, y, true);
                            gridGraph.trySetBlocked(x, y, true);
                            gridGraph.trySetBlocked(x+1, y, true);
                            break;
                    }
                }
            }
        }
    }

    private static void testAndAddLine(int x1, int y1, int x2, int y2,
            GridGraph gridGraph, GridLineSet gridLineSet) {
        
        if (gridGraph.lineOfSight(x1, y1, x2, y2)) {
            gridLineSet.addLine(x1, y1, x2, y2, Color.GREEN);
        } else {
            gridLineSet.addLine(x1, y1, x2, y2, Color.RED);
        }
        
    }
    
    private static void runTestAllAlgos() {
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> new AStar(gridGraph, sx, sy, ex, ey);
//        runTests("AStar_TI");
//        
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> new BreadthFirstSearch(gridGraph, sx, sy, ex, ey);
//        runTests("BFS");
//        
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> BreadthFirstSearch.postSmooth(gridGraph, sx, sy, ex, ey);
//        runTests("BFS-PS");
        
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> AStar.postSmooth(gridGraph, sx, sy, ex, ey);
//        runTests("AStar-PS_TI");
        
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> AStar.dijkstra(gridGraph, sx, sy, ex, ey);
//        runTests("Dijkstra");
        
        //algoFunction = (gridGraph, sx, sy, ex, ey) -> new Anya(gridGraph, sx, sy, ex, ey);
        //runTests("Anya");

//        algoFunction = (gridGraph, sx, sy, ex, ey) -> new BasicThetaStar(gridGraph, sx, sy, ex, ey);  
//        runTests("BasicThetaStar_TI");

//        algoFunction = (gridGraph, sx, sy, ex, ey) -> BasicThetaStar.postSmooth(gridGraph, sx, sy, ex, ey);  
//        runTests("BasicThetaStar-PS_TI");
        
//        algoFunction = (gridGraph, sx, sy, ex, ey) -> new VisibilityGraphAlgorithm(gridGraph, sx, sy, ex, ey);
//        runTests("VisibilityGraph");
        
        algoFunction = (gridGraph, sx, sy, ex, ey) -> VisibilityGraphAlgorithm.graphReuse(gridGraph, sx, sy, ex, ey);
        runTests("VisibilityGraph_REUSE");
        
    }
    
    private static void runTests(String algoName) {
        runTest(algoName, 4, PathLengthClass.LONGEST);
//        for (int i=1; i<=8; i++) {
//            runTest(algoName, i, PathLengthClass.ALL);
//        }
    }
    
    private static void runTest(String algoName, int index, PathLengthClass pathLengthClass) {
        String filename = algoName + "_Maze" + index + "_" + pathLengthClass.name();
        System.out.println("RUNNING TEST: " +  filename);
        
        TestDataLibrary library = new TestDataLibrary(index, pathLengthClass);
        GraphInfo graphInfo = library.getGraphInfo();
        GridGraph gridGraph = generateSeededRandomGraph(graphInfo.seed,
                graphInfo.sizeX, graphInfo.sizeY, graphInfo.ratio, 0,0,0,0);

        //FileIO fileIO = new FileIO(PATH_TESTDATA_NAME + filename + ".txt");
        FileIO fileIO = new FileIO(PATH_TESTDATA_NAME + "test.txt");
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