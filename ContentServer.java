import java.io.*;
import java.net.*;

public class ContentServer {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: ContentServer <server> <port> <filepath>");
            return;
        }

        String server = args[0];
        int port = Integer.parseInt(args[1]);
        String filepath = args[2];

        try (Socket socket = new Socket(server, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Read weather data from file
            String jsonData = readWeatherDataFromFile(filepath);

            // Send PUT request with JSON data
            out.println("PUT /weather.json HTTP/1.1");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + jsonData.length());
            out.println();
            out.println(jsonData);

            // Read and display response
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readWeatherDataFromFile(String filepath) {
        return "{\"id\": \"IDS60901\", \"name\": \"Adelaide\", \"air_temp\": 13.3}"; // Example static data
    }
}
