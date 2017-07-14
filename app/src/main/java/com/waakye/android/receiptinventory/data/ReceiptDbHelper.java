package com.waakye.android.receiptinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lesterlie on 7/13/17.
 * Database helper for the ReceiptInventory app. Manages database creation and version management.
 */

public class ReceiptDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ReceiptDbHelper.class.getSimpleName();

    /** Name of the database file. */
    private static final String DATABASE_NAME = "expenses.db";

    /**
     * Database version.  If you change the database schema, you must increment the database version
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ReceiptDbHelper}
     * @param context of the app
     */
    public ReceiptDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the receipts table.
        String SQL_CREATE_RECEIPTS_TABLE = "CREATE TABLE " + ReceiptContract.ReceiptEntry.TABLE_NAME
                + " (" + ReceiptContract.ReceiptEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME + " TEXT NOT NULL, "
                + ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_TYPE + " INTEGER NOT NULL, "
                + ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_IMAGE_URI + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_RECEIPTS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to be done here.

    }
}
