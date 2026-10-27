package com.krishiai.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "krishiai.db";
    private static final int    DB_VERSION = 1;

    // Feedback table
    private static final String TABLE_FEEDBACK = "feedback";
    private static final String COL_ID         = "id";
    private static final String COL_DISEASE    = "disease_name";
    private static final String COL_RATING     = "rating";
    private static final String COL_HELPFUL    = "helpful";
    private static final String COL_COMMENT    = "comment";
    private static final String COL_DATE       = "date";

    // Scan history table
    private static final String TABLE_HISTORY  = "scan_history";
    private static final String COL_PLANT      = "plant_name";
    private static final String COL_CONFIDENCE = "confidence";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create feedback table
        db.execSQL("CREATE TABLE " + TABLE_FEEDBACK + " (" +
                COL_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DISEASE + " TEXT, " +
                COL_RATING  + " INTEGER, " +
                COL_HELPFUL + " INTEGER, " +
                COL_COMMENT + " TEXT, " +
                COL_DATE    + " TEXT)");

        // Create scan history table
        db.execSQL("CREATE TABLE " + TABLE_HISTORY + " (" +
                COL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DISEASE    + " TEXT, " +
                COL_PLANT      + " TEXT, " +
                COL_CONFIDENCE + " REAL, " +
                COL_DATE       + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEEDBACK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    // ── SAVE FEEDBACK ─────────────────────────────────────────
    public boolean saveFeedback(String diseaseName, int rating,
                                boolean helpful, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DISEASE, diseaseName);
        values.put(COL_RATING,  rating);
        values.put(COL_HELPFUL, helpful ? 1 : 0);
        values.put(COL_COMMENT, comment);
        values.put(COL_DATE,
                new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",
                        java.util.Locale.getDefault())
                        .format(new java.util.Date()));
        long result = db.insert(TABLE_FEEDBACK, null, values);
        db.close();
        return result != -1;
    }

    // ── SAVE SCAN TO HISTORY ──────────────────────────────────
    public void saveScanHistory(String diseaseName, String plantName,
                                float confidence) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DISEASE,    diseaseName);
        values.put(COL_PLANT,      plantName);
        values.put(COL_CONFIDENCE, confidence);
        values.put(COL_DATE,
                new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",
                        java.util.Locale.getDefault())
                        .format(new java.util.Date()));
        db.insert(TABLE_HISTORY, null, values);
        db.close();
    }

    // ── GET ALL FEEDBACK ──────────────────────────────────────
    public List<String> getAllFeedback() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_FEEDBACK +
                        " ORDER BY " + COL_ID + " DESC", null);
        while (cursor.moveToNext()) {
            String entry =
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)) + "\n" +
                            "Disease: " + cursor.getString(cursor.getColumnIndexOrThrow(COL_DISEASE)) + "\n" +
                            "Rating: " + cursor.getInt(cursor.getColumnIndexOrThrow(COL_RATING)) + "⭐\n" +
                            "Helpful: " + (cursor.getInt(cursor.getColumnIndexOrThrow(COL_HELPFUL)) == 1 ? "Yes" : "No") + "\n" +
                            "Comment: " + cursor.getString(cursor.getColumnIndexOrThrow(COL_COMMENT));
            list.add(entry);
        }
        cursor.close();
        db.close();
        return list;
    }

    // ── GET SCAN HISTORY ──────────────────────────────────────
    public List<String> getScanHistory() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_HISTORY +
                        " ORDER BY " + COL_ID + " DESC LIMIT 20", null);
        while (cursor.moveToNext()) {
            String entry =
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)) + " — " +
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_DISEASE)) + " (" +
                            (int)(cursor.getFloat(cursor.getColumnIndexOrThrow(COL_CONFIDENCE)) * 100) + "%)";
            list.add(entry);
        }
        cursor.close();
        db.close();
        return list;
    }
}