package grid;

import algorithms.datatypes.Point;

/**
 * Represents the Grid of blocked/unblocked tiles.
 */
public class GridGraph {

    private boolean[][] tiles;
    public final int sizeX;
    public final int sizeY;
    public final int sizeXplusOne;
    
    private final float SQRT_TWO = (float)Math.sqrt(2);
    
    public GridGraph(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeXplusOne = sizeX+1;
        
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

    public int toOneDimIndex(int x, int y) {
        return y*sizeXplusOne + x;
    }
    
    public int toTwoDimX(int index) {
        return index%sizeXplusOne;
    }
    
    public int toTwoDimY(int index) {
        return index/sizeXplusOne;
    }

    public boolean isUnblockedCoordinate(int x, int y) {
        return !topRightOfBlockedTile(x,y) ||
                !topLeftOfBlockedTile(x,y) ||
                !bottomRightOfBlockedTile(x,y) ||
                !bottomLeftOfBlockedTile(x,y);
    }
    
    public boolean topRightOfBlockedTile(int x, int y) {
        return isBlocked(x-1, y-1);
    }

    public boolean topLeftOfBlockedTile(int x, int y) {
        return isBlocked(x, y-1);
    }

    public boolean bottomRightOfBlockedTile(int x, int y) {
        return isBlocked(x-1, y);
    }

    public boolean bottomLeftOfBlockedTile(int x, int y) {
        return isBlocked(x, y);
    }
    
    /**
     * x1,y1,x2,y2 refer to the top left corner of the tile.
     * @param x1 Condition: x1 between 0 and sizeX inclusive.
     * @param y1 Condition: y1 between 0 and sizeY inclusive.
     * @param x2 Condition: x2 between 0 and sizeX inclusive.
     * @param y2 Condition: y2 between 0 and sizeY inclusive.
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

    /**
     * Same as lineOfSight, but only works with a vertex and its 8 immediate neighbours.
     * Also (x1,y1) != (x2,y2)
     */
    public boolean neighbourLineOfSight(int x1, int y1, int x2, int y2) {
        if (x1 == x2) {
            if (y1 > y2) {
                return !isBlocked(x1,y2) || !isBlocked(x1-1,y2);
            } else { // y1 < y2
                return !isBlocked(x1,y1) || !isBlocked(x1-1,y1);
            }
        } else if (x1 < x2) {
            if (y1 == y2) {
                return !isBlocked(x1,y1) || !isBlocked(x1,y1-1);
            } else if (y1 < y2) {
                return !isBlocked(x1,y1);
            } else { // y2 < y1
                return !isBlocked(x1,y2);
            }
        } else { // x2 < x1
            if (y1 == y2) {
                return !isBlocked(x2,y1) || !isBlocked(x2,y1-1);
            } else if (y1 < y2) {
                return !isBlocked(x2,y1);
            } else { // y2 < y1
                return !isBlocked(x2,y2);
            }
        }
    }


    /**
     * @return true iff there is line-of-sight from (x1,y1) to (x2,y2).
     */
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
    

    /**
     * Used by Accelerated A* and MazeAnalysis.
     * leftRange is the number of blocks you can move left before hitting a blocked tile.
     * downRange is the number of blocks you can move down before hitting a blocked tile.
     * For blocked tiles, leftRange, downRange are both -1.
     * 
     * How to use the maxRange property:
     * 
     *  x,y is the starting point.
     *  k is the number of tiles diagonally up-right of the starting point.
     *  int i = x-y+sizeY;
     *  int j = Math.min(x, y);
     *  return maxRange[i][j + k] - k;
     */
    public int[][] computeMaxDownLeftRanges() {
        int[][] downRange = new int[sizeY+1][sizeX+1];
        int[][] leftRange = new int[sizeY+1][sizeX+1];

        for (int y=0; y<sizeY; ++y) {
            if (isBlocked(0, y))
                leftRange[y][0] = -1;
            else 
                leftRange[y][0] = 0;
            
            for (int x=1; x<sizeX; ++x) {
                if (isBlocked(x, y)) {
                    leftRange[y][x] = -1;
                } else {
                    leftRange[y][x] = leftRange[y][x-1] + 1;
                }
            }
        }

        for (int x=0; x<sizeX; ++x) {
            if (isBlocked(x, 0))
                downRange[0][x] = -1;
            else 
                downRange[0][x] = 0;
            
            for (int y=1; y<sizeY; ++y) {
                if (isBlocked(x, y)) {
                    downRange[y][x] = -1;
                } else {
                    downRange[y][x] = downRange[y-1][x] + 1;
                }
            }
        }
        
        for (int x=0; x<sizeX+1; ++x) {
            downRange[sizeY][x] = -1;
            leftRange[sizeY][x] = -1;
        }
        
        for (int y=0; y<sizeY; ++y) {
            downRange[y][sizeX] = -1;
            leftRange[y][sizeX] = -1;
        }
        
        int[][] maxRanges = new int[sizeX+sizeY+1][];
        int maxSize = Math.min(sizeX, sizeY) + 1;
        for (int i=0; i<maxRanges.length; ++i) {
            int currSize = Math.min(i+1, maxRanges.length-i);
            if (maxSize < currSize)
                currSize = maxSize;
            maxRanges[i] = new int[currSize];
            
            int currX = i - sizeY;
            if (currX < 0) currX = 0;
            int currY = currX - i + sizeY;
            for (int j=0; j<maxRanges[i].length; ++j) {
                maxRanges[i][j] = Math.min(downRange[currY][currX], leftRange[currY][currX]);
                currY++;
                currX++;
            }
            
        }
        return maxRanges;
    }
    
    /**
     * @return the percentage of blocked tiles as compared to the total grid size.
     */
    public float getPercentageBlocked() {
        return (float)getNumBlocked() / (sizeX*sizeY);
    }
    
    /**
     * @return the number of blocked tiles in the grid.
     */
    public int getNumBlocked() {
        int nBlocked = 0;
        for (int y=0; y<sizeY; y++) {
            for (int x=0; x<sizeX; x++) {
                if (isBlocked(x, y)) {
                    nBlocked++;
                }
            }
        }
        return nBlocked;
    }
}
