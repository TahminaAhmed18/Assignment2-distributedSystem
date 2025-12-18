import java.util.HashMap;
import java.util.Map;

public class WeatherData {
    private String id;
    private String name;
    private String state;
    private String time_zone;
    private double lat;
    private double lon;
    private String local_date_time;
    private String local_date_time_full;
    private double air_temp;
    private double apparent_t;
    private String cloud;
    private double dewpt;
    private double press;
    private int rel_hum;
    private String wind_dir;
    private int wind_spd_kmh;
    private int wind_spd_kt;
    private long timestamp;
    private int lamportTimestamp;

    // Constructor
    public WeatherData() {}

    public WeatherData(String data, int lamportTimestamp, long timestamp) {
        this.timestamp = timestamp;
        this.lamportTimestamp = lamportTimestamp;
        fromJson(data);
    }

    // **This is the method that was missing**
    public WeatherData fromJson(String jsonString) {

        Map<String, String> jsonMap = parseJsonToMap(jsonString);

        this.id = jsonMap.get("id");
        this.name = jsonMap.get("name");
        this.state = jsonMap.get("state");
        this.time_zone = jsonMap.get("time_zone");
        this.lat = Double.parseDouble(jsonMap.get("lat"));
        this.lon = Double.parseDouble(jsonMap.get("lon"));
        this.local_date_time = jsonMap.get("local_date_time");
        this.local_date_time_full = jsonMap.get("local_date_time_full");
        this.air_temp = Double.parseDouble(jsonMap.get("air_temp"));
        this.apparent_t = Double.parseDouble(jsonMap.get("apparent_t"));
        this.cloud = jsonMap.get("cloud");
        this.dewpt = Double.parseDouble(jsonMap.get("dewpt"));
        this.press = Double.parseDouble(jsonMap.get("press"));
        this.rel_hum = Integer.parseInt(jsonMap.get("rel_hum"));
        this.wind_dir = jsonMap.get("wind_dir");
        this.wind_spd_kmh = Integer.parseInt(jsonMap.get("wind_spd_kmh"));
        this.wind_spd_kt = Integer.parseInt(jsonMap.get("wind_spd_kt"));
        return this;

    }

    // Helper method to parse JSON string to a map (key-value pairs)
    private static Map<String, String> parseJsonToMap(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.replaceAll("[{}\"]", ""); // Remove curly braces and quotes
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            map.put(keyValue[0].trim(), keyValue[1].trim());
        }
        return map;
    }

    // Getters
    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Overriding the toString method to output the weather data as a JSON string
    @Override
    public String toString() {
        return "{ \"id\": \"" + id + "\", \"name\": \"" + name + "\", \"state\": \"" + state + "\", \"time_zone\": \"" + time_zone + "\", \"lat\": " + lat + ", \"lon\": " + lon + ", \"local_date_time\": \"" + local_date_time + "\", \"local_date_time_full\": \"" + local_date_time_full + "\", \"air_temp\": " + air_temp + ", \"apparent_t\": " + apparent_t + ", \"cloud\": \"" + cloud + "\", \"dewpt\": " + dewpt + ", \"press\": " + press + ", \"rel_hum\": " + rel_hum + ", \"wind_dir\": \"" + wind_dir + "\", \"wind_spd_kmh\": " + wind_spd_kmh + ", \"wind_spd_kt\": " + wind_spd_kt + " }";
    }
}
