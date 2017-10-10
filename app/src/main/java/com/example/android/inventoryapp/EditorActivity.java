package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import java.util.Locale;

import static java.lang.Integer.parseInt;

/**
 * This activity is for:
 * - adding a new product;
 * - seeing an existing product's detail description and editing it.
 * <p>
 * The code related to adding an image from external storage and saving it to the app database is
 * taken (with some modifications) from other Udacity students shared projects or mentors examples.
 * Links:
 * https://github.com/AKBwebdev/StoreInventory
 * https://github.com/crlsndrsjmnz/MyShareImageExample
 * https://github.com/mgerjikov/InventoryProject
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final String STATE_IMAGE_URI = "STATE_IMAGE_URI";
    Context context = this;
    int productQuantity;
    private EditText mProductNameEditText;
    private EditText mDescriptionEditText;
    private EditText mPriceEditText;
    private EditText mStockEditText;
    private EditText mSupplierNameEditText;
    private EditText mPhoneEditText;
    private EditText mEmailEditText;
    private ImageView mImageViewPhone;
    private ImageView mImageViewEmail;
    private ImageView mProductImageView;
    private Uri mCurrentProductUri;
    private Uri mImageUri;
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_detail);

        // Find layout elements
        ImageView mImageViewDecrease = (ImageView) findViewById(R.id.button_decrease_stock);
        ImageView mImageViewIncrease = (ImageView) findViewById(R.id.button_increase_stock);
        mImageViewEmail = (ImageView) findViewById(R.id.image_email);
        mImageViewPhone = (ImageView) findViewById(R.id.image_phone);
        mProductNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_description);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mStockEditText = (EditText) findViewById(R.id.edit_stock);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mPhoneEditText = (EditText) findViewById(R.id.edit_phone);
        mEmailEditText = (EditText) findViewById(R.id.edit_email);
        mImageViewEmail = (ImageView) findViewById(R.id.image_email);
        mImageViewPhone = (ImageView) findViewById(R.id.image_phone);
        mProductImageView = (ImageView) findViewById(R.id.image_product);
        ImageButton mAddPhotoImageButton = (ImageButton) findViewById(R.id.button_add_photo);

        // Set on touch listener to views which can be modified
        mImageViewDecrease.setOnTouchListener(mTouchListener);
        mImageViewIncrease.setOnTouchListener(mTouchListener);
        mProductNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mStockEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);
        mAddPhotoImageButton.setOnTouchListener(mTouchListener);

        // Examine the intent that was used to launch this activity,
        // in oder to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {

            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));

            mImageViewDecrease.setVisibility(View.GONE);
            mImageViewIncrease.setVisibility(View.GONE);
            mImageViewEmail.setVisibility(View.GONE);
            mImageViewPhone.setVisibility(View.GONE);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            mImageViewDecrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    productQuantity = Integer.parseInt(mStockEditText.getText().toString().trim());

                    if (productQuantity > 0) {
                        int newQuantity = productQuantity - 1;
                        mStockEditText.setText(String.valueOf(newQuantity));

                        ContentValues values = new ContentValues();
                        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
                        getContentResolver().update(mCurrentProductUri, values, null, null);

                    } else {
                        Toast.makeText(context, "No stock", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            mImageViewIncrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    productQuantity = Integer.parseInt(mStockEditText.getText().toString().trim());
                    productQuantity++;
                    mStockEditText.setText(String.valueOf(productQuantity));
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
                    getContentResolver().update(mCurrentProductUri, values, null, null);
                }
            });

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mAddPhotoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
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
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Get user input from editor and save product into database.
     */
    private void saveProduct() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mProductNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String stockString = mStockEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();
        String imagePath;
        if (mImageUri != null) {
            imagePath = mImageUri.toString();
        } else {
            imagePath = "";
        }

        // Check if required fields are filled.
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.toast_for_name),
                    Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.toast_for_price),
                    Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, getString(R.string.toast_for_supplier_name),
                    Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(emailString)) {
            Toast.makeText(this, getString(R.string.toast_for_email),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Create a ContentValues object where column names are the keys,
            // and pet attributes from the editor are the values.
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
            values.put(ProductEntry.COLUMN_PRODUCT_DESCRIPTION, descriptionString);
            values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
            values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierNameString);
            values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, phoneString);
            values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, emailString);
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imagePath);

            // If the quantity is not provided by the user, don't try to parse the string into an
            // integer value. Use 0 by default.
            int quantity = 0;
            if (!TextUtils.isEmpty(stockString)) {
                quantity = parseInt(stockString);
            }
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

            // Determine if this is a new or existing product by checking if mCurrentPetUri is null or not
            if (mCurrentProductUri == null) {

                // Insert a new product into the provider, returning the content URI for the new product.
                Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                // Otherwise this is an EXISTING product, so update the product with content URI:
                // mCurrentProductUri and pass in the new ContentValues.
                int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_product_successful),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {

        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the products table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_DESCRIPTION,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_IMAGE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,     // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_DESCRIPTION);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int stockColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
            int emailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            // Extract out the value from the Cursor for the given column index
            final String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            int stock = cursor.getInt(stockColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            final String phone = cursor.getString(phoneColumnIndex);
            final String email = cursor.getString(emailColumnIndex);
            final String image = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mProductNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            mPriceEditText.setText(String.format(Locale.ROOT, "%.02f", price));
            mStockEditText.setText(String.valueOf(stock));
            mSupplierNameEditText.setText(supplier);
            mPhoneEditText.setText(phone);
            mEmailEditText.setText(email);

            try {
                mImageUri = Uri.parse(image);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            // Display image attached to the product
            ViewTreeObserver viewTreeObserver = mProductImageView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mProductImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mProductImageView.setImageURI(mImageUri);
                }
            });

            mImageViewEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Order of " + name);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });

            mImageViewPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phone));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductNameEditText.setText("");
        mDescriptionEditText.setText("");
        mPriceEditText.setText("");
        mStockEditText.setText("");
        mSupplierNameEditText.setText("");
        mPhoneEditText.setText("");
        mEmailEditText.setText("");
    }

    public void openImageSelector() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            try {
                mImageUri = resultData.getData();
                int takeFlags = resultData.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                try {
                    getContentResolver().takePersistableUriPermission(mImageUri, takeFlags);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                mProductImageView.setImageURI(mImageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mImageUri != null) {
            outState.putString(STATE_IMAGE_URI, mImageUri.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_IMAGE_URI) &&
                !savedInstanceState.getString(STATE_IMAGE_URI).equals("")) {
            mImageUri = Uri.parse(savedInstanceState.getString(STATE_IMAGE_URI));

            ViewTreeObserver viewTreeObserver = mProductImageView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mProductImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mProductImageView.setImageURI(mImageUri);
                }
            });
        }
    }
}
