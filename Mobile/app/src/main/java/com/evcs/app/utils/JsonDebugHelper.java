package com.evcs.app.utils;

import android.util.Log;
import com.evcs.app.data.models.Booking;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class JsonDebugHelper {
    
    public static void debugBookingJson(String tag, String jsonString) {
        Log.d(tag, "Raw JSON: " + jsonString);
        
        try {
            Gson gson = new Gson();
            JsonArray jsonArray = gson.fromJson(jsonString, JsonArray.class);
            
            Log.d(tag, "Array size: " + jsonArray.size());
            
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject booking = jsonArray.get(i).getAsJsonObject();
                Log.d(tag, "Booking " + i + ":");
                
                if (booking.has("_id")) {
                    Log.d(tag, "  ID: " + booking.get("_id").getAsString());
                }
                if (booking.has("status")) {
                    Log.d(tag, "  Status: " + booking.get("status").getAsString());
                }
                if (booking.has("slotNumber")) {
                    Log.d(tag, "  Slot: " + booking.get("slotNumber").getAsInt());
                }
                if (booking.has("stationId")) {
                    JsonElement stationId = booking.get("stationId");
                    if (stationId.isJsonObject()) {
                        Log.d(tag, "  StationId is object: " + stationId.toString());
                    } else {
                        Log.d(tag, "  StationId: " + stationId.getAsString());
                    }
                }
                if (booking.has("startTime")) {
                    Log.d(tag, "  Start: " + booking.get("startTime").getAsString());
                }
                if (booking.has("endTime")) {
                    Log.d(tag, "  End: " + booking.get("endTime").getAsString());
                }
            }
        } catch (Exception e) {
            Log.e(tag, "Error parsing JSON", e);
        }
    }
}