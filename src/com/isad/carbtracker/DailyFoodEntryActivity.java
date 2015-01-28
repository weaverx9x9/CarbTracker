package com.isad.carbtracker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class DailyFoodEntryActivity extends Activity {
	// For debugging.
	private static final String TAG = "DailyFoodEntryActivity";
	private static final boolean D = true;
	
	private TextView mName;
	private TextView mDate;
	private TextView mTime;
	private TextView mCarbs;
	private TextView mGrams;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dailyentry);
		if(D) Log.i(TAG, "+++ ON CREATE +++");
		Bundle extras = getIntent().getExtras();
		mName = (TextView)findViewById(R.id.name);
		mDate = (TextView)findViewById(R.id.date);
		mTime = (TextView)findViewById(R.id.time);
		mCarbs = (TextView)findViewById(R.id.carbs);
		mGrams = (TextView)findViewById(R.id.grams);
		String name = "",
			   date = "",
			   time = "";
		Float carbs = 0f;
		Integer grams = 0;
		
		if(extras != null) {
			name = extras.getString(AbstractDbAdapter.KEY_DESC);
			date = extras.getString(AbstractDbAdapter.KEY_DATE);
			time = extras.getString(AbstractDbAdapter.KEY_TIMESTAMP);
			carbs = extras.getFloat(AbstractDbAdapter.KEY_HCARBS);
			grams = extras.getInt(AbstractDbAdapter.KEY_GRAMS);
			date = Tools.formatDate(date, "/");
			time = Tools.formatTime(time, false);
		} else {
			Toast.makeText(this, "Error", Toast.LENGTH_SHORT);
		}
		
		mName.setText(name);
		mDate.setText(date);
		mTime.setText(time);
		mCarbs.setText(Tools.formatDecimal(carbs.toString(), 2));
		mGrams.setText(grams.toString());
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
