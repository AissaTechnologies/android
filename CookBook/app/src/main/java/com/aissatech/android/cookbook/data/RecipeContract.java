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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;


/**
 * Defines table and column names for the recipes database.
 */
public class RecipeContract {
    // The "Content authority" is a name for the entire content provider, similar to the relationship
    // between a domain name and its website.  A convenient string to use for the content authority
    // is the package name for the app, which is guaranteed to be unique on the device.
    public static final String CONTENT_AUTHORITY = "com.aissatech.android.cookbook";//.app

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // Possible paths (appended to base content URI for possible URI's) - tables name
    public static final String PATH_RECIPE = "recipe";
    public static final String PATH_LOCATION = "location";
    public static final String PATH_FOOD = "food";

    /* Inner class that defines the table contents of the location table */
    public static final class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "location";

        // The location setting string is what will be sent to Food2Fork as the location query.
        public static final String COLUMN_LOCATION_SETTING = "location_setting";
        // Human readable location string, provided by the API.  Because for styling,
        // "Italy" is more recognizable than 00182.
        public static final String COLUMN_COUNTRY_NAME = "country_name";
        // In order to uniquely pinpoint the location on the map when we launch the
        // map intent, we store the latitude and longitude
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";

        // UriMatcher
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
     /* Inner class that defines the table contents of the Food table */
//    public static final class FoodEntry implements BaseColumns {
//         public static final String TABLE_NAME = "food";
//         // The food setting string is what will be sent to Food2Fork as the food query.
//         public static final String COLUMN_FOOD_SETTING = "food_setting";
//
//         // UriMatcher
//         public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();
//         // cursor return list of items
//         public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
//         // cursor return a single item
//         public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
//
//         public static Uri buildLocationUri(long id) {
//             return ContentUris.withAppendedId(CONTENT_URI, id);
//         }
//
//    }

    /* Inner class that defines the table contents of the Recipe table */
    public static final class RecipeEntry implements BaseColumns {

        public static final String TABLE_NAME = "recipe";

        // Column with the foreign key into the location table.
        public static final String COLUMN_LOC_KEY = "location_id";
        // Column with the foreign key into the food table.
//        public static final String COLUMN_FOOD_KEY = "food_id";

         // Recipe id as returned by API, to identify the icon to be used
        public static final String COLUMN_PUBLISHER = "publisher";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SOURCE_URL = "source_url";
        public static final String COLUMN_RECIPE_ID = "recipe_id";
        public static final String COLUMN_IMAGE_URL = "image_url";
        public static final String COLUMN_SOCIAL_RANK = "social_rank";
        public static final String COLUMN_PUBLISHER_URL = "publisher_url";

        // UriMatcher
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPE).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECIPE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECIPE;


        public static Uri buildRecipeUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildRecipeLocation(String locationSetting) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

//        public static Uri buildRecipeFood(String foodSetting) {
//            return CONTENT_URI.buildUpon().appendPath(foodSetting).build();
//        }

        public static String getLocationSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getRecipeFromUri(Uri uri){
            return uri.getPathSegments().get(0);
        }

//        public static String getFoodSettingFromUri(Uri uri) {
//            return uri.getPathSegments().get(1);
//        }
    }
}
