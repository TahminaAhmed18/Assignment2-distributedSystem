import java.io.*;
import java.net.*;
import com.google.gson.*;  // Importing GSON for JSON parsing

public class GETClient {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: GETClient <server> <port>");
            return;
        }

        String server = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(server, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send GET request
            out.println("GET /weather.json HTTP/1.1");
            out.println("Host: " + server);
            out.println("Connection: close");
            out.println();

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
                System.out.println("Error: 400 Bad Request");
                return;
            } else if (statusCode == 500) {
                System.out.println("Error: 500 Internal Server Error");
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
                WeatherData weatherData = gson.fromJson(jsonResponse.toString(), WeatherData.class);

                // Display the weather data
                System.out.println("Weather Data:");
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
            } else {
                System.out.println("No JSON data received.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Class to represent the weather data structure
    static class WeatherData {
        String id;
        String name;
        double air_temp;
        double apparent_t;
        String cloud;
        double dewpt;
        double press;
        int rel_hum;
        String wind_dir;
        int wind_spd_kmh;
        int wind_spd_kt;
    }
}
