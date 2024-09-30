import org.junit.jupiter.api.*;
import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class TestAggregationServer {

    // Before all tests, ensure the server is running
    @BeforeAll
    public static void setUp() {
        // You can either manually start the Aggregation Server or use a subprocess to start it here
    }

    // Define the missing testGetRequest method
    public void testGetRequest() throws IOException {
        try (Socket socket = new Socket("localhost", 4567);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send GET request
            out.println("GET /weather.json HTTP/1.1");
            out.println();
            out.flush();

            // Read and verify the server response
            String response = in.readLine();
            assertTrue(response.contains("200 OK"), "Expected HTTP 200 OK response");

            // Read the body of the response
            String line;
            StringBuilder responseData = new StringBuilder();
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                responseData.append(line);
            }

            // Verify the response content (modify this to fit your actual test case)
            assertTrue(responseData.toString().contains("Tahmina Ahmed"), "Expected weather data not found");
        }
    }

    // Example PUT request test for reference
    public void testPutRequest() throws IOException {
        try (Socket socket = new Socket("localhost", 4567);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String jsonData = "{\"id\": \"a1938593\", \"name\": \"Tahmina Ahmed\", \"air_temp\": 13.3}";
            out.println("PUT /weather.json HTTP/1.1");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + jsonData.length());
            out.println();
            out.println(jsonData);
            out.flush();

            String response = in.readLine();
            assertTrue(response.contains("200 OK"), "Expected HTTP 200 OK response");
        }
    }

    // Test for handling multiple concurrent clients
    @Test
    public void testMultipleConcurrentClients() throws InterruptedException {
        Runnable clientTask = () -> {
            try {
                testPutRequest();
                testGetRequest();
            } catch (IOException e) {
                // Replacing printStackTrace with more robust logging
                System.err.println("Error during concurrent client request: " + e.getMessage());
            }
        };

        // Create multiple threads (clients) and execute them concurrently
        Thread client1 = new Thread(clientTask);
        Thread client2 = new Thread(clientTask);
        Thread client3 = new Thread(clientTask);

        client1.start();
        client2.start();
        client3.start();

        // Wait for all clients to complete
        client1.join();
        client2.join();
        client3.join();
    }
}
