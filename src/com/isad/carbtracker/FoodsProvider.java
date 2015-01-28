package com.isad.carbtracker;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class FoodsProvider extends ContentProvider {
	// For debugging.
	//private static final String TAG = "FoodsProvider";
	//private static final boolean D = true;
	
    public static String AUTHORITY = "com.isad.carbtracker.FoodsProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/foods");

    // MIME types used for searching foods or looking up a single food.
    public static final String NAME_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.isad.carbtracker";
    public static final String CARBS_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.isad.carbtracker";
    public static final String KCAL_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.isad.carbtracker";
    public static final String FIBER_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.isad.carbtracker";
    public static final String SUGAR_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.isad.carbtracker";
    public static final String GMWT1_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.isad.carbtracker";
    public static final String GMWT1_DESC_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.isad.carbtracker";
    public static final String GMWT2_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.isad.carbtracker";
    public static final String GMWT2_DESC_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.isad.carbtracker";
    public static final String ADD_FOOD_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.isad.carbtracker";
    private FoodsDbAdapter mFoodsDatabase;

    // UriMatcher stuff
    private static final int SEARCH_FOODS = 0;
    private static final int GET_FOOD = 1;
    private static final int SEARCH_SUGGEST = 2;
    private static final int REFRESH_SHORTCUT = 3;
    private static final int ADD_FOOD = 4;

    private static final UriMatcher sURIMatcher = buildUriMatcher();

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        // to get foods...
        matcher.addURI(AUTHORITY, "foods", SEARCH_FOODS);
        matcher.addURI(AUTHORITY, "foods/#", GET_FOOD);
        // to get suggestions...
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", ADD_FOOD);
        return matcher;
    }

    @Override
    public boolean onCreate() {
    	mFoodsDatabase = new FoodsDbAdapter(getContext());
        return true;
    }

    /**
     * Handles all the foods searches and suggestion queries from the Search Manager.
     * When requesting a specific food, the URI alone is required.
     * When searching all of the foods for matches, the selectionArgs argument must carry
     * the search query as the first element.
     * All other arguments are ignored.
     * 
     * @param uri 			Type of the query.
     * @param projection 	Unused in this implementation.
     * @param selection 	Also unused in this implementation.
     * @param selectionArgs Contains the search query.
     * @param sortOrder 	For sorting the suggestions, unused in this implementation. 
     * @return 				A cursor over matching item/items.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly.
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:  // This is for when the user is still typing, before pressing search.
                if (selectionArgs == null) {
                  throw new IllegalArgumentException(
                      "selectionArgs must be provided for the Uri: " + uri);
                }
                return getSuggestions(selectionArgs[0]);
            case SEARCH_FOODS:   // This is for if the search button is actually pressed.
                if (selectionArgs == null) {
                  throw new IllegalArgumentException(
                      "selectionArgs must be provided for the Uri: " + uri);
                }
                return search(selectionArgs[0]);
            case GET_FOOD:
                return getFood(uri);
            case ADD_FOOD:
            	
            case REFRESH_SHORTCUT:
                return refreshShortcut(uri);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    /**
     * This function actually returns the list of suggestions to the user.
     * The columns to include in the search suggestions list are specified 
     * here as well.  
     * @param query The actual search query.
     * @return Foods that match the query, and the columns specified here.
     */
    private Cursor getSuggestions(String query) {
      query = query.toLowerCase();
      String[] columns = new String[] {
          BaseColumns._ID,
          AbstractDbAdapter.KEY_NAME,
          AbstractDbAdapter.KEY_CARBS,
          SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};
      MatrixCursor cursor = new MatrixCursor(new String[] {
    	BaseColumns._ID,
    	SearchManager.SUGGEST_COLUMN_TEXT_1,
    	SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
    	SearchManager.SUGGEST_COLUMN_INTENT_DATA
      });
      cursor.addRow(new Object[] {
    	0, 
    	"Select to add \"" + query + "\" to the database",
    	Intent.ACTION_INSERT,
    	ADD_FOOD_MIME_TYPE + "/" + query
      });
      
      Cursor foodsCursor = mFoodsDatabase.getFoodMatches(query, columns);
      if(foodsCursor == null) {
    	  return new MergeCursor(new Cursor[] {
    			  cursor,
    			  foodsCursor
    	  });
      } else {
    	  return foodsCursor;
      }
    }

    private Cursor search(String query) {
      query = query.toLowerCase();
      return mFoodsDatabase.getFoodMatches(query, null);
    }

    private Cursor getFood(Uri uri) {
      String rowId = uri.getLastPathSegment();
      return mFoodsDatabase.getFood(rowId, null);
    }

    private Cursor refreshShortcut(Uri uri) {
      String rowId = uri.getLastPathSegment();
      String[] columns = new String[] {
          BaseColumns._ID,
          AbstractDbAdapter.KEY_NAME,
          AbstractDbAdapter.KEY_CARBS,
          SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
          SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};
      return mFoodsDatabase.getFood(rowId, columns);
    }

    /**
     * This method is required in order to query the supported types.
     * It's also useful in our own query() method to determine the type of Uri received.
     */
    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case SEARCH_FOODS:
                return NAME_MIME_TYPE;
            case GET_FOOD:
                return CARBS_MIME_TYPE;
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case REFRESH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;
            case ADD_FOOD:
            	return ADD_FOOD_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    // Other required implementations...
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
