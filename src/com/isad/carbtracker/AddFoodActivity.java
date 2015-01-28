package com.isad.carbtracker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddFoodActivity extends Activity {
	// For debugging.
	private static final String TAG = "AddFoodActivity";
	private static final boolean D = true;
	
	// The numbers at the end of each of these elements
	// are based on the row that they are in the layout.
	// For example, mTextViewDesc doesn't have a 1 because 
	// that is fixed at 100 grams in XML, so we start from 
	// row 2, the same applies to mTextViewGrams.
	private EditText mEditTextAmount1;
	private EditText mEditTextAmount2;
	private EditText mEditTextAmount3;
	private EditText mEditTextAmount4;
	private EditText mEditTextCustomGrams4;
	private TextView mTextViewDesc2;
	private TextView mTextViewDesc3;
	private TextView mTextViewGrams2;
	private TextView mTextViewGrams3;
	private TextView mTextViewFoodName;
	private Button mAddItemButton;
	private Button mUpdateTotalButton;
	private TextView mTotalCarbsTextView;
	private CheckBox mCheckBox1;
	private CheckBox mCheckBox2;
	private CheckBox mCheckBox3;
	private CheckBox mCheckBox4;
	
	private double mTotalCarbs;
	private double mCarbs;
	private double mFiber;
	private double mGmWt1;
	private double mGmWt2;
	private int    mTotalGrams;
	private long   mIndex;
	private String mName;
	private CarbHistoryDbAdapter mCarbHistory;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_food);
		if(D) Log.i(TAG, "++ ON CREATE ++");
		Bundle extras = getIntent().getExtras();
		mCarbHistory = new CarbHistoryDbAdapter(getBaseContext());
		mCarbHistory.open();
		// View elements.
		mEditTextAmount1 = (EditText)findViewById(R.id.editText1);
		mEditTextAmount2 = (EditText)findViewById(R.id.editText2);
		mEditTextAmount3 = (EditText)findViewById(R.id.editText3);
		mEditTextAmount4 = (EditText)findViewById(R.id.editText4);
		mEditTextCustomGrams4 = (EditText)findViewById(R.id.editText_customWeight);
		mTextViewDesc2 = (TextView)findViewById(R.id.textView2);
		mTextViewDesc3 = (TextView)findViewById(R.id.textView3);
		mTextViewGrams2 = (TextView)findViewById(R.id.textView22);
		mTextViewGrams3 = (TextView)findViewById(R.id.textView32);
		mTextViewFoodName = (TextView)findViewById(R.id.addFood_foodName);
		mAddItemButton = (Button)findViewById(R.id.button1);
		mUpdateTotalButton = (Button)findViewById(R.id.button2);
		mCheckBox1 = (CheckBox)findViewById(R.id.checkBox1);
		mCheckBox2 = (CheckBox)findViewById(R.id.checkBox2);
		mCheckBox3 = (CheckBox)findViewById(R.id.checkBox3);
		mCheckBox4 = (CheckBox)findViewById(R.id.checkBox4);
		mTotalCarbsTextView = (TextView)findViewById(R.id.totalCarbs_value);
		mTotalCarbs = 0;

		String name = mName = extras.getString(AbstractDbAdapter.KEY_NAME);
		String desc1 = extras.getString(AbstractDbAdapter.KEY_GMWT1_DESC);
		String desc2 = extras.getString(AbstractDbAdapter.KEY_GMWT2_DESC);
		String grams1 = extras.getString(AbstractDbAdapter.KEY_GMWT1);
		String grams2 = extras.getString(AbstractDbAdapter.KEY_GMWT2);
		String fiber = extras.getString(AbstractDbAdapter.KEY_FIBER);
		String carbs = extras.getString(AbstractDbAdapter.KEY_CARBS);
		mIndex = extras.getLong(AbstractDbAdapter.KEY_FOODSID);
		
		if(mName.length() > 0) mTextViewFoodName.setText(mName);
		
		try {
			if(fiber != null && fiber.length() > 0) mFiber = Double.parseDouble(fiber);
			else mFiber = 0;
			if(carbs != null && carbs.length() > 0) mCarbs = Double.parseDouble(carbs);
			else mCarbs = 0;
			if(grams1 != null && grams1.length() > 0) mGmWt1 = Double.parseDouble(grams1);
			else mGmWt1 = 0;
			if(grams2 != null && grams2.length() > 0) mGmWt2 = Double.parseDouble(grams2);
			else mGmWt2 = 0;
			if(name.length() < 1) mName = name = "";
			else mName = name;
			if(desc1.length() < 1) desc1 = "";
			if(desc2.length() < 1) desc2 = "";
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (NumberFormatException nfe) { // may not need this.
			nfe.printStackTrace();
		}
		mTextViewDesc2.setText(desc1);
		mTextViewDesc3.setText(desc2);
		mTextViewGrams2.setText(grams1);
		mTextViewGrams3.setText(grams2);

		mUpdateTotalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCheckBox1.isChecked() || mCheckBox2.isChecked() ||
				   mCheckBox3.isChecked() || mCheckBox4.isChecked()) {
					mTotalCarbs = 0;
					mTotalCarbs = getCarbs();
					mTotalCarbsTextView.setText(String.format("%.2f", mTotalCarbs));
				} else if(!mCheckBox1.isChecked() && !mCheckBox2.isChecked() &&
						  !mCheckBox3.isChecked() && !mCheckBox4.isChecked()) {
					mTotalCarbsTextView.setText("0.0");
				}
			}
		});

		mAddItemButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCheckBox1.isChecked() || mCheckBox2.isChecked() ||
						mCheckBox3.isChecked() || mCheckBox4.isChecked()) {
					mTotalCarbs = getCarbs();
					if(mName != null) mCarbHistory.insert(mName, mTotalCarbs, mTotalGrams, mIndex);
					finish();
				} else { // No boxes checked.
					Toast.makeText(AddFoodActivity.this, "No amount selected", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	/**
	 * Gets the number of net carbohydrates based on the users entries.
	 * 
	 * @return A float representing the number of carbohydrates based on the quantity 
	 * 	      specified by the user.
	 */
	private float getCarbs() {
		mTotalCarbs = 0; 
		float carbs = 0;
		mTotalGrams = 0;
		// TODO Need to find a solution here other than try catch.  Need
		// to understand why it is crashing on line double amount2 = Double.parse...
		try {
			if(mCheckBox1.isChecked()) {
				String tempString = mEditTextAmount1.getText().toString();
				if(tempString == null || tempString.length() < 1) tempString = "1";
				double amount = Double.parseDouble(tempString);
				mTotalGrams += amount * 100;
				carbs += (amount * mCarbs) - (amount * mFiber);
			}
			if(mCheckBox2.isChecked()) {
				if(D) Log.i(TAG, "in mCheckBox2");
				String tempString = mEditTextAmount2.getText().toString();
				if(tempString == null || tempString.length() < 1) {
					tempString = "1";
				}
				double amount = Double.parseDouble(tempString);
				mTotalGrams += amount * mGmWt1;
				carbs += (amount * (mGmWt1) * (mCarbs/100)) - 
						 (amount * (mGmWt1) * (mFiber/100));
			}
			if(mCheckBox3.isChecked()) {
				String tempString = mEditTextAmount3.getText().toString();
				if(tempString == null) tempString = "1";
				double amount = Double.parseDouble(tempString);
				mTotalGrams += amount * mGmWt2;
				carbs += (amount * (mGmWt2) * (mCarbs/100)) -
						 (amount * (mGmWt2) * (mFiber/100));
			}
			if(mCheckBox4.isChecked()) {
				String tempString1 = mEditTextAmount4.getText().toString(),
						tempString2 = mEditTextCustomGrams4.getText().toString();
				if(tempString1 == null) tempString1 = "1";
				if(tempString2 == null) tempString2 = "0.0";
				double amount1 = Double.parseDouble(tempString1);
				double amount2 = Double.parseDouble(tempString2);
				mTotalGrams += amount1 * amount2;
				carbs += ((amount1 * amount2) * (mCarbs/100)) -
						 ((amount1 * amount2) * (mFiber/100));
			}
		} catch (NumberFormatException e) {
			Log.e(TAG, "Error calculating Carbs, problem with one of the values");
			e.printStackTrace();
		}
		return carbs;
	}

	@Override
	protected void onStop() {
		super.onStop();
		mCarbHistory.close();
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
