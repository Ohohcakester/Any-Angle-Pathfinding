package main.testgen;
/**
 * <pre>
 *  SHORTEST, // 0-25th percentile
 *  LOWER,    // 0-60th percentile
 *  MIDDLE,   // 25-75th percentile
 *  HIGHER,   // 40-100th percentile
 *  LONGEST,  // 75th-100th percentile
 *  ALL
 *  </pre>
 */
public enum PathLengthClass {
    SHORTEST, // 0-25th percentile
    LOWER,    // 0-60th percentile
    MIDDLE,   // 25-75th percentile
    HIGHER,   // 40-100th percentile
    LONGEST,  // 75th-100th percentile
    ALL
}