package com.aissatech.android.cookbook;

/**
 * Created by aissa_000 on 05/04/2015.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeFragment extends Fragment{
    // Adapters allows us to bind data to the user interface elements
    // array adapter
    private ArrayAdapter<String> mRecipeAdapter;

    public RecipeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add this line in order for this fragment to handle menu events.
        // we want callback from this item
        setHasOptionsMenu(true);
    }

    // inflate recipefragment into the menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.recipefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        // on refresh menu item, dynamic data from server API
        if (id == R.id.action_refresh) {

            updateRecipe();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    // onCreateView - static array data
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Create some dummy data for the ListView.  Here's a sample recipe list
        String[] arrayRecipes = {
                "Lasagna",
                "Tagliatelle ai funghi porcini",
                "Rigatoni alla Carbonara",
                "Risotto al radicchio rosso Trevigiano",
                "Bucatini all'amatriciana",
                "Penne all'arabbiata",
                "Risotto allo zafferano"
        };

        List<String> recipesList = Arrays.asList(arrayRecipes);
        /*mRecipeAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_recipe,
                R.id.list_item_recipe_textView,
                arrayRecipes
        );*/
        // mRecipeAdapter fill with json API recipe result
        mRecipeAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_recipe,
                R.id.list_item_recipe_textView,
                new ArrayList<String>()
        );

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listView_recipe);
        listView.setAdapter(mRecipeAdapter);

        // toast on click of the item list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String recipe = mRecipeAdapter.getItem(position);
                //Toast.makeText(getActivity(), recipe, Toast.LENGTH_SHORT);
                // instead of toast, we create an intent to launch the DetailActivity
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                                    .putExtra(Intent.EXTRA_TEXT, recipe);
                startActivity(intent);
            }
        });

        return rootView;
    }

    // update recipe data
    private void updateRecipe() {
        //FetchDietTask dietTask = new FetchDietTask();
        // using the external FetchDietTask java file
        FetchDietTask dietTask = new FetchDietTask(getActivity(), mRecipeAdapter);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String recipe = prefs.getString(getString(R.string.pref_food_key), getString(R.string.pref_food_default));
//        String recipe1 = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        dietTask.execute(recipe);
//        dietTask.execute(recipe1);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateRecipe();
    }
    // moved on the external FetchDietTask java file
    /*
    public class FetchDietTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchDietTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String recipeJsonStr = null;
            String key = "660fba1454becb3e8f3a1c30327dec1f";//key dev
            int count = 30;

            try {
                // Construct the URL for the Food2Fork query
                //URL url = new URL("http://food2fork.com/api/search?key=660fba1454becb3e8f3a1c30327dec1f&q=carbonara");
                //URL url = new URL("http://food2fork.com/api/search?key=660fba1454becb3e8f3a1c30327dec1f&q=Italy");

                // build URIs with params, key and query as params
                final String RECIPE_BASE_URL = "http://food2fork.com/api/search?";
                final String KEY_PARAM = "key";//keydev
                final String QUERY_PARAM = "q";//query=food || location

                Uri builtUri = Uri.parse(RECIPE_BASE_URL).buildUpon()
                                  .appendQueryParameter(KEY_PARAM, key)
                                  .appendQueryParameter(QUERY_PARAM, params[0])
                                  .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to Food2Fork, and open the connection
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
                recipeJsonStr = buffer.toString();
                Log.v("RecipeFragment", "Camelia, recipeJsonStr = " + recipeJsonStr);

            } catch (IOException e) {
                Log.e("RecipeFragment", "Error ", e);
                // If the code didn't successfully get the recipe data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("RecipeFragment", "Error closing stream", e);
                    }
                }
            }

            // call json parser data from the server
            try{
                return getDietDataFromJson(recipeJsonStr, count);
            } catch(JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }
 */
        // parsing JSON from server

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
  /*
        private String[] getDietDataFromJson(String recipeJsonStr, int count)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String F2F_LIST = "recipes";
            final String F2F_PUBLISHER = "publisher";
            final String F2F_URL = "f2f_url";
            final String F2F_TITLE = "title";
            final String F2F_SOURCE_URL = "source_url";
            final String F2F_RECIPE_ID = "recipe_id";
            final String F2F_IMAGE_URL = "image_url";
            final String F2F_SOCIAL_RANK = "social_rank";
            final String F2F_PUBLISHER_URL = "publisher_url";
            //final String F2F_INGREDIENTS = "ingredients";


            JSONObject recipeJson = new JSONObject(recipeJsonStr);
            JSONArray recipeArray = recipeJson.getJSONArray(F2F_LIST);

            String[] resultStrs = new String[count];
            for(int i = 0; i < recipeArray.length(); i++) {
                String publisher;
                String url;
                String title;
                String sourceUrl;
                String recipeId;
                String imageUrl;
                String socialRank;
                String publisherUrl;
                // recipeObject is an element of the recipeArray
                JSONObject recipeObject = recipeArray.getJSONObject(i);

                // publisher is the first element of the recipeObject
                publisher = recipeObject.getString(F2F_PUBLISHER);
                url = recipeObject.getString(F2F_URL);
                title = recipeObject.getString(F2F_TITLE);
                sourceUrl = recipeObject.getString(F2F_SOURCE_URL);
                recipeId = recipeObject.getString(F2F_RECIPE_ID);
                imageUrl = recipeObject.getString(F2F_IMAGE_URL);
                socialRank = recipeObject.getString(F2F_SOCIAL_RANK);
                publisherUrl = recipeObject.getString(F2F_PUBLISHER_URL);

                // result string array
                resultStrs[i] = recipeId + " - " + title + " - " + publisher;
//                resultStrs[i] = publisher + " - " +
//                                url + " - " +
//                                title + " - " +
//                                sourceUrl + " - " +
//                                recipeId + " - " +
//                                imageUrl + " - " +
//                                socialRank + " - " +
//                                publisherUrl;
                Log.v("RecipeFragment", "Camelia,  array, element" + i + " = " + resultStrs[i]);
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Recipe entry: " + s);
            }
            return resultStrs;
        }

        // string array - recipe result return from doInBackground
        @Override
        protected void onPostExecute(String[] result){
            if(result != null){
                mRecipeAdapter.clear();
                for(String recipeStr : result){
                    mRecipeAdapter.add(recipeStr);
                }
            }
        }
    }
    */
}
