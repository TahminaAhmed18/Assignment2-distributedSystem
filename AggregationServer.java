import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class AggregationServer {
    private static final String STORAGE_FILE = "weather_data_store.txt";
    private static Map<String, WeatherData> weatherDataStore = new ConcurrentHashMap<>();
    private static LamportClock lamportClock = new LamportClock();
    private static final long EXPIRATION_TIME_MS = 30000; // 30 seconds
    private static final Gson gson = new Gson();  // Using Gson for JSON handling

    public static void main(String[] args) {
        int port = 4567;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        loadWeatherDataFromFile();

        // Schedule periodic data expiration
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                expireOldData();
            }
        }, 0, 5000);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Aggregation Server running on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error while starting server: " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String request = in.readLine();
                lamportClock.update();

                if (request.startsWith("PUT")) {
                    handlePutRequest(in, out);
                } else if (request.startsWith("GET")) {
                    handleGetRequest(out);
                } else {
                    out.println("HTTP/1.1 400 Bad Request");
                    out.println();
                    System.err.println("Received an invalid request: " + request);
                }

            } catch (IOException e) {
                System.err.println("Error handling client request: " + e.getMessage());
            }
        }

        private void handlePutRequest(BufferedReader in, PrintWriter out) {
            lamportClock.update();

            try {
                StringBuilder jsonData = new StringBuilder();
                String line;
                boolean hasContent = false;

                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    jsonData.append(line);
                    hasContent = true;
                }

                if (!hasContent) {
                    out.println("HTTP/1.1 204 No Content");
                    out.println();
                    System.err.println("PUT request received with no content.");
                    return;
                }

                // Parse the JSON data into a WeatherData object
                WeatherData weatherData = gson.fromJson(jsonData.toString(), WeatherData.class);

                if (weatherData == null || weatherData.getId() == null) {
                    out.println("HTTP/1.1 400 Bad Request");
                    out.println();
                    System.err.println("Invalid or missing weather data ID.");
                    return;
                }

                // Update the weather data store
                boolean isNewEntry = !weatherDataStore.containsKey(weatherData.getId());
                weatherDataStore.put(weatherData.getId(), new WeatherData(
                        jsonData.toString(), lamportClock.getTime(), System.currentTimeMillis()
                ));

                saveWeatherDataToFile();

                if (isNewEntry) {
                    out.println("HTTP/1.1 201 Created");
                } else {
                    out.println("HTTP/1.1 200 OK");
                }
                out.println();

            } catch (JsonSyntaxException e) {
                out.println("HTTP/1.1 500 Internal Server Error");
                out.println();
                System.err.println("JSON parsing error: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Error processing PUT request: " + e.getMessage());
            }
        }

        private void handleGetRequest(PrintWriter out) {
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: application/json");
            out.println();

            String jsonResponse = gson.toJson(weatherDataStore.values());
            out.println(jsonResponse);
        }
    }

    private static void expireOldData() {
        long currentTime = System.currentTimeMillis();
        weatherDataStore.entrySet().removeIf(entry -> currentTime - entry.getValue().getTimestamp() > EXPIRATION_TIME_MS);
    }

    private static void saveWeatherDataToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STORAGE_FILE))) {
            for (Map.Entry<String, WeatherData> entry : weatherDataStore.entrySet()) {
                writer.write(gson.toJson(entry.getValue()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving weather data to file: " + e.getMessage());
        }
    }

    private static void loadWeatherDataFromFile() {
        File file = new File(STORAGE_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(STORAGE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                WeatherData weatherData = gson.fromJson(line, WeatherData.class);
                if (weatherData != null && weatherData.getId() != null) {
                    weatherDataStore.put(weatherData.getId(), weatherData);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading weather data from file: " + e.getMessage());
        }
    }

    static class WeatherData {
        private String id;
        private String data;
        private int lamportTimestamp;
        private long timestamp;

        public WeatherData(String data, int lamportTimestamp, long timestamp) {
            // Extract the "id" field from the "data" string using the JSON parser
            this.id = extractIdFromData(data);
            this.data = data;
            this.lamportTimestamp = lamportTimestamp;
            this.timestamp = timestamp;
        }

        public String getId() {
            return id;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return data;
        }

        private String extractIdFromData(String data) {
            WeatherData parsedData = gson.fromJson(data, WeatherData.class);
            return parsedData.getId();
        }
    }
}
