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