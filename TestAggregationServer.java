import org.junit.jupiter.api.*;
import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestAggregationServer {
    @Test
    public void testPutRequest() throws IOException {
        try (Socket socket = new Socket("localhost", 4567);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send PUT request with personalized weather data
            out.println("PUT /weather.json HTTP/1.1");
            out.println();
            out.println("{\"id\": \"a1938593\", \"name\": \"Tahmina Ahmed\", \"university\": \"University of Adelaide\"}");
            out.flush();  // Ensure the data is sent immediately

            // Read response
            String response = in.readLine();
            assertTrue(response.contains("200 OK"));
        }
    }
}
