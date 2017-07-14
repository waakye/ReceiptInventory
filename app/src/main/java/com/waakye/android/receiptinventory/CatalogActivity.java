package com.waakye.android.receiptinventory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.waakye.android.receiptinventory.data.ReceiptContract;
import com.waakye.android.receiptinventory.data.ReceiptDbHelper;

public class CatalogActivity extends AppCompatActivity {

    /** Database helper that will provide us access to the database */
    private ReceiptDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // To access our database, we instantiate our sublcass of SQLiteOpenHelper and pass the
        // context, which is the current activity.
        mDbHelper = new ReceiptDbHelper(this);

    }
    @Override
    protected void onStart(){
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the receipts database.
     */
    private void displayDatabaseInfo(){
        // Define a projection that specifies which columns from the database you will actually use
        // after the query.
        String[] projectionAllColumns = {
                ReceiptContract.ReceiptEntry._ID,
                ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME,
                ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE,
                ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY,
                ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_TYPE,
                ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_IMAGE_URI};

        // Perform a query on the provider using the ContentResolver.
        // Use the {@link ReceiptEntry#CONTENT_URI} to access the receipt data
        Cursor cursor = getContentResolver().query(
                ReceiptContract.ReceiptEntry.CONTENT_URI,   // The content URI of the words table
                projectionAllColumns,                       // The columns to return for each row
                null,                                       // Selection criteria
                null,                                       // Selection criteria
                null);                                      // The sort order for the returned rows

        TextView displayView = (TextView)findViewById(R.id.text_view_receipt);

        try {
            // Create a header in the Text View that looks like this:
            //
            // The receipts table contains <number of rows in Cursor> receipts
            // _id - name - price - quantity - type - image_uri
            //
            // In the while loop below, iterate through the rows of the cursor and display the
            // information from each column in this order
            displayView.setText("The receipts table contains " + cursor.getCount()
                    + " receipts.\n\n");
            displayView.append(ReceiptContract.ReceiptEntry._ID + " - "
                + ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME + " - "
                + ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE + " - "
                + ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY + " - "
                + ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_TYPE + " - "
                + ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_IMAGE_URI + "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY);
            int typeColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_TYPE);
            int imageUriColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_IMAGE_URI);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()){
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                int currentType = cursor.getInt(typeColumnIndex);
                String currentImageUri = cursor.getString(imageUriColumnIndex);
                // Display the values from each column of the current row in the TextView
                displayView.append("\n" + currentID + " - "
                    + currentName + " - "
                    + currentPrice + " - "
                    + currentQuantity + " - "
                    + currentType + " - "
                    + currentImageUri);
            }

        } finally {
            // Always close the cursor when you're done reading from it.  This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

    // TODO: Define a projection and query to search for the quantity of a specific receipt

    /**
     * Helper method to insert hardcoded receipt data into the database.  For debugging purposes only.
     */
    private void insertReceipt(){
        // Gets the database in write mode.
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys, and Roberto's receipt
        // attributes are the values
        ContentValues values = new ContentValues();
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME, "Roberto's");
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE, 15);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY, 3);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_TYPE,
                ReceiptContract.ReceiptEntry.RECEIPT_MEALS);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_IMAGE_URI, "content://");

        // Insert a new row for Roberto's in the database, returning the ID of that new row.
        // The first argument for db.insert() is the receipts table name.
        // The second argument provides the name of a column in which the framework can insert NULL
        // in the event that the ContentValues is empty (if this is set to "null", then the
        // framework will not insert a row when there are no values).
        // The third argument is the ContentValues object containing the info for Roberto's
        long newRowId = db.insert(ReceiptContract.ReceiptEntry.TABLE_NAME, null, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file
        // This adds menu items to the app bar
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch(item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertReceipt();
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
