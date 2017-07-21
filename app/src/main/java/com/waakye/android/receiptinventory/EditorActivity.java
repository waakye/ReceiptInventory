package com.waakye.android.receiptinventory;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /** Identifier for the receipt data loader */
    private static final int EXISTING_RECEIPT_LOADER = 0;

    /** Content URI for the existing receipt (null if it's a new receipt) */
    private Uri mCurrentReceiptUri;

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

    /** Boolean flag that keeps track of whether the receipt has been edited (true) or not (false) */
    private boolean mReceiptHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mReceiptHasChanged boolean to true
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mReceiptHasChanged = true;
            return false;
        }
    };

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
        mCurrentReceiptUri = intent.getData();

        // If the intent DOES NOT contain a receipt content URI, then we know that we are creating
        // a new receipt
        if (mCurrentReceiptUri == null){
            // This is a new receipt, so change the app bar to say "Add a Receipt"
            setTitle(getString(R.string.editor_activity_title_new_receipt));

            // Invalidate the options menu, so the "Delete" menu option can be hidden
            // (It doesn't make sense to delete a receipt that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise, this an existing receipt, so change app bar to say "Edit Receipt"
            setTitle(getString(R.string.editor_activity_title_edit_receipt));

            // Initialize a loader to read the receipt data from the database
            // and display the current values in the editor.
            getLoaderManager().initLoader(EXISTING_RECEIPT_LOADER, null, this);
        }

        // Find all the relevant views that we need to read user input from
        mNameEditText = (EditText)findViewById(R.id.edit_receipt_name);
        mPriceEditText = (EditText)findViewById(R.id.edit_receipt_price);
        mQuantityEditText = (EditText)findViewById(R.id.edit_receipt_quantity);
        mReceiptTypeSpinner = (Spinner)findViewById(R.id.spinner_receipt_type);

        // Set up OnTouchListeners on all the input fields, so we can determine if the user has
        // touched or modified them.  This will let us know if there are unsaved changes or not,
        // if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mReceiptTypeSpinner.setOnTouchListener(mTouchListener);

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
    private void saveReceipt(){
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        // Check if this is supposed to be a new receipt and check if all the fields in the editor
        // are blank
        if(mCurrentReceiptUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) &&
                mReceiptType == ReceiptContract.ReceiptEntry.RECEIPT_UNKNOWN){
            // Since no fields were modified, we can return early without creating a new receipt.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are keys and receipt attributes from
        // the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME, nameString);
        // If the price or quantity is not provided by the user, don't try to parse the string into
        // an integer value.  Use 0 by default
        int price = 0;
        if(!TextUtils.isEmpty(priceString)){
            price = Integer.parseInt(priceString);
        }
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE, price);

        int quantity = 0;
        if(!TextUtils.isEmpty(quantityString)){
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY, quantity);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_TYPE, mReceiptType);
        values.put(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_IMAGE_URI, "content://");

        // Determine if this is a new or existing receipt by checking if mCurrentReceiptUri is null
        // or not
        if(mCurrentReceiptUri == null) {
            // This is a NEW receipt, so insert a new receipt into the provider, returning the
            // content URI for the new receipt
            Uri newUri = getContentResolver().insert(ReceiptContract.ReceiptEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if(newUri == null) {
                // If the new content URI is null, then there was an error with insertion
                Toast.makeText(this, getString(R.string.editor_insert_receipt_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast
                Toast.makeText(this, getString(R.string.editor_insert_receipt_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise, this is an EXISTING receipt, so update the receipt with content URI:
            // mCurrentReceiptUri and pass in the new ContentValues.  Pass in null for the
            // selection and selection args because mCurrentReceiptUri will already identify the
            // correct row in the database that we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentReceiptUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if(rowsAffected == 0){
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_receipt_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast
                Toast.makeText(this, getString(R.string.editor_update_receipt_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the menu can be updated
     * (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        // If this is a new receipt, hide the "Delete" menu item.
        if(mCurrentReceiptUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // User clicked on a menu option in the app bar overflow menu
        switch(item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save receipt to database
                saveReceipt();
                // Exit activity
                finish();
                return true;
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the receipt hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if(!mReceiptHasChanged){
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise, if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that changes should be
                // discarded
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed
     */
    @Override
    public void onBackPressed(){
        // If the receipt hasn't changed, continue with handling back button press
        if(!mReceiptHasChanged){
            super.onBackPressed();
            return;
        }

        // Otherwise, if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming the changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all receipt attributes, define a projection that contains all
        // columns from the receipts table
        String[] projection = {
                ReceiptContract.ReceiptEntry._ID,
                ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME,
                ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE,
                ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY,
                ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_TYPE,
                ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_IMAGE_URI};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,       // Parent activity context
                mCurrentReceiptUri,         // Query the content URI for the current receipt
                projection,                 // Columns to include in the resulting Cursor
                null,                       // No selection clause
                null,                       // No selection arguments
                null);                      // Default sort order
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost if they
     * continue leaving the editor.
     *
     * @param discardButtonClickListener  is the click listener for what to do when the user
     *                                    confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners for the
        // positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the receipt
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // End early if the cursor is null or there is less than 1 row in the cursor
        if(cursor == null || cursor.getCount() < 1){
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if(cursor.moveToFirst()) {
            // Find the columns of the receipt attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_QUANTITY);
            int receiptTypeColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_TYPE);
            int imageUriColumnIndex = cursor.getColumnIndex(ReceiptContract.ReceiptEntry.COLUMN_RECEIPT_IMAGE_URI);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int receiptType = cursor.getInt(receiptTypeColumnIndex);
            String imageUri = cursor.getString(imageUriColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));

            // Type is a dropdown spinner so map the constant value from the database into one of
            // the dropdown options, then call setSelection() so that the option is displayed on
            // screen as the current selection
            switch(receiptType){
                case ReceiptContract.ReceiptEntry.RECEIPT_LODGING:
                    mReceiptTypeSpinner.setSelection(1);
                    break;
                case ReceiptContract.ReceiptEntry.RECEIPT_MEALS:
                    mReceiptTypeSpinner.setSelection(2);
                    break;
                case ReceiptContract.ReceiptEntry.RECEIPT_TRANSPORTATION:
                    mReceiptTypeSpinner.setSelection(3);
                    break;
                case ReceiptContract.ReceiptEntry.RECEIPT_ENTERTAINMENT:
                    mReceiptTypeSpinner.setSelection(4);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out the data from the input fields
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mReceiptTypeSpinner.setSelection(0); // Select "Unknown" Receipt Type
    }
}
