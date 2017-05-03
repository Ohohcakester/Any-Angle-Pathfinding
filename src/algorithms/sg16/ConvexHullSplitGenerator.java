package algorithms.sg16;

import java.util.Arrays;

import grid.GridGraph;

public class ConvexHullSplitGenerator {

    private static final int MARKED = -1;

    private SG16VisibilityGraph.ConvexHull[] convexHulls;    
    private int nHulls;

    private final GridGraph graph;
    private final int sizeX;
    private final int sizeY;
    private final int sizeXPlusOne;
    private final int sizeYPlusOne;

    private final int startX;
    private final int startY;
    private final int endX;
    private final int endY;

    // How labels work:
    // Labels are attached to tiles.
    // Label 0: an unblocked tile or an already visited blocked tile.
    // Label >0: unvisited blocked tiles
    // Different labels divide connected components.
    private final int[] labels;
    private int nextUnusedLabel = 2;

    private final int[] obstacleIndexes;

    private int[] floodFillX;
    private int[] floodFillY;
    private int floodFillSize;


    /**
     * Builds a convex hull form vertices in an anticlockwise fashion.
     *
     * This is the algorithm described in the paper by [Melkman, 1987]
     * "Online Construction of the Convex Hull of a Simple Polyline"
     * I used the tutorial from http://geomalgorithms.com/a12-_hull-3.html
     */
    private static class ConvexHullBuilder {
        private final int sx;
        private final int sy;

        // Deque data structure
        private int[] xVertices;
        private int[] yVertices;
        private int head; // start index: head
        private int tail; // end index: tail
        private int size;

        // (sx, sy) is the first vertex
        public ConvexHullBuilder(int sx, int sy) {
            xVertices = new int[11];
            yVertices = new int[11];
            head = 0;
            tail = -1;

            this.sx = sx;
            this.sy = sy;
            addLast(sx, sy);
        }

        // returns false iff it is the final vertex that closes the convex hull.
        public final boolean addVertex(int x, int y) {
            if (size <= 2) {
                addLast(x,y);
                if (size == 2) addFirst(x,y);
                return notLastVertex(x,y);
            }

            boolean didSomething = false;
            while (size >= 3 && isAntiClockwise(headNext(), head, x, y)) {
                didSomething = true;
                popFirst();
            }

            while (size >= 3 && isClockwise(tailPrev(), tail, x, y)) {
                didSomething = true;
                popLast();
            }

            if (didSomething) {
                addFirst(x,y);
                addLast(x,y);
            }
            return notLastVertex(x,y);
        }

        private final boolean notLastVertex(int x, int y) {
            return x != sx || y != sy;
        }

        /**             __.
         *               /|
         *              / (2)
         *      (1)    /        Anticlockwise: (1)x(2) > 0
         *  o-------->o
         */
        private final boolean isAntiClockwise(int index1, int index2, int x3, int y3) {
            int x1 = xVertices[index1];
            int y1 = yVertices[index1];
            int x2 = xVertices[index2];
            int y2 = yVertices[index2];

            int dx1 = x2 - x1;
            int dy1 = y2 - y1;
            int dx2 = x3 - x2;
            int dy2 = y3 - y2;

            return dx1*dy2 - dy1*dx2 >= 0;
        }

        private final boolean isClockwise(int index1, int index2, int x3, int y3) {
            int x1 = xVertices[index1];
            int y1 = yVertices[index1];
            int x2 = xVertices[index2];
            int y2 = yVertices[index2];

            int dx1 = x2 - x1;
            int dy1 = y2 - y1;
            int dx2 = x3 - x2;
            int dy2 = y3 - y2;

            return dx1*dy2 - dy1*dx2 <= 0;
        }

        private final void addFirst(int x, int y) {
            maybeExpandArrays();
            --head;
            if (head < 0) head += xVertices.length;
            xVertices[head] = x;
            yVertices[head] = y;
            ++size;
        }

        private final void addLast(int x, int y) {
            maybeExpandArrays();
            ++tail;
            if (tail >= xVertices.length) tail -= xVertices.length;
            xVertices[tail] = x;
            yVertices[tail] = y;
            ++size;
        }

        private final void popFirst() {
            ++head;
            if (head >= xVertices.length) head -= xVertices.length;
            --size;
        }

        private final void popLast() {
            --tail;
            if (tail < 0) tail += xVertices.length;
            --size;
        }

        private final int headNext() {
            int index = head+1;
            if (index >= xVertices.length) index -= xVertices.length;
            return index;
        }

        private final int tailPrev() {
            int index = tail-1;
            if (index < 0) index += xVertices.length;
            return index;
        }

        private final void maybeExpandArrays() {
            if (size < xVertices.length) return;
            int[] newX = new int[xVertices.length*2];
            int[] newY = new int[yVertices.length*2];

            int index = 0;
            if (head <= tail) {
                for (int i=head; i<=tail; ++i) {
                    newX[index] = xVertices[i];
                    newY[index] = yVertices[i];
                    ++index;
                }
            } else {
                for (int i=head; i<xVertices.length; ++i) {
                    newX[index] = xVertices[i];
                    newY[index] = yVertices[i];
                    ++index;
                }
                for (int i=0; i<=tail; ++i) {
                    newX[index] = xVertices[i];
                    newY[index] = yVertices[i];
                    ++index;
                }
            }
            xVertices = newX;
            yVertices = newY;

            head = 0;
            tail = size-1;
        }

        public final SG16VisibilityGraph.ConvexHull makeHull() {
            SG16VisibilityGraph.ConvexHull hull = new SG16VisibilityGraph.ConvexHull();
            hull.xVertices = new int[size-1];
            hull.yVertices = new int[size-1];
            hull.size = size-1;

            // Note we exclude the tail because tail == head.
            int index = 0;
            if (head <= tail) {
                for (int i=head; i<tail; ++i) {
                    hull.xVertices[index] = xVertices[i];
                    hull.yVertices[index] = yVertices[i];
                    ++index;
                }
            } else {
                for (int i=head; i<xVertices.length; ++i) {
                    hull.xVertices[index] = xVertices[i];
                    hull.yVertices[index] = yVertices[i];
                    ++index;
                }
                for (int i=0; i<tail; ++i) {
                    hull.xVertices[index] = xVertices[i];
                    hull.yVertices[index] = yVertices[i];
                    ++index;
                }
            }

            return hull;
        }

        private final void printArrays() {
            StringBuilder sb = new StringBuilder();
            if (head <= tail) {
                for (int i=head; i<=tail; ++i) {
                    sb.append("(" + xVertices[i] + ", " + yVertices[i] + ")");
                }
            } else {
                for (int i=head; i<xVertices.length; ++i) {
                    sb.append("(" + xVertices[i] + ", " + yVertices[i] + ")");
                }
                for (int i=0; i<=tail; ++i) {
                    sb.append("(" + xVertices[i] + ", " + yVertices[i] + ")");
                }
            }
            System.out.println(sb);
        }
    }

    public ConvexHullSplitGenerator(GridGraph graph, int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;

        this.graph = graph;
        this.sizeXPlusOne = graph.sizeX+1;
        this.sizeYPlusOne = graph.sizeY+1;
        this.sizeX = graph.sizeX;
        this.sizeY = graph.sizeY;
        labels = new int[sizeY*sizeX];
        obstacleIndexes = new int[sizeY*sizeX];

        floodFillX = new int[11];
        floodFillY = new int[11];
        floodFillSize = 0;

        convexHulls = new SG16VisibilityGraph.ConvexHull[11];
        nHulls = 0;

        generateConvexHulls();
    }

    private final void initialiseLabels() {
        for (int y=0; y<sizeY; ++y) {
            for (int x=0; x<sizeX; ++x) {
                setLabel(x, y, graph.isBlockedRaw(x, y) ? 1 : 0);
            }
        }
        nextUnusedLabel = 2;
    }

    private final void initialiseObstacleIndexes() {
        for (int y=0; y<sizeY; ++y) {
            for (int x=0; x<sizeX; ++x) {
                obstacleIndexes[y*sizeX + x] = graph.isBlockedRaw(x, y) ? -1 : -2;
            }
        }

        int totalSize = sizeX*sizeY;

        int obstacleIndex = 0;
        for (int i=0; i<totalSize; ++i) {
            if (obstacleIndexes[i] != -1) continue; // visited

            int x = i%sizeX;
            int y = i/sizeX;
            markObstacle(x, y, obstacleIndex);
            
            ++obstacleIndex;
        }
    }

    private final void markObstacle(int px, int py, int index) {
        clearFloodFill();
        addToFloodFill(px, py);
        obstacleIndexes[py*sizeX + px] = index;
        
        int queueHead = 0;
        while (queueHead < floodFillSize) {
            int x = floodFillX[queueHead];
            int y = floodFillY[queueHead];
            ++queueHead;

            int nx, ny;

            nx = x-1; ny = y;
            if (nx >= 0 && obstacleIndexes[ny*sizeX + nx] == -1) {
                obstacleIndexes[ny*sizeX + nx] = index;
                addToFloodFill(nx, ny);
            }

            nx = x+1; ny = y;
            if (nx < sizeX && obstacleIndexes[ny*sizeX + nx] == -1) {
                obstacleIndexes[ny*sizeX + nx] = index;
                addToFloodFill(nx, ny);
            }

            nx = x; ny = y-1;
            if (ny >= 0 && obstacleIndexes[ny*sizeX + nx] == -1) {
                obstacleIndexes[ny*sizeX + nx] = index;
                addToFloodFill(nx, ny);
            }

            nx = x; ny = y+1;
            if (ny < sizeY && obstacleIndexes[ny*sizeX + nx] == -1) {
                obstacleIndexes[ny*sizeX + nx] = index;
                addToFloodFill(nx, ny);
            }
        }
    }

    private final void generateConvexHulls() {
        initialiseObstacleIndexes();
        initialiseLabels();
        int totalSize = sizeX*sizeY;

        for (int i=0; i<totalSize; ++i) {
            if (labels[i] == 0) continue; // visited
            int obstacleIndex = obstacleIndexes[i];

            int x = i%sizeX;
            int y = i/sizeX;


            // We mark the entire blocked tile island as -1 (MARKED)
            // if hasIntersection, the island will be unmarked by the splitting process.
            // if !hasIntersection, the island will be unmarked by the markInteriorAsDone process.
            floodFillMarkEqual(x, y);

            SG16VisibilityGraph.ConvexHull convexHull = generateConvexHull(x, y);
            convexHull.obstacleIndex = obstacleIndex;

            boolean hasIntersection = checkIntersectionAndMaybeSplit(convexHull);
            if (!hasIntersection) {
                markInteriorAsDone(convexHull, x, y);
                addConvexHull(convexHull);
            } else {
                // Try again after splitting.
                --i;
            }
        }
    }

    private final SG16VisibilityGraph.ConvexHull generateConvexHull(int px, int py) {
        int label = getLabel(px, py);

        // guaranteed to be the bottom-left most tile? (smallest y, then smallest x)
        // so grid vertex (px, py) will be a vertex of the final convex hull.
        /*  ___ ___ ___ ___ ___ ___ ___ ___ ___
         * |   |   |   |   |   |   |   |   |   |
         * |___|___|___|___|___|___|___|___|___|
         *             | .(px,py)  |   |   |   |
         *             |___|___|___|___|___|___|
         *          (px,py)                        */

        int prevX = px;
        int prevY = py;
        int nextX = px+1;
        int nextY = py;

        ConvexHullBuilder hullBuilder = new ConvexHullBuilder(prevX, prevY);

        while (hullBuilder.addVertex(nextX, nextY)) {
            int nextPrevX = nextX;
            int nextPrevY = nextY;

            // set nextX, nextY
            if (nextY > prevY) {
                // Up
                if (topRightOfLabelledTile(nextX, nextY, label)) {
                    // Blocked tile on left side.
                    if (!bottomRightOfLabelledTile(nextX, nextY, label)) {
                        // Go left next
                        nextX = nextX - 1;
                        nextY = nextY;
                    } else if (!bottomLeftOfLabelledTile(nextX, nextY, label)) {
                        // Go up next
                        nextX = nextX;
                        nextY = nextY + 1;
                    } else {
                        // Go right next
                        nextX = nextX + 1;
                        nextY = nextY;
                    }

                } else {
                    // Blocked tile on right side
                    if (!bottomLeftOfLabelledTile(nextX, nextY, label)) {
                        // Go right next
                        nextX = nextX + 1;
                        nextY = nextY;
                    } else if (!bottomRightOfLabelledTile(nextX, nextY, label)) {
                        // Go up next
                        nextX = nextX;
                        nextY = nextY + 1;
                    } else {
                        // Go left next
                        nextX = nextX - 1;
                        nextY = nextY;
                    }
                }

            } else if (nextY < prevY) {
                // Down
                if (bottomRightOfLabelledTile(nextX, nextY, label)) {
                    // Blocked tile on left side.
                    if (!topRightOfLabelledTile(nextX, nextY, label)) {
                        // Go left next
                        nextX = nextX - 1;
                        nextY = nextY;
                    } else if (!topLeftOfLabelledTile(nextX, nextY, label)) {
                        // Go down next
                        nextX = nextX;
                        nextY = nextY - 1;
                    } else {
                        // Go right next
                        nextX = nextX + 1;
                        nextY = nextY;
                    }

                } else {
                    // Blocked tile on right side
                    if (!topLeftOfLabelledTile(nextX, nextY, label)) {
                        // Go right next
                        nextX = nextX + 1;
                        nextY = nextY;
                    } else if (!topRightOfLabelledTile(nextX, nextY, label)) {
                        // Go down next
                        nextX = nextX;
                        nextY = nextY - 1;
                    } else {
                        // Go left next
                        nextX = nextX - 1;
                        nextY = nextY;
                    }
                }

            } else if (nextX > prevX) {
                // Right
                if (bottomRightOfLabelledTile(nextX, nextY, label)) {
                    // Blocked tile above.
                    if (!bottomLeftOfLabelledTile(nextX, nextY, label)) {
                        // Go up next
                        nextX = nextX;
                        nextY = nextY + 1;
                    } else if (!topLeftOfLabelledTile(nextX, nextY, label)) {
                        // Go right next
                        nextX = nextX + 1;
                        nextY = nextY;
                    } else {
                        // Go down next
                        nextX = nextX;
                        nextY = nextY - 1;
                    }

                } else {
                    // Blocked tile below
                    if (!topLeftOfLabelledTile(nextX, nextY, label)) {
                        // Go down next
                        nextX = nextX;
                        nextY = nextY - 1;
                    } else if (!bottomLeftOfLabelledTile(nextX, nextY, label)) {
                        // Go right next
                        nextX = nextX + 1;
                        nextY = nextY;
                    } else {
                        // Go up next
                        nextX = nextX;
                        nextY = nextY + 1;
                    }
                }

            } else if (nextX < prevX) {
                // Left
                if (bottomLeftOfLabelledTile(nextX, nextY, label)) {
                    // Blocked tile above.
                    if (!bottomRightOfLabelledTile(nextX, nextY, label)) {
                        // Go up next
                        nextX = nextX;
                        nextY = nextY + 1;
                    } else if (!topRightOfLabelledTile(nextX, nextY, label)) {
                        // Go left next
                        nextX = nextX - 1;
                        nextY = nextY;
                    } else {
                        // Go down next
                        nextX = nextX;
                        nextY = nextY - 1;
                    }

                } else {
                    // Blocked tile below
                    if (!topRightOfLabelledTile(nextX, nextY, label)) {
                        // Go down next
                        nextX = nextX;
                        nextY = nextY - 1;
                    } else if (!bottomRightOfLabelledTile(nextX, nextY, label)) {
                        // Go left next
                        nextX = nextX - 1;
                        nextY = nextY;
                    } else {
                        // Go up next
                        nextX = nextX;
                        nextY = nextY + 1;
                    }
                }

            } else {
                System.out.println("ERROR");
            }

            // set prevX, prevY
            prevX = nextPrevX;
            prevY = nextPrevY;
        }

        return hullBuilder.makeHull();
    }

    private final boolean topRightOfLabelledTile(int x, int y, int label) {
        return x > 0 && y > 0 && (label == getLabel(x-1, y-1));
    }

    private final boolean topLeftOfLabelledTile(int x, int y, int label) {
        return x < sizeX && y > 0 && (label == getLabel(x, y-1));
    }

    private final boolean bottomRightOfLabelledTile(int x, int y, int label) {
        return x > 0 && y < sizeY && (label == getLabel(x-1, y));
    }

    private final boolean bottomLeftOfLabelledTile(int x, int y, int label) {
        return x < sizeX && y < sizeY && (label == getLabel(x, y));
    }

    // Checks if the convex hull intersects any blocked tile.
    // If it does, it splits the island's labels (the island is denoted by (px, py).
    private final boolean checkIntersectionAndMaybeSplit(SG16VisibilityGraph.ConvexHull hull) {
        if (checkIfContainsPointAndMaybeSplit(startX, startY, hull)) return true;
        if (checkIfContainsPointAndMaybeSplit(endX, endY, hull)) return true;

        int size = hull.size;
        int prevX = hull.xVertices[size-1];
        int prevY = hull.yVertices[size-1];
        for (int i=0; i<size; ++i) {
            int currX = hull.xVertices[i];
            int currY = hull.yVertices[i];
            if (checkIntersectionAndMaybeSplit(prevX, prevY, currX, currY)) {
                return true;
            }
            prevX = currX;
            prevY = currY;
        }

        return false;
    }

    private final boolean checkIfContainsPointAndMaybeSplit(int pointX, int pointY, SG16VisibilityGraph.ConvexHull hull) {

        // Check each half-line that makes up the polygon.
        int size = hull.size;
        int prevX = hull.xVertices[size-1];
        int prevY = hull.yVertices[size-1];
        for (int i=0; i<size; ++i) {
            int currX = hull.xVertices[i];
            int currY = hull.yVertices[i];
            
            int dx1 = currX - prevX;
            int dy1 = currY - prevY;
            int dx2 = pointX - prevX;
            int dy2 = pointY - prevY;
            int crossProd = dx1*dy2 - dy1*dx2; // (1)x(2)
            if (crossProd <= 0) return false; // point is outside polygon.

            prevX = currX;
            prevY = currY;
        }

        // Point is within all halfspace. Point must be in polygon.

        // Split
        int newLabel1 = nextUnusedLabel++;
        int newLabel2 = nextUnusedLabel++;

        for (int i=0; i<floodFillSize; ++i) {
            int x = floodFillX[i];
            int y = floodFillY[i];
            int dx = x - pointX;
            int dy = y - pointY;

            setLabel(x, y, ((2*dx+1)*(2*dy+1) > 0) ? newLabel1 : newLabel2);
        }

        return true;
    }

    // Same as above, but for a specific line in the convex hull.
    private final boolean checkIntersectionAndMaybeSplit(int x1, int y1, int x2, int y2) {
        if (lineOfSightIgnoringMarked(x1, y1, x2, y2)) return false;
        // Note: x1, y1, x2, y2 are in grid vertex coordinates.
        // px, py are in tile coordinates.

        // No line of sight. Split.
        float midX = 0.5f*(x1+x2);
        float midY = 0.5f*(y1+y2);

        // convert to tile coordinates and round to nearest tile. (note the two 0.5's cancel themselves out)
        int currX = (int)midX;
        int currY = (int)midY;

        // Rotate anticlockwise by 90deg: (dx, dy) := (-dy, dx)
        float dx = -(y2-y1);
        float dy = (x2-x1);

        int signX = 1;
        int signY = 1;
        if (dx < 0) {
            dx *= -1;
            signX = -1;
        }
        if (dy < 0) {
            dy *= -1;
            signY = -1;
        }

        // Use Bresenham algorithm to find first intersection.
        float xProgress = currX - midX;
        float yProgress = currY - midY;
        
        int moveX, moveY;
        while (!withinTileBounds(currX, currY) || getLabel(currX, currY) != MARKED) {
            float xRelProgress = xProgress*dy - yProgress*dx;

            if (xRelProgress < 0) {
                moveX = signX;
                moveY = 0;
                xProgress += 1;
            } else if (xRelProgress > 0) {
                moveX = 0;
                moveY = signY;
                yProgress += 1;
            } else { // xRelProgress == 0
                moveX = signX;
                moveY = signY;
                xProgress += 1;
                yProgress += 1;
            }

            currX += moveX;
            currY += moveY;
        }

        // currX, currY is now the split point.
        splitCurrentlyMarkedIsland(currX, currY, signX, signY);

        return true;
    }

    private final boolean withinTileBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < sizeX && y < sizeY;
    }

    private final void splitCurrentlyMarkedIsland(int splitX, int splitY, int signX, int signY) {
        int newLabel1 = nextUnusedLabel++;
        int newLabel2 = nextUnusedLabel++;

        for (int i=0; i<floodFillSize; ++i) {
            int x = floodFillX[i];
            int y = floodFillY[i];

            if ((x - splitX)*signX >= 0 && (y - splitY)*signY >= 0) {
                setLabel(x, y, newLabel1);
            } else {
                setLabel(x, y, newLabel2);
            }
        }
    }

    public final boolean lineOfSightIgnoringMarked(int x1, int y1, int x2, int y2) {
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
                    if (isBlockedIgnoringMarked(x1 + offsetX, y1 + offsetY))
                        return false;
                    y1 += signY;
                    f -= dx;
                }
                if (f != 0 && isBlockedIgnoringMarked(x1 + offsetX, y1 + offsetY))
                    return false;
                if (dy == 0 && isBlockedIgnoringMarked(x1 + offsetX, y1) && isBlockedIgnoringMarked(x1 + offsetX, y1 - 1))
                    return false;
                
                x1 += signX;
            }
        }
        else {
            while (y1 != y2) {
                f += dx;
                if (f >= dy) {
                    if (isBlockedIgnoringMarked(x1 + offsetX, y1 + offsetY))
                        return false;
                    x1 += signX;
                    f -= dy;
                }
                if (f != 0 && isBlockedIgnoringMarked(x1 + offsetX, y1 + offsetY))
                    return false;
                if (dx == 0 && isBlockedIgnoringMarked(x1, y1 + offsetY) && isBlockedIgnoringMarked(x1 - 1, y1 + offsetY))
                    return false;
                
                y1 += signY;
            }
        }
        return true;
    }

    private final boolean isBlockedIgnoringMarked(int x, int y) {
        // A blocked tile must be blocked and unmarked.
        return graph.isBlocked(x, y) && !(x >= 0 && x < sizeX && y >= 0 && y < sizeY && getLabel(x, y) == MARKED);
    }

    private final void markInteriorAsDone(SG16VisibilityGraph.ConvexHull hull, int px, int py) {
        int size = hull.size;
        int prevX = hull.xVertices[size-1];
        int prevY = hull.yVertices[size-1];

        clearFloodFill();
        for (int i=0; i<size; ++i) {
            int currX = hull.xVertices[i];
            int currY = hull.yVertices[i];
            addLineToFloodFill(prevX, prevY, currX, currY);
            
            prevX = currX;
            prevY = currY;
        }

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int i=0; i<floodFillSize; ++i) {
            int y = floodFillY[i];
            minY = y < minY ? y : minY;
            maxY = y > maxY ? y : maxY;
        }
        int[] minXs = new int[maxY - minY + 1];
        int[] maxXs = new int[maxY - minY + 1];
        Arrays.fill(minXs, Integer.MAX_VALUE);
        Arrays.fill(maxXs, Integer.MIN_VALUE);

        for (int i=0; i<floodFillSize; ++i) {
            int x = floodFillX[i];
            int y = floodFillY[i] - minY;
            minXs[y] = x < minXs[y] ? x : minXs[y];
            maxXs[y] = x > maxXs[y] ? x : maxXs[y];
        }

        for (int y=minY; y<=maxY; ++y) {
            int yIndex = y-minY;
            int minX = minXs[yIndex];
            int maxX = maxXs[yIndex];
            for (int x=minX; x<=maxX; ++x) {
                setLabel(x, y, 0);
            }
        }
    }

    private final void addLineToFloodFill(int x1, int y1, int x2, int y2) {
        // Horizontal Case
        if (y1 == y2) {
            if (x1 < x2) {
                // going right. Mark above
                for (int x=x1; x<x2; ++x) {
                    addToFloodFill(x, y1);
                }
            } else {
                // going left. Mark below.
                for (int x=x2; x<x1; ++x) {
                    addToFloodFill(x, y1-1);
                }
            }
            return;
        }

        // Vertical Case
        if (x1 == x2) {
            if (y1 < y2) {
                // going up. Mark left
                for (int y=y1; y<y2; ++y) {
                    addToFloodFill(x1-1, y);
                }
            } else {
                // going down. Mark right.
                for (int y=y2; y<y1; ++y) {
                    addToFloodFill(x1, y);
                }
            }
            return;
        }

        // Not horizontal or vertical. do a bresenham scan and mark all intersecting tiles.
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
                    addToFloodFill(x1 + offsetX, y1 + offsetY);
                    y1 += signY;
                    f -= dx;
                }
                if (f != 0) addToFloodFill(x1 + offsetX, y1 + offsetY);
                
                x1 += signX;
            }
        }
        else {
            while (y1 != y2) {
                f += dx;
                if (f >= dy) {
                    addToFloodFill(x1 + offsetX, y1 + offsetY);
                    x1 += signX;
                    f -= dy;
                }
                if (f != 0) addToFloodFill(x1 + offsetX, y1 + offsetY);
                
                y1 += signY;
            }
        }
    }


    private final void addConvexHull(SG16VisibilityGraph.ConvexHull hull) {
        if (nHulls >= convexHulls.length) convexHulls = Arrays.copyOf(convexHulls, convexHulls.length*2);
        convexHulls[nHulls++] = hull;
    }


    private final void clearFloodFill() {
        floodFillSize = 0;
    }

    private final void addToFloodFill(int x, int y) {
        if (floodFillSize >= floodFillX.length) {
            floodFillX = Arrays.copyOf(floodFillX, floodFillX.length*2);
            floodFillY = Arrays.copyOf(floodFillY, floodFillY.length*2);
        }
        floodFillX[floodFillSize] = x;
        floodFillY[floodFillSize] = y;
        ++floodFillSize;
    }

    // floodfill mark everything equal to label(x, y)
    private final void floodFillMarkEqual(int x, int y) {
        int k = getLabel(x, y);
        floodFillMark(x, y, k, k);
    }

    // floodfill mark everything not equal to MARKED
    private final void floodFillMarkUnmarked(int x, int y) {
        floodFillMark(x, y, -1, Integer.MAX_VALUE);
    }

    // != -1: (0, MAX_INT)
    // == k: (k,k)
    private final void floodFillMark(int px, int py, int min, int max) {
        clearFloodFill();
        addToFloodFill(px, py);
        
        int queueHead = 0;
        while (queueHead < floodFillSize) {
            int x = floodFillX[queueHead];
            int y = floodFillY[queueHead];
            ++queueHead;

            int label = getLabel(x, y);
            if (label == MARKED) continue;

            setLabel(x, y, MARKED);
            int nx, ny;

            nx = x-1; ny = y;
            if (nx >= 0) {
                label = getLabel(nx, ny);
                if (min <= label && label <= max) addToFloodFill(nx, ny);
            }

            nx = x+1; ny = y;
            if (nx < sizeX) {
                label = getLabel(nx, ny);
                if (min <= label && label <= max) addToFloodFill(nx, ny);
            }

            nx = x; ny = y-1;
            if (ny >= 0) {
                label = getLabel(nx, ny);
                if (min <= label && label <= max) addToFloodFill(nx, ny);
            }

            nx = x; ny = y+1;
            if (ny < sizeY) {
                label = getLabel(nx, ny);
                if (min <= label && label <= max) addToFloodFill(nx, ny);
            }
        }
    }

    private final void setLabel(int x, int y, int value) {
        labels[y*sizeX + x] = value;
    }

    private final int getLabel(int x, int y) {
        return labels[y*sizeX + x];
    }

    public static SG16VisibilityGraph.ConvexHull[] generate(GridGraph graph, int sx, int sy, int ex, int ey) {
        ConvexHullSplitGenerator generator = new ConvexHullSplitGenerator(graph, sx, sy, ex, ey);
        return Arrays.copyOf(generator.convexHulls, generator.nHulls);
    }
}