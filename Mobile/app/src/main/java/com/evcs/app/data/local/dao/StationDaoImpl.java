package com.evcs.app.data.local.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.evcs.app.data.local.DatabaseHelper;
import com.evcs.app.data.models.Station;
import java.util.ArrayList;
import java.util.List;

public class StationDaoImpl {
    
    private DatabaseHelper dbHelper;
    
    public StationDaoImpl(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
    
    public List<Station> getAllStations() {
        List<Station> stations = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_STATIONS, null, null, null, null, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                stations.add(cursorToStation(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return stations;
    }
    
    public List<Station> getActiveStations() {
        List<Station> stations = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_STATIONS, null, 
                DatabaseHelper.STATION_ACTIVE + "=?", new String[]{"1"}, 
                null, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                stations.add(cursorToStation(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return stations;
    }
    
    public Station getStationById(String stationId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_STATIONS, null, 
                DatabaseHelper.STATION_ID + "=?", new String[]{stationId}, 
                null, null, null);
        
        Station station = null;
        if (cursor.moveToFirst()) {
            station = cursorToStation(cursor);
        }
        cursor.close();
        return station;
    }
    
    public List<Station> getStationsByType(String type) {
        List<Station> stations = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_STATIONS, null, 
                DatabaseHelper.STATION_TYPE + "=?", new String[]{type}, 
                null, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                stations.add(cursorToStation(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return stations;
    }
    
    public List<Station> getActiveStationsByType(String type) {
        List<Station> stations = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_STATIONS, null, 
                DatabaseHelper.STATION_ACTIVE + "=? AND " + DatabaseHelper.STATION_TYPE + "=?", 
                new String[]{"1", type}, null, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                stations.add(cursorToStation(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return stations;
    }
    
    public void insertStation(Station station) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = stationToContentValues(station);
        db.insertWithOnConflict(DatabaseHelper.TABLE_STATIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
    
    public void insertStations(List<Station> stations) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Station station : stations) {
                ContentValues values = stationToContentValues(station);
                db.insertWithOnConflict(DatabaseHelper.TABLE_STATIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    
    public void updateStation(Station station) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = stationToContentValues(station);
        db.update(DatabaseHelper.TABLE_STATIONS, values, 
                DatabaseHelper.STATION_ID + "=?", new String[]{station.getId()});
    }
    
    public void deleteStation(Station station) {
        deleteStationById(station.getId());
    }
    
    public void deleteAllStations() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_STATIONS, null, null);
    }
    
    public void deleteStationById(String stationId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_STATIONS, 
                DatabaseHelper.STATION_ID + "=?", new String[]{stationId});
    }
    
    private Station cursorToStation(Cursor cursor) {
        Station station = new Station();
        station.setId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STATION_ID)));
        station.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STATION_NAME)));
        station.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STATION_TYPE)));
        station.setSlotCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STATION_SLOT_COUNT)));
        station.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STATION_ACTIVE)) == 1);
        station.setLat(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.STATION_LAT)));
        station.setLng(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.STATION_LNG)));
        station.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STATION_ADDRESS)));
        
        // Handle operating hours
        String openTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STATION_HOURS_OPEN));
        String closeTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STATION_HOURS_CLOSE));
        if (openTime != null || closeTime != null) {
            Station.OperatingHours hours = new Station.OperatingHours(openTime, closeTime);
            station.setHours(hours);
        }
        
        return station;
    }
    
    private ContentValues stationToContentValues(Station station) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.STATION_ID, station.getId());
        values.put(DatabaseHelper.STATION_NAME, station.getName());
        values.put(DatabaseHelper.STATION_TYPE, station.getType());
        values.put(DatabaseHelper.STATION_SLOT_COUNT, station.getSlotCount());
        values.put(DatabaseHelper.STATION_ACTIVE, station.isActive() ? 1 : 0);
        values.put(DatabaseHelper.STATION_LAT, station.getLat());
        values.put(DatabaseHelper.STATION_LNG, station.getLng());
        values.put(DatabaseHelper.STATION_ADDRESS, station.getAddress());
        
        // Handle operating hours
        if (station.getHours() != null) {
            values.put(DatabaseHelper.STATION_HOURS_OPEN, station.getHours().getOpen());
            values.put(DatabaseHelper.STATION_HOURS_CLOSE, station.getHours().getClose());
        }
        
        return values;
    }
}