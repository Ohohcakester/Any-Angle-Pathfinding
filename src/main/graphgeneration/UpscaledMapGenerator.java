package main.graphgeneration;

import grid.GridAndGoals;
import grid.GridGraph;
import grid.StartGoalPoints;

public class UpscaledMapGenerator {
    
    public static GridAndGoals upscale(GridAndGoals gridAndGoals, int multiplier, boolean smooth) {
        StartGoalPoints sgp = gridAndGoals.startGoalPoints;
        GridGraph newGridGraph = upscale(gridAndGoals.gridGraph, multiplier, smooth);
        
        return new GridAndGoals(newGridGraph, sgp.sx*multiplier, sgp.sy*multiplier, sgp.ex*multiplier, sgp.ey*multiplier);
    }
    
    public static GridGraph upscale(GridGraph graph, int multiplier, boolean smooth) {
        int sizeX = graph.sizeX * multiplier;
        int sizeY = graph.sizeY * multiplier;
        
        boolean[][] grid = new boolean[sizeY][];
        for (int y=0;y<sizeY;++y) grid[y] = new boolean[sizeX];
        
        for (int y=0;y<sizeY;y+=multiplier) {
            for (int x=0;x<sizeX;x+=multiplier) {
                boolean blocked = graph.isBlocked(x/multiplier, y/multiplier);
                for (int i=0;i<multiplier;++i) {
                    for (int j=0;j<multiplier;++j) {
                        grid[y+i][x+j] = blocked;
                    }
                }
            }
        }
        
        if (smooth) {
            smoothUpscaledMap(multiplier, sizeX, sizeY, grid);
        }
        
        GridGraph newGraph = new GridGraph(sizeX, sizeY);
        for (int y=0;y<sizeY;++y) {
            for (int x=0;x<sizeX;++x) {
                newGraph.setBlocked(x,y,grid[y][x]);
            }
        }
        return newGraph;
    }

    private static void smoothUpscaledMap(int multiplier, int sizeX, int sizeY, boolean[][] grid) {
        int resolution = multiplier-1;
        int width = resolution*2+1;
        int cutoff = width*width/2;
        
        int[][] count = new int[sizeY][];
        for (int y=0;y<sizeY;++y) count[y] = new int[sizeX];
        
        // Compute counts. For now we don't include off-edge tiles in the count.
        AutomataGenerator.runAutomataIterationUnblockedBorders(sizeX, sizeY, resolution, grid, count);
        
        for (int y=0;y<sizeY;++y) {
            for (int x=0;x<sizeX;++x) {
                // Idea: For tiles that are off the edge of the map, we count them as blocked/unblocked tiles depending
                //       on whether the current tile is blocked. (bias towards not changing)
                
                // If grid[y][x] is unblocked, off-map tiles are counted as 0.
                int bias = 0;
                
                if (grid[y][x]) {
                    // If grid[y][x] is blocked, we count the number of off-map tiles that count[y][x] should have included.
                    int blankUp = Math.max(0, 0-y+resolution);
                    int blankLeft = Math.max(0, 0-x+resolution);
                    int blankDown = Math.max(0, y-sizeY+resolution);
                    int blankRight = Math.max(0, x-sizeX+resolution);
                    bias = width*width - (width-blankUp-blankDown)*(width-blankLeft-blankRight);
                }
                
                grid[y][x] = count[y][x] + bias > cutoff;
            }
        }
    }
}
