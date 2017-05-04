package algorithms.jgrapht;

/**
 * Implements a node of the Fibonacci heap. It holds the information necessary
 * for maintaining the structure of the heap. It also holds the reference to the
 * key value (which is used to determine the heap structure).
 *
 * @author Nathan Fiedler
 */
public class FibonacciHeapNode<T>
{
    //~ Instance fields --------------------------------------------------------

    /**
     * Node data.
     */
    T data;

    /**
     * first child node
     */
    FibonacciHeapNode<T> child;

    /**
     * left sibling node
     */
    FibonacciHeapNode<T> left;

    /**
     * parent node
     */
    FibonacciHeapNode<T> parent;

    /**
     * right sibling node
     */
    FibonacciHeapNode<T> right;

    /**
     * true if this node has had a child removed since this node was added to
     * its parent
     */
    boolean mark;

    /**
     * key value for this node
     */
    double key;
    double secondaryKey;
    public final static long BIG_ONE = 100000;
    public final static double epsilon = 1/BIG_ONE;

    /**
     * number of children of this node (does not count grandchildren)
     */
    int degree;

    //~ Constructors -----------------------------------------------------------

    /**
     * Default constructor. Initializes the right and left pointers, making this
     * a circular doubly-linked list.
     *
     * @param data data for this node
     */
    public FibonacciHeapNode(T data)
    {
        this.data = data;
    	reset();
    }
    
    protected void reset()
    {
    	parent = null;
    	child = null;
        right = this;
        left = this;
        key = 0;
        secondaryKey = 0;
        degree = 0;
        mark = false;
    }
    
    //~ Methods ----------------------------------------------------------------

    /**
     * Obtain the key for this node.
     *
     * @return the key
     */
    public final double getKey()
    {
        return key;
    }

    public final double getSecondaryKey()
    {
        return secondaryKey;
    }

    /**
     * Obtain the data for this node.
     */
    public final T getData()
    {
        return data;
    }

    /**
     * Return the string representation of this object.
     *
     * @return string representing this object
     */
    public String toString()
    {
        return Double.toString(key);
    }

    /*
     * @return true if this node has a lower priority
     * than @parameter other
     */
    public boolean lessThan(FibonacciHeapNode<T> other)
    {
    	return FibonacciHeapNode.lessThan(
    			this.key, this.secondaryKey,
    			other.key, other.secondaryKey);
    }   
    
    public static boolean lessThan(double pk_a, double sk_a, 
    		double pk_b, double sk_b)
	{
        long tmpKey = (long)(pk_a * FibonacciHeapNode.BIG_ONE + 0.5);
        long tmpOther = (long)(pk_b * FibonacciHeapNode.BIG_ONE + 0.5);
        if(tmpKey < tmpOther)
        {
            return true;
        }

        // tie-break in favour of nodes with higher 
        // secondaryKey values
        if(tmpKey == tmpOther)
        {
            tmpKey = (long)(sk_a * BIG_ONE + 0.5);
            tmpOther = (long)(sk_b * BIG_ONE + 0.5);
            if(tmpKey > tmpOther)
            {
                return true;
            }
        }
        return false;	
	}
    // toString
}

// End FibonacciHeapNode.java
