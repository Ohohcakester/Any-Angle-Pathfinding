package draw;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

import algorithms.anya.Fraction;

public class GridLineSet {

    private ArrayList<Line> lineList;
    private ArrayList<FractionLine> fractionLineList;

    public class Line {
        public final Color color;
        public final int x1;
        public final int y1;
        public final int x2;
        public final int y2;

        public Line(int x1, int y1, int x2, int y2, Color color) {
            this.color = color;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }
    
    public class FractionLine {
        public final Color color;
        public final Fraction x1;
        public final Fraction y1;
        public final Fraction x2;
        public final Fraction y2;

        public FractionLine(Fraction x1, Fraction y1, Fraction x2,
                Fraction y2, Color color) {
            this.color = color;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }
    
    public GridLineSet() {
        lineList = new ArrayList<>();
        fractionLineList = new ArrayList<>();
    }

    public void addLine(int x1, int y1, int x2, int y2, Color color) {
        lineList.add(new Line(x1,y1,x2,y2,color));
    }

    public void addLine(Fraction x1, Fraction y1, Fraction x2, Fraction y2, Color color) {
        fractionLineList.add(new FractionLine(x1,y1,x2,y2,color));
    }
    
    public Collection<Line> getLineList() {
        return lineList;
    }
    
    public Collection<FractionLine> getFractionLineList() {
        return fractionLineList;
    }
}
