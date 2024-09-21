public class LamportClock {
    private int time;

    public LamportClock() {
        time = 0;
    }

    public synchronized void update() {
        time++;
    }

    public synchronized void update(int receivedTime) {
        time = Math.max(time, receivedTime) + 1;
    }

    public synchronized int getTime() {
        return time;
    }
}
