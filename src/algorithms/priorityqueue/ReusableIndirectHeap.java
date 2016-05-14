package algorithms.priorityqueue;


/**
 * Indirect binary heap. Used for O(lgn) deleteMin and O(lgn) decreaseKey.
 */
public class ReusableIndirectHeap {

    private static float[] keyList;
    private static int[] inList;
    private static int[] outList;
    private int heapSize;
        
    private static float defaultKey = Float.POSITIVE_INFINITY;

    private static int[] ticketCheck;
    private static int ticketNumber = 0;
    
    public static final class Context {
        private float[] keyList;
        private int[] inList;
        private int[] outList;
        private int[] ticketCheck;
        private int ticketNumber;
        
        public Context(){};
    }
    
    public static final void loadContext(Context context) {
        ReusableIndirectHeap.keyList = context.keyList;
        ReusableIndirectHeap.inList = context.inList;
        ReusableIndirectHeap.outList = context.outList;
        ReusableIndirectHeap.ticketCheck = context.ticketCheck;
        ReusableIndirectHeap.ticketNumber = context.ticketNumber;
    }
    
    public static final void saveContext(Context context) {
        context.keyList = ReusableIndirectHeap.keyList;
        context.inList = ReusableIndirectHeap.inList;
        context.outList = ReusableIndirectHeap.outList;
        context.ticketCheck = ReusableIndirectHeap.ticketCheck;
        context.ticketNumber = ReusableIndirectHeap.ticketNumber;
    }

    public static void initialise(int size, float defaultKey) {
        ReusableIndirectHeap.defaultKey = defaultKey;
        
        if (ticketCheck == null || ticketCheck.length != size) {
            //System.out.println("REINITIALISE HEAP " + size);
            keyList = new float[size];
            inList = new int[size];
            outList = new int[size];
            ticketCheck = new int[size];
            ticketNumber = 1;
        } else if (ticketNumber == -1) {
            ticketCheck = new int[size];
            ticketNumber = 1;
        } else {
            ticketNumber++;
        }
    }
    
    public static final float getKey(int index) {
        return ticketCheck[index] == ticketNumber ? keyList[index] : defaultKey;
    }
    
    public static final int getIn(int index) {
        return ticketCheck[index] == ticketNumber ? inList[index] : index;
    }
    
    public static final int getOut(int index) {
        return ticketCheck[index] == ticketNumber ? outList[index] : index;
    }
    
    public static final void setKey(int index, float value) {
        if (ticketCheck[index] != ticketNumber) {
            keyList[index] = value;
            inList[index] = index;
            outList[index] = index;
            ticketCheck[index] = ticketNumber;
        } else {
            keyList[index] = value;
        }
    }
    
    public static final void setIn(int index, int value) {
        if (ticketCheck[index] != ticketNumber) {
            keyList[index] = defaultKey;
            inList[index] = value;
            outList[index] = index;
            ticketCheck[index] = ticketNumber;
        } else {
            inList[index] = value;
        }
    }
    
    public static final void setOut(int index, int value) {
        if (ticketCheck[index] != ticketNumber) {
            keyList[index] = defaultKey;
            inList[index] = index;
            outList[index] = value;
            ticketCheck[index] = ticketNumber;
        } else {
            outList[index] = value;
        }
    }

    
    /**
     * Runtime: O(1)
     */
    public ReusableIndirectHeap(int size) {
        initialise(size, Float.POSITIVE_INFINITY);
        heapSize = 0;
    }
    
    /**
     * Runtime: O(1)
     */
    public ReusableIndirectHeap(int size, int memorySize) {
        initialise(memorySize, Float.POSITIVE_INFINITY);
        heapSize = 0;
    }

    /**
     * Runtime: O(n)
     */
    /*public void heapify() {
        for (int i=heapSize/2-1; i>=0; i--) {
            bubbleDown(i);
        }
    }*/
    
    
    private final void bubbleUp(int index) {
        int parent = (index-1) / 2;
        while (index > 0 && getKey(index) < getKey(parent)) {
            // If meets the conditions to bubble up,
            swapData(index, parent);
            index = parent;
            parent = (index-1) / 2;
        }
    }
    
    private final void swapData(int a, int b) {
        // s = Data at a = out[a]
        // t = Data at b = out[b]
        // key[a] <-> key[b]
        // in[s] <-> in[t]
        // out[a] <-> out[b]
        
        int s = getOut(a);
        int t = getOut(b);
        
        swapKey(a, b);
        swapIn(s, t);
        swapOut(a, b);
    }
    
    /**
     * swap integers in list
     */
    private final void swapKey(int i1, int i2) {
        float temp = getKey(i1);
        setKey(i1,getKey(i2));
        setKey(i2,temp);
    }
    
    /**
     * swap integers in list
     */
    private final void swapOut(int i1, int i2) {
        int temp = getOut(i1);
        setOut(i1,getOut(i2));
        setOut(i2,temp);
    }
    
    /**
     * swap integers in list
     */
    private final void swapIn(int i1, int i2) {
        int temp = getIn(i1);
        setIn(i1,getIn(i2));
        setIn(i2,temp);
    }
    
    private final int smallerNode(int index1, int index2) {
        if (index1 >= heapSize) {
            if (index2 >= heapSize)
                return -1;
            
            return index2;
        }
        if (index2 >= heapSize)
            return index1;
        
        return getKey(index1) < getKey(index2) ? index1 : index2;
    }
    
    private final void bubbleDown(int index) {
        int leftChild = 2*index+1;
        int rightChild = 2*index+2;
        int smallerChild = smallerNode(leftChild, rightChild);
        
        while (smallerChild != -1 && getKey(index) > getKey(smallerChild)) {
            // If meets the conditions to bubble down,
            swapData(index,smallerChild);
            
            // Recurse
            index = smallerChild;
            leftChild = 2*index+1;
            rightChild = 2*index+2;
            smallerChild = smallerNode(leftChild, rightChild);
        }
    }

    /**
     * Runtime: O(lgn)
     */
    public final void decreaseKey(int outIndex, float newKey) {
        // Assume newKey < old key
        int inIndex = getIn(outIndex);

        // Optimisation: Jump the newly set value to the bottom of the heap.
        // Faster if there are a lot of POSITIVE_INFINITY values.
        // This is equivalent to an insert operation.
        if (getKey(inIndex) == Float.POSITIVE_INFINITY) {
            swapData(inIndex, heapSize);
            inIndex = heapSize;
            ++heapSize;
        }
        setKey(inIndex,newKey);
        
        bubbleUp(inIndex);
    }
    
    public final float getMinValue() {
        return getKey(0);
    }

    /**
     * Runtime: O(lgn)
     * @return index of min element
     */
    public final int popMinIndex() {
        if (heapSize == 0)
            throw new NullPointerException("Indirect Heap is empty!");
        else if (heapSize == 1) {
            --heapSize;
            return getOut(0);
        }
        // nodeList.size() > 1
        
        // s = Data at 0 = out[0]
        // t = Data at lastIndex = out[lastIndex]
        // key[0] = key[lastIndex], remove key[lastIndex]
        // in[s] = -1
        // in[t] = 0
        // out[0] = out[lastIndex], remove out[lastIndex]
        
        //E temp = keyList.get(0);
        int s = getOut(0);        
        swapData(0, heapSize-1);
        
        --heapSize;
        bubbleDown(0);
        
        return s;
    }


    public String arrayToString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<ticketCheck.length; i++) {
            if (i == heapSize) sb.append("* ");
            sb.append("[");
            sb.append(getOut(i));
            sb.append(" ");
            float val = getKey(i);
            sb.append(val == Float.POSITIVE_INFINITY ? "_" : (int)val);
            sb.append("], ");
        }
        return getIn(1) + " / " + sb.toString();
    }
    
    /*
    private static final int parent(int index) {
        return (index-1)/2;
    }
    
    private static final int leftChild(int index) {
        return 2*index+1;
    }
    
    private static final int rightChild(int index) {
        return 2*index+2;
    }
    */
    
    public final int size() {
        return heapSize;
    }
    
    public final boolean isEmpty() {
        return heapSize <= 0;
    }

    public static void clearMemory() {
        keyList = null;
        inList = null;
        outList = null;
        ticketCheck = null;
        System.gc();
    }
}