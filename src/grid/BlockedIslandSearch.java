package grid;

public class BlockedIslandSearch {
    private int[] queueX;
    private int[] queueY;
    private int queueStart;
    private int queueEnd;
    
    private final GridGraph graph;
    
    public BlockedIslandSearch(GridGraph graph) {
        this.graph = graph;
        queueX = new int[11];
        queueY = new int[11];
    }

    public int computeIslandSize(boolean[] visited, int sx, int sy, boolean acceptBorderIslands) {
        queueStart = 0;
        queueEnd = 0;
        
        if (!graph.isBlocked(sx, sy)) return 0;
        int islandSize = 1;
        enqueue(sx, sy);

        boolean isBorder = false;
        while (queueStart != queueEnd) {
            int cx = queueX[queueStart];
            int cy = queueY[queueStart];
            dequeue();
            if (cx == 0 || cy == 0 || cx == graph.sizeX-1 || cy == graph.sizeY-1) {
                isBorder = true;
            }
            
            islandSize += tryExplore(visited,cx,cy+1);
            islandSize += tryExplore(visited,cx,cy-1);
            islandSize += tryExplore(visited,cx-1,cy);
            islandSize += tryExplore(visited,cx+1,cy);
        }
        
        //System.out.println(isBorder + " " + islandSize);
        if (isBorder && !acceptBorderIslands) return 0;
        return islandSize;
    }

    private final int tryExplore(boolean[] visited, int px, int py) {
        if (py < 0 || px < 0 || py >= graph.sizeY || px >= graph.sizeX) return 0;
        int index = py*graph.sizeX + px;
        if (!graph.isBlocked(px, py) || visited[index]) return 0;
        visited[index] = true;
        enqueue(px, py);
        return 1;
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
            queueEnd = currLength;
        }
    }
    
    private final void dequeue() {
        queueStart = (queueStart + 1)%queueX.length;
    }
}