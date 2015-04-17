package com.aissatech.android.cookbook.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

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

public class RecipeProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RecipeDbHelper mOpenHelper;

    private static final int RECIPE = 100;
    private static final int RECIPE_WITH_LOCATION = 101;
//    private static final int RECIPE_WITH_FOOD = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private static final SQLiteQueryBuilder sRecipeByLocationSettingQueryBuilder;
//    private static final SQLiteQueryBuilder sRecipeByFoodSettingQueryBuilder;
    // select * from recipes table
    private static final SQLiteQueryBuilder sRecipesQueryBuilder;

    static{
        sRecipeByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
//        sRecipeByFoodSettingQueryBuilder = new SQLiteQueryBuilder();
        sRecipesQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //Recipe INNER JOIN location ON recipe.location_id = location._id
        sRecipeByLocationSettingQueryBuilder.setTables(
                RecipeContract.RecipeEntry.TABLE_NAME + " INNER JOIN " +
                        RecipeContract.LocationEntry.TABLE_NAME +
                        " ON " + RecipeContract.RecipeEntry.TABLE_NAME +
                        "." + RecipeContract.RecipeEntry.COLUMN_LOC_KEY +
                        " = " + RecipeContract.LocationEntry.TABLE_NAME +
                        "." + RecipeContract.LocationEntry._ID);

//        //This is an inner join which looks like
//        //Recipe INNER JOIN location ON recipe.food_id = food._id
//        sRecipeByFoodSettingQueryBuilder.setTables(
//                RecipeContract.RecipeEntry.TABLE_NAME + " INNER JOIN " +
//                        RecipeContract.LocationEntry.TABLE_NAME +
//                        " ON " + RecipeContract.RecipeEntry.TABLE_NAME +
//                        "." + RecipeContract.RecipeEntry.COLUMN_FOOD_KEY +
//                        " = " + RecipeContract.FoodEntry.TABLE_NAME +
//                        "." + RecipeContract.FoodEntry._ID);
    }

    //location.location_setting = ?
    private static final String sLocationSettingSelection =
            RecipeContract.LocationEntry.TABLE_NAME +
                    "." + RecipeContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";

//    //food.food_setting = ?
//    private static final String sFoodSettingSelection =
//            RecipeContract.FoodEntry.TABLE_NAME +
//                    "." + RecipeContract.FoodEntry.COLUMN_FOOD_SETTING + " = ? ";

    // select * from recipe where title = '';
    private static final String sRecipesSelection =
            RecipeContract.RecipeEntry.TABLE_NAME +
                    "." + RecipeContract.RecipeEntry.COLUMN_TITLE + " = ? ";
    @Override
    public boolean onCreate() {
        mOpenHelper = new RecipeDbHelper(getContext());
        return true;
    }

    private Cursor getRecipeByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        // get location setting string
        String locationSetting = RecipeContract.RecipeEntry.getLocationSettingFromUri(uri);

        String selection = sLocationSettingSelection;
        String[] selectionArgs = new String[]{locationSetting};

        return sRecipeByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getRecipeSelection(Uri uri, String[] projection, String sortOrder) {
        // get recipe uri string
        String recipes = RecipeContract.RecipeEntry.getRecipeFromUri(uri);

        String selection = sRecipesSelection;
        String[] selectionArgs = new String[]{recipes};// recipes = RecipeEntry.COLUMN_TITLE

        return sRecipesQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection, // select *
                selection, // from recipe
                selectionArgs, // where title = ?
                null,
                null,
                sortOrder // order by
        );
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "recipe/*/*"
//            case RECIPE_WITH_FOOD:
//            {
//                retCursor = getRecipeByFoodSetting(uri, projection, sortOrder);
//                break;
//            }
            // "recipe/*"
            case RECIPE_WITH_LOCATION: {
                retCursor = getRecipeByLocationSetting(uri, projection, sortOrder);
                break;
            }
//            // "recipe"
//            case RECIPE: {
//                //retCursor = null;
//                retCursor = getRecipeSelection(uri, projection, sortOrder);
//                break;
//            }
            // "weather"
            case RECIPE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RecipeContract.RecipeEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location/*"
            case LOCATION_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RecipeContract.LocationEntry.TABLE_NAME,
                        projection,
                        RecipeContract.LocationEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location"
            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RecipeContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
    *
    *  insert function
    *
    * */

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case RECIPE: {
//                normalizeDate(values);
                long _id = db.insert(RecipeContract.RecipeEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = RecipeContract.RecipeEntry.buildRecipeUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION: {
                long _id = db.insert(RecipeContract.LocationEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = RecipeContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    /*
     *
     *  update function
     *
     * */

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Student: This is a lot like the delete function.  We return the number of rows impacted
        // by the update.
        //return 0;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case RECIPE:
                rowsUpdated = db.update(RecipeContract.RecipeEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case LOCATION:
                rowsUpdated = db.update(RecipeContract.LocationEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    /*
     *
     *   delete function
     *
     * */

     @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database

        // Student: Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.

        // Student: A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.

        // Student: return the actual rows deleted
        //return 0;
         final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         final int match = sUriMatcher.match(uri);
         int rowsDeleted;
         switch (match) {
             case RECIPE:
                 rowsDeleted = db.delete(
                         RecipeContract.RecipeEntry.TABLE_NAME, selection, selectionArgs);
                 break;
             case LOCATION:
                 rowsDeleted = db.delete(
                         RecipeContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                 break;
             default:
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
         }
         // Because a null deletes all rows
         if (selection == null || rowsDeleted != 0) {
             getContext().getContentResolver().notifyChange(uri, null);
         }
         return rowsDeleted;
    }
//    private Cursor getRecipeByFoodSetting(Uri uri, String[] projection, String sortOrder) {
//        String foodSetting = RecipeContract.RecipeEntry.getFoodSettingFromUri(uri);
//
//        String[] selectionArgs;
//        String selection;
//
//        selection = sLocationSettingSelection;
//        selectionArgs = new String[]{locationSetting};
//
//        return sRecipeByFoodSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
//                projection,
//                selection,
//                selectionArgs,
//                null,
//                null,
//                sortOrder
//        );
//    }

    /*
        Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the RECIPE, RECIPE_WITH_LOCATION, RECIPE_WITH_FOOD and LOCATION
        integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RecipeContract.CONTENT_AUTHORITY;

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // RecipeContract to help define the types to the UriMatcher.
        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, RecipeContract.PATH_RECIPE, RECIPE);
        matcher.addURI(authority, RecipeContract.PATH_RECIPE + "/*",RECIPE_WITH_LOCATION);//* = string
        matcher.addURI(authority, RecipeContract.PATH_LOCATION, LOCATION);

        // 3) Return the new matcher!
//        return null;
        return matcher;
    }

    /*
        Students: We've coded this for you.  We just create a new WeatherDbHelper for later use
        here.
     */


    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.

     */
    // isn't necessary for our app
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Uncomment and fill out these two cases
//            case RECIPE_WITH_FOOD:
//            case RECIPE_WITH_LOCATION:
            case RECIPE:
                return RecipeContract.RecipeEntry.CONTENT_TYPE;
            case LOCATION:
                return RecipeContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    // insert
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case RECIPE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(RecipeContract.RecipeEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}