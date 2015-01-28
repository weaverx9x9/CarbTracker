package com.isad.carbtracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

public class AbstractDbAdapter {
	// For debugging.
    private static final String TAG = "FoodsDatabase";
    private static final boolean D = true;
    
    //The columns we'll include in the FTSfoods table
    public static final String KEY_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String KEY_CARBS = SearchManager.SUGGEST_COLUMN_TEXT_2;
    public static final String KEY_KCAL = "Energ_Kcal";
    public static final String KEY_FIBER = "Fiber_TD";
    public static final String KEY_SUGAR = "Sugar_Tot";
    public static final String KEY_GMWT1 = "GmWt_1";
    public static final String KEY_GMWT1_DESC = "GmWt_Desc1";
    public static final String KEY_GMWT2 = "GmWt_2";
    public static final String KEY_GMWT2_DESC = "GmWt_Desc2";
    
    // Columns for CarbHistory Table.
    public static final String KEY_TIMESTAMP = "TimeStamp";
    public static final String KEY_DATE = "Date";
    public static final String KEY_DESC = "Name";
    public static final String KEY_HCARBS = "Carbs";	
    public static final String KEY_DAILYTOTAL = "DailyTotal";
    public static final String KEY_GRAMS = "Grams";
    public static final String KEY_FOODSID = "FoodsIndex";
    
    // Columns for WeightHistory Table.
    public static final String KEY_WEIGHT = "Weight";
    public static final String KEY_MAXDATE = "MaxDate";		
    
	// For column headers.												
	private final static int NAME = 0;
	private final static int CARBS = 1;
	private final static int FIBER = 2;
	private final static int SUGAR = 3;
	private final static int GMWT1 = 4;
	private final static int GMWT1_DESC = 5;	
	private final static int GMWT2 = 6;
	private final static int GMWT2_DESC = 7;
	
	private static final String DATABASE_NAME = "foods";
    protected static final String FTS_VIRTUAL_TABLE = "FTSfoods";
    protected static final String CARB_HISTORY_TABLE = "CarbHistory";
    protected static final String WEIGHT_HISTORY_TABLE = "WeightHistory";
    
    private static final int DATABASE_VERSION = 2;

    protected FoodsOpenHelper mDatabaseOpenHelper;
    protected SQLiteDatabase mDatabase;
    protected final Context mContext;
    
    /* Note that FTS3 does not support column constraints and thus, you cannot
     * declare a primary key. However, "rowid" is automatically used as a unique
     * identifier, so when making requests, we will use "_id" as an alias for "rowid"
     */
    private static final String FTS_TABLE_CREATE =
            "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
            " USING fts3 ("  +
            KEY_NAME + ", "  +
            KEY_CARBS + ", " +
            KEY_FIBER + ", " +
            KEY_SUGAR + ", " +
            KEY_GMWT1 + ", " + 
            KEY_GMWT1_DESC + ", " +
            KEY_GMWT2 + ", " +
            KEY_GMWT2_DESC + ");";

    // Create Table String for CarbHistory Table.
    private static final String CARBHISTORY_TABLE_CREATE = 
    		"CREATE TABLE " + CARB_HISTORY_TABLE + 
    		"(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
    		KEY_TIMESTAMP + " CHAR(6)," +
    		KEY_DATE + " CHAR(8) NOT NULL, " +
    		KEY_DESC + " VARCHAR(220), " +
    		KEY_HCARBS + " NUMERIC, " +
    		KEY_GRAMS + " INTEGER, " + 
    		KEY_FOODSID + " INTEGER);";

    // Create Table String for WeightHistory Table.
    private static final String WEIGHT_HISTORY_TABLE_CREATE = 
    		"CREATE TABLE " + WEIGHT_HISTORY_TABLE + 
    		"(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
    		KEY_DATE + " CHAR(8) NOT NULL UNIQUE, " +
    		KEY_WEIGHT + " NUMERIC);";    
    
    /**
     * Constructor
     * @param context The Context within which to work, used to create the DB
     */
    public AbstractDbAdapter(Context context) {
        mDatabaseOpenHelper = new FoodsOpenHelper(context);
        this.mContext = context;
        //mTools = new Tools();
    }

    /**
     * This creates/opens the database.
     */
    protected static class FoodsOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        private SQLiteDatabase mmDatabase;
        
        FoodsOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mmDatabase = db;
            mmDatabase.execSQL(FTS_TABLE_CREATE);
            mmDatabase.execSQL(CARBHISTORY_TABLE_CREATE);
            mmDatabase.execSQL(WEIGHT_HISTORY_TABLE_CREATE);
            if(D) Log.i(TAG, "***************** Created Tables *****************");
            loadDictionary();
        }

        /**
         * Starts a thread to load the database table with foods
         */
        private void loadDictionary() {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        loadFoods();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        /**
         * This is the function that actually does the parsing of the 
         * database file.  In this implementation we are using a csv
         * file.  We are using open opencsv-2.3 to open and go through
         * the rows.  After getting a row, a call to addFood is called 
         * to actually insert the food in the database.
         * @throws IOException
         */
        private void loadFoods() throws IOException {
            if(D) Log.d(TAG, "Loading foods...");
            final Resources resources = mHelperContext.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.sr24);
            CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
            
            try {
                String[] line;
                while ((line = reader.readNext()) != null) {
                    long id = addFood(line[NAME].trim(), line[CARBS].trim(),
                    				  line[FIBER].trim(), line[SUGAR].trim(), line[GMWT1].trim(),
                    				  line[GMWT1_DESC].trim(), line[GMWT2].trim(), line[GMWT2_DESC].trim());
                    if (id < 0) {
                        Log.e(TAG, "unable to add food: " + line[0].trim());
                    }
                }
            } finally {
                reader.close();
            }
            if(D) Log.d(TAG, "DONE loading foods.");
        }

        /**
         * Add a food to the database.
         * @return rowId or -1 if failed
         */
        public long addFood(String shrt_desc, String carbohydrt, String fiber_td, 
        					String sugar_tot, String gm_wt1, String gm_wt1_desc, 
        					String gm_wt2, String gm_wt2_desc) {
        	ContentValues initialValues = new ContentValues();
        	// If there is no food description (name) we must not add the
        	// food.  This indicates an error.
        	if(shrt_desc.length() == 0) return -1;
            initialValues.put(KEY_NAME, shrt_desc);
            initialValues.put(KEY_CARBS, carbohydrt);
            initialValues.put(KEY_FIBER, fiber_td);
            initialValues.put(KEY_SUGAR, sugar_tot);
            initialValues.put(KEY_GMWT1, gm_wt1);
            initialValues.put(KEY_GMWT1_DESC, gm_wt1_desc);
            initialValues.put(KEY_GMWT2, gm_wt2);
            initialValues.put(KEY_GMWT2_DESC, gm_wt2_desc);
            return mmDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + CARB_HISTORY_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + WEIGHT_HISTORY_TABLE);
            onCreate(db);
        }
    }
    
    public AbstractDbAdapter open() throws SQLException {
    	mDatabaseOpenHelper = new FoodsOpenHelper(mContext);
    	mDatabase = mDatabaseOpenHelper.getWritableDatabase();
    	return this;
    }
    
    public void close() {
    	mDatabaseOpenHelper.close();
    }
}
