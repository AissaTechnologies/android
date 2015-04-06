package com.aissatech.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by aissa_000 on 07/03/2015.
 */

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    // make adapter global to retrieve new data from the server
    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            // moved the code into the updateWeather() method
            /*FetchWeatherTask weatherTask = new FetchWeatherTask();
            // weatherTask.execute("94043");
            // using user's location preference stored in SharedPreferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
            weatherTask.execute(location);*/

            // updating weather on the refresh menu item
            updateWeather();
            updateTemparatureUnit();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        // weatherTask.execute("94043");
        // using user's location preference stored in SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

        weatherTask.execute(location);
    }

    public void updateTemparatureUnit(){
        FetchWeatherTask temperatureTask = new FetchWeatherTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //String celsius = prefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_metric));
        //String fahrenheit = prefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_imperial));
        //temperatureTask.execute(celsius);
        // temperatureTask.execute(celsius, fahrenheit);
        //temperatureTask.execute(fahrenheit);
        String units = prefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_metric));
        temperatureTask.execute(units);
    }

    @Override
    public void onStart() {
        super.onStart();
        // we want to update data also on the start of the fragment
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Camelia
            /*List<String> daysList = new ArrayList<String>();
            daysList.add("Today - Sunny - 88 / 63");
            daysList.add("Tomorrow - Foggy - 70 / 46");
            daysList.add("Tues - Foggy - 85 / 52"");
            daysList.add("Weds - Cloudy - 72 / 63");
            daysList.add("Thurs - Rainy - 64 / 51");
            daysList.add("Fri - Foggy - 70 / 46");
            daysList.add("Sat - Sunny - 76 / 68");*/

        String[] forecastArray = {
                "Today - Sunny - 88 / 63",
                "Tomorrow - Foggy - 70 / 46",
                "Tues - Foggy - 85 / 52",
                "Weds - Cloudy - 72 / 63",
                "Thurs - Rainy - 64 / 51",
                "Fri - Foggy - 70 / 46",
                "Sat - Sunny - 76 / 68"
        };

        // fill list
        List<String> weekForecast = Arrays.asList(forecastArray);

        // array adapter
        /*mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),// The current context (this fragment's parent activity)
                R.layout.list_item_forecast, // The name of the layout ID
                R.id.list_item_forecast_textView,// The ID of the textview to populate.
                weekForecast);  // forecast data*/

        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),// The current context (this fragment's parent activity)
                R.layout.list_item_forecast, // The name of the layout ID
                R.id.list_item_forecast_textView,// The ID of the textview to populate.
                new ArrayList<String>());  // forecast data from the cloud

        // set adapter on the listView
        ListView lView = (ListView) rootView.findViewById(R.id.listview_forecast);
        lView.setAdapter(mForecastAdapter);
        // display the toast with the item info
        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // clicking on list item, it opens a toast with the weather informations
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = mForecastAdapter.getItem(position);
                // Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                // instead of toast, we will use an explicit intent to launch the DetailActivity
                // which text we will capture on DetailActivity class
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });


        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {

            // http request to the OpenWeatherMap API
            HttpURLConnection urlConn = null;
            BufferedReader reader = null;
            // forecastJsonStr will contain the raw JSON response as a string
            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 7;

            try {

                //2nd vaariant
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "metric";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                                    .appendQueryParameter(QUERY_PARAM, params[0])
                                    .appendQueryParameter(FORMAT_PARAM, format)
                                    .appendQueryParameter(UNITS_PARAM, units)
                                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                                    .build();
                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI = " + builtUri.toString());

                // 1st variant, open the connection
                // OWM's forecast API page: http://openweathermap.org/API#forecast
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");

                Log.d("ForecastFragment", "Url = " + url);
                // request to OpenWeatherMap
                urlConn = (HttpURLConnection)url.openConnection();
                urlConn.setRequestMethod("GET");
                // http status code
                int code = urlConn.getResponseCode();
                Log.i("PlaceholderFragment", "Camelia, Status code = " + code);
                urlConn.connect();

                // read the input stream
                InputStream input = urlConn.getInputStream();

                if(input == null) {
                    // do nothing
                    //Log.i("PlaceholderFragment", "Input is null");
                    //forecastJsonStr = null;
                    return null;
                }
                // reader
                reader = new BufferedReader(new InputStreamReader(input));

                String line;
                StringBuffer bufferStr = new StringBuffer();

                while( (line = reader.readLine()) != null){
                    // buffer for debugging
                    bufferStr.append(line + "\n");
                }

                // bufferStr is empty,  no point in parsing.
                if(bufferStr.length() == 0) {
                    //forecastJsonStr = null;
                    return null;
                }

                // JSON response
                forecastJsonStr = bufferStr.toString();

                Log.v(LOG_TAG, "Camelia, ForecastJSONStr ------------- = " + forecastJsonStr);

            } catch(IOException ex){
                // If the code didn't successfully get the weather data,
                // there's no point in attempting to parse it.
                //Log.e("ForecastFragment", "Camelia, Error IO", ex);

                Log.e(LOG_TAG, "Error", ex);
                return null;
            } finally {
                // close connection
                if(urlConn != null){
                    urlConn.disconnect();
                }
                if(reader != null){
                    // close reader
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("PlaceholderFragment", "Camelia, Error closing stream", e);
                    }
                }
            }
            // call json parser data from the server
            try{
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch(JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // if it is some error
            return null;
        }

        // parsing JSON from server
        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            // for getting sure it prints the right elements
            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Camelia, Forecast single resultStrs string: " + s);
                Log.v(LOG_TAG, "Camelia, Forecast resultStrs length: " + resultStrs.length);
            }
            return resultStrs;

        }

        @Override
        protected void onPostExecute(String[] result){// string array from forecast result
            if(result != null){
                mForecastAdapter.clear();
                for(String dayForecastStr : result){
                    mForecastAdapter.add(dayForecastStr);
                }
            }
        }
    }
}
