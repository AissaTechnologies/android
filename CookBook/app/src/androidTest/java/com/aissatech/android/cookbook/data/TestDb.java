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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(RecipeDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }
    // test db creation - OK
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(RecipeContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(RecipeContract.RecipeEntry.TABLE_NAME);
        //tableNameHashSet.add(RecipeContract.FoodEntry.TABLE_NAME);

        mContext.deleteDatabase(RecipeDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new RecipeDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and Recipe entry tables
        assertTrue("Error: Your database was created without both the LocationEntry and RecipeEntry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + RecipeContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(RecipeContract.LocationEntry._ID);
        locationColumnHashSet.add(RecipeContract.LocationEntry.COLUMN_COUNTRY_NAME);
        locationColumnHashSet.add(RecipeContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(RecipeContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(RecipeContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    /*
        insert and query the location database.
        I also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
    public void testLocationTable() {

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        RecipeDbHelper dbHelper = new RecipeDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what we want to insert
        // (I used the creatItalyLocationValues)
        ContentValues testValues = TestUtilities.createItalyLocationValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long locationRowId;
        locationRowId = db.insert(RecipeContract.LocationEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                            RecipeContract.LocationEntry.TABLE_NAME,  // Table to Query
                            null, // all columns
                            null, // Columns for the "where" clause
                            null, // Values for the "where" clause
                            null, // columns to group by
                            null, // columns to filter by row groups
                            null // sort order
                       );

        // Move the cursor to a valid database row and check to see if we got any records back from the query
        assertTrue( "Error: No Records returned from location query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (I used the validateCurrentRecord function in TestUtilities to validate the query)
        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed", cursor, testValues);

        // Finally, close the cursor and database
        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from location query", cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();

    }

    /*
        test for insert and query the Recipe table
     */
    public void testRecipeTable() {
        // First insert the location, and then use the locationRowId to insert the Recipe

        // Instead of rewriting all of the code we've already written in testLocationTable
        // we can move this code to insertLocation and then call insertLocation from both
        // tests. Why move it? We need the code to return the ID of the inserted location
        // and our testLocationTable can only return void because it's a test.

        // First step: Get reference to writable database
        RecipeDbHelper dbHelper = new RecipeDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what you want to insert
        // (you can use the createRecipeValues TestUtilities function if you wish)
        ContentValues testValues = TestUtilities.createRecipeValues();

        // Insert ContentValues into database and get a row ID back
        long recipeRowId;
        recipeRowId = db.insert(RecipeContract.RecipeEntry.TABLE_NAME, null, testValues);
        // Verify we got a row back.
        assertTrue(recipeRowId != -1);
        // Query the database and receive a Cursor back
        Cursor cursor = db.query(
                RecipeContract.RecipeEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row
        assertTrue( "Error: No Records returned from location query", cursor.moveToFirst() );

        // Validate data in resulting Cursor with the original ContentValues
        // using the validateCurrentRecord function in TestUtilities to validate the query
        TestUtilities.validateCurrentRecord("Error: Recipe Query Validation Failed", cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from location query", cursor.moveToNext() );

        // Finally, close the cursor and database
        cursor.close();
        db.close();
    }

    public void testInsertReadDb() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        RecipeDbHelper dbHelper = new RecipeDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues testValues = TestUtilities.createItalyLocationValues();

        // insert
        long locationRowId;
        locationRowId = db.insert(RecipeContract.LocationEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.

        // A cursor is the primary interface to the query results.
        Cursor locationCursor = db.query(
                RecipeContract.LocationEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        TestUtilities.validateCursor("Error locationCursor validation", locationCursor, testValues);



        // Fantastic.  Now that we have a location, add some recipe!
        ContentValues recipeValues = TestUtilities.createRecipeValues(locationRowId);

        long recipeRowId = db.insert(RecipeContract.RecipeEntry.TABLE_NAME, null, recipeValues);
        assertTrue(recipeRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor recipeCursor = db.query(
                RecipeContract.RecipeEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        TestUtilities.validateCursor("Error recipeCursor validation", recipeCursor, recipeValues);

        dbHelper.close();
    }

    // test OK
//    public void testInsertReadDb2() {
//
//        // Test data we're going to insert into the DB to see if it works.
//        String testLocationSetting = "77500 ";//Cancun
//        String testCountryName = "Mexico";
//        double testLatitude = 19.0;
//        double testLongitude = 102.3667;
//
//        // If there's an error in those massive SQL table creation Strings,
//        // errors will be thrown here when you try to get a writable database.
//        RecipeDbHelper dbHelper = new RecipeDbHelper(mContext);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        // Create a new map of values, where column names are the keys
//        ContentValues values = new ContentValues();
//        values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
//        values.put(LocationEntry.COLUMN_COUNTRY_NAME, testCountryName);
//        values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
//        values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);
//
//        long locationRowId = db.insert(LocationEntry.TABLE_NAME,null,values);
//
//        // Verify we got a row back
//        assertTrue(locationRowId != -1);
//        Log.d(LOG_TAG, "New row id: " + locationRowId);
//
//        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
//        // the round trip.
//
//        // query the inserted data. Specify which columns you want.
//        String[] columns = {
//                LocationEntry._ID,
//                LocationEntry.COLUMN_LOCATION_SETTING,
//                LocationEntry.COLUMN_COUNTRY_NAME,
//                LocationEntry.COLUMN_COORD_LAT,
//                LocationEntry.COLUMN_COORD_LONG
//        };
//
//        // A cursor is your primary interface to the query results.
//        Cursor cursor = db.query(
//                LocationEntry.TABLE_NAME,  // Table to Query
//                columns,
//                null, // Columns for the "where" clause
//                null, // Values for the "where" clause
//                null, // columns to group by
//                null, // columns to filter by row groups
//                null // sort order
//        );
//
//        // If possible, move to the first row of the query results.
//        if (cursor.moveToFirst()) {
//            // Get the value in each column by finding the appropriate column index.
//            int locationIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
//            String location = cursor.getString(locationIndex);
//
//            int nameIndex = cursor.getColumnIndex((LocationEntry.COLUMN_COUNTRY_NAME));
//            String name = cursor.getString(nameIndex);
//
//            int latIndex = cursor.getColumnIndex((LocationEntry.COLUMN_COORD_LAT));
//            double latitude = cursor.getDouble(latIndex);
//
//            int longIndex = cursor.getColumnIndex((LocationEntry.COLUMN_COORD_LONG));
//            double longitude = cursor.getDouble(longIndex);
//
//            // Hooray, data was returned!  Assert that it's the right data, and that the database
//            // creation code is working as intended.
//            // Then take a break.  We both know that wasn't easy.
//            assertEquals(testCountryName, name);
//            assertEquals(testLocationSetting, location);
//            assertEquals(testLatitude, latitude);
//            assertEquals(testLongitude, longitude);
//
//            // Fantastic.  Now that we have a location, add some weather!
//        } else {
//            // That's weird, it works on MY machine...
//            fail("No values returned :(");
//        }
//
//        // Fantastic.  Now that we have a location, add some weather!
//        ContentValues recipeValues = new ContentValues();
//        recipeValues.put(RecipeEntry.COLUMN_LOC_KEY, locationRowId);
//        recipeValues.put(RecipeEntry.COLUMN_PUBLISHER, "All Recipes");
//        recipeValues.put(RecipeEntry.COLUMN_URL, "http://food2fork.com/view/23882");
//        recipeValues.put(RecipeEntry.COLUMN_TITLE, "New Mexico Green Chile Breakfast Burritos");
//        recipeValues.put(RecipeEntry.COLUMN_SOURCE_URL, "http://allrecipes.com/Recipe/New-Mexico-Green-Chile-Breakfast-Burritos/Detail.aspx");
//        recipeValues.put(RecipeEntry.COLUMN_RECIPE_ID, "23882");
//        recipeValues.put(RecipeEntry.COLUMN_IMAGE_URL, "http://static.food2fork.com/473633f10c.jpg");
//        recipeValues.put(RecipeEntry.COLUMN_SOCIAL_RANK, 73.8904259305868);
//        recipeValues.put(RecipeEntry.COLUMN_PUBLISHER_URL, "http://allrecipes.com");
//
//
//        long recipeRowId = db.insert(RecipeEntry.TABLE_NAME, null, recipeValues);
//        assertTrue(recipeRowId != -1);
//
//        // A cursor is your primary interface to the query results.
//        Cursor recipeCursor = db.query(
//                RecipeEntry.TABLE_NAME,  // Table to Query
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null, // columns to group by
//                null, // columns to filter by row groups
//                null  // sort order
//        );
//
//        if (!recipeCursor.moveToFirst()) {
//            fail("No weather data returned!");
//        }
//
//        assertEquals(recipeCursor.getInt(
//                recipeCursor.getColumnIndex(RecipeEntry.COLUMN_LOC_KEY)), locationRowId);
//        assertEquals(recipeCursor.getString(
//                recipeCursor.getColumnIndex(RecipeEntry.COLUMN_PUBLISHER)), "All Recipes");
//        assertEquals(recipeCursor.getString(
//                recipeCursor.getColumnIndex(RecipeEntry.COLUMN_URL)), "http://food2fork.com/view/23882");
//        assertEquals(recipeCursor.getString(
//                recipeCursor.getColumnIndex(RecipeEntry.COLUMN_TITLE)), "New Mexico Green Chile Breakfast Burritos");
//        assertEquals(recipeCursor.getString(
//                recipeCursor.getColumnIndex(RecipeEntry.COLUMN_SOURCE_URL)), "http://allrecipes.com/Recipe/New-Mexico-Green-Chile-Breakfast-Burritos/Detail.aspx");
//        assertEquals(recipeCursor.getString(
//                recipeCursor.getColumnIndex(RecipeEntry.COLUMN_RECIPE_ID)), "23882");
//        assertEquals(recipeCursor.getString(
//                recipeCursor.getColumnIndex(RecipeEntry.COLUMN_IMAGE_URL)), "http://static.food2fork.com/473633f10c.jpg");
//        assertEquals(recipeCursor.getDouble(
//                recipeCursor.getColumnIndex(RecipeEntry.COLUMN_SOCIAL_RANK)), 73.8904259305868);
//        assertEquals(recipeCursor.getString(
//                recipeCursor.getColumnIndex(RecipeEntry.COLUMN_PUBLISHER_URL)), "http://allrecipes.com");
//
//        recipeCursor.close();
//
//        dbHelper.close();
//    }

    public long insertLocation() {
        return -1L;
    }
}
