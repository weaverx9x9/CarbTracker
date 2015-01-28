package com.isad.carbtracker;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WeightEntryActivity extends Activity {
	// For debugging.
	//private static final String TAG = "WeightEntryActivity";
	//private static final boolean D = true;
	
	private EditText 		mWeightEditText;
	private Button 			mAddWeightButton;
	private WeightHistoryDbAdapter 	mWeightHistory;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWeightHistory = new WeightHistoryDbAdapter(getApplication());
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.weight_entry);
		mAddWeightButton = (Button)findViewById(R.id.add_weight_button);
		mWeightEditText = (EditText)findViewById(R.id.weight_edit_text);
		final float previousWeight = previousWeight();
		mWeightEditText.setText(Float.toString(previousWeight));
		mAddWeightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String weightString = mWeightEditText.getText().toString();
				float weight = 0;
				if(weightString != null && weightString.length() > 1) {
					try {
						weight = Float.parseFloat(weightString);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					String date = Tools.getDate();
					date = Tools.formatDate(date, "");
					
					long error = 0;
					Cursor tempCursor = mWeightHistory.entryExists(date);
					if(tempCursor == null || tempCursor.getCount() < 1) error = mWeightHistory.insert(date, weight);
					else mWeightHistory.update(date, weight);
					if(tempCursor != null) tempCursor.close(); 
					
					if(error < 0) {
						Toast.makeText(WeightEntryActivity.this, "Error Adding Weight. Weight for " + 
								Tools.formatDate(Tools.getDate(), "/") + " already exists.  Error Code: " 
								+ error, Toast.LENGTH_LONG).show(); 
					} 
					finish();
				}
			}
			
		});
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mWeightHistory.close();
	}
	
	/**
	 * Get the most recent weight from the WeightHistory Table.
	 * @return An integer representing the most recent weight entry.
	 */
	private float previousWeight() {
		Cursor cursor = mWeightHistory.getLastWeight();
		if(cursor == null) {
			return 0;
		} else {
			if(!cursor.moveToFirst()) return 0;
			int weightIndex = cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_WEIGHT);
			float weight = 0;
			weight = cursor.getFloat(weightIndex);
			cursor.close();
			return weight;
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
		case R.id.carbtracker_prefs:
			Intent intent = new Intent(this, CarbTrackerPreferencesActivity.class);
			startActivity(intent);
		default:
			return false;
		}
	}
}
