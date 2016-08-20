package main.graphgeneration;

import java.util.ArrayList;

import algorithms.datatypes.Point;
import grid.GridAndGoals;
import grid.GridGraph;
import main.analysis.MazeAnalysis;

public class TiledMapGenerator {
    
    public static GridAndGoals mergeMapsDefault(GridGraph[] gridGraphs, int nCols, int nRows) {
        return new GridAndGoals(mergeMaps(gridGraphs, nCols, nRows), 0, 0, 0, 0);
    }
    
    
    public static GridGraph mergeMaps(GridGraph[] gridGraphs, int nCols, int nRows) {
        // Do checks first.
        if (gridGraphs.length != nCols*nRows) {
            throw new UnsupportedOperationException("Wrong number of maps to tile! Expected: " + 
                                                    (nCols*nRows) + ". Actual: " + gridGraphs.length);
        }
        
        int graphSizeX = gridGraphs[0].sizeX;
        int graphSizeY = gridGraphs[0].sizeY;
        
        for (int i=0;i<gridGraphs.length;++i) {
            if (gridGraphs[i].sizeX != graphSizeX || gridGraphs[i].sizeY != graphSizeY) {
                throw new UnsupportedOperationException("GridGraphs are not all of the same size: " + i);
            }       
        }

        int sizeX = graphSizeX * nCols;
        int sizeY = graphSizeY * nRows;
        
        boolean[][] grid = new boolean[sizeY][];
        for (int y=0;y<sizeY;++y) grid[y] = new boolean[sizeX];
        
        for (int r=0;r<nRows;++r) {
            for (int c=0;c<nCols;++c) {
                GridGraph gridGraph = gridGraphs[r*nCols + c];
                int baseX = c*graphSizeX;
                int baseY = r*graphSizeY;
                
                for (int y=0;y<graphSizeY;++y) {
                    for (int x=0;x<graphSizeX;++x) {
                        grid[y+baseY][x+baseX] = gridGraph.isBlocked(x, y);
                    }
                }
            }
        }
        
        joinMaps(gridGraphs, grid, graphSizeX, graphSizeY, nCols, nRows);
        
        
        GridGraph newGridGraph = new GridGraph(sizeX, sizeY);
        for (int y=0;y<sizeY;++y) {
            for (int x=0;x<sizeX;++x) {
                newGridGraph.setBlocked(x, y, grid[y][x]);
            }
        }
        return newGridGraph;
    }


    private static void joinMaps(GridGraph[] gridGraphs, boolean[][] grid, int graphSizeX, int graphSizeY, int nCols, int nRows) {
        int[][] yMinyMaxxMinxMax = new int[gridGraphs.length][];
        int midX = graphSizeX/2;
        int midY = graphSizeY/2;
        
        for (int i=0;i<gridGraphs.length;++i) {
            ArrayList<ArrayList<Point>> connectedSets = MazeAnalysis.findConnectedSetsFast(gridGraphs[i]);
            ArrayList<Point> largestConnectedSet = MazeAnalysis.getLargestSet(connectedSets);
            int yMin = graphSizeY;
            int yMax = -1;
            int xMin = graphSizeX;
            int xMax = -1;
            
            for (Point point : largestConnectedSet) {
                if (point.x == midX) {
                    if (point.y < yMin) yMin = point.y;
                    if (point.y > yMax) yMax = point.y;
                }
                if (point.y == midY) {
                    if (point.x < xMin) xMin = point.x;
                    if (point.x > xMax) xMax = point.x;
                }
            }
            yMinyMaxxMinxMax[i] = new int[]{yMin,yMax,xMin,xMax};
        }
        
        for (int r=0;r<nRows;++r) {
            for (int c=0;c<nCols;++c) {
                int g2 = r*nCols + c; 
                if (r > 0) {
                    // Connect vertical bridges
                    int bridgeX = c*graphSizeX + midX;
                    
                    int g1 = g2 - nCols;
                    int yStart = yMinyMaxxMinxMax[g1][1] + (r-1)*graphSizeY;
                    int yEnd = yMinyMaxxMinxMax[g2][0] + r*graphSizeY;
                    
                    for (int i=yStart;i<=yEnd;++i) {
                        grid[i][bridgeX] = false;
                    }
                }
                

                if (c > 0) {
                    // Connect horizontal bridges
                    int bridgeY = r*graphSizeY + midY;
                    
                    int g1 = g2 - 1;
                    int xStart = yMinyMaxxMinxMax[g1][3] + (c-1)*graphSizeX;
                    int xEnd = yMinyMaxxMinxMax[g2][2] + c*graphSizeX;

                    for (int i=xStart;i<=xEnd;++i) {
                        grid[bridgeY][i] = false;
                    }
                }
            }
        }
    }
}
