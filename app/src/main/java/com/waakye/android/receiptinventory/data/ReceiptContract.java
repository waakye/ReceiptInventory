package com.waakye.android.receiptinventory.data;

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
     * Inner class that defines constant values for the receipts database table.
     * Each entry in the table represents a single receipt.
     */
    public static final class ReceiptEntry implements BaseColumns {

        /** Name of database table for receipts */
        public final static String TABLE_NAME = "receipts";

        /**
         * Unique ID number for the receipt (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

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
         * Possible values for the type of the receipt
         */
        public static final int RECEIPT_UNKNOWN = 0;
        public static final int RECEIPT_LODGING = 1;
        public static final int RECEIPT_MEALS = 2;
        public static final int RECEIPT_TRANSPORTATION = 3;
        public static final int RECEIPT_ENTERTAINMENT = 4;
    }
}
