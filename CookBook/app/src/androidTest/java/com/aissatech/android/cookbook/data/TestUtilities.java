package com.aissatech.android.cookbook.data;

/**
 * Created by aissa_000 on 06/04/2015.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.aissatech.android.cookbook.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/*
    Students: These are functions and some test data to make it easier to test your database and
    Content Provider.  Note that you'll want your RecipeContract class to exactly match the one
    in our solution to use these as-given.
 */
public class TestUtilities extends AndroidTestCase {
    static final String TEST_LOCATION = "40121";//Bologna
//    static final String TEST_FOOD = "lasagna";

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        default Recipe values for the database tests - OK
     */
    static ContentValues createRecipeValues(long locationRowId) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_LOC_KEY, locationRowId);
//        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_PUBLISHER, "The Pioneer Woman");
//        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_URL, "http://food2fork.com/view/47036");
//        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_TITLE, "Vegetable Lasagna");
//        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_SOURCE_URL, "http://thepioneerwoman.com/cooking/2011/04/vegetable-lasagna/");
//        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_RECIPE_ID, "47036");
//        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_IMAGE_URL, "http://static.food2fork.com/lasagnad1d7.jpg");
//        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_SOCIAL_RANK, 99.9999999995794);//99.99999999957947
//        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_PUBLISHER_URL, "http://thepioneerwoman.com");
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_PUBLISHER, "Chow");
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_URL, "http://food2fork.com/view/c32bc2");
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_TITLE, "Little Italy Recipe");
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_SOURCE_URL, "http://www.chow.com/recipes/28337-little-italy");
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_RECIPE_ID, "c32bc2");
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_IMAGE_URL, "http://static.food2fork.com/28337_little_italy_600817c.jpg");
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_SOCIAL_RANK, 82.1692651734995);//82.16926517349957
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_PUBLISHER_URL, "http://www.chow.com");

        return recipeValues;
    }

    /*
       default Recipe values for the database tests - OK
    */
    static ContentValues createRecipeValues() {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_LOC_KEY, TEST_LOCATION);
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_PUBLISHER, "Bon Appetit");
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_URL, "http://food2fork.com/view/48979");
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_TITLE, "Squash and Broccoli Rabe Lasagna");
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_SOURCE_URL, "http://www.bonappetit.com/recipes/2012/10/squash-and-broccoli-rabe-lasagna");
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_RECIPE_ID, "48979");
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_IMAGE_URL, "http://static.food2fork.com/squashandbroccolirabelasagna646e0f9.jpg");
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_SOCIAL_RANK, 99.9999941239205);//99.99999412392056
        recipeValues.put(RecipeContract.RecipeEntry.COLUMN_PUBLISHER_URL, "http://www.bonappetit.com");

        return recipeValues;
    }

    /*
        test for insert Italy Location values - OK
    */

    static long insertRecipeValues(Context context) {
        // insert our test records into the database
        RecipeDbHelper dbHelper = new RecipeDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // content values
        ContentValues testValues = TestUtilities.createRecipeValues();

        // return the location row id to use as foreign key for the recipe table
        long locationRowId;
        locationRowId = db.insert(RecipeContract.RecipeEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Italy Location Values", locationRowId != -1);

        return locationRowId;
    }

    /*
        test for creating the the Location ContentValues of the RecipeContract - OK
     */
    static ContentValues createItalyLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(RecipeContract.LocationEntry.COLUMN_LOCATION_SETTING, TEST_LOCATION);
        testValues.put(RecipeContract.LocationEntry.COLUMN_COUNTRY_NAME, "Italy");
        testValues.put(RecipeContract.LocationEntry.COLUMN_COORD_LAT, 43);//43.0000
        testValues.put(RecipeContract.LocationEntry.COLUMN_COORD_LONG, 12);//12.0000

        return testValues;
    }

    /*
        test for insert Italy Location values - OK
    */

    static long insertItalyLocationValues(Context context) {
        // insert our test records into the database
        RecipeDbHelper dbHelper = new RecipeDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createItalyLocationValues();

        // return the location row id to use as foreign key for the recipe table
        long locationRowId;
        locationRowId = db.insert(RecipeContract.LocationEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Italy Location Values", locationRowId != -1);

        return locationRowId;
    }


    /*
        Students: The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}

