package uiandio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import algorithms.datatypes.Point;
import grid.GridGraph;
import main.mazes.MazeAndTestCases;
import main.testgen.StartEndPointData;

public class BenchmarkGraphImporter {
    private static String BENCHMARKS_PATH = "originalbenchmarks/";
    
    private static String nameToMapFile(String name) {
        return name + ".map";
    }
    
    private static String nameToScenFile(String name) {
        return name + ".map.scen";
    }

    private static String readFile(String path) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static GridGraph loadBenchmarkMaze(String mazeName) {
        String path = BENCHMARKS_PATH + nameToMapFile(mazeName);
        String data = readFile(path);
        int mapStart = data.indexOf("map") + 3;
        String[] headerData = data.substring(0, mapStart).split("\n");
        
        int sizeX = -1;
        int sizeY = -1;
        for (String header : headerData) {
            if (header.startsWith("width")) {
                String temp = header.trim();
                temp = temp.substring(temp.lastIndexOf(" ")+1);
                sizeX = Integer.parseInt(temp);
            }
            if (header.startsWith("height")) {
                String temp = header.trim();
                temp = temp.substring(temp.lastIndexOf(" ")+1);
                sizeY = Integer.parseInt(temp);
            }
        }
        if (sizeX <= 0 || sizeY <= 0) throw new UnsupportedOperationException("Unable to read map size!");
        
        
        GridGraph gridGraph = new GridGraph(sizeX, sizeY);
        int index = 0;
        int curr = mapStart;
        int maxIndex = sizeX*sizeY;
        while (index < maxIndex) {
            char c = data.charAt(curr);
            
            /*   . - passable terrain
             *   G - passable terrain
             *   @ - out of bounds
             *   O - out of bounds
             *   T - trees (unpassable)
             *   S - swamp (passable from regular terrain)
             *   W - water (traversable, but not passable from terrain)
             */
            int blocked = -1; 
            switch(c) {
                case '.': blocked = 0; break;
                case 'G': blocked = 0; break;
                case '@': blocked = 1; break;
                case 'O': blocked = 1; break;
                case 'T': blocked = 1; break;
                case 'S': blocked = 0; break;
                case 'W': blocked = 1; break;
            }
            
            int x = index%sizeX;
            int y = index/sizeX;
            if (blocked == 1) {
                gridGraph.setBlocked(x, y, true);
                ++index;
            } else if (blocked == 0) {
                gridGraph.setBlocked(x, y, false);
                ++index;
            }
            ++curr;
        }
        
        return gridGraph;
    }

    public static ArrayList<StartEndPointData> loadBenchmarkMazeProblems(String mazeName) {
        String path = BENCHMARKS_PATH + nameToScenFile(mazeName);
        String[] data = readFile(path).split("\n");
        
        ArrayList<StartEndPointData> problems = new ArrayList<>();
        // Bucket, map, map width, map height, start x-coordinate, start y-coordinate, goal x-coordinate, goal y-coordinate, optimal length
        for (String line : data) {
            String[] tokens = line.trim().split("\\s+");
            if (tokens.length < 9) continue;
            int sx = Integer.parseInt(tokens[4]);
            int sy = Integer.parseInt(tokens[5]);
            int ex = Integer.parseInt(tokens[6]);
            int ey = Integer.parseInt(tokens[7]);
            double pathlength = Double.parseDouble(tokens[8]);
            problems.add(new StartEndPointData(new Point(sx, sy), new Point(ex, ey), pathlength));
        }
        return problems;
    }
    
    public static MazeAndTestCases loadBenchmark(String mazeName) {
        GridGraph gridGraph = loadBenchmarkMaze(mazeName);
        ArrayList<StartEndPointData> problems = loadBenchmarkMazeProblems(mazeName);
        return new MazeAndTestCases(mazeName, gridGraph, problems);
    }
}
