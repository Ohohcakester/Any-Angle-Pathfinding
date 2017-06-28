package main.testgen;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import algorithms.datatypes.Point;
import grid.GridGraph;
import main.AnyAnglePathfinding;
import main.analysis.MazeAnalyser;
import main.analysis.MazeAnalysis;
import main.analysis.ProblemAnalysis;
import main.analysis.TwoPoint;
import main.graphgeneration.DefaultGenerator;
import main.mazes.MazeAndTestCases;
import main.mazes.StoredTestMazes;
import uiandio.FileIO;
import uiandio.GraphExporter;
import uiandio.GraphExporterPretty;
import uiandio.GraphImporter;

public class TestDataGenerator {

    public static void run() {
        /*generateFromFolder("gamemaps/room32/", "room32");
        generateFromFolder("gamemaps/corr16/", "corr16");
        generateFromFolder("gamemaps/obst40/", "obst40");
        generateFromFolder("gamemaps/obst10/", "obst10");
        generateFromFolder("gamemaps/corr2/", "corr2");*/
        //generateRandom();
        
        export();
    }
    
    private static void export() {

        // "scaledmazes"
        for (int scale = 4; scale <= 12; scale += 4) {
            exportTestData(StoredTestMazes.loadScaledMaze("sc1_NovaStation",scale));
            exportTestData(StoredTestMazes.loadScaledMaze("wc3_darkforest",scale));
            exportTestData(StoredTestMazes.loadScaledMaze("sc1_RedCanyons",scale));
            exportTestData(StoredTestMazes.loadScaledMaze("wc3_swampofsorrows",scale));
            exportTestData(StoredTestMazes.loadScaledMaze("sc1_Triskelion",scale));
            exportTestData(StoredTestMazes.loadScaledMaze("wc3_theglaive",scale));
        }

        // "tiledmazes"
        for (int mazePoolIndex=0;mazePoolIndex<4;++mazePoolIndex) {
            for (int size = 4; size <= 12; size+=4) {
                exportTestData(StoredTestMazes.loadTiledMaze(mazePoolIndex, size));
            }
        }

        // "automatadcmazes"
        for (int percentBlockedIndex=0; percentBlockedIndex<2; ++percentBlockedIndex) {
            for (int resolutionIndex=0; resolutionIndex<5; resolutionIndex+=2) {
                for (int sizeIndex=0; sizeIndex<5; sizeIndex+=2) {
                    exportTestData(StoredTestMazes.loadAutomataDCMaze(sizeIndex, resolutionIndex, percentBlockedIndex));
                }
            }
        }

        // "mazemaps"
        {
            int[] connectednessRatioIndexes = new int[] {0,1,2};
            for (int sizeIndex=0; sizeIndex<7; sizeIndex+=2) {
                int corridorWidthIndex = 1;
                for (int j=0; j<connectednessRatioIndexes.length; ++j) {
                    int connectednessRatioIndex = connectednessRatioIndexes[j];
                    exportTestData(StoredTestMazes.loadMazeMaze(sizeIndex, corridorWidthIndex, connectednessRatioIndex));
                }
            }
        }
    }
    
    /**
     * Example: generateFromFolder("gamemaps/sc2/", "sc2");
     */
    public static void generateFromFolder(String path, String mazeNamePrefix) {
        File dir = new File(path);
        File[] files = dir.listFiles((file, name) -> name.endsWith(".txt"));
        for (File file : files) {
            String filePath = file.getPath();
            String name = file.getName();
            name = name.substring(0, name.lastIndexOf('.'));
            name = mazeNamePrefix + "_" + name;

            GridGraph gridGraph = GraphImporter.importGraphFromFile(filePath);
            generateTestData(gridGraph, 20, name);
            System.gc();
        }
    }
    
    public static void generateRandom() {
        int[] ratios = new int[]{7, 15, 50};

//        for (int ratio : ratios) generateRandomDefault(1000,1000,ratio);
//        for (int ratio : ratios) generateRandomDefault(2000,2000,ratio);
//        for (int ratio : ratios) generateRandomDefault(3000,3000,ratio);
//        for (int ratio : ratios) generateRandomDefault(4000,4000,ratio);
//        for (int ratio : ratios) generateRandomDefault(5000,5000,ratio);
        
        /*for (int ratio : ratios) generateRandomDefault(10,10,ratio);
        for (int ratio : ratios) generateRandomDefault(20,20,ratio);
        for (int ratio : ratios) generateRandomDefault(20,6,ratio);
        for (int ratio : ratios) generateRandomDefault(30,30,ratio);
        for (int ratio : ratios) generateRandomDefault(50,50,ratio);
        for (int ratio : ratios) generateRandomDefault(100,20,ratio);
        for (int ratio : ratios) generateRandomDefault(100,50,ratio);
        for (int ratio : ratios) generateRandomDefault(100,100,ratio);
        for (int ratio : ratios) generateRandomDefault(20,200,ratio);
        for (int ratio : ratios) generateRandomDefault(300,300,ratio);
        for (int ratio : ratios) generateRandomDefault(50,500,ratio);
        for (int ratio : ratios) generateRandomDefault(15,1000,ratio);
        for (int ratio : ratios) generateRandomDefault(500,500,ratio);*/
    }
    
    
    private static void generateRandomDefault(int sizeX, int sizeY, int unblockedRatio) {
        Random rand = new Random();
        int seed = rand.nextInt();
        GridGraph gridGraph = DefaultGenerator.generateSeededGraphOnly(seed, sizeX, sizeY, unblockedRatio);
        String mazeName = Stringifier.defaultToStringReadable(seed, sizeX, sizeY, unblockedRatio);
        generateTestData(gridGraph, 50, mazeName);
    }
    
    
    public static ArrayList<TwoPoint> generateTwoPointList(int...points) {
        if (points.length%4 != 0) {
            throw new UnsupportedOperationException("The number of arguments must be a multiple of 4!");
        }
        ArrayList<TwoPoint> twoPointList = new ArrayList<>();
        for (int i=0; i<points.length/4; i++) {
            int sx = points[4*i];
            int sy = points[4*i+1];
            int ex = points[4*i+2];
            int ey = points[4*i+3];
            
            Point start = new Point(sx,sy);
            Point end = new Point(ex, ey);
            twoPointList.add(new TwoPoint(start, end));
        }
        return twoPointList;
    }
    
    public static void regenerateSpecificAnalysis(String mazeName, String options) {
        GridGraph gridGraph = GraphImporter.loadStoredMaze(mazeName);
        MazeAnalysis mazeAnalysis = MazeAnalysis.options(gridGraph, options);
        throw new UnsupportedOperationException("Not implemneted yet!");
    }
    
    public static void regenerateAnalysis(String mazeName) {
        System.out.println("Regenerate analysis file for " + mazeName + "...");
        String filePath = AnyAnglePathfinding.PATH_MAZEDATA + mazeName + "/";
        
        GridGraph gridGraph = GraphImporter.loadStoredMaze(mazeName);
        MazeAnalysis mazeAnalysis = new MazeAnalysis(gridGraph);
        makeAnalysisFile(mazeAnalysis, filePath);
        
        System.out.println("Done.");
    }
    
    public static void regeneratePathData(String mazeName) {
        throw new UnsupportedOperationException("Not implemneted yet!");
    }
    
    
    public static void generateTestData(GridGraph gridGraph, ArrayList<TwoPoint> problemList,
            String mazeName, boolean analyseMaze) {
        String filePath = AnyAnglePathfinding.PATH_MAZEDATA + mazeName + "/";
        MazeAnalyser mazeAnalyser = new MazeAnalyser(gridGraph, problemList, analyseMaze);

        System.out.println("-Writing to folder: " + filePath);
        FileIO.makeDirs(filePath);

        if (analyseMaze) {
            makeMazeFile(gridGraph, filePath);
            makePrettyMazeFile(gridGraph, filePath);
            makeAnalysisFile(mazeAnalyser.mazeAnalysis, filePath);
        }
        
        makeProblemFiles(mazeAnalyser.problemList, filePath);
        System.out.println("-Write complete.");
    }

    
    
    public static void exportTestData(MazeAndTestCases mazeAndTestCases) {
        String filePath = AnyAnglePathfinding.PATH_MAZEDATA + "export_" + mazeAndTestCases.mazeName + "/";
        ArrayList<TwoPoint> problems = new ArrayList<>();
        for (StartEndPointData data : mazeAndTestCases.problems) {
            problems.add(data.toTwoPoint());
        }
        MazeAnalyser mazeAnalyser = new MazeAnalyser(mazeAndTestCases.gridGraph, problems, true);

        System.out.println("-Writing to folder: " + filePath);
        FileIO.makeDirs(filePath);

        makeMazeFile(mazeAndTestCases.gridGraph, filePath);
        //makePrettyMazeFile(mazeAndTestCases.gridGraph, filePath);
        makeAnalysisFile(mazeAnalyser.mazeAnalysis, filePath);
        
        makeProblemFiles(mazeAnalyser.problemList, filePath);
        System.out.println("-Write complete.");
    }

    
    
    
    private static void generateTestData(GridGraph gridGraph, int nProblems, String mazeName){
        String filePath = AnyAnglePathfinding.PATH_MAZEDATA + mazeName + "/";
        
        // Maze analysis
        MazeAnalyser mazeAnalyser = new MazeAnalyser(gridGraph, nProblems);

        System.out.println("-Writing to folder: " + filePath);
        FileIO.makeDirs(filePath);
        makeMazeFile(gridGraph, filePath);
        makePrettyMazeFile(gridGraph, filePath);
        makeAnalysisFile(mazeAnalyser.mazeAnalysis, filePath);
        
        // Problem analysis
        mazeAnalyser.startProblemAnalysis();
        
        System.out.println("-Writing to folder: " + filePath);
        makeProblemFiles(mazeAnalyser.problemList, filePath);
        System.out.println("-Write complete.");
    }

    private static void makeMazeFile(GridGraph gridGraph, String filePath) {
        GraphExporter graphExporter = new GraphExporter(gridGraph);
        FileIO fileIO = new FileIO(filePath + "maze.txt");
        while (graphExporter.hasNextLine()) {
            fileIO.writeLine(graphExporter.nextLine());
        }
        fileIO.close();
    }

    private static void makePrettyMazeFile(GridGraph gridGraph, String filePath) {
        GraphExporter graphExporter = new GraphExporterPretty(gridGraph);
        FileIO fileIO = new FileIO(filePath + "maze_pretty.txt");
        while (graphExporter.hasNextLine()) {
            fileIO.writeLine(graphExporter.nextLine());
        }
        fileIO.close();
    }

    private static void makeAnalysisFile(MazeAnalysis analysis,
            String filePath) {
        FileIO fileIO = new FileIO(filePath + "analysis.txt");
        addParameter(fileIO, "sizeX", analysis.sizeX);
        addParameter(fileIO, "sizeY", analysis.sizeY);
        addParameter(fileIO, "nTiles", analysis.sizeX*analysis.sizeY);
        addParameter(fileIO, "nBlocked", analysis.nBlocked);
        addParameter(fileIO, "blockDensity", analysis.blockDensity);
        addParameter(fileIO, "averageOpenSpaceSize", analysis.averageOpenSpaceSize);
        addParameter(fileIO, "averageBlockedIslandSize", analysis.averageBlockedIslandSize);
        addParameter(fileIO, "averageFloatingBlockedIslandSize", analysis.averageFloatingBlockedIslandSize);
        addParameter(fileIO, "hasSqueezableCorners", analysis.hasSqueezableCorners);
        addParameter(fileIO, "largestConnectedSetSize", analysis.largestConnectedSet.size());
        addParameter(fileIO, "numConnectedSets", analysis.connectedSets.size());
        addParameter(fileIO, "connectedSetSizes", analysis.computeConnectedSetSizeList());
        /*fileIO.writeLine("--connected sets--");
        for (ArrayList<Point> list : analysis.connectedSets) {
            fileIO.writeLine(list.toString());
        }*/
        fileIO.close();
    }
    
    private static void makeProblemFiles(
            ArrayList<ProblemAnalysis> problemList, String filePath) {
        for (ProblemAnalysis problem : problemList) {
            String name = makeProblemFileName(problem.sx, problem.sy, problem.ex, problem.ey);
            FileIO fileIO = new FileIO(filePath + name);
            addParameter(fileIO, "sx", problem.sx);
            addParameter(fileIO, "sy", problem.sy);
            addParameter(fileIO, "ex", problem.ex);
            addParameter(fileIO, "ey", problem.ey);
            addParameter(fileIO, "shortestPathLength", problem.shortestPathLength);
            addParameter(fileIO, "straightLineDistance", problem.straightLineDistance);
            addParameter(fileIO, "directness", problem.directness);
            addParameter(fileIO, "distanceCoverage", problem.distanceCoverage);
            addParameter(fileIO, "minMapCoverage", problem.minMapCoverage);
            addParameter(fileIO, "shortestPathHeadingChanges", problem.shortestPathHeadingChanges);
            if (problem.minHeadingChanges != -1) {
                addParameter(fileIO, "minHeadingChanges", problem.minHeadingChanges);
            }
            fileIO.close();
        }
    }

    private static String makeProblemFileName(int sx, int sy, int ex, int ey) {
        return Stringifier.makeProblemName(sx,sy,ex,ey) + ".problem";
    }
    
    private static void addParameter(FileIO fileIO, String name, Object value) {
        fileIO.writeRow(name + ":", value.toString());
    }
    
    
}