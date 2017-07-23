package com.waakye.android.receiptinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.waakye.android.receiptinventory.data.ReceiptContract;

/**
 * Created by lesterlie on 7/15/17.
 */

public class ReceiptCursorAdapter extends CursorAdapter {

    public static final String LOG_TAG = ReceiptCursorAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link ReceiptCursorAdapter}
     *
     * @param context   The context
     * @param c         The cursor from which to get data
     */
    public ReceiptCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view.  No data is set (or bound) to the views yet
     *
     * @param context   app context
     * @param cursor    The cursor from which to get the data.  The cursor is already moved to the
     *                  correct position
     * @param parent    The parent to which the new view is attached to
     * @return  the newly created list item view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.e(LOG_TAG, "newView() method called ...");
        // Inflate a list item view using the layout specified in the receipt_list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.receipt_list_item, parent, false);
    }

    /**
     * This method binds the receipt data (in the current row pointed by the cursor) to the given
     * list item layout.  For example, the name for the current receipt can be set on the name
     * TextView in the list item layout
     *
     * @param view      Existing view, returned earlier by newView() method
     * @param context   app context
     * @param cursor    The cursor from which to get the data.  The cursor is already moved to the
     *                  correct row.
     *
     *                  Seems that the bindView items (name, price, quantity) must match the
     *                  projection from the CursorLoader in the CatalogActivity
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        Log.e(LOG_TAG, "bindView() method called...");
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView)view.findViewById(R.id.receipt_name);
        TextView priceTextView = (TextView)view.findViewById(R.id.receipt_price);
        TextView quantityTextView = (TextView)view.findViewById(R.id.receipt_quantity);
//        TextView receiptTypeTextView = (TextView) view.findViewById(R.id.receipt_type);
//        TextView imageUriTextView = (TextView)view.findViewById(R.id.receipt_image_uri);

        // Find the columns of receipt attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY);
//        int receiptTypeColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_TYPE);
//        int imageUriColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_IMAGE_URI);

        // Read the receipt attributes from the Cursor for the current receipt
        String receiptName = cursor.getString(nameColumnIndex);
        int receiptPrice = cursor.getInt(priceColumnIndex);
        final int receiptQuantity = cursor.getInt(quantityColumnIndex);
//        String receiptType = cursor.getString(receiptTypeColumnIndex);
//        String receiptImageUri = cursor.getString(imageUriColumnIndex);

        // If the receipt type is empty string or null, then use some default text that says
        // "Unknown Type", so that TextView isn't blank
//        if(TextUtils.isEmpty(receiptType)) {
//            receiptType = context.getString(R.string.unknown_receipt_type);
//        }

        // get row ID
        final int rowId = cursor.getInt(idColumnIndex);

        // Update the TextViews with the attributes for the current receipt
        nameTextView.setText(receiptName);
        priceTextView.setText(receiptPrice);
        quantityTextView.setText(receiptQuantity);
//        receiptTypeTextView.setText(receiptType);
//        imageUriTextView.setText(receiptImageUri);

        Button saleButton = (Button) view.findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Uri currentReceiptUri =
                        ContentUris.withAppendedId(ReceiptContract.ReceiptEntry.CONTENT_URI, rowId);
                reduceByOne(context, receiptQuantity, currentReceiptUri);
            }
        });
    }

    /**
     * Reduce quantity of receipt by one
     * @param context   activity context
     * @param receiptUri  Uri to update receipt
     * @param quantity    current receipt quantity
     */
    private void reduceByOne(Context context, int quantity, Uri receiptUri) {
        if (quantity == 0) {
            Log.v("ReceiptCursorAdapter", "quantity cannot be reduced");
        } else {
            int newQuantity = quantity - 1;
            Log.v("ReceiptCursorAdapter", "new quantity is: " + newQuantity);

            // Create content value
            ContentValues values = new ContentValues();
            values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY, newQuantity);
            int rowsAffected = context.getContentResolver().update(receiptUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful
            if(rowsAffected == 0){
                Log.v("ReceiptCursorAdapter", "no rows changed");
            } else {
                Log.v("ReceiptCursorAdapter", "rows changed = " + rowsAffected);
            }
        }
    }

}
