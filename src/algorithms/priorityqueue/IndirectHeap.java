package algorithms.priorityqueue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Indirect binary heap. Used for O(lgn) deleteMin and O(lgn) decreaseKey.
 */
public class IndirectHeap<E extends Comparable<? super E>> implements Serializable {

    private ArrayList<E> keyList;
    private ArrayList<Integer> inList;
    private ArrayList<Integer> outList;
    private boolean minHeap;
    
    private Comparator<E> comparator;
    
    private int compare(E a, E b) {
        if (comparator == null) {
            return a.compareTo(b);
        }
        return comparator.compare(a,b);
    }
    
    public IndirectHeap(boolean minHeap) {
        this.minHeap = minHeap;
        keyList = new ArrayList<>();
        inList = new ArrayList<>();
        outList = new ArrayList<>();
    }

    /**
     * Runtime: O(n)
     */
    public IndirectHeap(E[] array, boolean minHeap) {
        this(minHeap);
        keyList.ensureCapacity(array.length);
        inList.ensureCapacity(array.length);
        outList.ensureCapacity(array.length);
        
        int index = 0;
        for (E e : array) {
            keyList.add(e);
            inList.add(index);
            outList.add(index);
            index++;
        }
    }
    
    /**
     * Increases the capacity of the indirect heap, so that it can hold at
     * least the number of elements specified by capacity without having to
     * reallocate the heap.
     */
    public void reserve(int capacity) {
        keyList.ensureCapacity(capacity);
        inList.ensureCapacity(capacity);
        outList.ensureCapacity(capacity);
    }
    
    /**
     * Returns the handle to the value.
     */
    public int insert(E value) {
        int index = keyList.size();
        
        keyList.add(value);
        inList.add(index);
        outList.add(index);
        bubbleUp(index);
        
        return index;
    }
    
    public void setComparator(Comparator<E> comparator) {
        this.comparator = comparator;
    }

    /**
     * Runtime: O(n)
     */
    public void heapify() {
        for (int i=keyList.size()/2-1; i>=0; i--) {
            bubbleDown(i);
        }
    }
    
    private void bubbleUp(int index) {
        if (index == 0) // Reached root
            return;
        
        int parent = (index-1)/2;
        if ((compare(keyList.get(index), keyList.get(parent)) < 0) == minHeap) {
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
        
        int s = outList.get(a);
        int t = outList.get(b);
        
        swapE(keyList, a, b);
        swapI(inList, s, t);
        swapI(outList, a, b);
    }
    
    /**
     * swap integers in list
     */
    private void swapI(ArrayList<Integer> list, int i1, int i2) {
        Integer temp = list.get(i1);
        list.set(i1,list.get(i2));
        list.set(i2,temp);
    }
    
    /**
     * swap elements in list.
     */
    private void swapE(ArrayList<E> list, int i1, int i2) {
        E temp = list.get(i1);
        list.set(i1,list.get(i2));
        list.set(i2,temp);
    }
    
    
    private int smallerNodeIffMin(int index1, int index2) {
        if (index1 >= keyList.size()) {
            if (index2 >= keyList.size())
                return -1;
            
            return index2;
        }
        if (index2 >= keyList.size())
            return index1;
        
        return ((compare(keyList.get(index1), keyList.get(index2)) < 0) == minHeap) ? index1 : index2;
    }
    
    private void bubbleDown(int index) {
        int leftChild = leftChild(index);
        int rightChild = rightChild(index);
        
        int smallerChild = smallerNodeIffMin(leftChild, rightChild);
        if (smallerChild == -1) return;
        
        if ((compare(keyList.get(index), keyList.get(smallerChild)) > 0) == minHeap) {
            // If meets the conditions to bubble down,
            swapData(index,smallerChild);
            bubbleDown(smallerChild);
        }
    }

    /**
     * Runtime: O(lgn)
     */
    public void decreaseKey(int outIndex, E newKey) {
        // Assume newKey < old key
        //System.out.println(keyList);
        //System.out.println(inList);
        //System.out.println(outList);
        int inIndex = inList.get(outIndex);
        keyList.set(inIndex,newKey);
        bubbleUp(inIndex);
    }
    
    public E getMinValue() {
        return keyList.get(0);
    }

    /**
     * Runtime: O(lgn)
     * @return index of min element
     */
    public int popMinIndex() {
        if (keyList.size() == 0)
            throw new NullPointerException("Indirect Heap is empty!");
        else if (keyList.size() == 1) {
            int s = outList.get(0);
            inList.set(s,-1);
            keyList.remove(0);
            return outList.remove(0);
        }
        // nodeList.size() > 1
        
        // s = Data at 0 = out[0]
        // t = Data at lastIndex = out[lastIndex]
        // key[0] = key[lastIndex], remove key[lastIndex]
        // in[s] = -1
        // in[t] = 0
        // out[0] = out[lastIndex], remove out[lastIndex]
        
        //E temp = keyList.get(0);
        int lastIndex = keyList.size()-1;
        
        int s = outList.get(0);
        int t = outList.get(lastIndex);
        
        keyList.set(0,keyList.get(lastIndex));
        keyList.remove(lastIndex);
        inList.set(s,-1);
        inList.set(t,0);
        outList.set(0,outList.get(lastIndex));
        outList.remove(lastIndex);
        
        bubbleDown(0);
        
        return s;
    }


    /**
     * Runtime: O(lgn)
     * @return value of min element
     */
    public E popMinValue() {
        if (keyList.size() == 0)
            throw new NullPointerException("Indirect Heap is empty!");
        else if (keyList.size() == 1) {
            int s = outList.get(0);
            inList.set(s,-1);
            E value = keyList.get(0);
            keyList.remove(0);
            outList.remove(0);
            return value;
        }
        // nodeList.size() > 1
        
        // s = Data at 0 = out[0]
        // t = Data at lastIndex = out[lastIndex]
        // key[0] = key[lastIndex], remove key[lastIndex]
        // in[s] = -1
        // in[t] = 0
        // out[0] = out[lastIndex], remove out[lastIndex]
        
        //E temp = keyList.get(0);
        int lastIndex = keyList.size()-1;
        
        int s = outList.get(0);
        int t = outList.get(lastIndex);
        
        keyList.set(0,keyList.get(lastIndex));
        E value = keyList.get(lastIndex);
        keyList.remove(lastIndex);
        inList.set(s,-1);
        inList.set(t,0);
        outList.set(0,outList.get(lastIndex));
        outList.remove(lastIndex);
        
        bubbleDown(0);
        
        return value;
    }

    
    public String arrayToString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<keyList.size(); i++) {
            sb.append(i);
            sb.append(" ");
            sb.append(keyList.get(i));
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
        return keyList.isEmpty();
    }
}