package com.isad.carbtracker;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class WeightHistoryChart {
	private static final String DATE_FORMAT = "MM/dd/yyyy";
	public static final String PREFS_NAME = "CarbTrackerPrefs";

	public GraphicalView getView(Context context, List<WeightPoint> points) {
		String title = "Weight History";
		SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
		int lineColor = prefs.getInt("weighthistory_line_color", Color.GREEN);
		int axesColor = prefs.getInt("weighthistory_axes_color", Color.GRAY);
		int labelColor = prefs.getInt("weighthistory_label_color", Color.LTGRAY);
		int[] colors = new int[] { lineColor };
		PointStyle[] styles = new PointStyle[] { PointStyle.POINT};
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);

		WeightPoint max = Collections.max(points);
		WeightPoint min = Collections.min(points);
		setChartSettings(renderer, title,
				"Date",
				"Weight", points.get(0)
				.getDate().getTime(), points.get(points.size() - 1)
				.getDate().getTime(), min.getWeight(), max.getWeight(), axesColor, labelColor);
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