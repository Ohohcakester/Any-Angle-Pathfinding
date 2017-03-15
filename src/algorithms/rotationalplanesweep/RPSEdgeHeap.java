package algorithms.rotationalplanesweep;

public class RPSEdgeHeap {

    private RPSScanner.Edge[] edges;
    private int heapSize;

    public RPSEdgeHeap(RPSScanner.Edge[] edges) {
        this.edges = edges;
        heapSize = 0;
        for (int i=0; i<edges.length; ++i) {
            edges[i].heapIndex = i;
        }
    }

    public final RPSScanner.Edge[] getEdgeList() {
        return edges;
    }

    public final void clear() {
        heapSize = 0;
    }

    public final int size() {
        return heapSize;
    }

    public final boolean isEmpty() {
        return heapSize == 0;
    }

    public final RPSScanner.Edge getMin() {
        return edges[0];
    }

    public final void delete(RPSScanner.Edge edge, int sx,int sy) {
        // Safety check for Debugging:
        //if (edge.heapIndex >= heapSize) throw new UnsupportedOperationException("ELEMENT NOT IN HEAP: " + edge);

        int currentIndex = edge.heapIndex;
        swap(currentIndex, heapSize-1);
        --heapSize;

        if (currentIndex >= heapSize) return;
        bubbleUp(currentIndex, sx,sy);
        bubbleDown(currentIndex, sx,sy);
    }

    public final void insert(RPSScanner.Edge edge, int sx,int sy) {
        // Safety check for Debugging:
        //if (edge.heapIndex < heapSize) throw new UnsupportedOperationException("ELEMENT ALREADY EXISTS: " + edge);

        swap(edge.heapIndex, heapSize);
        ++heapSize;
        bubbleUp(edge.heapIndex, sx,sy);
    }

    private final void bubbleDown(int i, int sx,int sy) {
        while (true) {
            int left = 2*i + 1;
            if (left >= heapSize) return;

            int right = 2*i + 2;
            
            // Swap with the smaller one
            int swapTarget = right;
            if (right >= heapSize || edges[left].isLessThan(edges[right], sx, sy)) {
                swapTarget = left;
            }

            if (edges[swapTarget].isLessThan(edges[i], sx, sy)) {
                swap(i, swapTarget);
                i = swapTarget;
            } else {
                return;
            }
        }
    }

    private final void bubbleUp(int i, int sx,int sy) {
        while (i > 0) {
            int parent = (i-1)/2;

            if (edges[i].isLessThan(edges[parent], sx, sy)) {
                swap(i, parent);
                i = parent;
            } else {
                break;
            }
        }
    }

    private final void swap(int i, int j) {
        RPSScanner.Edge temp = edges[i];
        edges[i] = edges[j];
        edges[j] = temp;

        edges[i].heapIndex = i;
        edges[j].heapIndex = j;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<heapSize; ++i) {
            RPSScanner.Edge e = edges[i];
            sb.append(e.heapIndex + ": " + e.u.x + ", " + e.u.y + ", " + e.v.x + ", " + e.v.y);
            sb.append("\n");
        }
        return sb.toString();
    }
}