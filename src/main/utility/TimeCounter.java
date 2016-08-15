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

    public static int counterA;
    public static int counterB;
    public static int counterC;
    public static int counterD;
    public static int counterE;
    
    public static int iterations;
    
    private static boolean isFrozen;
    public static long timeA_freeze;
    public static long timeB_freeze;
    public static long timeC_freeze;
    public static long timeD_freeze;
    public static long timeE_freeze;
    public static int counterA_freeze;
    public static int counterB_freeze;
    public static int counterC_freeze;
    public static int counterD_freeze;
    public static int counterE_freeze;
    public static int iterations_freeze;
    
    public static void reset() {
        unfreeze();
        timeA = 0;
        timeB = 0;
        timeC = 0;
        timeD = 0;
        timeE = 0;
        counterA = 0;
        counterB = 0;
        counterC = 0;
        counterD = 0;
        counterE = 0;
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
        counterA_freeze = counterA;
        counterB_freeze = counterB;
        counterC_freeze = counterC;
        counterD_freeze = counterD;
        counterE_freeze = counterE;
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
        counterA = counterA_freeze;
        counterB = counterB_freeze;
        counterC = counterC_freeze;
        counterD = counterD_freeze;
        counterE = counterE_freeze;
        iterations = iterations_freeze;
    }

    private static double toSecs(long value) {
        return value / 1000000.;
    }

    private static double toSecsPer(long value) {
        return value / 1000000. / iterations;
    }

    private static double per(int value) {
        return (double)value / iterations;
    }
    
    private static void println(StringBuilder sb, String line) {
        sb.append(line).append('\n');
    }

    public static void print() {
        System.out.println(getPrintString());
    }

    public static void printAverage() {
        System.out.println(getPrintAverageString());
    }

    public static String getPrintString() {
        tryRetrieveFrozenValues();
        
        StringBuilder sb = new StringBuilder();
        if (timeA != 0) println(sb, "Time A: " + toSecs(timeA));
        if (timeB != 0) println(sb, "Time B: " + toSecs(timeB));
        if (timeC != 0) println(sb, "Time C: " + toSecs(timeC));
        if (timeD != 0) println(sb, "Time D: " + toSecs(timeD));
        if (timeE != 0) println(sb, "Time E: " + toSecs(timeE));
        if (counterA != 0) println(sb, "Count A: " + counterA);
        if (counterB != 0) println(sb, "Count B: " + counterB);
        if (counterC != 0) println(sb, "Count C: " + counterC);
        if (counterD != 0) println(sb, "Count D: " + counterD);
        if (counterE != 0) println(sb, "Count E: " + counterE);

        println(sb, "Iterations: " + iterations);

        return sb.toString();
    }

    public static String getPrintAverageString() {
        tryRetrieveFrozenValues();
        
        StringBuilder sb = new StringBuilder();
        if (timeA != 0) println(sb, "Time A: " + toSecsPer(timeA));
        if (timeB != 0) println(sb, "Time B: " + toSecsPer(timeB));
        if (timeC != 0) println(sb, "Time C: " + toSecsPer(timeC));
        if (timeD != 0) println(sb, "Time D: " + toSecsPer(timeD));
        if (timeE != 0) println(sb, "Time E: " + toSecsPer(timeE));
        if (counterA != 0) println(sb, "Count A: " + per(counterA));
        if (counterB != 0) println(sb, "Count B: " + per(counterB));
        if (counterC != 0) println(sb, "Count C: " + per(counterC));
        if (counterD != 0) println(sb, "Count D: " + per(counterD));
        if (counterE != 0) println(sb, "Count E: " + per(counterE));

        println(sb, "Iterations: " + iterations);

        return sb.toString();
    }
    
    
}
