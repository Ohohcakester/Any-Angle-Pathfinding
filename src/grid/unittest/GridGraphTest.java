package grid.unittest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import grid.GridGraph;
import main.graphgeneration.DefaultGenerator;

import org.junit.Test;

public class GridGraphTest {

    @Test
    public void testLineOfSight() {
        int sizeX = 30;
        int sizeY = 30;
        GridGraph graph = DefaultGenerator.generateSeededGraphOnly(218421,sizeX,sizeY,5);

        StringBuilder sb = new StringBuilder();
        String row = "";
        for (int y=-1; y<=sizeX; ++y) {
            for (int x=-1; x<=sizeY; ++x) {
                row += graph.isBlocked(x, y) ? "@" : " ";
            }
            sb.append(row + "\n");
            row = "";
        }
        System.out.println(sb);

        for (int y=-1; y<=sizeX; ++y) {
            for (int x=-1; x<=sizeY; ++x) {
                for (int dx=-1;dx<=1;++dx) {
                    for (int dy=-1;dy<=1;++dy) {
                        if (dy == 0 && dx == 0) continue;
                        int px = x+dx;
                        int py = y+dy;
                        //if (graph.isValidCoordinate(px, py)) {
                            boolean blocked1 = graph.lineOfSight(x,y,px,py);
                            boolean blocked2 = graph.neighbourLineOfSight(x,y,px,py);
                            //System.out.println(x + "," + y + " - " + px + "," + py + " : " + blocked1 + " | " + blocked2);
                            assertEquals(blocked2, blocked1);
                        //}
                    }
                }
            }
        }
    }

    @Test
    public void speedTest() {
        int sizeX = 3000;
        int sizeY = 3000;
        GridGraph graph = DefaultGenerator.generateSeededGraphOnly(3123,sizeX,sizeY,4);

        long d1 = System.currentTimeMillis();
        for (int y=-1; y<=sizeX; ++y) {
            for (int x=-1; x<=sizeY; ++x) {
                for (int dx=-1;dx<=1;++dx) {
                    for (int dy=-1;dy<=1;++dy) {
                        if (dy == 0 && dx == 0) continue;
                        int px = x+dx;
                        int py = y+dy;
                        boolean blocked1 = graph.lineOfSight(x,y,px,py);
                    }
                }
            }
        }

        long d2 = System.currentTimeMillis();
        for (int y=-1; y<=sizeX; ++y) {
            for (int x=-1; x<=sizeY; ++x) {
                for (int dx=-1;dx<=1;++dx) {
                    for (int dy=-1;dy<=1;++dy) {
                        if (dy == 0 && dx == 0) continue;
                        int px = x+dx;
                        int py = y+dy;
                        boolean blocked2 = graph.neighbourLineOfSight(x,y,px,py);
                    }
                }
            }
        }
        long d3 = System.currentTimeMillis();
        System.out.println((d2-d1) + " _ " + (d3-d2));
    }

}
