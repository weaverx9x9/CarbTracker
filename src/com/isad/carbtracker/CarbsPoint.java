package com.isad.carbtracker;
import java.util.Date;


public class CarbsPoint implements Comparable<CarbsPoint> {
	private Date mDate;
	private Double mCarbs;
	
	public CarbsPoint(Date date, double carbs) {
		this.mDate = date;
		this.mCarbs = carbs;
	}
	
	public Date getDate() {
		return mDate;
	}
	
	public double getCarbs() {
		return mCarbs;
	}
	
	@Override
	public int compareTo(CarbsPoint another) {
		return mCarbs.compareTo(another.getCarbs());
	}
}
