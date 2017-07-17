package com.waakye.android.receiptinventory;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.waakye.android.receiptinventory.data.ReceiptContract;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by lesterlie on 7/12/17.
 * Allows the user to create a new receipt or edit an existing one.
 */

public class EditorActivity extends AppCompatActivity {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

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

    /** Variables and constants related to image picker */
    private static final int PICK_IMAGE_REQUEST = 0;
//    private static final int SEND_MAIL_REQUEST = 1;

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private boolean isGalleryPicture = false;

    private static final String STATE_URI = "STATE_URI";

    private ImageView mImageView;
    private TextView mTextView;
    private Button imageButton;

    private Uri mUri;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity, in order to figure out if
        // we're creating a new receipt or editing an existing one.
        Intent intent = getIntent();
        Uri currentReceiptUri = intent.getData();

        // If the intent DOES NOT contain a receipt content URI, then we know that we are creating
        // a new receipt
        if(currentReceiptUri == null){
            // This is a new receipt, so change the app bar to say "Add a Receipt"
            setTitle(getString(R.string.editor_activity_title_new_receipt));
        } else {
            // Otherwise, this an existing receipt, so change app bar to say "Edit Receipt"
            setTitle(getString(R.string.editor_activity_title_edit_receipt));
        }

        // Find all the relevant views that we need to read user input from
        mNameEditText = (EditText)findViewById(R.id.edit_receipt_name);
        mPriceEditText = (EditText)findViewById(R.id.edit_receipt_price);
        mQuantityEditText = (EditText)findViewById(R.id.edit_receipt_quantity);
        mReceiptTypeSpinner = (Spinner)findViewById(R.id.spinner_receipt_type);

        setupSpinner();

        mTextView = (TextView) findViewById(R.id.image_uri);
        mImageView = (ImageView) findViewById(R.id.image);

        imageButton = (Button)findViewById(R.id.add_image_button);
        imageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openImageSelector();
            }
        });
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

        // Create a ContentValues object where column names are keys and receipt attributes from
        // the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME, nameString);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE, price);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY, quantity);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_TYPE, mReceiptType);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_IMAGE_URI, "content://");

        // Insert a new receipt into the provider, returning the content URI for the new receipt
        Uri newUri = getContentResolver().insert(ReceiptContract.ReceiptEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newUri == null){
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_insert_receipt_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_insert_receipt_successful),
                    Toast.LENGTH_SHORT).show();
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


    public void openImageSelector() {
        Log.e(LOG_TAG, "openImageSelector() called ...");
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    /**
     * After the user selects a document in the picker, onActivityResult() gets called.
     * The resultData parameter contains the URI that points to the selected document.
     * Extract the URI using getData().  When you have it, you can use it to retrieve the document
     * the user wants
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.i(LOG_TAG, "Received an \"Activity Result\"");
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());

                mTextView.setText(mUri.toString());
                mBitmap = getBitmapFromUri(mUri);
                mImageView.setImageBitmap(mBitmap);

                isGalleryPicture = true;
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Log.i(LOG_TAG, "Uri: " + mUri.toString());

            mTextView.setText(mUri.toString());
            mBitmap = getBitmapFromUri(mUri);
            mImageView.setImageBitmap(mBitmap);

            isGalleryPicture = false;
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        Log.e(LOG_TAG, "getBitmapFromUri() method called ... ");
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }

}
