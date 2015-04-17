package com.aissatech.android.cookbook.data;

/**
 * Created by aissa_000 on 06/04/2015.
 */
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

/*
    Note: This is not a complete set of tests of the Sunshine ContentProvider, but it does test
    that at least the basic functionality has been implemented correctly.

    Students: Uncomment the tests in this class as you implement the functionality in your
    ContentProvider to make sure that you've implemented things reasonably correctly.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

       Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
       the delete functionality in the ContentProvider.
     */
    // test OK
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                RecipeContract.RecipeEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                RecipeContract.LocationEntry.CONTENT_URI,
                null,
                null
        );
        // query the RecipeEntry: select * from recipe
        Cursor cursor = mContext.getContentResolver().query(
                RecipeContract.RecipeEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        // check if records in RecipeEntry was deleted
        assertEquals("Error: Records not deleted from Recipe table during delete", 0, cursor.getCount());
        cursor.close();

        // query the LocationEntry: select * from location
        cursor = mContext.getContentResolver().query(
                RecipeContract.LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        // check if records in LocationEntry was deleted
        assertEquals("Error: Records not deleted from Location table during delete", 0, cursor.getCount());
        cursor.close();
    }

    /*
       This helper function deletes all records from both database tables using the database
       functions only.  This is designed to be used to reset the state of the database until the
       delete functionality is available in the ContentProvider.
     */

    // test OK
    public void deleteAllRecordsFromDB() {
        RecipeDbHelper dbHelper = new RecipeDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(RecipeContract.RecipeEntry.TABLE_NAME, null, null);
        db.delete(RecipeContract.LocationEntry.TABLE_NAME, null, null);
        db.close();
    }

    /*
        Refactor this function to use the deleteAllRecordsFromProvider functionality once
        you have implemented delete functionality there.
     */

    // test OK
    public void deleteAllRecords() {
        deleteAllRecordsFromDB();
        //deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).

    // test OK
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
          Refactor this function to use the createBulkInsertRecipeValues functionality once
          you have implemented insert functionality there.
    */

    // test OK
    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertRecipeValues(long locationRowId) {

        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {
            ContentValues recipeValues = new ContentValues();
            recipeValues.put(RecipeContract.RecipeEntry.COLUMN_LOC_KEY, locationRowId);
            recipeValues.put(RecipeContract.RecipeEntry.COLUMN_PUBLISHER, "Bon Appetit");
            recipeValues.put(RecipeContract.RecipeEntry.COLUMN_URL, "http://food2fork.com/view/48979");
            recipeValues.put(RecipeContract.RecipeEntry.COLUMN_TITLE, "Squash and Broccoli Rabe Lasagna");
            recipeValues.put(RecipeContract.RecipeEntry.COLUMN_SOURCE_URL, "http://www.bonappetit.com/recipes/2012/10/squash-and-broccoli-rabe-lasagna");
            recipeValues.put(RecipeContract.RecipeEntry.COLUMN_RECIPE_ID, "48979");
            recipeValues.put(RecipeContract.RecipeEntry.COLUMN_IMAGE_URL, "http://static.food2fork.com/squashandbroccolirabelasagna646e0f9.jpg");
            recipeValues.put(RecipeContract.RecipeEntry.COLUMN_SOCIAL_RANK, 99.9999941239205);//99.99999412392056
            recipeValues.put(RecipeContract.RecipeEntry.COLUMN_PUBLISHER_URL, "http://www.bonappetit.com");
            returnContentValues[i] = recipeValues;
        }
        return returnContentValues;
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
        Students: Uncomment this test to make sure you've correctly registered the WeatherProvider.
     */



    // test OK
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // RecipeProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                RecipeProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: RecipeProvider registered with authority: " + providerInfo.authority +
                    " instead of authority: " + RecipeContract.CONTENT_AUTHORITY,
                    providerInfo.authority, RecipeContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: RecipeProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
            Students: Uncomment this test to verify that your implementation of GetType is
            functioning correctly.
         */

    // -------- test KO
//    public void testGetType() {
//        // content://com.aissatech.android.cookbook/recipe/
//        String type = mContext.getContentResolver().getType(RecipeContract.RecipeEntry.CONTENT_URI);
//        // vnd.android.cursor.dir/com.aissatech.android.cookbook/recipe
//        assertEquals("Error: the WeatherEntry CONTENT_URI should return WeatherEntry.CONTENT_TYPE",
//                RecipeContract.RecipeEntry.CONTENT_TYPE, type);
//
//        String testLocation = "Italy";
//        // content://com.aissatech.android.cookbook/recipe/Italy
//        type = mContext.getContentResolver().getType(
//                RecipeContract.RecipeEntry.buildRecipeLocation(testLocation));
//        // vnd.android.cursor.dir/com.aissatech.android.cookbook/recipe
//        assertEquals("Error: the RecipeEntry CONTENT_URI with location should return RecipeEntry.CONTENT_TYPE",
//                RecipeContract.RecipeEntry.CONTENT_TYPE, type);
//
//
//        // content://com.aissatech.android.cookbook/location/
//        type = mContext.getContentResolver().getType(RecipeContract.LocationEntry.CONTENT_URI);
//        // vnd.android.cursor.dir/com.aissatech.android.cookbook/location
//        assertEquals("Error: the LocationEntry CONTENT_URI should return LocationEntry.CONTENT_TYPE",
//                RecipeContract.LocationEntry.CONTENT_TYPE, type);
//    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if the basic recipe query functionality
        given in the ContentProvider is working correctly.
     */
    // -------- test KO
//    public void testBasicRecipeQuery() {
//        // insert our test records into the database
//        RecipeDbHelper dbHelper = new RecipeDbHelper(mContext);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        // test location values
//        ContentValues testValues = TestUtilities.createItalyLocationValues();
//        long locationRowId = TestUtilities.insertItalyLocationValues(mContext);
//
//        // Fantastic.  Now that we have a location, add some recipe!
//        ContentValues recipeValues = TestUtilities.createRecipeValues(locationRowId);
//
//        long recipeRowId = db.insert(RecipeContract.RecipeEntry.TABLE_NAME, null, recipeValues);
//        assertTrue("Unable to Insert RecipeEntry into the Database", recipeRowId != -1);
//
//        db.close();
//
//        // Test the basic content provider query
//        Cursor recipeCursor = mContext.getContentResolver().query(
//                RecipeContract.RecipeEntry.CONTENT_URI,
//                null,
//                null,
//                null,
//                null
//        );
//
//        // Make sure we get the correct cursor out of the database
//        TestUtilities.validateCursor("testBasicRecipeQuery", recipeCursor, recipeValues);
//    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if your location queries are
        performing correctly.
     */

    // test OK
    public void testBasicLocationQueries() {
        // insert our test records into the database
        RecipeDbHelper dbHelper = new RecipeDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createItalyLocationValues();
        long locationRowId = TestUtilities.insertItalyLocationValues(mContext);

        // Test the basic content provider query
        Cursor locationCursor = mContext.getContentResolver().query(
                RecipeContract.LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicLocationQueries, location query", locationCursor, testValues);

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Location Query did not properly set NotificationUri",
                    locationCursor.getNotificationUri(), RecipeContract.LocationEntry.CONTENT_URI);
        }
    }

    /*
        This test uses the provider to insert and then update the data. Uncomment this test to
        see if your update location is functioning correctly.
     */

    // test OK
    public void testUpdateLocation() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createItalyLocationValues();

        Uri locationUri = mContext.getContentResolver().
                insert(RecipeContract.LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(RecipeContract.LocationEntry._ID, locationRowId);
        updatedValues.put(RecipeContract.LocationEntry.COLUMN_COUNTRY_NAME, "England");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor locationCursor = mContext.getContentResolver().query(RecipeContract.LocationEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        locationCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                RecipeContract.LocationEntry.CONTENT_URI, updatedValues, RecipeContract.LocationEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // Students: If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        locationCursor.unregisterContentObserver(tco);
        locationCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                RecipeContract.LocationEntry.CONTENT_URI,
                null,   // projection
                RecipeContract.LocationEntry._ID + " = " + locationRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateLocation.  Error validating location entry update.",
                cursor, updatedValues);

        cursor.close();
    }


    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the insert functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.

    // test OK
    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createItalyLocationValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(RecipeContract.LocationEntry.CONTENT_URI, true, tco);
        Uri locationUri = mContext.getContentResolver().insert(RecipeContract.LocationEntry.CONTENT_URI, testValues);

        // Did our content observer get called?  Students:  If this fails, your insert location
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                RecipeContract.LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating LocationEntry.",
                cursor, testValues);

        // Fantastic.  Now that we have a location, add some recipe!
        ContentValues recipeValues = TestUtilities.createRecipeValues(locationRowId);
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(RecipeContract.RecipeEntry.CONTENT_URI, true, tco);

        Uri recipeInsertUri = mContext.getContentResolver()
                .insert(RecipeContract.RecipeEntry.CONTENT_URI, recipeValues);
        assertTrue(recipeInsertUri != null);

        // Did our content observer get called?  Students:  If this fails, your insert weather
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor recipeCursor = mContext.getContentResolver().query(
                RecipeContract.RecipeEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating RecipeEntry insert.",
                recipeCursor, recipeValues);

        // Add the location values in with the recipe data so that we can make
        // sure that the join worked and we actually get all the values back
        recipeValues.putAll(testValues);

        // Get the joined Recipe and Location data
        recipeCursor = mContext.getContentResolver().query(
                RecipeContract.RecipeEntry.buildRecipeLocation(TestUtilities.TEST_LOCATION),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Recipe and Location Data.",
                recipeCursor, recipeValues);

    }

    // Make sure we can still delete after adding/updating stuff
    // Uncomment this test after you have completed writing the delete functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.

    // test OK
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our location delete.
        TestUtilities.TestContentObserver locationObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(RecipeContract.LocationEntry.CONTENT_URI, true, locationObserver);

        // Register a content observer for our recipe delete.
        TestUtilities.TestContentObserver recipeObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(RecipeContract.RecipeEntry.CONTENT_URI, true, recipeObserver);

        deleteAllRecordsFromProvider();

        // Students: If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        locationObserver.waitForNotificationOrFail();
        recipeObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(locationObserver);
        mContext.getContentResolver().unregisterContentObserver(recipeObserver);
    }


    // Uncomment this test after you have completed writing the BulkInsert functionality
    // in your provider.  Note that this test will work with the built-in (default) provider
    // implementation, which just inserts records one-at-a-time, so really do implement the
    // BulkInsert ContentProvider function.

    // test OK
    public void testBulkInsert() {
        // first, let's create a location value
        ContentValues testValues = TestUtilities.createItalyLocationValues();
        Uri locationUri = mContext.getContentResolver().insert(RecipeContract.LocationEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                RecipeContract.LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testBulkInsert. Error validating LocationEntry.",
                cursor, testValues);

        // Now we can bulkInsert some recipe.  In fact, we only implement BulkInsert for recipe
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertRecipeValues(locationRowId);

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver recipeObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(RecipeContract.RecipeEntry.CONTENT_URI, true, recipeObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(RecipeContract.RecipeEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        recipeObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(recipeObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        cursor = mContext.getContentResolver().query(
                RecipeContract.RecipeEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
//                RecipeContract.RecipeEntry.COLUMN_DATE + " ASC"  // sort order == by DATE ASCENDING
                RecipeContract.RecipeEntry.COLUMN_TITLE + " ASC"  // sort order == by TITLE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating WeatherEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}

