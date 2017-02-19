package pgraph.grid;

/**
 * Created with IntelliJ IDEA.
 * User: dindaro
 * Date: 15.01.2013
 * Time: 22:30
 *
 * Lattice Coordinates
 */
public class GridPosition {
    int X;
    int Y;

    public GridPosition() {
        X=0;
        Y=0;
    }

    /**
     * Constructor
     * @param x
     * @param y
     */
    public GridPosition(int x, int y) {
        X = x;
        Y = y;
    }

    /**
     * Getter
     * @return
     */
    public int getX() {
        return X;
    }

    /**
     * Setter
     * @return
     */
    public void setX(int x) {
        X = x;
    }

    /**
     * Getter
     * @return
     */
    public int getY() {
        return Y;
    }

    /**
     * Setter
     * @return
     */
    public void setY(int y) {
        Y = y;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GridPosition that = (GridPosition) o;

        if (X != that.X) return false;
        if (Y != that.Y) return false;

        return true;
    }

    @Override
    public String toString() {
        return "[" + X + "," + Y + "]";
    }

    @Override
    public int hashCode() {
        int result = X;
        result = 31 * result + Y;
        return result;
    }
}
