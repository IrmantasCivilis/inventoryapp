package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import static android.content.ContentUris.withAppendedId;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */

public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0/* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView productNameTextView = (TextView) view.findViewById(R.id.product_name);
        TextView productPriceTextView = (TextView) view.findViewById(R.id.product_price);
        TextView productQuantityTextView = (TextView) view.findViewById(R.id.product_quantity);
        ImageButton productSaleImageButton = (ImageButton) view.findViewById(R.id.sale_button);

        // Find the columns of product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);

        // Read the product attributes from the Cursor for the current product
        final int productId = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
        String productName = cursor.getString(nameColumnIndex);
        Double productPrice = cursor.getDouble(priceColumnIndex);
        final int productQuantity = cursor.getInt(quantityColumnIndex);

        String price = Double.toString(productPrice);
        String quantity = Integer.toString(productQuantity);

        // Update the TextViews with the attributes for the current product
        productNameTextView.setText(productName);
        productPriceTextView.setText(price);
        productQuantityTextView.setText(quantity);

        // Set a on click listener on a sale button which reduces the quantity by one
        productSaleImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int position = cursor.getPosition();
                cursor.moveToPosition(position);

                if (productQuantity > 0) {
                    int newQuantity = productQuantity - 1;
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
                    Uri newUri = withAppendedId(ProductEntry.CONTENT_URI, productId);
                    context.getContentResolver().update(newUri, values, null, null);
                } else {
                    Toast.makeText(context, "No stock", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
