package main.testgen;

public class Stringifier {

    public static String makeProblemName(int sx, int sy, int ex, int ey) {
        return sx + "-" + sy + "_" + ex + "-" + ey;
    }

    public static String defaultToString(int seed, int sizeX, int sizeY, int unblockedRatio) {
        StringBuilder sb = new StringBuilder("def_");
        sb.append(intToStr(seed)).append("_");
        sb.append(intToStr(sizeX)).append("_");
        sb.append(intToStr(sizeY)).append("_");
        sb.append(intToStr(unblockedRatio));
        return sb.toString();
    }
    
    public static String automataToString(long seed, int sizeX, int sizeY, int unblockedRatio, int iterations, float resolutionMultiplier, int cutoffOffset, boolean bordersAreBlocked) {
        StringBuilder sb = new StringBuilder("gen_");
        sb.append(sizeX).append("x");
        sb.append(sizeY).append("_");
        sb.append(unblockedRatio).append("_");
        sb.append(iterations).append("_");
        sb.append((int)(resolutionMultiplier*1000)).append("_");
        sb.append(cutoffOffset).append("_");
        sb.append(bordersAreBlocked ? "T" : "F").append("_");
        sb.append(longToStr(seed));
        return sb.toString();
    }
    
    public static String automataDCToString(long seed, int sizeX, int sizeY, float percentBlocked, int iterations, float resolutionMultiplier, boolean bordersAreBlocked) {
        StringBuilder sb = new StringBuilder("autodc_");
        sb.append(sizeX).append("x");
        sb.append(sizeY).append("_");
        sb.append((int)(percentBlocked*1000)).append("_");
        sb.append(iterations).append("_");
        sb.append((int)(resolutionMultiplier*1000)).append("_");
        sb.append(bordersAreBlocked ? "T" : "F").append("_");
        sb.append(longToStr(seed));
        return sb.toString();
    }
    
    public static String mazeMapToString(long seed, int sizeX, int sizeY, int corridorWidth, float connectednessRatio) {
        StringBuilder sb = new StringBuilder("maze_");
        sb.append(sizeX).append("x");
        sb.append(sizeY).append("_");
        sb.append(corridorWidth).append("_");
        sb.append(floatToOneDigitStandardFormStr(connectednessRatio)).append("_");
        sb.append(longToStr(seed));
        return sb.toString();
    }

    public static String defaultToStringReadable(int seed, int sizeX, int sizeY, int unblockedRatio) {
        StringBuilder sb = new StringBuilder("gen_");
        sb.append(sizeX).append("x");
        sb.append(sizeY).append("_");
        sb.append(unblockedRatio).append("_");
        sb.append(intToStr(seed));
        return sb.toString();
    }
    
    public static int[] defaultToParameters(String input) {
        String[] args = input.split("_");
        if (!args[0].equals("def"))
            throw new UnsupportedOperationException("Invalid string: " + input);
        int[] pars = new int[args.length-1];
        for (int i=0; i<pars.length; i++) {
            pars[i] = strToInt(args[i+1]);
        }
        return pars;
    }

    public static String intToStr(int i) {
        StringBuilder sb = new StringBuilder("i");
        while (i != 0) {
            sb.append(digitToChar((i%32+32)%32));
            i = i>>>5;
        }
        return sb.toString();
    }
    
    public static String longToStr(long i) {
        StringBuilder sb = new StringBuilder("i");
        while (i != 0) {
            sb.append(digitToChar((int)(i%32+32)%32));
            i = i>>>5;
        }
        return sb.toString();
    }

    public static String floatToOneDigitStandardFormStr(float f) {
        float EPS = 0.0000001f;

        if (f == 0) return "0E0";
        int digits = 0;
    
        while (Math.abs(f) > 1+EPS) {
            f /= 10;
            digits++;
        }
        while (Math.abs(f) < 1-EPS) {
            f *= 10;
            digits--;
        }
        return (int)(f > 0 ? f+EPS : f-EPS) + "E" + digits;
    }
    
    public static int strToInt(String s) {
        int num = 0;
        for (int i=1; i<s.length(); i++) {
            num += charToDigit(s.charAt(i)) << (5*(i-1));
        }
        return num;
    }
    
    public static char digitToChar(int d) {
        if (d < 0) {
            throw new UnsupportedOperationException("Exceeds range: " + d);
        } else if (d < 26) {
            return (char)('A' + d);
        } else if (d < 36) {
            return (char)('0' + (d-26));
        } else {
            throw new UnsupportedOperationException("Exceeds range: " + d);
        }
    }
    
    public static int charToDigit(char c) {
        if (c >= 'A' && c <= 'Z') {
            return (int)(c-'A');
        } else if (c >= '0' && c <= '9') {
            return (int)(c-'0')+26;
        } else {
            throw new UnsupportedOperationException("Exceeds range: " + c);
        }
        
    }
    
}