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

    public static int[] ticketCheck;
    public static int ticketNumber = 0;

    public static void initialise(int size, float defaultKey) {
        ReusableIndirectHeap.defaultKey = defaultKey;
        
        if (ticketCheck == null || ticketCheck.length != size) {
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
    
    public static float getKey(int index) {
        if (ticketCheck[index] != ticketNumber) return defaultKey;
        return keyList[index];
    }
    
    public static int getIn(int index) {
        if (ticketCheck[index] != ticketNumber) return index;
        return inList[index];
    }
    
    public static int getOut(int index) {
        if (ticketCheck[index] != ticketNumber) return index;
        return outList[index];
    }
    
    public static void setKey(int index, float value) {
        if (ticketCheck[index] != ticketNumber) {
            keyList[index] = value;
            inList[index] = index;
            outList[index] = index;
            ticketCheck[index] = ticketNumber;
        } else {
            keyList[index] = value;
        }
    }
    
    public static void setIn(int index, int value) {
        if (ticketCheck[index] != ticketNumber) {
            keyList[index] = defaultKey;
            inList[index] = value;
            outList[index] = index;
            ticketCheck[index] = ticketNumber;
        } else {
            inList[index] = value;
        }
    }
    
    public static void setOut(int index, int value) {
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
        heapSize = size;
    }

    /**
     * Runtime: O(n)
     */
    /*public void heapify() {
        for (int i=keyList.length/2-1; i>=0; i--) {
            bubbleDown(i);
        }
    }*/
    
    
    private void bubbleUp(int index) {
        if (index == 0) // Reached root
            return;
        
        int parent = (index-1)/2;
        if (getKey(index) < getKey(parent)) {
            // If meets the conditions to bubble up,
            swapData(index,parent);
            bubbleUp(parent);
        }
    }
    
    private void swapData(int a, int b) {
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
    private void swapKey(int i1, int i2) {
        float temp = getKey(i1);
        setKey(i1,getKey(i2));
        setKey(i2,temp);
    }
    
    /**
     * swap integers in list
     */
    private void swapOut(int i1, int i2) {
        int temp = getOut(i1);
        setOut(i1,getOut(i2));
        setOut(i2,temp);
    }
    
    /**
     * swap integers in list
     */
    private void swapIn(int i1, int i2) {
        int temp = getIn(i1);
        setIn(i1,getIn(i2));
        setIn(i2,temp);
    }
    
    private int smallerNode(int index1, int index2) {
        if (index1 >= heapSize) {
            if (index2 >= heapSize)
                return -1;
            
            return index2;
        }
        if (index2 >= heapSize)
            return index1;
        
        return getKey(index1) < getKey(index2) ? index1 : index2;
    }
    
    private void bubbleDown(int index) {
        int leftChild = leftChild(index);
        int rightChild = rightChild(index);
        
        int smallerChild = smallerNode(leftChild, rightChild);
        if (smallerChild == -1) return;
        
        if (getKey(index) > getKey(smallerChild)) {
            // If meets the conditions to bubble down,
            swapData(index,smallerChild);
            bubbleDown(smallerChild);
        }
    }

    /**
     * Runtime: O(lgn)
     */
    public void decreaseKey(int outIndex, float newKey) {
        // Assume newKey < old key
        //System.out.println(keyList);
        //System.out.println(inList);
        //System.out.println(outList);
        int inIndex = getIn(outIndex);
        setKey(inIndex,newKey);
        bubbleUp(inIndex);
    }
    
    public float getMinValue() {
        return getKey(0);
    }

    /**
     * Runtime: O(lgn)
     * @return index of min element
     */
    public int popMinIndex() {
        if (heapSize == 0)
            throw new NullPointerException("Indirect Heap is empty!");
        else if (heapSize == 1) {
            int s = getOut(0);
            setIn(s,-1);
            heapSize--;
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
        int lastIndex = heapSize-1;
        
        int s = getOut(0);
        int t = getOut(lastIndex);
        
        setKey(0,getKey(lastIndex));
        setIn(s,-1);
        setIn(t,0);
        setOut(0,getOut(lastIndex));
        
        heapSize--;
        
        bubbleDown(0);
        
        return s;
    }

    
    public String arrayToString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<heapSize; i++) {
            sb.append(i);
            sb.append(" ");
            sb.append(getKey(i));
            sb.append("\n");
        }
        return sb.toString();
    }
    
    private static int parent(int index) {
        return (index-1)/2;
    }
    
    private static int leftChild(int index) {
        return 2*index+1;
    }
    
    private static int rightChild(int index) {
        return 2*index+2;
    }
    
    public boolean isEmpty() {
        return heapSize <= 0;
    }
}