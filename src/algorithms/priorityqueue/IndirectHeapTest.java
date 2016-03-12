package algorithms.priorityqueue;

import static org.junit.Assert.*;

import org.junit.Test;

public class IndirectHeapTest {

    @Test
    public void test() {
        FastVariableSizeIndirectHeap pq = new FastVariableSizeIndirectHeap(7);
        
        int[] indexes = new int[239];
        for (int i=0;i<61;++i) {
            indexes[i] = pq.insert((i*73)%239);
        }
        
        pq.reserve(200);

        for (int i=61;i<239;++i) {
            indexes[i] = pq.insert((i*73)%239);
        }
        
        pq.reserve(400);

        assertEquals(239, pq.size());

        pq.reserve(1000);

        assertEquals(239, pq.size());
        
        for (int i=0;i<100;++i) {
            assertEquals(i, (int)pq.getMinValue());
            assertEquals(i, (pq.popMinIndex()*73)%239);
        }

        assertEquals(false, pq.isEmpty());
        assertEquals(139, pq.size());
        
        int[] newHandles = new int[139];
        int currIndex = 0;
        
        for (int i=0;i<239;++i) {
            if ((i*73)%239 >= 100) {
                pq.decreaseKey(indexes[i], -1-currIndex);
                newHandles[currIndex++] = indexes[i];
            }
        }
        
        for (int i=0;i<39;++i) {
            assertEquals(i-139, (int)pq.getMinValue());
            assertEquals(newHandles[139-i-1], pq.popMinIndex());
        }
        
        // Now we only have newHandles[0:100] in the pq, with values -1 to -100 respectively.
        // Next we insert values from -0.5 to -100.5 (101 values). newerHandles[0] is -0.5.
        
        int[] newerHandles = new int[101];
        for (int i=0;i<101;++i) {
            newerHandles[i] = pq.insert(-i-0.5f);
        }

        
        assertEquals(201, pq.size());
        
        // Now we pop everything.
        for (int i=0;i<100;++i) {
            assertFloatEquals(-100.5f + i, pq.getMinValue());
            assertEquals(newerHandles[100-i], pq.popMinIndex());

            assertEquals(200-2*i, pq.size());

            assertFloatEquals(-100f + i, pq.getMinValue());
            assertEquals(newHandles[99-i], pq.popMinIndex());

            assertEquals(199-2*i, pq.size());
        }
        
        assertFloatEquals(-0.5f, pq.getMinValue());
        pq.decreaseKey(newerHandles[0], -1000f);
        assertFloatEquals(-1000f, pq.getMinValue());
        assertEquals(newerHandles[0], pq.popMinIndex());

        assertEquals(0, pq.size());
    }
    
    public static void assertFloatEquals(float expected, float actual) {
        assertTrue(Math.abs(expected-actual) < 0.001f);
    }

}