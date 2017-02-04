package main.graphgeneration;

public class AffineTransform {
    double a11;
    double a12;
    double a21;
    double a22;
    double b1;
    double b2;

    private AffineTransform() {
        a11 = 1;
        a22 = 1;
    }

    public static AffineTransform copy(AffineTransform old) {
        AffineTransform transform = new AffineTransform();
        transform.a11 = old.a11;
        transform.a12 = old.a12;
        transform.a21 = old.a21;
        transform.a22 = old.a22;
        transform.b1 = old.b1;
        transform.b2 = old.b2;
        return transform;
    }

    public static AffineTransform identity() {
        return new AffineTransform();
    }

    public static AffineTransform translate(double x, double y) {
        AffineTransform transform = new AffineTransform();
        transform.b1 = x;
        transform.b2 = y;
        return transform;
    }

    public static AffineTransform rotate(double angle) {
        AffineTransform transform = new AffineTransform();
        transform.a11 = Math.cos(angle);
        transform.a12 = -Math.sin(angle);
        transform.a21 = Math.sin(angle);
        transform.a22 = Math.cos(angle);
        return transform;
    }

    public static AffineTransform scale(double factor) {
        AffineTransform transform = new AffineTransform();
        transform.a11 = factor;
        transform.a22 = factor;
        return transform;
    }

    public static AffineTransform scale(double xFactor, double yFactor) {
        AffineTransform transform = new AffineTransform();
        transform.a11 = xFactor;
        transform.a22 = yFactor;
        return transform;
    }

    public static AffineTransform shear(double xSkew) {
        AffineTransform transform = new AffineTransform();
        transform.a12 = xSkew;
        return transform;
    }

    public static AffineTransform compose(AffineTransform t1, AffineTransform t2) {
        AffineTransform transform = new AffineTransform();
        transform.b1 = t1.double_x(t2.b1, t2.b2);
        transform.b2 = t1.double_y(t2.b1, t2.b2);

        transform.a11 = t1.a11*t2.a11 + t1.a12*t2.a21;
        transform.a21 = t1.a21*t2.a11 + t1.a22*t2.a21;

        transform.a12 = t1.a11*t2.a12 + t1.a12*t2.a22;
        transform.a22 = t1.a21*t2.a12 + t1.a22*t2.a22;
        return transform;
    }

    public AffineTransform inverse() {
        AffineTransform transform = new AffineTransform();
        double det = a11*a22 - a12*a21;
        // Ax + b = y  =>  x = inv(A)x + inv(A)b
        transform.a11 = a22/det;
        transform.a12 = -a12/det;
        transform.a21 = -a21/det;
        transform.a22 = a11/det;

        transform.b1 = a11*b1 + a12*b2;
        transform.b2 = a21*b1 + a22*b2;

        return transform;
    }

    int x(int x, int y) {
        return (int)(a11*x + a12*y + b1 + 0.5);
    }

    int y(int x, int y) {
        return (int)(a21*x + a22*y + b2 + 0.5);
    }

    double double_x(double x, double y) {
        return a11*x + a12*y + b1;
    }

    double double_y(double x, double y) {
        return a21*x + a22*y + b2;
    }
}