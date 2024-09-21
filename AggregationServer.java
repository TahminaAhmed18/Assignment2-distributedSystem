import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class AggregationServer {
    private static final String STORAGE_FILE = "weather_data_store.txt";
    private static Map<String, WeatherData> weatherDataStore = new ConcurrentHashMap<>();
    private static LamportClock lamportClock = new LamportClock();
    private static final long EXPIRATION_TIME_MS = 30000; // 30 seconds

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
            e.printStackTrace();
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
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handlePutRequest(BufferedReader in, PrintWriter out) throws IOException {
            lamportClock.update();

            StringBuilder jsonData = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                jsonData.append(line);
            }

            // Parse the JSON (you would use a proper parser here)
            String id = parseIdFromJson(jsonData.toString());

            weatherDataStore.put(id, new WeatherData(jsonData.toString(), lamportClock.getTime(), System.currentTimeMillis()));
            saveWeatherDataToFile();
            out.println("HTTP/1.1 200 OK");
        }

        private void handleGetRequest(PrintWriter out) {
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: application/json");
            out.println(weatherDataStore.toString()); // Replace with proper JSON output
        }
    }

    private static void expireOldData() {
        long currentTime = System.currentTimeMillis();
        weatherDataStore.entrySet().removeIf(entry -> currentTime - entry.getValue().getTimestamp() > EXPIRATION_TIME_MS);
    }

    private static void saveWeatherDataToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STORAGE_FILE))) {
            for (Map.Entry<String, WeatherData> entry : weatherDataStore.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue().toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                String[] parts = line.split(":");
                weatherDataStore.put(parts[0], new WeatherData(parts[1], lamportClock.getTime(), System.currentTimeMillis()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String parseIdFromJson(String json) {
        return "IDS60901"; // Example static ID, replace with real JSON parsing
    }

    static class WeatherData {
        private String data;
        private int lamportTimestamp;
        private long timestamp;

        public WeatherData(String data, int lamportTimestamp, long timestamp) {
            this.data = data;
            this.lamportTimestamp = lamportTimestamp;
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return data;
        }
    }
}
