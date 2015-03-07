package main.testgen;
import grid.GridGraph;

import java.util.Arrays;
import java.util.LinkedList;

import algorithms.datatypes.Point;


/**
 * Test Mazes:                  <br>
 * 1) 30x30, ratio 7            <br>
 * 2) 30x30, ratio 15           <br>
 * 3) 30x30, ratio 50           <br>
 * 4) 100x100, ratio 7          <br>
 * 5) 100x100, ratio 15         <br>
 * 6) 100x100, ratio 50         <br>
 * 7) 500x500, ratio 7          <br>
 * 8) 500x500, ratio 50         <br>
 *                                                              <br>
 * Each maze has 100 test cases as shown in the constructor.    <br>
 * Each test case has:                                          <br>
 * - A start point                                              <br>
 * - A goal point                                               <br>
 * - Length of the optimal path form start to goal.             <br>
 *                                                                  <br>
 * These test cases were auto-generated from the function:          <br>
 * AnyAnglePathfinding.generateRandomTestDataAndPrint(gridGraph);   <br>
 * <br>
 * Instructions for use:
 * <br> (1) Initialise a TestDataLibrary, specify the maze (testIndex) and the PathLengthClass you wish to use.
 * <br> (2) Obtain the graph information using getGraphInfo() and initialise the gridGraph.
 * <br> (3) use the hasNextData() and getNextData() methods to retrieve the test cases one-by-one and test them.
 * <br> (4) use the other "get" methods in this class to obtain statistics about the test cases you used.
 * <br>
 * Note: The AnyAnglePathfinding.runTest() method does the above steps automatically.
 */
public abstract class TestDataLibrary {
    
    private LinkedList<StartEndPointData> dataList;
    private int nData;
    private float lowestComputedLength;
    private float highestComputedLength;
    private float meanComputedLength;
    private float overallMeanLength;
    
    public abstract GridGraph generateGraph();
    public abstract int getNTrials();
    
    protected void setupTest(int[] startXs, int[] startYs, int[] endXs,
            int[] endYs, double[] lengths, float minLength, float maxLength) {
        
        dataList = new LinkedList<>();
        assert startXs.length == startYs.length;
        assert startYs.length == endXs.length;
        assert endXs.length == endYs.length;
        assert endYs.length == lengths.length;

        double sumOverall = 0;
        double sumComputed = 0;
        lowestComputedLength = Float.POSITIVE_INFINITY;
        lowestComputedLength = Float.NEGATIVE_INFINITY;
        
        for (int i=0; i<startXs.length; i++) {
            double length = lengths[i];
            sumOverall += length;
            if (minLength <= lengths[i] && lengths[i] <= maxLength) {
                sumComputed += length;
                if (length < lowestComputedLength) lowestComputedLength = (float)length;
                if (length > lowestComputedLength) lowestComputedLength = (float)length;
                
                Point start = new Point(startXs[i], startYs[i]);
                Point end = new Point(endXs[i], endYs[i]);
                StartEndPointData data = new StartEndPointData(start, end, (float)length);
                dataList.add(data);
            }
        }
        
        nData = dataList.size();
        overallMeanLength = (float)(sumComputed / dataList.size());
        meanComputedLength = (float)(sumOverall / lengths.length);
    }


    /**
     * MUST NOT SORT THE LENGTHS ARRAY.
     */
    protected static float[] computeMinMax(PathLengthClass pathLengthClass,
            double[] lengths) {
        double[] newLengths = Arrays.copyOf(lengths, lengths.length);
        Arrays.sort(newLengths);
        
        float[] minMax = new float[2];

        float lowerPercentile = -1;
        float upperPercentile = -1;
        minMax[0] = Float.NEGATIVE_INFINITY;
        minMax[1] = Float.POSITIVE_INFINITY;
        
        switch(pathLengthClass) {
            case SHORTEST :
                upperPercentile = 0.25f;
                break;
            case LOWER :
                upperPercentile = 0.6f;
                break;
            case MIDDLE :
                lowerPercentile = 0.25f;
                upperPercentile = 0.75f;
                break;
            case HIGHER :
                lowerPercentile = 0.4f;
                break;
            case LONGEST :
                lowerPercentile = 0.75f;
                break;
            case ALL :
                break;
            default :
                break;
        }
        
        if (lowerPercentile > 0) {
            int index = (int)(newLengths.length*lowerPercentile);
            minMax[0] = (float)newLengths[index];
        }
        if (upperPercentile > 0) {
            int index = (int)(newLengths.length*upperPercentile);
            minMax[1] = (float)newLengths[index];
        }
        
        return minMax;
    }

    /**
     * Print statistics regarding the lengths array that has been input.
     */
    public static void analyseArray(double[] lengths) {
        Arrays.sort(lengths);
        System.out.println("Length: " + lengths.length);
        
        double sum = 0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (double length : lengths) {
            sum += length;
            if (length < min) min = length;
            if (length > max) max = length;
        }
        
        double mean = sum / lengths.length;
        
        System.out.println("Min: " + min);
        System.out.println("Max: " + max);
        System.out.println("Mean: " + mean);

        int pL = lengths.length*1/4;
        int pH = lengths.length*3/4;

        System.out.println("Lower: " + lengths[pL]);
        System.out.println("Upper: " + lengths[pH]); 
    }
    
    /**
     * @return true iff there are still remaining test cases.
     */
    public boolean hasNextData() {
        return !dataList.isEmpty();
    }
    
    /**
     * @return Get the data for the next test case.
     */
    public StartEndPointData getNextData() {
        return dataList.poll();
    }
    
    public float getLowestComputedLength() {
        return lowestComputedLength;
    }

    public float getHighestComputedLength() {
        return highestComputedLength;
    }

    public float getMeanComputedLength() {
        return meanComputedLength;
    }

    public float getOverallMeanLength() {
        return overallMeanLength;
    }

    public int getNData() {
        return nData;
    }
}