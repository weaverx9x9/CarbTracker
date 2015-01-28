package com.isad.carbtracker;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SearchActivity extends Activity {
	private static final String TAG = "SearchActivity";
	private static final boolean D = true;
    private TextView mTextView;
    private ListView mListView;
    private TextView mNoResults;
    private TextView mItemName;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        mTextView = (TextView) findViewById(R.id.text);
        mListView = (ListView) findViewById(R.id.list);
        mNoResults = (TextView) findViewById(R.id.noresults);
        
        Intent intent = getIntent();
        
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // handles a click on a search suggestion; launches activity to show word
            Intent foodIntent = new Intent(this, FoodActivity.class);
            foodIntent.setData(intent.getData());
            startActivity(foodIntent);
            finish();
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            showResults(query);
        } else if(Intent.ACTION_INSERT.equals(intent.getAction())) {
        	Intent addIntent = new Intent(this, AddNewFoodActivity.class);
        	addIntent.putExtra("query", intent.getData().getLastPathSegment());
        	startActivity(addIntent);
        	//finish();   TODO Should I call this here?
        }
    }

    /**
     * Searches the database and displays results for the given query.
     * @param query The search query
     */
    private void showResults(String query) {
        Cursor cursor = managedQuery(FoodsProvider.CONTENT_URI, null, null,
                                new String[] {query}, null);
        if (cursor == null) {
            mTextView.setText(getString(R.string.no_results, new Object[] {query}));
            MatrixCursor cursor1 = new MatrixCursor(new String[] {
            		BaseColumns._ID,
            		SearchManager.SUGGEST_COLUMN_TEXT_1,
            		SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
            		SearchManager.SUGGEST_COLUMN_INTENT_DATA
            });
            cursor1.addRow(new Object[] {
            		0, 
            		"Select to add \"" + query + "\" to the database",
            		Intent.ACTION_INSERT,
            		FoodsProvider.ADD_FOOD_MIME_TYPE + "/" + query
            });

            String[] from = new String[] { AbstractDbAdapter.KEY_NAME };
            int[] to = new int[] { R.id.food };
            SimpleCursorAdapter foods = new SimpleCursorAdapter(this,
                                          R.layout.result, cursor1, from, to);
            mListView.setAdapter(foods);
            final String finalQuery = query;
            mListView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                	Intent addFoodIntent = new Intent(getApplicationContext(), AddNewFoodActivity.class);
                	addFoodIntent.putExtra("query", finalQuery);
                	startActivity(addFoodIntent);
                }
            });
        } else {
            // Display the number of results
            int count = cursor.getCount();
            String countString = getResources().getQuantityString(R.plurals.search_results,
                                    count, new Object[] {count, query});
            mTextView.setText(countString);

            // Specify the columns we want to display in the result
            String[] from = new String[] { AbstractDbAdapter.KEY_NAME,
                                           AbstractDbAdapter.KEY_CARBS };

            // Specify the corresponding layout elements where we want the columns to go
            int[] to = new int[] { R.id.food,
                                   R.id.carbs };

            // Create a simple cursor adapter for the definitions and apply them to the ListView
            SimpleCursorAdapter foods = new SimpleCursorAdapter(this,
                                          R.layout.result, cursor, from, to);
            mListView.setAdapter(foods);
            
            // Define the on-click listener for the list items
            mListView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Build the Intent used to open WordActivity with a specific word Uri
                    Intent foodIntent = new Intent(getApplicationContext(), FoodActivity.class);
                    Uri data = Uri.withAppendedPath(FoodsProvider.CONTENT_URI,
                                                    String.valueOf(id));
                    foodIntent.setData(data);
                    startActivity(foodIntent);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                return true;
            default:
                return false;
        }
    }
}
