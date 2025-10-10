package com.evcs.app.data.local.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.evcs.app.data.local.DatabaseHelper;
import com.evcs.app.data.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl {
    
    private DatabaseHelper dbHelper;
    
    public UserDaoImpl(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
    
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, null, null, null, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }
    
    public User getUserById(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, 
                DatabaseHelper.USER_ID + "=?", new String[]{userId}, 
                null, null, null);
        
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }
    
    public User getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, 
                DatabaseHelper.USER_EMAIL + "=?", new String[]{email}, 
                null, null, null);
        
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }
    
    public User getUserByNic(String nic) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, 
                DatabaseHelper.USER_NIC + "=?", new String[]{nic}, 
                null, null, null);
        
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }
    
    public void insertUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = userToContentValues(user);
        db.insertWithOnConflict(DatabaseHelper.TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
    
    public void insertUsers(List<User> users) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (User user : users) {
                ContentValues values = userToContentValues(user);
                db.insertWithOnConflict(DatabaseHelper.TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    
    public void updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = userToContentValues(user);
        db.update(DatabaseHelper.TABLE_USERS, values, 
                DatabaseHelper.USER_ID + "=?", new String[]{user.getId()});
    }
    
    public void deleteUser(User user) {
        deleteUserById(user.getId());
    }
    
    public void deleteAllUsers() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_USERS, null, null);
    }
    
    public void deleteUserById(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_USERS, 
                DatabaseHelper.USER_ID + "=?", new String[]{userId});
    }
    
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_ID)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_ROLE)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_EMAIL)));
        user.setNic(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_NIC)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_NAME)));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_PHONE)));
        user.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_STATUS)));
        user.setToken(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_TOKEN)));
        return user;
    }
    
    private ContentValues userToContentValues(User user) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.USER_ID, user.getId());
        values.put(DatabaseHelper.USER_ROLE, user.getRole());
        values.put(DatabaseHelper.USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.USER_NIC, user.getNic());
        values.put(DatabaseHelper.USER_NAME, user.getName());
        values.put(DatabaseHelper.USER_PHONE, user.getPhone());
        values.put(DatabaseHelper.USER_STATUS, user.getStatus());
        values.put(DatabaseHelper.USER_TOKEN, user.getToken());
        return values;
    }
}