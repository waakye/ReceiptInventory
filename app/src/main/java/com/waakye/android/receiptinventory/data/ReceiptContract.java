package com.waakye.android.receiptinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by lesterlie on 7/13/17.
 * API Contract for the ReceiptInventory app
 */

public final class ReceiptContract {

    // To prevent someone from accidentally instantiating the contract class, give it an empty
    // constructor.
    private ReceiptContract(){}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on
     * the device.
     */
    public static final String CONTENT_AUTHORITY = "com.waakye.android.receiptinventory";

    /**
     * Use the CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.waakye.android.receiptinventory/receipts/" is a valid path for
     * looking at receipt data.
     */
    public static final String PATH_RECEIPTS = "receipts";

    /**
     * Inner class that defines constant values for the receipts database table.
     * Each entry in the table represents a single receipt.
     */
    public static final class ReceiptEntry implements BaseColumns {

        /** The content URI to access the receipt data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_RECEIPTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of receipts
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECEIPTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single receipt.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECEIPTS;

        /** Name of database table for receipts */
        public final static String TABLE_NAME = "receipts";

        /**
         * Unique ID number for the receipt (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the vendor for the receipt
         *
         * Type: TEXT
         */
        public final static String COLUMN_RECEIPT_NAME = "name";

        /**
         * Cost (or price) of the receipt.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_RECEIPT_PRICE = "price";

        /**
         * Quantity of the receipts
         *
         * Type: INTEGER
         */
        public final static String COLUMN_RECEIPT_QUANTITY = "quantity";

        /**
         * Type of the receipt.
         *
         * The only possible values are {@link #RECEIPT_UNKNOWN}, {@link #RECEIPT_LODGING},
         * {@link #RECEIPT_MEALS}, {@link #RECEIPT_TRANSPORTATION}, {@link #RECEIPT_ENTERTAINMENT}.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_RECEIPT_TYPE = "receipt_type";

        /**
         * Image uri for the receipt
         *
         * Type: TEXT
         */
        public final static String COLUMN_RECEIPT_IMAGE_URI = "image_uri";

        /**
         * Image bitmap for the receipt's image
         *
         * Type: String
         */
        public final static String COLUMN_RECEIPT_IMAGE = "image";

        /**
         * Possible values for the type of the receipt
         */
        public static final int RECEIPT_UNKNOWN = 0;
        public static final int RECEIPT_LODGING = 1;
        public static final int RECEIPT_MEALS = 2;
        public static final int RECEIPT_TRANSPORTATION = 3;
        public static final int RECEIPT_ENTERTAINMENT = 4;

        /**
         * Returns whether or not the given type is {@link #RECEIPT_UNKNOWN},
         * {@link #RECEIPT_LODGING}, {@link #RECEIPT_MEALS}, {@link #RECEIPT_TRANSPORTATION},
         * {@link #RECEIPT_ENTERTAINMENT}
         */
        public static boolean isValidType(int receiptType){
            if(receiptType == RECEIPT_UNKNOWN || receiptType == RECEIPT_LODGING ||
                    receiptType == RECEIPT_MEALS || receiptType == RECEIPT_TRANSPORTATION ||
                    receiptType == RECEIPT_ENTERTAINMENT) {
                return true;
            }
            return false;
        }
    }
}
