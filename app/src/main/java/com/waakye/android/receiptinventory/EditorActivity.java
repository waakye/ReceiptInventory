package com.waakye.android.receiptinventory;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.waakye.android.receiptinventory.data.ReceiptContract;
import com.waakye.android.receiptinventory.data.ReceiptDbHelper;

/**
 * Created by lesterlie on 7/12/17.
 * Allows the user to create a new receipt or edit an existing one.
 */

public class EditorActivity extends AppCompatActivity {

    /** EditText field to enter the receipt's name */
    private EditText mNameEditText;

    /** EditText field to enter the receipt's price or cost */
    private EditText mPriceEditText;

    /** EditText field to enter the receipt's quantity */
    private EditText mQuantityEditText;

    /** EditText field to enter the receipt's type */
    private Spinner mReceiptTypeSpinner;

    /**
     * Type of the receipt.  The possible values are:
     * 0 for Unknown, 1 for Lodging, 2 for Meals, 3 for Transportation, 4 for Entertainment
     * Type of the receipt. The possible values are in the ReceiptContract.java file
     */
    private int mReceiptType = ReceiptContract.ReceiptEntry.RECEIPT_UNKNOWN;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all the relevant views that we need to read user input from
        mNameEditText = (EditText)findViewById(R.id.edit_receipt_name);
        mPriceEditText = (EditText)findViewById(R.id.edit_receipt_price);
        mQuantityEditText = (EditText)findViewById(R.id.edit_receipt_quantity);
        mReceiptTypeSpinner = (Spinner)findViewById(R.id.spinner_receipt_type);

        setupSpinner();

    }

    /**
     * Setup the dropdown spinner that allows the user to select the type of the receipt
     */
    private void setupSpinner(){
        // Create an adapter for the spinner.  The list options are from the String array it will
        // use and the spinner will use the default layout
        ArrayAdapter receiptTypeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_receipt_type_options, android.R.layout.simple_spinner_item);

        // Specify the dropdown layout style - simple list view with 1 item per line
        receiptTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mReceiptTypeSpinner.setAdapter(receiptTypeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mReceiptTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position,long id){
                String selection = (String)parent.getItemAtPosition(position);
                if(!TextUtils.isEmpty(selection)){
                    if(selection.equals(getString(R.string.receipt_type_unknown))){
                        mReceiptType = ReceiptContract.ReceiptEntry.RECEIPT_UNKNOWN;
                    } else if (selection.equals(getString(R.string.receipt_type_lodging))){
                        mReceiptType = ReceiptContract.ReceiptEntry.RECEIPT_LODGING;
                    } else if (selection.equals(getString(R.string.receipt_type_meals))){
                        mReceiptType = ReceiptContract.ReceiptEntry.RECEIPT_MEALS;
                    } else if (selection.equals(getString(R.string.receipt_type_transportation))){
                        mReceiptType = ReceiptContract.ReceiptEntry.RECEIPT_TRANSPORTATION;
                    } else if (selection.equals(getString(R.string.receipt_type_entertainment))){
                        mReceiptType = ReceiptContract.ReceiptEntry.RECEIPT_ENTERTAINMENT;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent){
                mReceiptType = ReceiptContract.ReceiptEntry.RECEIPT_UNKNOWN;
            }
        });
    }

    /**
     * Get user input from editor and save new receipt into database.
     */
    private void insertReceipt(){
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        int price = Integer.parseInt(priceString);
        int quantity = Integer.parseInt(quantityString);

        // Create database helper
        ReceiptDbHelper mDbHelper = new ReceiptDbHelper(this);

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are keys and receipt attributes from
        // the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME, nameString);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE, price);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY, quantity);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_TYPE, mReceiptType);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_IMAGE_URI, "content://");

        // Insert a new row for receipt in the database, returning the ID of that new row
        long newRowId = db.insert(ReceiptContract.ReceiptEntry.TABLE_NAME, null, values);

        // Show a toast message depending on whether or not the insertion was successful
        if(newRowId == -1){
            // If the row ID is -1, then there was an error with insertion
            Toast.makeText(this, "Error with saving receipt", Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID
            Toast.makeText(this, "Receipt saved with row ID: " + newRowId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // User clicked on a menu option in the app bar overflow menu
        switch(item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save receipt to database
                insertReceipt();
                // Exit activity
                finish();
                return true;
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
