package com.isad.carbtracker;

import java.text.DecimalFormat;
import java.util.Calendar;

import android.util.Log;

/**
 * This class contains various helper functions for managing time.
 * @author weaver
 *
 */
public final class Tools {
	// For debugging.
	private static final String TAG = "CarbTrackerTools";
	private static final boolean D = false;
	
	// Suppress default constructor for noninstantiability.
	private Tools() {
		throw new AssertionError();
	}

    /**
     * Gets the time (in 24-hour format), with a two digit format for each 
     * element.  For example, 1:45:26 pm would be 134526.  1:03:14 would be
     * 010314.
     * @return time in format described above, as a string.
     */
    public static String getTime() {
    	if(D) Log.i(TAG, "getTime()");
    	String time = "";
    	Calendar c = Calendar.getInstance();
    	int hours = c.get(Calendar.HOUR_OF_DAY);
    	int minutes = c.get(Calendar.MINUTE);
    	int seconds = c.get(Calendar.SECOND);
    	if(Integer.toString(hours).length() < 2) time = "0" + Integer.toString(hours);
    	else time = Integer.toString(hours);
    	if(Integer.toString(minutes).length() < 2) time += "0" + Integer.toString(minutes);
    	else time += Integer.toString(minutes);
    	if(Integer.toString(seconds).length() < 2) time += "0" + Integer.toString(seconds);
    	else time += Integer.toString(seconds);
    	return time;
    }
    
    /**
     * Rounds a number to the specified number of decimal places.
     * @param value   The value to round.
     * @param places  The number of decimal places.
     * @return        The rounded value.
     */
    public static String formatDecimal(String value, int places) {
    	Double number = 0d;
		try {
			number = Double.parseDouble(value);
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		}
    	String format = "#.";
    	for(int i = 0; i < places; i++) format += "#";
    	DecimalFormat rounded = new DecimalFormat(format);
    	String formattedNumber = value;
		try {
			formattedNumber = rounded.format(number);
		} catch (IllegalArgumentException e) {
			formattedNumber = "-1";
			e.printStackTrace();
		}
    	return formattedNumber;
    }
    
	/**
	 * Gets the current date as a string, uses two digits for 
	 * month and day.
	 * @return String representation of the current date.
	 */
	public static String getDate() {
		if(D) Log.i(TAG, "getDate()");
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int day = c.get(Calendar.DATE);
		int month = c.get(Calendar.MONTH);
		month += 1;
		String date = "";
		if(Integer.toString(month).length() < 2) date += "0";
		date += Integer.toString(month);
		if(Integer.toString(day).length() < 2) date += "0";
		date += Integer.toString(day);
		date += Integer.toString(year);
		return date;
	}
	
	/**
	 * Format a time string into human readable format.
	 * 
	 * @param time      The time passed in as HHmmSS (in military time).
	 * @param military  if True, format as military time. Else, use am/pm.
	 * @return          Formatted time string.
	 */
	public static String formatTime(String time, boolean military) {
		String formattedTime = "";
		if(military) {
			formattedTime = time;
		} else {
			if(time.length() < 6) time = new StringBuffer(time).insert(0, "0").toString();
			String hour = time.substring(0, 2);
			int hourInt = Integer.parseInt(hour);
			Integer twelveHour = hourInt % 12;
			if(twelveHour == 0) twelveHour = 12;
			formattedTime += twelveHour.toString();
			formattedTime += ":";
			formattedTime += time.substring(2, 4);
			formattedTime += ":";
			formattedTime += time.substring(4, 6);
			if(hourInt >= 12) formattedTime += " PM";
			else formattedTime += " AM";
		}
		return formattedTime;
	}
	
	/**
	 * Format a date for presenting to the user, with / separating month,
	 * day and year.
	 * @param date The date to format, must be in 2 digit format for day and month.
	 * @param seperator The character to use to seperate month, day and year.
	 * @return     The formated date as a string.
	 */
	public static String formatDate(String date, String seperator) {
		if(D) Log.i(TAG, "formatDate(...)");
		String formatedDate = "";
		if(date.length() < 8) date = new StringBuffer(date).insert(0, "0").toString();
		try {
			formatedDate += date.substring(0, 2);
			formatedDate += seperator + date.substring(2, 4);
			formatedDate += seperator + date.substring(4, 8);
		} catch (StringIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return formatedDate;
	}
	
	/**
	 * Return the year portion of the date.
	 * @param date Passed in as a string in the form MMddyyyy.
	 * @return The year as an integer, with 0 being 1900.
	 */
	public static int getYear(String date) {
		if(D) Log.i(TAG, "getYear(...)");
		if(date.length() < 8) date = formatDate(date, "");
		int year = -1;
		String yearString = "0";
		try {
			yearString = date.substring(4, 8);
		} catch (StringIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		year = Integer.parseInt(yearString);
		year -= 1900;
		return year;
	}
	
	/**
	 * Return the month portion of the date.
	 * @param date Passed in as a string in the form MMddyyyy.
	 * @return Return the month as an integer.  The months range from 0-11.
	 */
	public static int getMonth(String date) {
		if(D) Log.i(TAG, "getMonth(...)");
		if(date.length() < 8) date = formatDate(date, "");
		int month = -1;
		String monthString = "0";
		try {
			monthString = date.substring(0, 2);
		} catch (StringIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		month = Integer.parseInt(monthString);
		month--;
		return month;
	}
	
	/**
	 * Return the day portion of the date.
	 * @param date Passed in as a string in the form MMddyyyy.
	 * @return Return the date as an integer.
	 */
	public static int getDay(String date) {
		if(D) Log.i(TAG, "getDay(...)");
		if(date.length() < 8) date = formatDate(date, "");
		int day = -1;
		String dayString = "0";
		try {
			dayString = date.substring(2, 4);
		} catch (StringIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		day = Integer.parseInt(dayString);
		return day;
	}
}
