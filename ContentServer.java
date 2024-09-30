import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ContentServer {

    private static LamportClock lamportClock = new LamportClock("ContentServer");

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

            // Read weather data from the specified file
            String jsonData = readWeatherDataFromFile(filepath);

            if (jsonData == null || jsonData.isEmpty()) {
                System.err.println("Failed to read weather data from file.");
                return;
            }

            // Update Lamport Clock
            lamportClock.update();

            // Send PUT request with JSON data
            out.println("PUT /weather.json HTTP/1.1");
            out.println("Content-Type: application/json");
            out.println("Lamport-Clock: " + lamportClock.getTime());
            out.println("Content-Length: " + jsonData.length());
            out.println();
            out.println(jsonData);
            out.flush();

            // Read and display the response from the server
            handleServerResponse(in);

        } catch (IOException e) {
            System.err.println("Error communicating with server: " + e.getMessage());
        }
    }

    // Reads weather data dynamically from the file with more robust parsing
    private static String readWeatherDataFromFile(String filepath) {
        StringBuilder jsonData = new StringBuilder();
        try (Scanner scanner = new Scanner(new File(filepath))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                // Skip empty or malformed lines
                if (line.isEmpty() || !line.contains(":")) {
                    System.err.println("Skipping malformed or empty line in file: " + line);
                    continue;
                }

                jsonData.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filepath);
            return null;
        }
        return jsonData.toString();
    }

    // Handles the server's response, logging errors if they occur
    private static void handleServerResponse(BufferedReader in) throws IOException {
        String responseLine;
        int statusCode = 0;

        // Read headers
        while ((responseLine = in.readLine()) != null && !responseLine.isEmpty()) {
            System.out.println(responseLine);

            // Check for HTTP status code
            if (responseLine.startsWith("HTTP/1.1")) {
                String[] statusParts = responseLine.split(" ");
                statusCode = Integer.parseInt(statusParts[1]);
            }
        }

        // Handle error cases based on the status code
        if (statusCode == 400) {
            System.err.println("Error: 400 Bad Request - The server rejected the request due to malformed data.");
        } else if (statusCode == 500) {
            System.err.println("Error: 500 Internal Server Error - The server encountered an error processing the request.");
        } else if (statusCode == 201) {
            System.out.println("Success: 201 Created - New weather data entry created on the server.");
        } else if (statusCode == 200) {
            System.out.println("Success: 200 OK - Weather data updated successfully.");
        } else if (statusCode == 204) {
            System.out.println("Success: 204 No Content - Server received the request, but no content was provided.");
        } else {
            System.err.println("Error: Unexpected response code " + statusCode);
        }
    }
}
