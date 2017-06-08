package main.mazes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import algorithms.datatypes.Point;
import grid.GridGraph;
import main.analysis.MazeAnalysis;
import main.analysis.TwoPoint;
import main.graphgeneration.AutomataGenerator;
import main.graphgeneration.MazeMapGenerator;
import main.graphgeneration.TiledMapGenerator;
import main.graphgeneration.UpscaledMapGenerator;
import main.testgen.StartEndPointData;
import main.testgen.Stringifier;
import main.utility.Utility;
import uiandio.GraphImporter;

public class StoredTestMazes {

    private static final int NUM_TEST_PROBLEMS = 100;
    
    public static MazeAndTestCases loadAutomataMaze(int sizeIndex, int resolutionIndex) {

        int sizeX, sizeY;
        switch (sizeIndex) {
            case 0: sizeX = sizeY = 2000; break;
            case 1: sizeX = sizeY = 3000; break;
            case 2: sizeX = sizeY = 4000; break;
            case 3: sizeX = sizeY = 5000; break;
            case 4: sizeX = sizeY = 6000; break;
            case 5: sizeX = sizeY = 7000; break;
            case 6: sizeX = sizeY = 8000; break;
            default: throw new UnsupportedOperationException("Invalid sizeIndex: " + sizeIndex);
        }

        float resolution;
        switch (resolutionIndex) {
            case 0: resolution = 0.05f; break;
            case 1: resolution = 0.1f; break;
            case 2: resolution = 0.2f; break;
            case 3: resolution = 0.3f; break;
            case 4: resolution = 0.5f; break;
            case 5: resolution = 1f; break;
            case 6: resolution = 2f; break;
            case 7: resolution = 3f; break;
            case 8: resolution = 4f; break;
            case 9: resolution = 5f; break;
            default: throw new UnsupportedOperationException("Invalid resolutionIndex: " + resolutionIndex);
        }
        // Standardise resolution.
        resolution = resolution * 1000 / sizeX;

        int seed = sizeIndex + 577*(resolutionIndex+1);
        int problemSeed = resolutionIndex + 9127*(sizeIndex+1);

        int unblockedRatio = 5;
        int iterations = 3;
        int cutoffOffset = 0;
        boolean bordersAreBlocked = false;
        int nProblems = NUM_TEST_PROBLEMS;

        GridGraph gridGraph = AutomataGenerator.generateSeededGraphOnly(seed, sizeX, sizeY, unblockedRatio, iterations, resolution, cutoffOffset, bordersAreBlocked);
        ArrayList<StartEndPointData> problems = generateProblems(gridGraph, nProblems, problemSeed); 
        String mazeName = Stringifier.automataToString(seed, sizeX, sizeY, unblockedRatio, iterations, resolution, cutoffOffset, bordersAreBlocked);
        return new MazeAndTestCases(mazeName, gridGraph, problems);
    }

    public static MazeAndTestCases loadAutomataDCMaze(int sizeIndex, int resolutionIndex, int percentBlockedIndex) {

        int sizeX, sizeY;
        switch (sizeIndex) {
            case 0: sizeX = sizeY = 2000; break;
            case 1: sizeX = sizeY = 3000; break;
            case 2: sizeX = sizeY = 4000; break;
            case 3: sizeX = sizeY = 5000; break;
            case 4: sizeX = sizeY = 6000; break;
            case 5: sizeX = sizeY = 7000; break;
            case 6: sizeX = sizeY = 8000; break;
            default: throw new UnsupportedOperationException("Invalid sizeIndex: " + sizeIndex);
        }

        float resolution;
        switch (resolutionIndex) {
            case 0: resolution = 0.2f; break;
            case 1: resolution = 0.4f; break;
            case 2: resolution = 0.6f; break;
            case 3: resolution = 0.8f; break;
            case 4: resolution = 1.0f; break;
            default: throw new UnsupportedOperationException("Invalid resolutionIndex: " + resolutionIndex);
        }
        // Standardise resolution.
        resolution = resolution * 2000 / sizeX;

        float percentBlocked;
        switch (percentBlockedIndex) {
            case 0: percentBlocked = 0.45f; break;
            case 1: percentBlocked = 0.30f; break;
            case 2: percentBlocked = 0.20f; break;
            case 3: percentBlocked = 0.10f; break;
            default: throw new UnsupportedOperationException("Invalid percentBlockedIndex: " + resolutionIndex);
        }

        // These seeds are carefully chosen so that the largest connected size is at least 10x the combined size of the remaining connected components.
        // These are chosen for 0 <= sizeIndex <= 6 and 0 <= resolutionIndex <= 4.
        int seed = (sizeIndex+1)*31 + 577*(resolutionIndex+1);
        if (sizeIndex >= 4) seed += 3;
        if (sizeIndex >= 5) seed += 2;
        if (sizeIndex >= 6) seed += 3;
        if (sizeIndex == 6 && resolutionIndex == 1) seed = 3926;
        
        int problemSeed = (resolutionIndex+1)*47 + 9127*(sizeIndex+1);

        int iterations = 5;
        boolean bordersAreBlocked = false;
        int nProblems = NUM_TEST_PROBLEMS;

        GridGraph gridGraph = AutomataGenerator.generateSeededGraphOnlyDynamicCutoff(seed, sizeX, sizeY, percentBlocked, iterations, resolution, bordersAreBlocked);
        
        // Validation code for largest connected set size.
        /*if (Utility.validateMazeConnectedSetSize(gridGraph, 10f)) {
            System.out.println("Valid");
        } else {
            throw new UnsupportedOperationException("INVALID");
        }
        if ("".isEmpty()) return null;*/

        ArrayList<StartEndPointData> problems = generateProblemsInLargestSet(gridGraph, nProblems, problemSeed); 
        String mazeName = Stringifier.automataDCToString(seed, sizeX, sizeY, percentBlocked, iterations, resolution, bordersAreBlocked);
        return new MazeAndTestCases(mazeName, gridGraph, problems);
    }

    public static MazeAndTestCases loadScaledMaze(String mazeName, int multiplier) {
        GridGraph gridGraph = GraphImporter.loadStoredMaze(mazeName);
        GridGraph newGridGraph = UpscaledMapGenerator.upscale(gridGraph, multiplier, true);
        
        int nProblems = NUM_TEST_PROBLEMS;
        int seed = mazeName.hashCode()+(multiplier+1)*31;
        
        ArrayList<StartEndPointData> newProblems = generateProblems(newGridGraph, nProblems, seed);
        
        String newMazeName = mazeName + "_x" + multiplier;
        return new MazeAndTestCases(newMazeName, newGridGraph, newProblems);
    }

    public static MazeAndTestCases loadTiledMaze(int mazePoolIndex, int size) {
        int nTiles = size*size;
        GridGraph[] mazePool = getMazePool(mazePoolIndex);
        GridGraph[] mazes = new GridGraph[nTiles];
        for (int i=0;i<nTiles;++i) {
            mazes[i] = mazePool[i%mazePool.length];
        }
        
        GridGraph newGridGraph = TiledMapGenerator.mergeMaps(mazes, size, size);
        
        int nProblems = NUM_TEST_PROBLEMS;
        int seed = size*787+(mazePoolIndex+1)*23;
        
        ArrayList<StartEndPointData> newProblems = generateProblems(newGridGraph, nProblems, seed);
        
        String newMazeName = "tiled_" + mazePoolIndex + "_" + size;
        
        return new MazeAndTestCases(newMazeName, newGridGraph, newProblems);
    }

    public static MazeAndTestCases loadMazeMaze(int sizeIndex, int corridorWidthIndex, int connectednessIndex) {

        int sizeX, sizeY;
        switch (sizeIndex) {
            case 0: sizeX = sizeY = 500; break;
            case 1: sizeX = sizeY = 1000; break;
            case 2: sizeX = sizeY = 2000; break;
            case 3: sizeX = sizeY = 3000; break;
            case 4: sizeX = sizeY = 4000; break;
            case 5: sizeX = sizeY = 5000; break;
            case 6: sizeX = sizeY = 6000; break;
            default: throw new UnsupportedOperationException("Invalid sizeIndex: " + sizeIndex);
        }

        int corridorWidth;
        switch (corridorWidthIndex) {
            case 0: corridorWidth = 1; break;
            case 1: corridorWidth = 2; break;
            case 2: corridorWidth = 4; break;
            default: throw new UnsupportedOperationException("Invalid corridorWidthIndex: " + corridorWidthIndex);
        }

        float connectednessRatio;
        switch (connectednessIndex) {
            case 0: connectednessRatio = 0f; break;
            case 1: connectednessRatio = 0.0001f; break;
            case 2: connectednessRatio = 0.001f; break;
            case 3: connectednessRatio = 0.01f; break;
            case 4: connectednessRatio = 0.1f; break;
            default: throw new UnsupportedOperationException("Invalid connectednessIndex: " + connectednessIndex);
        }

        int nProblems = NUM_TEST_PROBLEMS;
        int seed = (sizeIndex+1)*197;
        seed = (seed + connectednessIndex+1)*53;
        seed = (seed + corridorWidthIndex+1)*131;
        int problemSeed = (seed + sizeIndex+1)*13;
        
        GridGraph gridGraph = MazeMapGenerator.generateSeededGraphOnly(seed, sizeX, sizeY, corridorWidth, connectednessRatio);
        ArrayList<StartEndPointData> problems = generateProblemsInLargestSetOffline(gridGraph, nProblems, problemSeed); 
        String mazeName = Stringifier.mazeMapToString(seed, sizeX, sizeY, corridorWidth, connectednessRatio);
        return new MazeAndTestCases(mazeName, gridGraph, problems);
    }
    
    private static GridGraph[] getMazePool(int mazePoolIndex) {
        switch (mazePoolIndex) {
            case 0:
                return loadMazesFromNames(new String[] {
                    "sc1_BigGameHunters",
                    "sc1_RedCanyons",
                    "sc1_Eruption",
                });
            case 1:
                return loadMazesFromNames(new String[] {
                    "sc1_HotZone",
                    "sc1_Isolation",
                    "sc1_SpaceAtoll",
                    "sc1_Ramparts",
                    "sc1_OrbitalGully",
                });
            case 2:
                return loadMazesFromNames(new String[] {
                    "wc3_bootybay",
                    "wc3_darkforest",
                    "wc3_icecrown",
                });
            case 3:
                return loadMazesFromNames(new String[] {
                    "wc3_hillsofglory",
                    "wc3_nighthaven",
                    "wc3_theglaive",
                    "wc3_battleground",
                    "wc3_deadwaterdrop",
                });
        }
        
        throw new UnsupportedOperationException("Invalid maze pool index: " + mazePoolIndex);
    }
    
    private static GridGraph[] loadMazesFromNames(String[] mazeNames) {
        GridGraph[] gridGraphs = new GridGraph[mazeNames.length];
        for (int i=0;i<mazeNames.length;++i) {
            gridGraphs[i] = GraphImporter.loadStoredMaze(mazeNames[i]);
        }
        return gridGraphs;
    }


    private static ArrayList<StartEndPointData> generateProblems(GridGraph gridGraph, int nProblems, int seed) {
        ArrayList<ArrayList<Point>> connectedSets = MazeAnalysis.findConnectedSetsFast(gridGraph);
        Random rand = new Random(seed);
        ArrayList<StartEndPointData> problemList = new ArrayList<>();
        HashSet<TwoPoint> chosenProblems = new HashSet<>();
        
        int chances = nProblems; // prevent infinite loop        
        for (int i=0; i<nProblems; i++) {
            TwoPoint tp = generateProblem(rand, connectedSets);
            while (chances > 0 && chosenProblems.contains(tp)) {
                tp = generateProblem(rand, connectedSets);
                --chances;
            }
            chosenProblems.add(tp);
            
            problemList.add(computeStartEndPointData(gridGraph, tp.p1, tp.p2));
        }
        
        return problemList;
    }

    private static ArrayList<StartEndPointData> generateProblemsInLargestSet(GridGraph gridGraph, int nProblems, int seed) {
        return generateProblemsInLargestSet(gridGraph, nProblems, seed, true);
    }

    private static ArrayList<StartEndPointData> generateProblemsInLargestSetOffline(GridGraph gridGraph, int nProblems, int seed) {
        return generateProblemsInLargestSet(gridGraph, nProblems, seed, false);
    }

    private static ArrayList<StartEndPointData> generateProblemsInLargestSet(GridGraph gridGraph, int nProblems, int seed, boolean online) {
        ArrayList<ArrayList<Point>> connectedSets = MazeAnalysis.findConnectedSetsFast(gridGraph);
        ArrayList<Point> largestSet = MazeAnalysis.getLargestSet(connectedSets);
        
        Random rand = new Random(seed);
        ArrayList<StartEndPointData> problemList = new ArrayList<>();
        HashSet<TwoPoint> chosenProblems = new HashSet<>();
        
        int chances = nProblems; // prevent infinite loop        
        for (int i=0; i<nProblems; i++) {
            TwoPoint tp = generateProblemInSet(rand, largestSet);
            while (chances > 0 && chosenProblems.contains(tp)) {
                tp = generateProblemInSet(rand, largestSet);
                --chances;
            }
            chosenProblems.add(tp);
            
            problemList.add(computeStartEndPointData(gridGraph, tp.p1, tp.p2, online));
        }
        
        return problemList;
    }

    private static StartEndPointData computeStartEndPointData(GridGraph gridGraph, Point p1, Point p2) {
        return computeStartEndPointData(gridGraph, p1, p2, true);
    }

    private static StartEndPointData computeStartEndPointDataOffline(GridGraph gridGraph, Point p1, Point p2) {
        return computeStartEndPointData(gridGraph, p1, p2, false);
    }

    private static StartEndPointData computeStartEndPointData(GridGraph gridGraph, Point p1, Point p2, boolean online) {
        double shortestPathLength = online ?
            Utility.computeOptimalPathLengthOnline(gridGraph, p1.x, p1.y, p2.x, p2.y) :
            Utility.computeOptimalPathLengthOffline(gridGraph, p1.x, p1.y, p2.x, p2.y);
        return new StartEndPointData(p1, p2, shortestPathLength);
    }
    
    private static TwoPoint generateProblem(Random rand, ArrayList<ArrayList<Point>> connectedSets) {
        int nTraversableNodes = 0;
        for (ArrayList<Point> list : connectedSets) {
            nTraversableNodes += list.size();
        }
        int index = rand.nextInt(nTraversableNodes);
        int listIndex = 0;
        for (int i=0; i< connectedSets.size(); i++) {
            int size = connectedSets.get(i).size();
            if (index >= size) {
                index -= size;
            } else {
                listIndex = i;
                break;
            }
        }
        
        ArrayList<Point> list = connectedSets.get(listIndex);
        int index2 = rand.nextInt(list.size()-1);
        if (index2 == index) {
            index2 = list.size()-1;
        }
        
        Point p1 = list.get(index);
        Point p2 = list.get(index2);
        return new TwoPoint(p1, p2);
    }
    
    private static TwoPoint generateProblemInSet(Random rand, ArrayList<Point> largestSet) {
        int index = rand.nextInt(largestSet.size());
        int index2 = rand.nextInt(largestSet.size()-1);
        if (index2 == index) {
            index2 = largestSet.size()-1;
        }
        
        Point p1 = largestSet.get(index);
        Point p2 = largestSet.get(index2);
        return new TwoPoint(p1, p2);
    }
}
