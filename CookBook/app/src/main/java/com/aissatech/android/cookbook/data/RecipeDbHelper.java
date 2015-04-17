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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.aissatech.android.cookbook.data.RecipeContract.LocationEntry;
import com.aissatech.android.cookbook.data.RecipeContract.RecipeEntry;

/**
 * Manages a local database for recipes data.
 */
public class RecipeDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "recipes.db";

    public RecipeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // location table
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY," +
                LocationEntry.COLUMN_LOCATION_SETTING + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_COUNTRY_NAME + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL " +
        " );";

        // recipe table
        final String SQL_CREATE_RECIPE_TABLE = "CREATE TABLE " + RecipeEntry.TABLE_NAME + " (" +
                RecipeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RecipeEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                RecipeEntry.COLUMN_PUBLISHER + " TEXT NOT NULL, " +
                RecipeEntry.COLUMN_URL + " TEXT NOT NULL, " +
                RecipeEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                RecipeEntry.COLUMN_SOURCE_URL + " TEXT NOT NULL, " +
                RecipeEntry.COLUMN_RECIPE_ID + " TEXT NOT NULL, " +
                RecipeEntry.COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
                RecipeEntry.COLUMN_SOCIAL_RANK + " TEXT NOT NULL, " +
                RecipeEntry.COLUMN_PUBLISHER_URL + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + RecipeEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + ") );";


        // food table
//        final String SQL_CREATE_FOOD_TABLE = "CREATE TABLE " + FoodEntry.TABLE_NAME + " (" +
//                FoodEntry._ID + " INTEGER PRIMARY KEY," +
//                FoodEntry.COLUMN_FOOD_SETTING + " TEXT NOT NULL, " +
//                " );";


        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_RECIPE_TABLE);
//        sqLiteDatabase.execSQL(SQL_CREATE_FOOD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RecipeEntry.TABLE_NAME);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FoodEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

