package com.codevengers.voiceemergency;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationFetcher {

    public static String getLocationLink() {
        try {
            // Use ip-api.com for better IP-based geolocation
            URL url = new URL("http://ip-api.com/json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            double latitude = json.getDouble("lat");
            double longitude = json.getDouble("lon");

            // Rough location as a Google Maps link
            return "https://www.google.com/maps?q=" + latitude + "," + longitude;

        } catch (Exception e) {
            System.out.println("❌ Failed to fetch location: " + e.getMessage());
            return "Location not available.";
        }
    }
}
