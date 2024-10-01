public class LamportClock {
    private int time;
    private final String identifier;  // A unique identifier for each clock (could be client/server ID)

    // Constructor with an identifier (client/server ID)
    public LamportClock(String identifier) {
        this.time = 0;
        this.identifier = identifier;
    }

    // Synchronized method to update the local clock
    public synchronized void update() {
        time++;
    }

    // Synchronized method to update the clock based on a received timestamp and its own time
    public synchronized void update(int receivedTime) {
        time = Math.max(time, receivedTime) + 1;
    }

    // Synchronized method to get the current time
    public synchronized int getTime() {
        return time;
    }

    // Get the clock's unique identifier
    public String getIdentifier() {
        return identifier;
    }

    // Overriding toString() method to print the clock info in a readable format
    @Override
    public String toString() {
        return "LamportClock{" +
                "identifier='" + identifier + '\'' +
                ", time=" + time +
                '}';
    }
}

