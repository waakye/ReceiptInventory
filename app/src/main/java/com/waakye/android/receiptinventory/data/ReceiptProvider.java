package com.waakye.android.receiptinventory.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
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
                        String sortOrder) {
        return null;
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
