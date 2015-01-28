package com.isad.carbtracker;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;

public class FoodsDbAdapter extends AbstractDbAdapter {
	// For Debugging.
	//private static final String TAG = "FoodsDbAdapter";
	//private static final boolean D = true;

    private static final HashMap<String,String> mFoodsColumnMap = buildFoodsColumnMap();	
	
	public FoodsDbAdapter(Context context) {
		super(context);
	}
	
    /**
     * Returns a Cursor positioned at the food specified by rowId
     * This builds a query that looks like:
     * 
     *      SELECT <columns> FROM <table> WHERE rowid = <rowId>
     *  
     * @param rowId id of food to retrieve
     * @param columns The columns to include, if null then all are included
     * @return Cursor positioned to matching food, or null if not found.
     */
    public Cursor getFood(String rowId, String[] columns) {
        String selection = "rowid = ?";
        String[] selectionArgs = new String[] {rowId};
        return query(selection, selectionArgs, columns);
    }	
    
    /**
     * Returns a Cursor over all foods that match the given query
     * This builds a query that looks like:
     *
     *     SELECT <columns> FROM <table> WHERE <KEY_NAME> MATCH 'query*'
     *     
     * which is an FTS3 search for the query text (plus a wildcard) inside 
     * the name column.
     * @param query The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all foods that match, or null if none found.
     */
    public Cursor getFoodMatches(String query, String[] columns) {
        String selection = KEY_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, columns);
    }  
    
	/**
     * Builds a map for all columns that may be requested, which will be given to the 
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include 
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildFoodsColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(KEY_NAME, KEY_NAME);
        map.put(KEY_CARBS, KEY_CARBS);
        map.put(KEY_FIBER, KEY_FIBER);
        map.put(KEY_SUGAR, KEY_SUGAR);
        map.put(KEY_GMWT1, KEY_GMWT1);
        map.put(KEY_GMWT1_DESC, KEY_GMWT1_DESC);
        map.put(KEY_GMWT2, KEY_GMWT2);
        map.put(KEY_GMWT2_DESC, KEY_GMWT2_DESC);
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }
    
    /**
     * Performs a database query.
     * @param selection The selection clause
     * @param selectionArgs Selection arguments for "?" components in the selection
     * @param columns The columns to return
     * @return A Cursor over all rows matching the query
     */
    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);
        builder.setProjectionMap(mFoodsColumnMap);

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    } 
    
    /**
     * @param name    The name of the food.
     * @param weight  The weight of the serving size.
     * @param carbs   The carbs per 100 grams.
     * @param fiber   The fiber per 100 grams.
     * @param sugar   The grams per 100 grams.
     * @param desc    Description of the serving, i.e. 1 cup, 1 ounce.
     * @return        rowId of newly inserted item, -1 for error.
     */
    public long insert(String name, double weight, double carbs, double fiber, 
    				   double sugar, String desc) {
    	SQLiteDatabase db  = mDatabaseOpenHelper.getWritableDatabase();
    	ContentValues cv = new ContentValues();
    	cv.put(KEY_NAME, name);
    	cv.put(KEY_CARBS, carbs); // per 100
    	cv.put(KEY_FIBER, fiber); // per 100
    	cv.put(KEY_SUGAR, sugar); // per 100
    	cv.put(KEY_GMWT1, weight);
    	cv.put(KEY_GMWT1_DESC, desc);
    	cv.put(KEY_GMWT2, "");
    	cv.put(KEY_GMWT2_DESC, "");
    	return db.insert(FTS_VIRTUAL_TABLE, null, cv);
    }
}
