package com.evcs.app.data.local;

import android.content.Context;
import com.evcs.app.data.local.dao.BookingDaoImpl;
import com.evcs.app.data.local.dao.StationDaoImpl;
import com.evcs.app.data.local.dao.UserDaoImpl;

public class SQLiteDatabase {
    
    private static SQLiteDatabase instance;
    private DatabaseHelper dbHelper;
    private UserDaoImpl userDao;
    private StationDaoImpl stationDao;
    private BookingDaoImpl bookingDao;
    
    private SQLiteDatabase(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        userDao = new UserDaoImpl(dbHelper);
        stationDao = new StationDaoImpl(dbHelper);
        bookingDao = new BookingDaoImpl(dbHelper);
    }
    
    public static synchronized SQLiteDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new SQLiteDatabase(context.getApplicationContext());
        }
        return instance;
    }
    
    public UserDaoImpl userDao() {
        return userDao;
    }
    
    public StationDaoImpl stationDao() {
        return stationDao;
    }
    
    public BookingDaoImpl bookingDao() {
        return bookingDao;
    }
    
    public static void destroyInstance() {
        if (instance != null) {
            DatabaseHelper.destroyInstance();
            instance = null;
        }
    }
}