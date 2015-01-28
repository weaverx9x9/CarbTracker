package com.isad.carbtracker;
import android.app.Activity;
import android.os.Bundle;


public class CarbTrackerPreferencesActivity extends Activity {
	// For debugging.
	//private static final String TAG = "CarbTrackerPreferenceActivity";
	//private static final boolean D = true;
	
	public static final String PREFS_NAME = "CarbTrackerPrefs";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//addPreferencesFromResource(R.xml.carbhistory_preference);
		setContentView(R.layout.temp);
	}
}
