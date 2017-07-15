package com.waakye.android.receiptinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by lesterlie on 7/14/17.
 */

public class ReceiptProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = ReceiptProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the receipts table */
    private static final int RECEIPTS = 100;

    /** URI matcher code for the content URI for a single receipt in the receipts table. */
    private static final int RECEIPT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer.  This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize.  All paths added to the UriMatcher have a corresponding code to
        // return when a match is found.

        // The content URI of the form "content://com.waakye.android.receiptinventory/receipts"
        // will map to the integer code {@link #RECEIPTS}. This URI is used to provide access to
        // MULTIPLE rows of the receipts table.
        sUriMatcher.addURI(ReceiptContract.CONTENT_AUTHORITY, ReceiptContract.PATH_RECEIPTS,
                RECEIPTS);

        // The content URI of the form "content://com.waakye.android.receiptinventory/receipts/#"
        // will map to the integer code {@link #RECEIPT_ID}.  This is used to provide access to
        // ONE single row of the receipts table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.waakye.android.receiptinventory/receipts/3" matches, but
        // "content://com.waakye.android.receiptinventory/receipts" does not match.
        sUriMatcher.addURI(ReceiptContract.CONTENT_AUTHORITY, ReceiptContract.PATH_RECEIPTS + "/#",
                RECEIPT_ID);
    }

    /** Database helper object */
    private ReceiptDbHelper mDbHelper;

    @Override
    public boolean onCreate(){
        mDbHelper = new ReceiptDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder){
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch(match){
            case RECEIPTS:
                // For the RECEIPTS code, query the receipts table directly with the given
                // projection, selection, selection arguments, and sort order.  The cursor could
                // contain multiple rows of the receipts table.
                cursor = database.query(ReceiptContract.ReceiptEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case RECEIPT_ID:
                // For the RECEIPT_ID code, extract out the ID from the URI.
                // For an example URI such as
                // "content://com.waakye.android.receiptinventory/receipts/3", the selection will
                // be "_id=?" and the selection argument will be a String array containing the
                // actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in that selection
                // arguments that will fill in the "?".  Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ReceiptContract.ReceiptEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the receipts table where the _id equals 3 to
                // return a Cursor containing that row of the table.
                cursor = database.query(ReceiptContract.ReceiptEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case RECEIPTS:
                return insertReceipt(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a receipt into the database with the given content values.  Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertReceipt(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME);
        if(name == null){
            throw new IllegalArgumentException("Receipt requires a name");
        }


        // If the price is provided, check that it's greater than or equal to $0
        Integer price = values.getAsInteger(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE);
        if(price != null && price <0){
            throw new IllegalArgumentException("Receipt requires a valid price.");
        }

        // If the quantity is provided, check that it's greater than or equal to 0
        Integer quantity = values.getAsInteger(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY);
        if(quantity != null && quantity < 0){
            throw new IllegalArgumentException("Receipt requires a valid quantity.");
        }

        // Check that the type is valid
        Integer receiptType = values.getAsInteger(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_TYPE);
        if(receiptType == null || !ReceiptContract.ReceiptEntry.isValidType(receiptType)){
            throw new IllegalArgumentException("Receipt requires valid type.");
        }

        // No need to check image_uri because receipt not required to have one

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new receipt with the given values
        long id = database.insert(ReceiptContract.ReceiptEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch(match) {
            case RECEIPTS:
                return updateReceipt(uri, contentValues, selection, selectionArgs);
            case RECEIPT_ID:
                // For the RECEIPT_ID code, extract out the ID from the URI, so we know which row
                // to update.  Selection will be "_id=?" and selection arguments will be a String
                // array containing the actual ID.
                selection = ReceiptContract.ReceiptEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateReceipt(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update receipts in database with the given content values.  Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more receipts).
     * Return the number of rows that were successfully updated
     */
    private int updateReceipt(Uri uri, ContentValues values, String selection,
                              String[] selectionArgs){
        // If the {@link ReceiptEntry#COLUMN_RECEIPT_NAME} key is present, check that the name value
        // is not null
        if (values.containsKey(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME)){
            String name = values.getAsString(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME);
            if (name == null){
                throw new IllegalArgumentException("Receipt requires a name.");
            }
        }

        // If the {@link ReceiptEntry#COLUMN_RECEIPT_PRICE} key is present, check that the price
        // values is not null
        if (values.containsKey(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE)){
            Integer price = values.getAsInteger(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE);
            if(price != null && price < 0){
                throw new IllegalArgumentException("Receipt requires a price.");
            }
        }

        // If the {@link ReceiptEntry#COLUMN_RECEIPT_QUANTITY} key is present, check that the
        // quantity values is not null
        if (values.containsKey(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY)){
            Integer quantity = values.getAsInteger(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY);
            if(quantity != null && quantity < 0){
                throw new IllegalArgumentException("Receipt requires a quantity.");
            }
        }

        // If the {@link ReceiptEntry#COLUMN_RECEIPT_TYPE} key is present, check that the type
        // is valid.
        if(values.containsKey(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_TYPE)){
            Integer receiptType = values.getAsInteger(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_TYPE);
            if (receiptType == null || !ReceiptContract.ReceiptEntry.isValidType(receiptType)){
                throw new IllegalArgumentException("Receipt requires a type");
            }
        }

        // No need to check the image_uri, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if(values.size() == 0){
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        return database.update(ReceiptContract.ReceiptEntry.TABLE_NAME, values, selection,
                selectionArgs);
    }


}
