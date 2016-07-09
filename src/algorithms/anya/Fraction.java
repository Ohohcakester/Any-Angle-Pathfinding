package algorithms.anya;


public final class Fraction implements Comparable<Fraction> {

    public final int n; // numerator. Can be negative
    public final int d; // denominator. Cannot be negative.
    
    /**
     * @param n Integer.
     */
    public Fraction(int n) {
        this.n = n;
        this.d = 1;
    }
    
    public Fraction(int n, int d) {
        //if (d == 0) throw new ArithmeticException("Invalid denominator");
        
        if (d < 0) {
            n = -n;
            d = -d;
        }
        int gcd = gcd(n,d);
        
        this.n = n/gcd;
        this.d = d/gcd;
    }
    
    private Fraction(long n, long d) {
        //if (d == 0) throw new ArithmeticException("Invalid denominator");
        
        if (d < 0) {
            n = -n;
            d = -d;
        }
        long gcd = gcd(n,d);
        
        this.n = (int)(n/gcd);
        this.d = (int)(d/gcd);
    }
    
    public final boolean isWholeNumber() {
        //assert gcd(n,d) == 1;
        return (d == 1);
    }
    
    public final boolean isLessThanOrEqual(Fraction o) {
        return (long)n*o.d - (long)o.n*d <= 0; // n1d2 - n2d1
    }
    
    public final boolean isLessThan(Fraction o) {
        return (long)n*o.d - (long)o.n*d < 0; // n1d2 - n2d1
    }
    
    public final boolean isLessThanOrEqual(int x) {
        return n <= d*x;
    }
    
    public final boolean isLessThan(int x) {
        return n < d*x;
    }
    
    public final Fraction multiplyDivide(int multiply, int divide) {
        return new Fraction((long)n*multiply, (long)d*divide);
    }
    
    public final Fraction multiply(Fraction o) {
        return new Fraction(n*o.n, d*o.d);
    }
    
    public final Fraction divide(Fraction o) {
        return new Fraction(n*o.d, d*o.n);
    }

    public final Fraction minus(Fraction o) {
        return new Fraction(n*o.d - o.n*d, d*o.d);
    }

    public final Fraction minus(int _n, int _d) {
        return new Fraction(n*_d - _n*d, d*_d);
    }

    public final Fraction plus(Fraction o) {
        return new Fraction(n*o.d + o.n*d, d*o.d);
    }

    public final Fraction plus(int _n, int _d) {
        return new Fraction(n*_d + _n*d, d*_d);
    }

    public final Fraction minus(int value) {
        return new Fraction(n - value*d, d);
    }

    public final Fraction plus(int value) {
        return new Fraction(n + value*d, d);
    }
    
    /**
     * @return largest integer leq to this.
     */
    public final int floor() {
        if (d == 1) return n;
        if (n > 0) {
            return n/d;
        } else {
            return (n+1)/d - 1;
        }
    }
    
    /**
     * @return smallest integer geq to this.
     */
    public final int ceil() {
        if (d == 1) return n;
        if (n > 0) {
            return (n-1)/d + 1;
        } else {
            return n/d;
        }
    }
    
    public final float toFloat() {
        return (float)n/d;
    }

    @Override
    public final int compareTo(Fraction o) {
        // this - that.
        // n1/d1 < n2/d2 iff n1d2 < n2d1 as d1,d2 are positive.
        return n*o.d - o.n*d; // n1d2 - n2d1
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (60*n/d);
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        Fraction o = (Fraction)obj;
        return (n*o.d == o.n*d);
    }

    // slightly faster than equals()
    public final boolean isEqualTo(Fraction o) {
        return (n*o.d == o.n*d);
    }

    public final boolean isEqualTo(int k) {
        return (n == k*d);
    }
    
    public static final int gcd(int x, int y) {
        int result = gcdRecurse(x,y);
        return result<0 ? -result : result;
    }
    
    private static final int gcdRecurse(int x, int y) {
        return x == 0 ? y : gcdRecurse(y%x, x);
    }

    
    public static final long gcd(long x, long y) {
        long result = gcdRecurse(x,y);
        return result<0 ? -result : result;
    }
    
    private static final long gcdRecurse(long x, long y) {
        return x == 0 ? y : gcdRecurse(y%x, x);
    }
    
    /*
     * I would switch to using this for GCD, but somehow the recursive version runs consistently faster. Compiler optimisation?
     * See FractionTest.java
     */
    public static final int gcdIterative(int x, int y) {
        while(true) {
            if (x == 0) return y<0?-y:y;
            y %= x;
            if (y == 0) return x<0?-x:x;
            x %= y;
        }
    }

    public static final float length(Fraction width, int height) {
        float fWidth = width.toFloat();
        return (float)Math.sqrt((fWidth*fWidth) + (height*height));
    }

    @Override
    public final String toString() {
        return n+"/"+d;
    }
}
