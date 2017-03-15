package pgraph.base;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: dindaro
 * Date: 13.01.2013
 * Time: 19:48
 *
 * Base edge class
 */
public abstract  class BaseEdge implements Comparable<BaseEdge>
{
    public long id=-1;
    public BaseVertex start ;
    public BaseVertex end ;
    public double weight;
    public double otherCost= 0;
    public double zoneCost = 0;


    /**
     * Constructor for BaseEdge
     * @param id
     * @param start
     * @param end
     * @param weight
     */
    protected BaseEdge(long id, BaseVertex start, BaseVertex end, double weight) {

        this.id = id;
        this.start = start;
        this.end = end;
        this.weight = weight;
    }

    public boolean equals(BaseEdge o) {
        return id == o.id;
    }

    /**
     * Returns the euclidian length of the edge
     * @return
     */
    public abstract double getLength() ;


    /**
     * Returns the weight of the edge which is the cost of traversal
     * @return
     */
    public double getEdgeWeight()
    {
        return weight;
    }

    /**
     * Compares to another edge
     * @param o
     * @return
     */
    @Override
    public int compareTo(BaseEdge o) {
        return Long.compare(this.id,o.id);  //To change body of implemented methods use File | Settings | File Templates.
    }
}
