package org.jgrapht.traverse;

/**
 * Created with IntelliJ IDEA.
 * User: dindar.oz
 * Date: 3/29/13
 * Time: 9:19 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Heuristic<V> {
    public double getValue(V n);
    public double getValue(V n, V t);
}
