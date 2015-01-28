package com.isad.carbtracker;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class WeightHistoryPreferencesActivity extends PreferenceActivity {
	// For debugging.
	private static final String TAG = "WeightHistoryPreferencesActivity";
	private static final boolean D = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(D) Log.i(TAG, "+++ ON CREATE +++");
		addPreferencesFromResource(R.xml.weighthistory_preference);
	}
}
