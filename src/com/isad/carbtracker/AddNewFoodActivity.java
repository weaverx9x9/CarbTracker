package com.isad.carbtracker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Adds a new food Item to the database.  
 * @author weaver
 * TODO Need to finish implementing this item.
 */
public class AddNewFoodActivity extends Activity {
	// For debugging
	private static final String TAG = "AddNewFoodActivity";
	private static final boolean D = true;
	
	private EditText  mName;
	private EditText  mWeight;
	private EditText  mCarbs;
	private EditText  mFiber;
	private EditText  mSugar;
	private EditText  mDesc;
	private Button    mAddFood;
	private FoodsDbAdapter mFoodsDatabase;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addnewfood);
		mFoodsDatabase = new FoodsDbAdapter(getApplicationContext());
		mFoodsDatabase.open();
        Bundle bundle = getIntent().getExtras();
        String query = bundle.getString("query");
        mName = (EditText)  findViewById(R.id.editText1);
		mWeight = (EditText) findViewById(R.id.editText2);
		mCarbs = (EditText) findViewById(R.id.editText3);
		mFiber = (EditText) findViewById(R.id.editText4);
		mSugar = (EditText) findViewById(R.id.editText5);
		mDesc = (EditText)findViewById(R.id.editText6);
		mAddFood = (Button) findViewById(R.id.button1);
		
		mName.setText(query);
		
		// TODO Add something here to make the Text red if the user doesn't
		// enter in all data, after clicking the button.
		mAddFood.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mName.getText().length() > 0 &&
				   mWeight.getText().length() > 0 &&
				   mCarbs.getText().length() > 0 &&
				   mFiber.getText().length() > 0 &&
				   mSugar.getText().length() > 0 &&
				   mDesc.getText().length() > 0) {
					String name = mName.getText().toString();
					String weightString = mWeight.getText().toString();
					String carbsString = mCarbs.getText().toString();
					String fiberString = mFiber.getText().toString();
					String sugarString = mSugar.getText().toString();
					String desc = mDesc.getText().toString();
					double weight = Double.parseDouble(weightString);
					double carbs = Double.parseDouble(carbsString);
					double fiber = Double.parseDouble(fiberString);
					double sugar = Double.parseDouble(sugarString);
					double factor = Math.round(100 / weight);
					carbs *= factor;
					fiber *= factor;
					sugar *= factor;
					long errorCode = mFoodsDatabase.insert(name, weight, carbs, fiber, sugar, desc);
					if(errorCode == -1) 
						Toast.makeText(getApplicationContext(), "Error inserting in the database", Toast.LENGTH_SHORT).show();
					else {
						if(D) Log.i(TAG, "Item Added");
						mFoodsDatabase.close();
						finish();
					}
					
				} else {
					Toast.makeText(getApplicationContext(), "You must enter all values", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mFoodsDatabase.close();
	}
}
