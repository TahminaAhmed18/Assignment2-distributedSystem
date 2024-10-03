import com.google.gson.Gson;
import org.junit.Test;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

public class TestAggregationServer {

    @Test
    public void testPutRequest() {
        String server = "localhost";
        int port = 4567;
        String jsonData = "{\n" +
                "\"id\": \"TestID\",\n" +
                "\"name\": \"TestLocation\",\n" +
                "\"air_temp\": 20.0\n" +
                "}";

        try (Socket socket = new Socket(server, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send PUT request
            out.println("PUT /weather.json HTTP/1.1");
            out.println("Host: " + server);
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + jsonData.length());
            out.println();
            out.println(jsonData);
            out.flush();

            // Read and display response
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                System.out.println(responseLine);
            }

        } catch (IOException e) {
            System.err.println("Error sending PUT request: " + e.getMessage());
        }
    }

    @Test
    public void testGetClient() {
        String server = "localhost";
        int port = 4567;

        try (Socket socket = new Socket(server, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send GET request
            out.println("GET /weather.json HTTP/1.1");
            out.println("Host: " + server);
            out.println("Connection: close");
            out.println();
            out.flush();

            // Read and display response headers
            String responseLine;
            boolean isJson = false;
            int statusCode = 0;

            while ((responseLine = in.readLine()) != null && !responseLine.isEmpty()) {
                System.out.println(responseLine);

                // Check for the HTTP status code
                if (responseLine.startsWith("HTTP/1.1")) {
                    String[] statusParts = responseLine.split(" ");
                    statusCode = Integer.parseInt(statusParts[1]);
                }

                // Check if content type is JSON
                if (responseLine.contains("Content-Type: application/json")) {
                    isJson = true;
                }
            }

            // Handle different status codes
            if (statusCode == 400) {
                System.out.println("Error: 400 Bad Request - The server could not understand the request.");
                return;
            } else if (statusCode == 500) {
                System.out.println("Error: 500 Internal Server Error - The server encountered an unexpected condition.");
                return;
            } else if (statusCode != 200) {
                System.out.println("Error: Unexpected response code " + statusCode);
                return;
            }

            // Read the body of the response if it is JSON
            if (isJson) {
                StringBuilder jsonResponse = new StringBuilder();
                String bodyLine;
                while ((bodyLine = in.readLine()) != null) {
                    jsonResponse.append(bodyLine);
                }

                // Parse the JSON response using GSON
                Gson gson = new Gson();
                GETClient.WeatherData[] weatherDataArray = gson.fromJson(jsonResponse.toString(), GETClient.WeatherData[].class);

                // Display the weather data
                System.out.println("Weather Data:");
                for (GETClient.WeatherData weatherData : weatherDataArray) {
                    System.out.println("ID: " + weatherData.id);
                    System.out.println("Name: " + weatherData.name);
                    System.out.println("Air Temperature: " + weatherData.air_temp);
                    System.out.println("Apparent Temperature: " + weatherData.apparent_t);
                    System.out.println("Cloud: " + weatherData.cloud);
                    System.out.println("Dew Point: " + weatherData.dewpt);
                    System.out.println("Pressure: " + weatherData.press);
                    System.out.println("Relative Humidity: " + weatherData.rel_hum);
                    System.out.println("Wind Direction: " + weatherData.wind_dir);
                    System.out.println("Wind Speed (km/h): " + weatherData.wind_spd_kmh);
                    System.out.println("Wind Speed (knots): " + weatherData.wind_spd_kt);
                    System.out.println("---------------------------");
                }
            } else {
                System.out.println("No JSON data received.");
            }

        } catch (ConnectException e) {
            System.err.println("Error: Could not connect to the server at " + server + ":" + port);
        } catch (IOException e) {
            System.err.println("Error: IO exception while communicating with the server - " + e.getMessage());
        }
    }

    @Test
    public void testMultipleConcurrentClients() throws InterruptedException {
        Runnable clientTask = this::testGetClient;

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
