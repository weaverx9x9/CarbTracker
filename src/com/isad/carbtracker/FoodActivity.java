package com.isad.carbtracker;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * This activity displays the details of a food.  Such as the number
 * of carbohydrates per 100 gram serving, fiber, sugar, weight, etc..
 * There is also a button that allows the user to add the food to the
 * foods they have eaten for the day, this causes their total carbohydrates 
 * for the day, on the main application screen to be updated.
 * 
 * @author weaver
 *
 */
public class FoodActivity extends Activity {
	// For debugging.
	private static final String TAG = "FoodActivity";
	private static final boolean D = true;
	
	private Button mAddFoodButton;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.food);
    	if(D) Log.i(TAG, "++ ONCREATE ++");

    	mAddFoodButton = (Button)findViewById(R.id.button1);
        Uri uri = getIntent().getData();
        Cursor cursor = managedQuery(uri, null, null, null, null);

        if (cursor == null) {
            finish();
        } else {
            cursor.moveToFirst();
            TextView name_tv = (TextView)findViewById(R.id.food_name_value);
            TextView carbs_tv = (TextView) findViewById(R.id.food_carbs_value);
            TextView fiber_tv = (TextView) findViewById(R.id.food_fiber_value);
            TextView sugar_tv = (TextView) findViewById(R.id.food_sugar_value);
            TextView gmwt1_tv = (TextView) findViewById(R.id.serving_size1_grams_value);
            TextView gmwt1_desc_tv = (TextView) findViewById(R.id.serving_size1_value);
            TextView gmwt2_tv = (TextView) findViewById(R.id.serving_size2_grams_value);
            TextView gmwt2_desc_tv = (TextView) findViewById(R.id.serving_size2_value);
            int nameIndex = cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_NAME);
            int carbsIndex = cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_CARBS);
            int fiberIndex = cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_FIBER);
            int sugarIndex = cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_SUGAR);
            int gmwt1Index = cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_GMWT1);
            int gmwt1_descIndex = cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_GMWT1_DESC);
            int gmwt2Index = cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_GMWT2);
            int gmwt2_descIndex = cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_GMWT2_DESC);
            int rowIndex = cursor.getColumnIndexOrThrow("_id");
            
            String fiberString = cursor.getString(fiberIndex);
            if(fiberString.length() < 1 || fiberString == null) fiberString = "0";
            fiber_tv.setText(fiberString);
            String sugarString = cursor.getString(sugarIndex);
            if(sugarString.length() < 1 || sugarString == null) sugarString = "0";
            sugar_tv.setText(sugarString);
            String carbsString = cursor.getString(carbsIndex);
            if(carbsString.length() < 1 || carbsString == null) carbsString = "0";
            carbs_tv.setText(carbsString);
            
            String gmwt1String = cursor.getString(gmwt1Index);
            if(gmwt1String.length() < 1 || gmwt1String == null) gmwt1String = "";
            gmwt1_tv.setText(gmwt1String);
            String gmwt2String = cursor.getString(gmwt2Index);
            if(gmwt2String.length() < 1 || gmwt2String == null) gmwt2String = "";
            gmwt2_tv.setText(gmwt2String);
            String gmwt1_descString = cursor.getString(gmwt1_descIndex);
            if(gmwt1_descString.length() < 1 || gmwt1_descString == null) gmwt1_descString = "";
            gmwt1_desc_tv.setText(gmwt1_descString);
            String gmwt2_descString = cursor.getString(gmwt2_descIndex);
            if(gmwt2_descString.length() < 1 || gmwt2_descString == null) gmwt2_descString = "";
            gmwt2_desc_tv.setText(gmwt2_descString);
            name_tv.setText(cursor.getString(nameIndex));
            
            //fiber.setText(cursor.getString(fiberIndex));
            //sugar.setText(cursor.getString(sugarIndex));
            //carbs.setText(cursor.getString(carbsIndex));
            
            // For passing the data to AddFoodActivity.
            final String name = cursor.getString(nameIndex);
            final String fiber = cursor.getString(fiberIndex);
            final String carbs = cursor.getString(carbsIndex);
            final String gmwt1 = cursor.getString(gmwt1Index);
            final String gmwt2 = cursor.getString(gmwt2Index);
            final String gmwt1_desc = cursor.getString(gmwt1_descIndex);
            final String gmwt2_desc = cursor.getString(gmwt2_descIndex);
            final long index = cursor.getLong(rowIndex);
            
            mAddFoodButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(FoodActivity.this, AddFoodActivity.class);
					intent.putExtra(AbstractDbAdapter.KEY_NAME, name);
					intent.putExtra(AbstractDbAdapter.KEY_FIBER, fiber);
					intent.putExtra(AbstractDbAdapter.KEY_CARBS, carbs);
					intent.putExtra(AbstractDbAdapter.KEY_GMWT1, gmwt1);
					intent.putExtra(AbstractDbAdapter.KEY_GMWT2, gmwt2);
					intent.putExtra(AbstractDbAdapter.KEY_GMWT1_DESC, gmwt1_desc);
					intent.putExtra(AbstractDbAdapter.KEY_GMWT2_DESC, gmwt2_desc);
					intent.putExtra(AbstractDbAdapter.KEY_FOODSID, index);
					startActivity(intent);
					finish();
				}
            });
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
            default:
                return false;
        }
    }
}
