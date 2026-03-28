package com.codevengers.voiceemergency;

public class LocationFetcher {

    public static String getLocationLink() {
        try {
            // Simulate getting location
            // In a real implementation, you would:
            // 1. Get GPS coordinates
            // 2. Use geolocation API
            // 3. Generate Google Maps link

            // For demo purposes, return a sample location
            double latitude = 23.8103 + (Math.random() - 0.5) * 0.01; // Dhaka area
            double longitude = 90.4125 + (Math.random() - 0.5) * 0.01;

            String googleMapsLink = String.format(
                    "https://www.google.com/maps?q=%.6f,%.6f",
                    latitude, longitude
            );

            System.out.println("📍 Location fetched: " + googleMapsLink);
            return googleMapsLink;

        } catch (Exception e) {
            System.err.println("❌ Error fetching location: " + e.getMessage());
            return "Location unavailable";
        }
    }

    public static String getCurrentLocation() {
        return getLocationLink();
    }
}
