package com.waakye.android.receiptinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by lesterlie on 7/14/17.
 */

public class ReceiptProvider extends ContentProvider {

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
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }


}
