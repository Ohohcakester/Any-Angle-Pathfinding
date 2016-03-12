package algorithms.priorityqueue;

import java.util.Arrays;


/**
 * Indirect binary heap. Used for O(lgn) deleteMin and O(lgn) decreaseKey.
 */
public class FastVariableSizeIndirectHeap {
    
    private float[] keyList;
    private int[] inList;
    private int[] outList;
    private int heapSize;
    private int nextIndex;
    
    /**
     * Runtime: O(1)
     */
    public FastVariableSizeIndirectHeap(int capacity) {
        keyList = new float[capacity];
        inList = new int[capacity];
        outList = new int[capacity];
        
        heapSize = 0;
        nextIndex = 0;
    }

    public FastVariableSizeIndirectHeap() {
        this(11);
    }

    /**
     * Runtime: O(n)
     */
    /*public void heapify() {
        for (int i=keyList.length/2-1; i>=0; i--) {
            bubbleDown(i);
        }
    }*/

    /**
     * Increases the capacity of the indirect heap, so that it can hold at
     * least the number of elements specified by capacity without having to
     * reallocate the heap.
     */
    public void reserve(int capacity) {
        if (keyList.length < capacity) {
            keyList = Arrays.copyOf(keyList, capacity);
            inList = Arrays.copyOf(inList, capacity);
            outList = Arrays.copyOf(outList, capacity);
        }
    }
    
    /**
     * Returns the handle to the value.
     */
    public int insert(float value) {
        if (nextIndex >= keyList.length) {
            int newLength = keyList.length*2;
            // Too small.
            keyList = Arrays.copyOf(keyList, newLength);
            inList = Arrays.copyOf(inList, newLength);
            outList = Arrays.copyOf(outList, newLength);
        }
        
        int inIndex = heapSize;
        int outIndex = nextIndex;
        
        keyList[heapSize] = value;
        inList[nextIndex] = heapSize;
        outList[heapSize] = nextIndex;
        heapSize++;
        nextIndex++;
        
        bubbleUp(inIndex);
        
        return outIndex;
    }
    
    private void bubbleUp(int index) {
        if (index == 0) // Reached root
            return;
        
        int parent = (index-1)/2;
        if (keyList[index] < keyList[parent]) {
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
        
        int s = outList[a];
        int t = outList[b];
        
        swapKey(a, b);
        swapIn(s, t);
        swapOut(a, b);
    }
    
    /**
     * swap integers in list
     */
    private void swapKey(int i1, int i2) {
        float temp = keyList[i1];
        keyList[i1] = keyList[i2];
        keyList[i2] = temp;
    }
    
    /**
     * swap integers in list
     */
    private void swapOut(int i1, int i2) {
        int temp = outList[i1];
        outList[i1] = outList[i2];
        outList[i2] = temp;
    }
    
    /**
     * swap integers in list
     */
    private void swapIn(int i1, int i2) {
        int temp = inList[i1];
        inList[i1] = inList[i2];
        inList[i2] = temp;
    }
    
    private int smallerNode(int index1, int index2) {
        if (index1 >= heapSize) {
            if (index2 >= heapSize)
                return -1;
            
            return index2;
        }
        if (index2 >= heapSize)
            return index1;
        
        return keyList[index1] < keyList[index2] ? index1 : index2;
    }
    
    private void bubbleDown(int index) {
        int leftChild = leftChild(index);
        int rightChild = rightChild(index);
        
        int smallerChild = smallerNode(leftChild, rightChild);
        if (smallerChild == -1) return;
        
        if (keyList[index] > keyList[smallerChild]) {
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
        int inIndex = inList[outIndex];
        keyList[inIndex] = newKey;
        bubbleUp(inIndex);
    }
    
    public float getMinValue() {
        return keyList[0];
    }
    
    public int getMinIndex() {
        return outList[0];
    }

    /**
     * Runtime: O(lgn)
     * @return index of min element
     */
    public int popMinIndex() {
        if (heapSize == 0)
            throw new NullPointerException("Indirect Heap is empty!");
        else if (heapSize == 1) {
            int s = outList[0];
            inList[s] = -1;
            heapSize--;
            return s;
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
        
        int s = outList[0];
        int t = outList[lastIndex];
        
        keyList[0] = keyList[lastIndex];
        inList[s] =-1;
        inList[t] = 0;
        outList[0] = outList[lastIndex];
        
        heapSize--;
        
        bubbleDown(0);
        
        return s;
    }

    
    public String arrayToString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<heapSize; i++) {
            sb.append(i);
            sb.append(" ");
            sb.append(keyList[i]);
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
    
    public int size() {
        return heapSize;
    }
}