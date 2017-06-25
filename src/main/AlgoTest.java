package main;

import java.util.ArrayList;

import algorithms.AStar;
import algorithms.AStarOctileHeuristic;
import algorithms.AStarStaticMemory;
import algorithms.AcceleratedAStar;
import algorithms.Anya;
import algorithms.anya16.Anya16;
import algorithms.BasicThetaStar;
import algorithms.BreadthFirstSearch;
import algorithms.JumpPointSearch;
import algorithms.LazyThetaStar;
import algorithms.PathFindingAlgorithm;
import algorithms.RecursiveThetaStar;
import algorithms.VisibilityGraphAlgorithm;
import algorithms.datatypes.Point;
import algorithms.sparsevgs.EdgeNLevelSparseVisibilityGraphAlgorithm;
import algorithms.sparsevgs.EdgeNLevelSparseVisibilityGraphAlgorithmFibHeap;
import algorithms.sparsevgs.SparseVisibilityGraphAlgorithm;
import algorithms.sparsevgs.SparseVisibilityGraphAlgorithmFibHeap;
import algorithms.sparsevgs.VisibilityGraphAlgorithmOptimised;
import algorithms.strictthetastar.RecursiveStrictThetaStar;
import algorithms.strictthetastar.StrictThetaStar;
import algorithms.sg16.SG16Algorithm;
import grid.GridGraph;
import main.analysis.MazeAnalysis;
import main.analysis.TwoPoint;
import main.mazes.MazeAndTestCases;
import main.mazes.StoredTestMazes;
import main.testgen.PathLengthClass;
import main.testgen.StandardMazes;
import main.testgen.StartEndPointData;
import main.testgen.TestDataGenerator;
import main.testgen.TestDataLibrary;
import main.utility.TimeCounter;
import main.utility.Utility;
import uiandio.BenchmarkGraphImporter;
import uiandio.BenchmarkGraphSets;
import uiandio.FileIO;
import uiandio.GraphImporter;

public class AlgoTest {
    private static FileIO io;
    private static boolean writeToFile = true;
    private static String outputdir = "testResults/";

    private static void setOutputdir(String postfix) {
        outputdir = "testResults_" + postfix + "/";
    }

    private static boolean pleaseTurnOffAssertions() {
        System.out.println("Please turn off assertions during tests (Run -> Run Configurations -> Arguments -> remove -ea)");
        return false;
    }

    public static void run() {
        assert pleaseTurnOffAssertions();

        FileIO.makeDirs(outputdir);
        System.gc(); System.gc();

        String[] algoNames = new String[]{
            // Define algorithms to test here
            "Anya16",
            "BasicThetaStar",
        };

        String[] mapSetNames = new String[]{
            // Define the map sets to test on here
            "benchmarks",
            "automatadcmazes",
        };
        
        for (int i=0; i<algoNames.length; ++i) {
            for (int j=0; j<mapSetNames.length; ++j) {
                testSequence(getAlgo(algoNames[i]), algoNames[i], mapSetNames[j], "");
            }
        }
    }
    
    public static void runWithArgs(String[] args) {
        assert pleaseTurnOffAssertions();

        // 1. Algorithm Name
        // 2. Map Set
        // 3. Test Type
        // 4. Output Directory name postfix
        String algoName = args[1];
        String mapSetName = args[2];
        String testType = args.length >= 4 ? args[3] : "";
        if (args.length >= 5) setOutputdir(args[4]);

        FileIO.makeDirs(outputdir);
        System.gc(); System.gc();

        AlgoFunction algo = getAlgo(algoName);
        testSequence(algo, algoName, mapSetName, testType);
    }

    public static AlgoFunction getAlgo(String algoName) {

        switch (algoName) {
            case "AStar": return AStar::new;
            case "AStarSLD": return AStarStaticMemory::new;
            case "AStarPS": return AStar::postSmooth;
            case "AStar Octile": return AStarOctileHeuristic::new;
            case "AStarOctile PostSmooth": return AStarOctileHeuristic::postSmooth;
            case "BreadthFirstSearch": return BreadthFirstSearch::new;
            case "JumpPointSearch": return JumpPointSearch::new;
            case "JPS PostSmooth": return JumpPointSearch::postSmooth;
            case "LazyThetaStar": return LazyThetaStar::new;
            case "BasicThetaStar": return BasicThetaStar::new;
            case "BasicThetaStar_PS": return BasicThetaStar::postSmooth;
            case "Dijkstra": return AStar::dijkstra;
            case "AcceleratedAStar": return AcceleratedAStar::new;
            case "Anya": return Anya::new;
            case "Anya16": return Anya16::new;

            case "VisibilityGraphs": return VisibilityGraphAlgorithm::new;
            case "VisibilityGraphReuse": return VisibilityGraphAlgorithm::graphReuse;
            case "VisibilityGraphReuseOptimised": return VisibilityGraphAlgorithmOptimised::graphReuse;

            case "StrictThetaStar": return StrictThetaStar::new;
            case "StrictThetaStarPS": return StrictThetaStar::postSmooth;
            case "RecStrictThetaStar": return RecursiveStrictThetaStar::new;
            case "RecStrictThetaStarPS": return RecursiveStrictThetaStar::postSmooth;
            case "RecStrictThetaStar_2": return (a, b, c, d, e) -> RecursiveStrictThetaStar.depthLimit(a, b, c, d, e, 2);
            case "RecursiveThetaStar": return RecursiveThetaStar::new;

            case "SparseVisibilityGraphs": return SparseVisibilityGraphAlgorithm::graphReuse;
            case "SparseVisibilityGraphsFibHeap": return SparseVisibilityGraphAlgorithmFibHeap::graphReuse;
            case "EdgeNLevelSparseVisibilityGraphs": return EdgeNLevelSparseVisibilityGraphAlgorithm::graphReuse;
            case "EdgeNLevelSparseVisibilityGraphsFibHeap": return EdgeNLevelSparseVisibilityGraphAlgorithmFibHeap::graphReuse;
            case "Edge1LevelSparseVisibilityGraphs": return EdgeNLevelSparseVisibilityGraphAlgorithm.withLevelLimit(1);
            case "Edge2LevelSparseVisibilityGraphs": return EdgeNLevelSparseVisibilityGraphAlgorithm.withLevelLimit(2);
            case "Edge3LevelSparseVisibilityGraphs": return EdgeNLevelSparseVisibilityGraphAlgorithm.withLevelLimit(3);

            case "SG16A": return SG16Algorithm::new;
        }

        throw new UnsupportedOperationException("Invalid Algorithm! " + algoName);
    }


    public static void testSequence(AlgoFunction algo, String name, String mapSetName, String testType) {
        String path = outputdir + name.replace(" ", "_") + ".txt";
        if (writeToFile) io = new FileIO(path);

        boolean pathLengthOnly = false;
        boolean runningTimeOnly = false;
        boolean initialisationTime = false;

        // TestFunctionData testFunction_slow = printAverageData(50, 5);
        TestFunctionData testFunction_single = printAverageData(1,1);
        TestFunctionData testFunction_slow = printAverageData(5, 5);
        TestFunctionData testFunction_fast = printAverageData(50, 30);
        switch (testType) {
            case "pathLengthOnly":
                testFunction_single = testPathLengthOnly;
                testFunction_slow = testPathLengthOnly;
                testFunction_fast = testPathLengthOnly;
                break;
            case "individualPathLengthOnly":
                testFunction_single = analyseIndividualPaths;
                testFunction_slow = analyseIndividualPaths;
                testFunction_fast = analyseIndividualPaths;
                break;
            case "runningTimeOnly":
                testFunction_single = testIndividualRunningTimes(1, 1);
                testFunction_slow = testIndividualRunningTimes(20, 1);
                testFunction_fast = testIndividualRunningTimes(50, 5);
                break;
            case "initialisationTime":
                testFunction_single = testInitialisationTime;
                testFunction_slow = testInitialisationTime;
                testFunction_fast = testInitialisationTime;
                break;
            case "testing":
                testFunction_single = (a,b,c,d) -> {};
                testFunction_slow = (a,b,c,d) -> {};
                testFunction_fast = (a,b,c,d) -> {};
                break;
            case "":
            case "default":
                break;
            default:
                throw new UnsupportedOperationException("Invalid Test Type");
        }


        println("=== Testing " + name + " === " + mapSetName + " ===");
        
        switch (mapSetName) {
        case "gamemaps": {
            testOnMazeData("sc2_steppesofwar", algo, testFunction_slow);
            testOnMazeData("sc2_losttemple", algo, testFunction_slow);
            testOnMazeData("sc2_extinction", algo, testFunction_slow);
            
            testOnMazeData("baldursgate_AR0070SR", algo, testFunction_slow);
            testOnMazeData("baldursgate_AR0705SR", algo, testFunction_slow);
            testOnMazeData("baldursgate_AR0418SR", algo, testFunction_slow);
            
            testOnMazeData("sc1_TheFrozenSea", algo, testFunction_slow);
            testOnMazeData("sc1_SpaceDebris", algo, testFunction_slow);
            
            testOnMazeData("wc3_icecrown", algo, testFunction_slow);
            testOnMazeData("wc3_dragonfire", algo, testFunction_slow);
            
            testOnMazeData("sc1_Legacy", algo, testFunction_slow);
            
            testOnMazeData("sc1_GreenerPastures", algo, testFunction_slow);
            
            testOnMazeData("baldursgate_AR0603SR", algo, testFunction_slow);
            
            testOnMazeData("wc3_mysticisles", algo, testFunction_slow);
            
            testOnMazeData("wc3_petrifiedforest", algo, testFunction_slow);
            break;
        }

        case "generatedmaps": {
            // Low Density - 6% - 50x50
            testOnMazeData("def_iCUZANYD_iSB_iSB_iSB", algo, testFunction_slow);
            // Medium Density - 20% - 50x50
            testOnMazeData("def_i10VA3PD_iSB_iSB_iP", algo, testFunction_slow);
            // High Density - 40% - 50x50
            testOnMazeData("def_i3ML5FBD_iSB_iSB_iH", algo, testFunction_slow);
           
            // Low Density - 6% - 300x300
            testOnMazeData("def_iHHLNUOB_iMJ_iMJ_iSB", algo, testFunction_slow);
            // Medium Density - 20% - 300x300
            testOnMazeData("def_iZLPIX5B_iMJ_iMJ_iP", algo, testFunction_slow);
            // High Density - 40% - 300x300
            testOnMazeData("def_iVVJKDR_iMJ_iMJ_iH", algo, testFunction_slow);
           
            // Low Density - 6% - 500x500
            testOnMazeData("def_iIRXXUKC_iUP_iUP_iSB", algo, testFunction_slow);
            // Medium Density - 20% - 500x500
            testOnMazeData("def_iOMJ14Z_iUP_iUP_iP", algo, testFunction_slow);
           
            // LowDensity2 - 6% - 300x300
            testOnMazeData("def_iMJWB0QC_iMJ_iMJ_iSB", algo, testFunction_slow);
            // MediumDensity2 - 20% - 300x300
            testOnMazeData("def_iBCA5SS_iMJ_iMJ_iP", algo, testFunction_slow);
            // HighDensity2 - 40% - 300x300
            testOnMazeData("def_i11AHREB_iMJ_iMJ_iH", algo, testFunction_slow);
           
            // LowDensity3 - 6% - 300x300
            testOnMazeData("def_iOH1TZ0D_iMJ_iMJ_iSB", algo, testFunction_slow);
            // MediumDensity3 - 20% - 300x300
            testOnMazeData("def_iAN3IE0C_iMJ_iMJ_iP", algo, testFunction_slow);
            // HighDensity3 - 40% - 300x300
            testOnMazeData("def_iI0RFKYC_iMJ_iMJ_iH", algo, testFunction_slow);
           
            // 6%Density - 500x500
            testOnMazeData("def_iIRXXUKC_iUP_iUP_iSB", algo, testFunction_slow);
            // 20%Density - 500x500
            testOnMazeData("def_iOMJ14Z_iUP_iUP_iP", algo, testFunction_slow);
            // 40%Density - 500x500
            testOnMazeData("def_iREPZHKB_iUP_iUP_iH", algo, testFunction_slow);
           
            // 6%Density2 - 500x500
            testOnMazeData("def_i5QEPQ2C_iUP_iUP_iSB", algo, testFunction_slow);
            // 20%Density2 - 500x500
            testOnMazeData("def_iKMRKFCD_iUP_iUP_iP", algo, testFunction_slow);
            // 40%Density2 - 500x500
            testOnMazeData("def_i5GM4YH_iUP_iUP_iH", algo, testFunction_slow);
           
            // 6%Density3 - 500x500
            testOnMazeData("def_iFOLAODC_iUP_iUP_iSB", algo, testFunction_slow);
            // 20%Density3 - 500x500
            testOnMazeData("def_i5GZXLUD_iUP_iUP_iP", algo, testFunction_slow);
            // 40%Density3 - 500x500
            testOnMazeData("def_iA0VKRW_iUP_iUP_iH", algo, testFunction_slow);
            testOnMazeData("gen_1000x1000_50_iY5U5GAC", algo, testFunction_slow);
            testOnMazeData("gen_1000x1000_15_iFTBETRD", algo, testFunction_slow);
            testOnMazeData("gen_1000x1000_7_i3WH4IJD", algo, testFunction_slow);
           
            testOnMazeData("gen_2000x2000_50_iK54OY1C", algo, testFunction_slow);
            testOnMazeData("gen_2000x2000_15_i4Z44NPC", algo, testFunction_slow);
            testOnMazeData("gen_2000x2000_7_iXT3AEED", algo, testFunction_slow);
           
            testOnMazeData("gen_3000x3000_50_iUE2IRAD", algo, testFunction_slow);
            testOnMazeData("gen_3000x3000_15_iGUM1R2C", algo, testFunction_slow);
            testOnMazeData("gen_3000x3000_7_iSR3L1J", algo, testFunction_slow);
           
            testOnMazeData("gen_4000x4000_50_i0GHV1UD", algo, testFunction_slow);
            testOnMazeData("gen_4000x4000_15_iNK5KHO", algo, testFunction_slow);
            testOnMazeData("gen_4000x4000_7_iNUJNZ3", algo, testFunction_slow);
           
            testOnMazeData("gen_5000x5000_50_iCFL2G3B", algo, testFunction_slow);
            testOnMazeData("gen_5000x5000_15_i0BTXUD", algo, testFunction_slow);
            testOnMazeData("gen_5000x5000_7_iHOPN1S", algo, testFunction_slow);
           
            testOnMazeData("obst10_random512-10-7", algo, testFunction_slow);
            testOnMazeData("obst40_random512-40-0", algo, testFunction_slow);
            testOnMazeData("obst40_random512-40-7", algo, testFunction_slow);
           
            testOnMazeData("corr2_maze512-2-5", algo, testFunction_slow);
            testOnMazeData("corr2_maze512-2-1", algo, testFunction_slow);
            testOnMazeData("corr2_maze512-2-7", algo, testFunction_slow);
            testOnMazeData("corr2_maze512-2-3", algo, testFunction_slow);
            testOnMazeData("corr2_maze512-2-9", algo, testFunction_slow);
            break;
        }
        

        case "benchmarks": {
            testOnBenchmarkMapSet("bg512", algo, testFunction_single);
            // testOnBenchmarkMapSet("dao", algo, testFunction_single);
            testOnBenchmarkMapSet("sc1", algo, testFunction_single);
            testOnBenchmarkMapSet("wc3maps512", algo, testFunction_single);
            break;
        }

        case "benchmarksrandom": {
            testOnBenchmarkMapSet("random10", algo, testFunction_single);
            testOnBenchmarkMapSet("random20", algo, testFunction_single);
            testOnBenchmarkMapSet("random30", algo, testFunction_single);
            testOnBenchmarkMapSet("random40", algo, testFunction_single);
            break;
        }
        

        case "automataoriginal": {
            for (int resolutionIndex=0; resolutionIndex<10; resolutionIndex++) {
                for (int scaleIndex=0; scaleIndex<7; ++scaleIndex) {
                    testOnStoredMaze(StoredTestMazes.loadAutomataMaze(scaleIndex, resolutionIndex), algo, testFunction_slow);
                    System.gc();System.gc();
                    try {Thread.sleep(2000);}
                    catch (Exception e) {throw new UnsupportedOperationException(e.getMessage());}
                    System.gc();System.gc();
                }
            }
            break;
        }
        

        case "scaledmazes": {
            testScaledMazes("sc1_NovaStation", algo, testFunction_slow);
            testScaledMazes("wc3_darkforest", algo, testFunction_slow);
            testScaledMazes("sc1_RedCanyons", algo, testFunction_slow);
            testScaledMazes("wc3_swampofsorrows", algo, testFunction_slow);
            testScaledMazes("sc1_Triskelion", algo, testFunction_slow);
            testScaledMazes("wc3_theglaive", algo, testFunction_slow);
            break;
        }
        

        case "tiledmazes": {
            for (int mazePoolIndex=0;mazePoolIndex<4;++mazePoolIndex) {
                for (int size = 4; size <= 12; size+=4) {
                    testOnStoredMaze(StoredTestMazes.loadTiledMaze(mazePoolIndex, size), algo, testFunction_slow);
                    System.gc();System.gc();
                    try {Thread.sleep(2000);}
                    catch (Exception e) {throw new UnsupportedOperationException(e.getMessage());}
                    System.gc();System.gc();
                }
            }
            break;
        }


        case "automatadcmazes": {
            for (int percentBlockedIndex=0; percentBlockedIndex<2; ++percentBlockedIndex) {
                for (int resolutionIndex=0; resolutionIndex<5; resolutionIndex+=2) {
                    for (int sizeIndex=0; sizeIndex<5; sizeIndex+=2) {
                        testOnStoredMaze(StoredTestMazes.loadAutomataDCMaze(sizeIndex, resolutionIndex, percentBlockedIndex), algo, testFunction_single);
                        System.gc();
                        try {Thread.sleep(2000);}
                        catch (Exception e) {throw new UnsupportedOperationException(e.getMessage());}
                        System.gc();
                        try {Thread.sleep(2000);}
                        catch (Exception e) {throw new UnsupportedOperationException(e.getMessage());}
                        System.gc();
                        try {Thread.sleep(2000);}
                        catch (Exception e) {throw new UnsupportedOperationException(e.getMessage());}
                        System.gc();
                    }
                }
            }
            break;
        }


        case "mazemaps": {
            int[] connectednessRatioIndexes = new int[] {0,1,2};
            for (int sizeIndex=0; sizeIndex<7; sizeIndex+=2) {
                int corridorWidthIndex = 1;
                for (int j=0; j<connectednessRatioIndexes.length; ++j) {
                    int connectednessRatioIndex = connectednessRatioIndexes[j];
                    testOnStoredMaze(StoredTestMazes.loadMazeMaze(sizeIndex, corridorWidthIndex, connectednessRatioIndex), algo, testFunction_slow);
                    System.gc();System.gc();
                    try {Thread.sleep(2000);}
                    catch (Exception e) {throw new UnsupportedOperationException(e.getMessage());}
                    System.gc();System.gc();
                }
            }
            break;
        }

        default:
            throw new UnsupportedOperationException("Invalid Map Set Name!");

        }

        println("=== FINISHED TEST FOR " + name + " === " + mapSetName + " ===");
        println();

        if (writeToFile) io.close();
    }
    
    public static void testOnBenchmarkMapSet(String setName, AlgoFunction algo, TestFunctionData testFunction) {
        String[] mapNames = BenchmarkGraphSets.getBenchmarkSet(setName);
        for (int i=0; i<mapNames.length;++i) {
            Utility.cleanUpPreallocatedMemory();
            testOnBenchmarkMaze(mapNames[i], algo, testFunction);
            Utility.cleanUpPreallocatedMemory();
            System.gc();
            try {Thread.sleep(2000);}
            catch (Exception e) {throw new UnsupportedOperationException(e.getMessage());}
            System.gc();
        }
    }

    public static void testScaledMazes(String mazeName, AlgoFunction algo, TestFunctionData testFunction) {
        for (int scale = 4; scale <= 12; scale += 4) {
            testOnStoredMaze(StoredTestMazes.loadScaledMaze(mazeName,scale), algo, testFunction);
            System.gc();System.gc();
            try {Thread.sleep(2000);}
            catch (Exception e) {throw new UnsupportedOperationException(e.getMessage());}
            System.gc();System.gc();
        }
    }

    public static void printMazeDetails(String mazeName, GridGraph gridGraph) {
        MazeAnalysis.Options options = new MazeAnalysis.Options();
        
        options.sizeX = true;
        options.sizeY = true;
        options.averageBlockedIslandSize = true;
        options.averageFloatingBlockedIslandSize = true;
        options.blockDensity = true;
        options.averageOpenSpaceSize = true;
        options.largestRatioToSecond = true;
        options.largestRatioToRemaining = true;
        
        MazeAnalysis an = new MazeAnalysis(gridGraph, options);
        
        StringBuilder sb = new StringBuilder();
        sb.append(mazeName);
        sb.append(" - ").append(an.sizeX);
        sb.append(" - ").append(an.sizeY);
        sb.append(" - ").append(an.blockDensity);
        sb.append(" - ").append(an.averageBlockedIslandSize);
        sb.append(" - ").append(an.averageFloatingBlockedIslandSize);
        sb.append(" - ").append(an.averageOpenSpaceSize);
        sb.append(" - ").append(an.largestRatioToSecond);
        sb.append(" - ").append(an.largestRatioToRemaining);
        
        println(sb.toString());
    }

    private static void println(Object line) {
        if (writeToFile) {
            io.writeLine(line.toString());
            io.flush();
            System.out.println(line);
        } else {
            System.out.println(line);
        }
    }

    private static void println() {
        println("");
    }

    public static ArrayList<TwoPoint> toTwoPointlist(int... points) {
        return TestDataGenerator.generateTwoPointList(points);
    }

    public static void testOnMaze(String mazeName, AlgoFunction algoFunction, TestFunction test) {
        ArrayList<TwoPoint> problems = GraphImporter.loadStoredMazeProblems(mazeName);
        testOnMaze(mazeName, problems, algoFunction, test);
    }

    public static void testOnMaze(String mazeName, ArrayList<TwoPoint> problems, AlgoFunction algoFunction,
            TestFunction test) {
        GridGraph gridGraph = GraphImporter.loadStoredMaze(mazeName);
        test.test(mazeName, gridGraph, problems, algoFunction);
    }

    public static void testOnGraph(GridGraph gridGraph, ArrayList<TwoPoint> problems, AlgoFunction algoFunction,
            TestFunction test) {
        test.test("undefined", gridGraph, problems, algoFunction);
    }

    public static void testOnBenchmarkMaze(String mazeName, AlgoFunction algoFunction, TestFunctionData test) {
        Utility.cleanUpPreallocatedMemory();
        {
            ArrayList<StartEndPointData> problems = BenchmarkGraphImporter.loadBenchmarkMazeProblems(mazeName);
            GridGraph gridGraph = BenchmarkGraphImporter.loadBenchmarkMaze(mazeName);

            printMazeDetails(mazeName, gridGraph);
            
            test.test(mazeName, gridGraph, problems, algoFunction);
        }
        Utility.cleanUpPreallocatedMemory();
    }

    public static void testOnStoredMaze(MazeAndTestCases mazeAndTestCases, AlgoFunction algoFunction, TestFunctionData test) {
        Utility.cleanUpPreallocatedMemory();
        {
            ArrayList<StartEndPointData> problems = mazeAndTestCases.problems;
            GridGraph gridGraph = mazeAndTestCases.gridGraph;
            String mazeName = mazeAndTestCases.mazeName;

            printMazeDetails(mazeName, gridGraph);
            
            test.test(mazeName, gridGraph, problems, algoFunction);
            mazeAndTestCases = null;
        }
        Utility.cleanUpPreallocatedMemory();
    }

    public static void testOnMazeData(String mazeName, AlgoFunction algoFunction, TestFunctionData test) {
        ArrayList<StartEndPointData> problems = GraphImporter.loadStoredMazeProblemData(mazeName);
        testOnMazeData(mazeName, problems, algoFunction, test);

        Utility.cleanUpPreallocatedMemory();
    }

    public static void testOnMazeData(String mazeName, ArrayList<StartEndPointData> problems, AlgoFunction algoFunction, TestFunctionData test) {
        GridGraph gridGraph = GraphImporter.loadStoredMaze(mazeName);
        printMazeDetails(mazeName, gridGraph);
        test.test(mazeName, gridGraph, problems, algoFunction);
    }

    public static void testOnGraphData(GridGraph gridGraph, ArrayList<StartEndPointData> problems, AlgoFunction algoFunction, TestFunctionData test) {
        test.test("undefined", gridGraph, problems, algoFunction);
    }

    private static final TestFunctionData testPathLengthOnly = (mazeName, gridGraph, problems, algoFunction) -> {

        TestResult[] testResults = new TestResult[problems.size()];
        int index = 0;
        double totalPathLength = 0;
        int totalTautPaths = 0;
        int totalOptimalPaths = 0;

        TimeCounter.reset();
        for (StartEndPointData problem : problems) {
            TwoPoint tp = new TwoPoint(problem.start, problem.end);
            testResults[index] = testAlgorithmPathLength(gridGraph, algoFunction, tp);
            totalPathLength += testResults[index].pathLength / problem.shortestPath;
            totalTautPaths += (testResults[index].isTaut ? 1 : 0);
            totalOptimalPaths += (Utility.isOptimal(testResults[index].pathLength, problem.shortestPath) ? 1 : 0);
            index++;
            TimeCounter.iterations++;
        }
        int nResults = testResults.length;
        TimeCounter.printAverage();

        println("Average Path Length: " + (totalPathLength / nResults));
        println("Percentage Taut: " + (totalTautPaths / (float) nResults));
        println("Percentage Optimal: " + (totalOptimalPaths / (float) nResults));
    };

    private static final TestFunctionData analyseIndividualPaths = (mazeName, gridGraph, problems, algoFunction) -> {
        println("|||||mazeName,start,end,optimalLength,actualLength,isTaut,isOptimal");
        for (StartEndPointData problem : problems) {
            TwoPoint tp = new TwoPoint(problem.start, problem.end);
            TestResult testResult = testAlgorithmPathLength(gridGraph, algoFunction, tp);

            // double pathLengthRatio = testResult.pathLength / problem.shortestPath;
            int isTaut = (testResult.isTaut ? 1 : 0);
            int isOptimal = (Utility.isOptimal(testResult.pathLength, problem.shortestPath) ? 1 : 0);

            StringBuilder sb = new StringBuilder(">>>>>");
            sb.append(mazeName).append(",");
            sb.append(problem.start).append(",");
            sb.append(problem.end).append(",");
            sb.append(problem.shortestPath).append(",");
            sb.append(testResult.pathLength).append(",");
            sb.append(isTaut).append(",");
            sb.append(isOptimal);
            println(sb.toString());
        }
    };

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
        int totalTautPaths = 0;
        for (TestResult testResult : testResults) {
            totalMean += testResult.time;
            totalSD += testResult.timeSD;
            totalPathLength += testResult.pathLength;
            totalTautPaths += (testResult.isTaut ? 1 : 0);
        }
        int nResults = testResults.length;

        println("Sample Size: " + sampleSize + " x " + nTrials + " trials");
        println("Average Time: " + (totalMean / nResults));
        println("Average SD: " + (totalSD / nResults));
        println("Average Path Length: " + (totalPathLength / nResults));
        println("Percentage Taut: " + (totalTautPaths / (float) nResults));
    };

    private static final TestFunctionData printAverageData(int sampleSize, int nTrials) {
        return (mazeName, gridGraph, problems, algoFunction) -> {

            double sum = 0;
            double sumSquare = 0;
            double totalPathLength = 0;
            int totalTautPaths = 0;
            int totalOptimalPaths = 0;

            int nResults = 0;

            TimeCounter.reset();
            for (StartEndPointData problem : problems) {
                TwoPoint tp = new TwoPoint(problem.start, problem.end);
                TestResult testResult = testAlgorithm(gridGraph, algoFunction, tp, sampleSize, nTrials);

                sum += testResult.time;
                sumSquare += testResult.time * testResult.time;
                totalPathLength += testResult.pathLength / problem.shortestPath;
                totalTautPaths += (testResult.isTaut ? 1 : 0);
                totalOptimalPaths += (Utility.isOptimal(testResult.pathLength, problem.shortestPath) ? 1 : 0);

                nResults++;
                TimeCounter.iterations += nTrials * sampleSize;
            }
            //TimeCounter.printAverage();
            println(TimeCounter.getPrintAverageString());

            double mean = (double) sum / nResults;
            double secondMomentTimesN = (double) sumSquare;
            double sampleVariance = (secondMomentTimesN - nResults * (mean * mean)) / (nResults - 1);
            double standardDeviation = Math.sqrt(sampleVariance);

            println("Sample Size: " + sampleSize + " x " + nTrials + " trials");
            println("Average Time: " + mean);
            println("Standard Dev: " + standardDeviation);
            println("Average Path Length: " + (totalPathLength / nResults));
            println("Percentage Taut: " + (totalTautPaths / (float) nResults));
            println("Percentage Optimal: " + (totalOptimalPaths / (float) nResults));
            // StrictVisibilityGraphAlgorithmV2.printTimes();a=true;
            println();
        };
    }

    private static final TestFunctionData testIndividualRunningTimes(int sampleSize, int nTrials) {
        return (mazeName, gridGraph, problems, algoFunction) -> {

            ArrayList<TwoPoint> twoPointList = new ArrayList<>();
            ArrayList<long[]> runningTimesList = new ArrayList<>();

            TimeCounter.reset();
            for (StartEndPointData problem : problems) {
                TwoPoint tp = new TwoPoint(problem.start, problem.end);
                long[] runningTimes = new long[sampleSize];

                TimeCounter.freeze();
                // Do two blank runs first to increase consistency of results.
                testAlgorithmTimeOnce(gridGraph, algoFunction, tp, 2);
                TimeCounter.unfreeze();

                for (int i = 0; i < sampleSize; ++i) {
                    runningTimes[i] = testAlgorithmTimeOnce(gridGraph, algoFunction, tp, nTrials);
                }

                twoPointList.add(tp);
                runningTimesList.add(runningTimes);
            }
            TimeCounter.print();

            println("Maze Name: " + mazeName);
            println("Sample Size: " + sampleSize + " x " + nTrials + " trials");
            for (int i = 0; i < twoPointList.size(); ++i) {
                TwoPoint tp = twoPointList.get(i);
                long[] runningTimes = runningTimesList.get(i);

                StringBuilder sb = new StringBuilder();
                sb.append(tp.p1.x + "-" + tp.p1.y + "_" + tp.p2.x + "-" + tp.p2.y);
                for (long runningTime : runningTimes) {
                    sb.append(" ");
                    sb.append(runningTime);
                }
                println(sb);
            }
            println();
        };
    }

    private static final TestFunctionData testInitialisationTime = (mazeName, gridGraph, problems, algoFunction) -> {
        StartEndPointData problem = problems.get(0);
        Point p1 = problem.start;
        Point p2 = problem.end;
        TimeCounter.reset();
        long startTime = System.nanoTime();
        Utility.generatePath(algoFunction, gridGraph, p1.x, p1.y, p2.x, p2.y);
        long endTime = System.nanoTime();
        double timeTaken = (endTime - startTime) / 1000000.0;
        TimeCounter.printAverage();

        println("Initialisation Time: " + timeTaken);
        println();
    };

    private static long testAlgorithmTimeOnce(GridGraph gridGraph, AlgoFunction algoFunction, TwoPoint tp,
            int nTrials) {

        int startX = tp.p1.x;
        int startY = tp.p1.y;
        int endX = tp.p2.x;
        int endY = tp.p2.y;

        long start = System.nanoTime();
        for (int i = 0; i < nTrials; i++) {
            AlgoTest.testAlgorithmSpeed(algoFunction, gridGraph, startX, startY, endX, endY);
        }
        long end = System.nanoTime();

        long timeTakenNanosecs = end - start;
        // System.out.println(timeTakenNanosecs);
        return timeTakenNanosecs;
    }

    private static TestResult testAlgorithmTime(GridGraph gridGraph, AlgoFunction algoFunction, TwoPoint tp,
            int sampleSize, int nTrials) {

        double[] data = new double[sampleSize];

        double sum = 0;
        double sumSquare = 0;

        int startX = tp.p1.x;
        int startY = tp.p1.y;
        int endX = tp.p2.x;
        int endY = tp.p2.y;

        for (int s = 0; s < sampleSize; s++) {
            long start = System.nanoTime();
            for (int i = 0; i < nTrials; i++) {
                AlgoTest.testAlgorithmSpeed(algoFunction, gridGraph, startX, startY, endX, endY);
            }
            long end = System.nanoTime();
            // System.gc();

            data[s] = (double) (end - start) / 1000000.;

            sum += data[s];
            sumSquare += data[s] * data[s];
        }

        double mean = (double) sum / nTrials / sampleSize;
        double secondMomentTimesN = (double) sumSquare / nTrials / nTrials;
        double sampleVariance = (secondMomentTimesN - sampleSize * (mean * mean)) / (sampleSize - 1);
        double standardDeviation = Math.sqrt(sampleVariance);

        TestResult testResult = new TestResult(sampleSize, mean, standardDeviation, -1f, false);
        return testResult;
    }

    private static TestResult testAlgorithmPathLength(GridGraph gridGraph, AlgoFunction algoFunction, TwoPoint tp) {

        int[][] path = Utility.generatePath(algoFunction, gridGraph, tp.p1.x, tp.p1.y, tp.p2.x, tp.p2.y);
        double pathLength = Utility.computePathLength(gridGraph, path);
        boolean isTaut = Utility.isPathTaut(gridGraph, path);

        TestResult testResult = new TestResult(-1, -1, -1, pathLength, isTaut);
        return testResult;
    }

    private static TestResult testAlgorithm(GridGraph gridGraph, AlgoFunction algoFunction, TwoPoint tp, int sampleSize,
            int nTrials) {
        TimeCounter.freeze();
        TestResult pathLength = testAlgorithmPathLength(gridGraph, algoFunction, tp);
        TimeCounter.unfreeze();
        TestResult time = testAlgorithmTime(gridGraph, algoFunction, tp, sampleSize, nTrials);
        return new TestResult(time.timesRan, time.time, time.timeSD, pathLength.pathLength, pathLength.isTaut);
    }

    /**
     * Run tests for all the algorithms, and outputs them to the file.<br>
     * Please look into this method for more information.<br>
     * Comment out the algorithms you don't want to test. EDIT: No longer used.
     * Replaced by run() as the main testing method.
     */
    @Deprecated
    private static void runTestAllAlgos() {
        /*
         * AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new
         * AStar(gridGraph, sx, sy, ex, ey); runTests("AStar_TI");
         * 
         * AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new
         * BreadthFirstSearch(gridGraph, sx, sy, ex, ey); runTests("BFS");
         * 
         * AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) ->
         * BreadthFirstSearch.postSmooth(gridGraph, sx, sy, ex, ey);
         * runTests("BFS-PS");
         * 
         * AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) ->
         * AStar.postSmooth(gridGraph, sx, sy, ex, ey); runTests("AStar-PS_TI");
         * 
         * AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) ->
         * AStar.dijkstra(gridGraph, sx, sy, ex, ey); runTests("Dijkstra");
         * 
         * // Warning: Anya is unstable AnyAnglePathfinding.algoFunction =
         * (gridGraph, sx, sy, ex, ey) -> new Anya(gridGraph, sx, sy, ex, ey);
         * runTests("Anya");
         * 
         * AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new
         * BasicThetaStar(gridGraph, sx, sy, ex, ey);
         * runTests("BasicThetaStar_TI");
         * 
         * AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) ->
         * BasicThetaStar.postSmooth(gridGraph, sx, sy, ex, ey);
         * runTests("BasicThetaStar-PS_TI");
         * 
         * Warning: VisibilityGraph is slow AnyAnglePathfinding.algoFunction =
         * (gridGraph, sx, sy, ex, ey) -> new
         * VisibilityGraphAlgorithm(gridGraph, sx, sy, ex, ey);
         * runTests("VisibilityGraph");
         * 
         * AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) ->
         * VisibilityGraphAlgorithm.graphReuse(gridGraph, sx, sy, ex, ey);
         * runTests("VisibilityGraph_REUSE");
         * 
         * AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) ->
         * VisibilityGraphAlgorithm.graphReuseSlowDijkstra(gridGraph, sx, sy,
         * ex, ey); runTests("VisibilityGraph_REUSE_SLOWDIJKSTRA");
         * 
         * AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) -> new
         * StrictThetaStar(gridGraph, sx, sy, ex, ey);
         * runTests("StrictThetaStar_5b");
         * 
         * AnyAnglePathfinding.algoFunction = (gridGraph, sx, sy, ex, ey) ->
         * StrictThetaStar.noHeuristic(gridGraph, sx, sy, ex, ey);
         * runTests("StrictThetaStar_NoHeuristic_5");
         */
    }

    /**
     * Runs tests for the specified algorithm. Uses the runTest method, which
     * will output the test results to the file. EDIT: No longer used. Replaced
     * by run() as the main testing method.
     * 
     * @param algoName
     *            The name of the algorithm to be used in the file.
     */
    @Deprecated
    private static void runTests(AlgoFunction algo, String algoName) {
        // runTest(algoName, 4, PathLengthClass.LONGEST);
        for (int i = 1; i <= 6; i++) {
            AlgoTest.runTest(algo, algoName, i, PathLengthClass.ALL);
        }
    }

    /**
     * Runs a test stated in the TestDataLibrary, and outputs the test result to
     * a file.
     * 
     * @param algoName
     *            The name of the algorithm to be used in the file.
     * @param index
     *            The index of the test to use (which maze)
     * @param pathLengthClass
     *            The category of path lengths to use in the test. Refer to
     *            TestDataLibrary for more information. EDIT: No longer used.
     *            Replaced by run() as the main testing method.
     */
    @Deprecated
    private static void runTest(AlgoFunction algo, String algoName, int index, PathLengthClass pathLengthClass) {
        String filename = algoName + "_Maze" + index + "_" + pathLengthClass.name();
        System.out.println("RUNNING TEST: " + filename);

        TestDataLibrary library = new StandardMazes(index, pathLengthClass);
        GridGraph gridGraph = library.generateGraph();

        System.out.println("Percentage Blocked:" + gridGraph.getPercentageBlocked());

        FileIO fileIO = new FileIO(AnyAnglePathfinding.PATH_TESTDATA + filename + ".txt");
        fileIO.writeRow("Algorithm", "Maze", "ComputedPath", "OptimalPath", "PathLengthRatio", "Time", "TimeSD",
                "Start", "End", "Trails");

        TestResult tempRes = AlgoTest.testAlgorithm(algo, gridGraph, 0, 0, 1, 0, 1, 1);
        System.out.println("Preprocess time: " + tempRes.time);

        while (library.hasNextData()) {
            StartEndPointData data = library.getNextData();

            TestResult testResult = AlgoTest.testAlgorithm(algo, gridGraph, data.start.x, data.start.y, data.end.x,
                    data.end.y, 10, library.getNTrials());

            boolean valid = (testResult.pathLength > 0.00001f);
            double ratio = testResult.pathLength / data.shortestPath;

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

            fileIO.writeRow(algorithm, maze, pathLength, shortestPathLength, pathLengthRatio, time, timeSD, start, end,
                    nTrials);
            fileIO.flush();
        }

        fileIO.close();
    }

    /**
     * Conducts a running time / path length test on the current algorithm on a
     * specified gridGraph. The algorithm used is the algorithm stored in
     * algoFunction.
     * 
     * @param gridGraph
     *            the grid to run the algorithm on.
     * @param startX
     *            x-coordinate of start point
     * @param startY
     *            y-coordinate of start point
     * @param endX
     *            x-coordinate of goal point
     * @param endY
     *            y-coordinate of goal point
     * @param sampleSize
     *            The size of the sample to use. Used in computing Standard
     *            Deviation.
     * @param nTrials
     *            The number of times the algorithm is run per test. For
     *            example, if sampleSize is 10 and nTrials is 50, the algorithm
     *            is run 50 times for each of the 10 tests to get 10 results.
     *            This is used for test cases which possbly last shorter than a
     *            millisecond, making it hard to record the running time of we
     *            only ran the algorithm once per record.
     * @return
     */
    private static TestResult testAlgorithm(AlgoFunction algoFunction, GridGraph gridGraph, int startX, int startY,
            int endX, int endY, int sampleSize, int nTrials) {

        int[] data = new int[sampleSize];

        int sum = 0;
        long sumSquare = 0;

        // sampleSize = 0;// UNCOMMENT TO TEST PATH LENGTH ONLY
        for (int s = 0; s < sampleSize; s++) {
            long start = System.nanoTime();
            for (int i = 0; i < nTrials; i++) {
                AlgoTest.testAlgorithmSpeed(algoFunction, gridGraph, startX, startY, endX, endY);
            }
            long end = System.nanoTime();
            System.gc();

            data[s] = (int) (end - start);

            sum += data[s];
            sumSquare += data[s] * data[s];
        }

        double mean = (double) sum / nTrials / sampleSize;
        double secondMomentTimesN = (double) sumSquare / nTrials / nTrials;
        double sampleVariance = (secondMomentTimesN - sampleSize * (mean * mean)) / (sampleSize - 1);
        double standardDeviation = Math.sqrt(sampleVariance);

        int[][] path = Utility.generatePath(algoFunction, gridGraph, startX, startY, endX, endY);
        double pathLength = Utility.computePathLength(gridGraph, path);
        boolean isTaut = Utility.isPathTaut(gridGraph, path);

        TestResult testResult = new TestResult(sampleSize, mean, standardDeviation, pathLength, isTaut);
        return testResult;
    }

    /**
     * Tells the algorithm to compute a path. returns nothing. Used to test how
     * long the algorithm takes to complete the computation.
     */
    private static void testAlgorithmSpeed(AlgoFunction algoFunction, GridGraph gridGraph, int sx, int sy, int ex,
            int ey) {
        PathFindingAlgorithm algo = algoFunction.getAlgo(gridGraph, sx, sy, ex, ey);
        algo.computePath();
    }

}

class TestResult {
    public final int timesRan;
    public final double time;
    public final double timeSD;
    public final double pathLength;
    public final boolean isTaut;

    public TestResult(int timesRan, double time, double timeSD, double pathLength, boolean isTaut) {
        this.timesRan = timesRan;
        this.time = time;
        this.timeSD = timeSD;
        this.pathLength = pathLength;
        this.isTaut = isTaut;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Times ran: " + timesRan).append("\n");
        sb.append("Mean Time (ms): " + time + " (+/-" + timeSD + ")").append("\n");
        sb.append("Path length: " + pathLength).append("\n");
        sb.append("Is Taut: " + (isTaut ? "YES" : "NO")).append("\n");

        return sb.toString();
    }
}

interface TestFunction {
    public void test(String mazeName, GridGraph gridGraph, ArrayList<TwoPoint> problemSet, AlgoFunction algoFunction);
}

interface TestFunctionData {
    public void test(String mazeName, GridGraph gridGraph, ArrayList<StartEndPointData> problemSet,
            AlgoFunction algoFunction);
}
