package grid;

import grid.anya.Point;

public class GridGraph {

    private boolean[][] tiles;
    public final int sizeX;
    public final int sizeY;
    
    private final float SQRT_TWO = (float)Math.sqrt(2);
    
    public GridGraph(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        tiles = new boolean[sizeY][sizeX];
        for (int i = 0; i < sizeY; i++) {
            tiles[i] = new boolean[sizeX];
        }
    }
    
    public void setBlocked(int x, int y, boolean value) {
        tiles[y][x] = value;
    }
    
    public void trySetBlocked(int x, int y, boolean value) {
        if (isValidBlock(x,y))
            tiles[y][x] = value;
    }
    
    public boolean isBlocked(int x, int y) {
        if (x >= sizeX || y >= sizeY) return true;
        if (x < 0 || y < 0) return true;
        return tiles[y][x];
    }
    
    public boolean isValidCoordinate(int x, int y) {
        return (x <= sizeX && y <= sizeY &&
                x >= 0 && y >= 0);
    }
    
    public boolean isValidBlock(int x, int y) {
        return (x < sizeX && y < sizeY &&
                x >= 0 && y >= 0);
    }
    
    /**
     * x1,y1,x2,y2 refer to the top left corner of the tile.
     * @param x1 0 <= x1 <= sizeX
     * @param y1 0 <= y1 <= sizeY
     * @param x2 0 <= x2 <= sizeX
     * @param y2 0 <= y2 <= sizeY
     * @return distance.
     */
    public float distance(int x1, int y1, int x2, int y2) {
        int xDiff = x2 - x1;
        int yDiff = y2 - y1;
        
        if (xDiff == 0) {
            return (float)Math.abs(yDiff);
        }
        if (yDiff == 0) {
            return (float)Math.abs(xDiff);
        }
        if (xDiff == yDiff || xDiff == -yDiff) {
            return SQRT_TWO*Math.abs(xDiff);
        }
        
        int squareDistance = xDiff*xDiff + yDiff*yDiff;
        
        return (float)Math.sqrt(squareDistance);
    }
    
    
    
    public boolean lineOfSight(int x1, int y1, int x2, int y2) {
        int dy = y2 - y1;
        int dx = x2 - x1;
        
        int f = 0;

        int signY = 1;
        int signX = 1;
        int offsetX = 0;
        int offsetY = 0;
        
        if (dy < 0) {
            dy *= -1;
            signY = -1;
            offsetY = -1;
        }
        if (dx < 0) {
            dx *= -1;
            signX = -1;
            offsetX = -1;
        }
        
        if (dx >= dy) {
            while (x1 != x2) {
                f += dy;
                if (f >= dx) {
                    if (isBlocked(x1 + offsetX, y1 + offsetY))
                        return false;
                    y1 += signY;
                    f -= dx;
                }
                if (f != 0 && isBlocked(x1 + offsetX, y1 + offsetY))
                    return false;
                if (dy == 0 && isBlocked(x1 + offsetX, y1) && isBlocked(x1 + offsetX, y1 - 1))
                    return false;
                
                x1 += signX;
            }
        }
        else {
            while (y1 != y2) {
                f += dx;
                if (f >= dy) {
                    if (isBlocked(x1 + offsetX, y1 + offsetY))
                        return false;
                    x1 += signX;
                    f -= dy;
                }
                if (f != 0 && isBlocked(x1 + offsetX, y1 + offsetY))
                    return false;
                if (dx == 0 && isBlocked(x1, y1 + offsetY) && isBlocked(x1 - 1, y1 + offsetY))
                    return false;
                
                y1 += signY;
            }
        }
        return true;
    }

    public Point findFirstBlockedTile(int x1, int y1, int dx, int dy) {
        
        int f = 0;

        int signY = 1;
        int signX = 1;
        int offsetX = 0;
        int offsetY = 0;
        
        if (dy < 0) {
            dy *= -1;
            signY = -1;
            offsetY = -1;
        }
        if (dx < 0) {
            dx *= -1;
            signX = -1;
            offsetX = -1;
        }
        
        if (dx >= dy) {
            while (true) {
                f += dy;
                if (f >= dx) {
                    if (isBlocked(x1 + offsetX, y1 + offsetY))
                        return new Point(x1+offsetX, y1+offsetY);
                    y1 += signY;
                    f -= dx;
                }
                if (f != 0 && isBlocked(x1 + offsetX, y1 + offsetY))
                    return new Point(x1+offsetX, y1+offsetY);
                if (dy == 0 && isBlocked(x1 + offsetX, y1) && isBlocked(x1 + offsetX, y1 - 1))
                    return new Point(x1+offsetX, -1);
                
                x1 += signX;
            }
        }
        else {
            while (true) {
                f += dx;
                if (f >= dy) {
                    if (isBlocked(x1 + offsetX, y1 + offsetY))
                        return new Point(x1+offsetX, y1+offsetY);
                    x1 += signX;
                    f -= dy;
                }
                if (f != 0 && isBlocked(x1 + offsetX, y1 + offsetY))
                    return new Point(x1+offsetX, y1+offsetY);
                if (dx == 0 && isBlocked(x1, y1 + offsetY) && isBlocked(x1 - 1, y1 + offsetY))
                    return new Point(-1, y1+offsetY);
                
                y1 += signY;
            }
        }
        //return null;
    }
}
