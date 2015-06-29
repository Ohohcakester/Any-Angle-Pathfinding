package algorithms.datatypes;

public class Memory {
    private static float[] distance;
    private static int[] parent;
    private static boolean[] visited;

    private static float defaultDistance = 0;
    private static int defaultParent = -1;
    private static boolean defaultVisited = false;
    
    private static int[] ticketCheck;
    private static int ticketNumber = 0;
    
    private static int size = 0;
    
    public static int initialise(int size, float defaultDistance, int defaultParent, boolean defaultVisited) {
        Memory.defaultDistance = defaultDistance;
        Memory.defaultParent = defaultParent;
        Memory.defaultVisited= defaultVisited;
        Memory.size = size;
        
        if (ticketCheck == null || ticketCheck.length != size) {
            distance = new float[size];
            parent = new int[size];
            visited = new boolean[size];
            ticketCheck = new int[size];
            ticketNumber = 1;
        } else if (ticketNumber == -1) {
            ticketCheck = new int[size];
            ticketNumber = 1;
        } else {
            ticketNumber++;
        }
        
        return ticketNumber;
    }
    
    public static int currentTicket() {
        return ticketNumber;
    }
    
    public static int size() {
        return size;
    }
    
    public static float distance(int index) {
        if (ticketCheck[index] != ticketNumber) return defaultDistance;
        return distance[index];
    }
    
    public static int parent(int index) {
        if (ticketCheck[index] != ticketNumber) return defaultParent;
        return parent[index];
    }
    
    public static boolean visited(int index) {
        if (ticketCheck[index] != ticketNumber) return defaultVisited;
        return visited[index];
    }
    
    public static void setDistance(int index, float value) {
        if (ticketCheck[index] != ticketNumber) {
            distance[index] = value;
            parent[index] = defaultParent;
            visited[index] = defaultVisited;
            ticketCheck[index] = ticketNumber;
        } else {
            distance[index] = value;
        }
    }
    
    public static void setParent(int index, int value) {
        if (ticketCheck[index] != ticketNumber) {
            distance[index] = defaultDistance;
            parent[index] = value;
            visited[index] = defaultVisited;
            ticketCheck[index] = ticketNumber;
        } else {
            parent[index] = value;
        }
    }
    
    public static void setVisited(int index, boolean value) {
        if (ticketCheck[index] != ticketNumber) {
            distance[index] = defaultDistance;
            parent[index] = defaultParent;
            visited[index] = value;
            ticketCheck[index] = ticketNumber;
        } else {
            visited[index] = value;
        }
    }
}
