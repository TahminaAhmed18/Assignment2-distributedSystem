import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ContentServer {

    private static LamportClock lamportClock = new LamportClock();

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
                System.out.println("Failed to read weather data from file.");
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
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Reads weather data dynamically from the file
    private static String readWeatherDataFromFile(String filepath) {
        StringBuilder jsonData = new StringBuilder();
        try (Scanner scanner = new Scanner(new File(filepath))) {
            while (scanner.hasNextLine()) {
                jsonData.append(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filepath);
            return null;
        }
        return jsonData.toString();
    }
}
