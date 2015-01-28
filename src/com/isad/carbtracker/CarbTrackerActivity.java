package com.isad.carbtracker;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This is the main application screen.  This is what is presented to
 * the user when they run the application.  It has buttons to launch
 * the Carbohydrate History Component, Weight history Component, and
 * the Search Component.  The Search Component may be launched by
 * either pressing the search soft key on the users phone, or, if the 
 * user's phone does not have a search soft key they can click on search
 * and then press the menu button and select search.  Search may be performed
 * from anywhere in the application by pressing the search soft key.
 * 
 * TODO Need to fix the pie chart so that it doesn't add new categories
 * each time the user returns to this activity.  As it is, it's recreating
 * each category each time the application is loaded, or returning to this
 * activity from another activity.  
 * 
 * @author Weaver Hastings
 *
 */
public class CarbTrackerActivity extends Activity {
	// For debugging.
	//private static final String TAG = "CarbTracker";
	private final String TAG = this.getClass().getSimpleName();
	private static final boolean D = true;
	
	// For Pie Chart.
	public static final String TYPE = "type"; 
	private static int[] COLORS = new int[] {Color.GREEN, Color.YELLOW, Color.RED};
	
	private CategorySeries mSeries = new CategorySeries("");
	private DefaultRenderer mRenderer = new DefaultRenderer();
	private GraphicalView mChartView;
	private SimpleSeriesRenderer mRendererGreen = new SimpleSeriesRenderer();
	private SimpleSeriesRenderer mRendererYellow = new SimpleSeriesRenderer();
	private SimpleSeriesRenderer mRendererRed = new SimpleSeriesRenderer();
	
	// For setting the colors.
	private final int GREEN = 0;
	private final int YELLOW = 1;
	private final int RED = 2;
	
	// Cut-off values for pie chart categories.
	private final int GREEN_CUTOFF = 3;
	private final int YELLOW_CUTOFF = 8;

	private Button 					 mSearchButton;
	private Button					 mCarbHistoryButton;
	private Button					 mWeightHistoryButton;
	private Button					 mEnterWeightButton;
	private TextView				 mDailyCarbCountTextView;
	private CarbHistoryDbAdapter	 mCarbHistory;
	private FoodsDbAdapter			 mFoodsDatabase;
	public static final String 		 PREFS_NAME = "CarbTrackerPrefs";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		if(D) Log.i(TAG, "+++ ON CREATE +++");
		mCarbHistory = new CarbHistoryDbAdapter(getApplicationContext());
		mFoodsDatabase = new FoodsDbAdapter(getApplicationContext());
		mSearchButton = (Button)findViewById(R.id.search_button);
		mCarbHistoryButton = (Button)findViewById(R.id.carb_history_button);
		mWeightHistoryButton = (Button)findViewById(R.id.weight_history_button);
		mEnterWeightButton = (Button)findViewById(R.id.enter_weight_button);
		mDailyCarbCountTextView = (TextView) findViewById(R.id.carb_count);
		mFoodsDatabase.open();
		// For the pie chart.
		mRenderer.setChartTitleTextSize(20); 
		mRenderer.setMargins(new int[] {20, 30, 15, 0});
		mRenderer.setZoomButtonsVisible(false);
		mRenderer.setStartAngle(90);
		mRenderer.setShowLabels(false);
		mRenderer.setZoomEnabled(false);
		mRenderer.setPanEnabled(false); 
		mRenderer.setShowLegend(false);
		
		mSearchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent search_intent = new Intent(CarbTrackerActivity.this, SearchActivity.class);
				startActivity(search_intent);
			}
		});

		mCarbHistoryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent carb_history_intent = new Intent(CarbTrackerActivity.this, CarbHistoryActivity.class);
				startActivity(carb_history_intent);
			}
		});

		mWeightHistoryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent weight_history_intent = new Intent(CarbTrackerActivity.this, WeightHistoryActivity.class);
				startActivity(weight_history_intent);
			}
		});
		
		mDailyCarbCountTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent daily_carbs_intent = new Intent(CarbTrackerActivity.this, DailyCarbsActivity.class);
				startActivity(daily_carbs_intent);
			}
		});
		
		mEnterWeightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent add_weight_intent = new Intent(CarbTrackerActivity.this, WeightEntryActivity.class);
				startActivity(add_weight_intent);
			}
		});
	}
	
	private void upDatePieChart() {
		FoodGroups foodCategories = getPieChartValues();
		mSeries.clear();
		mSeries.add("Green Foods Series", foodCategories.getGreen());
		mRendererGreen.setColor(COLORS[GREEN]);
		mSeries.add("Yellow Foods Series", foodCategories.getYellow());
		mRendererYellow.setColor(COLORS[YELLOW]);
		mSeries.add("Red Foods Series", foodCategories.getRed());
		mRendererRed.setColor(COLORS[RED]);
		mRenderer.addSeriesRenderer(GREEN, mRendererGreen);
		mRenderer.addSeriesRenderer(YELLOW, mRendererYellow);
		mRenderer.addSeriesRenderer(RED, mRendererRed);

		if(mChartView != null) {
			mChartView.repaint();
		} 
	}
	
	/**
	 * Gets the values for the daily pie chart.
	 * @return an integer array with the values.
	 */
	private FoodGroups getPieChartValues() {
		float 	greenGroup = 0,	
				yellowGroup = 0,
				redGroup = 0;
		FoodGroups values = new FoodGroups();
		String[] carbHistoryColumns = new String[] {"_id",
										  AbstractDbAdapter.KEY_DATE,
										  AbstractDbAdapter.KEY_DESC,
										  AbstractDbAdapter.KEY_HCARBS,
										  AbstractDbAdapter.KEY_FOODSID};
		String[] foodsTableColumns = new String[] {"_id",
										  AbstractDbAdapter.KEY_CARBS};
		Cursor carbHistoryCursor = mCarbHistory.getDailyFoods(Tools.getDate(), carbHistoryColumns);
		if(carbHistoryCursor == null) {
			return values;
		} else if(!carbHistoryCursor.moveToFirst()) {
			carbHistoryCursor.close();
			return values;
		}
		do {
			int foodsIndex = carbHistoryCursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_FOODSID);
			String rowId = carbHistoryCursor.getString(foodsIndex);
			if(D) Log.i(TAG, "rowId = " + rowId);
			int dailyCarbsIndex = carbHistoryCursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_HCARBS);
			float dailyCarbsEntry = carbHistoryCursor.getFloat(dailyCarbsIndex);
			Cursor foodsCursor = mFoodsDatabase.getFood(rowId, foodsTableColumns);
			if(foodsCursor == null) {
				if(D) Log.i(TAG, "no match found in FoodsTable for " + rowId);
				return values;
			}
			
			int carbsIndex = foodsCursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_CARBS);
			float carbs = foodsCursor.getFloat(carbsIndex);
			foodsCursor.close();
			if(carbs >= 0 && carbs <= GREEN_CUTOFF) 					greenGroup += dailyCarbsEntry; 
			else if(carbs > GREEN_CUTOFF && carbs <= YELLOW_CUTOFF) 	yellowGroup += dailyCarbsEntry;
			else 													    redGroup += dailyCarbsEntry;
		} while(carbHistoryCursor.moveToNext());
		carbHistoryCursor.close();
		values.setGreen(greenGroup);
		values.setYellow(yellowGroup);
		values.setRed(redGroup);
		return values;
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		if(D) Log.i(TAG, "onRestoreInstanceState()");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(D) Log.i(TAG, "onSaveInstanceState()");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(D) Log.i(TAG, "- ON PAUSE -");
		mCarbHistory.close();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(D) Log.i(TAG, "-- ON STOP --");
		//mCarbHistory.close();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		if(D) Log.i(TAG, "++ ON RESTART ++");
		//mCarbHistory.open();
		restoreState();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(D) Log.i(TAG, "--- ON DESTROY ---");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(D) Log.i(TAG, "+ ON RESUME +");
		mCarbHistory.open();
		restoreState();
		upDatePieChart();
		if(mChartView == null && mSeries != null && mRenderer != null) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.pie_chart);
			if(mSeries != null && mRenderer != null) {
				try {
					mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
				mRenderer.setClickEnabled(false);
				mRenderer.setSelectableBuffer(10);
				layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT));
			}
		} else {
			mChartView.repaint();
		}
	}
	
	/**
	 * Helper method for restoring the state of the application.
	 * Namely the Daily Carbohydrate Count.
	 */
	private void restoreState() {
		String date = Tools.getDate();
		float carbs = 0;
		Cursor cursor = null;
		try {
			cursor = mCarbHistory.getDailyCarbTotal(date);
			if(cursor != null && cursor.moveToFirst()) {
				int index = cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_DAILYTOTAL);
				carbs = cursor.getFloat(index);
				cursor.close();
			}
		} catch (NullPointerException npe) {
			if(cursor != null) cursor.close();
			npe.printStackTrace();
		}
		mDailyCarbCountTextView.setText(Integer.toString(Math.round(carbs)));
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		if((keyCode == KeyEvent.KEYCODE_BACK)) {
			showDialog(0);
		}
    	return false;
    }
    
    @Override
    protected Dialog onCreateDialog(int d) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Are you sure you want to exit?");
    	builder.setCancelable(false);
    	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
    	return builder.create();
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
            case R.id.carbtracker_prefs:
            	Intent intent = new Intent(this, CarbTrackerPreferencesActivity.class);
            	startActivity(intent);
            	return true;
            default:
                return false;
        }
    }
}