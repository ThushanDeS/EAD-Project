package com.evcs.app.data.local.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.evcs.app.data.local.DatabaseHelper;
import com.evcs.app.data.models.Booking;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookingDaoImpl {
    
    private DatabaseHelper dbHelper;
    
    public BookingDaoImpl(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
    
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS, null, null, null, null, null, 
                DatabaseHelper.BOOKING_START_TIME + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookings;
    }
    
    public List<Booking> getBookingsByOwnerId(String ownerId) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS, null, 
                DatabaseHelper.BOOKING_OWNER_ID + "=?", new String[]{ownerId}, 
                null, null, DatabaseHelper.BOOKING_START_TIME + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookings;
    }
    
    public Booking getBookingById(String bookingId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS, null, 
                DatabaseHelper.BOOKING_ID + "=?", new String[]{bookingId}, 
                null, null, null);
        
        Booking booking = null;
        if (cursor.moveToFirst()) {
            booking = cursorToBooking(cursor);
        }
        cursor.close();
        return booking;
    }
    
    public List<Booking> getBookingsByStatus(String status) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS, null, 
                DatabaseHelper.BOOKING_STATUS + "=?", new String[]{status}, 
                null, null, DatabaseHelper.BOOKING_START_TIME + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookings;
    }
    
    public List<Booking> getBookingsByOwnerAndStatus(String ownerId, String status) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS, null, 
                DatabaseHelper.BOOKING_OWNER_ID + "=? AND " + DatabaseHelper.BOOKING_STATUS + "=?", 
                new String[]{ownerId, status}, null, null, DatabaseHelper.BOOKING_START_TIME + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookings;
    }
    
    public List<Booking> getFutureBookingsByOwner(String ownerId, Date currentDate) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS, null, 
                DatabaseHelper.BOOKING_OWNER_ID + "=? AND " + DatabaseHelper.BOOKING_START_TIME + ">?", 
                new String[]{ownerId, String.valueOf(currentDate.getTime())}, 
                null, null, DatabaseHelper.BOOKING_START_TIME + " ASC");
        
        if (cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookings;
    }
    
    public List<Booking> getPastBookingsByOwner(String ownerId, Date currentDate) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS, null, 
                DatabaseHelper.BOOKING_OWNER_ID + "=? AND " + DatabaseHelper.BOOKING_START_TIME + "<?", 
                new String[]{ownerId, String.valueOf(currentDate.getTime())}, 
                null, null, DatabaseHelper.BOOKING_START_TIME + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookings;
    }
    
    public List<Booking> getActiveBookingsByOwner(String ownerId) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS, null, 
                DatabaseHelper.BOOKING_OWNER_ID + "=? AND " + DatabaseHelper.BOOKING_STATUS + " IN ('pending', 'approved', 'in_progress')", 
                new String[]{ownerId}, null, null, DatabaseHelper.BOOKING_START_TIME + " ASC");
        
        if (cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookings;
    }
    
    public int getBookingsCountByStatus(String ownerId, String status) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS, new String[]{"COUNT(*)"}, 
                DatabaseHelper.BOOKING_OWNER_ID + "=? AND " + DatabaseHelper.BOOKING_STATUS + "=?", 
                new String[]{ownerId, status}, null, null, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    
    public void insertBooking(Booking booking) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = bookingToContentValues(booking);
        db.insertWithOnConflict(DatabaseHelper.TABLE_BOOKINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
    
    public void insertBookings(List<Booking> bookings) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Booking booking : bookings) {
                ContentValues values = bookingToContentValues(booking);
                db.insertWithOnConflict(DatabaseHelper.TABLE_BOOKINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    
    public void updateBooking(Booking booking) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = bookingToContentValues(booking);
        db.update(DatabaseHelper.TABLE_BOOKINGS, values, 
                DatabaseHelper.BOOKING_ID + "=?", new String[]{booking.getId()});
    }
    
    public void deleteBooking(Booking booking) {
        deleteBookingById(booking.getId());
    }
    
    public void deleteAllBookings() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_BOOKINGS, null, null);
    }
    
    public void deleteBookingById(String bookingId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_BOOKINGS, 
                DatabaseHelper.BOOKING_ID + "=?", new String[]{bookingId});
    }
    
    public void deleteBookingsByOwnerId(String ownerId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_BOOKINGS, 
                DatabaseHelper.BOOKING_OWNER_ID + "=?", new String[]{ownerId});
    }
    
    private Booking cursorToBooking(Cursor cursor) {
        Booking booking = new Booking();
        booking.setId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_ID)));
        booking.setStationId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_STATION_ID)));
        booking.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_OWNER_ID)));
        booking.setSlotNumber(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_SLOT_NUMBER)));
        
        // Handle Date fields (stored as long timestamps)
        long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_START_TIME));
        if (startTime != 0) booking.setStartTime(new Date(startTime));
        
        long endTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_END_TIME));
        if (endTime != 0) booking.setEndTime(new Date(endTime));
        
        booking.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_STATUS)));
        booking.setQrCode(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_QR_CODE)));
        booking.setQrImageBase64(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_QR_IMAGE_BASE64)));
        booking.setQrImageContentType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_QR_IMAGE_CONTENT_TYPE)));
        booking.setCreatedBy(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_CREATED_BY)));
        
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_CREATED_AT));
        if (createdAt != 0) booking.setCreatedAt(new Date(createdAt));
        
        long updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_UPDATED_AT));
        if (updatedAt != 0) booking.setUpdatedAt(new Date(updatedAt));
        
        booking.setFinalizedBy(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_FINALIZED_BY)));
        
        long finalizedAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_FINALIZED_AT));
        if (finalizedAt != 0) booking.setFinalizedAt(new Date(finalizedAt));
        
        // Handle nullable Double field
        int energyKWhIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_ENERGY_KWH);
        if (!cursor.isNull(energyKWhIndex)) {
            booking.setEnergyKWh(cursor.getDouble(energyKWhIndex));
        }
        
        booking.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_NOTES)));
        booking.setStationName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_STATION_NAME)));
        booking.setStationAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_STATION_ADDRESS)));
        booking.setOwnerName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_OWNER_NAME)));
        booking.setOwnerNic(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BOOKING_OWNER_NIC)));
        
        return booking;
    }
    
    private ContentValues bookingToContentValues(Booking booking) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.BOOKING_ID, booking.getId());
        values.put(DatabaseHelper.BOOKING_STATION_ID, booking.getStationId());
        values.put(DatabaseHelper.BOOKING_OWNER_ID, booking.getOwnerId());
        values.put(DatabaseHelper.BOOKING_SLOT_NUMBER, booking.getSlotNumber());
        
        // Handle Date fields (store as long timestamps)
        if (booking.getStartTime() != null) {
            values.put(DatabaseHelper.BOOKING_START_TIME, booking.getStartTime().getTime());
        }
        if (booking.getEndTime() != null) {
            values.put(DatabaseHelper.BOOKING_END_TIME, booking.getEndTime().getTime());
        }
        
        values.put(DatabaseHelper.BOOKING_STATUS, booking.getStatus());
        values.put(DatabaseHelper.BOOKING_QR_CODE, booking.getQrCode());
        values.put(DatabaseHelper.BOOKING_QR_IMAGE_BASE64, booking.getQrImageBase64());
        values.put(DatabaseHelper.BOOKING_QR_IMAGE_CONTENT_TYPE, booking.getQrImageContentType());
        values.put(DatabaseHelper.BOOKING_CREATED_BY, booking.getCreatedBy());
        
        if (booking.getCreatedAt() != null) {
            values.put(DatabaseHelper.BOOKING_CREATED_AT, booking.getCreatedAt().getTime());
        }
        if (booking.getUpdatedAt() != null) {
            values.put(DatabaseHelper.BOOKING_UPDATED_AT, booking.getUpdatedAt().getTime());
        }
        
        values.put(DatabaseHelper.BOOKING_FINALIZED_BY, booking.getFinalizedBy());
        
        if (booking.getFinalizedAt() != null) {
            values.put(DatabaseHelper.BOOKING_FINALIZED_AT, booking.getFinalizedAt().getTime());
        }
        
        // Handle nullable Double field
        if (booking.getEnergyKWh() != null) {
            values.put(DatabaseHelper.BOOKING_ENERGY_KWH, booking.getEnergyKWh());
        }
        
        values.put(DatabaseHelper.BOOKING_NOTES, booking.getNotes());
        values.put(DatabaseHelper.BOOKING_STATION_NAME, booking.getStationName());
        values.put(DatabaseHelper.BOOKING_STATION_ADDRESS, booking.getStationAddress());
        values.put(DatabaseHelper.BOOKING_OWNER_NAME, booking.getOwnerName());
        values.put(DatabaseHelper.BOOKING_OWNER_NIC, booking.getOwnerNic());
        
        return values;
    }
}