package com.android.lm.tasker.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Lara on 6/21/2016.
 */
public class TaskDbHelper extends SQLiteOpenHelper implements BaseColumns {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "TaskData.db";
    public static final String TABLE_NAME = "tasks";

    // !! The rest of the code uses different data types for priorty and date_num ...
    public static final String COLUMN_NAME_ROWID = "_id";
    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_NOTES = "notes";
    public static final String COLUMN_NAME_PRIORITY = "priority";
    public static final String COLUMN_NAME_DATE = "task_date";
    public static final String COLUMN_NAME_DATE_NUM = "task_date_num";
    public static final String COLUMN_NAME_STATUS = "task_status";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " ("
                    + COLUMN_NAME_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_NAME_NAME + " TEXT NOT NULL, "
                    + COLUMN_NAME_NOTES + " TEXT, "
                    + COLUMN_NAME_PRIORITY + " TEXT, "
                    + COLUMN_NAME_DATE + " TEXT, "
                    + COLUMN_NAME_DATE_NUM + " TEXT, "
                    + COLUMN_NAME_STATUS + " TEXT);";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        //boolean b = context.deleteDatabase(DATABASE_NAME);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    // Adding a new task to the data base:
    public long createTask(Task task) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        long newRowId;

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_NAME, task.getName());
        values.put(COLUMN_NAME_NOTES, task.getNotes());
        values.put(COLUMN_NAME_PRIORITY, task.getPriority());
        values.put(COLUMN_NAME_DATE_NUM, task.getDateNum());
        values.put(COLUMN_NAME_DATE, task.getDate());
        values.put(COLUMN_NAME_STATUS, task.getIsChecked());

        // Inserting Row
        newRowId = db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection

        return newRowId;
    }

    // Get single task from the data base:
    Task readTask(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Task retrievedTask;

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                COLUMN_NAME_ROWID,
                COLUMN_NAME_NAME,
                COLUMN_NAME_NOTES,
                COLUMN_NAME_PRIORITY,
                COLUMN_NAME_DATE,
                COLUMN_NAME_DATE_NUM,
                COLUMN_NAME_STATUS
        };

        Cursor cursor = db.query(
                TABLE_NAME,  // The table to query
                projection,         // The columns to return
                COLUMN_NAME_ROWID + "=?",               // The columns for the WHERE clause
                new String[] {String.valueOf((id))},               // The values for the WHERE clause
                null,               // don't group the rows
                null,               // don't filter by row groups
                null                // The sort order
        );

        if(cursor != null) {
            cursor.moveToFirst();
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NAME));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOTES));
            boolean priority = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_PRIORITY)) > 0;
            long date_num = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_DATE_NUM));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DATE));
            long row_id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_ROWID));
            boolean status = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_STATUS)) > 0;

            retrievedTask = new Task(name, notes, priority, date_num, date, row_id, status);
        } else {
            retrievedTask = null;
        }

        return retrievedTask;
    }

    // Getting all tasks:
    public ArrayList<Task> readAllTasks() {
        ArrayList<Task> taskList = new ArrayList<Task>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        try {
            if(cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NAME));
                    String notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_NOTES));
                    boolean priority = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_PRIORITY)) > 0;
                    long date_num = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_DATE_NUM));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DATE));
                    long row_id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_ROWID));
                    boolean status = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_STATUS)) > 0;

                    Task task = new Task(name, notes, priority, date_num, date, row_id, status);
                    // Adding task to list
                    taskList.add(0, task);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }


        return taskList;
    }

    // Update a single task. Returns the number of rows affected.
    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_NAME, task.getName());
        values.put(COLUMN_NAME_NOTES, task.getNotes());
        values.put(COLUMN_NAME_PRIORITY, task.getPriority());
        values.put(COLUMN_NAME_DATE_NUM, task.getDateNum());
        values.put(COLUMN_NAME_DATE, task.getDate());
        values.put(COLUMN_NAME_STATUS, task.getIsChecked());

        // updating row
        return db.update(TABLE_NAME, values, COLUMN_NAME_ROWID + " = ?",
                new String[] { String.valueOf(task.getDbRowId()) });
    }

    // Delete a single task:
    public void deleteTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_NAME, COLUMN_NAME_ROWID + " = ?",
                new String[] { String.valueOf(task.getDbRowId()) });
        db.close();
    }


    public void updateAllTaskStatus(ArrayList<Task> currentTasks) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        int updateStatus;

        for(Task task : currentTasks) {
            values.put(COLUMN_NAME_STATUS, task.getIsChecked());
            updateStatus = db.update(TABLE_NAME, values, COLUMN_NAME_ROWID + " = ?",
                    new String[] { String.valueOf(task.getDbRowId()) });

        }
    }

}
