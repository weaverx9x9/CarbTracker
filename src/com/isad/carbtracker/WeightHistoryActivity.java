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

public class WeightHistoryActivity extends Activity {
	// For debugging
	private static final String TAG = "WeightHistoryActivity";
	private static final boolean D = true;
	
	private static final String LINE_COLOR = "weighthistory_line_color";
	private static final String AXES_COLOR = "weighthistory_axes_color";
	private static final String LABEL_COLOR = "weighthistory_label_color";
	protected int mLineColor;
	protected int mLabelColor;
	protected int mAxesColor;
	
	private WeightHistoryDbAdapter mWeightHistory;
	private SharedPreferences mPreferences;
	private WeightHistoryChart mChart;
	private 
	List<WeightPoint> mPoints;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWeightHistory = new WeightHistoryDbAdapter(getApplicationContext());
		setupChartColors();
		mChart = new WeightHistoryChart();
		mPoints = new ArrayList<WeightPoint>();
		mWeightHistory.open();
		mPoints = getPoints();
		setContentView(mChart.getView(this, mPoints));
	}

	/**
	 * Get the date, weight values from the WeightHistory Table.
	 * 
	 * @return Return a List of WeightPoints.
	 */
	private List<WeightPoint> getPoints() {
		Cursor cursor = mWeightHistory.getWeightHistory();
		List<WeightPoint> points = new ArrayList<WeightPoint>();
		if(cursor == null) {
			if(D) Log.i(TAG, "in if(cursor == null) of getPoints()");
			String d = Tools.getDate();
			int year = Tools.getYear(d);
			int month = Tools.getMonth(d);
			int day = Tools.getDay(d);
			points.add(new WeightPoint(new Date(year, month, day), 0.0));
			return points;
		} else {
			cursor.moveToFirst();
			int weightIndex = cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_WEIGHT);
			int dateIndex = cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_DATE);
			do {
				if(D) Log.i(TAG, "In do..while of getPoints()");
				String date = cursor.getString(dateIndex);
				if(D) Log.i(TAG, "date from cursor = " + date);
				date = Tools.formatDate(date, "");
				int year = Tools.getYear(date);
				int month = Tools.getMonth(date);
				int day = Tools.getDay(date);
				double weight = 0;
				weight = cursor.getFloat(weightIndex);
				points.add(new WeightPoint(new Date(year, month, day), weight));
			} while(cursor.moveToNext());
			return points;
		}
	}

	private void setupChartColors() {
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String lineColor = mPreferences.getString(LINE_COLOR, "-16711936");
		String labelColor = mPreferences.getString(LABEL_COLOR, "-3355444");
		String axesColor = mPreferences.getString(AXES_COLOR, "-7829368");
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
		//mWeightHistory.close();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(D) Log.i(TAG, "- ON PAUSE -");
		mWeightHistory.close();
	}
	
	@Override 
	protected void onResume() {
		super.onResume();
		if(D) Log.i(TAG, "+ ON RESUME +");
		mWeightHistory.open();
		setupChartColors();
		mPoints = getPoints();
		setContentView(mChart.getView(this, mPoints));
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		if(D) Log.i(TAG, "++ ON RESTART ++");
		//mWeightHistory.open();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(D) Log.i(TAG, "--- ON DESTROY ---");
		//mWeightHistory.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.weighthistory_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(D) Log.i(TAG, "item = " + item.toString());
		switch (item.getItemId()) {
		case R.id.search:
			onSearchRequested();
			return true;
		case R.id.weighthistory_prefs:
			Intent intent = new Intent(this, WeightHistoryPreferencesActivity.class);
			startActivity(intent);
			return true;
		default:
			return false;
		}
	}  
	
	public class WeightHistoryChart {
		private static final String DATE_FORMAT = "MM/dd/yyyy";
		public static final String PREFS_NAME = "CarbTrackerPrefs";

		public GraphicalView getView(Context context, List<WeightPoint> points) {
			String title = "Weight History";
			int[] colors = new int[] { mLineColor };
			PointStyle[] styles = new PointStyle[] { PointStyle.POINT};
			XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
			WeightPoint max = Collections.max(points);
			WeightPoint min = Collections.min(points);
			setChartSettings(renderer, title,
					"Date",
					"Weight", points.get(0)
					.getDate().getTime(), points.get(points.size() - 1)
					.getDate().getTime(), min.getWeight(), max.getWeight(), mAxesColor, mLabelColor);
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
				List<WeightPoint> points) {
			XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
			TimeSeries series = new TimeSeries(title);
			DecimalFormat rounded = new DecimalFormat("#.##");
			for (WeightPoint point : points) {
				series.add(point.getDate(), Double.valueOf(rounded.format(point.getWeight())));
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
