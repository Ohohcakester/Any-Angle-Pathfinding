package grid;

import java.util.ArrayList;

import algorithms.datatypes.Point;

public class ReachableNodesFast {
    private int[] queueX;
    private int[] queueY;
    private int queueStart;
    private int queueEnd;
    
    private final GridGraph graph;
    
    public ReachableNodesFast(GridGraph graph) {
        this.graph = graph;
        queueX = new int[11];
        queueY = new int[11];
    }

    public ArrayList<Point> computeReachable(boolean[] visited, int sx, int sy) {
        queueStart = 0;
        queueEnd = 0;
        
        ArrayList<Point> list = new ArrayList<>();
        enqueue(sx, sy);

        while (queueStart != queueEnd) {
            int cx = queueX[queueStart];
            int cy = queueY[queueStart];
            dequeue();

            if (canGoUp(cx,cy)) explore(list,visited,cx,cy+1);
            if (canGoDown(cx,cy)) explore(list,visited,cx,cy-1);
            if (canGoLeft(cx,cy)) explore(list,visited,cx-1,cy);
            if (canGoRight(cx,cy)) explore(list,visited,cx+1,cy);
        }
        
        return list;
    }

    private final void explore(ArrayList<Point> list, boolean[] visited, int px, int py) {
        int index = graph.toOneDimIndex(px, py);
        if (visited[index]) return;
        visited[index] = true;
        enqueue(px, py);
        list.add(new Point(px,py));
    }

    private final boolean canGoUp(int x, int y) {
        return !graph.bottomRightOfBlockedTile(x,y) || !graph.bottomLeftOfBlockedTile(x,y);
    }
    
    private final boolean canGoDown(int x, int y) {
        return !graph.topRightOfBlockedTile(x,y) || !graph.topLeftOfBlockedTile(x,y);
    }
    
    private final boolean canGoLeft(int x, int y) {
        return !graph.topRightOfBlockedTile(x,y) || !graph.bottomRightOfBlockedTile(x,y);
    }
    
    private final boolean canGoRight(int x, int y) {
        return !graph.topLeftOfBlockedTile(x,y) || !graph.bottomLeftOfBlockedTile(x,y);
    }
    
    private final void enqueue(int x, int y) {
        queueX[queueEnd] = x;
        queueY[queueEnd] = y;
        
        queueEnd = (queueEnd + 1)%queueX.length;
        if (queueStart == queueEnd) {
            // queue is full. Need to expand.
            int currLength = queueX.length;
            
            int[] newQueueX = new int[currLength*2];
            int[] newQueueY = new int[currLength*2];
            
            int size = 0;
            for (int i=queueStart;i<currLength;++i) {
                newQueueX[size] = queueX[i];
                newQueueY[size] = queueY[i];
                ++size;
            }
            for (int i=0;i<queueEnd;++i) {
                newQueueX[size] = queueX[i];
                newQueueY[size] = queueY[i];
                ++size;
            }
            //assert size == currLength;
            queueX = newQueueX;
            queueY = newQueueY;
            
            queueStart = 0;
            queueEnd = currLength+1;
        }
    }
    
    private final void dequeue() {
        queueStart = (queueStart + 1)%queueX.length;
    }
}