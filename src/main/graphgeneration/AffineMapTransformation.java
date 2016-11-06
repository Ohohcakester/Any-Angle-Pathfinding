package main.graphgeneration;

import grid.GridAndGoals;
import grid.GridGraph;
import grid.StartGoalPoints;

public class AffineMapTransformation {
    
    public static GridAndGoals transform(GridAndGoals gridAndGoals, AffineTransform transform, boolean smooth) {
        StartGoalPoints sgp = gridAndGoals.startGoalPoints;
        GridGraph newGridGraph = transform(gridAndGoals.gridGraph, transform, smooth);
        
        return new GridAndGoals(newGridGraph, sgp.sx, sgp.sy, sgp.ex, sgp.ey);
    }
    
    public static GridGraph transform(GridGraph graph, AffineTransform transform, boolean smooth) {
        int sizeX = graph.sizeX;
        int sizeY = graph.sizeY;
        int minX,minY,maxX,maxY;
        {
            int x1 = 0;
            int y1 = 0;
            int x2 = sizeX-1;
            int y2 = sizeY-1;

            int c1x = transform.x(x1,y1);
            int c1y = transform.y(x1,y1);
            int c2x = transform.x(x1,y2);
            int c2y = transform.y(x1,y2);
            int c3x = transform.x(x2,y1);
            int c3y = transform.y(x2,y1);
            int c4x = transform.x(x2,y2);
            int c4y = transform.y(x2,y2);
            minX = Math.min(c1x,Math.min(c2x,Math.min(c3x,c4x)));
            minY = Math.min(c1y,Math.min(c2y,Math.min(c3y,c4y)));
            maxX = Math.max(c1x,Math.max(c2x,Math.max(c3x,c4x)));
            maxY = Math.max(c1y,Math.max(c2y,Math.max(c3y,c4y)));
        }

        int newSizeX = maxX - minX + 1;
        int newSizeY = maxY - minY + 1;

        boolean[][] grid = new boolean[newSizeY][newSizeX];
        
        AffineTransform inv = transform.inverse();

        for (int y=minY;y<=maxY;++y) {
            for (int x=minX;x<=maxX;++x) {
                grid[y-minY][x-minX] = graph.isBlocked(inv.x(x,y), inv.y(x,y));
            }
        }
        
        if (smooth) {
            int scale = Math.max(Math.max(newSizeX/sizeX, newSizeY/sizeY),1);
            if (scale > 1) smoothUpscaledMap(scale, sizeX, sizeY, grid);
        }
        
        GridGraph newGraph = new GridGraph(newSizeX, newSizeY);
        for (int y=0;y<newSizeY;++y) {
            for (int x=0;x<newSizeX;++x) {
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
