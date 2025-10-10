package com.evcs.app.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "evcs_database.db";
    private static final int DATABASE_VERSION = 2;
    
    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_STATIONS = "stations";
    public static final String TABLE_BOOKINGS = "bookings";
    
    // Users table columns
    public static final String USER_ID = "id";
    public static final String USER_ROLE = "role";
    public static final String USER_EMAIL = "email";
    public static final String USER_NIC = "nic";
    public static final String USER_NAME = "name";
    public static final String USER_PHONE = "phone";
    public static final String USER_STATUS = "status";
    public static final String USER_TOKEN = "token";
    
    // Stations table columns
    public static final String STATION_ID = "id";
    public static final String STATION_NAME = "name";
    public static final String STATION_TYPE = "type";
    public static final String STATION_SLOT_COUNT = "slotCount";
    public static final String STATION_ACTIVE = "active";
    public static final String STATION_LAT = "lat";
    public static final String STATION_LNG = "lng";
    public static final String STATION_ADDRESS = "address";
    public static final String STATION_HOURS_OPEN = "hours_open";
    public static final String STATION_HOURS_CLOSE = "hours_close";
    
    // Bookings table columns
    public static final String BOOKING_ID = "id";
    public static final String BOOKING_STATION_ID = "stationId";
    public static final String BOOKING_OWNER_ID = "ownerId";
    public static final String BOOKING_SLOT_NUMBER = "slotNumber";
    public static final String BOOKING_START_TIME = "startTime";
    public static final String BOOKING_END_TIME = "endTime";
    public static final String BOOKING_STATUS = "status";
    public static final String BOOKING_QR_CODE = "qrCode";
    public static final String BOOKING_QR_IMAGE_BASE64 = "qrImageBase64";
    public static final String BOOKING_QR_IMAGE_CONTENT_TYPE = "qrImageContentType";
    public static final String BOOKING_CREATED_BY = "createdBy";
    public static final String BOOKING_CREATED_AT = "createdAt";
    public static final String BOOKING_UPDATED_AT = "updatedAt";
    public static final String BOOKING_FINALIZED_BY = "finalizedBy";
    public static final String BOOKING_FINALIZED_AT = "finalizedAt";
    public static final String BOOKING_ENERGY_KWH = "energyKWh";
    public static final String BOOKING_NOTES = "notes";
    public static final String BOOKING_STATION_NAME = "stationName";
    public static final String BOOKING_STATION_ADDRESS = "stationAddress";
    public static final String BOOKING_OWNER_NAME = "ownerName";
    public static final String BOOKING_OWNER_NIC = "ownerNic";
    
    // Create table statements
    private static final String CREATE_USERS_TABLE = 
        "CREATE TABLE " + TABLE_USERS + " (" +
        USER_ID + " TEXT PRIMARY KEY NOT NULL, " +
        USER_ROLE + " TEXT, " +
        USER_EMAIL + " TEXT, " +
        USER_NIC + " TEXT, " +
        USER_NAME + " TEXT, " +
        USER_PHONE + " TEXT, " +
        USER_STATUS + " TEXT, " +
        USER_TOKEN + " TEXT" +
        ")";
    
    private static final String CREATE_STATIONS_TABLE = 
        "CREATE TABLE " + TABLE_STATIONS + " (" +
        STATION_ID + " TEXT PRIMARY KEY NOT NULL, " +
        STATION_NAME + " TEXT, " +
        STATION_TYPE + " TEXT, " +
        STATION_SLOT_COUNT + " INTEGER, " +
        STATION_ACTIVE + " INTEGER, " +
        STATION_LAT + " REAL, " +
        STATION_LNG + " REAL, " +
        STATION_ADDRESS + " TEXT, " +
        STATION_HOURS_OPEN + " TEXT, " +
        STATION_HOURS_CLOSE + " TEXT" +
        ")";
    
    private static final String CREATE_BOOKINGS_TABLE = 
        "CREATE TABLE " + TABLE_BOOKINGS + " (" +
        BOOKING_ID + " TEXT PRIMARY KEY NOT NULL, " +
        BOOKING_STATION_ID + " TEXT, " +
        BOOKING_OWNER_ID + " TEXT, " +
        BOOKING_SLOT_NUMBER + " INTEGER, " +
        BOOKING_START_TIME + " INTEGER, " +
        BOOKING_END_TIME + " INTEGER, " +
        BOOKING_STATUS + " TEXT, " +
        BOOKING_QR_CODE + " TEXT, " +
        BOOKING_QR_IMAGE_BASE64 + " TEXT, " +
        BOOKING_QR_IMAGE_CONTENT_TYPE + " TEXT, " +
        BOOKING_CREATED_BY + " TEXT, " +
        BOOKING_CREATED_AT + " INTEGER, " +
        BOOKING_UPDATED_AT + " INTEGER, " +
        BOOKING_FINALIZED_BY + " TEXT, " +
        BOOKING_FINALIZED_AT + " INTEGER, " +
        BOOKING_ENERGY_KWH + " REAL, " +
        BOOKING_NOTES + " TEXT, " +
        BOOKING_STATION_NAME + " TEXT, " +
        BOOKING_STATION_ADDRESS + " TEXT, " +
        BOOKING_OWNER_NAME + " TEXT, " +
        BOOKING_OWNER_NIC + " TEXT" +
        ")";
    
    private static DatabaseHelper instance;
    
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_STATIONS_TABLE);
        db.execSQL(CREATE_BOOKINGS_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop existing tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        
        // Create new tables
        onCreate(db);
    }
    
    public static void destroyInstance() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }
}