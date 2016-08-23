package main;

import java.util.ArrayList;

import algorithms.AStar;
import algorithms.AStarOctileHeuristic;
import algorithms.AStarStaticMemory;
import algorithms.AcceleratedAStar;
import algorithms.Anya;
import algorithms.BasicThetaStar;
import algorithms.BreadthFirstSearch;
import algorithms.JumpPointSearch;
import algorithms.LazyThetaStar;
import algorithms.PathFindingAlgorithm;
import algorithms.RecursiveThetaStar;
import algorithms.RestrictedVisibilityGraphAlgorithm;
import algorithms.StrictVisibilityGraphAlgorithm;
import algorithms.StrictVisibilityGraphAlgorithmV2;
import algorithms.VisibilityGraphAlgorithm;
import algorithms.datatypes.Point;
import algorithms.incrementalvgs.IVGAlgorithm;
import algorithms.sparsevgs.DirectedEdgeNLevelSparseVisibilityGraphAlgorithm;
import algorithms.sparsevgs.EdgeNLevelSparseVisibilityGraphAlgorithm;
import algorithms.sparsevgs.SparseVisibilityGraphAlgorithm;
import algorithms.sparsevgs.VertexNLevelSparseVisibilityGraphAlgorithm;
import algorithms.sparsevgs.VisibilityGraphAlgorithmOptimised;
import algorithms.strictthetastar.RecursiveStrictThetaStar;
import algorithms.strictthetastar.StrictThetaStar;
import algorithms.subgoalgraphs.AnyAngleNLevelSubgoalGraphsAlgorithm;
import algorithms.subgoalgraphs.AnyAngleSubgoalGraphsAlgorithm;
import algorithms.subgoalgraphs.NLevelSubgoalGraphsAlgorithm;
import algorithms.subgoalgraphs.RecursiveStrictAnyAngleSubgoalGraphsAlgorithm;
import algorithms.subgoalgraphs.StrictAnyAngleSubgoalGraphsAlgorithm;
import algorithms.subgoalgraphs.SubgoalGraphsAlgorithm;
import algorithms.vertexanya.VertexAnya;
import algorithms.vertexanya.VertexAnyaMarking;
import algorithms.vertexanya.VertexAnyaMarkingV2;
import algorithms.vertexanya.VertexAnyaMarkingV3;
import algorithms.vertexanya.VertexAnyaNoExtents;
import algorithms.vertexanya.VisibilityScanSearchEager;
import algorithms.vertexanya.VisibilityScanSearchSemiEager;
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
import uiandio.FileIO;
import uiandio.GraphImporter;

public class AlgoTest {
    private static FileIO io;
    private static boolean writeToFile = false;

    private static boolean pleaseTurnOffAssertions() {
        System.out.println("Please turn off assertions during tests (Run -> Run Configurations -> Arguments -> remove -ea)");
        return false;
    }
    
    public static void run() {
        assert pleaseTurnOffAssertions();
        //runTestAllAlgos();
       
        AlgoFunction aStar = AStar::new;
        AlgoFunction aStarStatic = AStarStaticMemory::new;
        AlgoFunction aStarOctile = AStarOctileHeuristic::new;
        AlgoFunction aStarOctilePS = AStarOctileHeuristic::postSmooth;
        AlgoFunction bfs = BreadthFirstSearch::new;
        AlgoFunction jumpPointSearch = JumpPointSearch::new;
        AlgoFunction jpsPS = JumpPointSearch::postSmooth;
        AlgoFunction lazyThetaStar = LazyThetaStar::new;
        AlgoFunction basicThetaStar = BasicThetaStar::new;
        AlgoFunction basicThetaStarPS = BasicThetaStar::postSmooth;
        AlgoFunction aStarPS = AStar::postSmooth;
        AlgoFunction dijkstra = AStar::dijkstra;
        AlgoFunction accAStar = AcceleratedAStar::new;
        AlgoFunction anya = Anya::new;

        AlgoFunction subgoalGraphs = SubgoalGraphsAlgorithm::new;
        AlgoFunction _2LevelSubgoalGraphs = NLevelSubgoalGraphsAlgorithm.levels(2);
        AlgoFunction _3LevelSubgoalGraphs = NLevelSubgoalGraphsAlgorithm.levels(3);
        AlgoFunction nLevelSubgoalGraphs = NLevelSubgoalGraphsAlgorithm::new;

        AlgoFunction anyAngleSubgoalGraphs = AnyAngleSubgoalGraphsAlgorithm::new;
        AlgoFunction anyAngleNLevelSubgoalGraphs = AnyAngleNLevelSubgoalGraphsAlgorithm::new;

        AlgoFunction strictAnyAngleSubgoalGraphs = StrictAnyAngleSubgoalGraphsAlgorithm::new;
        AlgoFunction recursiveStrictAnyAngleSubgoalGraphs = RecursiveStrictAnyAngleSubgoalGraphsAlgorithm::new;

        AlgoFunction vga = VisibilityGraphAlgorithm::new;
        AlgoFunction vgaReuse = VisibilityGraphAlgorithm::graphReuse;
        AlgoFunction vgaReuseOptimised = VisibilityGraphAlgorithmOptimised::graphReuse;
        AlgoFunction rVGA = RestrictedVisibilityGraphAlgorithm::new;
        AlgoFunction sVGA = StrictVisibilityGraphAlgorithm::new;
        AlgoFunction sVGAv2 = StrictVisibilityGraphAlgorithmV2::new;

        AlgoFunction strictThetaStar = StrictThetaStar::new;
        AlgoFunction strictThetaStarPS = StrictThetaStar::postSmooth;
        AlgoFunction recStrictThetaStar = RecursiveStrictThetaStar::new;
        AlgoFunction recStrictThetaStarPS = RecursiveStrictThetaStar::postSmooth;
        AlgoFunction recStrictThetaStar_2 = (a, b, c, d, e) -> RecursiveStrictThetaStar.depthLimit(a, b, c, d, e, 2);

        AlgoFunction sparseVGA = SparseVisibilityGraphAlgorithm::graphReuse;
        AlgoFunction vertexNLevelSparseVGA = VertexNLevelSparseVisibilityGraphAlgorithm::graphReuse;
        AlgoFunction edgeNLevelSparseVGA = EdgeNLevelSparseVisibilityGraphAlgorithm::graphReuse;
        AlgoFunction edge1LevelSparseVGA = EdgeNLevelSparseVisibilityGraphAlgorithm.withLevelLimit(1);
        AlgoFunction edge2LevelSparseVGA = EdgeNLevelSparseVisibilityGraphAlgorithm.withLevelLimit(2);
        AlgoFunction edge3LevelSparseVGA = EdgeNLevelSparseVisibilityGraphAlgorithm.withLevelLimit(3);
        AlgoFunction directedEdgeNLevelSparseVGA = DirectedEdgeNLevelSparseVisibilityGraphAlgorithm::graphReuse;

        AlgoFunction incrementalVGA = IVGAlgorithm::new;
        AlgoFunction vertexAnya = VertexAnya::new;
        AlgoFunction vertexAnyaNoExtents = VertexAnyaNoExtents::new;
        AlgoFunction vertexAnyaMarking = VertexAnyaMarking::new;
        AlgoFunction vertexAnyaMarkingV2 = VertexAnyaMarkingV2::new;
        AlgoFunction vertexAnyaMarkingV3 = VertexAnyaMarkingV3::new;
        AlgoFunction visibilityScanSearchEager = VisibilityScanSearchEager::new;
        AlgoFunction visibilityScanSearchSemiEager = VisibilityScanSearchSemiEager::new;

        AlgoFunction recursiveThetaStar = RecursiveThetaStar::new;

        FileIO.makeDirs("testResults/");
        System.gc(); System.gc();

        // float[] buffers = new float[]{0f,0.01f,0.1f,0.2f,0.4f,0.6f,0.8f,1f,1.2f,1.5f,2f,3f,5f,8f,10f,20f,30f,50f};
        // for (float buffer : buffers) {
        //     AlgoFunction algo = (a,b,c,d,e) ->
        //     StrictThetaStarV2e.setBuffer(a,b,c,d,e,buffer);
        //     testSequence(algo, "StrictThetaStarV2e_"+buffer);
        // }
        // for (float buffer : buffers) {
        //     AlgoFunction algo = (a,b,c,d,e) ->
        //     StrictThetaStarV1.setBuffer(a,b,c,d,e,buffer);
        //     testSequence(algo, "StrictThetaStarV1_"+buffer);
        // }
        // testSequence(aStar, "AStar");
        // testSequence(aStarPS, "AStarPS");
        // testSequence(aStarStatic, "AStar SLD");
        // testSequence(aStarOctile, "AStar Octile");
        // testSequence(bfs, "BreadthFirstSearch");
        // testSequence(jumpPointSearch, "JumpPointSearch");
        // testSequence(basicThetaStar, "BasicThetaStar");
        // testSequence(basicThetaStarPS, "BasicThetaStar_PS");
        // testSequence(lazyThetaStar, "LazyThetaStar");
        // testSequence(accAStar, "AcceleratedAStar");
        // testSequence(aStarOctilePS, "AStarOctile PostSmooth");
        // testSequence(jpsPS, "JPS PostSmooth");
        // testSequence(dijkstra, "Dijkstra");
        // testSequence(vgaReuse, "VisibilityGraph Reuse");
        // testSequence(vgaReuseOptimised, "VisibilityGraph Reuse Optimised");
        // testSequence(vga, "VisibilityGraphs");
        // testSequence(vga, "VISIBILITY GRAPHS PART 2");
        // testSequence(anya, "Anya");

        // testSequence(subgoalGraphs, "SubgoalGraphs");
        // testSequence(_2LevelSubgoalGraphs, "2LevelSubgoalGraphs");
        // testSequence(_3LevelSubgoalGraphs, "3LevelSubgoalGraphs");
        // testSequence(nLevelSubgoalGraphs, "NLevelSubgoalGraphs");

        // testSequence(anyAngleSubgoalGraphs, "AnyAngleSubgoalGraphs");
        // testSequence(anyAngleNLevelSubgoalGraphs,
        // "AnyAngleNLevelSubgoalGraphs");

        // testSequence(strictAnyAngleSubgoalGraphs, "StrictAnyAngleSubgoalGraphs");
        // testSequence(recursiveStrictAnyAngleSubgoalGraphs, "RecursiveStrictAnyAngleSubgoalGraphs");

        // testSequence(vertexAnya, "VertexAnya");
        // testSequence(vertexAnyaNoExtents, "VertexAnyaNoExtents");
        // testSequence(vertexAnyaMarking, "VertexAnyaMarking");
        // testSequence(vertexAnyaMarkingV2, "VertexAnyaMarkingV2");
        // testSequence(vertexAnyaMarkingV3, "VertexAnyaMarkingV3");
        // testSequence(visibilityScanSearchEager, "VisibilityScanSearchEager");
        // testSequence(visibilityScanSearchSemiEager,
        // "VisibilityScanSearchSemiEager");

        // testSequence(recursiveThetaStar, "RecursiveThetaStar");
        // testSequence(strictThetaStar, "StrictThetaStar");
        // testSequence(strictThetaStarPS, "StrictThetaStarPS");
        // testSequence(recStrictThetaStar, "RecStrictThetaStar");
        // testSequence(recStrictThetaStarPS, "RecStrictThetaStarPS");
        // testSequence(recStrictThetaStar_2, "RecStrictThetaStar_2");
        // testSequence(sVGA, "StrictVisibilityGraphs");

        // testSequence(sVGAv2, "StrictVisibilityGraphsV2");
        // testSequence(sparseVGA, "SparseVisibilityGraphs");
        // testSequence(vertexNLevelSparseVGA, "VertexNLevelSparseVisibilityGraphs");
        // testSequence(edgeNLevelSparseVGA, "EdgeNLevelSparseVisibilityGraphs");
        // testSequence(edge1LevelSparseVGA, "Edge1LevelSparseVisibilityGraphs");
        // testSequence(edge2LevelSparseVGA, "Edge2LevelSparseVisibilityGraphs");
        // testSequence(edge3LevelSparseVGA, "Edge3LevelSparseVisibilityGraphs");
        // testSequence(directedEdgeNLevelSparseVGA, "DirectedEdgeNLevelSparseVisibilityGraphs");
        // testSequence(incrementalVGA, "IncrementalVisibilityGraphs");

        // testSequence(rVGA, "RestrictedVisibilityGraphs");
        // testSequence(vga, "VisibilityGraphs");
    }

    public static void testSequence(AlgoFunction algo, String name) {
        String path = "testResults/" + name.replace(" ", "_") + ".txt";
        if (writeToFile) io = new FileIO(path);

        boolean pathLengthOnly = false;
        boolean runningTimeOnly = false;
        boolean tnitialisationTime = false;

        // TestFunctionData testFunction_slow = printAverageData(50, 5);
        TestFunctionData testFunction_slow = printAverageData(5, 5);
        TestFunctionData testFunction_fast = printAverageData(50, 30);
        if (pathLengthOnly) {
            testFunction_slow = testPathLengthOnly;
            testFunction_fast = testPathLengthOnly;
            // testFunction_slow = analyseIndividualPaths;
            // testFunction_fast = analyseIndividualPaths;
        }
        if (runningTimeOnly) {
            testFunction_slow = testIndividualRunningTimes(20, 1);
            testFunction_fast = testIndividualRunningTimes(50, 5);
        }
        if (tnitialisationTime) {
            testFunction_slow = testInitialisationTime;
        }

        println("=== Testing " + name + " ===");
        println("<< GAME MAPS >>");

        // println("sc2_steppesofwar - 164x164");// - spacious");
        // testOnMazeData("sc2_steppesofwar", algo, testFunction_slow);
        // println("sc2_losttemple - 132x131");
        // testOnMazeData("sc2_losttemple", algo, testFunction_slow);
        // println("sc2_extinction - 164x164");// - less spacious");
        // testOnMazeData("sc2_extinction", algo, testFunction_slow);
        //
        // println("baldursgate_AR0070SR - 124x134");
        // testOnMazeData("baldursgate_AR0070SR", algo, testFunction_slow);
        // println("baldursgate_AR0705SR - 100x86");// - less spacious");
        // testOnMazeData("baldursgate_AR0705SR", algo, testFunction_slow);
        // println("baldursgate_AR0418SR - 84x75");// - spacious");
        // testOnMazeData("baldursgate_AR0418SR", algo, testFunction_slow);
        //
        // println("sc1_TheFrozenSea - 1024x1024");
        // testOnMazeData("sc1_TheFrozenSea", algo, testFunction_slow);
        // println("sc1_SpaceDebris - 512x512");
        // testOnMazeData("sc1_SpaceDebris", algo, testFunction_slow);
        //
        // println("wc3_icecrown - 512x512");// (spacious)");
        // testOnMazeData("wc3_icecrown", algo, testFunction_slow);
        // println("wc3_dragonfire - 512x512");// (less spacious)");
        // testOnMazeData("wc3_dragonfire", algo, testFunction_slow);
        //
        // println("sc1_Legacy - 512x512");
        // testOnMazeData("sc1_Legacy", algo, testFunction_slow);
        //
        // println("sc1_GreenerPastures - 768x512");
        // testOnMazeData("sc1_GreenerPastures", algo, testFunction_slow);
        //
        // println("baldursgate_AR0603SR - 512x512");
        // testOnMazeData("baldursgate_AR0603SR", algo, testFunction_slow);
        //
        // println("wc3_mysticisles - 512x512");
        // testOnMazeData("wc3_mysticisles", algo, testFunction_slow);
        //
        // println("wc3_petrifiedforest - 512x512");
        // testOnMazeData("wc3_petrifiedforest", algo, testFunction_slow);

        println("<< GENERATED MAPS >>");
        //
        // println("Low Density - 6% - 50x50");
        // testOnMazeData("def_iCUZANYD_iSB_iSB_iSB", algo, testFunction_slow);
        // println("Medium Density - 20% - 50x50");
        // testOnMazeData("def_i10VA3PD_iSB_iSB_iP", algo, testFunction_slow);
        // println("High Density - 40% - 50x50");
        // testOnMazeData("def_i3ML5FBD_iSB_iSB_iH", algo, testFunction_slow);
        //
        // println("Low Density - 6% - 300x300");
        // testOnMazeData("def_iHHLNUOB_iMJ_iMJ_iSB", algo, testFunction_slow);
        // println("Medium Density - 20% - 300x300");
        // testOnMazeData("def_iZLPIX5B_iMJ_iMJ_iP", algo, testFunction_slow);
        // println("High Density - 40% - 300x300");
        // testOnMazeData("def_iVVJKDR_iMJ_iMJ_iH", algo, testFunction_slow);
        //
        // println("Low Density - 6% - 500x500");
        // testOnMazeData("def_iIRXXUKC_iUP_iUP_iSB", algo, testFunction_slow);
        // println("Medium Density - 20% - 500x500");
        // testOnMazeData("def_iOMJ14Z_iUP_iUP_iP", algo, testFunction_slow);
        //
        // println("LowDensity2 - 6% - 300x300");
        // testOnMazeData("def_iMJWB0QC_iMJ_iMJ_iSB", algo, testFunction_slow);
        // println("MediumDensity2 - 20% - 300x300");
        // testOnMazeData("def_iBCA5SS_iMJ_iMJ_iP", algo, testFunction_slow);
        // println("HighDensity2 - 40% - 300x300");
        // testOnMazeData("def_i11AHREB_iMJ_iMJ_iH", algo, testFunction_slow);
        //
        // println("LowDensity3 - 6% - 300x300");
        // testOnMazeData("def_iOH1TZ0D_iMJ_iMJ_iSB", algo, testFunction_slow);
        // println("MediumDensity3 - 20% - 300x300");
        // testOnMazeData("def_iAN3IE0C_iMJ_iMJ_iP", algo, testFunction_slow);
        // println("HighDensity3 - 40% - 300x300");
        // testOnMazeData("def_iI0RFKYC_iMJ_iMJ_iH", algo, testFunction_slow);
        //
        // println("6%Density - 500x500");
        // testOnMazeData("def_iIRXXUKC_iUP_iUP_iSB", algo, testFunction_slow);
        // println("20%Density - 500x500");
        // testOnMazeData("def_iOMJ14Z_iUP_iUP_iP", algo, testFunction_slow);
        // println("40%Density - 500x500");
        // testOnMazeData("def_iREPZHKB_iUP_iUP_iH", algo, testFunction_slow);
        //
        // println("6%Density - 1000x1000");
        // testOnMazeData("gen_1000x1000_50_iY5U5GAC", algo, testFunction_slow);
        // println("20%Density - 1000x1000");
        // testOnMazeData("gen_1000x1000_15_iFTBETRD", algo, testFunction_slow);
        // println("40%Density - 1000x1000");
        // testOnMazeData("gen_1000x1000_7_i3WH4IJD", algo, testFunction_slow);
        //
        // println("6%Density - 2000x2000");
        // testOnMazeData("gen_2000x2000_50_iK54OY1C", algo, testFunction_slow);
        // println("20%Density - 2000x2000");
        // testOnMazeData("gen_2000x2000_15_i4Z44NPC", algo, testFunction_slow);
        // println("40%Density - 2000x2000");
        // testOnMazeData("gen_2000x2000_7_iXT3AEED", algo, testFunction_slow);
        //
        // println("6%Density - 3000x3000");
        // testOnMazeData("gen_3000x3000_50_iUE2IRAD", algo, testFunction_slow);
        // println("20%Density - 3000x3000");
        // testOnMazeData("gen_3000x3000_15_iGUM1R2C", algo, testFunction_slow);
        // println("40%Density - 3000x3000");
        // testOnMazeData("gen_3000x3000_7_iSR3L1J", algo, testFunction_slow);
        //
        // println("6%Density - 4000x4000");
        // testOnMazeData("gen_4000x4000_50_i0GHV1UD", algo, testFunction_slow);
        // println("20%Density - 4000x4000");
        // testOnMazeData("gen_4000x4000_15_iNK5KHO", algo, testFunction_slow);
        // println("40%Density - 4000x4000");
        // testOnMazeData("gen_4000x4000_7_iNUJNZ3", algo, testFunction_slow);
        //
        // println("6%Density - 5000x5000");
        // testOnMazeData("gen_5000x5000_50_iCFL2G3B", algo, testFunction_slow);
        // println("20%Density - 5000x5000");
        // testOnMazeData("gen_5000x5000_15_i0BTXUD", algo, testFunction_slow);
        // println("40%Density - 5000x5000");
        // testOnMazeData("gen_5000x5000_7_iHOPN1S", algo, testFunction_slow);
        //
        // println("6%Density2 - 500x500");
        // testOnMazeData("def_i5QEPQ2C_iUP_iUP_iSB", algo, testFunction_slow);
        // println("20%Density2 - 500x500");
        // testOnMazeData("def_iKMRKFCD_iUP_iUP_iP", algo, testFunction_slow);
        // println("40%Density2 - 500x500");
        // testOnMazeData("def_i5GM4YH_iUP_iUP_iH", algo, testFunction_slow);
        //
        // println("6%Density3 - 500x500");
        // testOnMazeData("def_iFOLAODC_iUP_iUP_iSB", algo, testFunction_slow);
        // println("20%Density3 - 500x500");
        // testOnMazeData("def_i5GZXLUD_iUP_iUP_iP", algo, testFunction_slow);
        // println("40%Density3 - 500x500");
        // testOnMazeData("def_iA0VKRW_iUP_iUP_iH", algo, testFunction_slow);
        //
        // println("obst10_random512-10-7 - 512x512");
        // testOnMazeData("obst10_random512-10-7", algo, testFunction_slow);
        // println("obst40_random512-40-0 - 512x512");
        // testOnMazeData("obst40_random512-40-0", algo, testFunction_slow);
        // println("obst40_random512-40-7 - 512x512");
        // testOnMazeData("obst40_random512-40-7", algo, testFunction_slow);
        //
        // println("corr2_maze512-2-5 - 512x512");
        // testOnMazeData("corr2_maze512-2-5", algo, testFunction_slow);
        // println("corr2_maze512-2-1 - 512x512");
        // testOnMazeData("corr2_maze512-2-1", algo, testFunction_slow);
        // println("corr2_maze512-2-7 - 512x512");
        // testOnMazeData("corr2_maze512-2-7", algo, testFunction_slow);
        // println("corr2_maze512-2-3 - 512x512");
        // testOnMazeData("corr2_maze512-2-3", algo, testFunction_slow);
        // println("corr2_maze512-2-9 - 512x512");
        // testOnMazeData("corr2_maze512-2-9", algo, testFunction_slow);

        println("<< STORED GENERATED MAPS >>");
        
        // for (int resolutionIndex=0; resolutionIndex<10; resolutionIndex++) {
        //     for (int scaleIndex=0; scaleIndex<7; ++scaleIndex) {
        //         testOnStoredMaze(StoredTestMazes.loadAutomataMaze(scaleIndex, resolutionIndex), algo, testFunction_slow);
        //         System.gc();System.gc();
        //         try {Thread.sleep(2000);}
        //         catch (Exception e) {throw new UnsupportedOperationException(e.getMessage());}
        //         System.gc();System.gc();
        //     }
        // }
        
        // testScaledMazes("sc1_NovaStation", algo, testFunction_slow);
        // testScaledMazes("wc3_darkforest", algo, testFunction_slow);
        // testScaledMazes("sc1_RedCanyons", algo, testFunction_slow);
        // testScaledMazes("wc3_swampofsorrows", algo, testFunction_slow);
        // testScaledMazes("sc1_Triskelion", algo, testFunction_slow);
        // testScaledMazes("wc3_theglaive", algo, testFunction_slow);
        
        // for (int mazePoolIndex=0;mazePoolIndex<4;++mazePoolIndex) {
        //     for (int size = 4; size <= 12; ++size) {
        //         testOnStoredMaze(StoredTestMazes.loadTiledMaze(mazePoolIndex, size), algo, testFunction_slow);
        //         System.gc();System.gc();
        //         try {Thread.sleep(2000);}
        //         catch (Exception e) {throw new UnsupportedOperationException(e.getMessage());}
        //         System.gc();System.gc();
        //     }
        // }

        println("=== FINISHED TEST FOR " + name + " ===");
        println();

        if (writeToFile) io.close();
    }
    
    public static void testScaledMazes(String mazeName, AlgoFunction algo, TestFunctionData testFunction) {
        for (int scale = 2; scale <= 12; scale += 2) {
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
        
        MazeAnalysis an = new MazeAnalysis(gridGraph, options);
        
        StringBuilder sb = new StringBuilder();
        sb.append(mazeName);
        sb.append(" - ").append(an.sizeX).append("x").append(an.sizeY);
        sb.append(" - ").append(an.blockDensity);
        sb.append(" - ").append(an.averageBlockedIslandSize);
        sb.append(" - ").append(an.averageFloatingBlockedIslandSize);
        
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
