import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class GLocation {
    // Fetches weather data for a given location
    public static JSONObject getWeatherData(String locationName) {
        // Retrieve location data based on the given location name
        JSONArray locationData = getLocationData(locationName);

        // Check if location data is available
        if (locationData == null || locationData.isEmpty()) {
            System.out.println("Error: Location data not found.");
            return null;
        }

        // Extract latitude and longitude from the location data
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // Construct the API URL for fetching weather data using latitude and longitude
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&current=temperature_2m,relative_humidity_2m,rain,snowfall,weather_code,wind_speed_10m" +
                "&hourly=temperature_2m,relative_humidity_2m,rain,showers,snowfall,weather_code,wind_speed_10m" +
                "&timezone=auto&past_days=7";

        try {
            // Fetch API response
            HttpURLConnection connection = fetchApiResponse(urlString);

            // Check if the connection was successful
            if (connection == null || connection.getResponseCode() != 200) {
                System.out.println("Error: Couldn't connect to API");
                return null;
            }

            // Read the response JSON
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(connection.getInputStream());
            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            connection.disconnect();

            // Parse the response JSON
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(resultJson.toString());

            // Extract hourly weather data from the response
            JSONObject hourlyWeatherData = (JSONObject) resultJsonObj.get("hourly");

            // Find the index of current time in the hourly data
            JSONArray time = (JSONArray) hourlyWeatherData.get("time");
            int index = findIndexofCurrentTime(time);

            // Extract temperature data
            JSONArray temperatureData = (JSONArray) hourlyWeatherData.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            // Extract weather condition data
            JSONArray weatherCode = (JSONArray) hourlyWeatherData.get("weather_code");
            String weatherCondition = convertWeatherCode((long) weatherCode.get(index));

            // Extract humidity data
            JSONArray relativeHumidity = (JSONArray) hourlyWeatherData.get("relative_humidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            // Extract wind speed data
            JSONArray windSpeedData = (JSONArray) hourlyWeatherData.get("wind_speed_10m");
            double windSpeed = (double) windSpeedData.get(index);

            // Create a JSON object to store weather data and return it
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("wind_speed", windSpeed);
            return weatherData;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // Retrieves geographic coordinates for a given location name
    public static JSONArray getLocationData(String locationName) {
        // Replace any whitespace in location name with '+' to adhere to API's request format
        locationName = locationName.replaceAll(" ", "+");
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";
        try {
            // Fetch API response
            HttpURLConnection connection = fetchApiResponse(urlString);

            // Check if the connection was successful
            if (connection != null && connection.getResponseCode() == 200) {
                // Read and parse the response JSON
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                while (scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }
                scanner.close();
                connection.disconnect();

                // Parse the response JSON and return location data
                JSONParser parser = new JSONParser();
                JSONObject resultJsonObject = (JSONObject) parser.parse(resultJson.toString());
                return (JSONArray) resultJsonObject.get("results");
            } else {
                System.out.println("Error: Couldn't connect to API");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Fetches API response for the given URL
    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            // Create connection
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set request method to GET and connect
            connection.setRequestMethod("GET");
            connection.connect();
            return connection;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Finds the index of the current time in the given list of time values
    private static int findIndexofCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();
        for (int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if (time.equalsIgnoreCase(currentTime)) {
                return i;
            }
        }
        return 0; // Default to the first index if current time is not found
    }

    // Gets the current time in the specified format
    public static String getCurrentTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':'00");
        return currentDateTime.format(formatter);
    }

    // Converts weather code to human-readable weather condition
    private static String convertWeatherCode(long weatherCode) {
        String weatherCondition;

        if (weatherCode == 0L ) {
            weatherCondition = "Clear";
        } else if (weatherCode >= 1L && weatherCode <= 3L) {
            weatherCondition = "Cloudy";
        } else if ((weatherCode >= 51L && weatherCode <= 67L) || (weatherCode >= 80L && weatherCode <= 99L)) {
            weatherCondition = "Rainy";
        } else if (weatherCode >= 71L && weatherCode <= 77L) {
            weatherCondition = "Snowy";
        } else {
            weatherCondition = "Unknown";
        }
        return weatherCondition;
    }

}
