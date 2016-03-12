package algorithms.anya;


public class Fraction implements Comparable<Fraction> {

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
        if (d == 0) throw new ArithmeticException("Invalid denominator");
        
        if (d < 0) {
            n = -n;
            d = -d;
        }
        int gcd = gcd(n,d);
        
        this.n = n/gcd;
        this.d = d/gcd;
    }
    
    public boolean isWholeNumber() {
        assert gcd(n,d) == 1;
        return (d == 1);
    }
    
    public boolean isLessThanOrEqual(Fraction o) {
        return this.compareTo(o) <= 0;
    }
    
    public boolean isLessThan(Fraction o) {
        return this.compareTo(o) < 0;
    }
    
    public boolean isLessThanOrEqual(int x) {
        return n <= d*x;
    }
    
    public boolean isLessThan(int x) {
        return n < d*x;
    }
    
    public Fraction multiplyDivide(int multiply, int divide) {
        return new Fraction(n*multiply, d*divide);
    }
    
    public Fraction multiply(Fraction o) {
        return new Fraction(n*o.n, d*o.d);
    }
    
    public Fraction divide(Fraction o) {
        return new Fraction(n*o.d, d*o.n);
    }

    public Fraction minus(Fraction o) {
        return new Fraction(n*o.d - o.n*d, d*o.d);
    }

    public Fraction plus(Fraction o) {
        return new Fraction(n*o.d + o.n*d, d*o.d);
    }

    public Fraction minus(int value) {
        return new Fraction(n - value*d, d);
    }

    public Fraction plus(int value) {
        return new Fraction(n + value*d, d);
    }
    
    /**
     * @return largest integer leq to this.
     */
    public int floor() {
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
    public int ceil() {
        if (d == 1) return n;
        if (n > 0) {
            return (n-1)/d + 1;
        } else {
            return n/d;
        }
    }
    
    public float toFloat() {
        return (float)n/d;
    }

    @Override
    public int compareTo(Fraction o) {
        // this - that.
        // n1/d1 < n2/d2 iff n1d2 < n2d1 as d1,d2 are positive.
        return n*o.d - o.n*d; // n1d2 - n2d1
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (60*n/d);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        Fraction o = (Fraction)obj;
        return (n*o.d == o.n*d);
    }

    
    public static int gcd(int x, int y) {
        int result = gcdRecurse(x,y);
        return result<0 ? -result : result;
    }
    
    private static int gcdRecurse(int x, int y) {
        return x == 0 ? y : gcdRecurse(y%x, x);
    }

    public static float length(Fraction width, int height) {
        float fWidth = width.toFloat();
        return (float)Math.sqrt((fWidth*fWidth) + (height*height));
    }

    @Override
    public String toString() {
        return n+"/"+d;
    }
}
