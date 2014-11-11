package grid.anya;

import java.util.Comparator;

public class IntervalFValueComparator implements Comparator<Interval> {

    @Override
    public int compare(Interval o1, Interval o2) {
        float result = o1.fValue - o2.fValue;
        if (result < 0) return -1;
        else if (result > 0) return 1;
        return 0;
    }

}
