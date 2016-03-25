package algorithms.datatypes;

public final class Point {
    public final int x;
    public final int y;
    
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        Point other = (Point) obj;
        return x == other.x && y == other.y;
    }
    
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
    
}
