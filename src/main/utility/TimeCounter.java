package main.utility;

/**
 * A helper class that can be useful for timing specific aspects of an algorithm.
 * Idea:
 *   long start = System.nanoTime();
 *   long end = System.nanoTime();
 *   TimeCounter.time1 += end - start;
 *   TimeCounter.iterations++;
 */
public class TimeCounter {
    public static long timeA;
    public static long timeB;
    public static long timeC;
    public static long timeD;
    public static long timeE;
    
    public static int iterations;
    
    private static boolean isFrozen;
    public static long timeA_freeze;
    public static long timeB_freeze;
    public static long timeC_freeze;
    public static long timeD_freeze;
    public static long timeE_freeze;
    public static int iterations_freeze;
    
    public static void reset() {
        unfreeze();
        timeA = 0;
        timeB = 0;
        timeC = 0;
        timeD = 0;
        timeE = 0;
        iterations = 0;
    }
    
    /**
     * Temporarily freeze the counter by storing the values.
     */
    public static void freeze() {
        if (isFrozen) return;
        timeA_freeze = timeA;
        timeB_freeze = timeB;
        timeC_freeze = timeC;
        timeD_freeze = timeD;
        timeE_freeze = timeE;
        iterations_freeze = iterations;
        
        isFrozen = true;
    }
    
    /**
     * Unfreeze the counter 
     */
    public static void unfreeze() {
        if (!isFrozen) return;
        tryRetrieveFrozenValues();
        
        isFrozen = false;
    }
    
    private static void tryRetrieveFrozenValues() {
        if (!isFrozen) return;
        timeA = timeA_freeze;
        timeB = timeB_freeze;
        timeC = timeC_freeze;
        timeD = timeD_freeze;
        timeE = timeE_freeze;
        iterations = iterations_freeze;
    }

    private static double toSecs(long value) {
        return value / 1000000000.;
    }

    private static double toSecsPer(long value) {
        return value / 1000000000. / iterations;
    }

    public static void print() {
        tryRetrieveFrozenValues();
        
        System.out.println("Time A: " + toSecs(timeA));
        System.out.println("Time B: " + toSecs(timeB));
        System.out.println("Time C: " + toSecs(timeC));
        System.out.println("Time D: " + toSecs(timeD));
        System.out.println("Time E: " + toSecs(timeE));

        System.out.println("Iterations: " + iterations);
    }

    public static void printAverage() {
        tryRetrieveFrozenValues();
        
        System.out.println("Time A: " + toSecsPer(timeA));
        System.out.println("Time B: " + toSecsPer(timeB));
        System.out.println("Time C: " + toSecsPer(timeC));
        System.out.println("Time D: " + toSecsPer(timeD));
        System.out.println("Time E: " + toSecsPer(timeE));

        System.out.println("Iterations: " + iterations);
    }
    
    
}
