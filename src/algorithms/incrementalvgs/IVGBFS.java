package algorithms.incrementalvgs;

import grid.GridGraph;

public class IVGBFS {
    
    private static final float APPROXIMATION_RATIO = (float)Math.sqrt(2);

    private final IVG visibilityGraph;
    private final int sx;
    private final int sy;
    private final int ex;
    private final int ey;
    
    private int[] queueX;
    private int[] queueY;
    private int[] queueDist;
    private int queueStart;
    private int queueEnd;
    
    private final boolean[][] visited;
    private final int[] xOffsets;
    private final int yOffset;

    public IVGBFS(GridGraph graph, int sx, int sy, int ex, int ey, float existingPathLength, IVG visibilityGraph) {
        this.sx = sx;
        this.sy = sy;
        this.ex = ex;
        this.ey = ey;
        this.visibilityGraph = visibilityGraph;
        queueX = new int[11];
        queueY = new int[11];
        queueDist = new int[11];
        queueStart = 0;
        queueEnd = 0;

        
        GridEllipse ellipse = GridEllipse.looseEllipse(sx, sy, ex, ey, existingPathLength);
        yOffset = ellipse.yMin;
        xOffsets = new int[ellipse.yMax - ellipse.yMin + 1];
        visited = new boolean[xOffsets.length][];
        
        for (int y=0;y<visited.length;++y) {
            xOffsets[y] = ellipse.xLeft[y];
            visited[y] = new boolean[ellipse.xRight[y] - ellipse.xLeft[y] + 1];
            //System.out.println(xOffsets[y] + " " + (xOffsets[y] + visited[y].length - 1));
        }
    }

    public void computePath() {
        enqueue(sx, sy, 0);

        int[] xNeighbours = new int[]{-1,1, 0,0};
        int[] yNeighbours = new int[]{ 0,0,-1,1};
        
        while (queueStart != queueEnd) {
            int cx = queueX[queueStart];
            int cy = queueY[queueStart];
            int distance = queueDist[queueStart] + 1;
            dequeue();

            for (int i=0;i<4;++i) {
                int px = cx+xNeighbours[i];
                int py = cy+yNeighbours[i];

                int indexY = py - yOffset;
                if (indexY < 0 || indexY >= xOffsets.length) continue;
                int indexX = px - xOffsets[indexY];
                if (indexX < 0 || indexX >= visited[indexY].length) continue;
                if (visited[indexY][indexX]) continue;
                visited[indexY][indexX] = true;
                updateHeuristic(distance, px, py);
                enqueue(px, py, distance);
            }
        }
    }
    
    private final void enqueue(int x, int y, int dist) {
        queueX[queueEnd] = x;
        queueY[queueEnd] = y;
        queueDist[queueEnd] = dist;
        
        queueEnd = (queueEnd + 1)%queueX.length;
        if (queueStart == queueEnd) {
            // queue is full. Need to expand.
            int currLength = queueX.length;
            
            int[] newQueueX = new int[currLength*2];
            int[] newQueueY = new int[currLength*2];
            int[] newQueueDist = new int[currLength*2];
            
            int size = 0;
            for (int i=queueStart;i<currLength;++i) {
                newQueueX[size] = queueX[i];
                newQueueY[size] = queueY[i];
                newQueueDist[size] = queueDist[i]; 
                ++size;
            }
            for (int i=0;i<queueEnd;++i) {
                newQueueX[size] = queueX[i];
                newQueueY[size] = queueY[i];
                newQueueDist[size] = queueDist[i]; 
                ++size;
            }
            //assert size == currLength;
            queueX = newQueueX;
            queueY = newQueueY;
            queueDist = newQueueDist;
            
            queueStart = 0;
            queueEnd = currLength+1;
        }
    }
    
    private final void dequeue() {
        queueStart = (queueStart + 1)%queueX.length;
    }

    private final void updateHeuristic(int distance, int x, int y) {
        int index = visibilityGraph.tryGetIndexOf(x,y);
        if (index == -1) return;
        float heuristicValue = distance / APPROXIMATION_RATIO;
        visibilityGraph.setHeuristic(index, heuristicValue);
    }

}
