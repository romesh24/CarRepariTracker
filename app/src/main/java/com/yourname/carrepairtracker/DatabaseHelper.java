package com.yourname.carrepairtracker;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {



    private static final String DATABASE_NAME = "CarRepairTracker.db";
    private static final int DATABASE_VERSION = 4;

    // User table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_USER_EMAIL = "user_email";
    private static final String COLUMN_USER_PASSWORD = "user_password";

    // Customer details table
    private static final String TABLE_CUSTOMERS = "customer_details";
    private static final String COLUMN_CUSTOMER_ID = "customer_id";
    public static final String COLUMN_CUSTOMER_NAME = "customer_name";
    public static final String COLUMN_CUSTOMER_PHONE = "phone";
    public static final String COLUMN_CUSTOMER_EMAIL = "email";
    public static final String COLUMN_USER_ID_FK = "user_id_fk";

    // Vehicle details table
    public static final String TABLE_VEHICLES = "vehicle_details";
    public static final String COLUMN_VEHICLE_ID = "vehicle_id";
    public static final String COLUMN_VEHICLE_NUMBER = "vehicle_number";
    public static final String COLUMN_FUEL_TYPE = "fuel_type";
    public static final String COLUMN_MODEL = "model";
    private static final String COLUMN_YEAR = "year";
    private static final String COLUMN_CUSTOMER_ID_FK = "customer_id_fk";

    // Service details table
    static final String TABLE_SERVICES = "service_details";
    public static final String COLUMN_SERVICE_ID = "service_id";
    public static final String COLUMN_SERVICE_TYPE = "service_type";
    public static final String COLUMN_SERVICE_DATE = "service_date";
    public static final String COLUMN_NEXT_SERVICE_DATE = "next_service_date";

    private static final String COLUMN_NOTES = "notes";
    private static final String COLUMN_VEHICLE_ID_FK = "vehicle_id_fk";
   // private static final String COLUMN_PRICE = "service_price";
   public static final String COLUMN_PRICE = "price";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_NAME + " TEXT,"
                + COLUMN_USER_EMAIL + " TEXT UNIQUE,"
                + COLUMN_USER_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create customer details table
        String CREATE_CUSTOMERS_TABLE = "CREATE TABLE " + TABLE_CUSTOMERS + "("
                + COLUMN_CUSTOMER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CUSTOMER_NAME + " TEXT NOT NULL,"
                + COLUMN_CUSTOMER_PHONE + " TEXT,"
                + COLUMN_CUSTOMER_EMAIL + " TEXT,"
                + COLUMN_USER_ID_FK + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_CUSTOMERS_TABLE);

        // Create vehicle details table
        String CREATE_VEHICLES_TABLE = "CREATE TABLE " + TABLE_VEHICLES + "("
                + COLUMN_VEHICLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_VEHICLE_NUMBER + " TEXT UNIQUE NOT NULL,"
                + COLUMN_FUEL_TYPE + " TEXT CHECK(" + COLUMN_FUEL_TYPE + " IN ('petrol','diesel','hybrid','electric')),"
                + COLUMN_MODEL + " TEXT,"
                + COLUMN_YEAR + " INTEGER,"
                + COLUMN_CUSTOMER_ID_FK + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_CUSTOMER_ID_FK + ") REFERENCES " + TABLE_CUSTOMERS + "(" + COLUMN_CUSTOMER_ID + ")"
                + ")";
        db.execSQL(CREATE_VEHICLES_TABLE);

        // Create service details table
        String CREATE_SERVICES_TABLE = "CREATE TABLE " + TABLE_SERVICES + "("
                + COLUMN_SERVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SERVICE_TYPE + " TEXT NOT NULL,"
                + COLUMN_SERVICE_DATE + " TEXT NOT NULL,"
                + COLUMN_NEXT_SERVICE_DATE + " TEXT,"
                + COLUMN_PRICE + " REAL,"
                + COLUMN_NOTES + " TEXT,"
                + COLUMN_VEHICLE_ID_FK + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_VEHICLE_ID_FK + ") REFERENCES " + TABLE_VEHICLES + "(" + COLUMN_VEHICLE_ID + ")"
                + ")";
        db.execSQL(CREATE_SERVICES_TABLE);
    }
// Add these methods to your existing DatabaseHelper class

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    public void checkDatabaseIntegrity() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("PRAGMA integrity_check", null);
        if (cursor != null && cursor.moveToFirst()) {
            Log.d("DBIntegrity", cursor.getString(0));
            cursor.close();
        }
    }

    public int getVehicleIdByNumber(String vehicleNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_VEHICLES,
                new String[]{COLUMN_VEHICLE_ID},
                COLUMN_VEHICLE_NUMBER + "=?",
                new String[]{vehicleNumber},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        if (cursor != null) cursor.close();
        return -1;
    }

    public int getCustomerIdByName(String customerName, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CUSTOMERS,
                new String[]{COLUMN_CUSTOMER_ID},
                COLUMN_CUSTOMER_NAME + "=? AND " + COLUMN_USER_ID_FK + "=?",
                new String[]{customerName, String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        if (cursor != null) cursor.close();
        return -1;
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VEHICLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
    public Cursor getAllServices(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT s." + COLUMN_SERVICE_ID + ", " +
                        "c." + COLUMN_CUSTOMER_NAME + ", " +
                        "v." + COLUMN_VEHICLE_NUMBER + ", " +
                        "v." + COLUMN_FUEL_TYPE + ", " +
                        "s." + COLUMN_SERVICE_TYPE + ", " +
                        "s." + COLUMN_SERVICE_DATE + ", " +
                        "s." + COLUMN_NEXT_SERVICE_DATE + ", " +
                        "s." + COLUMN_PRICE + " " +
                        "FROM " + TABLE_SERVICES + " s " +
                        "JOIN " + TABLE_VEHICLES + " v ON s." + COLUMN_VEHICLE_ID_FK + " = v." + COLUMN_VEHICLE_ID + " " +
                        "JOIN " + TABLE_CUSTOMERS + " c ON v." + COLUMN_CUSTOMER_ID_FK + " = c." + COLUMN_CUSTOMER_ID + " " +
                        "WHERE c." + COLUMN_USER_ID_FK + " = ?",
                new String[]{String.valueOf(userId)}
        );
    }
    // User methods
    public boolean addUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_EMAIL + " = ?" + " AND " + COLUMN_USER_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public Cursor getServicesByVehicle(String vehicleNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SERVICES,
                null,
                COLUMN_VEHICLE_NUMBER + "=?",
                new String[]{vehicleNumber},
                null, null, COLUMN_SERVICE_DATE + " DESC");
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public int getUserId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
            cursor.close();
            return id;
        }
        cursor.close();
        return -1;
    }

    // Customer methods
    public long addCustomer(String name, String phone, String email, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUSTOMER_NAME, name);
        values.put(COLUMN_CUSTOMER_PHONE, phone);
        values.put(COLUMN_CUSTOMER_EMAIL, email);
        values.put(COLUMN_USER_ID_FK, userId);

        return db.insert(TABLE_CUSTOMERS, null, values);
    }

    public Cursor getCustomersByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CUSTOMERS,
                null,
                COLUMN_USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
    }

    // Vehicle methods
    public long addVehicle(String vehicleNumber, String fuelType, String model,
                           int year, int customerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VEHICLE_NUMBER, vehicleNumber);
        values.put(COLUMN_FUEL_TYPE, fuelType);
        values.put(COLUMN_MODEL, model);
        values.put(COLUMN_YEAR, year);
        values.put(COLUMN_CUSTOMER_ID_FK, customerId);

        return db.insert(TABLE_VEHICLES, null, values);
    }

    public Cursor getVehiclesByCustomer(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_VEHICLES,
                null,
                COLUMN_CUSTOMER_ID_FK + "=?",
                new String[]{String.valueOf(customerId)},
                null, null, null);
    }

    // Service methods
    public long addService(String serviceType, String serviceDate, String nextServiceDate,
                           double price, String notes, int vehicleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SERVICE_TYPE, serviceType);
        values.put(COLUMN_SERVICE_DATE, serviceDate);
        values.put(COLUMN_NEXT_SERVICE_DATE, nextServiceDate);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_NOTES, notes);
        values.put(COLUMN_VEHICLE_ID_FK, vehicleId);

        return db.insert(TABLE_SERVICES, null, values);
    }

    public Cursor getServicesByVehicle(int vehicleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SERVICES,
                null,
                COLUMN_VEHICLE_ID_FK + "=?",
                new String[]{String.valueOf(vehicleId)},
                null, null, null);
    }

    public boolean deleteService(int serviceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_SERVICES,
                COLUMN_SERVICE_ID + "=?",
                new String[]{String.valueOf(serviceId)}) > 0;
    }
    public boolean updateService(int serviceId, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.update(TABLE_SERVICES, values,
                COLUMN_SERVICE_ID + "=?",
                new String[]{String.valueOf(serviceId)});
        return rowsAffected > 0;
    }
    public Cursor getAllServicesWithDetails(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT s." + COLUMN_SERVICE_ID + ", " +
                        "v." + COLUMN_VEHICLE_NUMBER + ", " +
                        "s." + COLUMN_SERVICE_DATE + ", " +
                        "c." + COLUMN_CUSTOMER_NAME + ", " +
                        "s." + COLUMN_SERVICE_TYPE + ", " +
                        "s." + COLUMN_NEXT_SERVICE_DATE + ", " +
                        "s." + COLUMN_PRICE + ", " +
                        "v." + COLUMN_MODEL + ", " +
                        "v." + COLUMN_FUEL_TYPE + " " +  // Added
                        "FROM " + TABLE_SERVICES + " s " +
                        "JOIN " + TABLE_VEHICLES + " v ON s." + COLUMN_VEHICLE_ID_FK + " = v." + COLUMN_VEHICLE_ID + " " +
                        "JOIN " + TABLE_CUSTOMERS + " c ON v." + COLUMN_CUSTOMER_ID_FK + " = c." + COLUMN_CUSTOMER_ID + " " +
                        "WHERE c." + COLUMN_USER_ID_FK + " = ?",
                new String[]{String.valueOf(userId)}
        );
    }
}