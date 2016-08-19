package main.graphgeneration;

import grid.GridAndGoals;
import grid.GridGraph;
import grid.StartGoalPoints;

public class UpscaledMapGenerator {
    
    public static GridAndGoals upscale(GridAndGoals gridAndGoals, int multiplier) {
        StartGoalPoints sgp = gridAndGoals.startGoalPoints;
        GridGraph newGridGraph = upscale(gridAndGoals.gridGraph, multiplier);
        
        return new GridAndGoals(newGridGraph, sgp.sx*multiplier, sgp.sy*multiplier, sgp.ex*multiplier, sgp.ey*multiplier);
    }
    
    public static GridGraph upscale(GridGraph graph, int multiplier) {
        int sizeX = graph.sizeX * multiplier;
        int sizeY = graph.sizeY * multiplier;
        
        GridGraph newGraph = new GridGraph(sizeX, sizeY);
        for (int y=0;y<sizeY;y+=multiplier) {
            for (int x=0;x<sizeX;x+=multiplier) {
                boolean blocked = graph.isBlocked(x/multiplier, y/multiplier);
                for (int i=0;i<multiplier;++i) {
                    for (int j=0;j<multiplier;++j) {
                        newGraph.setBlocked(x+i, y+j, blocked);
                    }
                }
            }
        }
        
        return newGraph;
    }
    
}
