package algorithms.datatypes;

public final class Memory {
    private static float[] distance;
    private static int[] parent;
    private static boolean[] visited;

    private static float defaultDistance = 0;
    private static int defaultParent = -1;
    private static boolean defaultVisited = false;
    
    private static int[] ticketCheck;
    private static int ticketNumber = 0;
    
    private static int size = 0;
    
    public static final class Context {
        private float[] distance;
        private int[] parent;
        private boolean[] visited;
        private float defaultDistance;
        private int defaultParent;
        private boolean defaultVisited;
        private int[] ticketCheck;
        private int ticketNumber;
        private int size;

        public Context(){};
    }
    
    public static final void loadContext(Context context) {
        Memory.distance = context.distance;
        Memory.parent = context.parent;
        Memory.visited = context.visited;
        Memory.defaultDistance = context.defaultDistance;
        Memory.defaultParent = context.defaultParent;
        Memory.defaultVisited = context.defaultVisited;
        Memory.ticketCheck = context.ticketCheck;
        Memory.ticketNumber = context.ticketNumber;
        Memory.size = context.size;
    }
    
    public static final void saveContext(Context context) {
        context.distance = Memory.distance;
        context.parent = Memory.parent;
        context.visited = Memory.visited;
        context.defaultDistance = Memory.defaultDistance;
        context.defaultParent = Memory.defaultParent;
        context.defaultVisited = Memory.defaultVisited;
        context.ticketCheck = Memory.ticketCheck;
        context.ticketNumber = Memory.ticketNumber;
        context.size = Memory.size;
    }
    
    public static final int initialise(int size, float defaultDistance, int defaultParent, boolean defaultVisited) {
        Memory.defaultDistance = defaultDistance;
        Memory.defaultParent = defaultParent;
        Memory.defaultVisited= defaultVisited;
        Memory.size = size;
        
        if (ticketCheck == null || ticketCheck.length != size) {
            //System.out.println("REINITIALISE MEMORY " + size);
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
    
    public static final int currentTicket() {
        return ticketNumber;
    }
    
    public static final int size() {
        return size;
    }
    
    public static final float distance(int index) {
        if (ticketCheck[index] != ticketNumber) return defaultDistance;
        return distance[index];
    }
    
    public static final int parent(int index) {
        if (ticketCheck[index] != ticketNumber) return defaultParent;
        return parent[index];
    }
    
    public static final boolean visited(int index) {
        if (ticketCheck[index] != ticketNumber) return defaultVisited;
        return visited[index];
    }
    
    public static final void setDistance(int index, float value) {
        if (ticketCheck[index] != ticketNumber) {
            distance[index] = value;
            parent[index] = defaultParent;
            visited[index] = defaultVisited;
            ticketCheck[index] = ticketNumber;
        } else {
            distance[index] = value;
        }
    }
    
    public static final void setParent(int index, int value) {
        if (ticketCheck[index] != ticketNumber) {
            distance[index] = defaultDistance;
            parent[index] = value;
            visited[index] = defaultVisited;
            ticketCheck[index] = ticketNumber;
        } else {
            parent[index] = value;
        }
    }
    
    public static final void setVisited(int index, boolean value) {
        if (ticketCheck[index] != ticketNumber) {
            distance[index] = defaultDistance;
            parent[index] = defaultParent;
            visited[index] = value;
            ticketCheck[index] = ticketNumber;
        } else {
            visited[index] = value;
        }
    }

    public static void clearMemory() {
        distance = null;
        parent = null;
        visited = null;
        ticketCheck = null;
        System.gc();
    }
}
