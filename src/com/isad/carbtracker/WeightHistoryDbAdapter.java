package com.isad.carbtracker;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

public class WeightHistoryDbAdapter extends AbstractDbAdapter {
	
	// For debugging.
	private static final String TAG = "WeightHistoryDbAdapter";
	private static final boolean D = true;
    
    // Database name.
    private static final String WEIGHT_HISTORY_TABLE = "WeightHistory";    
    private static final HashMap<String,String> mWeightHistoryColumnMap = buildWeightHistoryColumnMap();
    //private Tools mTools;
    
    public WeightHistoryDbAdapter(Context context) {
    	super(context);
    	//mTools = new Tools();
    }
    
    /**
     * Builds a map for all columns that may be requested in the WeightHistory
     * table.
     * @return hash map containing the mapping between aliases and actual column names.
     */
    private static HashMap<String,String> buildWeightHistoryColumnMap() {
    	HashMap<String,String> map = new HashMap<String,String>();
    	map.put(KEY_DATE, KEY_DATE);
    	map.put(KEY_WEIGHT, KEY_WEIGHT);
    	map.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
    	return map;
    }
    
    /**
     * This is for querying the WeightHistory table.
     * @param selection       	The selection clause.
     * @param selectionArgs   	Selection arguments for "?" components in the selection.
     * @param columns 			The columns to return.
     * @param groupBy			For SQL GROUP BY argument.
     * @param distinct			Boolean for whether or not the rows should be distinct.
     * @return					A cursor over all rows matching the query.
     */
    private Cursor query(String selection, String[] selectionArgs, String[] columns,
    		String groupBy, boolean distinct) {
    	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
    	builder.setTables(WEIGHT_HISTORY_TABLE);
    	builder.setProjectionMap(mWeightHistoryColumnMap);
    	if(distinct) builder.setDistinct(true);
    	Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), columns, 
    								  selection, selectionArgs, groupBy, null, null);
    	if(cursor == null) {
    		return null;
    	} else if(!cursor.moveToFirst()) {
    		cursor.close();
    		return null;
    	}
    	return cursor;
    }
    
	/**
	 * Insert a weight entry into the weight history table.
	 * @param date   The date of the weight value.
	 * @param weight The weight value itself.
	 */
	public long insert(String date, float weight) {
		if(date != null && date.length() > 0) {
			ContentValues cv = new ContentValues();
			cv.put(KEY_DATE, date);
			cv.put(KEY_WEIGHT, weight);
			SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
			return db.insert(WEIGHT_HISTORY_TABLE, null, cv);
		} else return -1;
	}
	
	/**
	 * Return a cursor to the most recent weight entry.
	 * @return 
	 */
	public Cursor getLastWeight() {
		String[] columns = new String[] {"MAX(" + KEY_DATE + ") AS " + KEY_MAXDATE,
										 KEY_WEIGHT};
		return query(null, null, columns, null, false);
	}
	
	/**
	 * This is used for changing a weight entry in the weight
	 * history table.  
	 * @param date   The date of the entry to update.
	 * @param weight The value to change it to.
	 * @return ???
	 */
	public int update(String date, float weight) {
		if(date != null && date.length() > 0) {
			ContentValues cv = new ContentValues();
			cv.put(KEY_DATE, date);
			cv.put(KEY_WEIGHT, weight);
			SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
			String whereClause = KEY_DATE + " = ?";
			String[] whereArgs = { date };
			return db.update(WEIGHT_HISTORY_TABLE, cv, whereClause, whereArgs);
		}
		return 0;
	}
	
	public Cursor entryExists(String date) {
		SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
		String selection = KEY_DATE + " = ?" ;
		String[] selectionArgs = { date };
		String[] where = { KEY_DATE };
		return db.query(WEIGHT_HISTORY_TABLE, where, selection, selectionArgs, null, null, null);
	}
	
	
	
    /**
     * This function is used by the WeightHistoryActivity to display
     * a graph of the users weight history.
     * @return  Cursor over table with dates and weight entries.
     */
    public Cursor getWeightHistory() {
    	if(D) Log.i(TAG, "getWeightHistory()");
    	String[] columns = new String[] {KEY_DATE,
    									 KEY_WEIGHT};
    	return query(null, null, columns, null, false);
    }	
}
