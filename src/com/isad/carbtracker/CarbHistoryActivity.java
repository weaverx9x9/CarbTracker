package com.isad.carbtracker;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class CarbHistoryActivity extends Activity {
	private static final String TAG = "CarbHistoryActivity";
	private static final boolean D = true;
	
	private static final String LINE_COLOR = "carbhistory_line_color";
	private static final String AXES_COLOR = "carbhistory_axes_color";
	private static final String LABEL_COLOR = "carbhistory_label_color";
	protected int mLineColor;
	protected int mLabelColor;
	protected int mAxesColor;
	private CarbHistoryDbAdapter mCarbHistory;
	private SharedPreferences mPreferences;
	private CarbHistoryChart mChart;
	private List<CarbsPoint> mPoints;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(D) Log.i(TAG, "+++ ON CREATE +++");
		mCarbHistory = new CarbHistoryDbAdapter(getApplicationContext());
		setupChartColors();
		mChart = new CarbHistoryChart();
		mPoints = new ArrayList<CarbsPoint>();
		mCarbHistory.open();
		mPoints = getPoints();
		setContentView(mChart.getView(this, mPoints));
	}
	
	/**
	 * Get the date, carbohydrate pairs for displaying in the 
	 * CarbHistory graph.
	 * @return A List of Points.
	 * TODO Could clean this up a bit.
	 */
	private List<CarbsPoint> getPoints() {
		Cursor cursor = mCarbHistory.getCarbHistory();
		List<CarbsPoint> points = new ArrayList<CarbsPoint>();
		if(cursor == null) {
			String d = Tools.getDate();
			int year = Tools.getYear(d);
			int month = Tools.getMonth(d);
			int day = Tools.getDay(d);
			points.add(new CarbsPoint(new Date(year, month, day), 0));
			return points;
		} else {
			cursor.moveToFirst();
			int totalCarbsIndex = cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_DAILYTOTAL);
			int dateIndex = cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_DATE);
			do {
				String date = cursor.getString(dateIndex);
				if(D) Log.i(TAG, "date from cursor = " + date);
				date = Tools.formatDate(date, "");
				int year = Tools.getYear(date);
				if(D) Log.i(TAG, "year = " + year);
				int month = Tools.getMonth(date);
				int day = Tools.getDay(date);
				float carbs = cursor.getFloat(totalCarbsIndex);
				points.add(new CarbsPoint(new Date(year, month, day), carbs));
			} while(cursor.moveToNext());
			return points;
		}
	}
	
	private void setupChartColors() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String lineColor = preferences.getString(LINE_COLOR, "Color.GREEN");
		String labelColor = preferences.getString(LABEL_COLOR, "Color.LTGRAY");
		String axesColor = preferences.getString(AXES_COLOR, "Color.GRAY");
		try {
			mLineColor = Integer.parseInt(lineColor);
			mLabelColor = Integer.parseInt(labelColor);
			mAxesColor = Integer.parseInt(axesColor);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Override
	protected void onStop() {
		super.onStop();
		if(D) Log.i(TAG, "-- ON STOP --");
		mCarbHistory.close();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(D) Log.i(TAG, "- ON PAUSE -");
		//mCarbHistory.close();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(D) Log.i(TAG, "+ ON RESUME +");
		mCarbHistory.open();
		setupChartColors();
		mPoints = getPoints();
		setContentView(mChart.getView(this, mPoints));
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if(D) Log.i(TAG, "++ ON RESTART ++");
		//mCarbHistory.open();
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	  
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.carbhistory_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			onSearchRequested();
			return true;
		case R.id.carbhistory_prefs:
			Intent intent = new Intent(this, CarbHistoryPreferencesActivity.class);
			startActivity(intent);
			return true;
		default:
			return false;
		}
	}
	
	private class CarbHistoryChart {
		private static final String DATE_FORMAT = "MM/dd/yyyy";
		
		public GraphicalView getView(Context context, List<CarbsPoint> points) {
			String title = "Carbohydrate History";
			int[] colors = new int[] { mLineColor };
			PointStyle[] styles = new PointStyle[] { PointStyle.POINT};
			XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
			CarbsPoint max = Collections.max(points);
			setChartSettings(renderer, title,
					"Date",
					"Carbohydrates", points.get(0)
					.getDate().getTime(), points.get(points.size() - 1)
					.getDate().getTime(), 0, max.getCarbs(), mAxesColor, mLabelColor);
			renderer.setXLabels(5);
			renderer.setYLabels(10);
			SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(0);
			seriesRenderer.setDisplayChartValues(true);

			return ChartFactory.getTimeChartView(context,
					buildDateDataset(title, points), renderer, DATE_FORMAT);
		}

		protected void setChartSettings(XYMultipleSeriesRenderer renderer,
				String title, String xTitle, String yTitle, double xMin,
				double xMax, double yMin, double yMax, int axesColor,
				int labelsColor) {
			renderer.setChartTitle(title);
			renderer.setXTitle(xTitle);
			renderer.setYTitle(yTitle);
			renderer.setXAxisMin(xMin);
			renderer.setXAxisMax(xMax);
			renderer.setYAxisMin(yMin);
			renderer.setYAxisMax(yMax);
			renderer.setAxesColor(axesColor);
			renderer.setLabelsColor(labelsColor);
		}

		protected XYMultipleSeriesRenderer buildRenderer(int[] colors,
				PointStyle[] styles) {
			XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
			setRendererProperties(renderer, colors, styles);
			return renderer;
		}

		protected XYMultipleSeriesDataset buildDateDataset(String title,
				List<CarbsPoint> points) {
			XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
			TimeSeries series = new TimeSeries(title);
			DecimalFormat rounded = new DecimalFormat("#.##");
			for (CarbsPoint point : points) {
				series.add(point.getDate(), Double.valueOf(rounded.format(point.getCarbs())));
			}
			dataset.addSeries(series);
			return dataset;
		}

		protected void setRendererProperties(XYMultipleSeriesRenderer renderer, int[] colors,
				PointStyle[] styles) {
			renderer.setAxisTitleTextSize(16);
			renderer.setChartTitleTextSize(20);
			renderer.setLabelsTextSize(15);
			renderer.setShowLegend(false);
			renderer.setAntialiasing(true);
			renderer.setPointSize(10f);
			renderer.setMargins(new int[] { 20, 30, 15, 20 });
			int length = colors.length;
			for (int i = 0; i < length; i++) {
				XYSeriesRenderer r = new XYSeriesRenderer();
				r.setColor(colors[i]);
				r.setPointStyle(styles[i]);
				renderer.addSeriesRenderer(r);
			}
		}
	}	
	
}
