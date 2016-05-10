package algorithms.anya;

import static org.junit.Assert.*;

import org.junit.Test;

public class FractionTest {

    @Test
    public void test() {
        assertEquals(Fraction.gcd(5, 3), 1);
        assertEquals(Fraction.gcd(5, 10), 5);
        assertEquals(Fraction.gcd(8, 2), 2);
        assertEquals(Fraction.gcd(80, 45), 5);
        assertEquals(Fraction.gcd(-5, 3), 1);
        assertEquals(Fraction.gcd(5, -3), 1);
        assertEquals(Fraction.gcd(5, 0), 5);
        assertEquals(Fraction.gcd(6, -4), 2);
        assertEquals(Fraction.gcd(-24, -18), 6);
        assertEquals(Fraction.gcd(0, 0), 0);
        assertEquals(Fraction.gcd(0, -2), 2);
        assertEquals(Fraction.gcd(-2, -2), 2);
        assertEquals(Fraction.gcd(-18, 35), 1);
        assertEquals(Fraction.gcd(1, 0), 1);
        assertEquals(Fraction.gcd(1, -1), 1);
        assertEquals(Fraction.gcd(-72, -13), 1);

        assertEquals(Fraction.gcdIterative(5, 3), 1);
        assertEquals(Fraction.gcdIterative(5, 10), 5);
        assertEquals(Fraction.gcdIterative(8, 2), 2);
        assertEquals(Fraction.gcdIterative(80, 45), 5);
        assertEquals(Fraction.gcdIterative(-5, 3), 1);
        assertEquals(Fraction.gcdIterative(5, -3), 1);
        assertEquals(Fraction.gcdIterative(5, 0), 5);
        assertEquals(Fraction.gcdIterative(6, -4), 2);
        assertEquals(Fraction.gcdIterative(-24, -18), 6);
        assertEquals(Fraction.gcdIterative(0, 0), 0);
        assertEquals(Fraction.gcdIterative(0, -2), 2);
        assertEquals(Fraction.gcdIterative(-2, -2), 2);
        assertEquals(Fraction.gcdIterative(-18, 35), 1);
        assertEquals(Fraction.gcdIterative(1, 0), 1);
        assertEquals(Fraction.gcdIterative(1, -1), 1);
        assertEquals(Fraction.gcdIterative(-72, -13), 1);
        

        assertEquals(-2, (new Fraction(-6,3)).floor());
        assertEquals(-2, (new Fraction(-5,3)).floor());
        assertEquals(-2, (new Fraction(-4,3)).floor());
        assertEquals(-1, (new Fraction(-3,3)).floor());
        assertEquals(-1, (new Fraction(-2,3)).floor());
        assertEquals(-1, (new Fraction(-1,3)).floor());
        assertEquals(0, (new Fraction(0,3)).floor());
        assertEquals(0, (new Fraction(1,3)).floor());
        assertEquals(0, (new Fraction(2,3)).floor());
        assertEquals(1, (new Fraction(3,3)).floor());
        assertEquals(1, (new Fraction(4,3)).floor());
        assertEquals(1, (new Fraction(5,3)).floor());
        assertEquals(2, (new Fraction(6,3)).floor());


        assertEquals(-2, (new Fraction(-6,3)).ceil());
        assertEquals(-1, (new Fraction(-5,3)).ceil());
        assertEquals(-1, (new Fraction(-4,3)).ceil());
        assertEquals(-1, (new Fraction(-3,3)).ceil());
        assertEquals(0, (new Fraction(-2,3)).ceil());
        assertEquals(0, (new Fraction(-1,3)).ceil());
        assertEquals(0, (new Fraction(0,3)).ceil());
        assertEquals(1, (new Fraction(1,3)).ceil());
        assertEquals(1, (new Fraction(2,3)).ceil());
        assertEquals(1, (new Fraction(3,3)).ceil());
        assertEquals(2, (new Fraction(4,3)).ceil());
        assertEquals(2, (new Fraction(5,3)).ceil());
        assertEquals(2, (new Fraction(6,3)).ceil());

        int a = 0;
        long start, end;
        int itrs = 100000000;
        start = System.nanoTime();
        for (int i=0;i<itrs;++i) {
            a = Fraction.gcdIterative(17349, 583120);
        }
        end = System.nanoTime();
        System.out.println((end-start)/1000000.f);
        
        start = System.nanoTime();
        for (int i=0;i<itrs;++i) {
            a = Fraction.gcd(17349, 583120);
        }
        end = System.nanoTime();
        System.out.println((end-start)/1000000.f);
        System.out.println(a);
    }

}