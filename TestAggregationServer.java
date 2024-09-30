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

    // Test for PUT request
    @Test
    public void testPutRequest() throws IOException {
        try (Socket socket = new Socket("localhost", 4567);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send PUT request with personalized weather data
            String jsonData = "{\"id\": \"a1938593\", \"name\": \"Tahmina Ahmed\", \"air_temp\": 13.3, \"apparent_t\": 9.5, " +
                    "\"cloud\": \"Partly cloudy\", \"dewpt\": 5.7, \"press\": 1023.9, \"rel_hum\": 60, \"wind_dir\": \"S\", \"wind_spd_kmh\": 15, \"wind_spd_kt\": 8}";
            out.println("PUT /weather.json HTTP/1.1");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + jsonData.length());
            out.println();
            out.println(jsonData);
            out.flush();  // Ensure the data is sent immediately

            // Read response
            String response = in.readLine();
            assertTrue(response.contains("200 OK"), "Expected HTTP 200 OK response");

            // Optionally, validate that the server correctly stored the data (this would need to be checked with a GET request).
        }
    }

    // Test for GET request to retrieve the weather data
    @Test
    public void testGetRequest() throws IOException {
        try (Socket socket = new Socket("localhost", 4567);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send GET request
            out.println("GET /weather.json HTTP/1.1");
            out.println();
            out.flush();

            // Read response and validate content
            String response = in.readLine();
            assertTrue(response.contains("200 OK"), "Expected HTTP 200 OK response");

            String line;
            StringBuilder responseData = new StringBuilder();
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                responseData.append(line);
            }

            // Check if the returned data matches what was sent in the PUT request
            assertTrue(responseData.toString().contains("Tahmina Ahmed"), "Response should contain correct weather data");
            assertTrue(responseData.toString().contains("13.3"), "Response should contain correct temperature data");
        }
    }

    // Test for data expiration after 30 seconds
    @Test
    public void testDataExpiration() throws IOException, InterruptedException {
        // Send PUT request to add data
        testPutRequest();

        // Wait for longer than the expiration time (31 seconds)
        Thread.sleep(31000);

        // Try to retrieve the data with a GET request
        try (Socket socket = new Socket("localhost", 4567);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send GET request
            out.println("GET /weather.json HTTP/1.1");
            out.println();
            out.flush();

            // Read response and validate content
            String response = in.readLine();
            assertTrue(response.contains("200 OK"), "Expected HTTP 200 OK response");

            String line;
            StringBuilder responseData = new StringBuilder();
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                responseData.append(line);
            }

            // Check if the data has expired (response should not contain the expired data)
            assertFalse(responseData.toString().contains("Tahmina Ahmed"), "Expired data should no longer be present in the response");
        }
    }

    // Test for Lamport Clock synchronization
    @Test
    public void testLamportClockSynchronization() throws IOException {
        try (Socket socket = new Socket("localhost", 4567);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send PUT request with Lamport Clock timestamp
            String jsonData = "{\"id\": \"a1938593\", \"name\": \"Tahmina Ahmed\", \"air_temp\": 13.3, \"lamportTimestamp\": 10}";
            out.println("PUT /weather.json HTTP/1.1");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + jsonData.length());
            out.println();
            out.println(jsonData);
            out.flush();

            // Read response
            String response = in.readLine();
            assertTrue(response.contains("200 OK"), "Expected HTTP 200 OK response");

            // Send GET request to ensure the Lamport timestamp was recorded and returned correctly
            out.println("GET /weather.json HTTP/1.1");
            out.println();
            out.flush();

            // Read the response and check that the Lamport Clock was correctly synchronized
            String line;
            StringBuilder responseData = new StringBuilder();
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                responseData.append(line);
            }
            assertTrue(responseData.toString().contains("lamportTimestamp\": 10"), "Expected Lamport Clock synchronization");
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
                e.printStackTrace();
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
