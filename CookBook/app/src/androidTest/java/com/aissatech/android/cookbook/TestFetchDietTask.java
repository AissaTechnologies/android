package com.aissatech.android.cookbook;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.aissatech.android.cookbook.data.RecipeContract;

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

public class TestFetchDietTask extends AndroidTestCase {
    static final String ADD_LOCATION_SETTING = "Cancun, ME";
    static final String ADD_LOCATION_COUNTRY = "Mexico";
    static final double ADD_LOCATION_LAT = 19.0000;
    static final double ADD_LOCATION_LON = 102.3667;

    /*
        Uncomment testAddLocation after you have written the AddLocation function.
        This test will only run on API level 11 and higher because of a requirement in the
        content provider.
     */
    @TargetApi(11)
    public void testAddLocation() {
        // start from a clean state
        getContext().getContentResolver().delete(RecipeContract.LocationEntry.CONTENT_URI,
                RecipeContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{ADD_LOCATION_SETTING});

        FetchDietTask fdt = new FetchDietTask(getContext(), null);
        long locationId = fdt.addLocation(ADD_LOCATION_SETTING, ADD_LOCATION_COUNTRY,
                ADD_LOCATION_LAT, ADD_LOCATION_LON);

        // does addLocation return a valid record ID?
        assertFalse("Error: addLocation returned an invalid ID on insert",
                locationId == -1);

        // test all this twice
        for ( int i = 0; i < 2; i++ ) {

            // does the ID point to our location?
            Cursor locationCursor = getContext().getContentResolver().query(
                    RecipeContract.LocationEntry.CONTENT_URI,
                    new String[]{
                            RecipeContract.LocationEntry._ID,
                            RecipeContract.LocationEntry.COLUMN_LOCATION_SETTING,
                            RecipeContract.LocationEntry.COLUMN_COUNTRY_NAME,
                            RecipeContract.LocationEntry.COLUMN_COORD_LAT,
                            RecipeContract.LocationEntry.COLUMN_COORD_LONG
                    },
                    RecipeContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                    new String[]{ADD_LOCATION_SETTING},
                    null);

            // these match the indices of the projection
            if (locationCursor.moveToFirst()) {
                assertEquals("Error: the queried value of locationId does not match the returned value" +
                        "from addLocation", locationCursor.getLong(0), locationId);
                assertEquals("Error: the queried value of location setting is incorrect",
                        locationCursor.getString(1), ADD_LOCATION_SETTING);
                assertEquals("Error: the queried value of location city is incorrect",
                        locationCursor.getString(2), ADD_LOCATION_COUNTRY);
                assertEquals("Error: the queried value of latitude is incorrect",
                        locationCursor.getDouble(3), ADD_LOCATION_LAT);
                assertEquals("Error: the queried value of longitude is incorrect",
                        locationCursor.getDouble(4), ADD_LOCATION_LON);
            } else {
                fail("Error: the id you used to query returned an empty cursor");
            }

            // there should be no more records
            assertFalse("Error: there should be only one record returned from a location query",
                    locationCursor.moveToNext());

            // add the location again
            long newLocationId = fdt.addLocation(ADD_LOCATION_SETTING, ADD_LOCATION_COUNTRY,
                    ADD_LOCATION_LAT, ADD_LOCATION_LON);

            assertEquals("Error: inserting a location again should return the same ID",
                    locationId, newLocationId);
        }
        // reset our state back to normal
        getContext().getContentResolver().delete(RecipeContract.LocationEntry.CONTENT_URI,
                RecipeContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{ADD_LOCATION_SETTING});

        // clean up the test so that other tests can use the content provider
        getContext().getContentResolver().
                acquireContentProviderClient(RecipeContract.LocationEntry.CONTENT_URI).
                getLocalContentProvider().shutdown();
    }
}
