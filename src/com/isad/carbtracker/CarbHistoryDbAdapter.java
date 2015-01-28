package com.isad.carbtracker;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

public class CarbHistoryDbAdapter extends AbstractDbAdapter {

	// For debugging.
	private static final String TAG = "CarbHistoryDbAdapter";
	private static final boolean D = true;
	
    private static final HashMap<String,String> mCarbHistoryColumnMap = buildCarbHistoryColumnMap();
    
    public CarbHistoryDbAdapter(Context context) {
    	super(context);
    }
    
    /**
     * Builds a map for all columns that may be requested in the CarbHistory 
     * table.  
     * 
     * @return hash map containing the mapping between aliases and actual column names.
     */
    private static HashMap<String,String> buildCarbHistoryColumnMap() {
    	HashMap<String,String> map = new HashMap<String,String>();
    	map.put(KEY_TIMESTAMP, KEY_TIMESTAMP);
    	map.put(KEY_DATE, KEY_DATE);
    	map.put(KEY_DESC, KEY_DESC);
    	map.put(KEY_HCARBS, KEY_HCARBS);
    	map.put(KEY_GRAMS, KEY_GRAMS);
    	map.put(KEY_FOODSID, KEY_FOODSID);
    	map.put(BaseColumns._ID, "rowid AS " +
    			BaseColumns._ID);
    	return map;
    }  
    
    /**
     * This function is used by the CarbHistoryActivity to display a 
     * graph of total carbohydrates the user ate per day.
     * 
     * @return Cursor over table with total carbohydrates for previous days.
     */
    public Cursor getCarbHistory() {
    	String[] columns = new String[] {KEY_DATE, 
    									 "SUM(" + KEY_HCARBS + ") AS " + 
    									 KEY_DAILYTOTAL};
    	return query(null, null, columns, KEY_DATE, true);
    } 
    
    /**
     * This returns a cursor over all the foods that were eaten on a
     * given date.
     * 
     * @param date     The date to query for.
     * @param columns  The date column.
     * @return         A cursor over all foods for the given date.
     */
    public Cursor getDailyFoods(String date, String[] columns) {
    	String selection = KEY_DATE + " = ?";
    	String[] selectionArgs = new String[] {date};
    	return query(selection, selectionArgs, columns, null, false);
    }   
    
    /**
     * Get a particular entry from the carbohydrate history table.
     * 
     * @param rowId    The rowId of the entry to retrieve.
     * @param columns  The columns to return, if null then all are returned.
     * @return         Cursor positioned at matching entry, or null if not found.
     */
    public Cursor getCarbHistoryEntry(String rowId, String columns[]) {
    	String selection = "rowid = ?";
    	String[] selectionArgs = new String[] {rowId};
    	return query(selection, selectionArgs, columns, null, false);
    }
    
    
    /**
     * Get the total number of carbohydrates consumed on a given date.
     * 
     * @param  date The day to get the carbohydrates for.
     * @return return an integer representing the total number of carbohydrates
     * 		   consumed on that day.  Return -1 for error.
     */
    public Cursor getDailyCarbTotal(String date) {
    	String selection = "Date = ?";
    	String[] columns = new String[] {"SUM(" + KEY_HCARBS + ") AS " + KEY_DAILYTOTAL};
    	String[] selectionArgs = new String[] {date};
    	return query(selection, selectionArgs, columns, null, false);
    }   
    
    /**
     * Delete an item from the carbohydrate history table.
     * @param item  The rowId of the item to delete.
     * @return      The number of rows affected.
     */
    public int delete(long item) {
    	return mDatabase.delete(CARB_HISTORY_TABLE, "_id = " + item, null);
    }  
    
    /**
     * This is for querying the CarbHistory table for previously eaten foods.
     * 
     * @param selection 		The selection clause.
     * @param selectionArgs 	Selection arguments for "?" components in the selection.
     * @param columns			The columns to return.
     * @param groupBy			For SQL GROUP BY argument.
     * @param distinct     		Boolean for whether or not the selection should be distinct.
     * @return					A cursor over all rows matching the query.
     */
    private Cursor query(String selection, String[] selectionArgs, String[] columns, 
    		String groupBy, boolean distinct) {
    	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
    	builder.setTables(CARB_HISTORY_TABLE);
    	builder.setProjectionMap(mCarbHistoryColumnMap);
    	if(distinct) builder.setDistinct(true);
    	Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
    								   columns, selection, selectionArgs, groupBy, null, null);
    	if(cursor == null) {
    		return null;
    	} else if(!cursor.moveToFirst()) {
    		cursor.close();
    		return null;
    	}
    	return cursor;
    }
    
    /**
     * Inserts an item in the CarbHistory table.
     * 
     * @param name		The name of the food.
     * @param carbs		The amount of carbohydrates eaten.
     * @return          The rowId of the newly inserted item, or -1 for error.
     */
	public long insert(String name, Double carbs, int grams, long index) {
		if(name != null) {
			ContentValues cv = new ContentValues();
			String time = Tools.getTime();
			String date = Tools.getDate();
			cv.put(KEY_TIMESTAMP, time);
			cv.put(KEY_DATE, date);
			cv.put(KEY_DESC, name);
			cv.put(KEY_HCARBS, carbs);
			cv.put(KEY_GRAMS, grams);
			cv.put(KEY_FOODSID, index);
			SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
			return db.insert(CARB_HISTORY_TABLE, null, cv);
		} else return -1;
    }
}
