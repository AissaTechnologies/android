package com.aissatech.android.cookbook;

/**
 * Created by aissa_000 on 06/04/2015.
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.aissatech.android.cookbook.data.RecipeContract;
import com.aissatech.android.cookbook.data.RecipeContract.RecipeEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

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

public class FetchDietTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchDietTask.class.getSimpleName();

    private ArrayAdapter<String> mRecipeAdapter;
    private final Context mContext;

    public FetchDietTask(Context context, ArrayAdapter<String> forecastAdapter) {
        mContext = context;
        mRecipeAdapter = forecastAdapter;
    }

    private boolean DEBUG = true;

    /**
     * Helper method to handle insertion of a new location in the recipe database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param countryName A human-readable country name, e.g "Italy"
     * @param lat the latitude of the country
     * @param lon the longitude of the country
     * @return the row ID of the added location.
     */

    long addLocation(String locationSetting, String countryName, double lat, double lon) {
        // add to database
//        if ( cVVector.size() > 0 ) {
//            ContentValues[] cvArray = new ContentValues[cVVector.size()];
//            cVVector.toArray(cvArray);
//            mContext.getContentResolver().bulkInsert(RecipeEntry.CONTENT_URI, cvArray);
//
//        }
        /* @return the row ID of the added location.
        */

        // First, check if the location with this country name exists in the db
        // If it exists, return the current ID
        // Otherwise, insert it using the content resolver and the base URI

        long locationId;

        Cursor cursor = mContext.getContentResolver().query(
                RecipeContract.LocationEntry.CONTENT_URI,
                new String[]{RecipeContract.LocationEntry._ID},
                RecipeContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null);

//        Cursor cur = mContext.getContentResolver().query(
//                recipeForLocationUri, null, null, null, sortOrder);
//
//                cVVector = new Vector<ContentValues>(cur.getCount());
//                if ( cur.moveToFirst() ) {
//                do {
//                       ContentValues cv = new ContentValues();
//                        DatabaseUtils.cursorRowToContentValues(cur, cv);
//                       cVVector.add(cv);
//                    } while (cur.moveToNext());
//                }
//        Log.d(LOG_TAG, "FetchWeatherTask Complete. " + cVVector.size() + " Inserted");

        // If it exists, return the current ID
        if (cursor.moveToFirst()) {
            int locationIdIndex = cursor.getColumnIndex(RecipeContract.LocationEntry._ID);
            return cursor.getLong(locationIdIndex);
        }
        // Otherwise, insert it using the content resolver and the base URI
        else {
            ContentValues locationValues = new ContentValues();
            locationValues.put(RecipeContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(RecipeContract.LocationEntry.COLUMN_COUNTRY_NAME, countryName);
            locationValues.put(RecipeContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(RecipeContract.LocationEntry.COLUMN_COORD_LONG, lon);

            Uri locationInsertUri = mContext.getContentResolver()
                    .insert(RecipeContract.LocationEntry.CONTENT_URI, locationValues);

            return ContentUris.parseId(locationInsertUri);
        }
    }
    /*
        This code will allow the FetchDietTask to continue to return the strings that
        the UX expects so that we can continue to test the application even once we begin using
        the database.
     */
    String[] convertContentValuesToUXFormat(Vector<ContentValues> cvv) {
        // return strings to keep UI functional for now
        String[] resultStrs = new String[cvv.size()];
        for ( int i = 0; i < cvv.size(); i++ ) {
            ContentValues recipeValues = cvv.elementAt(i);

            resultStrs[i] = recipeValues.getAsString(RecipeEntry.COLUMN_RECIPE_ID) +
//                    " - " + recipeValues.getAsString(RecipeEntry.COLUMN_TITLE) +
                    " - " + recipeValues.getAsString(RecipeEntry.COLUMN_TITLE) +
                    " - " + recipeValues.getAsString(RecipeEntry.COLUMN_PUBLISHER);
        }
        return resultStrs;
    }

    /**
     * Take the String representing the complete recipe in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getDietDataFromJson(String recipeJsonStr, String locationSetting)
            throws JSONException {

        // recipe information.  Each recipe's info is an element of the "recipes" array.
        final String F2F_LIST = "recipes";
        final String F2F_PUBLISHER = "publisher";
        final String F2F_URL = "f2f_url";
        final String F2F_TITLE = "title";
        final String F2F_SOURCE_URL = "source_url";
        final String F2F_RECIPE_ID = "recipe_id";
        final String F2F_IMAGE_URL = "image_url";
        final String F2F_SOCIAL_RANK = "social_rank";
        final String F2F_PUBLISHER_URL = "publisher_url";

        try {
            JSONObject recipeJson = new JSONObject(recipeJsonStr);
            JSONArray recipeArray = recipeJson.getJSONArray(F2F_LIST);

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(recipeArray.length());

            for(int i = 0; i < recipeArray.length(); i++) {
                // These are the values that will be collected.

//                int recipeId;
                String publisher;
                String url;
                String title;
                String sourceUrl;
                String recipeId;
                String imageUrl;
                String socialRank;
                String publisherUrl;

                // Get the JSON object
                JSONObject recipeObj = recipeArray.getJSONObject(i);

//                recipeId = recipeObj.getInt(F2F_RECIPE_ID);
                publisher = recipeObj.getString(F2F_PUBLISHER);
                url = recipeObj.getString(F2F_URL);
                title = recipeObj.getString(F2F_TITLE);
                sourceUrl = recipeObj.getString(F2F_SOURCE_URL);
                recipeId = recipeObj.getString(F2F_RECIPE_ID);
                imageUrl = recipeObj.getString(F2F_IMAGE_URL);
                socialRank = recipeObj.getString(F2F_SOCIAL_RANK);
                publisherUrl = recipeObj.getString(F2F_PUBLISHER_URL);
                ContentValues recipeValues = new ContentValues();

                recipeValues.put(RecipeEntry.COLUMN_TITLE, title);
                recipeValues.put(RecipeEntry.COLUMN_RECIPE_ID, recipeId);
                recipeValues.put(RecipeEntry.COLUMN_PUBLISHER, publisher);

                cVVector.add(recipeValues);
            }

            // add to database
            if ( cVVector.size() > 0 ) {
                // Student: call bulkInsert to add the recipeEntries to the database here
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                mContext.getContentResolver().bulkInsert(RecipeEntry.CONTENT_URI, cvArray);
            }

            // Sort order:  Ascending, by title.
            String sortOrder = RecipeEntry.COLUMN_TITLE + " ASC";
            Uri recipeForLocationUri = RecipeEntry.buildRecipeLocation(locationSetting);

            // display what what you stored in the bulkInsert
            Cursor cur = mContext.getContentResolver().query(recipeForLocationUri,
                    null, null, null, sortOrder);

            cVVector = new Vector<ContentValues>(cur.getCount());
            if ( cur.moveToFirst() ) {
                do {
                    ContentValues cv = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cur, cv);
                    cVVector.add(cv);
                } while (cur.moveToNext());
            }

            Log.d(LOG_TAG, "FetchDietTask Complete. " + cVVector.size() + " Inserted");

            String[] resultStrs = convertContentValuesToUXFormat(cVVector);
            return resultStrs;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected String[] doInBackground(String... params) {

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }
        String locationQuery = params[0];

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        String recipeJsonStr = null;
        String key = "660fba1454becb3e8f3a1c30327dec1f";
        int count = 30;

        try {
            // Construct the URL for the F2F query
            // Possible parameters are avaiable at F2F's recipe API page, at
            // http://food2fork.com/about/api
            final String RECIPE_BASE_URL = "http://food2fork.com/api/search?";
            final String KEY_PARAM = "key";
            final String QUERY_PARAM = "q";

            Uri builtUri = Uri.parse(RECIPE_BASE_URL).buildUpon()
                    .appendQueryParameter(KEY_PARAM, key)
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to F2F, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getDietDataFromJson(recipeJsonStr, locationQuery);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result != null && mRecipeAdapter != null) {
            mRecipeAdapter.clear();
            for(String recipeStr : result) {
                mRecipeAdapter.add(recipeStr);
            }
            // New data is back from the server.  Hooray!
        }
    }
}

