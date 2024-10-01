public class TestLamportClock {
    public static void main(String[] args) {
        LamportClock clock = new LamportClock("TestClock");
        System.out.println("Initial Clock Time: " + clock.getTime());

        // Simulate an event
        clock.update();
        System.out.println("Clock Time After Update: " + clock.getTime());

        // Simulate receiving an external message with time 5
        clock.update(5);
        System.out.println("Clock Time After Receiving Time 5: " + clock.getTime());
    }
}
