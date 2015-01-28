package com.isad.carbtracker;

import java.text.DecimalFormat;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * 
 * @author weaver
 */
public class DailyCarbsActivity extends ListActivity {
	// For debugging.
	private static final String TAG = "DailyCarbsActivity";
	private static final boolean D = true;
	
	private CarbHistoryDbAdapter mCarbHistory;
	private Cursor 		  mCursor;
	private ListView	  mListView;
	private static final int DELETE_ID = Menu.FIRST;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.daily_carbs);
		if(D) Log.i(TAG, "+++ ON CREATE +++");
		mCarbHistory = new CarbHistoryDbAdapter(getApplicationContext());
		mCarbHistory.open();
	}
	
	private void updateList() {
		String[] columns = new String[] {"_id", 
				AbstractDbAdapter.KEY_DATE,
				AbstractDbAdapter.KEY_TIMESTAMP,
				AbstractDbAdapter.KEY_DESC, 
				AbstractDbAdapter.KEY_HCARBS};  
		mCursor = mCarbHistory.getDailyFoods(Tools.getDate(), columns);
		if(mCursor == null) {
			mCursor = new MatrixCursor(columns);
		}
		startManagingCursor(mCursor);
		int[] to = new int[] {R.id.name_entry, 
							  R.id.carbs_entry, 
							  R.id.time_entry};
		columns = new String[] {AbstractDbAdapter.KEY_DESC, 
								AbstractDbAdapter.KEY_HCARBS, 
								AbstractDbAdapter.KEY_TIMESTAMP};
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.daily_list_entry,
				mCursor, columns, to) {
			@Override
			public void setViewText(TextView v, String text) {
				super.setViewText(v, formatText(v, text));
			}
		};
		setListAdapter(adapter);
	}

	private String formatText(TextView v, String text) {
		String formattedText = "";
		switch(v.getId()) {
		case R.id.carbs_entry:
			formattedText = Tools.formatDecimal(text, 2);
			if(formattedText == "-1") formattedText = text; // error was encountered.
			formattedText += " carbohydrates";
			break;
		case R.id.time_entry:
			formattedText = Tools.formatTime(text, false);
			break;
		case R.id.name_entry:
			formattedText = text;
			break;
		}
		return formattedText;
	}
	
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.remove_entry);
	}
    
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info1 = (AdapterContextMenuInfo)item.getMenuInfo();
			mCarbHistory.delete(info1.id);
			updateList();
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l,  v, position, id);
		Cursor c = mCursor;
		c.moveToPosition(position);
		if(D) Log.i(TAG, "position = " + position);
		if(c != null) {
			Intent intent = new Intent(this, DailyFoodEntryActivity.class);
			try {
				Integer rowId = c.getInt(c.getColumnIndexOrThrow("_id"));
				Cursor cursor = mCarbHistory.getCarbHistoryEntry(rowId.toString(), null);
				if(cursor != null) {
					if(cursor.moveToFirst()) {
						intent.putExtra(AbstractDbAdapter.KEY_DATE, cursor.getString(cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_DATE)));
						intent.putExtra(AbstractDbAdapter.KEY_TIMESTAMP, cursor.getString(cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_TIMESTAMP)));
						intent.putExtra(AbstractDbAdapter.KEY_DESC, cursor.getString(cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_DESC)));
						intent.putExtra(AbstractDbAdapter.KEY_HCARBS, cursor.getFloat(cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_HCARBS)));
						intent.putExtra(AbstractDbAdapter.KEY_GRAMS, cursor.getInt(cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_GRAMS)));
						cursor.close();
					} else {
						cursor.close();
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			
			startActivity(intent);
		}
	}
	
    @Override
    protected void onStop() {
    	super.onStop();
    	if(D) Log.i(TAG, "-- ON STOP --");
    	if(mCursor != null) mCursor.close();
    	mCarbHistory.close();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if(D) Log.i(TAG, "- ON PAUSE -");
    	
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mCarbHistory.open();
		mListView = getListView();
		registerForContextMenu(mListView);
		updateList();
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
