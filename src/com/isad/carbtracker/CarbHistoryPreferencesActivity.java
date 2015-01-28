package com.isad.carbtracker;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class CarbHistoryPreferencesActivity extends PreferenceActivity {
	// For debugging.
	private static final String TAG = "CarbHistoryPreferencesActivity";
	private static final boolean D = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(D) Log.i(TAG, "+++ ON CREATE +++");
		addPreferencesFromResource(R.xml.carbhistory_preference);
	}
}
