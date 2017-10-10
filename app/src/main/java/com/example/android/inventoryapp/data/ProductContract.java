package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The schema of products table.
 */

public final class ProductContract {

    /**
     * Name for ContentProvider
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    /**
     * Base Uri of ContentProvider
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_PRODUCTS = "products";

    private ProductContract() {
    }

    /**
     * Inner class that defines constant values for the products table.
     * Each entry in the table represents a single product.
     */
    public static class ProductEntry implements BaseColumns {

        /**
         * The content URI to access the product data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * Name of table in the database
         */
        public static final String TABLE_NAME = "products";

        /**
         * Table column names
         */                                                                    // Data types
        public static final String _ID = BaseColumns._ID;                      // Integer
        public static final String COLUMN_PRODUCT_NAME = "name";               // Text
        public static final String COLUMN_PRODUCT_DESCRIPTION = "description"; // Text
        public static final String COLUMN_PRODUCT_PRICE = "price";             // Real
        public static final String COLUMN_PRODUCT_QUANTITY = "stock";          // Integer
        public static final String COLUMN_PRODUCT_SUPPLIER_NAME = "supplier";  // Text
        public static final String COLUMN_PRODUCT_SUPPLIER_PHONE = "phone";    // Text
        public static final String COLUMN_PRODUCT_SUPPLIER_EMAIL = "email";    // Text
        public static final String COLUMN_PRODUCT_IMAGE = "image";             // Text
    }
}






