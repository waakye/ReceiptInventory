package com.waakye.android.receiptinventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.waakye.android.receiptinventory.data.ReceiptContract;

public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    /** Identifier for the receipt data loader */
    private static final int RECEIPT_LOADER = 0;

    /** Adapter for the ListView */
    ReceiptCursorAdapter mCursorAdapter;

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

        // Find the ListView which will be populated with the receipt data
        ListView receiptListView = (ListView)findViewById(R.id.list);

        // Find and set empty view on the listview so that it only shows when the list has 0 items
        View emptyView = findViewById(R.id.empty_view);
        receiptListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of receipt data in the Cursor
        // There is no receipt data yet (until the loader finishes) so pass in null for the Cursor
        mCursorAdapter = new ReceiptCursorAdapter(this, null);
        receiptListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        receiptListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id){
                // Create a new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific receipt that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ReceiptEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.waakye.android.receiptinventory/receipts/2"
                // if the receipt with ID 2 was clicked on.
                Uri currentReceiptUri
                        = ContentUris.withAppendedId(ReceiptContract.ReceiptEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentReceiptUri);

                // Launch the {@link EditorActivity} to display the data for the current receipt
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(RECEIPT_LOADER, null, this);
    }

    // TODO: Define a projection and query to search for the quantity of a specific receipt

    /**
     * Helper method to insert hardcoded receipt data into the database.  For debugging purposes only.
     */
    private void insertReceipt() {
        // Create a ContentValues object where column names are the keys, and Roberto's receipt
        // attributes are the values
        ContentValues values = new ContentValues();
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME, "Roberto's");
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE, 15);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY, 3);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_TYPE,
                ReceiptContract.ReceiptEntry.RECEIPT_MEALS);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_IMAGE_URI, "content://");

        // Insert a new row for Roberto's into the provider using the ContentResolver.
        // Use the {@link ReceiptEntry#CONTENT_URI} to indicate we want to insert into the
        // receipts database table.
        // Receive the new content URI that will allow us to access Roberto's data in the future
        Uri newUri = getContentResolver().insert(ReceiptContract.ReceiptEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all receipts in the database
     */
    private void deleteAllReceipts(){
        int rowsDeleted = getContentResolver().delete(ReceiptContract.ReceiptEntry.CONTENT_URI,
                null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from receipts database");
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
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllReceipts();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about
        String[] projection = {
                ReceiptContract.ReceiptEntry._ID,
                ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME,
                ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE,
                ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                ReceiptContract.ReceiptEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ReceiptCursorAdapter} with this new cursor containing updated receipt data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
