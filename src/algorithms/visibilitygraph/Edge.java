package algorithms.visibilitygraph;

public class Edge {

    public final int source;
    public final int dest;
    public final float weight;
    
    public Edge(int source, int dest, float weight) {
        this.source = source;
        this.dest = dest;
        this.weight = weight;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dest;
        return result;
    }

    /**
     * Depends only on destination index.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Edge other = (Edge) obj;
        if (dest != other.dest)
            return false;
        return true;
    }
}
