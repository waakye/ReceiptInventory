package com.waakye.android.receiptinventory;

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
     */
    private int mReceiptType = 0;

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
                        mReceiptType = 0; // Unknown
                    } else if (selection.equals(getString(R.string.receipt_type_lodging))){
                        mReceiptType = 1; // Lodging
                    } else if (selection.equals(getString(R.string.receipt_type_meals))){
                        mReceiptType = 2; // Meals
                    } else if (selection.equals(getString(R.string.receipt_type_transportation))){
                        mReceiptType = 3; // Transportation
                    } else if (selection.equals(getString(R.string.receipt_type_entertainment))){
                        mReceiptType = 4; // Entertainment
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent){
                mReceiptType = 0; // Unknown
            }
        });
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
                // Do nothing for now
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
